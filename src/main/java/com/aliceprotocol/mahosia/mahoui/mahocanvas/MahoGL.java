package com.aliceprotocol.mahosia.mahoui.mahocanvas;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * MahoCanvas的所辖的实际渲染器。基于OpenGL 4.6，使用Core Profile。
 * <p>
 *     渲染循环:
 *     <p>
 *         初始化 -> 循环[提交命令 -> 渲染 -> 提交到Fx窗口] -> 清理
 *     </p>
 *     <p>
 *         渲染循环在额外的线程运行，与Fx窗口线程隔离，主线程/Fx窗口线程使用submit()方法提交到渲染侧。
 *     </p>
 * </p>
 */
public class MahoGL {
    private final Consumer<Image> frameConsumer;
    private final Consumer<String> shaderErrConsumer;
    private final BlockingQueue<Runnable> cmdQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean isRun = new AtomicBoolean(false);

    private Thread renderThread;
    private long windowPtr;

    private int frameBuffer;
    private int frameBufferTex;
    private int fbWidth = 1;
    private int fbHeight = 1;
    private int cvWidth = 1;
    private int cvHeight = 1;

    private int vao, vbo, ebo;

    private ShaderProgram shaderProgram;
    private final Map<Integer, Integer> tex = new HashMap<>();
    private final Map<String, IUniformValue> uniforms = new HashMap<>();

    private int nextTexId = 1;
    private long startNano;
    private int frameIdx;

    public MahoGL(Consumer<Image> frameConsumer, Consumer<String> shaderErrConsumer) {
        this.frameConsumer = frameConsumer;
        this.shaderErrConsumer = shaderErrConsumer;
    }

    public void start() {
        if (isRun.getAndSet(true)) {
            return;
        }

        renderThread = new Thread(this::renderLoop, "MahoGL");
        renderThread.setDaemon(true);
        renderThread.start();
    }

    public void stop() {
        isRun.set(false);
    }

    public void dispose() {
        stop();

        if (renderThread != null) {
            try {
                renderThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void resize(int fbWidth, int fbHeight, int cvWidth, int cvHeight) {
        submit(() -> {
            this.fbWidth = Math.max(1, fbWidth);
            this.fbHeight = Math.max(1, fbHeight);
            this.cvWidth = Math.max(1, cvWidth);
            this.cvHeight = Math.max(1, cvHeight);
            recreateFb();
        });
    }

    public int texLoad(Path path) {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        submit(() -> {
            try {
                int tex = loadTexToGl(path);
                int texId = nextTexId++;

                this.tex.put(texId, tex);
                future.complete(texId);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future.join();
    }

    public void texRemove(int texId) {
        submit(() -> {
            Integer tex = this.tex.remove(texId);
            if (tex != null) {
                glDeleteTextures(tex);
            }
        });
    }

    public void setShader(String vert, String frag) {
        submit(() -> {
            try {
                ShaderProgram sp = ShaderProgram.create(vert, frag);
                if (shaderProgram != null) {
                    shaderProgram.dispose();
                }

                shaderProgram = sp;
                shaderErrConsumer.accept("");
            } catch (Exception e) {
                shaderErrConsumer.accept(e.getMessage());
            }
        });
    }

    public void setUniform(String id, float v) {
        submit(() -> uniforms.put(id, new Uniform.FloatUniform(v)));
    }

    public void setUniform(String id, float x, float y) {
        submit(() -> uniforms.put(id, new Uniform.Vec2Uniform(x, y)));
    }

    public void setUniform(String id, float x, float y, float z) {
        submit(() -> uniforms.put(id, new Uniform.Vec3Uniform(x, y, z)));
    }

    public void setUniform(String id, float x, float y, float z, float w) {
        submit(() -> uniforms.put(id, new Uniform.Vec4Uniform(x, y, z, w)));
    }

    public void setUniform(String id, int tex) {
        submit(() -> uniforms.put(id, new Uniform.TexUniform(tex)));
    }

    private void renderLoop() {
        try {
            init();
            startNano = System.nanoTime();

            while (isRun.get()) {
                drainCmd();
                renderFrame();
                sendFrameToFx();

                frameIdx++;

                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (Exception e) {
            shaderErrConsumer.accept(e.getMessage());
        } finally {
            cleanUp();
            isRun.set(false);
        }
    }

    private void drainCmd() {
        Runnable cmd;

        while ((cmd = cmdQueue.poll()) != null) {
            cmd.run();
        }
    }

    private void init() throws IOException {
        if (!glfwInit()) {
            throw new IllegalStateException("[MahoGL] Failed to init GLFW.");
        }

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        windowPtr = glfwCreateWindow(1, 1, "MahoGL Content", NULL, NULL);

        if (windowPtr == NULL) {
            throw new IllegalStateException("[MahoGL] Failed to create content window.");
        }

        glfwMakeContextCurrent(windowPtr);
        GL.createCapabilities();

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);

        testRendering();
        recreateFb();

        shaderProgram = ShaderProgram.create(
                getDefaultShader("/com.aliceprotocol.mahosia/mahoui/mahocanvas/defaultVert.glsl"),
                getDefaultShader("/com.aliceprotocol.mahosia/mahoui/mahocanvas/defaultFrag.glsl"));
    }

    private String getDefaultShader(String path) throws IOException {
        var url = MahoGL.class.getResource(path);
        if (url == null) {
            throw new IOException("[MahoGL] Default VertShader not found: " + path);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
            return content.toString();
        }
    }

    private void testRendering() {
        float[] vertices = {
                -1f, -1f, 0f, 0f,
                1f, -1f, 1f, 0f,
                1f, 1f, 1f, 1f,
                -1f, 1f, 0f, 1f
        };

        int[] indices = { 0, 1, 2, 2, 3, 0 };

        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);

        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2L * Float.BYTES);

        glBindVertexArray(0);
    }

    private void recreateFb() {
        if (frameBufferTex != 0) {
            glDeleteTextures(frameBufferTex);
            frameBufferTex = 0;
        }

        if (frameBuffer != 0) {
            glDeleteFramebuffers(frameBuffer);
            frameBuffer = 0;
        }

        frameBufferTex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, frameBufferTex);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, fbWidth, fbHeight,
                0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer)null);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        frameBuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, frameBufferTex, 0);

        int res = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (res != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("[MahoGL] Frame Buffer is incompleted: " + res);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void renderFrame() {
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        glViewport(0, 0, fbWidth, fbHeight);

        glClearColor(0.22f, 0.22f, 0.22f, 1f);
        glClear(GL_COLOR_BUFFER_BIT);

        if (shaderProgram != null) {
            shaderProgram.use();

            float t = (System.nanoTime() - startNano) / 1_000_000_000.0f;

            shaderProgram.setUniform("uTime", t);
            shaderProgram.setUniform("uFrame", (float)frameIdx);
            shaderProgram.setUniform("uRes", fbWidth, fbHeight);
            applyUniform();

            glBindVertexArray(vao);
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glFlush();
    }

    private void applyUniform() {
        int texUnit = 0;

        for (Map.Entry<String, IUniformValue> entry : uniforms.entrySet()) {
            String id = entry.getKey();
            IUniformValue v = entry.getValue();

            if (v instanceof Uniform.FloatUniform) {
                Uniform.FloatUniform u = (Uniform.FloatUniform)v;
                shaderProgram.setUniform(id, u.value);
            } else if (v instanceof Uniform.Vec2Uniform) {
                Uniform.Vec2Uniform u = (Uniform.Vec2Uniform)v;
                shaderProgram.setUniform(id, u.x, u.y);
            } else if (v instanceof Uniform.Vec3Uniform) {
                Uniform.Vec3Uniform u = (Uniform.Vec3Uniform)v;
                shaderProgram.setUniform(id, u.x, u.y, u.z);
            } else if (v instanceof Uniform.Vec4Uniform) {
                Uniform.Vec4Uniform u = (Uniform.Vec4Uniform)v;
                shaderProgram.setUniform(id, u.x, u.y, u.z, u.w);
            } else if (v instanceof Uniform.TexUniform) {
                Uniform.TexUniform u = (Uniform.TexUniform)v;
                Integer glTex = tex.get(u.texId);

                if (glTex != null) {
                    glActiveTexture(GL_TEXTURE0 + texUnit);
                    glBindTexture(GL_TEXTURE_2D, glTex);
                    shaderProgram.setUniform(id, texUnit);
                    texUnit++;
                }
            }
        }
    }

    private void sendFrameToFx() {
        ByteBuffer rgba = BufferUtils.createByteBuffer(fbWidth * fbHeight * 4);
        byte[] bgra = new byte[fbWidth * fbHeight * 4];

        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        glReadBuffer(GL_COLOR_ATTACHMENT0);
        glReadPixels(0, 0, fbWidth, fbHeight, GL_RGBA, GL_UNSIGNED_BYTE, rgba);

        for (int y = 0; y < fbHeight; y++) {
            int srcY = fbHeight - 1 - y;
            for (int x = 0; x < fbWidth; x++) {
                int src = (srcY * fbWidth + x) * 4;
                int tar = (y * fbWidth + x) * 4;

                byte r = rgba.get(src);
                byte g = rgba.get(src + 1);
                byte b = rgba.get(src + 2);
                byte a = rgba.get(src + 3);

                bgra[tar] = b;
                bgra[tar + 1] = g;
                bgra[tar + 2] = r;
                bgra[tar + 3] = a;
            }
        }

        Platform.runLater(() -> {
            WritableImage img = new WritableImage(fbWidth, fbHeight);
            img.getPixelWriter().setPixels(0, 0, fbWidth, fbHeight, PixelFormat.getByteBgraInstance(),
                    bgra, 0, fbWidth * 4);
            frameConsumer.accept(img);
        });
    }

    private int loadTexToGl(Path path) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h  = stack.mallocInt(1);
            IntBuffer c = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(false);

            ByteBuffer p = STBImage.stbi_load(path.toAbsolutePath().toString(), w, h, c, 4);

            if (p == null) {
                throw new IllegalStateException("[MahoGL] Failed to load Tex: " + STBImage.stbi_failure_reason());
            }

            int tex = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, tex);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, w.get(0), h.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, p);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            glBindTexture(GL_TEXTURE_2D, 0);
            STBImage.stbi_image_free(p);

            return tex;
        }
    }

    private void cleanUp() {
        for (Integer tex: this.tex.values()) {
            glDeleteTextures(tex);
        }

        tex.clear();
        uniforms.clear();

        if (shaderProgram != null) {
            shaderProgram.dispose();
            shaderProgram = null;
        }

        if (frameBufferTex != 0) {
            glDeleteTextures(frameBufferTex);
            frameBufferTex = 0;
        }

        if (frameBuffer != 0) {
            glDeleteFramebuffers(frameBuffer);
        }

        if (ebo != 0) {
            glDeleteBuffers(ebo);
            ebo = 0;
        }

        if (vbo != 0) {
            glDeleteBuffers(vbo);
            vbo = 0;
        }

        if (vao != 0) {
            glDeleteVertexArrays(vao);
            vao = 0;
        }

        if (windowPtr != NULL) {
            glfwDestroyWindow(windowPtr);
            windowPtr = NULL;
        }

        glfwTerminate();
    }

    private void submit(Runnable cmd) {
        if (!isRun.get()) {
            return;
        }
        cmdQueue.offer(cmd);
    }
}

package com.aliceprotocol.mahosia.mahoui.mahocanvas;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import java.nio.file.Path;

/**
 * Mahosia的主要绘制控件，由LWJGL驱动。
 * <p>
 *     主要API:
 *     <p>
 *         setUniform() - 系列方法。更改当前着色器的暴露变量，类似C-BUFFER in Unity
 *     </p>
 *     <p>
 *         tex??() - 系列方法。加载纹理到渲染器从渲染器移除纹理
 *     </p>
 * </p>
 *
 */
public class MahoCanvas extends StackPane {

    private final Canvas canvas;
    private final ReadOnlyStringWrapper shaderError = new ReadOnlyStringWrapper("");

    private final MahoGL renderer;

    private boolean isStarted;

    public MahoCanvas() {
        this.canvas = new Canvas();
        getChildren().add(canvas);

        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());

        widthProperty().addListener(((observable,
                                      old,
                                      recent) -> requestResize()));
        heightProperty().addListener(((observable,
                                      old,
                                      recent) -> requestResize()));
        sceneProperty().addListener((observable, old, recent) -> {
            if (recent != null) {
                start();
            } else {
                stop();
            }
        });

        this.renderer = new MahoGL(this::presentFrame, this::onShaderErr);
    }

    public void start() {
        if (isStarted) {
            return;
        }

        isStarted = true;

        renderer.start();
        requestResize();
    }

    public void stop() {
        if (!isStarted) {
            return;
        }

        isStarted = false;
        renderer.stop();
    }

    public void dispose() {
        stop();
        renderer.dispose();
    }

    public int textureLoad(Path path) {
        if (!isStarted) {
            throw new IllegalStateException("[MahoCanvas] Cannot load texture before renderer is started.");
        }
        return renderer.texLoad(path);
    }

    public void textureRemove(int texId) {
        renderer.texRemove(texId);
    }

    public void setShader(String vert, String frag) {
        shaderError.set("");
        renderer.setShader(vert, frag);
    }

    public void setUniform(String id, float value) {
        renderer.setUniform(id, value);
    }

    public void setUniform(String id, float x, float y) {
        renderer.setUniform(id, x, y);
    }

    public void setUniform(String id, float x, float y, float z) {
        renderer.setUniform(id, x, y, z);
    }

    public void setUniform(String id, float x, float y, float z, float w) {
        renderer.setUniform(id, x, y, z, w);
    }

    public void setUniform(String id, int texId) {
        renderer.setUniform(id, texId);
    }

    public ReadOnlyStringProperty shaderErrProperty() {
        return shaderError.getReadOnlyProperty();
    }

    public String getShaderErr() {
        return shaderError.get();
    }

    private void requestResize() {
        if (!isStarted) {
            return;
        }

        var w = Math.max(0, (int)Math.ceil(canvas.getWidth()));
        var h = Math.max(0, (int)Math.ceil(canvas.getHeight()));

        if (w <= 0 || h <= 0) {
            return;
        }

        double sX = 1.0;
        double sY = 1.0;

        if (getScene() != null && getScene().getWindow() != null) {
            sX = getScene().getWindow().getOutputScaleX();
            sY = getScene().getWindow().getOutputScaleY();
        }

        int fbw = Math.max(1, (int)Math.ceil(w * sX));
        int fbh = Math.max(1, (int)Math.ceil(h * sY));

        renderer.resize(fbw, fbh, w, h);
    }

    private void presentFrame(Image image) {
        if (image == null) {
            return;
        }

        if (Platform.isFxApplicationThread()) {
            drawFrame(image);
        } else {
            Platform.runLater(() -> drawFrame(image));
        }
    }

    private void drawFrame(Image image) {
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.getGraphicsContext2D().drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void onShaderErr(String msg) {
        String text = msg == null ? "" : msg;

        if (!text.isEmpty()) {
            System.err.println(text);
        }

        if (Platform.isFxApplicationThread()) {
            shaderError.set(msg == null ? "" : msg);
        } else {
            Platform.runLater(() -> shaderError.set(msg == null ? "" : msg));
        }
    }
}

package com.aliceprotocol.mahosia.mahoui.mahocanvas;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL46C.*;

/**
 * MahoCanvas所所辖渲染器的公共着色器暴露类，用于调整着色器的公开（Uniform）变量。
 * <p>
 *     主要功能:
 *     <p>
 *         setUniform() - 系列方法。MahoCanvas的实际uniform设置系列方法底层实现，直接与Renderer中的SP对象交互。
 *     </p>
 * </p>
 */

public class ShaderProgram
{
    private final int id;
    private final Map<String, Integer> uniLocations = new HashMap<>();

    public ShaderProgram(int id) {
        this.id = id;
    }

    public static ShaderProgram create(String vert, String frag ) {
        int vs = compile(GL_VERTEX_SHADER, vert);
        int fs = compile(GL_FRAGMENT_SHADER, frag);

        int pr = glCreateProgram();

        glAttachShader(pr, vs);
        glAttachShader(pr, fs);
        glLinkProgram(pr);

        if (glGetProgrami(pr, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(pr);
            glDeleteShader(vs);
            glDeleteShader(fs);
            glDeleteProgram(pr);
            throw new IllegalStateException("[MahoRenderer - SP] Shader link failed:\n" + log);
        }

        glDeleteShader(vs);
        glDeleteShader(fs);
        return new ShaderProgram(pr);
    }

    public static int compile(int type, String src) {
        int shader = glCreateShader(type);
        glShaderSource(shader, src);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            glDeleteShader(shader);
            throw new IllegalStateException("[MahoRenderer - SP] Shader compile failed:\n" + log);
        }

        return shader;
    }

    public void use() {
        glUseProgram(id);
    }

    public void setUniform(String id, float value) {
        int loc = uniLocations(id);
        if (loc >= 0) {
            glUniform1f(loc, value);
        }
    }

    public void setUniform(String id, int value) {
        int loc = uniLocations(id);
        if (loc >= 0) {
            glUniform1i(loc, value);
        }
    }

    public void setUniform(String id, float x, float y) {
        int loc = uniLocations(id);
        if (loc >= 0) {
            glUniform2f(loc, x, y);
        }
    }

    public void setUniform(String id, float x, float y, float z) {
        int loc = uniLocations(id);
        if (loc >= 0) {
            glUniform3f(loc, x, y ,z);
        }
    }

    public void setUniform(String id, float x, float y, float z, float w) {
        int loc = uniLocations(id);
        if (loc >= 0) {
            glUniform4f(loc, x, y, z, w);
        }
    }

    public int uniLocations(String id) {
        Integer cache = uniLocations.get(id);

        if (cache != null) {
            return cache;
        }

        int loc = glGetUniformLocation(this.id, id);
        uniLocations.put(id, loc);
        return loc;
    }

    public void dispose() {
        glDeleteProgram(this.id);
    }
}

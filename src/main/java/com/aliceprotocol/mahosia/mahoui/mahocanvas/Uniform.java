package com.aliceprotocol.mahosia.mahoui.mahocanvas;

public class Uniform {
    public static final class FloatUniform implements IUniformValue {
        public final float value;

        public FloatUniform(float value) {
            this.value = value;
        }
    }

    public static final class Vec2Uniform implements IUniformValue {
        public final float x, y;

        public Vec2Uniform(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public static final class Vec3Uniform implements IUniformValue {
        public final float x, y, z;

        public Vec3Uniform(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static final class Vec4Uniform implements IUniformValue {
        public final float x, y, z, w;

        public Vec4Uniform(float x, float y, float z, float w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }
    }

    public static final class TexUniform implements IUniformValue {
        public final int texId;

        public TexUniform(int texId) {
            this.texId = texId;
        }
    }
}

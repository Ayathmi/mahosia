package com.aliceprotocol.mahosia.mahoui.mahocanvas;

import static org.lwjgl.opengl.GL46C.*;

public class BlendConfig {
    public final boolean enabled;
    public final int srcFactor;
    public final int dstFactor;
    public final int equation;

    public BlendConfig(boolean enabled, int src, int dst, int equ) {
        this.enabled = enabled;
        this.srcFactor = src;
        this.dstFactor = dst;
        this.equation = equ;
    }

    public static BlendConfig disabled() {
        return new BlendConfig(false, 0, 0, 0);
    }

    public static BlendConfig alpha() {
        return new BlendConfig(true, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_FUNC_ADD);
    }

    public static BlendConfig additive() {
        return new BlendConfig(true, GL_ONE, GL_ONE, GL_FUNC_ADD);
    }

    public static BlendConfig multiply() {
        return new BlendConfig(true, GL_DST_COLOR, GL_ZERO, GL_FUNC_ADD);
    }

    public static BlendConfig screen() {
        return new BlendConfig(true, GL_ONE, GL_ONE_MINUS_SRC_COLOR, GL_FUNC_ADD);
    }

    public static BlendConfig subtract() {
        return new BlendConfig(true, GL_ONE, GL_ONE, GL_FUNC_SUBTRACT);
    }

    public static BlendConfig subtractReverse() {
        return new BlendConfig(true, GL_ONE, GL_ONE, GL_FUNC_REVERSE_SUBTRACT);
    }

    public static BlendConfig custom(int srcFactor, int dstFactor, int equation) {
        return new BlendConfig(true, srcFactor, dstFactor, equation);
    }
}

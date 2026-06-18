package com.aliceprotocol.mahosia.mahoui.mahocanvas;

public class PassConfig {
    ShaderProgram program;
    BlendConfig blend;

    PassConfig(ShaderProgram sp) {
        this.program = sp;
        this.blend = BlendConfig.disabled();
    }

    void setBlend(BlendConfig bl) {
        this.blend = bl;
    }
}

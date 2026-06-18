package com.aliceprotocol.mahosia.mahomodel;

public enum UniformType {
    FLOAT(1), VEC2(2), VEC3(3), VEC4(4), TEX(1);

    public final int componentCount;

    UniformType(int c) {
        this.componentCount = c;
    }
}

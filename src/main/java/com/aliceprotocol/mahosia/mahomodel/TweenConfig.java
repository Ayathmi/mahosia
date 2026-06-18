package com.aliceprotocol.mahosia.mahomodel;

public class TweenConfig {
    private final String uniformId;
    private final int componentIdx;
    private final float startValue;
    private final float endValue;
    private final float duration;
    private final TweenMode mode;

    public TweenConfig(String id, int idx, float start, float end, float duration, TweenMode mode) {
        this.uniformId = id;
        this.componentIdx = idx;
        this.startValue = start;
        this.endValue = end;
        this.duration = duration;
        this.mode = mode;
    }

    public String getUniformId() {
        return uniformId;
    }

    public int getComponentIdx() {
        return componentIdx;
    }

    public float getStartValue() {
        return startValue;
    }

    public float getEndValue() {
        return endValue;
    }

    public float getDuration() {
        return duration;
    }

    public TweenMode getMode() {
        return mode;
    }
}

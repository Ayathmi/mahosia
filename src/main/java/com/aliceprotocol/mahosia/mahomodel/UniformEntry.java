package com.aliceprotocol.mahosia.mahomodel;

public class UniformEntry {
    private final String id;
    private final UniformType type;
    private final float[] values;

    public UniformEntry(String id, UniformType type, float... values) {
        this.id = id;
        this.type = type;
        this.values = values;
    }

    public String getId() {
        return id;
    }

    public UniformType getType() {
        return type;
    }

    public float[] getValues() {
        return values.clone();
    }

    public void setValue(int i, float v) {
        values[i] = v;
    }

    public float getValue(int i) {
        return values[i];
    }

    @Override
    public String toString() {
        return id + " (" + type + ")";
    }
}

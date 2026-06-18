package com.aliceprotocol.mahosia.mahomodel;

public class ShaderPassEntry {
    private final String label;
    private String vertSrc;
    private String fragSrc;

    public ShaderPassEntry(String label, String vertSrc, String fragSrc) {
        this.label = label;
        this.vertSrc = vertSrc;
        this.fragSrc = fragSrc;
    }

    public String getLabel() { return label; }
    public String getVertSrc() { return vertSrc; }
    public String getFragSrc() { return fragSrc; }

    @Override
    public String toString() { return label; }
}
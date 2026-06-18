package com.aliceprotocol.mahosia.mahomodel;

import java.nio.file.Path;

public class TextureEntry {
    private final int texId;
    private final Path path;
    private final String display;

    public TextureEntry(int id, Path path) {
        this.texId = id;
        this.path = path;
        this.display = path.getFileName().toString();
    }

    public int getTexId() {
        return texId;
    }

    public Path getPath() {
        return path;
    }

    public String getDisplay() {
        return display;
    }

    @Override
    public String toString() {
        return display + " [id=" + texId + "]";
    }
}

package com.aliceprotocol.mahosia.mahovm;

import com.aliceprotocol.mahosia.mahomodel.*;
import com.aliceprotocol.mahosia.mahoui.mahocanvas.MahoCanvas;
import javafx.beans.property.*;
import javafx.collections.*;

import java.nio.file.Path;

public class MahosiaVM {
    private final MahoCanvas canvas;

    private final ObservableList<TextureEntry> textures = FXCollections.observableArrayList();
    private final ObservableList<ShaderPassEntry> passes = FXCollections.observableArrayList();
    private final ObservableList<UniformEntry> uniforms = FXCollections.observableArrayList();

    private final ReadOnlyStringWrapper shaderErr = new ReadOnlyStringWrapper();

    public MahosiaVM(MahoCanvas canvas) {
        this.canvas = canvas;
        shaderErr.bind(canvas.shaderErrProperty());
    }

    public ObservableList<TextureEntry> getTextures() {
        return textures;
    }

    public TextureEntry loadTexture(Path path) {
        int id = canvas.textureLoad(path);
        TextureEntry entry = new TextureEntry(id, path);
        textures.add(entry);
        return entry;
    }

    public void removeTexture(TextureEntry entry) {
        canvas.textureRemove(entry.getTexId());
        textures.remove(entry);
    }

    public ObservableList<ShaderPassEntry> getPasses() {
        return passes;
    }

    public void addPass(String vert, String frag, String label) {
        int idx = passes.size();
        canvas.setPass(idx, vert, frag);
        passes.add(new ShaderPassEntry(label, vert, frag));
    }

    public void removePass(int idx) {
        canvas.removePass(idx);
        passes.remove(idx);
    }

    public void movePassUp(int idx) {
        if (idx <= 0 || idx >= passes.size()) {
            return;
        }
        swapAndRebuildPasses(idx, idx - 1);
    }

    public void movePassDown(int index) {
        if (index < 0 || index >= passes.size() - 1) return;
        swapAndRebuildPasses(index, index + 1);
    }

    private void swapAndRebuildPasses(int a, int b) {
        java.util.Collections.swap(passes, a, b);
        canvas.clearPasses();
        for (int i = 0; i < passes.size(); i++) {
            ShaderPassEntry p = passes.get(i);
            canvas.setPass(i, p.getVertSrc(), p.getFragSrc());
        }
    }

    public ObservableList<UniformEntry> getUniforms() { return uniforms; }

    public void addUniform(String name, UniformType type, float... values) {
        UniformEntry entry = new UniformEntry(name, type, values);
        uniforms.add(entry);
        pushUniform(entry);
    }

    public void updateUniform(UniformEntry entry) {
        pushUniform(entry);
    }

    private void pushUniform(UniformEntry e) {
        float[] v = e.getValues();
        switch (e.getType()) {
            case FLOAT: canvas.setUniform(e.getId(), v[0]); break;
            case VEC2:  canvas.setUniform(e.getId(), v[0], v[1]); break;
            case VEC3:  canvas.setUniform(e.getId(), v[0], v[1], v[2]); break;
            case VEC4:  canvas.setUniform(e.getId(), v[0], v[1], v[2], v[3]); break;
            case TEX:   canvas.setUniform(e.getId(), (int)v[0]); break;
        }
    }

    public ReadOnlyStringProperty shaderErrorProperty() {
        return shaderErr.getReadOnlyProperty();
    }
}

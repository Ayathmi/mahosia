package com.aliceprotocol.mahosia.mahoapp;

import com.aliceprotocol.mahosia.mahoui.mahocanvas.MahoCanvas;
import javafx.fxml.FXML;
import javafx.scene.control.Control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MahosiaController {
    @FXML
    public MahoCanvas mahoCanvas;

    private int texId;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "MahoController");
        t.setDaemon(true);
        return t;
    });

    @FXML
    private void initialize() throws InterruptedException {
        mahoCanvas.rendererReadyProperty().addListener((obs, was, isReady) -> {
            if (!isReady) {
                return;
            }
            setupScene();
        });

    }

    private void setupScene() {
        try {
            String vert = loadShader("/com.aliceprotocol.mahosia/mahoui/mahocanvas/crtVert.glsl");
            String frag = loadShader("/com.aliceprotocol.mahosia/mahoui/mahocanvas/crtFrag.glsl");
            mahoCanvas.setShader(vert, frag);

            URL texUrl = getClass().getResource("/com.aliceprotocol.mahosia/mahoasset/fallback_test_tex.png");
            if (texUrl == null) {
                throw new IOException("[MahoCanvas - Controller] Test texture not found in resources.");
            }
            Path tmp = Files.createTempFile("mahosia_test_", ".png");
            Files.copy(texUrl.openStream(), tmp, StandardCopyOption.REPLACE_EXISTING);
            texId = mahoCanvas.textureLoad(tmp);

            mahoCanvas.setUniform("uTex", texId);

            mahoCanvas.setUniform("uFlashAmount", 0f);
            mahoCanvas.setUniform("uFlashSize", 0f);
            mahoCanvas.setUniform("uFlashSpeed", 0f);
            mahoCanvas.setUniform("uVignetteAmount", 0f);
            mahoCanvas.setUniform("uVignetteFalloff", 0f);
            mahoCanvas.setUniform("uTearAmount", 0f);
            mahoCanvas.setUniform("uTearFrequency", 0f);
            mahoCanvas.setUniform("uDistortAmount", 0f);
            mahoCanvas.setUniform("uNoiseAmount", 0f);
            mahoCanvas.setUniform("uScanlineAmount", 0f);
            mahoCanvas.setUniform("uChromaAmount", 0f);

            scheduler.scheduleAtFixedRate(this::pushAllUniforms, 0, 5 , TimeUnit.SECONDS);
        } catch (Exception e) {
            mahoCanvas.onShaderErr(e);
        }
    }

    private void pushAllUniforms(){
        mahoCanvas.setUniform("uTex", texId);
        mahoCanvas.setUniform("uFlashAmount", 0.12f);
        mahoCanvas.setUniform("uFlashSize", 0.0025f);
        mahoCanvas.setUniform("uFlashSpeed", 5.0f);
        mahoCanvas.setUniform("uVignetteAmount", 0.35f);
        mahoCanvas.setUniform("uVignetteFalloff", 0.6f);
        mahoCanvas.setUniform("uTearAmount", 0.035f);
        mahoCanvas.setUniform("uTearFrequency", 0.06f);
        mahoCanvas.setUniform("uDistortAmount", 0.04f);
        mahoCanvas.setUniform("uNoiseAmount", 0.015f);
        mahoCanvas.setUniform("uScanlineAmount", 5f);
        mahoCanvas.setUniform("uChromaAmount", 0.008f);
    }

    public String loadShader(String path) throws IOException {
        var url = getClass().getResource(path);
        if (url == null) {
            throw new IOException("[MahoCanvas - Controller] Shader not found: " + path);
        }

        try (var reader = new BufferedReader(
                new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            var builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            return builder.toString();
        }
    }
}

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

    private int uniformCycleIndex = 0;

    private static final int UNIFORM_CYCLE_COUNT = 15;

    private void setupScene() {
        try {
            String vert = loadShader("/com.aliceprotocol.mahosia/mahoui/mahocanvas/glitchshader/pass.vert.glsl");

            mahoCanvas.setPass(0, vert, loadShader("/com.aliceprotocol.mahosia/mahoui/mahocanvas/glitchshader/pass_0_copy.frag.glsl"));
            mahoCanvas.setPass(1, vert, loadShader("/com.aliceprotocol.mahosia/mahoui/mahocanvas/glitchshader/pass_1_distort.frag.glsl"));
            mahoCanvas.setPass(2, vert, loadShader("/com.aliceprotocol.mahosia/mahoui/mahocanvas/glitchshader/pass_2_tear.frag.glsl"));
            mahoCanvas.setPass(3, vert, loadShader("/com.aliceprotocol.mahosia/mahoui/mahocanvas/glitchshader/pass_3_mix.frag.glsl"));
            mahoCanvas.setPass(4, vert, loadShader("/com.aliceprotocol.mahosia/mahoui/mahocanvas/glitchshader/pass_4_chroma.frag.glsl"));
            mahoCanvas.setPass(5, vert, loadShader("/com.aliceprotocol.mahosia/mahoui/mahocanvas/glitchshader/pass_5_flash.frag.glsl"));
            mahoCanvas.setPass(6, vert, loadShader("/com.aliceprotocol.mahosia/mahoui/mahocanvas/glitchshader/pass_6_scanline.frag.glsl"));
            mahoCanvas.setPass(7, vert, loadShader("/com.aliceprotocol.mahosia/mahoui/mahocanvas/glitchshader/pass_7_noise_vignette.frag.glsl"));

            URL texUrl = getClass().getResource("/com.aliceprotocol.mahosia/mahoasset/fallback_test_tex.png");
            if (texUrl == null) throw new IOException("[MahoCanvas - Controller]Test texture not found.");
            Path tmp = Files.createTempFile("mahosia_test_", ".png");
            Files.copy(texUrl.openStream(), tmp, StandardCopyOption.REPLACE_EXISTING);
            texId = mahoCanvas.textureLoad(tmp);
            mahoCanvas.setUniform("uTex", texId);

            mahoCanvas.setUniform("uDistortAmount",0f);
            mahoCanvas.setUniform("uTearAmount", 0f);
            mahoCanvas.setUniform("uTearFrequency", 0f);
            mahoCanvas.setUniform("uMixWeight0", 0.45f);
            mahoCanvas.setUniform("uMixWeight1", 0.25f);
            mahoCanvas.setUniform("uMixWeight2", 0.3f);
            mahoCanvas.setUniform("uChromaAmount", 0f);
            mahoCanvas.setUniform("uFlashAmount", 0f);
            mahoCanvas.setUniform("uFlashSize", 0f);
            mahoCanvas.setUniform("uFlashSpeed", 0f);
            mahoCanvas.setUniform("uScanlineAmount", 0f);
            mahoCanvas.setUniform("uNoiseAmount", 0f);
            mahoCanvas.setUniform("uVignetteAmount", 0.033f);
            mahoCanvas.setUniform("uVignetteFalloff", 0.128f);

            scheduler.scheduleAtFixedRate(this::pushAllUniforms, 5, 2, TimeUnit.SECONDS);

        } catch (Exception e) {
            mahoCanvas.onShaderErr(e);
        }
    }

    private void pushAllUniforms() {
        var r = new Random();

        switch (uniformCycleIndex) {
            case 0: mahoCanvas.setUniform("uTex", texId); break;
            case 1:  mahoCanvas.setUniform("uDistortAmount", 0.05f + r.nextFloat() * 0.25f); break;
            case 2:  mahoCanvas.setUniform("uTearAmount", 0.01f + r.nextFloat() * 0.10f); break;
            case 3:  mahoCanvas.setUniform("uTearFrequency", 0.05f + r.nextFloat() * 0.50f); break;
            case 4:  mahoCanvas.setUniform("uMixWeight0", 0.15f + r.nextFloat() * 0.35f); break;
            case 5:  mahoCanvas.setUniform("uMixWeight1", 0.15f + r.nextFloat() * 0.35f); break;
            case 6:  mahoCanvas.setUniform("uMixWeight2", 0.15f + r.nextFloat() * 0.35f); break;
            case 7:  mahoCanvas.setUniform("uChromaAmount", 0.0005f + r.nextFloat() * 0.006f); break;
            case 8:  mahoCanvas.setUniform("uFlashAmount", 0.10f + r.nextFloat() * 0.60f); break;
            case 9:  mahoCanvas.setUniform("uFlashSize", 0.03f + r.nextFloat() * 0.20f); break;
            case 10:  mahoCanvas.setUniform("uFlashSpeed", 2.0f + r.nextFloat() * 12.0f); break;
            case 11: mahoCanvas.setUniform("uScanlineAmount", 0.20f + r.nextFloat() * 0.60f); break;
            case 12: mahoCanvas.setUniform("uNoiseAmount", 0.01f + r.nextFloat() * 0.10f); break;
            case 13: mahoCanvas.setUniform("uVignetteAmount", 0.30f + r.nextFloat() * 0.60f); break;
            case 14: mahoCanvas.setUniform("uVignetteFalloff", 0.8f + r.nextFloat() * 3.0f); break;
        }

        uniformCycleIndex = (uniformCycleIndex + 1) % UNIFORM_CYCLE_COUNT;
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

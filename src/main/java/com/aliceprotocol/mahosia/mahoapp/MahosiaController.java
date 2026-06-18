package com.aliceprotocol.mahosia.mahoapp;

import com.aliceprotocol.mahosia.mahomodel.*;
import com.aliceprotocol.mahosia.mahovm.*;
import com.aliceprotocol.mahosia.mahoui.mahocanvas.MahoCanvas;
import com.aliceprotocol.mahosia.mahovm.tweening.TweenEngine;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

public class MahosiaController {

    @FXML public MahoCanvas mahoCanvas;
    @FXML public ListView<TextureEntry> textureList;
    @FXML public ListView<ShaderPassEntry> passList;
    @FXML public ListView<TweenEngine.ActiveTween> tweenList;
    @FXML public VBox uniformBox;
    @FXML public Label errorLabel;

    private MahosiaVM vm;
    private TweenEngine tweenEngine;

    @FXML
    private void initialize() {
        mahoCanvas.rendererReadyProperty().addListener((obs, was, ready) -> {
            if (!ready) return;
            vm = new MahosiaVM(mahoCanvas);
            tweenEngine = new TweenEngine(vm);
            tweenEngine.start();
            bindUI();
        });
    }

    private void bindUI() {
        textureList.setItems(vm.getTextures());
        passList.setItems(vm.getPasses());
        tweenList.setItems(tweenEngine.getActiveTweens());
        errorLabel.textProperty().bind(vm.shaderErrorProperty());

        vm.getUniforms().addListener((ListChangeListener<UniformEntry>) change -> rebuildUniformBox());
    }

    private void rebuildUniformBox() {
        uniformBox.getChildren().clear();
        for (UniformEntry entry : vm.getUniforms()) {
            uniformBox.getChildren().add(UniformEditorBuilder.build(entry, vm));
        }
    }

    @FXML
    private void onAddTexture() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Texture Image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.tga"));
        File file = fc.showOpenDialog(mahoCanvas.getScene().getWindow());
        if (file != null) {
            vm.loadTexture(file.toPath());
        }
    }

    @FXML
    private void onRemoveTexture() {
        TextureEntry sel = textureList.getSelectionModel().getSelectedItem();
        if (sel != null) vm.removeTexture(sel);
    }

    @FXML
    private void onAddPass() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("GLSL Shaders", "*.glsl", "*.frag", "*.vert"));

        fc.setTitle("Select Vertex Shader");
        File vertFile = fc.showOpenDialog(mahoCanvas.getScene().getWindow());
        if (vertFile == null) return;

        fc.setTitle("Select Fragment Shader");
        File fragFile = fc.showOpenDialog(mahoCanvas.getScene().getWindow());
        if (fragFile == null) return;

        try {
            String vertSrc = Files.readString(vertFile.toPath(), StandardCharsets.UTF_8);
            String fragSrc = Files.readString(fragFile.toPath(), StandardCharsets.UTF_8);
            String label = fragFile.getName();
            vm.addPass(vertSrc, fragSrc, label);
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onRemovePass() {
        int idx = passList.getSelectionModel().getSelectedIndex();
        if (idx >= 0) vm.removePass(idx);
    }

    @FXML
    private void onMovePassUp() {
        int idx = passList.getSelectionModel().getSelectedIndex();
        vm.movePassUp(idx);
        passList.getSelectionModel().select(idx - 1);
    }

    @FXML
    private void onMovePassDown() {
        int idx = passList.getSelectionModel().getSelectedIndex();
        vm.movePassDown(idx);
        passList.getSelectionModel().select(idx + 1);
    }

    @FXML
    private void onAddUniform() {
        Optional<UniformEntry> result = UniformAddDialog.show();
        result.ifPresent(entry -> vm.addUniform(entry.getId(), entry.getType(), entry.getValues()));
    }

    @FXML
    private void onRemoveUniform() {
        if (uniformBox.getChildren().isEmpty()) return;
        int lastIdx = vm.getUniforms().size() - 1;
        if (lastIdx >= 0) {
            vm.getUniforms().remove(lastIdx);
        }
    }

    @FXML
    private void onAddTween() {
        if (vm.getUniforms().isEmpty()) {
            errorLabel.setText("Please add at least one uniform first.");
            return;
        }

        Optional<TweenConfig> result = TweenDialog.show(vm.getUniforms());
        result.ifPresent(config -> {
            UniformEntry target = null;
            for (UniformEntry e : vm.getUniforms()) {
                if (e.getId().equals(config.getUniformId())) {
                    target = e;
                    break;
                }
            }
            if (target != null && target.getType() != UniformType.TEX) {
                tweenEngine.addTween(config, target);
            }
        });
    }

    @FXML
    private void onRemoveTween() {
        TweenEngine.ActiveTween sel = tweenList.getSelectionModel().getSelectedItem();
        if (sel != null) tweenEngine.removeTween(sel);
    }
}
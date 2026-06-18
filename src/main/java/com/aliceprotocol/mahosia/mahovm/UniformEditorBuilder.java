package com.aliceprotocol.mahosia.mahovm;

import com.aliceprotocol.mahosia.mahomodel.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class UniformEditorBuilder {

    public static HBox build(UniformEntry entry, MahosiaVM vm) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().add(new Label(entry.getId()));

        if (entry.getType() == UniformType.TEX) {
            // 纹理类型：下拉选择已加载的纹理
            ComboBox<TextureEntry> combo = new ComboBox<>(vm.getTextures());
            combo.setOnAction(e -> {
                TextureEntry sel = combo.getValue();
                if (sel != null) {
                    entry.setValue(0, sel.getTexId());
                    vm.updateUniform(entry);
                }
            });
            row.getChildren().add(combo);
        } else {
            // float/vec2/3/4：每个分量一个Slider
            String[] labels = {"X", "Y", "Z", "W"};
            for (int i = 0; i < entry.getType().componentCount; i++) {
                final int ci = i;
                Slider slider = new Slider(0, 1, entry.getValue(i));
                slider.setPrefWidth(120);
                TextField field = new TextField(String.format("%.3f", entry.getValue(i)));
                field.setPrefWidth(60);

                slider.valueProperty().addListener((obs, old, val) -> {
                    entry.setValue(ci, val.floatValue());
                    field.setText(String.format("%.3f", val.floatValue()));
                    vm.updateUniform(entry);
                });

                row.getChildren().addAll(
                        new Label(labels[i] + ":"), slider, field
                );
            }
        }

        return row;
    }
}
package com.aliceprotocol.mahosia.mahovm;

import com.aliceprotocol.mahosia.mahomodel.UniformEntry;
import com.aliceprotocol.mahosia.mahomodel.UniformType;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class UniformAddDialog {

    public static Optional<UniformEntry> show() {
        Dialog<UniformEntry> dialog = new Dialog<>();
        dialog.setTitle("Add Uniform");

        TextField nameField = new TextField("uMyUniform");
        ComboBox<UniformType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(UniformType.values());
        typeCombo.setValue(UniformType.FLOAT);

        TextField v0 = new TextField("0.0");
        TextField v1 = new TextField("0.0");
        TextField v2 = new TextField("0.0");
        TextField v3 = new TextField("0.0");
        Label v0L = new Label("X:");
        Label v1L = new Label("Y:");
        Label v2L = new Label("Z:");
        Label v3L = new Label("W:");

        Runnable updateVisibility = () -> {
            UniformType t = typeCombo.getValue();
            int c = t == UniformType.TEX ? 1 : t.componentCount;
            v0.setVisible(true);  v0L.setVisible(true);
            v1.setVisible(c >= 2); v1L.setVisible(c >= 2);
            v2.setVisible(c >= 3); v2L.setVisible(c >= 3);
            v3.setVisible(c >= 4); v3L.setVisible(c >= 4);
            if (t == UniformType.TEX) {
                v0L.setText("TexID:");
            } else {
                v0L.setText("X:");
            }
        };
        typeCombo.setOnAction(e -> updateVisibility.run());
        updateVisibility.run();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Name:"), nameField);
        grid.addRow(1, new Label("Type:"), typeCombo);
        grid.addRow(2, v0L, v0);
        grid.addRow(3, v1L, v1);
        grid.addRow(4, v2L, v2);
        grid.addRow(5, v3L, v3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) return null;
                UniformType type = typeCombo.getValue();
                float[] vals;
                switch (type) {
                    case FLOAT:
                        vals = new float[]{Float.parseFloat(v0.getText())};
                        break;
                    case VEC2:
                        vals = new float[]{Float.parseFloat(v0.getText()), Float.parseFloat(v1.getText())};
                        break;
                    case VEC3:
                        vals = new float[]{Float.parseFloat(v0.getText()), Float.parseFloat(v1.getText()), Float.parseFloat(v2.getText())};
                        break;
                    case VEC4:
                        vals = new float[]{Float.parseFloat(v0.getText()), Float.parseFloat(v1.getText()), Float.parseFloat(v2.getText()), Float.parseFloat(v3.getText())};
                        break;
                    case TEX:
                        vals = new float[]{Integer.parseInt(v0.getText())};
                        break;
                    default:
                        return null;
                }
                return new UniformEntry(name, type, vals);
            } catch (NumberFormatException ex) {
                return null;
            }
        });

        return dialog.showAndWait();
    }
}
// TweenDialog.java
package com.aliceprotocol.mahosia.mahovm;

import com.aliceprotocol.mahosia.mahomodel.*;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Optional;

public class TweenDialog {

    public static Optional<TweenConfig> show(ObservableList<UniformEntry> uniforms) {
        Dialog<TweenConfig> dialog = new Dialog<>();
        dialog.setTitle("Add Tween");

        ComboBox<UniformEntry> uniformCombo = new ComboBox<>(uniforms);
        Spinner<Integer> componentSpinner = new Spinner<>(0, 3, 0);
        TextField startField = new TextField("0.0");
        TextField endField = new TextField("1.0");
        TextField durationField = new TextField("2.0");
        ComboBox<TweenMode> modeCombo = new ComboBox<>();
        modeCombo.getItems().addAll(TweenMode.values());
        modeCombo.setValue(TweenMode.LOOP);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Uniform:"), uniformCombo);
        grid.addRow(1, new Label("Component:"), componentSpinner);
        grid.addRow(2, new Label("Start:"), startField);
        grid.addRow(3, new Label("End:"), endField);
        grid.addRow(4, new Label("Duration(s):"), durationField);
        grid.addRow(5, new Label("Mode:"), modeCombo);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && uniformCombo.getValue() != null) {
                return new TweenConfig(
                        uniformCombo.getValue().getId(),
                        componentSpinner.getValue(),
                        Float.parseFloat(startField.getText()),
                        Float.parseFloat(endField.getText()),
                        Float.parseFloat(durationField.getText()),
                        modeCombo.getValue()
                );
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
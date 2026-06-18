package com.aliceprotocol.mahosia.mahoapp;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.MalformedURLException;
import java.net.URL;


public class MahoApp {
    public MahoApp() throws MalformedURLException {
        var url = new URL("/com.aliceprotocol.mahoapp.mahoapp.fxml");
        var fxmlLoader = new FXMLLoader(url);
        var stage = new Stage();
        var scene = new Scene(fxmlLoader.getRoot());
        stage.setScene(scene);
    }
}

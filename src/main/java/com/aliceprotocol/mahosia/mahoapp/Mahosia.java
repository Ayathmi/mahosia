package com.aliceprotocol.mahosia.mahoapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.MalformedURLException;
import java.net.URL;


public class Mahosia extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        var url = getClass().getResource("/com.aliceprotocol.mahosia/mahoapp/app.fxml");
        var fxml = new FXMLLoader(url);
        var scene = new Scene(fxml.load());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
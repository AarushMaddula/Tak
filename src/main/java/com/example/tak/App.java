package com.example.tak;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static final float WIDTH = 1400;
    private static final float HEIGHT = 800;


    @Override
    public void start(Stage stage) throws IOException {
        stage.setHeight(800);
        stage.setWidth(1400);
        stage.setTitle("Tak!");

        sceneCollector.setStage(stage);
        sceneCollector.setStartingScreen();
    }


}
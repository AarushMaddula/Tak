package com.tak.game;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static final float WIDTH = 1400;
    private static final float HEIGHT = 800;


    @Override
    public void start(Stage stage) throws IOException {
        //launches the game
        stage.setHeight(800);
        stage.setWidth(1400);
        stage.setTitle("Tak!");

        sceneCollector.setStage(stage);
        sceneCollector.setStartingScreen();
    }


}
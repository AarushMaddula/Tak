package com.example.tak;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class sceneCollector {

    private static Stage stage;
    private static double WIDTH, HEIGHT;

    private static int size = 0;

    public static void setStage(Stage stage) {
        sceneCollector.stage = stage;

        WIDTH = (int) stage.getWidth();
        HEIGHT = (int) stage.getHeight();
    }

    public static void setStartingScreen() {

        SmartGroup root = new SmartGroup();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight(), true);

        Image image = new Image(Objects.requireNonNull(sceneCollector.class.getResourceAsStream("background.png")));
        ImageView iv = new ImageView(image);

        iv.prefWidth(WIDTH);
        iv.prefHeight(HEIGHT);

        Button button = new Button();

        button.setPrefWidth(250);
        button.setPrefHeight(50);

        button.setTranslateX((WIDTH - 250) / 2);
        button.setTranslateY((HEIGHT - 50) / 2);

        button.setText("Play");

        button.setOnMouseClicked(e -> {
            setSelectionScreen();
        });

        Text text = new Text("TAK!");

        text.setFont(Font.font ("Verdana", 80));
        text.setFill(Color.LIGHTYELLOW);

        text.setTranslateX((WIDTH - 200) / 2);
        text.setTranslateY((HEIGHT - 200) / 2);

        root.getChildren().addAll(iv, button, text);

        stage.setScene(scene);
        stage.show();
    }

    public static void setSelectionScreen() {
        SmartGroup root = new SmartGroup();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight(), true);

        Image image = new Image(Objects.requireNonNull(sceneCollector.class.getResourceAsStream("background.png")));
        ImageView iv = new ImageView(image);

        iv.prefWidth(WIDTH);
        iv.prefHeight(HEIGHT);

        Button button = new Button();

        button.setPrefWidth(250);
        button.setPrefHeight(50);

        button.setTranslateX((WIDTH - 250) / 2);
        button.setTranslateY((HEIGHT - 50) / 2);

        button.setText("PLAY SINGLEPLAYER");

        button.setOnMouseClicked(e -> {
            if (size == 0) return;
            GameInstance game = new GameInstance(stage, size, null);
        });

        Button aiButton = new Button();

        aiButton.setPrefWidth(250);
        aiButton.setPrefHeight(50);

        aiButton.setTranslateX((WIDTH - 250) / 2);
        aiButton.setTranslateY((HEIGHT + 150) / 2);

        aiButton.setText("PLAY AGAINST COMPUTER");

        aiButton.setOnMouseClicked(e -> {
            if (size == 0) return;
            GameInstance game = new GameInstance(stage, size, null);
        });

        Button multiButton = new Button();

        multiButton.setPrefWidth(250);
        multiButton.setPrefHeight(50);

        multiButton.setTranslateX((WIDTH - 250) / 2);
        multiButton.setTranslateY((HEIGHT + 50) / 2);

        multiButton.setText("PLAY MULTIPLAYER");

        multiButton.setOnMouseClicked(e -> {
            //if (size == 0) return;

            Client client = new Client();
            try {
                client.startConnection("127.0.0.1", 5555);
                //client.startConnection("0.tcp.ngrok.io", 16845);
            } catch (IOException | ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }

            new GameInstance(stage, 5, client);
        });

        GridPane gridpane = new GridPane();

        for (int i = 3; i < 9; i++) {
            Button sizeButton = new Button();

            gridpane.getColumnConstraints().add(new ColumnConstraints(42));
            gridpane.getRowConstraints().add(new RowConstraints(42));


            sizeButton.setOnMouseClicked(e -> {
                size = Integer.parseInt(sizeButton.getText());
            });

            sizeButton.setText(String.valueOf(i));
            gridpane.add(sizeButton, i - 3, 0);
        }

        gridpane.setTranslateX((WIDTH - 250) / 2);
        gridpane.setTranslateY((HEIGHT) / 2 + 250);


        Text text = new Text("TAK!");

        text.setFont(Font.font("Verdana", 80));
        text.setFill(Color.LIGHTYELLOW);

        text.setTranslateX((WIDTH - 200) / 2);
        text.setTranslateY((HEIGHT - 200) / 2);

        root.getChildren().addAll(iv, multiButton, button, text, aiButton, gridpane);

        stage.setScene(scene);
        stage.show();
    }
}

package com.tak.game;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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
import java.util.concurrent.atomic.AtomicInteger;

//manages the scenes
public class sceneCollector {

    private static Stage stage;
    private static int WIDTH, HEIGHT;

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
        AtomicInteger size = new AtomicInteger();

        SmartGroup root = new SmartGroup();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight(), true);

        //sets background
        Image backgroundImage = new Image(Objects.requireNonNull(sceneCollector.class.getResourceAsStream("background.png")));
        ImageView background = new ImageView(backgroundImage);

        background.prefWidth(WIDTH);
        background.prefHeight(HEIGHT);

        //makes alert text
        Alert alert = new Alert(WIDTH, HEIGHT);
        alert.setFont(Font.font("Verdana", 10));
        alert.setFill(Color.RED);
        alert.setTranslateY(HEIGHT - 50);

        //makes the server config settings
        TextField ip = new TextField();
        ip.setFont(Font.font("Verdana", 15));
        ip.setTranslateX((WIDTH - 250) / 2);
        ip.setTranslateY(HEIGHT - 220);
        ip.setPrefWidth(250);

        Text ipText = new Text("IP");
        ipText.setFont(Font.font("Verdana", 15));
        ipText.setFill(Color.WHITE);
        ipText.setTranslateX((WIDTH - 250) / 2);
        ipText.setTranslateY(HEIGHT - 225);

        TextField port = new TextField();
        port.setFont(Font.font("Verdana", 15));
        port.setTranslateX((WIDTH - 250) / 2);
        port.setTranslateY(HEIGHT - 170);
        port.setPrefWidth(250);

        Text portText = new Text("Port");
        portText.setFont(Font.font("Verdana", 15));
        portText.setFill(Color.WHITE);
        portText.setTranslateX((WIDTH - 250) / 2);
        portText.setTranslateY(HEIGHT - 175);

        //makes the title
        Text text = new Text("TAK!");

        text.setFont(Font.font("Verdana", 80));
        text.setFill(Color.LIGHTYELLOW);

        text.setTranslateX((WIDTH - 200) / 2);
        text.setTranslateY((HEIGHT - 200) / 2);

        //makes the single player button
        Button singleButton = new Button();

        singleButton.setPrefWidth(250);
        singleButton.setPrefHeight(50);

        singleButton.setTranslateX((WIDTH - 250) / 2);
        singleButton.setTranslateY((HEIGHT - 50) / 2);

        singleButton.setText("PLAY SINGLEPLAYER");

        singleButton.setOnMouseClicked(e -> {
            if (size.get() == 0) {
                alert.setAlert("Board Size Has Not Been Selected!");
                return;
            }
            GameInstance game = new GameInstance(stage, size.get(), null);
        });

        //makes the multiplayer button
        Button multiButton = new Button();

        multiButton.setPrefWidth(250);
        multiButton.setPrefHeight(50);

        multiButton.setTranslateX((WIDTH - 250) / 2);
        multiButton.setTranslateY((HEIGHT + 50) / 2);

        multiButton.setText("PLAY MULTIPLAYER");

        multiButton.setOnMouseClicked(e -> {
            Client client = new Client();

            if (Objects.equals(ip.getText(), "") || Objects.equals(port.getText(), "")) {
                alert.setAlert("Invalid Server Configuration");
                return;
            }

            try {
                client.startConnection(ip.getText(), Integer.parseInt(port.getText()));
                new GameInstance(stage, 5, client);
            } catch (IOException | ClassNotFoundException ex) {
                alert.setAlert("Error Connecting to Server");
            }
        });

        //makes the AI player button
        Button aiButton = new Button();

        aiButton.setPrefWidth(250);
        aiButton.setPrefHeight(50);

        aiButton.setTranslateX((WIDTH - 250) / 2);
        aiButton.setTranslateY((HEIGHT + 150) / 2);

        aiButton.setText("PLAY AGAINST COMPUTER");

        aiButton.setOnMouseClicked(e -> {
            alert.setAlert("Game mode Not Available");
        });

        //makes the size text & buttons
        Text sizeText = new Text("Size");
        sizeText.setFont(Font.font("Verdana", 15));
        sizeText.setFill(Color.WHITE);
        sizeText.setTranslateX((WIDTH - 250) / 2);
        sizeText.setTranslateY(HEIGHT / 2 + 300);

        GridPane gridpane = new GridPane();

        for (int i = 3; i < 9; i++) {
            Button sizeButton = new Button();

            gridpane.getColumnConstraints().add(new ColumnConstraints(42));
            gridpane.getRowConstraints().add(new RowConstraints(42));


            sizeButton.setOnMouseClicked(e -> {
                size.set(Integer.parseInt(sizeButton.getText()));
            });

            sizeButton.setText(String.valueOf(i));
            gridpane.add(sizeButton, i - 3, 0);
        }

        gridpane.setTranslateX((WIDTH - 250) / 2);
        gridpane.setTranslateY(HEIGHT / 2 + 300);

        //adds all objects to the root
        root.getChildren().addAll(background, multiButton, singleButton, text, aiButton, gridpane, alert, ip, port, ipText, portText, sizeText);

        stage.setScene(scene);
        stage.show();
    }


}

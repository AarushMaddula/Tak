package com.tak.game;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Alert extends Text {

    int WIDTH, HEIGHT;

    Alert(int WIDTH, int HEIGHT) {
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;

        this.setFont(Font.font("Verdana", 10));
        this.setFill(Color.RED);
        this.setTranslateY(HEIGHT - 50);
    }

    void setAlert(String text) {
        this.setText(text);
        this.setTranslateX((WIDTH - this.getLayoutBounds().getWidth()) / 2);
    }

    void clearAlert() {
        this.setText("");
    }
}

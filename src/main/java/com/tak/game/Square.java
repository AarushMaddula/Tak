package com.tak.game;

import javafx.scene.shape.Box;

public class Square extends Box {
    private int row, column;

    void setRow (int row) {
        this.row = row;
    }

    void setColumn (int column) {
        this.column = column;
    }

    int getRow () {
        return row;
    }

    int getColumn () {
        return column;
    }

}

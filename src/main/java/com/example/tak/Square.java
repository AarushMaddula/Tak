package com.example.tak;

import javafx.scene.shape.Box;

public class Square extends Box {
    int row, column;

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

package com.tak.game;

import java.util.Stack;

public class GameSquare extends Stack<GamePiece> {

    int row, column;

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }


}

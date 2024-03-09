package com.tak.game;

import java.io.Serializable;

public class GamePiece implements Serializable {
    PieceType type;
    Colors color;
    int order, row, column;

    int id;

    GamePiece(Colors color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setType(PieceType type) {
        this.type = type;
    }

    public PieceType getType() {
        return type;
    }

    public Colors getColor() {return color;}

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    void setRow (int row) {
        this.row = row;
    }

    int getRow() {
        return row;
    }

    void setColumn (int column) {
        this.column = column;
    }

    int getColumn() {
        return column;
    }

}

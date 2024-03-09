package com.tak.game;

import javafx.scene.shape.Box;

import java.io.Serializable;

public class BoardPiece extends Box implements Serializable {

    private int row, column, order, id;

    private Boolean onBoard;

    private Colors color;

    private PieceType type;

    private int boardPositionX, boardPositionY, boardPositionZ;

    public int getPieceId() {
        return id;
    }

    public void setPieceId(int id) {
        this.id = id;
    }

    public Boolean getOnBoard() {
        return onBoard;
    }

    public void setOnBoard(Boolean onBoard) {
        this.onBoard = onBoard;
    }

    public void setBoardPositionX(int boardPositionX) {
        this.boardPositionX = boardPositionX;
    }

    int getBoardPositionX() {
        return boardPositionX;
    }

    public int getBoardPositionY() {
        return boardPositionY;
    }

    public void setBoardPositionY(int boardPositionY) {
        this.boardPositionY = boardPositionY;
    }

    public int getBoardPositionZ() {
        return boardPositionZ;
    }

    public void setBoardPositionZ(int boardPositionZ) {
        this.boardPositionZ = boardPositionZ;
    }

    void setColor(Colors color) {this.color = color;}

    Colors getColor() {return color;}

    void setRow(int row) {
        this.row = row;
    }

    void setColumn(int column) {
        this.column = column;
    }

    int getRow() {
        return row;
    }

    int getColumn() {
        return column;
    }

    void setOrder(int order) {
        this.order = order;
    }

    int getOrder() {
        return order;
    }

    void setType(PieceType type) {
        this.type = type;
    }

    PieceType getType() {
        return type;
    }
}

package com.tak.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;

public class Player implements Serializable {
    ArrayList<GameSquare> playerPieces;
    Colors color;
    Client client;
    TakGame game;

    Player (Colors color, int size, TakGame game) {
        this.color = color;
        this.playerPieces = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            playerPieces.add(new GameSquare());
        }
        this.game = game;
    }

    public void removePiece(GamePiece piece) {
        for (GameSquare stack: playerPieces) {
            stack.remove(piece);
        }
    }

    public void setBishopPieceCount(int numPieces) {
        int size = playerPieces.size();

        for (int i = 0; i < numPieces; i++) {
            GamePiece bishop = new GamePiece(color, PieceType.BISHOP);

            bishop.setId(game.getCurrId());

            bishop.setOrder(0);
            playerPieces.get((size - 1) - i).add(bishop);
        }
    }

    public void setNormalPieceCount(int numPieces) {
        for (int i = 0; i < numPieces; i++) {
            GamePiece normal = new GamePiece(color, PieceType.FLAT);

            normal.setId(game.getCurrId());

            normal.setOrder(i % 15);
            playerPieces.get(i / 15).add(normal);
        }
    }

    public ArrayList<GameSquare> getPlayerPieces() {
        return playerPieces;
    }

    public Colors getColor() {return color;}

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

}

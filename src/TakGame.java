package src;

import java.util.ArrayList;
import java.util.Stack;
import java.util.List;

public class TakGame {

    private Square[][] board;

    TakGame(int dimensions) {

        board = new Square[dimensions][dimensions];

        for (int r = 0; r < dimensions; r++) {
            for (int c = 0; c < dimensions; c++) {
                board[r][c] = new Square();
            }
        }


    }



    class Square {
        Stack<Piece> pieces;

        Square() {
            pieces = new Stack<Piece>();
        }
        public void addPiece(Piece piece) {
            pieces.push(piece);
        }
    }

    class Piece {
        Colors color;
        PieceType type;

        Piece(Colors color, PieceType type) {
            this.color = color;
            this.type = type;
        }

    }

    class Player {
        Colors color;
        List<Piece> inventory;

        Player(Colors color) {
            this.color = color;
            inventory = new ArrayList<>();
        }

        public void addPiece(Piece piece) {
            inventory.add(piece);
        }

        public void takeTurn(String input) {

        }
    }

}

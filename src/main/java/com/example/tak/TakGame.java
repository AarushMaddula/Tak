package com.example.tak;

import java.util.*;

public class TakGame {

    private int turn;

    private int size;

    private Stack<Piece>[][] board;

    private Stack<Piece> playerSelection;

    private ArrayList<Player> players = new ArrayList<>();

    private ArrayList<String> moves = new ArrayList<>();

    TakGame(int size) {
        board = new Stack[size][size];

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                board[r][c] = new Stack<>();
            }
        }

        this.size = size;

        Colors[] colors = {Colors.WHITE, Colors.BLACK};

        for (Colors color : colors) {
            Player player = new Player(color, size);
            player.setNormalPieceCount(getNumNormalPieces(size));
            player.setBishopPieceCount(getNumBishopPieces(size));
            players.add(player);
        }

        moves.add(getBoardString());
    }

    TakGame(String boardString) {
        size = getSizeFromBoard(boardString);

        Colors[] colors = {Colors.WHITE, Colors.BLACK};

        for (Colors color : colors) {
            Player player = new Player(color, size);
            players.add(player);
        }

        setBoardString(boardString);
        moves.add(getBoardString());
    }

    public String getBoardString() {
        // Format : turn, | = new row, [] = stack, bottom to top
        StringBuilder boardString = new StringBuilder();
        boardString.append(turn).append(".");

        int countEmpty = 0;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Stack<Piece> square = board[r][c];

                if (square.empty()) countEmpty++;
                else if (countEmpty != 0) {
                    boardString.append(countEmpty);
                    countEmpty = 0;
                }

                boardString.append("[");

                Iterator<Piece> stack = square.iterator();

                while (stack.hasNext()) {
                    Piece piece = stack.next();
                    PieceType type = piece.getType();
                    Colors color = piece.getColor();

                    switch (type) {
                        case FLAT:
                            boardString.append(color == Colors.WHITE ? "f" : "F");
                        case STANDING:
                            boardString.append(color == Colors.WHITE ? "s" : "S");
                        case BISHOP:
                            boardString.append(color == Colors.WHITE ? "b" : "B");
                    }
                }

                boardString.append("]");
            }

            if (countEmpty != 0) {
                boardString.append(countEmpty);
                countEmpty = 0;
            }
        }

        return boardString.toString();
    }

    public void setBoardString(String fullBoardString) {
        String[] boardStringSplit = fullBoardString.split("\\.");
        turn = Integer.getInteger(boardStringSplit[0]);
        String boardString = boardStringSplit[1];

        int row = 0;
        int column = 0;

        size = getSizeFromBoard(fullBoardString);
        board = new Stack[size][size];

        int numBishopsWhite = 0;
        int numNormalPiecesWhite = 0;

        int numBishopsBlack = 0;
        int numNormalPiecesBlack = 0;

        Stack<Piece> square = new Stack<>();

        for (int i = 0; i < boardString.length(); i++) {
            char c = boardString.charAt(i);

            if (Character.isDigit(c)) {
                column += c;
                continue;
            } else if (c == '[') {
                column++;
                square.empty();
                continue;
            } else if (c == '|') {
                row++;
                column = 0;
                continue;
            } else if (c == ']') {
                board[row][column] = square;
                continue;
            }

            Colors color = Character.isLowerCase(c) ? Colors.WHITE : Colors.BLACK;

            PieceType type = null;

            switch (Character.toLowerCase(c)) {
                case 'f' -> type = PieceType.FLAT;
                case 's' -> type = PieceType.STANDING;
                case 'b' -> type = PieceType.BISHOP;
                default -> System.out.println("INVALID PIECE TYPE IN BOARD STRING");
            }

            if (color == Colors.WHITE) {
                if (type == PieceType.FLAT || type == PieceType.STANDING) {
                    numNormalPiecesWhite++;
                } else {
                    numBishopsWhite++;
                }
            } else {
                if (type == PieceType.FLAT || type == PieceType.STANDING) {
                    numNormalPiecesBlack++;
                } else {
                    numBishopsBlack++;
                }
            }


            Piece piece = new Piece(color, type);
            square.push(piece);
        }

        for (Player player : players) {
            if (player.getColor() == Colors.WHITE) {
                player.setNormalPieceCount(getNumNormalPieces(size) - numNormalPiecesWhite);
                player.setBishopPieceCount(getNumBishopPieces(size) - numBishopsWhite);

            } else {
                player.setNormalPieceCount(getNumNormalPieces(size) - numNormalPiecesBlack);
                player.setBishopPieceCount(getNumBishopPieces(size) - numBishopsBlack);
            }
        }

    }

    class Player {

        ArrayList<Stack<Piece>> playerPieces;
        Colors color;

        Player (Colors color, int size) {
            this.color = color;
            this.playerPieces = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                playerPieces.add(new Stack<Piece>());
            }
        }

        public void removePiece(Piece piece) {
            for (Stack<Piece> stack: playerPieces) {
                stack.remove(piece);
            }
        }

        public void setBishopPieceCount(int numPieces) {
            int size = playerPieces.size();

            for (int i = 0; i < numPieces; i++) {
                Piece bishop = new Piece(color, PieceType.BISHOP);

                bishop.setOrder(0);
                playerPieces.get((size - 1) - i).add(bishop);
            }
        }

        public void setNormalPieceCount(int numPieces) {
            for (int i = 0; i < numPieces; i++) {
                Piece normal = new Piece(color, PieceType.FLAT);

                normal.setOrder(i % 15);
                playerPieces.get(i / 15).add(normal);
            }
        }

        public void addPiece(Piece piece) {

        }

        public ArrayList<Stack<Piece>> getPlayerPieces() {
            return playerPieces;
        }

        public Colors getColor() {return color;}
    }

    class Piece {
        PieceType type;
        Colors color;

        HelloApplication.Piece boardPiece;
        int order, row, column;

        Piece (Colors color, PieceType type) {
            this.color = color;
            this.type = type;
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

        public void setBoardPiece(HelloApplication.Piece boardPiece) {
            this.boardPiece = boardPiece;
        }

        public HelloApplication.Piece getBoardPiece() {
            return boardPiece;
        }
    }

    public boolean isValidMove(int row, int column) {
        Stack<Piece> square = board[row][column];
        return square.isEmpty();
    }

    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, int bottomIndex) {
        Stack<Piece> square = board[fromRow][fromCol];
        Stack<Piece> endSquare = board[toRow][toCol];

        Player player = getCurrentPlayer();
        Colors color = player.getColor();

        Piece topPiece = square.peek();

        if ((square.size() - bottomIndex) + endSquare.size() > size || topPiece.getColor() != color) {
            return false;
        }

        if (!endSquare.isEmpty()) {
            Piece topEndPiece = endSquare.peek();

            if (topEndPiece.getType() != PieceType.FLAT && !(square.peek().getType() == PieceType.BISHOP && square.size() == 1)) {
                return false;
            }

        }

        int totalOffset = Math.abs(toRow - fromRow) + Math.abs(toCol - fromCol);

        if (totalOffset != 1) {
            return false;
        }

        return true;
    }

    public void moveStack(int fromRow, int fromCol, int toRow, int toCol, int bottomIndex) {
        Stack<Piece> square = board[fromRow][fromCol];
        Stack<Piece> endSquare = board[toRow][toCol];

        List<Piece> subStack = new ArrayList<>();

        int stackSize = square.size();
        int count = 0;
        for (int i = 0; i < stackSize; i++) {
            Piece piece = square.get(count);

            if (count >= bottomIndex) {
                square.remove(count);
                subStack.add(piece);
            } else {
                count++;
            }
        }

        if (!endSquare.isEmpty() && endSquare.peek().getType() == PieceType.STANDING) {
            endSquare.peek().setType(PieceType.FLAT);
        }

        //adds all selected pieces to final square
        for (Piece piece: subStack) {
            piece.setOrder(endSquare.size());
            piece.setRow(toRow);
            piece.setColumn(toCol);

            endSquare.add(piece);
        }

        //removes pieces from original square
        int numTime = square.size() - bottomIndex;
        for (int i = 0; i < numTime; i++) {
            square.pop();
        }

        turn++;
        moves.add(getBoardString());
    }

    public void placePiece(int row, int column, Piece piece) {
        Player player = piece.getColor() == Colors.WHITE ? players.get(0) : players.get(1);

        player.removePiece(piece);
        Stack<Piece> square = board[row][column];

        piece.setOrder(square.size());
        piece.setRow(row);
        piece.setColumn(column);

        square.add(piece);

        turn++;
        moves.add(getBoardString());
    }

    public boolean isFinished() {
        return false;
    }

    private int getNumNormalPieces(int size) {
        return switch (size) {
            case 3 -> 10;
            case 4 -> 15;
            case 5 -> 21;
            case 6 -> 30;
            case 7 -> 40;
            case 8 -> 50;
            default -> {
                System.out.println("Size is invalid");
                yield 0;
            }
        };
    }

    private int getNumBishopPieces(int size) {
        return switch (size) {
            case 3, 4 -> 0;
            case 5, 6 -> 1;
            case 7, 8 -> 2;
            default -> {
                System.out.println("Size is invalid");
                yield 0;
            }
        };
    }

    private int getSizeFromBoard(String fullBoardString) {
        int boardSize = 0;

        String[] boardStringSplit = fullBoardString.split("\\.");
        String boardString = boardStringSplit[1];

        for (int i = 0; i < boardString.length(); i++) {
            char c = boardString.charAt(i);

            if (Character.isDigit(c)) {
                boardSize += c;
            } else if (c == '[') {
                boardSize++;
            } else if (c == '|') {
                break;
            }
        }

        return boardSize;

    }

    public Player getCurrentPlayer() {
        return players.get(turn % 2);
    }

    public Stack<Piece> getSquare(int row, int col) {
        return board[row][col];
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }
}

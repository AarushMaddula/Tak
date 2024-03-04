package com.example.tak;

import java.io.Serializable;
import java.util.*;

public class TakGame implements Serializable {

    private int turn;

    private int size;

    private Stack<GamePiece>[][] board;

    private final Stack<GamePiece> playerSelection = new Stack<>();

    private Direction direction = null;

    private final ArrayList<Player> players = new ArrayList<>();

    private final Stack<String> moves = new Stack<>();

    private int currId = 0;

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
            Player player = new Player(color, size, this);
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
            Player player = new Player(color, size, this);
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
                Stack<GamePiece> square = board[r][c];

                if (square.isEmpty()) {
                    countEmpty++;
                    continue;
                } else if (countEmpty != 0) {
                    boardString.append(countEmpty);
                    countEmpty = 0;
                }

                boardString.append("[");

                for (GamePiece piece : square) {
                    PieceType type = piece.getType();
                    Colors color = piece.getColor();

                    switch (type) {
                        case FLAT -> boardString.append(color == Colors.WHITE ? "f" : "F");
                        case STANDING -> boardString.append(color == Colors.WHITE ? "s" : "S");
                        case BISHOP -> boardString.append(color == Colors.WHITE ? "b" : "B");
                    }
                }

                boardString.append("]");
            }

            if (countEmpty != 0) {
                boardString.append(countEmpty);
                countEmpty = 0;
            }

            if (r != size - 1) boardString.append("|");
        }

        return boardString.toString();
    }

    public void setBoardString(String fullBoardString) {
        String[] boardStringSplit = fullBoardString.split("\\.");
        turn = Integer.parseInt(boardStringSplit[0]);
        String boardString = boardStringSplit[1];

        int row = 0;
        int column = 0;

        size = getSizeFromBoard(fullBoardString);
        board = new Stack[size][size];

        int numBishopsWhite = 0;
        int numNormalPiecesWhite = 0;

        int numBishopsBlack = 0;
        int numNormalPiecesBlack = 0;

        Stack<GamePiece> square = new Stack<>();

        for (int i = 0; i < boardString.length(); i++) {
            char c = boardString.charAt(i);

            if (Character.isDigit(c)) {
                for (int j = 0; j < c - '0'; j++) {
                    board[row][column] = new Stack<>();
                    column++;
                }
                continue;
            } else if (c == '[') {
                continue;
            } else if (c == '|') {
                row++;
                column = 0;
                continue;
            } else if (c == ']') {
                board[row][column] = square;
                square = new Stack<>();
                column++;
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


            GamePiece piece = new GamePiece(color, type);

            piece.setOrder(square.size());
            piece.setRow(row);
            piece.setColumn(column);
            piece.setId(getCurrId());

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

    public boolean isValidPlacement(int row, int column) {
        Stack<GamePiece> square = board[row][column];
        return square.isEmpty();
    }

    public boolean isValidMove(int toRow, int toCol) {
        if (playerSelection.isEmpty()) return false;

        Stack<GamePiece> endSquare = board[toRow][toCol];
        GamePiece piece = playerSelection.get(0);

        int fromRow = piece.getRow();
        int fromCol = piece.getColumn();

        if (!endSquare.isEmpty()) {
            GamePiece topEndPiece = endSquare.peek();

            if (topEndPiece.getType() == PieceType.STANDING && !(piece.getType() == PieceType.BISHOP)) {
                return false;
            }

            if (topEndPiece.getType() == PieceType.BISHOP) {
                return false;
            }

        }

        int totalOffset = Math.abs(toRow - fromRow) + Math.abs(toCol - fromCol);

        if (totalOffset > 1) {
            return false;
        }

        if (direction != null && totalOffset != 0) {
            int changeRow = toRow - fromRow;
            int changeColumn = toCol - fromCol;

            if (changeRow > 0 && direction == Direction.NORTH) {
                return true;
            } else if (changeRow < 0 && direction == Direction.SOUTH){
                return true;
            } else if (changeColumn > 0 && direction == Direction.EAST) {
                return true;
            } else if (changeColumn < 0 && direction == Direction.WEST) {
                return true;
            }
            return false;
        }

        return true;
    }

    public void moveStack(int toRow, int toCol) {
        Stack<GamePiece> endSquare = board[toRow][toCol];
        GamePiece piece = playerSelection.get(0);

        if (direction == null) {
            int fromRow = piece.getRow();
            int fromCol = piece.getColumn();

            int changeRow = toRow - fromRow;
            int changeColumn = toCol - fromCol;

            if (changeRow > 0) {
                direction = Direction.NORTH;
            } else if (changeRow < 0){
                direction = Direction.SOUTH;
            } else if (changeColumn > 0) {
                direction = Direction.EAST;
            } else if (changeColumn < 0) {
                direction = Direction.WEST;
            }
        }

        for (GamePiece selPiece : playerSelection) {
            selPiece.setOrder(selPiece.getOrder() - 1);
            selPiece.setRow(toRow);
            selPiece.setColumn(toCol);
        }
        piece.setOrder(endSquare.size());


        if (piece.getType() == PieceType.BISHOP && !endSquare.isEmpty()) endSquare.peek().setType(PieceType.FLAT);

        endSquare.add(piece);
        playerSelection.remove(0);

        if (playerSelection.isEmpty()) {
            direction = null;
        }
    }

    public void setSelection(int id) {
        GamePiece piece = findGamePiece(id);

        int row = piece.getRow();
        int column = piece.getColumn();

        playerSelection.add(piece);

        Stack<GamePiece> square = getSquare(row, column);
        square.remove(piece);
    }

    public void clearSelection() {
        playerSelection.clear();
    }

    public void addPiece(GamePiece piece, int row, int column) {
        this.getSquare(row, column).add(piece);
    }

    public Stack<GamePiece> getPlayerSelection() {
        return playerSelection;
    }

    public void placePiece(int row, int column) {
        GamePiece piece = playerSelection.get(0);

        Player player = piece.getColor() == Colors.WHITE ? players.get(0) : players.get(1);

        player.removePiece(piece);
        Stack<GamePiece> square = board[row][column];

        piece.setOrder(square.size());
        piece.setRow(row);
        piece.setColumn(column);

        square.add(piece);
        playerSelection.remove(0);
    }

    public void toNextTurn() {
        turn++;
        moves.add(getBoardString());
        System.out.println(getBoardString());
    }

    public Player isFinished() {

        //checks for a road

        boolean whiteWin = false;
        boolean blackWin = false;

        Stack<GamePiece>[] row = board[0];

        for (Stack<GamePiece> square : row) {
            if (square.isEmpty()) continue;

            GamePiece piece = square.peek();

            if (isRoad(piece, new ArrayList<>(), Direction.NORTH)) {
                if (piece.getColor() == Colors.WHITE) {
                    whiteWin = true;
                } else {
                    blackWin = true;
                }
            }
        }

        for (int r = 0; r < size; r++) {
            Stack<GamePiece> square = board[r][0];

            if (square.isEmpty()) continue;

            GamePiece piece = square.peek();

            if (isRoad(piece, new ArrayList<>(), Direction.EAST)) {
                if (piece.getColor() == Colors.WHITE) {
                    whiteWin = true;
                } else {
                    blackWin = true;
                }
            }
        }

        if (whiteWin && blackWin) {
            return getCurrentPlayer();
        } else if (whiteWin) {
            return players.get(0);
        } else if (blackWin) {
            return players.get(1);
        }

        boolean boardFilled = true;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Stack<GamePiece> square = board[r][c];

                if (square.isEmpty()) {
                    boardFilled = false;
                    break;
                }
            }
        }

        if (boardFilled) return countPoints();

        for (Player player: players) {
            int pieces = player.getPlayerPieces().size();

            if (pieces == 0) return countPoints();
        }

        return null;
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
                boardSize += c - '0';
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

    public Colors getCurrentColor() {
        return getCurrentPlayer().getColor();
    }

    public Stack<GamePiece> getSquare(int row, int col) {
        return board[row][col];
    }

    public Stack<GamePiece>[][] getBoard() {
        return board;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public Stack<String> getMoves() {
        return moves;
    }

    private boolean isRoad(GamePiece currPiece, ArrayList<GamePiece> traveledPieces, Direction endSide) {

        int row = currPiece.getRow();
        int column = currPiece.getColumn();
        Colors color = currPiece.getColor();

        if (currPiece.getType() != PieceType.STANDING && currPiece.getColor() == color) return false;

        traveledPieces.add(currPiece);

        boolean isConnected = false;

        //base case

        if (endSide == Direction.EAST && column == size - 1) {
            return true;
        }

        if (endSide == Direction.WEST && column == 0) {
            return true;
        }

        if (endSide == Direction.NORTH && row == size - 1) {
            return true;
        }

        if (endSide == Direction.SOUTH && row == 0) {
            return true;
        }


        //get north square
        if (row + 1 < size) {
            Stack<GamePiece> square = board[row + 1][column];

            if (!square.isEmpty()) {
                GamePiece topPiece = square.peek();

                if (topPiece.getType() != PieceType.STANDING && topPiece.getColor() == color && !traveledPieces.contains(topPiece)) {
                    if (isRoad(topPiece, traveledPieces, endSide)) isConnected = true;
                }

            }
        }
        //get south square
        if (row - 1 >= 0) {
            Stack<GamePiece> square = board[row - 1][column];

            if (!square.isEmpty()) {

                GamePiece topPiece = square.peek();

                if (topPiece.getType() != PieceType.STANDING && topPiece.getColor() == color && !traveledPieces.contains(topPiece)) {
                    if (isRoad(topPiece, traveledPieces, endSide)) isConnected = true;
                }            }
        }
        //get east square
        if (column + 1 < size) {
            Stack<GamePiece> square = board[row][column + 1];
            if (!square.isEmpty()) {
                GamePiece topPiece = square.peek();

                if (topPiece.getType() != PieceType.STANDING && topPiece.getColor() == color && !traveledPieces.contains(topPiece)) {
                    if (isRoad(topPiece, traveledPieces, endSide)) isConnected = true;
                }            }
        }
        //get west square
        if (column - 1 >= 0) {
            Stack<GamePiece> square = board[row][column - 1];

            if (!square.isEmpty()) {
                GamePiece topPiece = square.peek();

                if (topPiece.getType() != PieceType.STANDING && topPiece.getColor() == color && !traveledPieces.contains(topPiece)) {
                    if (isRoad(topPiece, traveledPieces, endSide)) isConnected = true;
                }
            }
        }

        return isConnected;
    }

    private Player countPoints() {

        int intWhiteFlatPieces = 0;
        int intBlackFlatPieces = 0;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Stack<GamePiece> square = board[r][c];

                if (square.isEmpty()) continue;

                GamePiece piece = square.peek();
                if (piece.getColor() == Colors.WHITE && piece.getType() == PieceType.FLAT) {
                    intWhiteFlatPieces++;
                } else {
                    intBlackFlatPieces++;
                }
            }
        }

        if (intWhiteFlatPieces > intBlackFlatPieces) {
            return players.get(0);
        } else if (intWhiteFlatPieces < intBlackFlatPieces) {
            return players.get(1);
        } else {
            return null;
        }
    }

    private GamePiece findGamePiece(int id) {
        for (Player player: players) {
            for (Stack<GamePiece> pieceStack : player.playerPieces) {
                for (GamePiece piece : pieceStack) {
                    if (piece.getId() == id) return piece;
                }
            }
        }

        for (Stack<GamePiece>[] row : board) {
            for (Stack<GamePiece> column : row) {
                for (GamePiece piece : column) {
                    if (piece.getId() == id) return piece;
                }
            }
        }

        return null;
    }

    public int getCurrId() {
        int result = currId;
        currId++;
        return result;
    }

    public void setGamePieceType(int id, PieceType type) {
        GamePiece gamePiece = findGamePiece(id);
        gamePiece.setType(type);
    }
}

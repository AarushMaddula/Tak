package com.tak.game;

import java.io.Serializable;
import java.util.*;

//processes and stores the data used in the game, essentially the backend

public class TakGame implements Serializable {

    private int turn;

    private int size;

    private GameSquare[][] board;

    private final GameSquare playerSelection = new GameSquare();

    private Direction direction = null;

    private final ArrayList<Player> players = new ArrayList<>();

    private final Stack<String> moves = new Stack<>();

    private int currId = 0;

    //init the game based on size
    TakGame(int size) {
        board = new GameSquare[size][size];

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                GameSquare square = new GameSquare();
                square.setRow(r);
                square.setColumn(c);

                board[r][c] = square;
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

    //init the game based on a given state
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

    //retrieves board state
    public String getBoardString() {
        // Format : turn, | = new row, [] = stack, bottom to top
        StringBuilder boardString = new StringBuilder();
        boardString.append(turn).append(".");

        int countEmpty = 0;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                GameSquare square = board[r][c];

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
                        case FLAT:
                            boardString.append(color == Colors.WHITE ? "f" : "F");
                            break;
                        case STANDING:
                            boardString.append(color == Colors.WHITE ? "s" : "S");
                            break;
                        case BISHOP:
                            boardString.append(color == Colors.WHITE ? "b" : "B");
                            break;
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

    //sets board state based on string
    public void setBoardString(String fullBoardString) {
        String[] boardStringSplit = fullBoardString.split("\\.");
        turn = Integer.parseInt(boardStringSplit[0]);
        String boardString = boardStringSplit[1];

        int row = 0;
        int column = 0;

        size = getSizeFromBoard(fullBoardString);
        board = new GameSquare[size][size];

        int numBishopsWhite = 0;
        int numNormalPiecesWhite = 0;

        int numBishopsBlack = 0;
        int numNormalPiecesBlack = 0;

        GameSquare selectedSquare = new GameSquare();

        for (int i = 0; i < boardString.length(); i++) {
            char c = boardString.charAt(i);

            if (Character.isDigit(c)) {
                for (int j = 0; j < c - '0'; j++) {
                    GameSquare square = new GameSquare();
                    square.setRow(row);
                    square.setColumn(column);
                    board[row][column] = square;

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
                board[row][column] = selectedSquare;
                selectedSquare = new GameSquare();
                column++;
                continue;
            }

            Colors color = Character.isLowerCase(c) ? Colors.WHITE : Colors.BLACK;

            PieceType type = null;

            switch (Character.toLowerCase(c)) {
                case 'f':
                    type = PieceType.FLAT;
                    break;
                case 's':
                    type = PieceType.STANDING;
                    break;
                case 'b':
                    type = PieceType.BISHOP;
                    break;
                default:
                    System.out.println("INVALID PIECE TYPE IN BOARD STRING");
                    break;
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

            piece.setOrder(selectedSquare.size());
            piece.setRow(row);
            piece.setColumn(column);
            piece.setId(getCurrId());

            selectedSquare.push(piece);
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

    //checks if a piece can be placed on the board from the side
    public boolean isValidPlacement(int row, int column) {
        GameSquare square = board[row][column];
        return square.isEmpty();
    }

    //checks if a piece can be moved to a spot on the board
    public boolean isValidMove(int row, int column) {
        if (playerSelection.isEmpty()) return false;

        if (row < 0 || row >= size || column < 0 || column >= size) return false;

        GameSquare endSquare = board[row][column];
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

        int totalOffset = Math.abs(row - fromRow) + Math.abs(column - fromCol);

        if (totalOffset > 1) {
            return false;
        }

        if (direction != null && totalOffset != 0) {
            int changeRow = row - fromRow;
            int changeColumn = column - fromCol;

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

    //moves the player selection to the selected square & drops one piece
    public void moveStack(int toRow, int toCol) {
        GameSquare endSquare = board[toRow][toCol];
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

    //adds a piece to the player selection
    public void setSelection(int id) {
        GamePiece piece = findGamePiece(id);

        int row = piece.getRow();
        int column = piece.getColumn();

        playerSelection.add(piece);

        GameSquare square = getSquare(row, column);
        square.remove(piece);
    }

    //clears the player selection
    public void clearSelection() {
        playerSelection.clear();
    }

    //adds a piece to a given square
    public void addPiece(GamePiece piece, int row, int column) {
        this.getSquare(row, column).add(piece);
    }

    //retrieves the player's selection
    public GameSquare getPlayerSelection() {
        return playerSelection;
    }

    //places a piece to a square
    public void placePiece(int row, int column) {
        GamePiece piece = playerSelection.get(0);

        Player player = piece.getColor() == Colors.WHITE ? players.get(0) : players.get(1);

        player.removePiece(piece);
        GameSquare square = board[row][column];

        piece.setOrder(square.size());
        piece.setRow(row);
        piece.setColumn(column);

        square.add(piece);
        playerSelection.remove(0);
    }

    //switches to the next turn when called
    public void toNextTurn() {
        turn++;
        moves.add(getBoardString());
        System.out.println(getBoardString());
    }

    //retrieves if the game is finished and who won
    public Colors isFinished() {

        //checks for a road

        boolean whiteWin = false;
        boolean blackWin = false;

        GameSquare[] row = board[0];

        for (GameSquare square : row) {
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
            GameSquare square = board[r][0];

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
            return getCurrentPlayer().getColor();
        } else if (whiteWin) {
            return Colors.WHITE;
        } else if (blackWin) {
            return Colors.BLACK;
        }

        boolean boardFilled = true;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                GameSquare square = board[r][c];

                if (square.isEmpty()) {
                    boardFilled = false;
                    break;
                }
            }
        }

        if (boardFilled) return countPoints();

        for (Player player: players) {
            boolean isPieces = false;

            ArrayList<GameSquare> pieces = player.getPlayerPieces();
            for (GameSquare pieceStack: pieces) {
                if (!pieceStack.isEmpty()) {
                    isPieces = true;
                    break;
                }
            }

            if (!isPieces) return countPoints();
        }

        return Colors.NONE;
    }

    private int getNumNormalPieces(int size) {
        switch (size) {
            case 3: return 10;
            case 4: return 15;
            case 5: return 21;
            case 6: return 30;
            case 7: return 40;
            case 8: return 50;
            default:
                System.out.println("Size is invalid");
                return 0;
        }
    }

    private int getNumBishopPieces(int size) {
        switch (size) {
            case 3: return 0;
            case 4: return 0;
            case 5: return 1;
            case 6: return 1;
            case 7: return 2;
            case 8: return 2;
            default:
                System.out.println("Size is invalid");
                return 0;
        }
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

    public GameSquare getSquare(int row, int col) {
        return board[row][col];
    }

    public GameSquare[][] getBoard() {
        return board;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public Stack<String> getMoves() {
        return moves;
    }

    //checks for a road
    private boolean isRoad(GamePiece currPiece, ArrayList<GamePiece> traveledPieces, Direction endSide) {

        int row = currPiece.getRow();
        int column = currPiece.getColumn();
        Colors color = currPiece.getColor();

        if (currPiece.getType() == PieceType.STANDING) return false;

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
            GameSquare square = board[row + 1][column];

            if (!square.isEmpty()) {
                GamePiece topPiece = square.peek();

                if (topPiece.getType() != PieceType.STANDING && topPiece.getColor() == color && !traveledPieces.contains(topPiece)) {
                    if (isRoad(topPiece, traveledPieces, endSide)) isConnected = true;
                }

            }
        }
        //get south square
        if (row - 1 >= 0) {
            GameSquare square = board[row - 1][column];

            if (!square.isEmpty()) {

                GamePiece topPiece = square.peek();

                if (topPiece.getType() != PieceType.STANDING && topPiece.getColor() == color && !traveledPieces.contains(topPiece)) {
                    if (isRoad(topPiece, traveledPieces, endSide)) isConnected = true;
                }            }
        }
        //get east square
        if (column + 1 < size) {
            GameSquare square = board[row][column + 1];
            if (!square.isEmpty()) {
                GamePiece topPiece = square.peek();

                if (topPiece.getType() != PieceType.STANDING && topPiece.getColor() == color && !traveledPieces.contains(topPiece)) {
                    if (isRoad(topPiece, traveledPieces, endSide)) isConnected = true;
                }            }
        }
        //get west square
        if (column - 1 >= 0) {
            GameSquare square = board[row][column - 1];

            if (!square.isEmpty()) {
                GamePiece topPiece = square.peek();

                if (topPiece.getType() != PieceType.STANDING && topPiece.getColor() == color && !traveledPieces.contains(topPiece)) {
                    if (isRoad(topPiece, traveledPieces, endSide)) isConnected = true;
                }
            }
        }

        return isConnected;
    }

    //counts the number of flat pieces each player has on the board and returns highest value
    private Colors countPoints() {

        int intWhiteFlatPieces = 0;
        int intBlackFlatPieces = 0;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                GameSquare square = board[r][c];

                if (square.isEmpty()) continue;

                GamePiece piece = square.peek();
                if (piece.getColor() == Colors.WHITE && piece.getType() == PieceType.FLAT) {
                    intWhiteFlatPieces++;
                } else if (piece.getType() == PieceType.FLAT){
                    intBlackFlatPieces++;
                }
            }
        }

        if (intWhiteFlatPieces > intBlackFlatPieces) {
            return Colors.WHITE;
        } else if (intWhiteFlatPieces < intBlackFlatPieces) {
            return Colors.BLACK;
        } else {
            return Colors.TIE;
        }
    }

    private GamePiece findGamePiece(int id) {
        for (Player player: players) {
            for (GameSquare pieceStack : player.playerPieces) {
                for (GamePiece piece : pieceStack) {
                    if (piece.getId() == id) return piece;
                }
            }
        }

        for (GameSquare[] row : board) {
            for (GameSquare column : row) {
                for (GamePiece piece : column) {
                    if (piece.getId() == id) return piece;
                }
            }
        }

        return null;
    }

    //used for creating new pieces
    public int getCurrId() {
        int result = currId;
        currId++;
        return result;
    }

    //changes the piecetype
    public void setGamePieceType(int id, PieceType type) {
        GamePiece gamePiece = findGamePiece(id);
        gamePiece.setType(type);
    }

    //gets all possible moves for a selected piece
    public ArrayList<GameSquare> getPossibleMoves() {
        ArrayList<GameSquare> possibleMoves = new ArrayList<>();

        if (playerSelection.isEmpty()) return possibleMoves;

        GamePiece bottomPiece = playerSelection.get(0);

        int row = bottomPiece.getRow();
        int column = bottomPiece.getColumn();

        if (isValidMove(row - 1, column)) possibleMoves.add(board[row - 1][column]);
        if (isValidMove(row + 1, column)) possibleMoves.add(board[row + 1][column]);
        if (isValidMove(row, column - 1)) possibleMoves.add(board[row][column - 1]);
        if (isValidMove(row, column + 1)) possibleMoves.add(board[row][column + 1]);
        if (direction != null) possibleMoves.add(board[row][column]);

        return possibleMoves;
    }
}

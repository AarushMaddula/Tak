package com.example.tak;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class TakGameHandler {

    TakGame game;

    Client client;

    TakGameHandler(int size, Client client) {
        this.client = client;

        if (client == null) {
            this.game = new TakGame(size);
        }
    }

    TakGameHandler(String boardString, Client client) {
        this.client = client;

        if (client == null) {
            this.game = new TakGame(boardString);
        }
    }

    public String getBoardString() {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("getBoardString");

            try {
                return client.sendRequest(method).boardString;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return game.getBoardString();
    }

    public void setBoardString(String fullBoardString) {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("setBoardString");
            method.add(fullBoardString);
            try {
                client.sendRequest(method);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        } else game.setBoardString(fullBoardString);
    }

    public Boolean isValidPlacement(int row, int column) {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("isValidPlacement");
            method.add(row);
            method.add(column);

            try {
                return client.sendRequest(method).isValidPlacement;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        }

        return game.isValidPlacement(row, column);
    }

    public Boolean isValidMove(int row, int column) {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("isValidMove");
            method.add(row);
            method.add(column);

            try {
                return client.sendRequest(method).isValidMove;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        }

        return game.isValidMove(row, column);
    }

    public void moveStack(int row, int column) {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("moveStack");
            method.add(row);
            method.add(column);

            try {
                client.sendRequest(method);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        } else game.moveStack(row, column);
    }

    public void setSelection(int id) {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("setSelection");
            method.add(id);

            try {
                client.sendRequest(method);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        } else game.setSelection(id);
    }

    public Stack<GamePiece> getPlayerSelection() {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("getPlayerSelection");

            try {
                return client.sendRequest(method).gamePieces;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        return game.getPlayerSelection();
    }

    public void clearSelection() {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("clearSelection");

            try {
                client.sendRequest(method);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else game.clearSelection();
    }

    public void addPiece(GamePiece piece, int row, int column) {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("addPiece");
            method.add(piece);
            method.add(row);
            method.add(column);

            try {
                client.sendRequest(method);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else game.addPiece(piece, row, column);
    }

    public void placePiece(int row, int column) {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("placePiece");
            method.add(row);
            method.add(column);

            try {
                client.sendRequest(method);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else game.placePiece(row, column);
    }

    public void toNextTurn() {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("toNextTurn");

            try {
                client.sendRequest(method);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else game.toNextTurn();
    }

    public Player isFinished() {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("isFinished");

            try {
                return client.sendRequest(method).player;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else return game.isFinished();
    }

    public Player getCurrentPlayer() {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("getCurrentPlayer");

            try {
                return client.sendRequest(method).player;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else return game.getCurrentPlayer();
    }

    public Colors getCurrentColor() {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("getCurrentColor");

            try {
                return client.sendRequest(method).color;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else return game.getCurrentColor();
    }

    public Stack<GamePiece> getSquare(int row, int col) {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("getSquare");
            method.add(row);
            method.add(col);

            try {
                return client.sendRequest(method).gamePieces;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else return game.getSquare(row, col);
    }

    public Stack<GamePiece>[][] getBoard() {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("getBoard");

            try {
                return client.sendRequest(method).board;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else return game.getBoard();
    }

    public ArrayList<Player> getPlayers() {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("getPlayers");

            try {
                Message msg = client.sendRequest(method);
                return msg.players;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else return game.getPlayers();
    }

    public Stack<String> getMoves() {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("getMoves");

            try {
                return client.sendRequest(method).moves;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else return game.getMoves();
    }

    public void setGamePieceType(int id, PieceType type) {
        if (client != null) {
            ArrayList<Object> method = new ArrayList<>();
            method.add("setGamePieceType");
            method.add(id);
            method.add(type);

            try {
                client.sendRequest(method);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        } else game.setGamePieceType(id, type);
    }

}

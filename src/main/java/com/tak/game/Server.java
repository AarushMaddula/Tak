package com.tak.game;

import java.io.*;
import java.net.*;
import java.util.ArrayList;


public class Server {
    private ServerSocket serverSocket;

    private static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    private int SIZE;

    private static TakGame game = null;

    private static int numPlayers = 0;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);

        game = new TakGame(5);

        while (true)
            new ClientHandler(serverSocket.accept()).start();
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    private static class ClientHandler extends Thread {

        private Colors color;
        private Socket clientSocket;
        private ObjectOutputStream out;
        private ObjectInputStream in;

        public ClientHandler(Socket socket) throws IOException {
            this.clientSocket = socket;
            clientHandlers.add(this);

            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            numPlayers++;
            if (numPlayers == 1) {
                color = Colors.WHITE;
            } else {
                color = Colors.BLACK;
            }

            Message msg = new Message();
            msg.color = color;
            out.writeObject(msg);

        }

        public void run() {
            try {
                ArrayList<Object> input;
                while ((input = (ArrayList<Object>) in.readObject()) != null) {
                    Message msg = new Message();

                    System.out.println(input);

                    switch ((String) input.get(0)) {
                        case "getBoardString":
                            msg.boardString = game.getBoardString();
                            break;
                        case "setBoardString":
                            game.setBoardString((String) input.get(1));
                            break;
                        case "isValidPlacement":
                            msg.isValidPlacement = game.isValidPlacement((int) input.get(1), (int) input.get(2));
                            break;
                        case "isValidMove":
                            msg.isValidMove = game.isValidMove((int) input.get(1), (int) input.get(2));
                            break;
                        case "moveStack":
                            game.moveStack((int) input.get(1), (int) input.get(2));
                            break;
                        case "setSelection":
                            game.setSelection((int) input.get(1));
                            break;
                        case "getPlayerSelection":
                            msg.gamePieces.addAll(game.getPlayerSelection());
                            break;
                        case "placePiece":
                            game.placePiece((int) input.get(1), (int) input.get(2));
                            break;
                        case "toNextTurn":
                            game.toNextTurn();
                            break;
                        case "isFinished":
                            msg.player = game.isFinished();
                            break;
                        case "getCurrentPlayer":
                            msg.player = game.getCurrentPlayer();
                            break;
                        case "getSquare":
                            msg.gamePieces.addAll(game.getSquare((int) input.get(1), (int) input.get(2)));
                            break;
                        case "getBoard":
                            msg.board = game.getBoard();
                            break;
                        case "getPlayers":
                            msg.players.addAll(game.getPlayers());
                            break;
                        case "getMoves":
                            msg.moves.addAll(game.getMoves());
                            break;
                        case "clearSelection":
                            game.clearSelection();
                            break;
                        case "addPiece":
                            game.addPiece((GamePiece) input.get(1), (int) input.get(2), (int) input.get(3));
                            break;
                        case "getCurrentColor":
                            msg.color = game.getCurrentColor();
                            break;
                        case "setGamePieceType":
                            game.setGamePieceType((int) input.get(1), (PieceType) input.get(2));
                            break;
                        default:
                            System.out.println("Error");
                            break;
                    }

                    msg.msgID = (int) input.get(input.size() - 1);

                    out.reset();
                    out.writeUnshared(msg);
                    out.reset();

                    System.out.println(input);

                    if (input.get(0).equals("toNextTurn")) {
                        Player gameFinished = game.isFinished();
                        System.out.println(gameFinished);

                        for (ClientHandler clientHandler: clientHandlers) {
                            if (clientHandler.color != this.color && gameFinished == null) {
                                Message newMsg = new Message();
                                newMsg.nextTurn = true;
                                clientHandler.out.writeUnshared(newMsg);
                            } else if (gameFinished != null) {
                                Message newMsg = new Message();
                                newMsg.gameFinished = gameFinished;
                                clientHandler.out.writeUnshared(newMsg);
                            }
                        }
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start(5555);
    }
}
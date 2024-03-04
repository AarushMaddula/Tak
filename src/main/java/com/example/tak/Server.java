package com.example.tak;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Stack;


public class Server {
    private ServerSocket serverSocket;

    private static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    private int SIZE;

    private static int cId = 0;

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

                    switch ((String) input.getFirst()) {
                        case "getBoardString" -> msg.boardString = game.getBoardString();
                        case "setBoardString" -> game.setBoardString((String) input.get(1));
                        case "isValidPlacement" -> msg.isValidPlacement = game.isValidPlacement((int) input.get(1), (int) input.get(2));
                        case "isValidMove" -> msg.isValidMove = game.isValidMove((int) input.get(1), (int) input.get(2));
                        case "moveStack" -> game.moveStack((int) input.get(1), (int) input.get(2));
                        case "setSelection" -> game.setSelection((int) input.get(1));
                        case "getPlayerSelection" -> msg.gamePieces.addAll(game.getPlayerSelection());
                        case "placePiece" -> game.placePiece((int) input.get(1), (int) input.get(2));
                        case "toNextTurn" -> game.toNextTurn();
                        case "isFinished" -> msg.player = game.isFinished();
                        case "getCurrentPlayer" -> msg.player = game.getCurrentPlayer();
                        case "getSquare" ->  msg.gamePieces.addAll(game.getSquare((int) input.get(1), (int) input.get(2)));
                        case "getBoard" ->  msg.board = game.getBoard();
                        case "getPlayers" ->  msg.players.addAll(game.getPlayers());
                        case "getMoves" -> msg.moves.addAll(game.getMoves());
                        case "clearSelection" -> game.clearSelection();
                        case "addPiece" -> game.addPiece((GamePiece) input.get(1), (int) input.get(2), (int) input.get(3));
                        case "getCurrentColor" -> msg.color = game.getCurrentColor();
                        case "setGamePieceType" -> game.setGamePieceType((int) input.get(1), (PieceType) input.get(2));
                        default -> System.out.println("Error");
                    }

                    msg.id = cId;
                    cId++;

                    out.reset();
                    out.writeUnshared(msg);
                    out.reset();

                    System.out.println(input);

                    if (input.getFirst().equals("toNextTurn")) {
                        for (ClientHandler clientHandler: clientHandlers) {

                            if (clientHandler.color != this.color) {
                                Message newMsg = new Message();
                                newMsg.nextTurn = true;
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
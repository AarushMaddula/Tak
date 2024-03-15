package com.tak.game;

import javafx.application.Platform;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;


public class Client {
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private GameInstance gameInstance = null;

    private int currId = 0;

    private final ArrayList<Message> msgs = new ArrayList<>();

    public void startConnection(String ip, int port) throws IOException, ClassNotFoundException {
        clientSocket = new Socket(ip, port);
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());

        new MyThread();
    }

    public void setGameInstance(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    class MyThread implements Runnable {

        Thread t;

        MyThread()
        {
            t = new Thread(this);
            System.out.println("New thread: " + t);
            t.start(); // Starting the thread
        }

        // execution of thread starts from run() method
        public void run()
        {
            Message msg;

            while (clientSocket.isConnected()) {
                try {
                    msg = (Message) in.readUnshared();
                    if (msg.nextTurn) {
                        gameInstance.toNextMove();
                    } else if (msg.gameFinished != null) {
                        Message finalMsg = msg;
                        Platform.runLater(() -> gameInstance.endGame(finalMsg.gameFinished));
                    } else {
                        msgs.add(msg);

                        msgs.removeIf(currMSG -> currMSG.dispose);

                        System.out.println("Received MessageID: " + msg.msgID + " | " + msgs);
                    }
                } catch (IOException | ClassNotFoundException ignored) {

                }
            }
        }
    }

    public Colors getColor() throws IOException, ClassNotFoundException {
        boolean isReached = false;
        Message msgRecieved = null;

        while (!isReached) {
            CopyOnWriteArrayList<Message> msgsCopy = new CopyOnWriteArrayList<>(msgs);

            for (Message msg : msgsCopy) {
                if (msg.color != null) {
                    msgRecieved = msg;
                    isReached = true;
                    msg.dispose= true;
                }
            }
        }

        return msgRecieved.color;
    }

    public Message sendRequest(ArrayList<Object> method) throws IOException, ClassNotFoundException {

        int msgSentId = currId;
        method.add(currId);
        currId++;

        out.writeUnshared(method);

        boolean isReached = false;
        Message msgRecieved = null;

        System.out.println("Sent MessageID: " + currId);

        while (!isReached) {
            CopyOnWriteArrayList<Message> msgsCopy = new CopyOnWriteArrayList<>(msgs);

            for (Message msg : msgsCopy) {
                if (msg.msgID == msgSentId) {
                    msgRecieved = msg;
                    isReached = true;
                    msg.dispose= true;
                }
            }
        }

        return msgRecieved;

    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
}

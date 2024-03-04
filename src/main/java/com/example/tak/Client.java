package com.example.tak;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client {
    private static Socket clientSocket;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;

    private static GameInstance gameInstance = null;

    private static MyThread currThread = null;

    public void startConnection(String ip, int port) throws IOException, ClassNotFoundException {
        clientSocket = new Socket(ip, port);
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());
    }

    public void setGameInstance(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    static class MyThread implements Runnable {

        Thread t;

        MyThread()
        {
            t = new Thread(this);
            System.out.println("New thread: " + t);
            currThread = this;
            t.start(); // Starting the thread
        }

        // execution of thread starts from run() method
        public void run()
        {
            Message msg;

            boolean msgGot = false;

            while (clientSocket.isConnected() && !msgGot) {
                try {
                    msg = (Message) in.readUnshared();
                    if (msg.nextTurn) {
                        msgGot = true;
                        gameInstance.setCurrentColor(gameInstance.getInstanceColor());
                        t.interrupt();
                    }
                } catch (IOException | ClassNotFoundException e) {
                    //System.out.println("err");
                }
            }
        }
    }

    public Colors getColor() throws IOException, ClassNotFoundException {
        Message msg = (Message) in.readUnshared();
        return msg.color;
    }

    public Message sendRequest(ArrayList<Object> method) throws IOException, ClassNotFoundException {
        if (currThread != null) currThread.t.interrupt();

        out.writeUnshared(method);

        Message msg = (Message) in.readUnshared();

        if (method.getFirst() == "toNextTurn") {
            MyThread thread = new MyThread();
            currThread = thread;
        }
        return msg;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
}

package com.tak.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;

public class Message implements Serializable {

    public int msgID = -1;

    public Colors color = null;

    boolean nextTurn = false;

    public Stack<GamePiece>[][] board;

    public ArrayList<Player> players = new ArrayList<>();

    public String boardString;

    public Boolean isValidPlacement, isValidMove;

    public Player player;

    public Stack<GamePiece> gamePieces = new Stack<>();

    public Stack<String> moves = new Stack<>();

    public ArrayList<Object> method = new ArrayList<>();

    public boolean dispose = false;

    public Player gameFinished = null;


}

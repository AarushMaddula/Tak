package com.tak.game;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;

//Builds & displays the game to the user using JavaFX library

public class GameInstance {
    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);
    private final int SIZE;
    private boolean isMovingStack = false;
    private final TakGameHandler game;
    private final SmartGroup root3D;
    private final AnchorPane globalRoot;
    private final int WIDTH, HEIGHT;
    private Colors instanceColor = null;
    boolean isMultiPlayer = false;
    private Colors currentColor;

    private final Alert alert;

    GameInstance(Stage stage, int size, Client client) {
        SIZE = size;

        root3D = new SmartGroup();

        WIDTH = (int) stage.getWidth();
        HEIGHT = (int) stage.getHeight();

        this.game = new TakGameHandler(SIZE, client);

        if (client != null) {
            isMultiPlayer = true;

            try {
                Client.setGameInstance(this);
                instanceColor = Client.getColor();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        currentColor = game.getCurrentColor();
        setAlignment();
        initBoard(root3D);

        Button goBack = new Button();
        goBack.prefWidth(100);
        goBack.prefHeight(25);
        goBack.setTranslateX(25);
        goBack.setTranslateY(25);
        goBack.setText("I can't do this!");
        goBack.setOnAction(e -> sceneCollector.setSelectionScreen());

        globalRoot = new AnchorPane();
        Scene scene = new Scene(globalRoot, stage.getWidth(), stage.getHeight(), true);

        SubScene sub = new SubScene(root3D, WIDTH, HEIGHT,true, SceneAntialiasing.BALANCED);
        Camera camera = new PerspectiveCamera();

        sub.setCamera(camera);
        sub.setFill(Color.ROYALBLUE);
        root3D.translateXProperty().set(WIDTH/2);
        root3D.translateYProperty().set(HEIGHT/2);
        root3D.translateZProperty().set(250);

        initMouseControl(root3D, sub, stage);

        stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            int offset = 45;

            switch (event.getCode()) {
                case W:
                    angleX.set(offset);
                    angleY.set(180);
                    break;
                case S:
                    angleX.set(offset);
                    angleY.set(0);
                    break;
                case E:
                    angleX.set(90);
                    angleY.set(0);
                    break;
                case A:
                    angleX.set(offset);
                    angleY.set(-90);
                    break;
                case D:
                    angleX.set(offset);
                    angleY.set(90);
                    break;
            }
        });

        stage.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getTarget().getClass() != BoardPiece.class && event.getTarget().getClass() != Square.class && !isMovingStack) {
                undoSelection();
            }
        });

        alert = new Alert(WIDTH, HEIGHT);

        globalRoot.getChildren().addAll(sub, goBack, alert);

        stage.setScene(scene);
        stage.show();
    }

    //allows the player to move their camera around the board
    private void initMouseControl(SmartGroup root, SubScene scene, Stage stage) {
        Rotate xRotate;
        Rotate yRotate;
        root.getTransforms().addAll(
                xRotate = new Rotate(0, Rotate.X_AXIS),
                yRotate = new Rotate(0, Rotate.Y_AXIS)
        );
        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        angleX.set(50);

        scene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();

            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
        });

        scene.setOnMouseDragged(event -> {
            double x = anchorAngleX - (anchorY - event.getSceneY());
            double y = anchorAngleY + (anchorX - event.getSceneX());


            angleX.set(x);
            angleY.set(y);
        });

        stage.addEventHandler(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            root.translateZProperty().set(root.getTranslateZ() - delta);
        });
    }

    //builds the board & pieces
    public void initBoard(SmartGroup root) {

        //make board
        PhongMaterial boardTexture = new PhongMaterial();
        boardTexture.setDiffuseColor(Color.CHOCOLATE);
        boardTexture.setDiffuseMap(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/tak/game/boardTexture.jpg"))));

        int length = (SIZE * 110) - 10 + 80;

        Box gameBoard = new Box(length, 20, length);
        gameBoard.setMaterial(boardTexture);
        root.getChildren().add(gameBoard);

        PhongMaterial squareTexture = new PhongMaterial();
        squareTexture.setDiffuseMap(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/tak/game/RockTextures.jpeg"))));

        //makes the lighting

        root.getChildren().add(new AmbientLight());

        //make the square tiles

        int startX = (SIZE - 1) * -55;
        int startZ = (SIZE - 1) * -55;

        for (int r = 0; r < SIZE; r++) {

            for (int c = 0; c < SIZE; c++) {
                Square square = new Square();

                square.setWidth(100);
                square.setHeight(2);
                square.setDepth(100);

                square.translateXProperty().set(startX);
                square.translateZProperty().set(startZ);
                square.translateYProperty().set(-11);

                square.setMaterial(squareTexture);
                square.setRow(r);
                square.setColumn(c);

                square.setOnMouseClicked(e -> {
                    movePiece(square.getRow(), square.getColumn());
                });

                root.getChildren().add(square);

                startX += 110;
            }
            startX = (SIZE - 1) * -55;
            startZ += 110;
        }

        //places letters/numbers on board

        int distance = (SIZE * 55) + 15;
        int x = distance * -1;
        int z = distance * -1 + 70;

        for (int i = 0; i < 2; i++) {

            for (int num = 0; num < SIZE; num++) {

                char text = i % 2 == 0 ? (char) ('1' + num) : (char) ('A' + num);

                Text c = new Text(String.valueOf(text));
                c.setFill(Color.GOLD);
                c.translateXProperty().set(x);
                c.translateZProperty().set(z);
                c.translateYProperty().set(-11);

                Transform r = new Rotate(-90, Rotate.X_AXIS);

                c.getTransforms().add(r);
                root.getChildren().add(c);

                Text c1 = new Text(String.valueOf(text));
                c1.setFill(Color.GOLD);

                if (i == 0) {
                    c1.translateXProperty().set(x * -1);
                    c1.translateZProperty().set(z);
                } else {
                    c1.translateXProperty().set(x);
                    c1.translateZProperty().set(z * -1);
                }

                c1.translateYProperty().set(-11);

                Transform r1 = new Rotate(-90, Rotate.X_AXIS);
                Transform r2 = new Rotate(180, Rotate.Z_AXIS);

                c1.getTransforms().addAll(r1, r2);
                root.getChildren().add(c1);

                if (i % 2 == 0) {
                    z += 110;
                } else {
                    x += 110;
                }
            }

            x = distance * -1 + 70;
            z = distance * -1;

        }

        Stack<GamePiece>[][] board = game.getBoard();

        //places all pieces on the board
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                Stack<GamePiece> square = board[row][column];

                for (int order = 0; order < square.size(); order++) {
                    GamePiece gamepiece = square.get(order);

                    Colors color = gamepiece.getColor();
                    PieceType pieceType = gamepiece.getType();

                    BoardPiece piece = new BoardPiece();
                    piece.setColor(color);
                    piece.setType(pieceType);
                    piece.setPieceId(gamepiece.getId());
                    piece.setOnBoard(true);

                    convertPieceShape(piece, pieceType);
                    setCoordinate(piece, row, column, order);

                    PhongMaterial mat4 = new PhongMaterial();
                    if (color == Colors.WHITE) {
                        mat4.setDiffuseMap(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/tak/game/WhitePiece.jpg"))));
                    } else {
                        mat4.setDiffuseMap(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/tak/game/BlackPiece.jpg"))));
                    }

                    piece.setMaterial(mat4);

                    piece.setOnMouseClicked(e -> boardPieceBehavior(piece));

                    root.getChildren().add(piece);
                }
            }
        }

        //places all player pieces on the sides of board

        for (Player player: game.getPlayers()) {

            Colors playerColor = player.getColor();
            ArrayList<Stack<GamePiece>> playerPieces = player.getPlayerPieces();

            for (int s = 0; s < playerPieces.size(); s++) {
                Stack<GamePiece> stack = playerPieces.get(s);

                for (int order = 0; order < stack.size(); order++) {
                    BoardPiece piece = new BoardPiece();
                    GamePiece gamePiece = stack.get(order);
                    piece.setPieceId(gamePiece.getId());
                    piece.setOnBoard(false);

                    PieceType pieceType = gamePiece.getType();
                    piece.setType(pieceType);
                    convertPieceShape(piece, pieceType);

                    piece.setColor(playerColor);

                    setCoordinate(piece, s, order);

                    piece.setOnMouseClicked(e -> playerPieceBehavior(piece));

                    PhongMaterial mat = new PhongMaterial();
                    if (playerColor == Colors.WHITE) {
                        mat.setDiffuseMap(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/tak/game/WhitePiece.jpg"))));
                    } else {
                        mat.setDiffuseMap(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/tak/game/BlackPiece.jpg"))));
                    }
                    piece.setMaterial(mat);

                    root.getChildren().add(piece);
                }
            }
        }
    }

    //refreshes the board each turn
    public void loadBoard() {

        Stack<GamePiece>[][] board = game.getBoard();

        //places all pieces on the board
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                Stack<GamePiece> square = board[row][column];

                for (int order = 0; order < square.size(); order++) {
                    GamePiece gamePiece = square.get(order);
                    BoardPiece piece = getBoardPiece(gamePiece);

                    convertPieceShape(piece, gamePiece.getType());
                    setCoordinate(piece, row, column, order);
                    piece.setOnBoard(true);

                    piece.setOnMouseClicked(e -> boardPieceBehavior(piece));
                }
            }
        }

        //places all player pieces on the sides of board
        for (Player player: game.getPlayers()) {

            ArrayList<Stack<GamePiece>> playerPieces = player.getPlayerPieces();

            for (int s = 0; s < playerPieces.size(); s++) {
                Stack<GamePiece> stack = playerPieces.get(s);

                for (int order = 0; order < stack.size(); order++) {
                    GamePiece gamePiece = stack.get(order);
                    BoardPiece piece = getBoardPiece(gamePiece);

                    if (gamePiece.getType() == PieceType.STANDING) {
                        piece.setType(PieceType.FLAT);
                        game.setGamePieceType(gamePiece.getId(), PieceType.FLAT);
                        convertPieceShape(piece, PieceType.FLAT);
                    }

                    piece.setOnBoard(false);
                    setCoordinate(piece, s, order);

                    piece.setOnMouseClicked(e -> playerPieceBehavior(piece));
                }
            }
        }

        //places player selected pieces in respective spots
        Stack<GamePiece> playerSelection = game.getPlayerSelection();

        for (GamePiece gamePiece: playerSelection) {
            BoardPiece boardPiece = getBoardPiece(gamePiece);

            int XOffset = (gamePiece.getColumn() * 110) - (SIZE - 1) * 55;
            int YOffset = (int) ((-236 + gamePiece.getOrder() * -11) - (boardPiece.getHeight() / 2));
            int ZOffset = (gamePiece.getRow() * 110) - (SIZE - 1) * 55;

            boardPiece.translateXProperty().set(XOffset);
            boardPiece.translateYProperty().set(YOffset);
            boardPiece.translateZProperty().set(ZOffset);
        }
    }

    //retrieves the pieces currently selected by the player
    public void getSelection(BoardPiece piece, boolean addPieces) {
        Stack<GamePiece> selectedSquare = game.getSquare(piece.getRow(), piece.getColumn());

        int size = selectedSquare.size();
        int order = 0;

        for (int j = 0; j < size; j++) {
            GamePiece selPiece = selectedSquare.get(order);
            BoardPiece selBoardPiece = getBoardPiece(selPiece);

            if (selPiece.getOrder() >= piece.getOrder()) {
                int diffOrder = selPiece.getOrder() - piece.getOrder();
                int YOffset = (int) ((-236 + diffOrder * -11) - (selBoardPiece.getHeight() / 2));
                selBoardPiece.translateYProperty().set(YOffset);
                if (addPieces) game.setSelection(selPiece.getId());
            } else {
                order++;
            }
        }
    }

    //clears the player's selection & places pieces back to their original spots
    public void undoSelection() {
        Stack<GamePiece> pieceSelected = game.getPlayerSelection();


        for (GamePiece gamePiece: pieceSelected) {
            BoardPiece piece = getBoardPiece(gamePiece);

            if (!piece.getOnBoard() && piece.getType() != PieceType.BISHOP) convertPieceShape(piece, PieceType.FLAT);

            piece.translateXProperty().set(piece.getBoardPositionX());
            piece.translateYProperty().set(piece.getBoardPositionY());
            piece.translateZProperty().set(piece.getBoardPositionZ());


            int row = gamePiece.getRow();
            int column = gamePiece.getColumn();

            if (piece.getOnBoard()) game.addPiece(gamePiece, row, column);
        }

        game.clearSelection();
    }

    //changes the shape/type of the piece
    public void convertPieceShape(BoardPiece p, PieceType type) {

        p.getTransforms().clear();

        int[] dimensions = new int[3];

        if (type == PieceType.FLAT) {
            dimensions = new int[]{80, 10, 80};
        } else if (type == PieceType.STANDING) {
            dimensions = new int[]{20, 50, 80};
            Transform t = new Rotate(45, Rotate.Y_AXIS);
            p.getTransforms().add(t);
        } else if (type == PieceType.BISHOP) {
            dimensions = new int[]{40, 80, 40};
        }

        p.setWidth(dimensions[0]);
        p.setHeight(dimensions[1]);
        p.setDepth(dimensions[2]);
    }

    //moves the bottom piece of the player selection to the desired spot if possible
    public void movePiece(int toRow, int toColumn) {

        Stack<GamePiece> pieceSelected = game.getPlayerSelection();

        if (pieceSelected.isEmpty()) {
            alert.setAlert("No Piece Selected!");
            return;
        }

        GamePiece gamePiece = pieceSelected.get(0);
        BoardPiece piece = getBoardPiece(gamePiece);

        if (!piece.getOnBoard() && game.isValidPlacement(toRow, toColumn)) {
            game.placePiece(toRow, toColumn);
            alert.clearAlert();
        } else if (piece.getOnBoard() && game.isValidMove(toRow, toColumn)) {
            game.moveStack(toRow, toColumn);
            alert.clearAlert();
        } else {
            alert.setAlert("Invalid Move!");
            return;
        }

        loadBoard();

        if (game.getPlayerSelection().isEmpty()) {
            if (!Objects.equals(game.getMoves().peek(), game.getBoardString())) {
                game.toNextTurn();
                currentColor = currentColor == Colors.WHITE ? Colors.BLACK : Colors.WHITE;
                isMovingStack = false;

                if (isMultiPlayer) return;

                setAlignment();

                Player playerWon = game.isFinished();

                if (playerWon != null) {
                    endGame(playerWon);
                }
            }
        } else {
            isMovingStack = true;
        }
    }

    //checks if a piece can be moved by a player
    public boolean isMovable(BoardPiece piece) {
        Stack<GamePiece> square = game.getSquare(piece.getRow(), piece.getColumn());
        GamePiece topGamePiece = square.peek();

        if (isMultiPlayer && currentColor != instanceColor) return false;
        if (!topGamePiece.getColor().equals(currentColor)) return false;
        if (square.size() - piece.getOrder() > SIZE) return false;

        return topGamePiece.getType().equals(PieceType.FLAT) || piece.getOrder() == topGamePiece.getOrder();
    }

    //set the coordinate of a piece upon initialization (on a board)
    public void setCoordinate(BoardPiece piece, int row, int column, int order) {
        int XOffset = (column * 110) - (SIZE - 1) * 55;
        int YOffset = (int) (-11 - (order * 11) - (piece.getHeight() / 2));
        int ZOffset = (row * 110) - (SIZE - 1) * 55;

        piece.translateXProperty().set(XOffset);
        piece.translateYProperty().set(YOffset);
        piece.translateZProperty().set(ZOffset);

        piece.setBoardPositionX(XOffset);
        piece.setBoardPositionY(YOffset);
        piece.setBoardPositionZ(ZOffset);

        piece.setRow(row);
        piece.setColumn(column);
        piece.setOrder(order);
    }

    //set the coordinate of a piece upon initialization (on the pieces on the side)
    public void setCoordinate(BoardPiece piece, int stack, int order){
        int XOffset = (SIZE * 55 - 55) * -1 + (stack * 110);
        int YOffset = (int) (-11 - (order * 11) - (piece.getHeight() / 2));
        int ZOffset = (SIZE * 55 + 115);

        if (piece.getColor() == Colors.WHITE) ZOffset *= -1;

        piece.translateXProperty().set(XOffset);
        piece.translateYProperty().set(YOffset);
        piece.translateZProperty().set(ZOffset);

        piece.setBoardPositionX(XOffset);
        piece.setBoardPositionY(YOffset);
        piece.setBoardPositionZ(ZOffset);

        piece.setOrder(order);
    }

    //sets the behavior of the pieces on the side of the board
    public void playerPieceBehavior(BoardPiece piece) {
        if (instanceColor != currentColor && isMultiPlayer) return;
        if (piece.getColor() != currentColor) return;

        Stack<GamePiece> stack = game.getPlayerSelection();
        if (stack.isEmpty()) {
            game.setSelection(piece.getPieceId());
            piece.translateYProperty().set(-236);
            alert.clearAlert();
        } else if (game.getPlayerSelection().get(0).getId() != piece.getPieceId()) {
            return;
        } else if (piece.getType() == PieceType.FLAT) {
            piece.setType(PieceType.STANDING);
            game.setGamePieceType(piece.getPieceId(), PieceType.STANDING);

            convertPieceShape(piece, PieceType.STANDING);
        } else if (piece.getType() == PieceType.STANDING) {
            piece.setType(PieceType.FLAT);
            game.setGamePieceType(piece.getPieceId(), PieceType.FLAT);

            convertPieceShape(piece, PieceType.FLAT);
        }
    }

    //sets the behavior of the pieces on the board
    public void boardPieceBehavior(BoardPiece piece) {
        if (!game.getPlayerSelection().isEmpty()) {
            movePiece(piece.getRow(), piece.getColumn());
            return;
        }

        if (!isMovable(piece)) return;

        getSelection(piece, true);
        alert.clearAlert();
    }

    //retrieves the boardPiece given the gamePiece
    public BoardPiece getBoardPiece(GamePiece gamePiece) {
        for (Object object : root3D.getChildren()) {
            if (object.getClass() != BoardPiece.class) continue;

            if (((BoardPiece) object).getPieceId() != gamePiece.getId()) continue;

            return (BoardPiece) object;
        }
        return null;
    }

    //sets the player's camera to the correct side based on player color
    public void setAlignment() {
        if ((isMultiPlayer && instanceColor == Colors.WHITE) || (!isMultiPlayer && currentColor == Colors.WHITE)) {
            angleY.set(0);
            angleX.set(50);
        } else {
            angleY.set(180);
            angleX.set(50);
        }
    }

    //goes to next turn when called by server
    public void toNextMove() {
        this.currentColor = instanceColor;
        new nextTurnThread();
    }

    //ends the game when called by server
    public void endGame(Player playerWon) {
        Rectangle bg = new Rectangle();

        bg.setHeight(HEIGHT);
        bg.setWidth(WIDTH);
        bg.setFill(Color.BLACK);
        bg.opacityProperty().set(0.5);

        Text winnerText = new Text();
        winnerText.setFont(Font.font("Verdana", 20));
        winnerText.setText("Game Ended! " + playerWon.getColor() + " won!");
        winnerText.setTranslateX((WIDTH - winnerText.getLayoutBounds().getWidth()) / 2);
        winnerText.setTranslateY(HEIGHT / 2);
        winnerText.setFill(Color.WHITE);

        Button goBack = new Button();
        goBack.setPrefWidth(250);
        goBack.setPrefHeight(50);
        goBack.setTranslateX((WIDTH - 250) / 2);
        goBack.setTranslateY((HEIGHT * 2) / 3);
        goBack.setText("Return");
        goBack.setOnAction(e -> sceneCollector.setSelectionScreen());

        globalRoot.getChildren().addAll(bg,winnerText, goBack);
    }

    class nextTurnThread implements Runnable {

        Thread t;

        nextTurnThread()
        {
            t = new Thread(this);
            t.start(); // Starting the thread
        }

        // execution of thread starts from run() method
        public void run()
        {
            loadBoard();
        }
    }
}

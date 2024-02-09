package com.example.tak;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.scene.shape.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class HelloApplication extends Application {

    public static final float WIDTH = 1400;
    public static final float HEIGHT = 800;

    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;

    private final DoubleProperty angleX = new SimpleDoubleProperty(0);

    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

    public final int SIZE = 6;

    public boolean isSelected = false;
    private final Stack<Piece> pieceSelected = new Stack<>();
    private Colors color;

    @Override
    public void start(Stage stage) throws IOException {

        TakGame game = new TakGame(SIZE);
        color = game.getCurrentPlayer().getColor();
        SmartGroup root = new SmartGroup();

        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseColor(Color.CHOCOLATE);
        mat.setDiffuseMap(new Image(getClass().getResourceAsStream("/com/example/tak/boardTexture.jpg")));

        int length = (SIZE * 110) - 10 + 80;

        Box board = new Box(length, 20, length);
        board.setMaterial(mat);
        root.getChildren().add(board);

        PhongMaterial mat2 = new PhongMaterial();
        mat2.setDiffuseMap(new Image(getClass().getResourceAsStream("/com/example/tak/RockTextures.jpeg")));

        root.getChildren().add(new AmbientLight());

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

                square.setMaterial(mat2);
                square.setRow(r);
                square.setColumn(c);

                square.setOnMousePressed(e -> {
                    movePiece(square.getRow(), square.getColumn(), game, root);
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
                    z += i / 2 == 0 ? 110 : -110;
                } else {
                    x += i / 2 == 0 ? 110 : -110;
                }
            }

            x = distance * -1 + 70;
            z = distance * -1;

        }

        loadBoard(root, game);

        Camera camera = new PerspectiveCamera();

        Scene scene = new Scene(root, WIDTH, HEIGHT, true);
        scene.setCamera(camera);
        scene.setFill(Color.SILVER);
        root.translateXProperty().set(WIDTH/2);
        root.translateYProperty().set(HEIGHT/2);
        root.translateZProperty().set(-100);

        initMouseControl(root, scene, stage);

        stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case W:
                    root.translateZProperty().set(root.getTranslateZ() + 100);
                    break;
                case S:
                    root.translateZProperty().set(root.getTranslateZ() - 100);
                    break;
                case Q:
                    root.rotateX(10);
                    break;
                case E:
                    root.rotateX(-10);
                    break;
                case Z:
                    root.rotateY(10);
                    break;
                case C:
                    root.rotateY(-10);
                    break;
            }
        });

        stage.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getTarget().getClass() != Piece.class && event.getTarget().getClass() != Square.class) {
                undoSelection();
            }
        });

        stage.setTitle("Tak!");
        stage.setScene(scene);
        stage.show();
    }

    private void initMouseControl(SmartGroup root, Scene scene, Stage stage) {
        Rotate xRotate;
        Rotate yRotate;
        root.getTransforms().addAll(
                xRotate = new Rotate(0, Rotate.X_AXIS),
                yRotate = new Rotate(0, Rotate.Y_AXIS)
        );
        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        scene.setOnMousePressed(event -> {
            if (isSelected) return;

            anchorX = event.getSceneX();
            anchorY = event.getSceneY();

            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
        });

        scene.setOnMouseDragged(event -> {
            if (isSelected) return;

            double x = anchorAngleX - (anchorY - event.getSceneY());
            double y = anchorAngleY + (anchorX - event.getSceneX());


            angleX.set(x);
            angleY.set(y);
        });

        stage.addEventHandler(ScrollEvent.SCROLL, event -> {
            if (isSelected) return;

            double delta = event.getDeltaY();
            root.translateZProperty().set(root.getTranslateZ() - delta);
        });
    }

    public static void main(String[] args) {
        launch();
    }

    public void loadBoard(SmartGroup root, TakGame game) {
        int numChildren = root.getChildren().size();
        int count = 0;

        //removes all pieces from root

        for (int i = 0; i < numChildren; i++) {
            if (root.getChildren().get(count).getClass() == Piece.class) {
                root.getChildren().remove(count);
            } else {
                count++;
            }
        }

        //places all pieces on the board

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Stack<TakGame.Piece> square = game.getSquare(r, c);

                int order = 0;

                for (int i = 0; i < square.size(); i++) {
                    TakGame.Piece gamepiece = square.get(i);

                    Colors color = gamepiece.getColor();
                    PieceType pieceType = gamepiece.getType();

                    Piece piece = new Piece();
                    piece.setGamePiece(gamepiece);
                    piece.setOnBoard(true);

                    convertPieceShape(piece, pieceType);

                    int XOffset = (c * 110) - (SIZE - 1) * 55;
                    int YOffset = pieceType == PieceType.BISHOP ? (int) (-11 - (order * 11) - (piece.getHeight() / 2)) : -17 + (order * -11);
                    int ZOffset = (r * 110) - (SIZE - 1) * 55;

                    piece.translateXProperty().set(XOffset);
                    piece.translateYProperty().set(YOffset);
                    piece.translateZProperty().set(ZOffset);

                    piece.setBoardPositionX(XOffset);
                    piece.setBoardPositionY(YOffset);
                    piece.setBoardPositionZ(ZOffset);

                    PhongMaterial mat = new PhongMaterial();
                    Color fxColor = color.equals(Colors.WHITE) ? Color.WHITE : Color.BLACK;
                    mat.setDiffuseColor(fxColor);
                    piece.setMaterial(mat);

                    piece.setOnMouseClicked(e -> {
                        if (!pieceSelected.isEmpty()) {
                            movePiece(piece.getRow(), piece.getColumn(), game, root);
                            return;
                        }

                        if (!isMovable(piece, game)) return;

                        Stack<TakGame.Piece> selectedSquare = game.getSquare(piece.getRow(), piece.getColumn());

                        for (int j = 0; j < selectedSquare.size(); j++) {
                            TakGame.Piece selPiece = selectedSquare.get(j);

                            if (selPiece.getOrder() >= piece.getOrder()) {
                                int diffOrder = selPiece.getOrder() - piece.getOrder();
                                int yValue = pieceType == PieceType.BISHOP ? (int) ((-236 + diffOrder * -11) - (piece.getHeight() / 2)) : -236 + diffOrder * -11;
                                selPiece.getBoardPiece().translateYProperty().set(yValue);

                                pieceSelected.add(selPiece.getBoardPiece());
                            }
                        }
                    });

                    root.getChildren().add(piece);
                    order++;
                }
            }
        }

        //places all player pieces on the sides of board

        for (TakGame.Player player: game.getPlayers()) {

            Colors playerColor = player.getColor();
            ArrayList<Stack<TakGame.Piece>> playerPieces = player.getPlayerPieces();

            for (int s = 0; s < playerPieces.size(); s++) {
                Stack<TakGame.Piece> stack = playerPieces.get(s);

                for (int order = 0; order < stack.size(); order++) {
                    Piece piece = new Piece();
                    TakGame.Piece gamePiece = stack.get(order);
                    piece.setGamePiece(gamePiece);
                    piece.setOnBoard(false);

                    PieceType pieceType = gamePiece.getType();
                    convertPieceShape(piece, pieceType);

                    int XOffset = (SIZE * 55 - 55) * -1 + (s * 110);
                    int YOffset = pieceType == PieceType.BISHOP ? (int) (-11 - (order * 11) - (piece.getHeight() / 2)) : (order % 15 * -11) - 16;
                    int ZOffset = (SIZE * 55 + 115);

                    if (playerColor == Colors.WHITE) ZOffset *= -1;

                    piece.translateXProperty().set(XOffset);
                    piece.translateYProperty().set(YOffset);
                    piece.translateZProperty().set(ZOffset);
                    
                    piece.setBoardPositionX(XOffset);
                    piece.setBoardPositionY(YOffset);
                    piece.setBoardPositionZ(ZOffset);

                    piece.setOnMouseClicked(e -> {
                        if (piece.getColor() != game.getCurrentPlayer().getColor()) return;

                        if (pieceSelected.isEmpty()) {
                            undoSelection();
                            pieceSelected.add(piece);
                            piece.translateYProperty().set(-236);

                        } else if (pieceSelected.get(0) != piece) {
                            return;
                        } else if (piece.getType() == PieceType.FLAT) {
                            piece.setType(PieceType.STANDING);
                            convertPieceShape(piece, PieceType.STANDING);
                        } else if (piece.getType() == PieceType.STANDING) {
                            piece.setType(PieceType.FLAT);
                            convertPieceShape(piece, PieceType.FLAT);
                        }
                    });


                    PhongMaterial mat = new PhongMaterial();
                    Color fxColor = playerColor.equals(Colors.WHITE) ? Color.WHITE : Color.BLACK;
                    mat.setDiffuseColor(fxColor);
                    piece.setMaterial(mat);

                    root.getChildren().add(piece);
                }
            }
        }

    }

    public void undoSelection() {
        for (Piece piece: pieceSelected) {
            if (!piece.getOnBoard()) convertPieceShape(piece, PieceType.FLAT);

            piece.translateXProperty().set(piece.getBoardPositionX());
            piece.translateYProperty().set(piece.getBoardPositionY());
            piece.translateZProperty().set(piece.getBoardPositionZ());
        }

        pieceSelected.clear();
    }

    public void convertPieceShape(Piece p, PieceType type) {

        p.getTransforms().clear();

        int[] dimensions = new int[3];

        switch (type) {
            case FLAT ->
                    dimensions = new int[]{80, 10, 80};
            case STANDING -> {
                dimensions = new int[]{20, 50, 80};
                Transform t = new Rotate(45, Rotate.Y_AXIS);
                p.getTransforms().add(t);
            }
            case BISHOP ->
                    dimensions = new int[]{40, 80, 40};
        }

        p.setWidth(dimensions[0]);
        p.setHeight(dimensions[1]);
        p.setDepth(dimensions[2]);
    }

    public void movePiece(int toRow, int toColumn, TakGame game, SmartGroup root) {
        if (pieceSelected.isEmpty()) {
            System.out.println("No Piece is Selected");
            return;
        }

        Piece piece = pieceSelected.get(0);
        int fromRow = piece.getRow();
        int fromColumn = piece.getColumn();
        int order = piece.getOrder();

        if (!piece.getOnBoard() && game.isValidMove(toRow, toColumn)) {
            game.placePiece(toRow, toColumn, piece.getGamePiece());
        } else if (piece.getOnBoard() && game.isValidMove(fromRow, fromColumn, toRow, toColumn, order)) {
            game.moveStack(fromRow, fromColumn, toRow, toColumn, order);
        } else {
            System.out.println("Invalid Move");
            return;
        }

        pieceSelected.clear();
        color = game.getCurrentPlayer().getColor();
        loadBoard(root, game);
    }

    public boolean isMovable(Piece piece, TakGame game) {
        Stack<TakGame.Piece> square = game.getSquare(piece.getRow(), piece.getColumn());
        TakGame.Piece gamePiece = square.peek();

        if (!gamePiece.getColor().equals(color)) return false;

        return gamePiece.getType().equals(PieceType.FLAT) || piece.getOrder() == gamePiece.getOrder();
    }

    class SmartGroup extends Group {
        Rotate r;
        Transform t = new Rotate();

        void rotateX(int ang) {
            r = new Rotate(ang, Rotate.X_AXIS);
            t = t.createConcatenation(r);
            this.getTransforms().clear();
            this.getTransforms().addAll(t);
        }

        void rotateY(int ang) {
            r = new Rotate(ang, Rotate.Y_AXIS);
            t = t.createConcatenation(r);
            this.getTransforms().clear();
            this.getTransforms().addAll(t);
        }

    }

    class Square extends Box {
        int row, column;

        void setRow (int row) {
            this.row = row;
        }

        void setColumn (int column) {
            this.column = column;
        }

        int getRow () {
            return row;
        }

        int getColumn () {
            return column;
        }

    }

    class Piece extends Box{

        private TakGame.Piece gamePiece;
        private Boolean onBoard;

        public Boolean getOnBoard() {
            return onBoard;
        }

        public void setOnBoard(Boolean onBoard) {
            this.onBoard = onBoard;
        }

        int boardPositionX, boardPositionY, boardPositionZ;

        public void setBoardPositionX(int boardPositionX) {
            this.boardPositionX = boardPositionX;
        }

        int getBoardPositionX() {
            return boardPositionX;
        }

        public int getBoardPositionY() {
            return boardPositionY;
        }

        public void setBoardPositionY(int boardPositionY) {
            this.boardPositionY = boardPositionY;
        }

        public int getBoardPositionZ() {
            return boardPositionZ;
        }

        public void setBoardPositionZ(int boardPositionZ) {
            this.boardPositionZ = boardPositionZ;
        }


        void setGamePiece(TakGame.Piece gamePiece) {
            this.gamePiece = gamePiece;
            gamePiece.setBoardPiece(this);
        }

        TakGame.Piece getGamePiece() {
            return gamePiece;
        }

        Colors getColor() {
            return gamePiece.getColor();
        }

        void setRow(int row) {
            gamePiece.setRow(row);
        }

        void setColumn(int column) {
            gamePiece.setRow(column);
        }

        int getRow() {
            return gamePiece.getRow();
        }

        int getColumn() {
            return gamePiece.getColumn();
        }

        void setOrder(int order) {
            gamePiece.setOrder(order);
        }

        int getOrder() {return gamePiece.getOrder();}

        void setType(PieceType type) {
            gamePiece.setType(type);
        }

        PieceType getType() {return gamePiece.getType();}

    }


}
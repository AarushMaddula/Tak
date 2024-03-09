module com.example.tak {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.tak.game to javafx.fxml;
    exports com.tak.game;
}
module com.example.music_player {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.net.http;
    requires com.google.gson;
    requires java.sql;


    opens com.example.music_player to javafx.fxml;
    exports com.example.music_player;
}
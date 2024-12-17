package com.example.music_player;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class App extends Application {
    private Stage primaryStage;
    private Stage lyricsStage;
    private static App instance;

    @Override
    public void start(Stage primaryStage) throws IOException {
        instance = this;
        this.primaryStage = primaryStage;
        loadAuthenticationScene();

        primaryStage.setTitle("CASSETTE MUSIC PLAYER");
        primaryStage.setMinWidth(789.0);
        primaryStage.setMinHeight(404.0);
        primaryStage.show();
    }

    public static App getInstance() {
        return instance;
    }

    public void loadAuthenticationScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("AuthenticationForm.fxml"));
        Scene authScene = new Scene(fxmlLoader.load());
        primaryStage.setScene(authScene);
        primaryStage.setResizable(false);
        primaryStage.setFullScreen(false);
    }

    public void loadMusicPlayerScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("MusicPlayerForm.fxml"));
        Scene musicPlayerScene = new Scene(fxmlLoader.load());
        primaryStage.setScene(musicPlayerScene);
        primaryStage.setResizable(true);
        primaryStage.setFullScreen(false);
    }

    public void setLyricsStage(Stage lyricsStage) {
        this.lyricsStage = lyricsStage;
    }

    public void closeLyricsStage() {
        if (lyricsStage != null) {
            lyricsStage.close();
            lyricsStage = null; // Reset to prevent memory leaks
        }
    }

    public static void main(String[] args) {
        launch();
    }
}

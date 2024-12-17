package com.example.music_player;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.nio.file.*;
import java.io.File;
import java.util.Collections;

public class MusicPlayerFormController {

    @FXML
    private VBox BorderPane_bottom , BorderPane_left;

    @FXML
    private TableView<Song> songlist_tableview;

    @FXML
    private Label title;

    @FXML
    private ImageView playPauseBtn, speakerBtn, repeatBtn, shuffleBtn, nextBtn, previousBtn , lyricsBtn;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Slider volumeSlider;

    @FXML
    private TextField searchBox;

    @FXML
    private Button searchBtn;

    private ObservableList<Song> songList = FXCollections.observableArrayList();
    private MediaPlayer mediaPlayer;

    private boolean isPlaying = false;
    private boolean isShuffle = false;
    private boolean isRepeatCurrent = false;
    private boolean isRepeatAll = false;
    private int loopState = 0;
    private boolean isLyrics = false;

    private double previousVolume = 5; // Track previous volume level (default is 5)

    private final Image playImage = new Image(getClass().getResource("/icons/play.png").toExternalForm());
    private final Image pauseImage = new Image(getClass().getResource("/icons/pause.png").toExternalForm());
    private final Image muteImage = new Image(getClass().getResource("/icons/mute.png").toExternalForm());
    private final Image unmuteImage = new Image(getClass().getResource("/icons/unmute.png").toExternalForm());
    private final Image loopAllImage = new Image(getClass().getResource("/icons/loopAll.png").toExternalForm());
    private final Image loopCurrentImage = new Image(getClass().getResource("/icons/loopCurrent.png").toExternalForm());

    @FXML
    public void initialize() {
        // Set initial volume to 5
        volumeSlider.setValue(5); // Set the slider to 5

        // Setup TableView columns

        TableColumn<Song, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());

        TableColumn<Song, String> artistColumn = new TableColumn<>("Artist");
        artistColumn.setCellValueFactory(cellData -> cellData.getValue().artistProperty());

        TableColumn<Song, String> durationColumn = new TableColumn<>("Duration");
        durationColumn.setCellValueFactory(cellData -> cellData.getValue().durationProperty());

        songlist_tableview.getColumns().addAll(titleColumn, artistColumn, durationColumn);

        // Load songs into the TableView
        loadSongs();

        songlist_tableview.setItems(songList);

        // Configure volume slider
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue() / 100.0); // Set media player volume based on slider value
            }
            updateSpeakerIcon(newValue.doubleValue());  // Update speaker icon based on volume slider
        });

        // Progress bar interaction listeners
        progressBar.setOnMousePressed(event -> {
            if (mediaPlayer != null) {
                double clickX = event.getX();
                double width = progressBar.getWidth();
                double progress = clickX / width;
                mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(progress));

                // If the song is paused, start it from the new position
                if (!isPlaying) {
                    mediaPlayer.play();
                    playPauseBtn.setImage(pauseImage); // Update play/pause button
                    isPlaying = true;
                }
            }
        });



        progressBar.setOnMouseDragged(event -> {
            if (mediaPlayer != null) {
                double dragX = event.getX();
                double width = progressBar.getWidth();
                double progress = dragX / width;
                mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(progress));

                // If the song is paused, start it from the new position
                if (!isPlaying) {
                    mediaPlayer.play();
                    playPauseBtn.setImage(pauseImage); // Update play/pause button
                    isPlaying = true;
                }
            }
        });

        songlist_tableview.setOnMouseClicked(this::onSongClicked);

        monitorMusicDirectory();
    }

    public void loadSongs() {
        File musicDirectory = new File("C:\\Music"); // Replace with your music folder path
        File[] musicFiles = musicDirectory.listFiles((dir, name) -> name.endsWith(".mp3"));

        if (musicFiles != null) {
            for (File file : musicFiles) {
                String fileName = file.getName().replace(".mp3" , ""); // remove .mp3 from the name
                String[] parts = fileName.split("-");
                if(parts.length == 2)
                {
                    String title = parts[0].trim();
                    String artist = parts[1].trim();
                    String duration = "----";

                    Song song = new Song(title,artist,duration,file);
                    songList.add(song);
                }
            }
        }
    }

    private void monitorMusicDirectory() {
        File musicDirectory = new File("C:\\Music"); // Replace with your music folder path

        new Thread(() -> {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path path = musicDirectory.toPath();
                path.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY
                );

                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        Path filePath = path.resolve((Path) event.context());
                        File file = filePath.toFile();

                        Platform.runLater(() -> {
                            if (kind == StandardWatchEventKinds.ENTRY_CREATE && file.getName().endsWith(".mp3")) {
                                addNewSong(file);
                            } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY && file.getName().endsWith(".mp3")) {
                                updateExistingSong(file);
                            } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                removeSong(file);
                            }
                        });
                    }
                    key.reset();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void addNewSong(File file) {
        String fileName = file.getName().replace(".mp3", "").trim();
        String[] parts = fileName.split(" - ", 2);

        if (parts.length == 2) {
            String title = parts[0].trim();
            String artist = parts[1].trim();
            String duration = "----"; // Default duration

            Song newSong = new Song(title, artist, duration, file);
            if (songList.stream().noneMatch(song -> song.getFile().equals(file))) {
                songList.add(newSong);
            }
        } else {
            System.err.println("Invalid file naming convention: " + fileName);
        }

        songlist_tableview.refresh();
    }

    private void updateExistingSong(File file) {
        Song existingSong = songList.stream()
                .filter(song -> song.getFile().equals(file))
                .findFirst()
                .orElse(null);

        if (existingSong != null) {
            String fileName = file.getName().replace(".mp3", "").trim();
            String[] parts = fileName.split(" - ", 2);

            if (parts.length == 2) {
                String newTitle = parts[0].trim();
                String newArtist = parts[1].trim();
                existingSong.setTitle(newTitle);
                existingSong.setArtist(newArtist);
                songlist_tableview.refresh();
            } else {
                System.err.println("Invalid file naming convention for updated file: " + fileName);
            }
        }
    }

    private void removeSong(File file) {
        songList.removeIf(song -> song.getFile().equals(file));
        songlist_tableview.refresh();
    }


    @FXML
    public void onSongClicked(MouseEvent event) {
        Song selectedSong = songlist_tableview.getSelectionModel().getSelectedItem();

        // Ensure a valid song is selected and it's not the currently playing song
        if (selectedSong != null && selectedSong.getFile().exists()) {
            // Only play if the selected song is not the currently playing song
            if (mediaPlayer == null || !selectedSong.equals(getCurrentSong())) {
                playSong(selectedSong);
            }
        } else {
            // Clear the selection if the clicked row is invalid
            songlist_tableview.getSelectionModel().clearSelection();
        }
    }

    private Song getCurrentSong() {
        if (mediaPlayer == null) {
            return null;
        }

        for (Song song : songList) {
            if (song.getFile().toURI().toString().equals(mediaPlayer.getMedia().getSource())) {
                return song;
            }
        }
        return null;
    }


    public void playSong(Song song) {

        if(isLyrics)
        {
            closeLyricsWindow();
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        Media media = new Media(song.getFile().toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        // Set the initial volume to match the volume slider
        mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);

        mediaPlayer.setOnReady(() -> {
            String duration = formatDuration(mediaPlayer.getMedia().getDuration());
            song.setDuration(duration);
            title.setText(song.getTitle());

            mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (mediaPlayer.getTotalDuration() != null) {
                    progressBar.setProgress(newValue.toSeconds() / mediaPlayer.getTotalDuration().toSeconds());
                }
            });

            mediaPlayer.play();
            playPauseBtn.setImage(pauseImage);
            isPlaying = true;
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            if (isRepeatCurrent) {
                mediaPlayer.seek(mediaPlayer.getStartTime());  // Repeat current song
                mediaPlayer.play();
            }
            else if(isRepeatAll)
            {
                if(isShuffle)
                {
                    int randomIndex = (int) (Math.random() * songList.size());
                    Song randomSong = songList.get(randomIndex);
                    songlist_tableview.getSelectionModel().select(randomSong);
                    playSong(randomSong);

                } else
                {
                    playNextMusic(null);
                }

            }
            else {
                playPauseBtn.setImage(playImage);
                isPlaying = false;
            }
        });
    }



    private String formatDuration(javafx.util.Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @FXML
    void playPauseMusic(MouseEvent event) {
        if (mediaPlayer == null) return;

        if (isPlaying) {
            mediaPlayer.pause();
            playPauseBtn.setImage(playImage);
        } else {
            if (mediaPlayer.getCurrentTime().greaterThanOrEqualTo(mediaPlayer.getTotalDuration())) {
                // If at the end of the song, reset to the start and play
                mediaPlayer.seek(mediaPlayer.getStartTime());
            }
            mediaPlayer.play();
            playPauseBtn.setImage(pauseImage);
        }
        isPlaying = !isPlaying;
    }

    @FXML
    public void playPreviousMusic(MouseEvent mouseEvent) {

        if(isLyrics)
        {
            closeLyricsWindow();
        }

        int currentIndex = songlist_tableview.getSelectionModel().getSelectedIndex();

        if (isRepeatCurrent) {
            // If repeat is on, restart the current song
            Song currentSong = songList.get(currentIndex);
            songlist_tableview.getSelectionModel().select(currentSong);
            playSong(currentSong);
        } else {
            if(isShuffle)
            {
                int randomIndex = (int) (Math.random() * songList.size());
                Song randomSong = songList.get(randomIndex);
                songlist_tableview.getSelectionModel().select(randomSong);
                playSong(randomSong);
            }
            else
            {
                if (currentIndex > 0) {
                    Song prevSong = songList.get(currentIndex - 1);
                    songlist_tableview.getSelectionModel().select(prevSong);
                    playSong(prevSong);
                } else {
                    Song lastSong = songList.get(songList.size() - 1);
                    songlist_tableview.getSelectionModel().select(lastSong);
                    playSong(lastSong);
                }
            }
        }
    }

    @FXML
    public void playNextMusic(MouseEvent mouseEvent) {

        if(isLyrics)
        {
            closeLyricsWindow();
        }

        int currentIndex = songlist_tableview.getSelectionModel().getSelectedIndex();

        if (isRepeatCurrent) {
            // If repeat is on, restart the current song
            Song currentSong = songList.get(currentIndex);
            songlist_tableview.getSelectionModel().select(currentSong);
            playSong(currentSong);
        } else {
            if(isShuffle)
            {
                int randomIndex = (int) (Math.random() * songList.size());
                Song randomSong = songList.get(randomIndex);
                songlist_tableview.getSelectionModel().select(randomSong);
                playSong(randomSong);
            }
            else
            {
                if (currentIndex < songList.size() - 1) {
                    Song nextSong = songList.get(currentIndex + 1);
                    songlist_tableview.getSelectionModel().select(nextSong);
                    playSong(nextSong);
                } else {
                    Song firstSong = songList.get(0);
                    songlist_tableview.getSelectionModel().select(firstSong);
                    playSong(firstSong);
                }
            }
        }
    }

    @FXML
    public void shuffleMusicList(MouseEvent mouseEvent) {
        isShuffle = !isShuffle;
        if (isShuffle) {
            shuffleBtn.getStyleClass().add("glow-subtle"); // Add glow effect
        } else {
            shuffleBtn.getStyleClass().remove("glow-subtle"); // Remove glow effect
        }
    }


    @FXML
    public void repeatCurrentMusic(MouseEvent mouseEvent) {

        loopState = (loopState + 1) % 3;

        repeatBtn.getStyleClass().remove("glow-subtle");
        switch (loopState) {
            case 0: // no loop , no glow
                repeatBtn.setImage(loopAllImage);
                repeatBtn.getStyleClass().remove("glow-subtle");
                isRepeatCurrent = false;
                isRepeatAll = false;
                break;

            case 1: // LoopAll (with glow)
                repeatBtn.setImage(loopAllImage);
                repeatBtn.getStyleClass().add("glow-subtle");
                isRepeatCurrent = false;
                isRepeatAll = true;
                break;

            case 2: // LoopCurrent (with glow)
                repeatBtn.setImage(loopCurrentImage);
                repeatBtn.getStyleClass().add("glow-subtle");
                isRepeatCurrent = true;
                isRepeatAll = false;
                break;
        }
    }



    public void deviceFilesLabelClicked(MouseEvent mouseEvent) {
        showAlert(Alert.AlertType.INFORMATION, "Message" ,"Feature is still in developement");
    }

    public void exploreLabelClicked(MouseEvent mouseEvent) {
        showAlert(Alert.AlertType.INFORMATION,"Message" ,"Feature is still in developement");
    }

    public void playlistLabelClicked(MouseEvent mouseEvent) {
        showAlert(Alert.AlertType.INFORMATION, "Message" ,"Feature is still in developement");
    }

    // alert box
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void muteUnmuteSpeaker(MouseEvent mouseEvent) {
        if (mediaPlayer != null) {
            boolean isMuted = mediaPlayer.isMute();

            // If muted, unmute and restore volume
            if (isMuted) {
                mediaPlayer.setMute(false); // Unmute the speaker
                volumeSlider.setValue(previousVolume); // Restore previous volume
                speakerBtn.setImage(unmuteImage); // Change icon to unmuted
            }
            // If unmuted, mute and set volume to 0
            else {
                previousVolume = volumeSlider.getValue(); // Save current volume before muting
                mediaPlayer.setMute(true); // Mute the speaker
                volumeSlider.setValue(0); // Set the volume slider to 0
                speakerBtn.setImage(muteImage); // Change icon to muted
            }
        }
    }
    // Update speaker icon based on the volume level
    private void updateSpeakerIcon(double volume) {
        if (volume == 0) {
            speakerBtn.setImage(muteImage); // Show mute icon if volume is 0
        } else {
            speakerBtn.setImage(unmuteImage); // Show unmute icon if volume is above 0
        }
    }

    @FXML
    public void menuBtnClicked(MouseEvent mouseEvent) {
        if (BorderPane_left.isVisible()) {

            BorderPane_left.setVisible(false);
            BorderPane_left.setManaged(false); // Prevent layout from reserving space for the hidden pane
            songlist_tableview.prefWidthProperty().bind(BorderPane_bottom.widthProperty()); // Expand TableView
        } else {
            // Show the left pane and reset the TableView's width
            BorderPane_left.setVisible(true); // Show the pane
            BorderPane_left.setManaged(true); // Allow layout to reserve space
            songlist_tableview.prefWidthProperty().unbind(); // Unbind to reset the width
            songlist_tableview.setPrefWidth(BorderPane_bottom.getWidth() - BorderPane_left.getWidth()); // Adjust width
        }
    }



    @FXML
    private void fetchLyricsAction() {
        Song selectedSong = songlist_tableview.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {

            // Glow effect toggle for lyricsBtn
            isLyrics = !isLyrics;

            if (isLyrics)
                lyricsBtn.getStyleClass().add("glow-subtle");
            else
                lyricsBtn.getStyleClass().remove("glow-subtle");

            String artist = selectedSong.getArtist();
            String title = selectedSong.getTitle();

            // Fetch lyrics
            String lyrics = LyricsFetcher.fetchLyrics(artist, title);

            lyrics = lyrics.replaceAll("\\n+", "\n").trim();  // Replace multiple newlines with a single one to make lyrics equi-distant

            // TextArea to display the lyrics
            TextArea lyricsTextArea = new TextArea(lyrics);
            lyricsTextArea.setEditable(false); // non-editable
            lyricsTextArea.setWrapText(true);

            // Set dark background and white text color, no extra line spacing
            lyricsTextArea.setStyle("-fx-background-color: #2e2e2e; -fx-text-fill: black; "
                    + "-fx-font-size: 16px; -fx-font-family: 'Arial';"
                    + "-fx-line-spacing: 0px; -fx-padding: 10px;"
                    + "-fx-border-width: 0px;");


            Stage lyricsStage = new Stage();
            lyricsStage.setTitle(title + " - " + artist);


            Scene lyricsScene = new Scene(lyricsTextArea, 404, 596);

            lyricsStage.setScene(lyricsScene);
            App.getInstance().setLyricsStage(lyricsStage);
            lyricsStage.setAlwaysOnTop(true);
            lyricsStage.show();

            lyricsStage.setOnCloseRequest(event -> {
                closeLyricsWindow();
            });
        }
    }

    private void closeLyricsWindow() {
        isLyrics = false;
        lyricsBtn.getStyleClass().remove("glow-subtle");
        App.getInstance().closeLyricsStage();
    }


    public void searchBtnClicked(MouseEvent mouseEvent) {

    }
}

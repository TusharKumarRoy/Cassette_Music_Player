package com.example.music_player;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;

public class Song {

    private StringProperty title;
    private StringProperty artist;
    private StringProperty duration;
    private File file;

    public Song(String title, String artist, String duration, File file) {
        this.title = new SimpleStringProperty(title);
        this.artist = new SimpleStringProperty(artist);
        this.duration = new SimpleStringProperty(duration);
        this.file = file;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty artistProperty() {
        return artist;
    }

    public StringProperty durationProperty() {
        return duration;
    }

    public File getFile() {
        return file;
    }



    public String getTitle() {
        return title.get();
    }

    public String getArtist() {
        return artist.get();
    }

    public String getDuration() {
        return duration.get();
    }

    public void setTitle(String newTitle) {
        this.title.set(newTitle);
    }

    public void setArtist(String newArtist) {
        this.artist.set(newArtist);
    }
    public void setDuration(String duration) {
        this.duration.set(duration);
    }
}

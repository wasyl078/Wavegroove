package com.example.wavegroove.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Song implements Serializable {

    // Variables
    @PrimaryKey
    public long songId;
    private String title;
    private String album;
    private String artist;
    private String uri;
    private String duration;
    private String imageUri;

    // Constructor
    public Song(String title, String album, String artist, String uri, String duration, String imageUri) {
        this.songId = uri.hashCode();
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.uri = uri;
        this.duration = duration;
        this.imageUri = imageUri;
    }

    // Getters
    public long getSongId() {
        return songId;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public String getUri() {
        return uri;
    }

    public String getDuration() {
        return duration;
    }

    public String getImageUri() {
        return imageUri;
    }

    // Setters
    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setImage(String duration) {
        this.duration = duration;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
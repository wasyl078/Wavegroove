package com.example.wavegroove.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Playlist implements Serializable {
    // Variables
    @PrimaryKey
    public long playlistId;
    private String name;
    private String coverUri;

    // Constructor
    public Playlist(long playlistId, String name, String coverUri) {
        this.playlistId = playlistId;
        this.name = name;
        this.coverUri = coverUri;
    }

    // Getters
    public long getPlaylistId() {
        return playlistId;
    }

    public String getName() {
        return name;
    }

    public String getCoverUri() {
        return coverUri;
    }

    // Setters
    public void setPlaylistId(long playlistId) {
        this.playlistId = playlistId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCoverUri(String coverUri) {
        this.coverUri = coverUri;
    }
}
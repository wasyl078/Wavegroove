package com.example.wavegroove.database;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PlaylistDB {
    @Embedded
    Playlist playlist;

    @Relation(
            parentColumn = "playlistId",
            entityColumn = "songId",
            associateBy = @Junction(PlaylistSongDB.class)
    )
    List<Song> songs;

    public PlaylistDB(Playlist playlist, List<Song> songs) {
        this.playlist = playlist;
        this.songs = songs;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public List<Song> getSongs() {
        Collections.sort(songs, (s1, s2) -> s1.getTitle().compareToIgnoreCase(s2.getTitle()));
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }
}

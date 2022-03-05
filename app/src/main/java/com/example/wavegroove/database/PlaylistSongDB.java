package com.example.wavegroove.database;


import androidx.room.Entity;
import androidx.room.ForeignKey;

import java.util.UUID;

@Entity(primaryKeys = {"playlistId", "songId"},
        foreignKeys = {
                @ForeignKey(entity = Playlist.class, parentColumns = "playlistId", childColumns = "playlistId"),
                @ForeignKey(entity = Song.class, parentColumns = "songId", childColumns = "songId")
        })
public class PlaylistSongDB {
    long playlistId;
    long songId;
    long id;

    public PlaylistSongDB(long playlistId, long songId) {
        long id = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
        this.playlistId = playlistId;
        this.songId = songId;
    }

    public long getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(long playlistId) {
        this.playlistId = playlistId;
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}

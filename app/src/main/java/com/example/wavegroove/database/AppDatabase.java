package com.example.wavegroove.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Song.class, Playlist.class, PlaylistSongDB.class}, version = 118)
public abstract class AppDatabase extends RoomDatabase {
    public abstract GlobalDao globalDao();
}
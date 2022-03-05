package com.example.wavegroove.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface GlobalDao {

    // Song
    @Query("SELECT * FROM Song ORDER BY title")
    List<Song> getAllSongs();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllSongs(List<Song> songs);

    @Query("DELETE FROM Song")
    void nukeTableSong();

    @Query("DELETE FROM SONG WHERE songId = :songId")
    void deleteSongById(long songId);

    // Playlist
    @Query("SELECT * FROM Playlist ORDER BY name")
    List<Playlist> getAllPlaylists();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllPlayLists(List<Playlist> playlists);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlayList(Playlist playlist);

    @Query("DELETE FROM Playlist")
    void nukeTablePlaylist();

    @Query("DELETE FROM Playlist WHERE playlistId = :playlistId")
    void deletePlaylist(long playlistId);

    // PlaylistDB
    @Transaction
    @Query("SELECT * FROM Playlist ORDER BY name")
    List<PlaylistDB> getAllPlaylistsDB();

    // PlaylistSongDB
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllPlaylistSongDB(List<PlaylistSongDB> playlistSongDBS);

    @Query("DELETE FROM PlaylistSongDB WHERE playlistId = :playlistId AND songId =:trackId")
    void deleteByPlaylistIdAndTrackId(long playlistId, long trackId);

    @Query("DELETE FROM PlaylistSongDB WHERE playlistId = :playlistId")
    void deleteByPlaylistId(long playlistId);

    @Query("DELETE FROM PlaylistSongDB WHERE songId =:songId")
    void deleteConnectionBySongId(long songId);

    @Query("DELETE FROM PlaylistSongDB")
    void nukeTablePlaylistSongDB();
}

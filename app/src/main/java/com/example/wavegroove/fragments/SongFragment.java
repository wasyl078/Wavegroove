package com.example.wavegroove.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.wavegroove.R;
import com.example.wavegroove.database.Playlist;
import com.example.wavegroove.database.Song;
import com.example.wavegroove.general.MainActivity;
import com.example.wavegroove.general.Utils;

public class SongFragment extends Fragment {

    // Consts & Variables
    private MainActivity thisActivity;
    private TextView titleTextView;
    private TextView artistTextView;
    private TextView albumTextView;
    private TextView durationStartTextView;
    private TextView durationEndTextView;
    private ImageView songImageView;
    private ImageButton playPauseImageButton;
    private ImageButton shuffleImageButton;
    private ImageButton fastRewindImageButton;
    private ImageButton fastForwardImageButton;
    private ImageButton repeatImageButton;
    private ProgressBar songProgressBar;
    private Song actualSong;

    // Constructor
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up views and animations
        setUpViewsAndAnimations();

        // Set up listeners
        setUpListeners();

        // Update
        thisActivity.getService().repairAllViews();
    }


    // Set up views and animations
    private void setUpViewsAndAnimations(){
        thisActivity = (MainActivity) getActivity();
        titleTextView = thisActivity.findViewById(R.id.songTitleTextView);
        artistTextView = thisActivity.findViewById(R.id.songArtistTextView);
        albumTextView = thisActivity.findViewById(R.id.songAlbumTextView);
        durationStartTextView = thisActivity.findViewById(R.id.songDurationStartTextView);
        durationEndTextView = thisActivity.findViewById(R.id.songDurationEndTextView);
        songImageView = thisActivity.findViewById(R.id.songImageView);
        playPauseImageButton = thisActivity.findViewById(R.id.playPauseImageButton);
        shuffleImageButton = thisActivity.findViewById(R.id.shuffleImageButton);
        fastRewindImageButton = thisActivity.findViewById(R.id.fastRewindImageButton);
        fastForwardImageButton = thisActivity.findViewById(R.id.fastForwardImageButton);
        repeatImageButton = thisActivity.findViewById(R.id.repeatImageButton);
        songProgressBar = thisActivity.findViewById(R.id.songProgressBar);

        Utils.addScaleUpDownAnimation(thisActivity, playPauseImageButton);
        Utils.addScaleUpDownAnimation(thisActivity, shuffleImageButton);
        Utils.addScaleUpDownAnimation(thisActivity, fastRewindImageButton);
        Utils.addScaleUpDownAnimation(thisActivity, fastForwardImageButton);
        Utils.addScaleUpDownAnimation(thisActivity, repeatImageButton);
    }

    // Set up listeners
    private void setUpListeners() {
        playPauseImageButton.setOnClickListener(v -> {
            if (thisActivity.getService().getActualSong() == null)
                thisActivity.getService().changeActualSong(actualSong);
            else
                thisActivity.getService().playPausePressed();
        });
        shuffleImageButton.setOnClickListener(v -> thisActivity.getService().shuffleModePressed());
        fastRewindImageButton.setOnClickListener(v -> thisActivity.getService().fastRewindPressed());
        fastForwardImageButton.setOnClickListener(v -> thisActivity.getService().fastForwardPressed());
        repeatImageButton.setOnClickListener(v -> thisActivity.getService().queueModePressed());
    }

    // Updating actual view
    public void updateView(Song song, Drawable playPauseIcon, String imageUri, Drawable shuffleIcon, Drawable queueIcon) {
        if (song != null) {
            titleTextView.setText(song.getTitle());
            artistTextView.setText(song.getArtist());
            albumTextView.setText(song.getAlbum());
            if(imageUri != null)
                Utils.setRoundedImageWithPicasso(thisActivity, songImageView, 370, imageUri);
            else {
                songImageView.setBackgroundResource(R.drawable.square_round_corners_app_icon_blue);
                songImageView.setImageResource(R.drawable.hifi);
            }
            durationEndTextView.setText(song.getDuration());
        }
        playPauseImageButton.setImageDrawable(playPauseIcon);
        shuffleImageButton.setImageDrawable(shuffleIcon);
        repeatImageButton.setImageDrawable(queueIcon);
    }

    // Other
    @Override
    public void onResume() {
        super.onResume();
        for (int i = 0; i < thisActivity.getPlaylistDBS().size(); i++) {
            Playlist bufPlaylist = thisActivity.getPlaylistDBS().get(i).getPlaylist();
            for (int j = 0; j < thisActivity.getPlaylistDBS().get(i).getSongs().size(); j++) {
                Song bufSong = thisActivity.getPlaylistDBS().get(i).getSongs().get(j);
                if (bufSong == actualSong) {
                    thisActivity.updateTopText(bufPlaylist.getName());
                    return;
                }
            }
        }
        thisActivity.updateTopText("playlist or song from playlist was removed");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_song, container, false);
    }

    // Getters & setters
    public Song getActualSong() {
        return actualSong;
    }

    public void setActualSong(Song actualSong) {
        this.actualSong = actualSong;
    }

    public TextView getDurationStartTextView() {
        return durationStartTextView;
    }

    public void setDurationStartTextView(TextView durationStartTextView) {
        this.durationStartTextView = durationStartTextView;
    }

    public ProgressBar getSongProgressBar() {
        return songProgressBar;
    }

    public void setSongProgressBar(ProgressBar songProgressBar) {
        this.songProgressBar = songProgressBar;
    }

}
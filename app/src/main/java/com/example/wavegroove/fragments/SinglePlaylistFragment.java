package com.example.wavegroove.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wavegroove.R;
import com.example.wavegroove.database.PlaylistDB;
import com.example.wavegroove.database.Song;
import com.example.wavegroove.general.MainActivity;
import com.example.wavegroove.general.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

@SuppressWarnings("ALL")
public class SinglePlaylistFragment extends Fragment {

    // Consts & Variables
    public static final int PICK_IMAGE = 110;
    private MainActivity thisActivity;
    private PlaylistDB playlistDB;
    private TextView emptyPlaylistTextView;
    private RelativeLayout addFilesButton;
    private RelativeLayout startPlaylistButton;
    private ListView songsListView;
    private SongsCustomListViewAdapter songsCustomListViewAdapter;

    // Constructor
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up views and animations
        setUpViewsAndAnimations();

        // Set up listeners
        setUpListeners();

        // Move buttons and list view
        updateView();
    }

    // Set up views and animations
    private void setUpViewsAndAnimations() {
        thisActivity = (MainActivity) getActivity();
        Objects.requireNonNull(thisActivity).updateTopText(playlistDB.getPlaylist().getName());
        emptyPlaylistTextView = thisActivity.findViewById(R.id.emptyPlaylistTextView);
        songsListView = thisActivity.findViewById(R.id.playlistSongsListView);
        addFilesButton = thisActivity.findViewById(R.id.addFilesSinglePlaylistButton);
        startPlaylistButton = thisActivity.findViewById(R.id.startPlaylistSinglePlaylistButton);
        ((TextView) addFilesButton.findViewById(R.id.innerTextInCustomButtonTextView)).setText("ADD FILES");
        ((ImageView) addFilesButton.findViewById(R.id.innerImageInCustomButtonImageView)).setImageResource(R.drawable.ic_folder_plus);
        ((TextView) startPlaylistButton.findViewById(R.id.innerTextInCustomButtonTextView)).setText("START PLAYLIST");
        ((ImageView) startPlaylistButton.findViewById(R.id.innerImageInCustomButtonImageView)).setImageResource(R.drawable.ic_play_circle);

        Utils.addScaleUpDownAnimation(thisActivity, addFilesButton);
        Utils.addScaleUpDownAnimation(thisActivity, startPlaylistButton);
    }

    // Set up listeners
    private void setUpListeners() {
        startPlaylistButton.setOnClickListener(v -> {
            for (Song bufSong : playlistDB.getSongs())
                if (Utils.isUriAvailable(thisActivity, bufSong.getUri()))
                    thisActivity.getService().addSongToQueue(bufSong);
        });

        addFilesButton.setOnClickListener(v -> thisActivity.changeFragmentToAddFilesFragment(playlistDB));
    }

    // Updating view
    private void updateView() {
        if (playlistDB.getSongs().size() > 0) {
            // Create songs list
            ArrayList<Song> songs = new ArrayList<>((ArrayList<Song>) playlistDB.getSongs());
            songs.add(0, null);
            songsCustomListViewAdapter = new SongsCustomListViewAdapter(thisActivity, songs);
            songsListView.setAdapter(songsCustomListViewAdapter);

            // Hide empty playlist text
            emptyPlaylistTextView.setVisibility(View.GONE);

            // Show songs list
            songsListView.setVisibility(View.VISIBLE);

            // ADD FILES button to left
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) addFilesButton.getLayoutParams();
            lp.removeRule(RelativeLayout.CENTER_HORIZONTAL);
            lp.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);

            // show START PLAYLIST button
            startPlaylistButton.setVisibility(View.VISIBLE);
        } else {
            // Show empty playlist text
            emptyPlaylistTextView.setVisibility(View.VISIBLE);

            // Hide songs list
            songsListView.setVisibility(View.GONE);

            // ADD FILES button to center
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) addFilesButton.getLayoutParams();
            lp.removeRule(RelativeLayout.ALIGN_PARENT_START);
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

            // Hide START Playlist button
            startPlaylistButton.setVisibility(View.GONE);
        }
    }

    // Dialog window for on three dots click
    public void createDialogWindow(Song song) {
        final Dialog dialog = new Dialog(thisActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.modal_window_song_options);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView titleTextView = dialog.findViewById(R.id.songTitleModalWindowSongTextView);
        titleTextView.setText(song.getTitle());
        addListenersToDialogWindow(dialog, song);
        dialog.show();
        Utils.addScaleUpDownAnimation(thisActivity, dialog.findViewById(R.id.addSongToqueueModalWindowSongButton));
        Utils.addScaleUpDownAnimation(thisActivity, dialog.findViewById(R.id.removeSongFromPlaylistModalWindowSongButton));
    }

    private void addListenersToDialogWindow(Dialog dialog, Song song) {
        View bufView = dialog.findViewById(R.id.addSongToqueueModalWindowSongButton);
        if (Utils.isUriAvailable(thisActivity, song.getUri())) {
            bufView.setOnClickListener(view -> {
                thisActivity.getService().addSongToQueue(song);
                dialog.cancel();
            });
            bufView.setVisibility(View.VISIBLE);
        } else {
            bufView.setVisibility(View.GONE);
        }

        dialog.findViewById(R.id.removeSongFromPlaylistModalWindowSongButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long playlistId = playlistDB.getPlaylist().playlistId;
                thisActivity.getGlobalDao().deleteByPlaylistIdAndTrackId(playlistId, song.songId);
                thisActivity.setPlaylistDBS((ArrayList<PlaylistDB>) thisActivity.getGlobalDao().getAllPlaylistsDB());
                dialog.cancel();
                for (PlaylistDB pl : thisActivity.getPlaylistDBS()) {
                    if (pl.getPlaylist().playlistId == playlistId) {
                        playlistDB = pl;
                        ArrayList<Song> songs = new ArrayList<>((ArrayList<Song>) pl.getSongs());
                        songs.add(0, null);
                        songsCustomListViewAdapter.updateDataSet(songs);
                        break;
                    }
                }
            }
        });
    }

    // On result called - album cover change
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(thisActivity, "Wrong image", Toast.LENGTH_LONG).show();
                return;
            }
            playlistDB.getPlaylist().setCoverUri(data.getData().toString());
            thisActivity.getGlobalDao().insertPlayList(playlistDB.getPlaylist());
            thisActivity.setPlaylistDBS((ArrayList<PlaylistDB>) thisActivity.getGlobalDao().getAllPlaylistsDB());
            ArrayList<Song> songs = new ArrayList<>((ArrayList<Song>) playlistDB.getSongs());
            songs.add(0, null);
            songsCustomListViewAdapter.updateDataSet(songs);
        }
    }

    // Custom songs adapter
    class SongsCustomListViewAdapter extends BaseAdapter {

        LayoutInflater inflter;
        private ArrayList<Song> songs;

        public SongsCustomListViewAdapter(Context context, ArrayList<Song> songs) {
            this.songs = songs;
            inflter = (LayoutInflater.from(context));
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {

            View view = convertView;
            ViewHolder holder;
            if (view == null) {
                LayoutInflater vi = (LayoutInflater) thisActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.song_row, null);
                holder = new ViewHolder();
                holder.songTitleTextView = view.findViewById(R.id.songTitleSongRowTextView);
                holder.artistTextView = view.findViewById(R.id.songArtistSongRowTextView);
                holder.albumTextView = view.findViewById(R.id.songAlbumSongRowTextView);
                holder.durationTextView = view.findViewById(R.id.songDurationSongRowTextView);
                holder.coverImageView = view.findViewById(R.id.songRowImageView);
                holder.threeDots = view.findViewById(R.id.verticalDotsSongRowImageView);
                holder.playlsitCoverImageView = view.findViewById(R.id.singlePlaylistCoverImageView);
                Utils.addScaleUpDownAnimation(thisActivity, holder.threeDots);
                Utils.addScaleUpDownAnimation(thisActivity, view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            if (i == 0)
                return firstAlbumCoverRow(holder, view);
            else
                return otherRowsStandardSongs(holder, view, i);
        }

        private View firstAlbumCoverRow(ViewHolder holder, View view) {
            holder.songTitleTextView.setVisibility(View.GONE);
            holder.artistTextView.setVisibility(View.GONE);
            holder.albumTextView.setVisibility(View.GONE);
            holder.durationTextView.setVisibility(View.GONE);
            holder.threeDots.setVisibility(View.GONE);
            holder.coverImageView.setVisibility(View.GONE);
            holder.playlsitCoverImageView.setVisibility(View.VISIBLE);

            String coverUri = playlistDB.getPlaylist().getCoverUri();
            if (coverUri != null) {
                Uri uri = Uri.parse(coverUri);
                Utils.setRoundedImageWithPicasso(thisActivity, holder.playlsitCoverImageView, 170, uri);
            }
            view.setOnClickListener(v -> {
                final Dialog dialog = new Dialog(thisActivity);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.modal_window_single_playlists_options);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                TextView titleTextView = dialog.findViewById(R.id.playlistNameModalWindowPlaylistTextView);
                titleTextView.setText(playlistDB.getPlaylist().getName());
                titleTextView.setSelected(true);

                dialog.findViewById(R.id.changePlaylistCoverModalWindowSinglePlaylistButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                        dialog.cancel();
                    }
                });
                dialog.show();
                Utils.addScaleUpDownAnimation(thisActivity, dialog.findViewById(R.id.changePlaylistCoverModalWindowSinglePlaylistButton));
            });
            return view;
        }

        private View otherRowsStandardSongs(ViewHolder holder, View view, int i) {
            holder.songTitleTextView.setVisibility(View.VISIBLE);
            holder.artistTextView.setVisibility(View.VISIBLE);
            holder.albumTextView.setVisibility(View.VISIBLE);
            holder.durationTextView.setVisibility(View.VISIBLE);
            holder.threeDots.setVisibility(View.VISIBLE);
            holder.coverImageView.setVisibility(View.VISIBLE);
            holder.playlsitCoverImageView.setVisibility(View.GONE);

            holder.songTitleTextView.setText(songs.get(i).getTitle());
            holder.artistTextView.setText(songs.get(i).getArtist());
            holder.albumTextView.setText(songs.get(i).getAlbum());
            holder.durationTextView.setText(songs.get(i).getDuration());
            holder.threeDots.setOnClickListener(v -> createDialogWindow(songs.get(i)));

            if (Utils.isUriAvailable(thisActivity, songs.get(i).getUri())) {
                // Available
                holder.songTitleTextView.setTextColor(Color.BLACK);
                holder.artistTextView.setTextColor(Color.BLACK);
                holder.albumTextView.setTextColor(Color.BLACK);
                holder.durationTextView.setTextColor(Color.BLACK);
                view.setEnabled(true);

                if (songs.get(i).getImageUri() != null) {
                    Utils.setRoundedImageWithPicasso(thisActivity, holder.coverImageView, 60, songs.get(i).getImageUri());
                } else {
                    holder.coverImageView.setBackgroundResource(R.drawable.square_round_corners_app_icon_blue);
                    holder.coverImageView.setImageResource(R.drawable.hifi);
                }
                holder.coverImageView.getBackground().setColorFilter(null);
                holder.coverImageView.setColorFilter(null);
                view.setOnClickListener(v -> thisActivity.getService().changeActualSong(songs.get(i)));
            } else {
                // Not available
                holder.songTitleTextView.setTextColor(Color.GRAY);
                holder.artistTextView.setTextColor(Color.GRAY);
                holder.albumTextView.setTextColor(Color.GRAY);
                holder.durationTextView.setTextColor(Color.GRAY);
                view.setEnabled(false);

                if (songs.get(i).getImageUri() != null) {
                    Utils.setRoundedErrorImageWithPicasso(thisActivity, holder.coverImageView, 60, songs.get(i).getImageUri());
                } else {
                    holder.coverImageView.setBackgroundResource(R.drawable.square_round_corners_app_icon_blue);
                    holder.coverImageView.setImageResource(R.drawable.ic_alert_octagon);
                    holder.coverImageView.setColorFilter(Color.GRAY);
                    PorterDuffColorFilter greyFilter = new PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                    holder.coverImageView.getBackground().setColorFilter(greyFilter);
                    holder.coverImageView.setColorFilter(greyFilter);
                }
            }
            return view;
        }

        @Override
        public int getCount() {
            return songs.size();
        }

        @Override
        public Object getItem(int i) {
            return songs.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public void updateDataSet(ArrayList<Song> songs) {
            this.songs.clear();
            this.songs.addAll(songs);
            this.notifyDataSetChanged();
            updateView();
        }
    }

    static class ViewHolder {
        ImageView threeDots;
        ImageView playlsitCoverImageView;
        ImageView coverImageView;
        TextView songTitleTextView;
        TextView artistTextView;
        TextView albumTextView;
        TextView durationTextView;
    }

    // Other


    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_single_playlist, container, false);
    }

    public void setPlaylist(PlaylistDB playlistDB) {
        this.playlistDB = playlistDB;
    }
}
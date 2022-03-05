package com.example.wavegroove.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wavegroove.R;
import com.example.wavegroove.database.Playlist;
import com.example.wavegroove.database.PlaylistDB;
import com.example.wavegroove.general.MainActivity;
import com.example.wavegroove.general.Utils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class AllPlaylistsFragment extends Fragment {

    // Consts & Variables
    public static final int PICK_IMAGE = 111;
    private MainActivity thisActivity;
    private GridView customGridView;
    private PlaylistDB bufPlaylsit;
    private PlaylistListCustomAdapter playlistListCustomAdapter;

    // Constructor
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up
        thisActivity = (MainActivity) getActivity();
        Objects.requireNonNull(thisActivity).updateTopText("list of your playlists");
        if (thisActivity.getPlaylistDBS().size() == 0)
            createNewPlaylist();

        // All playlists custom grid set up
        customgirdViewSetUp();
    }

    // Creating new playlist
    public void createNewPlaylist() {
        final Dialog dialog = new Dialog(thisActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.modal_window_create_new_playlist);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        addListenersToDialogWindow(dialog);
        dialog.show();
        Utils.addScaleUpDownAnimation(thisActivity, dialog.findViewById(R.id.acceptNewPlaylistButton));
        Utils.addScaleUpDownAnimation(thisActivity, dialog.findViewById(R.id.cancelNewPlaylistButton));
    }

    private void addListenersToDialogWindow(Dialog dialog) {
        dialog.findViewById(R.id.acceptNewPlaylistButton).setOnClickListener(view -> {
            EditText newNameEditText = dialog.findViewById(R.id.newPlaylistNameEditText);
            String newName = newNameEditText.getText().toString();
            newNameEditText.setBackgroundResource(R.drawable.text_field_normal);

            boolean properName = true;
            for (PlaylistDB pl : thisActivity.getPlaylistDBS())
                if (pl.getPlaylist().getName().equals(newName)) {
                    properName = false;
                    break;
                }

            if (properName) {
                long newId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
                thisActivity.getGlobalDao().insertPlayList(new Playlist(newId, newName, null));
                thisActivity.setPlaylistDBS((ArrayList<PlaylistDB>) thisActivity.getGlobalDao().getAllPlaylistsDB());
                dialog.cancel();
                playlistListCustomAdapter.updateDataSet();
            } else {
                newNameEditText.setBackgroundResource(R.drawable.text_field_error);
            }
        });
        dialog.findViewById(R.id.cancelNewPlaylistButton).setOnClickListener(view -> dialog.cancel());
    }

    // All playlists custom grid set up
    private void customgirdViewSetUp() {
        customGridView = thisActivity.findViewById(R.id.customGridView);
        playlistListCustomAdapter = new PlaylistListCustomAdapter(getActivity());
        customGridView.setAdapter(playlistListCustomAdapter);
        customGridView.setOnItemClickListener((adapterView, view1, i, l) -> thisActivity.changeFragmentToSinglePlaylistFragment(thisActivity.getPlaylistDBS().get(i)));
        customGridView.setOnItemLongClickListener((parent, view, position, id) -> {
            createDialogWindow(thisActivity.getPlaylistDBS().get(position));
            return true;
        });
    }

    // Dialog window - single playlist options
    public void createDialogWindow(PlaylistDB playlistDB) {
        final Dialog dialog = new Dialog(thisActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.modal_window_playlists_options);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView titleTextView = dialog.findViewById(R.id.playlistNameModalWindowPlaylistTextView);
        titleTextView.setText(playlistDB.getPlaylist().getName());
        titleTextView.setSelected(true);
        addListenersToOnePlaylistOptionsDialogWindow(dialog, playlistDB);
        dialog.show();
    }

    private void addListenersToOnePlaylistOptionsDialogWindow(Dialog dialog, final PlaylistDB playlistDB) {
        dialog.findViewById(R.id.addFilesToPlaylistModalWindowPlaylistButton).setOnClickListener(view -> {
            thisActivity.changeFragmentToAddFilesFragment(playlistDB);
            dialog.cancel();
        });
        dialog.findViewById(R.id.deletePlaylistModalWindowPlaylistButton).setOnClickListener(v -> {
            long playlistId = playlistDB.getPlaylist().playlistId;
            thisActivity.getGlobalDao().deleteByPlaylistId(playlistId);
            thisActivity.getGlobalDao().deletePlaylist(playlistId);
            thisActivity.setPlaylistDBS((ArrayList<PlaylistDB>) thisActivity.getGlobalDao().getAllPlaylistsDB());
            playlistListCustomAdapter.updateDataSet();
            dialog.cancel();
        });
        dialog.findViewById(R.id.changePlaylistCoverModalWindowPlaylistButton).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            bufPlaylsit = playlistDB;
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            dialog.cancel();
        });
        Utils.addScaleUpDownAnimation(thisActivity, dialog.findViewById(R.id.addFilesToPlaylistModalWindowPlaylistButton));
        Utils.addScaleUpDownAnimation(thisActivity, dialog.findViewById(R.id.deletePlaylistModalWindowPlaylistButton));
        Utils.addScaleUpDownAnimation(thisActivity, dialog.findViewById(R.id.changePlaylistCoverModalWindowPlaylistButton));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null || data.getData() == null) {
                Toast.makeText(thisActivity, "Wrong image", Toast.LENGTH_LONG).show();
                return;
            }
            bufPlaylsit.getPlaylist().setCoverUri(data.getData().toString());
            thisActivity.getGlobalDao().insertPlayList(bufPlaylsit.getPlaylist());
            thisActivity.setPlaylistDBS((ArrayList<PlaylistDB>) thisActivity.getGlobalDao().getAllPlaylistsDB());
            playlistListCustomAdapter.updateDataSet();
        }
    }

    // My custom adapter for playlists
    class PlaylistListCustomAdapter extends BaseAdapter {
        Context context;
        LayoutInflater inflter;

        public PlaylistListCustomAdapter(Context context) {
            this.context = context;
            inflter = (LayoutInflater.from(context));
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.playlist_row, null);
            Playlist playlist = thisActivity.getPlaylistDBS().get(i).getPlaylist();
            TextView playlistNameTextView = view.findViewById(R.id.playlistNameTextView);
            playlistNameTextView.setText(playlist.getName());
            playlistNameTextView.setSelected(true);

            // Album cover
            if (playlist.getCoverUri() != null) {
                ImageView albumcoverImageView = view.findViewById(R.id.singlePlaylistImageView);
                Uri coverUri = Uri.parse(playlist.getCoverUri());
                Utils.setRoundedImageWithPicasso(thisActivity, albumcoverImageView, 150, coverUri);
            }

            Utils.addScaleUpDownAnimation(thisActivity, view);
            return view;
        }

        @Override
        public int getCount() {
            return thisActivity.getPlaylistDBS().size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        public void updateDataSet() {
            thisActivity.getPlaylistDBS().clear();
            thisActivity.getPlaylistDBS().addAll(thisActivity.getGlobalDao().getAllPlaylistsDB());
            this.notifyDataSetChanged();
        }
    }

    // Other
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_playlists, container, false);
    }
}
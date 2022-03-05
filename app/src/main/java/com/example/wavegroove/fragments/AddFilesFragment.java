package com.example.wavegroove.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.wavegroove.R;
import com.example.wavegroove.database.PlaylistDB;
import com.example.wavegroove.database.PlaylistSongDB;
import com.example.wavegroove.database.Song;
import com.example.wavegroove.general.MainActivity;
import com.example.wavegroove.general.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;

public class AddFilesFragment extends Fragment {

    // Consts & Variables
    private MainActivity thisActivity;
    private PlaylistDB playlistDB;
    private ArrayList<Song> audioFiles;
    private ListView importedSongsListView;
    private TextView emptyFileListTextView;
    private RelativeLayout importFromMemoryButton;
    private RelativeLayout addSelectedButton;
    private int EXPECTED_RESULT_CODE = 43;

    // Constructor
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up views and animations
        setUpViewsAndAnimations();

        // Listeners
        addAddSelectedButtonListener();
        addImportFromMemoryButtonListener();

        // Refresh views
        updateView();
    }

    // Set up views and animations
    private void setUpViewsAndAnimations(){
        thisActivity = (MainActivity) getActivity();
        Objects.requireNonNull(thisActivity).updateTopText("file list");
        audioFiles = (ArrayList<Song>) thisActivity.getGlobalDao().getAllSongs();
        importedSongsListView = thisActivity.findViewById(R.id.importedFilesListView);
        emptyFileListTextView = thisActivity.findViewById(R.id.emptyFileListTextView);
        importFromMemoryButton = thisActivity.findViewById(R.id.importFromMemoryButton);
        addSelectedButton = thisActivity.findViewById(R.id.addSelectedToPlaylistButton);
        ((TextView) importFromMemoryButton.findViewById(R.id.innerTextInCustomButtonTextView)).setText("IMPORT FROM MEMORY");
        ((TextView) addSelectedButton.findViewById(R.id.innerTextInCustomButtonTextView)).setText("ADD SELECTED TO PLAYLIST");
        ((ImageView) addSelectedButton.findViewById(R.id.innerImageInCustomButtonImageView)).setImageResource(R.drawable.ic_add_circle);
        Utils.addScaleUpDownAnimation(thisActivity, importFromMemoryButton);
        Utils.addScaleUpDownAnimation(thisActivity, addSelectedButton);
    }

    // Add Selected Button Listener
    private void addAddSelectedButtonListener() {
        addSelectedButton.setOnClickListener(v -> {
            // Get checked songs
            ArrayList<Song> audioFilesToAdd = new ArrayList<>();
            ArrayList<PlaylistSongDB> playlistSongDBS = new ArrayList<>();
            CustomListViewAdapter adapter = (CustomListViewAdapter) importedSongsListView.getAdapter();

            for (int i = 0; i < adapter.songs.size(); i++) {
                boolean chosenAt = adapter.isChosenAt(i);
                boolean alreadyAdded = false;

                for (Song song : playlistDB.getSongs())
                    if (adapter.songs.get(i).getSongId() == song.getSongId()) {
                        alreadyAdded = true;
                        break;
                    }

                if (chosenAt && !alreadyAdded) {
                    audioFilesToAdd.add(audioFiles.get(i));
                    playlistSongDBS.add(new PlaylistSongDB(playlistDB.getPlaylist().getPlaylistId(), audioFiles.get(i).getSongId()));
                }
            }

            // Insert songs
            thisActivity.getGlobalDao().insertAllSongs(audioFilesToAdd);
            audioFiles = (ArrayList<Song>) thisActivity.getGlobalDao().getAllSongs();
            thisActivity.getGlobalDao().insertAllPlaylistSongDB(playlistSongDBS);
            long plaulistId = playlistDB.getPlaylist().getPlaylistId();
            thisActivity.setPlaylistDBS((ArrayList<PlaylistDB>) thisActivity.getGlobalDao().getAllPlaylistsDB());
            for (int i = 0; i < thisActivity.getPlaylistDBS().size(); i++)
                if (thisActivity.getPlaylistDBS().get(i).getPlaylist().getPlaylistId() == plaulistId)
                    playlistDB = thisActivity.getPlaylistDBS().get(i);

            // Go to single playlist view
            thisActivity.changeFragmentToSinglePlaylistFragment(playlistDB);
        });
    }

    // Import From Memory Button Listener
    private void addImportFromMemoryButtonListener() {
        importFromMemoryButton.setOnClickListener(v -> {
            Intent filesIntent;
            filesIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            filesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            filesIntent.addCategory(Intent.CATEGORY_OPENABLE);
            filesIntent.setType("*/*");
            String[] mimetypes = {"audio/wave", "audio/mpeg"};
            filesIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            startActivityForResult(filesIntent, EXPECTED_RESULT_CODE);
        });
    }

    // Function called after returning from file menager
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<Song> newSongs = new ArrayList<>();
        if (requestCode == EXPECTED_RESULT_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        newSongs.add(createSingleSong(uri));
                    }
                } else {
                    Uri uri = data.getData();
                    newSongs.add(createSingleSong(uri));
                }
            }
            if (newSongs.size() > 0) {
                thisActivity.getGlobalDao().insertAllSongs(newSongs);
                audioFiles = (ArrayList<Song>) thisActivity.getGlobalDao().getAllSongs();
                updateView();
            }
        }
    }

    // Making single song object from Uri
    private Song createSingleSong(Uri uri) {
        thisActivity.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        // Init
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        System.out.println(uri);
        mmr.setDataSource(thisActivity, uri);
        String title;
        String album;
        String artist;
        String duration;
        String imageUri = null;

        // Title
        try {
            title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (title.equals(""))
                title = "Unknown Title";
        } catch (NullPointerException e) {
            title = "Unknown Title";
        }

        // Album
        try {
            album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            if (album.equals(""))
                album = "Unknown Album";
        } catch (NullPointerException e) {
            album = "Unknown Album";
        }

        // Artist
        try {
            artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if (artist.equals(""))
                artist = "Unknown Artist";
        } catch (NullPointerException e) {
            artist = "Unknown Artist";
        }

        // Cover
        byte[] data = mmr.getEmbeddedPicture();
        if (data != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root);
            myDir.mkdirs();
            long buf_id = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
            String fname = buf_id + ".jpg";
            System.out.println(fname);
            File file = new File(myDir, fname);
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            imageUri = file.getAbsolutePath();
        }

        // Duration
        try {
            long durationInMs = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            long minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMs);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMs) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationInMs));
            if (seconds < 10)
                duration = String.format(Locale.ENGLISH, "%d:0%d", minutes, seconds);
            else
                duration = String.format(Locale.ENGLISH, "%d:%d", minutes, seconds);
        } catch (NullPointerException | NumberFormatException e) {
            duration = "XX:XX";
        }

        return new Song(title, album, artist, uri.toString(), duration, imageUri);
    }

    // Updating view
    private void updateView() {
        if (audioFiles.size() > 0) {
            // Create audio files list
            CustomListViewAdapter customListViewAdapter = new CustomListViewAdapter(thisActivity, audioFiles);
            importedSongsListView.setAdapter(customListViewAdapter);
            customListViewAdapter.notifyDataSetChanged();

            // Hide empty file list text
            emptyFileListTextView.setVisibility(View.GONE);

            // Show file list
            importedSongsListView.setVisibility(View.VISIBLE);

            // IMPORT FROM MEMORY button to left
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) importFromMemoryButton.getLayoutParams();
            lp.removeRule(RelativeLayout.CENTER_HORIZONTAL);
            lp.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);

            // show ADD SELECTED TO PLAYLIST button
            addSelectedButton.setVisibility(View.VISIBLE);
        } else {
            // Show empty file list text
            emptyFileListTextView.setVisibility(View.VISIBLE);

            // Hide file list
            importedSongsListView.setVisibility(View.GONE);

            // IMPORT FROM MEMORY button to center
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) importFromMemoryButton.getLayoutParams();
            lp.removeRule(RelativeLayout.ALIGN_PARENT_START);
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

            // Hide ADD SELECTED TO PLAYLIST Playlist button
            addSelectedButton.setVisibility(View.GONE);
        }
    }

    // Dialog window for long press on imported song
    public void createDialogWindow(Song song) {
        final Dialog dialog = new Dialog(thisActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.modal_window_imported_song_options);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView titleTextView = dialog.findViewById(R.id.songTitleModalWindowImportedSongTextView);
        titleTextView.setText(song.getTitle());
        dialog.findViewById(R.id.deleteSongModalWindowImportedSongButton).setOnClickListener((View.OnClickListener) v -> {
            thisActivity.getGlobalDao().deleteConnectionBySongId(song.getSongId());
            thisActivity.getGlobalDao().deleteSongById(song.getSongId());
            audioFiles = (ArrayList<Song>) thisActivity.getGlobalDao().getAllSongs();
            thisActivity.setPlaylistDBS((ArrayList<PlaylistDB>) thisActivity.getGlobalDao().getAllPlaylistsDB());
            dialog.cancel();
            updateView();
        });
        dialog.show();
        Utils.addScaleUpDownAnimation(thisActivity, dialog.findViewById(R.id.deleteSongModalWindowImportedSongButton));
    }

    // Custom audio files list adapter
    class CustomListViewAdapter extends BaseAdapter {

        LayoutInflater inflter;
        private ArrayList<Song> songs;
        private HashMap<Integer, Boolean> choosen;

        public CustomListViewAdapter(Context context, ArrayList<Song> songs) {
            Collections.sort(songs, (s1, s2) -> s1.getTitle().compareToIgnoreCase(s2.getTitle()));
            this.songs = songs;
            inflter = (LayoutInflater.from(context));
            choosen = new HashMap<>();
        }

        public boolean isChosenAt(int i) {
            if (choosen.containsKey(i)) {
                return choosen.get(i);
            } else {
                return true;
            }
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            View view = convertView;
            ViewHolder holder;
            if (view == null) {
                LayoutInflater vi = (LayoutInflater) thisActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.imported_song_row, null);
                holder = new ViewHolder();
                holder.songTitleTextView = view.findViewById(R.id.songTitleImportedSongRowTextView);
                holder.artistTextView = view.findViewById(R.id.songArtistImportedSongRowTextView);
                holder.albumTextView = view.findViewById(R.id.songAlbumImportedSongRowTextView);
                holder.durationTextView = view.findViewById(R.id.songDurationImportedSongRowTextView);
                holder.songCover = view.findViewById(R.id.importedSongRowImageView);
                holder.checkBox = view.findViewById(R.id.toAddImportedSongRowCheckBox);
                holder.checkBox.setChecked(true);
                holder.checkBox.setEnabled(true);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            if (!choosen.containsKey(i))
                choosen.put(i, true);

            boolean alreadyAdded = false;
            for (Song song : playlistDB.getSongs())
                if (songs.get(i).getSongId() == song.getSongId()) {
                    alreadyAdded = true;
                    break;
                }

            view.setOnLongClickListener(v -> {
                createDialogWindow(songs.get(i));
                return false;
            });

            if (alreadyAdded) {
                holder.checkBox.setEnabled(false);
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setEnabled(true);
                holder.checkBox.setChecked(choosen.get(i));
            }

            view.setOnClickListener(v -> {
                if (holder.checkBox.isEnabled()) {
                    holder.checkBox.setChecked(!holder.checkBox.isChecked());
                    choosen.replace(i, holder.checkBox.isChecked());
                }
            });

            holder.checkBox.setOnClickListener(v -> choosen.replace(i, holder.checkBox.isChecked()));
            holder.songTitleTextView.setText(songs.get(i).getTitle());
            holder.artistTextView.setText(songs.get(i).getArtist());
            holder.albumTextView.setText(songs.get(i).getAlbum());
            holder.durationTextView.setText(songs.get(i).getDuration());

            if (Utils.isUriAvailable(thisActivity, songs.get(i).getUri())) {
                // Available
                holder.songTitleTextView.setTextColor(Color.BLACK);
                holder.artistTextView.setTextColor(Color.BLACK);
                holder.albumTextView.setTextColor(Color.BLACK);
                holder.durationTextView.setTextColor(Color.BLACK);
                if (songs.get(i).getImageUri() != null)
                    Utils.setRoundedImageWithPicasso(thisActivity, holder.songCover, 60, songs.get(i).getImageUri());
                else{
                    holder.songCover.setBackgroundResource(R.drawable.square_round_corners_app_icon_blue);
                    holder.songCover.setImageResource(R.drawable.hifi);
                }
            } else {
                // Not available
                holder.songTitleTextView.setTextColor(Color.GRAY);
                holder.artistTextView.setTextColor(Color.GRAY);
                holder.albumTextView.setTextColor(Color.GRAY);
                holder.durationTextView.setTextColor(Color.GRAY);

                if (songs.get(i).getImageUri() != null) {
                    Utils.setRoundedErrorImageWithPicasso(thisActivity, holder.songCover, 60, songs.get(i).getImageUri());
                } else {
                    holder.songCover.setBackgroundResource(R.drawable.square_round_corners_app_icon_blue);
                    holder.songCover.setImageResource(R.drawable.ic_alert_octagon);
                    holder.songCover.setColorFilter(Color.GRAY);
                    PorterDuffColorFilter greyFilter = new PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                    holder.songCover.getBackground().setColorFilter(greyFilter);
                    holder.songCover.setColorFilter(greyFilter);
                }
                holder.checkBox.setEnabled(false);

                if (!alreadyAdded) {
                    holder.checkBox.setChecked(false);
                    choosen.replace(i, false);
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
    }

    static class ViewHolder {
        CheckBox checkBox;
        ImageView songCover;
        TextView songTitleTextView;
        TextView artistTextView;
        TextView albumTextView;
        TextView durationTextView;
    }

    // Other
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_files, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    public void setPlaylist(PlaylistDB playlistDB) {
        this.playlistDB = playlistDB;
    }
}
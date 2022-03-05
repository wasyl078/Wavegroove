package com.example.wavegroove.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
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
import com.example.wavegroove.database.Song;
import com.example.wavegroove.general.MainActivity;
import com.example.wavegroove.general.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class QueueFragment extends Fragment {

    // Consts & Variables
    private MainActivity thisActivity;
    private ListView queueListView;
    private TextView emptyQueueTextView;
    private TextView nextTextView;
    private View queueNowPlayingView;
    private TextView queueTitleTextView;
    private TextView queueArtistTextView;
    private TextView queueAlbumTextView;
    private ImageView queueCoverImageView;

    // Constructor
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up views
        setUpViews();

        // Refresh queue
        updateQueuedSongsView(thisActivity.getService().getQueue(), thisActivity.getService().getActualSong());
    }


    // Set up views
    private void setUpViews() {
        thisActivity = (MainActivity) getActivity();
        Objects.requireNonNull(thisActivity).updateTopText("queue");
        queueListView = thisActivity.findViewById(R.id.queuedSongsListView);
        emptyQueueTextView = thisActivity.findViewById(R.id.emptyQueueTextView);
        nextTextView = thisActivity.findViewById(R.id.nextTextView);
        queueNowPlayingView = thisActivity.findViewById(R.id.queueNowPlayingElement);
        queueTitleTextView = thisActivity.findViewById(R.id.queueTitleTextView);
        queueArtistTextView = thisActivity.findViewById(R.id.queueArtistTextView);
        queueAlbumTextView = thisActivity.findViewById(R.id.queueAlbumTextView);
        queueCoverImageView = thisActivity.findViewById(R.id.queueImageView);
    }

    // Update view
    public void updateQueuedSongsView(ArrayList<Song> queue, Song actualSong) {
        // Now playing
        if (actualSong == null) {
            queueNowPlayingView.setVisibility(View.GONE);
        } else {
            queueNowPlayingView.setVisibility(View.VISIBLE);
            queueTitleTextView.setText(actualSong.getTitle());
            queueArtistTextView.setText(actualSong.getArtist());
            queueAlbumTextView.setText(actualSong.getAlbum());
            if (actualSong.getImageUri() != null) {
                final File file = new File(actualSong.getImageUri());
                Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Bitmap roundedBitmap = Utils.getRoundedCornerBitmap(bitmap, 15);
                        queueCoverImageView.setImageBitmap(roundedBitmap);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onPrepareLoad(final Drawable placeHolderDrawable) {
                    }
                };
                int sizeInPx = Utils.dpToPixels(60);
                Picasso.get().load(file).resize(sizeInPx, sizeInPx).centerCrop().into(target);
            } else {
                queueCoverImageView.setImageResource(R.drawable.hifi);
            }
        }

        // List
        if (queue.size() > 0) {
            QueueCustomListViewAdapter queueCustomListViewAdapter = new QueueCustomListViewAdapter(thisActivity, queue);
            queueListView.setAdapter(queueCustomListViewAdapter);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) queueListView.getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, nextTextView.getId());
            lp.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
            lp.setMargins(0, 15, 0, 0);

            emptyQueueTextView.setVisibility(View.GONE);
            nextTextView.setVisibility(View.VISIBLE);

            if (thisActivity.getService().isShuffling()) {
                nextTextView.setText("probably next");
            } else {
                nextTextView.setText("next");
            }

        } else {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) queueListView.getLayoutParams();
            lp.removeRule(RelativeLayout.BELOW);
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

            emptyQueueTextView.setVisibility(View.VISIBLE);
            nextTextView.setVisibility(View.GONE);
        }
    }

    // Dialog window for on element in queue click
    private void createDialogQueueWindow(Song song, int idx) {
        final Dialog dialog = new Dialog(thisActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.modal_window_queued_song);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView titleTextView = dialog.findViewById(R.id.songNameModalWindowQueuedSongTextView);
        titleTextView.setText(song.getTitle());
        addListenersToDialogWindow(dialog, idx);
        dialog.show();
        Utils.addScaleUpDownAnimation(thisActivity, dialog.findViewById(R.id.moveTopModalWindowQueuedSongButton));
        Utils.addScaleUpDownAnimation(thisActivity, dialog.findViewById(R.id.moveBottonModalWindowQueuedSongButton));
        Utils.addScaleUpDownAnimation(thisActivity, dialog.findViewById(R.id.removeFromQueueCoverModalWindowQueuedSongButton));
    }

    private void addListenersToDialogWindow(Dialog dialog, int idx) {
        dialog.findViewById(R.id.moveTopModalWindowQueuedSongButton).setOnClickListener(v -> {
            thisActivity.getService().moveQueuedSongTop(idx);
            dialog.cancel();
        });
        dialog.findViewById(R.id.moveBottonModalWindowQueuedSongButton).setOnClickListener(v -> {
            thisActivity.getService().moveQueuedSongBottom(idx);
            dialog.cancel();
        });

        dialog.findViewById(R.id.removeFromQueueCoverModalWindowQueuedSongButton).setOnClickListener(v -> {
            thisActivity.getService().removeSongFromQueue(idx);
            dialog.cancel();
        });
    }

    // Custom queue adapter
    class QueueCustomListViewAdapter extends BaseAdapter {

        LayoutInflater inflter;
        private ArrayList<Song> songsInQueue;

        public QueueCustomListViewAdapter(Context context, ArrayList<Song> songs) {
            this.songsInQueue = songs;
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
                view.setTag(holder);
                Utils.addScaleUpDownAnimation(thisActivity, holder.threeDots);
                Utils.addScaleUpDownAnimation(thisActivity, view);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.songTitleTextView.setText(songsInQueue.get(i).getTitle());
            holder.artistTextView.setText(songsInQueue.get(i).getArtist());
            holder.albumTextView.setText(songsInQueue.get(i).getAlbum());
            holder.durationTextView.setText(songsInQueue.get(i).getDuration());

            if (songsInQueue.get(i).getImageUri() != null) {
                Utils.setRoundedImageWithPicasso(thisActivity, holder.coverImageView, 60, songsInQueue.get(i).getImageUri());
            } else {
                holder.coverImageView.setBackgroundResource(R.drawable.square_round_corners_app_icon_blue);
                holder.coverImageView.setImageResource(R.drawable.hifi);
            }

            holder.threeDots.setOnClickListener(v -> createDialogQueueWindow(songsInQueue.get(i), i));
            return view;
        }

        @Override
        public int getCount() {
            return songsInQueue.size();
        }

        @Override
        public Object getItem(int i) {
            return songsInQueue.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }
    }


    static class ViewHolder {
        ImageView threeDots;
        ImageView coverImageView;
        TextView songTitleTextView;
        TextView artistTextView;
        TextView albumTextView;
        TextView durationTextView;
    }

    // Other
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_queue, container, false);
    }
}
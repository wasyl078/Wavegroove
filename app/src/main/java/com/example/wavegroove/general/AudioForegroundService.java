package com.example.wavegroove.general;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.wavegroove.R;
import com.example.wavegroove.database.Song;
import com.example.wavegroove.fragments.QueueFragment;
import com.example.wavegroove.fragments.SongFragment;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class AudioForegroundService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {

    // Consts: notifications
    private static final int NOTIFICATION_WIDGET_ID = 1;
    public static final String NOTIFICATION_CONTENT = "wavegroove is active in background.";
    public static final String CHANNEL_NAME = "Service Name";
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static final String BUTTON_REWIND = "button_rewind";
    public static final String BUTTON_PLAYPAUSE = "button_playpause";
    public static final String BUTTON_FORWARD = "button_forward";

    // Variables: notifications
    private RemoteViews remoteViews;
    private RemoteViews remoteViewsDefault;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;

    // Const & Variables: play modes
    private int shuffleModeActual;
    private int queueModeActual;
    private int nowPlayingActual;
    private static final int SHUFFLE_MODE_OFF = 0;
    private static final int SHUFFLE_MODE_ON = 1;
    private static final int QUEUE_MODE_OFF = 1;
    private static final int QUEUE_MODE_STANDARD = 0;
    private static final int QUEUE_MODE_ONE = 2;
    private static final int NOW_PLAYING_ON = 0;
    private static final int NOW_PLAYING_PAUSED = 1;

    // Const & Variables: audio player
    private int resumePosition;
    private Song actualSong;
    private ArrayList<Song> queue;
    private MainActivity main;
    private MediaPlayer mediaPlayer = null;
    private AudioManager audioManager;

    // Constructor
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaPlayer != null) {
            repairAllViews();
            return START_NOT_STICKY;
        }

        // Set up options
        setUpOptions();

        // Set up notification manager
        setUpNotificationManager();

        // Set up notification buttons
        setUpNotificationButtons();

        // Set up builder and notification
        setUpBuilder();

        // Set up progress-bar-updating thread
        setUpProgressBarUpdatingThread();

        return START_NOT_STICKY;
    }

    // Set up options
    private void setUpOptions() {
        queue = new ArrayList<>();
        shuffleModeActual = SHUFFLE_MODE_OFF;
        queueModeActual = QUEUE_MODE_OFF;
        nowPlayingActual = NOW_PLAYING_PAUSED;
    }

    // Set up notification manager
    private void setUpNotificationManager() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(serviceChannel);
        }
    }

    // Set up notification buttons
    private void setUpNotificationButtons() {
        // Init remote views
        remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification);
        remoteViews.setImageViewResource(R.id.customNotificationImageView, R.drawable.hifi);

        // Rewind notification button
        Intent buttonIntent = new Intent(BUTTON_REWIND);
        buttonIntent.putExtra("id", NOTIFICATION_WIDGET_ID);
        PendingIntent pendingIntentRewind = PendingIntent.getBroadcast(this, 0, buttonIntent, 0);

        // Play-pause notification button
        Intent buttonIntentPlayPause = new Intent(BUTTON_PLAYPAUSE);
        buttonIntentPlayPause.putExtra("id", NOTIFICATION_WIDGET_ID);
        PendingIntent pendingIntentPlayPause = PendingIntent.getBroadcast(this, 0, buttonIntentPlayPause, 0);

        // Forward notification button
        Intent buttonIntentForward = new Intent(BUTTON_FORWARD);
        buttonIntentForward.putExtra("id", NOTIFICATION_WIDGET_ID);
        PendingIntent pendingIntentForward = PendingIntent.getBroadcast(this, 0, buttonIntentForward, 0);

        // Add listeners to remote views
        remoteViews.setOnClickPendingIntent(R.id.customNotificationRewindButton, pendingIntentRewind);
        remoteViews.setOnClickPendingIntent(R.id.customNotificationPlayPauseButton, pendingIntentPlayPause);
        remoteViews.setOnClickPendingIntent(R.id.customNotificationForwardButton, pendingIntentForward);
    }

    // Set up builder and notification
    private void setUpBuilder() {
        Intent notificationIntentOpenApp = new Intent(this, MainActivity.class);
        PendingIntent pendingIntentOpenApp = PendingIntent.getActivity(this, 0, notificationIntentOpenApp, 0);

        remoteViewsDefault = new RemoteViews(getPackageName(), R.layout.custom_notification_nothing_played);
        remoteViews.setImageViewResource(R.id.customNotificationImageView, R.drawable.hifi);

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(NOTIFICATION_CONTENT)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.hifi)
                .setCustomContentView(remoteViews)
                .setContentIntent(pendingIntentOpenApp)
                .setAutoCancel(true)
                .setChannelId(CHANNEL_ID)
                .setOnlyAlertOnce(true)
                .setColor(ContextCompat.getColor(this, R.color.AppIconBlue))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.hifi));
        startForeground(NOTIFICATION_WIDGET_ID, builder.build());

        if (!requestAudioFocus())
            stopSelf();
    }


    // Set up progress-bar-updating thread
    private void setUpProgressBarUpdatingThread() {
        Thread updatingProgressBards = new Thread() {
            public void run() {
                while (true) {
                    try {
                        if (actualSong != null && mediaPlayer != null && mediaPlayer.isPlaying()) {
                            int durationInMs = mediaPlayer.getCurrentPosition();
                            int progressPercentage = (int) Math.ceil(durationInMs * 100.0 / mediaPlayer.getDuration());
                            updateSongFragmentProgressBar(durationInMs, progressPercentage);
                            updateMusicBarProgressBar(progressPercentage);
                            Thread.sleep(50);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        updatingProgressBards.start();
    }

    // In new thread: update progress bar in Song Fragment
    private void updateSongFragmentProgressBar(int durationInMs, int progressPercentage) {
        ProgressBar barFromSongFragment = main.getSongFragment().getSongProgressBar();
        TextView startText = main.getSongFragment().getDurationStartTextView();

        if (barFromSongFragment != null) {
            barFromSongFragment.setProgress(progressPercentage, true);
            String duration;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMs);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMs) - minutes;
            if (seconds < 10)
                duration = String.format(Locale.ENGLISH, "%d:0%d", minutes, seconds);
            else
                duration = String.format(Locale.ENGLISH, "%d:%d", minutes, seconds);
            main.runOnUiThread(() -> startText.setText(duration));
        }
    }

    // In new thread: update music bar in Main Activity
    private void updateMusicBarProgressBar(int progressPercentage) {
        ImageView barFromMusicBar = main.getMusicBarBarImageView();

        if (barFromMusicBar != null) {
            Display display = main.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            main.runOnUiThread(() -> {
                ViewGroup.LayoutParams params = barFromMusicBar.getLayoutParams();
                params.width = width * progressPercentage / 100;
                barFromMusicBar.setLayoutParams(params);
            });
        }
    }

    // Media player init function
    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();

        //Set up event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(actualSong.getUri()));
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    // Audio Player utils
    public void shuffleModePressed() {
        switch (shuffleModeActual) {
            case SHUFFLE_MODE_ON:
                shuffleModeActual = SHUFFLE_MODE_OFF;
                break;
            case SHUFFLE_MODE_OFF:
                shuffleModeActual = SHUFFLE_MODE_ON;
                break;
        }
        repairAllViews();
    }

    public void fastRewindPressed() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            resumePosition = 0;
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
        repairAllViews();
    }

    public void playPausePressed() {
        if (nowPlayingActual == NOW_PLAYING_PAUSED) {
            nowPlayingActual = NOW_PLAYING_ON;
            if (mediaPlayer == null) {
                initMediaPlayer();
            } else {
                mediaPlayer.seekTo(resumePosition);
                mediaPlayer.start();
            }
        } else if (nowPlayingActual == NOW_PLAYING_ON) {
            nowPlayingActual = NOW_PLAYING_PAUSED;
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
        repairAllViews();
    }

    public void fastForwardPressed() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getDuration();
            if (queue.size() == 0) {
                updateMusicBarProgressBar(100);
                updateSongFragmentProgressBar(resumePosition, 100);
            }
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
        repairAllViews();
    }

    public void queueModePressed() {
        switch (queueModeActual) {
            case QUEUE_MODE_STANDARD:
                queueModeActual = QUEUE_MODE_ONE;
                break;
            case QUEUE_MODE_ONE:
                queueModeActual = QUEUE_MODE_OFF;
                break;
            case QUEUE_MODE_OFF:
                queueModeActual = QUEUE_MODE_STANDARD;
                break;
        }
        repairAllViews();
    }

    public void addSongToQueue(Song song) {
        System.out.println("SONG ADDED TO QUEUE: " + song.getTitle());
        queue.add(song);

        if (queue.size() == 1 && nowPlayingActual == NOW_PLAYING_PAUSED) {
            actualSong = queue.get(0);
            queue.clear();
            playPausePressed();
        }
    }

    public void changeActualSong(Song song) {
        System.out.println("ACTUAL SONG CHANGED: " + song.getTitle());
        actualSong = song;
        nowPlayingActual = NOW_PLAYING_ON;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        initMediaPlayer();
        repairAllViews();
    }

    public void moveQueuedSongTop(int idx) {
        System.out.printf("MOVE TOP: %d%n", idx);
        if (idx > 0) {
            Collections.swap(queue, idx - 1, idx);
            repairAllViews();
        }
    }

    public void moveQueuedSongBottom(int idx) {
        System.out.printf("MOVE BOTTOM: %d%n", idx);
        if (idx < queue.size() - 1) {
            Collections.swap(queue, idx, idx + 1);
            repairAllViews();
        }
    }

    public void removeSongFromQueue(int idx) {
        queue.remove(idx);
        repairAllViews();
    }

    public void clearQueue() {
        queue.clear();
        repairAllViews();
    }


    // Universal repairing views method
    public void repairAllViews() {
        Drawable playPauseIcon = null;
        Drawable shuffleModeIcon = null;
        Drawable queueModeIcon = null;
        String imageUri = null;

        // Song cover
        if (actualSong != null) {
            imageUri = actualSong.getImageUri();
        }

        // Play/pause icon
        switch (nowPlayingActual) {
            case NOW_PLAYING_ON:
                playPauseIcon = ContextCompat.getDrawable(main, R.drawable.ic_pause_circle_filled);
                break;
            case NOW_PLAYING_PAUSED:
                playPauseIcon = ContextCompat.getDrawable(main, R.drawable.ic_play_circle_filled);
                break;
        }

        // Shuffle mode icon
        switch (shuffleModeActual) {
            case SHUFFLE_MODE_ON:
                shuffleModeIcon = ContextCompat.getDrawable(main, R.drawable.ic_shuffle);
                break;
            case SHUFFLE_MODE_OFF:
                shuffleModeIcon = ContextCompat.getDrawable(main, R.drawable.ic_continuos_play);
                break;
        }

        // Queue mode icon
        switch (queueModeActual) {
            case QUEUE_MODE_STANDARD:
                queueModeIcon = ContextCompat.getDrawable(main, R.drawable.ic_repeat);
                break;
            case QUEUE_MODE_ONE:
                queueModeIcon = ContextCompat.getDrawable(main, R.drawable.ic_repeat_one);
                break;
            case QUEUE_MODE_OFF:
                queueModeIcon = ContextCompat.getDrawable(main, R.drawable.ic_no_repeat);
                break;
        }

        // Update views
        main.updateMusicBar(actualSong, playPauseIcon, imageUri);
        updateNotification();
        if (main.getSongFragment() != null && Utils.actualFragment(main) instanceof SongFragment)
            main.getSongFragment().updateView(actualSong, playPauseIcon, imageUri, shuffleModeIcon, queueModeIcon);

        if (Utils.actualFragment(main) instanceof QueueFragment)
            main.getQueueFragment().updateQueuedSongsView(queue, actualSong);
    }

    // Notification updates
    public void updateNotification() {
        if (actualSong == null) {
            remoteViewsDefault.setImageViewResource(R.id.customNotificationDefaultImageView, R.drawable.hifi);
            builder.setCustomContentView(remoteViewsDefault);
        } else {
            if (actualSong.getImageUri() != null)
                updateNotificationCoverImageWithPicasso();
            else
                remoteViews.setImageViewResource(R.id.customNotificationImageView, R.drawable.hifi);

            // Play puase button
            if (nowPlayingActual == NOW_PLAYING_ON)
                remoteViews.setInt(R.id.customNotificationPlayPauseButton, "setBackgroundResource", R.drawable.ic_pause_circle_filled);
            else
                remoteViews.setInt(R.id.customNotificationPlayPauseButton, "setBackgroundResource", R.drawable.ic_play_circle_filled);

            // Title, album, artis
            remoteViews.setTextViewText(R.id.customNotificationTitleTextView, actualSong.getTitle());
            remoteViews.setTextViewText(R.id.customNotificationAlbumTextView, actualSong.getAlbum());
            remoteViews.setTextViewText(R.id.customNotificationArtistTextView, actualSong.getArtist());

            builder.setCustomContentView(remoteViews);
        }

        // Refresh
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_WIDGET_ID, builder.build());
    }

    private void updateNotificationCoverImageWithPicasso() {
        final File file = new File(actualSong.getImageUri());
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Bitmap roundedBitmap = Utils.getRoundedCornerBitmap(bitmap, 15);
                remoteViews.setImageViewBitmap(R.id.customNotificationImageView, roundedBitmap);
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
        Picasso.get().load(file).resize(sizeInPx, sizeInPx).centerCrop().placeholder(R.drawable.ic_refresh).into(target);
    }

    // When song ends
    @Override
    public void onCompletion(MediaPlayer mp) {
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
        Song previousSong = actualSong;
        actualSong = null;
        resumePosition = 0;
        nowPlayingActual = NOW_PLAYING_PAUSED;

        if (queueModeActual == QUEUE_MODE_ONE) {
            actualSong = previousSong;
            nowPlayingActual = NOW_PLAYING_ON;
            initMediaPlayer();
        } else if (queue.size() > 0) {
            if (shuffleModeActual == SHUFFLE_MODE_OFF) {
                actualSong = queue.get(0);
                queue.remove(0);
            } else {
                Random rand = new Random();
                int randomIndex = rand.nextInt(queue.size());
                actualSong = queue.get(randomIndex);
                queue.remove(randomIndex);
            }

            if (queueModeActual == QUEUE_MODE_STANDARD)
                queue.add(previousSong);

            nowPlayingActual = NOW_PLAYING_ON;
            initMediaPlayer();
        }
        repairAllViews();
    }

    // Rest of overriden functions
    @Override
    public void onAudioFocusChange(int focusState) {
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer == null && actualSong != null)
                    initMediaPlayer();
                else if (!mediaPlayer.isPlaying())
                    mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer != null && mediaPlayer.isPlaying())
                    mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Toast.makeText(this.main, "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK ", Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Toast.makeText(this.main, "MEDIA ERROR SERVER DIED ", Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Toast.makeText(this.main, "MEDIA ERROR UNKNOWN ", Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
    }

    // Others
    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.release();
        }
        removeAudioFocus();
    }

    IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public AudioForegroundService getServerInstance() {
            return AudioForegroundService.this;
        }
    }

    // Getters & setters
    public Song getActualSong() {
        return actualSong;
    }

    public void setMainActivity(MainActivity main) {
        this.main = main;
    }

    public ArrayList<Song> getQueue() {
        return queue;
    }

    public boolean isShuffling() {
        return shuffleModeActual == SHUFFLE_MODE_ON;
    }
}

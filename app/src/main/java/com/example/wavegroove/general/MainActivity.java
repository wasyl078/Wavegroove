package com.example.wavegroove.general;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wavegroove.R;
import com.example.wavegroove.database.AppDatabase;
import com.example.wavegroove.database.GlobalDao;
import com.example.wavegroove.database.PlaylistDB;
import com.example.wavegroove.database.Song;
import com.example.wavegroove.fragments.AddFilesFragment;
import com.example.wavegroove.fragments.AllPlaylistsFragment;
import com.example.wavegroove.fragments.QueueFragment;
import com.example.wavegroove.fragments.SinglePlaylistFragment;
import com.example.wavegroove.fragments.SongFragment;
import com.jaeger.library.StatusBarUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {

    // Consts & Variables: general
    public static final int MULTIPLE_PERMISSIONS = 10;
    private AppDatabase database;
    private GlobalDao globalDao;
    private AudioForegroundService service;
    private NotificationListener receiver;
    private boolean bounded;
    private ArrayList<PlaylistDB> playlistDBS;

    // Consts & Variables: views
    private View dropdownMenu;
    private View musicBar;
    private ImageView hifiMainButton;
    private ImageView musicBarSongImage;
    private TextView musicBarSongTitle;
    private TextView musicBarSongAlbum;
    private TextView musicBarSongArtist;
    private TextView musicBarSongDuration;
    private ImageButton musicBarFastRewindButton;
    private ImageButton musicBarFastForwardButton;
    private ImageButton musicBarPlayPauseButton;
    private ImageView musicBarBarImageView;
    private FrameLayout mainFrameLayout;
    private TextView actualFragmentTextView;
    private Button createNewPlaylistButton;
    private Button clearQueueButton;

    // Consts & Variables: fragments
    private SongFragment songFragment;
    private QueueFragment queueFragment;
    private AddFilesFragment addFilesFragment;
    private AllPlaylistsFragment allPlaylistsFragment;
    private SinglePlaylistFragment singlePlaylistFragment;

    // Set Caligraphy
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    // Constructor
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Caligraphy
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/gothici.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
        setContentView(R.layout.activity_main);

        // Permissions
        checkPermissions();

        // Set up broadcast receiver
        setUpBroadcastReceiver();

        // Set Status Bar transparent
        StatusBarUtil.setTransparent(this);

        // Set up datatabase
        setUpDatabase();

        // Start service
        startService();

        // Set up views and animations
        setUpViewsAndAnimations();

        // Set up listeners
        addMainLayoutListener();
        addDropdownmenuListener();
        addMusicBarListeners();

        // Set up fragments
        setUpFragments();
    }

    // Permissions
    public boolean checkPermissions() {
        int result;

        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    // Set up broadcast receiver
    private void setUpBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioForegroundService.BUTTON_REWIND);
        filter.addAction(AudioForegroundService.BUTTON_PLAYPAUSE);
        filter.addAction(AudioForegroundService.BUTTON_FORWARD);
        receiver = new NotificationListener();
        registerReceiver(receiver, filter);
    }

    // Set up database
    private void setUpDatabase() {
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database-name")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        globalDao = database.globalDao();
        playlistDBS = (ArrayList<PlaylistDB>) globalDao.getAllPlaylistsDB();
    }

    // Set up views and animations
    private void setUpViewsAndAnimations() {
        // Views
        actualFragmentTextView = findViewById(R.id.actualFragmentTextView);
        dropdownMenu = findViewById(R.id.dropdownMenu);
        musicBarSongImage = findViewById(R.id.songMusicBarImageView);
        musicBarSongTitle = findViewById(R.id.titleMusicBarTextView);
        musicBarSongAlbum = findViewById(R.id.albumMusicBarTextView);
        musicBarSongArtist = findViewById(R.id.artistMusicBarTextView);
        musicBarSongDuration = findViewById(R.id.durationMusicBarTextView);
        musicBarFastRewindButton = findViewById(R.id.fastRewindMusicBarImageButton);
        musicBarFastForwardButton = findViewById(R.id.fastForwardMusicBarImageButton);
        musicBarPlayPauseButton = findViewById(R.id.playPauseMusicBarImageButton);
        musicBarBarImageView = findViewById(R.id.progressBarRedMusiBarImageView);
        musicBar = findViewById(R.id.musicBarIncluded);
        mainFrameLayout = findViewById(R.id.mainFrameLayout);
        hifiMainButton = findViewById(R.id.hifiMainButtonImageView);
        createNewPlaylistButton = findViewById(R.id.createNewPlaylistButton);
        clearQueueButton = findViewById(R.id.clearQueueButton);

        // Animations
        Utils.addScaleUpDownAnimation(this, hifiMainButton);
        Utils.addScaleUpDownAnimation(this, musicBarFastRewindButton);
        Utils.addScaleUpDownAnimation(this, musicBarFastForwardButton);
        Utils.addScaleUpDownAnimation(this, musicBarPlayPauseButton);
        Utils.addScaleUpDownAnimation(this, R.id.allPlaylistsHamburgerImageButton);
        Utils.addScaleUpDownAnimation(this, createNewPlaylistButton);
        Utils.addScaleUpDownAnimation(this, clearQueueButton);
        Utils.addScaleUpDownAnimation(this, R.id.playlistPlayImageView);
        Utils.addScaleUpDownAnimation(this, R.id.playlistNuteImageView);
    }

    // Set up listeners
    private void addMainLayoutListener() {
        View bufView = findViewById(R.id.mainLayout);
        bufView.setOnTouchListener((v, event) -> {
            if (dropdownMenu.getVisibility() == View.VISIBLE)
                dropdownMenu.setVisibility(View.GONE);
            return false;
        });
    }

    private void addDropdownmenuListener() {
        createNewPlaylistButton.setOnClickListener(v -> {
            dropdownMenu.setVisibility(View.GONE);
            allPlaylistsFragment.createNewPlaylist();
        });
        clearQueueButton.setOnClickListener(v -> {
            service.clearQueue();
            dropdownMenu.setVisibility(View.GONE);
        });
    }

    private void addMusicBarListeners() {
        musicBarFastRewindButton.setOnClickListener(v -> service.fastRewindPressed());
        musicBarFastForwardButton.setOnClickListener(v -> service.fastForwardPressed());
        musicBarPlayPauseButton.setOnClickListener(v -> service.playPausePressed());
        hifiMainButton.setOnClickListener(v -> {
            Song actualSong = service.getActualSong();
            if (actualSong != null)
                changeFragmentToSongFragment(actualSong);
        });
    }

    // Set up fragments
    private void setUpFragments() {
        allPlaylistsFragment = new AllPlaylistsFragment();
        singlePlaylistFragment = new SinglePlaylistFragment();
        addFilesFragment = new AddFilesFragment();
        songFragment = new SongFragment();
        queueFragment = new QueueFragment();
        changeFragmentToAllPlaylistsFragment(null);
    }

    // Fragments utils
    public void changeFragmentToAllPlaylistsFragment(View view) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrameLayout, allPlaylistsFragment, "ALLPLAYLISTS");
        fragmentTransaction.commit();
        getSupportFragmentManager().executePendingTransactions();
        if (service != null)
            service.repairAllViews();
    }

    public void changeFragmentToSinglePlaylistFragment(PlaylistDB playlistDB) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrameLayout, singlePlaylistFragment).addToBackStack(null);
        fragmentTransaction.commit();
        singlePlaylistFragment.setPlaylist(playlistDB);
        service.repairAllViews();
    }

    public void changeFragmentToAddFilesFragment(PlaylistDB playlistDB) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrameLayout, addFilesFragment).addToBackStack(null);
        fragmentTransaction.commit();
        addFilesFragment.setPlaylist(playlistDB);
        service.repairAllViews();
    }

    public void changeFragmentToSongFragment(Song song) {
        if (!(Utils.actualFragment(this) instanceof SongFragment)) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.mainFrameLayout, songFragment).addToBackStack(null);
            fragmentTransaction.commit();
        }
        service.repairAllViews();
        songFragment.setActualSong(song);
    }

    public void changeFragmentToQueueFragment(View view) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrameLayout, queueFragment).addToBackStack(null);
        fragmentTransaction.commit();
        getSupportFragmentManager().executePendingTransactions();
        service.repairAllViews();
    }

    // Music bar utils
    public void switchMusicBarOn() {
        musicBar.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mainFrameLayout.getLayoutParams();
        lp.removeRule(RelativeLayout.ABOVE);
        lp.addRule(RelativeLayout.ABOVE, R.id.musicBarIncluded);
        mainFrameLayout.setLayoutParams(lp);
    }

    public void switchMusicBarOff() {
        musicBar.setVisibility(View.GONE);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mainFrameLayout.getLayoutParams();
        lp.removeRule(RelativeLayout.ABOVE);
        lp.addRule(RelativeLayout.ABOVE, R.id.blueBottomNavBarBacgroundImageView);
        mainFrameLayout.setLayoutParams(lp);
    }

    public void updateMusicBar(Song song, Drawable playPauseIcon, String imageUri) {
        // Song fragment should not have Music Bar
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.mainFrameLayout);
        if (f instanceof SongFragment) {
            switchMusicBarOff();
            return;
        }

        if (song != null) {
            // Song is playing -> Music Bar visible
            switchMusicBarOn();
            if (imageUri == null) {
                musicBarSongImage.setBackgroundResource(R.drawable.square_round_corners_app_icon_blue);
                musicBarSongImage.setImageResource(R.drawable.hifi);
            } else {
                Utils.setRoundedImageWithPicasso(this, musicBarSongImage, 50, imageUri);
            }
            musicBarSongTitle.setText(song.getTitle());
            musicBarSongAlbum.setText(song.getAlbum());
            musicBarSongArtist.setText(song.getArtist());
            musicBarSongDuration.setText(song.getDuration());
            musicBarPlayPauseButton.setImageDrawable(playPauseIcon);
            return;
        } else {
            // Song is not playing -> Music Bar not visible
            switchMusicBarOff();
        }
    }

    // Service binding utils
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this, "Service is disconnected", Toast.LENGTH_SHORT).show();
            bounded = false;
            service = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(MainActivity.this, "Service is connected", Toast.LENGTH_SHORT).show();
            bounded = true;
            AudioForegroundService.LocalBinder mLocalBinder = (AudioForegroundService.LocalBinder) service;
            MainActivity.this.service = mLocalBinder.getServerInstance();
            MainActivity.this.service.setMainActivity(MainActivity.this);
            MainActivity.this.service.repairAllViews();
        }
    };

    public void startService() {
        Intent serviceIntent = new Intent(this, AudioForegroundService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
        bindService(serviceIntent, connection, BIND_AUTO_CREATE);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, AudioForegroundService.class);
        stopService(serviceIntent);
    }

    // Other
    public void updateTopText(String text) {
        actualFragmentTextView.setSelected(true);
        actualFragmentTextView.setText(text);
    }

    public void onHamburgerClick(View view) {
        if (dropdownMenu.getVisibility() == View.VISIBLE)
            dropdownMenu.setVisibility(View.GONE);
        else {
            dropdownMenu.setVisibility(View.VISIBLE);
            dropdownMenu.bringToFront();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bounded) {
            unbindService(connection);
            bounded = false;
        }
        stopService();
        System.exit(-1);
    }

    @Override
    public void onBackPressed() {
        if (Objects.requireNonNull(getSupportFragmentManager().findFragmentByTag("ALLPLAYLISTS")).isVisible()) {
            System.exit(0);
        }
        super.onBackPressed();
        service.repairAllViews();
    }

    // Getters
    public GlobalDao getGlobalDao() {
        return globalDao;
    }

    public SongFragment getSongFragment() {
        return songFragment;
    }

    public ArrayList<PlaylistDB> getPlaylistDBS() {
        return playlistDBS;
    }

    public void setPlaylistDBS(ArrayList<PlaylistDB> playlistDBS) {
        this.playlistDBS = playlistDBS;
    }

    public AudioForegroundService getService() {
        return service;
    }

    public TextView getActualFragmentTextView() {
        return actualFragmentTextView;
    }

    public QueueFragment getQueueFragment() {
        return queueFragment;
    }

    public ImageView getMusicBarBarImageView() {
        return musicBarBarImageView;
    }

    public void setMusicBarBarImageView(ImageView musicBarBarImageView) {
        this.musicBarBarImageView = musicBarBarImageView;
    }
}
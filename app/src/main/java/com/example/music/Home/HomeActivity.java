package com.example.music.Home;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chibde.visualizer.BarVisualizer;
import com.chibde.visualizer.CircleBarVisualizer;
import com.chibde.visualizer.CircleVisualizer;
import com.chibde.visualizer.LineBarVisualizer;
import com.chibde.visualizer.LineVisualizer;
import com.chibde.visualizer.SquareBarVisualizer;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.music.Helpers.PlayerService;
import com.example.music.Helpers.Song;
import com.example.music.Helpers.SongAdapter;
import com.example.music.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.Player;
import com.google.android.material.tabs.TabLayout;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class HomeActivity extends AppCompatActivity {

    //Home members are here
    RecyclerView recyclerView;
    SongAdapter songAdapter;
    List<Song> allSongs = new ArrayList<>();
    ActivityResultLauncher<String> storagePermissionLauncher;
    final String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

    ImageView search_btn, back_btn;
    EditText search_box;
    TextView music_txt,NoSongFoundText;
    ExoPlayer player;

    SwipeRefreshLayout swipe_refresh;

    //home player controls are here'
    RelativeLayout homeControlWrapper;
    TextView music_name;
    ImageView previous_icon, play_icon, next_icon;

    TabLayout TabLayout;


    //player view members are here
    ImageView player_fast_rewind_btn, player_fast_forward_btn, player_Settings_btn,
            player_playList_btn, player_play_btn, player_previous_btn, player_next_btn,
            player_repeat_btn, player_shuffle_btn,player_fav_btn;
    TextView player_song_name, total_song_played_duration, total_song_duration;
    SeekBar player_seekbar;
    BarVisualizer Bar_visualizer;
    LineVisualizer Line_visualizer;
    LineBarVisualizer LineBar_visualizer;
    CircleVisualizer Circle_visualizer;
    CircleBarVisualizer CircleBar_visualizer;

    SquareBarVisualizer SquareBar_visualizer;

    RelativeLayout player_view_layout;

    CircleImageView song_image;

    int repeatMode = 0;
    int shuffleMode = 0;

    int progressValue = 0;


    public ActivityResultLauncher<String> recordAudioPermissionLauncher;
    public final String recordAudioPermission = Manifest.permission.RECORD_AUDIO;

    Boolean playHomeValue = false;
    Boolean playPlayerValue = false;

    //is the act bound?
    boolean isBound = false;

    BlurView blurView;


    //    Settings view members are here
    RelativeLayout Settings_View;
    ImageView settings_back;
    TextView settings_music_name;

    CheckBox enable_Forward_Rewind, enable_Repeat_shuffle;
    RadioButton enable_BarVisualizer, enable_LineVisualizer, enable_CircleVisualizer,
            enable_CircleBarVisualizer, enable_LineBarVisualizer, enable_SquareBarVisualizer, None;
    List<Song> filteredList = new ArrayList<>();
    Boolean search = false;

    private ActivityResultLauncher<IntentSenderRequest> songDeleteIntentLauncher;

    Boolean BV = true;
    Boolean LV = false;
    Boolean CV = false;
    Boolean CBV = false;
    Boolean LBV = false;
    Boolean SBV = false;
    Boolean NoneV = false;

    List<Song> playedList = new ArrayList<>();

    List<Song> favList = new ArrayList<>();


    boolean song = true;
    boolean played = false;

    boolean fav = false;

    boolean isfav = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //player controls start here
        doBindService();

        //change status bar color
        changeStatus();

        //home find id
        homeFIndId();

        //player find id
        playerFindId();

        //Settings view find id
        settingFindID();

        //enable repeat, shuffle, forward, rewind
        enableControls();

        //enable visualizers
        enableVisualizer();

        loadFavSong();

        //storage permission launcher
        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                //fetch songs
                fetchSongs();
            } else {
                userResponse();
            }
        });
        //on click of search icon
        search_btn.setOnClickListener(view -> {
            showKeyBoard();
            music_txt.setVisibility(View.GONE);
            search_box.setVisibility(View.VISIBLE);
            search_box.requestFocus();
            YoYo.with(Techniques.BounceInRight)
                    .duration(2000)
                    .playOn(search_box);
            back_btn.setVisibility(View.VISIBLE);
            search_btn.setVisibility(View.GONE);
        });
        //on click of back icon
        back_btn.setOnClickListener(view -> {
            hideKeyBoard();
            music_txt.setVisibility(View.VISIBLE);
            search_box.setVisibility(View.GONE);
            back_btn.setVisibility(View.GONE);
            search_btn.setVisibility(View.VISIBLE);
            search_box.setText("");
        });
        //to filter song
        search_box.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                search = true;
                filteredSong(s.toString());


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //record audio permission launcher
        recordAudioPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted && player.isPlaying()) {
                activateAudioVisualizer();
            } else {
                userResponseForRecordPerm();
            }
        });

        songDeleteIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        fetchSongs();
                    } else {
                        Toast.makeText(this, "Failed try again!", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    //load fav Song
    private void loadFavSong() {
        SharedPreferences favSongs = getSharedPreferences("favSongs",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = favSongs.getString("Titles",null);
        Type type = new TypeToken<List<Song>>(){}.getType();
        favList = gson.fromJson(json,type);

        try {
            if (favList == null){
                favList = new ArrayList<>();
                songAdapter.favSong(favList);
                isfav = true;
            }else {
                songAdapter.favSong(favList);
                isfav = true;
            }
        }catch (Exception e){

        }

    }

    //hide soft keyboard
    private void hideKeyBoard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //show soft key board
    private void showKeyBoard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    //enable Visualizer
    private void enableVisualizer() {
        SharedPreferences getBV = getSharedPreferences("BV", MODE_PRIVATE);
        Boolean getBVValue = getBV.getBoolean("BVV", true);

        SharedPreferences getLV = getSharedPreferences("LV", MODE_PRIVATE);
        Boolean getLVValue = getLV.getBoolean("LVV", false);

        SharedPreferences getCV = getSharedPreferences("CV", MODE_PRIVATE);
        Boolean getCVValue = getCV.getBoolean("CVV", false);

        SharedPreferences getCBV = getSharedPreferences("CBV", MODE_PRIVATE);
        Boolean getCBVValue = getCBV.getBoolean("CBVV", false);

        SharedPreferences getLBV = getSharedPreferences("LBV", MODE_PRIVATE);
        Boolean getLBVValue = getLBV.getBoolean("LBVV", false);

        SharedPreferences getSBV = getSharedPreferences("SBV", MODE_PRIVATE);
        Boolean getSBVValue = getSBV.getBoolean("SBVV", false);

        SharedPreferences getNV = getSharedPreferences("NV", MODE_PRIVATE);
        Boolean getNVValue = getNV.getBoolean("NVV", false);


        if (getBVValue == true) {
                BV = true;
                LV = false;
                CV = false;
                CBV = false;
                LBV = false;
                SBV = false;
                NoneV = false;
            enable_BarVisualizer.setChecked(true);
            enable_LineVisualizer.setChecked(false);
            enable_CircleVisualizer.setChecked(false);
            enable_CircleBarVisualizer.setChecked(false);
            enable_LineBarVisualizer.setChecked(false);
            enable_SquareBarVisualizer.setChecked(false);
            None.setChecked(false);
        }
        if (getLVValue == true) {
                BV = false;
                LV = true;
                CV = false;
                CBV = false;
                LBV = false;
                SBV = false;
                NoneV = false;
            enable_BarVisualizer.setChecked(false);
            enable_LineVisualizer.setChecked(true);
            enable_CircleVisualizer.setChecked(false);
            enable_CircleBarVisualizer.setChecked(false);
            enable_LineBarVisualizer.setChecked(false);
            enable_SquareBarVisualizer.setChecked(false);
            None.setChecked(false);
        }
        if (getCVValue == true) {
                BV = false;
                LV = false;
                CV = true;
                CBV = false;
                LBV = false;
                SBV = false;
                NoneV = false;
            enable_BarVisualizer.setChecked(false);
            enable_LineVisualizer.setChecked(false);
            enable_CircleVisualizer.setChecked(true);
            enable_CircleBarVisualizer.setChecked(false);
            enable_LineBarVisualizer.setChecked(false);
            enable_SquareBarVisualizer.setChecked(false);
            None.setChecked(false);
        }
        if (getCBVValue == true) {
                BV = false;
                LV = false;
                CV = false;
                CBV = true;
                LBV = false;
                SBV = false;
                NoneV = false;
            enable_BarVisualizer.setChecked(false);
            enable_LineVisualizer.setChecked(false);
            enable_CircleVisualizer.setChecked(false);
            enable_CircleBarVisualizer.setChecked(true);
            enable_LineBarVisualizer.setChecked(false);
            enable_SquareBarVisualizer.setChecked(false);
            None.setChecked(false);
        }
        if (getLBVValue == true) {
                BV = false;
                LV = false;
                CV = false;
                CBV = false;
                LBV = true;
                SBV = false;
                NoneV = false;
            enable_BarVisualizer.setChecked(false);
            enable_LineVisualizer.setChecked(false);
            enable_CircleVisualizer.setChecked(false);
            enable_CircleBarVisualizer.setChecked(false);
            enable_LineBarVisualizer.setChecked(true);
            enable_SquareBarVisualizer.setChecked(false);
            None.setChecked(false);
        }
        if (getSBVValue == true) {
                BV = false;
                LV = false;
                CV = false;
                CBV = false;
                LBV = false;
                SBV = true;
                NoneV = false;
            enable_BarVisualizer.setChecked(false);
            enable_LineVisualizer.setChecked(false);
            enable_CircleVisualizer.setChecked(false);
            enable_CircleBarVisualizer.setChecked(false);
            enable_LineBarVisualizer.setChecked(false);
            enable_SquareBarVisualizer.setChecked(true);
            None.setChecked(false);
        }
        if (getNVValue == true) {
                BV = false;
                LV = false;
                CV = false;
                CBV = false;
                LBV = false;
                SBV = false;
                NoneV = true;

            enable_BarVisualizer.setChecked(false);
            enable_LineVisualizer.setChecked(false);
            enable_CircleVisualizer.setChecked(false);
            enable_CircleBarVisualizer.setChecked(false);
            enable_LineBarVisualizer.setChecked(false);
            enable_SquareBarVisualizer.setChecked(false);
            None.setChecked(true);
        }

    }

    //enable or disable controls
    private void enableControls() {
        //enable Repeat and Shuffle
        SharedPreferences getRNS = getSharedPreferences("RNS", MODE_PRIVATE);
        Boolean getRNSValue = getRNS.getBoolean("RNSValue", true);
        if (getRNSValue == true) {
            player_repeat_btn.setVisibility(View.VISIBLE);
            player_shuffle_btn.setVisibility(View.VISIBLE);
            enable_Repeat_shuffle.setChecked(true);
        } else if (getRNSValue == false) {
            player_repeat_btn.setVisibility(View.GONE);
            player_shuffle_btn.setVisibility(View.GONE);
            enable_Repeat_shuffle.setChecked(false);
        } else {
            player_repeat_btn.setVisibility(View.GONE);
            player_shuffle_btn.setVisibility(View.GONE);
            enable_Repeat_shuffle.setChecked(false);
        }

        //enable Forward and Rewind
        SharedPreferences getFNS = getSharedPreferences("FNR", MODE_PRIVATE);
        Boolean getFNRValue = getFNS.getBoolean("FNRValue", false);
        if (getFNRValue == true) {
            player_fast_rewind_btn.setVisibility(View.VISIBLE);
            player_fast_forward_btn.setVisibility(View.VISIBLE);

            enable_Forward_Rewind.setChecked(true);
        } else if (getFNRValue == false) {
            player_fast_rewind_btn.setVisibility(View.GONE);
            player_fast_forward_btn.setVisibility(View.GONE);

            enable_Forward_Rewind.setChecked(false);
        } else {
            player_fast_rewind_btn.setVisibility(View.GONE);
            player_fast_forward_btn.setVisibility(View.GONE);

            enable_Forward_Rewind.setChecked(false);
        }
    }

    //settings find id
    private void settingFindID() {
        Settings_View = findViewById(R.id.Settings_View);
        settings_back = findViewById(R.id.settings_back);

        settings_music_name = findViewById(R.id.settings_music_name);
        settings_music_name.setSelected(true);

        enable_Repeat_shuffle = findViewById(R.id.enable_Repeat_shuffle);
        enable_Repeat_shuffle.setChecked(true);
        enable_Forward_Rewind = findViewById(R.id.enable_Forward_Rewind);
        enable_Forward_Rewind.setChecked(false);


        enable_BarVisualizer = findViewById(R.id.enable_BarVisualizer);
        enable_BarVisualizer.setChecked(true);

        enable_LineVisualizer = findViewById(R.id.enable_LineVisualizer);

        enable_CircleVisualizer = findViewById(R.id.enable_CircleVisualizer);

        enable_CircleBarVisualizer = findViewById(R.id.enable_CircleBarVisualizer);

        enable_LineBarVisualizer = findViewById(R.id.enable_LineBarVisualizer);

        enable_SquareBarVisualizer = findViewById(R.id.enable_SquareBarVisualizer);

        None = findViewById(R.id.None);

    }

    //player find id
    private void playerFindId() {
        player_playList_btn = findViewById(R.id.player_playList_btn);
        player_play_btn = findViewById(R.id.player_play_btn);
        player_previous_btn = findViewById(R.id.player_previous_btn);
        player_next_btn = findViewById(R.id.player_next_btn);
        player_repeat_btn = findViewById(R.id.player_repeat_btn);
        player_shuffle_btn = findViewById(R.id.player_shuffle_btn);
        player_Settings_btn = findViewById(R.id.player_Settings_btn);
        player_fast_forward_btn = findViewById(R.id.player_fast_forward_btn);
        player_fast_rewind_btn = findViewById(R.id.player_fast_rewind_btn);
        blurView = findViewById(R.id.blurView);
        player_song_name = findViewById(R.id.song_name);
        player_song_name.setSelected(true);
        total_song_played_duration = findViewById(R.id.total_song_played_duration);
        total_song_duration = findViewById(R.id.total_song_duration);
        player_seekbar = findViewById(R.id.seekbar);
        player_view_layout = findViewById(R.id.player_view_layout);

        Bar_visualizer = findViewById(R.id.Bar_visualizer);
        Line_visualizer = findViewById(R.id.Line_visualizer);
        Circle_visualizer = findViewById(R.id.Circle_visualizer);
        CircleBar_visualizer = findViewById(R.id.CircleBar_visualizer);
        LineBar_visualizer = findViewById(R.id.LineBar_visualizer);
        SquareBar_visualizer = findViewById(R.id.SquareBar_visualizer);


        song_image = findViewById(R.id.song_image);
        player_fav_btn = findViewById(R.id.player_fav_btn);
    }

    //home find id
    private void homeFIndId() {
        recyclerView = findViewById(R.id.Songs_Display);

        search_btn = findViewById(R.id.search_song_icon);
        back_btn = findViewById(R.id.back_song_icon);
        search_box = findViewById(R.id.search_song_Text);
        music_txt = findViewById(R.id.Music_text);
        NoSongFoundText = findViewById(R.id.NoSongFoundText);

        homeControlWrapper = findViewById(R.id.homeControlWrapper);
        music_name = findViewById(R.id.music_name);
        music_name.setSelected(true);

        previous_icon = findViewById(R.id.previous_icon);
        play_icon = findViewById(R.id.play_icon);
        next_icon = findViewById(R.id.next_icon);

        swipe_refresh = findViewById(R.id.swipe_refresh);
        TabLayout = findViewById(R.id.TabLayout);

        TabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                if (TabLayout.getSelectedTabPosition() == 0){
                    song = true;
                    played = false;
                    fav = false;
                    fetchSongs();
                    recyclerView.setVisibility(View.VISIBLE);
                    NoSongFoundText.setVisibility(View.GONE);
                }
                if (TabLayout.getSelectedTabPosition() == 1){
                    song = false;
                    played = true;
                    fav = false;
                    if (playedList.isEmpty()) {
                        NoSongFoundText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        songAdapter.playedSong(playedList);
                    }else {
                        NoSongFoundText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        songAdapter.playedSong(playedList);
                    }
                }
                if (TabLayout.getSelectedTabPosition() == 2){
//                    fav songs
                    song = false;
                    played = false;
                    fav = true;
                    if (favList.isEmpty()) {
                        NoSongFoundText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        songAdapter.favSong(favList);
                    }else {
                        NoSongFoundText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        songAdapter.favSong(favList);
                    }
                }
            }

            @Override
            public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {

            }
        });
    }

    private void filteredSong(String toString) {

        List<Song> filteredList = new ArrayList<>();

        if (allSongs.size() > 0) {
            for (Song song : allSongs) {
                if (song.getTitle().toLowerCase().contains(toString)) {
                    filteredList.add(song);
                }
            }
            if (filteredList.isEmpty()) {
                NoSongFoundText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                songAdapter.filterSong(filteredList);
            }else {
                NoSongFoundText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                songAdapter.filterSong(filteredList);
            }
        }
    }

    //change status bar color
    private void changeStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = this.getWindow();
            window.setStatusBarColor(this.getResources().getColor(R.color.bg));
        }
    }

    //player controls
    private void playerControls() {

        //on click of home wrapper
        homeControlWrapper.setOnClickListener(view -> {
            hideKeyBoard();
            player_view_layout.setVisibility(View.VISIBLE);
            String title = String.valueOf(player.getCurrentMediaItem().mediaMetadata.title).toLowerCase();
            checkFav(title);
        });

        //play list button clicked
        player_playList_btn.setOnClickListener(view -> {
            player_view_layout.setVisibility(View.GONE);
        });


        //player Listener
        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(MediaItem mediaItem, int reason) {
                Player.Listener.super.onMediaItemTransition(mediaItem, reason);
                //show the playing song
                assert mediaItem != null;
                music_name.setText(mediaItem.mediaMetadata.title);
                player_song_name.setText(mediaItem.mediaMetadata.title);
                settings_music_name.setText(mediaItem.mediaMetadata.title);

                total_song_played_duration.setText(getReadableTime((int) player.getCurrentPosition()));
                player_seekbar.setProgress((int) player.getCurrentPosition());
                player_seekbar.setMax((int) player.getDuration());
                total_song_duration.setText(getReadableTime((int) player.getDuration()));

                player_play_btn.setImageResource(R.drawable.pause_icon);
                play_icon.setImageResource(R.drawable.home_pause_icon);

                //show current art work
                showCurrentArtWork();

                //update progress of playing song
                updatePlayerPositionProgress();

                if (player.isPlaying()) {
                    //art work animation
                    song_image.setAnimation(loadRotation());
                }

                //set the visualizer
                activateAudioVisualizer();


            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState == ExoPlayer.STATE_READY) {
                    //set the values to player view
                    homeControlWrapper.setVisibility(View.VISIBLE);
                    music_name.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    player_song_name.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    settings_music_name.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);

                    total_song_played_duration.setText(getReadableTime((int) player.getCurrentPosition()));
                    total_song_duration.setText(getReadableTime((int) player.getDuration()));

                    player_seekbar.setMax((int) player.getDuration());
                    player_seekbar.setProgress((int) player.getCurrentPosition());

                    player_play_btn.setImageResource(R.drawable.pause_icon);
                    play_icon.setImageResource(R.drawable.home_pause_icon);

                    //show current art work
                    showCurrentArtWork();

                    //update progress of playing song
                    updatePlayerPositionProgress();

                    if (player.isPlaying()) {
                        //art work animation
                        song_image.setAnimation(loadRotation());
                    }

                    //set the visualizer
                    activateAudioVisualizer();
                } else {
                    player_play_btn.setImageResource(R.drawable.play_icon);
                    play_icon.setImageResource(R.drawable.home_play_icon);
                    song_image.clearAnimation();
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
                if (isPlaying) {
                    homeControlWrapper.setVisibility(View.VISIBLE);
                    music_name.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    player_song_name.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    settings_music_name.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);

                    total_song_played_duration.setText(getReadableTime((int) player.getCurrentPosition()));
                    total_song_duration.setText(getReadableTime((int) player.getDuration()));

                    player_seekbar.setMax((int) player.getDuration());
                    player_seekbar.setProgress((int) player.getCurrentPosition());

                    player_play_btn.setImageResource(R.drawable.pause_icon);
                    play_icon.setImageResource(R.drawable.home_pause_icon);

                    //show current art work
                    showCurrentArtWork();

                    //update progress of playing song
                    updatePlayerPositionProgress();

                    if (player.isPlaying()) {
                        //art work animation
                        song_image.setAnimation(loadRotation());
                    }

                    //set the visualizer
                    activateAudioVisualizer();

                }else {
                    player_play_btn.setImageResource(R.drawable.play_icon);
                    play_icon.setImageResource(R.drawable.home_play_icon);
                    song_image.clearAnimation();
                }
            }


        });

        //home next icon
        next_icon.setOnClickListener(view -> skipToNextSong());
        player_next_btn.setOnClickListener(view -> skipToNextSong());

        previous_icon.setOnClickListener(view -> skipToPreviousSong());
        player_previous_btn.setOnClickListener(view -> skipToPreviousSong());


        //play or pause player for player view
        player_play_btn.setOnClickListener(view -> {

            if (player.hasNextMediaItem() || player.hasPreviousMediaItem()) {
                playPlayerValue = true;
                playHomeValue = false;
                playOrPausePlayer();
            }
            else if (search == true && filteredList.size() <= 1) {
                playPlayerValue = true;
                playHomeValue = false;
                playOrPausePlayer();
            }
            else {
                player.setMediaItems(getMediaItems(), 0, 0);
                player.prepare();
                player.play();
                String title = String.valueOf(player.getCurrentMediaItem().mediaMetadata.title).toLowerCase();
                checkFav(title);

            }
        });

        //play or pause player from home
        play_icon.setOnClickListener(view -> {
            if (player.hasNextMediaItem() || player.hasPreviousMediaItem()) {
                playPlayerValue = false;
                playHomeValue = true;
                playOrPausePlayer();
            }
            else if (search == true && filteredList.size() <= 1) {
                playPlayerValue = false;
                playHomeValue = true;
                playOrPausePlayer();
            }
            else {
                player.setMediaItems(getMediaItems(), 0, 0);
                player.prepare();
                player.play();
                String title = String.valueOf(player.getCurrentMediaItem().mediaMetadata.title).toLowerCase();
                checkFav(title);
            }
        });

        //seekbar listener
        player_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressValue = seekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player.getPlaybackState() == ExoPlayer.STATE_READY) {
                    player_seekbar.setProgress(progressValue);
                    total_song_played_duration.setText(getReadableTime(progressValue));
                    player.seekTo(progressValue);
                }
            }
        });

        //repeat mode
        player_repeat_btn.setOnClickListener(view -> {
            if (repeatMode == 0) {
                player_repeat_btn.setImageResource(R.drawable.repeat_one_icon);
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
                repeatMode = 1;
            } else if (repeatMode == 1) {
                player_repeat_btn.setImageResource(R.drawable.repeat_icon);
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_OFF);
                repeatMode = 0;
            }
        });

        //shuffle mode
        player_shuffle_btn.setOnClickListener(view -> {
            if (shuffleMode == 0) {
                player_shuffle_btn.setImageResource(R.drawable.shuffle_on_icon);
                player.setShuffleModeEnabled(true);
                shuffleMode = 1;
            } else if (shuffleMode == 1) {
                player_shuffle_btn.setImageResource(R.drawable.shuffle_icon);
                player.setShuffleModeEnabled(false);
                shuffleMode = 0;
            }
        });

        //fast forward action
        player_fast_forward_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressValue += 10000;
                if (player.getPlaybackState() == ExoPlayer.STATE_READY) {
                    player_seekbar.setProgress(progressValue);
                    total_song_played_duration.setText(getReadableTime(progressValue));
                    player.seekTo(progressValue);
                }
            }
        });

        //fast rewind action
        player_fast_rewind_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressValue -= 10000;
                if (player.getPlaybackState() == ExoPlayer.STATE_READY) {
                    player_seekbar.setProgress(progressValue);
                    total_song_played_duration.setText(getReadableTime(progressValue));
                    player.seekTo(progressValue);
                }
            }
        });

        //when setting image is clicked
        player_Settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //calls
                settingsViewControl();
            }
        });

//        player fav button
        player_fav_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = String.valueOf(player.getCurrentMediaItem().mediaMetadata.title).toLowerCase();

                SharedPreferences favSongs = getSharedPreferences("favSongs",MODE_PRIVATE);
                SharedPreferences.Editor favEditor = favSongs.edit();
                Gson gson = new Gson();

                if (isfav == false){
                    try {
                        for (Song song : favList) {
                            if (song.getTitle().toLowerCase().contains(title)) {
                                favList.remove(song);
                                isfav = true;
                                player_fav_btn.setImageResource(R.drawable.favorite_border_con);
                                String  json = gson.toJson(favList);
                                favEditor.putString("Titles",json);
                                favEditor.apply();
                            }
                        }
                    }catch (Exception e){}

                        if (allSongs.size() > 0) {
                            for (Song song : allSongs) {
                                if (song.getTitle().toLowerCase().contains(title)) {
                                    favList.add(song);
                                    songAdapter.notifyDataSetChanged();
                                    Toast.makeText(HomeActivity.this, "It's A favorite! Press again to remove", Toast.LENGTH_SHORT).show();
                                    isfav = true;
                                    player_fav_btn.setImageResource(R.drawable.favorite_icon);
                                    String  json = gson.toJson(favList);
                                    favEditor.putString("Titles",json);
                                    favEditor.apply();
                                }
                            }
                        }
                }
                else if (isfav == true){
                    try {
                        if (favList.size() > 0) {
                            for (Song song : favList) {
                                if (song.getTitle().toLowerCase().contains(title)) {
                                    favList.remove(song);
                                    songAdapter.notifyDataSetChanged();
                                    Toast.makeText(HomeActivity.this, "Removed from favorite", Toast.LENGTH_SHORT).show();
                                    isfav = false;
                                    player_fav_btn.setImageResource(R.drawable.favorite_border_con);
                                    String  json = gson.toJson(favList);
                                    favEditor.putString("Titles",json);
                                    favEditor.apply();
                                }
                            }
                        }
                    }catch (Exception e){}
                }
            }
        });
    }

    //bind service
    private void doBindService() {
        Intent PlayerServiceIntent = new Intent(this, PlayerService.class);
        bindService(PlayerServiceIntent, playerServiceConnection, Context.BIND_AUTO_CREATE);

    }

    //service connection
    ServiceConnection playerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //get the service
            PlayerService.ServiceBinder binder = (PlayerService.ServiceBinder) iBinder;

            player = binder.getPlayerService().player;
            isBound = true;

            //ready to show song
            storagePermissionLauncher.launch(permission);

            if (player.isPlaying()) {
                //set the values to player view
                homeControlWrapper.setVisibility(View.VISIBLE);
                music_name.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                player_song_name.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                settings_music_name.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);

                total_song_played_duration.setText(getReadableTime((int) player.getCurrentPosition()));
                total_song_duration.setText(getReadableTime((int) player.getDuration()));

                player_seekbar.setMax((int) player.getDuration());
                player_seekbar.setProgress((int) player.getCurrentPosition());

                player_play_btn.setImageResource(R.drawable.pause_icon);
                play_icon.setImageResource(R.drawable.home_pause_icon);

                //show current art work
                showCurrentArtWork();

                //update progress of playing song
                updatePlayerPositionProgress();

                //art work animation
                song_image.setAnimation(loadRotation());

                //set the visualizer
                activateAudioVisualizer();
            } else {
                homeControlWrapper.setVisibility(View.GONE);
                player_play_btn.setImageResource(R.drawable.play_icon);
                play_icon.setImageResource(R.drawable.home_play_icon);
            }


            //call player controls
            playerControls();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }

    };


    //get player song list
    public List<MediaItem> getMediaItems() {
        //difine list of media items
        List<MediaItem> mediaItems = new ArrayList<>();

        for (Song song : allSongs) {
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(song.getUri())
                    .setMediaMetadata(getMetaData(song))
                    .build();

            //add media item to media item list
            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }

    //create media metaData
    private MediaMetadata getMetaData(Song song) {
        return new MediaMetadata.Builder()
                .setTitle(song.getTitle())
                .build();
    }

    //Settings View Controls
    private void settingsViewControl() {

        addBlurBackground();

        Settings_View.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.BounceInRight)
                .duration(2000)
                .playOn(Settings_View);

        blurView.setVisibility(View.VISIBLE);

        //back button clicked
        settings_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings_View.setVisibility(View.GONE);
                blurView.setVisibility(View.GONE);
            }
        });


        //enable Repeat and shuffle
        enable_Repeat_shuffle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                boolean enableRNS = enable_Repeat_shuffle.isChecked();
                SharedPreferences shrdRNS = getSharedPreferences("RNS", MODE_PRIVATE);
                SharedPreferences.Editor RNSEditor = shrdRNS.edit();
                if (enableRNS) {
                    player_repeat_btn.setVisibility(View.VISIBLE);
                    player_shuffle_btn.setVisibility(View.VISIBLE);

                    RNSEditor.putBoolean("RNSValue", enableRNS);
                    RNSEditor.apply();

                } else {
                    player_repeat_btn.setVisibility(View.GONE);
                    player_shuffle_btn.setVisibility(View.GONE);

                    RNSEditor.putBoolean("RNSValue", enableRNS);
                    RNSEditor.apply();
                }
            }
        });

        //enable Forward and Rewind
        enable_Forward_Rewind.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean enableFNR = enable_Forward_Rewind.isChecked();
                SharedPreferences shrdgetFNR = getSharedPreferences("FNR", MODE_PRIVATE);
                SharedPreferences.Editor getFNREdit = shrdgetFNR.edit();

                if (enableFNR) {
                    player_fast_rewind_btn.setVisibility(View.VISIBLE);
                    player_fast_forward_btn.setVisibility(View.VISIBLE);

                    getFNREdit.putBoolean("FNRValue", enableFNR);
                    getFNREdit.apply();
                } else {
                    player_fast_rewind_btn.setVisibility(View.GONE);
                    player_fast_forward_btn.setVisibility(View.GONE);

                    getFNREdit.putBoolean("FNRValue", enableFNR);
                    getFNREdit.apply();
                }
            }
        });


        //creating Shared Preferences for each visualizer
        SharedPreferences ShardBV = getSharedPreferences("BV", MODE_PRIVATE);
        SharedPreferences.Editor setBV = ShardBV.edit();

        SharedPreferences ShardLV = getSharedPreferences("LV", MODE_PRIVATE);
        SharedPreferences.Editor setLV = ShardLV.edit();

        SharedPreferences ShardCV = getSharedPreferences("CV", MODE_PRIVATE);
        SharedPreferences.Editor setCV = ShardCV.edit();

        SharedPreferences ShardCBV = getSharedPreferences("CBV", MODE_PRIVATE);
        SharedPreferences.Editor setCBV = ShardCBV.edit();

        SharedPreferences ShardLBV = getSharedPreferences("LBV", MODE_PRIVATE);
        SharedPreferences.Editor setLBV = ShardLBV.edit();

        SharedPreferences ShardSBV = getSharedPreferences("SBV", MODE_PRIVATE);
        SharedPreferences.Editor setSBV = ShardSBV.edit();

        SharedPreferences ShardNV = getSharedPreferences("NV", MODE_PRIVATE);
        SharedPreferences.Editor setNV = ShardNV.edit();


        //enable Bar Visualizer
        enable_BarVisualizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BV = true;
                LV = false;
                CV = false;
                CBV = false;
                LBV = false;
                SBV = false;
                NoneV = false;

                Settings_View.setVisibility(View.GONE);
                blurView.setVisibility(View.GONE);

                activateAudioVisualizer();

                setBV.putBoolean("BVV", BV);
                setBV.apply();

                setLV.putBoolean("LVV", LV);
                setLV.apply();

                setCV.putBoolean("CVV", CV);
                setCV.apply();

                setCBV.putBoolean("CBVV", CBV);
                setCBV.apply();

                setLBV.putBoolean("LBVV", LBV);
                setLBV.apply();

                setSBV.putBoolean("SBVV", SBV);
                setSBV.apply();

                setNV.putBoolean("NVV", NoneV);
                setNV.apply();
            }
        });

        //enable Line Visualizer
        enable_LineVisualizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BV = false;
                LV = true;
                CV = false;
                CBV = false;
                LBV = false;
                SBV = false;
                NoneV = false;

                Settings_View.setVisibility(View.GONE);
                blurView.setVisibility(View.GONE);

                activateAudioVisualizer();

                setBV.putBoolean("BVV", BV);
                setBV.apply();

                setLV.putBoolean("LVV", LV);
                setLV.apply();

                setCV.putBoolean("CVV", CV);
                setCV.apply();

                setCBV.putBoolean("CBVV", CBV);
                setCBV.apply();

                setLBV.putBoolean("LBVV", LBV);
                setLBV.apply();

                setSBV.putBoolean("SBVV", SBV);
                setSBV.apply();

                setNV.putBoolean("NVV", NoneV);
                setNV.apply();
            }
        });

        //enable Circle Visualizer
        enable_CircleVisualizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BV = false;
                LV = false;
                CV = true;
                CBV = false;
                LBV = false;
                SBV = false;
                NoneV = false;

                Settings_View.setVisibility(View.GONE);
                blurView.setVisibility(View.GONE);

                activateAudioVisualizer();

                setBV.putBoolean("BVV", BV);
                setBV.apply();

                setLV.putBoolean("LVV", LV);
                setLV.apply();

                setCV.putBoolean("CVV", CV);
                setCV.apply();

                setCBV.putBoolean("CBVV", CBV);
                setCBV.apply();

                setLBV.putBoolean("LBVV", LBV);
                setLBV.apply();

                setSBV.putBoolean("SBVV", SBV);
                setSBV.apply();

                setNV.putBoolean("NVV", NoneV);
                setNV.apply();
            }
        });

        //enable Circle Bar Visualizer
        enable_CircleBarVisualizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BV = false;
                LV = false;
                CV = false;
                CBV = true;
                LBV = false;
                SBV = false;
                NoneV = false;

                Settings_View.setVisibility(View.GONE);
                blurView.setVisibility(View.GONE);

                activateAudioVisualizer();

                setBV.putBoolean("BVV", BV);
                setBV.apply();

                setLV.putBoolean("LVV", LV);
                setLV.apply();

                setCV.putBoolean("CVV", CV);
                setCV.apply();

                setCBV.putBoolean("CBVV", CBV);
                setCBV.apply();

                setLBV.putBoolean("LBVV", LBV);
                setLBV.apply();

                setSBV.putBoolean("SBVV", SBV);
                setSBV.apply();

                setNV.putBoolean("NVV", NoneV);
                setNV.apply();
            }
        });

        //enable Line Bar Visualizer
        enable_LineBarVisualizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BV = false;
                LV = false;
                CV = false;
                CBV = false;
                LBV = true;
                SBV = false;
                NoneV = false;

                Settings_View.setVisibility(View.GONE);
                blurView.setVisibility(View.GONE);

                activateAudioVisualizer();

                setBV.putBoolean("BVV", BV);
                setBV.apply();

                setLV.putBoolean("LVV", LV);
                setLV.apply();

                setCV.putBoolean("CVV", CV);
                setCV.apply();

                setCBV.putBoolean("CBVV", CBV);
                setCBV.apply();

                setLBV.putBoolean("LBVV", LBV);
                setLBV.apply();

                setSBV.putBoolean("SBVV", SBV);
                setSBV.apply();

                setNV.putBoolean("NVV", NoneV);
                setNV.apply();
            }
        });

        //enable Square Bar Visualizer
        enable_SquareBarVisualizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BV = false;
                LV = false;
                CV = false;
                CBV = false;
                LBV = false;
                SBV = true;
                NoneV = false;

                Settings_View.setVisibility(View.GONE);
                blurView.setVisibility(View.GONE);

                activateAudioVisualizer();

                setBV.putBoolean("BVV", BV);
                setBV.apply();

                setLV.putBoolean("LVV", LV);
                setLV.apply();

                setCV.putBoolean("CVV", CV);
                setCV.apply();

                setCBV.putBoolean("CBVV", CBV);
                setCBV.apply();

                setLBV.putBoolean("LBVV", LBV);
                setLBV.apply();

                setSBV.putBoolean("SBVV", SBV);
                setSBV.apply();

                setNV.putBoolean("NVV", NoneV);
                setNV.apply();
            }
        });

        //enable None Visualizer
        None.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BV = false;
                LV = false;
                CV = false;
                CBV = false;
                LBV = false;
                SBV = false;
                NoneV = true;

                Settings_View.setVisibility(View.GONE);
                blurView.setVisibility(View.GONE);

                activateAudioVisualizer();

                setBV.putBoolean("BVV", BV);
                setBV.apply();

                setLV.putBoolean("LVV", LV);
                setLV.apply();

                setCV.putBoolean("CVV", CV);
                setCV.apply();

                setCBV.putBoolean("CBVV", CBV);
                setCBV.apply();

                setLBV.putBoolean("LBVV", LBV);
                setLBV.apply();

                setSBV.putBoolean("SBVV", SBV);
                setSBV.apply();

                setNV.putBoolean("NVV", NoneV);
                setNV.apply();
            }
        });
    }

    //add blur background
    private void addBlurBackground() {
        float radius = 8f;

        View decorView = getWindow().getDecorView();

        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);

        Drawable windowBackground = decorView.getBackground();

        blurView.setupWith(rootView, new RenderScriptBlur(this)) // or RenderEffectBlur
                .setFrameClearDrawable(windowBackground) // Optional
                .setBlurRadius(radius);
    }

    //play or pause player
    private void playOrPausePlayer() {
        if (playPlayerValue == true) {
            if (player.isPlaying()) {
                player.pause();
                player_play_btn.setImageResource(R.drawable.play_icon);
                song_image.clearAnimation();
                String title = String.valueOf(player.getCurrentMediaItem().mediaMetadata.title).toLowerCase();
                checkFav(title);
            } else {
                player.play();
                player_play_btn.setImageResource(R.drawable.pause_icon);
                song_image.startAnimation(loadRotation());
                String title = String.valueOf(player.getCurrentMediaItem().mediaMetadata.title).toLowerCase();
                checkFav(title);
            }

        }
        if (playHomeValue == true) {

            if (player.isPlaying()) {
                player.pause();
                play_icon.setImageResource(R.drawable.home_play_icon);
                song_image.clearAnimation();
                String title = String.valueOf(player.getCurrentMediaItem().mediaMetadata.title).toLowerCase();
                checkFav(title);
            } else {
                player.play();
                play_icon.setImageResource(R.drawable.home_pause_icon);
                song_image.startAnimation(loadRotation());
                String title = String.valueOf(player.getCurrentMediaItem().mediaMetadata.title).toLowerCase();
                checkFav(title);

            }
        }
    }
    //skip to next song
    private void skipToNextSong() {
        if (player.hasNextMediaItem()) {
            player.seekToNext();
            player.play();
            String title = String.valueOf(player.getCurrentMediaItem().mediaMetadata.title).toLowerCase();
            checkFav(title);
        }else {
            player.setMediaItems(getMediaItems(),0,0);
            String title = String.valueOf(player.getCurrentMediaItem().mediaMetadata.title).toLowerCase();
            checkFav(title);
        }
    }

    //skip to previous song
    private void skipToPreviousSong() {
        String title = String.valueOf(player.getCurrentMediaItem().mediaMetadata.title).toLowerCase();
        if (player.hasPreviousMediaItem()) {
            player.seekToPrevious();
            player.play();
            checkFav(title);
        }
        else {
            player.setMediaItems(getMediaItems(),allSongs.size()-1,0);
            checkFav(title);
        }
    }

    //    check for fav
    private void checkFav(String title) {
        try {
            for (Song song : favList) {
                if (song.getTitle().toLowerCase().contains(title)) {
                    isfav = true;
                    player_fav_btn.setImageResource(R.drawable.favorite_icon);
                }else {
                    isfav = false;
                    player_fav_btn.setImageResource(R.drawable.favorite_border_con);

                }
            }
        }catch (Exception e){}
    }
    //Rotation animation
    private Animation loadRotation() {
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(10000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        return rotateAnimation;
    }

    //update progress
    private void updatePlayerPositionProgress() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player.isPlaying()) {
                    total_song_played_duration.setText(getReadableTime((int) player.getCurrentPosition()));
                    player_seekbar.setProgress((int) player.getCurrentPosition());
                }
                //repeat calling method
                updatePlayerPositionProgress();
            }
        }, 1000);
    }

    //show current art work
    private void showCurrentArtWork() {
        song_image.setImageURI(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri);

        if (song_image.getDrawable() == null) {
            song_image.setImageResource(R.drawable.music);
        }
    }

    //get progress time
    String getReadableTime(int currentPosition) {
        String time;
        int hrs = currentPosition / (1000 * 60 * 60);
        int min = (currentPosition % (1000 * 60 * 60)) / (1000 * 60);
        int secs = (((currentPosition % (1000 * 60 * 60)) % (1000 * 60 * 60)) % (1000 * 60)) / 1000;

        if (hrs < 1) {
            time = String.format("%02d:%02d", min, secs);
        } else {
            time = String.format("%1d:%02d:%02d", hrs, min, secs);
        }
        return time;

    }


    //user response for audio record permission
    private void userResponseForRecordPerm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(recordAudioPermission)) {
                //show an educational UI why we need these permission
                //use alert box

                View view = LayoutInflater.from(HomeActivity.this).inflate(R.layout.ask_perm, null);
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(HomeActivity.this);
                builder.setView(view);

                android.app.AlertDialog alertDialog = builder.create();
                alertDialog.setCancelable(false);

                TextView perm_title = view.findViewById(R.id.perm_title);
                TextView perm_message = view.findViewById(R.id.perm_message);
                TextView Allow = view.findViewById(R.id.Allow);
                TextView Cancel = view.findViewById(R.id.Cancel);

                perm_title.setText("Requesting to show Audio visualizer");
                perm_message.setText("Allow us to show audio visualizer on this device");

                Allow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recordAudioPermissionLauncher.launch(recordAudioPermission);
                        alertDialog.dismiss();
                    }
                });

                Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                alertDialog.show();
            }
        } else {
            Toast.makeText(HomeActivity.this, "Denied to show audio visualizer", Toast.LENGTH_SHORT).show();
        }
    }

    //on back pressed
    @SuppressLint("SuspiciousIndentation")
    @Override
    public void onBackPressed() {
        if (Settings_View.getVisibility() == View.VISIBLE){
            Settings_View.setVisibility(View.GONE);
            blurView.setVisibility(View.GONE);
        }
        else if (player_view_layout.getVisibility() == View.VISIBLE) {
            player_view_layout.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
            this.finishAffinity();
        }
    }

    //activate audio visualizer
    private void activateAudioVisualizer() {
        //check if permission granted
        if (ContextCompat.checkSelfPermission(this, recordAudioPermission) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (BV == true) {
            Bar_visualizer.setColor(ContextCompat.getColor(this, R.color.bg));
            Bar_visualizer.setDensity(100);
            Bar_visualizer.setPlayer(player.getAudioSessionId());

            Bar_visualizer.setVisibility(View.VISIBLE);
            Line_visualizer.setVisibility(View.GONE);
            Circle_visualizer.setVisibility(View.GONE);
            CircleBar_visualizer.setVisibility(View.GONE);
            LineBar_visualizer.setVisibility(View.GONE);
            SquareBar_visualizer.setVisibility(View.GONE);

        }
        if (LV == true) {
            Line_visualizer.setColor(ContextCompat.getColor(this, R.color.bg));
            Line_visualizer.setStrokeWidth(1);
            Line_visualizer.setPlayer(player.getAudioSessionId());

            Bar_visualizer.setVisibility(View.GONE);
            Line_visualizer.setVisibility(View.VISIBLE);
            Circle_visualizer.setVisibility(View.GONE);
            CircleBar_visualizer.setVisibility(View.GONE);
            LineBar_visualizer.setVisibility(View.GONE);
            SquareBar_visualizer.setVisibility(View.GONE);
        }
        if (CV == true) {
            Circle_visualizer.setColor(ContextCompat.getColor(this, R.color.bg));
            Circle_visualizer.setStrokeWidth(1);
            Circle_visualizer.setRadiusMultiplier(3f);
            Circle_visualizer.setPlayer(player.getAudioSessionId());

            Bar_visualizer.setVisibility(View.GONE);
            Line_visualizer.setVisibility(View.GONE);
            Circle_visualizer.setVisibility(View.VISIBLE);
            CircleBar_visualizer.setVisibility(View.GONE);
            LineBar_visualizer.setVisibility(View.GONE);
            SquareBar_visualizer.setVisibility(View.GONE);
        }
        if (CBV == true) {
            CircleBar_visualizer.setColor(ContextCompat.getColor(this, R.color.bg));
            CircleBar_visualizer.setPlayer(player.getAudioSessionId());

            Bar_visualizer.setVisibility(View.GONE);
            Line_visualizer.setVisibility(View.GONE);
            Circle_visualizer.setVisibility(View.GONE);
            CircleBar_visualizer.setVisibility(View.VISIBLE);
            LineBar_visualizer.setVisibility(View.GONE);
            SquareBar_visualizer.setVisibility(View.GONE);
        }
        if (LBV == true) {
            LineBar_visualizer.setColor(ContextCompat.getColor(this, R.color.bg));
            LineBar_visualizer.setDensity(100);
            LineBar_visualizer.setPlayer(player.getAudioSessionId());

            Bar_visualizer.setVisibility(View.GONE);
            Line_visualizer.setVisibility(View.GONE);
            Circle_visualizer.setVisibility(View.GONE);
            CircleBar_visualizer.setVisibility(View.GONE);
            LineBar_visualizer.setVisibility(View.VISIBLE);
            SquareBar_visualizer.setVisibility(View.GONE);
        }
        if (SBV == true) {
            SquareBar_visualizer.setColor(ContextCompat.getColor(this, R.color.bg));
            SquareBar_visualizer.setDensity(60);
            SquareBar_visualizer.setGap(2);
            SquareBar_visualizer.setPlayer(player.getAudioSessionId());

            Bar_visualizer.setVisibility(View.GONE);
            Line_visualizer.setVisibility(View.GONE);
            Circle_visualizer.setVisibility(View.GONE);
            CircleBar_visualizer.setVisibility(View.GONE);
            LineBar_visualizer.setVisibility(View.GONE);
            SquareBar_visualizer.setVisibility(View.VISIBLE);
        }
        if (NoneV == true) {
            Bar_visualizer.setVisibility(View.GONE);
            Line_visualizer.setVisibility(View.GONE);
            Circle_visualizer.setVisibility(View.GONE);
            CircleBar_visualizer.setVisibility(View.GONE);
            LineBar_visualizer.setVisibility(View.GONE);
            SquareBar_visualizer.setVisibility(View.GONE);
        }
    }

    //user responses on permission
    private void userResponse() {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            //fetch song
            fetchSongs();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(permission)) {
                //show an educational UI why we need these permission
                //use alert box
                View view = LayoutInflater.from(HomeActivity.this).inflate(R.layout.ask_perm, null);
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(HomeActivity.this);
                builder.setView(view);

                android.app.AlertDialog alertDialog = builder.create();
                alertDialog.setCancelable(false);

                TextView perm_title = view.findViewById(R.id.perm_title);
                TextView perm_message = view.findViewById(R.id.perm_message);
                TextView Allow = view.findViewById(R.id.Allow);
                TextView Cancel = view.findViewById(R.id.Cancel);

                perm_title.setText("Requesting permission");
                perm_message.setText("Allow us to fetch song on your device");

                Allow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        storagePermissionLauncher.launch(permission);
                        alertDialog.dismiss();
                    }
                });

                Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                alertDialog.show();
            }
        } else {
            Toast.makeText(HomeActivity.this, "Denied for required permissions", Toast.LENGTH_SHORT).show();
        }
    }

    //if permission granted fetch songs
    private void fetchSongs() {
        //fetch songs
        List<Song> songs = new ArrayList<>();
        Uri mediaStoreUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaStoreUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            mediaStoreUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        //define projection
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA, //for path

        };

        //order
        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        //get song
        try (Cursor cursor = getContentResolver().query(mediaStoreUri, projection, null, null, sortOrder)) {
            //cache cursor indices
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int albumIDColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            int IDColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

            //clear the previous loaded before adding loading again
            while (cursor.moveToNext()) {
                //get the value of column
                long id = cursor.getLong(idColumn);
                int duration = cursor.getInt(durationColumn);
                int size = cursor.getInt(sizeColumn);
                long albumId = cursor.getLong(albumIDColumn);
                Long Id = cursor.getLong(IDColumn);
                String name = cursor.getString(nameColumn);
                String path = cursor.getString(pathColumn);

                //song uri
                String uri = String.valueOf(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id));

                //remove mp3 extension
                name = name.substring(0, name.lastIndexOf("."));


                //song item
                Song song = new Song(name, uri, size, duration, id, path);

                //add song to song list
                songs.add(song);
            }
            //display song
            showSongs(songs);
        }
    }

    private void doUnBindService() {
        if (isBound) {
            unbindService(playerServiceConnection);
            isBound = false;
        }
    }

    //when application destroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnBindService();
    }

    //show the song to user
    private void showSongs(List<Song> songs) {
        if (songs.size() == 0) {
            Toast.makeText(this, "No songs", Toast.LENGTH_SHORT).show();
            return;
        }

        //save songs
        allSongs.clear();
        allSongs.addAll(songs);

        //layout manger
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);


        //song adapter
        songAdapter = new SongAdapter(this, songs, player, player_view_layout);
        //set adapter to recyclerview
        //recyclerView.setAdapter(songAdapter);

        //delete song
        songAdapter.setOnItemClickListener(new SongAdapter.onItemClickListener() {
            @Override
            public void onItemClick(Song position) {
                if (ContextCompat.checkSelfPermission(HomeActivity.this, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(HomeActivity.this, "No permission to delete", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (player != null && player.getPlaybackState() == Player.STATE_READY) {
                    String playSongUri = (String) player.getCurrentMediaItem().mediaMetadata.title;
                    if (playSongUri.toString().equalsIgnoreCase(position.getTitle().toString())) {
                        Toast.makeText(HomeActivity.this, "Can't delete playing song", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                View view = LayoutInflater.from(HomeActivity.this).inflate(R.layout.ask_perm, null);
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(HomeActivity.this);
                builder.setView(view);

                android.app.AlertDialog alertDialog = builder.create();
                alertDialog.setCancelable(false);

                TextView perm_title = view.findViewById(R.id.perm_title);
                TextView perm_message = view.findViewById(R.id.perm_message);
                TextView Allow = view.findViewById(R.id.Allow);
                TextView Cancel = view.findViewById(R.id.Cancel);

                perm_title.setText("Permanently delete Song?");
                perm_message.setText("Are you sure to delete "
                        + position.getTitle() + " from your device?");

                Allow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                            try {
                                File fileToDelete = new File(position.getPath());
                                fileToDelete.delete();
                                getContentResolver().delete(Uri.parse(position.getUri()), null, null);
                                allSongs.remove(position);
                                fetchSongs();
                                Toast.makeText(HomeActivity.this, "File Deleted", Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();
                            } catch (Exception e) {
                                Toast.makeText(HomeActivity.this, "Not Allowed to delete", Toast.LENGTH_SHORT).show();
                                fetchSongs();
                                alertDialog.dismiss();

                            }
                        } else {
                            List<Uri> deleteList = new ArrayList<>();
                            deleteList.add(Uri.parse(position.getUri()));
                            PendingIntent pendingIntent = MediaStore.createDeleteRequest(getContentResolver(), deleteList);
                            IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(pendingIntent.getIntentSender()).build();

                            songDeleteIntentLauncher.launch(intentSenderRequest);
                            allSongs.remove(position);
                            fetchSongs();
                            alertDialog.dismiss();

                        }
                    }
                });

                Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                alertDialog.show();
            }
        });

        //set as ringtone
        songAdapter.setOnItemRingClickListener(new SongAdapter.onItemClickRingListener() {
            @Override
            public void onItemRingClick(Song position) {
                String musicId = String.valueOf(position.getID());
                if (ContextCompat.checkSelfPermission(HomeActivity.this, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(HomeActivity.this, "No permission to set ringtone", Toast.LENGTH_SHORT).show();
                    return;
                }


                View view = LayoutInflater.from(HomeActivity.this).inflate(R.layout.ask_perm, null);
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(HomeActivity.this);
                builder.setView(view);

                android.app.AlertDialog alertDialog = builder.create();
                alertDialog.setCancelable(false);

                TextView perm_title = view.findViewById(R.id.perm_title);
                TextView perm_message = view.findViewById(R.id.perm_message);
                TextView Allow = view.findViewById(R.id.Allow);
                TextView Cancel = view.findViewById(R.id.Cancel);

                perm_title.setText("Want to set as ringtone? ");
                perm_message.setText("Are you sure to set  "
                        + position.getTitle() + " as ring tone");

                Allow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                            try {
                                setAsRingtone(musicId);
                                alertDialog.dismiss();
                            } catch (Exception e) {
                                Toast.makeText(HomeActivity.this, "Something went Wrong", Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();
                            }
                        } else {
                            setAsRingtone(musicId);
                            alertDialog.dismiss();
                        }
                    }
                });

                Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                alertDialog.show();
            }
        });

        songAdapter.setOnItemPlayedClickListener(new SongAdapter.onItemClickPlayedListener() {
            @Override
            public void onItemPlayedClick(Song position) {
                String title = position.getTitle().toLowerCase();


                playedSongs(title);

            }
        });

        //check for fav
        songAdapter.setOnFavClickListener(new SongAdapter.onItemFavListener() {
            @Override
            public void onItemFavClick(Song position) {
                String title = position.getTitle().toLowerCase();
                for (Song song1:favList){
                    if (song1.getTitle().toLowerCase().contains(title)){
                        isfav = true;
                        player_fav_btn.setImageResource(R.drawable.favorite_icon);
                    }else {
                        isfav = false;
                        player_fav_btn.setImageResource(R.drawable.favorite_border_con);
                    }
                }
            }
        });


        //animation to adapter
        ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(songAdapter);
        scaleInAnimationAdapter.setDuration(1000);
        scaleInAnimationAdapter.setInterpolator(new OvershootInterpolator());
        scaleInAnimationAdapter.setFirstOnly(false);
        recyclerView.setAdapter(scaleInAnimationAdapter);

        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (song == true){
                    fetchSongs();
                    swipe_refresh.setRefreshing(false);
                }
                if (played == true){
                    if (playedList.isEmpty()) {
                        NoSongFoundText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        songAdapter.playedSong(playedList);
                        swipe_refresh.setRefreshing(false);

                    }else {
                        NoSongFoundText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        songAdapter.playedSong(playedList);
                        swipe_refresh.setRefreshing(false);

                    }
                }
                if (fav == true){
                    if (favList.isEmpty()) {
                        NoSongFoundText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        songAdapter.favSong(favList);
                        swipe_refresh.setRefreshing(false);

                    }else {
                        NoSongFoundText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        songAdapter.favSong(favList);
                        swipe_refresh.setRefreshing(false);

                    }
                }
            }
        });
    }

    private void playedSongs(String title) {
        try {
            for (Song song1 : playedList) {
                if (song1.getTitle().toLowerCase().contains(title)) {
                    playedList.remove(song1);
                }
            }
        }catch (Exception e){
        }

        if (allSongs.size() > 0) {
            for (Song song : allSongs) {
                if (song.getTitle().toLowerCase().contains(title)) {
                    playedList.add(song);
                }
            }
        }
    }

    //set song as ringtone
    private void setAsRingtone(String musicId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(this)) {
                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(musicId));
                RingtoneManager.setActualDefaultRingtoneUri(
                        this,
                        RingtoneManager.TYPE_RINGTONE,
                        uri
                );
                Toast.makeText(this, "Ring set successfully", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }
}
package com.jubaer_ahmed.newmusicplayer;

import android.animation.ObjectAnimator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuffColorFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    ImageView imagLogo;
    TextView txtsName, textsStart, textsStop;
    ImageButton playBtn, nextBtn, prevBtn, forwardBtn, rewindBtn, loopBtn, shuffleBtn;
    SeekBar seekBar;

    ShapeableImageView imageView;


    // Media Player and related variables
    static MediaPlayer mediaPlayer;
    ArrayList<String> mySongs;
    int position;
    boolean isLooping = false;
    boolean isShuffle = false;

    Handler handler = new Handler();
    Runnable updateSeekbarRunnable;

    private static final int FORWARD_DURATION = 10000;
    private static final int REWIND_DURATION = 10000;

    private static final String CHANNEL_ID = "music_player_channel";
    private NotificationManager notificationManager;

    MediaSessionCompat mediaSession;

    // Store the instance of PlayerActivity
    private static PlayerActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playBtn = findViewById(R.id.playBtn);
        nextBtn = findViewById(R.id.nextBtn);
        prevBtn = findViewById(R.id.prevBtn);
        forwardBtn = findViewById(R.id.forwardBtn);
        rewindBtn = findViewById(R.id.rewindBtn);
        loopBtn = findViewById(R.id.loopBtn);
        shuffleBtn = findViewById(R.id.shuffleBtn);
        imagLogo = findViewById(R.id.shapeableImageView);
        txtsName = findViewById(R.id.txtsong);
        textsStart = findViewById(R.id.textsStart);
        textsStop = findViewById(R.id.textsStop);
        seekBar = findViewById(R.id.seekbar);

         imageView = findViewById(R.id.shapeableImageView);  // Correct variable type
        imageView.setShapeAppearanceModel(
                imageView.getShapeAppearanceModel()
                        .toBuilder()
                        .setAllCornerSizes(50f)  // Set custom corner size in pixels
                        .build());



        txtsName.setSelected(true);

        playBtn.setOnClickListener(v -> togglePlayPause());
        nextBtn.setOnClickListener(v -> nextSong());
        prevBtn.setOnClickListener(v -> previousSong());
        forwardBtn.setOnClickListener(v -> forwardSong());
        rewindBtn.setOnClickListener(v -> rewindSong());
        loopBtn.setOnClickListener(v -> toggleLooping());
        shuffleBtn.setOnClickListener(v -> toggleShuffle());

        Intent intent = getIntent();
        mySongs = intent.getStringArrayListExtra("songs");
        position = intent.getIntExtra("pos", 0);
        playSong(position);

        customizeSeekBar();
        updateDurationUI();


        instance = this;

        mediaSession = new MediaSessionCompat(this, "MusicPlayer");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setActive(true);

        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_PLAY_PAUSE");
        filter.addAction("ACTION_NEXT");
        filter.addAction("ACTION_PREVIOUS");
        registerReceiver(new MediaActionReceiver(), filter);
    }


    private void customizeSeekBar() {
        seekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(
                ContextCompat.getColor(this, R.color.blue),
                android.graphics.PorterDuff.Mode.MULTIPLY));
        seekBar.getThumb().setColorFilter(new PorterDuffColorFilter(
                ContextCompat.getColor(this, R.color.blue),
                android.graphics.PorterDuff.Mode.SRC_IN));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                    mediaPlayer.start();
                }
            }
        });
    }

    private void updateDurationUI() {
        if (mediaPlayer != null) {
            String endTime = createTime(mediaPlayer.getDuration());
            textsStop.setText(endTime);
            mediaPlayer.setOnCompletionListener(mp -> {
                if (isLooping) {
                    playSong(position);
                } else {
                    nextSong();
                }
            });
        }
    }

    private void playSong(int position) {
        releaseMediaPlayer();

        Uri uri = Uri.parse(mySongs.get(position));
        String songName = new File(mySongs.get(position)).getName();
        txtsName.setText(songName);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        seekBar.setMax(mediaPlayer.getDuration());
        updateSeekbar();

        playBtn.setImageResource(R.drawable.pause_icon);
        createNotification(songName, true);
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playBtn.setImageResource(R.drawable.play_icon);
                createNotification(txtsName.getText().toString(), false);
            } else {
                mediaPlayer.start();
                playBtn.setImageResource(R.drawable.pause_icon);
                createNotification(txtsName.getText().toString(), true);
            }
        }
    }

    void nextSong() {
        if (isShuffle) {
            position = (int) (Math.random() * mySongs.size());
        } else {
            position = (position + 1) % mySongs.size();
        }
        rotateLogo();
        playSong(position);
    }

    void previousSong() {
        position = (position - 1 + mySongs.size()) % mySongs.size();
        rotateLogo2();
        playSong(position);
    }

    private void forwardSong() {
        if (mediaPlayer != null) {
            int newPosition = mediaPlayer.getCurrentPosition() + FORWARD_DURATION;
            mediaPlayer.seekTo(Math.min(newPosition, mediaPlayer.getDuration()));
        }
    }

    private void rewindSong() {
        if (mediaPlayer != null) {
            int newPosition = mediaPlayer.getCurrentPosition() - REWIND_DURATION;
            mediaPlayer.seekTo(Math.max(newPosition, 0));
        }
    }

    private String createTime(int duration) {
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;
        return String.format("%d:%02d", min, sec);
    }

    private void updateSeekbar() {
        updateSeekbarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    textsStart.setText(createTime(mediaPlayer.getCurrentPosition()));
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.post(updateSeekbarRunnable);
    }

    private void rotateLogo() {

        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
        animator.setDuration(500);
        animator.start();
    }

    private void rotateLogo2() {

        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation",360f,0f);
        animator.setDuration(500);
        animator.start();
    }

    private void toggleLooping() {
        isLooping = !isLooping;
        loopBtn.setImageResource(isLooping ? R.drawable.loop_on_icon : R.drawable.loop_off_icon);
    }

    private void toggleShuffle() {
        isShuffle = !isShuffle;
        shuffleBtn.setImageResource(isShuffle ? R.drawable.shuffle_on_icon : R.drawable.shuffle_of_icon);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        handler.removeCallbacks(updateSeekbarRunnable);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(1);
        }
        instance = null;
    }

    private void createNotification(String songTitle, boolean isPlaying) {
        createNotificationChannel();

        Intent playPauseIntent = new Intent(this, MediaActionReceiver.class);
        playPauseIntent.setAction("ACTION_PLAY_PAUSE");
        PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(
                this, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent nextIntent = new Intent(this, MediaActionReceiver.class);
        nextIntent.setAction("ACTION_NEXT");
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(
                this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent prevIntent = new Intent(this, MediaActionReceiver.class);
        prevIntent.setAction("ACTION_PREVIOUS");
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(
                this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        int playPauseIcon = isPlaying ? R.drawable.pause_icon : R.drawable.play_icon;

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(songTitle)
                .setContentText("Unknown Artist")
                .setSmallIcon(R.drawable.music_logo)
                .addAction(R.drawable.prev_ic_blck, "Previous", prevPendingIntent)
                .addAction(playPauseIcon, "Play/Pause", playPausePendingIntent)
                .addAction(R.drawable.next_ic_blck, "Next", nextPendingIntent)
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        notificationManager.notify(1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Music Player", NotificationManager.IMPORTANCE_LOW);
            notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static void updateNotification(boolean isPlaying) {
        if (instance != null) {
            instance.createNotification(instance.getCurrentSongTitle(), isPlaying);
        }
    }

    public String getCurrentSongTitle() {
        return txtsName.getText().toString();
    }

    public static void staticNextSong() {
        if (instance != null) {
            instance.nextSong();
        }
    }

    public static void staticPreviousSong() {
        if (instance != null) {
            instance.previousSong();
        }
    }
}

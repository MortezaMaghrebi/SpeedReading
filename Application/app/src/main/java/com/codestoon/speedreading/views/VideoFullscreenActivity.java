package com.codestoon.speedreading.views;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codestoon.speedreading.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class VideoFullscreenActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    private ImageView btnClose;
    private ProgressBar progressBar;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_fullscreen);

        playerView = findViewById(R.id.playerView);
        btnClose = findViewById(R.id.btnCloseVideo);
        progressBar = findViewById(R.id.progressBar);

        // Initialize ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        String folder = getIntent().getStringExtra("folder");
        String fileName = getIntent().getStringExtra("fileName");

        if (folder != null && fileName != null) {
            playVideoFromAssets(folder, fileName);
        } else {
            Toast.makeText(this, "فایل ویدیو یافت نشد", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnClose.setOnClickListener(v -> finish());

        // Handle player errors
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                handler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(VideoFullscreenActivity.this,
                            "خطا در پخش ویدیو: " + error.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    handler.post(() -> progressBar.setVisibility(View.GONE));
                }
            }
        });
    }

    private void playVideoFromAssets(String folder, String fileName) {
        progressBar.setVisibility(View.VISIBLE);

        try {
            // روش 1: استفاده از AssetFileDescriptor با ExoPlayer
            AssetFileDescriptor afd = getAssets().openFd("videos/" + folder + "/" + fileName);

            // ساخت Uri از AssetFileDescriptor
            Uri uri = Uri.parse("asset://videos/" + folder + "/" + fileName);

            // روش بهتر: کپی به کش و پخش
            copyAndPlayVideo(folder, fileName);

        } catch (Exception e) {
            e.printStackTrace();
            // اگر روش اول失败، روش دوم رو امتحان کن
            copyAndPlayVideo(folder, fileName);
        }
    }

    private void copyAndPlayVideo(String folder, String fileName) {
        try {
            // کپی فایل از assets به کش
            InputStream inputStream = getAssets().open("videos/" + folder + "/" + fileName);

            // ایجاد فایل در کش
            File cacheDir = getCacheDir();
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            File videoFile = new File(cacheDir, fileName);

            // کپی فایل
            FileOutputStream outputStream = new FileOutputStream(videoFile);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            // بررسی اینکه فایل کپی شده وجود داره
            if (!videoFile.exists() || videoFile.length() == 0) {
                Toast.makeText(this, "فایل ویدیو خالی است", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            // پخش با ExoPlayer از فایل کش
            DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this);
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(Uri.fromFile(videoFile)));

            player.setMediaSource(mediaSource);
            player.prepare();
            player.setPlayWhenReady(true);
            player.setRepeatMode(Player.REPEAT_MODE_OFF);

            // پاک کردن فایل بعد از بسته شدن (اختیاری)
            videoFile.deleteOnExit();

        } catch (Exception e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "خطا در کپی و پخش ویدیو: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
        handler.removeCallbacksAndMessages(null);
    }
}
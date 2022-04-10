package com.example.exoplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private com.google.android.exoplayer2.ui.PlayerView mPlayerView;
    private ExoPlayer mExoPlayer;
    private FloatingActionButton fab;

    private String song_url = "https://opengameart.org/sites/default/files/the_field_of_dreams.mp3";
    private String[] musicList = new String[]{
            "https://opengameart.org/sites/default/files/song18_0.mp3",
            "https://opengameart.org/sites/default/files/Heroic%20Demise%20%28New%29_0.mp3",
            "https://opengameart.org/sites/default/files/Woodland%20Fantasy_0.mp3",
            "https://opengameart.org/sites/default/files/happy_0.mp3"
    };

    private ProgressBar mProgressBar;
    private Player.Listener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlayerView = findViewById(R.id.my_player_view);
        mExoPlayer = new ExoPlayer.Builder(this).build();
        mProgressBar = findViewById(R.id.my_progress_bar);
        fab = findViewById(R.id.my_fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupPermissions();
            }
        });

        mPlayerView.setPlayer(mExoPlayer);

        for (String song_url :
                musicList) {
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(song_url));
            mExoPlayer.addMediaItem(mediaItem);
        }

//        mExoPlayer.setMediaItem(mediaItem);
        mExoPlayer.prepare();
        mExoPlayer.play();

        mListener = new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if(playbackState == Player.STATE_BUFFERING){
                    mProgressBar.setVisibility(View.VISIBLE);
                } else if(playbackState == Player.STATE_READY){
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        };
        mExoPlayer.addListener(mListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mExoPlayer != null){
            mExoPlayer.release();
        }
    }

    private void setupPermissions(){
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {

                // Download the current music
                if(report.areAllPermissionsGranted()) {
                    downloadTheCurrentMusic(mExoPlayer.getCurrentMediaItem().playbackProperties.uri.toString());
                }
            }
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                token.continuePermissionRequest();

            }
        }).check();
    }

    private void downloadTheCurrentMusic(String musicUrlString){
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(musicUrlString);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        String musicName = musicUrlString.substring(musicUrlString.lastIndexOf("/") + 1);
        request.setTitle(musicName);
        request.setVisibleInDownloadsUi(true);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, uri.getLastPathSegment());
        downloadManager.enqueue(request);
    }
}
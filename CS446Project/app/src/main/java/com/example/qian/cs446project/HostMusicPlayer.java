package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import static com.example.qian.cs446project.CS446Utils.broadcastIntentWithoutExtras;

public class HostMusicPlayer extends SynchronicityMusicPlayer {

    private BroadcastReceiver hostMusicPlayerReceiver;
    private IntentFilter hostMusicPlayerFilter;

    public HostMusicPlayer(final Context applicationContext, Playlist playlist) {
        super(applicationContext, playlist);
        // Broadcast the session playlist.
        Intent returnPlaylistIntent =
                new Intent(applicationContext
                        .getString(R.string.return_session_playlist));
        returnPlaylistIntent.putExtra(applicationContext
                .getString(R.string.session_playlist), HostMusicPlayer.this.playlist);
        LocalBroadcastManager.getInstance(applicationContext)
                .sendBroadcast(returnPlaylistIntent);
        hostMusicPlayerFilter = new IntentFilter();
        hostMusicPlayerFilter.addAction(applicationContext.getString(R.string.receive_stop));
        hostMusicPlayerFilter.addAction(applicationContext.getString(R.string.receive_play));
        hostMusicPlayerFilter
                .addAction(applicationContext.getString(R.string.receive_pause));
        hostMusicPlayerReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Context applicationContext = HostMusicPlayer.this.applicationContext;
                if (action.equals(applicationContext.getString(R.string.receive_stop))) {
                    mediaPlayer.stop();
                } else if (action.equals(applicationContext.getString(R.string.receive_play))) {
                    startMediaPlayer();
                } else if (action.equals(applicationContext.getString(R.string.receive_pause))) {
                    mediaPlayer.pause();
                }
            }

        };
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
                hostMusicPlayerReceiver, hostMusicPlayerFilter
        );
        createMediaPlayer();
    }

    @Override
    public void play() {
        if (!musicPlayerState.isPlaying()) {
            super.play();
            // Broadcast an Intent to indicate that the GUI of the host's phone must update to
            // reflect the fact that the playlist is playing.
            broadcastIntentWithoutExtras(applicationContext.getString(R.string.playing_update_GUI),
                    applicationContext);
        }
    }

    @Override
    public void pause() {
        if (!musicPlayerState.isPaused()) {
            super.pause();
            // Broadcast an Intent to indicate that the GUI of the host's phone must update to
            // reflect the fact that the playlist is paused.
            broadcastIntentWithoutExtras(applicationContext.getString(R.string.paused_update_GUI),
                    applicationContext);
        }
    }

    @Override
    public void stop() {
        if (!musicPlayerState.isStopped()) {
            super.stop();
            makeThisGroupPlay();
        }
    }

    void songCompleted(MediaPlayer mediaPlayer) {
        super.songCompleted(mediaPlayer);
        makeThisGroupPlay();
        if (musicPlayerState.isStopped()) {
            // Broadcast an Intent to indicate that the session playlist has transitioned to a
            // stopped state.
            broadcastIntentWithoutExtras(applicationContext.getString(R.string.playlist_stopped),
                    applicationContext);
        }
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        LocalBroadcastManager.getInstance(applicationContext)
                .unregisterReceiver(hostMusicPlayerReceiver);
        hostMusicPlayerFilter = null;
        hostMusicPlayerReceiver = null;
    }

}

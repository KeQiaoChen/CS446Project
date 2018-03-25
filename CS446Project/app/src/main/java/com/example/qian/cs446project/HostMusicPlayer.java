package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import static com.example.qian.cs446project.CS446Utils.broadcastIntentWithoutExtras;

public class HostMusicPlayer extends SynchronicityMusicPlayer {

    private BroadcastReceiver hostMusicPlayerReceiver;
    private IntentFilter hostMusicPlayerFilter;

    @Override
    void resetPlaylist() {
        super.resetPlaylist();
        // Broadcast an Intent to indicate that the session playlist has transitioned to a
        // stopped state.
        broadcastIntentWithoutExtras(applicationContext.getString(R.string.playlist_stopped),
                applicationContext);
    }

    public HostMusicPlayer(final Context applicationContext, Playlist playlist) {
        super(applicationContext, playlist);
        // Broadcast an Intent to tell the app that the user has created a session.
        broadcastIntentWithoutExtras(applicationContext.getString(R.string.session_created),
                applicationContext);
        hostMusicPlayerFilter = new IntentFilter();
        // When a participant joins the session, the connection manager requests the session
        // playlist from the host.
        hostMusicPlayerFilter.addAction(applicationContext.getString(R.string
                .request_session_playlist));
        hostMusicPlayerFilter.addAction(applicationContext.getString(R.string.receive_stop));
        hostMusicPlayerFilter.addAction(applicationContext.getString(R.string.receive_play));
        hostMusicPlayerFilter
                .addAction(applicationContext.getString(R.string.receive_pause));
        hostMusicPlayerReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Context applicationContext = HostMusicPlayer.this.applicationContext;
                if (action.equals(applicationContext.getString(R.string.request_session_playlist)))
                {
                    Intent returnPlaylistIntent =
                            new Intent(applicationContext
                                    .getString(R.string.return_session_playlist));
                    returnPlaylistIntent.putExtra(applicationContext
                            .getString(R.string.session_playlist), HostMusicPlayer.this.playlist);
                    LocalBroadcastManager.getInstance(applicationContext)
                            .sendBroadcast(returnPlaylistIntent);
                } else if (action.equals(applicationContext.getString(R.string.receive_stop))) {
                    mediaPlayer.stop();
                } else if (action.equals(applicationContext.getString(R.string.receive_play))) {
                    mediaPlayer.start();
                } else if (action.equals(applicationContext.getString(R.string.receive_pause))) {
                    mediaPlayer.pause();
                }
            }

        };
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
                hostMusicPlayerReceiver, hostMusicPlayerFilter
        );
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

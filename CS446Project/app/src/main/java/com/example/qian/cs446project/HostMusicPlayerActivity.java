package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.synchronicity.APBdev.connectivity.WifiConnectionManager;

/**
 * Created by Qian on 2018-02-20.
 */

public class HostMusicPlayerActivity extends SynchronicityMusicPlayerActivity {

    private ImageView playPauseButtons;
    private ImageView stopButton;
    private Playlist playlist;
    private IntentFilter hostMusicPlayerActivityFilter;
    private BroadcastReceiver hostMusicPlayerReceiver;
    private boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_music_player);
        playPauseButtons = findViewById(R.id.imageViewPlayPauseButtons);
        muteTogglingButton = findViewById(R.id.imageViewMuteTogglingButton);
        playPauseButtons.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                playUpdateGUINotifyService(view);
            }

        });
        stopButton = findViewById(R.id.imageViewStopButton);
        playlist = getIntent().getParcelableExtra(applicationContext
                .getString(R.string.session_playlist));
        // HostMusicPlayerActivity represents screen 6 in the mockup. A Playlist is passed to
        // HostMusicPlayerActivity to represent the session playlist.
        setPlaylist(playlist);
        baseConnectionManager.initiateSession("Demo");
    }

    @Override
    protected void onStart() {
        super.onStart();
        hostMusicPlayerActivityFilter = new IntentFilter();
        // When a user joins a session, the Play, Pause, and Stop buttons should be disabled for the
        // host because the new participant needs time to receive and download at least the 1st
        // song in the playlist.
        hostMusicPlayerActivityFilter
                .addAction(applicationContext.getString(R.string
                .participant_joined));
        // When all participants have finished downloading at least the 1st song in the playlist,
        // the host's Play, Pause, and Stop buttons should be enabled.
        hostMusicPlayerActivityFilter
                .addAction(applicationContext.getString(R.string
                .all_participants_ready));
        hostMusicPlayerActivityFilter.addAction(applicationContext.getString(R.string
                .user_chose_session));
        // Change the icon for the play/pause button to the pause icon and make it so that if the
        // host presses that button again, the session playlist pauses.
        hostMusicPlayerActivityFilter.addAction(applicationContext.getString(R.string
                .playing_update_GUI));
        // Change the icon for the play/pause button to the play icon and make it so that if the
        // host presses that button again, the session playlist resumes playing.
        hostMusicPlayerActivityFilter.addAction(applicationContext.getString(R.string
                .paused_update_GUI));
        hostMusicPlayerReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                TextView waitMessage = findViewById(R.id.textViewWaitMessage);
                if (action.equals(applicationContext.getString(R.string.participant_joined))) {
                    playPauseButtons.setEnabled(false);
                    stopButton.setEnabled(false);
                    waitMessage.setText(applicationContext.getString(R.string.wait_message));
                } else if (action
                        .equals(applicationContext.getString(R.string.all_participants_ready))) {
                    playPauseButtons.setEnabled(true);
                    stopButton.setEnabled(true);
                    waitMessage.setText(applicationContext.getString(R.string.ready_to_play));
                } else if (action.equals(applicationContext.getString(R.string.playing_update_GUI)))
                {
                    // TODO: Currently, the Music Player on the host's phone notifies the
                    // Connection Manager that the host has played, paused, or stopped the playlist
                    // by calling a method in the Connection Manager. We plan to replace these
                    // method calls with broadcast Intents.
                    baseConnectionManager.sendSig(WifiConnectionManager.SIG_PLAY_CODE);
                    playPauseButtons.setImageResource(android.R.drawable.ic_media_pause);
                    playPauseButtons.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            pauseUpdateGUINotifyService(view);
                        }

                    });
                } else if (action.equals(applicationContext.getString(R.string.paused_update_GUI)))
                {
                    // TODO: Currently, the Music Player on the host's phone notifies the
                    // Connection Manager that the host has played, paused, or stopped the playlist
                    // by calling a method in the Connection Manager. We plan to replace these
                    // method calls with broadcast Intents.
                    baseConnectionManager.sendSig(WifiConnectionManager.SIG_PAUSE_CODE);
                    enablePlay();
                }
            }

        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                hostMusicPlayerReceiver, hostMusicPlayerActivityFilter
        );
        // Broadcast an Intent to indicate that HostMusicPlayerActivity has started.
        Intent startedIntent = new Intent(applicationContext.getString(R.string
                .host_music_player_activity_started));
        startedIntent.putExtra(applicationContext.getString(R.string.session_playlist), playlist);
        LocalBroadcastManager.getInstance(this).sendBroadcast(startedIntent);
    }

    private void enablePlay() {
        playPauseButtons.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                playUpdateGUINotifyService(view);
            }

        });
        playPauseButtons.setImageResource(android.R.drawable.ic_media_play);
    }

    @Override
    void showPlaylistStopped() {
        super.showPlaylistStopped();
        // TODO: Currently, the Music Player on the host's phone notifies the Connection Manager
        // that the host has played, paused, or stopped the playlist by calling a method in the
        // Connection Manager. We plan to replace these method calls with broadcast Intents.
        baseConnectionManager.sendSig(WifiConnectionManager.SIG_STOP_CODE);
        enablePlay();
    }

    @Override
    void resetPlaylistGUI(boolean playlistPlayedUntilEnd) {
        super.resetPlaylistGUI(playlistPlayedUntilEnd);
        enablePlay();
    }

    @Override
    protected void onStop() {
        super.onStop();
        hostMusicPlayerActivityFilter = null;
        hostMusicPlayerReceiver = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(hostMusicPlayerReceiver);
    }

}

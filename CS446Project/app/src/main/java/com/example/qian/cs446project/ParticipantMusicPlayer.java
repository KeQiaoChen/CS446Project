package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

public class ParticipantMusicPlayer extends SynchronicityMusicPlayer {

    private BroadcastReceiver participantMusicPlayerReceiver;
    private IntentFilter participantMusicPlayerFilter;

    public ParticipantMusicPlayer(Context applicationContext, Playlist playlist) {
        super(applicationContext, playlist);
        participantMusicPlayerFilter = new IntentFilter();
        // When the 1st song in the session playlist finishes downloading onto the participant's
        // device, it creates a MediaPlayer with that song.
        participantMusicPlayerFilter
                .addAction(applicationContext.getString(R.string.song_finished_downloading));
        participantMusicPlayerReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Context applicationContext = ParticipantMusicPlayer.this.applicationContext;
                if (action.equals(applicationContext.getString(R.string.song_finished_downloading)))
                {
                    createMediaPlayer();
                }
            }

        };
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
                participantMusicPlayerReceiver, participantMusicPlayerFilter
        );
    }

    @Override
    public void play() {
        if (!musicPlayerState.isPlaying()) {
            super.play();
            mediaPlayer.start();
        }
    }

    @Override
    public void pause() {
        if (!musicPlayerState.isPaused()) {
            super.pause();
            mediaPlayer.pause();
        }
    }

    @Override
    public void stop() {
        if (!musicPlayerState.isStopped()) {
            super.stop();
            mediaPlayer.stop();
        }
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        LocalBroadcastManager.getInstance(applicationContext)
                .unregisterReceiver(participantMusicPlayerReceiver);
        participantMusicPlayerFilter = null;
        participantMusicPlayerReceiver = null;
    }

}

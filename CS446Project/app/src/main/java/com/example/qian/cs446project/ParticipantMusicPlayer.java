package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import static com.example.qian.cs446project.CS446Utils.broadcastIntentWithoutExtras;

public class ParticipantMusicPlayer extends SynchronicityMusicPlayer {

    private BroadcastReceiver participantMusicPlayerReceiver;
    private IntentFilter participantMusicPlayerFilter;
    private boolean myTurnToPlay = false;

    public ParticipantMusicPlayer(Context applicationContext, Playlist playlist) {
        super(applicationContext, playlist);
        String temporaryDirectory = applicationContext.getCacheDir().toString();
        String title;
        String filePath;
        Song ithSong;
        for (int i = 0; i < playlist.songs.size(); ++i) {
            ithSong = playlist.songs.get(i);
            filePath = ithSong.getFilePath();
            title = filePath.substring(filePath.lastIndexOf('/'));
            playlist.songs.get(i).setFilePath(temporaryDirectory + title);
        }
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
                    mediaPlayer.setVolume(0, 0);
                    broadcastIntentWithoutExtras(
                            applicationContext.getString(R.string.other_group_plays),
                            ParticipantMusicPlayer.this.applicationContext);
                }
            }

        };
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
                participantMusicPlayerReceiver, participantMusicPlayerFilter
        );
        createMediaPlayer();
        mediaPlayer.setVolume(0, 0);
        broadcastIntentWithoutExtras(applicationContext.getString(R.string.other_group_plays),
                ParticipantMusicPlayer.this.applicationContext);
    }

    @Override
    public void play() {
        if (!musicPlayerState.isPlaying()) {
            super.play();
            startMediaPlayer();
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
            makeThisGroupMute();
        }
    }

    @Override
    void songCompleted(MediaPlayer mediaPlayer) {
        super.songCompleted(mediaPlayer);
        makeThisGroupMute();
    }

    @Override
    boolean getMyTurnToPlay() {
        return myTurnToPlay;
    }

    @Override
    void setMyTurnToPlay(boolean myTurnToPlay) {
        this.myTurnToPlay = myTurnToPlay;
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

package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

import static com.example.qian.cs446project.CS446Utils.broadcastIntentWithoutExtras;

/**
 * Created by Qian on 2018-03-18.
 */

public abstract class SynchronicityMusicPlayer implements MusicPlayer {

    MusicPlayerState musicPlayerState = new MusicPlayerStoppedState();
    private boolean muted = false;
    private boolean movingToNextSong = false;
    private int currentSong = 0;
    MediaPlayer mediaPlayer;
    Context applicationContext;
    Playlist playlist;
    private IntentFilter synchronicityMusicPlayerFilter;
    private BroadcastReceiver synchronicityMusicPlayerReceiver;
    private int secondsSinceLastSwitched = 0;
    private boolean myTurnToPlay = true;

    public SynchronicityMusicPlayer(final Context applicationContext, Playlist playlist) {
        this.applicationContext = applicationContext;
        this.playlist = playlist;
        synchronicityMusicPlayerFilter = new IntentFilter();
        // Play the playlist on the user's phone when the user presses the play button (in the
        // case that the user is the host) or when the user's phone receives the play signal (in
        // the case that the user is a participant).
        synchronicityMusicPlayerFilter.addAction(applicationContext.getString(R.string.play));
        // Pause the playlist on the user's phone when the user presses the pause button (in the
        // case that the user is the host) or when the user's phone receives the pause signal (in
        // the case that the user is a participant).
        synchronicityMusicPlayerFilter.addAction(applicationContext.getString(R.string
                .pause));
        // Stop the playlist on the user's phone when the user presses the stop button (in the
        // case that the user is the host) or when the user's phone receives the stop signal (in
        // the case that the user is a participant)
        synchronicityMusicPlayerFilter.addAction(applicationContext.getString(R.string.stop));
        // Mutes the playlist on the user's phone when the user presses the mute button.
        synchronicityMusicPlayerFilter.addAction(applicationContext.getString(R.string.mute));
        // Unmutes the playlist on the user's phone when the user presses the unmute button.
        synchronicityMusicPlayerFilter.addAction(applicationContext.getString(R.string.unmute));
        synchronicityMusicPlayerReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(applicationContext.getString(R.string.play))) {
                    play();
                } else if (action.equals(applicationContext.getString(R.string.pause))) {
                    pause();
                } else if (action.equals(applicationContext.getString(R.string.stop))) {
                    stop();
                } else if (action.equals(applicationContext.getString(R.string.mute))) {
                    mute();
                } else if (action.equals(applicationContext.getString(R.string.unmute))) {
                    unmute();
                }
            }

        };
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
                synchronicityMusicPlayerReceiver, synchronicityMusicPlayerFilter
        );
    }

    private void setMediaPlayerToCurrentSong() {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(playlist.songs.get(currentSong).getFilePath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetPlaylist() {
        currentSong = 0;
        setMediaPlayerToCurrentSong();
    }

    void songCompleted(MediaPlayer mediaPlayer) {
        movingToNextSong = true;
        secondsSinceLastSwitched = 0;
        if (currentSong < playlist.songs.size() - 1) {
            ++currentSong;
            setMediaPlayerToCurrentSong();
            mediaPlayer.start();
        } else {
            currentSong = 0;
            musicPlayerState = new MusicPlayerStoppedState();
        }
        // Broadcast an Intent to indicate that a song in the session playlist has completed.
        Intent onCompletionIntent =
                new Intent(applicationContext.getString(R.string.song_completed));
        onCompletionIntent
                .putExtra(applicationContext.getString(R.string.current_song_index), currentSong);
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(onCompletionIntent);
        movingToNextSong = false;
    }

    public void createMediaPlayer() {
        mediaPlayer = MediaPlayer.create(applicationContext,
                Uri.parse(playlist.songs.get(currentSong).getFilePath()));
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                songCompleted(mediaPlayer);
            }

        });

    }

    boolean getMyTurnToPlay() {
        return myTurnToPlay;
    }

    void setMyTurnToPlay(boolean myTurnToPlay) {
        this.myTurnToPlay = myTurnToPlay;
    }

    void makeThisGroupMute() {
        setMyTurnToPlay(false);
        mediaPlayer.setVolume(0, 0);
        // Broadcast an Intent to indicate that it is now the other group's turn to play the
        // playlist.
        broadcastIntentWithoutExtras(applicationContext.getString(R.string.other_group_plays),
                SynchronicityMusicPlayer.this.applicationContext);
    }

    void makeThisGroupPlay() {
        setMyTurnToPlay(true);
        if (!muted) {
            mediaPlayer.setVolume(1, 1);
        }
        // Broadcast an Intent to indicate that it is now the user's group's turn to play the
        // playlist.
        broadcastIntentWithoutExtras(applicationContext.getString(R.string.this_group_plays),
                SynchronicityMusicPlayer.this.applicationContext);
    }

    @Override
    public void play() {
        if (musicPlayerState.isStopped()) {
            // Broadcast an Intent to indicate that the session playlist has transitioned from a
            // stopped state to a playing state.
            broadcastIntentWithoutExtras(
                    applicationContext.getString(R.string.playlist_not_stopped),
                    applicationContext);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    boolean playedDuringThisThread = false;
                    while (currentSong < playlist.songs.size() && mediaPlayer != null) {
                        try {
                            if (!movingToNextSong && musicPlayerState.isPlaying()) {
                                playedDuringThisThread = true;
                                Intent updateSongProgressIndicatorsInGUIIntent =
                                        new Intent(applicationContext.getString(R.string
                                                .update_song_progress));
                                updateSongProgressIndicatorsInGUIIntent.putExtra(
                                        applicationContext.getString(R.string
                                                .currently_playing_song_index), currentSong
                                );
                                updateSongProgressIndicatorsInGUIIntent.putExtra(
                                        applicationContext.getString(R.string
                                                .current_position), mediaPlayer.getCurrentPosition()
                                );
                                // Broadcast an Intent to indicate that the progress bar, elapsed
                                // time, and remaining time for the currently playing song must be
                                // updated in the GUI.
                                LocalBroadcastManager.getInstance(applicationContext)
                                        .sendBroadcast(updateSongProgressIndicatorsInGUIIntent);
                                ++secondsSinceLastSwitched;
                                if (secondsSinceLastSwitched == 60) {
                                    secondsSinceLastSwitched = 0;
                                    if (getMyTurnToPlay()) {
                                        makeThisGroupMute();
                                    } else {
                                        makeThisGroupPlay();
                                    }
                                }
                            } else if (playedDuringThisThread && musicPlayerState.isStopped()) {
                                break;
                            }
                            Thread.sleep(1000);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                }

            }).start();
        }
    }

    @Override
    public void pause() {
        musicPlayerState = new MusicPlayerPausedState();
    }

    @Override
    public void stop() {
        musicPlayerState = new MusicPlayerStoppedState();
        secondsSinceLastSwitched = 0;
        // Broadcast an Intent to indicate that the session playlist has transitioned to a stopped
        // state.
        broadcastIntentWithoutExtras(applicationContext.getString(R.string.playlist_stopped),
                applicationContext);
    }

    void startMediaPlayer() {
        if (musicPlayerState.isStopped()) {
            resetPlaylist();
        }
        musicPlayerState = new MusicPlayerPlayingState();
        mediaPlayer.start();
    }

    @Override
    public void mute() {
        if (!muted) {
            mediaPlayer.setVolume(0, 0);
            muted = true;
            // Broadcast an Intent to indicate that the playlist on the user's phone has been muted.
            broadcastIntentWithoutExtras(applicationContext.getString(R.string.muted_update_GUI),
                    applicationContext);
        }
    }

    @Override
    public void unmute() {
        if (muted) {
            mediaPlayer.setVolume(1, 1);
            muted = false;
            // Broadcast an Intent to indicate that the playlist on the user's phone has been
            // unmuted.
            broadcastIntentWithoutExtras(applicationContext.getString(R.string.unmuted_update_GUI),
                    applicationContext);
        }
    }

    public void cleanUp() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        LocalBroadcastManager.getInstance(applicationContext)
                .unregisterReceiver(synchronicityMusicPlayerReceiver);
        synchronicityMusicPlayerFilter = null;
        synchronicityMusicPlayerReceiver = null;
    }

}

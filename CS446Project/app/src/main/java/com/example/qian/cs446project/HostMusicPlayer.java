package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by Qian on 2018-02-20.
 */

public class HostMusicPlayer extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private MediaPlayer mediaPlayer;
    private Playlist playlist;
    private int previousSong = -1;
    private int currentSong = 0;
    private Boolean stopped = true;
    private Boolean muted = false;
    private Boolean movingToNextSong = false;
    private ImageView playPauseButtons;
    private ImageView stopButton;
    private ImageView muteTogglingButton;
    private TextView waitMessage;
    private SeekBar songProgressBar;
    private int songLength;
    private CustomMusicAdapter customMusicAdapter;
    private static final CS446Utils cs446Utils = new CS446Utils();
    private Context applicationContext = getApplicationContext();

    private void createMediaPlayer() {
        mediaPlayer = MediaPlayer.create(applicationContext,
                    Uri.parse(Environment.getExternalStorageDirectory().getPath() +
                    playlist.songs.get(currentSong).getFilePath()));
        mediaPlayer.setOnCompletionListener(this);
    }

    private BroadcastReceiver hostMusicPlayerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(applicationContext.getString(R.string.participant_joined))) {
                playPauseButtons.setEnabled(false);
                stopButton.setEnabled(false);
                waitMessage.setText(applicationContext.getString(R.string.wait_message));
            } else if (action.equals(applicationContext.getString(R.string.all_participants_ready)))
            {
                playPauseButtons.setEnabled(true);
                stopButton.setEnabled(true);
                waitMessage.setText(applicationContext.getString(R.string.ready_to_play));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_music_player);
        ListView listView = findViewById(R.id.songlist);
        // The HostMusicPlayer activity represents screen 6 in the mockup. A Playlist is passed to
        // the HostMusicPlayer activity to represent the session playlist.
        playlist = getIntent().getParcelableExtra(applicationContext.getString(R.string.session_playlist));
        currentSong = 0;
        muteTogglingButton = findViewById(R.id.muteTogglingButton);
        createMediaPlayer();
        customMusicAdapter = new CustomMusicAdapter(this, R.layout.song, playlist);
        listView.setAdapter(customMusicAdapter);
        playPauseButtons = findViewById(R.id.playPauseButtons);
        stopButton = findViewById(R.id.stopButton);
        waitMessage = findViewById(R.id.waitMessage);
        IntentFilter waitForDownload = new IntentFilter();
        // When a user joins a session, the Play, Pause, and Stop buttons should be disabled for the
        // host because the new participant needs time to receive and download at least the 1st
        // song in the playlist.
        waitForDownload.addAction(applicationContext.getString(R.string.participant_joined));
        // When all participants have finished downloading at least the 1st song in the playlist,
        // the host's Play, Pause, and Stop buttons should be enabled.
        waitForDownload.addAction(applicationContext.getString(R.string.all_participants_ready));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                hostMusicPlayerReceiver, waitForDownload
        );
    }

    private void broadcastIntent(String intentName) {
        Intent intentToBroadcast = new Intent(intentName);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentToBroadcast);
    }

    private void unboldPreviousSongMetadata() {
        if (previousSong >= 0) {
            customMusicAdapter.getTitles().get(previousSong).setTypeface(null, Typeface.NORMAL);
            customMusicAdapter.getArtists()
                    .get(previousSong).setTypeface(null, Typeface.NORMAL);
            customMusicAdapter.getAlbums().get(previousSong).setTypeface(null, Typeface.NORMAL);
        }
    }

    private void boldCurrentSongMetadata() {
        customMusicAdapter.getTitles().get(currentSong).setTypeface(null, Typeface.BOLD);
        customMusicAdapter.getArtists().get(currentSong).setTypeface(null, Typeface.BOLD);
        customMusicAdapter.getAlbums().get(currentSong).setTypeface(null, Typeface.BOLD);
    }

    // Ke Qiao Chen: I based this method on viewHolder.ivPlay's OnClickListener in
    // https://github.com/quocnguyenvan/media-player-demo/blob/master/app/src/main/java/com/quocnguyen/mediaplayerdemo/CustomMusicAdapter.java
    // except for the following changes:
    // - I named the variable that indicates whether or not the playlist is stopped "stopped"
    // instead of "flag" for clarity.
    // - I based the Thread that updates the progress bar, elapsed time, and remaining time on
    // https://www.youtube.com/watch?v=zCYQBIcePaw at 11:57 except for the following changes:
    //  - I stop trying to update these widgets when the user has stopped the playlist or when the
    //  playlist has finished.
    //  - I do not try to update these widgets when a song in the playlist has ended but the next
    //  has not yet begun.
    public void onTogglePlay(View v) {
        if (stopped) {
            songLength = playlist.songs.get(currentSong).getDuration();
            unboldPreviousSongMetadata();
            boldCurrentSongMetadata();
            // Thread to update the song progress bar, elapsed time, and remaining time
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!stopped && currentSong < playlist.songs.size() && mediaPlayer != null) {
                        try {
                            if (!movingToNextSong) {
                                Message message = new Message();
                                message.what = mediaPlayer.getCurrentPosition();
                                timeUpdateHandler.sendMessage(message);
                                Thread.sleep(1000);
                            }
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }
                }
            }).start();
            // At least in the prototype, we might want to prevent users from joining a session
            // while a session playlist is playing or paused. MusicPlayer broadcasts an intent when
            // the host starts the session playlist and another when the host stops the session
            // playlist or the playlist finishes. As a result, other components know when they
            // should prevent users from joining a session.
            broadcastIntent(applicationContext.getString(R.string.playlist_not_stopped));
            stopped = false;
        }
        if (mediaPlayer.isPlaying()) {
            // Broadcast an intent for all participants to pause the playlist.
            broadcastIntent(applicationContext.getString(R.string.pause));
            mediaPlayer.pause();
            playPauseButtons.setImageResource(R.drawable.play);
        } else {
            // Broadcast an intent for all participants to play the playlist.
            broadcastIntent(applicationContext.getString(R.string.play));
            mediaPlayer.start();
            playPauseButtons.setImageResource(R.drawable.pause);
        }
    }

    // Ke Qiao Chen: I based this method on https://www.youtube.com/watch?v=zCYQBIcePaw at 14:14
    // except that tutorial only plays a single song instead of a playlist. To update the progress
    // bar, elapsed time, and remaining time for specific songs, I use the currentSong variable to
    // specify a song in the playlist.
    private Handler timeUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            // Update the song progress bar.
            songProgressBar = customMusicAdapter.getSongProgressBars().get(currentSong);
            songProgressBar.setProgress(currentPosition);
            // Update elapsed time and remaining time.
            String elapsedTimeValue = cs446Utils.formatTime(currentPosition);
            TextView elapsedTime = customMusicAdapter.getElapsedTimes().get(currentSong);
            elapsedTime.setText(elapsedTimeValue);
            TextView remainingTime = customMusicAdapter.getRemainingTimes().get(currentSong);
            String remainingTimeValue =
                    cs446Utils.formatTime(songLength - currentPosition);
            remainingTime.setText("-" + remainingTimeValue);
        }
    };

    private void setMediaPlayerToCurrentSong() {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().getPath() +
                    playlist.songs.get(currentSong).getFilePath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetPlaylist() {
        stopped = true;
        // At least in the prototype, we might want to prevent users from joining a session
        // while a session playlist is playing or paused. MusicPlayer broadcasts an intent when
        // the host starts the session playlist and another when the host stops the session
        // playlist or the playlist finishes. As a result, other components know when they
        // should prevent users from joining a session.
        broadcastIntent(applicationContext.getString(R.string.playlist_stopped));
        if (currentSong < playlist.songs.size()) {
            previousSong = currentSong;
        } else {
            previousSong = playlist.songs.size() - 1;
        }
        unboldPreviousSongMetadata();
        currentSong = 0;
        setMediaPlayerToCurrentSong();
        for (int i = 0; i < playlist.songs.size(); ++i) {
            customMusicAdapter.getSongProgressBars().get(i).setProgress(0);
            customMusicAdapter.getElapsedTimes().get(i)
                    .setText(cs446Utils.formatTime(0));
            customMusicAdapter.getRemainingTimes().get(i).
                    setText("-" + cs446Utils.formatTime(playlist.songs.get(i).getDuration()));
        }
        playPauseButtons.setImageResource(R.drawable.play);
    }

    // Ke Qiao Chen: I based this method on viewHolder.ivStop's OnClickListener in
    // https://github.com/quocnguyenvan/media-player-demo/blob/master/app/src/main/java/com/quocnguyen/mediaplayerdemo/CustomMusicAdapter.java
    // except that
    // - I do not release the MediaPlayer because it is likely that the user will replay the
    // playlist after stopping it.
    // - I reset each song's progress bar, elapsed time, and remaining time. The tutorial does not
    // not include these widgets.
    public void onStop(View v) {
        if (!stopped) {
            // Broadcast an Intent for all participants to stop the playlist.
            broadcastIntent(applicationContext.getString(R.string.stop));
            mediaPlayer.stop();
            resetPlaylist();
        }
    }

    public void onToggleMute(View v) {
        if (muted) {
            mediaPlayer.setVolume(1, 1);
            muteTogglingButton.setImageResource(R.drawable.mute);
            muted = false;
        } else {
            mediaPlayer.setVolume(0, 0);
            muteTogglingButton.setImageResource(R.drawable.unmute);
            muted = true;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        movingToNextSong = true;
        previousSong = currentSong;
        unboldPreviousSongMetadata();
        ++currentSong;
        if (currentSong < playlist.songs.size()) {
            setMediaPlayerToCurrentSong();
            songProgressBar = customMusicAdapter.getSongProgressBars().get(currentSong);
            songLength = playlist.songs.get(currentSong).getDuration();
            boldCurrentSongMetadata();
            mediaPlayer.start();
        } else {
            resetPlaylist();
        }
        movingToNextSong = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopped = true;
        mediaPlayer.release();
        mediaPlayer = null;
    }

}

package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.synchronicity.APBdev.connectivity.BaseConnectionManager;
import com.synchronicity.APBdev.connectivity.WifiConnectionManager;

import static com.example.qian.cs446project.CS446Utils.broadcastIntentWithoutExtras;
import static com.example.qian.cs446project.CS446Utils.formatTime;

public class SynchronicityMusicPlayerActivity extends AppCompatActivity
        implements MusicPlayerActivity {

    private CustomMusicAdapter customMusicAdapter;
    Context applicationContext;
    ImageView muteTogglingButton;
    private Playlist playlist;
    private SeekBar songProgressBar;
    private int songLength;
    private int currentSong;
    private IntentFilter synchronicityMusicPlayerActivityFilter;
    private BroadcastReceiver synchronicityMusicPlayerActivityReceiver;
    // Andrew's stuff
    BaseConnectionManager baseConnectionManager;
    BroadcastReceiver wifiBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_music_player);
        applicationContext = getApplicationContext();
        baseConnectionManager = new WifiConnectionManager(this, this);
        wifiBroadcastReceiver = baseConnectionManager.getBroadcastReceiver();
        registerReceiver(wifiBroadcastReceiver, baseConnectionManager.getIntentFilter());
    }

    @Override
    protected void onStart() {
        super.onStart();
        synchronicityMusicPlayerActivityFilter = new IntentFilter();
        // When a song in the session playlist finishes, SynchronicityMusicPlayerActivity updates
        // the GUI to indicate that the next song is playing, if applicable. Otherwise,
        // SynchronicityMusicPlayerActivity resets the progress bar, elapsed time, and remaining
        // time of each song in the session playlist.
        synchronicityMusicPlayerActivityFilter
                .addAction(applicationContext.getString(R.string
                .song_completed));
        // When the session playlist transitions from a stopped state to a playing state, the
        // metadata of the first song should be bolded to show that it is playing.
        synchronicityMusicPlayerActivityFilter
                .addAction(applicationContext.getString(R.string.playlist_not_stopped));
        // Update the currently playing song's progress bar, elapsed time, and remaining time in the
        // GUI as needed.
        synchronicityMusicPlayerActivityFilter
                .addAction(applicationContext.getString(R.string.update_song_progress));
        // Update the GUI to reflect the fact that the playlist has been stopped.
        synchronicityMusicPlayerActivityFilter
                .addAction(applicationContext.getString(R.string
                .playlist_stopped));
        // Change the icon for the mute-toggling button to a speaker and make it so that if the
        // user presses that button again, the playlist unmutes on that phone.
        synchronicityMusicPlayerActivityFilter
                .addAction(applicationContext.getString(R.string.muted_update_GUI));
        // Change the icon for the mute-toggling button to a speaker that is crossed out and make it
        // so that if the user presses that button again, the playlist mutes on that phone.
        synchronicityMusicPlayerActivityFilter
                .addAction(applicationContext.getString(R.string.unmuted_update_GUI));
        // When it is the other group's turn to play the playlist, disable the mute toggling button.
        synchronicityMusicPlayerActivityFilter
                .addAction(applicationContext.getString(R.string.other_group_plays));
        // When it is the user's group's turn to play the playlist, enable the mute toggling button.
        synchronicityMusicPlayerActivityFilter
                .addAction(applicationContext.getString(R.string.this_group_plays));
        synchronicityMusicPlayerActivityReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(applicationContext.getString(R.string.song_completed))) {
                    songCompleted(intent.getIntExtra(applicationContext.getString(R.string
                            .current_song_index), 0));
                } else if (action.equals(applicationContext.getString(R.string
                        .playlist_not_stopped))) {
                    currentSong = 0;
                    songLength = playlist.songs.get(currentSong).getDuration();
                    boldSongMetadata(currentSong);
                } else if (action.equals(applicationContext.getString(R.string
                        .update_song_progress))) {
                    currentSong = intent.getIntExtra(
                            applicationContext.getString(R.string.currently_playing_song_index),
                            currentSong);
                    Message message = new Message();
                    message.what = intent.getIntExtra(applicationContext.getString(R.string
                            .current_position), 0);
                    timeUpdateHandler.sendMessage(message);
                } else if (action.equals(applicationContext.getString(R.string.muted_update_GUI))) {
                    muteTogglingButton.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
                    muteTogglingButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            unmuteUpdateGUINotifyService(view);
                        }

                    });
                } else if (action.equals(applicationContext.getString(R.string.unmuted_update_GUI)))
                {
                    muteTogglingButton.setImageResource(android.R.drawable.ic_lock_silent_mode);
                    muteTogglingButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            muteUpdateGUINotifyService(view);
                        }

                    });
                } else if (action.equals(applicationContext.getString(R.string.playlist_stopped))) {
                    showPlaylistStopped();
                } else if (action.equals(applicationContext.getString(R.string.other_group_plays)))
                {
                    muteTogglingButton.setEnabled(false);
                } else if (action.equals(applicationContext.getString(R.string.this_group_plays))) {
                    muteTogglingButton.setEnabled(true);
                }
            }

        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                synchronicityMusicPlayerActivityReceiver, synchronicityMusicPlayerActivityFilter
        );
    }

    void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        this.customMusicAdapter =
                new CustomMusicAdapter(this, R.layout.song_in_gui, playlist);
        ListView listView = findViewById(R.id.listViewSonglist);
        listView.setAdapter(customMusicAdapter);
    }

    private void unboldSongMetadata(int songIndex) {
        if (songIndex >= 0) {
            customMusicAdapter.getSongsInGUI().get(songIndex).getTitle()
                    .setTypeface(null, Typeface.NORMAL);
            customMusicAdapter.getSongsInGUI().get(songIndex).getArtist()
                    .setTypeface(null, Typeface.NORMAL);
            customMusicAdapter.getSongsInGUI().get(songIndex).getAlbum()
                    .setTypeface(null, Typeface.NORMAL);
        }
    }

    private void boldSongMetadata(int songIndex) {
        customMusicAdapter.getSongsInGUI().get(songIndex).getTitle()
                .setTypeface(null, Typeface.BOLD);
        customMusicAdapter.getSongsInGUI().get(songIndex).getArtist()
                .setTypeface(null, Typeface.BOLD);
        customMusicAdapter.getSongsInGUI().get(songIndex).getAlbum()
                .setTypeface(null, Typeface.BOLD);
    }

    void resetPlaylistGUI(boolean playlistPlayedUntilEnd) {
        if (playlistPlayedUntilEnd) {
            unboldSongMetadata(playlist.songs.size() - 1);
        }
        for (int i = 0; i < playlist.songs.size(); ++i) {
            customMusicAdapter.getSongsInGUI().get(i).getSongProgressBar().setProgress(0);
            customMusicAdapter.getSongsInGUI().get(i).getElapsedTime()
                    .setText(formatTime(0));
            customMusicAdapter.getSongsInGUI().get(i).getRemainingTime()
                    .setText("-" + formatTime(playlist.songs.get(i).getDuration()));
        }
    }

    private void songCompleted(int currentSong) {
        this.currentSong = currentSong;
        if (currentSong >= 1) {
            unboldSongMetadata(currentSong - 1);
            boldSongMetadata(currentSong);
        } else {
            resetPlaylistGUI(true);
        }
        songProgressBar = customMusicAdapter.getSongsInGUI().get(currentSong).getSongProgressBar();
        songLength = playlist.songs.get(currentSong).getDuration();
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
            songProgressBar =
                    customMusicAdapter.getSongsInGUI().get(currentSong).getSongProgressBar();
            songProgressBar.setProgress(currentPosition);
            // Update elapsed time and remaining time.
            String elapsedTimeValue = formatTime(currentPosition);
            TextView elapsedTime =
                    customMusicAdapter.getSongsInGUI().get(currentSong).getElapsedTime();
            elapsedTime.setText(elapsedTimeValue);
            TextView remainingTime =
                    customMusicAdapter.getSongsInGUI().get(currentSong).getRemainingTime();
            String remainingTimeValue =
                    formatTime(songLength - currentPosition);
            remainingTime.setText("-" + remainingTimeValue);
        }
    };

    @Override
    public void playUpdateGUINotifyService(View view) {
        // Broadcast an Intent to indicate that the user has pressed the play button (in the case
        // that the user is the host) or that the user's phone has received a play signal (in the
        // case that the user is a participant).
        broadcastIntentWithoutExtras(applicationContext.getString(R.string.play),
                this);
    }

    @Override
    public void pauseUpdateGUINotifyService(View view) {
        // Broadcast an Intent to indicate that the user has pressed the pause button (in the case
        // that the user is the host) or that the user's phone has received a pause signal (in the
        // case that the user is a participant).
        broadcastIntentWithoutExtras(applicationContext.getString(R.string.pause),
                this);
    }

    @Override
    public void stopUpdateGUINotifyService(View view) {
        // Broadcast an Intent to indicate that the user has pressed the stop button (in the case
        // that the user is the host) or that the user's phone has received a stop signal (in the
        // case that the user is a participant).
        broadcastIntentWithoutExtras(applicationContext.getString(R.string.stop),
                this);
    }

    void showPlaylistStopped() {
        unboldSongMetadata(currentSong);
        resetPlaylistGUI(false);
    }

    @Override
    public void muteUpdateGUINotifyService(View view) {
        // Broadcast an Intent to indicate that the user has pressed the mute button.
        broadcastIntentWithoutExtras(applicationContext.getString(R.string.mute),
                this);
    }

    @Override
    public void unmuteUpdateGUINotifyService(View view) {
        // Broadcast an Intent to indicate that the user has pressed the unmute button.
        broadcastIntentWithoutExtras(applicationContext.getString(R.string.unmute),
                this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        customMusicAdapter = null;
        baseConnectionManager.cleanUp();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(wifiBroadcastReceiver);
        wifiBroadcastReceiver = null;
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(synchronicityMusicPlayerActivityReceiver);
        synchronicityMusicPlayerActivityFilter = null;
        synchronicityMusicPlayerActivityReceiver = null;
    }

}

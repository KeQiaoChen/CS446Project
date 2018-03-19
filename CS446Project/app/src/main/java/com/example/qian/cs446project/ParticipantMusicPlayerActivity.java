package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.synchronicity.APBdev.connectivity.BaseConnectionManager;
import com.synchronicity.APBdev.connectivity.WifiConnectionManager;

import java.io.IOException;
import java.util.ArrayList;

import static android.widget.Toast.makeText;
import static com.example.qian.cs446project.CS446Utils.formatTime;

/**
 * Created by Qian on 2018-02-21.
 */

public class ParticipantMusicPlayerActivity extends AppCompatActivity
        implements MediaPlayer.OnCompletionListener {

    private MediaPlayer mediaPlayer;
    private Playlist playlist;
    private int previousSong = -1;
    private int currentSong = 0;
    private Boolean stopped = true;
    private Boolean muted = false;
    private Boolean movingToNextSong = false;
    private ImageView muteTogglingButton;
    private SeekBar songProgressBar;
    private int songLength;
    private CustomMusicAdapter customMusicAdapter;
    private Context applicationContext;
    private ListView listView;
    private TextView waitMessage;
    private IntentFilter participantIntentFilter;
    private BroadcastReceiver participantMusicPlayerReceiver;
    // Andrew's stuff
    private BaseConnectionManager baseConnectionManager;
    private BroadcastReceiver wifiBroadcastReceiver;

    private void createMediaPlayer() {
        mediaPlayer = MediaPlayer.create(applicationContext,
                Uri.parse(playlist.songs.get(currentSong).getFilePath()));
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_music_player);
        listView = findViewById(R.id.listViewSonglist);
        currentSong = 0;
        muteTogglingButton = findViewById(R.id.imageViewMuteTogglingButton);
        waitMessage = findViewById(R.id.textViewWaitForSongs);
        applicationContext = getApplicationContext();
        ArrayList<Playlist> allPlaylists = PlaylistManager.listAllAppPlaylists(applicationContext);
        baseConnectionManager = new WifiConnectionManager(this, this);
        wifiBroadcastReceiver = baseConnectionManager.getBroadcastReceiver();
        if (allPlaylists == null || allPlaylists.size() == 0) {
            Toast errorToast = Toast.makeText(applicationContext, "Participant failed to retrieve session playlist.",
                    Toast.LENGTH_SHORT);
            errorToast.setMargin(50, 50);
            errorToast.show();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(participantMusicPlayerReceiver);
            participantIntentFilter = null;
            participantMusicPlayerReceiver = null;
            stopped = true;
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            baseConnectionManager.cleanUp();
            System.exit(1);
        } else {
            playlist = allPlaylists.get(0);
            customMusicAdapter =
                    new CustomMusicAdapter(ParticipantMusicPlayerActivity.this,
                            R.layout.song_in_gui, playlist);
            listView.setAdapter(customMusicAdapter);
            waitMessage.setVisibility(View.INVISIBLE);
            PlaylistManager.listAllPlaylistSongs(applicationContext, playlist);

            if (playlist.songs == null || playlist.songs.size() == 0) {
                Toast errorToast = Toast.makeText(applicationContext, "Participant failed to retrieve session playlist songs.",
                        Toast.LENGTH_SHORT);
                errorToast.setMargin(50, 50);
                errorToast.show();
                LocalBroadcastManager.getInstance(this).unregisterReceiver(participantMusicPlayerReceiver);
                participantIntentFilter = null;
                participantMusicPlayerReceiver = null;
                stopped = true;
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                baseConnectionManager.cleanUp();
                System.exit(1);
            }
        }
        // Andrew's code
        registerReceiver(wifiBroadcastReceiver, baseConnectionManager.getIntentFilter());
        baseConnectionManager.joinSession("Demo");
        createMediaPlayer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        participantIntentFilter = new IntentFilter();
        // When the 1st song in the session playlist finishes downloading onto the participant's
        // device, it creates a MediaPlayer with that song.
        participantIntentFilter
                .addAction(applicationContext.getString(R.string.song_finished_downloading));
        // When the host presses Play for the session playlist, the session playlist should play on
        // the participant's device.
        participantIntentFilter.addAction(applicationContext.getString(R.string.receive_play));
        // When the host pauses the session playlist, the session playlist should pause on the
        // participant's device.
        participantIntentFilter.addAction(applicationContext.getString(R.string.receive_pause));
        // When the host stops the session playlist, the session playlist should stop on the
        // the participant's device.
        participantIntentFilter.addAction(applicationContext.getString(R.string.receive_stop));
        // When the metadata for all songs in the session playlist is available for the
        // participant, ParticipantMusicPlayerActivity populates its ListView with those songs.
        participantIntentFilter.addAction(applicationContext.getString(R.string.playlist_ready));
        participantMusicPlayerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(applicationContext.getString(R.string.song_finished_downloading)))
                {
                    createMediaPlayer();
                } else if (action.equals(applicationContext.getString(R.string.receive_play))) {
                    play();
                } else if (action.equals(applicationContext.getString(R.string.receive_pause))) {
                    mediaPlayer.pause();
                } else if (action.equals(applicationContext.getString(R.string.receive_stop))) {
                    stop();
                } else if (action.equals(applicationContext.getString(R.string.playlist_ready))) {
                    playlist = intent.getParcelableExtra(applicationContext
                            .getString(R.string.session_playlist));
                    customMusicAdapter =
                            new CustomMusicAdapter(ParticipantMusicPlayerActivity.this,
                                    R.layout.song_in_gui, playlist);
                    listView.setAdapter(customMusicAdapter);
                    waitMessage.setVisibility(View.INVISIBLE);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                participantMusicPlayerReceiver, participantIntentFilter
        );
        // Broadcast an Intent to tell other components that the user has joined a certain
        // session and is requesting the session playlist. This Intent contains the session name.
        Intent participantJoinedSessionIntent =
                new Intent(applicationContext.getString(R.string.user_chose_session));
        String sessionName =
                getIntent().getStringExtra(applicationContext.getString(R.string.session_name));
        participantJoinedSessionIntent
                .putExtra(applicationContext.getString(R.string.name_of_chosen_session),
                        sessionName);
        LocalBroadcastManager.getInstance(this).sendBroadcast(participantJoinedSessionIntent);
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
        stopped = true;
        if (currentSong < playlist.songs.size()) {
            previousSong = currentSong;
        } else {
            previousSong = playlist.songs.size() - 1;
        }
        unboldPreviousSongMetadata();
        currentSong = 0;
        setMediaPlayerToCurrentSong();
        for (int i = 0; i < playlist.songs.size(); ++i) {
            customMusicAdapter.getSongsInGUI().get(i).getSongProgressBar().setProgress(0);
            customMusicAdapter.getSongsInGUI().get(i).getElapsedTime()
                    .setText(formatTime(0));
            customMusicAdapter.getSongsInGUI().get(i).getRemainingTime()
                    .setText("-" + formatTime(playlist.songs.get(i).getDuration()));
        }
    }

    private void unboldPreviousSongMetadata() {
        if (previousSong >= 0) {
            customMusicAdapter.getSongsInGUI().get(previousSong).getTitle()
                    .setTypeface(null, Typeface.NORMAL);
            customMusicAdapter.getSongsInGUI().get(previousSong).getArtist()
                    .setTypeface(null, Typeface.NORMAL);
            customMusicAdapter.getSongsInGUI().get(previousSong).getAlbum()
                    .setTypeface(null, Typeface.NORMAL);
        }
    }

    private void boldCurrentSongMetadata() {
        customMusicAdapter.getSongsInGUI().get(currentSong).getTitle()
                .setTypeface(null, Typeface.BOLD);
        customMusicAdapter.getSongsInGUI().get(currentSong).getArtist()
                .setTypeface(null, Typeface.BOLD);
        customMusicAdapter.getSongsInGUI().get(currentSong).getAlbum()
                .setTypeface(null, Typeface.BOLD);
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
    private void play() {
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
            stopped = false;
        }
        mediaPlayer.start();
    }

    // Ke Qiao Chen: I based this method on viewHolder.ivStop's OnClickListener in
    // https://github.com/quocnguyenvan/media-player-demo/blob/master/app/src/main/java/com/quocnguyen/mediaplayerdemo/CustomMusicAdapter.java
    // except that
    // - I do not release the MediaPlayer because it is likely that the user will replay the
    // playlist after stopping it.
    // - I reset each song's progress bar, elapsed time, and remaining time. The tutorial does not
    // include these widgets.
    private void stop() {
        if (!stopped) {
            mediaPlayer.stop();
            resetPlaylist();
        }
    }

    public void onToggleMute(View v) {
        if (muted) {
            mediaPlayer.setVolume(1, 1);
            muteTogglingButton.setImageResource(android.R.drawable.ic_lock_silent_mode);
            muted = false;
        } else {
            mediaPlayer.setVolume(0, 0);
            muteTogglingButton.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
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
            songProgressBar =
                    customMusicAdapter.getSongsInGUI().get(currentSong).getSongProgressBar();
            songLength = playlist.songs.get(currentSong).getDuration();
            boldCurrentSongMetadata();
            mediaPlayer.start();
        } else {
            resetPlaylist();
        }
        movingToNextSong = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(participantMusicPlayerReceiver);
        participantIntentFilter = null;
        participantMusicPlayerReceiver = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopped = true;
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        baseConnectionManager.cleanUp();
    }

}

package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Qian on 2018-02-21.
 */

public class ParticipantMusicPlayerActivity extends SynchronicityMusicPlayerActivity {

    private Playlist playlist;
    private Context applicationContext;
    private TextView waitMessage;
    private IntentFilter participantIntentFilter;
    private BroadcastReceiver participantMusicPlayerReceiver;
    private boolean receivedPivotChoice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_music_player);
        muteTogglingButton = findViewById(R.id.imageViewMuteTogglingButton);
        waitMessage = findViewById(R.id.textViewWaitForSongs);
        applicationContext = getApplicationContext();
        // TODO: Since our application cannot yet send files from one device to another,
        // participants just use the first of their personal playlists.
//        ArrayList<Playlist> allPlaylists = PlaylistManager.listAllAppPlaylists(applicationContext);
//        if (allPlaylists == null || allPlaylists.size() == 0) {
//            Toast errorToast = Toast.makeText(applicationContext, "Participant failed to retrieve session playlist.",
//                    Toast.LENGTH_SHORT);
//            errorToast.setMargin(50, 50);
//            errorToast.show();
//            //baseConnectionManager.cleanUp();
//            System.exit(1);
//        } else {
//            playlist = allPlaylists.get(0);
//            PlaylistManager.listAllPlaylistSongs(applicationContext, playlist);
//
//            if (playlist.songs == null || playlist.songs.size() == 0) {
//                Toast errorToast = Toast.makeText(applicationContext, "Participant failed to retrieve session playlist songs.",
//                        Toast.LENGTH_SHORT);
//                errorToast.setMargin(50, 50);
//                errorToast.show();
//                LocalBroadcastManager.getInstance(this).unregisterReceiver(participantMusicPlayerReceiver);
//                participantIntentFilter = null;
//                participantMusicPlayerReceiver = null;
//                //baseConnectionManager.cleanUp();
//                System.exit(1);
//            }
//            setPlaylist(playlist);
//            waitMessage.setVisibility(View.INVISIBLE);
//            // Broadcast an Intent to indicate that ParticipantMusicPlayerActivity has
//            // started and has the playlist.
//            Intent startedIntent = new Intent(applicationContext.getString(R.string
//                    .participant_music_player_activity_started));
//            startedIntent.putExtra(applicationContext.getString(R.string.session_playlist),
//                    playlist);
//            LocalBroadcastManager.getInstance(ParticipantMusicPlayerActivity.this)
//                    .sendBroadcast(startedIntent);
//            // Andrew's code
//            //baseConnectionManager.joinSession("Demo");
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        participantIntentFilter = new IntentFilter();
        // When the host presses Play for the session playlist, the session playlist should play on
        // the participant's device.
        participantIntentFilter.addAction(applicationContext.getString(R.string.receive_play));
        // When the host pauses the session playlist, the session playlist should pause on the
        // participant's device.
        participantIntentFilter.addAction(applicationContext.getString(R.string.receive_pause));
        // When the host stops the session playlist, the session playlist should stop on the
        // the participant's device.
        participantIntentFilter.addAction(applicationContext.getString(R.string.receive_stop));
        // When the playlist metadata is ready on the participant's phone, show its metadata in the
        // GUI and broadcast an Intent to indicate that ParticipantMusicPlayerActivity has started
        // and has the playlist.
        participantIntentFilter.addAction(applicationContext.getString(R.string.playlist_ready));
        participantIntentFilter
                .addAction(applicationContext.getString(R.string.pivot_choice_ready));
        participantMusicPlayerReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(applicationContext.getString(R.string.receive_play))) {
                    playUpdateGUINotifyService(null);
                } else if (action.equals(applicationContext.getString(R.string.receive_pause))) {
                    pauseUpdateGUINotifyService(null);
                } else if (action.equals(applicationContext.getString(R.string.receive_stop))) {
                    stopUpdateGUINotifyService(null);
                } else if (action.equals(applicationContext.getString(R.string.playlist_ready))) {
                    playlist = intent.getParcelableExtra(applicationContext
                            .getString(R.string.session_playlist));
                    setPlaylist(playlist);
                    waitMessage.setVisibility(View.INVISIBLE);
                    if (receivedPivotChoice) {
                        // Broadcast an Intent to indicate that ParticipantMusicPlayerActivity has
                        // started and has the playlist.
                        Intent startedIntent =
                                new Intent(applicationContext.getString(R.string
                                        .participant_music_player_activity_started));
                        startedIntent.putExtra(
                                applicationContext.getString(R.string.session_playlist), playlist);

                        LocalBroadcastManager.getInstance(ParticipantMusicPlayerActivity.this)
                                .sendBroadcast(startedIntent);
                    }
                } else if (action.equals(applicationContext.getString(R.string.pivot_choice_ready)))
                {
                    receivedPivotChoice = true;
                    if (playlist != null) {
                        Intent startedIntent =
                                new Intent(applicationContext.getString(R.string
                                        .participant_music_player_activity_started));
                        startedIntent.putExtra(
                                applicationContext.getString(R.string.session_playlist), playlist);
                        startedIntent.putExtra(
                                applicationContext.getString(R.string.is_pivot_on), true);
                        LocalBroadcastManager.getInstance(ParticipantMusicPlayerActivity.this)
                                .sendBroadcast(startedIntent);
                    }
                }
            }

        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                participantMusicPlayerReceiver, participantIntentFilter
        );
        // Broadcast an Intent to tell other components that the user has joined a certain session
        // and is requesting the session playlist. This Intent contains the session name.
        Intent participantJoinedSessionIntent =
                new Intent(applicationContext.getString(R.string.join_session_message));
        String sessionName =
                getIntent().getStringExtra(applicationContext.getString(R.string.session_name));
        participantJoinedSessionIntent
                .putExtra(applicationContext.getString(R.string.session_name_key),
                        sessionName);
        LocalBroadcastManager.getInstance(this).sendBroadcast(participantJoinedSessionIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(participantMusicPlayerReceiver);
        participantIntentFilter = null;
        participantMusicPlayerReceiver = null;
    }

}

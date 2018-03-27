package com.example.qian.cs446project;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.synchronicity.APBdev.connectivity.ConnectionManager;
import com.synchronicity.APBdev.connectivity.WifiConnectionManager;

public class MainService extends Service {

    private IntentFilter mainServiceFilter;
    private BroadcastReceiver mainServiceReceiver;
    private HostMusicPlayer hostMusicPlayer;
    private ParticipantMusicPlayer participantMusicPlayer;
    private PlaylistManager playlistManager;
    private ConnectionManager connectionManager;

    public MainService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        final Context applicationContext = getApplicationContext();

        // Hari's component instantiation.
        playlistManager = new PlaylistManager(getApplicationContext());
        // Andrew's component instantiation.
        connectionManager = new WifiConnectionManager(applicationContext);

        mainServiceFilter = new IntentFilter();
        // When HostMusicPlayerActivity starts, create an instance of HostMusicPlayer, which acts as
        // the model in the MVC design pattern such that HostMusicPlayerActivity is the view.
        mainServiceFilter.addAction(applicationContext.getString(R.string
                .host_music_player_activity_started));
        // When ParticipantMusicPlayerActivity starts and receives the playlist, create an instance
        // of ParticipantMusicPlayer, which acts as the model in the MVC design pattern such that
        // ParticipantMusicPlayerActivity is the view.
        mainServiceFilter.addAction(applicationContext.getString(R.string
                .participant_music_player_activity_started));
        mainServiceReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(applicationContext.getString(R.string
                        .host_music_player_activity_started))) {
                    Playlist playlist = intent.getParcelableExtra(
                            applicationContext.getString(R.string.session_playlist));
                    boolean pivotOn = intent.getBooleanExtra(
                            applicationContext.getString(R.string.is_pivot_on), true);
                    hostMusicPlayer =
                            new HostMusicPlayer(MainService.this, playlist,
                                    pivotOn);
                } else if (action
                        .equals(applicationContext.getString(R.string
                        .participant_music_player_activity_started))) {
                    Playlist playlist = intent.getParcelableExtra(
                            applicationContext.getString(R.string.session_playlist));
                    boolean isPivotOn = intent.getBooleanExtra(
                            applicationContext.getString(R.string.is_pivot_on), true);
                    participantMusicPlayer =
                            new ParticipantMusicPlayer(MainService.this, playlist,
                                    isPivotOn);
                }
            }

        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mainServiceReceiver, mainServiceFilter
        );
        return mainBinder;
    }

    public class MainBinder extends Binder {

        MainService getBinder() {
            return MainService.this;
        }

    }

    MainBinder mainBinder = new MainBinder();

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ServiceDestroy>","Started destroying service.");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mainServiceReceiver);
        mainServiceFilter = null;
        mainServiceReceiver = null;
        if (hostMusicPlayer != null) {
            hostMusicPlayer.cleanUp();
            hostMusicPlayer = null;
        }
        if (participantMusicPlayer != null) {
            participantMusicPlayer.cleanUp();
            participantMusicPlayer = null;
        }

        /*
        CONNECTION MANAGER CLEANUP CODE
         */
        if(connectionManager != null) {
            connectionManager.cleanUp();
        }

    }

}

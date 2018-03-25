package com.example.qian.cs446project;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

public class MainService extends Service {

    private IntentFilter mainServiceFilter;
    private BroadcastReceiver mainServiceReceiver;
    private SynchronicityMusicPlayer synchronicityMusicPlayer;
    private PlaylistManager playlistManager;

    public MainService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        final Context applicationContext = getApplicationContext();
        playlistManager = new PlaylistManager(getApplicationContext());
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
                    synchronicityMusicPlayer =
                            new HostMusicPlayer(MainService.this, playlist);
                } else if (action.equals(applicationContext.getString(R.string
                        .participant_music_player_activity_started))) {
                    Playlist playlist = intent.getParcelableExtra(
                            applicationContext.getString(R.string.session_playlist));
                    synchronicityMusicPlayer =
                            new ParticipantMusicPlayer(MainService.this, playlist);
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
        mainServiceFilter = null;
        mainServiceReceiver = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mainServiceReceiver);
        if (synchronicityMusicPlayer != null) {
            synchronicityMusicPlayer.cleanUp();
            synchronicityMusicPlayer = null;
        }
    }

}

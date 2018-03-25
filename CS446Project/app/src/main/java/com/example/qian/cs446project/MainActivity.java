package com.example.qian.cs446project;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private MainService mainService;
    private boolean bound = false;

    private ServiceConnection mainActivityServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MainService.MainBinder mainBinder = (MainService.MainBinder) iBinder;
            mainService = mainBinder.getBinder();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonCreateSession = findViewById(R.id.buttonCreateSession);
        buttonCreateSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createSessionIntent = new Intent(MainActivity.this, CreateSessionActivity.class);
                startActivity(createSessionIntent);
            }
        });

        Button buttonManagePlaylist = findViewById(R.id.buttonManagePlaylist);
        buttonManagePlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent managePlaylistIntent = new Intent(MainActivity.this, ManagePlaylistActivity.class);
                startActivity(managePlaylistIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MainService.class);
        bindService(intent, mainActivityServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void onJoinSession(View v) {
        // Create a ChooseSessionActivity (screen 2 in mockup).
        Intent joinSessionIntent = new Intent(this, ChooseSessionActivity.class);
        startActivity(joinSessionIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bound) {
            unbindService(mainActivityServiceConnection);
            bound = false;
        }
    }

}

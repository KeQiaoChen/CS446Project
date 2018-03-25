package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class ManagePlaylistActivity extends AppCompatActivity {
    private IntentFilter intentFilterManagePlaylist;
    private BroadcastReceiver broadcastReceiverManagePlaylist;
    private ArrayList<Playlist> allAppPlaylists;
    ListView listViewManagePlaylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_playlist);

        listViewManagePlaylist = findViewById(R.id.listViewManagePlaylist);

        intentFilterManagePlaylist = new IntentFilter();
        intentFilterManagePlaylist.addAction(getString(R.string.all_app_playlists));

        broadcastReceiverManagePlaylist = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                allAppPlaylists = intent.getParcelableArrayListExtra(getString(R.string.extra_name_arraylist_playlist));
                ArrayAdapter<Playlist> allAppPlaylistAdapter = new ArrayAdapter<>(ManagePlaylistActivity.this, android.R.layout.simple_list_item_1, allAppPlaylists);
                listViewManagePlaylist = findViewById(R.id.listViewManagePlaylist);
                listViewManagePlaylist.setAdapter(allAppPlaylistAdapter);
            }
        };

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiverManagePlaylist, intentFilterManagePlaylist);

        CS446Utils.broadcastIntentWithoutExtras(getString(R.string.list_all_app_playlists), getApplicationContext());

        // Button should launch activity to create a new playlist.
        Button buttonNewPlaylist = findViewById(R.id.buttonNewPlaylist);
        buttonNewPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createPlaylistIntent = new Intent(ManagePlaylistActivity.this, CreatePlaylistActivity.class);
                startActivity(createPlaylistIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        CS446Utils.broadcastIntentWithoutExtras(getString(R.string.list_all_app_playlists), getApplicationContext());
    }
}

package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class CreateSessionActivity extends AppCompatActivity {
    private IntentFilter intentFilterCreateSession;
    private BroadcastReceiver broadcastReceiverCreateSession;
    private ArrayList<Playlist> allAppPlaylists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_session);

        // Retrieve all app playlists and display them on listview.
        allAppPlaylists = PlaylistManager.listAllAppPlaylists(getApplicationContext());
        ArrayAdapter<Playlist> allAppPlaylistAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allAppPlaylists);
        final ListView listViewSelectPlaylist = findViewById(R.id.listViewSelectPlaylist);
        listViewSelectPlaylist.setAdapter(allAppPlaylistAdapter);

        // Start session on button click.
        Button buttonStartSession = findViewById(R.id.buttonStartSession);
        buttonStartSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve session name.
                EditText sessionNameEditText = findViewById(R.id.editTextSessionName);
                String sessionName = sessionNameEditText.getText().toString();

                // Retrieve selected playlist object.
                Playlist selectedPlaylist = (Playlist) listViewSelectPlaylist.getItemAtPosition(listViewSelectPlaylist.getCheckedItemPosition());
                PlaylistManager.listAllPlaylistSongs(getApplicationContext(), selectedPlaylist);
                // Broadcast a targeted Intent to create HostMusicPlayerActivity (screen 6 in
                // mockup). This Intent contains the name of the session the user created and the
                // playlist that the user chose for the session.
                Intent createSessionIntent = new Intent(CreateSessionActivity.this,
                        HostMusicPlayerActivity.class);
                createSessionIntent.putExtra(getApplicationContext()
                        .getString(R.string.created_session_name), sessionName);
                createSessionIntent.putExtra(getApplicationContext()
                        .getString(R.string.session_playlist), selectedPlaylist);
                startActivity(createSessionIntent);
            }
        });
    }
}
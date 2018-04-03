package com.uwaterloo.qian.cs446project;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class CreatePlaylistActivity extends AppCompatActivity {
    private ArrayList<Song> allDeviceSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_playlist);

        // Retrieve all device songs and populate muli-selection listview.
        allDeviceSongs = PlaylistManager.listAllDeviceSongs(getApplicationContext());
        ArrayAdapter<Song> allDeviceSongAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, allDeviceSongs);
        final ListView listViewSelectSongs = findViewById(R.id.listViewSelectSongs);
        listViewSelectSongs.setAdapter(allDeviceSongAdapter);

        // Button click should create new playlist with specified name and specified songs.
        Button buttonCreatePlaylist = findViewById(R.id.buttonCreatePlaylist);
        buttonCreatePlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve playlist name and create playlist.
                EditText editTextPlaylistName = findViewById(R.id.editTextPlaylistName);
                Playlist newPlaylist = PlaylistManager.createPlaylist(getApplicationContext(), editTextPlaylistName.getText().toString());
                if (newPlaylist == null) {
                    Toast.makeText(getApplicationContext(), "Failed to create playlist. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add specified songs to playlist. Specify any errors.
                SparseBooleanArray checkItemPositions = listViewSelectSongs.getCheckedItemPositions();
                for (int i = 0; i < listViewSelectSongs.getCount(); i++) {
                    if (checkItemPositions.get(i) == true) {
                        if (PlaylistManager.addSongToPlaylist(getApplicationContext(), newPlaylist, (Song) listViewSelectSongs.getItemAtPosition(i)) == false) {
                            Toast.makeText(getApplicationContext(), "Failed to create playlist. Please try again.", Toast.LENGTH_SHORT).show();

                            if (PlaylistManager.deletePlaylist(getApplicationContext(), newPlaylist) == false) {
                                Toast.makeText(getApplicationContext(), "Failed to delete corrupted playlist.", Toast.LENGTH_SHORT).show();
                            }

                            return;
                        }
                    }
                }

                Toast.makeText(getApplicationContext(), "Successfully created playlist.", Toast.LENGTH_SHORT).show();
                CreatePlaylistActivity.this.finish();
            }
        });
    }
}

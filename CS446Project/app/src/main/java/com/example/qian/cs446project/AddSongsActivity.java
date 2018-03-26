package com.example.qian.cs446project;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AddSongsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_songs);

        final Button buttonAddSelectedSongsToPlaylist = findViewById(R.id.buttonAddSelectedSongsToPlaylist);
        final ListView listViewSelectSongsToAddToPlaylist = findViewById(R.id.listViewSelectSongsToAddToPlaylist);

        ArrayAdapter<Song> allDeviceSongAdapter = new ArrayAdapter<>(AddSongsActivity.this, android.R.layout.simple_list_item_multiple_choice, PlaylistManager.listAllDeviceSongs(getApplicationContext()));
        listViewSelectSongsToAddToPlaylist.setAdapter(allDeviceSongAdapter);
        listViewSelectSongsToAddToPlaylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (listViewSelectSongsToAddToPlaylist.getCheckedItemCount() == 0) {
                    buttonAddSelectedSongsToPlaylist.setEnabled(false);
                } else {
                    buttonAddSelectedSongsToPlaylist.setEnabled(true);
                }
            }
        });

        buttonAddSelectedSongsToPlaylist.setEnabled(false);
        buttonAddSelectedSongsToPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listViewSelectSongsToAddToPlaylist.getCheckedItemCount() == 0) {
                    Toast.makeText(getApplicationContext(), "Must select at least 1 song to add to playlist", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get all specified songs. Specify any errors.
                ArrayList<Song> songsToAddToPlaylist = new ArrayList<>();
                SparseBooleanArray checkItemPositions = listViewSelectSongsToAddToPlaylist.getCheckedItemPositions();
                for (int i = 0; i < listViewSelectSongsToAddToPlaylist.getCount(); i++) {
                    if (checkItemPositions.get(i) == true) {
                        if (!songsToAddToPlaylist.add((Song) listViewSelectSongsToAddToPlaylist.getItemAtPosition(i))) {
                            Toast.makeText(getApplicationContext(), "Failed to add songs to playlist. Please try again.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }

                Intent songsToAddToPlaylistIntent = new Intent();
                songsToAddToPlaylistIntent.putParcelableArrayListExtra(getString(R.string.extra_name_arraylist_song), songsToAddToPlaylist);
                setResult(RESULT_OK, songsToAddToPlaylistIntent);
                finish();
            }
        });
    }
}

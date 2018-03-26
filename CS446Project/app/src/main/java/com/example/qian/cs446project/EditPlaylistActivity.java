package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class EditPlaylistActivity extends AppCompatActivity {
    final private int addSongsRequestCode = 1;
    private Playlist editPlaylist;
    ListView listViewEditPlaylist;
    Button buttonRemoveSelectedSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_playlist);

        editPlaylist = getIntent().getParcelableExtra(getString(R.string.extra_name_playlist));

        TextView textViewEditPlaylist = findViewById(R.id.textViewEditPlaylist);
        Button buttonAddSongs = findViewById(R.id.buttonAddSongs);
        listViewEditPlaylist = findViewById(R.id.listViewEditPlaylist);
        buttonRemoveSelectedSongs = findViewById(R.id.buttonRemoveSelectedSongs);
        textViewEditPlaylist.setText(editPlaylist.getPlaylistName());
        buttonRemoveSelectedSongs.setEnabled(false);

        buttonAddSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addSongsIntent = new Intent(EditPlaylistActivity.this, AddSongsActivity.class);
                startActivityForResult(addSongsIntent, addSongsRequestCode);
            }
        });

        listViewEditPlaylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (listViewEditPlaylist.getCheckedItemCount() == 0) {
                    buttonRemoveSelectedSongs.setEnabled(false);
                } else {
                    buttonRemoveSelectedSongs.setEnabled(true);
                }
            }
        });

        buttonRemoveSelectedSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listViewEditPlaylist.getCheckedItemCount() == 0) {
                    Toast.makeText(getApplicationContext(), "Must select at least 1 song to delete", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent listEditPlaylistRemoveSongsIntent = new Intent(getString(R.string.list_all_playlist_songs));

                // Remove specified songs from playlist. Specify any errors.
                SparseBooleanArray checkItemPositions = listViewEditPlaylist.getCheckedItemPositions();
                for (int i = 0; i < listViewEditPlaylist.getCount(); i++) {
                    if (checkItemPositions.get(i) == true) {
                        if (PlaylistManager.removeSongFromPlaylist(getApplicationContext(), editPlaylist, (Song) listViewEditPlaylist.getItemAtPosition(i)) == false) {
                            Toast.makeText(getApplicationContext(), "Failed to remove songs from playlist. Please try again.", Toast.LENGTH_SHORT).show();
                            listEditPlaylistRemoveSongsIntent.putExtra(getString(R.string.extra_name_playlist), editPlaylist);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(listEditPlaylistRemoveSongsIntent);
                            return;
                        }
                    }
                }

                listEditPlaylistRemoveSongsIntent.putExtra(getString(R.string.extra_name_playlist), editPlaylist);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(listEditPlaylistRemoveSongsIntent);
            }
        });

        IntentFilter intentFilterEditPlaylist = new IntentFilter();
        intentFilterEditPlaylist.addAction(getString(R.string.all_playlist_songs));
        BroadcastReceiver broadcastReceiverEditPlaylist = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                buttonRemoveSelectedSongs.setEnabled(false);
                editPlaylist = intent.getParcelableExtra(getString(R.string.extra_name_linkedlist_song));
                ArrayAdapter<Song> allPlaylistSongAdapter = new ArrayAdapter<>(EditPlaylistActivity.this, android.R.layout.simple_list_item_multiple_choice, editPlaylist.songs);
                listViewEditPlaylist.setAdapter(allPlaylistSongAdapter);
                listViewEditPlaylist.clearChoices();
                listViewEditPlaylist.requestLayout();
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiverEditPlaylist, intentFilterEditPlaylist);
        Intent listEditPlaylistSongsIntent = new Intent(getString(R.string.list_all_playlist_songs));
        listEditPlaylistSongsIntent.putExtra(getString(R.string.extra_name_playlist), editPlaylist);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(listEditPlaylistSongsIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == addSongsRequestCode && resultCode == RESULT_OK) {
            ArrayList<Song> songsToBeAddedToPlaylist = data.getParcelableArrayListExtra(getString(R.string.extra_name_arraylist_song));
            Intent listEditPlaylistAddSongsIntent = new Intent(getString(R.string.list_all_playlist_songs));

            for (Song songToBeAddedToPlaylist : songsToBeAddedToPlaylist) {
                if (!PlaylistManager.addSongToPlaylist(getApplicationContext(), editPlaylist, songToBeAddedToPlaylist)) {
                    Toast.makeText(getApplicationContext(), "Failed to add songs to playlist. Please try again.", Toast.LENGTH_SHORT).show();
                    break;
                }
            }

            listEditPlaylistAddSongsIntent.putExtra(getString(R.string.extra_name_playlist), editPlaylist);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(listEditPlaylistAddSongsIntent);
        }
    }
}

package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ManagePlaylistActivity extends AppCompatActivity {
    private IntentFilter intentFilterManagePlaylist;
    private BroadcastReceiver broadcastReceiverManagePlaylist;
    private ArrayList<Playlist> allAppPlaylists;
    ListView listViewManagePlaylist;
    Button buttonEditPlaylist;
    Button buttonDeletePlaylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_playlist);

        buttonEditPlaylist = findViewById(R.id.buttonEditPlaylist);
        buttonDeletePlaylist = findViewById(R.id.buttonDeletePlaylist);
        buttonEditPlaylist.setEnabled(false);
        buttonDeletePlaylist.setEnabled(false);
        listViewManagePlaylist = findViewById(R.id.listViewManagePlaylist);
        listViewManagePlaylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (listViewManagePlaylist.getCheckedItemCount() == 1) {
                    buttonEditPlaylist.setEnabled(true);
                } else {
                    buttonEditPlaylist.setEnabled(false);
                }

                if (listViewManagePlaylist.getCheckedItemCount() == 0) {
                    buttonDeletePlaylist.setEnabled(false);
                } else {
                    buttonDeletePlaylist.setEnabled(true);
                }
            }
        });

        intentFilterManagePlaylist = new IntentFilter();
        intentFilterManagePlaylist.addAction(getString(R.string.all_app_playlists));

        broadcastReceiverManagePlaylist = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                buttonEditPlaylist.setEnabled(false);
                buttonDeletePlaylist.setEnabled(false);
                allAppPlaylists = intent.getParcelableArrayListExtra(getString(R.string.extra_name_arraylist_playlist));
                ArrayAdapter<Playlist> allAppPlaylistAdapter = new ArrayAdapter<>(ManagePlaylistActivity.this, android.R.layout.simple_list_item_multiple_choice, allAppPlaylists);
                listViewManagePlaylist = findViewById(R.id.listViewManagePlaylist);
                listViewManagePlaylist.setAdapter(allAppPlaylistAdapter);
                listViewManagePlaylist.clearChoices();
                listViewManagePlaylist.requestLayout();
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

        buttonEditPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listViewManagePlaylist.getCheckedItemCount() != 1) {
                    Toast.makeText(getApplicationContext(), "Must select exactly 1 playlist to edit", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent editPlaylistIntent = new Intent(ManagePlaylistActivity.this, EditPlaylistActivity.class);
                Playlist editSelectedPlaylist = null;

                // Get checked item that requires editing. Specify any errors.
                SparseBooleanArray checkItemPositions = listViewManagePlaylist.getCheckedItemPositions();
                for (int i = 0; i < listViewManagePlaylist.getCount(); i++) {
                    if (checkItemPositions.get(i) == true) {
                        editSelectedPlaylist = (Playlist) listViewManagePlaylist.getItemAtPosition(i);
                        break;
                    }
                }

                PlaylistManager.listAllPlaylistSongs(getApplicationContext(), editSelectedPlaylist);

                editPlaylistIntent.putExtra(getApplication().getString(R.string.extra_name_playlist), editSelectedPlaylist);
                startActivity(editPlaylistIntent);
            }
        });

        buttonDeletePlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listViewManagePlaylist.getCheckedItemCount() == 0) {
                    Toast.makeText(getApplicationContext(), "Must select at least 1 playlist to delete", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Delete specified playlists. Specify any errors.
                SparseBooleanArray checkItemPositions = listViewManagePlaylist.getCheckedItemPositions();
                for (int i = 0; i < listViewManagePlaylist.getCount(); i++) {
                    if (checkItemPositions.get(i) == true) {
                        if (PlaylistManager.deletePlaylist(getApplicationContext(), (Playlist) listViewManagePlaylist.getItemAtPosition(i)) == false) {
                            Toast.makeText(getApplicationContext(), "Failed to delete playlist. Please try again.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }

                CS446Utils.broadcastIntentWithoutExtras(getString(R.string.list_all_app_playlists), getApplicationContext());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        CS446Utils.broadcastIntentWithoutExtras(getString(R.string.list_all_app_playlists), getApplicationContext());
    }
}

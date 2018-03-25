package com.example.qian.cs446project;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Class com.example.qian.cs446project.PlaylistManager manages playlist
 */

public class PlaylistManager {
    private Context mycon;
    private IntentFilter intentFilter;
    private BroadcastReceiver broadcastReceiver;


    public PlaylistManager(Context mycon) {
        this.mycon = mycon;

        intentFilter = new IntentFilter();
        intentFilter.addAction(mycon.getString(R.string.list_all_device_songs));
        intentFilter.addAction(mycon.getString(R.string.list_all_app_playlists));
        intentFilter.addAction(mycon.getString(R.string.list_all_playlist_songs));
        intentFilter.addAction(mycon.getString(R.string.create_new_playlist));
        intentFilter.addAction(mycon.getString(R.string.add_song_to_playlist));

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String intentAction = intent.getAction();

                if (intentAction.equals(PlaylistManager.this.mycon.getString(R.string.list_all_device_songs))) {
                    listAllDeviceSongs(PlaylistManager.this.mycon);
                } else if (intentAction.equals(PlaylistManager.this.mycon.getString(R.string.list_all_app_playlists))) {
                    listAllAppPlaylists(PlaylistManager.this.mycon);
                } else if (intentAction.equals(PlaylistManager.this.mycon.getString(R.string.list_all_playlist_songs))) {
                    Playlist pl = intent.getParcelableExtra(PlaylistManager.this.mycon.getString(R.string.extra_name_playlist));
                    listAllPlaylistSongs(PlaylistManager.this.mycon, pl);
                } else if (intentAction.equals(PlaylistManager.this.mycon.getString(R.string.create_new_playlist))) {
                    String playlistName = intent.getParcelableExtra(PlaylistManager.this.mycon.getString(R.string.extra_name_playlist_name));
                    createPlaylist(PlaylistManager.this.mycon, playlistName);
                } else if (intentAction.equals(PlaylistManager.this.mycon.getString(R.string.add_song_to_playlist))) {
                    Playlist pl = intent.getParcelableExtra(PlaylistManager.this.mycon.getString(R.string.extra_name_playlist));
                    Song s = intent.getParcelableExtra(PlaylistManager.this.mycon.getString(R.string.extra_name_song));
                    addSongToPlaylist(PlaylistManager.this.mycon, pl, s);
                }
            }
        };

        LocalBroadcastManager.getInstance(mycon).registerReceiver(broadcastReceiver, intentFilter);
    }

    // listAllDeviceSongs searches the device for all audio files and generates an ArrayList<com.example.qian.cs446project.Song> object for all
    // the songs to return.
    public static ArrayList<Song> listAllDeviceSongs(Context ctx) {
        Log.d("Playlist Manager", "START LIST ALL SONGS");

        // Retrieve all device songs and place them into a Cursor object.
        Cursor allSongsCursor = ctx.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        ArrayList<Song> allDeviceSongs = new ArrayList<>();

        // Iterate over non-null Cursor object and create ArrayList<com.example.qian.cs446project.Song> object.
        if (allSongsCursor != null) {
            while (allSongsCursor.moveToNext()) {
                String title = allSongsCursor.getString(allSongsCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = allSongsCursor.getString(allSongsCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = allSongsCursor.getString(allSongsCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                int duration = allSongsCursor.getInt(allSongsCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String filePath = allSongsCursor.getString(allSongsCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                int id = allSongsCursor.getInt(allSongsCursor.getColumnIndex(MediaStore.Audio.Media._ID));

                Log.d("Playlist Manager", "Title: " + title);
                Log.d("Playlist Manager", "Album: " + album);
                Log.d("Playlist Manager", "Artist: " + artist);
                Log.d("Playlist Manager", "Duration: " + id);
                Log.d("Playlist Manager", "File Path: " + filePath);
                Log.d("Playlist Manager", "ID: " + id);

                if (!allDeviceSongs.add(new Song(title, album, artist, duration, filePath, id))) {
                    Log.e("Playlist Manager", "Could not add song " + title + " with ID: " + id);
                    allSongsCursor.close();
                    return null;
                }
            }
            allSongsCursor.close();
        } else {
            Log.d("Playlist Manager", "No songs found on device");
        }

        // Return appropriate value.
        Log.d("Playlist Manager", "END LIST ALL SONGS");
        Intent allDeviceSongsIntent = new Intent(ctx.getString(R.string.all_device_songs));
        allDeviceSongsIntent.putExtra(ctx.getString(R.string.extra_name_arraylist_song), allDeviceSongs);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(allDeviceSongsIntent);
        return allDeviceSongs;
    }

    // listAllAppPlaylists searches the device for all playlists created by this android app, generates an
    // ArrayList<com.example.qian.cs446project.Playlist> object containing the playlist data, and returns said object. Note that the returned
    // com.example.qian.cs446project.Playlist objects do not have their songs attribute populated. Please call
    // com.example.qian.cs446project.PlaylistManager.listAllPlaylistSongs to populate the songs attribute.
    public static ArrayList<Playlist> listAllAppPlaylists(Context ctx) {
        Log.d("Playlist Manager", "START LIST ALL PLAYLISTS");

        // Retrieve all app playlists and place them into a Cursor object.
        Cursor allPlaylistsCursor = ctx.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null, null, null, null);
        ArrayList<Playlist> allAppPlaylists = new ArrayList<>();

        // Iterate over non-null Cursor object and create ArrayList<com.example.qian.cs446project.Playlist> object.
        if (allPlaylistsCursor != null) {
            while (allPlaylistsCursor.moveToNext()) {
                String playlistName = allPlaylistsCursor.getString(allPlaylistsCursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
                int playlistID = allPlaylistsCursor.getInt(allPlaylistsCursor.getColumnIndex(MediaStore.Audio.Playlists._ID));

                Log.d("Playlist Manager", "Name: " + playlistName);
                Log.d("Playlist Manager", "ID: " + playlistID);

                if (!allAppPlaylists.add(new Playlist(playlistName, playlistID))) {
                    Log.e("Playlist Manager", "Could not add playlist " + playlistName + " with ID: " + playlistID);
                    allPlaylistsCursor.close();
                    return null;
                }
            }

            allPlaylistsCursor.close();
        }

        // Return appropriate value.
        Log.d("Playlist Manager", "END LIST ALL PLAYLISTS");
        Intent allAppPlaylistsIntent = new Intent(ctx.getString(R.string.all_app_playlists));
        allAppPlaylistsIntent.putExtra(ctx.getString(R.string.extra_name_arraylist_playlist), allAppPlaylists);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(allAppPlaylistsIntent);
        return allAppPlaylists;
    }

    // listAllPlaylistSongs queries for all the songs under the given playlist, updates the given com.example.qian.cs446project.Playlist
    // object's songs variable with the queried song data, and returns the com.example.qian.cs446project.Playlist object's updated songs
    // variable.
    public static LinkedList<Song> listAllPlaylistSongs(Context ctx, Playlist pl) {
        Log.d("Playlist Manager", "START LIST ALL PLAYLIST SONGS");

        // Get playlist URI and put all playlist songs into Cursor object.
        Uri playlistUri = MediaStore.Audio.Playlists.Members.getContentUri("external", pl.getPlaylistID());
        Cursor playlistSongsCursor = ctx.getContentResolver().query(playlistUri, null, null, null, null);

        // Reset playlist songs.
        pl.songs = new LinkedList<>();

        // Iterate over non-null Cursor object and create LinkedList<com.example.qian.cs446project.Song> object.
        if (playlistSongsCursor != null) {
            while (playlistSongsCursor.moveToNext()) {
                String title = playlistSongsCursor.getString(playlistSongsCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE));
                String album = playlistSongsCursor.getString(playlistSongsCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM));
                String artist = playlistSongsCursor.getString(playlistSongsCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST));
                int duration = playlistSongsCursor.getInt(playlistSongsCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.DURATION));
                String filePath = playlistSongsCursor.getString(playlistSongsCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.DATA));
                int id = playlistSongsCursor.getInt(playlistSongsCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID));

                Log.d("Playlist Manager", "Title: " + title);
                Log.d("Playlist Manager", "Album: " + album);
                Log.d("Playlist Manager", "Artist: " + artist);
                Log.d("Playlist Manager", "Duration: " + duration);
                Log.d("Playlist Manager", "File Path: " + filePath);
                Log.d("Playlist Manager", "ID: " + id);

                if (!pl.songs.add(new Song(title, album, artist, duration, filePath, id))) {
                    Log.e("Playlist Manager", "Could not add song " + title + " with ID: " + id);
                    playlistSongsCursor.close();
                    pl.songs = null;
                    return null;
                }
            }

            playlistSongsCursor.close();
        }

        // Return appropriate value.
        Log.d("Playlist Manager", "END LIST ALL PLAYLIST SONGS");
        Intent allPlaylistSongsIntent = new Intent(ctx.getString(R.string.all_playlist_songs));
        allPlaylistSongsIntent.putExtra(ctx.getString(R.string.extra_name_linkedlist_song), pl.songs);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(allPlaylistSongsIntent);
        return pl.songs;
    }

    // createPlaylist creates a new playlist on the device with the given name and returns a com.example.qian.cs446project.Playlist object
    // representing the newly created playlist.
    public static Playlist createPlaylist(Context ctx, String playlistName) {
        Log.d("Playlist Manager", "START CREATE PLAYLIST");

        Intent newPlaylistCreatedIntent = new Intent(ctx.getString(R.string.new_playlist_created));

        // Initialize playlist values.
        ContentValues contentValuesPlaylist = new ContentValues();
        contentValuesPlaylist.put(MediaStore.Audio.Playlists.NAME, playlistName);
        contentValuesPlaylist.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());

        // Create playlist and retrieve its ID.
        Uri playlistURI = ctx.getContentResolver().insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, contentValuesPlaylist);
        if (playlistURI == null) {
            Log.e("Playlist Manager", "An error occurred creating playlist " + playlistName);
            newPlaylistCreatedIntent.putExtra(ctx.getString(R.string.extra_name_playlist), (Playlist) null);
            LocalBroadcastManager.getInstance(ctx).sendBroadcast(newPlaylistCreatedIntent);
            return null;
        }
        Cursor playlistIDCursor = ctx.getContentResolver().query(playlistURI, new String[]{MediaStore.Audio.Playlists._ID}, null, null, null);
        if (playlistIDCursor == null) {
            Log.e("Playlist Manager", "An error occurred creating playlist " + playlistName);
            newPlaylistCreatedIntent.putExtra(ctx.getString(R.string.extra_name_playlist), (Playlist) null);
            LocalBroadcastManager.getInstance(ctx).sendBroadcast(newPlaylistCreatedIntent);
            return null;
        }
        if (playlistIDCursor.getCount() != 1) {
            Log.e("Playlist Manager", "An error occurred creating playlist " + playlistName);
            newPlaylistCreatedIntent.putExtra(ctx.getString(R.string.extra_name_playlist), (Playlist) null);
            LocalBroadcastManager.getInstance(ctx).sendBroadcast(newPlaylistCreatedIntent);
            return null;
        }
        playlistIDCursor.moveToFirst();
        int id = playlistIDCursor.getInt(playlistIDCursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
        playlistIDCursor.close();

        // Return appropriate value.
        Log.d("Playlist Manager", "END CREATE PLAYLIST");
        Playlist newlyCreatedPlaylist = new Playlist(playlistName, id);
        newPlaylistCreatedIntent.putExtra(ctx.getString(R.string.extra_name_playlist), newlyCreatedPlaylist);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(newPlaylistCreatedIntent);
        return newlyCreatedPlaylist;
    }

    // deletePlaylist deletes the given playlist from the device and nullifies the given com.example.qian.cs446project.Playlist object. The
    // returned Boolean value indicates a success (true) or failure (false).
    public static Boolean deletePlaylist(Context ctx, Playlist pl) {
        Log.d("Playlist Manager", "START DELETE PLAYLIST");

        // Retrieve all app playlists with the given playlist ID and places them into a Cursor object.
        Cursor allAppPlaylistsCursor = ctx.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Playlists._ID + " = '" + pl.getPlaylistID() + "'", null, null);
        int numOfDeletedPlaylists = 0;

        // Delete all playlists with the given ID.
        if (allAppPlaylistsCursor != null) {
            while (allAppPlaylistsCursor.moveToNext()) {
                numOfDeletedPlaylists += ctx.getContentResolver().delete(ContentUris.withAppendedId(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, allAppPlaylistsCursor.getLong(allAppPlaylistsCursor.getColumnIndex(MediaStore.Audio.Playlists._ID))), null, null);
            }

            allAppPlaylistsCursor.close();
        }

        // Deletion failure if number of deleted playlists is not 1.
        if (numOfDeletedPlaylists != 1) {
            Log.e("Playlist Manager", "An error occurred deleting playlist " + pl.getPlaylistName() + " and " + numOfDeletedPlaylists + " were deleted");
            return false;
        }

        // Return appropriate value.
        Log.d("Playlist Manager", "END DELETE PLAYLIST");
        pl.close();
        return true;
    }

    // addSongToPlaylist updates the device's playlist with the provided song and updates the given com.example.qian.cs446project.Playlist
    // object's songs attribute with the provided song as well. The returned Boolean value indicates a success
    // (true) or failure (false).
    public static Boolean addSongToPlaylist(Context ctx, Playlist pl, Song s) {
        Log.d("Playlist Manager", "START ADD SONG");

        Intent songAddedToPlaylistIntent = new Intent(ctx.getString(R.string.song_added_to_playlist));

        // Retrieve number of songs in playlist. Required to ensure new songs are added to end of playlist.
        final int previousPlaylistSongCount = getPlaylistSongCount(ctx, pl.getPlaylistID());

        // Add new song to end of playlist.
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", pl.getPlaylistID());
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, previousPlaylistSongCount + 1);
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, s.getLocalDeviceFileID());
        ctx.getContentResolver().insert(uri, values);

        // Retrieve number of songs in playlist. Required to check if new song was actually added.
        final int currentPlaylistSongCount = getPlaylistSongCount(ctx, pl.getPlaylistID());

        // Error if new song count is not 1 greater than old song count.
        if (previousPlaylistSongCount != currentPlaylistSongCount - 1) {
            Log.e("Playlist Manager", "com.example.qian.cs446project.Song " + s.getTitle() + " with ID " + s.getLocalDeviceFileID() + "could not be added to playlist " + pl.getPlaylistName() + " with ID " + pl.getPlaylistID());
            songAddedToPlaylistIntent.putExtra(ctx.getString(R.string.extra_name_success), false);
            LocalBroadcastManager.getInstance(ctx).sendBroadcast(songAddedToPlaylistIntent);
            return false;
        }

        // Return appropriate value.
        Log.d("Playlist Manager", "END ADD SONG");
        if (!pl.songs.add(s)) {
            Log.e("Playlist Manager", "Failed to add song " + s.getTitle() + " with ID " + s.getLocalDeviceFileID() + " to playlist object " + pl.getPlaylistName() + " with ID " + pl.getPlaylistID());
            songAddedToPlaylistIntent.putExtra(ctx.getString(R.string.extra_name_success), false);
            LocalBroadcastManager.getInstance(ctx).sendBroadcast(songAddedToPlaylistIntent);
            return false;
        }
        songAddedToPlaylistIntent.putExtra(ctx.getString(R.string.extra_name_success), true);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(songAddedToPlaylistIntent);
        return true;
    }

    // removeSongFromPlaylist removes the provided song from the device's playlist and removes the provided song
    // from the provided com.example.qian.cs446project.Playlist object as well. The returned Boolean value indicates a success (true) or failure
    // (false).
    public static Boolean removeSongFromPlaylist(Context ctx, Playlist pl, Song s) {
        Log.d("Playlist Manager", "START REMOVE SONG");

        // Retrieve playlist URI and delete song from the playlist.
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", pl.getPlaylistID());
        int deletionCount = ctx.getContentResolver().delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + " = " + s.getLocalDeviceFileID(), null);

        // Ensure correct number of songs was deleted.
        if (deletionCount != 1) {
            Log.e("Playlist Manager", "Deletion failure of song " + s.getTitle() + "with ID " + s.getLocalDeviceFileID() + " from playlist " + pl.getPlaylistName() + " with ID " + pl.getPlaylistID());
            return false;
        }

        // Return appropriate value.
        Log.d("Playlist Manager", "END REMOVE SONG");
        return true;
    }

    // getPlaylistSongCount returns the number of songs under the device playlist that has an ID of playlistID.
    private static int getPlaylistSongCount(Context ctx, int playlistID) {
        // Query for playlist song count and place result in Cursor object.
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistID);
        Cursor playlistSongCountCursor = ctx.getContentResolver().query(uri, new String[]{"count(*)"}, null, null, null);

        // Retrieve song count from Cursor object.
        if (playlistSongCountCursor == null) {
            return -1;
        }
        playlistSongCountCursor.moveToFirst();
        final int songCount = playlistSongCountCursor.getInt(0);
        playlistSongCountCursor.close();

        // Return appropriate value.
        return songCount;
    }
}

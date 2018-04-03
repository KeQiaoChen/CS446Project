package com.uwaterloo.qian.cs446project;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedList;

/**
 * Class com.uwaterloo.qian.cs446project.Playlist encapsulates multiple com.uwaterloo.qian.cs446project.Song objects.
 */

public class Playlist implements Parcelable {
    public LinkedList<Song> songs = new LinkedList<Song>();

    private String playlistName;
    private int playlistID;

    // Constructor simply initializes its members.
    public Playlist(String playlistName, int playlistID) {
        this.playlistName = playlistName;
        this.playlistID = playlistID;
    }

    @Override
    public String toString() { return playlistName; }

    public String getPlaylistName() {
        return playlistName;
    }

    public int getPlaylistID() {
        return playlistID;
    }

    public void close() {
        songs = null;
        playlistName = null;
        playlistID = -1;
    }

    /******* All the functions below implement the Parcelable interface. *******/

    // Write object values to parcel for storage.
    public void writeToParcel(Parcel dest, int flags){
        dest.writeString(playlistName);
        dest.writeInt(playlistID);
        dest.writeTypedList(songs);
    }

    // Constructor used for parcel.
    public Playlist(Parcel parcel){
        playlistName = parcel.readString();
        playlistID = parcel.readInt();
        parcel.readTypedList(songs, Song.CREATOR);
    }

    // Creator - used when un-parceling our parcel (creating the object).
    public static final Parcelable.Creator<Playlist> CREATOR = new Parcelable.Creator<Playlist>(){
        @Override
        public Playlist createFromParcel(Parcel parcel) {
            return new Playlist(parcel);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    // Return hashcode of object.
    public int describeContents() {
        return hashCode();
    }
}

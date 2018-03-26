package com.example.qian.cs446project;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class Song represents a local audio file.
 */

public class Song implements Parcelable {
    private String title;
    private String album;
    private String artist;
    private String filePath;
    private int duration;
    private int localDeviceFileID;

    // Constructor simply assigns given values to class attributes.
    public Song(String title, String album, String artist, int duration, String filePath, int localDeviceFileID) {
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.duration = duration;
        this.filePath = filePath;
        this.localDeviceFileID = localDeviceFileID;
    }

    @Override
    public String toString() { return title; }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public int getDuration() {
        return duration;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getLocalDeviceFileID() {
        return localDeviceFileID;
    }

    /******* All the functions below implement the Parcelable interface. *******/

    // Write object values to parcel for storage.
    public void writeToParcel(Parcel dest, int flags){
        dest.writeString(title);
        dest.writeString(album);
        dest.writeString(artist);
        dest.writeInt(duration);
        dest.writeString(filePath);
        dest.writeInt(localDeviceFileID);
    }

    // Constructor used for parcel.
    public Song(Parcel parcel){
        title = parcel.readString();
        album = parcel.readString();
        artist = parcel.readString();
        duration = parcel.readInt();
        filePath = parcel.readString();
        localDeviceFileID = parcel.readInt();
    }

    // Creator - used when un-parceling our parcel (creating the object).
    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>(){
        @Override
        public Song createFromParcel(Parcel parcel) {
            return new Song(parcel);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    // Return hashcode of object.
    public int describeContents() {
        return hashCode();
    }
}

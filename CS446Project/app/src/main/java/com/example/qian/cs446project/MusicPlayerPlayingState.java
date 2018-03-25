package com.example.qian.cs446project;

/**
 * Created by Qian on 2018-03-19.
 */

public class MusicPlayerPlayingState implements MusicPlayerState {

    public boolean isPlaying() {
        return true;
    }

    public boolean isPaused() {
        return false;
    }

    public boolean isStopped() {
        return false;
    }

}

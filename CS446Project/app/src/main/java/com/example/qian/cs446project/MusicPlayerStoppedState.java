package com.example.qian.cs446project;

/**
 * Created by Qian on 2018-03-19.
 */

public class MusicPlayerStoppedState implements MusicPlayerState {

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public boolean isStopped() {
        return true;
    }

}

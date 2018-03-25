package com.example.qian.cs446project;

/**
 * Created by Qian on 2018-03-19.
 */

public class MusicPlayerPausedState implements MusicPlayerState {

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public boolean isPaused() {
        return true;
    }

    @Override
    public boolean isStopped() {
        return false;
    }

}

package com.example.qian.cs446project;

/**
 * Created by Qian on 2018-03-19.
 */

public interface MusicPlayerState {

    // Returns true if the session playlist is playing, false otherwise.
    boolean isPlaying();

    // Returns true if the session playlist is paused, false otherwise.
    boolean isPaused();

    // Returns true if the session playlist is stopped, false otherwise.
    boolean isStopped();

}

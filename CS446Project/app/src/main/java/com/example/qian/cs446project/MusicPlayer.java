package com.example.qian.cs446project;

import android.media.MediaPlayer;
import android.view.View;

/**
 * Created by Qian on 2018-03-14.
 */

public interface MusicPlayer {

    // Play the session playlist.
    void play();

    // Pause the session playlist.
    void pause();

    // Stop the session playlist.
    void stop();

    // Mute the session playlist.
    void mute();

    // Unmute the session playlist.
    void unmute();

}

package com.example.qian.cs446project;

import android.view.View;

/**
 * Created by Qian on 2018-03-18.
 */

public interface MusicPlayerActivity {

    // Updates the GUI to indicate the session playlist is playing and notifies the appropriate
    // service.
    void playUpdateGUINotifyService(View view);

    // Updates the GUI to indicate the session playlist is paused and notifies the appropriate
    // service.
    void pauseUpdateGUINotifyService(View view);

    // Updates the GUI to indicate the session playlist is stopped and notifies the appropriate
    // service.
    void stopUpdateGUINotifyService(View view);

    // Updates the GUI to indicate the session playlist is muted and notifies the appropriate
    // service.
    void muteUpdateGUINotifyService(View view);

    // Updates the GUI to indicate the session playlist is not muted and notifies the appropriate
    // service.
    void unmuteUpdateGUINotifyService(View view);

}

package com.uwaterloo.qian.cs446project;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Qian on 2018-02-21.
 */

public class CS446Utils {

    // Ke Qiao Chen: I based this method on the createTimeLabel(int time) method shown in
    // https://www.youtube.com/watch?v=zCYQBIcePaw at 13:47
    public static String formatTime(int timeInMilliseconds) {
        int minutes = timeInMilliseconds / 1000 / 60;
        int seconds = timeInMilliseconds / 1000 % 60;
        String formattedTime = "" + minutes + ":";
        if (seconds < 10) {
            formattedTime += "0";
        }
        formattedTime += seconds;
        return formattedTime;
    }

    public static void broadcastIntentWithoutExtras(String intentName, Context broadcastSender) {
        Intent intentToBroadcast = new Intent(intentName);
        LocalBroadcastManager.getInstance(broadcastSender).sendBroadcast(intentToBroadcast);
    }

}

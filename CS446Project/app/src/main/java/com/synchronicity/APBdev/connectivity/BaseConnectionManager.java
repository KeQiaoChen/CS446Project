package com.synchronicity.APBdev.connectivity;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

/**
 * Created by Andrew on 2018-02-15.
 */

public abstract class BaseConnectionManager implements ConnectionManager {

    protected BroadcastReceiver broadcastReceiver = null;
    protected IntentFilter intentFilter = null;

    // Methods for common connectivity of ConnectionManager.
    public abstract void initiateSession(String SessionName);
    public abstract void joinSession(String sessionName);
    public abstract void cleanUp();

    // Common data xfer methods for ConnectionManager.
    /*
    public abstract void sendData();
    public abstract void receiveData();
    */

    public abstract void sendSig(byte signal);

    // BroadcastReceiver and IntentFilter methods.
    public abstract BroadcastReceiver getBroadcastReceiver();
    public abstract IntentFilter getIntentFilter();

}

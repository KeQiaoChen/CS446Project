package com.synchronicity.APBdev.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import com.example.qian.cs446project.Playlist;
import com.example.qian.cs446project.R;
import com.synchronicity.APBdev.util.ParcelableUtil;

import java.util.HashMap;
import java.util.Map;


/*
NOTE TO THE PROFESSOR AND TA'S:

In the process of cleaning up my module to reflect a more structured component, I may not have had
time to fully copy over old functionality. That is, the intended structure for this component in its
final form exists, but the functionality that was put on display in for the demo would have to be
viewed by checking out an older commit.

Thus even though design and architecture are on display, the actual functionality of the system
behaviour still needs to be updated.
 */

/*
ON THE ARCHITECTURE AND DESIGN OF THIS COMPONENT:

This component acts as a facade to the other two sub-modules to which it delegates its work.
The sub-module, NsdManager takes care of network discover and establishing initial wifi connections.
The sub-module, SocketManager takes care of maintaining socket connections and sending / receiving
of data.

This component, as well as each of the sub-components used BroadcastReceiver and LocalBroadcastManager
objects to enforce the Publish / Subscribe architecture, with the BroadcastReceiver receiving signals,
and the LocalBroadcastManager sending signals of other application components.

Please note that even though these structures are in place, that they have not yet been initialized
with the IntentFilter objects which are required to pick up the Intent messages sent by the other
components in the application. Nor have the BroadcastReceiver Objects been updated to reflect the
functionality that they will call. Please see the NOTE TO THE PROFESSOR AND TA'S for the explanation
as to why this is. Please refer to an older commit, or other components for an example of how we have
used BroadcastReceivers.

 */

public class WifiConnectionManager implements ConnectionManager {


    /*
    FIELDS
     */


    private Context context;
    private NsdManager nsdManager;
    private SocketManager socketManager;
    private Map<String,String> recordMap;
    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    private final String classTag = "WifiConnectionManager> ";


    /*
    CONSTRUCTORS
     */


    public WifiConnectionManager(Context context) {

        this.context = context;
        this.nsdManager = new WifiNsdManager(context);
        this.socketManager = new WifiSocketManager(context);
        this.recordMap = new HashMap<>();

        // Fill recordMap info here.
        recordMap.put("foo","bar");

        // BroadcastReceive creation and registration + LocalBroadcastManager.
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
        this.intentFilter = new IntentFilter();
        context.registerReceiver(this.broadcastReceiver, this.intentFilter);

    }


    /*
    PUBLIC METHODS
     */


    public void createSession(String sessionName) {
        this.nsdManager.advertiseService(recordMap);
    }

    public void joinSession(String sessionName) {
        this.nsdManager.connectToService(recordMap);
    }

    public void findSessions() {
        this.nsdManager.findService(recordMap);
    }

    public void sendData(Parcelable parcelable) {
        this.socketManager.sendData(parcelable);
    }

    public void sendDataByPath(String pathToData) {
        this.socketManager.sendDataByPath(pathToData);
    }

    public void sendSignal(byte signal) {
        this.socketManager.sendSignal(signal);
    }

    public void cleanUp() {
        this.nsdManager.cleanUp();
        this.socketManager.cleanUp();
        this.context.unregisterReceiver(this.broadcastReceiver);
    }

}
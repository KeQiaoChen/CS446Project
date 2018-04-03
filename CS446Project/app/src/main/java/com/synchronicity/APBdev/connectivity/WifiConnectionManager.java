package com.synchronicity.APBdev.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.uwaterloo.qian.cs446project.R;
import com.synchronicity.APBdev.util.StampUtil;

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
as to why this is. Please refer to an older commit, or other components for an uwaterloo of how we have
used BroadcastReceivers.

 */

public class WifiConnectionManager implements ConnectionManager {


    /*
    FIELDS
     */


    private Context context;
    private NsdManager nsdManager;
    private SocketManager socketManager;
    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    private final String classTag = "WifiConnectionManager> ";


    /*
    CONSTRUCTORS
     */


    public WifiConnectionManager(Context context) {

        Log.d(classTag,"Constructor call");

        this.context = context;
        this.socketManager = new WifiSocketManager(context);
        this.nsdManager = new WifiNsdManager(context, this.socketManager);
        // BroadcastReceive creation and registration + LocalBroadcastManager.
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                if (action.equals(context.getString(R.string.create_session_message))) {

                    String sessionName = intent.getStringExtra(context.getString(R.string.session_name_key));
                    WifiConnectionManager.this.createSession(sessionName);

                }
                else if (action.equals(context.getString(R.string.join_session_message))) {

                    String sessionName = intent.getStringExtra(context.getString(R.string.session_name_key));
                    WifiConnectionManager.this.joinSession(sessionName);

                }
                else if (action.equals(context.getString(R.string.find_session_message))) {

                    WifiConnectionManager.this.findSessions();

                }
                else if (action.equals((context.getString(R.string.send_play)))) {

                    Intent sendIntent = new Intent(context.getString(R.string.receive_play));
                    WifiConnectionManager.this.socketManager.sendSignal(WifiSocketManager.Constants.PLAY_SIGNAL);
                    WifiConnectionManager.this.localBroadcastManager.sendBroadcast(sendIntent);

                }
                else if (action.equals((context.getString(R.string.send_pause)))) {

                    Intent sendIntent = new Intent(context.getString(R.string.receive_pause));
                    WifiConnectionManager.this.socketManager.sendSignal(WifiSocketManager.Constants.PAUSE_SIGNAL);
                    WifiConnectionManager.this.localBroadcastManager.sendBroadcast(sendIntent);

                }
                else if (action.equals((context.getString(R.string.send_stop)))) {

                    Intent sendIntent = new Intent(context.getString(R.string.receive_stop));
                    WifiConnectionManager.this.socketManager.sendSignal(WifiSocketManager.Constants.STOP_SIGNAL);
                    WifiConnectionManager.this.localBroadcastManager.sendBroadcast(sendIntent);

                }

            }
        };
        this.intentFilter = new IntentFilter();
        // Actions for intra component communication.
        intentFilter.addAction(context.getString(R.string.create_session_message));
        intentFilter.addAction(context.getString(R.string.join_session_message));
        intentFilter.addAction(context.getString(R.string.find_session_message));
        // Play pause stop intent filers
        intentFilter.addAction(context.getString(R.string.send_play));
        intentFilter.addAction(context.getString(R.string.send_pause));
        intentFilter.addAction(context.getString(R.string.send_stop));
        // LocalBroadcastManager registration for BroadcastReceiver.
        localBroadcastManager.registerReceiver(this.broadcastReceiver, this.intentFilter);
        // Global registration for BroadcastReceiver.
        context.registerReceiver(this.broadcastReceiver, this.intentFilter);

    }


    /*
    PUBLIC METHODS
     */


    public void createSession(String sessionName) {

        this.nsdManager.createServiceGroup();
        this.socketManager.initServerSocket();
        int hostPort = this.socketManager.getServerPort();

        Map<String,String> tempRecord = new HashMap<>();
        tempRecord.put(WifiNsdManager.NSD_INFO_SERVICE_NAME_ID, WifiNsdManager.NSD_INFO_SERVICE_NAME_VALUE);
        tempRecord.put(WifiNsdManager.NSD_INFO_SERVICE_PROTOCOL_ID, WifiNsdManager.NSD_INFO_SERVICE_PROTOCOL_VALUE);
        tempRecord.put(WifiNsdManager.NSD_INFO_INSTANCE_NAME_ID, sessionName);
        tempRecord.put(WifiNsdManager.NSD_INFO_SERVICE_PORT_ID, Integer.toString(hostPort));
        tempRecord.put(WifiNsdManager.NSD_INFO_SERVICE_TIME_ID, Long.toString(StampUtil.newTimeStamp()));

        this.nsdManager.advertiseService(tempRecord);

    }

    public void joinSession(String sessionName) {

        Map<String,String> tempRecord = new HashMap<>();
        tempRecord.put(WifiNsdManager.NSD_INFO_INSTANCE_NAME_ID, sessionName);

        nsdManager.connectToService(tempRecord);

    }

    public void findSessions() {

        Map<String,String> tempRecord = new HashMap<>();
        tempRecord.put(WifiNsdManager.NSD_INFO_SERVICE_NAME_ID, WifiNsdManager.NSD_INFO_SERVICE_NAME_VALUE);

        this.nsdManager.findService(tempRecord);

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
        this.localBroadcastManager.unregisterReceiver(this.broadcastReceiver);
    }

    /*
    PRIVATE METHODS
     */

}
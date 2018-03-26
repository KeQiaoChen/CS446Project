package com.synchronicity.APBdev.connectivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.qian.cs446project.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Andrew on 2018-02-15.
 */

public class WifiConnectionManager extends BaseConnectionManager {
    //Fields
    private Context context;
    private Activity activity;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pDnsSdServiceInfo serviceInfo;
    private WifiP2pServiceRequest serviceRequest;
    private HashMap<String, HashSet<WifiP2pDevice>> activeSessions;
    private HashSet<Socket> openSockets;
    private LocalBroadcastManager localBroadcastManager;
    private int serverPort;
    // Constants for NSD. These should go in an enum later?
    private static final String NSD_SERVICE_INSTANCE_VALUE = "SynchronicityCS446";
    private static final String NSD_SERVICE_PROTOCOL_VALUE = "_presence._tcp";
    private static final String NSD_RECORD_APP_ID = "AppName";
    private static final String NSD_RECORD_SESSION_ID = "SessionName";
    private static final String NSD_RECORD_PORT_ID = "PortNum";
    // Constants for signals. These should go in an enum later?
    private static final int BUFFER_SIZE = 1024;
    public static final byte SIG_PLAY_CODE = 1;
    public static final byte SIG_PAUSE_CODE = 2;
    public static final byte SIG_STOP_CODE = 3;
    private static final byte SIG_HAND_SHAKE = 4;
    // Debug constants
    private final String TAG = "WifiConnectionManager";


    // --- Constructors. --- //


    public WifiConnectionManager(Context context, Activity activity) {
        this.activity = activity;
        this.context = context;
        this.manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        this.channel = manager.initialize(context, context.getMainLooper(), null);
        this.serviceInfo = null;
        this.serviceRequest = null;
        this.activeSessions = new HashMap<String, HashSet<WifiP2pDevice>>();
        this.openSockets = new HashSet<Socket>();
        this.serverPort = 0;
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }


    // --- Methods adding to fields that need to be synced. --- //

    public void addOpenSocket(Socket socket) {
       // synchronized (this.openSockets) {
            Log.d("AddSocket", "start");
            openSockets.add(socket);
            Log.d("AddSocket", "end");

This component acts as a facade to the other two sub-modules to which it delegates its work.
The sub-module, NsdManager takes care of network discover and establishing initial wifi connections.
The sub-module, SocketManager takes care of maintaining socket connections and sending / receiving
of data.


    // --- Methods for common data transfer of ConnectionManager. --- //


    private void serverSocketAcceptThreadInit() {
        /*
            In a new thread, listen for incoming connections and create a client socket.
            We want the data to be received asynchronously as well, so we put that in its own.
            This should only be called once after the server socket is set up.
        */
        try {
            // This needs to be outside the thread so it can update the port value synchronously.
            final ServerSocket serverSocket = new ServerSocket(WifiConnectionManager.this.serverPort);
            WifiConnectionManager.this.serverPort = serverSocket.getLocalPort();
            Log.d("LocalPort", Integer.toString(WifiConnectionManager.this.serverPort));
            new Thread(new Runnable () {
                @Override
                public void run() {
                    Log.d("ServerInit", "Start");
                    // Toast.makeText(WifiConnectionManager.this.context, "Server Socket OK!", Toast.LENGTH_SHORT).show();
                    while (!serverSocket.isClosed()) {
                        Log.d("ServerInitWhile", "Start");
                        Socket clientSocket = null;
                        try {
                            clientSocket = serverSocket.accept();
                            Log.d("ServerInitWhile", "after accept");
                            // Add the socket to our set of open sockets. We can call these later in
                            // threaded methods to send data. In the mean time we should listen for incoming
                            // data.
                            WifiConnectionManager.this.addOpenSocket(clientSocket);
                            Log.d("ServerInitWhile", "after addOpenSocket");
                            // Listen for incoming data. Use serverInDataListenThreadInit.
                        } catch(IOException e) {
                            Log.d("ClientSocketFailure", e.getMessage());
                        }
                        Log.d("ServerInitWhile", "End");
                    }
                    Log.d("ServerInit", "End");
                }
            }).start();
        } catch (IOException e) {
            Log.d(TAG, "SERVERSOCKET initializing a thread ... FAILURE.");
            Log.d(TAG, e.getMessage());
        }
    }

    /*
    private void serverInDataListenThreadInit(final Socket clientSocket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Starts a thread that listens for incoming data.
                // Implement this when we need to send data to the server.
                // Should use serverInDataHandler.
            }
        });
    }
    private void serverInDataHandler(InputStream inputStream) {
        // Check for bytes to read.
        // Implement this for when the server needs to receive info.
    }
    */

    /*
        Implementation influenced by code example on stackoverflow:
        URL: https://stackoverflow.com/questions/18000093/how-to-marshall-and-unmarshall-a-parcelable-to-a-byte-array-with-help-of-parcel
    */


<<<<<<< HEAD
    private Context context;
    private NsdManager nsdManager;
    private SocketManager socketManager;
    private Map<String,String> recordMap;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    private final String classTag = "WifiConnectionManager> ";

    /*
        Draws on examples from the Android Developers Guide.
        https://developer.android.com/training/connect-devices-wirelessly/nsd-wifi-direct.html
     */

    /*
        Draws on examples from the following URL:
        https://androiddevsimplified.wordpress.com/2016/09/14/wifi-direct-service-discovery-in-android/

        Most of the examples describe templates for handling NSD and what the various callback
        methods are used for. The examples do not describe explicitly how to use the framework
        with respect to our application and therefore functionality will diverge beyond simple
        set up of the service
     */

        this.context = context;
        this.nsdManager = new WifiNsdManager(context);
        this.socketManager = new WifiSocketManager(context);
        this.recordMap = new HashMap<>();

        // Fill recordMap info here.
        recordMap.put("foo","bar");

    private void registerForNSD(String serviceName, String serviceProtocol, String sessionName, int sessionPort) {
        /*
            Set up a record with information about our service. We should include a session name,
            the app name (or other ID unique to the app), and the connection information.

            Use the following in this.manager.addLocalService.
         */
        Map record = new HashMap();
        record.put(this.NSD_RECORD_APP_ID, serviceName);
        record.put(this.NSD_RECORD_SESSION_ID, sessionName);
        record.put(this.NSD_RECORD_PORT_ID, Integer.toString(sessionPort));
        serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(serviceName, serviceProtocol, record);
        WifiP2pManager.ActionListener actionListener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "NSD local service added ... SUCCESS. (Server)");
                Toast.makeText(WifiConnectionManager.this.context, "Service Added!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "NSD local service added ... FAILURE. (Server)\nReason code: " + reason);
                Toast.makeText(WifiConnectionManager.this.context, "Service not added!", Toast.LENGTH_SHORT).show();
            }
        };
        this.manager.addLocalService(channel, serviceInfo, actionListener);
    }

    private void discoverForNSD(final String sessionName) {
        /*
            Set up listeners for NSD service responses, and for an incoming NSD textRecord.

            When a textRecord is received, check to make sure it's our app, and then save contained
            information about the session and device address in a map we can use when we wish to
            connect with peers.

    public void createSession(String sessionName) {
        this.nsdManager.advertiseService(recordMap);
    }

    public void joinSession(String sessionName) {
        this.nsdManager.connectToService(recordMap);
    }

    public void findSessions() {
        this.nsdManager.findService(recordMap);
    }

        });
    }


    public void sendSignal(byte signal) {
        this.socketManager.sendSignal(signal);

    }

    private class WcmBroadcastReceiver extends BroadcastReceiver {
        private WcmBroadcastReceiver() {
            super();
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            /* Monolithic if statement here. */
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
                /*
                    If wifi is enabled, everything is good, continue as normal ?
                    If wifi is disabled, notify user and cleanup any outstanding connections ?
                 */
                 int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                 if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {

                 } else {

                 }

            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                /*
                    Do as the above comment says.
                 */
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
                /*
                    Cleanup resources associated with lost connection.
                 */
                // The following code uses heavily from:
                // https://developer.android.com/training/connect-devices-wirelessly/wifi-direct.html
                Log.d("BCRinner", "Start");
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
                        @Override
                        public void onConnectionInfoAvailable(final WifiP2pInfo info) {
                            InetAddress groupOwnerAddress = info.groupOwnerAddress;

                            // If this device is the group owner, probably want to init a ServerSocket.
                            // Else this device is probably a client, and we should connect to the host.
                            if (info.groupFormed && !info.isGroupOwner) {
                                Log.d("ClientListener", "Port: "+Integer.toString(WifiConnectionManager.this.serverPort));
                                WifiConnectionManager.this.clientSocketInDataListenThreadInit(groupOwnerAddress.getHostAddress(), WifiConnectionManager.this.serverPort);
                            }
                        }
                    };
                    WifiConnectionManager.this.manager.requestConnectionInfo(WifiConnectionManager.this.channel, connectionInfoListener);
                    Log.d("BCRinner", "end");
                }
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
                /*
                    Similar to WIFI_P2P_STATE_CHANGED_ACTION ?
                 */
            }
        }
    }

    private class WcmIntentFilter extends IntentFilter {
        private WcmIntentFilter () {
            super();
        }
    }
}
package com.synchronicity.APBdev.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.qian.cs446project.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/*
The WifiNsdManager class implements the NsdManager interface using androids WifiP2p APIs. This class
facilitates formation of connections between devices by advertising the Synchronicity application
over an ad-hoc wireless network, which other users of the application can detect and subsequently
connect to.

The class is designed to act as a sub-module to other another class hierarchy from which work is
delegated.
 */

public class WifiNsdManager implements NsdManager<Map<String,String>> {


    /*
    FIELDS
     */

    private Context context;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel wifiP2pChannel;
    private WifiP2pServiceInfo wifiP2pServiceInfo;
    private WifiP2pServiceRequest wifiP2pServiceRequest;
    private HashMap<String,HashSet<WifiP2pDevice>> sessionMap;
    private NsdState nsdState;
    private String currentSession;
    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;
    /*
    Constant used for logging. classTag, combined with funcTag (found in functions) defines the
    tagging convention used for logging.
     */
    private final String classTag = "NsdManager>";
    /*
    Constants related to network service discovery using WifiP2p. These constants are used as keys
    for the Map which defines the NsdInfo. Constants ending in _ID refer to keys that should have
    values assigned to them outside of this class in a Map<String,String> object. This object is
    subsequently wrapped in an NsdInfo object to adhere to the NsdManager interface. The constants
    ending in _VALUE are included for convenience as a counterpart value to the respective keys.
     */
    public static final String NSD_INFO_SERVICE_NAME_ID = "sName";
    public static final String NSD_INFO_SERVICE_PROTOCOL_ID = "sProtocol";
    public static final String NSD_INFO_SERVICE_PORT_ID = "sPort";
    public static final String NSD_INFO_SERVICE_ADDRESS_ID = "sAddress";
    public static final String NSD_INFO_SERVICE_AD_ID = "sAd";
    public static final String NSD_INFO_SERVCIE_OLD_AD_ID = "sOldId";
    public static final String NSD_INFO_SERVICE_NAME_VALUE ="Synchronicity";
    public static final String NSD_INFO_SERVICE_PROTOCOL_VALUE ="_presence._tcp";
    /*
    Constants related to a specific instance of network service discovery using WifiP2p. These are
    constants which are also used for advertisement of a network service, but identify more specific
    (key,value) pairs which identify the instance of a shared service, as well as the user a unique
    identifier for the user advertising this service. Unlike
     */
    public static final String NSD_INFO_INSTANCE_NAME_ID = "sName";
    public static final String NSD_INFO_INSTANCE_USERTAG_ID = "sUser";

    /*
    CONSTRUCTORS
     */


    WifiNsdManager(Context context) {

        this.context = context;
        this.wifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        this.wifiP2pChannel = this.wifiP2pManager.initialize(context, context.getMainLooper(), null);
        this.wifiP2pServiceInfo = null;
        this.wifiP2pServiceRequest = null;
        this.sessionMap = new HashMap<>();
        this.nsdState = new PreSessionState();
        this.currentSession = "";

        this.localBroadcastManager = LocalBroadcastManager.getInstance(this.context);
        // BroadcastReceive creation and registration.
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


    @Override
    public void advertiseService(Map<String,String> nsdInfo) {

        final String funcTag = "Advertise> ";

        /*
        If we try to start another instance of service advertisement while we are already advertising,
        then we are in an error state and throw a new RuntimeException. Whatever calls this should
        check to make sure that we are not already running a service.
         */
        if (this.wifiP2pServiceInfo != null) {
            throw new RuntimeException(classTag+funcTag+"wifiP2pServiceInfo already has a value.");
        }
        
        String serviceName = nsdInfo.get(WifiNsdManager.NSD_INFO_SERVICE_NAME_ID);
        String serviceProtocol = nsdInfo.get(WifiNsdManager.NSD_INFO_SERVICE_PROTOCOL_ID);

        this.wifiP2pServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(
                serviceName,
                serviceProtocol,
                nsdInfo
        );

        this.wifiP2pManager.addLocalService(
                this.wifiP2pChannel,
                this.wifiP2pServiceInfo,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(classTag +funcTag,"addLocalService success.");
                    }
                    @Override
                    public void onFailure(int reason) {
                        Log.d(classTag +funcTag,"addLocalService failure.");
                    }
                }
        );

    }

    @Override
    public void findService(Map<String,String> nsdInfo) {

        final String funcTag = "Find> ";

        /*
        If we try to start another instance of service discovery while we are already searching,
        then we are in an error state and throw a new RuntimeException. Whatever calls this should
        check to make sure that we are not already searching for a service.
         */
        if (this.wifiP2pServiceRequest != null) {
            throw new RuntimeException(classTag+funcTag+"wifiP2pServiceRequest already has a value.");
        }

        final String serviceName = nsdInfo.get(WifiNsdManager.NSD_INFO_SERVICE_NAME_ID);

        this.wifiP2pManager.setDnsSdResponseListeners(
                this.wifiP2pChannel,
                new WifiP2pManager.DnsSdServiceResponseListener() {
                    @Override
                    public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                        Log.d(classTag+funcTag,"findService - service found.");
                    }
                },
                new WifiP2pManager.DnsSdTxtRecordListener() {
                    @Override
                    public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                        Log.d(classTag+funcTag,"findService - text record available.");
                        String otherServiceName = txtRecordMap.get(WifiNsdManager.NSD_INFO_SERVICE_NAME_ID);
                        /*
                        Check if the other service is our app. If so, then get the name of the
                        session being advertised, and add it to our set of available sessions.
                        If the session already exists, then add the new device to the set of devices
                        associated with the session.
                         */
                        if (serviceName == otherServiceName) {
                            String otherServiceInstance = txtRecordMap.get(WifiNsdManager.NSD_INFO_INSTANCE_NAME_ID);
                            updateSessionMap(otherServiceInstance, srcDevice);
                        }
                    }
                }
        );

    }

    @Override
    public void connectToService(Map<String,String> nsdInfo) {

        String instanceName = nsdInfo.get(WifiNsdManager.NSD_INFO_SERVICE_NAME_ID);
        this.currentSession = instanceName;

        /*
        Connect to all of the devices which we have tracked for the particular service instance.
         */
        for (WifiP2pDevice remoteDevice : this.sessionMap.get(instanceName)) {
            this.establishConnection(remoteDevice);
        }

        this.nsdState = new OngoingSessionState();

    }

    @Override
    public void createServiceGroup() {

        final String funcTag = "newGroup> ";

        this.wifiP2pManager.createGroup(
                this.wifiP2pChannel,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                       Log.d(classTag+funcTag,"createGroup success.");
                    }
                    @Override
                    public void onFailure(int reason) {
                        Log.d(classTag+funcTag, "createGroup failure");
                    }
                }
        );

    }

    @Override
    public void cleanUp() {

        final String funcTag = "clean> ";

        if(wifiP2pServiceInfo != null) {
            this.wifiP2pManager.removeLocalService(
                    this.wifiP2pChannel,
                    this.wifiP2pServiceInfo,
                    new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(classTag+funcTag, "cleanUp success. (removeLocalService)");
                        }
                        @Override
                        public void onFailure(int reason) {
                            Log.d(classTag+funcTag, "cleanUp failure. (removeLocalService)");
                        }
                    }
            );
        }

        if(wifiP2pServiceRequest != null) {
            this.wifiP2pManager.removeServiceRequest(
                    this.wifiP2pChannel,
                    this.wifiP2pServiceRequest,
                    new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(classTag+funcTag, "cleanUp success. (removeServiceRequest)");
                        }
                        @Override
                        public void onFailure(int reason) {
                            Log.d(classTag+funcTag, "cleanUp failure. (removeServiceRequest)");
                        }
                    }
            );
        }

        this.wifiP2pManager.cancelConnect(
                this.wifiP2pChannel,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(classTag+funcTag, "cleanUp success. (cancelConnect)");
                    }
                    @Override
                    public void onFailure(int reason) {
                        Log.d(classTag+funcTag, "cleanUp failure. (cancelConnect)");
                    }
                }
        );

        this.wifiP2pManager.removeGroup(
                this.wifiP2pChannel,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(classTag+funcTag, "cleanUp success. (removeGroup)");
                    }
                    @Override
                    public void onFailure(int reason) {
                        Log.d(classTag+funcTag, "cleanUp failure. (removeGroup)");
                    }
                }
        );

        this.context.unregisterReceiver(this.broadcastReceiver);

    }


    /*
    PRIVATE METHODS
     */


    /*
    A helper method which updates the sessionMap field (which tracks service instances + related
    devices). The method delegates its work out to a NsdState object, which will behave
    differently based on state. For example, when the user has connected to a particular service
    instance, we want to connect to any new device that the user encounters which is already a part
    of the ad-hoc network that is identical to the service instance in addition to updating the
    sessionMap field. On the other hand, when the user is not yet connected to a service instance,
    and is currently searching for different service instances to which they can connect, then we
    want to only update the sessionMap variable with information about new serviceInstances that the
    user encounters, and delay connection until the user chooses a service instance to join.
     */
    private void updateSessionMap(String instanceName, WifiP2pDevice device) {
        this.nsdState.updateServiceMap(instanceName, device);
    }

    /*
    A helper method which facilitates the establishment of connections between devices over wifi
    using Androids WifiP2p framework.
     */
    private void establishConnection(WifiP2pDevice remoteDevice) {

        final String funcTag = "eConn> ";

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = remoteDevice.deviceAddress;

        this.wifiP2pManager.connect(
                this.wifiP2pChannel,
                config,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(classTag+funcTag,"establishConnection success.");
                    }
                    @Override
                    public void onFailure(int reason) {
                        Log.d(classTag+funcTag,"establishConnection failure.");
                    }
                }
        );

    }


    /*
    INNER CLASSES
     */

    /*
    An inner interface facilitating the state dependent behaviour for WifiNsdManager.
     */
    private interface NsdState {
        void updateServiceMap(String instanceName, WifiP2pDevice device);
    }

    /*
    An inner class implementing the NsdState interface. It is a concrete class which
    implements behaviour when the WifiNsdManager is in a pre-session state.
     */
    private class PreSessionState implements NsdState {
        @Override
        public void updateServiceMap(String instanceName, WifiP2pDevice device) {

            if (!WifiNsdManager.this.sessionMap.containsKey(instanceName)) {

                WifiNsdManager.this.sessionMap.put(
                        instanceName,
                        new HashSet<WifiP2pDevice>()
                );

                /*
                We haven't encountered this session instance before, so we want to update the
                components that care about knowing of new sessions in a pre-session state.
                */
                Intent broadcastIntent = new Intent(context.getString(R.string.find_session_return));
                broadcastIntent.putExtra(context.getString(R.string.available_sessions_key), instanceName);
                localBroadcastManager.sendBroadcast(broadcastIntent);

            }
            WifiNsdManager.this.sessionMap.get(instanceName).add(device);
        }
    }

    /*
    An inner class implementing the NsdState interface. It is a concrete class which
    implements behaviour when the WifiNsdManager is in a pre-session state.
     */
    private class OngoingSessionState implements NsdState {
        @Override
        public void updateServiceMap(String instanceName, WifiP2pDevice device) {
            if (instanceName == WifiNsdManager.this.currentSession) {
                WifiNsdManager.this.sessionMap.get(instanceName).add(device);
                WifiNsdManager.this.establishConnection(device);
            }
        }
    }

}

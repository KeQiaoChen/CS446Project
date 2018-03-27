package com.synchronicity.APBdev.connectivity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.qian.cs446project.Playlist;
import com.example.qian.cs446project.R;
import com.synchronicity.APBdev.util.ParcelableUtil;
import com.synchronicity.APBdev.util.StampUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class WifiSocketManager implements SocketManager {


    /*
     FIELDS
     */


    private Context context;
    private ServerSocket serverSocket;
    private Set<Socket> activeConnectionsSet;
    private Set<ServerInfo> serverInfoSet;
    private DistributionStrategy distributionStrategy;
    private DataStampPool dataStampPool;
    private Playlist playlist;
    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;


    /*
     CONSTANTS
     */


    private final String classTag = "SocketMgr> ";

    /*
    A special inner class (not appearing with other inner classes), which defines constants related
    to this object.
     */
    public static class Constants {
        /*
        Internal constants for defining header and buffer sizes.
         */
        public static final int HEADER_SIZE = 128;
        public static final int BUFFER_SIZE = 1024;
        /*
        Signal constants are defined here. They are to be used for creating headers, and possibly
        as a parameter to sendSignal.
         */
        public static final byte NULL_SIGNAL = 0;
        public static final byte PLAY_SIGNAL = 1;
        public static final byte PAUSE_SIGNAL = 2;
        public static final byte STOP_SIGNAL = 3;
        public static final byte SEND_PLAYLIST_SIGNAL = (byte) 4;
        public static final byte SEND_DATA_SIGNAL = 5;
        public static final byte END_SESSION_SIGNAL = 6;
        /*
        Indices for data in the header.
         */
        public static final int SIGNAL_INDEX = 0;
        public static final int STAMP_INDEX = 1;
        public static final int DATA_LENGTH_INDEX = 9;
        public static final int FILE_NAME_LENGTH_INDEX = 13;
        public static final int FILE_NAME_STRING_INDEX = 17;
    }


    /*
     CONSTRUCTORS
     */


    WifiSocketManager(Context context) {

        this.context = context;
        this.activeConnectionsSet = new HashSet<>();
        this.serverInfoSet = new HashSet<>();
        this.dataStampPool = new DataStampPool(32);
        this.playlist = null;
        this.distributionStrategy = new GreedyStrategy();
        try {
            this.serverSocket = new ServerSocket();
        } catch (IOException ioException) {
            ioException.printStackTrace();
            Log.d(classTag,"An IO exception was thrown in the SocketManager constructor.");
        }
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        // BroadcastReceive creation and registration.
        this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                Log.d("intent>",action);

                if (action.equals(context.getString(R.string.give_session_playlist))) {

                    Log.d("playlist>", "!");
                    WifiSocketManager.this.playlist = intent.getExtras().getParcelable(context.getString(R.string.session_playlist));

                }

            }
        };
        this.intentFilter = new IntentFilter();
        this.intentFilter.addAction(context.getString(R.string.give_session_playlist));
        this.intentFilter.addAction(context.getString(R.string.user_chose_session));

        this.localBroadcastManager.registerReceiver(this.broadcastReceiver, this.intentFilter);

    }


    /*
     PUBLIC METHODS
     */

    /*
    Return the port that this server is listening on.
     */
    @Override
    public int getServerPort() {
        return this.serverSocket.getLocalPort();
    }

    /*
    Return the address which this server is initialized to.
     */
    @Override
    public String getServerAddress() {
        return this.serverSocket.getInetAddress().getHostAddress();
    }

    /*
    Initializes a server socket so that it may receive incoming connection requests. Upon receiving
    a request, the ServerSocket creates a Socket object that can be used for two way communication.
     */
    @Override
    public void initServerSocket() {

        final String funcTag = "initServ> ";

        Log.d(funcTag, "init serv func start");

        /*
        On initialization of a server socket, bind it to an address and port. This could throw
         */
        try {
            this.serverSocket.bind(null);
        } catch (IOException ioException) {
            Log.d(classTag+funcTag,"There was an IO exception");
            ioException.printStackTrace();
        }

        /*
        Start a new thread that allows the server socket to listen for incoming connections. When
        a connection is formed, the method initDataReceiveHandler is called, which creates a new
        thread which listens for incoming data on the new connections.
         */
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.d(funcTag, "init serv thread start");

                        try {
                            while(!WifiSocketManager.this.serverSocket.isClosed()) {

                                Log.d(funcTag, "init serv while");


                                Socket remoteSocket = WifiSocketManager.this.serverSocket.accept();
                                WifiSocketManager.this.activeConnectionsSet.add(remoteSocket);
                                WifiSocketManager.this.initDataReceiveHandler(remoteSocket);
                                Playlist playlist = WifiSocketManager.this.playlist;
                                WifiSocketManager.this.sendData(playlist);


                            }
                        } catch (IOException ioException) {
                            Log.d(classTag+funcTag, "There was an IO exception.");
                            ioException.printStackTrace();
                        }
                        Log.d(funcTag, "init serv thread end");

                    }
                }
        ).start();

        Log.d(funcTag, "init serv func end");

    }

    /*
    Forms a permanent socket connection between this device and a remote device. This connection is
    intended to be used for two way communication of simple signals that need to be deployed in a
    short amount of time. Note that connections for transferring data are done in an add-hoc manner.
     */
    @Override
    public void connectToServer(final String hostName, final int hostPort) {

        final String funcTag = "Con2Serv>";

        Log.d(classTag+funcTag,"Connected to server.");

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        /*
                        Form the connection to the remote socket. This allows two way communication between this
                        device and the other device.
                         */
                        Log.d("Con2Serv>", "Thread start.");


                        Socket remoteSocket = formServerConnection(hostName, hostPort);
                        WifiSocketManager.this.serverInfoSet.add(new ServerInfo(hostName, hostPort));
                        WifiSocketManager.this.activeConnectionsSet.add(remoteSocket);
                        WifiSocketManager.this.initDataReceiveHandler(remoteSocket);

                        Log.d("Con2Serv>", "Thread end.");


                    }
                }
        ).start();

    }

    /*
    Uses the distribution strategy to rapidly send a small signal to the other devices for the
    purpose of controlling play, pause, and stopping.
     */
    @Override
    public void sendSignal(byte sigType) {

        byte[] headerBytes = this.makeHeader(sigType, 0, null);
        this.distributionStrategy.distributeSignal(headerBytes);

    }

    /*
    Uses the distribution strategy to rapidly send a marshaled parcelable to another device.
     */
    @Override
    public void sendData(Parcelable parcelable) {

        String funcTag = "sendData>";

        Log.d(funcTag,"start");

        byte[] dataBytes = ParcelableUtil.marshall(parcelable);

        Log.d("dataBytes>", "Length of dataBytes: "+Integer.toString(dataBytes.length));

        byte[] headerBytes = this.makeHeader(Constants.SEND_PLAYLIST_SIGNAL, dataBytes.length, null);
        this.distributionStrategy.distributeData(headerBytes, dataBytes);

        Log.d(funcTag,"end");

    }

    /*
    Uses the distribution strategy to get the contents of a file found at pathToData, and rapidly
    send the data to another device.
     */
    @Override
    public void sendDataByPath(String pathToData) {

        final String funcTag = "sendPath> ";

        try {
            byte[] bufferBytes = new byte[WifiSocketManager.Constants.BUFFER_SIZE];

            FileInputStream fileInputStream = new FileInputStream(pathToData);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            int bytesRead = 0;

            while (bytesRead != -1) {
                bytesRead = fileInputStream.read(bufferBytes);
                byteArrayOutputStream.write(bufferBytes, 0, bytesRead);
            }

            String[] pathTokens = pathToData.split("/");
            String fileName = pathTokens[pathTokens.length - 1];

            byte[] dataBytes = byteArrayOutputStream.toByteArray();
            byte[] headerBytes = this.makeHeader(Constants.SEND_DATA_SIGNAL, dataBytes.length, fileName);

            this.distributionStrategy.distributeData(headerBytes, dataBytes);
        } catch (Exception exception) {
            exception.printStackTrace();
            Log.d(classTag+funcTag,"An exception was thrown in sendDataByPath");
        }
    }

    /*

     */
    @Override
    public void cleanUp(){
        this.localBroadcastManager.unregisterReceiver(this.broadcastReceiver);
    }


    /*
    PRIVATE METHODS
     */


    /*
    A private method that facilitates the creation of a thread that listens for incoming data on the
    socket that is referenced by remoteSocket.
     */
    private void initDataReceiveHandler(final Socket remoteSocket) {

        final String funcTag = "dataHandle> ";

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (!remoteSocket.isClosed()) {

                            Log.d(funcTag, "Thread start.");


                            try {
                                // Read a number of bytes no bigger than the size of the header.
                                byte[] headerBytes = new byte[Constants.HEADER_SIZE];
                                int readHeaderPosition = 0;
                                int readHeaderMax = Constants.HEADER_SIZE;

                                BufferedInputStream bufferedInputStream = new BufferedInputStream(remoteSocket.getInputStream());

                                Log.d(funcTag, "got to before while");

                                while (readHeaderMax - readHeaderPosition != 0) {

                                    Log.d(funcTag, "readHeaderPosition value: " + Integer.toString(readHeaderPosition));

                                    readHeaderPosition += bufferedInputStream.read(headerBytes, readHeaderPosition, readHeaderMax - readHeaderPosition);
                                    Log.d(funcTag, "readHeaderPosition value: " + Integer.toString(readHeaderPosition));

                                }

                                Log.d(funcTag, "got to after while");

                                // Based on the header and the data, do something:
                                byte signalType = WifiSocketManager.this.getHeaderSignalType(headerBytes);

                                Log.d(funcTag, "Signal Type: "+Byte.toString(signalType));

                                switch (signalType) {

                                    case Constants.NULL_SIGNAL:
                                    case Constants.PLAY_SIGNAL:
                                    case Constants.PAUSE_SIGNAL:
                                    case Constants.STOP_SIGNAL:
                                        forwardSignal(headerBytes);
                                        break;

                                    case Constants.END_SESSION_SIGNAL:
                                        forwardSignal(headerBytes);
                                        // Logic for ending session here.
                                        break;

                                    case Constants.SEND_PLAYLIST_SIGNAL:
                                    case Constants.SEND_DATA_SIGNAL:

                                        int dataLength = getDataLength(headerBytes);
                                        byte[] dataBytes = new byte[dataLength];
                                        int readDataPosition = 0;
                                        int readDataMax = dataLength;

                                        while (readDataMax - readDataPosition != 0) {

                                            readDataPosition += bufferedInputStream.read(dataBytes, readDataPosition, readDataMax - readDataPosition);

                                        }

                                        // forwardData(headerBytes, dataBytes);

                                        // If signal type is for a playlist, then un-marshal and send in an intent for others to handl.
                                        if (signalType == Constants.SEND_PLAYLIST_SIGNAL) {
                                            Playlist parcelable = ParcelableUtil.unmarshall(dataBytes, Playlist.CREATOR);

                                            // Send and intent here.
                                            if (parcelable == null) {
                                                Log.d("InitReceiveDebug>","parcelable is null" );
                                            }
                                            else {
                                                Log.d("InitReceiveDebug>", "parcelable is not null");
                                            }

                                            Context context = WifiSocketManager.this.context;
                                            Intent intent = new Intent(context.getString(R.string.playlist_ready));
                                            intent.putExtra(context.getString(R.string.session_playlist), parcelable);
                                            WifiSocketManager.this.localBroadcastManager.sendBroadcast(intent);


                                        }

                                        // if signal type is a for a file, then write that file to the temp folder and send and intent
                                        // for others to do something with that?
                                        if (signalType == Constants.SEND_DATA_SIGNAL) {
                                            // read the string
                                            String fileName = getFileNameString(headerBytes);
                                            String filePath = "";
                                            FileOutputStream fileOutputStream = new FileOutputStream(filePath + fileName);
                                            fileOutputStream.write(dataBytes);
                                        }

                                        // close the socket.
                                        remoteSocket.close();

                                        break;

                                    default:
                                        break;

                                }
                            } catch (Exception exception) {
                                exception.printStackTrace();
                                Log.d("exeption>","an exception happened in the handler. It's a bit of a mess so we have to do some work to find the problem");
                            }

                        }
                    }
                }
        ).start();
    }

    private void forwardSignal(byte[] headerBytes) {

        long stamp = getHeaderStamp(headerBytes);

        if (!dataStampPool.containsStamp(stamp)) {
            this.dataStampPool.add(stamp);
            this.distributionStrategy.distributeSignal(headerBytes);
        }

    }

    private void forwardData(byte[] headerBytes, byte[] dataBytes) {

        long stamp = getHeaderStamp(headerBytes);

        if (!dataStampPool.containsStamp(stamp)) {
            this.dataStampPool.add(stamp);
            this.distributionStrategy.distributeData(headerBytes, dataBytes);
        }

    }

    /*
    A private method that forms a connection between two devices, where one device supplies the
    information for which ServerSocket it would like to connect using the hostName and hostPort
    parameters.
     */
    private Socket formServerConnection(String hostName, int hostPort) {

        final String funcTag = "formConn> ";

        Log.d("formConn>", "func start.");


        /*
        Attempt to connect to the ServerSocket on the device which we wish to communicate.
         */
        Socket remoteSocket = null;
        try {
            remoteSocket = new Socket();
            remoteSocket.bind(null);
            remoteSocket.connect(new InetSocketAddress(hostName, hostPort), 500);
        } catch (IOException ioException) {
            Log.d(classTag+funcTag,"There was an IO exception.");
            ioException.printStackTrace();
        }

        Log.d("formConn>", "func end.");


        return remoteSocket;
    }

    /*
    A private method to aid in the construction of a header that specified meta-data for the purpose
    of communication between devices.
     */
    private byte[] makeHeader(byte signalType, int dataLength, String dataName) {

        byte[] dataHeader = new byte[WifiSocketManager.Constants.HEADER_SIZE];
        int headerTop = 0;

        // First byte should be the type of data transfer.
        dataHeader[headerTop] = signalType;
        headerTop += 1;

        // Next we need to stamp the data.
        long stamp = StampUtil.newUniqueStamp();

        for (int i = 0; i < 8; i++) {
            dataHeader[headerTop] = (byte) (stamp >> i*8);
            headerTop += 1;
        }

        // Next we need to get the data length.
        byte[] lengthBytes = ByteBuffer.allocate(4).putInt(dataLength).array();
        for (byte aByte : lengthBytes) {
            dataHeader[headerTop] = aByte;
            headerTop += 1;
        }

        /*
        for (int i = 0; i < 4; i++) {
            dataHeader[headerTop] = (byte) (dataLength >> i*8);
            headerTop += 1;
        }
        */

        // Next, if string is not null, then add the length of the string, as well as the string.
        if (dataName != null) {

            byte[] dataNameBytes = dataName.getBytes();
            int dataNameLength = dataNameBytes.length;

            for (int i = 0; i < 4; i++) {
                dataHeader[headerTop] = (byte) (dataNameLength >> i * 8);
                headerTop += 1;
            }

            for (int i = 0; i < dataNameLength; i++) {
                dataHeader[headerTop] = dataNameBytes[i];
                headerTop += 1;
            }

        }

        return dataHeader;

    }

    /*
    Gets the header signal type;
     */
    private byte getHeaderSignalType(byte[] headerBytes) {
        return headerBytes[0];
    }

    /*
    Gets the header stamp value
     */
    private long getHeaderStamp(byte[] headerBytes) {

        byte[] stampData = new byte[8];

        for (int i = 0; i < 8; i++) {
            stampData[i] = headerBytes[Constants.STAMP_INDEX + i];
        }

        long stamp = 0;

        for (int i = 0; i < 8; i++) {
            long tempStamp = stampData[i];
            tempStamp = (tempStamp << i*8);
            stamp += tempStamp;
        }

        return stamp;

    }

    /*
    Gets the header data length of the file being transferred.
     */
    private int getDataLength(byte[] headerBytes) {

        byte[] lengthData = new byte[4];

        for (int i = 0; i < 4; i++) {
            lengthData[i] = headerBytes[Constants.DATA_LENGTH_INDEX + i];
        }

        return ByteBuffer.wrap(lengthData).getInt();

        /*

        int length = 0;
        int tempLength = 0;

        for (int i = 0; i < 4; i++) {
            tempLength = lengthData[i];
            tempLength = (tempLength << i*8);
            length += tempLength;
        }

        return length;

        */

    }

    /*
    Gets the header length of the string of the file name being transferred.
     */
    private int getFileNameLength(byte[] headerBytes) {

        byte[] lengthData = new byte[4];

        for (int i = 0; i < 4; i++) {
            lengthData[i] = headerBytes[Constants.FILE_NAME_LENGTH_INDEX + i];
        }

        int length = 0;

        for (int i = 0; i < 4; i++) {
            int tempLength = lengthData[i];
            length = (tempLength << i*8);
            length += tempLength;
        }

        return length;

    }

    /*
    Gets the header file name string.
     */
    private String getFileNameString(byte[] headerBytes) {

        int stringDataLength = this.getFileNameLength(headerBytes);
        byte[] stringData = new byte[stringDataLength];

        for (int i = 0; i < stringDataLength; i++) {
            stringData[i] = headerBytes[Constants.FILE_NAME_STRING_INDEX + i];
        }

        return new String(stringData);

    }


    /*
    INNER CLASSES
     */


    /*
    A private interface that defines the operations that are used for strategies regarding data
    distribution.
     */
    private interface DistributionStrategy {
        void distributeData(final byte[] headerBytes, final byte[] dataBytes);
        void distributeSignal(final byte[] headerBytes);
    }

    /*
    A realisation of the distribution strategy interface. This is accomplished by allowing the
    strategy to greedily attempt to send data to as many remote devices as possible.
     */
    private class GreedyStrategy implements DistributionStrategy {

        private final String classTag = "greedStrat> ";

        /*
        Method for distributing data using the greedy strategy.
         */
        public void distributeData(final byte[] headerBytes, final byte[] dataBytes) {

            final String funcTag = "greedData> ";

            /*
            For each of the servers that we have information for, attempt to form a connection
            in an ad-hoc fashion for the purposes of transferring data. The sender initially sends
            the header for the data being sent, and if the receiver does not need the data that the
            sender is offering, then the receiver terminates the socket connection and the thread
            should terminate and return immediately.
             */

            /*
            for (ServerInfo serverInfo : serverInfoSet) {
                try {
                    Socket remoteSocket = WifiSocketManager.this.formServerConnection(serverInfo.serverName, serverInfo.serverPort);
                    final OutputStream outputStream = remoteSocket.getOutputStream();
                    outputStream.write(headerBytes);

                    new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        outputStream.write(dataBytes);
                                    } catch (IOException ioException) {
                                        Log.d(classTag+funcTag,"IO exception in greedy distribution thread.");
                                        ioException.printStackTrace();
                                    }
                                }
                            }
                    ).start();
                } catch (IOException ioException) {
                    Log.d(classTag+funcTag,"IO exception in greedy distribution");
                    ioException.printStackTrace();
                }
            }
            */

            for (final Socket remoteSocket : WifiSocketManager.this.activeConnectionsSet) {

                Log.d(funcTag,"greedy strat for");

                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    Log.d(funcTag,"greedy before write bytes");

                                    int headerSize = headerBytes.length;
                                    int encodedSize = getDataLength(headerBytes);

                                    OutputStream outputStream = remoteSocket.getOutputStream();
                                    outputStream.write(headerBytes);
                                    outputStream.write(dataBytes);

                                    Log.d(funcTag,"greedy after write bytes");

                                }
                                catch (IOException e) {
                                    Log.d(classTag+funcTag, "IO exception in thread greedy strat.");
                                    e.printStackTrace();
                                }
                            }
                        }
                ).start();

            }

        }

        /*
        Method for distributing signals using the greedy strategy.
         */
        public void distributeSignal(final byte[] headerBytes) {

            final String funcTag = "greedySig> ";

            /*
            For each of the sockets in the set of active connections, attempt to send a signal
            inside of a header.
             */
            for (final Socket remoteSocket : activeConnectionsSet) {
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    OutputStream outputStream = remoteSocket.getOutputStream();
                                    outputStream.write(headerBytes);
                                } catch (IOException ioException) {
                                    Log.d(classTag+funcTag,"IO exception in greedy distribution thread.");
                                    ioException.printStackTrace();
                                }
                            }
                        }
                ).start();
            }

        }
    }

    /*
    A simple collection of the unique data stamps that are included in the header information of
    communications between devices. This allows a device to figure out if it has already received
    information about communications, and to react accordingly.
     */
    private class DataStampPool {

        int dataStampSetHead;
        int dataStampSetCapacity;
        long[] dataStampCollection;

        DataStampPool(int dataStampSetCapacity) {

            this.dataStampSetHead = 0;
            this.dataStampSetCapacity = dataStampSetCapacity;
            this.dataStampCollection = new long[dataStampSetCapacity];

        }

        public void add(long dataStamp) {

            this.dataStampCollection[this.dataStampSetHead] = dataStamp;
            this.dataStampSetHead = (this.dataStampSetHead + 1) % dataStampSetCapacity;

        }

        public boolean containsStamp(long dataStamp) {

            for (long currentStamp : this.dataStampCollection) {
                if (currentStamp == dataStamp) {
                    return true;
                }
            }
            return false;

        }

    }

    /*
    A simple data structure that aggregates information needed for connecting to a ServerSocket.
     */
    private class ServerInfo {

        String serverName;
        int serverPort;

        ServerInfo(String serverName, int serverPort) {
            this.serverName = serverName;
            this.serverPort = serverPort;
        }

    }

}

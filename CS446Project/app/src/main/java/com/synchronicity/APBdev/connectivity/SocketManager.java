package com.synchronicity.APBdev.connectivity;


        import android.os.Parcelable;

        import java.net.ServerSocket;
        import java.net.Socket;

public interface SocketManager {

    interface ActionListener {
        void onReceive(Socket remoteSocket);
    }

    int getServerPort();
    String getServerAddress();
    void initServerSocket();
    void connectToServer(final String hostName, final int hostPort);
    void sendData(Parcelable parcelable);
    void sendDataByPath(String pathToData);
    void sendSignal(byte sigType);
    void cleanUp();


}

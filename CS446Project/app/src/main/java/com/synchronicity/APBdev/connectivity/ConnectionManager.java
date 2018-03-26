package com.synchronicity.APBdev.connectivity;

import android.os.Parcelable;

public interface ConnectionManager {

    // Methods for common connectivity of ConnectionManager.
    void createSession(String sessionName);
    void joinSession(String sessionName);
    void findSessions();
    void sendData(Parcelable parcelable);
    void sendDataByPath(String pathToData);
    void sendSignal(int signal);
    void cleanUp();

}

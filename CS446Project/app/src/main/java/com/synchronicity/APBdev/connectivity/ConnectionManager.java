package com.synchronicity.APBdev.connectivity;

import android.os.Parcelable;

import java.util.ArrayList;

public interface ConnectionManager {

    // Methods for common connectivity of ConnectionManager.
    void createSession(String sessionName);
    void joinSession(String sessionName);
    void findSessions();
    void sendData(Parcelable parcelable);
    void sendDataByPath(String pathToData);
    void sendSignal(byte signal);
    void cleanUp();

}

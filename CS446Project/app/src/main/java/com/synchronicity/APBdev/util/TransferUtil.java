package com.synchronicity.APBdev.util;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*
Credit for the code in copyFile is from the source code of the examples for transferring over
WifiP2p. The source can be found at:

https://github.com/Miserlou/Android-SDK-Samples/blob/master/WiFiDirectDemo/src/com/example/android/wifidirect/DeviceDetailFragment.java
 */

public class TransferUtil {

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d("transferUtil>", e.toString());
            return false;
        }
        return true;
    }

}

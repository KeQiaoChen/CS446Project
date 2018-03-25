package com.synchronicity.APBdev.util;

/*
 This is a utility class for marshalling and unmarshalling of Parcelable type objects. I am not the
 original author of the source code, and the place in which I found this code can be visited at the
 following URL:

 https://stackoverflow.com/questions/18000093/how-to-marshall-and-unmarshall-a-parcelable-to-a-byte-array-with-help-of-parcel

 The link to the profile of the author of the code can be visited at the following URL:

 https://stackoverflow.com/users/194894/flow
 */

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableUtil {
    public static byte[] marshall(Parcelable parceable) {
        Parcel parcel = Parcel.obtain();
        parceable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }

    public static Parcel unmarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0); // This is extremely important!
        return parcel;
    }

    public static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = unmarshall(bytes);
        T result = creator.createFromParcel(parcel);
        parcel.recycle();
        return result;
    }
}
package com.synchronicity.APBdev.util;

import java.util.Random;

public class StampUtil {

    private static final Random random = new Random();

    public static Long newUniqueStamp() {

        return (System.currentTimeMillis() << 32) + random.nextInt();

    }

    public static Long newTimeStamp() {

        return System.currentTimeMillis();

    }

}

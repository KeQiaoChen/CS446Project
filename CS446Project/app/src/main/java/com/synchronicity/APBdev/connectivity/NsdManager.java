package com.synchronicity.APBdev.connectivity;

import java.util.ArrayList;

/**
 * Created by Andrew on 2018-03-16.
 */

/*
    The class NsdManager defines an interface for objects which drive Network Service Discovery.
    The class uses a wrapper called NsdInfo to polymorphically wrap any kind of type that the
    client wishes to use to identify information about their service. This wrapper uses Java generic
    templates to fulfil this purpose, and so it is incumbant upon the client to know exaclty what
    types that they will be using in the implementation of NsdManager.
 */

public interface NsdManager<T> {

        void advertiseService(T nsdInfo);

        ArrayList<String> findService(T nsdInfo);

        void connectToService(T nsdInfo);

        void cleanUp();

}
package com.synchronicity.APBdev.connectivity;

/*
    This is a class which is intended to abstract the information that is used to advertise services
    over a network. It acts as a wrapper for the information that is actually needed by the type
    NsdManager, which is an interface. Thus the client can define the type of object that they want
    to use for advertising their service and then pass it the the NsdManager to handle as they so
    please by wrapping it with this class, and then dealing with it in their own implementation.
 */

public interface NsdInfo {

    <T> T getInfo();

}

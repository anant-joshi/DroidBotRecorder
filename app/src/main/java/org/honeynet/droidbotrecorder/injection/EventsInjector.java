package org.honeynet.droidbotrecorder.injection;

import java.util.ArrayList;

/**
 * Created by anant on 1/6/18.
 */

public class EventsInjector {
    public static final String LogTag = "EVENTS";

    static {
        System.loadLibrary("EventsInjector");
    }

    public ArrayList<InputDevice> inputDevices = new ArrayList<>();

    static native int intEnableDebug(int enable);

    static native int scanDevices();

    static native int openDevice(int deviceId);

    static native int closeDevice(int deviceId);

    static native String getDevicePath(int deviceId);

    static native String getDeviceName(int deviceId);

    static native int pollDevice(int deviceId);

    static native int getType();

    static native int getCode();

    static native int getValue();

    static native int injectEvent(int deviceId, int type, int code, int value);

    public int init() {
        inputDevices.clear();
        return 0;
    }

    public int release() {
        return 0;
    }


}

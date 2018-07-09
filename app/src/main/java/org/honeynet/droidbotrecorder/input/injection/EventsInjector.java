package org.honeynet.droidbotrecorder.input.injection;

import android.util.Log;

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

    public EventsInjector() {
        inputDevices.clear();
        int num_devices = scanDevices();
        if (num_devices <= 0) {
            Log.v("EventsInjector", "" + num_devices);
        }
        for (int i = 0; i < num_devices; i++) {
            inputDevices.add(new InputDevice(i, getDevicePath(i)));
        }
        this.enableDebug(true);
    }

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

    public InputDevice getTouchScreen() {
        InputDevice device = null;
        for (InputDevice inputDevice : inputDevices) {
            if (inputDevice.getPath().contains("event1")) {
                device = inputDevice;
                break;
            }
        }
        try {
            Log.v("Touchscreen", device.getPath());
        } catch (NullPointerException e) {
            Log.e("EventsInjector", "getTouchScreen(): " + e.getMessage());
        }
        return device;
    }

    public void enableDebug(boolean enable) {
        if (enable) {
            intEnableDebug(1);
        }
    }

    public void release() {
        for (InputDevice inputDevice : inputDevices) {
            inputDevice.close();
        }
    }
}

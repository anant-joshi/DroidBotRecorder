package org.honeynet.droidbotrecorder.injection;

import android.util.Log;

import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

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

    public void enableDebug(boolean enable) {
        if (enable) {
            intEnableDebug(1);
        }
    }

    public int init() {
        inputDevices.clear();
        //Test for now:
        //Chmod 666 for /dev/input/
        Command chmodDevInput = new Command(1, "chmod 0777 /dev/input/event0");
        try {
            RootTools.getShell(true).add(chmodDevInput);
        } catch (IOException | TimeoutException | RootDeniedException ex) {
            Log.e("EventInjector: Init", ex.getMessage());
        }

        int num_devices = scanDevices();

        for (int i = 0; i < num_devices; i++) {
            inputDevices.add(new InputDevice(i, getDevicePath(i)));
        }
        return num_devices;
    }

    public void release() {
        for (InputDevice inputDevice : inputDevices) {
            inputDevice.close();
        }
    }
}

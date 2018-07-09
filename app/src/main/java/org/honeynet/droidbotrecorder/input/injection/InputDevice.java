package org.honeynet.droidbotrecorder.input.injection;

import android.util.Log;

/**
 * Created by anant on 1/6/18.
 */

public class InputDevice {
    private static final int EV_ABS = 0x0003,
            ABS_MT_TRACKING_ID = 0x0039,
            EV_KEY = 0x0001,
            ABS_MT_PRESSURE = 0x003a,
            ABS_MT_POSITION_X = 0x0035,
            ABS_MT_POSITION_Y = 0x0036,
            EV_SYN = 0,
            SYN_REPORT = 0,
            ABS_MT_TOUCH_MAJOR = 0x0030;
    private int id;
    private String path;
    private String name;
    private boolean open;

    public InputDevice(InputDevice in) {
        this.id = in.id;
        this.path = in.path;
        this.name = in.name;
        this.open = in.open;
    }

    public InputDevice(int id, String path) {
        this.id = id;
        this.path = path;
    }

    public int getPollingEvent() {
        return EventsInjector.pollDevice(this.id);
    }

    public int getSuccessfulPollingType() {
        return EventsInjector.getType();
    }

    public int getSuccessfulPollingCode() {
        return EventsInjector.getCode();
    }

    public int getSuccessfulPollingValue() {
        return EventsInjector.getValue();
    }

    public boolean isOpen() {
        return this.open;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getPath() {
        return this.path;
    }

    public void close() {
        this.open = false;
        EventsInjector.closeDevice(this.id);
    }

    public boolean open() {
        int result = EventsInjector.openDevice(this.id);
        if (result == 0) {
            this.name = EventsInjector.getDeviceName(this.id);
            Log.d("InputDevice", "Open:" + this.path + " Name:" + this.name + " Result:" + result);
        } else {
            Log.d("InputDevice", "Cannot open device");
        }
        return (result == 0);
    }

    public int sendKey(int key, boolean state) {
        int value = (state) ? 1 : 0;
        return EventsInjector.injectEvent(this.id, EV_KEY, key, value);
    }

    private boolean isLegalCoordinate(int val) {
        return (val >= 0 && val < 32768);
    }

    public boolean sendTouchDownRel(int x, int y) {
        if (!(isLegalCoordinate(x) && isLegalCoordinate(y))) {
            Log.e("InputDevice.java", "Error: please enter a value between 0 and 32767");
            return false;
        }
        EventsInjector.injectEvent(id, EV_ABS, ABS_MT_TRACKING_ID, 0x0000);
        EventsInjector.injectEvent(id, EV_ABS, ABS_MT_TOUCH_MAJOR, 0x0020);
        EventsInjector.injectEvent(id, EV_ABS, ABS_MT_PRESSURE, 0x0081);
        EventsInjector.injectEvent(id, EV_ABS, ABS_MT_POSITION_X, x);
        EventsInjector.injectEvent(id, EV_ABS, ABS_MT_POSITION_Y, y);
        EventsInjector.injectEvent(id, EV_SYN, SYN_REPORT, 0x0000);
        return true;
    }

    public boolean sendTouchDownAbs(int x, int y, int screenW, int screenH) {
        return this.sendTouchDownRel((0x7fff / screenW * x), (0x7fff / screenH * y));
    }

    public int sendTouchUp() {
        EventsInjector.injectEvent(this.id, EV_ABS, ABS_MT_PRESSURE, 0);
        EventsInjector.injectEvent(this.id, EV_ABS, ABS_MT_TRACKING_ID, -1);
        EventsInjector.injectEvent(this.id, EV_SYN, SYN_REPORT, 0);
        return 0;
    }
}


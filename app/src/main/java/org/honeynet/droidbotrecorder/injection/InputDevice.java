package org.honeynet.droidbotrecorder.injection;

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

    public int sendKey(int key, boolean state) {
        int value = (state) ? 1 : 0;
        return EventsInjector.injectEvent(this.id, EV_KEY, key, value);
    }

    public int sendTouchDownAbs(int x, int y) {
        EventsInjector.injectEvent(id, EV_ABS, ABS_MT_TRACKING_ID, 0x0000);
        EventsInjector.injectEvent(id, EV_ABS, ABS_MT_TOUCH_MAJOR, 0x0020);
        EventsInjector.injectEvent(id, EV_ABS, ABS_MT_PRESSURE, 0x0081);
        EventsInjector.injectEvent(id, EV_ABS, ABS_MT_POSITION_X, x);
        EventsInjector.injectEvent(id, EV_ABS, ABS_MT_POSITION_Y, y);
        EventsInjector.injectEvent(id, EV_SYN, SYN_REPORT, 0x0000);
        return 0;
    }

    public int sendTouchDownRel(double x, double y, int screenW, int screenH) {
        int xAbs = 0;
        int yAbs = 0;
        xAbs = (int) (screenW * x);
        yAbs = (int) (screenH * y);
        return this.sendTouchDownAbs(xAbs, yAbs);
    }

    public int sendTouchUp() {
        EventsInjector.injectEvent(this.id, EV_ABS, ABS_MT_PRESSURE, 0);
        EventsInjector.injectEvent(this.id, EV_ABS, ABS_MT_TRACKING_ID, -1);
        EventsInjector.injectEvent(this.id, EV_SYN, SYN_REPORT, 0);
        return 0;
    }
}


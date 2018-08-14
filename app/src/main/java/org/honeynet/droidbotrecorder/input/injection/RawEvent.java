package org.honeynet.droidbotrecorder.input.injection;

/**
 * Created by anant on 13/8/18.
 */

public class RawEvent {
    private int type;
    private int code;
    private long value;
    private long timeInMillis;

    public RawEvent(int type, int code, int value) {
        this.type = type;
        this.code = code;
        this.value = value;
        this.timeInMillis = System.currentTimeMillis();
    }

    public int getType() {
        return type;
    }

    public int getCode() {
        return code;
    }

    public long getValue() {
        return value;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }
}

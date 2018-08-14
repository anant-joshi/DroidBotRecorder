package org.honeynet.droidbotrecorder.serialization;

/**
 * Created by anant on 14/8/18.
 */

public class InnerLongTouch extends InnerTouch{
    private long duration;

    public InnerLongTouch(int x, int y, SerializedView view, long duration) {
        super(x, y, view);
        this.duration = duration;
        this.eventType = "LongTouchEvent";
    }
}

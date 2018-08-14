package org.honeynet.droidbotrecorder.serialization;

/**
 * Created by anant on 7/8/18.
 */

public class TouchEvent extends InnerEvent {
    private long x;
    private long y;
    private SerializedView view;

    public TouchEvent(long x, long y, SerializedView view) {
        this.x = x;
        this.y = y;
        this.view = view;
    }
}

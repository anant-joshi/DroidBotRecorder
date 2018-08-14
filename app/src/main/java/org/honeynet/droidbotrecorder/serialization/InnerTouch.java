package org.honeynet.droidbotrecorder.serialization;

/**
 * Created by anant on 14/8/18.
 */

public class InnerTouch extends InnerEvent{
    private int x;
    private int y;
    private SerializedView view;

    public InnerTouch(int x, int y, SerializedView view) {
        this.x = x;
        this.y = y;
        this.view = view;
        this.eventType = "TouchEvent";
    }
}

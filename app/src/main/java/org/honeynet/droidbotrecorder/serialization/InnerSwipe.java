package org.honeynet.droidbotrecorder.serialization;

/**
 * Created by anant on 14/8/18.
 */

public class InnerSwipe extends InnerEvent{
    private int startX;
    private int startY;
    private int endX;
    private int endY;
    private SerializedView startView;
    private SerializedView endView;
    private long duration;

    public InnerSwipe(int startX, int startY, int endX, int endY, SerializedView startView, SerializedView endView, long duration) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.startView = startView;
        this.endView = endView;
        this.duration = duration;
        this.eventType = "SwipeEvent";
    }
}

package org.honeynet.droidbotrecorder.serialization;

/**
 * Created by anant on 14/8/18.
 */

public class InnerScroll extends InnerEvent{
    private int x;
    private int y;
    private SerializedView view;
    private String direction;

    public InnerScroll(int x, int y, SerializedView view, String direction) {
        this.x = x;
        this.y = y;
        this.view = view;
        this.direction = direction;
    }
}

package org.honeynet.droidbotrecorder.input;

/**
 * Created by anant on 13/8/18.
 */

public class TouchEventBuilder {
    private boolean isDown;
    private long x;
    private long y;

    public TouchEventBuilder(){}

    public void setIsDown(boolean down) {
        isDown = down;
    }

    public void setX(long x) {
        this.x = x;
    }

    public void setY(long y) {
        this.y = y;
    }

    public TouchEvent build(){
        return new TouchEvent(isDown, x, y);
    }
}

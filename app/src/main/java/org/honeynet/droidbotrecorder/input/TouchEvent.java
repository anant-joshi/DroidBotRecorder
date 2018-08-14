package org.honeynet.droidbotrecorder.input;


public class TouchEvent {
    private boolean isDown;
    private long relX;
    private long relY;
    private long timeInMillis;




    public TouchEvent(boolean isDown, long relX, long relY){
        this.isDown = isDown;
        this.relX = relX;
        this.relY = relY;
        this.timeInMillis = System.currentTimeMillis();
    }

    public boolean isDown() {
        return isDown;
    }

    public long getRelX() {
        return relX;
    }

    public long getRelY() {
        return relY;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

}

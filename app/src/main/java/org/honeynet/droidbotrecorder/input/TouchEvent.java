package org.honeynet.droidbotrecorder.input;


public class TouchEvent {
    private boolean isDown;
    private int relX;
    private int relY;
    private long timeInMillis;




    public TouchEvent(boolean isDown, int relX, int relY){
        this.isDown = isDown;
        this.relX = relX;
        this.relY = relY;
        this.timeInMillis = System.currentTimeMillis();
    }

    public boolean isDown() {
        return isDown;
    }

    public int getRelX() {
        return relX;
    }

    public int getRelY() {
        return relY;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

}

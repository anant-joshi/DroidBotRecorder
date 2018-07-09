package org.honeynet.droidbotrecorder.input.web;

import android.support.annotation.NonNull;

/**
 * Created by anant on 11/6/18.
 */

public class InputRequest implements Comparable<InputRequest> {
    private int x;
    private int y;
    private boolean isDown;
    private double timestamp;

    public InputRequest(int x, int y, boolean isDown, double timestamp) {
        this.x = x;
        this.y = y;
        this.isDown = isDown;
        this.timestamp = timestamp;
    }

    public boolean isDown() {
        return isDown;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    @Override
    public int compareTo(@NonNull InputRequest ir) {
        return (this.timestamp > ir.timestamp) ? 1 : -1;
    }
}

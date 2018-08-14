package org.honeynet.droidbotrecorder.serialization;

/**
 * Created by anant on 7/8/18.
 */

public class InputEvent {
    protected String startState;
    protected String stopState;
    protected String tag;
    protected InnerEvent event;

    public InputEvent(String startState, String stopState, String tag, InnerEvent event) {
        this.startState = startState;
        this.stopState = stopState;
        this.tag = tag;
        this.event = event;
    }
}


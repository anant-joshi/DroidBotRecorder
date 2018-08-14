package org.honeynet.droidbotrecorder;


import android.view.accessibility.AccessibilityNodeInfo;

import org.honeynet.droidbotrecorder.input.TouchEvent;
import org.honeynet.droidbotrecorder.serialization.State;

import java.util.ArrayList;
import java.util.List;

public class ActivityLog {

    private static final ActivityLog ourInstance = new ActivityLog();
    public List<AccessibilityNodeInfo> viewStateList;
    public List<String> activityList;
    public List<TouchEvent> touchEventList;
    public List<State> deviceStateList;


    private ActivityLog() {
        this.viewStateList = new ArrayList<>();
        this.activityList = new ArrayList<>();
        this.touchEventList = new ArrayList<>();
        this.deviceStateList = new ArrayList<>();
    }


    public static ActivityLog getInstance() {
        return ourInstance;
    }

}

package org.honeynet.droidbotrecorder;


import android.view.accessibility.AccessibilityNodeInfo;

import org.honeynet.droidbotrecorder.input.TouchEvent;

import java.util.ArrayList;
import java.util.List;

public class ActivityLog {
    private static final ActivityLog ourInstance = new ActivityLog();
    public static List<AccessibilityNodeInfo> viewStateList = new ArrayList<>();
    public static List<TouchEvent> touchEventList = new ArrayList<>();
    private static boolean isRecording;
    private static String recordingName;

    private ActivityLog() {
        isRecording = false;
    }

    static ActivityLog getInstance() {
        return ourInstance;
    }

    public static void toggleRecording() {
        isRecording = !isRecording;
    }

    public static boolean isRecording() {
        return isRecording;
    }

    public static void setRecording(boolean isRecording) {
        ActivityLog.isRecording = isRecording;
    }
}

package org.honeynet.droidbotrecorder.input;

import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import org.honeynet.droidbotrecorder.ActivityLog;
import org.honeynet.droidbotrecorder.CorrelationService;

import java.util.List;


public class TouchRecognizer {
    public static final int TYPE_TAP = 0;
    public static final int TYPE_LONG_PRESS = 1;
    public static final int TYPE_SWIPE = 2;
    public static final int TYPE_SCROLL = 3;
    private static final String TAG = "TOUCH_RECOGNIZER";
    private static int currentType = -1;
    private static TouchRecognizer instance = new TouchRecognizer();
    private static AccessibilityNodeInfo lastState;
    private static List<TouchEvent> touchList = ActivityLog.touchEventList;


    private TouchRecognizer() {
        lastState = null;
    }

    public static void setLastState(AccessibilityNodeInfo lastState) {
        TouchRecognizer.lastState = lastState;
    }

    public static TouchRecognizer getInstance() {
        return instance;
    }

    public static void sendEvent(boolean isDown, int relX, int relY) {
        touchList.add(new TouchEvent(isDown, relX, relY));
        if (!isDown) {
            handleTouch();
            Log.v(TAG, "Handled Touch");
        }
    }

    public static void handleTouch() {
        if (touchList.size() > 1) {
            //Touch is up
            if (checkTap()) {
                handleTap();
            } else if (checkLongPress()) {
                handleLongPress();
            } else if (checkScroll()) {
                handleScroll();
            } else if (checkSwipe()) {
                handleSwipe();
            }
        }
    }

    private static boolean checkTap() {
        int lastTouchUpIndex = -1;
        for (int i = 0; i < touchList.size(); i++) {
            if (!touchList.get(i).isDown() && i != touchList.size() - 1) {
                lastTouchUpIndex = i;
            }
        }
        return ((lastTouchUpIndex + 2 == touchList.size() - 1) && (
                touchList.get(touchList.size() - 1).getTimeInMillis()
                        - touchList.get(lastTouchUpIndex + 1).getTimeInMillis() < 200));
    }

    private static boolean checkSwipe() {
        if (touchList.size() < 3) {
            //A swipe requires at least 3 touch events
            return false;
        }
        int lastTouchUpIndex = -1;
        for (int i = 0; i < touchList.size(); i++) {
            if (!touchList.get(i).isDown() && i != touchList.size() - 1) {
                lastTouchUpIndex = i;
            }
        }


        return (!inVicinity(touchList.get(lastTouchUpIndex + 1), touchList.get(touchList.size() - 1)))
                && (
                (
                        touchList.get(touchList.size() - 1).getTimeInMillis()
                                - touchList.get(lastTouchUpIndex + 1).getTimeInMillis()
                ) > 200
        );
    }

    private static boolean isTouchInScrollable(AccessibilityNodeInfo nodeInfo, int absX, int absY) {
        boolean nodeContainsTouch;
        Rect rekt = new Rect();
        nodeInfo.getBoundsInScreen(rekt);
        nodeContainsTouch = rekt.contains(absX, absY);
        boolean anyChildScrollable = false;
        if (nodeInfo.isScrollable()) {
            return nodeContainsTouch;
        } else {
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                anyChildScrollable = anyChildScrollable | isTouchInScrollable(
                        nodeInfo.getChild(i),
                        absX,
                        absY
                );
            }
            return anyChildScrollable & nodeContainsTouch;
        }
    }

    private static boolean checkScroll() {
        boolean result = false;
        if (ActivityLog.viewStateList.size() < 2) {
            return false;
        }
        AccessibilityNodeInfo state = ActivityLog.viewStateList.get(ActivityLog.viewStateList.size() - 1);
        int relX, relY, absX, absY;
        relX = touchList.get(0).getRelX();
        relY = touchList.get(0).getRelY();
        absX = (CorrelationService.displaySize.width() * relX) / 32767;
        absY = (CorrelationService.displaySize.height() * relY) / 32767;
        result = isTouchInScrollable(state, absX, absY);
        return result & checkSwipe();
    }

    private static boolean checkLongPress() {
        TouchEvent keyStone = touchList.get(0);
        for (TouchEvent event : touchList) {
            if (!inVicinity(keyStone, event)) {
                return false;
            }
        }
        return (
                (touchList.get(touchList.size() - 1).getTimeInMillis()
                        - touchList.get(0).getTimeInMillis()) > 500
        );
    }


    private static void handleTap() {
        Log.v(TAG, "Tap! at: " + System.currentTimeMillis());
    }

    private static void handleLongPress() {
    }

    private static void handleScroll() {
        Log.v(TAG, "Scroll! at: " + System.currentTimeMillis());
    }

    private static void handleSwipe() {
        Log.v(TAG, "Swipe! at: " + System.currentTimeMillis());
    }

    private static boolean inVicinity(TouchEvent t1, TouchEvent t2) {
        return (
                (
                        ((double) Math.abs(t1.getRelX() - t2.getRelX())) / t1.getRelX() < 0.02
                ) && (
                        ((double) Math.abs(t1.getRelY() - t2.getRelY())) / t1.getRelY() < 0.02
                )
        );
    }


}

package org.honeynet.droidbotrecorder.input;

import android.view.accessibility.AccessibilityNodeInfo;
import java.util.ArrayList;
import java.util.List;


public class TouchRecognizer {
    public static final int TYPE_TAP = 0;
    public static final int TYPE_LONG_PRESS = 1;
    public static final int TYPE_SWIPE = 2;
    public static final int TYPE_SCROLL = 3;
    private static int currentType = -1;
    private static TouchRecognizer instance = new TouchRecognizer();
    private static List<TouchEvent> touchList;
    private static AccessibilityNodeInfo lastState;


    private TouchRecognizer() {
        touchList = new ArrayList<>();
        lastState = null;
    }

    public static void setLastState(AccessibilityNodeInfo lastState){
        TouchRecognizer.lastState = lastState;
    }

    public static TouchRecognizer getInstance() {
        return instance;
    }

    public static void sendEvent(boolean isDown, int relX, int relY) {
        touchList.add(new TouchEvent(isDown, relX, relY));
        if (!isDown) {
            handleTouch();
        }
    }

    public static void handleTouch() {
        if (!touchList.get(touchList.size() - 1).isDown()) {
            //Touch is up
            if (checkTap()) {
                handleTap();
            } else if (checkLongPress()) {
                handleLongPress();
            } else if (checkScroll()) {
                handleScroll();
            }
        }
    }

    private static boolean checkTap() {
        return touchList.size() == 2 && inVicinity(touchList.get(0), touchList.get(1)) &&
                (touchList.get(0).getTimeInMillis() - touchList.get(1).getTimeInMillis()) < 500;
    }

//    private static boolean checkScroll() {
//        TouchEvent first = touchList.get(0);
//        TouchEvent last = touchList.get(touchList.size() - 1);
//        double directionX = last.getRelX() - first.getRelX();
//        double directionY = last.getRelY() - first.getRelY();
//        double magnitude = Math.sqrt(Math.pow(directionX, 2) + Math.pow(directionY, 2));
//        double direction = Math.atan2(directionY, directionX);
//        for (TouchEvent event: touchList) {
//        }
//        return false;
//    }

    private static boolean checkScroll(){
        return false;
    }

    private static boolean checkLongPress() {
        TouchEvent keyStone = touchList.get(0);
        for (TouchEvent event : touchList) {
            if (!inVicinity(keyStone, event)) {
                return false;
            }
        }
        return (
                touchList.get(touchList.size() - 1).getTimeInMillis()
                        - touchList.get(0).getTimeInMillis()
        ) > 500;
    }


    private static void handleTap() {
    }

    private static void handleLongPress() {
    }

    private static void handleScroll() {
    }

    private static void handleSwipe() {
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

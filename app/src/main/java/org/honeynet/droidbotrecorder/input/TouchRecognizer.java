package org.honeynet.droidbotrecorder.input;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.honeynet.droidbotrecorder.ActivityLog;
import org.honeynet.droidbotrecorder.CorrelationService;
import org.honeynet.droidbotrecorder.serialization.InnerLongTouch;
import org.honeynet.droidbotrecorder.serialization.InnerSwipe;
import org.honeynet.droidbotrecorder.serialization.InnerTouch;
import org.honeynet.droidbotrecorder.serialization.InputEvent;
import org.honeynet.droidbotrecorder.serialization.SerializationUtils;
import org.honeynet.droidbotrecorder.serialization.SerializedView;
import org.honeynet.droidbotrecorder.serialization.State;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.List;


public class TouchRecognizer {
    public static final int TYPE_TAP = 0;
    public static final int TYPE_LONG_PRESS = 1;
    public static final int TYPE_SWIPE = 2;
    public static final int TYPE_SCROLL = 3;
    private static final String TAG = "TOUCH_RECOGNIZER";
    private static TouchRecognizer instance = new TouchRecognizer();
    private int currentType = -1;
    private AccessibilityNodeInfo lastState;
    private List<TouchEvent> touchList = ActivityLog.getInstance().touchEventList;
    private List<AccessibilityNodeInfo> viewList = ActivityLog.getInstance().viewStateList;
    private List<State> stateList = ActivityLog.getInstance().deviceStateList;
    private Context context;


    private TouchRecognizer() {
        lastState = null;
    }

    public static TouchRecognizer getInstance(Context context) {
        instance.context = context;
        return instance;
    }

    public static TouchEvent relToAbs(long relX, long relY) {
        int absX, absY;
        absX = (int) (CorrelationService.displaySize.width() * relX) / 32767;
        absY = (int) (CorrelationService.displaySize.height() * relY) / 32767;
        return new TouchEvent(false, absX, absY);
    }

    public void setLastState(AccessibilityNodeInfo lastState) {
        this.lastState = lastState;
    }

    public void sendEvent(boolean isDown, int relX, int relY) {
        touchList.add(new TouchEvent(isDown, relX, relY));
        if (!isDown) {
            handleTouch();
            Log.v(TAG, "Handled Touch");
        }
    }

    public void handleTouch() {
        if (touchList.size() > 1 && !touchList.get(touchList.size() - 1).isDown()) {
            //Touch is up
            int i = 1;
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

    private boolean checkTap() {
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

    private boolean checkSwipe() {
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


        return (
                !inVicinity(touchList.get(lastTouchUpIndex + 1), touchList.get(touchList.size() - 1))
        ) && (
                (
                        touchList.get(touchList.size() - 1).getTimeInMillis()
                                - touchList.get(lastTouchUpIndex + 1).getTimeInMillis()
                ) > 200
        );
    }

    private boolean isTouchInScrollable(AccessibilityNodeInfo nodeInfo, int absX, int absY) {
        boolean nodeContainsTouch;
        Rect rekt = new Rect();
        if (nodeInfo == null) return false;
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

    private boolean checkScroll() {
        boolean result = false;
        Log.v("TouchRecognizer", "Checking Scroll");
        if (viewList.size() < 2) {
            return false;
        }
        AccessibilityNodeInfo state = viewList.get(viewList.size() - 1);
        long relX, relY;
        int absX, absY;
        relX = touchList.get(0).getRelX();
        relY = touchList.get(0).getRelY();
        TouchEvent abs = TouchRecognizer.relToAbs(relX, relY);
        absX = (int) abs.getRelX();
        absY = (int) abs.getRelY();
        result = isTouchInScrollable(state, absX, absY);
        Log.v("TouchRecognizer", "Touch is " + ((result) ? "" : "not ") + "in scrollable");
        return result & checkSwipe();
    }

    private boolean checkLongPress() {
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


    private SerializedView getInnerMostView(List<SerializedView> views, int absX, int absY) {
        Rect innerBounds = CorrelationService.displaySize;
        SerializedView innerMost = views.get(0);
        for (SerializedView view : views) {
            Rect viewBounds = view.getBounds();
            if (viewBounds.contains(absX, absY) && innerBounds.contains(viewBounds)) {
                innerBounds = viewBounds;
                innerMost = view;
            }
        }
        return innerMost;
    }

    private void writeEventToFile(InputEvent event, String filename){
        File sdcard = context.getExternalFilesDir(null);
        File path = new File(sdcard.getAbsolutePath()+"/run_"+CorrelationService.runId+"/events/");
        if(!path.mkdirs()){
            Log.e("ERROR", path.getAbsolutePath());
//            throw new RuntimeException("Error, unable to create folder");
        }
        File file = new File(path, filename);
        try {
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            Gson gson = new GsonBuilder().setPrettyPrinting().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
            Type type = new TypeToken<InputEvent>(){}.getType();
            writer.print(gson.toJson(event));
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            Log.e("WRITE_NODE_INFO", e.getLocalizedMessage());
            Log.e("WRITE_NODE_INFO", e.getMessage());
            e.printStackTrace();
            Log.e("WRITE_NODE_INFO", e.getClass().toString());
        }
    }



    private void handleTap() {
        Log.v(TAG, "Tap! at: " + System.currentTimeMillis());
//        ActivityLog.touchEventList.clear();
        TouchEvent rel = touchList.get(touchList.size() - 1);
        TouchEvent abs = relToAbs(rel.getRelX(), rel.getRelY());
        SerializedView view = getInnerMostView(
                stateList.get(stateList.size() - 1).views,
                (int) abs.getRelX(),
                (int) abs.getRelY()
        );
        InnerTouch inner = new InnerTouch((int) abs.getRelX(), (int) abs.getRelY(), view);
        touchList.clear();
        InputEvent event = new InputEvent(
                stateList.get(0).stateStr, stateList.get(stateList.size() - 1).stateStr, SerializationUtils.getTag(), inner);
        writeEventToFile(event, "event_"+System.currentTimeMillis()+".json");
    }

    private void handleLongPress() {
        //Handle LongPress here
        TouchEvent rel = touchList.get(touchList.size() - 1);
        TouchEvent abs = relToAbs(rel.getRelX(), rel.getRelY());
        SerializedView view = getInnerMostView(
                ActivityLog.getInstance().deviceStateList.get(stateList.size() - 1).views,
                (int) abs.getRelX(),
                (int) abs.getRelY()
        );
        long duration = touchList.get(touchList.size() - 1).getTimeInMillis() - touchList.get(0).getTimeInMillis();
        InnerLongTouch inner = new InnerLongTouch((int) abs.getRelX(), (int) abs.getRelY(), view, duration);
        touchList.clear();
        InputEvent event = new InputEvent(
                stateList.get(0).stateStr, stateList.get(stateList.size() - 1).stateStr, SerializationUtils.getTag(), inner);

        writeEventToFile(event, "event_"+System.currentTimeMillis()+".json");

    }


    private void handleScroll() {
        Log.v(TAG, "Scroll! at: " + System.currentTimeMillis());
//        ActivityLog.touchEventList.clear();
        touchList.clear();
    }

    private void handleSwipe() {
        Log.v(TAG, "Swipe! at: " + System.currentTimeMillis());
//        ActivityLog.touchEventList.clear();
        TouchEvent startTouch = touchList.get(0);
        TouchEvent absStartTouch = relToAbs(startTouch.getRelX(), startTouch.getRelY());
        TouchEvent endTouch = touchList.get(touchList.size() - 1);
        TouchEvent absEndTouch = relToAbs(endTouch.getRelX(), endTouch.getRelY());
        List<SerializedView> state = stateList.get(stateList.size() - 1).views;
        SerializedView startView = getInnerMostView(state, (int) startTouch.getRelX(), (int) startTouch.getRelY());
        SerializedView endView = getInnerMostView(state, (int) endTouch.getRelX(), (int) endTouch.getRelY());
        long duration = touchList.get(touchList.size() - 1).getTimeInMillis() - touchList.get(0).getTimeInMillis();
        InnerSwipe inner = new InnerSwipe(
                (int) startTouch.getRelX(),
                (int) startTouch.getRelY(),
                (int) endTouch.getRelX(),
                (int) endTouch.getRelY(),
                startView,
                endView,
                duration
        );
        touchList.clear();
        InputEvent event = new InputEvent(
                stateList.get(0).stateStr, stateList.get(stateList.size() - 1).stateStr, SerializationUtils.getTag(), inner);
        writeEventToFile(event, "event_"+System.currentTimeMillis()+".json");
    }

    private boolean inVicinity(TouchEvent t1, TouchEvent t2) {
        return (
                (
                        ((double) Math.abs(t1.getRelX() - t2.getRelX())) / t1.getRelX() < 0.02
                ) && (
                        ((double) Math.abs(t1.getRelY() - t2.getRelY())) / t1.getRelY() < 0.02
                )
        );
    }


}

package org.honeynet.droidbotrecorder;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.honeynet.droidbotrecorder.input.TouchEventBuilder;
import org.honeynet.droidbotrecorder.input.TouchRecognizer;
import org.honeynet.droidbotrecorder.input.injection.EventsInjector;
import org.honeynet.droidbotrecorder.input.injection.InputDevice;
import org.honeynet.droidbotrecorder.input.injection.RawEvent;
import org.honeynet.droidbotrecorder.serialization.SerializationUtils;
import org.honeynet.droidbotrecorder.serialization.SerializedView;
import org.honeynet.droidbotrecorder.serialization.State;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.github.privacystreams.accessibility.AccEvent;
import io.github.privacystreams.core.Callback;
import io.github.privacystreams.core.Item;
import io.github.privacystreams.core.UQI;
import io.github.privacystreams.core.purposes.Purpose;

public class CorrelationService extends IntentService {

    public static final String ACTION_INIT = "org.honeynet.droidbotrecorder.layout.action.ACTION_INIT";
    public static final String ACTION_TOUCH_INPUT = "org.honeynet.droidbotrecorder.layout.action.ACTION_TOUCH_INPUT";
    public static final String ACTION_LAYOUT_DUMP = "org.honeynet.droidbotrecorder.layout.action.ACTION_LAYOUT_DUMP";
    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";
    private static final int FOREGROUND_ID = 182752;
    public static volatile AccessibilityNodeInfo lastState = null;
    public static volatile AccessibilityNodeInfo latestState = null;
    public static Rect displaySize;
    public static String currentActivity = "[unknown]";
    public static String runId;



    public CorrelationService() {
        super("CorrelationService");
        runId = SerializationUtils.getMd5Str("DroidbotRecorder"+Math.random());
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */

    public static void initialize(Context context) {
        Intent intent = new Intent(context, CorrelationService.class);
        intent.setAction(ACTION_INIT);
        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        try{
            Display display = window.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int right = size.x;
            int bottom = size.y;
            displaySize = new Rect(0, 0, right, bottom);
            context.startService(intent);
        }catch(NullPointerException npe){
            Log.e("CorrelationService", npe.getMessage());
            npe.printStackTrace();
        }
    }

    private static void showNodeInfo(AccessibilityNodeInfo nodeInfo, int indentLevel) {
        StringBuilder paddingBuilder = new StringBuilder();
        for (int i = 0; i < indentLevel; i++) paddingBuilder.append("\t");
        String padding = paddingBuilder.toString();
        String paddingMinus1 = padding.substring(1);
        Rect outBounds = new Rect();
        nodeInfo.getBoundsInScreen(outBounds);
        Log.v("SHOW_NODE_INFO", paddingMinus1 + "{");
        Log.v(
                "SHOW_NODE_INFO",
                padding
                        + "ContentFreeSignature : "
                        + "\"[class]"
                        + nodeInfo.getClassName()
                        + "[resource_id]"
                        + nodeInfo.getViewIdResourceName()
                        + "\"\n"
        );
        Log.v("SHOW_NODE_INFO", padding + "\"PackageName\" : \"" + nodeInfo.getPackageName() + "\"\n");
        Log.v("SHOW_NODE_INFO", padding + "\"Bounds\" : \"" + outBounds.toString() + "\"\n");
        Log.v("SHOW_NODE_INFO", padding + "\"isScrollable\" : \"" + nodeInfo.isScrollable() + "\"\n");
        if (nodeInfo.getChildCount() > 0) {
            Log.v("SHOW_NODE_INFO", padding + "\"Children\" : [\n");
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                showNodeInfo(nodeInfo.getChild(i), indentLevel + 2);
            }
            Log.v("SHOW_NODE_INFO", padding + "]\n");
        }
        Log.v("SHOW_NODE_INFO", paddingMinus1 + "},\n");
    }

    private void writeNodeInfoToFile(AccessibilityNodeInfo rootNode, String filename, Context context) {
        File sdcard = context.getExternalFilesDir(null);
        File path = new File(sdcard.getAbsolutePath()+"/run_"+runId+"/states/");
        if(!path.mkdirs()){
            Log.e("ERROR", path.getAbsolutePath());
//            throw new RuntimeException("Error, unable to create folder");
        }
        File file = new File(path, filename);
        try {
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            String writeValue = writeNodeInfo(rootNode);
            writer.print(writeValue);
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            Log.e("WRITE_NODE_INFO", e.getLocalizedMessage());
            Log.e("WRITE_NODE_INFO", e.getMessage());
            e.printStackTrace();
            Log.e("WRITE_NODE_INFO", e.getClass().toString());
        }
    }

    private void serializeNodeInfo(List<SerializedView> nodeList, AccessibilityNodeInfo nodeInfo, int parentIndex){
        Rect bounds = new Rect();
        if(nodeInfo == null){
            return;
        }
        nodeInfo.getBoundsInScreen(bounds);
        SerializedView view = new SerializedView(
                nodeInfo.getViewIdResourceName(),
                (String) nodeInfo.getContentDescription(),
                (nodeInfo.getText() == null)?"":nodeInfo.getText().toString(),
                nodeInfo.isVisibleToUser(),
                nodeInfo.isCheckable(),
                nodeInfo.isChecked(),
                nodeInfo.isSelected(),
                nodeInfo.getChildCount(),
                nodeInfo.isPassword(),
                parentIndex,
                nodeInfo.isFocusable(),
                nodeInfo.isEditable(),
                nodeInfo.isFocused(),
                nodeInfo.isClickable(),
                (String) nodeInfo.getClassName(),
                nodeInfo.isScrollable(),
                nodeInfo.isLongClickable(),
                "",
                nodeInfo.isEnabled(),
                bounds,
                new ArrayList<Integer>()
        );
        int index = nodeList.size();
        if(parentIndex >= 0){
            SerializedView parent = nodeList.get(parentIndex);
            parent.addChildIndex(index);
        }
        nodeList.add(view);
        for(int i = 0; i < nodeInfo.getChildCount(); i++){
            serializeNodeInfo(nodeList, nodeInfo.getChild(i), index);
        }
    }

    private String writeNodeInfo(AccessibilityNodeInfo rootNode){
        List<SerializedView> viewList = new ArrayList<>();
        serializeNodeInfo(viewList, rootNode, -1);
        int activityIndex = ActivityLog.getInstance().activityList.size()-1;
        String activityName = (activityIndex<0)?"[unknown]":ActivityLog.getInstance().activityList.get(activityIndex);
        SerializationUtils.setViewStrs(viewList, activityName);
        Type type = new TypeToken<State>(){}.getType();
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
        State state = new State(viewList, activityName);
        ActivityLog.getInstance().deviceStateList.add(state);
        return gson.toJson(state, type);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action == null) {
                action = "";
            }
            switch (action) {
                case ACTION_INIT: {
                    startForeground(FOREGROUND_ID, buildNotification());
                    handleInitialize();
                }
                break;
            }
        }
    }

    private Thread deviceStateLogger = new Thread() {
        @Override
        public void run() {
            super.run();
            while (!this.isInterrupted()) {
                try {
                    if (!(latestState == null)) {
                        if (!(latestState == lastState)) {
                            Log.v("LOG_DEVICE_STATE", "Writing state at:" + System.currentTimeMillis());
                            writeNodeInfoToFile(latestState, "state_" + System.currentTimeMillis() + ".json", getBaseContext());
                            lastState = latestState;
                        }
                    }
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Log.e("TEST_THREAD", e.getMessage());
                }
            }
        }
    };


    private Thread touchReader = new Thread(){
        @Override
        public void run() {
            super.run();
            EventsInjector injector = new EventsInjector();
            InputDevice device = injector.getTouchScreen();
            if(!device.isOpen()){
                if(!device.open()){
                    throw new RuntimeException("Unable to open device: "+device.getName());
                }
            }
            long lastX = 0;
            long lastY = 0;
            boolean reset = true;
            boolean setIsDown = false;
            TouchEventBuilder touchEventBuilder = new TouchEventBuilder();
            TouchRecognizer touchRecognizer = TouchRecognizer.getInstance(CorrelationService.this);
            while(!this.isInterrupted()){
                RawEvent event = device.getEvent();
                if(reset){
                    reset = false;
                    touchEventBuilder = new TouchEventBuilder();
                }
                switch (event.getType()){
                    case InputDevice.EV_ABS:{
                        switch(event.getCode()){
                            case InputDevice.ABS_MT_TRACKING_ID:{
                                if(event.getValue() < 0){
                                    touchEventBuilder.setIsDown(false);
                                } else {
                                    touchEventBuilder.setIsDown(true);
                                }
                                setIsDown = true;
                            }break;
                            case InputDevice.ABS_MT_POSITION_X:{
                                lastX = event.getValue();
                            }break;
                            case InputDevice.ABS_MT_POSITION_Y:{
                                lastY = event.getValue();
                            }break;
                            default:{}
                        }
                    }break;
                    case InputDevice.EV_SYN:{
                        reset = true;
                        touchEventBuilder.setX(lastX);
                        touchEventBuilder.setY(lastY);
                        if(!setIsDown){
                            touchEventBuilder.setIsDown(true);
                        }
                        ActivityLog.getInstance().touchEventList.add(touchEventBuilder.build());
                        setIsDown = false;
                        if(!ActivityLog.getInstance().touchEventList.get(ActivityLog.getInstance().touchEventList.size()-1).isDown()){
                            touchRecognizer.handleTouch();
                        }
                    }break;
                }
            }
        }
    };

    private void handleInitialize() {
        Context context = this;
        Log.v("CorrelationService", "INIT!!");
        UQI uqi = new UQI(context);
        uqi.getData(AccEvent.asUIActions(), Purpose.FEATURE("Collect Layout from System"))
            .keepChanges()
            .forEach(
                new Callback<Item>() {
                    @Override
                    protected void onInput(Item input) {
                        if (AccEvent.class == input.getClass()) {
                            if(!CorrelationService.this.touchReader.isAlive()){
                                CorrelationService.this.touchReader.start();
                            }
                            if(!CorrelationService.this.deviceStateLogger.isAlive()){
                                CorrelationService.this.deviceStateLogger.start();
                            }
                            AccEvent event = (AccEvent) input;
//                                Log.v("ON_INPUT", "Event recieved at " + System.currentTimeMillis());
                            AccessibilityEvent accessibilityEvent = event.getValueByField(AccEvent.EVENT);
                            if(accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
                                currentActivity = accessibilityEvent.getClassName().toString();
                            }
                            latestState = event.getValueByField(AccEvent.ROOT_NODE);
                            ActivityLog.getInstance().viewStateList.add(latestState);
                        }
                    }
                }
            );
    }

    private Notification buildNotification() {
        NotificationCompat.Builder notificationBuilder = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = new NotificationCompat.Builder(
                    this,
                    createNotificationChannel()
            );

        } else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }

        notificationBuilder
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Activity Recorder")
                .setContentText("Recording all the things!");
        return notificationBuilder.build();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        String channelId = "correlation_service";
        String channelName = "Correlation Service";
        NotificationChannel chan = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_NONE
        );
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(chan);
        return channelId;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

package org.honeynet.droidbotrecorder;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.File;
import java.io.PrintWriter;

import io.github.privacystreams.accessibility.AccEvent;
import io.github.privacystreams.core.Callback;
import io.github.privacystreams.core.Item;
import io.github.privacystreams.core.UQI;
import io.github.privacystreams.core.purposes.Purpose;

public class CorrelationService extends IntentService {

    public static final String ACTION_INIT = "org.honeynet.droidbotrecorder.layout.action.ACTION_INIT";
    public static final String ACTION_TOUCH_INPUT = "org.honeynet.droidbotrecorder.layout.action.ACTION_TOUCH_INPUT";
    public static final String ACTION_LAYOUT_DUMP = "org.honeynet.droidbotrecorder.layout.action.ACTION_LAYOUT_DUMP";
    private static final String AUTHORITY =
            BuildConfig.APPLICATION_ID + ".provider";
    private static final int FOREGROUND_ID = 182752;
    static boolean justOnce = true;
    private AccessibilityEvent prevAccessibilityEvent;

    public CorrelationService() {
        super("CorrelationService");
        prevAccessibilityEvent = null;
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
        context.startService(intent);
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

    private static void writeNodeInfoToFile(AccessibilityNodeInfo rootNode, String filename, Context context) {
        File path = context.getFilesDir();
        File file = new File(path, filename);
        try {
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            writeNodeInfo(rootNode, 1, writer);
            Log.v("FILE_PATH", file.getAbsolutePath());
            writer.close();
        } catch (Exception e) {
            Log.e("WRITE_NODE_INFO", e.getMessage());
        }
    }

    private static void writeBounds(Rect bounds, PrintWriter writer, String padding) {
        writer.println(padding + "\"bounds\" : " + "[");
        writer.println(padding + "\t[");
        writer.println(padding + "\t\t" + bounds.left + ",");
        writer.println(padding + "\t\t" + bounds.top + "");
        writer.println(padding + "\t],");
        writer.println(padding + "\t[");
        writer.println(padding + "\t\t" + bounds.left + ",");
        writer.println(padding + "\t\t" + bounds.top + "");
        writer.println(padding + "\t]");
        writer.println(padding + "],");
    }

    private static Object serializedValue(Object value) {
        if (value instanceof CharSequence) {
            return "\"" + value + "\"";
        } else {
            return value;
        }
    }

    private static void writeNodeInfo(AccessibilityNodeInfo nodeInfo, int indentLevel, PrintWriter writer) {
        StringBuilder paddingBuilder = new StringBuilder();
        for (int i = 0; i < indentLevel; i++) paddingBuilder.append("\t");
        String padding = paddingBuilder.toString();
        String paddingMinus1 = padding.substring(1);
        Rect outBounds = new Rect();
        nodeInfo.getBoundsInScreen(outBounds);
        writer.println(paddingMinus1 + "{");
        writer.println(
                padding +
                        "\"content_description\" : " +
                        serializedValue(nodeInfo.getContentDescription()) +
                        ","
        );
        writer.println(padding + "\"resource_id\" : " + serializedValue(nodeInfo.getViewIdResourceName()) + ",");
        writer.println(padding + "\"text\" : " + serializedValue(nodeInfo.getText()) + ",");
        writer.println(padding + "\"visible\" : " + serializedValue(nodeInfo.isVisibleToUser()) + ",");
        writer.println(padding + "\"checkable\" : " + serializedValue(nodeInfo.isCheckable()) + ",");
        writer.println(padding + "\"size\" : " + serializedValue(
                (outBounds.bottom - outBounds.top) + "*" + (outBounds.right - outBounds.left)
                ) + ","
        );
        writer.println(padding + "\"checked\" : " + serializedValue(nodeInfo.isChecked()) + ",");
        writer.println(padding + "\"selected\" : " + serializedValue(nodeInfo.isSelected()) + ",");
        writer.println(padding + "\"child_count\" : " + serializedValue(nodeInfo.getChildCount()) + ",");
        writer.println(
                padding
                        + "content_free_signature : "
                        + "\"[class]"
                        + nodeInfo.getClassName()
                        + "[resource_id]"
                        + nodeInfo.getViewIdResourceName()
                        + "\","
        );
        writer.println(padding + "\"is_password\" : " + serializedValue(nodeInfo.isPassword()) + ",");
        writer.println(padding + "\"focusable\" : " + serializedValue(nodeInfo.isFocusable()) + ",");
        writer.println(padding + "\"editable\" : " + serializedValue(nodeInfo.isEditable()) + ",");
        writer.println(padding + "\"focused\" : " + serializedValue(nodeInfo.isFocused()) + ",");
        writer.println(padding + "\"clickable\" : " + serializedValue(nodeInfo.isClickable()) + ",");
        writer.println(padding + "\"class\" : " + serializedValue(nodeInfo.getClassName()) + ",");
        writer.println(padding + "\"scrollable\" : " + serializedValue(nodeInfo.isScrollable()) + ",");
        writer.println(padding + "\"package\" : " + serializedValue(nodeInfo.getPackageName()) + ",");
        writer.println(padding + "\"long_clickable\" : " + serializedValue(nodeInfo.isLongClickable()) + ",");
        writer.println(padding + "\"view_str\" : " + serializedValue(getViewStr(nodeInfo)) + ",");
        writer.println(padding + "\"enabled\" : " + serializedValue(nodeInfo.isEnabled()) + ",");
        writeBounds(outBounds, writer, padding);
        if (nodeInfo.getChildCount() > 0) {
            writer.println(padding + "\"children\" : [");
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                writeNodeInfo(nodeInfo.getChild(i), indentLevel + 2, writer);
            }
            writer.println(padding + "],");
        }
        writer.println(padding + "\"signature\" : " + serializedValue(getSignature(nodeInfo)));
        writer.println(paddingMinus1 + "},");
    }

    private static String getSignature(AccessibilityNodeInfo nodeInfo) {
        return "[" +
                ((nodeInfo.isEnabled()) ? "enabled" : "") + "," +
                ((nodeInfo.isChecked()) ? "checked" : "") + "," +
                ((nodeInfo.isSelected()) ? "selected" : "") + "," +
                "]";
    }

    private static String getViewStr(AccessibilityNodeInfo nodeInfo) {
        //TODO: Implement this properly
        return "";
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
//                Toast.makeText(getApplicationContext(), "Started Service", Toast.LENGTH_SHORT).show();
                    handleInitialize();
                }
                break;

                case ACTION_LAYOUT_DUMP: {

                }
                break;

                case ACTION_TOUCH_INPUT: {
                }
                break;
            }
        }
    }

    private void handleInitialize() {
        Context context = this;
        UQI uqi = new UQI(context);
        uqi.getData(
                AccEvent.asWindowChanges(),
                Purpose.FEATURE("Collect Layout from System")
        ).keepChanges().forEach(
                new Callback<Item>() {
                    @Override
                    protected void onInput(Item input) {
                        if (AccEvent.class == input.getClass()) {
                            //TODO: Handle UI update
                            AccEvent event = (AccEvent) input;
                            if (!(
                                    event.getValueByField(AccEvent.PACKAGE_NAME).equals(
                                            "com.android.systemui"
                                    )
                                    //                                || event.getValueByField(AccEvent.EVENT_TYPE).equals(
                                    //                                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                                    //                                )
                            )) {
                                AccessibilityEvent accessibilityEvent = event.getValueByField(AccEvent.EVENT);

                                Log.v("EVENT_TYPE: ",
                                        "----" +
                                                AccessibilityEvent.eventTypeToString(
                                                        accessibilityEvent.getEventType()
                                                )
                                                + "----"
                                );
                                Log.v("ACTION: ", MotionEvent.actionToString(accessibilityEvent.getAction()));
                                AccessibilityNodeInfo rootNode = event.getValueByField(AccEvent.ROOT_NODE);
                                writeNodeInfoToFile(rootNode, "state_" + "" + System.currentTimeMillis() + ".json", getBaseContext());
                                if (event.getValueByField(AccEvent.EVENT_TYPE)
                                        .equals(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)) {
                                    int changeType = accessibilityEvent.getContentChangeTypes();
                                    if ((changeType & AccessibilityEvent.CONTENT_CHANGE_TYPE_SUBTREE) != 0) {
                                        //TODO: Handle scroll/change in page, etc.
                                    }
                                } else {
//                                    Log.v("AccessibilityEvent", "event: "+event.toJson());
                                }
                                prevAccessibilityEvent = event.getValueByField(AccEvent.EVENT);
                            }
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
}

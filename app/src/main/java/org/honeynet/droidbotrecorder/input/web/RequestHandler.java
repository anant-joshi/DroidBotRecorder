package org.honeynet.droidbotrecorder.input.web;

import android.util.Log;

import org.honeynet.droidbotrecorder.ActivityLog;
import org.honeynet.droidbotrecorder.input.TouchEvent;
import org.honeynet.droidbotrecorder.input.TouchRecognizer;
import org.honeynet.droidbotrecorder.input.injection.EventsInjector;
import org.honeynet.droidbotrecorder.input.injection.InputDevice;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by anant on 11/6/18.
 */

public class RequestHandler implements Runnable {
    private EventsInjector injector;
    private InputDevice touchScreen;
    private PriorityBlockingQueue<InputRequest> requestQueue;
    private InputRequest latestRequest;
    private InputRequest lastRequest;
    private Thread inputRequestLogger;

    public RequestHandler(PriorityBlockingQueue<InputRequest> queue) {
        this.requestQueue = queue;
        if (injector == null) {
            injector = new EventsInjector();
            injector.enableDebug(false);
        }
        if (touchScreen == null) {
            touchScreen = injector.getTouchScreen();
            touchScreen.open();
        }
        latestRequest = null;
        lastRequest = null;
        inputRequestLogger = new Thread() {
            @Override
            public void run() {
                super.run();
                while (!inputRequestLogger.isInterrupted()) {
                    if (!(latestRequest == null)) {
                        if (lastRequest != latestRequest) {
                            //TODO: Log touch event
//                            TouchRecognizer.sendEvent(latestRequest.isDown(), latestRequest.getX(), latestRequest.getY());
//                            TouchRecognizer.handleTouch();
                            Log.v(
                                    "REQUEST_HANDLER",
                                    "Touch event logged at " + System.currentTimeMillis()

                            );
                            lastRequest = latestRequest;
                        }
                    }
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        Log.e("REQUEST_HANDLER", e.getMessage());
                    }
                }
            }
        };
        inputRequestLogger.start();
    }

    public void close() {
        if (touchScreen.isOpen()) {
            touchScreen.close();
        }
        touchScreen = null;
    }

    @Override
    public void run() {
        if (!touchScreen.isOpen()) {
            touchScreen.open();
        }
        while (true) {
            if (touchScreen == null) {
                Log.v("RequestHandler", "null");
                break;
            }
            try {
                Log.v("RequestHandler", "SanityCheck" + Math.random());
                InputRequest request = requestQueue.take();
                latestRequest = request;
                ActivityLog.touchEventList.add(new TouchEvent(latestRequest.isDown(), latestRequest.getX(), latestRequest.getY()));
                if (request.isDown()) {
                    touchScreen.sendTouchDownRel(request.getX(), request.getY());
                    Log.v("RequestHandler", "down!");
                } else {
                    touchScreen.sendTouchUp();
                    TouchRecognizer.handleTouch();
                }
            } catch (InterruptedException e) {
                Log.e("RequestHandler", e.getMessage());
                Log.e("RequestHandler", injector.toString());
                Log.e("RequestHandler", touchScreen.toString());
            }
        }
    }
}

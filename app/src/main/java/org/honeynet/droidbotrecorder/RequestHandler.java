package org.honeynet.droidbotrecorder;

import android.util.Log;

import org.honeynet.droidbotrecorder.injection.EventsInjector;
import org.honeynet.droidbotrecorder.injection.InputDevice;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by anant on 11/6/18.
 */

public class RequestHandler implements Runnable {
    private EventsInjector injector = null;
    private InputDevice touchScreen = null;
    private PriorityBlockingQueue<InputRequest> requestQueue;

    public RequestHandler(PriorityBlockingQueue<InputRequest> queue) {
        this.requestQueue = queue;
        if (injector == null) {
            injector = new EventsInjector();
        }
        if (touchScreen == null) {
            touchScreen = injector.getTouchScreen();
            touchScreen.open();
        }
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
                if (request.isDown()) {
                    touchScreen.sendTouchDownRel(request.getX(), request.getY());
                    Log.v("RequestHandler", "down!");
                } else {
                    touchScreen.sendTouchUp();
                }
            } catch (InterruptedException e) {
                Log.e("RequestHandler", e.getMessage());
                Log.e("RequestHandler", injector.toString());
                Log.e("RequestHandler", touchScreen.toString());
            }
        }
    }
}

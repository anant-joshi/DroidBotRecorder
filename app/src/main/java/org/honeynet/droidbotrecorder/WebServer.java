package org.honeynet.droidbotrecorder;

import android.content.Context;
import android.util.Log;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by anant on 11/6/18.
 */

public class WebServer implements Runnable {
    private PriorityBlockingQueue<InputRequest> queue;
    private AsyncHttpServer server;
    private Context context;
    private List<WebSocket> webSockets;

    public WebServer(PriorityBlockingQueue<InputRequest> requestQueue, Context cntxt) {
        this.queue = requestQueue;
        this.context = cntxt;
        server = new AsyncHttpServer();
        webSockets = new ArrayList<>();

        server.get("/", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                try {
                    InputStream stream = context.getResources().openRawResource(R.raw.test);
                    Scanner sc = new Scanner(stream);
                    StringBuilder stringBuilder = new StringBuilder();
                    while (sc.hasNextLine()) {
                        stringBuilder.append(sc.nextLine());
                        stringBuilder.append("\n");
                    }
                    sc.close();
                    response.send(stringBuilder.toString());
                    return;
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage());
                }
                response.send("");
            }
        });

        server.websocket("/input", new AsyncHttpServer.WebSocketRequestCallback() {
            @Override
            public void onConnected(final WebSocket webSocket, AsyncHttpServerRequest request) {
                webSockets.add(webSocket);
                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        try {
                            if (ex != null) {
                                Log.e("Websocket", "Error");
                            }
                        } finally {
                            webSockets.remove(webSocket);
                        }
                    }
                });
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(String s) {
                        try {
                            JSONObject input = new JSONObject(s);
                            boolean isDown = input.getBoolean("type");
                            if (isDown) {
                                int x = (int) input.getDouble("x");
                                int y = (int) input.getDouble("y");
                                double timestamp = input.getDouble("timestamp");
                                queue.add(new InputRequest(x, y, isDown, timestamp));
                            } else {
                                double timestamp = input.getDouble("timestamp");
                                queue.add(new InputRequest(-1, -1, isDown, timestamp));
                            }
                        } catch (JSONException e) {
                            Log.e("WebSocket", e.getMessage());
                        }
                    }
                });
            }
        });
    }


    @Override
    public void run() {
        server.listen(58008);
    }

    public void close() {
        server.stop();
    }
}

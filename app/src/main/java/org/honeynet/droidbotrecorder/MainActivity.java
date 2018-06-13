package org.honeynet.droidbotrecorder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.concurrent.PriorityBlockingQueue;

public class MainActivity extends AppCompatActivity {

    RequestHandler handler;
    WebServer webServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PriorityBlockingQueue<InputRequest> queue = new PriorityBlockingQueue<>();
        this.webServer = new WebServer(queue, this);
        this.handler = new RequestHandler(queue);
        this.webServer.run();
        this.handler.run();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.handler.close();
        this.webServer.close();
    }
}

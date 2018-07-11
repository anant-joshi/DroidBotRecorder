package org.honeynet.droidbotrecorder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.honeynet.droidbotrecorder.input.web.InputRequest;
import org.honeynet.droidbotrecorder.input.web.RequestHandler;
import org.honeynet.droidbotrecorder.input.web.WebServer;

import java.util.concurrent.PriorityBlockingQueue;

public class MainActivity extends AppCompatActivity {

    RequestHandler handler;
    WebServer webServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PriorityBlockingQueue<InputRequest> queue = new PriorityBlockingQueue<>();
        CorrelationService.initialize(this);
        this.webServer = new WebServer(queue, this);
        this.handler = new RequestHandler(queue);
        this.webServer.run();
        this.handler.run();
//        Intent startServiceIntent = new Intent(this, CorrelationService.class);
//        startServiceIntent.setAction(CorrelationService.ACTION_INIT);
//        startService(startServiceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.handler.close();
        this.webServer.close();
    }
}

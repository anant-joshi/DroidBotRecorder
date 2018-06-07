package org.honeynet.droidbotrecorder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.honeynet.droidbotrecorder.injection.EventsInjector;
import org.honeynet.droidbotrecorder.injection.InputDevice;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventsInjector injector = new EventsInjector();
        Log.v("TestInject", "Trying to init");
        injector.enableDebug(true);
        int numDevices = injector.init();
        Log.v("TestInject", "Number of devices: " + numDevices);
        List<InputDevice> inputDevices = injector.inputDevices;
        InputDevice testDevice = inputDevices.get(0);
        Log.v("TestInject", "Trying to open the first device");
        boolean isOpen = testDevice.open(true);
        Log.v("TestInject", "The device is " + ((isOpen) ? "" : "not ") + "open");
        Log.v("TestInject", "Trying to close device");
        testDevice.close();
        Log.v("TestInject", "Closed Device");
    }
}

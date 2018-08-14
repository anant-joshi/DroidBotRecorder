package org.honeynet.droidbotrecorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.honeynet.droidbotrecorder.input.TouchEvent;
import org.honeynet.droidbotrecorder.input.TouchEventBuilder;
import org.honeynet.droidbotrecorder.input.injection.EventsInjector;
import org.honeynet.droidbotrecorder.input.injection.InputDevice;

import org.honeynet.droidbotrecorder.input.injection.RawEvent;
import org.honeynet.droidbotrecorder.input.web.WebServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int intValue(String str){
        int intValue = 0;
        for(int i = 0; i < str.length(); i++){
            intValue = intValue*64+((int) str.charAt(i));
        }
        return intValue;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Intent startServiceIntent = new Intent(this, CorrelationService.class);
//        startServiceIntent.setAction(CorrelationService.ACTION_INIT);
//        startService(startServiceIntent);
        CorrelationService.initialize(this);
    }

    private JSONObject deviceToJson(InputDevice device) throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", device.getId());
        jsonObject.put("name", device.getName());
        jsonObject.put("path", device.getPath());
        jsonObject.put("open", device.isOpen());
        return jsonObject;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        this.handler.close();
//        this.webServer.close();
    }
}

package org.honeynet.droidbotrecorder.serialization;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anant on 12/8/18.
 */

public class State {
    public List<SerializedView> getViews() {
        return views;
    }

    public List<SerializedView> views;
    public String stateStrContentFree;
    public String stateStr;
    public String tag;
    public String foregroundActivity;



    public State(List<SerializedView> views, String activityName) {
        this.views = views;
        List<String> stateStrContentFree = new ArrayList<>();
        List<String> stateStr = new ArrayList<>();
        this.foregroundActivity = activityName;
        this.tag = SerializationUtils.getTag();

        for(SerializedView view: views){
            stateStr.add(view.getViewStr());
            stateStrContentFree.add(view.getContentFreeSignature());
        }
        this.stateStr = SerializationUtils.getMd5Str(activityName + "{" + SerializationUtils.listToStr(stateStr) +"}");
        this.stateStrContentFree = SerializationUtils.getMd5Str(activityName + "{" + SerializationUtils.listToStr(stateStrContentFree) +"}");
    }
}

package org.honeynet.droidbotrecorder.serialization;

import android.view.accessibility.AccessibilityNodeInfo;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by anant on 14/8/18.
 */

public class SerializationUtils {

    public static String getMd5Str(String string){
        try{
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(StandardCharsets.UTF_8.encode(string));
            return String.format("%032x", new BigInteger(1, md5.digest()));
        }catch (NoSuchAlgorithmException e){return "";}
    }

    static String listToStr(List list){
        StringBuilder retval = new StringBuilder();
        for(Object obj:list){
            retval = retval.append(obj.toString()).append(",");
        }
        String ret = retval.toString();
        ret = ret.substring(0, ret.length()-1);
        return ret;
    }

    public static String getTag(){
         return (new SimpleDateFormat("yyyy-MM-dd_HHmmss")).format(new Date());
    }

    private static Collection<Integer> getParents(int index, List<SerializedView> views){
        LinkedHashSet<Integer> parents = new LinkedHashSet<>();
       if(!(index < 0 || index >= views.size())){
           SerializedView view = views.get(index);
           int parent = view.getParent();
           parents.add(parent);
           parents.addAll(getParents(parent, views));
       }
       return parents;
    }

    private static Collection<Integer> getChildren(int index, List<SerializedView> views){
        LinkedHashSet<Integer> children = new LinkedHashSet<>();
        if(!(index < 0 || index >= views.size())){
            SerializedView view = views.get(index);
            List<Integer> directChildren = view.getChildren();
            children.addAll(directChildren);
            for(Integer childIndex: directChildren){
                children.addAll(getChildren(childIndex, views));
            }
        }
        return children;
    }

    public static void setViewStrs(List<SerializedView> views, String activityName){
        for(int i = 0; i < views.size(); i++){
            List<Integer> ancestors = new ArrayList<>(getParents(i, views));
            Collections.reverse(ancestors);
            List<Integer> children = new ArrayList<>(getChildren(i, views));
            Collections.sort(children);
            StringBuilder childrenBuilder = new StringBuilder();
            StringBuilder ancestorBuilder = new StringBuilder();
            for(Integer child: children){
                if(child >=0 && child < views.size())
                    childrenBuilder.append(views.get(child).getSignature()).append("||");
            }

            for(Integer ancestor: ancestors){
                if(ancestor >=0 && ancestor < views.size())
                    ancestorBuilder.append(views.get(ancestor).getSignature()).append("//");
            }

            String childrenStr = childrenBuilder.toString();
            String ancestorStr = ancestorBuilder.toString();
            if(childrenStr.length()>=2)
                childrenStr = childrenStr.substring(0, childrenStr.length()-2);
            if(ancestorStr.length()>=2)
                ancestorStr = ancestorStr.substring(0, ancestorStr.length()-2);
            String viewStr = "Activity:" + activityName + "\n"
                    + "Self:" + views.get(i).getSignature() + "\n"
                    + "Parents:" + ancestorStr + "\n"
                    +  "Children:" + childrenStr;
            views.get(i).setViewStr(getMd5Str(viewStr));
        }
    }
}

package org.honeynet.droidbotrecorder.serialization;

import android.graphics.Rect;

import com.google.gson.annotations.SerializedName;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by anant on 8/8/18.
 */

public class SerializedView {
    private String contentDescription;
    private String resourceId;
    private String text;
    private boolean visible;
    private boolean checkable;
    private String size;
    private boolean checked;
    private boolean selected;
    private int childCount;
    private String contentFreeSignature;
    private boolean isPassword;
    private boolean focusable;
    private boolean editable;
    private boolean focused;
    private boolean clickable;
    @SerializedName("class")
    private String className;
    private boolean scrollable;
    private boolean longClickable;
    private boolean enabled;
    private int[][] bounds;
    private String signature;
    private List<Integer> children;

    public void setViewStr(String viewStr) {
        this.viewStr = viewStr;
    }

    private String viewStr;
    private int parent;

    public String getContentFreeSignature() {
        return contentFreeSignature;
    }

    public String getViewStr() {
        return viewStr;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getText() {
        return text;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isCheckable() {
        return checkable;
    }

    public String getSize() {
        return size;
    }

    public boolean isChecked() {
        return checked;
    }

    public boolean isSelected() {
        return selected;
    }

    public int getChildCount() {
        return childCount;
    }

    public boolean isPassword() {
        return isPassword;
    }

    public boolean isFocusable() {
        return focusable;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isFocused() {
        return focused;
    }

    public boolean isClickable() {
        return clickable;
    }

    public String getClassName() {
        return className;
    }

    public boolean isScrollable() {
        return scrollable;
    }

    public boolean isLongClickable() {
        return longClickable;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getSignature() {
        return signature;
    }

    public List<Integer> getChildren() {
        return children;
    }

    public int getParent() {
        return parent;
    }

    public SerializedView(
            String resourceId,
            String contentDescription,
            String text,
            boolean visible,
            boolean checkable,
            boolean checked,
            boolean selected,
            int childCount,
            boolean isPassword,
            int parent,
            boolean focusable,
            boolean editable,
            boolean focused,
            boolean clickable,
            String className,
            boolean scrollable,
            boolean longClickable,
            String viewStr,
            boolean enabled,
            Rect bounds,
            List<Integer> children
    ) {
        this.resourceId = resourceId;
        this.contentDescription = contentDescription;
        this.text = text;
        this.visible = visible;
        this.checkable = checkable;
        this.checked = checked;
        this.selected = selected;
        this.childCount = childCount;
        this.isPassword = isPassword;
        this.parent = parent;
        this.focusable = focusable;
        this.editable = editable;
        this.focused = focused;
        this.clickable = clickable;
        this.className = className;
        this.scrollable = scrollable;
        this.longClickable = longClickable;
        this.viewStr = viewStr;
        this.enabled = enabled;
        this.children = children;
        this.contentFreeSignature = "[class]" + this.className + "[resource_id]" + this.resourceId;
        this.signature =
            contentFreeSignature
            + "["
            + ((this.enabled)?"enabled,":",")
            + ((this.checked)?"checked,":",")
            + ((this.selected)?"selected,":",")
            + "]";

        this.bounds = new int[2][2];
        this.bounds[0][0] = bounds.left;
        this.bounds[0][1] = bounds.top;
        this.bounds[1][0] = bounds.right;
        this.bounds[1][1] = bounds.bottom;
        this.size = (bounds.bottom - bounds.top) + "*" + (bounds.right - bounds.left);
    }

    public Rect getBounds(){
        return new Rect(
                this.bounds[0][0],
                this.bounds[0][1],
                this.bounds[1][0],
                this.bounds[1][1]
        );
    }

    public void addChildIndex(int childIndex){
        this.children.add(childIndex);
    }
}

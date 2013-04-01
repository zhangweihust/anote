package com.android.note.gesture;

import java.util.Set;
import java.util.ArrayList;

public abstract class AmGestureLibrary {
    protected final AmGestureStore mStore;

    protected AmGestureLibrary() {
        mStore = new AmGestureStore();
    }

    public abstract boolean save(boolean flag);
    public abstract boolean load(boolean flag);

    public boolean isReadOnly() {
        return false;
    }

    /** @hide */
    public AmLearner getLearner() {
        return mStore.getLearner();
    }

    public void setOrientationStyle(int style) {
        mStore.setOrientationStyle(style);
    }

    public int getOrientationStyle() {
        return mStore.getOrientationStyle();
    }

    public void setSequenceType(int type) {
        mStore.setSequenceType(type);
    }

    public int getSequenceType() {
        return mStore.getSequenceType();
    }

    public Set<String> getGestureEntries() {
        return mStore.getGestureEntries();
    }

    public ArrayList<AmPrediction> recognize(AmGesture gesture) {
        return mStore.recognize(gesture);
    }

    public void addGesture(String entryName, AmGesture gesture) {
        mStore.addGesture(entryName, gesture);
    }

    public void removeGesture(String entryName, AmGesture gesture) {
        mStore.removeGesture(entryName, gesture);
    }

    public void removeEntry(String entryName) {
        mStore.removeEntry(entryName);
    }

    public ArrayList<AmGesture> getGestures(String entryName) {
        return mStore.getGestures(entryName);
    }
}

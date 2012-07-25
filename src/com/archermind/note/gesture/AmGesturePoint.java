package com.archermind.note.gesture;

import java.io.DataInputStream;
import java.io.IOException;

public class AmGesturePoint {
    public final float x;
    public final float y;

    public AmGesturePoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    static AmGesturePoint deserialize(DataInputStream in) throws IOException {
        // Read X and Y
        final float x = in.readFloat();
        final float y = in.readFloat();
        return new AmGesturePoint(x, y);
    }
    
    @Override
    public Object clone() {
        return new AmGesturePoint(x, y);
    }
}

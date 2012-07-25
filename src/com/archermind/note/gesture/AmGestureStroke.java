package com.archermind.note.gesture;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class AmGestureStroke extends AmGestureMeta {
    static final float TOUCH_TOLERANCE = 3;

    public final RectF boundingBox;

    public final float length;
    public final float[] points;

    private Path mCachedPath;

    /**
     * A constructor that constructs a gesture stroke from a list of gesture points.
     * 
     * @param points
     */
    public AmGestureStroke(ArrayList<AmGesturePoint> points) {
        final int count = points.size();
        final float[] tmpPoints = new float[count * 2];

        RectF bx = null;
        float len = 0;
        int index = 0;

        for (int i = 0; i < count; i++) {
            final AmGesturePoint p = points.get(i);
            tmpPoints[i * 2] = p.x;
            tmpPoints[i * 2 + 1] = p.y;

            if (bx == null) {
                bx = new RectF();
                bx.top = p.y;
                bx.left = p.x;
                bx.right = p.x;
                bx.bottom = p.y;
                len = 0;
            } else {
                len += Math.sqrt(Math.pow(p.x - tmpPoints[(i - 1) * 2], 2)
                        + Math.pow(p.y - tmpPoints[(i -1 ) * 2 + 1], 2));
                bx.union(p.x, p.y);
            }
            index++;
        }
        
        this.points = tmpPoints;
        boundingBox = bx;
        length = len;
    }

    /**
     * A faster constructor specially for cloning a stroke.
     */
    private AmGestureStroke(RectF bbx, float len, float[] pts) {
        boundingBox = new RectF(bbx.left, bbx.top, bbx.right, bbx.bottom);
        length = len;
        points = pts.clone();
    }
    
    @Override
    public Object clone() {
        return new AmGestureStroke(boundingBox, length, points);
    }
    
    /**
     * Draws the stroke with a given canvas and paint.
     * 
     * @param canvas
     */
    void draw(Canvas canvas, Paint paint) {
        if (mCachedPath == null) {
            makePath();
        }

        canvas.drawPath(mCachedPath, paint);
    }

    public Path getPath() {
        if (mCachedPath == null) {
            makePath();
        }

        return mCachedPath;
    }

    private void makePath() {
        final float[] localPoints = points;
        final int count = localPoints.length;

        Path path = null;

        float mX = 0;
        float mY = 0;

        for (int i = 0; i < count; i += 2) {
            float x = localPoints[i];
            float y = localPoints[i + 1];
            if (path == null) {
                path = new Path();
                path.moveTo(x, y);
                mX = x;
                mY = y;
            } else {
                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                    mX = x;
                    mY = y;
                }
            }
        }

        mCachedPath = path;
    }

    /**
     * Converts the stroke to a Path of a given number of points.
     * 
     * @param width the width of the bounding box of the target path
     * @param height the height of the bounding box of the target path
     * @param numSample the number of points needed
     * 
     * @return the path
     */
    public Path toPath(float width, float height, int numSample) {
        final float[] pts = AmGestureUtils.temporalSampling(this, numSample);
        final RectF rect = boundingBox;

        AmGestureUtils.translate(pts, -rect.left, -rect.top);
        
        float sx = width / rect.width();
        float sy = height / rect.height();
        float scale = sx > sy ? sy : sx;
       AmGestureUtils.scale(pts, scale, scale);

        float mX = 0;
        float mY = 0;

        Path path = null;

        final int count = pts.length;

        for (int i = 0; i < count; i += 2) {
            float x = pts[i];
            float y = pts[i + 1];
            if (path == null) {
                path = new Path();
                path.moveTo(x, y);
                mX = x;
                mY = y;
            } else {
                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                    mX = x;
                    mY = y;
                }
            }
        }

        return path;
    }

    void serialize(DataOutputStream out) throws IOException {
        final float[] pts = points;
        final int count = points.length;

        // Write number of points
        out.writeInt(count / 2);

        for (int i = 0; i < count; i += 2) {
            // Write X
            out.writeFloat(pts[i]);
            // Write Y
            out.writeFloat(pts[i + 1]);
        }
    }

    static AmGestureStroke deserialize(DataInputStream in) throws IOException {
        // Number of points
        final int count = in.readInt();

        final ArrayList<AmGesturePoint> points = new ArrayList<AmGesturePoint>(count);
        for (int i = 0; i < count; i++) {
            points.add(AmGesturePoint.deserialize(in));
        }

        return new AmGestureStroke(points);
    }    

    /**
     * Invalidates the cached path that is used to render the stroke.
     */
    public void clearPath() {
        if (mCachedPath != null) mCachedPath.rewind();
    }
}

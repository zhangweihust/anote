package com.archermind.note.gesture;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class AmGesture implements Parcelable {
    private static final long GESTURE_ID_BASE = System.currentTimeMillis();

    private static final int BITMAP_RENDERING_WIDTH = 2;

    private static final boolean BITMAP_RENDERING_ANTIALIAS = true;
    private static final boolean BITMAP_RENDERING_DITHER = true;

    private static final AtomicInteger sGestureCount = new AtomicInteger(0);

    private final RectF mBoundingBox = new RectF();

    // the same as its instance ID
    private long mGestureID;
    
    private float mGesturePaintWidth;
    
    private int mGesturePaintColor;

    private final ArrayList<AmGestureStroke> mStrokes = new ArrayList<AmGestureStroke>();

    public AmGesture() {
        mGestureID = GESTURE_ID_BASE + sGestureCount.incrementAndGet();
    }

    @Override
    public Object clone() {
        AmGesture gesture = new AmGesture();
        gesture.mBoundingBox.set(mBoundingBox.left, mBoundingBox.top, 
                                        mBoundingBox.right, mBoundingBox.bottom);
        final int count = mStrokes.size();
        for (int i = 0; i < count; i++) {
            AmGestureStroke stroke = mStrokes.get(i);
            gesture.mStrokes.add((AmGestureStroke)stroke.clone());
        }
        return gesture;
    }
    
    /**
     * @return all the strokes of the gesture
     */
    public ArrayList<AmGestureStroke> getStrokes() {
        return mStrokes;
    }

    /**
     * @return the number of strokes included by this gesture
     */
    public int getStrokesCount() {
        return mStrokes.size();
    }

    /**
     * Adds a stroke to the gesture.
     * 
     * @param stroke
     */
    public void addStroke(AmGestureStroke stroke) {
        mStrokes.add(stroke);
        mBoundingBox.union(stroke.boundingBox);
    }

    /**
     * Calculates the total length of the gesture. When there are multiple strokes in
     * the gesture, this returns the sum of the lengths of all the strokes.
     * 
     * @return the length of the gesture
     */
    public float getLength() {
        int len = 0;
        final ArrayList<AmGestureStroke> strokes = mStrokes;
        final int count = strokes.size();

        for (int i = 0; i < count; i++) {
            len += strokes.get(i).length;
        }

        return len;
    }

    /**
     * @return the bounding box of the gesture
     */
    public RectF getBoundingBox() {
        return mBoundingBox;
    }

    public Path toPath() {
        return toPath(null);
    }

    public Path toPath(Path path) {
        if (path == null) path = new Path();

        final ArrayList<AmGestureStroke> strokes = mStrokes;
        final int count = strokes.size();

        for (int i = 0; i < count; i++) {
            path.addPath(strokes.get(i).getPath());
        }

        return path;
    }

    public Path toPath(int width, int height, int edge, int numSample) {
        return toPath(null, width, height, edge, numSample);
    }

    public Path toPath(Path path, int width, int height, int edge, int numSample) {
        if (path == null) path = new Path();

        final ArrayList<AmGestureStroke> strokes = mStrokes;
        final int count = strokes.size();

        for (int i = 0; i < count; i++) {
            path.addPath(strokes.get(i).toPath(width - 2 * edge, height - 2 * edge, numSample));
        }

        return path;
    }

    /**
     * Sets the id of the gesture.
     * 
     * @param id
     */
    void setID(long id) {
        mGestureID = id;
    }

    /**
     * @return the id of the gesture
     */
    public long getID() {
        return mGestureID;
    }

    /**
     * Creates a bitmap of the gesture with a transparent background.
     * 
     * @param width width of the target bitmap
     * @param height height of the target bitmap
     * @param edge the edge
     * @param numSample
     * @param color
     * @return the bitmap
     */
    public Bitmap toBitmap(int width, int height, int edge, int numSample, int color) {
        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);

        canvas.translate(edge, edge);

        final Paint paint = new Paint();
        paint.setAntiAlias(BITMAP_RENDERING_ANTIALIAS);
        paint.setDither(BITMAP_RENDERING_DITHER);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(BITMAP_RENDERING_WIDTH);

        final ArrayList<AmGestureStroke> strokes = mStrokes;
        final int count = strokes.size();

        for (int i = 0; i < count; i++) {
            Path path = strokes.get(i).toPath(width - 2 * edge, height - 2 * edge, numSample);
            canvas.drawPath(path, paint);
        }

        return bitmap;
    }

    /**
     * Creates a bitmap of the gesture with a transparent background.
     * 
     * @param width
     * @param height
     * @param inset
     * @param color
     * @return the bitmap
     */
    public Bitmap toBitmap(int width, int height, int inset, int color) {
        final Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);

        final Paint paint = new Paint();
        paint.setAntiAlias(BITMAP_RENDERING_ANTIALIAS);
        paint.setDither(BITMAP_RENDERING_DITHER);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(mGesturePaintWidth / 3.0f);

        final Path path = toPath();
        final RectF bounds = new RectF();
        path.computeBounds(bounds, true);

        final float sx = (width - 2 * inset) / bounds.width();
        final float sy = (height - 2 * inset) / bounds.height();
        final float scale = sx > sy ? sy : sx;
        paint.setStrokeWidth((mGesturePaintWidth / scale) / 3.0f);

        path.offset(-bounds.left + (width - bounds.width() * scale) / 2.0f,
                -bounds.top + (height - bounds.height() * scale) / 2.0f);

        canvas.translate(inset, inset);
        canvas.scale(scale, scale);

        canvas.drawPath(path, paint);

        return bitmap;
    }

    void serialize(DataOutputStream out) throws IOException {
        final ArrayList<AmGestureStroke> strokes = mStrokes;
        final int count = strokes.size();

        // Write gesture ID
        out.writeLong(mGestureID);
        
        out.writeFloat(mGesturePaintWidth);
        
        out.writeInt(mGesturePaintColor);
        
        // Write number of strokes
        out.writeInt(count);

        for (int i = 0; i < count; i++) {
            strokes.get(i).serialize(out);
        }
    }

    static AmGesture deserialize(DataInputStream in) throws IOException {
        final AmGesture gesture = new AmGesture();

        // Gesture ID
        gesture.mGestureID = in.readLong();
        
        gesture.mGesturePaintWidth = in.readFloat();
        
        gesture.mGesturePaintColor = in.readInt();
        
        // Number of strokes
        final int count = in.readInt();

        for (int i = 0; i < count; i++) {
            gesture.addStroke(AmGestureStroke.deserialize(in));
        }

        return gesture;
    }
    
    public static final Parcelable.Creator<AmGesture> CREATOR = new Parcelable.Creator<AmGesture>() {
        public AmGesture createFromParcel(Parcel in) {
        	AmGesture gesture = null;
            final long gestureID = in.readLong();

            final DataInputStream inStream = new DataInputStream(
                    new ByteArrayInputStream(in.createByteArray()));

            try {
                gesture = deserialize(inStream);
            } catch (IOException e) {
                Log.e(AmGestureConstants.LOG_TAG, "Error reading Gesture from parcel:", e);
            } finally {
                AmGestureUtils.closeStream(inStream);
            }

            if (gesture != null) {
                gesture.mGestureID = gestureID;
            }

            return gesture;
        }

        public AmGesture[] newArray(int size) {
            return new AmGesture[size];
        }
    };

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(mGestureID);

        boolean result = false;
        final ByteArrayOutputStream byteStream =
                new ByteArrayOutputStream(AmGestureConstants.IO_BUFFER_SIZE);
        final DataOutputStream outStream = new DataOutputStream(byteStream);

        try {
            serialize(outStream);
            result = true;
        } catch (IOException e) {
            Log.e(AmGestureConstants.LOG_TAG, "Error writing Gesture to parcel:", e);
        } finally {
        	AmGestureUtils.closeStream(outStream);
            AmGestureUtils.closeStream(byteStream);
        }

        if (result) {
            out.writeByteArray(byteStream.toByteArray());
        }
    }
    
    public int describeContents() {
        return 0;
    }
    
    public void setGesturePaintWidth(float width) {
        mGesturePaintWidth = width;
    }
    
    public float getGesturePaintWidth() {
        return mGesturePaintWidth;
    }
    
    public void setGesturePaintColor(int color) {
        mGesturePaintColor = color;
    }
    
    public int getGesturePaintColor() {
        return mGesturePaintColor;
    }
}

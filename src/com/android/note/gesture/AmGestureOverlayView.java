package com.android.note.gesture;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.os.SystemClock;

import java.util.ArrayList;

public class AmGestureOverlayView extends FrameLayout {
    public static final int GESTURE_STROKE_TYPE_SINGLE = 0;
    public static final int GESTURE_STROKE_TYPE_MULTIPLE = 1;

    public static final int ORIENTATION_HORIZONTAL = 0;
    public static final int ORIENTATION_VERTICAL = 1;

    private static final int FADE_ANIMATION_RATE = 16;
    private static final boolean GESTURE_RENDERING_ANTIALIAS = true;
    private static final boolean DITHER_FLAG = true;

    private final Paint mGesturePaint = new Paint();

    private long mFadeDuration = 150;
    private long mFadeOffset = 820;
    private long mFadingStart;
    private boolean mFadingHasStarted;
    private boolean mFadeEnabled = true;

    private int mCurrentColor;
    private int mCertainGestureColor = 0xFF000000;
    private int mUncertainGestureColor = 0x48FFFF00;
    private float mGestureStrokeWidth = 12.0f;
    private int mInvalidateExtraBorder = 10;

    private int mGestureStrokeType = GESTURE_STROKE_TYPE_MULTIPLE;
    private float mGestureStrokeLengthThreshold = 50.0f;
    private float mGestureStrokeSquarenessTreshold = 0.275f;
    private float mGestureStrokeAngleThreshold = 40.0f;

    private int mOrientation = ORIENTATION_VERTICAL;

    private final Rect mInvalidRect = new Rect();
    private final Path mPath = new Path();
    private boolean mGestureVisible = true;

    private float mX;
    private float mY;

    private float mCurveEndX;
    private float mCurveEndY;

    private float mTotalLength;
    private boolean mIsGesturing = false;
    private boolean mPreviousWasGesturing = false;
    private boolean mInterceptEvents = true;
    private boolean mIsListeningForGestures;
    private boolean mResetGesture;

    // current gesture
    private AmGesture mCurrentGesture;
    private final ArrayList<AmGesturePoint> mStrokeBuffer = new ArrayList<AmGesturePoint>(100);

    // TODO: Make this a list of WeakReferences
    private final ArrayList<OnAmGestureListener> mOnGestureListeners =
            new ArrayList<OnAmGestureListener>();
    // TODO: Make this a list of WeakReferences
    private final ArrayList<OnAmGesturePerformedListener> mOnGesturePerformedListeners =
            new ArrayList<OnAmGesturePerformedListener>();
    // TODO: Make this a list of WeakReferences
    private final ArrayList<OnAmGesturingListener> mOnGesturingListeners =
            new ArrayList<OnAmGesturingListener>();

    private boolean mHandleGestureActions;

    // fading out effect
    private boolean mIsFadingOut = false;
    private float mFadingAlpha = 1.0f;
    private final AccelerateDecelerateInterpolator mInterpolator =
            new AccelerateDecelerateInterpolator();

    private final FadeOutRunnable mFadingOut = new FadeOutRunnable();

    public AmGestureOverlayView(Context context) {
        super(context);
        init();
    }

    public AmGestureOverlayView(Context context, AttributeSet attrs) {
    	super(context, attrs);
        init();
    }

    public AmGestureOverlayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        /*TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.GestureOverlayView, defStyle, 0);

        mGestureStrokeWidth = a.getFloat(R.styleable.GestureOverlayView_gestureStrokeWidth,
                mGestureStrokeWidth);
        mInvalidateExtraBorder = Math.max(1, ((int) mGestureStrokeWidth) - 1);
        mCertainGestureColor = a.getColor(R.styleable.GestureOverlayView_gestureColor,
                mCertainGestureColor);
        mUncertainGestureColor = a.getColor(R.styleable.GestureOverlayView_uncertainGestureColor,
                mUncertainGestureColor);
        mFadeDuration = a.getInt(R.styleable.GestureOverlayView_fadeDuration, (int) mFadeDuration);
        mFadeOffset = a.getInt(R.styleable.GestureOverlayView_fadeOffset, (int) mFadeOffset);
        mGestureStrokeType = a.getInt(R.styleable.GestureOverlayView_gestureStrokeType,
                mGestureStrokeType);
        mGestureStrokeLengthThreshold = a.getFloat(
                R.styleable.GestureOverlayView_gestureStrokeLengthThreshold,
                mGestureStrokeLengthThreshold);
        mGestureStrokeAngleThreshold = a.getFloat(
                R.styleable.GestureOverlayView_gestureStrokeAngleThreshold,
                mGestureStrokeAngleThreshold);
        mGestureStrokeSquarenessTreshold = a.getFloat(
                R.styleable.GestureOverlayView_gestureStrokeSquarenessThreshold,
                mGestureStrokeSquarenessTreshold);
        mInterceptEvents = a.getBoolean(R.styleable.GestureOverlayView_eventsInterceptionEnabled,
                mInterceptEvents);
        mFadeEnabled = a.getBoolean(R.styleable.GestureOverlayView_fadeEnabled,
                mFadeEnabled);
        mOrientation = a.getInt(R.styleable.GestureOverlayView_orientation, mOrientation);

        a.recycle();*/

        init();
    }

    private void init() {
        setWillNotDraw(false);

        final Paint gesturePaint = mGesturePaint;
        gesturePaint.setAntiAlias(GESTURE_RENDERING_ANTIALIAS);
        gesturePaint.setColor(mCertainGestureColor);
        gesturePaint.setStyle(Paint.Style.STROKE);
        gesturePaint.setStrokeJoin(Paint.Join.ROUND);
        gesturePaint.setStrokeCap(Paint.Cap.ROUND);
        gesturePaint.setStrokeWidth(mGestureStrokeWidth);
        gesturePaint.setDither(DITHER_FLAG);

        mCurrentColor = mCertainGestureColor;
        setPaintAlpha(255);
    }


	public ArrayList<AmGesturePoint> getCurrentStroke() {
        return mStrokeBuffer;
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void setGestureColor(int color) {
        mCertainGestureColor = color;setCurrentColor(color);
    }

    public void setUncertainGestureColor(int color) {
        mUncertainGestureColor = color;
    }

    public int getUncertainGestureColor() {
        return mUncertainGestureColor;
    }

    public int getGestureColor() {
        return mCertainGestureColor;
    }

    public float getGestureStrokeWidth() {
        return mGestureStrokeWidth;
    }

    public void setGestureStrokeWidth(float gestureStrokeWidth) {
        mGestureStrokeWidth = gestureStrokeWidth;
        mInvalidateExtraBorder = Math.max(1, ((int) gestureStrokeWidth) - 1);
        mGesturePaint.setStrokeWidth(gestureStrokeWidth);
    }

    public int getGestureStrokeType() {
        return mGestureStrokeType;
    }

    public void setGestureStrokeType(int gestureStrokeType) {
        mGestureStrokeType = gestureStrokeType;
    }

    public float getGestureStrokeLengthThreshold() {
        return mGestureStrokeLengthThreshold;
    }

    public void setGestureStrokeLengthThreshold(float gestureStrokeLengthThreshold) {
        mGestureStrokeLengthThreshold = gestureStrokeLengthThreshold;
    }

    public float getGestureStrokeSquarenessTreshold() {
        return mGestureStrokeSquarenessTreshold;
    }

    public void setGestureStrokeSquarenessTreshold(float gestureStrokeSquarenessTreshold) {
        mGestureStrokeSquarenessTreshold = gestureStrokeSquarenessTreshold;
    }

    public float getGestureStrokeAngleThreshold() {
        return mGestureStrokeAngleThreshold;
    }

    public void setGestureStrokeAngleThreshold(float gestureStrokeAngleThreshold) {
        mGestureStrokeAngleThreshold = gestureStrokeAngleThreshold;
    }

    public boolean isEventsInterceptionEnabled() {
        return mInterceptEvents;
    }

    public void setEventsInterceptionEnabled(boolean enabled) {
        mInterceptEvents = enabled;
    }

    public boolean isFadeEnabled() {
        return mFadeEnabled;
    }

    public void setFadeEnabled(boolean fadeEnabled) {
        mFadeEnabled = fadeEnabled;
    }

    public AmGesture getGesture() {
        return mCurrentGesture;
    }

    public void setGesture(AmGesture gesture) {
        if (mCurrentGesture != null) {
            clear(false);
        }

        setCurrentColor(mCertainGestureColor);
        mCurrentGesture = gesture;

        final Path path = mCurrentGesture.toPath();
        final RectF bounds = new RectF();
        path.computeBounds(bounds, true);

        // TODO: The path should also be scaled to fit inside this view
        mPath.rewind();
        mPath.addPath(path, -bounds.left + (getWidth() - bounds.width()) / 2.0f,
                -bounds.top + (getHeight() - bounds.height()) / 2.0f);

        mResetGesture = true;

        invalidate();
    }

    public Path getGesturePath() {
        return mPath;
    }

    public Path getGesturePath(Path path) {
        path.set(mPath);
        return path;
    }

    public boolean isGestureVisible() {
        return mGestureVisible;
    }

    public void setGestureVisible(boolean visible) {
        mGestureVisible = visible;
    }

    public long getFadeOffset() {
        return mFadeOffset;
    }

    public void setFadeOffset(long fadeOffset) {
        mFadeOffset = fadeOffset;
    }

    public void addOnGestureListener(OnAmGestureListener listener) {
        mOnGestureListeners.add(listener);
    }

    public void removeOnGestureListener(OnAmGestureListener listener) {
        mOnGestureListeners.remove(listener);
    }

    public void removeAllOnGestureListeners() {
        mOnGestureListeners.clear();
    }

    public void addOnGesturePerformedListener(OnAmGesturePerformedListener listener) {
        mOnGesturePerformedListeners.add(listener);
        if (mOnGesturePerformedListeners.size() > 0) {
            mHandleGestureActions = true;
        }
    }

    public void removeOnGesturePerformedListener(OnAmGesturePerformedListener listener) {
        mOnGesturePerformedListeners.remove(listener);
        if (mOnGesturePerformedListeners.size() <= 0) {
            mHandleGestureActions = false;
        }
    }

    public void removeAllOnGesturePerformedListeners() {
        mOnGesturePerformedListeners.clear();
        mHandleGestureActions = false;
    }

    public void addOnGesturingListener(OnAmGesturingListener listener) {
        mOnGesturingListeners.add(listener);
    }

    public void removeOnGesturingListener(OnAmGesturingListener listener) {
        mOnGesturingListeners.remove(listener);
    }

    public void removeAllOnGesturingListeners() {
        mOnGesturingListeners.clear();
    }

    public boolean isGesturing() {
        return mIsGesturing;
    }

    private void setCurrentColor(int color) {
        mCurrentColor = color;
        if (mFadingHasStarted) {
            setPaintAlpha((int) (255 * mFadingAlpha));
        } else {
            setPaintAlpha(255);
        }
        invalidate();
    }

    /**
     * @hide
     */
    public Paint getGesturePaint() {
        return mGesturePaint;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        
        if (mCurrentGesture != null && mGestureVisible) {
            canvas.drawPath(mPath, mGesturePaint);
        }
    }

    private void setPaintAlpha(int alpha) {
        alpha += alpha >> 7;
        final int baseAlpha = mCurrentColor >>> 24;
        final int useAlpha = baseAlpha * alpha >> 8;
        mGesturePaint.setColor((mCurrentColor << 8 >>> 8) | (useAlpha << 24));
    }

    public void clear(boolean animated) {
        clear(animated, false, true);
    }

    private void clear(boolean animated, boolean fireActionPerformed, boolean immediate) {
        setPaintAlpha(255);
        removeCallbacks(mFadingOut);
        mResetGesture = false;
        mFadingOut.fireActionPerformed = fireActionPerformed;
        mFadingOut.resetMultipleStrokes = false;

        if (animated && mCurrentGesture != null) {
            mFadingAlpha = 1.0f;
            mIsFadingOut = true;
            mFadingHasStarted = false;
            mFadingStart = AnimationUtils.currentAnimationTimeMillis() + mFadeOffset;

            postDelayed(mFadingOut, mFadeOffset);
        } else {
            mFadingAlpha = 1.0f;
            mIsFadingOut = false;
            mFadingHasStarted = false;

            if (immediate) {
                mCurrentGesture = null;
                mPath.rewind();
                invalidate();
            } else if (fireActionPerformed) {
                postDelayed(mFadingOut, mFadeOffset);
            } else if (mGestureStrokeType == GESTURE_STROKE_TYPE_MULTIPLE) {
                mFadingOut.resetMultipleStrokes = true;
                postDelayed(mFadingOut, mFadeOffset);
            } else {
                mCurrentGesture = null;
                mPath.rewind();
                invalidate();
            }
        }
    }

    public void cancelClearAnimation() {
        setPaintAlpha(255);
        mIsFadingOut = false;
        mFadingHasStarted = false;
        removeCallbacks(mFadingOut);
        mPath.rewind();
        mCurrentGesture = null;
    }

    public void cancelGesture() {
        mIsListeningForGestures = false;

        // add the stroke to the current gesture
        mCurrentGesture.addStroke(new AmGestureStroke(mStrokeBuffer));

        // pass the event to handlers
        final long now = SystemClock.uptimeMillis();
        final MotionEvent event = MotionEvent.obtain(now, now,
                MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);

        final ArrayList<OnAmGestureListener> listeners = mOnGestureListeners;
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
            listeners.get(i).onGestureCancelled(this, event);
        }

        event.recycle();

        clear(false);
        mIsGesturing = false;
        mPreviousWasGesturing = false;
        mStrokeBuffer.clear();

        final ArrayList<OnAmGesturingListener> otherListeners = mOnGesturingListeners;
        count = otherListeners.size();
        for (int i = 0; i < count; i++) {
            otherListeners.get(i).onGesturingEnded(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelClearAnimation();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
    	if (isEnabled()) {
            final boolean cancelDispatch = (mIsGesturing || (mCurrentGesture != null &&
                    mCurrentGesture.getStrokesCount() > 0 && mPreviousWasGesturing)) &&
                    mInterceptEvents;

            processEvent(event);

            if (cancelDispatch) {
                event.setAction(MotionEvent.ACTION_CANCEL);
            }

            super.dispatchTouchEvent(event);

            return true;
        }

        return super.dispatchTouchEvent(event);
    }

    private boolean processEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(event);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (mIsListeningForGestures) {
                    Rect rect = touchMove(event);
                    if (rect != null) {
                        invalidate(rect);
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsListeningForGestures) {
                    touchUp(event, false);
                    invalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsListeningForGestures) {
                    touchUp(event, true);
                    invalidate();
                    return true;
                }
        }

        return false;
    }

    private void touchDown(MotionEvent event) {
        mIsListeningForGestures = true;

        float x = event.getX();
        float y = event.getY();

        mX = x;
        mY = y;

        mTotalLength = 0;
        mIsGesturing = false;

        if (mGestureStrokeType == GESTURE_STROKE_TYPE_SINGLE || mResetGesture) {
            if (mHandleGestureActions) setCurrentColor(mUncertainGestureColor);
            mResetGesture = false;
            mCurrentGesture = null;
            mPath.rewind();
        } else if (mCurrentGesture == null || mCurrentGesture.getStrokesCount() == 0) {
            if (mHandleGestureActions) setCurrentColor(mUncertainGestureColor);
        }

        // if there is fading out going on, stop it.
        if (mFadingHasStarted) {
            cancelClearAnimation();
        } else if (mIsFadingOut) {
            setPaintAlpha(255);
            mIsFadingOut = false;
            mFadingHasStarted = false;
            removeCallbacks(mFadingOut);
        }

        if (mCurrentGesture == null) {
            mCurrentGesture = new AmGesture();
        }

        mStrokeBuffer.add(new AmGesturePoint(x, y));
        mPath.moveTo(x, y);

        final int border = mInvalidateExtraBorder;
        mInvalidRect.set((int) x - border, (int) y - border, (int) x + border, (int) y + border);

        mCurveEndX = x;
        mCurveEndY = y;

        // pass the event to handlers
        final ArrayList<OnAmGestureListener> listeners = mOnGestureListeners;
        final int count = listeners.size();
        for (int i = 0; i < count; i++) {
            listeners.get(i).onGestureStarted(this, event);
        }
    }

    private Rect touchMove(MotionEvent event) {
        Rect areaToRefresh = null;

        final float x = event.getX();
        final float y = event.getY();

        final float previousX = mX;
        final float previousY = mY;

        final float dx = Math.abs(x - previousX);
        final float dy = Math.abs(y - previousY);

        if (dx >= AmGestureStroke.TOUCH_TOLERANCE || dy >= AmGestureStroke.TOUCH_TOLERANCE) {
            areaToRefresh = mInvalidRect;

            // start with the curve end
            final int border = mInvalidateExtraBorder;
            areaToRefresh.set((int) mCurveEndX - border, (int) mCurveEndY - border,
                    (int) mCurveEndX + border, (int) mCurveEndY + border);

            float cX = mCurveEndX = (x + previousX) / 2;
            float cY = mCurveEndY = (y + previousY) / 2;

            mPath.quadTo(previousX, previousY, cX, cY);

            // union with the control point of the new curve
            areaToRefresh.union((int) previousX - border, (int) previousY - border,
                    (int) previousX + border, (int) previousY + border);

            // union with the end point of the new curve
            areaToRefresh.union((int) cX - border, (int) cY - border,
                    (int) cX + border, (int) cY + border);

            mX = x;
            mY = y;

            mStrokeBuffer.add(new AmGesturePoint(x, y));

            
            if (mHandleGestureActions && !mIsGesturing) {
                mTotalLength += (float) Math.sqrt(dx * dx + dy * dy);

                
                if (mTotalLength > mGestureStrokeLengthThreshold) {
                	
                    final AmOrientedBoundingBox box =
                            AmGestureUtils.computeOrientedBoundingBox(mStrokeBuffer);

                    float angle = Math.abs(box.orientation);
                    if (angle > 90) {
                        angle = 180 - angle;
                    }

                    if (box.squareness > mGestureStrokeSquarenessTreshold ||
                            (mOrientation == ORIENTATION_VERTICAL ?
                                    angle < mGestureStrokeAngleThreshold :
                                    angle > mGestureStrokeAngleThreshold)) {

                    	
                        mIsGesturing = true;
                        setCurrentColor(mCertainGestureColor);

                        final ArrayList<OnAmGesturingListener> listeners = mOnGesturingListeners;
                        int count = listeners.size();
                        for (int i = 0; i < count; i++) {
                            listeners.get(i).onGesturingStarted(this);
                        }
                    }
                }
            }

            // pass the event to handlers
            final ArrayList<OnAmGestureListener> listeners = mOnGestureListeners;
            final int count = listeners.size();
            for (int i = 0; i < count; i++) {
                listeners.get(i).onGesture(this, event);
            }
        }

        return areaToRefresh;
    }

    private void touchUp(MotionEvent event, boolean cancel) {
        mIsListeningForGestures = false;

        // A gesture wasn't started or was cancelled
        if (mCurrentGesture != null) {
            // add the stroke to the current gesture
            mCurrentGesture.addStroke(new AmGestureStroke(mStrokeBuffer));
            
            mCurrentGesture.setGesturePaintWidth(mGestureStrokeWidth);
            
            mCurrentGesture.setGesturePaintColor(mCertainGestureColor);

            if (!cancel) {
                // pass the event to handlers
                final ArrayList<OnAmGestureListener> listeners = mOnGestureListeners;
                int count = listeners.size();
                for (int i = 0; i < count; i++) {
                    listeners.get(i).onGestureEnded(this, event);
                }

                clear(mHandleGestureActions && mFadeEnabled, mHandleGestureActions && mIsGesturing,
                        false);
            } else {
                cancelGesture(event);

            }
        } else {
            cancelGesture(event);
        }

        mStrokeBuffer.clear();
        mPreviousWasGesturing = mIsGesturing;
        mIsGesturing = false;

        final ArrayList<OnAmGesturingListener> listeners = mOnGesturingListeners;
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
            listeners.get(i).onGesturingEnded(this);
        }
    }

    private void cancelGesture(MotionEvent event) {
        // pass the event to handlers
        final ArrayList<OnAmGestureListener> listeners = mOnGestureListeners;
        final int count = listeners.size();
        for (int i = 0; i < count; i++) {
            listeners.get(i).onGestureCancelled(this, event);
        }

        clear(false);
    }

    private void fireOnGesturePerformed() {
        final ArrayList<OnAmGesturePerformedListener> actionListeners = mOnGesturePerformedListeners;
        final int count = actionListeners.size();
        for (int i = 0; i < count; i++) {
            actionListeners.get(i).onGesturePerformed(AmGestureOverlayView.this, mCurrentGesture);
        }
    }

    private class FadeOutRunnable implements Runnable {
        boolean fireActionPerformed;
        boolean resetMultipleStrokes;

        public void run() {
        	final ArrayList<OnAmGestureListener> listeners = mOnGestureListeners;
            final int count = listeners.size();
            for (int i = 0; i < count; i++) {
                listeners.get(i).onGestureCancelled(AmGestureOverlayView.this, null);
            }
            if (mIsFadingOut) {
                final long now = AnimationUtils.currentAnimationTimeMillis();
                final long duration = now - mFadingStart;

                if (duration > mFadeDuration) {
                    if (fireActionPerformed) {
                        fireOnGesturePerformed();
                    }

                    mPreviousWasGesturing = false;
                    mIsFadingOut = false;
                    mFadingHasStarted = false;
                    mPath.rewind();
                    
                    mCurrentGesture = null;
                    setPaintAlpha(255);
                } else {
                    mFadingHasStarted = true;
                    float interpolatedTime = Math.max(0.0f,
                            Math.min(1.0f, duration / (float) mFadeDuration));
                    mFadingAlpha = 1.0f - mInterpolator.getInterpolation(interpolatedTime);
                    setPaintAlpha((int) (255 * mFadingAlpha));
                    postDelayed(this, FADE_ANIMATION_RATE);
                }
            } else if (resetMultipleStrokes) {
                mResetGesture = true;
            } else {
                fireOnGesturePerformed();

                mFadingHasStarted = false;
                mPath.rewind();
                mCurrentGesture = null;
                mPreviousWasGesturing = false;
                setPaintAlpha(255);
            }

            invalidate();
        }
    }

    public static interface OnAmGesturingListener {
        void onGesturingStarted(AmGestureOverlayView overlay);

        void onGesturingEnded(AmGestureOverlayView overlay);
    }

    public static interface OnAmGestureListener {
        void onGestureStarted(AmGestureOverlayView overlay, MotionEvent event);

        void onGesture(AmGestureOverlayView overlay, MotionEvent event);

        void onGestureEnded(AmGestureOverlayView overlay, MotionEvent event);

        void onGestureCancelled(AmGestureOverlayView overlay, MotionEvent event);
    }

    public static interface OnAmGesturePerformedListener {
        void onGesturePerformed(AmGestureOverlayView overlay, AmGesture gesture);
    }
}

package com.archermind.note.editnote;

import com.archermind.note.Screens.EditNoteScreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;

public class MyEditText extends EditText implements ColorPickerDialog.OnColorChangedListener {
	private Rect mRect;
    private Paint mPaint;
    /**
     * 字体大小
     */
    public static int fontSize = 32;
    /**
     * 字体颜色
     */
    public static int fontColor = Color.BLACK;;
    /**
     * 文本初始高度
     */
    public static int initNoteHight = 480;
    /**
     * 分割线颜色
     */
    public static int lineColor = 0x8092bcdd;
    /**
     * 文本追加高度
     */
    public static int append = 240;
    
    private boolean isGraffiting = false;
    
    /**
     * 轨迹
     */
    private Path mPath = null;
    
    /**
     * 轨迹画笔
     */
    private Paint mFingerPen = null;

    /**
     * 橡皮擦
     */
    private Paint mClearPaint = null;
    
    private float mPointX = 0;
    
    private float mPointY = 0;
    
    private Canvas mCanvas = null;
    
    private Bitmap mBmp = null;
	private Paint mBitmapPaint;
    
    private static final float TOUCH_TOLERANCE = 4.0F;
    
    // we need this constructor for LayoutInflater
    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(lineColor);
        setTextSize(fontSize);
        setTextColor(fontColor);
        
        mPath = new Path();
        mFingerPen = new Paint();
        mFingerPen.setColor(0xFFFF0000);
        mFingerPen.setStrokeWidth(12);
        mFingerPen.setStyle(Paint.Style.STROKE);
        mFingerPen.setStrokeJoin(Paint.Join.ROUND);
        mFingerPen.setStrokeCap(Paint.Cap.ROUND);
        
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        
        mClearPaint = new Paint();
        mClearPaint.setColor(0x00000000);
        
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        Rect r = mRect;
        Paint paint = mPaint;
        int lineHeight = getLineHeight();
        int height = getLineBounds(getLineCount() - 1, r);
        if(height >= initNoteHight - lineHeight){
        	initNoteHight += append;
        }
    	int count = (int) (initNoteHight / lineHeight + 2);
        int baseline = 0;
        for (int i = 0; i < count; i++) {
        	baseline += lineHeight;
            canvas.drawLine(r.left, baseline + 8, r.right, baseline + 8, paint);
        }
        if (mBmp != null) {
            canvas.drawBitmap(mBmp, 0, 0, mBitmapPaint);
        }
        canvas.drawPath(mPath, mFingerPen);
        super.onDraw(canvas);
    }
    
    public void setIsGraffit(boolean flag) {
    	isGraffiting = flag;
    }
    
    public void setFingerColor(int color) {
    	mFingerPen.setColor(color);
    }
    
    public void setFingerStrokeWidt(int width) {
    	mFingerPen.setStrokeWidth(width);
    }
    
    
    
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
	    if (mBmp == null) {
	    	mBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	        mCanvas = new Canvas(mBmp);
	    }
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (EditNoteScreen.mState != EditNoteScreen.GRAFFITINSERTSTATE) {
		    return super.onTouchEvent(event);
		} else {
			switch (event.getAction()) {
			case  MotionEvent.ACTION_DOWN:
				touch_down(event);
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				touch_move(event);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				touch_up(event);
				invalidate();
				break;
			case MotionEvent.ACTION_CANCEL:
			}
		}
		
		return true;
	}
    
    private void touch_down(MotionEvent event) {
    	mPath.reset();
    	mPath.moveTo(event.getX(), event.getY());
    	mPointX = event.getX();
    	mPointY = event.getY();
    }
    
    private void touch_move(MotionEvent event) {
    	float x = event.getX();
    	float y = event.getY();
    	
    	float f1 = Math.abs(x - mPointX);
    	float f2 = Math.abs(y - mPointY);
    	
    	if ((f1 > TOUCH_TOLERANCE) || (f2 > TOUCH_TOLERANCE)) {
    	    mPath.quadTo(mPointX, mPointY, (x + mPointX)/2.0F, (y + mPointY)/2.0F);
    	    mPointX = x;
        	mPointY = y;
    	}
    	
    }
    
    private void touch_up(MotionEvent event) {
    	mPath.lineTo(mPointX, mPointY);
    	if (mCanvas != null) {
    	    mCanvas.drawPath(mPath, mFingerPen);
    	}
    	mPath.reset();
    }

	@Override
	public void colorChanged(int color) {
		// TODO Auto-generated method stub
		mFingerPen.setColor(color);
	}
	
	public Paint getFingerPen() {
		return mFingerPen;
	}
}


package com.archermind.note.editnote;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.archermind.note.Screens.EditNoteScreen;
import com.archermind.note.gesture.AmGesture;
import com.archermind.note.gesture.AmGestureLibraries;
import com.archermind.note.gesture.AmGestureLibrary;
import com.archermind.note.gesture.AmGesturePoint;
import com.archermind.note.gesture.AmGestureStroke;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;


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
    
    private Paint mTempPaint = null;
    
    private int mFingerColor = 0x00000000;
    private int mFingerStrokeWidth = 12;
    
    private float mPointX = 0;
    
    private float mPointY = 0;
    
    private Bitmap mBmp = null;
	private Paint mBitmapPaint;
	private Canvas mCanvas = null;
    
	private AmGesture mCurrentGesture;
	private final ArrayList<AmGesturePoint> mStrokeBuffer = new ArrayList<AmGesturePoint>(100);
    
    private File graffitFile = null;
	private AmGestureLibrary mStore = null;

	private static final float TOUCH_TOLERANCE = 4.0F;
	
	private VelocityTracker mVelocityTracker;
	
	private EditNoteScreen mEditNote;
	
	private InputMethodManager  imm = null;
	
	private boolean isChangePage = false;
	
	private int mVelocityX = 0;
	
	private int inType = 0;
    
    // we need this constructor for LayoutInflater
    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(lineColor);
        setTextSize(fontSize);
        setTextColor(fontColor);
        setLongClickable(false);
        
        inType = getInputType(); 
        
        mPath = new Path();
        mFingerPen = new Paint();
        mFingerPen.setColor(0xFFFF0000);
        mFingerColor = 0xFFFF0000;
        mFingerPen.setStrokeWidth(12);
        mFingerStrokeWidth = 12;
        mFingerPen.setAntiAlias(true);
        
        mFingerPen.setStyle(Paint.Style.STROKE);
        mFingerPen.setStrokeJoin(Paint.Join.ROUND);
        mFingerPen.setStrokeCap(Paint.Cap.ROUND);
        
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        
        mClearPaint = new Paint();
        mClearPaint.setColor(0x00000000);
        
        mTempPaint = new Paint();
        mTempPaint.setColor(0xFFFF0000);
        mTempPaint.setStrokeWidth(12);
        mTempPaint.setAntiAlias(true);
        mTempPaint.setStyle(Paint.Style.STROKE);
        mTempPaint.setStrokeJoin(Paint.Join.ROUND);
        mTempPaint.setStrokeCap(Paint.Cap.ROUND);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
    	super.onDraw(canvas);
        
        if (mBmp != null) {
            canvas.drawBitmap(mBmp, 0, 0, mBitmapPaint);
        }
        
        canvas.drawPath(mPath, mFingerPen);
    }
    
    public void setEditNote(EditNoteScreen editnote) {
    	mEditNote = editnote;
    	imm = (InputMethodManager) mEditNote.getSystemService(Context.INPUT_METHOD_SERVICE);
    }
    
    private void drawGesture(Canvas canvas) {
    	ArrayList<AmGestureStroke> mStrokes = mCurrentGesture.getStrokes();
    	for(AmGestureStroke stroke:mStrokes) {
    		mTempPaint.setColor(stroke.getFingerColor());
    		mTempPaint.setStrokeWidth(stroke.getFingerStrokeWidth());
    		stroke.draw(canvas, mTempPaint);
    	}
    }
    
    

    
    @Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		// TODO Auto-generated method stub
		super.onScrollChanged(l, t, oldl, oldt);
	}

	public void setIsGraffit(boolean flag) {
    	isGraffiting = flag;
    	invalidate();
    }
    
    public void setFingerColor(int color) {
    	mFingerColor = color;
    }
    
    public void setFingerStrokeWidth(int width) {
    	mFingerPen.setStrokeWidth(width);
    	mFingerStrokeWidth = width;
    }
    
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		if (mBmp == null) {
	    	mBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	        mCanvas = new Canvas(mBmp);
	    }
		reloadGraffit("page0");
	}
	
	public void reload() {
		mPath = mCurrentGesture.toPath();
		invalidate();
	}
	
	public void recycleBitmap() {
		if (mBmp != null && !mBmp.isRecycled()) {
		    mBmp.recycle();
		    mBmp = null;
		}
	}
	
	public boolean save() {
		if (graffitFile == null) {
			graffitFile = new File("/sdcard/aNote/graffit");
    	}
    	
    	if (!graffitFile.exists()) {
			try {
				graffitFile.getParentFile().mkdirs();
				graffitFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	
    	if (mStore == null) {
            mStore = AmGestureLibraries.fromFile(graffitFile);
        }
    	if (mStore.getGestureEntries() == null || mStore.getGestureEntries().size() == 0) {
    		graffitFile.delete();
    		return true;
    	}
    	mStore.save(true);
    	return true;
	}
	
	public void addGraffit(String name) {
		if (mCurrentGesture == null || mCurrentGesture.getStrokes().size() == 0) {
            return;
        }
		

		if (graffitFile == null) {
			graffitFile = new File("/sdcard/aNote/graffit");
    	}
    	
    	if (!graffitFile.exists()) {
			try {
				graffitFile.getParentFile().mkdirs();
				graffitFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	
    	if (mStore == null) {
            mStore = AmGestureLibraries.fromFile(graffitFile);
        }
    	
    	if (mStore.getGestures(name) != null) {
    		mStore.removeEntry(name);
    	}
    	mStore.addGesture(name, mCurrentGesture);
    	mStore.save(true);
    	
        mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
	}
	
	public void reloadGraffit(String name) {
		if (graffitFile == null) {
			graffitFile = new File("/sdcard/aNote/graffit");
    	}
    	
    	if (!graffitFile.exists()) {
    		return;
		}
    	
    	if (mStore == null) {
            mStore = AmGestureLibraries.fromFile(graffitFile);
        }
    	
    	mStore.load(true);
    		
    	mCurrentGesture = null;
        mCurrentGesture = new AmGesture();
    	
    	if (mStore.getGestures(name) != null) {
	    	mCurrentGesture = mStore.getGestures(name).get(0);
	    	ArrayList<AmGestureStroke> mStrokes = mCurrentGesture.getStrokes();
	    	for(AmGestureStroke stroke:mStrokes) {
	    		mTempPaint.setColor(stroke.getFingerColor());
	    		mTempPaint.setStrokeWidth(stroke.getFingerStrokeWidth());
	    		if (stroke.getFingerColor() == 0x00000000) {
	    			mTempPaint.setXfermode(new PorterDuffXfermode(
		                    PorterDuff.Mode.CLEAR));
	    		} else {
	    			mTempPaint.setXfermode(null);
	    		}
	    		stroke.draw(mCanvas, mTempPaint);
	    	}
	    }
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (EditNoteScreen.mState != EditNoteScreen.GRAFFITINSERTSTATE) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				isChangePage = false;
				break;
			case MotionEvent.ACTION_MOVE:
				if (isChangePage) {
					imm.hideSoftInputFromWindow(getWindowToken(), 0);
					break;
				}
				if (mVelocityTracker == null) {
					mVelocityTracker = VelocityTracker.obtain();
				}
				mVelocityTracker.addMovement(event);
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000);
				mVelocityX = (int) velocityTracker.getXVelocity();
				int velocityY = (int) velocityTracker.getYVelocity();
				if (Math.abs(mVelocityX) > 100 && Math.abs(mVelocityX) > Math.abs(velocityY)) {
					isChangePage = true;
					imm.hideSoftInputFromWindow(getWindowToken(), 0);
					return true;
				}
				break;
			case MotionEvent.ACTION_UP:
				if (isChangePage) {
					if (mVelocityX > 0) {
						if (mEditNote != null) {
						    mEditNote.movePrePage();
						    imm.hideSoftInputFromWindow(getWindowToken(), 0);
						}
					} else {
						if (mEditNote != null) {
						    mEditNote.moveNextPage();
						    imm.hideSoftInputFromWindow(getWindowToken(), 0);
						}
					}
					return true;
				}
				break;
			}
		    if (EditNoteScreen.mState == EditNoteScreen.SOFTINPUTSTATE) {
		        return super.onTouchEvent(event);
		    } else {
		    	try {
		            Method method = TextView.class.getMethod("setSoftInputShownOnFocus", boolean.class);
		            method.invoke(this, false);
		            super.onTouchEvent(event);
		            method.invoke(this, true);
		        } catch (Exception e) {
		            // Fallback to the second method
		        	e.printStackTrace();
		        }
		    }
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
    	mPointX = event.getX();
    	mPointY = event.getY();
    	mPath.moveTo(mPointX, mPointY);
    	mStrokeBuffer.add(new AmGesturePoint(mPointX, mPointY));
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
        	mStrokeBuffer.add(new AmGesturePoint(mPointX, mPointY));
        	if (!mEditNote.hasChanged()) {
        		mEditNote.setHasChanged(true);
        	}
    	}
    	
    }
    
    private void touch_up(MotionEvent event) {
    	mPath.lineTo(mPointX, mPointY);
    	if (mCurrentGesture == null) {
            mCurrentGesture = new AmGesture();
        }
    	if (mCanvas != null) {
    	    mCanvas.drawPath(mPath, mFingerPen);
    	}
    	AmGestureStroke gestureStroke = new AmGestureStroke(mStrokeBuffer);
    	gestureStroke.setFingerColor(mFingerColor);
    	gestureStroke.setFingerStrokeWidth(mFingerStrokeWidth);
    	mCurrentGesture.addStroke(gestureStroke);
    	mStrokeBuffer.clear();
    	mPath.reset();
    }

	@Override
	public void colorChanged(int color) {
		// TODO Auto-generated method stub
		mFingerPen.setColor(color);
		mFingerColor = color;
	}
	
	public Paint getFingerPen() {
		return mFingerPen;
	}
}


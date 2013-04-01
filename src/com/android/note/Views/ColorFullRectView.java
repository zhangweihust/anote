package com.android.note.Views;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.android.note.Screens.HomeScreen;
import com.archermind.note.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * @author Grantland Chew
 * @since Feb 13, 2011
 */
public class ColorFullRectView extends View {
	
	public ColorFullRectView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ColorFullRectView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public ColorFullRectView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	private static int mPading = 5;
	private static int mStrokeWidth = 3;
	
    @Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		Paint paint = new Paint();
		paint.setStrokeWidth(mStrokeWidth);
		// 将边框设为黑色.  
		paint.setColor(getResources().getColor(R.color.blue));  
		// 画TextView的4个边.  
		canvas.drawLine(mPading, mPading, this.getWidth() - mPading, mPading, paint);  
		canvas.drawLine(mPading, mPading, mPading, this.getHeight() - mPading, paint);  
		canvas.drawLine(this.getWidth() - mPading, mPading, this.getWidth() - mPading, this.getHeight() - mPading, paint);  
		canvas.drawLine(mPading, this.getHeight() - mPading, this.getWidth() - mPading, this.getHeight() - mPading, paint);
	}
	
}
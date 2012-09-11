package com.archermind.note.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

public class PhotoGallery extends Gallery{

	private int FLINGTHRESHOLD;
	
	// Convert the dips to pixels 
	
	public PhotoGallery(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		float scale = getResources().getDisplayMetrics().density; 
	    FLINGTHRESHOLD = (int) (20.0f * scale + 0.5f);
	}
	
	public PhotoGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		float scale = getResources().getDisplayMetrics().density; 
	    FLINGTHRESHOLD = (int) (20.0f * scale + 0.5f);
	}

	public PhotoGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		float scale = getResources().getDisplayMetrics().density; 
	    FLINGTHRESHOLD = (int) (20.0f * scale + 0.5f);
	}

	// 实现短距离滑动
	 @Override
	 public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//	      int kEvent;
//	      if (isScrollingLeft(e1, e2)) {
//	          // Check if scrolling left
//	          kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
//	      } else {
//	         // Otherwise scrolling right
//	         kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
//	      }
//	      onKeyDown(kEvent, null);
//	      return false;
		 
		 
		 if (velocityX>FLINGTHRESHOLD) 
		 {
			 return super.onFling(e1, e2, FLINGTHRESHOLD, velocityY);
		 } 
		 else if (velocityX<-FLINGTHRESHOLD) 
		 {
			 return super.onFling(e1, e2, -FLINGTHRESHOLD, velocityY);
		 } 
		 else 
		 {
			 return super.onFling(e1, e2, velocityX, velocityY);
		 } 
	 }
}

package com.archermind.note.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

public class MenuRightHorizontalScrollView extends HorizontalScrollView {

	/* 菜单状态 */
	private boolean menuOut;

	public MenuRightHorizontalScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	public MenuRightHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public MenuRightHorizontalScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}

	private void init() {
		this.setHorizontalFadingEdgeEnabled(false);
		this.setVerticalFadingEdgeEnabled(false);
		this.menuOut = false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return false;
	}

	public void MenuOut(boolean aMenuOut) {
		this.menuOut = aMenuOut;
	}
	
	public boolean MenuOut() {
		return this.menuOut;
	}
}

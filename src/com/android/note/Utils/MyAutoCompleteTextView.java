package com.android.note.Utils;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.AutoCompleteTextView;

public class MyAutoCompleteTextView extends AutoCompleteTextView {
    
	public MyAutoCompleteTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	public MyAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public MyAutoCompleteTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	@Override
    protected void onFocusChanged(boolean focused, int direction,
    		Rect previouslyFocusedRect) {
    	performFiltering(getText(), KeyEvent.KEYCODE_UNKNOWN);
    }
    @Override
    public boolean enoughToFilter() {
    	return true;
    }
    
}

package com.android.note.gesture;

public class AmGestureMeta {

	private int mFingerColor = 0x00000000;
	private int mFingerStrokeWidth = 12;
	
	public void setFingerColor(int color) {
		mFingerColor = color;
	}
	
	public int getFingerColor() {
		return mFingerColor;
	}
	
	public void setFingerStrokeWidth(int width) {
		mFingerStrokeWidth = width;
	}
	
	public int getFingerStrokeWidth() {
		return mFingerStrokeWidth;
	}
}

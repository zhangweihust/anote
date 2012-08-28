package com.archermind.note.Screens;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;

public class Screen extends Activity {
	private ProgressDialog mProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  
	}
	/**
	 * 显示等待框
	 * 
	 * @param title
	 * @param message
	 */
	public void showProgress(String title, String message) {
		mProgressDialog = ProgressDialog.show(this, title, message);
		mProgressDialog.setCancelable(true);
	}

	/**
	 * 取消等待框
	 */
	public void dismissProgress() {
		if (mProgressDialog != null) {
			try {
				mProgressDialog.dismiss();
			} catch (Exception e) {

			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(mProgressDialog != null){
			mProgressDialog.dismiss();
		}
	}
	
	
}

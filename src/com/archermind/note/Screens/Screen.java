package com.archermind.note.Screens;

import android.app.Activity;
import android.app.ProgressDialog;

public class Screen extends Activity {
	private ProgressDialog mProgressDialog;
	/**
	 * 显示等待框
	 * 
	 * @param title
	 * @param message
	 */
	protected void showProgress(String title, String message) {
		mProgressDialog = ProgressDialog.show(this, title, message);
		mProgressDialog.setCancelable(true);
	}

	/**
	 * 取消等待框
	 */
	protected void dismissProgress() {
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

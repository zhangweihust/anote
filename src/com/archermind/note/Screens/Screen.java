package com.archermind.note.Screens;

import com.archermind.note.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class Screen extends Activity {
	private Dialog mProgressDialog;
	
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
		try{
		mProgressDialog = new Dialog(this, R.style.CustomDialog);
		mProgressDialog.setContentView(R.layout.dialog_progress);
		TextView textView = (TextView) mProgressDialog.findViewById(R.id.progress_msg);
		textView.setText(message);
		if (android.os.Build.VERSION.SDK_INT > 8) {
			Typeface type = Typeface.createFromAsset(getAssets(), "xdxwzt.ttf");
			textView.setTypeface(type);
		}
		mProgressDialog.show();
		}catch (OutOfMemoryError e) {
			// TODO: handle exception
			e.printStackTrace();
		}
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

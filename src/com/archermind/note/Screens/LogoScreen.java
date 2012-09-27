package com.archermind.note.Screens;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Services.ServiceManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class LogoScreen extends Screen{
	
	private final long time = 2000;
	private static Context mContext;
	private Dialog noSdcardDialog;
	
	private Runnable logo = new Runnable() {

		@Override
		public void run() {
			try {
				Thread.sleep(time);

				
				handler.post(new Runnable() {

					@Override
					public void run() {
						boolean isHasSD = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
						if(!isHasSD){
							noSdcardDialog.show();
						}else{
							Intent intent = new Intent();
							intent.setClass(mContext, MainScreen.class);
							mContext.startActivity(intent);
							LogoScreen.this.finish();
						}
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

	private final Handler handler;

	public LogoScreen() {
		handler = new Handler();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logo_screen);
		mContext = this;
		
		noSdcardDialog = new Dialog(mContext, R.style.CornerDialog);
		noSdcardDialog.setContentView(R.layout.dialog_ok);
		TextView titleView = (TextView) noSdcardDialog
				.findViewById(R.id.dialog_title);
		titleView.setText(R.string.check_sd_title);
		TextView msgView = (TextView) noSdcardDialog
				.findViewById(R.id.dialog_message);
		msgView.setText(R.string.check_sd_msg);
		Button btn_ok = (Button) noSdcardDialog
				.findViewById(R.id.dialog_btn_ok);
		btn_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				noSdcardDialog.dismiss();
				LogoScreen.this.finish();
				ServiceManager.exit();
			}
		});
		
		noSdcardDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
				// TODO Auto-generated method stub
				if(arg2.getKeyCode() == KeyEvent.KEYCODE_BACK || arg2.getKeyCode() == KeyEvent.KEYCODE_HOME){
					return true;
				}
				return false;
			}
		});
		
		new Thread(logo).start();
	}
}

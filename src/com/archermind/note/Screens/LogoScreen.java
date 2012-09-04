package com.archermind.note.Screens;

import com.archermind.note.R;
import com.archermind.note.Services.ServiceManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.TextView;


public class LogoScreen extends Screen{
	
	private final long time = 2000;
	private Context mContext;
	
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
							AlertDialog.Builder builder = new Builder(LogoScreen.this);
							  builder.setMessage(getResources().getString(R.string.check_sd_msg));

							  builder.setTitle(getResources().getString(R.string.check_sd_title));

							  builder.setNegativeButton(R.string.check_sd_ok, new DialogInterface.OnClickListener() {
								
								public void onClick(DialogInterface arg0, int arg1) {
									// TODO Auto-generated method stub
									LogoScreen.this.finish();
									ServiceManager.exit();
								}
							});

							builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
								
								@Override
								public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
									// TODO Auto-generated method stub
									if(arg2.getKeyCode() == KeyEvent.KEYCODE_BACK || arg2.getKeyCode() == KeyEvent.KEYCODE_HOME){
										return true;
									}
									return false;
								}
							});
							
							builder.create().show();
						}
						
						Intent intent = new Intent();
						intent.setClass(mContext, MainScreen.class);
						mContext.startActivity(intent);
						LogoScreen.this.finish();
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
		new Thread(logo).start();
	}
}

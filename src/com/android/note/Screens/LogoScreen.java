package com.android.note.Screens;

import com.android.note.NoteApplication;
import com.android.note.Services.ServiceManager;
import com.archermind.note.R;

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
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class LogoScreen extends Screen{
	
	private final long time = 2000;
	private static Context mContext;
	private Dialog noSdcardDialog;
	private boolean flag = false;
	

	/**
     * 用Handler来更新UI
     */
    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {

            switch (msg.what) {

                case 1:
                	System.out.println("===flag is ====" + flag);
                	boolean isHasSD = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
					if(!isHasSD){
						noSdcardDialog.show();
					}else if(!flag){
						Intent intent = new Intent();
						intent.setClass(mContext, MainScreen.class);
						mContext.startActivity(intent);
						LogoScreen.this.finish();
                    }
                    break;

                default:
                    break;
            }
        }
    };

	public LogoScreen() {
		super();
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
		
		new Thread() {
            public void run() {

                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                handler.sendEmptyMessage(1);
            }
        }.start();
	}
	
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            return true;
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_HOME){
        	flag = true;
			this.finish();
        }
        return super.dispatchKeyEvent(event);
    }
    
    @Override
    public void onAttachedToWindow () {
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
        super.onAttachedToWindow();
    }
}

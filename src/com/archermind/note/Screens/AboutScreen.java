package com.archermind.note.Screens;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Utils.DownloadApkHelper;
import com.archermind.note.Utils.VersionUtil;

public class AboutScreen extends Screen implements OnClickListener {
	private AboutScreen mContext;
	private ImageButton mBtnBack;
	private Button mBtnCheckUpdate;
	private TextView mlogoTitle;
	private Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_screen);

		
//		mLoadingLayout = (LinearLayout)findViewById(R.id.fullscreen_loading_style);

		handler = new Handler(){
	            @Override
	            public void handleMessage(Message msg) {
	                // TODO Auto-generated method stub
	                super.handleMessage(msg);
	                if (msg.what==1){
//	                    mLoadingLayout.setVisibility(View.GONE);
//	                    mMainLayout.setVisibility(View.VISIBLE);
//	        			btn_check_update.setVisibility(View.VISIBLE);
	                }                
	            }
	            
	        };
	        
		mContext = AboutScreen.this;

		mBtnBack = (ImageButton) findViewById(R.id.back);
		mBtnBack.setOnClickListener(this);
		
		mBtnCheckUpdate = (Button) findViewById(R.id.about_check_update);
		mBtnCheckUpdate.setOnClickListener(this);
						
		mlogoTitle = (TextView) findViewById(R.id.about_logo_title);
		Typeface type = Typeface.createFromAsset(getAssets(),"xdxwzt.ttf");
		mlogoTitle.setTypeface(type);
		mlogoTitle.setText(R.string.about_logo_title);
		
		TextView txtVersionContext = (TextView) findViewById(R.id.about_version_content);
		String verName = "1.0";
		try {
			verName = getPackageManager().getPackageInfo("com.archermind.note", 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		txtVersionContext.setText("V " + verName);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;
		case R.id.about_check_update: {
	        
	        boolean networkIsOk = false;
			try {
				ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo ni = cm.getActiveNetworkInfo();
				networkIsOk = (ni != null ? ni.isConnectedOrConnecting() : false);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(networkIsOk) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						System.out.println("=CCC= MANUAL_UPDATE");
						Looper.prepare();
						DownloadApkHelper downloadApk = new DownloadApkHelper(mContext, Looper.myLooper());
						downloadApk.updateApk(DownloadApkHelper.MANUAL_UPDATE, handler);
						Looper.loop();
					}
				}).start();
			} else {
				Toast.makeText(NoteApplication.getContext(),
						R.string.network_none,
						Toast.LENGTH_SHORT).show();
			}
		}
		break;
		}
	}
}

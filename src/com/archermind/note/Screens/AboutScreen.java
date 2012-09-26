package com.archermind.note.Screens;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Utils.DownloadApkHelper;
import com.archermind.note.Utils.NetworkUtils;
import com.archermind.note.Utils.ServerInterface;

public class AboutScreen extends Screen implements OnClickListener {
	private AboutScreen mContext;
	private Button mBtnBack;
	private Button mBtnCheckUpdate;
	private TextView mlogoTitle;
	private LinearLayout mWebLayout;
	private LinearLayout mBlogLayout;
	private Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_screen);

		// mLoadingLayout =
		// (LinearLayout)findViewById(R.id.fullscreen_loading_style);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if (msg.what == 1) {
					// mLoadingLayout.setVisibility(View.GONE);
					// mMainLayout.setVisibility(View.VISIBLE);
					// btn_check_update.setVisibility(View.VISIBLE);
					dismissProgress();
				}
			}

		};

		mContext = AboutScreen.this;

		mBtnBack = (Button) findViewById(R.id.back);
		mBtnBack.setOnClickListener(this);

		mBtnCheckUpdate = (Button) findViewById(R.id.about_check_update);
		mBtnCheckUpdate.setOnClickListener(this);

		mlogoTitle = (TextView) findViewById(R.id.about_logo_title);
		if (android.os.Build.VERSION.SDK_INT > 8) {
			Typeface type = Typeface.createFromAsset(getAssets(), "xdxwzt.ttf");
			mlogoTitle.setTypeface(type);
		}
		mlogoTitle.setText(R.string.about_logo_title);

		mWebLayout = (LinearLayout) findViewById(R.id.about_web_layout);
		mWebLayout.setOnClickListener(this);

		TextView txtVersionContext = (TextView) findViewById(R.id.about_version_content);
		String verName = "1.0";
		try {
			verName = getPackageManager().getPackageInfo("com.archermind.note",
					0).versionName;
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
		case R.id.about_web_layout:
			Intent intent = new Intent("android.intent.action.VIEW");
			intent.setData(Uri.parse(ServerInterface.URL_SERVER));
			startActivity(intent);
			break;
		case R.id.about_check_update: {
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				showProgress(null, getString(R.string.screen_update_get_new_version));			
				DownloadApkHelper downloadApk = new DownloadApkHelper(mContext);
				downloadApk.checkUpdate();
				
			} else {
				Toast.makeText(NoteApplication.getContext(),
						R.string.network_none, Toast.LENGTH_SHORT).show();
			}
		}
			break;
		}
	}
}

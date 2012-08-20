package com.archermind.note.Screens;

import java.io.IOException;

import com.archermind.note.R;
import com.archermind.note.Utils.NetworkUtils;
import com.archermind.note.Utils.PreferencesHelper;
import com.weibo.net.AccessToken;
import com.weibo.net.AsyncWeiboRunner;
import com.weibo.net.Oauth2AccessTokenHeader;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ShareScreen extends Screen implements OnClickListener {

	private SharedPreferences mPreferences;
	private Button mBackButton;
	private EditText mEditText;
	private Button mSinaButton;
	private Button mQQButton;
	private Button mRenrenButton;
	private static final String TAG = "ShareScreen";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share);
		mPreferences = PreferencesHelper.getSharedPreferences(this, 0);

		mBackButton = (Button) findViewById(R.id.screen_top_play_control_back);
		mBackButton.setOnClickListener(this);
		mEditText = (EditText) findViewById(R.id.share_edit);
		mSinaButton = (Button) findViewById(R.id.btn_share_sina);
		mSinaButton.setOnClickListener(this);
		mQQButton = (Button) findViewById(R.id.btn_share_qq);
		mQQButton.setOnClickListener(this);
		mRenrenButton = (Button) findViewById(R.id.btn_share_renren);
		mRenrenButton.setOnClickListener(this);
	}


	private void shareToSina() {
		String token = mPreferences.getString(
				PreferencesHelper.XML_SINA_ACCTSS_TOKEN, null);
		Long expires_in = mPreferences.getLong(
				PreferencesHelper.XML_SINA_EXPIRES_IN, 0);
		if (token != null && System.currentTimeMillis() < expires_in) {
			showProgress(null, getString(R.string.share_dialog_msg_sina));
			Weibo weibo = Weibo.getInstance();
			weibo.setAccessToken(new AccessToken(token,
					ShareSettingScreen.APPSECRET_SINA));
			Utility.setAuthorization(new Oauth2AccessTokenHeader()); // 必需，设置发送的http请求头信息，不设会出现auth
																		// faild
			WeiboParameters bundle = new WeiboParameters();
			bundle.add("source", ShareSettingScreen.APPKEY_SINA);
			// bundle.add("pic", null);// 图片路径
			bundle.add("status", mEditText.getText().toString());
			String url = Weibo.SERVER + "statuses/update.json";
			AsyncWeiboRunner weiboRunner = new AsyncWeiboRunner(weibo);
			weiboRunner.request(this, url, bundle, Utility.HTTPMETHOD_POST,
					new AsyncWeiboRunner.RequestListener() {

						@Override
						public void onIOException(IOException e) {
							dismissProgress();
							Toast.makeText(ShareScreen.this,
									R.string.share_failed, Toast.LENGTH_SHORT)
									.show();
							e.printStackTrace();
						}

						@Override
						public void onError(WeiboException e) {
							dismissProgress();
							if (e.getStatusCode() == 40111) {
								Toast.makeText(ShareScreen.this,
										R.string.share_sina_expired,
										Toast.LENGTH_SHORT).show();
								Editor editor = mPreferences.edit();
								editor.remove(PreferencesHelper.XML_SINA_ACCTSS_TOKEN);
								editor.remove(PreferencesHelper.XML_SINA_EXPIRES_IN);
								editor.commit();
								Intent intent = new Intent(ShareScreen.this,
										ShareSettingScreen.class);
								startActivity(intent);
							} else if (e.getStatusCode() == 20019) {
								Toast.makeText(ShareScreen.this,
										R.string.share_err_equal_weibo,
										Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(ShareScreen.this,
										R.string.share_failed,
										Toast.LENGTH_SHORT).show();
							}
							Log.e(TAG, "Sina Weibo错误码：" + e.getStatusCode());
						}

						@Override
						public void onComplete(String response) {
							dismissProgress();
							Toast.makeText(ShareScreen.this,
									R.string.share_success, Toast.LENGTH_SHORT)
									.show();
						}
					});
		} else {
			Toast.makeText(this, R.string.share_sina_expired,
					Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(this, ShareSettingScreen.class);
			startActivity(intent);
		}

	}

	private void shareToQQ(){
		
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.screen_top_play_control_back:
			finish();
			break;
		case R.id.btn_share_sina:
			if(NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE){
				shareToSina();
			}else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT).show();
			}
			
			break;
		case R.id.btn_share_qq:
			break;
		case R.id.btn_share_renren:
			break;
		default:
			break;
		}
	}

}

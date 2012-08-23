package com.archermind.note.Screens;

import java.io.IOException;

import org.json.JSONObject;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Utils.NetworkUtils;
import com.archermind.note.Utils.PreferencesHelper;
import com.renren.api.connect.android.AsyncRenren;
import com.renren.api.connect.android.Renren;
import com.renren.api.connect.android.common.AbstractRequestListener;
import com.renren.api.connect.android.exception.RenrenError;
import com.renren.api.connect.android.feed.FeedPublishRequestParam;
import com.renren.api.connect.android.feed.FeedPublishResponseBean;
import com.tencent.weibo.api.TAPI;
import com.tencent.weibo.constants.OAuthConstants;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.oauthv2.OAuthV2Client;
import com.weibo.net.AccessToken;
import com.weibo.net.AsyncWeiboRunner;
import com.weibo.net.Oauth2AccessTokenHeader;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;

import android.content.Intent;
import android.content.SharedPreferences;
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
		if (NoteApplication.getInstance().ismBound_Sina()) {
			String token = mPreferences.getString(
					PreferencesHelper.XML_SINA_ACCTSS_TOKEN, null);
			if (token != null) {
				Weibo weibo = Weibo.getInstance();
				weibo.setAccessToken(new AccessToken(token,
						AccountScreen.APPSECRET_SINA));
				Utility.setAuthorization(new Oauth2AccessTokenHeader()); // 必需，设置发送的http请求头信息，不设会出现auth
																			// faild
				WeiboParameters bundle = new WeiboParameters();
				bundle.add("source", AccountScreen.APPKEY_SINA);
				// bundle.add("pic", null);// 图片路径
				bundle.add("status", mEditText.getText().toString());
				String url = Weibo.SERVER + "statuses/update.json";
				AsyncWeiboRunner weiboRunner = new AsyncWeiboRunner(weibo);
				weiboRunner.request(this, url, bundle, Utility.HTTPMETHOD_POST,
						new AsyncWeiboRunner.RequestListener() {

							@Override
							public void onIOException(IOException e) {
								Toast.makeText(ShareScreen.this,
										R.string.share_failed,
										Toast.LENGTH_SHORT).show();
								e.printStackTrace();
							}

							@Override
							public void onError(WeiboException e) {
								if (e.getStatusCode() == 40111) { // accessToken过期错误码
									Toast.makeText(ShareScreen.this,
											R.string.share_sina_expired,
											Toast.LENGTH_LONG).show();
									Intent intent = new Intent(
											ShareScreen.this, LoginScreen.class);
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
								Toast.makeText(ShareScreen.this,
										R.string.share_success,
										Toast.LENGTH_SHORT).show();
							}
						});
			} else {
				Toast.makeText(this, R.string.share_sina_expired,
						Toast.LENGTH_LONG).show();
				Intent intent = new Intent(this, LoginScreen.class);
				startActivity(intent);
			}
		} else {
			Toast.makeText(this, R.string.account_bound_sina_none,
					Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(this, AccountScreen.class);
			startActivity(intent);
		}
	}

	private void shareToQQ() {
		if (NoteApplication.getInstance().ismBound_QQ()) {
			String token = mPreferences.getString(
					PreferencesHelper.XML_QQ_ACCTSS_TOKEN, null);
			String openid = mPreferences.getString(
					PreferencesHelper.XML_QQ_OPENID, null);
			if (token != null && openid != null) {
				// 关闭OAuthV2Client中的默认开启的QHttpClient。
				OAuthV2Client.getQHttpClient().shutdownConnection();
				OAuthV2 oAuthV2 = new OAuthV2("http://www.archermind.com");
				oAuthV2.setClientId(AccountScreen.APPKEY_QQ);
				oAuthV2.setAccessToken(token);
				oAuthV2.setOpenid(openid);
				TAPI tapi = new TAPI(OAuthConstants.OAUTH_VERSION_2_A);
				try {
					String response = tapi.add(oAuthV2, "json",
							"Android客户端文字微博", "127.0.0.1");
					Log.i(TAG, response);
					JSONObject jsonObject = new JSONObject(response);
					if (jsonObject.optInt("errcode") == 0) {
						Toast.makeText(ShareScreen.this,
								R.string.share_success, Toast.LENGTH_SHORT)
								.show();
					} else if (jsonObject.optInt("errcode") == 37) { // accessToken过期错误码
						Toast.makeText(ShareScreen.this,
								R.string.share_qq_expired, Toast.LENGTH_LONG)
								.show();
						Intent intent = new Intent(ShareScreen.this,
								LoginScreen.class);
						startActivity(intent);
					} else {
						Toast.makeText(ShareScreen.this, R.string.share_failed,
								Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(ShareScreen.this, R.string.share_failed,
							Toast.LENGTH_SHORT).show();
				}
				tapi.shutdownConnection();
			} else {
				Toast.makeText(this, R.string.share_qq_expired,
						Toast.LENGTH_LONG).show();
				Intent intent = new Intent(this, LoginScreen.class);
				startActivity(intent);
			}
		} else {
			Toast.makeText(this, R.string.account_bound_qq_none,
					Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(this, AccountScreen.class);
			startActivity(intent);
		}
	}

	private void shareToRenren() {
		if (NoteApplication.getInstance().ismBound_Renren()) {
			String token = mPreferences.getString(
					PreferencesHelper.XML_RENREN_ACCTSS_TOKEN, null);
			if (token != null) {
				Renren renren = new Renren(AccountScreen.APPKEY_RENREN,
						AccountScreen.APPSECRET_RENREN,
						AccountScreen.APPID_RENREN, this);
				renren.updateAccessToken(token);
				AsyncRenren asyncRenren = new AsyncRenren(renren);
				FeedPublishRequestParam param = new FeedPublishRequestParam(
						"test_title", "test_content", "www.archermind.com",
						null, null, null, null, null);
				AbstractRequestListener<FeedPublishResponseBean> listener = new AbstractRequestListener<FeedPublishResponseBean>() {

					@Override
					public void onRenrenError(RenrenError rre) {
						if(rre.getErrorCode() == 2002){
							Toast.makeText(ShareScreen.this,
									R.string.share_renren_expired, Toast.LENGTH_LONG)
									.show();
							Intent intent = new Intent(ShareScreen.this,
									LoginScreen.class);
							startActivity(intent);
						} else {
							Toast.makeText(ShareScreen.this, R.string.share_failed,
									Toast.LENGTH_SHORT).show();
						}
					}

					@Override
					public void onFault(Throwable t) {
						Toast.makeText(ShareScreen.this, R.string.share_failed,
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onComplete(FeedPublishResponseBean bean) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(ShareScreen.this,
										R.string.share_success,
										Toast.LENGTH_SHORT).show();
							}
						});
					}
				};
				asyncRenren.publishFeed(param, listener, true);

			} else {
				Toast.makeText(this, R.string.share_renren_expired,
						Toast.LENGTH_LONG).show();
				Intent intent = new Intent(this, LoginScreen.class);
				startActivity(intent);
			}
		} else {
			Toast.makeText(this, R.string.account_bound_renren_none,
					Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(this, AccountScreen.class);
			startActivity(intent);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.screen_top_play_control_back:
			finish();
			break;
		case R.id.btn_share_sina:
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				shareToSina();
			} else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.btn_share_qq:
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				shareToQQ();
			} else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.btn_share_renren:
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				shareToRenren();
			} else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		default:
			break;
		}
	}

}

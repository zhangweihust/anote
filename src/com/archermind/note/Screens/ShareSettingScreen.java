package com.archermind.note.Screens;

import com.archermind.note.R;
import com.archermind.note.Utils.PreferencesHelper;
import com.renren.api.connect.android.Renren;
import com.renren.api.connect.android.exception.RenrenAuthError;
import com.renren.api.connect.android.view.RenrenAuthListener;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.webview.OAuthV2AuthorizeWebView;
import com.weibo.net.DialogError;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ShareSettingScreen extends Screen implements OnClickListener {

	private Button mBackButton;
	private TextView mSinaView;
	private TextView mSinaView_Cancel;
	private TextView mQQView;
	private TextView mQQView_Cancel;
	private TextView mRenrenView;
	private TextView mRenrenView_Cancel;
	public static final String APPKEY_SINA = "1294484213";// 申请的新浪KEY
	public static final String APPSECRET_SINA = "69c73b5fa22fbda126d4db68118afaa6"; // 申请的新浪SECRET
	public static final String APPKEY_QQ = "801210743";// 申请的腾讯KEY
	public static final String APPSECRET_QQ = "bee5553c65ee5ebb84f08f0c45630c4d"; // 申请的腾讯SECRET
	public static final String APPKEY_RENREN = "87e5e8e6175b46519fe9eb40968ba2dc";// 申请的人人KEY
	public static final String APPSECRET_RENREN = "3f093253cf344d3099e6df1e42f1d661"; // 申请的人人SECRET
	public static final String APPID_RENREN = "207067";
	private static final String TAG = "ShareSettingScreen";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_setting);
		initViews();
		checkBind();
	}

	private void initViews() {
		mBackButton = (Button) findViewById(R.id.screen_top_play_control_back);
		mBackButton.setOnClickListener(this);
		mSinaView = (TextView) findViewById(R.id.share_setting_tv_sina);
		mSinaView.setOnClickListener(this);
		mSinaView_Cancel = (TextView) findViewById(R.id.share_setting_tv_sina_cancel);
		mSinaView_Cancel.setOnClickListener(this);
		mQQView = (TextView) findViewById(R.id.share_setting_tv_qq);
		mQQView.setOnClickListener(this);
		mQQView_Cancel = (TextView) findViewById(R.id.share_setting_tv_qq_cancel);
		mQQView_Cancel.setOnClickListener(this);
		mRenrenView = (TextView) findViewById(R.id.share_setting_tv_renren);
		mRenrenView.setOnClickListener(this);
		mRenrenView_Cancel = (TextView) findViewById(R.id.share_setting_tv_renren_cancel);
		mRenrenView_Cancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.screen_top_play_control_back:
			finish();
			break;
		case R.id.share_setting_tv_sina:
			bindSinaweibo();
			break;
		case R.id.share_setting_tv_sina_cancel:
			unbindSinaweibo();
			break;
		case R.id.share_setting_tv_qq:
			bindQQweibo();
			break;
		case R.id.share_setting_tv_qq_cancel:
			unbindQQweibo();
			break;
		case R.id.share_setting_tv_renren:
			bindRenRen();
			break;
		case R.id.share_setting_tv_renren_cancel:
			unbindRenren();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == OAuthV2AuthorizeWebView.RESULT_CODE) {
				OAuthV2 oAuthV2 = (OAuthV2) data.getExtras().getSerializable(
						"oauth");
				if (oAuthV2.getStatus() == 0) {
					Log.i(TAG,
							oAuthV2.getAccessToken() + "  "
									+ oAuthV2.getExpiresIn() + "  "
									+ oAuthV2.getOpenid());
					SharedPreferences sp = getSharedPreferences(
							PreferencesHelper.XML_NAME, 0);
					Editor editor = sp.edit();
					editor.putString(PreferencesHelper.XML_QQ_ACCTSS_TOKEN,
							oAuthV2.getAccessToken());
					editor.putLong(
							PreferencesHelper.XML_QQ_EXPIRES_IN,
							System.currentTimeMillis()
									+ Long.parseLong(oAuthV2.getExpiresIn())
									* 1000);
					editor.commit();
					mQQView.setVisibility(View.GONE);
					mQQView_Cancel.setVisibility(View.VISIBLE);
				} else {
					Toast.makeText(this, "呃，出现了点问题，再试一次吧！", Toast.LENGTH_SHORT)
							.show();
				}
			}
		}
	}

	private void checkBind() {
		SharedPreferences sp = getSharedPreferences(PreferencesHelper.XML_NAME,
				0);
		if (sp.getString(PreferencesHelper.XML_SINA_ACCTSS_TOKEN, null) != null) {
			mSinaView.setVisibility(View.GONE);
			mSinaView_Cancel.setVisibility(View.VISIBLE);
		}
		if (sp.getString(PreferencesHelper.XML_QQ_ACCTSS_TOKEN, null) != null) {
			mQQView.setVisibility(View.GONE);
			mQQView_Cancel.setVisibility(View.VISIBLE);
		}
	}

	/*
	 * 关联新浪微博 Oauth2.0 隐式授权认证方式
	 */
	private void bindSinaweibo() {
		Weibo weibo = Weibo.getInstance();
		weibo.setupConsumerConfig(APPKEY_SINA, APPSECRET_SINA);
		weibo.setRedirectUrl("http://www.sina.com");// 此处使用的URL必须和新浪微博上应用提供的回调地址一样
		weibo.authorize(this, new AuthDialogListener());
	}

	/*
	 * 解除新浪微博关联
	 */
	private void unbindSinaweibo() {
		new AlertDialog.Builder(this)
				.setTitle("确定解除关联？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences sp = getSharedPreferences(
								PreferencesHelper.XML_NAME, 0);
						Editor editor = sp.edit();
						editor.remove(PreferencesHelper.XML_SINA_ACCTSS_TOKEN);
						editor.remove(PreferencesHelper.XML_SINA_EXPIRES_IN);
						editor.commit();
						mSinaView.setVisibility(View.VISIBLE);
						mSinaView_Cancel.setVisibility(View.GONE);
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).create().show();

	}

	/*
	 * 关联腾讯微博 OAuth Version 2 授权认证方式
	 */
	private void bindQQweibo() {
		OAuthV2 oAuthV2 = new OAuthV2("http://www.archermind.com");
		oAuthV2.setClientId(ShareSettingScreen.APPKEY_QQ);
		oAuthV2.setClientSecret(ShareSettingScreen.APPSECRET_QQ);
		Intent intent = new Intent(this, OAuthV2AuthorizeWebView.class);
		intent.putExtra("oauth", oAuthV2);
		startActivityForResult(intent, 1);

	}

	/*
	 * 解除腾讯微博关联
	 */
	private void unbindQQweibo() {
		new AlertDialog.Builder(this)
				.setTitle("确定解除关联？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences sp = getSharedPreferences(
								PreferencesHelper.XML_NAME, 0);
						Editor editor = sp.edit();
						editor.remove(PreferencesHelper.XML_QQ_ACCTSS_TOKEN);
						editor.remove(PreferencesHelper.XML_QQ_EXPIRES_IN);
						editor.commit();
						mQQView.setVisibility(View.VISIBLE);
						mQQView_Cancel.setVisibility(View.GONE);
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).create().show();

	}

	/*
	 * 关联人人账号
	 */
	private void bindRenRen() {
		Renren renren = new Renren(APPKEY_RENREN, APPSECRET_RENREN,
				APPID_RENREN, this);
		renren.logout(this);
		renren.authorize(this, new RenrenAuthListener() {

			@Override
			public void onRenrenAuthError(RenrenAuthError renrenAuthError) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onComplete(Bundle values) {
				Log.e(TAG, values.toString());
				Log.e(TAG, values.getString("access_token"));
				Log.e(TAG, values.getString("expires_in"));
				SharedPreferences sp = getSharedPreferences(
						PreferencesHelper.XML_NAME, 0);
				Editor editor = sp.edit();
				editor.putString(PreferencesHelper.XML_RENREN_ACCTSS_TOKEN,
						values.getString("access_token"));
				editor.putLong(
						PreferencesHelper.XML_RENREN_EXPIRES_IN,
						System.currentTimeMillis()
								+ Long.parseLong(values.getString("expires_in"))
								* 1000);
				editor.commit();
				mRenrenView.setVisibility(View.GONE);
				mRenrenView_Cancel.setVisibility(View.VISIBLE);
			}

			@Override
			public void onCancelLogin() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onCancelAuth(Bundle values) {
				// TODO Auto-generated method stub

			}
		});
	}

	/*
	 * 解除人人账号关联
	 */
	private void unbindRenren() {
		new AlertDialog.Builder(this)
				.setTitle("确定解除关联？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences sp = getSharedPreferences(
								PreferencesHelper.XML_NAME, 0);
						Editor editor = sp.edit();
						editor.remove(PreferencesHelper.XML_RENREN_ACCTSS_TOKEN);
						editor.remove(PreferencesHelper.XML_RENREN_EXPIRES_IN);
						editor.commit();
						mRenrenView.setVisibility(View.VISIBLE);
						mRenrenView_Cancel.setVisibility(View.GONE);
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).create().show();

	}

	private class AuthDialogListener implements WeiboDialogListener {

		@Override
		public void onComplete(Bundle values) {
			Log.i(TAG,
					values.getString("access_token") + "  "
							+ values.getString("expires_in") + "  "
							+ values.getString("uid"));
			SharedPreferences sp = getSharedPreferences(
					PreferencesHelper.XML_NAME, 0);
			Editor editor = sp.edit();
			editor.putString(PreferencesHelper.XML_SINA_ACCTSS_TOKEN,
					values.getString("access_token"));
			editor.putLong(
					PreferencesHelper.XML_SINA_EXPIRES_IN,
					System.currentTimeMillis()
							+ Long.parseLong(values.getString("expires_in"))
							* 1000);
			editor.commit();
			mSinaView.setVisibility(View.GONE);
			mSinaView_Cancel.setVisibility(View.VISIBLE);
		}

		@Override
		public void onWeiboException(WeiboException e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onError(DialogError e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub

		}

	}
}

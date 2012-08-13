package com.archermind.note.Screens;

import org.json.JSONException;
import org.json.JSONObject;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Utils.PreferencesHelper;
import com.archermind.note.Utils.ServerInterface;
import com.renren.api.connect.android.Renren;
import com.renren.api.connect.android.exception.RenrenAuthError;
import com.renren.api.connect.android.view.RenrenAuthListener;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.webview.OAuthV2AuthorizeWebView;
import com.weibo.net.DialogError;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginScreen extends Screen implements OnClickListener {

	private EditText mUserName;
	private EditText mPassWord;
	private Button mLoginButton;
	private Button mBackButton;
	private TextView mLoginButton_sina;
	private TextView mLoginButton_qq;
	private TextView mLoginButton_renren;
	private ProgressDialog mProgressDialog;
	private Handler mHandler;
	private static final String TAG = "LoginScreen";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		initViews();
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				String result = (String) msg.obj;
				if (result.equals("" + ServerInterface.ERROR_SERVER_INTERNAL)) {
					Toast.makeText(LoginScreen.this,
							R.string.login_err_server_internal,
							Toast.LENGTH_SHORT).show();
					mProgressDialog.dismiss();
				} else if (result.equals(""
						+ ServerInterface.ERROR_USER_NOT_EXIST)) {
					Toast.makeText(LoginScreen.this,
							R.string.login_err_username_not_exist,
							Toast.LENGTH_SHORT).show();
					mProgressDialog.dismiss();
				} else if (result.equals(""
						+ ServerInterface.ERROR_PASSWORD_WRONG)) {
					Toast.makeText(LoginScreen.this,
							R.string.login_err_password_wrong,
							Toast.LENGTH_SHORT).show();
					mProgressDialog.dismiss();
				} else if (result.equals(""
						+ ServerInterface.ERROR_USER_NOT_BIND)) {
					mProgressDialog.dismiss();
					Intent intent = new Intent(LoginScreen.this,
							RegisterScreen.class);
					intent.putExtras(msg.getData());
					startActivity(intent);
				} else {
					mProgressDialog.dismiss();
					try {
						JSONObject jsonObject = new JSONObject(result);
						if (jsonObject.optString("flag").equals(
								"" + ServerInterface.SUCCESS)) {
							//保存本地
							SharedPreferences sp = getSharedPreferences(
									PreferencesHelper.XML_NAME, 0);
							Editor editor = sp.edit();
							editor.putString(
									PreferencesHelper.XML_USER_ACCOUNT,
									jsonObject.optString("email"));
							editor.putString(PreferencesHelper.XML_USER_PASSWD,
									jsonObject.getString("pswd"));
							editor.commit();
							//保存至Application
							NoteApplication noteApplication = NoteApplication.getInstance();
							noteApplication.setUserName(jsonObject.optString("email"));
							noteApplication.setUserId(jsonObject.optInt("user_id"));
							noteApplication.setLogin(true);
							Log.i(TAG, "login success");
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

		};
	}

	/*
	 * 
	 * 初始化各个控件
	 */
	private void initViews() {
		mUserName = (EditText) findViewById(R.id.editText_login_username);
		mPassWord = (EditText) findViewById(R.id.editText_login_password);
		mBackButton = (Button) findViewById(R.id.screen_top_play_control_back);
		mBackButton.setOnClickListener(this);
		mLoginButton = (Button) findViewById(R.id.btn_login);
		mLoginButton.setOnClickListener(this);
		mLoginButton_sina = (TextView) findViewById(R.id.btn_login_sina);
		mLoginButton_sina.setOnClickListener(this);
		mLoginButton_qq = (TextView) findViewById(R.id.btn_login_qq);
		mLoginButton_qq.setOnClickListener(this);
		mLoginButton_renren = (TextView) findViewById(R.id.btn_login_renren);
		mLoginButton_renren.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.screen_top_play_control_back:
			finish();
			break;
		case R.id.btn_login:
			login();
			break;
		case R.id.btn_login_sina:
			bindSinaweibo();
			break;
		case R.id.btn_login_qq:
			bindQQweibo();
			break;
		case R.id.btn_login_renren:
			bindRenRen();
			break;
		default:
			break;
		}
	}

	private void showProgressDialog(){
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage("正在登录...");
		mProgressDialog.show();
	}
	
	/*
	 * 普通登录：输入微笔记账号密码
	 */
	private void login() {
		final String username = mUserName.getText().toString().trim();
		final String password = mPassWord.getText().toString().trim();
		if (!ServerInterface.isEmail(username)) {
			Toast.makeText(this, R.string.login_err_username_format,
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (!ServerInterface.isPswdValid(password)) {
			Toast.makeText(this, R.string.login_err_password_format,
					Toast.LENGTH_SHORT).show();
			return;
		}
		showProgressDialog();//显示进度框
		new Thread() {

			@Override
			public void run() {
				String result = ServerInterface.login(username, password);
				Message message = new Message();
				message.obj = result;
				mHandler.sendMessage(message);
			}

		}.start();
	}

	/*
	 * 其他登录方式 参数： 登录类型：新浪，QQ，人人 绑定的新浪，QQ,人人的用户id
	 */
	private void login_others(int type, String uid) {
		final int type_thread = type;
		final String uid_thread = uid;
		showProgressDialog();//显示进度框
		new Thread() {

			@Override
			public void run() {
				String result = ServerInterface.checkBinding(type_thread,
						uid_thread);
				Message message = new Message();
				message.obj = result;
				Bundle bundle = new Bundle();
				bundle.putInt("type", type_thread);
				bundle.putString("uid", uid_thread);
				message.setData(bundle);
				mHandler.sendMessage(message);
			}

		}.start();

	}

	/*
	 * 绑定新浪微博账号 Oauth2.0 隐式授权认证方式
	 */
	private void bindSinaweibo() {
		Weibo weibo = Weibo.getInstance();
		weibo.setupConsumerConfig(ShareSettingScreen.APPKEY_SINA,
				ShareSettingScreen.APPSECRET_SINA);
		weibo.setRedirectUrl("http://www.sina.com");// 此处使用的URL必须和新浪微博上应用提供的回调地址一样
		weibo.authorize(this, new AuthDialogListener());
	}

	/*
	 * 绑定腾讯 OAuth Version 2 授权认证方式
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
	 * 绑定人人账号
	 */
	private void bindRenRen() {
		Renren renren = new Renren(ShareSettingScreen.APPKEY_RENREN,
				ShareSettingScreen.APPSECRET_RENREN,
				ShareSettingScreen.APPID_RENREN, this);
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
				login_others(ServerInterface.LOGIN_TYPE_RENREN, "");
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
					login_others(ServerInterface.LOGIN_TYPE_QQ,
							oAuthV2.getOpenid());
				} else {
					Toast.makeText(this, "呃，出现了点问题，再试一次吧！", Toast.LENGTH_SHORT)
							.show();
				}
			}
		}
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
			login_others(ServerInterface.LOGIN_TYPE_SINA,
					values.getString("uid"));
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

package com.archermind.note.Screens;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Events.IEventHandler;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.CookieCrypt;
import com.archermind.note.Utils.NetworkUtils;
import com.archermind.note.Utils.PreferencesHelper;
import com.archermind.note.Utils.ServerInterface;
import com.renren.api.connect.android.AsyncRenren;
import com.renren.api.connect.android.Renren;
import com.renren.api.connect.android.common.AbstractRequestListener;
import com.renren.api.connect.android.exception.RenrenAuthError;
import com.renren.api.connect.android.exception.RenrenError;
import com.renren.api.connect.android.users.UsersGetInfoRequestParam;
import com.renren.api.connect.android.users.UsersGetInfoResponseBean;
import com.renren.api.connect.android.view.RenrenAuthListener;
import com.tencent.weibo.api.UserAPI;
import com.tencent.weibo.constants.OAuthConstants;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.webview.OAuthV2AuthorizeWebView;
import com.weibo.net.AsyncWeiboRunner;
import com.weibo.net.DialogError;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;

import android.R.integer;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginScreen extends Screen implements OnClickListener {

	public static final String USERINFO_KEY = "archwh001";// 保存用户信息的加密密钥
	private EditText mUserName;
	private EditText mPassWord;
	private Button mLoginButton;
	private Button mBackButton;
	private TextView mLoginButton_sina;
	private TextView mLoginButton_qq;
	private TextView mLoginButton_renren;
	private CheckBox mCheckBox;
	private TextView mFindPassword;
	private Handler mHandler;
	private NetThread mNetThread;
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
					dismissProgress();
				} else if (result.equals(""
						+ ServerInterface.ERROR_USER_NOT_EXIST)) {
					Toast.makeText(LoginScreen.this,
							R.string.login_err_username_not_exist,
							Toast.LENGTH_SHORT).show();
					dismissProgress();
				} else if (result.equals(""
						+ ServerInterface.ERROR_PASSWORD_WRONG)) {
					Toast.makeText(LoginScreen.this,
							R.string.login_err_password_wrong,
							Toast.LENGTH_SHORT).show();
					dismissProgress();
				} else if (result.equals("" + ServerInterface.USER_NOT_BIND)) {
					Bundle data = msg.getData();
					getOthersUserInfo(data.getInt("type"),
							data.getString("uid"));
				} else {
					dismissProgress();
					try {
						JSONObject jsonObject = new JSONObject(result);
						if (jsonObject.optString("flag").equals(
								"" + ServerInterface.SUCCESS)) {
							// 保存本地
							SharedPreferences sp = getSharedPreferences(
									PreferencesHelper.XML_NAME, 0);
							Editor editor = sp.edit();
							editor.putString(
									PreferencesHelper.XML_USER_ACCOUNT,
									jsonObject.optString("email"));
							editor.putString(PreferencesHelper.XML_USER_PASSWD,
									jsonObject.getString("pswd"));
							editor.commit();
							// 保存至Application
							NoteApplication noteApplication = NoteApplication
									.getInstance();
							ServiceManager.setUserName(jsonObject
									.optString("email"));
							ServiceManager.setUserId(jsonObject
									.optInt("user_id"));
							ServiceManager.setmAvatarurl(jsonObject
									.optString("portrait"));
							ServiceManager.setmNickname(jsonObject
									.optString("nickname"));
							ServiceManager.setmSex(jsonObject
									.optString("gender"));
							ServiceManager.setmRegion(jsonObject
									.optString("region"));
							ServiceManager.setmSina_nickname(jsonObject
									.optString("flag_sina"));
							ServiceManager.setmQQ_nickname(jsonObject
									.optString("flag_qq"));
							ServiceManager.setmRenren_nickname(jsonObject
									.optString("flag_renren"));
							ServiceManager.setLogin(true);
							Log.i(TAG, "login success");
							finish();
						}
					} catch (JSONException e) {
						Toast.makeText(LoginScreen.this, R.string.login_failed,
								Toast.LENGTH_SHORT).show();
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
		// mUserName.setText(PreferencesHelper.getSharedPreferences(this, 0)
		// .getString(PreferencesHelper.XML_USER_ACCOUNT, null));
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
		mFindPassword = (TextView) findViewById(R.id.login_findpassword);
		mCheckBox = (CheckBox) findViewById(R.id.login_checkBox);
		mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				SharedPreferences sp = getSharedPreferences(
						PreferencesHelper.XML_NAME, 0);
				Editor editor = sp.edit();
				editor.putBoolean(PreferencesHelper.XML_AUTOLOGIN,
						isChecked ? true : false);
				editor.commit();
			}
		});
		mCheckBox.setChecked(true);
		mFindPassword.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.screen_top_play_control_back:
			finish();
			break;
		case R.id.btn_login:
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				login();
			} else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.btn_login_sina:
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				boundSinaweibo();
			} else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.btn_login_qq:
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				boundQQweibo();
			} else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.btn_login_renren:
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				boundRenRen();
			} else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.login_findpassword:
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				findPassword();
			} else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		default:
			break;
		}
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
			mUserName.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.shake));
			return;
		}
		if (!ServerInterface.isPswdValid(password)) {
			Toast.makeText(this, R.string.login_err_password_format,
					Toast.LENGTH_SHORT).show();
			mPassWord.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.shake));
			return;
		}
		showProgress(null, getString(R.string.login_dialog_msg));// 显示进度框
		new Thread() {

			@Override
			public void run() {
				String result = "";
				try {
					result = ServerInterface.login(username,
							CookieCrypt.encrypt(USERINFO_KEY, password));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
		showProgress(null, getString(R.string.login_dialog_msg));// 显示进度框
		if (mNetThread == null || !mNetThread.isAlive()) {
			mNetThread = new NetThread(type, uid);
			mNetThread.start();
		}
	}

	/*
	 * 绑定新浪微博账号 Oauth2.0 隐式授权认证方式
	 */
	private void boundSinaweibo() {
		Toast.makeText(this, "请稍候...",
				Toast.LENGTH_SHORT).show();
		Weibo weibo = Weibo.getInstance();
		weibo.setupConsumerConfig(AccountScreen.APPKEY_SINA,
				AccountScreen.APPSECRET_SINA);
		weibo.setRedirectUrl("http://www.sina.com");// 此处使用的URL必须和新浪微博上应用提供的回调地址一样
		weibo.authorize(this, new WeiboDialogListener() {

			@Override
			public void onComplete(Bundle values) {
				Log.i(TAG,
						values.getString("access_token") + "  "
								+ values.getString("expires_in") + "  "
								+ values.getString("uid"));
				SharedPreferences sp = getSharedPreferences(
						PreferencesHelper.XML_NAME, 0);
				Editor editor = sp.edit();
				editor.putString(PreferencesHelper.XML_SINA_ACCESS_TOKEN,
						values.getString("access_token"));
				editor.commit();
				if (values.getString("uid") != null
						&& !values.getString("uid").equals(""))
					login_others(ServerInterface.LOGIN_TYPE_SINA,
							values.getString("uid"));
			}

			@Override
			public void onWeiboException(WeiboException e) {
				Toast.makeText(LoginScreen.this, R.string.login_failed,
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onError(DialogError e) {
				Toast.makeText(LoginScreen.this, R.string.login_failed,
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onCancel() {
				// TODO Auto-generated method stub
			}

		});
	}

	/*
	 * 绑定腾讯 OAuth Version 2 授权认证方式
	 */
	private void boundQQweibo() {
		OAuthV2 oAuthV2 = new OAuthV2("http://www.archermind.com");
		oAuthV2.setClientId(AccountScreen.APPKEY_QQ);
		oAuthV2.setClientSecret(AccountScreen.APPSECRET_QQ);
		Intent intent = new Intent(this, OAuthV2AuthorizeWebView.class);
		intent.putExtra("oauth", oAuthV2);
		startActivityForResult(intent, 1);

	}

	/*
	 * 绑定人人账号
	 */
	private void boundRenRen() {
		final Renren renren = new Renren(AccountScreen.APPKEY_RENREN,
				AccountScreen.APPSECRET_RENREN, AccountScreen.APPID_RENREN,
				this);
		renren.logout(this);
		renren.authorize(this, new RenrenAuthListener() {

			@Override
			public void onRenrenAuthError(RenrenAuthError renrenAuthError) {
				Toast.makeText(LoginScreen.this, R.string.login_failed,
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onComplete(Bundle values) {
				Log.i(TAG, values.toString());
				Log.i(TAG, values.getString("access_token"));
				Log.i(TAG, values.getString("expires_in"));
				SharedPreferences sp = getSharedPreferences(
						PreferencesHelper.XML_NAME, 0);
				Editor editor = sp.edit();
				editor.putString(PreferencesHelper.XML_RENREN_ACCESS_TOKEN,
						values.getString("access_token"));
				editor.commit();
				Log.i(TAG, "renrenuid:" + renren.getCurrentUid());
				if (renren.isSessionKeyValid()) {
					login_others(ServerInterface.LOGIN_TYPE_RENREN,
							String.valueOf(renren.getCurrentUid()));
				} else {
					Toast.makeText(LoginScreen.this, R.string.login_failed,
							Toast.LENGTH_SHORT).show();
				}
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
	 * 找回密码
	 */
	private void findPassword() {
		String username = mUserName.getText().toString().trim();
		if (!ServerInterface.isEmail(username)) {
			Toast.makeText(this, R.string.login_err_username_format,
					Toast.LENGTH_SHORT).show();
			mUserName.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.shake));
		} else {
			FindPswdTask findPswdTask = new FindPswdTask();
			findPswdTask.execute(username);
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
					editor.putString(PreferencesHelper.XML_QQ_ACCESS_TOKEN,
							oAuthV2.getAccessToken());
					editor.putString(PreferencesHelper.XML_QQ_OPENID,
							oAuthV2.getOpenid());
					editor.commit();
					login_others(ServerInterface.LOGIN_TYPE_QQ,
							oAuthV2.getOpenid());
				} else {
					Toast.makeText(this, R.string.login_failed,
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	/*
	 * 获取用户新浪，腾讯，人人的昵称
	 */
	private void getOthersUserInfo(final int type, final String uid) {
		if (type == ServerInterface.LOGIN_TYPE_SINA) {
			Weibo weibo = Weibo.getInstance();
			String url = Weibo.SERVER + "users/show.json";
			WeiboParameters bundle = new WeiboParameters();
			bundle.add(
					"access_token",
					getSharedPreferences(PreferencesHelper.XML_NAME, 0)
							.getString(PreferencesHelper.XML_SINA_ACCESS_TOKEN,
									null));
			bundle.add("uid", uid);
			AsyncWeiboRunner weiboRunner = new AsyncWeiboRunner(weibo);
			weiboRunner.request(this, url, bundle, Utility.HTTPMETHOD_GET,
					new AsyncWeiboRunner.RequestListener() {

						@Override
						public void onIOException(IOException e) {
							showToast_Async(R.string.login_failed);
							dismissProgress();
							e.printStackTrace();
						}

						@Override
						public void onError(WeiboException e) {
							showToast_Async(R.string.login_failed);
							dismissProgress();
							e.printStackTrace();
						}

						@Override
						public void onComplete(String response) {
							try {
								Log.i(TAG, "获取的sina用户信息json:" + response);
								JSONObject jsonObject = new JSONObject(response);
								String nickname = jsonObject
										.optString("screen_name");
								String sex = jsonObject.optString("gender");
								String location = jsonObject
										.optString("location");
								Intent intent = new Intent(LoginScreen.this,
										RegisterScreen.class);
								intent.putExtra("type", type);
								intent.putExtra("bin_uid", uid);
								intent.putExtra("bin_nickname", nickname);
								intent.putExtra("sex", sex);
								intent.putExtra("location", location);
								dismissProgress(); // 消除进度框，准备跳转到注册页面
								startActivity(intent);
								finish();
							} catch (JSONException e) {
								showToast_Async(R.string.login_failed);
								dismissProgress();
							}
						}
					});
		} else if (type == ServerInterface.LOGIN_TYPE_QQ) {
			QQAsyncTask qqAsyncTask = new QQAsyncTask();
			qqAsyncTask.execute(
					getSharedPreferences(PreferencesHelper.XML_NAME, 0)
							.getString(PreferencesHelper.XML_QQ_ACCESS_TOKEN,
									null), uid);

		} else if (type == ServerInterface.LOGIN_TYPE_RENREN) {
			Renren renren = new Renren(AccountScreen.APPKEY_RENREN,
					AccountScreen.APPSECRET_RENREN, AccountScreen.APPID_RENREN,
					this);
			renren.updateAccessToken(getSharedPreferences(
					PreferencesHelper.XML_NAME, 0).getString(
					PreferencesHelper.XML_RENREN_ACCESS_TOKEN, null));
			AsyncRenren asyncRenren = new AsyncRenren(renren);
			UsersGetInfoRequestParam param = new UsersGetInfoRequestParam(
					new String[] { uid }, UsersGetInfoRequestParam.FIELDS_ALL);
			asyncRenren.getUsersInfo(param,
					new AbstractRequestListener<UsersGetInfoResponseBean>() {

						@Override
						public void onRenrenError(RenrenError renrenError) {
							showToast_Async(R.string.login_failed);
							dismissProgress();
							renrenError.printStackTrace();
						}

						@Override
						public void onFault(Throwable fault) {
							showToast_Async(R.string.login_failed);
							dismissProgress();
							fault.printStackTrace();
						}

						@Override
						public void onComplete(UsersGetInfoResponseBean bean) {
							Log.i(TAG, "获取的人人用户信息：" + bean.toString());
							try {
								String nickname = bean.getUsersInfo().get(0)
										.getName();
								String sex = String.valueOf(bean.getUsersInfo()
										.get(0).getSex());
								// 由于人人网的接口问题，暂无法获得地区...
								// HomeTownLocation location = bean
								// .getUsersInfo().get(0)
								// .getHomeTownLocation().get(0);
								Intent intent = new Intent(LoginScreen.this,
										RegisterScreen.class);
								intent.putExtra("type", type);
								intent.putExtra("bin_uid", uid);
								intent.putExtra("bin_nickname", nickname);
								intent.putExtra("sex", sex);
								intent.putExtra("location", "");
								dismissProgress(); // 消除进度框，准备跳转到注册页面
								startActivity(intent);
								finish();
							} catch (Exception e) {
								showToast_Async(R.string.login_failed);
								dismissProgress();
								e.printStackTrace();
							}
						}
					});
		}

	}

	
	
	/*
	 * 用于获取腾讯用户昵称的异步类
	 */
	class QQAsyncTask extends AsyncTask<String, integer, String> {

		@Override
		protected String doInBackground(String... params) {
			OAuthV2 oAuthV2 = new OAuthV2("http://www.archermind.com");
			oAuthV2.setClientId(AccountScreen.APPKEY_QQ);
			oAuthV2.setAccessToken(params[0]);
			oAuthV2.setOpenid(params[1]);
			UserAPI userAPI = new UserAPI(OAuthConstants.OAUTH_VERSION_2_A);
			String response;
			try {
				response = userAPI.info(oAuthV2, "json");// 调用QWeiboSDK获取用户信息
				Log.i(TAG, "获取的腾讯用户信息json:" + response);
				return response;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				userAPI.shutdownConnection();
			}

		}

		@Override
		protected void onPostExecute(String result) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(result);
				String data = jsonObject.optString("data");
				jsonObject = new JSONObject(data);
				String nickname = jsonObject.optString("nick");
				String sex = jsonObject.optString("sex");
				String location = jsonObject.optString("location");
				location = location.replace("中国 ", "");
				Intent intent = new Intent(LoginScreen.this,
						RegisterScreen.class);
				intent.putExtra("type", ServerInterface.LOGIN_TYPE_QQ);
				intent.putExtra(
						"bin_uid",
						getSharedPreferences(PreferencesHelper.XML_NAME, 0)
								.getString(PreferencesHelper.XML_QQ_OPENID,
										null));
				// intent.putExtra("url", jsonObject.optString("head") +
				// "/100");
				intent.putExtra("bin_nickname", nickname);
				intent.putExtra("sex", sex);
				intent.putExtra("location", location);
				dismissProgress(); // 消除进度框，准备跳转到注册页面
				startActivity(intent);
				finish();
			} catch (JSONException e) {
				Toast.makeText(NoteApplication.getContext(),
						R.string.login_failed, Toast.LENGTH_SHORT).show();
				dismissProgress();
				e.printStackTrace();
			}
		}
	}

	/*
	 * 网络线程，用于新浪，腾讯，人人账号的登录
	 */
	private class NetThread extends Thread {
		int type;
		String uid;

		public NetThread(int type, String uid) {
			super();
			this.type = type;
			this.uid = uid;
		}

		@Override
		public void run() {
			String result = ServerInterface.checkBounding(type, uid);
			Message message = new Message();
			message.obj = result;
			Bundle bundle = new Bundle();
			bundle.putInt("type", type);
			bundle.putString("uid", uid);
			message.setData(bundle);
			mHandler.sendMessage(message);
		}

	}

	/*
	 * 用于找回密码的异步类
	 */
	private class FindPswdTask extends AsyncTask<String, integer, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Toast.makeText(LoginScreen.this,
					R.string.login_findpassword_prepare, Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		protected String doInBackground(String... params) {
			return ServerInterface.FindPassword(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result.equals("0")) {
				Toast.makeText(LoginScreen.this,
						R.string.login_findpassword_success, Toast.LENGTH_SHORT)
						.show();
			} else if (result.equals("-1")) {
				Toast.makeText(LoginScreen.this,
						R.string.login_findpassword_err_user_none,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(LoginScreen.this,
						R.string.login_findpassword_failed, Toast.LENGTH_SHORT)
						.show();
			}
		}

	}

	/*
	 * 在异步线程中更新ui
	 */
	private void showToast_Async(final int id) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(NoteApplication.getContext(), id,
						Toast.LENGTH_SHORT).show();
			}
		});
	}
}

package com.archermind.note.Screens;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Screens.LoginScreen.QQAsyncTask;
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

public class AccountScreen extends Screen implements OnClickListener {

	public static final String APPKEY_SINA = "3366130678";// 申请的新浪KEY
	public static final String APPSECRET_SINA = "8bdbc9095e53791249c47db21dad550c"; // 申请的新浪SECRET
	public static final String APPKEY_QQ = "801210743";// 申请的腾讯KEY
	public static final String APPSECRET_QQ = "bee5553c65ee5ebb84f08f0c45630c4d"; // 申请的腾讯SECRET
	public static final String APPKEY_RENREN = "87e5e8e6175b46519fe9eb40968ba2dc";// 申请的人人KEY
	public static final String APPSECRET_RENREN = "3f093253cf344d3099e6df1e42f1d661"; // 申请的人人SECRET
	public static final String APPID_RENREN = "207067";
	private TextView mSina_unbound;
	private TextView mSina_bounded;
	private TextView mQQ_unbound;
	private TextView mQQ_bounded;
	private TextView mRenren_unbound;
	private TextView mRenren_bounded;

	private TextView mNewPasswdLabel;
	private TextView mConfirmPasswdLabel;
	private EditText mNewPasswd;
	private EditText mConfirmPasswd;

	private String mPswd;
	private ProgressBar mProgressBar;
	private SharedPreferences mPreferences;
	private int mType; // 绑定账号的类型
	private String mNickname; // 绑定账号的昵称
	private NetThread mNetThread;
	private static final String TAG = "AccountScreen";
	private InputMethodManager imm;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			dismissProgress();
			switch (msg.what) {
			case ServerInterface.ERROR_SERVER_INTERNAL:
				Toast.makeText(NoteApplication.getContext(),
						R.string.register_err_server_internal,
						Toast.LENGTH_SHORT).show();
				break;
			case ServerInterface.COOKIES_ERROR:
				NoteApplication.getInstance().setLogin(false);
				Toast.makeText(NoteApplication.getContext(),
						R.string.cookies_error, Toast.LENGTH_SHORT).show();
				break;
			case ServerInterface.SUCCESS:
				if (mType == ServerInterface.LOGIN_TYPE_SINA) {
					NoteApplication.getInstance().setmSina_nickname(mNickname);
					Toast.makeText(NoteApplication.getContext(),
							R.string.account_bound_success_sina,
							Toast.LENGTH_LONG).show();
				} else if (mType == ServerInterface.LOGIN_TYPE_QQ) {
					NoteApplication.getInstance().setmQQ_nickname(mNickname);
					Toast.makeText(NoteApplication.getContext(),
							R.string.account_bound_success_qq,
							Toast.LENGTH_LONG).show();
				} else if (mType == ServerInterface.LOGIN_TYPE_RENREN) {
					NoteApplication.getInstance()
							.setmRenren_nickname(mNickname);
					Toast.makeText(NoteApplication.getContext(),
							R.string.account_bound_success_renren,
							Toast.LENGTH_LONG).show();
				}
				onResume();// 刷新界面
				break;
			case ServerInterface.BOUNDACCOUNT_FAILED:
				Toast.makeText(NoteApplication.getContext(),
						R.string.account_bound_failed, Toast.LENGTH_SHORT)
						.show();
				break;
			case ServerInterface.BOUNDACCOUNT_FAILED_EXIST:
				Toast.makeText(NoteApplication.getContext(),
						R.string.account_bound_failed_exist, Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				break;
			}
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_screen);

		initViews();// 账号绑定相关的视图初始化

		Button btnback = (Button) this.findViewById(R.id.back);
		btnback.setOnClickListener(this);
		mProgressBar = (ProgressBar) findViewById(R.id.account_progressBar);

		mNewPasswdLabel = (TextView) this.findViewById(R.id.new_passwd_label);
		mConfirmPasswdLabel = (TextView) this
				.findViewById(R.id.confirm_passwd_label);
		mNewPasswd = (EditText) this.findViewById(R.id.new_passwd);
		mConfirmPasswd = (EditText) this.findViewById(R.id.confirm_passwd);
		CheckBox cb = (CheckBox) this.findViewById(R.id.use_change_passwd);
		final View user_passwd_layout = (View) this
				.findViewById(R.id.user_passwd_layout);
		final Button btnConfirmChange = (Button) this
				.findViewById(R.id.confirm_change);

		TextView user_account = (TextView) this.findViewById(R.id.user_account);
		user_account.setText(NoteApplication.getInstance().getUserName());

		mNewPasswdLabel.setTextColor(Color.GRAY);
		mConfirmPasswdLabel.setTextColor(Color.GRAY);
		mNewPasswd.setTextColor(Color.GRAY);
		mConfirmPasswd.setTextColor(Color.GRAY);
		setEditable(mNewPasswd, false, false);
		setEditable(mConfirmPasswd, false, false);
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		cb.setChecked(false);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if (arg1) {
					setEditable(mNewPasswd, true, true);
					setEditable(mConfirmPasswd, true, false);

					mNewPasswdLabel.setTextColor(Color.BLACK);
					mConfirmPasswdLabel.setTextColor(Color.BLACK);
					mNewPasswd.setTextColor(Color.BLACK);
					mConfirmPasswd.setTextColor(Color.BLACK);
					user_passwd_layout
							.setBackgroundResource(R.drawable.setting_background);
					btnConfirmChange.setEnabled(true);

				} else {
					setEditable(mNewPasswd, false, false);
					setEditable(mConfirmPasswd, false, false);

					mNewPasswdLabel.setTextColor(Color.GRAY);
					mConfirmPasswdLabel.setTextColor(Color.GRAY);
					mNewPasswd.setTextColor(Color.GRAY);
					mConfirmPasswd.setTextColor(Color.GRAY);
					user_passwd_layout
							.setBackgroundResource(R.drawable.setting_background_gray);
					btnConfirmChange.setEnabled(false);
				}
			}
		});

		btnConfirmChange.setOnClickListener(this);
		btnConfirmChange.setEnabled(false);

		mPreferences = PreferencesHelper.getSharedPreferences(this, 0);

	}

	private void setEditable(EditText mEdit, boolean enable,
			boolean requestFocus) {
		mEdit.setEnabled(enable);
		mEdit.setCursorVisible(enable);
		mEdit.setFocusableInTouchMode(enable);
		if (enable) {
			if (requestFocus) {
				mEdit.requestFocus();
				IBinder windowToken = mEdit.getWindowToken();
				if (windowToken != null) {
					imm.toggleSoftInputFromWindow(windowToken, 0,
							InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
		} else {
			if (mEdit.hasFocus()) {
				mEdit.clearFocus();
				IBinder windowToken = mEdit.getWindowToken();
				if (windowToken != null) {
					imm.hideSoftInputFromWindow(windowToken, 0);
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;
		case R.id.confirm_change:
			String password = mNewPasswd.getText().toString().trim();
			String pswdconfirm = mConfirmPasswd.getText().toString().trim();
			if (!ServerInterface.isPswdValid(password)) {
				Toast.makeText(this, R.string.register_err_password_format,
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (!password.equals(pswdconfirm)) {
				Toast.makeText(this, R.string.register_err_pswdconfirm,
						Toast.LENGTH_SHORT).show();
				return;
			}
			mPswd = password;
			ModifyPswdTask modifyPswdTask = new ModifyPswdTask();
			modifyPswdTask.execute(password);
			break;
		case R.id.acount_sina_unbound:
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				boundSinaweibo();
			} else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.acount_qq_unbound:
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				boundQQweibo();
			} else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.acount_renren_unbound:
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				boundRenRen();
			} else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		}
	}

	private void initViews() {
		mSina_unbound = (TextView) findViewById(R.id.acount_sina_unbound);
		mSina_unbound.setOnClickListener(this);
		mSina_bounded = (TextView) findViewById(R.id.acount_sina_bounded);

		mQQ_unbound = (TextView) findViewById(R.id.acount_qq_unbound);
		mQQ_unbound.setOnClickListener(this);
		mQQ_bounded = (TextView) findViewById(R.id.acount_qq_bounded);

		mRenren_unbound = (TextView) findViewById(R.id.acount_renren_unbound);
		mRenren_unbound.setOnClickListener(this);
		mRenren_bounded = (TextView) findViewById(R.id.acount_renren_bounded);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!NoteApplication.getInstance().getmSina_nickname().equals("")) {
			mSina_unbound.setVisibility(View.GONE);
			mSina_bounded.setVisibility(View.VISIBLE);
			mSina_bounded.setText(NoteApplication.getInstance()
					.getmSina_nickname());
		}
		if (!NoteApplication.getInstance().getmQQ_nickname().equals("")) {
			mQQ_unbound.setVisibility(View.GONE);
			mQQ_bounded.setVisibility(View.VISIBLE);
			mQQ_bounded
					.setText(NoteApplication.getInstance().getmQQ_nickname());
		}
		if (!NoteApplication.getInstance().getmRenren_nickname().equals("")) {
			mRenren_unbound.setVisibility(View.GONE);
			mRenren_bounded.setVisibility(View.VISIBLE);
			mRenren_bounded.setText(NoteApplication.getInstance()
					.getmRenren_nickname());
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
					Editor editor = mPreferences.edit();
					editor.putString(PreferencesHelper.XML_QQ_ACCESS_TOKEN,
							oAuthV2.getAccessToken());
					editor.putString(PreferencesHelper.XML_QQ_OPENID,
							oAuthV2.getOpenid());
					editor.commit();
					getOthersUserInfo(ServerInterface.LOGIN_TYPE_QQ,
							oAuthV2.getOpenid());
				} else {
					Toast.makeText(this, getString(R.string.login_failed),
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	private void boundAccount(int userId, int type, String bin_uid,
			String bin_nickname) {
		if (mNetThread == null || !mNetThread.isAlive()) {
			mNetThread = new NetThread(userId, type, bin_uid, bin_nickname);
			mNetThread.start();
		}
	}

	/*
	 * 绑定新浪微博 Oauth2.0 隐式授权认证方式
	 */
	private void boundSinaweibo() {
		mType = ServerInterface.LOGIN_TYPE_SINA;
		Weibo weibo = Weibo.getInstance();
		weibo.setupConsumerConfig(APPKEY_SINA, APPSECRET_SINA);
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
						&& !values.getString("uid").equals("")) {
					getOthersUserInfo(ServerInterface.LOGIN_TYPE_SINA,
							values.getString("uid"));
				}

			}

			@Override
			public void onWeiboException(WeiboException e) {
				Toast.makeText(AccountScreen.this,
						R.string.account_bound_failed, Toast.LENGTH_SHORT)
						.show();
				e.printStackTrace();
			}

			@Override
			public void onError(DialogError e) {
				Toast.makeText(AccountScreen.this,
						R.string.account_bound_failed, Toast.LENGTH_SHORT)
						.show();
				e.printStackTrace();
			}

			@Override
			public void onCancel() {
				// TODO Auto-generated method stub
			}

		});
	}

	/*
	 * 绑定腾讯微博 OAuth Version 2 授权认证方式
	 */
	private void boundQQweibo() {
		mType = ServerInterface.LOGIN_TYPE_QQ;
		OAuthV2 oAuthV2 = new OAuthV2("http://www.archermind.com");
		oAuthV2.setClientId(APPKEY_QQ);
		oAuthV2.setClientSecret(APPSECRET_QQ);
		Intent intent = new Intent(this, OAuthV2AuthorizeWebView.class);
		intent.putExtra("oauth", oAuthV2);
		startActivityForResult(intent, 1);

	}

	/*
	 * 绑定人人账号
	 */
	private void boundRenRen() {
		mType = ServerInterface.LOGIN_TYPE_RENREN;
		final Renren renren = new Renren(APPKEY_RENREN, APPSECRET_RENREN,
				APPID_RENREN, this);
		renren.logout(this);
		renren.authorize(this, new RenrenAuthListener() {

			@Override
			public void onRenrenAuthError(RenrenAuthError renrenAuthError) {
				Toast.makeText(AccountScreen.this,
						R.string.account_bound_failed, Toast.LENGTH_SHORT)
						.show();
			}

			@Override
			public void onComplete(Bundle values) {
				Log.e(TAG, values.toString());
				Log.e(TAG, values.getString("access_token"));
				Log.e(TAG, values.getString("expires_in"));
				Editor editor = mPreferences.edit();
				editor.putString(PreferencesHelper.XML_RENREN_ACCESS_TOKEN,
						values.getString("access_token"));
				editor.commit();
				if (renren.isSessionKeyValid()) {
					getOthersUserInfo(ServerInterface.LOGIN_TYPE_RENREN,
							String.valueOf(renren.getCurrentUid()));

				} else {
					Toast.makeText(AccountScreen.this,
							R.string.account_bound_failed, Toast.LENGTH_SHORT)
							.show();
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

	// 获取用户新浪，腾讯，人人的昵称
	private void getOthersUserInfo(final int type, final String uid) {
		showProgress(null, getString(R.string.account_bound_progross));// 显示“正在绑定“进度框
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
							showToast_Async(R.string.account_bound_failed);
							dismissProgress();
							e.printStackTrace();
						}

						@Override
						public void onError(WeiboException e) {
							showToast_Async(R.string.account_bound_failed);
							dismissProgress();
							e.printStackTrace();
						}

						@Override
						public void onComplete(String response) {
							try {
								Log.i(TAG, "获取的sina用户信息json:" + response);
								JSONObject jsonObject = new JSONObject(response);
								mNickname = jsonObject.optString("screen_name");
								boundAccount(NoteApplication.getInstance()
										.getUserId(),
										ServerInterface.LOGIN_TYPE_SINA, uid,
										mNickname);
							} catch (JSONException e) {
								showToast_Async(R.string.account_bound_failed);
								dismissProgress();
								e.printStackTrace();
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
							showToast_Async(R.string.account_bound_failed);
							dismissProgress();
							renrenError.printStackTrace();
						}

						@Override
						public void onFault(Throwable fault) {
							showToast_Async(R.string.account_bound_failed);
							dismissProgress();
							fault.printStackTrace();
						}

						@Override
						public void onComplete(UsersGetInfoResponseBean bean) {
							Log.i(TAG, "获取的人人用户信息：" + bean.toString());
							try {
								mNickname = bean.getUsersInfo().get(0)
										.getName();
								boundAccount(NoteApplication.getInstance()
										.getUserId(),
										ServerInterface.LOGIN_TYPE_RENREN, uid,
										mNickname);
							} catch (Exception e) {
								showToast_Async(R.string.account_bound_failed);
								dismissProgress();
								e.printStackTrace();
							}
						}
					});
		}

	}

	// 用于获取腾讯用户昵称的异步类
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
				String uid = jsonObject.optString("openid");
				mNickname = jsonObject.optString("nick");
				boundAccount(NoteApplication.getInstance().getUserId(),
						ServerInterface.LOGIN_TYPE_QQ, uid, mNickname);
			} catch (JSONException e) {
				Toast.makeText(NoteApplication.getContext(),
						R.string.account_bound_failed, Toast.LENGTH_SHORT)
						.show();
				dismissProgress();
				e.printStackTrace();
			}
		}
	}

	class ModifyPswdTask extends AsyncTask<String, integer, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressBar.setVisibility(View.VISIBLE);
			Toast.makeText(AccountScreen.this, R.string.update_progress,
					Toast.LENGTH_SHORT).show();
		}

		@Override
		protected String doInBackground(String... params) {
			return ServerInterface.modifyPassword(NoteApplication.getInstance()
					.getUserName(), params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result.equals("" + ServerInterface.SUCCESS)) {
				Editor editor = mPreferences.edit();
				editor.putString(PreferencesHelper.XML_USER_PASSWD, mPswd);
				editor.commit();
				Toast.makeText(AccountScreen.this, R.string.update_success,
						Toast.LENGTH_SHORT).show();
			} else if (result.equals("" + ServerInterface.COOKIES_ERROR)) {
				NoteApplication.getInstance().setLogin(false);
				Toast.makeText(NoteApplication.getContext(),
						R.string.cookies_error, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(AccountScreen.this, R.string.update_failed,
						Toast.LENGTH_SHORT).show();
			}
			mProgressBar.setVisibility(View.GONE);
		}

	}

	private class NetThread extends Thread {
		int userId;// 微笔记userid
		int type;// 绑定账号类型
		String bin_uid;// 绑定账号的uid
		String bin_nickname;// 绑定账号的昵称

		public NetThread(int userid, int type, String bin_uid,
				String bin_nickname) {
			super();
			this.userId = userid;
			this.type = type;
			this.bin_uid = bin_uid;
			this.bin_nickname = bin_nickname;
		}

		@Override
		public void run() {
			String result = ServerInterface.boundAccount(userId, type, bin_uid,
					bin_nickname);
			try {
				mHandler.sendEmptyMessage(Integer.parseInt(result));
			} catch (Exception e) {
				mHandler.sendEmptyMessage(ServerInterface.BOUNDACCOUNT_FAILED);
			}
		}

	}

	private void showToast_Async(final int id) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(NoteApplication.getContext(), id,
						Toast.LENGTH_SHORT).show();
			}
		});
	}

}

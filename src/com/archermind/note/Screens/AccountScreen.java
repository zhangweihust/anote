package com.archermind.note.Screens;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Utils.NetworkUtils;
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

	private Context mContext;

	private TextView mNewPasswdLabel;
	private TextView mConfirmPasswdLabel;
	private EditText mNewPasswd;
	private EditText mConfirmPasswd;

	private String mPasswd;
	private SharedPreferences mPreferences;
	private static final int BOUNDACCOUNT_OK = 1;
	private static final int BOUNDACCOUNT_FAILED = -1;
	private static final int BOUNDACCOUNT_FAILED_EXIST = -3;
	private String mCurrentTask = null;
	private NetThread mNetThread;
	private static final String TAG = "AccountScreen";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_screen);

		mContext = AccountScreen.this;

		initViews();// 账号绑定相关的视图初始化

		ImageButton btnback = (ImageButton) this.findViewById(R.id.back);
		btnback.setOnClickListener(this);

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
		mNewPasswd.setEnabled(false);
		mConfirmPasswd.setEnabled(false);
		cb.setChecked(false);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if (arg1) {
					mNewPasswd.setEnabled(true);
					mConfirmPasswd.setEnabled(true);

					mNewPasswdLabel.setTextColor(Color.BLACK);
					mConfirmPasswdLabel.setTextColor(Color.BLACK);
					mNewPasswd.setTextColor(Color.BLACK);
					mConfirmPasswd.setTextColor(Color.BLACK);
					user_passwd_layout
							.setBackgroundResource(R.drawable.setting_background);
					btnConfirmChange.setEnabled(true);

				} else {
					mNewPasswd.setEnabled(false);
					mConfirmPasswd.setEnabled(false);

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

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case ServerInterface.SUCCESS:
				Editor editor = mPreferences.edit();
				editor.putString(PreferencesHelper.XML_USER_PASSWD, mPasswd);
				editor.commit();
				Toast.makeText(NoteApplication.getContext(),
						R.string.confirm_success, Toast.LENGTH_SHORT).show();
				AccountScreen.this.finish();
				break;
			case ServerInterface.ERROR_ACCOUNT_EXIST:
				Toast.makeText(NoteApplication.getContext(),
						R.string.register_err_account_exist, Toast.LENGTH_SHORT)
						.show();
				AccountScreen.this.finish();
				break;
			case ServerInterface.ERROR_SERVER_INTERNAL:
				Toast.makeText(NoteApplication.getContext(),
						R.string.register_err_server_internal,
						Toast.LENGTH_SHORT).show();
				AccountScreen.this.finish();
				break;
			case BOUNDACCOUNT_OK:
				dismissProgress();
				if (mCurrentTask.equals("sina")) {
					NoteApplication.getInstance().setmBound_Sina(true);
					mSina_unbound.setVisibility(View.GONE);
					mSina_bounded.setVisibility(View.VISIBLE);
					Toast.makeText(NoteApplication.getContext(),
							R.string.account_bound_success_sina,
							Toast.LENGTH_LONG).show();
				} else if (mCurrentTask.equals("qq")) {
					NoteApplication.getInstance().setmBound_QQ(true);
					mQQ_unbound.setVisibility(View.GONE);
					mQQ_bounded.setVisibility(View.VISIBLE);
					Toast.makeText(NoteApplication.getContext(),
							R.string.account_bound_success_qq,
							Toast.LENGTH_LONG).show();
				} else if (mCurrentTask.equals("renren")) {
					NoteApplication.getInstance().setmBound_Renren(true);
					mRenren_unbound.setVisibility(View.GONE);
					mRenren_bounded.setVisibility(View.VISIBLE);
					Toast.makeText(NoteApplication.getContext(),
							R.string.account_bound_success_renren,
							Toast.LENGTH_LONG).show();
				}
				break;
			case BOUNDACCOUNT_FAILED:
				dismissProgress();
				Toast.makeText(NoteApplication.getContext(),
						R.string.account_bound_failed, Toast.LENGTH_SHORT)
						.show();
				break;
			case BOUNDACCOUNT_FAILED_EXIST:
				dismissProgress();
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
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;
		case R.id.confirm_change:
			final String password = mNewPasswd.getText().toString().trim();
			final String pswdconfirm = mConfirmPasswd.getText().toString()
					.trim();
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
			new Thread() {

				@Override
				public void run() {
					int result = ServerInterface.modifyPassword(NoteApplication
							.getInstance().getUserName(), password);
					mPasswd = password;
					mHandler.sendEmptyMessage(result);
				}

			}.start();
			break;
		case R.id.acount_sina_unbound:
			if(NetworkUtils.getNetworkState(this)!= NetworkUtils.NETWORN_NONE){
				boundSinaweibo();
			}else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
				.show();
			}
			break;
		case R.id.acount_qq_unbound:
			if(NetworkUtils.getNetworkState(this)!= NetworkUtils.NETWORN_NONE){
				boundQQweibo();
			}else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
				.show();
			}
			break;
		case R.id.acount_renren_unbound:
			if(NetworkUtils.getNetworkState(this)!= NetworkUtils.NETWORN_NONE){
				boundRenRen();
			}else {
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
		if(NoteApplication.getInstance().ismBound_Sina()){
			mSina_unbound.setVisibility(View.GONE);
			mSina_bounded.setVisibility(View.VISIBLE);
		}

		mQQ_unbound = (TextView) findViewById(R.id.acount_qq_unbound);
		mQQ_unbound.setOnClickListener(this);
		mQQ_bounded = (TextView) findViewById(R.id.acount_qq_bounded);
		if(NoteApplication.getInstance().ismBound_QQ()){
			mQQ_unbound.setVisibility(View.GONE);
			mQQ_bounded.setVisibility(View.VISIBLE);
		}

		mRenren_unbound = (TextView) findViewById(R.id.acount_renren_unbound);
		mRenren_unbound.setOnClickListener(this);
		mRenren_bounded = (TextView) findViewById(R.id.acount_renren_bounded);
		if(NoteApplication.getInstance().ismBound_Renren()){
			mRenren_unbound.setVisibility(View.GONE);
			mRenren_bounded.setVisibility(View.VISIBLE);
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
					boundAccount(NoteApplication.getInstance().getUserId(),
							ServerInterface.LOGIN_TYPE_QQ, oAuthV2.getOpenid());
				} else {
					Toast.makeText(this, getString(R.string.login_failed),
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	private void boundAccount(int userId, int type, String uid) {
		showProgress(null, getString(R.string.account_bound_progross));
		if (mNetThread == null || !mNetThread.isAlive()) {
			mNetThread = new NetThread(userId, type, uid);
			mNetThread.start();
		}
	}

	/*
	 * 绑定新浪微博 Oauth2.0 隐式授权认证方式
	 */
	private void boundSinaweibo() {
		mCurrentTask = "sina";
		Weibo weibo = Weibo.getInstance();
		weibo.setupConsumerConfig(APPKEY_SINA, APPSECRET_SINA);
		weibo.setRedirectUrl("http://www.sina.com");// 此处使用的URL必须和新浪微博上应用提供的回调地址一样
		weibo.authorize(this, new AuthDialogListener());
	}

	/*
	 * 绑定腾讯微博 OAuth Version 2 授权认证方式
	 */
	private void boundQQweibo() {
		mCurrentTask = "qq";
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
		mCurrentTask = "renren";
		final Renren renren = new Renren(APPKEY_RENREN, APPSECRET_RENREN,
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
				Editor editor = mPreferences.edit();
				editor.putString(PreferencesHelper.XML_RENREN_ACCESS_TOKEN,
						values.getString("access_token"));
				editor.commit();
				if (renren.isSessionKeyValid()) {
					boundAccount(NoteApplication.getInstance().getUserId(),
							ServerInterface.LOGIN_TYPE_RENREN,
							String.valueOf(renren.getCurrentUid()));
				} else {
					Toast.makeText(AccountScreen.this, R.string.login_failed,
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
			editor.putString(PreferencesHelper.XML_SINA_ACCESS_TOKEN,
					values.getString("access_token"));
			editor.commit();
			if (values.getString("uid") != null
					&& !values.getString("uid").equals("")) {
				boundAccount(NoteApplication.getInstance().getUserId(),
						ServerInterface.LOGIN_TYPE_SINA,
						values.getString("uid"));
			}

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

	private class NetThread extends Thread {
		int userId;// 微笔记userid
		int type;// 绑定账号类型
		String uid;// 绑定账号的uid

		public NetThread(int userid, int type, String uid) {
			super();
			this.userId = userid;
			this.type = type;
			this.uid = uid;
		}

		@Override
		public void run() {
			int result = ServerInterface.boundAccount(userId, type, uid);
			mHandler.sendEmptyMessage(result);
		}

	}

}

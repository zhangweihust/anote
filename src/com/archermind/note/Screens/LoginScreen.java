package com.archermind.note.Screens;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Utils.ServerInterface;
import com.weibo.net.DialogError;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;

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
	private Handler mHandler;
	public static final String CONSUMER_KEY = "1294484213";// 申请的KEY
	public static final String CONSUMER_SECRET = "69c73b5fa22fbda126d4db68118afaa6"; // 申请的SECRET
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
				switch (msg.what) {
				case ServerInterface.SUCCESS:
					Toast.makeText(LoginScreen.this, "登录成功！",
							Toast.LENGTH_SHORT).show();
					NoteApplication.setLogin(true);
					break;
				case ServerInterface.ERROR_USER_NOT_EXIST:
					Toast.makeText(LoginScreen.this,
							R.string.login_err_username_not_exist,
							Toast.LENGTH_SHORT).show();
					break;
				case ServerInterface.ERROR_PASSWORD_WRONG:
					Toast.makeText(LoginScreen.this,
							R.string.login_err_password_wrong,
							Toast.LENGTH_SHORT).show();
					break;
				case ServerInterface.ERROR_SERVER_INTERNAL:
					Toast.makeText(LoginScreen.this,
							R.string.login_err_server_internal,
							Toast.LENGTH_SHORT).show();
					break;
				case ServerInterface.ERROR_USER_NOT_BIND:
					Intent intent = new Intent(LoginScreen.this,
							RegisterScreen.class);
					intent.putExtras(msg.getData());
					startActivity(intent);
					break;
				case ServerInterface.USER_BINDED:
					try {
						String[] uname_pswd = msg.getData()
								.getString("uname_pswd").split("\\|");
						SharedPreferences sp = getSharedPreferences("userInfo",
								0);
						Editor editor = sp.edit();
						editor.putString("uname", uname_pswd[0]);
						editor.putString("pswd", uname_pswd[1]);
						editor.commit();
						Toast.makeText(LoginScreen.this, "登录成功！",
								Toast.LENGTH_SHORT).show();
						NoteApplication.setLogin(true);
					} catch (Exception e) {
						e.printStackTrace();
					}

					break;

				default:
					break;
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
			return;
		}
		 if (!ServerInterface.isPswdValid(password)) {
		 Toast.makeText(this, R.string.login_err_password_format, Toast.LENGTH_SHORT).show();
		 return;
		 }
		new Thread() {

			@Override
			public void run() {
				int result = ServerInterface.login(username, password);
				switch (result) {
				case ServerInterface.SUCCESS:
					mHandler.sendEmptyMessage(ServerInterface.SUCCESS);
					break;
				case ServerInterface.ERROR_USER_NOT_EXIST:
					mHandler.sendEmptyMessage(ServerInterface.ERROR_USER_NOT_EXIST);
					break;
				case ServerInterface.ERROR_PASSWORD_WRONG:
					mHandler.sendEmptyMessage(ServerInterface.ERROR_PASSWORD_WRONG);
					break;
				case ServerInterface.ERROR_SERVER_INTERNAL:
					mHandler.sendEmptyMessage(ServerInterface.ERROR_SERVER_INTERNAL);
					break;
				default:
					break;
				}
			}

		}.start();
	}

	/*
	 * 其他登录方式 参数： 登录类型：新浪，QQ，人人 绑定的新浪，QQ,人人的用户id
	 */
	private void login_others(int type, String uid) {
		final int type_thread = type;
		final String uid_thread = uid;
		new Thread() {

			@Override
			public void run() {
				String result = ServerInterface.checkBinding(type_thread,
						uid_thread);
				if (result.equals(String
						.valueOf(ServerInterface.ERROR_SERVER_INTERNAL))) {
					mHandler.sendEmptyMessage(ServerInterface.ERROR_SERVER_INTERNAL);
				} else if (result.equals(String
						.valueOf(ServerInterface.ERROR_USER_NOT_BIND))) {
					Message message = new Message();
					message.what = ServerInterface.ERROR_USER_NOT_BIND;
					Bundle bundle = new Bundle();
					bundle.putInt("login_type", type_thread);
					bundle.putString("uid", uid_thread);
					message.setData(bundle);
					mHandler.sendMessage(message);

				} else {
					Message message = new Message();
					message.what = ServerInterface.USER_BINDED;
					Bundle bundle = new Bundle();
					bundle.putString("uname_pswd", result);
					message.setData(bundle);
					mHandler.sendMessage(message);
				}
			}

		}.start();

	}

	/*
	 * 绑定新浪微博账号 
	 * Oauth2.0 隐式授权认证方式
	 */
	private void bindSinaweibo() {
		Weibo weibo = Weibo.getInstance();
		weibo.setupConsumerConfig(CONSUMER_KEY, CONSUMER_SECRET);
		weibo.setRedirectUrl("http://www.sina.com");// 此处使用的URL必须和新浪微博上应用提供的回调地址一样
		weibo.authorize(this, new AuthDialogListener());
	}

	private class AuthDialogListener implements WeiboDialogListener {

		@Override
		public void onComplete(Bundle values) {
			Log.i(TAG,
					values.getString("access_token") + "  "
							+ values.getString("expires_in") + "  "
							+ values.getString("uid"));
			SharedPreferences sp = getSharedPreferences("userInfo", 0);
			Editor editor = sp.edit();
			editor.putString("access_token", values.getString("access_token"));
			editor.putString("expires_in", values.getString("expires_in"));
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

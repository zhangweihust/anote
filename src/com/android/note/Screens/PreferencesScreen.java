package com.android.note.Screens;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.note.NoteApplication;
import com.android.note.Services.ServiceManager;
import com.android.note.Utils.NetworkUtils;
import com.android.note.Utils.PreferencesHelper;
import com.archermind.note.R;

public class PreferencesScreen extends Screen implements OnClickListener {

	private Context mContext;
	private ImageView mUserAvatar;
	private Button mLoginButton;
	private Button mLogoutButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences_screen);

		mContext = PreferencesScreen.this;

		Button btnback = (Button) this.findViewById(R.id.back);
		btnback.setOnClickListener(this);

		View personal_info = (View) this
				.findViewById(R.id.personal_info_layout);
		personal_info.setOnClickListener(this);
		View album_view = (View) this.findViewById(R.id.album_layout);
		album_view.setOnClickListener(this);
		View account_view = (View) this.findViewById(R.id.account_layout);
		account_view.setOnClickListener(this);
		View share_view = (View) this.findViewById(R.id.share_layout);
		share_view.setOnClickListener(this);
		View feedback_view = (View) this.findViewById(R.id.feedback_layout);
		feedback_view.setOnClickListener(this);
		View about_view = (View) this.findViewById(R.id.about_layout);
		about_view.setOnClickListener(this);

		mUserAvatar = (ImageView) this.findViewById(R.id.user_avatar);

//		Bitmap image = PreferencesHelper.getAvatarBitmap(this);
//		if (image != null) {
//			mUserAvatar.setImageBitmap(image);
//		}
		
		mLoginButton = (Button) findViewById(R.id.btn_login);
		mLoginButton.setOnClickListener(this);
		mLogoutButton = (Button) findViewById(R.id.btn_logout);
		mLogoutButton.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		Bitmap image = PreferencesHelper.getAvatarBitmap(this);
//		if (image != null) {
//			mUserAvatar.setImageBitmap(image);
//		}
		
		//判断是否登录
		if(ServiceManager.isLogin()){
			mLoginButton.setVisibility(View.GONE);
			mLogoutButton.setVisibility(View.VISIBLE);
		}else {
			mLoginButton.setVisibility(View.VISIBLE);
			mLogoutButton.setVisibility(View.GONE);
		}
	}
	
	private boolean isLogin() {
		if(ServiceManager.isLogin()){
			return true;
		}else {
			Toast.makeText(PreferencesScreen.this, getString(R.string.no_login_info), Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;
		case R.id.personal_info_layout:
			if (isLogin()) {
				intent.setClass(mContext, PersonInfoScreen.class);
				mContext.startActivity(intent);
			}
			break;
		case R.id.album_layout:
			if (isLogin()) {
				intent.setClass(mContext, AlbumScreen.class);
				mContext.startActivity(intent);
			}
			break;
		case R.id.account_layout:
			if (isLogin()) {
				intent.setClass(mContext, AccountScreen.class);
				mContext.startActivity(intent);
			}
			break;
		case R.id.share_layout:
			if (isLogin()) {
				intent.setClass(mContext, ShareSettingScreen.class);
				mContext.startActivity(intent);
			}
			break;
		case R.id.about_layout:
			intent.setClass(mContext, AboutScreen.class);
			mContext.startActivity(intent);
			break;
		case R.id.feedback_layout:
				intent.setClass(mContext, FeedbackScreen.class);
				mContext.startActivity(intent);
			break;
		case R.id.btn_login:
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				intent.setClass(mContext, LoginScreen.class);
				mContext.startActivity(intent);
			}else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_logout:
			showLogoutDialog();
			break;
		}
	}
	
	private void showLogoutDialog(){
//		new AlertDialog.Builder(this)
//		.setTitle(R.string.logout_title)
//		.setPositiveButton(R.string.setting_ok, new DialogInterface.OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				ServiceManager.setLogin(false);
//				mLoginButton.setVisibility(View.VISIBLE);
//				mLogoutButton.setVisibility(View.GONE);
//			}
//		})
//		.setNegativeButton(R.string.setting_cancel, new DialogInterface.OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//
//			}
//		}).create().show();
		final Dialog dialog = new Dialog(this, R.style.CornerDialog);
		dialog.setContentView(R.layout.dialog_ok_cancel);
		TextView titleView = (TextView) dialog.findViewById(R.id.dialog_title);
		titleView.setText(R.string.dialog_title_tips);
		TextView msgView = (TextView) dialog.findViewById(R.id.dialog_message);
		msgView.setText(R.string.dialog_title_logout);
		Button btn_ok = (Button) dialog.findViewById(R.id.dialog_btn_ok);
		btn_ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ServiceManager.setLogin(false);
				mLoginButton.setVisibility(View.VISIBLE);
				mLogoutButton.setVisibility(View.GONE);
				dialog.dismiss();
			}
		});
		Button btn_cancel = (Button) dialog.findViewById(R.id.dialog_btn_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
}

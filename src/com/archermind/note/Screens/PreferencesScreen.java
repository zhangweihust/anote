package com.archermind.note.Screens;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Utils.PreferencesHelper;

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

		ImageButton btnback = (ImageButton) this.findViewById(R.id.back);
		btnback.setOnClickListener(this);

		View personal_info = (View) this
				.findViewById(R.id.personal_info_layout);
		personal_info.setOnClickListener(this);
		View album_view = (View) this.findViewById(R.id.album_layout);
		album_view.setOnClickListener(this);
		View account_view = (View) this.findViewById(R.id.account_layout);
		account_view.setOnClickListener(this);

		mUserAvatar = (ImageView) this.findViewById(R.id.user_avatar);

		Bitmap image = PreferencesHelper.getAvatarBitmap(this);
		if (image != null) {
			mUserAvatar.setImageBitmap(image);
		}
		
		mLoginButton = (Button) findViewById(R.id.btn_login);
		mLoginButton.setOnClickListener(this);
		mLogoutButton = (Button) findViewById(R.id.btn_logout);
		mLogoutButton.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Bitmap image = PreferencesHelper.getAvatarBitmap(this);
		if (image != null) {
			mUserAvatar.setImageBitmap(image);
		}
		
		//判断是否登录
		if(NoteApplication.getInstance().isLogin()){
			mLoginButton.setVisibility(View.GONE);
			mLogoutButton.setVisibility(View.VISIBLE);
		}else {
			mLoginButton.setVisibility(View.VISIBLE);
			mLogoutButton.setVisibility(View.GONE);
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
			intent.setClass(mContext, PersonalInfoScreen.class);
			mContext.startActivity(intent);
			break;
		case R.id.album_layout:
			intent.setClass(mContext, AlbumScreen.class);
			mContext.startActivity(intent);
			break;
		case R.id.account_layout:
			intent.setClass(mContext, AccountScreen.class);
			mContext.startActivity(intent);
			break;
		case R.id.btn_login:
			intent.setClass(mContext, LoginScreen.class);
			mContext.startActivity(intent);
			break;
		case R.id.btn_logout:
			showLogoutDialog();
			break;
		}
	}
	
	private void showLogoutDialog(){
		new AlertDialog.Builder(this)
		.setTitle(R.string.logout_title)
		.setPositiveButton(R.string.setting_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				NoteApplication.getInstance().setLogin(false);
				mLoginButton.setVisibility(View.VISIBLE);
				mLogoutButton.setVisibility(View.GONE);
			}
		})
		.setNegativeButton(R.string.setting_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		}).create().show();
	}
}

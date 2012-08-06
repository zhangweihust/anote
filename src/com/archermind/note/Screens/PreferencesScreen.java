package com.archermind.note.Screens;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.archermind.note.R;
import com.archermind.note.Utils.PreferencesHelper;

public class PreferencesScreen extends Screen implements OnClickListener {

	private Context mContext;
	private ImageView mUserAvatar;

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
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Bitmap image = PreferencesHelper.getAvatarBitmap(this);
		if (image != null) {
			mUserAvatar.setImageBitmap(image);
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
		}
	}
}

package com.android.note.Screens;

/**
 * @author GG
 * CopyRight @ GaoGe
 * Oct 8, 2012 
 */

import com.android.note.Utils.PreferencesHelper;
import com.archermind.note.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ShareSettingScreen extends Screen implements
		OnCheckedChangeListener, OnClickListener {
	private Button backButton;
	private CheckBox sinaBox;
	private CheckBox qqBox;
	private CheckBox renrenBox;
	private Button confirmButton;
	private SharedPreferences mPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_setting);

		backButton = (Button) findViewById(R.id.back);
		backButton.setOnClickListener(this);

		sinaBox = (CheckBox) findViewById(R.id.sina_checkBox);
		sinaBox.setOnCheckedChangeListener(this);
		qqBox = (CheckBox) findViewById(R.id.qq_checkBox);
		qqBox.setOnCheckedChangeListener(this);
		renrenBox = (CheckBox) findViewById(R.id.renren_checkBox);
		renrenBox.setOnCheckedChangeListener(this);

		confirmButton = (Button) findViewById(R.id.share_setting_confirm);
		confirmButton.setOnClickListener(this);

		mPreferences = PreferencesHelper.getSharedPreferences(this, 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		String default_share = mPreferences.getString(
				PreferencesHelper.XML_DEFAULT_SHARE, null);
		if (default_share == null) {
		} else if (default_share.equals("sina")) {
			sinaBox.setChecked(true);
		} else if (default_share.equals("qq")) {
			qqBox.setChecked(true);
		} else if (default_share.equals("renren")) {
			renrenBox.setChecked(true);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.sina_checkBox:
			if (isChecked) {
				qqBox.setChecked(false);
				renrenBox.setChecked(false);
			}
			break;
		case R.id.qq_checkBox:
			if (isChecked) {
				sinaBox.setChecked(false);
				renrenBox.setChecked(false);
			}
			break;
		case R.id.renren_checkBox:
			if (isChecked) {
				sinaBox.setChecked(false);
				qqBox.setChecked(false);
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.share_setting_confirm:
			if (sinaBox.isChecked()) {
				mPreferences.edit()
						.putString(PreferencesHelper.XML_DEFAULT_SHARE, "sina")
						.commit();
			} else if (qqBox.isChecked()) {
				mPreferences.edit()
						.putString(PreferencesHelper.XML_DEFAULT_SHARE, "qq")
						.commit();
			} else if (renrenBox.isChecked()) {
				mPreferences
						.edit()
						.putString(PreferencesHelper.XML_DEFAULT_SHARE,
								"renren").commit();
			} else {
				mPreferences.edit()
						.putString(PreferencesHelper.XML_DEFAULT_SHARE, null)
						.commit();
			}
			
			finish();	
			break;
		default:
			break;
		}

	}

}

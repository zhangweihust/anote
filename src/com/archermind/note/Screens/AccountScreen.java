package com.archermind.note.Screens;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.archermind.note.R;

public class AccountScreen extends Screen  implements OnClickListener {
	private Context mContext;
	
	private EditText mNewPasswd;
	private EditText mConfirmPasswd;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_screen);

		mContext = AccountScreen.this;
		
		ImageButton btnback = (ImageButton) this.findViewById(R.id.back);
		btnback.setOnClickListener(this);

		mNewPasswd = (EditText) this.findViewById(R.id.new_passwd);
		mConfirmPasswd = (EditText) this.findViewById(R.id.confirm_passwd);
		CheckBox cb = (CheckBox) this.findViewById(R.id.use_change_passwd);
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
				} else {
					mNewPasswd.setEnabled(false);
					mConfirmPasswd.setEnabled(false);
				}
			}});

		Button btnChange = (Button) this.findViewById(R.id.confirm_change);
		btnChange.setOnClickListener(this);

	}
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;
		case R.id.confirm_change:
			//if ();
			break;
		}
	}
}

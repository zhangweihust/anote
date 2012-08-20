package com.archermind.note.Screens;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.archermind.note.Utils.PreferencesHelper;
import com.archermind.note.Utils.ServerInterface;

public class AccountScreen extends Screen  implements OnClickListener {
	private Context mContext;
	
	private TextView mNewPasswdLabel;
	private TextView mConfirmPasswdLabel;
	private EditText mNewPasswd;
	private EditText mConfirmPasswd;
	
	private String mPasswd;
	private SharedPreferences mPreferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_screen);

		mContext = AccountScreen.this;
		
		ImageButton btnback = (ImageButton) this.findViewById(R.id.back);
		btnback.setOnClickListener(this);

		mNewPasswdLabel = (TextView) this.findViewById(R.id.new_passwd_label);
		mConfirmPasswdLabel = (TextView) this.findViewById(R.id.confirm_passwd_label);
		mNewPasswd = (EditText) this.findViewById(R.id.new_passwd);
		mConfirmPasswd = (EditText) this.findViewById(R.id.confirm_passwd);
		CheckBox cb = (CheckBox) this.findViewById(R.id.use_change_passwd);
		final View user_passwd_layout = (View) this.findViewById(R.id.user_passwd_layout);
		final Button btnConfirmChange = (Button) this.findViewById(R.id.confirm_change);
		
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
					user_passwd_layout.setBackgroundResource(R.drawable.setting_background);
					btnConfirmChange.setEnabled(true);

				} else {
					mNewPasswd.setEnabled(false);
					mConfirmPasswd.setEnabled(false);
					
					mNewPasswdLabel.setTextColor(Color.GRAY);
					mConfirmPasswdLabel.setTextColor(Color.GRAY);
					mNewPasswd.setTextColor(Color.GRAY);
					mConfirmPasswd.setTextColor(Color.GRAY);
					user_passwd_layout.setBackgroundResource(R.drawable.setting_background_gray);
					btnConfirmChange.setEnabled(false);
				}
			}});

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
				editor.putString(PreferencesHelper.XML_USER_PASSWD,
						mPasswd);
				editor.commit();
				Toast.makeText(NoteApplication.getContext(), R.string.confirm_success, Toast.LENGTH_SHORT)
						.show();
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
			final String pswdconfirm = mConfirmPasswd.getText().toString().trim();
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
					int result = ServerInterface
							.modifyPassword(
									NoteApplication.getInstance().getUserName(), 
									password);
					mPasswd = password;
					mHandler.sendEmptyMessage(result);
				}

			}.start();
			break;
		}
	}
}

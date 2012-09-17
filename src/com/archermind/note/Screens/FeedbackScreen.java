package com.archermind.note.Screens;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.NetworkUtils;
import com.archermind.note.Utils.ServerInterface;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class FeedbackScreen extends Screen implements OnClickListener {

	private FeedbackScreen mContext;
	private Button mBtnBack;
	private Button mBtnCommit;
	private EditText mFeedbackContent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback_screen);
		
		mContext = FeedbackScreen.this;
		
		mBtnBack = (Button) findViewById(R.id.back);
		mBtnBack.setOnClickListener(this);
		
		mBtnCommit = (Button) findViewById(R.id.feedback_commit);
		mBtnCommit.setOnClickListener(this);
		
		mFeedbackContent = (EditText) findViewById(R.id.feedback_content);
		mFeedbackContent.setFocusable(true);
		mFeedbackContent.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				// TODO Auto-generated method stub
				final boolean isFocus = hasFocus;
				(new Handler()).postDelayed(new Runnable() {
					public void run() {
						InputMethodManager imm = (InputMethodManager) mFeedbackContent
								.getContext().getSystemService(
										Context.INPUT_METHOD_SERVICE);
						if (isFocus) {
							imm.toggleSoftInput(0,
									InputMethodManager.HIDE_NOT_ALWAYS);
						} else {
							imm.hideSoftInputFromWindow(mFeedbackContent
									.getWindowToken(), 0);
						}
					}
				}, 100);
			}

		});
		mFeedbackContent.requestFocus();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;
		case R.id.feedback_commit: 
			FeedbackScreen.this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					String strFeedbackContent = mFeedbackContent.getText().toString().trim();
					if (strFeedbackContent == null || strFeedbackContent.equals("")) {
						Toast.makeText(NoteApplication.getContext(),
								getResources().getString(R.string.feedback_err_input_is_null),
								Toast.LENGTH_SHORT).show();
					} else if(NetworkUtils.getNetworkState(FeedbackScreen.this) == NetworkUtils.NETWORN_NONE) {
							Toast.makeText(NoteApplication.getContext(),
									R.string.network_none, Toast.LENGTH_SHORT).show();
					} else{
						String result = ServerInterface.suggestionfeedback(ServiceManager
										.getUserId()+"", "", strFeedbackContent);
						if(result.equals("" + ServerInterface.SUCCESS)){
							Toast.makeText(NoteApplication.getContext(),
									R.string.feedback_commit_success, Toast.LENGTH_SHORT)
									.show();
							FeedbackScreen.this.finish();
						} else{
								Toast.makeText(NoteApplication.getContext(),
										R.string.feedback_commit_failure,
										Toast.LENGTH_SHORT).show();
					    }
					}
				}
			});
		  break;
		
	}
	}

}

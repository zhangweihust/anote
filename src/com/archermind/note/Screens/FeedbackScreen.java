package com.archermind.note.Screens;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Utils.PreferencesHelper;
import com.archermind.note.Utils.ServerInterface;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class FeedbackScreen extends Screen implements OnClickListener {

	private FeedbackScreen mContext;
	private ImageButton mBtnBack;
	private Button mBtnCommit;
	private TextView mFeedbackContent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback_screen);
		
		mContext = FeedbackScreen.this;
		
		mBtnBack = (ImageButton) findViewById(R.id.back);
		mBtnBack.setOnClickListener(this);
		
		mBtnCommit = (Button) findViewById(R.id.feedback_commit);
		mBtnCommit.setOnClickListener(this);
		
		mFeedbackContent = (TextView) findViewById(R.id.feedback_content);
	}
	
	private String mapErrorCode(int errCode) {
		String errStr = "";
		if (-1 == errCode) {
			errStr = mContext.getString(R.string.feedback_err_input_is_null);
		}
		return errStr;
	}
	
	private int check(String str) {
		if (str == null || "".equals(str)) {
			return -1;
		}
		return 0;
	}
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case ServerInterface.SUCCESS:
				Toast.makeText(NoteApplication.getContext(),
						R.string.feedback_commit_success, Toast.LENGTH_SHORT)
						.show();
				FeedbackScreen.this.finish();
				break;
			default:
				Toast.makeText(NoteApplication.getContext(),
						R.string.feedback_commit_failure, Toast.LENGTH_SHORT)
						.show();
				FeedbackScreen.this.finish();
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
		case R.id.feedback_commit: {
			final String strFeedbackContent = mFeedbackContent.getText().toString().trim();
			int errCode = check(strFeedbackContent);
			if (errCode != 0) {
				Toast.makeText(NoteApplication.getContext(),
						mapErrorCode(errCode),
						Toast.LENGTH_SHORT).show();
			} else {
				new Thread() {

					@Override
					public void run() {
						int result = ServerInterface.suggestionfeedback(String
								.valueOf(NoteApplication.getInstance()
										.getUserId()), "", strFeedbackContent);

						mHandler.sendEmptyMessage(result);
					}

				}.start();
			}
		}
			break;
		}
	}

}

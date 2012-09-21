package com.archermind.note.Screens;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Adapter.FaceAdapter;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.NetworkUtils;
import com.archermind.note.Utils.ServerInterface;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class NoteReplyScreen extends Screen implements OnClickListener {

	private NoteReplyScreen mContext;
	private Button mBtnBack;
	private Button mBtnCommit;
	private EditText mEtReplyContent;
	private Button mBtnExpression;
	private String nid;
	private FaceAdapter faceAdapter = null;
	private Dialog facechoose_dialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_reply_screen);
		
		mContext = NoteReplyScreen.this;
		
		nid = getIntent().getStringExtra("nid");
		System.out.println("===nid === " + nid);
		
		mBtnBack = (Button) findViewById(R.id.back);
		mBtnBack.setOnClickListener(this);
		
		mBtnCommit = (Button) findViewById(R.id.btn_commit);
		mBtnCommit.setOnClickListener(this);
		
		mEtReplyContent = (EditText) findViewById(R.id.et_reply_content);
/*		mEtReplyContent.setFocusable(true);
		mEtReplyContent.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				// TODO Auto-generated method stub
				final boolean isFocus = hasFocus;
				(new Handler()).postDelayed(new Runnable() {
					public void run() {
						InputMethodManager imm = (InputMethodManager) mEtReplyContent
								.getContext().getSystemService(
										Context.INPUT_METHOD_SERVICE);
						if (isFocus) {
							imm.toggleSoftInput(0,
									InputMethodManager.HIDE_NOT_ALWAYS);
						} else {
							imm.hideSoftInputFromWindow(mEtReplyContent
									.getWindowToken(), 0);
						}
					}
				}, 100);
			}

		});
		mEtReplyContent.requestFocus();*/
		
		mBtnExpression = (Button)findViewById(R.id.btn_expression);
		mBtnExpression.setOnClickListener(this);
		
	}
	
	private void initFaceDialog() {
		facechoose_dialog = new Dialog(this,R.style.CornerDialog);
		facechoose_dialog.setContentView(R.layout.face_dialog_window);
		
		facechoose_dialog.setTitle(getString(R.string.edit_pick_face));

		faceAdapter = new FaceAdapter(this);
		GridView faceGridview = (GridView) facechoose_dialog.findViewById(R.id.face_gridview);
		faceGridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				int viewId = (Integer) ((FrameLayout) view).getChildAt(0).getTag();	
				insertFace(viewId);				
				facechoose_dialog.dismiss();
			}
		});
		faceGridview.setAdapter(faceAdapter);
	}

	/**
	 * 插入表情
	 * @param id 表情id
	 */
	private void insertFace(int id) {
        String fname = this.getResources().getResourceName(id);
        System.out.println(fname);
        fname = ":" + fname.substring(fname.lastIndexOf("/") + 1, fname.length()) + ":";
		Drawable drawable =  this.getResources().getDrawable(id); 
		drawable.setBounds(0, 0, 48, 48);
		ImageSpan span = new ImageSpan(drawable);
		SpannableString spanStr = new SpannableString(fname);
		spanStr.setSpan(span, 0, spanStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		int index = mEtReplyContent.getSelectionStart();
		index = index < 0 ? 0 : index;
		mEtReplyContent.getText().insert(index, spanStr);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;
		case R.id.btn_commit: 
			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					String strReplyContent = mEtReplyContent.getText().toString().trim();
					if (strReplyContent == null || strReplyContent.equals("")) {
						Toast.makeText(NoteApplication.getContext(),getResources().getString(R.string.content_empty),
							Toast.LENGTH_SHORT).show();
					} else if(NetworkUtils.getNetworkState(NoteReplyScreen.this) == NetworkUtils.NETWORN_NONE) {
						Toast.makeText(NoteApplication.getContext(),
								R.string.network_none, Toast.LENGTH_SHORT).show();
					} else{				
						// TODO Auto-generated method stub
						System.out.println("==content==" + strReplyContent);
						String result = ServerInterface.setReply(ServiceManager
										.getUserId(), nid, strReplyContent);
						System.out.println(result);
						if(result.equals("" + ServerInterface.SUCCESS)){
							Toast.makeText(NoteApplication.getContext(),
									R.string.reply_success, Toast.LENGTH_SHORT)
									.show();				
					    	NoteReplyScreen.this.setResult(RESULT_OK);
							NoteReplyScreen.this.finish();
						}else if(result.equals("" + ServerInterface.COOKIES_ERROR)){
							ServiceManager.setLogin(false);
							Toast.makeText(NoteReplyScreen.this,
									R.string.cookies_error, Toast.LENGTH_SHORT).show();
							Intent intent = new Intent();
							intent.setClass(NoteReplyScreen.this, LoginScreen.class);
							NoteReplyScreen.this.startActivity(intent);
						}else{
							Toast.makeText(NoteApplication.getContext(),
									R.string.reply_failure, Toast.LENGTH_SHORT)
									.show();
						}
					
			}
		}});
			break;
		case R.id.btn_expression:
			if (facechoose_dialog == null) {
				initFaceDialog();
			}			
			facechoose_dialog.show();
			break;
		default:
		}
	}

}

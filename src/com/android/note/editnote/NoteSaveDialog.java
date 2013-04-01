package com.android.note.editnote;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.android.note.NoteApplication;
import com.android.note.Events.EventArgs;
import com.android.note.Events.EventTypes;
import com.android.note.Provider.DatabaseHelper;
import com.android.note.Screens.EditNoteScreen;
import com.android.note.Screens.HomeScreen;
import com.android.note.Screens.LoginScreen;
import com.android.note.Screens.MainScreen;
import com.android.note.Screens.ShareScreen;
import com.android.note.Services.ServiceManager;
import com.android.note.Utils.DateTimeUtils;
import com.android.note.Utils.NetworkUtils;
import com.android.note.Utils.ServerInterface;
import com.archermind.note.R;

import android.R.integer;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class NoteSaveDialog implements OnClickListener {

	private CheckBox note_save_checkBox;
	private Button note_save_ok;
	private Button note_save_cancel;
	private Dialog noteSaveDialog;
	private EditNoteScreen mEditNote;
	private EditText mEditText;
	private String mAction;// 保存的类别（新增或者修改）
	private String mSid;// 笔记的服务器id
	private Boolean mIfShare; // 是否分享标志

	public NoteSaveDialog(Context context) {
		noteSaveDialog = new Dialog(context, R.style.CornerDialog);
		noteSaveDialog.setContentView(R.layout.dialog_note_save);
		noteSaveDialog.setCanceledOnTouchOutside(true);
		mEditNote = (EditNoteScreen) context;
		init();
	}

	private void init() {
		note_save_checkBox = (CheckBox) noteSaveDialog
				.findViewById(R.id.note_save_checkbox);
		note_save_ok = (Button) noteSaveDialog.findViewById(R.id.dialog_btn_ok);
		note_save_cancel = (Button) noteSaveDialog
				.findViewById(R.id.dialog_btn_cancel);
		note_save_ok.setOnClickListener(this);
		note_save_cancel.setOnClickListener(this);

		mEditText = (EditText) noteSaveDialog
				.findViewById(R.id.dialog_note_title);
	}

	public void show() {
		noteSaveDialog.show();
		String oldTitle = mEditNote.getIntent().getStringExtra("title");
		mEditText.setText(oldTitle);
	}

	public void dismiss() {
		if (noteSaveDialog.isShowing()) {
			noteSaveDialog.dismiss();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dialog_btn_ok:
			String title = mEditText.getEditableText().toString();// 标题
			if ("".equals(title) || title == null) {
				Toast.makeText(mEditNote, R.string.note_input_title, Toast.LENGTH_SHORT).show();
				return;
			}	
			
			if (note_save_checkBox.isChecked()) {
				if (!ServiceManager.isLogin()) {
					Toast.makeText(mEditNote, R.string.no_login_info,
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(mEditNote, LoginScreen.class);
					mEditNote.startActivity(intent);
					return;
				}
			}
			
			try {
				// 保存笔记文件等
				
				mEditNote.save(mEditText.getEditableText().toString());

				// 保存相关信息至数据库
				long updateTime = System.currentTimeMillis();// 更新时间
				String diaryPath = mEditNote.getDiaryPath();// 存储地址
				String weather = mEditNote.getWeather();// 天气
				String noteId = String.valueOf(mEditNote.getIntent()
						.getIntExtra("noteID", 0)); // 笔记id

				ContentValues contentValues = new ContentValues();
				if (mEditNote.getIntent().getBooleanExtra("isNewNote", false)) {
					//long firstTime = mEditNote.getIntent().getLongExtra("time",
					//		0);
					long createTime = 0;
					//if (firstTime != 0) { // 如果是在指定日期下创建笔记
						createTime = EditNoteScreen.mNoteCreateTime;
					/*} else { // 直接创建笔记
						createTime = MainScreen.snoteCreateTime;
					}*/
					contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,
							mEditText.getEditableText().toString());
					contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME,
							createTime);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_LOCAL_CONTENT,
							diaryPath);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_UPDATE_TIME,
							updateTime);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_WEATHER,
							weather);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_PAGES, mEditNote.getTotalPages());
					noteId = String.valueOf(ServiceManager.getDbManager()
							.insertLocalNotes(contentValues));
					mEditNote.getIntent().putExtra("isNewNote", false);
					// 设定传递到分享界面的intent的相关参数
					mAction = "A";
					mSid = "0";

				} else {
					contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,
							mEditText.getEditableText().toString());
					contentValues.put(DatabaseHelper.COLUMN_NOTE_UPDATE_TIME,
							updateTime);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_WEATHER,
							weather);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_PAGES, mEditNote.getTotalPages());
					ServiceManager.getDbManager().updateLocalNotes(
							contentValues, Integer.parseInt(noteId));

					Cursor cursor = ServiceManager.getDbManager()
							.queryLocalNotesById(Integer.parseInt(noteId));
					if (cursor == null || cursor.getCount() == 0) {
						System.out.println("================不存在此数据===========");
						return;
					}
					cursor.moveToFirst();
					mSid = cursor
							.getString(cursor
									.getColumnIndex(DatabaseHelper.COLUMN_NOTE_SERVICE_ID));
					cursor.close();
					if (mSid != null && Integer.parseInt(mSid) > 0) {
						// 设定传递到分享界面的intent的相关参数
						mAction = "M";
					} else {
						mAction = "A";
						mSid = "0";
					}
				}

				if (note_save_checkBox.isChecked()) {
					Intent intent = new Intent(mEditNote, ShareScreen.class);
					intent.putStringArrayListExtra("picpathlist",
							mEditNote.mPicPathList);
					intent.putExtra("noteid", noteId);
					intent.putExtra("title", title);
					intent.putExtra("action", mAction);
					intent.putExtra("sid", mSid);
					intent.putExtra("pages", mEditNote.getTotalPages());
					mEditNote.startActivity(intent);
				}
				mEditNote.finish();
			} catch (Exception e) {
				e.printStackTrace();
			}
			dismiss();
			break;
		case R.id.dialog_btn_cancel:
			dismiss();
			mEditNote.finish();
			break;
		}
	}

	// class SaveTask extends AsyncTask<Boolean, integer, String> {
	//
	// @Override
	// protected void onPreExecute() {
	// super.onPreExecute();
	// mEditNote.showProgress(null,
	// mEditNote.getString(R.string.saving_note_now));
	// }
	//
	// @Override
	// protected String doInBackground(Boolean... params) {
	// try {
	// mEditNote.runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// mEditNote.save(mEditText.getEditableText().toString());
	// }
	// });
	//
	// // 保存相关信息至数据库
	// long updateTime = System.currentTimeMillis();// 更新时间
	// String diaryPath = mEditNote.getDiaryPath();// 存储地址
	// String weather = mEditNote.getWeather();// 天气
	// int noteId = mEditNote.getIntent().getIntExtra("noteID", 0); // 笔记id
	//
	// ContentValues contentValues = new ContentValues();
	// if (mEditNote.getIntent().getBooleanExtra("isNewNote", false)) {
	// long firstTime = mEditNote.getIntent().getLongExtra("time",
	// 0);
	// long createTime = 0;
	// if (firstTime != 0) { // 如果是在指定日期下创建笔记
	// createTime = DateTimeUtils.getTimeOfOneDay(firstTime,
	// MainScreen.snoteCreateTime);
	// } else { // 直接创建笔记
	// createTime = MainScreen.snoteCreateTime;
	// }
	// contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,
	// mEditText.getEditableText().toString());
	// contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME,
	// createTime);
	// contentValues.put(DatabaseHelper.COLUMN_NOTE_LOCAL_CONTENT,
	// diaryPath);
	// contentValues.put(DatabaseHelper.COLUMN_NOTE_UPDATE_TIME,
	// updateTime);
	// contentValues.put(DatabaseHelper.COLUMN_NOTE_WEATHER,
	// weather);
	// ServiceManager.getDbManager().insertLocalNotes(
	// contentValues);
	// mEditNote.getIntent().putExtra("isNewNote", false);
	// // 设定传递到分享界面的intent的相关参数
	// mAction = "A";
	// mSid = "0";
	//
	// } else {
	// contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,
	// mEditText.getEditableText().toString());
	// contentValues.put(DatabaseHelper.COLUMN_NOTE_UPDATE_TIME,
	// updateTime);
	// contentValues.put(DatabaseHelper.COLUMN_NOTE_WEATHER,
	// weather);
	// ServiceManager.getDbManager().updateLocalNotes(
	// contentValues, noteId);
	//
	// Cursor cursor = ServiceManager.getDbManager()
	// .queryLocalNotesById(noteId);
	// if (cursor == null || cursor.getCount() == 0) {
	// System.out.println("================不存在此数据===========");
	// return "failed";
	// }
	// cursor.moveToFirst();
	// mSid = cursor
	// .getString(cursor
	// .getColumnIndex(DatabaseHelper.COLUMN_NOTE_SERVICE_ID));
	// cursor.close();
	// if (mSid != null && Integer.parseInt(mSid) > 0) { //
	// 设定传递到分享界面的intent的相关参数
	// mAction = "M";
	// } else {
	// mAction = "A";
	// mSid = "0";
	// }
	// }
	// return "success";
	// } catch (Exception e) {
	// return "failed";
	// }
	// }
	//
	// @Override
	// protected void onPostExecute(String result) {
	// super.onPostExecute(result);
	// if (result.equals("success")) {
	// if (mIfShare) {
	// Intent intent = new Intent(mEditNote, ShareScreen.class);
	// intent.putStringArrayListExtra("picpathlist",
	// mEditNote.mPicPathList);
	// intent.putExtra("noteid", mEditNote.getIntent()
	// .getIntExtra("noteID", 0));
	// intent.putExtra("title", mEditText.getEditableText()
	// .toString());
	// intent.putExtra("action", mAction);
	// intent.putExtra("sid", mSid);
	// mEditNote.startActivity(intent);
	// }
	// } else {
	// Toast.makeText(mEditNote, "保存失败！", Toast.LENGTH_SHORT).show();
	// }
	// mEditNote.dismissProgress();
	// mEditNote.finish();
	// }
	//
	// }

}

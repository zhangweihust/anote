package com.archermind.note.editnote;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Events.EventTypes;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Screens.EditNoteScreen;
import com.archermind.note.Screens.HomeScreen;
import com.archermind.note.Screens.LoginScreen;
import com.archermind.note.Screens.MainScreen;
import com.archermind.note.Screens.ShareScreen;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.DateTimeUtils;
import com.archermind.note.Utils.NetworkUtils;
import com.archermind.note.Utils.ServerInterface;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class NoteSaveDialog implements OnClickListener{

	private Button note_save_ok;
	private Button  note_save_cancel;
	private RadioGroup saveGroup;
	private Dialog noteSaveDialog;
	private EditNoteScreen mEditNote;
	private EditText mEditText;
	
	public NoteSaveDialog(Context context) {
		noteSaveDialog = new Dialog(context);
		noteSaveDialog.setContentView(R.layout.note_save_dialog);
		noteSaveDialog.setCanceledOnTouchOutside(true);
		mEditNote = (EditNoteScreen)context;
		init();
	}
	
	private void init() {
		saveGroup = (RadioGroup) noteSaveDialog.findViewById(R.id.note_save_group);
		note_save_ok = (Button) noteSaveDialog.findViewById(R.id.note_save_ok);
		note_save_cancel = (Button) noteSaveDialog.findViewById(R.id.note_save_cancel);
		note_save_ok.setOnClickListener(this);
		note_save_cancel.setOnClickListener(this);
		
		noteSaveDialog.setTitle(mEditNote.getString(R.string.note_input_title));
		mEditText = (EditText) noteSaveDialog.findViewById(R.id.editText1);
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
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.note_save_ok:
			if (saveGroup.getCheckedRadioButtonId() == R.id.save_and_share) {
				final String title = mEditText.getEditableText().toString();// 标题
				if ("".equals(title) || title == null) {
					Toast.makeText(mEditNote, "标题为空，请输入标题", Toast.LENGTH_SHORT).show();
					return;
				}
				if (mEditNote.hasChanged()) {
				    mEditNote.save();
				}
				long updateTime = System.currentTimeMillis();// 更新时间
				String diaryPath = mEditNote.getDiaryPath();// 存储地址
				String weather = mEditNote.getWeather();// 天气
				final int noteId = mEditNote.getIntent().getIntExtra("noteID", 0);// 笔记id
				
				ContentValues contentValues = new ContentValues();
				if (mEditNote.getIntent().getBooleanExtra("isNewNote", false)) {
					long firstTime = mEditNote.getIntent().getLongExtra("time", 0);
					long createTime = 0;
					if (firstTime != 0) { // 如果是在指定日期下创建笔记
						createTime = DateTimeUtils.getTimeOfOneDay(firstTime, MainScreen.snoteCreateTime);
					} else { // 直接创建笔记
						createTime = MainScreen.snoteCreateTime;
					}
					contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,title);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME, createTime);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_LOCAL_CONTENT, diaryPath);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_UPDATE_TIME, updateTime);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_WEATHER, weather);
					long id = ServiceManager.getDbManager().insertLocalNotes(contentValues);
					mEditNote.getIntent().putExtra("isNewNote", false);
					
					if (!NoteApplication.getInstance().isLogin()) {
						Toast.makeText(mEditNote, R.string.no_login_info, Toast.LENGTH_SHORT).show();
						Intent intent = new Intent(mEditNote,LoginScreen.class);
						mEditNote.startActivity(intent);
						return;
					}
					
					EventArgs args = new EventArgs();
					args.setType(EventTypes.NOTE_TO_BE_SHARE);
					args.putExtra("noteid", String.valueOf(id));
					args.putExtra("title", title);
					args.putExtra("action", "A");
					args.putExtra("sid", "0");
					ServiceManager.getEventservice().onUpdateEvent(args);
					
					/*Intent intent = new Intent(mEditNote,ShareScreen.class);
					intent.putStringArrayListExtra("picpathlist", mEditNote.mPicPathList);
					intent.putExtra("noteid", String.valueOf(id));
					intent.putExtra("title", title);
					intent.putExtra("action", "A");
					intent.putExtra("sid", "0");
					mEditNote.startActivity(intent);*/
					dismiss();
					
				} else {
					contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,title);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_UPDATE_TIME, updateTime);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_WEATHER, weather);
					ServiceManager.getDbManager().updateLocalNotes(contentValues, noteId);
					
					if (!NoteApplication.getInstance().isLogin()) {
						Toast.makeText(mEditNote, R.string.no_login_info, Toast.LENGTH_SHORT).show();
						return;
					}
					
					Cursor cursor = ServiceManager.getDbManager().queryLocalNotesById(noteId);
					if (cursor == null || cursor.getCount() == 0) {
						System.out.println("================不存在此数据===========");
						return;
					}
					cursor.moveToFirst();
					String serviceID = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_SERVICE_ID));
					cursor.close();
					String action = "N";
					if (serviceID != null && Integer.parseInt(serviceID) > 0) {
						action = "M";
					} else {
						action = "A";
						serviceID = "0";
					}
						
					Log.d("=AAAA=","share now");
					EventArgs args = new EventArgs();
					args.setType(EventTypes.NOTE_TO_BE_SHARE);
					args.putExtra("noteid", String.valueOf(noteId));
					args.putExtra("title", title);
					args.putExtra("action", action);
					args.putExtra("sid", serviceID);
					ServiceManager.getEventservice().onUpdateEvent(args);
					
					
					/*Intent intent = new Intent(mEditNote,ShareScreen.class);
					intent.putStringArrayListExtra("picpathlist", mEditNote.mPicPathList);
					intent.putExtra("noteid", String.valueOf(noteId));
					intent.putExtra("title", title);
					intent.putExtra("action", action);
					intent.putExtra("sid", serviceID);
					mEditNote.startActivity(intent);*/
					dismiss();
				}
			} else if (saveGroup.getCheckedRadioButtonId() == R.id.save_only) {
				String title = mEditText.getEditableText().toString();
				if ("".equals(title) || title == null) {
					Toast.makeText(mEditNote, "标题为空，请输入标题",  Toast.LENGTH_SHORT).show();
					return;
				}
				
				if (mEditNote.hasChanged()) {
				    mEditNote.save();
				}
				long updateTime = System.currentTimeMillis();
				String diaryPath = mEditNote.getDiaryPath();
				String weather = mEditNote.getWeather();
				int noteId = mEditNote.getIntent().getIntExtra("noteID", 0);
				
				long firstTime = mEditNote.getIntent().getLongExtra("time", 0);
				long createTime = 0;
				if (firstTime != 0) {
					createTime = DateTimeUtils.getTimeOfOneDay(firstTime, MainScreen.snoteCreateTime);
				} else {
					createTime = MainScreen.snoteCreateTime;
				}
				
				ContentValues contentValues = new ContentValues();
				if (mEditNote.getIntent().getBooleanExtra("isNewNote", false)) {
					contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,title);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME, createTime);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_LOCAL_CONTENT, diaryPath);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_UPDATE_TIME, updateTime);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_WEATHER, weather);
					long id = ServiceManager.getDbManager().insertLocalNotes(contentValues);
					mEditNote.getIntent().putExtra("isNewNote", false);
					Log.d("=UUU=","id = " + id);
				} else {
					contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,title);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_UPDATE_TIME, updateTime);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_WEATHER, weather);
					ServiceManager.getDbManager().updateLocalNotes(contentValues, noteId);
				}
				dismiss();
				ServiceManager.getEventservice().onUpdateEvent(new EventArgs(EventTypes.NOTE_SAVE_OVER));
//				mEditNote.finish();
			} else {
				dismiss();
				mEditNote.finish();
			}
			break;
		case R.id.note_save_cancel:
			dismiss();
			mEditNote.finish();
			break;
		}
	}
	
//	private void uploadNote(long id,String title,String action,String sid,String userId) {
//		int totalPage = 0;
//		if (mEditNote.mPicPathList != null) {
//			totalPage = mEditNote.mPicPathList.size();
//		}
//		
//		String context = totalPage == 0 ? "" : mEditNote.mPicPathList.get(0);
//		
//		if (!"".equals(context)) {
//			context = context.substring(context.lastIndexOf("/") + 1);
//		}
//		
//		int serviceId = ServerInterface.uploadNote(id,userId,sid,action,title,context,String.valueOf(totalPage));
//		if ("A".equals(action)) {
//			if (serviceId > 0) {
//				ContentValues contentValues2 = new ContentValues();
//				contentValues2.put(DatabaseHelper.COLUMN_NOTE_SERVICE_ID, String.valueOf(serviceId));
//				contentValues2.put(DatabaseHelper.COLUMN_NOTE_USER_ID,Integer.parseInt(userId));
//				ServiceManager.getDbManager().updateLocalNotes(contentValues2,(int)id);
//				System.out.println("===============分享成功===============");
//				MainScreen.eventService.onUpdateEvent(new EventArgs(EventTypes.SHARE_NOTE_SUCCESSED));
//			} else {
//				System.out.println("===============分享失败===============");
//				MainScreen.eventService.onUpdateEvent(new EventArgs(EventTypes.SHARE_NOTE_FAILED));
//			}
//		} else if ("M".equals(action)) {
//			if (serviceId == 0) {
//				System.out.println("===============分享成功===============");
//				MainScreen.eventService.onUpdateEvent(new EventArgs(EventTypes.SHARE_NOTE_SUCCESSED));
//			} else {
//				System.out.println("===============分享失败===============");
//				MainScreen.eventService.onUpdateEvent(new EventArgs(EventTypes.SHARE_NOTE_FAILED));
//			}
//		}
//	}
//	
//	private void uploadImg(String userIdStr) {
//		int totalPage = 0;
//		if (mEditNote.mPicPathList != null) {
//			totalPage = mEditNote.mPicPathList.size();
//		}
//		
//		ServerInterface sInterface = new ServerInterface();
//		sInterface.InitAmtCloud(MainScreen.mContext);
//		
//		for (int i = 0; i < totalPage;i++) {
//			sInterface.uploadFile(MainScreen.mContext, userIdStr, mEditNote.mPicPathList.get(i));
//		}
//		
//	}

}

package com.archermind.note.editnote;

import com.archermind.note.R;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Events.EventTypes;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Screens.EditNoteScreen;
import com.archermind.note.Screens.HomeScreen;
import com.archermind.note.Screens.MainScreen;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.ServerInterface;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
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
		noteSaveDialog = new Dialog(context,R.style.noTitleDialog);
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
		
		mEditText = (EditText) noteSaveDialog.findViewById(R.id.editText1);
	}
	
	public void show() {
		noteSaveDialog.show();
		String oldTitle = mEditNote.getIntent().getStringExtra("title");
		mEditText.setText(oldTitle);
	}
	
	public void dismiss() {
		noteSaveDialog.dismiss();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.note_save_ok:
			if (saveGroup.getCheckedRadioButtonId() == R.id.save_and_share) {
				mEditNote.save();
				String title = mEditText.getEditableText().toString();
				if ("".equals(title) || title == null) {
					Toast.makeText(mEditNote, "标题为空，请输入标题", Toast.LENGTH_SHORT).show();
					return;
				}
				long updateTime = System.currentTimeMillis();
				String diaryPath = mEditNote.getDiaryPath();
//				if (mEditNote.getIntent().getBooleanExtra("isNewNote", false)) {
//				    args.setType(EventTypes.NOTE_INSERT_TO_DATABASE);
//				} else {
//					args.setType(EventTypes.NOTE_UPDATE_TO_DATABASE);
//					args.putExtra("noteID", mEditNote.getIntent().getIntExtra("noteID", 0));
//				}
//				args.putExtra("updateTime", updateTime);
//				args.putExtra("noteTitle", title);
//				args.putExtra("diaryPath", diaryPath);
//				HomeScreen.eventService.onUpdateEvent(args);
				int noteId = mEditNote.getIntent().getIntExtra("noteID", 0);
				int serviceId = 0;
				
				ContentValues contentValues = new ContentValues();
				if (mEditNote.getIntent().getBooleanExtra("isNewNote", false)) {
					contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,title);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_USER_ID, 1000);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME, MainScreen.snoteCreateTime);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_LOCAL_CONTENT, diaryPath);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_UPDATE_TIME, updateTime);
					long id = ServiceManager.getDbManager().insertLocalNotes(contentValues, System.currentTimeMillis());
					if (id > 0) {
						serviceId = ServerInterface.uploadNote(id,"100","0","A",title,"4");
						if (serviceId > 0) {
							ContentValues contentValues2 = new ContentValues();
							contentValues2.put(DatabaseHelper.COLUMN_NOTE_SERVICE_ID, String.valueOf(serviceId));
							ServiceManager.getDbManager().updateLocalNotes(contentValues2, (int)id);
						}
					} else {
						System.out.println("==========数据库插入失败==============");
					}
				} else {
					Cursor cursor = ServiceManager.getDbManager().queryLocalNotesById(noteId);
					if (cursor == null || cursor.getCount() == 0) {
						System.out.println("================不存在此数据===========");
						return;
					}
					String serviceID = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_SERVICE_ID));
					String action = "N";
					if (serviceID != null && Integer.parseInt(serviceID) > 0) {
						action = "M";
					} else {
						action = "A";
						serviceID = "0";
					}
					contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,title);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_UPDATE_TIME, updateTime);
					ServiceManager.getDbManager().updateLocalNotes(contentValues, noteId);
				    
					serviceId = ServerInterface.uploadNote(noteId,"100",serviceID,action,title,"4");
					
					if ("A".equals(action)) {
						if (serviceId > 0) {
							ContentValues contentValues2 = new ContentValues();
							contentValues2.put(DatabaseHelper.COLUMN_NOTE_SERVICE_ID, String.valueOf(serviceId));
							ServiceManager.getDbManager().updateLocalNotes(contentValues2, noteId);
						} else {
							System.out.println("===============分享失败===============");
						}
					} else if ("M".equals(action)) {
						if (serviceId == 0) {
							System.out.println("===============分享成功===============");
						} else {
							System.out.println("===============分享失败===============");
						}
					}
				}
			} else if (saveGroup.getCheckedRadioButtonId() == R.id.save_only) {
				mEditNote.save();
				String title = mEditText.getEditableText().toString();
				if ("".equals(title) || title == null) {
					Toast.makeText(mEditNote, "标题为空，请输入标题",  Toast.LENGTH_SHORT).show();
					return;
				}
				long updateTime = System.currentTimeMillis();
				String diaryPath = mEditNote.getDiaryPath();
				int noteId = mEditNote.getIntent().getIntExtra("noteID", 0);
				ContentValues contentValues = new ContentValues();
				if (mEditNote.getIntent().getBooleanExtra("isNewNote", false)) {
					contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,title);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_USER_ID, 1000);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME, MainScreen.snoteCreateTime);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_LOCAL_CONTENT, diaryPath);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_UPDATE_TIME, updateTime);
					long id = ServiceManager.getDbManager().insertLocalNotes(contentValues, System.currentTimeMillis());
				} else {
					contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,title);
					contentValues.put(DatabaseHelper.COLUMN_NOTE_UPDATE_TIME, updateTime);
					ServiceManager.getDbManager().updateLocalNotes(contentValues, noteId);
				}
			} else {
				String [] fileNames = {"/sdcard/aNote/picmap","/sdcard/aNote/text","/sdcard/aNote/gesture","/sdcard/aNote/graffit"};
				mEditNote.deletefiles(fileNames);
			}
			dismiss();
			mEditNote.finish();
			break;
		case R.id.note_save_cancel:
			dismiss();
			break;
		}
		
	}

}

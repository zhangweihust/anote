package com.archermind.note.editnote;

import com.archermind.note.R;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Events.EventTypes;
import com.archermind.note.Screens.EditNoteScreen;
import com.archermind.note.Screens.HomeScreen;

import android.app.Dialog;
import android.content.Context;
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
				EventArgs args= new EventArgs();
				if (mEditNote.getIntent().getBooleanExtra("isNewNote", false)) {
				    args.setType(EventTypes.NOTE_INSERT_TO_DATABASE);
				} else {
					args.setType(EventTypes.NOTE_UPDATE_TO_DATABASE);
					args.putExtra("noteID", mEditNote.getIntent().getIntExtra("noteID", 0));
				}
				args.putExtra("updateTime", updateTime);
				args.putExtra("noteTitle", title);
				args.putExtra("diaryPath", diaryPath);
				HomeScreen.eventService.onUpdateEvent(args);
				
			} else if (saveGroup.getCheckedRadioButtonId() == R.id.save_only) {
				mEditNote.save();
				String title = mEditText.getEditableText().toString();
				if ("".equals(title) || title == null) {
					Toast.makeText(mEditNote, "标题为空，请输入标题",  Toast.LENGTH_SHORT).show();
					return;
				}
				long updateTime = System.currentTimeMillis();
				String diaryPath = mEditNote.getDiaryPath();
				EventArgs args= new EventArgs();
				if (mEditNote.getIntent().getBooleanExtra("isNewNote", false)) {
				    args.setType(EventTypes.NOTE_INSERT_TO_DATABASE);
				} else {
					args.setType(EventTypes.NOTE_UPDATE_TO_DATABASE);
					args.putExtra("noteID", mEditNote.getIntent().getIntExtra("noteID", 0));
				}
				args.setType(EventTypes.NOTE_INSERT_TO_DATABASE);
				args.putExtra("updateTime", updateTime);
				args.putExtra("noteTitle", title);
				args.putExtra("diaryPath", diaryPath);
				HomeScreen.eventService.onUpdateEvent(args);
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

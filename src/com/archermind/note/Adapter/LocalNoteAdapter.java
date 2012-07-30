package com.archermind.note.Adapter;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.DateTimeUtils;
import com.archermind.note.Utils.NoteTypes;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LocalNoteAdapter  extends CursorAdapter {
	private LayoutInflater inflater;
	
	public LocalNoteAdapter(Context context, Cursor c) {
		super(context, c);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final NoteItem item = (NoteItem) view.getTag(R.layout.home_screen_listview_item);
		String title = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_TITLE));
		long time = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME));
		int type = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_TYPE));
		boolean first = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_LAST_FLAG)) == 1;
		boolean isSigned = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CONTENT_SIGNED)) == 1;
		item.title.setText(title);
		
		if(first){
			item.firstNoteDiv0.setVisibility(View.VISIBLE);
			item.firstNoteDiv1.setVisibility(View.VISIBLE);
			item.firstNoteOnly.setVisibility(View.VISIBLE);
			item.noteCount.setText("" + ServiceManager.getDbManager().queryTodayLocalNOTEs(time).getCount());
			item.weekDay.setText(DateTimeUtils.time2String("EEEE", time));
			item.date.setText(DateTimeUtils.time2String("yyyy年MM月dd日", time));
		} else {
			item.firstNoteDiv0.setVisibility(View.GONE);
			item.firstNoteDiv1.setVisibility(View.GONE);
			item.firstNoteOnly.setVisibility(View.GONE);
		}
		if(isSigned){
			item.isSigned.setImageResource(R.drawable.signed);
		} else {
			item.isSigned.setImageResource(R.drawable.unsigned);
		}
		if(type == NoteTypes.ACTIVITY){
			item.typeIcon.setImageResource(R.drawable.menu_right_activity);
		}else if(type == NoteTypes.MOOD){
			item.typeIcon.setImageResource(R.drawable.menu_right_mood);
		}else if(type == NoteTypes.SCHEDULE){
			item.typeIcon.setImageResource(R.drawable.menu_right_schedule);
		}else{
			item.typeIcon.setImageResource(R.drawable.menu_right_diary);
		}
			
		/*item.commentCount.setText("50");*/
		
		EventArgs args = new EventArgs();
		args.putExtra("time", time);
		args.putExtra("first", first);
		view.setTag(args);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.home_screen_listview_item, null);
		NoteItem item = new NoteItem();
		item.date = (TextView) view.findViewById(R.id.tv_date);
		item.weekDay = (TextView) view.findViewById(R.id.tv_week_day);
		item.noteCount = (TextView) view.findViewById(R.id.tv_note_count);
		item.typeIcon = (ImageView) view.findViewById(R.id.iv_note_type);
		item.firstNoteDiv0 = (View) view.findViewById(R.id.v_div0_first_note_only);
		item.firstNoteDiv1 = (View) view.findViewById(R.id.v_div1_first_note_only);
		item.title = (TextView) view.findViewById(R.id.tv_note_title);
		/*item.commentCount = (TextView) view.findViewById(R.id.tv_comment_count);*/
		item.isSigned = (ImageView) view.findViewById(R.id.iv_is_signed);
		item.firstNoteOnly= (LinearLayout) view.findViewById(R.id.ll_first_note_only);
		view.setTag(R.layout.home_screen_listview_item,item);
		return view;
	}
	
	private class NoteItem{
		private TextView date;
		private TextView weekDay;
		private TextView noteCount;
		private View firstNoteDiv1;
		private View firstNoteDiv0;
		private TextView title;
		private ImageView typeIcon;
		private ImageView isSigned;
		/*private TextView commentCount;*/
		private LinearLayout firstNoteOnly;
		private int type;
	}


}

package com.archermind.note.Adapter;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.DateTimeUtils;
import com.archermind.note.Utils.DensityUtil;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LocalNoteAdapter  extends CursorAdapter {
	private LayoutInflater inflater;
	private static int isFirst = 0;
	
	public LocalNoteAdapter(Context context, Cursor c) {
		super(context, c);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final NoteItem item = (NoteItem) view.getTag(R.layout.home_screen_listview_item);
		String title = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_TITLE));
		long time = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME));
		boolean first = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_LAST_FLAG)) == 1;
		boolean isSigned = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CONTENT_SIGNED)) == 1;
		item.tvTitle.setText(title);
		item.tvTime.setText(DateTimeUtils.time2String("hh:mm aa", time));
		
		if(first){
			item.vFirstNoteDiv.setVisibility(View.VISIBLE);
			item.tvDate.setVisibility(View.VISIBLE);
			item.tvWeekDay.setVisibility(View.VISIBLE);
			item.tvWeekDay.setText(DateTimeUtils.time2String("EEEE", time));
			item.tvDate.setText(DateTimeUtils.time2String("dd", time));
			view.setPadding(0, 0, 0, 0);
		} else {
			view.setPadding(0, 0, 0, DensityUtil.dip2px(context, 15));
			item.vFirstNoteDiv.setVisibility(View.GONE);
			item.tvDate.setVisibility(View.GONE);
			item.tvWeekDay.setVisibility(View.GONE);
			
		}
		if(isSigned){
			item.ivIsSigned.setVisibility(View.VISIBLE);
		} else {
			item.ivIsSigned.setVisibility(View.GONE);
		}
		
		EventArgs args = new EventArgs();
		args.putExtra("time", time);
		args.putExtra("first", first);
		view.setTag(args);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.home_screen_listview_item, null);
		NoteItem item = new NoteItem();
		item.tvDate = (TextView) view.findViewById(R.id.tv_date);
		item.tvWeekDay = (TextView) view.findViewById(R.id.tv_week_day);
		item.vFirstNoteDiv = (View) view.findViewById(R.id.v_div_first_note_only);
		item.tvTitle = (TextView) view.findViewById(R.id.tv_title);
		/*item.commentCount = (TextView) view.findViewById(R.id.tv_comment_count);*/
		item.ivIsSigned = (ImageView) view.findViewById(R.id.iv_is_signed);
		item.tvTime = (TextView) view.findViewById(R.id.tv_time);
		item.rlDay= (RelativeLayout) view.findViewById(R.id.rl_day);
		view.setTag(R.layout.home_screen_listview_item,item);
		return view;
	}
	
	private class NoteItem{
		private TextView tvDate;
		private TextView tvWeekDay;
		private View vFirstNoteDiv;
		private TextView tvTitle;
		private ImageView ivIsSigned;
		private TextView tvTime;
		/*private TextView commentCount;*/
		private RelativeLayout rlDay;
	}


}

package com.android.note.Adapter;


import com.android.note.Events.EventArgs;
import com.android.note.Provider.DatabaseHelper;
import com.android.note.Utils.DateTimeUtils;
import com.archermind.note.R;

import android.content.Context;
import android.database.Cursor;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LocalNoteAdapter extends CursorAdapter {
	private LayoutInflater inflater;

	public LocalNoteAdapter(Context context, Cursor c) {
		super(context, c);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, final Context context, final Cursor cursor) {
		NoteItem item = (NoteItem) view
				.getTag(R.layout.note_month_listview_item);
		String title = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_TITLE));
		long time = cursor.getLong(cursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME));
		boolean lastFlag = cursor.getInt(cursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_LAST_FLAG)) == 1;
		int isSigned = cursor.getInt(cursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CONTENT_SIGNED));
		item.tvTitle.setText(title);
		item.tvTime.setText(DateTimeUtils.time2String("HH:mm", time));

		if (lastFlag) {
			item.vFirstNoteDiv.setVisibility(View.VISIBLE);
			item.tvDate.setVisibility(View.VISIBLE);
			// item.tvWeekDay.setVisibility(View.VISIBLE);
			String weekday = DateTimeUtils.time2String("EEEE", time);
			String day = DateTimeUtils.time2String("dd", time);
			SpannableString sp = new SpannableString(day + "\n" + weekday);
			sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0,
					day.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			sp.setSpan(new AbsoluteSizeSpan((int) context.getResources()
					.getDimension(R.dimen.home_screen_date_info_text_size)), 0,
					day.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			sp.setSpan(new AbsoluteSizeSpan((int) context.getResources()
					.getDimension(R.dimen.home_screen_weekday_info_text_size)),
					day.length() + 1, day.length() + weekday.length() + 1,
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			item.tvDate.setText(sp);
		} else {
			item.vFirstNoteDiv.setVisibility(View.GONE);
			item.tvDate.setVisibility(View.INVISIBLE);
		}
		if (isSigned == 1) {
			item.ivIsSigned.setVisibility(View.VISIBLE);
		} else {
			item.ivIsSigned.setVisibility(View.GONE);
		}

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.note_month_listview_item, null);
		NoteItem item = new NoteItem();
		item.tvDate = (TextView) view.findViewById(R.id.tv_date);
		item.vFirstNoteDiv = (View) view
				.findViewById(R.id.v_div_first_note_only);
		item.tvTitle = (TextView) view.findViewById(R.id.tv_title);
		item.ivIsSigned = (ImageView) view.findViewById(R.id.iv_is_signed);
		item.tvTime = (TextView) view.findViewById(R.id.tv_time);
		view.setTag(R.layout.note_month_listview_item, item);
		return view;
	}

	private class NoteItem {
		private TextView tvDate;
		private View vFirstNoteDiv;
		private TextView tvTitle;
		private ImageView ivIsSigned;
		private TextView tvTime;
	}

}

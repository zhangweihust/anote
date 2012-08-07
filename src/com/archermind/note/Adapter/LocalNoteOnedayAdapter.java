package com.archermind.note.Adapter;

import java.util.Calendar;
import java.util.Locale;

import com.archermind.note.R;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Provider.DatabaseManager;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.DateTimeUtils;
import com.archermind.note.Utils.DensityUtil;

import android.R.integer;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LocalNoteOnedayAdapter  extends CursorAdapter {
	private LayoutInflater mInflater;
	private static CharSequence[] transactItemsSign = {"分享", "标记", "删除"};
	private static CharSequence[] transactItemsUnsign = {"分享", "取消标记", "删除"};
	private AlertDialog.Builder mbuilder;
	private AlertDialog madTransact;
	private static DatabaseManager mDb = ServiceManager.getDbManager();
	
	public LocalNoteOnedayAdapter(Context context, Cursor c) {
		super(context, c);
		mInflater = LayoutInflater.from(context);
		mbuilder = new AlertDialog.Builder(context);  
		mbuilder.setTitle("请操作");
	}

	@Override
	public void bindView(View view, final Context context, final Cursor cursor) {
		final NoteOneDayItem item = (NoteOneDayItem) view.getTag(R.layout.note_oneday_listview_item);
		String title = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_TITLE));
		System.out.println(title);
		final long time = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME));
		final boolean lastFlag = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_LAST_FLAG)) == 1;
		final int isSigned = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CONTENT_SIGNED));
		item.tvTitle.setText(title);
		item.tvTime.setText(DateTimeUtils.time2String("hh:mm aa", time));
		
		
		if(lastFlag){
			item.tvDays.setVisibility(View.VISIBLE);
			item.tvDate.setVisibility(View.VISIBLE);
			long duration = (DateTimeUtils.getToday(Calendar.PM, System.currentTimeMillis()) - time)/(1000 * 60 * 60 * 24);
			if(duration == 0){
				item.tvDays.setText("今天");
			}else{
				item.tvDays.setText(duration+"天前");
			}
			//item.tvWeekDay.setVisibility(View.VISIBLE);
			String weekday = DateTimeUtils.time2String("EEEE", time);
			String day = DateTimeUtils.time2String("dd", time);
			SpannableString sp = new SpannableString(day + "\n" + weekday);
			sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, day.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			sp.setSpan(new AbsoluteSizeSpan((int) context.getResources().getDimension(R.dimen.home_screen_date_info_text_size)) , 0, day.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			sp.setSpan(new AbsoluteSizeSpan((int) context.getResources().getDimension(R.dimen.home_screen_weekday_info_text_size)) , day.length() + 1, day.length()+weekday.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			item.tvDate.setText(sp);
/*			view.setPadding(0, DensityUtil.dip2px(context, 15), 0, 0);*/
		} else {
		/*	view.setPadding(0, 0, 0, 0);*/
			item.tvDays.setVisibility(View.GONE);
			item.tvDate.setVisibility(View.INVISIBLE);
			//item.tvWeekDay.setVisibility(View.GONE);
			
		}
		if(isSigned == 1){
			item.ivIsSigned.setVisibility(View.VISIBLE);
		} else {
			item.ivIsSigned.setVisibility(View.GONE);
		}
		
		item.rlNoteItem.setPadding(DensityUtil.dip2px(context, 5) + 1, DensityUtil.dip2px(context, 10), 0, 0);
		final int id = cursor.getInt((cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_ID)));
		
		item.rlNoteItem.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("=== longclick ===" + id + "is signed : " + isSigned + " lastFlag :" + lastFlag);

				CharSequence[] items = null;
				if(isSigned == 1){
					items = transactItemsUnsign;
				}else{
					items = transactItemsSign;
				}
				mbuilder.setItems(items, new DialogInterface.OnClickListener() {  
				    public void onClick(DialogInterface dialog, int item) {  
				        System.out.println(item + "ID : " + id + " isSigned : " + isSigned);
				        dialog.dismiss();
				        switch (item) {
						case 0:
							
							break;
						case 1:
							ContentValues contentValues = new ContentValues();					
							contentValues = new ContentValues();
							contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT_SIGNED,isSigned==0);
							mDb.updateLocalNotes(contentValues, id);
							break;
						case 2:
							mDb.deleteLocalNOTEs(id, lastFlag, time);
							break;
						default:
							break;
						}
				    }  
				 });  
				madTransact = mbuilder.create(); 
				madTransact.show();
				return false;
			}
		});
		EventArgs args = new EventArgs();
		args.putExtra("time", time);
		args.putExtra("lastFlag", lastFlag);
		view.setTag(args);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = mInflater.inflate(R.layout.note_oneday_listview_item, null);
		NoteOneDayItem item = new NoteOneDayItem();
		item.tvDate = (TextView) view.findViewById(R.id.tv_oneday_date);
		item.tvDays = (TextView) view.findViewById(R.id.tv_oneday_days);
		item.tvTitle = (TextView) view.findViewById(R.id.tv_oneday_title);
		item.ivIsSigned = (ImageView) view.findViewById(R.id.iv_oneday_is_signed);
		item.tvTime = (TextView) view.findViewById(R.id.tv_oneday_time);
		item.rlDay= (RelativeLayout) view.findViewById(R.id.rl_day);
		item.rlNoteItem = (RelativeLayout) view.findViewById(R.id.rl_note_item);
		item.tvNoteContent = (TextView) view.findViewById(R.id.tv_note_content);
		view.setTag(R.layout.note_oneday_listview_item,item);
		return view;
	}
	
	private class NoteOneDayItem{
		private TextView tvDate;
		private TextView tvDays;
		private TextView tvTitle;
		private ImageView ivIsSigned;
		private TextView tvTime;
		private TextView tvNoteContent;
		private RelativeLayout rlDay;
		private RelativeLayout rlNoteItem;
	}


}

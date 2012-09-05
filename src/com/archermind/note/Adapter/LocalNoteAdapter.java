package com.archermind.note.Adapter;

import java.util.ArrayList;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Provider.DatabaseManager;
import com.archermind.note.Screens.EditNoteScreen;
import com.archermind.note.Screens.MainScreen;
import com.archermind.note.Screens.ShareScreen;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.DateTimeUtils;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LocalNoteAdapter extends CursorAdapter {
	private LayoutInflater inflater;
	private static CharSequence[] transactItemsSign = { "分享", "标记", "删除" };
	private static CharSequence[] transactItemsUnsign = { "分享", "取消标记", "删除" };
	private AlertDialog.Builder mbuilder;
	private AlertDialog madTransact;
	private static DatabaseManager mDb = ServiceManager.getDbManager();
	private Context mContext;

	public LocalNoteAdapter(Context context, Cursor c) {
		super(context, c);
		mContext = context;
		inflater = LayoutInflater.from(context);
		mbuilder = new AlertDialog.Builder(context);
		mbuilder.setTitle("请操作");
	}

	@Override
	public void bindView(View view, final Context context, final Cursor cursor) {
		final NoteItem item = (NoteItem) view
				.getTag(R.layout.note_month_listview_item);
		final String title = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_TITLE));
		final long time = cursor.getLong(cursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME));
		final boolean lastFlag = cursor.getInt(cursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_LAST_FLAG)) == 1;
		final int isSigned = cursor.getInt(cursor
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
			/* view.setPadding(0, DensityUtil.dip2px(context, 15), 0, 0); */
		} else {
			/* view.setPadding(0, 0, 0, 0); */
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
			item.vFirstNoteDiv.setVisibility(View.GONE);
			item.tvDate.setVisibility(View.INVISIBLE);
			// item.tvWeekDay.setVisibility(View.GONE);

		}
		if (isSigned == 1) {
			item.ivIsSigned.setVisibility(View.VISIBLE);
		} else {
			item.ivIsSigned.setVisibility(View.GONE);
		}

		final int id = cursor.getInt((cursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_ID)));

		item.rlNoteInfo.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("=== longclick ===" + id + "is signed : "
						+ isSigned + " lastFlag :" + lastFlag);

				CharSequence[] items = null;
				if (isSigned == 1) {
					items = transactItemsUnsign;
				} else {
					items = transactItemsSign;
				}
				mbuilder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						System.out.println(item + "ID : " + id + " isSigned : "
								+ isSigned);
						dialog.dismiss();
						switch (item) {
						case 0:
							if (!NoteApplication.getInstance().isLogin()) {
								Toast.makeText(mContext,
										R.string.no_login_info,
										Toast.LENGTH_SHORT).show();
								return;
							}
							ArrayList<String> picPathList = EditNoteScreen.getNotePictureFromZip(cursor.getString(cursor
									.getColumnIndex(DatabaseHelper.COLUMN_NOTE_LOCAL_CONTENT)));
							String sid = cursor.getString((cursor
									.getColumnIndex(DatabaseHelper.COLUMN_NOTE_SERVICE_ID)));
							String action = sid == null ? "A" : "M";
							Intent intent = new Intent(mContext,
									ShareScreen.class);
							intent.putStringArrayListExtra("picpathlist",
									picPathList);
							intent.putExtra("noteid", String.valueOf(id));
							intent.putExtra("title", title);
							intent.putExtra("action", action);
							intent.putExtra("sid", sid);
							mContext.startActivity(intent);
							break;
						case 1:
							ContentValues contentValues = new ContentValues();
							contentValues = new ContentValues();
							contentValues.put(
									DatabaseHelper.COLUMN_NOTE_CONTENT_SIGNED,
									isSigned == 0);
							mDb.updateLocalNotes(contentValues, id);
							break;
						case 2:
							mDb.deleteLocalNOTEs(id, time);
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

		final String notePath = cursor.getString((cursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_LOCAL_CONTENT)));
		item.rlNoteInfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra("notePath", notePath);
				intent.putExtra("isNewNote", false);
				intent.putExtra("noteID", id);
				intent.putExtra("title", title);
				intent.setClass(MainScreen.mContext, EditNoteScreen.class);
				MainScreen.mContext.startActivity(intent);
			}
		});
		EventArgs args = new EventArgs();
		args.putExtra("time", time);
		args.putExtra("lastFlag", lastFlag);
		view.setTag(args);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.note_month_listview_item, null);
		NoteItem item = new NoteItem();
		item.tvDate = (TextView) view.findViewById(R.id.tv_date);
		// item.tvWeekDay = (TextView) view.findViewById(R.id.tv_week_day);
		item.vFirstNoteDiv = (View) view
				.findViewById(R.id.v_div_first_note_only);
		item.tvTitle = (TextView) view.findViewById(R.id.tv_title);
		/*
		 * item.commentCount = (TextView)
		 * view.findViewById(R.id.tv_comment_count);
		 */
		item.ivIsSigned = (ImageView) view.findViewById(R.id.iv_is_signed);
		item.tvTime = (TextView) view.findViewById(R.id.tv_time);
		item.rlDay = (RelativeLayout) view.findViewById(R.id.rl_day);
		item.rlNoteInfo = (RelativeLayout) view.findViewById(R.id.rl_note_info);
		view.setTag(R.layout.note_month_listview_item, item);
		return view;
	}

	private class NoteItem {
		private TextView tvDate;
		// private TextView tvWeekDay;
		private View vFirstNoteDiv;
		private TextView tvTitle;
		private ImageView ivIsSigned;
		private TextView tvTime;
		/* private TextView commentCount; */
		private RelativeLayout rlDay;
		private RelativeLayout rlNoteInfo;
	}

}

package com.archermind.note.Provider;

import java.util.Calendar;

import com.archermind.note.Utils.DateTimeUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class DatabaseManager {
	private Context context;
	private DatabaseHelper databaseHelper;
	private SQLiteDatabase database;

	public DatabaseManager(Context context) {
		this.context = context;
	}

	public void open() {
		databaseHelper = new DatabaseHelper(context);
		database = databaseHelper.getWritableDatabase();
	}

	public void close() {
		databaseHelper.close();
		database.close();
	}
	
	public boolean insertLocalNotes(ContentValues values, long timeInMillis) {
		Cursor c = queryTodayLocalNOTEs(timeInMillis);
		values.put(DatabaseHelper.COLUMN_NOTE_LAST_FLAG, true);
		if(c.getCount()>0){
			c.moveToFirst();
			int _id = c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_NOTE_ID));
			ContentValues v = new ContentValues();
			v.put(DatabaseHelper.COLUMN_NOTE_LAST_FLAG, false);
			updateLocalNotes(v, _id);
			c.close();
		}
		return database.insert(DatabaseHelper.TAB_NOTE, null, values) > 0;
	}

	public Cursor queryLocalNotes() {
		return database.query(DatabaseHelper.TAB_NOTE, null, null, null,
				null, null, DatabaseHelper.COLUMN_NOTE_CREATE_TIME + " DESC");
	}
	
	
	public void updateLocalNotes(ContentValues values, int id){
	    database.update(DatabaseHelper.TAB_NOTE, values, DatabaseHelper.COLUMN_NOTE_ID + " =? ", new String[] { String.valueOf(id)});
	}
	
	public void deleteLocalNOTEs(int id, boolean lastFlag, long timeInMillis) {
		   database.delete(DatabaseHelper.TAB_NOTE,
					DatabaseHelper.COLUMN_NOTE_ID + " =? ",
					new String[] { String.valueOf(id) });
		   if(lastFlag){//如果该日程是一天的第一条日程，则修改该天的第二条日程的标志位
				Cursor c = queryTodayLocalNOTEs(timeInMillis);
				if(c.getCount() > 0){
					c.moveToFirst();
					int _id = c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_NOTE_ID));
					ContentValues values = new ContentValues();
					values.put(DatabaseHelper.COLUMN_NOTE_LAST_FLAG, true);
					updateLocalNotes(values, _id);
					c.close();
				}
			}
	}

	public Cursor queryWeekLocalNOTEs(long timeInMillis) {
		return database
				.query(DatabaseHelper.TAB_NOTE,
						null,
						DatabaseHelper.COLUMN_NOTE_CREATE_TIME
								+ " BETWEEN ? AND ? ",
						new String[] { String.valueOf(DateTimeUtils.getDayOfWeek(Calendar.MONDAY, timeInMillis)), String.valueOf(DateTimeUtils.getDayOfWeek(Calendar.SUNDAY, timeInMillis)) },
						null, null, DatabaseHelper.COLUMN_NOTE_CREATE_TIME + " DESC");
	}
	
	
	
	public Cursor queryTodayLocalNOTEs(long timeInMillis) {
		return database
				.query(DatabaseHelper.TAB_NOTE,
						null,
						DatabaseHelper.COLUMN_NOTE_CREATE_TIME
								+ " BETWEEN ? AND ? ",
						new String[] { String.valueOf(DateTimeUtils.getToday(Calendar.AM, timeInMillis)), String.valueOf(DateTimeUtils.getToday(Calendar.PM, timeInMillis)) },
						null, null, DatabaseHelper.COLUMN_NOTE_CREATE_TIME + " DESC");
	}
	

	public Cursor queryMonthLocalNOTES(int month, int year){
		return database
				.query(DatabaseHelper.TAB_NOTE,
						null,
						DatabaseHelper.COLUMN_NOTE_CREATE_TIME
								+ " BETWEEN ? AND ? ",
						new String[] { String.valueOf(DateTimeUtils.getMonthStart(month, year)), String.valueOf(DateTimeUtils.getMonthStart(month+1, year)) },
						null, null, DatabaseHelper.COLUMN_NOTE_CREATE_TIME + " DESC");
	}
	
	public Cursor query3DaysBeforeLocalNOTEs(long timeInMillis) {
		return database
				.query(DatabaseHelper.TAB_NOTE,
						null,
						DatabaseHelper.COLUMN_NOTE_CREATE_TIME
								+ " BETWEEN ? AND ? ",
						new String[] { String.valueOf(DateTimeUtils.getThreeDaysBefore(timeInMillis)), String.valueOf(DateTimeUtils.getYesterdayEnd(timeInMillis)) },
						null, null, DatabaseHelper.COLUMN_NOTE_CREATE_TIME + " DESC");
	}
}

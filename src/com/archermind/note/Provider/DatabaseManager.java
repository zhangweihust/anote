package com.archermind.note.Provider;

import java.util.Calendar;

import com.archermind.note.Utils.DateTimeUtils;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class DatabaseManager {
	private Context context;
	private DatabaseHelper databaseHelper;
	private SQLiteDatabase database;
	
	public static int NO_NOTE = 0;
	public static int HAS_NOTE = 1;
	public static int HAS_SIGNED = 2;

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
	
	public long insertLocalNotes(ContentValues values) {
		Cursor c = queryTodayLocalNOTEs(values.getAsLong(databaseHelper.COLUMN_NOTE_CREATE_TIME));
		values.put(DatabaseHelper.COLUMN_NOTE_LAST_FLAG, true);
		if(c.getCount()>0){
			c.moveToFirst();
			int _id = c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_NOTE_ID));
			ContentValues v = new ContentValues();
			v.put(DatabaseHelper.COLUMN_NOTE_LAST_FLAG, false);
			updateLocalNotes(v, _id);
		}
		c.close();
		return database.insert(DatabaseHelper.TAB_NOTE, null, values);
	}

	public Cursor queryLocalNotes() {
		return database.query(DatabaseHelper.TAB_NOTE, null, null, null, null,
				 null, DatabaseHelper.COLUMN_NOTE_CREATE_TIME + " DESC");
	}
	
	
	public void updateLocalNotes(ContentValues values, int id){
	    database.update(DatabaseHelper.TAB_NOTE, values, DatabaseHelper.COLUMN_NOTE_ID + " =? ", new String[] { String.valueOf(id)});
	}
	
	public void deleteLocalNOTEs(int id,long timeInMillis) {	
		   Cursor dCursor = database.query(DatabaseHelper.TAB_NOTE, null, DatabaseHelper.COLUMN_NOTE_ID + " = ? ", new String[]{String.valueOf(id)}, null, null, null); 
		   if(dCursor.moveToNext()){ 
			   boolean lastFlag = dCursor.getInt(dCursor.getColumnIndex(databaseHelper.COLUMN_NOTE_LAST_FLAG)) == 1;
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
					}
					c.close();
				}
		   }
		   dCursor.close();
	}

/*	public Cursor queryWeekLocalNOTEs(int usrid, long timeInMillis) {
		return database
				.query(DatabaseHelper.TAB_NOTE,
						null,
						DatabaseHelper.COLUMN_NOTE_CREATE_TIME
								+ " BETWEEN ? AND ? AND " + DatabaseHelper.COLUMN_NOTE_USER_ID + " =? ",
						new String[] { String.valueOf(DateTimeUtils.getDayOfWeek(Calendar.MONDAY, timeInMillis)), String.valueOf(DateTimeUtils.getDayOfWeek(Calendar.SUNDAY, timeInMillis)), String.valueOf(usrid) },
						null, null, DatabaseHelper.COLUMN_NOTE_CREATE_TIME + " DESC");
	}*/
	
	
	
	public Cursor queryTodayLocalNOTEs(long timeInMillis) {
		return database
				.query(DatabaseHelper.TAB_NOTE,
						null,
						DatabaseHelper.COLUMN_NOTE_CREATE_TIME
								+ " BETWEEN ? AND ? ",
						new String[] { String.valueOf(DateTimeUtils.getToday(Calendar.AM, timeInMillis)), String.valueOf(DateTimeUtils.getToday(Calendar.PM, timeInMillis))},
						null, null, DatabaseHelper.COLUMN_NOTE_CREATE_TIME + " DESC");
	}
	

	public Cursor queryMonthLocalNOTES(int month, int year){
		return database
				.query(DatabaseHelper.TAB_NOTE,
						null,
						DatabaseHelper.COLUMN_NOTE_CREATE_TIME
								+ " BETWEEN ? AND ? ",
						new String[] { String.valueOf(DateTimeUtils.getMonthStart(month, year)), String.valueOf(DateTimeUtils.getMonthStart(month+1, year))},
						null, null, DatabaseHelper.COLUMN_NOTE_CREATE_TIME + " DESC");
	}
	/*
	public Cursor query3DaysBeforeLocalNOTEs(long timeInMillis) {
		return database
				.query(DatabaseHelper.TAB_NOTE,
						null,
						DatabaseHelper.COLUMN_NOTE_CREATE_TIME
								+ " BETWEEN ? AND ? ",
						new String[] { String.valueOf(DateTimeUtils.getThreeDaysBefore(timeInMillis)), String.valueOf(DateTimeUtils.getYesterdayEnd(timeInMillis)) },
						null, null, DatabaseHelper.COLUMN_NOTE_CREATE_TIME + " DESC");
	}*/
	
	public int queryTodayLocalNotesStatus(long timeInMillis){
		Cursor c = queryTodayLocalNOTEs(timeInMillis);
		int count = c.getCount();
		if(count == 0){
			c.close();
			return NO_NOTE;
		}else{
			int i = 0; 
			while(c.moveToNext()){
				i = c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CONTENT_SIGNED));
				if(i == DatabaseHelper.SIGNED){
					c.close();
					return HAS_SIGNED;
				}
			}
			c.close();
			return HAS_NOTE;
		}
	}
	
	public boolean insertLocalPhoto(ContentValues values) {
		return database.insert(DatabaseHelper.TAB_PHOTO, null, values) > 0;
	}

	public Cursor queryPhotoData() {
		return database.query(DatabaseHelper.TAB_PHOTO, null, null, null, null,
				null, null);
	}
	
	public Cursor queryLocalNotesById(int id) {
		return database
		.query(DatabaseHelper.TAB_NOTE,
				null,
				DatabaseHelper.COLUMN_NOTE_ID + " =? ",
				new String[] { String.valueOf(id) },
				null, null, null);
	}
	
	
	public long insertInformation(ContentValues values) {
		return database.insert(DatabaseHelper.TAB_REPLY, null, values);
	}

	public Cursor queryInformations() {
		return database.query(DatabaseHelper.TAB_REPLY, null, null, null, null,
				 null, DatabaseHelper.COLUMN_REPLY_TIME + " DESC");
	}
	
	public Cursor queryInformationsAfter(Long time) {
		return database.query(DatabaseHelper.TAB_REPLY, null, DatabaseHelper.COLUMN_REPLY_TIME + " > ? ",
				new String[] {String.valueOf(time)}, null,
				 null, DatabaseHelper.COLUMN_REPLY_TIME + " DESC");
	}
	
/*	public Cursor querySomeInformationsBefore(Long time, int count) {
		return database.query(DatabaseHelper.TAB_REPLY, null, DatabaseHelper.COLUMN_REPLY_TIME + " < ? ",
				new String[] {String.valueOf(time)}, null,
				 null, DatabaseHelper.COLUMN_REPLY_TIME + " DESC" , String.valueOf(count));
	}
	*/
	public void deleteInformations(long timeInMillis) {
		   Cursor dCursor = database.query(DatabaseHelper.TAB_REPLY, null, DatabaseHelper.COLUMN_REPLY_TIME + " < ? ", new String[]{String.valueOf(timeInMillis)}, null, null, null); 
		   while(dCursor.moveToNext()){ 
			   int id = dCursor.getInt(dCursor.getColumnIndex(databaseHelper.COLUMN_REPLY_ID));
			   database.delete(DatabaseHelper.TAB_NOTE,
						DatabaseHelper.COLUMN_REPLY_ID + " =? ",
						new String[] { String.valueOf(id) });
		   }
		   dCursor.close();
	}
}

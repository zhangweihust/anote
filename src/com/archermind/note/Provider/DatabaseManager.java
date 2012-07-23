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
	
	public boolean insertLocalNotes(ContentValues values) {
		return database.insert(DatabaseHelper.TAB_NOTE, null, values) > 0;
	}

	public Cursor queryLocalNotes() {
		return database.query(DatabaseHelper.TAB_NOTE, null, null, null,
				null, null, null);
	}
	public boolean deleteLocalNOTEs(int id) {
		return database.delete(DatabaseHelper.TAB_NOTE,
				DatabaseHelper.COLUMN_NOTE_ID + " =? ",
				new String[] { String.valueOf(id) }) >= 0;
	}

	public Cursor queryWeekLocalNOTEs() {
		return database
				.query(DatabaseHelper.TAB_NOTE,
						null,
						DatabaseHelper.COLUMN_NOTE_CREATE_TIME
								+ " BETWEEN ? AND ? ",
						new String[] { String.valueOf(DateTimeUtils.getDayOfWeek(Calendar.MONDAY)), String.valueOf(DateTimeUtils.getDayOfWeek(Calendar.SUNDAY)) },
						null, null, DatabaseHelper.COLUMN_NOTE_CREATE_TIME + " ASC");
	}
	
	public Cursor queryTodayLocalNOTEs() {
		return database
				.query(DatabaseHelper.TAB_NOTE,
						null,
						DatabaseHelper.COLUMN_NOTE_CREATE_TIME
								+ " BETWEEN ? AND ? ",
						new String[] { String.valueOf(DateTimeUtils.getToday(Calendar.AM)), String.valueOf(DateTimeUtils.getToday(Calendar.PM)) },
						null, null, null);
	}
	

	public Cursor query3DaysBeforeLocalNOTEs() {
		return database
				.query(DatabaseHelper.TAB_NOTE,
						null,
						DatabaseHelper.COLUMN_NOTE_CREATE_TIME
								+ " BETWEEN ? AND ? ",
						new String[] { String.valueOf(DateTimeUtils.getThreeDaysBefore()), String.valueOf(DateTimeUtils.getYesterdayEnd()) },
						null, null, DatabaseHelper.COLUMN_NOTE_CREATE_TIME + " ASC");
	}
}

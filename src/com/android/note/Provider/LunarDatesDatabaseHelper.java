package com.android.note.Provider;

import java.io.File;

import com.android.note.NoteApplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LunarDatesDatabaseHelper extends SQLiteOpenHelper {
	public static final String PATH = String.format("data/data/%s/databases/", NoteApplication.getContext().getPackageName());
	public static final String NAME = "lunardates.db";
	public static final String SRC_FILE = "lunardates.zip";
	public static final int version = 1;
	
	public static final int earlistYear = 1900;
	public static final int latestYear = 2049;
	
	public static final String TAB_CALENDAR_MAP = "calendar_map";
	public static final String COLUMN_CALENDAR_ID = "_id";
	public static final String COLUMN_CALENDAR_MONTH = "calendar_month";
	public static final String COLUMN_CALENDAR_LUNARDATE = "calendar_lunar_date";
	public static final String COLUMN_CALENDAR_DAYOFWEEK = "calendar_lunar_dayofweek";
	
	public LunarDatesDatabaseHelper(Context context) {
		super(context, NAME, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}

package com.archermind.note.Provider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.archermind.note.Screens.InformationScreen;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.DateTimeUtils;
import com.archermind.note.Utils.FileUtils;
import com.archermind.note.bean.UserLoginInfo;


public class DatabaseManager {
	private Context context;
	private DatabaseHelper databaseHelper;
	private SQLiteDatabase database;

	private LunarDatesDatabaseHelper lunarDatesDatabaseHelper;
	private SQLiteDatabase lunarDatesDatabase;
	
	public static int NO_NOTE = 0;
	public static int HAS_NOTE = 1;
	public static int HAS_SIGNED = 2;

	public DatabaseManager(Context context) {
		this.context = context;
	}

	public void open() {
		databaseHelper = new DatabaseHelper(context);
		database = databaseHelper.getWritableDatabase();
		File dest = new File(LunarDatesDatabaseHelper.PATH + LunarDatesDatabaseHelper.NAME);
		System.out.println(dest);
		if (!dest.exists()) {
			System.out.println("file not exist !");
			try {
				boolean isSuccess = FileUtils.unzipFirstEntryToFile(context.getAssets().open(LunarDatesDatabaseHelper.SRC_FILE), dest);
				System.out.println("success ? " + isSuccess);
				if(!isSuccess){
					dest.delete();
					ServiceManager.exit();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		lunarDatesDatabaseHelper= new LunarDatesDatabaseHelper(context);
		lunarDatesDatabase = lunarDatesDatabaseHelper.getWritableDatabase();
	}

	public void close() {
		databaseHelper.close();
		database.close();
		lunarDatesDatabaseHelper.close();
		lunarDatesDatabase.close();
	}
	
	public long insertLocalNotes(ContentValues values) {
		long now = values.getAsLong(databaseHelper.COLUMN_NOTE_CREATE_TIME);
		Cursor c = queryTodayLocalNOTEs(now);
		if(c.getCount()>0){
			c.moveToFirst();
			long time = c.getLong(c.getColumnIndex(databaseHelper.COLUMN_NOTE_CREATE_TIME));
			if(time < now){
				int _id = c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_NOTE_ID));
				ContentValues v = new ContentValues();
				v.put(DatabaseHelper.COLUMN_NOTE_LAST_FLAG, false);
				updateLocalNotes(v, _id);
				values.put(DatabaseHelper.COLUMN_NOTE_LAST_FLAG, true);
			}else{
				values.put(DatabaseHelper.COLUMN_NOTE_LAST_FLAG, false);
			}
		}else{
			values.put(DatabaseHelper.COLUMN_NOTE_LAST_FLAG, true);
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
	
	public void deleteLocalNOTEs(int id) {	
		   Cursor dCursor = database.query(DatabaseHelper.TAB_NOTE, null, DatabaseHelper.COLUMN_NOTE_ID + " = ? ", new String[]{String.valueOf(id)}, null, null, null); 
		   if(dCursor.moveToNext()){ 
			   boolean lastFlag = dCursor.getInt(dCursor.getColumnIndex(databaseHelper.COLUMN_NOTE_LAST_FLAG)) == 1;
			   long timeInMillis = dCursor.getLong(dCursor.getColumnIndex(databaseHelper.COLUMN_NOTE_CREATE_TIME));
			   database.delete(DatabaseHelper.TAB_NOTE,
						DatabaseHelper.COLUMN_NOTE_ID + " =? ",
						new String[] { String.valueOf(id) });
			   if(lastFlag){
					Cursor c = queryTodayLocalNOTEs(timeInMillis);
					//System.out.println("==== lastFlag:" + lastFlag + "c.getCount" + c.getCount() + "  " + timeInMillis);
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
		System.out.println("===userid===" + ServiceManager.getUserId());
		return database.query(DatabaseHelper.TAB_REPLY, null, databaseHelper.COLUMN_REPLY_USER_ID + " = ?", new String[] {String.valueOf(InformationScreen.getUserId())}, null,
				 null, DatabaseHelper.COLUMN_REPLY_TIME + " DESC");
	}
	
	public Cursor queryInformationsAfter(Long time) {
		return database.query(DatabaseHelper.TAB_REPLY, null, DatabaseHelper.COLUMN_REPLY_TIME + " > ? AND " + databaseHelper.COLUMN_REPLY_USER_ID + " = ?",
				new String[] {String.valueOf(time), String.valueOf(ServiceManager.getUserId())}, null,
				 null, DatabaseHelper.COLUMN_REPLY_TIME + " DESC");
	}
	
	public Cursor queryInformationsBefore(Long time) {
		return database.query(DatabaseHelper.TAB_REPLY, null, DatabaseHelper.COLUMN_REPLY_TIME + " < ? AND " + databaseHelper.COLUMN_REPLY_USER_ID + " = ?",
				new String[] {String.valueOf(time), String.valueOf(ServiceManager.getUserId())}, null,
				 null, DatabaseHelper.COLUMN_REPLY_TIME + " DESC");
	}
	
	public void deleteInformations(long timeInMillis) {
		   Cursor dCursor = database.query(DatabaseHelper.TAB_REPLY, null, DatabaseHelper.COLUMN_REPLY_TIME + " < ? AND " + databaseHelper.COLUMN_REPLY_USER_ID + " = ?", new String[]{String.valueOf(timeInMillis), String.valueOf(InformationScreen.getUserId())}, null, null, null); 
		   while(dCursor.moveToNext()){ 
			   int id = dCursor.getInt(dCursor.getColumnIndex(databaseHelper.COLUMN_REPLY_ID));
			   database.delete(DatabaseHelper.TAB_NOTE,
						DatabaseHelper.COLUMN_REPLY_ID + " =? ",
						new String[] { String.valueOf(id) });
		   }
		   dCursor.close();
	}
	
/*	public long getLastestReplyTime(){
		Cursor cursor = database.query(databaseHelper.TAB_REPLY, null, DatabaseHelper.COLUMN_REPLY_USER_ID + " = ?", new String[]{String.valueOf(InformationScreen.getUserId())}, null, null, databaseHelper.COLUMN_REPLY_TIME + " DESC", String.valueOf(1));
		long time = 0;
		if(cursor.moveToFirst()){
			time = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_REPLY_TIME));
		}
		System.out.println("===latest time = " + time);
		return time;
	}
	
	public long getEarlistReplyTime(){
		Cursor cursor = database.query(databaseHelper.TAB_REPLY, null, DatabaseHelper.COLUMN_REPLY_USER_ID + " = ?", new String[]{String.valueOf(InformationScreen.getUserId())}, null, null, databaseHelper.COLUMN_REPLY_TIME, String.valueOf(1));
		long time = 0;
		if(cursor.moveToFirst()){
			time = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_REPLY_TIME));
		}
		System.out.println("===earlist time = " + time);
		return time;
	}*/
	
    //将用户帐号、密码、是否保存密码信息添加到user表中
	public long insertUser(String username, String password, long isSave) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_USERNAME, username);
		values.put(DatabaseHelper.COLUMN_PASSWORD, password);
		values.put(DatabaseHelper.COLUMN_ISSAVE, isSave);
		return database.insert(DatabaseHelper.TAB_USER, null, values);
	}
	
    //查找user表中所有信息
	public List<UserLoginInfo> listAllUser() {
		List<UserLoginInfo> list = new ArrayList<UserLoginInfo>();
		Cursor cursor = database.query(DatabaseHelper.TAB_USER, 
				null, null, null, null, null, null);
		while (cursor.moveToNext()) {
			String username = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.COLUMN_USERNAME));
			String password = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.COLUMN_PASSWORD));
			long isSave = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ISSAVE));
			UserLoginInfo info = new UserLoginInfo(username, password, isSave);
			list.add(info);
		}
		cursor.close();
		return list;
	}
	
    //修改是否记住密码
	public int changeUserSavePassword(long isSave,String name){
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_ISSAVE, isSave);
		return database.update(DatabaseHelper.TAB_USER, values, 
				DatabaseHelper.COLUMN_USERNAME+"=?", new String[]{name});
	}
	
    //修改密码
	public int changeUserPassword(String pwd,String name){
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_PASSWORD, pwd);
		return database.update(DatabaseHelper.TAB_USER, values, 
				DatabaseHelper.COLUMN_USERNAME+"=?", new String[]{name});
	}
	
	public Cursor queryLunarDate(String month) {
		return lunarDatesDatabase.query(LunarDatesDatabaseHelper.TAB_CALENDAR_MAP, null,
				LunarDatesDatabaseHelper.COLUMN_CALENDAR_MONTH + " = ?",
				new String[]{String.valueOf(month)}, null, null, null);
	}
}

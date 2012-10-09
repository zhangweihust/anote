package com.archermind.note.Provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{
	
	public static final String NAME = "note.db";
	private static final int version = 2;
	
	public static int NOT_SIGN = 0;
	public static int SIGNED = 1;
	
	public static final String TAB_NOTE = "note";
	public static final String COLUMN_NOTE_ID = "_id";//笔记ID
	public static final String COLUMN_NOTE_USER_ID = "user_id";//用户ID
	public static final String COLUMN_NOTE_SHARE = "share";//是否分享
	public static final String COLUMN_NOTE_TYPE = "type";//笔记类别
	public static final String COLUMN_NOTE_OPER_FLAG = "oper_flag";//笔记操作标志
	public static final String COLUMN_NOTE_CREATE_TIME = "create_time";//笔记创建时间
	public static final String COLUMN_NOTE_UPDATE_TIME = "update_time";//笔记更新时间
	public static final String COLUMN_NOTE_WEATHER = "weather";//写笔记当天的天气
	public static final String COLUMN_NOTE_TITLE = "title";//笔记标题
	public static final String COLUMN_NOTE_CONTENT_TYPE = "content_type";//笔记内容的类型
	public static final String COLUMN_NOTE_CONTENT_SIGNED = "content_signed";//笔记是否被标记为重要，0／1
	public static final String COLUMN_NOTE_LOCAL_CONTENT = "local_content";//笔记本地的存储地址
	public static final String COLUMN_NOTE_SERVICE_ID = "content";//笔记服务器上的存储地址
	public static final String COLUMN_NOTE_LAST_FLAG = "last_flag";//是否为当天最后一条日志

	private static final String CRETAE_TAB_NOTE = " CREATE TABLE IF NOT EXISTS "
			+ TAB_NOTE
			+ " ( "
			+ COLUMN_NOTE_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL , "
			+ COLUMN_NOTE_USER_ID
			+ " INTEGER, "
			+ COLUMN_NOTE_SHARE
			+ " INTEGER, "
			+ COLUMN_NOTE_TYPE
			+ " INTEGER, "
			+ COLUMN_NOTE_OPER_FLAG
			+ " TEXT, "
			+ COLUMN_NOTE_CREATE_TIME
			+ " TEXT, "
			+ COLUMN_NOTE_UPDATE_TIME
			+ " TEXT, "
			+ COLUMN_NOTE_WEATHER
			+ " TEXT, "
			+ COLUMN_NOTE_TITLE
			+ " TEXT, "
			+ COLUMN_NOTE_CONTENT_TYPE
			+ " TEXT, "
			+ COLUMN_NOTE_CONTENT_SIGNED
			+ " INTEGER, "
			+ COLUMN_NOTE_LOCAL_CONTENT
			+ " TEXT, "
			+ COLUMN_NOTE_SERVICE_ID
			+ " TEXT, "
			+ COLUMN_NOTE_LAST_FLAG
			+ " BOOLEAN"
			+ ")";
/*	
	public static final String TAB_USER = "user";
	public static final String COLUMN_USER_USER_ID = "user_id";//用户ID
	public static final String COLUMN_USER_INTEREST_TYPE = "interest_type";//被此用户关注OR关注此用户
	public static final String COLUMN_USER_EMAIL = "email";//用户邮箱
	public static final String COLUMN_USER_PHOTO_ID = "photo_id";//用户头像url
	public static final String COLUMN_USER_PHOTO_CACHE = "photo_cache";//用户头像的本地缓存

	private static final String CRETAE_TAB_USER = " CREATE TABLE IF NOT EXISTS "
			+ TAB_USER
			+ " ( "
			+ COLUMN_USER_USER_ID
			+ " INTEGER PRIMARY KEY , "
			+ COLUMN_USER_INTEREST_TYPE
			+ " INTEGER, "
			+ COLUMN_USER_EMAIL
			+ " TEXT, "
			+ COLUMN_USER_PHOTO_ID
			+ " TEXT, "
			+ COLUMN_USER_PHOTO_CACHE
			+ " TEXT"
			+ ")";
	
	public static final String TAB_WEATHER = "weather";
	public static final String COLUMN_WEATHER_ID = "wid";//天气ID，自动递增，主键，无实际意义
	public static final String COLUMN_WEATHER_UPDATE_TIME = "update_type";//天气的更新时间
	public static final String COLUMN_WEATHER_WEATHER = "weather";//天气，包含4天天气
	public static final String COLUMN_WEATHER_CITY = "city";//所在城市

	private static final String CRETAE_TAB_WEATHER = " CREATE TABLE IF NOT EXISTS "
			+ TAB_WEATHER
			+ " ( "
			+ COLUMN_WEATHER_ID
			+ " INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , "
			+ COLUMN_WEATHER_UPDATE_TIME
			+ " TEXT, "
			+ COLUMN_WEATHER_WEATHER
			+ " TEXT, "
			+ COLUMN_WEATHER_CITY
			+ " TEXT"
			+ ")";
	*/
	
	public static final String TAB_REPLY = "note_reply";
	public static final String COLUMN_REPLY_ID = "_id";
	public static final String COLUMN_REPLY_NICKNAME = "nickname";//回帖人昵称
	public static final String COLUMN_REPLY_PHOTO = "photo";//回帖人头像
	public static final String COLUMN_REPLY_TITLE = "title";//帖子的标题
	public static final String COLUMN_REPLY_CONTENT = "content";//回帖的内容
	public static final String COLUMN_REPLY_TIME = "time";//回帖的时间
	public static final String COLUMN_REPLY_USER_ID = "user_id";//当前的用户ID
	public static final String COLUMN_REPLY_NID = "nid";//帖子的id

	private static final String CRETAE_TAB_REPLY= " CREATE TABLE IF NOT EXISTS "
			+ TAB_REPLY
			+ " ( "
			+ COLUMN_REPLY_ID
			+ " INTEGER PRIMARY KEY , "
			+ COLUMN_REPLY_NICKNAME
			+ " TEXT, "
			+ COLUMN_REPLY_PHOTO
			+ " TEXT, "
			+ COLUMN_REPLY_TITLE
			+ " TEXT, "
			+ COLUMN_REPLY_CONTENT
			+ " TEXT, "
			+ COLUMN_REPLY_TIME
			+ " TEXT, "
			+ COLUMN_REPLY_USER_ID
			+ " INTEGER, "
			+ COLUMN_REPLY_NID
			+ " INTEGER"
			+ ")";
	
	public static final String TAB_PHOTO = "photo";
	public static final String COLUMN_PHOTO_ID = "_id";//照片ID
	public static final String COLUMN_PHOTO_NAME = "name";//照片名称
	public static final String COLUMN_PHOTO_FILEPATH = "filepath";//照片filepath
 
	private static final String CRETAE_TAB_PHOTO = " CREATE TABLE IF NOT EXISTS "
	 + TAB_PHOTO 
	 + " (["+ COLUMN_PHOTO_ID
	 + "]AUTOINC PRIMARY KEY, ["
	 + COLUMN_PHOTO_NAME
	 + "]VARCHAR2(20) NOT NULL,"
	 + COLUMN_PHOTO_FILEPATH
	 + " TEXT"
	   + ")";
	public DatabaseHelper(Context context) {
		super(context, NAME, null, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		createTabs(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

	private void createTabs(SQLiteDatabase db) {
		db.execSQL(CRETAE_TAB_NOTE);
		db.execSQL(CRETAE_TAB_REPLY);
		//db.execSQL(CRETAE_TAB_USER);
		//db.execSQL(CRETAE_TAB_WEATHER);
		//db.execSQL(CRETAE_TAB_NOTE_RELATION);
		//db.execSQL(CRETAE_TAB_PHOTO);
	}
}

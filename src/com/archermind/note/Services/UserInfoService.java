package com.archermind.note.Services;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.archermind.note.NoteApplication;
import com.archermind.note.Screens.Screen;
import com.archermind.note.Utils.DateTimeUtils;
import com.archermind.note.Utils.VersionUtil;
import com.archermind.note.thread.DeviceInfoThread;
import com.archermind.note.thread.UserActiveInfoThread;

import android.R.integer;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.format.DateFormat;

public class UserInfoService{
	public static final long COUNT_DURATION = 10 * 60 * 1000;
	public static final int COUNT_TIMES = 3;
	public static final int USER_ACTIVE_DURATION = 7;
	private DeviceInfoThread deviceInfoThread;
	private UserActiveInfoThread userActiveInfoThread;
	private long startTime;

	public boolean start() {
		UserInfoThread userInfoThread = new UserInfoThread();
		userInfoThread.start();
		return true;
	}

	public boolean stop() {
		if (deviceInfoThread != null) {
			deviceInfoThread.stopThread();
		}
		
		if (userActiveInfoThread != null) {
			userActiveInfoThread.stopThread();
		}
		Context context = NoteApplication.getContext();
		SharedPreferences spActive = context.getSharedPreferences(UserActiveInfoThread.PREF, 0);
		SharedPreferences.Editor editor = spActive.edit();
		long lastDuration = spActive.getLong(UserActiveInfoThread.PREF_KEY_DURATION, 0);
		long duration = System.currentTimeMillis() - startTime + lastDuration;
		editor.putLong(UserActiveInfoThread.PREF_KEY_DURATION, duration);
		editor.commit();
		
		return true;
	}
	
	class UserInfoThread extends Thread{
		@Override
		public void run(){
			System.out.println("===userinfothread== run");
			Context context = NoteApplication.getContext();
			SharedPreferences sp = context.getSharedPreferences(DeviceInfoThread.PREF, 0);
		    int v = sp.getInt(DeviceInfoThread.PREF_KEY_VERSION, 0);
		    if(v == 0){
		    	SharedPreferences.Editor editor = sp.edit();
		    	editor.putInt(DeviceInfoThread.PREF_KEY_VERSION, VersionUtil.getVerCode(context));
		    	editor.putInt(deviceInfoThread.PREF_KEY_OK_TIME, 0);
		    	editor.commit();	    	
		    	deviceInfoThread = new DeviceInfoThread();
		    	deviceInfoThread.start();
		    }else if(v <= VersionUtil.getVerCode(context) && sp.getInt(deviceInfoThread.PREF_KEY_OK_TIME, 0) == 0){
		    	deviceInfoThread = new DeviceInfoThread();
		    	deviceInfoThread.start();
		    	
		    }
		    
		    SharedPreferences spActive = context.getSharedPreferences(UserActiveInfoThread.PREF, 0);    
		    long lastOKDate = spActive.getLong(UserActiveInfoThread.PREF_KEY_OK_LASTDATE, 0);
		    long curDate = Long.parseLong(DateTimeUtils.time2String("yyyyMMdd", System.currentTimeMillis()));
		    SharedPreferences.Editor editor = spActive.edit();
		    if(lastOKDate == 0){
		    	editor.putLong(UserActiveInfoThread.PREF_KEY_OK_LASTDATE, curDate);
		    }else if((curDate - lastOKDate) >= USER_ACTIVE_DURATION){
		    	userActiveInfoThread = new UserActiveInfoThread();
		    	userActiveInfoThread.start();
		    }
		    long lastDate = spActive.getLong(UserActiveInfoThread.PREF_KEY_LAST_DATE, 0);
		    if(lastDate < curDate){
		    	editor.putLong(UserActiveInfoThread.PREF_KEY_LAST_DATE, curDate);
		    	editor.putInt(UserActiveInfoThread.PREF_KEY_TIMES, spActive.getInt(UserActiveInfoThread.PREF_KEY_TIMES, 0)+1);
		    }
		    editor.commit();		    
		    startTime = System.currentTimeMillis();
		}
	}

}

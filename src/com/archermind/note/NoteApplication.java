package com.archermind.note;

import com.archermind.note.Services.ServiceManager;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class NoteApplication extends Application {

	private final static String TAG = "Note";

	private static NoteApplication instance;

	private boolean isLogin = false;

	private int mUserId;
	private String mUserName;

	public static Context getContext() {
		return NoteApplication.instance;
	}

	public boolean isLogin() {
		return this.isLogin;
	}

	public void setLogin(boolean isLogin) {
		this.isLogin = isLogin;
	}
	
	public String getUserName() {
		return this.mUserName;
	}

	public void setUserName(String userName) {
		this.mUserName = userName;
	}
	
	public int getUserId() {
		return this.mUserId;
	}

	public void setUserId(int userId) {
		this.mUserId = userId;
	}

	@Override
	public void onCreate() {
		NoteApplication.instance = this;
		super.onCreate();
		if (ServiceManager.isStarted()) {
		} else {
			if (!ServiceManager.start()) {
				ServiceManager.exit();
				return;
			}
		}
	}

	public static NoteApplication getInstance() {
		return instance;
	}
	
	@SuppressWarnings("rawtypes")
	public static void LogD(Class classz, String str) {
		// Log.d(TAG, classz.getCanonicalName() + "--->" + str);
	}
}

 
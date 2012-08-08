package com.archermind.note;


import com.archermind.note.Services.ServiceManager;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class NoteApplication extends Application{
	
	private static NoteApplication instance;
	private final static String TAG = "Note";
	private static boolean isLogin = false;
	
	public NoteApplication(){
		NoteApplication.instance = this;
	}
	
	 public static Context getContext() {
	        return NoteApplication.instance;
	    }
	 
	 
	 
	 public static boolean isLogin() {
		return isLogin;
	}

	public static void setLogin(boolean isLogin) {
		NoteApplication.isLogin = isLogin;
	}

	@Override
		public void onCreate() {
			super.onCreate();
			 if (ServiceManager.isStarted()) {
				} else {
					if (!ServiceManager.start()) {
						ServiceManager.exit();
						return;
					}
				}
		}
	 
	 @SuppressWarnings("rawtypes")
		public static void LogD(Class classz, String str){
			//Log.d(TAG, classz.getCanonicalName() + "--->" + str);
		}
}

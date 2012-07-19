package com.archermind.note;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class NoteApplication extends Application{
	
	private static NoteApplication instance;
	private final static String TAG = "Schedule";
	
	public NoteApplication(){
		NoteApplication.instance = this;
	}
	
	 public static Context getContext() {
	        return NoteApplication.instance;
	    }
	 
	 @Override
		public void onCreate() {
			super.onCreate();
		}
	 
	 @SuppressWarnings("rawtypes")
		public static void LogD(Class classz, String str){
			Log.d(TAG, classz.getCanonicalName() + "--->" + str);
		}
}

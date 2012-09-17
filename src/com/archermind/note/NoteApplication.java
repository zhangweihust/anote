package com.archermind.note;

import com.archermind.note.Services.ServiceManager;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;

public class NoteApplication extends Application {

	private final static String TAG = "Note";

	public static String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static String savePath = sdcard + "/aNote/";
	public static String packagePath = savePath + "package/";
	public static String crashPath = savePath + "crash/";
	public static boolean IS_AUTO_UPDATE = true;
	private static NoteApplication instance;

	public static boolean networkIsOk;

	public static Context getContext() {
		return NoteApplication.instance;
	}

	public static void toastShow(final Handler handler, final int resId) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(NoteApplication.instance, resId,
						Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		NoteApplication.instance = this;
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

 
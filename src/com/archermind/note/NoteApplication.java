package com.archermind.note;

import com.archermind.note.Services.ServiceManager;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class NoteApplication extends Application {

	private final static String TAG = "Note";

	public static String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static String savePath = sdcard + "/aNote/";
	public static String packagePath = savePath + "package/";
	public static boolean IS_AUTO_UPDATE = true;
	private static NoteApplication instance;

	private boolean downloadApkFlag = false;
	private boolean isLogin = false;

	private int mUserId;
	private String mUserName;
	private boolean mBound_Sina = false;
	private boolean mBound_QQ = false;
	private boolean mBound_Renren = false;

	private Context mTopWindowContext;

	public Context getTopWindowContext() {
		return mTopWindowContext;
	}
	
	public void setTopWindowContext(Context context) {
		this.mTopWindowContext = context;
	}
	
	public boolean isDownloadApkFlag() {
		return downloadApkFlag;
	}
	
	public void setDownloadApkFlag(boolean downloadApkFlag) {
		this.downloadApkFlag = downloadApkFlag;
	}
	
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
	
	public boolean ismBound_Sina() {
		return mBound_Sina;
	}

	public void setmBound_Sina(boolean mBound_Sina) {
		this.mBound_Sina = mBound_Sina;
	}

	public boolean ismBound_QQ() {
		return mBound_QQ;
	}

	public void setmBound_QQ(boolean mBound_QQ) {
		this.mBound_QQ = mBound_QQ;
	}

	public boolean ismBound_Renren() {
		return mBound_Renren;
	}

	public void setmBound_Renren(boolean mBound_Renren) {
		this.mBound_Renren = mBound_Renren;
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

 
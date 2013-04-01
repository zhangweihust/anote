package com.android.note.Services;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.view.WindowManager;

import com.amtcloud.mobile.android.utils.HttpUtils;
import com.android.note.NoteApplication;
import com.android.note.Provider.DatabaseManager;
import com.android.note.Utils.BitmapCache;

public class ServiceManager extends Service {

	private static final EventService eventService = new EventService();
	private static final NetworkService networkService = new NetworkService();
	private static final ExceptionService exceptionService = new ExceptionService();
	private static final UserInfoService userinfoService = new UserInfoService();
	private static boolean started;
	private static DatabaseManager dbManager = new DatabaseManager(NoteApplication.getContext());
	
	
	private static boolean _downloadApkFlag = false;
	private static boolean _isLogin = false;

	private static int _mUserId;
	private static String _mUserName = "";
	private static String _mAvatarurl = "";
	private static String _mNickname = "";
	private static String _mSex = "";
	private static String _mRegion = "";
	private static String _mSina_nickname = "";
	private static String _mQQ_nickname = "";
	private static String _mRenren_nickname = "";
	private static Context _mTopWindowContext;

	private static Handler _mHandler;

	private static WindowManager _mWindowManager;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	public static boolean start() {
		if(ServiceManager.started){
			return true;
		}
		// start Android service
		NoteApplication.getContext().startService(
				new Intent(NoteApplication.getContext(), ServiceManager.class));
		
		
		boolean success = true;

		success &= exceptionService.start();
		success &= eventService.start();
		success &= networkService.start();
		success &= userinfoService.start();
		
		dbManager.open();

		if(!success){
			NoteApplication.LogD(ServiceManager.class, "Failed to start services");
			return false;
		}
		
		ServiceManager.started = true;
		HttpUtils.setTimeOutParams(30000, 30000);	//设置与服务器进行网络交互时的连网超时和请求超时时间
		
		return true;
	}
	
	public static boolean stop() {
		if(!ServiceManager.started){
			return true;
		}
		
		// stops Android service
		NoteApplication.getContext().stopService(
				new Intent(NoteApplication.getContext(), ServiceManager.class));
		
		boolean success = true;

		success &= networkService.stop();
		success &= eventService.stop();
		success &= exceptionService.stop();
		success &= userinfoService.stop();
		
		dbManager.close();
		
		BitmapCache.getInstance().clearCache();
		if(!success){
			NoteApplication.LogD(ServiceManager.class, "Failed to stop services");
		}
		ServiceManager.started = false;
		return success;
	}
	
	public static EventService getEventservice() {
		return eventService;
	}
	public static NetworkService getNetworkService() {
		return networkService;
	}
	
	public static UserInfoService getUserInfoService(){
		return userinfoService;
	}
	
	public static DatabaseManager getDbManager() {
		return dbManager;
	}
	
	public static boolean isStarted() {
		return started;
	}

	
	public static void exit() {
		stop();
		//mainActivity.finish();
		System.exit(0);
	}
	
	
	public static Context getTopWindowContext() {
		return _mTopWindowContext;
	}
	
	public static void setTopWindowContext(Activity context) {
		_mTopWindowContext = context;
		_mWindowManager = context.getWindowManager();
	}
	
	public static WindowManager getWindowManager() {
		return _mWindowManager;
	}
	
	public static Handler getHandler() {
		return _mHandler;
	}
	
	public static void setHandler(Handler handler) {
		_mHandler = handler;
	}
	
	public static boolean isDownloadApkFlag() {
		return _downloadApkFlag;
	}
	
	public static void setDownloadApkFlag(boolean downloadApkFlag) {
		_downloadApkFlag = downloadApkFlag;
	}
	
	public static boolean isLogin() {
		return _isLogin;
	}

	public static void setLogin(boolean isLogin) {
		_isLogin = isLogin;
	}
	
	
	public static String getmAvatarurl() {
		return _mAvatarurl;
	}

	public static void setmAvatarurl(String mAvatarurl) {
		_mAvatarurl = mAvatarurl;
	}

	public static String getUserName() {
		System.out.println("====getUserName : " + _mUserName);
		return _mUserName;
	}

	public static void setUserName(String userName) {
		_mUserName = userName;
	}
	
	

	public static String getmNickname() {
		return _mNickname;
	}

	public static void setmNickname(String mNickname) {
		_mNickname = mNickname;
	}

	public static String getmSex() {
		return _mSex;
	}

	public static void setmSex(String mSex) {
		_mSex = mSex;
	}

	public static String getmRegion() {
		return _mRegion;
	}

	public static void setmRegion(String mRegion) {
		_mRegion = mRegion;
	}

	public static String getmSina_nickname() {
		return _mSina_nickname;
	}

	public static void setmSina_nickname(String mSina_nickname) {
		_mSina_nickname = mSina_nickname;
	}

	public static String getmQQ_nickname() {
		return _mQQ_nickname;
	}

	public static void setmQQ_nickname(String mQQ_nickname) {
		_mQQ_nickname = mQQ_nickname;
	}

	public static String getmRenren_nickname() {
		return _mRenren_nickname;
	}

	public static void setmRenren_nickname(String mRenren_nickname) {
		_mRenren_nickname = mRenren_nickname;
	}

	public static int getUserId() {
		return _mUserId;
	}

	public static void setUserId(int userId) {
		_mUserId = userId;
	}
	
}

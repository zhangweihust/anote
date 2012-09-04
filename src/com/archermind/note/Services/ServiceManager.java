package com.archermind.note.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.archermind.note.NoteApplication;
import com.archermind.note.Provider.DatabaseManager;
import com.archermind.note.Utils.BitmapCache;

public class ServiceManager extends Service {

	private static final EventService eventService = new EventService();
	private static final NetworkService networkService = new NetworkService();
	private static final ExceptionService exceptionService = new ExceptionService();
	private static final UserInfoService userinfoService = new UserInfoService();
	private static boolean started;
	private static DatabaseManager dbManager = new DatabaseManager(NoteApplication.getContext());
	
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

}

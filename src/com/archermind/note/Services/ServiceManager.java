package com.archermind.note.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.archermind.note.NoteApplication;

public class ServiceManager extends Service {

	private static final EventService eventService = new EventService();
	private static boolean started;
	
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

		success &= eventService.start();

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

		success &= eventService.stop();
		
		if(!success){
			NoteApplication.LogD(ServiceManager.class, "Failed to stop services");
		}
		ServiceManager.started = false;
		return success;
	}
	
	public static EventService getEventservice() {
		return eventService;
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

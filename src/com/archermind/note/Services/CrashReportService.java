package com.archermind.note.Services;

import com.archermind.note.NoteApplication;
import com.archermind.note.Task.SendCrashReportsTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CrashReportService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		System.out.println("=CCC= CrashReportService onCreate");
		NoteApplication.LogD(CrashReportService.class, "CrashReportService onCreate");
		SendCrashReportsTask task = new SendCrashReportsTask(this);
		task.execute();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		NoteApplication.LogD(CrashReportService.class, "CrashReportService onDestroy");
	}
	
	

}


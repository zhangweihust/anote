package com.archermind.note.Services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.message.BasicNameValuePair;

import com.archermind.note.NoteApplication;
import com.archermind.note.Task.SendCrashReportsTask;
import com.archermind.note.Utils.DateTimeUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.DisplayMetrics;


public class ExceptionService implements IService {
    /** 错误报告文件的扩展名 */  
	private static final String CRASH_REPORTER_EXTENSION = ".log";
	
	@Override
	public boolean start() {
		//AMTException.getInstance().init();
		return true;
	}

	@Override
	public boolean stop() {
		return true;
	}

	
	/**
	 * 收集设备参数信息
	 * @param ctx
	 */
	public static Map<String, String> collectDeviceInfo(Context ctx) {
		try {
			Map<String, String> infos = new HashMap<String, String>();
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null" : pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}
			String versionSDK = Integer.valueOf(android.os.Build.VERSION.SDK).toString();
			String phoneModel = android.os.Build.MODEL;
			infos.put("versionSDK", versionSDK);
			infos.put("phoneModel", phoneModel);
		 
			DisplayMetrics dm=new DisplayMetrics();
			if(NoteApplication.getInstance().getWindowManager() != null){	
				NoteApplication.getInstance().getWindowManager().getDefaultDisplay()
					.getMetrics(dm);
				float width = dm.widthPixels;
				float height = dm.heightPixels;
				float density = dm.densityDpi;
				infos.put("width", Float.toString(width));
				infos.put("height", Float.toString(height));
				infos.put("density", Float.toString(density));
			}	
			return infos;
		} catch (NameNotFoundException e) {
			
		}
		
		return null;
	}
	

	public static StringBuffer saveCrashInfo(Throwable ex, Map<String, String> infos){
		try {
			StringBuffer sb = new StringBuffer();
          if(infos != null){
			for (Map.Entry<String, String> entry : infos.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				sb.append(key + "=" + value + "\n");
			}
			}
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			ex.printStackTrace(printWriter);
			Throwable cause = ex.getCause();
			while (cause != null) {
				cause.printStackTrace(printWriter);
				cause = cause.getCause();
			}
			printWriter.close();
			String result = writer.toString();
			sb.append(result);
			return sb;
			} catch (Exception e) {
				// TODO: handle exception
			}
		return null;
	}
	
	public static StringBuffer saveCrashInfo(Exception e, Map<String, String> infos){
		try {		
			StringBuffer sb = new StringBuffer();
			if(infos != null){
				for (Map.Entry<String, String> entry : infos.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					sb.append(key + "=" + value + "\n");
				}
			}
			if (e == null) {
				sb.append("Exception:"
						+ "e is null,probably null pointer exception" + "\n");
			} else {
				sb.append(e.getClass().getCanonicalName() + ":"
						+ e.getMessage() + "\n");
				StackTraceElement[] stes = e.getStackTrace();
				for (StackTraceElement ste : stes) {
					sb.append("at " + ste.getClassName() + "$"
							+ ste.getMethodName() + "$" + ste.getFileName()
							+ ":" + ste.getLineNumber() + "\n");
				}
			}
			return sb;
		} catch (Exception e2) {
			// TODO: handle exception
		}
		return null;
	}
	
	
	
	public static void saveCrashInfo2File(StringBuffer sb) {
		if(sb == null || sb.equals("")){
			return ;
		}
		try {
			long timestamp = System.currentTimeMillis();
			String time = DateTimeUtils.time2String("yyyy-MM-dd-HH-mm-ss", System.currentTimeMillis());
			String fileName = "crash-" + time + "-" + timestamp + CRASH_REPORTER_EXTENSION;
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				String path = NoteApplication.crashPath;
				File dir = new File(path);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(path + fileName);
				fos.write(sb.toString().getBytes());
				fos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void logException(Exception e) {
		Map<String, String> infos = ExceptionService.collectDeviceInfo(NoteApplication.getContext());
		StringBuffer sb = ExceptionService.saveCrashInfo(e, infos);
		ExceptionService.saveCrashInfo2File(sb);
		System.out.println("======= logException===========");
		SendCrashReportsTask task = new SendCrashReportsTask();
		task.execute();
	}
	
	private static class AMTException implements UncaughtExceptionHandler {

		private static AMTException instance;

		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			Map<String, String> infos = ExceptionService.collectDeviceInfo(NoteApplication.getContext());
			StringBuffer sb = ExceptionService.saveCrashInfo(ex, infos);
			ExceptionService.saveCrashInfo2File(sb);
			ServiceManager.exit();
		}
		
		public void init() {
			Thread.setDefaultUncaughtExceptionHandler(this);
		}

		public static AMTException getInstance() {
			if (instance == null) {
				instance = new AMTException();
			}
			return instance;
		}
		
	}
}

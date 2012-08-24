package com.archermind.note.download;

import java.util.ArrayList;

import com.archermind.note.Views.AlbumScrollLayout.OnScreenChangeListenerDataLoad;
import com.archermind.note.download.DownloadJob.OnDownloadStateChangeListener;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class DownloadTaskManager {
	private ArrayList<String> downloadTaskPathList;
	private ArrayList<DownloadJob> downloadTaskList;
	private Context mContext;
	
	private static DownloadTaskManager instance;

	public static boolean networkIsOk = false;
	
	public static DownloadTaskManager getInstance() {
		if (instance == null) {
			instance = new DownloadTaskManager();
		}
		return instance;
	}
	
	public void setContext(Context context) {
		mContext = context;
	}
	
	public Context getContext() {
		return mContext;
	}
	
	public void sendDataSetChangedEvent() {
        final ArrayList<OnDataSetChangeListener> listeners = onDataSetChangeListeners;
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
        	listeners.get(i).OnDataSetChange(this);
        }
	}
	
	public void addNewTask(final DownloadJob job) {
		new Thread(new Runnable() {
			public void run() {
				Looper.prepare();
				DownloadTask mDownloadTask = new DownloadTask(job);
				job.setDownloadTask(mDownloadTask);
				mDownloadTask.execute(job.getDownloadType());
				Looper.loop();
			}
		}).start();
	}
	
	public ArrayList<DownloadJob> getDownloadTaskList() {
		return downloadTaskList;
	}
	
	public void setDownloadTaskList(ArrayList<DownloadJob> list) {
		this.downloadTaskList = list;
	}
	
	public boolean containsDownloadPath(String DownloadPath) {
		if (DownloadPath == null || "".equals(DownloadPath)) {
			return false;
		}
		
		int count = this.downloadTaskList.size();
        for (int i = 0; i < count; i++) {
        	if (DownloadPath.equals(this.downloadTaskList.get(i).getPath())) {
        		return true;
        	}
        }
        
		return false;
	}
	
	public boolean isOnline() {
		if (mContext == null) {
			DownloadTaskManager.LogD(DownloadTaskManager.class, "isOnline error, DownloadTaskManager getContext return null!");
			return false;
		}
		
		try {
			ConnectivityManager cm = (ConnectivityManager) DownloadTaskManager.getInstance().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			networkIsOk = (ni != null ? ni.isConnectedOrConnecting() : false);
			return networkIsOk;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private DownloadTaskManager() {
		downloadTaskPathList = new ArrayList<String>();
		downloadTaskList = new ArrayList<DownloadJob>();
		onDataSetChangeListeners = new ArrayList<OnDataSetChangeListener>();
	}
	
	public void addOnDataSetChangeListener(OnDataSetChangeListener listener) {
		this.onDataSetChangeListeners.add(listener);
	}

	public void removeOnDataSetChangeListener(OnDataSetChangeListener listener) {
		this.onDataSetChangeListeners.remove(listener);
	}
	
	public interface OnDataSetChangeListener {
		void OnDataSetChange(DownloadTaskManager manager);
	}
	private ArrayList<OnDataSetChangeListener> onDataSetChangeListeners;
	
	
	public static void LogD(Class classz, String msg) {
		if (logger != null) {
			logger.LogD(classz, msg);
		}
	}
	public static void setLogger(DownloadLogger aLogger) {
		logger = aLogger;
	}
	
	public interface DownloadLogger {
		void LogD(Class classz, String msg);
	}
	private static DownloadLogger logger;
	
	
}

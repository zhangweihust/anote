package com.archermind.note.download;

import java.util.ArrayList;

import android.os.Handler;

import com.archermind.note.download.DownloadTaskManager.OnDataSetChangeListener;
import com.archermind.note.gesture.AmGestureOverlayView.OnAmGesturingListener;


public class DownloadJob {
	private boolean rebegin;
	private String path;
	private String finalPath;
	private Integer downloadId;
	private long downloadStartPos;
	private int downloadType;
	private int downloadResource;
	private String downloadUrl;
	private long downloadCurrentSize;
	private long downloadTotalSize;
	private int downloadStateFlag;
	private String downloadStateInfo;
	private DownloadTask downloadTask;
	private int versionCode;
	private String cookie;    //5sing的cookie，登录下载用

	public DownloadJob() {
		onDownloadStateChangeListeners = new ArrayList<OnDownloadStateChangeListener>();
		onDownloadProgressChangeListeners = new ArrayList<OnDownloadProgressChangeListener>();
		downloadStateInfo = "";
	}
	
	public String getCookie() {
		return cookie;
	}

	public void setCookie(String szcookie) {
		this.cookie = szcookie;
	}


	public String getFinalPath() {
		return finalPath;
	}

	public void setFinalPath(String finalPath) {
		this.finalPath = finalPath;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public DownloadTask getDownloadTask() {
		return downloadTask;
	}

	public void setDownloadTask(DownloadTask downloadTask) {
		this.downloadTask = downloadTask;
	}

	public int getDownloadStateFlag() {
		return downloadStateFlag;
	}

	public void setDownloadStateFlag(int downloadStateFlag) {
		int oldStateFlag = this.downloadStateFlag;
		this.downloadStateFlag = downloadStateFlag;
		
		if (this.downloadStateFlag == oldStateFlag)
			return;
		
        final ArrayList<OnDownloadStateChangeListener> listeners = onDownloadStateChangeListeners;
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
        	listeners.get(i).OnDownloadStateChange(this, oldStateFlag, this.downloadStateFlag);
        }
	}
	
	public String getDownloadStateInfo() {
		return this.downloadStateInfo;
	}
	
	public void setDownloadStateInfo(String errStr) {
		this.downloadStateInfo = errStr;
	}

	public long getDownloadStartPos() {
		return downloadStartPos;
	}

	public void setDownloadStartPos(long downloadStartPos) {
		if (downloadCurrentSize == 0) downloadCurrentSize = downloadStartPos;
		this.downloadStartPos = downloadStartPos;
	}

	public int getDownloadType() {
		return downloadType;
	}

	public void setDownloadType(int downloadType) {
		this.downloadType = downloadType;
	}

	public int getDownloadResource() {
		return downloadResource;
	}

	public void setDownloadResource(int downloadResource) {
		this.downloadResource = downloadResource;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	
	public long getDownloadCurrentSize() {
		return downloadCurrentSize;
	}

	public void setDownloadCurrentSize(long downloadCurrentSize) {
		this.downloadCurrentSize = downloadCurrentSize;
		
        final ArrayList<OnDownloadProgressChangeListener> listeners = onDownloadProgressChangeListeners;
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
        	listeners.get(i).OnDownloadProgressChange(this, downloadCurrentSize, this.downloadTotalSize);
        }
	}

	public long getDownloadTotalSize() {
		return downloadTotalSize;
	}

	public void setDownloadTotalSize(long downloadTotalSize) {
		this.downloadTotalSize = downloadTotalSize;
	}

	public boolean isRebegin() {
		return rebegin;
	}

	public void setRebegin(boolean rebegin) {
		this.rebegin = rebegin;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Integer getDownloadId() {
		return downloadId;
	}

	public void setDownloadId(Integer downloadId) {
		this.downloadId = downloadId;
	}
	
	public void addOnDownloadProgressChangeListener(OnDownloadProgressChangeListener listener) {
		this.onDownloadProgressChangeListeners.add(listener);
	}

	public void removeOnDownloadProgressChangeListener(OnDownloadProgressChangeListener listener) {
		this.onDownloadProgressChangeListeners.remove(listener);
	}

	public interface OnDownloadProgressChangeListener {
		void OnDownloadProgressChange(DownloadJob job, long currentBytes, long totalBytes);
	}
	private ArrayList<OnDownloadProgressChangeListener> onDownloadProgressChangeListeners;
	
	
	public void addOnDownloadStateChangeListener(OnDownloadStateChangeListener listener) {
		this.onDownloadStateChangeListeners.add(listener);
	}

	public void removeOnDownloadStateChangeListener(OnDownloadStateChangeListener listener) {
		this.onDownloadStateChangeListeners.remove(listener);
	}

	public interface OnDownloadStateChangeListener {
		void OnDownloadStateChange(DownloadJob job, int oldState, int newState);
	}
	private ArrayList<OnDownloadStateChangeListener> onDownloadStateChangeListeners;
}

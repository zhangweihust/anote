package com.archermind.note.download;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.widget.Toast;

public class DownloadTask extends AsyncTask<Integer, Integer, Boolean> {

	DownloadJob mJob;
	private int max_asc_map = 200; // 对应asc码表,总共分配了200，确保足够
	private int startnum = 0; // 0-9数字表
	private int endnum = 9; // 9的位置
	private int num_asc_map = 48; // 数字0的asc码是48，数字asc码表的起始值
	private int startA_Z = 10; // 0-9 10个数字排完，接着是大写的26个字幕；起始位置是0+10=10；
	private int endA_Z = 35; // Z的位置为10+26-1；
	private int A_Z_asc_map = 55; // A的asc码是65，但是算法中获取A的asc码需要加上startA_Z的值，所以起始值为65-10=55;
	private int starta_z = 36; // 接着是小写的a-z；起始位置是10+26=36；
	private int enda_z = 61; // Z的位置为10+26+26-1；
	private int a_z_asc_map = 61; // a的asc码是97，但是算法中获取a的asc码需要加上starta_z的值，所以起始值为97-36=61;
	private int[] asc_arr1 = new int[max_asc_map];
	private int[] asc_arr2 = new int[max_asc_map];
	private long interval = 2 * 1000;
	private int retryTimes = 20;
	private long totalSize = 0;
	
	public static final int MESSAGE_WHAT_DOWNLOADING = 1001;
	public static final int MESSAGE_WHAT_DOWNLOADED = 1002;
	public static final int MESSAGE_WHAT_DOWNLOADED_ERROR = 1003;
	
	public static final int DOWNLOAD_WITH_ACCOMPANY = 601;
	public static final int DOWNLOAD_NOT_WITH_ACCOMPANY = 602;
	public static final int DOWNLOAD_START_ON_ZERO = 701;
	public static final int DOWNLOAD_START_ON_RANGE = 702;
	public static final int DOWNLOAD_START_ON_WEB = 703;
	public static final int DOWNLOAD_START_ON_APK = 704;
	public static final int DOWNLOAD_START_ON_IMAGE = 705;
	
	public static final int STATE_IDEL = 0;
	public static final int STATE_WAIT = 101;
	public static final int STATE_BEGIN = 102;
	public static final int STATE_PAUSE = 103;
	public static final int STATE_CANCEL = 104;
	public static final int STATE_FINISHED = 105;
	public static final int STATE_IN_QUEUE = 106;
	public static final int STATE_ERROR = 107;
	
	public static final String PACKAGE_SUFFIX = ".apk";
	public static final String ACCOMPANY_SUFFIX = ".bz";
	public static final String TMP_SUFFIX = ".tmp";
	public static final String PICTURE_SUFFIX = ".tp";
	
	/**
	 * The minimum amount of progress that has to be done before the progress
	 * bar gets updated
	 */
	public static final int MIN_PROGRESS_STEP = 1024 * 4;

	/**
	 * The minimum amount of time that has to elapse before the progress bar
	 * gets updated, in ms
	 */
	public static final long MIN_PROGRESS_TIME = 2000;

	public final static int ERROR_NONE = 0;
	public final static int ERROR_SD_NO_MEMORY = 1;
	public final static int ERROR_BLOCK_INTERNET = 2;
	public final static int ERROR_BLOCK_TEMPFILE_DELETE = 3;
	public final static int ERROR_BLOCK_NEED_TO_CHANGE_DOWNLOAD_RESOURCE = 4;
	public final static int ERROR_UNKONW = 5;
	public final static int TIME_OUT = 60000;
	private final static int BUFFER_SIZE = 1024 * 4;
	private final static int DOWNLOAD_VISIABLE = 1;
	private final static int DOWNLOAD_INVISIABLE = 2;
	private final static int DOWNLOAD_GETURL_ORIGINAL = 1;
	private final static int DOWNLOAD_GETURL_ACCOMPANY = 2;
	private int errStausCode = ERROR_NONE;
	private boolean interrupt = false;
	private RandomAccessFile outputStream;
	private long networkSpeed; // 网速
	private long previousTime;
	private long totalTime;
	private long downloadSize;
	private Throwable exception;
	HttpGet httpRequest;
	private int downloadType;
	private int downloadVisable = DOWNLOAD_INVISIABLE;

	//SharedPreferences preferences;
	private Bitmap image;
	//private OnScreenHint mOnScreenHint;
	public DownloadTask(DownloadJob job) {
		mJob = job;
	}

	private final class ProgressReportingRandomAccessFile extends RandomAccessFile {
		private int progress = 0;

		public ProgressReportingRandomAccessFile(File file, String mode) throws FileNotFoundException {
			super(file, mode);
		}

		@Override
		public void write(byte[] buffer, int offset, int count) throws IOException {
			super.write(buffer, offset, count);
			progress += count;
			publishProgress(progress);
		}
	}

	@Override
	public void onPreExecute() {
		DownloadTaskManager.LogD(DownloadTask.class,"开启一条下载线程1:" + mJob.getPath() + " size:" + mJob.getDownloadStartPos());
		previousTime = System.currentTimeMillis();
		DownloadTaskManager.getInstance().getDownloadTaskList().add(mJob);
		DownloadTaskManager.getInstance().sendDataSetChangedEvent();
		super.onPreExecute();
	}

	@Override
	public Boolean doInBackground(Integer... params) {
		downloadType = params[0];
		try {
			switch(params[0]){
			case DOWNLOAD_START_ON_RANGE:
				downloadVisable = DOWNLOAD_VISIABLE;
			case DOWNLOAD_START_ON_APK:
			case DOWNLOAD_START_ON_IMAGE:
				break;
			default:
				DownloadTaskManager.LogD(DownloadTask.class,"没理由进来");
				return false;
			}
			download(mJob);
			return true;
		} catch (SocketException e) {
			interrupt = true;
			exception = e;
			errStausCode = ERROR_UNKONW;
			DownloadTaskManager.LogD(DownloadTask.class,"SocketException:" + e);
			return false;
		} catch (Exception e) {
			interrupt = true;
			exception = e;
			errStausCode = ERROR_UNKONW;
			DownloadTaskManager.LogD(DownloadTask.class, "ERROR_UNKONW-->input = catch (Exception e) :" + e);
			return false;
		} 

	}

	@Override
	public void onPostExecute(Boolean result) {
		if(httpRequest != null)
			httpRequest.abort();
		DownloadTaskManager.LogD(DownloadTask.class, "onPostExecute:" + mJob.getPath());
		//DownloadTaskManager.getInstance().getDownloadTaskList().remove(mJob);

		if (interrupt) {
			if (errStausCode != ERROR_NONE) {
				switch(errStausCode){
				case ERROR_BLOCK_NEED_TO_CHANGE_DOWNLOAD_RESOURCE:{
					String errStr = mapErrorCode(errStausCode);
					DownloadTaskManager.LogD(DownloadTask.class, errStr);
					mJob.setDownloadStateFlag(STATE_ERROR);
					mJob.setDownloadStateInfo(errStr);
					}
					break;
					
				case ERROR_BLOCK_INTERNET:
				case ERROR_SD_NO_MEMORY:
				case ERROR_BLOCK_TEMPFILE_DELETE:
				case ERROR_UNKONW:
				default: {
					String errStr = mapErrorCode(errStausCode);
					DownloadTaskManager.LogD(DownloadTask.class, errStr);
					mJob.setDownloadStateFlag(STATE_PAUSE);
					mJob.setDownloadStateInfo(errStr);
				}
					break;
				}
			}
			return;
		}
		if (exception != null) {
			String errStr = "Download failed. " + exception;
			DownloadTaskManager.LogD(DownloadTask.class, errStr);
			mJob.setDownloadStateFlag(STATE_PAUSE);
			mJob.setDownloadStateInfo("Download failed. catch exception");
			return;
		}
	}

	@Override
	public void onCancelled() {
		super.onCancelled();
		interrupt = true;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		totalTime = System.currentTimeMillis() - previousTime;
		downloadSize = progress[0];
		networkSpeed = downloadSize / totalTime;

	}
	
	
	/** 
     *  异常自动恢复处理 
     *  使用HttpRequestRetryHandler接口实现请求的异常恢复 
     */  
    private static HttpRequestRetryHandler requestRetryHandler = new HttpRequestRetryHandler() {  
        // 自定义的恢复策略  
        public synchronized boolean retryRequest(IOException exception, int executionCount, HttpContext context) {  
            // 设置恢复策略，在发生异常时候将自动重试3次  
        	DownloadTaskManager.LogD(DownloadTask.class, "在发生异常时候将自动重试3次:" + executionCount);
            if (executionCount > 3) {    
                // 超过最大次数则不需要重试    
            	DownloadTaskManager.LogD(DownloadTask.class, "超过最大次数则不需要重试");
                return false;    
            }    
            if (exception instanceof NoHttpResponseException) {    
                // 服务停掉则重新尝试连接    
                return true;    
            }    
            if (exception instanceof SSLHandshakeException) {    
                // SSL异常不需要重试    
                return false;    
            }   
            HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);  
            boolean idempotent = (request instanceof HttpEntityEnclosingRequest);  
            if (!idempotent) {  
                // 请求内容相同则重试  
                return true;  
            }  
            return false;  
        }  
    };  

	private long download(final DownloadJob mJob) throws Exception {
		DownloadTaskManager.LogD(DownloadTask.class,"开启一条下载线程2:" + mJob.getPath() + " size:" + mJob.getDownloadStartPos());
		if (!DownloadTaskManager.getInstance().isOnline()) {
			errStausCode = ERROR_BLOCK_INTERNET;
			interrupt = true;
			DownloadTaskManager.networkIsOk = false;
			return 0l;
		} else {
			DownloadTaskManager.networkIsOk = true;
		}
		if (mJob.getDownloadUrl() == null || "".equals(mJob.getDownloadUrl()) ){
			errStausCode = ERROR_BLOCK_NEED_TO_CHANGE_DOWNLOAD_RESOURCE;
			interrupt = true;
			return 0l;
		}
		String url = mJob.getDownloadUrl();
		url = url.replace("\r", ""); 
		url = url.replace("\n", "");
		mJob.setDownloadUrl(url);
		DownloadTaskManager.LogD(DownloadTask.class, mJob.getPath() + ": " + mJob.getDownloadUrl());
		httpRequest = new HttpGet(mJob.getDownloadUrl());
		HttpParams httpParameters = new BasicHttpParams();
		// 请求超时
		int timeoutConnection = 30000;
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT)
		// 接收返回数据超时
		int timeoutSocket = 30000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
//		DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(3, true);
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);
		client.setHttpRequestRetryHandler(requestRetryHandler);
		HttpResponse httpResponse = client.execute(httpRequest);
		DownloadTaskManager.LogD(DownloadTask.class, "getStatusCode():"
				+ httpResponse.getStatusLine().getStatusCode());
		if (httpResponse.getStatusLine().getStatusCode() >= 400) {
			errStausCode = ERROR_BLOCK_NEED_TO_CHANGE_DOWNLOAD_RESOURCE;
			interrupt = true;
			return 0l;
		}
		HttpEntity httpEntity = httpResponse.getEntity();
        int length = ( int ) httpEntity.getContentLength();
        DownloadTaskManager.LogD(DownloadTask.class, mJob.getPath() + ": size:" + length);

		totalSize = length;
		DownloadTaskManager.LogD(DownloadTask.class, "totalSize :" + totalSize + "##currentSize : " + mJob.getDownloadStartPos());
		if (totalSize <= 0) {
			errStausCode = ERROR_BLOCK_NEED_TO_CHANGE_DOWNLOAD_RESOURCE;
			interrupt = true;
			return 0l;
		}
		mJob.setDownloadTotalSize(totalSize);
		mJob.setDownloadStateFlag(STATE_BEGIN);

		File file = new File(mJob.getPath());
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		} else {
			mJob.setDownloadStartPos(file.length());//有一种情况就是程序装之前本地aMusic文件下面有没有下载完的临时文件。
		}
		if (mJob.getDownloadStartPos() > 0 && totalSize > 0 && totalSize > mJob.getDownloadStartPos()) {
			String sProperty = "bytes=" + mJob.getDownloadStartPos() + "-";
			if(httpRequest != null)
				httpRequest.abort();
			httpRequest = new HttpGet(mJob.getDownloadUrl());   
			httpRequest.addHeader("Range", sProperty);
			httpResponse = client.execute(httpRequest);  
			DownloadTaskManager.LogD(DownloadTask.class,"getStatusCode():" + httpResponse.getStatusLine().getStatusCode());
			if(httpResponse.getStatusLine().getStatusCode() >= 400){
				errStausCode = ERROR_BLOCK_NEED_TO_CHANGE_DOWNLOAD_RESOURCE;
				interrupt = true;
				return 0l;
			}
			httpEntity = httpResponse.getEntity();
			
			int Total_Size = ( int ) httpEntity.getContentLength();
			if (Total_Size == totalSize) {//特殊处理，当不支持断点续传的时候
				if (file.exists()){
					file.delete();
				}
			}
			if (Total_Size <= 0) {
				DownloadTaskManager.LogD(DownloadTask.class, "ERROR_UNKONW-->Total_Size <= 0 :");
				errStausCode = ERROR_UNKONW;
				interrupt = true;
				return 0l;
			}
			totalSize = Total_Size;
			DownloadTaskManager.LogD(DownloadTask.class,"File is not complete, download now.");
			DownloadTaskManager.LogD(DownloadTask.class,"剩下的文件长度:" + httpEntity.getContentLength() + " totalSize:" + totalSize);
		} else if (totalSize == mJob.getDownloadStartPos()) {
			DownloadTaskManager.LogD(DownloadTask.class, "Output file already exists. Skipping download..");
			mJob.setDownloadStateFlag(STATE_FINISHED);
			return 0l;
		}

		long storage = getAvailableStorage();
		DownloadTaskManager.LogD(DownloadTask.class, "storage:" + storage + " totalSize:" + totalSize);
		if (mJob.getDownloadTotalSize() - mJob.getDownloadStartPos() > storage) {
			errStausCode = ERROR_SD_NO_MEMORY;
			interrupt = true;
			return 0l;
		}
		try {
			outputStream = new ProgressReportingRandomAccessFile(file, "rw");
		} catch (FileNotFoundException e) {
			errStausCode = ERROR_BLOCK_TEMPFILE_DELETE;
			interrupt = true;
			return 0l;
		}

		publishProgress(0, (int) totalSize);

		InputStream input = null;
		try {
			input = httpEntity.getContent();
		} catch (IOException ex) {
			if(httpRequest != null)
			publishProgress(0);
			DownloadTaskManager.LogD(DownloadTask.class, "ERROR_UNKONW-->input = httpEntity.getContent() :" + ex);
			errStausCode = ERROR_UNKONW;
			interrupt = true;
			return 0l;
		}

		int bytesCopied = copy(input, outputStream, file ,httpRequest);
		long CurrentSize = bytesCopied + mJob.getDownloadStartPos();
		mJob.setDownloadCurrentSize(CurrentSize);
		publishProgress(0);
		DownloadTaskManager.LogD(DownloadTask.class, " bytesCopied :" + bytesCopied + " totalSize :" + totalSize );
 
		if (CurrentSize == totalSize){
			DownloadTaskManager.LogD(DownloadTask.class, "Download completed successfully.");
			mJob.setDownloadCurrentSize(mJob.getDownloadTotalSize());
			mJob.setDownloadStateFlag(STATE_FINISHED);
		} else {
			DownloadTaskManager.LogD(DownloadTask.class, "ERROR_UNKONW-->bytesCopied == totalSize :");
			errStausCode = ERROR_UNKONW;
			interrupt = true;
		}
		return bytesCopied;
	}

	public int copy(InputStream input, RandomAccessFile out, File file, HttpGet httpRequest) throws Exception, IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		DownloadTaskManager.LogD(DownloadTask.class, "开始下载:" + file.getName());
		BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
		DownloadTaskManager.LogD(DownloadTask.class, "--------------------tmpfile-length: " + out.length());
		out.seek(out.length());
		int bytesSoFar = 0;
		int bytesNotified = bytesSoFar;
		long timeLastNotification = 0;
		long errorBlockTimePreviousTime = -1, expireTime = 0;
		try {
			while (!interrupt) {
				int bytesRead = 0;
				
				if (!file.exists()) {
					errStausCode = ERROR_BLOCK_TEMPFILE_DELETE;
					interrupt = true;
					break;
				}
				if(!DownloadTaskManager.networkIsOk) {
					DownloadTaskManager.LogD(DownloadTask.class, "SEND : !isOnline()");
					interrupt = true;
					errStausCode = ERROR_BLOCK_INTERNET;
					break;
				} 
				bytesRead = in.read(buffer, 0, BUFFER_SIZE);
				if (bytesRead == -1) {
					DownloadTaskManager.LogD(DownloadTask.class, "SEND : AUDIO_DOWNLOAD_DATA_OVER");
					break;
				}
				out.write(buffer, 0, bytesRead);
				bytesSoFar += bytesRead;

				long now = System.currentTimeMillis();

				if (bytesSoFar - bytesNotified > MIN_PROGRESS_STEP && now - timeLastNotification > MIN_PROGRESS_TIME) {
					bytesNotified = bytesSoFar;
					timeLastNotification = now;
					mJob.setDownloadCurrentSize(bytesSoFar + mJob.getDownloadStartPos());
				}


				if (bytesRead == 0) {
					mJob.setDownloadCurrentSize(bytesSoFar + mJob.getDownloadStartPos());
					DownloadTaskManager.LogD(DownloadTask.class, "SEND : bytesRead == 0");
					if (errorBlockTimePreviousTime > 0) {
						expireTime = System.currentTimeMillis() - errorBlockTimePreviousTime;
						if (expireTime > TIME_OUT) {
							DownloadTaskManager.LogD(DownloadTask.class, "expireTime > TIME_OUT");
							errStausCode = ERROR_UNKONW;
							interrupt = true;
							break;
						}
					} else {
						errorBlockTimePreviousTime = System.currentTimeMillis();
					}
				} else {
					expireTime = 0;
					errorBlockTimePreviousTime = -1;
				}
			}
			DownloadTaskManager.LogD(DownloadTask.class, "SEND : !isOUT");
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				DownloadTaskManager.LogD(DownloadTask.class, "ERROR_UNKONW-->out.close(); :");
				errStausCode = ERROR_UNKONW;
			}
			try {
				in.close();
			} catch (IOException e) {
				DownloadTaskManager.LogD(DownloadTask.class, "ERROR_UNKONW-->in.close(); :");
				errStausCode = ERROR_UNKONW;
			}
		}
		DownloadTaskManager.LogD(DownloadTask.class, "SEND : !isOUT:" +bytesSoFar);
		return bytesSoFar;
	}

	/*
	 * 获取 SD 卡内存
	 */
	public static long getAvailableStorage() {
		String storageDirectory = null;
		storageDirectory = Environment.getExternalStorageDirectory().toString();

	//	Log.v(null, "getAvailableStorage. storageDirectory : " + storageDirectory);

		try {
			StatFs stat = new StatFs(storageDirectory);
			long avaliableSize = ((long) stat.getAvailableBlocks() * (long) stat.getBlockSize());
		//	Log.v(null, "getAvailableStorage. avaliableSize : " + avaliableSize);
			return avaliableSize;
		} catch (RuntimeException ex) {
		//	Log.e(null, "getAvailableStorage - exception. return 0");
			return 0;
		}
	}
	
	private long getFileSize(String sURL) {
		long nEndPos = 0;
		try {
			URL url = new URL(sURL);
			int times = 0;
			// connect
			while (nEndPos <= 0 && times < retryTimes) {
				HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
				// getfilesize
				nEndPos = getSize(sURL);
				httpConnection.disconnect();
				times++;
				Thread.sleep(interval);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return nEndPos;
	}

	// get file size
	private long getSize(String sURL) {
		int nFileLength = -1;
		try {
			URL url = new URL(sURL);
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
			int responseCode = httpConnection.getResponseCode();
			if (responseCode >= 400) {
				System.err.println("Error Code : " + responseCode);
				return -2; // -2 represent access is error
			}
			String sHeader;
			for (int i = 1;; i++) {
				sHeader = httpConnection.getHeaderFieldKey(i);
				if (sHeader != null) {
					if (sHeader.toLowerCase().equals("content-length")) {
						nFileLength = Integer.parseInt(httpConnection.getHeaderField(sHeader));
						break;
					}
				} else
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println(nFileLength);
		return nFileLength;
	}


	private void init(int head, int bottom, int middle) {
		for (int i = head; i <= bottom; i++) {
			asc_arr1[i] = i + middle;
			asc_arr2[i + middle] = i;
		}

	}

	private String decode(String url, int sertim) {
		long len = url.length();
		String decurl = "";
		int key = 0;
		key = sertim % 26;
		if (key < 0) {
			key = 1;
		}

		init(startnum, endnum, num_asc_map);
		init(startA_Z, endA_Z, A_Z_asc_map);
		init(starta_z, enda_z, a_z_asc_map);
		for (int i = 0; i < len; i++) {
			char word = url.charAt(i);
			int wordasc = (int) word;
			if (((47 < wordasc) && (wordasc < 58)) || ((64 < wordasc) && (wordasc < 91)) || ((96 < wordasc) && (wordasc < 123))) {
				int pos = asc_arr2[wordasc] - key;
				if (pos < 0)
					pos += 62;
				word = (char) (asc_arr1[pos]);
			}
			decurl += word;
		}
		return decurl;

	}
	
	public static String postauth(String usernameAndPassword)
			throws IOException {
		/**
		 * 首先要和URL下的URLConnection对话。 URLConnection可以很容易的从URL得到。比如： // Using
		 * java.net.URL and //java.net.URLConnection
		 * 
		 * 使用页面发送请求的正常流程：在页面http://www.faircanton.com/message/loginlytebox.
		 * asp中输入用户名和密码，然后按登录，
		 * 跳转到页面http://www.faircanton.com/message/check.asp进行验证 验证的的结果返回到另一个页面
		 * 
		 * 使用java程序发送请求的流程：使用URLConnection向http://www.faircanton.com/message/
		 * check.asp发送请求 并传递两个参数：用户名和密码 然后用程序获取验证结果
		 */
		URL url = new URL("http://www.5sing.com/Popup/Login.aspx");
		URLConnection connection = url.openConnection();
		/**
		 * 然后把连接设为输出模式。URLConnection通常作为输入来使用，比如下载一个Web页。
		 * 通过把URLConnection设为输出，你可以把数据向你个Web页传送。下面是如何做：
		 */
		connection.setDoOutput(true);
		/**
		 * 最后，为了得到OutputStream，简单起见，把它约束在Writer并且放入POST信息中，例如： ...
		 */
		OutputStreamWriter out = new OutputStreamWriter(
				connection.getOutputStream(), "8859_1");
		out.write("__EVENTTARGET=&__EVENTARGUMENT=&__VIEWSTATE=%2FwEPDwUJNTMwMTc4MjMyZBgBBR5fX0NvbnRyb2xzUmVxdWlyZVBvc3RCYWNrS2V5X18WAQUGSXNTYXZleQeRUebp1KVeqi6teBH7W%2FJeOgc%3D&__EVENTVALIDATION=%2FwEWBQKX%2Fr%2BRDwKl1bKzCQK1qbSRCwKW8v7gAwLT%2Fr7ABJJKl0ZYaqfwREntUdzqb19cMv%2BC&"
				+ usernameAndPassword + "&Button=%E7%99%BB+%E5%BD%95"); // 向页面传递数据。post的关键所在！
		// remember to clean up
		out.flush();
		out.close();
		/**
		 * 这样就可以发送一个看起来象这样的POST： POST /jobsearch/jobsearch.cgi HTTP 1.0 ACCEPT:
		 * text/plain Content-type: application/x-www-form-urlencoded
		 * Content-length: 99 username=bob password=someword
		 */
		// 一旦发送成功，用以下方法就可以得到服务器的回应：
		String sCurrentLine;
		String sTotalString;
		sCurrentLine = "";
		sTotalString = "";
		InputStream l_urlStream;
		l_urlStream = connection.getInputStream();
		// 传说中的三层包装阿！
		BufferedReader l_reader = new BufferedReader(new InputStreamReader(
				l_urlStream));
		while ((sCurrentLine = l_reader.readLine()) != null) {
			sTotalString += sCurrentLine + "/r/n";

		}
		String setCookie = connection.getHeaderField("Set-Cookie");
		String cookie = setCookie.substring(0, setCookie.indexOf(";"));
		// System.out.println(cookie);
		// System.out.println(sTotalString);
		return cookie;
	}

	public long getNetworkSpeed() {
		return networkSpeed;
	}
	
	private String mapErrorCode(int aErrorCode) {
		String errStr;
		switch (errStausCode) {
		case ERROR_BLOCK_INTERNET:
			errStr = "download error: ERROR_BLOCK_INTERNET";
			break;
		case ERROR_SD_NO_MEMORY:
			errStr = "download error: ERROR_SD_NO_MEMORY";
			break;
		case ERROR_BLOCK_TEMPFILE_DELETE:
			errStr = "download error: ERROR_BLOCK_TEMPFILE_DELETE";
			break;
		case ERROR_BLOCK_NEED_TO_CHANGE_DOWNLOAD_RESOURCE:
			errStr = "download error: ERROR_BLOCK_NEED_TO_CHANGE_DOWNLOAD_RESOURCE";
			break;
		case ERROR_UNKONW:
		default:
			errStr = "download error: ERROR_UNKONW";
			break;
		}

		return errStr;
	}
}

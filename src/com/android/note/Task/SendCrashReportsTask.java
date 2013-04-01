package com.android.note.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.android.note.NoteApplication;
import com.android.note.Services.ServiceManager;
import com.android.note.Utils.ServerInterface;
import com.android.note.Utils.VersionUtil;

import android.app.Service;
import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

public class SendCrashReportsTask extends AsyncTask<Void, Void, Integer> {
	/** 错误报告文件的扩展名 */  
	private static final String CRASH_REPORTER_EXTENSION = ".log";

	public SendCrashReportsTask(){
		
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		sendCrashReportsToServer();
		return null;
	}
	
	/**
	 * 把错误报告发送给服务器,包含新产生的和以前没发送的.
	 * 
	 * @param ctx
	 */
	private void sendCrashReportsToServer() {
		String[] crFiles = getCrashReportFiles();
		if (crFiles != null && crFiles.length > 0) {
			TreeSet<String> sortedFiles = new TreeSet<String>();
			sortedFiles.addAll(Arrays.asList(crFiles));

			for (String fileName : sortedFiles) {
				File cr = new File(NoteApplication.crashPath, fileName);
				if(postReport(cr)){
					cr.delete();// 删除已发送的报告
					NoteApplication.LogD(SendCrashReportsTask.class, "Send OK :" + fileName);
					//System.out.println("=CCC= Send OK :" + fileName);
				} else {
					NoteApplication.LogD(SendCrashReportsTask.class, "Send FAILURE :" + fileName);
					//System.out.println("=CCC= Send FAILURE :" + fileName);
				}
				
			}
		} else {
			NoteApplication.LogD(SendCrashReportsTask.class, "本地没有LOG文件");
		}
	}

	private boolean postReport(File file) {
		// TODO 使用HTTP Post 发送错误报告到服务器
		// 这里不再详述,开发者可以根据OPhoneSDN上的其他网络操作
		// 教程来提交错误报告
		System.out.println("=CCC= postReport "+file.getAbsolutePath());
		HttpEntityEnclosingRequestBase httpRequest = new HttpPost(ServerInterface.URL_send_reports);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		StringBuffer sb = new StringBuffer();
		try {
			readToBuffer(sb, new FileInputStream(file));
		} catch (FileNotFoundException e1) {
			NoteApplication.LogD(SendCrashReportsTask.class, "FileNotFoundException");
			return false;
		} catch (IOException e1) {
			NoteApplication.LogD(SendCrashReportsTask.class, "IOException");
			return false;
		}

		int user_id = ServiceManager.getUserId();
		
		
        params.add(new BasicNameValuePair("user_id", Integer.valueOf(user_id).toString()));
		String versionSDK = Integer.valueOf(android.os.Build.VERSION.SDK)
				.toString();
		params.add(new BasicNameValuePair("version_sdk", versionSDK));

		DisplayMetrics dm = new DisplayMetrics();
		
		if(ServiceManager.getWindowManager() != null){	
			ServiceManager.getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
			float width = dm.widthPixels;
			float height = dm.heightPixels;
			float density = dm.densityDpi;
			params.add(new BasicNameValuePair("width", Float.toString(width)));
			params.add(new BasicNameValuePair("height", Float.toString(height)));
			params.add(new BasicNameValuePair("density", Float.toString(density)));
		}
		params.add(new BasicNameValuePair("version_code", Integer.valueOf(
				VersionUtil.getVerCode(NoteApplication.getContext())).toString()));
		params.add(new BasicNameValuePair("version_name", VersionUtil
				.getVerName(NoteApplication.getContext())));
		params.add(new BasicNameValuePair("report", sb.toString()));		
		
		String phoneModel = android.os.Build.MODEL;
		params.add(new BasicNameValuePair("phone_model", phoneModel));
		try {
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
			System.out.println("StatusCode :" + httpResponse.getStatusLine().getStatusCode());
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				String strResult = EntityUtils.toString(httpResponse
						.getEntity(), HTTP.UTF_8);
				System.out.println();
				if ("0".equals(strResult)) {
					System.out.println("=CCC= postReport OK "
							+ file.getAbsolutePath());
					return true;
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return false;
		
	}
	
	public void readToBuffer(StringBuffer buffer, InputStream is)
			throws IOException {
		String line; // 用来保存每行读取的内容
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(is));
		line = reader.readLine(); // 读取第一行
		while (line != null) { // 如果 line 为空说明读完了
			buffer.append(line); // 将读到的内容添加到 buffer 中
			buffer.append("/n"); // 添加换行符
			line = reader.readLine(); // 读取下一行
		}
	}
	
	/**
	 * 获取错误报告文件名
	 * @param ctx
	 * @return
	 */
	private String[] getCrashReportFiles() {
		File filesDir = new File(NoteApplication.crashPath);
		if (!filesDir.exists()) {
			filesDir.mkdirs();
		}
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(CRASH_REPORTER_EXTENSION);
			}
		};
		return filesDir.list(filter);
	}
	
}

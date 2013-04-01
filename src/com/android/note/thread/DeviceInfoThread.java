package com.android.note.thread;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.android.note.NoteApplication;
import com.android.note.Services.UserInfoService;
import com.android.note.Utils.DeviceInfo;
import com.android.note.Utils.NetworkUtils;
import com.android.note.Utils.ServerInterface;
import com.android.note.Utils.DeviceInfo.InfoName;

import android.content.Context;
import android.content.SharedPreferences;

public class DeviceInfoThread extends Thread {
	private boolean stop = false;
	public static String PREF = "DeviceInfo";
	public static String PREF_KEY_CONTENT = "content";
	public static String PREF_KEY_VERSION = "version";
	public static String PREF_KEY_OK_TIME = "ok_time";
	private Context context;

	@Override
	public void run() {
		int times = UserInfoService.COUNT_TIMES;
		while (!stop && times > 0) {
			System.out.println("===send deviceInfo===" + times);
			try {
				if (uploadCountToServer()) {
					SharedPreferences sp = context.getSharedPreferences(PREF, 0);
					SharedPreferences.Editor editor = sp.edit();
			    	editor.putInt(PREF_KEY_OK_TIME, 1);
			    	editor.commit();
					break;
				} 
				sleep(UserInfoService.COUNT_DURATION);
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				times--;
			}
		}
	}

	public DeviceInfoThread() {
		context = NoteApplication.getContext();
	}

	private boolean uploadCountToServer() {
		System.out.println("+++has network+++");
		if(NetworkUtils.getNetworkState(context) != NetworkUtils.NETWORN_NONE){
			System.out.println("+++has network+++");
			try {
				HttpEntityEnclosingRequestBase httpRequest = new HttpPost(ServerInterface.URL_DEVICEINFO);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair(InfoName.IMEI.toString(), DeviceInfo.getInformation(InfoName.IMEI)));
				params.add(new BasicNameValuePair(InfoName.CPU_MAX_FREQUENCY.toString(), DeviceInfo.getInformation(InfoName.CPU_MAX_FREQUENCY)));
				params.add(new BasicNameValuePair(InfoName.CPU_MODEL.toString(), DeviceInfo.getInformation(InfoName.CPU_MODEL)));
				params.add(new BasicNameValuePair(InfoName.MEMORY_TOTAL.toString(), DeviceInfo.getInformation(InfoName.MEMORY_TOTAL)));
				params.add(new BasicNameValuePair(InfoName.NOTE_VERSION.toString(), DeviceInfo.getInformation(InfoName.NOTE_VERSION)));
				params.add(new BasicNameValuePair(InfoName.PHONE_MODEL.toString(), DeviceInfo.getInformation(InfoName.PHONE_MODEL)));
				params.add(new BasicNameValuePair(InfoName.SCREEN_RESOLUTION.toString(), DeviceInfo.getInformation(InfoName.SCREEN_RESOLUTION)));
				params.add(new BasicNameValuePair(InfoName.SCREEN_DENSITYDPI.toString(), DeviceInfo.getInformation(InfoName.SCREEN_DENSITYDPI)));
				params.add(new BasicNameValuePair(InfoName.SYSTEM_VERSION.toString(), DeviceInfo.getInformation(InfoName.SYSTEM_VERSION)));
				httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				System.out.println("+++http request+++========");
				System.out.println(httpRequest.toString());
				HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
				System.out.println("+++return code+++" + httpResponse.getStatusLine().getStatusCode());
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					return true;
				}
			} catch (Exception e) {
				System.out.println("=== exception ===");
				e.printStackTrace();
			}
		}
		return false;
	}

	public void stopThread() {
		stop = true;
	}
}

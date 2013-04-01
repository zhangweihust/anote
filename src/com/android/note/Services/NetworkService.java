package com.android.note.Services;

import java.text.SimpleDateFormat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.widget.Toast;

import com.android.note.NoteApplication;
import com.android.note.Task.SendCrashReportsTask;
import com.archermind.note.R;

public class NetworkService implements IService {
	private Context context;
	private NetstateReceiver mReceiver;
	private SimpleDateFormat sDateFormat;
	private static final String XML_NAME = "SendCrash";
	private static final String XML_KEY_TIME = "crash_upload_time";
	SharedPreferences preferences;

	@Override
	public boolean start() {
		context = NoteApplication.getContext();
		mReceiver = new NetstateReceiver();
		preferences = context.getSharedPreferences(XML_NAME,Context.MODE_WORLD_WRITEABLE);
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		context.registerReceiver(mReceiver, filter);
		return true;
	}

	@Override
	public boolean stop() {
		context.unregisterReceiver(mReceiver);
		return true;
	}
	
	
	class NetstateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			NoteApplication.LogD(NetworkService.class, "Net is changed");
				ConnectivityManager manager = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo gprs = manager
						.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				NetworkInfo wifi = manager
						.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if ((gprs == null || !gprs.isConnected()) && (wifi == null ||!wifi.isConnected())) {
					//NoteApplication.networkIsOk = false;
				} else {
					//NoteApplication.networkIsOk = true;
					String saveTime =preferences.getString(XML_KEY_TIME, null);
					sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
					if(saveTime != null && sDateFormat.format(new java.util.Date()).equals(saveTime)){
						return;
					} 
					NoteApplication.LogD(NetworkService.class, "Net is connected");
					String date = sDateFormat.format(new java.util.Date());
					preferences.edit().putString(XML_KEY_TIME, date).commit();
					System.out.println("======= NetstateReceiver ===========");
					SendCrashReportsTask task = new SendCrashReportsTask();
					task.execute();
				}
		}

	}
}

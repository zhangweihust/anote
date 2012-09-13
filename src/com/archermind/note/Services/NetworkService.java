package com.archermind.note.Services;

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

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Task.SendCrashReportsTask;

public class NetworkService implements IService {
	private WifiManager wifiManager;
	private WifiLock wifiLock;
	private static String TAG = NetworkService.class.getCanonicalName();
	// Will be added in froyo SDK
	private int ConnectivityManager_TYPE_WIMAX = 6;
	private Context context;
	private Handler handler;
	private NetstateReceiver mReceiver;
	private SimpleDateFormat sDateFormat;
	private static final String XML_NAME = "SendCrash";
	private static final String XML_KEY_TIME = "crash_upload_time";
	SharedPreferences preferences;
	private int netType;

	@Override
	public boolean start() {
		context = NoteApplication.getContext();
		this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		handler = NoteApplication.getInstance().getHandler();
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
	
	
	public int getNetType() {
		return netType;
	}


	public boolean acquire(boolean show) {
		boolean connected = false;
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

		if (networkInfo == null) {
			if (show) {
				NoteApplication.toastShow(handler, R.string.get_network_failed);
			}

//			Log.d(NetworkService.TAG, "Failed to get Network information");
			return false;
		}

		netType = networkInfo.getType();
		int netSubType = networkInfo.getSubtype();
		if (!networkInfo.isAvailable()) {
			if (show) {
				NoteApplication.toastShow(handler, R.string.no_network);
			}

			return false;
		}

//		Log.d(NetworkService.TAG, String.format("netType=%d and netSubType=%d", netType, netSubType));

		if (netType == ConnectivityManager.TYPE_WIFI) {
			if (this.wifiManager.isWifiEnabled()) {
				this.wifiLock = this.wifiManager.createWifiLock(NetworkService.TAG);
				final WifiInfo wifiInfo = this.wifiManager.getConnectionInfo();
				if (wifiInfo != null && this.wifiLock != null) {
					final DetailedState detailedState = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
					if (detailedState == DetailedState.CONNECTED || detailedState == DetailedState.CONNECTING || detailedState == DetailedState.OBTAINING_IPADDR) {
						connected = true;
					}
				}
			} else {
				if (show) {
					NoteApplication.toastShow(handler, R.string.wifi_unable);
				}

//				Log.d(NetworkService.TAG, "WiFi not enabled");
			}
		} else if (netType == ConnectivityManager.TYPE_MOBILE || netType == ConnectivityManager_TYPE_WIMAX) {
			if ((netSubType >= TelephonyManager.NETWORK_TYPE_UMTS) || // HACK
					(netSubType == TelephonyManager.NETWORK_TYPE_GPRS) || (netSubType == TelephonyManager.NETWORK_TYPE_EDGE)) {
				connected = true;
			} else {
				if (show) {
					NoteApplication.toastShow(handler, R.string.n_3g_unable);
				}
				
//				Log.d(NetworkService.TAG, "3G not enabled");
			}
		}
		if (!connected) {
			return false;
		}
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
					NoteApplication.networkIsOk = false;
				} else {
					NoteApplication.networkIsOk = true;
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

package com.archermind.note.Screens;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.xml.datatype.Duration;
import javax.xml.transform.Templates;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.Toast;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Adapter.InformationAdapter;
import com.archermind.note.Adapter.MoreAdapter.ListItemsView;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Provider.DatabaseManager;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.DateTimeUtils;
import com.archermind.note.Utils.InformationData;
import com.archermind.note.Utils.NetworkUtils;
import com.archermind.note.Utils.ServerInterface;
import com.archermind.note.Views.XListView;
import com.archermind.note.Views.XListView.IXListViewListener;
import com.archermind.note.Views.XListView.OnXScrollListener;

public class InformationScreen extends Screen implements IXListViewListener,
		OnXScrollListener, OnClickListener {

	private XListView mxlvInformation;
	private Button mbtnBack;
	private ArrayList<InformationData> malInformations = new ArrayList<InformationData>();
	private Cursor mcInformations;
	private InformationAdapter mInformationAdapter;
	private static int PER_FRESH_COUNT = 10;
	private static int LOCAL_KEEP_COUNT = 30;
	private ArrayList<InformationData> listdata;

	protected static final int ON_Refresh = 0x101;
	protected static final int ON_LoadMore = 0x102;
	protected static final int ON_LoadData = 0x103;
	protected static final int ON_Null = 0x104;

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case ON_Refresh:
				mInformationAdapter.addPreData(listdata);
				mxlvInformation.setSelection(1);
				Toast.makeText(InformationScreen.this,
						"共有" + listdata.size() + "条更新", Toast.LENGTH_LONG)
						.show();
				mxlvInformation.setFooterVisibility(View.VISIBLE);
				mxlvInformation.setFooterDividersEnabled(true);
				mxlvInformation.setPullLoadEnable(true);
				refreshCompleted();
				break;
			case ON_LoadMore:
				mInformationAdapter.addAfterData(listdata);
				mxlvInformation.setSelection(mInformationAdapter.getCount());
				Toast.makeText(InformationScreen.this,
						"共有" + listdata.size() + "条更新", Toast.LENGTH_LONG)
						.show();
				moreCompleted();
				break;
			case ON_LoadData:
				mInformationAdapter.addPreData(malInformations);
				mxlvInformation.setSelection(1);
				if (mInformationAdapter.isEmpty()) {
					mInformationAdapter.setNoInformationPrompt(System
							.currentTimeMillis());					
				}
				
				if(mInformationAdapter.getCount()<10){
					mxlvInformation.setFooterVisibility(View.GONE);
					mxlvInformation.setFooterDividersEnabled(false);
					mxlvInformation.setPullLoadEnable(false);
				}
				break;
			case ON_Null:
				Toast.makeText(InformationScreen.this, "暂时没有更新",
						Toast.LENGTH_LONG).show();
				refreshCompleted();
				break;
			}

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.information_screen);

		mxlvInformation = (XListView) findViewById(R.id.xlv_information);
		mxlvInformation.setXListViewListener(this);
		mxlvInformation.setPullLoadEnable(true);
		mxlvInformation.setPullRefreshEnable(true);

		mbtnBack = (Button) findViewById(R.id.btn_back);
		mbtnBack.setOnClickListener(this);

		mInformationAdapter = new InformationAdapter(this, malInformations);
		mxlvInformation.setAdapter(mInformationAdapter);

		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mcInformations = ServiceManager.getDbManager()
						.queryInformations();
				cursorToListData(mcInformations, malInformations);
				mcInformations.close();
				mHandler.sendEmptyMessage(ON_LoadData);
			}
		}).start();
	}

	private void insertData(InformationData data) {
		ContentValues contentValues = new ContentValues();
		contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.COLUMN_REPLY_TITLE, data.title);
		contentValues.put(DatabaseHelper.COLUMN_REPLY_CONTENT, data.content);
		contentValues.put(DatabaseHelper.COLUMN_REPLY_TIME, data.time);
		contentValues.put(DatabaseHelper.COLUMN_REPLY_PHOTO, data.photo);
		contentValues.put(DatabaseHelper.COLUMN_REPLY_NICKNAME, data.nickname);
		ServiceManager.getDbManager().insertInformation(contentValues);
	}

	/*
	 * [{"date":"1345712808","content":"lalalala","title":"08236","nickname":"test1"
	 * ,"portrait":
	 * "http:\/\/yun.archermind.com\/mobile\/service\/showMedia?appId=0ba7932602af4a45bd866bad93be0e50&userName=68879@qq.com&mediaName=1_20120823_070307&mediaType=jpg"},{"date":"1345712708","content":"lalalala","title":"08236","nickname":"test1","portrait":""},{"date":"1345712608","content":"lalalala","title":"gdtjj","nickname":"test1","portrait":""},{"date":"1345712508","content":"lalalala","title":"gdtjj","nickname":"test1","portrait":""},{"date":"1345712408","content":"lalalala","title":"gddf","nickname":"test1","portrait":""},{"date":"1345712308","content":"lalalala","title":"gdgh","nickname":"test1","portrait":""},{"date":"1345712208","content":"lalalala","title":"gdgh","nickname":"test1","portrait":""},{"date":"1345712208","content":"lalalala","title":"gdgh","nickname":"wangac","portrait":""},{"date":"1345712108","content":"lalalala","title":"gdgh","nickname":"test1","portrait":"http:\/\/yun.archermind.com\/mobile\/service\/showMedia?appId=0ba7932602af4a45bd866bad93be0e50&userName=54756876@qq.com&mediaName=4_20120823_075718&mediaType=jpg"},{"date":"1345712008","content":"lalalala","title":"gdgh","nickname":"test1","portrait":"http:\/\/yun.archermind.com\/mobile\/service\/showMedia?appId=0ba7932602af4a45bd866bad93be0e50&userName=54756876@qq.com&mediaName=4_20120823_075718&mediaType=jpg"}]
	 */
	private void parseJsonandUpdateDatabase(String s) {
		try {
			if (s.contains("[")) {
				String photoUrl = s.substring(0, s.indexOf("["));
				JSONArray infoArray = new JSONArray(s.substring(s.indexOf("[")));
				InformationData info = null;
				JSONObject jsonObject = null;
				for (int i = 0; i < infoArray.length(); i++) {
					jsonObject = (JSONObject) infoArray.opt(i);
					info = new InformationData();
					info.content = jsonObject.getString("content");
					info.nickname = jsonObject.getString("nickname");
					info.userid = jsonObject.getInt("user_id");
					String tmp = jsonObject.getString("portrait");
					System.out.println("url0 = " + tmp);
					if (tmp != null && !tmp.trim().equals("")) {
						info.photo = photoUrl + tmp;
						System.out.println("url = " + info.photo);
					}
					info.title = jsonObject.getString("title");
					info.time = jsonObject.getLong("date");
					insertData(info);
				}
			} else {
				return;
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void parseJsonandUpdateArraylist(String s,
			ArrayList<InformationData> listdata) {
		try {
			if (s.contains("[")) {
				String photoUrl = s.substring(0, s.indexOf("["));
				JSONArray infoArray = new JSONArray(s.substring(s.indexOf("[")));
				InformationData info = null;
				JSONObject jsonObject = null;
				for (int i = 0; i < infoArray.length(); i++) {
					jsonObject = (JSONObject) infoArray.opt(i);
					info = new InformationData();
					info.content = jsonObject.getString("content");
					info.nickname = jsonObject.getString("nickname");
					info.userid = jsonObject.getInt("user_id");
					String tmp = jsonObject.getString("portrait");
					if (tmp != null && !tmp.trim().equals("")) {
						info.photo = photoUrl + tmp;
						System.out.println("url = " + info.photo);
					}
					info.title = jsonObject.getString("title");
					info.time = jsonObject.getLong("date");
					listdata.add(info);
				}
			} else {
				return;
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void cursorToListData(Cursor c, List<InformationData> listdata) {
		InformationData data;

		if (c == null) {
			return;
		}
		while (c.moveToNext()) {
			data = new InformationData();
			data.content = c.getString(c
					.getColumnIndex(DatabaseHelper.COLUMN_REPLY_CONTENT));
			data.time = c.getLong(c
					.getColumnIndex(DatabaseHelper.COLUMN_REPLY_TIME));
			data.title = c.getString(c
					.getColumnIndex(DatabaseHelper.COLUMN_REPLY_TITLE));
			data.photo = c.getString(c
					.getColumnIndex(DatabaseHelper.COLUMN_REPLY_PHOTO));
			data.nickname = c.getString(c
					.getColumnIndex(DatabaseHelper.COLUMN_REPLY_NICKNAME));
			listdata.add(data);
		}
	}

	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onXScrolling(View view) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		System.out.println("==== onRefresh ====");

		if (NetworkUtils.getNetworkState(InformationScreen.this) == NetworkUtils.NETWORN_NONE) {
			Toast.makeText(InformationScreen.this, R.string.network_none,
					Toast.LENGTH_SHORT).show();
			refreshCompleted();
			return;
		}
		if (!ServiceManager.isLogin()) {
			Toast.makeText(InformationScreen.this, R.string.no_login_info,
					Toast.LENGTH_SHORT).show();
			refreshCompleted();
			Intent intent = new Intent();
			intent.setClass(InformationScreen.this, LoginScreen.class);
			InformationScreen.this.startActivity(intent);
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				int userid = ServiceManager.getUserId();
				String result = null;
				if (mInformationAdapter.getLatestTime() == 0) {
					System.out.println("");
					result = ServerInterface.getReplyFromUser(userid,
							mInformationAdapter.getLatestTime(),
							PER_FRESH_COUNT);
				} else {
					System.out.println("lastestTime : "
							+ mInformationAdapter.getLatestTime());
					result = ServerInterface.getReplyFromUser(userid,
							mInformationAdapter.getLatestTime(), 1000);
				}
				if (result != null
						&& result.equals(ServerInterface.COOKIES_ERROR + "")) {
					ServiceManager.setLogin(false);
					InformationScreen.this.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(InformationScreen.this,
									R.string.cookies_error, Toast.LENGTH_SHORT).show();
						}
					});
					
					Intent intent = new Intent();
					intent.setClass(InformationScreen.this, LoginScreen.class);
					InformationScreen.this.startActivity(intent);
				} else if (result != null && result.contains("date")) {
					parseJsonandUpdateDatabase(result);
					Cursor c;
					// data after mInformationAdapter.getLatestTime();
					if (mcInformations.getCount() == 0) {
						c = ServiceManager.getDbManager().queryInformations();
					} else {
						System.out.println("mcinformation "
								+ mcInformations.getColumnCount());
						c = ServiceManager.getDbManager()
								.queryInformationsAfter(
										mInformationAdapter.getLatestTime());
					}
					listdata = new ArrayList<InformationData>();
					cursorToListData(c, listdata);
					// Collections.reverse(listdata); //
					// 数据库查询时按照降序排列，因此此处需要将listdata中数据倒序
					c.close();
					mHandler.sendEmptyMessage(ON_Refresh);
				} else {
					mHandler.sendEmptyMessage(ON_Null);
				}

			}
		}).start();
	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		System.out.println("==== onLoadMore ====");
		if (NetworkUtils.getNetworkState(InformationScreen.this) == NetworkUtils.NETWORN_NONE) {
			Toast.makeText(InformationScreen.this, R.string.network_none,
					Toast.LENGTH_SHORT).show();
			moreCompleted();
			return;
		}
		if (!ServiceManager.isLogin()) {
			Toast.makeText(InformationScreen.this, R.string.no_login_info,
					Toast.LENGTH_SHORT).show();
			moreCompleted();
			Intent intent = new Intent();
			intent.setClass(InformationScreen.this, LoginScreen.class);
			InformationScreen.this.startActivity(intent);
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				int userid = ServiceManager.getUserId();
				String result = ServerInterface.getReplyFromUser(userid,
						mInformationAdapter.getEarlistTime(), -PER_FRESH_COUNT);
				listdata = new ArrayList<InformationData>();
				if (result != null
						&& result.equals(ServerInterface.COOKIES_ERROR + "")) {
					ServiceManager.setLogin(false);
					Toast.makeText(InformationScreen.this,
							R.string.cookies_error, Toast.LENGTH_SHORT).show();
					Intent intent = new Intent();
					intent.setClass(InformationScreen.this, LoginScreen.class);
					InformationScreen.this.startActivity(intent);
				} else 	if (result != null && result.contains("date")) {
					parseJsonandUpdateArraylist(result, listdata);
					mHandler.sendEmptyMessage(ON_LoadMore);
				} else {
					mHandler.sendEmptyMessage(ON_Null);
				}
			}
		}).start();

	}

	private void refreshCompleted() {
		mxlvInformation.stopRefresh();
		mxlvInformation.stopLoadMore();
		mxlvInformation.setRefreshTime(DateTimeUtils.time2String(
				"yyyy-MM-dd HH:mm", System.currentTimeMillis()));
	}

	private void moreCompleted() {
		mxlvInformation.stopRefresh();
		mxlvInformation.stopLoadMore();
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.btn_back:
			finish();
			break;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		System.out.println("==== onDestroy ====");
		if (malInformations != null && !malInformations.isEmpty()
				&& malInformations.size() > LOCAL_KEEP_COUNT) {
			ServiceManager.getDbManager().deleteInformations(
					malInformations.get(LOCAL_KEEP_COUNT - 1).time);
		}
		super.onDestroy();
	}

}
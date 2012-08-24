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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

public class InformationScreen extends Screen implements IXListViewListener,OnXScrollListener, OnClickListener{
	
	private XListView mxlvInformation;
	private Button mbtnBack;
    private ArrayList<InformationData> malInformations = new ArrayList<InformationData>();
    private Cursor mcInformations;
    private InformationAdapter mInformationAdapter;
    private static int PER_FRESH_COUNT = 10;
    private static int LOCAL_KEEP_COUNT = 30;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_screen);
       
        mxlvInformation = (XListView)findViewById(R.id.xlv_information);
        mxlvInformation.setXListViewListener(this);
        mxlvInformation.setOnScrollListener(this);
        mxlvInformation.setPullLoadEnable(true); 
        
        mbtnBack = (Button)findViewById(R.id.btn_back);
        mbtnBack.setOnClickListener(this);
        
        mcInformations = ServiceManager.getDbManager().queryInformations();
        cursorToListData(mcInformations, malInformations);
        mInformationAdapter = new InformationAdapter(this, malInformations);
        mxlvInformation.setAdapter(mInformationAdapter);
        if(mInformationAdapter.isEmpty()){
        	mInformationAdapter.setNoInformationPrompt(System.currentTimeMillis());
        }
	}
	
	
	private void insertData(InformationData data){
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
	 * [{"date":"1345712808","content":"lalalala","title":"08236","nickname":"test1","portrait":"http:\/\/yun.archermind.com\/mobile\/service\/showMedia?appId=0ba7932602af4a45bd866bad93be0e50&userName=68879@qq.com&mediaName=1_20120823_070307&mediaType=jpg"},{"date":"1345712708","content":"lalalala","title":"08236","nickname":"test1","portrait":""},{"date":"1345712608","content":"lalalala","title":"gdtjj","nickname":"test1","portrait":""},{"date":"1345712508","content":"lalalala","title":"gdtjj","nickname":"test1","portrait":""},{"date":"1345712408","content":"lalalala","title":"gddf","nickname":"test1","portrait":""},{"date":"1345712308","content":"lalalala","title":"gdgh","nickname":"test1","portrait":""},{"date":"1345712208","content":"lalalala","title":"gdgh","nickname":"test1","portrait":""},{"date":"1345712208","content":"lalalala","title":"gdgh","nickname":"wangac","portrait":""},{"date":"1345712108","content":"lalalala","title":"gdgh","nickname":"test1","portrait":"http:\/\/yun.archermind.com\/mobile\/service\/showMedia?appId=0ba7932602af4a45bd866bad93be0e50&userName=54756876@qq.com&mediaName=4_20120823_075718&mediaType=jpg"},{"date":"1345712008","content":"lalalala","title":"gdgh","nickname":"test1","portrait":"http:\/\/yun.archermind.com\/mobile\/service\/showMedia?appId=0ba7932602af4a45bd866bad93be0e50&userName=54756876@qq.com&mediaName=4_20120823_075718&mediaType=jpg"}]
	 * 
	 * */
	private void parseJsonandUpdateDatabase(String s){
		try {
			JSONArray infoArray = new JSONArray(s);
			InformationData info = null;
			JSONObject jsonObject = null;
			for(int i=0; i< infoArray.length(); i++){
				jsonObject = (JSONObject) infoArray.opt(i);
				info = new InformationData();
				info.content = jsonObject.getString("content");
				info.nickname = jsonObject.getString("nickname");
				info.photo = jsonObject.getString("portrait");	
				info.title = jsonObject.getString("title");
				info.time = jsonObject.getLong("date");
				insertData(info);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void parseJsonandUpdateArraylist(String s, ArrayList<InformationData> listdata){
		try {
			JSONArray infoArray = new JSONArray(s);
			InformationData info = null;
			JSONObject jsonObject = null;
			for(int i=0 ; i < infoArray.length(); i++){
				jsonObject = (JSONObject) infoArray.opt(i);
				info = new InformationData();
				info.content = jsonObject.getString("content");
				info.nickname = jsonObject.getString("nickname");
				info.photo = jsonObject.getString("portrait");	
				info.title = jsonObject.getString("title");
				info.time = jsonObject.getLong("date");
				listdata.add(info);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void cursorToListData(Cursor c,List<InformationData> listdata)
	{
		InformationData data;
		
		if (c == null)
		{
			return;
		}	
		while (c.moveToNext())
		{
			data = new InformationData();
			data.content = c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_REPLY_CONTENT));
			data.time = c.getLong(c.getColumnIndex(DatabaseHelper.COLUMN_REPLY_TIME));
			data.title = c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_REPLY_TITLE));
			data.photo = c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_REPLY_PHOTO));
			data.nickname = c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_REPLY_NICKNAME));
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
		
		if(NetworkUtils.getNetworkState(InformationScreen.this) == NetworkUtils.NETWORN_NONE){
			Toast.makeText(InformationScreen.this, R.string.network_none, Toast.LENGTH_SHORT).show();
			refreshCompleted();
			return;
		}
		if(!NoteApplication.getInstance().isLogin()){
			Toast.makeText(InformationScreen.this, R.string.no_login_info, Toast.LENGTH_SHORT).show();
			refreshCompleted();
			return;
		}
		int userid = NoteApplication.getInstance().getUserId();;
	    String result = ServerInterface.getReplyFromUser(userid, mInformationAdapter.getLatestTime(), PER_FRESH_COUNT);
	    if(result != null && result.contains("date")){
	    	parseJsonandUpdateDatabase(result);
	    }else {
	    	Toast.makeText(InformationScreen.this, "暂时没有更新", Toast.LENGTH_LONG).show();
	    	refreshCompleted();
			return;
		}
	    
		Cursor c;
		 // data after mInformationAdapter.getLatestTime();
		if (mcInformations.getCount() == 0)
		{
			c = ServiceManager.getDbManager().queryInformations();
		}
		else
		{
			System.out.println("mcinformation " + mcInformations.getColumnCount());
			c = ServiceManager.getDbManager().queryInformationsAfter(mInformationAdapter.getLatestTime());
		}
		ArrayList<InformationData> listdata = new ArrayList<InformationData>();
		cursorToListData(c,listdata);
		Collections.reverse(listdata);		// 数据库查询时按照降序排列，因此此处需要将listdata中数据倒序 
		c.close();
		mInformationAdapter.addPreData(listdata);
		mxlvInformation.setSelection(1);
		Toast.makeText(InformationScreen.this, "共有" + c.getCount() + "条更新", Toast.LENGTH_LONG).show();
		refreshCompleted();
	}
	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		System.out.println("==== onLoadMore ====");
		if(NetworkUtils.getNetworkState(InformationScreen.this) == NetworkUtils.NETWORN_NONE){
			Toast.makeText(InformationScreen.this, R.string.network_none, Toast.LENGTH_SHORT).show();
			moreCompleted();
			return;
		}
		if(!NoteApplication.getInstance().isLogin()){
			Toast.makeText(InformationScreen.this, R.string.no_login_info, Toast.LENGTH_SHORT).show();
			moreCompleted();
			return;
		}
		int userid = NoteApplication.getInstance().getUserId();
	    String result = ServerInterface.getReplyFromUser(userid, mInformationAdapter.getEarlistTime(), -PER_FRESH_COUNT);
	    ArrayList<InformationData> listdata = new ArrayList<InformationData>();
	    if(result != null && result.contains("date")){
	    	parseJsonandUpdateArraylist(result, listdata);
	    }else {
	    	moreCompleted();
			return;
		}
	    
		mInformationAdapter.addAfterData(listdata);
		mxlvInformation.setSelection(mInformationAdapter.getCount());
		Toast.makeText(InformationScreen.this, "更新了" + listdata.size() + "条通知", Toast.LENGTH_LONG).show();
		moreCompleted();
	}
	
	private void refreshCompleted() {
		mxlvInformation.stopRefresh();
		mxlvInformation.stopLoadMore();
		mxlvInformation.setRefreshTime(DateTimeUtils.time2String("yyyy-MM-dd HH:mm",
				System.currentTimeMillis()));
	}
	
	private void moreCompleted() {
		mxlvInformation.stopRefresh();
		mxlvInformation.stopLoadMore();
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch(arg0.getId()){
		case R.id.btn_back:
			finish();
			break;
		}
	}

	public static Bitmap getHttpBitmap(String url) {
		
		Bitmap bitmap = null;
		try {
			URL myFileUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
			conn.setConnectTimeout(0);
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
			return bitmap;
		}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		System.out.println("==== onDestroy ====");
		if(malInformations != null && !malInformations.isEmpty() && malInformations.size()> LOCAL_KEEP_COUNT){
			ServiceManager.getDbManager().deleteInformations(malInformations.get(LOCAL_KEEP_COUNT-1).time);
		}
		super.onDestroy();
	}
	
}
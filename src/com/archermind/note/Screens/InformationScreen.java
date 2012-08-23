package com.archermind.note.Screens;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.R.integer;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.Button;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Adapter.InformationAdapter;
import com.archermind.note.Adapter.MoreAdapter.ListItemsView;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.InformationData;
import com.archermind.note.Views.XListView;
import com.archermind.note.Views.XListView.IXListViewListener;
import com.archermind.note.Views.XListView.OnXScrollListener;

public class InformationScreen extends Screen implements IXListViewListener,OnXScrollListener, OnClickListener{
	
	private XListView mxlvInformation;
	private Button mbtnBack;
    private ArrayList<InformationData> malInformations = new ArrayList<InformationData>();
    private Cursor mcInformations;
    private InformationAdapter mInformationAdapter;
    
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
        
        //mcInformations = ServiceManager.getDbManager().queryLocalNotes();
        mInformationAdapter = new InformationAdapter(this, malInformations);
        mxlvInformation.setAdapter(mInformationAdapter);
        if(mInformationAdapter.isEmpty()){
        	mInformationAdapter.setNoInformationPrompt(System.currentTimeMillis());
        }
	}
	
	private void getdata(int b){
		for(int i=0; i< 10 ; i++){
		InformationData abc = new InformationData();
		abc.id= b + i;
		abc.content = "lalallalallaldfjdkfjd " + abc.id;
		abc.nickname = "acb " + abc.id;
		abc.photo =getHttpBitmap("http://yun.archermind.com/mobile/service/showMedia?appId=0ba7932602af4a45bd866bad93be0e50&userName=test@163.com&mediaName=face_a1&mediaType=gif");	
		abc.title = "title " + abc.id;
		abc.time = System.currentTimeMillis()- i * 50000;
		malInformations.add(abc);
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
		
	}
	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		
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
}
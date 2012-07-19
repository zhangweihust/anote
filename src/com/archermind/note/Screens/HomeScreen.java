package com.archermind.note.Screens;


import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.archermind.note.R;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Events.IEventHandler;
import com.archermind.note.Services.EventService;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Views.VerticalScrollView;


public class HomeScreen extends Screen  implements IEventHandler{
    /** Called when the activity is first created. */
	private VerticalScrollView mllCalendarPage;
	private LinearLayout mllHomePage;
	private LinearLayout mllAlbumPage;
	private ImageView mListHeader;
	
	public static EventService eventService;

	public HomeScreen(){
		super();
		eventService = ServiceManager.getEventservice();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        
        mllHomePage = (LinearLayout)findViewById(R.id.ll_home_page);
        mllCalendarPage = (VerticalScrollView)findViewById(R.id.ll_calendar_page);
        mllAlbumPage = (LinearLayout)findViewById(R.id.ll_album_page);
        
        ListView list = (ListView) findViewById(R.id.list);
        mListHeader = new ImageView(this);
        mListHeader.setBackgroundResource(R.drawable.listview_header_up);
        mListHeader.setTag(R.drawable.listview_header_up);
        mListHeader.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Integer tag = (Integer) mListHeader.getTag();
				if(tag != null){
					if(tag.intValue() == R.drawable.listview_header_up){
						mllCalendarPage.snapToPage(1);
					} else if(tag.intValue() == R.drawable.listview_header_down) {
						mllCalendarPage.snapToPage(0);
					}
				}
			}
		});
		list.addHeaderView(mListHeader);
        list.setAdapter(new ArrayAdapter<String>(this,
		android.R.layout.simple_expandable_list_item_1,
		getData()));
        mllCalendarPage.addOnScrollListener(new VerticalScrollView.OnScrollListener() {
            public void onScroll(int scrollX) {
            }

            public void onViewScrollFinished(int currentPage) {
            }

			@Override
			public void snapToPage(int whichPage) {
				 Log.e("TestActivity", "whichPage=" + whichPage);
	            	if(whichPage  == 1){
	            		mListHeader.setBackgroundResource(R.drawable.listview_header_down);
	            		 mListHeader.setTag(R.drawable.listview_header_down);
	            	} else {
	            		mListHeader.setBackgroundResource(R.drawable.listview_header_up);
	            		 mListHeader.setTag(R.drawable.listview_header_up);
	            	}
			}
        });
        
        eventService.add(this);
    }

	@Override
	public boolean onEvent(Object sender, final EventArgs e) {
		// TODO Auto-generated method stub
		switch(e.getType()){
			case TITLE_BAR_CALENDER_CLICKED:
				HomeScreen.this.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						mllHomePage.setVisibility(View.GONE);
						mllCalendarPage.setVisibility(View.VISIBLE);
						mllAlbumPage.setVisibility(View.GONE);
					}});
				break;
			case TITLE_BAR_NOTEBOOK_CLICKED:
				HomeScreen.this.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						mllHomePage.setVisibility(View.VISIBLE);
						mllCalendarPage.setVisibility(View.GONE);
						mllAlbumPage.setVisibility(View.GONE);
					}});
				break;
			case TITLE_BAR_NOTE_ALBUM_CLICKED:
				HomeScreen.this.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						if(e.getExtra("type").equals(MainScreen.TYPE_NOTE)){
							mllHomePage.setVisibility(View.VISIBLE);
							mllCalendarPage.setVisibility(View.GONE);
							mllAlbumPage.setVisibility(View.GONE);
						}else{
							mllHomePage.setVisibility(View.GONE);
							mllCalendarPage.setVisibility(View.GONE);
							mllAlbumPage.setVisibility(View.VISIBLE);
						}
					}});
				break;
		}
		return false;
	}
    
      
	private List<String> getData(){
		List<String> data = new ArrayList<String>();
		data.add("测试数据1");
		data.add("测试数据2");
		data.add("测试数据3");
		data.add("测试数据4");
		data.add("测试数据1");
		data.add("测试数据2");
		data.add("测试数据3");
		data.add("测试数据4");
		data.add("测试数据1");
		data.add("测试数据2");
		data.add("测试数据3");
		data.add("测试数据4");
		return data;
	}
}

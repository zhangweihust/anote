package com.archermind.note.Screens;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.archermind.note.R;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Events.EventTypes;
import com.archermind.note.Events.IEventHandler;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Services.EventService;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.DateTimeUtils;
import com.archermind.note.Views.VerticalScrollView;


public class HomeScreen extends Screen  implements IEventHandler{
    /** Called when the activity is first created. */
	private VerticalScrollView mllCalendarPage;
	private LinearLayout mllHomePage;
	private LinearLayout mllAlbumPage;
	private LinearLayout mListHeader;
	private Button mBtnNewNote;
	private LinearLayout mllBottomInfo;
	private ImageView mIvMyNoteInfo;
	private TextView mTvNoNoteDays;
	private TextView mTvNoteCountToday;
	private static String tagCalendar = "calendar";
	private static String tagTimeList = "timelist";
	
	public static EventService eventService;

	public HomeScreen(){
		super();
		eventService = ServiceManager.getEventservice();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        
        //insert();
        
        mllHomePage = (LinearLayout)findViewById(R.id.ll_home_page);
        mllCalendarPage = (VerticalScrollView)findViewById(R.id.ll_calendar_page);
        mllAlbumPage = (LinearLayout)findViewById(R.id.ll_album_page);
        
        ListView list = (ListView) findViewById(R.id.list);
        
        mListHeader = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.home_screen_listview_header, null);
        mllBottomInfo = (LinearLayout)mListHeader.findViewById(R.id.ll_bottom_info);
    	mIvMyNoteInfo = (ImageView)mListHeader.findViewById(R.id.iv_my_note_info);
    	mTvNoNoteDays = (TextView)mListHeader.findViewById(R.id.tv_no_note_days);
        mTvNoteCountToday = (TextView)mListHeader.findViewById(R.id.tv_note_count_today);
        
        mBtnNewNote = (Button)mListHeader.findViewById(R.id.btn_new_note);
        mBtnNewNote.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				MainScreen.eventService.onUpdateEvent(new EventArgs(
						EventTypes.NEW_NOTE_BUTTON_CLICKED));
			}
        	
        });

        mListHeader.setTag(tagCalendar);
        mListHeader.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String tag = mListHeader.getTag().toString();
				if(tag != null){
					if(tag.equals(tagCalendar)){
						mllCalendarPage.snapToPage(1);
						 mllBottomInfo.setVisibility(View.GONE);
					} else if(tag.equals(tagTimeList)) {
						mllCalendarPage.snapToPage(0);
						 mllBottomInfo.setVisibility(View.VISIBLE);
					}
				}
			}
		});
		list.addHeaderView(mListHeader);
        list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1, getData()));
        mllCalendarPage.addOnScrollListener(new VerticalScrollView.OnScrollListener() {
            public void onScroll(int scrollX) {
            }

            public void onViewScrollFinished(int currentPage) {
            }

			@Override
			public void snapToPage(int whichPage) {
				 Log.e("TestActivity", "whichPage=" + whichPage);
	            	if(whichPage  == 1){
	            		 mListHeader.setTag(tagTimeList);
	            		 mllBottomInfo.setVisibility(View.GONE);
	            	} else {;
	            		 mListHeader.setTag(tagCalendar);
	            		 mllBottomInfo.setVisibility(View.VISIBLE);
	            	}
			}
        });
        
        eventService.add(this);
    }

    
    
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Cursor localNotes = ServiceManager.getDbManager().queryLocalNotes();
		if(localNotes.moveToFirst()){
			Long time = Long.parseLong(localNotes.getString(localNotes.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME)));
			Long today = (DateTimeUtils.getToday(Calendar.AM));
			if(time < today){
				Calendar cal = Calendar.getInstance(Locale.CHINA); 
				cal.setTimeInMillis(System.currentTimeMillis());
				int todayIndex = cal.get(Calendar.DAY_OF_YEAR);
				cal.setTimeInMillis(time);
				int lastDayIndex = cal.get(Calendar.DAY_OF_YEAR);
				int noNoteDays = todayIndex - lastDayIndex;
				mIvMyNoteInfo.setImageResource(R.drawable.no_note_some_days);
	        	mTvNoteCountToday.setVisibility(View.GONE);
	        	mTvNoNoteDays.setVisibility(View.VISIBLE);
	        	mTvNoNoteDays.setText("" + noNoteDays);
			}else{
		        int count = ServiceManager.getDbManager().queryTodayLocalNOTEs().getCount();
		        if(count == 0){
		        	mIvMyNoteInfo.setImageResource(R.drawable.no_note_today);
		        	mTvNoteCountToday.setVisibility(View.GONE);
		        	mTvNoNoteDays.setVisibility(View.GONE);
		        }else{
		        	mIvMyNoteInfo.setImageResource(R.drawable.has_note_today);
		        	mTvNoteCountToday.setText("" + count);
		        	mTvNoteCountToday.setVisibility(View.VISIBLE);
		        	mTvNoNoteDays.setVisibility(View.GONE);
		        }
				
			}
		}else{
        	mIvMyNoteInfo.setImageResource(R.drawable.no_note_today);
        	mTvNoteCountToday.setVisibility(View.GONE);
        	mTvNoNoteDays.setVisibility(View.GONE);
		}

		
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
		return true;
	}
    
    //插入测试数据
	private void insert() {
		// DateTimeUtils.getDayOfWeek(Calendar.SUNDAY);
		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.COLUMN_NOTE_ID,
				10003);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,
				"今天的第三条笔记");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_USER_ID, 1000);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT, "hello,3");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME, System.currentTimeMillis()-100000);
		ServiceManager.getDbManager().insertLocalNotes(contentValues);
		contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.COLUMN_NOTE_ID,
				10002);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,
				"今天的第二条笔记");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_USER_ID, 1000);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT, "hello,2");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME, System.currentTimeMillis()-150000);
		ServiceManager.getDbManager().insertLocalNotes(contentValues);
		contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.COLUMN_NOTE_ID,
				10001);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,
				"今天的第一条笔记");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_USER_ID, 1000);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT, "hello,1");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME, System.currentTimeMillis()-200000);
		ServiceManager.getDbManager().insertLocalNotes(contentValues);
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

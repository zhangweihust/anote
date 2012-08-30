package com.archermind.note.Screens;


import java.util.Calendar;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Adapter.CalendarAdapter;
import com.archermind.note.Adapter.LocalNoteAdapter;
import com.archermind.note.Adapter.LocalNoteOnedayAdapter;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Events.EventTypes;
import com.archermind.note.Events.IEventHandler;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Provider.DatabaseManager;
import com.archermind.note.Services.EventService;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.Constant;
import com.archermind.note.Utils.DateTimeUtils;
import com.archermind.note.Utils.DensityUtil;
import com.archermind.note.Utils.DownloadApkHelper;
import com.archermind.note.Views.VerticalScrollView;
import com.archermind.note.download.DownloadTaskManager;


public class HomeScreen extends Screen  implements IEventHandler, OnClickListener{
    /** Called when the activity is first created. */
	private VerticalScrollView mllCalendarPage;
	private static LinearLayout mllHomePage;
	private LinearLayout mListHeader;
	private TextView mTvMyNoteInfo;
	private static String tagCalendar = "calendar";
	private static String tagTimeList = "timelist";
/*	private Button mBtnPreMonth;
	private Button mBtnNextMonth;*/
	private Button mBtnBackCurmonth;
	private static int mCurMonth = 0;
	private static int mCurYear = 0;
	
	private static long mCurTime = 0;
/*	private Cursor mAllNotesCursor;
	public static int mCurPosition = 0;*/
	
	private ListView mlvMonthNotes;
	private Context mContext;
	private TextView mTvCurMonth;
	
	private ListView mlvDayNotes;
	private Button mbtnBackRecent;
	private long mLastestTime = 0;
	private long mEarlistTime = 0;
	private long mRecentTime = 0;
	
	//calendar
	private ViewFlipper flipper = null;
	public static CalendarAdapter mCalendarAdapter = null;
	private GridView mGridView = null;
	
	public static int PRE_MONTH = 0;
	public static int NEXT_MONTH = 1;
	
	private TextView tvCalendarWeekday0;
	private TextView tvCalendarWeekday1;
	private TextView tvCalendarWeekday2;
	private TextView tvCalendarWeekday3;
	private TextView tvCalendarWeekday4;
	private TextView tvCalendarWeekday5;
	private TextView tvCalendarWeekday6;
	
	
	private static boolean isFirst = true;
	private static int calendarHeight = 0;
	public static final EventService eventService = ServiceManager.getEventservice();

	public HomeScreen(){
		super();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        
        mContext = HomeScreen.this;
       
        mllHomePage = (LinearLayout)findViewById(R.id.ll_home_page);
        mllCalendarPage = (VerticalScrollView)findViewById(R.id.ll_calendar_page);
        
        mlvMonthNotes = (ListView) findViewById(R.id.lv_month_note_list);
        
        mlvDayNotes = (ListView) findViewById(R.id.lv_day_note_list);        
        mlvDayNotes.setDividerHeight(DensityUtil.dip2px(mContext, 5));
        
        mListHeader = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.home_screen_listview_header, null);
        mTvMyNoteInfo = (TextView)mListHeader.findViewById(R.id.tv_my_note_info);
        Typeface type = Typeface.createFromAsset(getAssets(),"xdxwzt.ttf");
		mTvMyNoteInfo.setTypeface(type);
        
        mTvCurMonth = (TextView)mListHeader.findViewById(R.id.tv_cur_month);
        
        mBtnBackCurmonth = (Button)mListHeader.findViewById(R.id.btn_back_curmonth);
        mBtnBackCurmonth.setOnClickListener(this);
/*        mBtnPreMonth = (Button)mListHeader.findViewById(R.id.btn_pre_month);
        mBtnPreMonth.setVisibility(View.GONE);
        mBtnPreMonth.setOnClickListener(this);

        mBtnNextMonth = (Button)mListHeader.findViewById(R.id.btn_next_month);
        mBtnNextMonth.setVisibility(View.GONE);
        mBtnNextMonth.setOnClickListener(this);*/
        
        mListHeader.setTag(tagCalendar);
        mListHeader.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String tag = mListHeader.getTag().toString();
				if(tag != null){
					if(tag.equals(tagCalendar)){
						mllCalendarPage.snapToPage(1);
						mTvMyNoteInfo.setVisibility(View.GONE);
						mListHeader.setTag(tagTimeList);
	            		//mllBottomInfo.setVisibility(View.GONE);
						Cursor cursor = ServiceManager
								.getDbManager().queryMonthLocalNOTES(mCurMonth, mCurYear);
						startManagingCursor(cursor);
	            		mlvMonthNotes.setAdapter(new LocalNoteAdapter(mContext, cursor));
					} else if(tag.equals(tagTimeList)) {
						mllCalendarPage.snapToPage(0);
						// mllBottomInfo.setVisibility(View.VISIBLE);
						 mListHeader.setTag(tagCalendar);
						 mTvMyNoteInfo.setVisibility(View.VISIBLE);
	            		 showCalendarMonth(NEXT_MONTH, false);
					}
				}
			}
        	
        });
        
        mlvMonthNotes.addHeaderView(mListHeader);
		Calendar time = Calendar.getInstance(Locale.CHINA); 
		time.setTimeInMillis(System.currentTimeMillis());
		mCurMonth = time.get(Calendar.MONTH);
		mCurYear = time.get(Calendar.YEAR);
		
		mTvCurMonth.setText(DateTimeUtils.time2String("yyyy年MM月", System.currentTimeMillis()));
		Cursor cursor = ServiceManager
				.getDbManager().queryMonthLocalNOTES(mCurMonth, mCurYear);
		startManagingCursor(cursor);
		mlvMonthNotes.setAdapter(new LocalNoteAdapter(this, cursor));

/*         mllCalendarPage.addOnScrollListener(new VerticalScrollView.OnScrollListener() {
            public void onScroll(int scrollX) {
            }

            public void onViewScrollFinished(int currentPage) {
            	//System.out.println("currentPage : " + currentPage);
            	
            }

			@Override
			public void snapToPage(int whichPage) {
	            	if(whichPage  == 1){
	            		System.out.println("====snapTOPage 1====" + mCurMonth);
	            		 mListHeader.setTag(tagTimeList);
	            		 mllBottomInfo.setVisibility(View.GONE);
	            		 mlvMonthNotes.setAdapter(new LocalNoteAdapter(mContext, ServiceManager
									.getDbManager().queryMonthLocalNOTES(mCurMonth, mCurYear)));
	            	} else {
	            		System.out.println("====snapTOPage 0====" + mCurMonth);
	            		 mListHeader.setTag(tagCalendar);
	            		 mllBottomInfo.setVisibility(View.VISIBLE);
	            		 showCalendarMonth(NEXT_MONTH);
	            	}
			}
        });*/
        
        eventService.add(this);
        
        flipper = (ViewFlipper) findViewById(R.id.flipper);
        flipper.removeAllViews();

        tvCalendarWeekday0 = (TextView)findViewById(R.id.tv_calendar_weekday0);
        tvCalendarWeekday1 = (TextView)findViewById(R.id.tv_calendar_weekday1);
        tvCalendarWeekday2 = (TextView)findViewById(R.id.tv_calendar_weekday2);
        tvCalendarWeekday3 = (TextView)findViewById(R.id.tv_calendar_weekday3);
        tvCalendarWeekday4 = (TextView)findViewById(R.id.tv_calendar_weekday4);
        tvCalendarWeekday5 = (TextView)findViewById(R.id.tv_calendar_weekday5);
        tvCalendarWeekday6 = (TextView)findViewById(R.id.tv_calendar_weekday6);
        
       //mAllNotesCursor = ServiceManager.getDbManager().queryLocalNotes(HomeScreen.USRID);
        
        mbtnBackRecent = (Button)findViewById(R.id.btn_back_recent);
        mbtnBackRecent.setOnClickListener(this);
    }
    @Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		System.out.println("onWindowFocusChanged flipper height : " + flipper.getHeight() + " mCurMonth : " + mCurMonth);
        if(flipper.getHeight() != 0){
        	calendarHeight = flipper.getHeight();
        }
		if(Constant.flagType == 1){
			tvCalendarWeekday0.setText(R.string.calendar_mon);
			tvCalendarWeekday1.setText(R.string.calendar_tue);
			tvCalendarWeekday2.setText(R.string.calendar_wed);
			tvCalendarWeekday3.setText(R.string.calendar_thu);
			tvCalendarWeekday4.setText(R.string.calendar_fri);
			tvCalendarWeekday5.setText(R.string.calendar_sat);
			tvCalendarWeekday6.setText(R.string.calendar_sun);
		}else{
			tvCalendarWeekday1.setText(R.string.calendar_mon);
			tvCalendarWeekday2.setText(R.string.calendar_tue);
			tvCalendarWeekday3.setText(R.string.calendar_wed);
			tvCalendarWeekday4.setText(R.string.calendar_thu);
			tvCalendarWeekday5.setText(R.string.calendar_fri);
			tvCalendarWeekday6.setText(R.string.calendar_sat);
			tvCalendarWeekday0.setText(R.string.calendar_sun);
		}
	
        if(isFirst){
		 if(NoteApplication.networkIsOk && NoteApplication.IS_AUTO_UPDATE) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					Looper.prepare();
					DownloadApkHelper downloadApk = new DownloadApkHelper(HomeScreen.this, Looper.myLooper());
					downloadApk.updateApk(DownloadApkHelper.AUTO_UPDATE, null);
					Looper.loop();
				}
			}).start();
		}
		}		

		if(isFirst || flipper.getChildCount()==0){
			showCalendarMonth(NEXT_MONTH, false);
			isFirst = false;
		}
		
	    if(mllCalendarPage.getVisibility() == View.VISIBLE && mListHeader.getTag().equals(tagTimeList)){
	    	Cursor cursor = ServiceManager
					.getDbManager().queryMonthLocalNOTES(mCurMonth, mCurYear);
	    	startManagingCursor(cursor);
	    	mlvMonthNotes.setAdapter(new LocalNoteAdapter(this, cursor));
	    }else if(mllHomePage.getVisibility() == View.VISIBLE){
	    	System.out.println(" before " + DateTimeUtils.time2String("yyyyMMdd", mCurTime));
	    	Cursor cursor = ServiceManager
					.getDbManager().queryTodayLocalNOTEs(mCurTime);
	    	startManagingCursor(cursor);
	    	mlvDayNotes.setAdapter(new LocalNoteOnedayAdapter(this, cursor));

	    	System.out.println(" after");
	    	if(Integer.parseInt(DateTimeUtils.time2String("yyyyMMdd", mCurTime)) != Integer.parseInt(DateTimeUtils.time2String("yyyyMMdd", mRecentTime))){
	    		mbtnBackRecent.setVisibility(View.VISIBLE);
	    	}else{
	    		mbtnBackRecent.setVisibility(View.GONE);
	    	}
	    }
    }
    
	private void addGridView() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		//取得屏幕的宽度和高度
		WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int Width = display.getWidth(); 
        int Height = display.getHeight();
        
        mGridView = new GridView(this);
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mGridView.setNumColumns(7);
        mGridView.setColumnWidth(Width/7 + 1);
        mGridView.setGravity(Gravity.CENTER);
       // mGridView.setSelector(getResources().getDrawable(R.drawable.calendar_item_selector));
        mGridView.setOnItemClickListener(new OnItemClickListener() {
            //gridView中的每一个item的点击事件
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				  //点击任何一个item，得到这个item的日期（排除点击的是周日到周六（点击不响应））
				  int startPosition = mCalendarAdapter.getStartPositon();
				  int endPosition = mCalendarAdapter.getEndPosition();
				  if(startPosition <= position  && position <= endPosition){
					  //System.out.println(mCalendarAdapter.getTimeByClickItem(position));
					  boolean lastIsToday = (mCalendarAdapter.lastClickTime > DateTimeUtils.getToday(Calendar.AM, System.currentTimeMillis()) && mCalendarAdapter.lastClickTime < DateTimeUtils.getToday(Calendar.PM, System.currentTimeMillis()));
					  if(mCalendarAdapter.lastClick != -1 && !lastIsToday){
						  RelativeLayout layout = (RelativeLayout) arg0.getChildAt(mCalendarAdapter.lastClick);
	                	  if(layout != null){
	                		  layout.setBackgroundResource(R.drawable.calendar_background);  
	                	  }
					  }else if(lastIsToday){
						  RelativeLayout layout = (RelativeLayout) arg0.getChildAt(mCalendarAdapter.lastClick);
	                	  if(layout != null){
	                		  layout.setBackgroundResource(R.color.calendar_today);  
	                	  }
					  }
					  ((RelativeLayout)arg1).setBackgroundColor(getResources().getColor(R.color.calendar_selected));
					  if(mCalendarAdapter.lastClick == position && mCalendarAdapter.getNoteInfo(position)!= DatabaseManager.NO_NOTE){
						  HomeScreen.eventService.onUpdateEvent(new EventArgs(
									EventTypes.SHOW_ONEDAY_NOTES).putExtra("time", mCalendarAdapter.getTimeByClickItem(position)));
					  }
					  mCalendarAdapter.lastClick = position;
					  mCalendarAdapter.lastClickTime = mCalendarAdapter.getTimeByClickItem(position);
				  }
			}
		});
        mGridView.setLayoutParams(params);
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		System.out.println(getClass() + ", onresume");
		Cursor localNotes = ServiceManager.getDbManager().queryLocalNotes();
		if(localNotes.getCount()!=0 && localNotes.moveToFirst()){
			Long time = Long.parseLong(localNotes.getString(localNotes.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME)));
			mLastestTime = DateTimeUtils.getToday(Calendar.PM, time);
			Long today = (DateTimeUtils.getToday(Calendar.AM, System.currentTimeMillis()));
			if(time < today){
				Calendar cal = Calendar.getInstance(Locale.CHINA); 
				cal.setTimeInMillis(System.currentTimeMillis());
				int todayIndex = cal.get(Calendar.DAY_OF_YEAR);
				cal.setTimeInMillis(time);
				int lastDayIndex = cal.get(Calendar.DAY_OF_YEAR);
				int noNoteDays = todayIndex - lastDayIndex;
				mTvMyNoteInfo.setText(noNoteDays + "天没有写笔记了哦");
			}else{
				Cursor cursor = ServiceManager.getDbManager().queryTodayLocalNOTEs(System.currentTimeMillis());
		        int count = cursor.getCount();
		        cursor.close();
		        if(count == 0){
		        	mTvMyNoteInfo.setText("今天还没有写笔记哦");
		        }else{
		        	mTvMyNoteInfo.setText("今天写了" + count + "篇笔记");
		        }
				
			}
			if(localNotes.moveToLast()){
				Long t = Long.parseLong(localNotes.getString(localNotes.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME)));
				mEarlistTime = DateTimeUtils.getToday(Calendar.AM, t);
			}else{
				mEarlistTime = System.currentTimeMillis();
			}
		}else{
			mLastestTime = System.currentTimeMillis();
			mEarlistTime = System.currentTimeMillis();
			mTvMyNoteInfo.setText("今天还没有写笔记哦");
		}
		
		if(localNotes.getCount()!= 0 && localNotes.moveToFirst()){
			Long time = Long.parseLong(localNotes.getString(localNotes.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME)));
			long sub = Math.abs(Long.parseLong(localNotes.getString(localNotes.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME))) - System.currentTimeMillis());
			mRecentTime = time;
			while (localNotes.moveToNext()) {
				time = Long.parseLong(localNotes.getString(localNotes.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME)));
				if(Math.abs(time - System.currentTimeMillis()) < sub){
					mRecentTime = time;
				}
			}
			if(mRecentTime ==0){
				mRecentTime = System.currentTimeMillis();
			}
		
		}
		
		localNotes.close();
		if(mllCalendarPage.getVisibility() == View.VISIBLE && mListHeader.getTag().equals(tagCalendar)){
			showCalendarMonth(NEXT_MONTH, false);
		}
	}
	

	@Override
	public boolean onEvent(Object sender, final EventArgs e) {
		// TODO Auto-generated method stub
		switch(e.getType()){
/*			case TITLE_BAR_CALENDER_CLICKED:
				HomeScreen.this.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						mllHomePage.setVisibility(View.GONE);
						mllCalendarPage.setVisibility(View.VISIBLE);
					}});
				break;
			case TITLE_BAR_NOTEBOOK_CLICKED:
				HomeScreen.this.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						mllHomePage.setVisibility(View.VISIBLE);
						mllCalendarPage.setVisibility(View.GONE);
					}});
				break;
			case TITLE_BAR_NOTE_ALBUM_CLICKED:
				HomeScreen.this.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						if(e.getExtra("type").equals(MainScreen.TYPE_NOTE)){
							mllHomePage.setVisibility(View.VISIBLE);
							mllCalendarPage.setVisibility(View.GONE);
						}else{
							mllHomePage.setVisibility(View.GONE);
							mllCalendarPage.setVisibility(View.GONE);
						}
					}});
				break;*/
			case HOMESCREEN_FLING:
				final String tag = mListHeader.getTag().toString();
				HomeScreen.this.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						String directory = e.getExtra("direction").toString();
						if (directory.equals(MainScreen.LEFT)) {
				            //向左滑动
							if(mllCalendarPage.getVisibility() == View.VISIBLE){
								gotoNextMonth();
								Calendar cal = Calendar.getInstance(Locale.CHINA); 
								cal.setTimeInMillis(System.currentTimeMillis());
								//System.out.println("month : " + cal.get(Calendar.MONTH) + ", year : " + cal.get(Calendar.YEAR));
								if(mCurMonth != cal.get(Calendar.MONTH) || mCurYear != cal.get(Calendar.YEAR)){
							    	mBtnBackCurmonth.setVisibility(View.VISIBLE);
							    }else{
							    	mBtnBackCurmonth.setVisibility(View.GONE);
							    }
								if(tag.equals(tagCalendar)){
									System.out.println("====next tagCalendar====" + mCurMonth + ", year : " + mCurYear);
									showCalendarMonth(NEXT_MONTH, true);
								}else{
									System.out.println("====next taglistview====" + mCurMonth);
									Cursor cursor = ServiceManager
											.getDbManager().queryMonthLocalNOTES(mCurMonth, mCurYear);
									startManagingCursor(cursor);
									mlvMonthNotes.setAdapter(new LocalNoteAdapter(mContext, cursor));
								}
							}else if(mllHomePage.getVisibility() == View.VISIBLE){
								long time = DateTimeUtils.getToday(Calendar.PM, mCurTime) + 2000;
								//System.out.println("mCurTime-- " + DateTimeUtils.time2String("yyyyMMdd hh:mm:ss", mCurTime));
								while(time < mLastestTime){
									//System.out.println("time-- " + DateTimeUtils.time2String("yyyyMMdd hh:mm:ss", time));
									Cursor cursor = ServiceManager.getDbManager().queryTodayLocalNOTEs(time);
									startManagingCursor(cursor);
									if(cursor.getCount() == 0){
										time = DateTimeUtils.getTomorrow(Calendar.AM, time) + 2000;
									}else{
										System.out.println("time " + DateTimeUtils.time2String("yyyyMMdd hh:mm:ss", time));
										mCurTime = time;
										mlvDayNotes.setAdapter(new LocalNoteOnedayAdapter(mContext, cursor));																	
										if(Integer.parseInt(DateTimeUtils.time2String("yyyyMMdd", mCurTime)) != Integer.parseInt(DateTimeUtils.time2String("yyyyMMdd", mRecentTime))){
								    		mbtnBackRecent.setVisibility(View.VISIBLE);
								    	}else{
								    		mbtnBackRecent.setVisibility(View.GONE);
								    	}
										MainScreen.eventService.onUpdateEvent(new EventArgs(EventTypes.MAIN_SCREEN_UPDATE_TITLE).putExtra("title", DateTimeUtils.time2String("yyyy.MM.dd", mCurTime)));
										break;
									}
								}									
						    }
						} else {
				            //向右滑动
							if(mllCalendarPage.getVisibility() == View.VISIBLE){
								gotoPreMonth();
								Calendar cal = Calendar.getInstance(Locale.CHINA); 
								cal.setTimeInMillis(System.currentTimeMillis());
								//System.out.println("month : " + cal.get(Calendar.MONTH) + ", year : " + cal.get(Calendar.YEAR));
							    if(mCurMonth != cal.get(Calendar.MONTH) || mCurYear != cal.get(Calendar.YEAR)){
							    	mBtnBackCurmonth.setVisibility(View.VISIBLE);
							    }else{
							    	mBtnBackCurmonth.setVisibility(View.GONE);
							    }
								if(tag.equals(tagCalendar)){
									System.out.println("====pre tagCalendar====" + mCurMonth + ", year : " + mCurYear);
									showCalendarMonth(PRE_MONTH, true);
								}else{
									System.out.println("====pre monthlist====" + mCurMonth);
									Cursor cursor = ServiceManager
											.getDbManager().queryMonthLocalNOTES(mCurMonth, mCurYear);
									startManagingCursor(cursor);
									mlvMonthNotes.setAdapter(new LocalNoteAdapter(mContext, cursor));
								}
							}else if(mllHomePage.getVisibility() == View.VISIBLE){
								long time = DateTimeUtils.getToday(Calendar.AM, mCurTime) - 2000;
								System.out.println("mCurTime-- " + DateTimeUtils.time2String("yyyyMMdd hh:mm:ss", mCurTime));
								System.out.println("mEarlistTime " + DateTimeUtils.time2String("yyyyMMdd hh:mm:ss", mEarlistTime));
								while(time > mEarlistTime){
									System.out.println("time-- " + DateTimeUtils.time2String("yyyyMMdd hh:mm:ss", time));
									Cursor cursor = ServiceManager.getDbManager().queryTodayLocalNOTEs(time);
									startManagingCursor(cursor);
									if(cursor.getCount() == 0){
										time = DateTimeUtils.getYesterday(Calendar.PM, time) - 2000;
										System.out.println("this day has no note");
									}else{
										System.out.println("this day has note");
										mCurTime = time;
										mlvDayNotes.setAdapter(new LocalNoteOnedayAdapter(mContext, cursor));								
										if(Integer.parseInt(DateTimeUtils.time2String("yyyyMMdd", mCurTime)) != Integer.parseInt(DateTimeUtils.time2String("yyyyMMdd", mRecentTime))){
								    		mbtnBackRecent.setVisibility(View.VISIBLE);
								    	}else{
								    		mbtnBackRecent.setVisibility(View.GONE);
								    	}
										MainScreen.eventService.onUpdateEvent(new EventArgs(EventTypes.MAIN_SCREEN_UPDATE_TITLE).putExtra("title", DateTimeUtils.time2String("yyyy.MM.dd", mCurTime)));
										break;
									}
								}		
							}
						}
					}});
				break;
			case NOTE_INSERT_TO_DATABASE:
				ContentValues contentValues = new ContentValues();
				String noteTitle = (String) e.getExtra("noteTitle");
				long updateTime = (Long)e.getExtra("updateTime");
				String diaryPath = (String) e.getExtra("diaryPath");
				contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,noteTitle);
				contentValues.put(DatabaseHelper.COLUMN_NOTE_USER_ID, 1000);
				contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME, MainScreen.snoteCreateTime);
				contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT_SIGNED, 1);
				contentValues.put(DatabaseHelper.COLUMN_NOTE_LOCAL_CONTENT, diaryPath);
				contentValues.put(DatabaseHelper.COLUMN_NOTE_UPDATE_TIME, updateTime);
				long id = ServiceManager.getDbManager().insertLocalNotes(contentValues);
			    break;
			case NOTE_UPDATE_TO_DATABASE:
				String noteTitle2 = (String) e.getExtra("noteTitle");
				long updateTime2 = (Long)e.getExtra("updateTime");
				int noteId = (Integer) e.getExtra("noteID");
				
				ContentValues contentValues2 = new ContentValues();
				contentValues2.put(DatabaseHelper.COLUMN_NOTE_TITLE,noteTitle2);
				contentValues2.put(DatabaseHelper.COLUMN_NOTE_UPDATE_TIME, updateTime2);
				ServiceManager.getDbManager().updateLocalNotes(contentValues2, noteId);
				break;
			case SHOW_ONEDAY_NOTES:
				final long time = Long.parseLong(e.getExtra("time").toString());
				mCurTime = time;
				HomeScreen.this.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						mllHomePage.setVisibility(View.VISIBLE);
						mllCalendarPage.setVisibility(View.GONE);
						Cursor cursor = ServiceManager
								.getDbManager().queryTodayLocalNOTEs(time);
						startManagingCursor(cursor);
						mlvDayNotes.setAdapter(new LocalNoteOnedayAdapter(mContext, cursor));
						if(Integer.parseInt(DateTimeUtils.time2String("yyyyMMdd", mCurTime)) != Integer.parseInt(DateTimeUtils.time2String("yyyyMMdd", mRecentTime))){
				    		mbtnBackRecent.setVisibility(View.VISIBLE);
				    	}else{
				    		mbtnBackRecent.setVisibility(View.GONE);
				    	}
						MainScreen.eventService.onUpdateEvent(new EventArgs(EventTypes.SHOW_OR_HIDE_BUTTON_BACK));
						MainScreen.eventService.onUpdateEvent(new EventArgs(EventTypes.MAIN_SCREEN_UPDATE_TITLE).putExtra("title", DateTimeUtils.time2String("yyyy.MM.dd", time)));
					}});
				break;
			case HOME_SCREEN_ONEDAY_NOTE_BACK_PRESSED:
				HomeScreen.this.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						mllHomePage.setVisibility(View.GONE);
						mllCalendarPage.setVisibility(View.VISIBLE);
						showCalendarMonth(NEXT_MONTH, true);
					}});
				break;
		}
		return true;
	}
    
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		int resId = arg0.getId();
		switch(resId){
		/*		case R.id.btn_pre_month:
			gotoPreMonth();			
			tag = mListHeader.getTag().toString();
			if(tag != null){
				if(tag.equals(tagCalendar)){
					System.out.println("====pre tagCalendar onclick====" + mCurMonth);
					showCalendarMonth(PRE_MONTH);
				} else if(tag.equals(tagTimeList)) {
					System.out.println("====pre tagListview onclick====" + mCurMonth);
					mlvMonthNotes.setAdapter(new LocalNoteAdapter(mContext, ServiceManager
							.getDbManager().queryMonthLocalNOTES(mCurMonth, mCurYear)));
				}
			}	
			break;
		case R.id.btn_next_month:
			gotoNextMonth();		
			tag = mListHeader.getTag().toString();
			if(tag != null){
				if(tag.equals(tagCalendar)){
					System.out.println("====next tagCalendar onclick====" + mCurMonth);
					showCalendarMonth(NEXT_MONTH);
				} else if(tag.equals(tagTimeList)) {
					System.out.println("====next tagLIstVIew onclick====" + mCurMonth);
					mlvMonthNotes.setAdapter(new LocalNoteAdapter(mContext, ServiceManager
							.getDbManager().queryMonthLocalNOTES(mCurMonth, mCurYear)));
				}
			}
			break;*/
		case R.id.btn_back_recent:
			Cursor cursor = ServiceManager
					.getDbManager().queryTodayLocalNOTEs(mRecentTime);
			startManagingCursor(cursor);
			mlvDayNotes.setAdapter(new LocalNoteOnedayAdapter(mContext, cursor));
			mCurTime = mRecentTime;
			mbtnBackRecent.setVisibility(View.GONE);
			MainScreen.eventService.onUpdateEvent(new EventArgs(EventTypes.MAIN_SCREEN_UPDATE_TITLE).putExtra("title", DateTimeUtils.time2String("yyyy.MM.dd", mRecentTime)));
			break;
		case R.id.btn_back_curmonth:
			final String tag = mListHeader.getTag().toString();
			Calendar cal = Calendar.getInstance(Locale.CHINA); 
			cal.setTimeInMillis(System.currentTimeMillis());
			//System.out.println("month : " + cal.get(Calendar.MONTH) + ", year : " + cal.get(Calendar.YEAR));
			
			boolean isPre = mCurYear > cal.get(Calendar.YEAR) && mCurMonth > cal.get(Calendar.MONTH);
			
			mCurMonth = cal.get(Calendar.MONTH);
			mCurYear = cal.get(Calendar.YEAR);
			mBtnBackCurmonth.setVisibility(View.GONE);
			mTvCurMonth.setText(DateTimeUtils.time2String("yyyy年MM月", System.currentTimeMillis()));
			if(tag.equals(tagCalendar)){
				if(isPre){
					showCalendarMonth(NEXT_MONTH, true);
				}else{
					showCalendarMonth(PRE_MONTH, true);
				}
			}else{
				Cursor cursor0 = ServiceManager
						.getDbManager().queryMonthLocalNOTES(mCurMonth, mCurYear);
				startManagingCursor(cursor0);
				mlvMonthNotes.setAdapter(new LocalNoteAdapter(mContext, cursor0));
			}
			break;
		default:
			
		}
	}
	
	private void gotoPreMonth(){
		System.out.println("====gotoPreMonth===="+ mCurMonth);
		if(mCurMonth == Calendar.JANUARY){
			mCurMonth = Calendar.DECEMBER;
			mCurYear = mCurYear -1;
		}else{
			mCurMonth = mCurMonth - 1;
		} 

		Calendar time = Calendar.getInstance(Locale.CHINA);
		time.set(Calendar.YEAR, mCurYear);
		time.set(Calendar.MONTH, mCurMonth);	
		time.set(Calendar.DATE, 15);
		mTvCurMonth.setText(DateTimeUtils.time2String("yyyy年MM月", time.getTimeInMillis()));
	}
	
	private void gotoNextMonth(){
		System.out.println("====gotoNextMonth====" + mCurMonth);
		if(mCurMonth == Calendar.DECEMBER){
			mCurMonth = Calendar.JANUARY;
			mCurYear = mCurYear + 1;
		}else{
			mCurMonth = mCurMonth + 1;
		}
		Calendar time = Calendar.getInstance(Locale.CHINA);
		time.set(Calendar.YEAR, mCurYear);
		time.set(Calendar.MONTH, mCurMonth);	
		time.set(Calendar.DATE, 15);
		mTvCurMonth.setText(DateTimeUtils.time2String("yyyy年MM月", time.getTimeInMillis()));
	}
	
	private void showCalendarMonth(int preORnext, boolean needAnimation){
		System.out.println("====showCalendarMonth====" + mCurMonth + "==" + needAnimation);

	    if(needAnimation){
		    if(preORnext == NEXT_MONTH){
		    	flipper.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.push_left_in));
		    	flipper.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.push_left_out));
		    }else{
		    	flipper.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.push_right_in));
				flipper.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.push_right_out));
		    }
	    }else{
	    	flipper.setInAnimation(null);
	    	flipper.setOutAnimation(null);
	    }
		
		addGridView();   //添加一个gridview
		mCalendarAdapter = new CalendarAdapter(this, getResources(), mCurYear, mCurMonth,calendarHeight, Constant.flagType);
	    mGridView.setAdapter(mCalendarAdapter);
	    
	    //flipper.removeAllViews();
	    //System.out.println(flipper.getChildCount() + "~~~~~~~1~~~~~~" + flipper.getDisplayedChild());
	    flipper.addView(mGridView);
        

	    
		flipper.setDisplayedChild(flipper.getChildCount()-1);
		//System.out.println(flipper.getChildCount() + "~~~~~~~2~~~~~~"+ flipper.getDisplayedChild());
		if(!isFirst){
			for(int i=0; i< flipper.getChildCount()-1; i++){
				flipper.removeViewAt(i);
			}
		}
		
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(mllHomePage.getVisibility() == View.VISIBLE){
			mllHomePage.setVisibility(View.GONE);
			mllCalendarPage.setVisibility(View.VISIBLE);
			showCalendarMonth(NEXT_MONTH, false);
			MainScreen.eventService.onUpdateEvent(new EventArgs(EventTypes.SHOW_OR_HIDE_BUTTON_BACK));
			MainScreen.eventService.onUpdateEvent(new EventArgs(EventTypes.MAIN_SCREEN_UPDATE_TITLE).putExtra("title", mContext.getResources().getString(R.string.home_screen_title)));
		}else{
			super.onBackPressed();
		}
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		//System.out.println("homescreen ondispatchKeyEvent");
		if(mllHomePage.getVisibility() == View.VISIBLE && event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
			System.out.println("homescreen ondispatchKeyEvent000");
			this.onBackPressed();
			return true;
		}
        return super.dispatchKeyEvent(event);
	}
	
	public static int isSubPage(){
		return mllHomePage.getVisibility();
	}
}

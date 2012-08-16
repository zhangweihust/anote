package com.archermind.note.Screens;


import java.util.Calendar;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

import com.archermind.note.R;
import com.archermind.note.Adapter.CalendarAdapter;
import com.archermind.note.Adapter.LocalNoteAdapter;
import com.archermind.note.Adapter.LocalNoteOnedayAdapter;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Events.EventTypes;
import com.archermind.note.Events.IEventHandler;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Services.EventService;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.Constant;
import com.archermind.note.Utils.DateTimeUtils;
import com.archermind.note.Utils.DensityUtil;
import com.archermind.note.Views.VerticalScrollView;


public class HomeScreen extends Screen  implements IEventHandler, OnClickListener{
    /** Called when the activity is first created. */
	private VerticalScrollView mllCalendarPage;
	private static LinearLayout mllHomePage;
	private LinearLayout mListHeader;
	private LinearLayout mllBottomInfo;
	private ImageView mIvMyNoteInfo;
	private TextView mTvNoNoteDays;
	private TextView mTvNoteCountToday;
	private static String tagCalendar = "calendar";
	private static String tagTimeList = "timelist";
	private Button mBtnPreMonth;
	private Button mBtnNextMonth;
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
	private long mRecentTime = 0;
	private long mEarlistTime = 0;
	
	//calendar
	private ViewFlipper flipper = null;
	private CalendarAdapter mCalendarAdapter = null;
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
	
	public static int USRID = 1000;
	
	public static final EventService eventService = ServiceManager.getEventservice();

	public HomeScreen(){
		super();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        
/*        if(ServiceManager.getDbManager().queryLocalNotes(HomeScreen.USRID).getCount() == 0){
        	insert();
        }*/
        
        mContext = HomeScreen.this;
       
        mllHomePage = (LinearLayout)findViewById(R.id.ll_home_page);
        mllCalendarPage = (VerticalScrollView)findViewById(R.id.ll_calendar_page);
        
        mlvMonthNotes = (ListView) findViewById(R.id.lv_month_note_list);
        
        mlvDayNotes = (ListView) findViewById(R.id.lv_day_note_list);        
        mlvDayNotes.setDividerHeight(DensityUtil.dip2px(mContext, 5));
        
        mListHeader = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.home_screen_listview_header, null);
        mllBottomInfo = (LinearLayout)mListHeader.findViewById(R.id.ll_bottom_info);
    	mIvMyNoteInfo = (ImageView)mListHeader.findViewById(R.id.iv_my_note_info);
    	mTvNoNoteDays = (TextView)mListHeader.findViewById(R.id.tv_no_note_days);
        mTvNoteCountToday = (TextView)mListHeader.findViewById(R.id.tv_note_count_today);
        mTvCurMonth = (TextView)mListHeader.findViewById(R.id.tv_cur_month);
        
        
        mBtnPreMonth = (Button)mListHeader.findViewById(R.id.btn_pre_month);
        mBtnPreMonth.setOnClickListener(this);

        mBtnNextMonth = (Button)mListHeader.findViewById(R.id.btn_next_month);
        mBtnNextMonth.setOnClickListener(this);
        
        mListHeader.setTag(tagCalendar);
        mListHeader.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String tag = mListHeader.getTag().toString();
				if(tag != null){
					if(tag.equals(tagCalendar)){
						mllCalendarPage.snapToPage(1);
						mllBottomInfo.setVisibility(View.GONE);
						mListHeader.setTag(tagTimeList);
	            		//mllBottomInfo.setVisibility(View.GONE);
	            		mlvMonthNotes.setAdapter(new LocalNoteAdapter(mContext, ServiceManager
									.getDbManager().queryMonthLocalNOTES(HomeScreen.USRID, mCurMonth, mCurYear)));
					} else if(tag.equals(tagTimeList)) {
						mllCalendarPage.snapToPage(0);
						// mllBottomInfo.setVisibility(View.VISIBLE);
						 mListHeader.setTag(tagCalendar);
	            		 mllBottomInfo.setVisibility(View.VISIBLE);
	            		 showCalendarMonth(NEXT_MONTH);
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
		mlvMonthNotes.setAdapter(new LocalNoteAdapter(this, ServiceManager
				.getDbManager().queryMonthLocalNOTES(HomeScreen.USRID, mCurMonth, mCurYear)));
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
									.getDbManager().queryMonthLocalNOTES(HomeScreen.USRID, mCurMonth, mCurYear)));
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
		
		mCalendarAdapter = new CalendarAdapter(this, getResources(), mCurYear, mCurMonth, flipper.getHeight(), Constant.flagType);
		addGridView();
	    mGridView.setAdapter(mCalendarAdapter);
	    flipper.removeAllViews();
	    flipper.addView(mGridView,0);
	    flipper.setDisplayedChild(0);

	    if(mllCalendarPage.getVisibility() == View.VISIBLE && mListHeader.getTag().equals(tagTimeList)){
	    	mlvMonthNotes.setAdapter(new LocalNoteAdapter(this, ServiceManager
				.getDbManager().queryMonthLocalNOTES(HomeScreen.USRID, mCurMonth, mCurYear)));
	    }else if(mllHomePage.getVisibility() == View.VISIBLE){
	    	System.out.println("onWindowFocusChanged before " + DateTimeUtils.time2String("yyyyMMdd", mCurTime));
	    	mlvDayNotes.setAdapter(new LocalNoteOnedayAdapter(this, ServiceManager
				.getDbManager().queryTodayLocalNOTEs(HomeScreen.USRID, mCurTime)));
	    	System.out.println("onWindowFocusChanged after");
	    	if(Integer.parseInt(DateTimeUtils.time2String("yyyyMMdd", mCurTime)) < Integer.parseInt(DateTimeUtils.time2String("yyyyMMdd", mRecentTime))){
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
/*        mGridView.setOnItemClickListener(new OnItemClickListener() {
            //gridView中的每一个item的点击事件
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				  //点击任何一个item，得到这个item的日期（排除点击的是周日到周六（点击不响应））
				  int startPosition = mCalendarAdapter.getStartPositon();
				  int endPosition = mCalendarAdapter.getEndPosition();
				  if(startPosition <= position  && position <= endPosition){
					  System.out.println(mCalendarAdapter.getDateByClickItem(position));
				  }
			}
		});*/
        mGridView.setLayoutParams(params);
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		System.out.println("homescreen onresume");
		Cursor localNotes = ServiceManager.getDbManager().queryLocalNotes(HomeScreen.USRID);
		if(localNotes.moveToFirst()){
			Long time = Long.parseLong(localNotes.getString(localNotes.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME)));
			mRecentTime = DateTimeUtils.getToday(Calendar.PM, time);
			Long today = (DateTimeUtils.getToday(Calendar.AM, System.currentTimeMillis()));
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
		        int count = ServiceManager.getDbManager().queryTodayLocalNOTEs(HomeScreen.USRID, System.currentTimeMillis()).getCount();
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
			if(localNotes.moveToLast()){
				Long t = Long.parseLong(localNotes.getString(localNotes.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME)));
				mEarlistTime = DateTimeUtils.getToday(Calendar.AM, t);
			}else{
				mEarlistTime = System.currentTimeMillis();
			}
		}else{
			mRecentTime = System.currentTimeMillis();
			mEarlistTime = System.currentTimeMillis();
        	mIvMyNoteInfo.setImageResource(R.drawable.no_note_today);
        	mTvNoteCountToday.setVisibility(View.GONE);
        	mTvNoNoteDays.setVisibility(View.GONE);
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
								if(tag.equals(tagCalendar)){
									System.out.println("====next tagCalendar====" + mCurMonth);
									showCalendarMonth(NEXT_MONTH);
								}else{
									System.out.println("====next taglistview====" + mCurMonth);
									mlvMonthNotes.setAdapter(new LocalNoteAdapter(mContext, ServiceManager
											.getDbManager().queryMonthLocalNOTES(HomeScreen.USRID, mCurMonth, mCurYear)));
								}
							}else if(mllHomePage.getVisibility() == View.VISIBLE){
								long time = DateTimeUtils.getToday(Calendar.PM, mCurTime) + 2000;
								//System.out.println("mCurTime-- " + DateTimeUtils.time2String("yyyyMMdd hh:mm:ss", mCurTime));
								while(time < mRecentTime){
									//System.out.println("time-- " + DateTimeUtils.time2String("yyyyMMdd hh:mm:ss", time));
									if(ServiceManager.getDbManager().queryTodayLocalNOTEs(HomeScreen.USRID, time).getCount() == 0){
										time = DateTimeUtils.getTomorrow(Calendar.PM, time);
									}else{
										System.out.println("time " + DateTimeUtils.time2String("yyyyMMdd hh:mm:ss", time));
										mCurTime = time;
										mlvDayNotes.setAdapter(new LocalNoteOnedayAdapter(mContext, ServiceManager.getDbManager().queryTodayLocalNOTEs(HomeScreen.USRID, mCurTime)));							
										if(Integer.parseInt(DateTimeUtils.time2String("yyyyMMdd", mCurTime)) < Integer.parseInt(DateTimeUtils.time2String("yyyyMMdd", mRecentTime))){
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
								if(tag.equals(tagCalendar)){
									System.out.println("====pre tagCalendar====" + mCurMonth);
									showCalendarMonth(PRE_MONTH);
								}else{
									System.out.println("====pre monthlist====" + mCurMonth);
									mlvMonthNotes.setAdapter(new LocalNoteAdapter(mContext, ServiceManager
											.getDbManager().queryMonthLocalNOTES(HomeScreen.USRID, mCurMonth, mCurYear)));
								}
							}else if(mllHomePage.getVisibility() == View.VISIBLE){
								long time = DateTimeUtils.getToday(Calendar.AM, mCurTime) - 1000;
								System.out.println("mCurTime-- " + DateTimeUtils.time2String("yyyyMMdd hh:mm:ss", mCurTime));
								while(time > mEarlistTime){
									//System.out.println("time-- " + DateTimeUtils.time2String("yyyyMMdd hh:mm:ss", time));
									if(ServiceManager.getDbManager().queryTodayLocalNOTEs(HomeScreen.USRID, time).getCount() == 0){
										time = DateTimeUtils.getYesterday(Calendar.AM, time);
									}else{
										//System.out.println("time " + DateTimeUtils.time2String("yyyyMMdd hh:mm:ss", time));
										mCurTime = time;
										mlvDayNotes.setAdapter(new LocalNoteOnedayAdapter(mContext, ServiceManager.getDbManager().queryTodayLocalNOTEs(HomeScreen.USRID, mCurTime)));							
								    	if(Integer.parseInt(DateTimeUtils.time2String("yyyyMMdd", mCurTime)) < Integer.parseInt(DateTimeUtils.time2String("yyyyMMdd", mRecentTime))){
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
				ServiceManager.getDbManager().insertLocalNotes(contentValues, System.currentTimeMillis()-30000);
				Log.d("=RRR=","noteTitle = " + noteTitle + " updateTime = " + updateTime + " diaryPath = " + diaryPath + " createTime = " + MainScreen.snoteCreateTime);
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
						mlvDayNotes.setAdapter(new LocalNoteOnedayAdapter(mContext, ServiceManager
								.getDbManager().queryTodayLocalNOTEs(HomeScreen.USRID, time)));
						if(Integer.parseInt(DateTimeUtils.time2String("yyyyMMdd", mCurTime)) < Integer.parseInt(DateTimeUtils.time2String("yyyyMMdd", mRecentTime))){
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
					}});
				break;
		}
		return true;
	}
    
    //插入测试数据
	private void insert() {
		// DateTimeUtils.getDayOfWeek(Calendar.SUNDAY);
		
		
		ContentValues contentValues = new ContentValues();
	
		contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.COLUMN_NOTE_ID,
				10007);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,
				"0626的第1条笔记");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_USER_ID, HomeScreen.USRID);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT, "hello,1");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME, DateTimeUtils.getMonthStart(Calendar.JULY, 2012)-30000);
		ServiceManager.getDbManager().insertLocalNotes(contentValues, DateTimeUtils.getMonthStart(Calendar.JULY, 2012)-30000);
	
		contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.COLUMN_NOTE_ID,
				10008);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,
				"0626的第2条笔记");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_USER_ID, HomeScreen.USRID);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT, "hello,2");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME, DateTimeUtils.getMonthStart(Calendar.JULY, 2012)-25000);
		ServiceManager.getDbManager().insertLocalNotes(contentValues, DateTimeUtils.getMonthStart(Calendar.JULY, 2012)-25000);

		
		contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.COLUMN_NOTE_ID,
				10009);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,
				"0626的第3条笔记");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_USER_ID, HomeScreen.USRID);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT, "hello,3");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME, DateTimeUtils.getMonthStart(Calendar.JULY, 2012)-20000);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT_SIGNED, 1);
		ServiceManager.getDbManager().insertLocalNotes(contentValues, DateTimeUtils.getMonthStart(Calendar.JULY, 2012)-30000);
		
		
		
		contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.COLUMN_NOTE_ID,
				10017);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,
				"26的第1条笔记");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_USER_ID, HomeScreen.USRID);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT, "hello,1");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME, DateTimeUtils.getToday(Calendar.AM, System.currentTimeMillis())-30000);
		ServiceManager.getDbManager().insertLocalNotes(contentValues, DateTimeUtils.getToday(Calendar.AM, System.currentTimeMillis())-30000);
		
		contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.COLUMN_NOTE_ID,
				10018);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,
				"26的第2条笔记");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_USER_ID, HomeScreen.USRID);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT, "hello,2");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME, DateTimeUtils.getToday(Calendar.AM, System.currentTimeMillis())-25000);
		ServiceManager.getDbManager().insertLocalNotes(contentValues, DateTimeUtils.getToday(Calendar.AM, System.currentTimeMillis())-30000);

		contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.COLUMN_NOTE_ID,
				10019);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,
				"26的第3条笔记");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_USER_ID, HomeScreen.USRID);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT, "hello,3");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME, DateTimeUtils.getToday(Calendar.AM, System.currentTimeMillis())-20000);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT_SIGNED, 1);
		ServiceManager.getDbManager().insertLocalNotes(contentValues, DateTimeUtils.getToday(Calendar.AM, System.currentTimeMillis())-30000);
		
		
		contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.COLUMN_NOTE_ID,
				10117);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,
				"27的第1条笔记");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_USER_ID, HomeScreen.USRID);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT, "hello,1");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME, System.currentTimeMillis()-30000);
		ServiceManager.getDbManager().insertLocalNotes(contentValues, System.currentTimeMillis()-30000);
	
		contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.COLUMN_NOTE_ID,
				10118);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,
				"27的第2条笔记");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_USER_ID, HomeScreen.USRID);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT, "hello,2");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME, System.currentTimeMillis()-25000);
		ServiceManager.getDbManager().insertLocalNotes(contentValues, System.currentTimeMillis()-30000);
	
		
		contentValues.put(DatabaseHelper.COLUMN_NOTE_ID,
				10119);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE,
				"27的第3条笔记");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_USER_ID, HomeScreen.USRID);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT, "hello,3");
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME, System.currentTimeMillis()-20000);
		contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT_SIGNED, 1);
		ServiceManager.getDbManager().insertLocalNotes(contentValues, System.currentTimeMillis()-30000);
    }

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		int resId = arg0.getId();
		String tag = null;
		switch(resId){
		case R.id.btn_pre_month:
			gotoPreMonth();			
			tag = mListHeader.getTag().toString();
			if(tag != null){
				if(tag.equals(tagCalendar)){
					System.out.println("====pre tagCalendar onclick====" + mCurMonth);
					showCalendarMonth(PRE_MONTH);
				} else if(tag.equals(tagTimeList)) {
					System.out.println("====pre tagListview onclick====" + mCurMonth);
					mlvMonthNotes.setAdapter(new LocalNoteAdapter(mContext, ServiceManager
							.getDbManager().queryMonthLocalNOTES(HomeScreen.USRID, mCurMonth, mCurYear)));
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
							.getDbManager().queryMonthLocalNOTES(HomeScreen.USRID, mCurMonth, mCurYear)));
				}
			}
			break;
		case R.id.btn_back_recent:
			mlvDayNotes.setAdapter(new LocalNoteOnedayAdapter(mContext, ServiceManager
					.getDbManager().queryTodayLocalNOTEs(HomeScreen.USRID, mRecentTime)));
			mCurTime = mRecentTime;
			mbtnBackRecent.setVisibility(View.GONE);
			MainScreen.eventService.onUpdateEvent(new EventArgs(EventTypes.MAIN_SCREEN_UPDATE_TITLE).putExtra("title", DateTimeUtils.time2String("yyyy.MM.dd", mRecentTime)));
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
	
	private void showCalendarMonth(int preORnext){
		System.out.println("====showCalendarMonth====" + mCurMonth);
		addGridView();   //添加一个gridview
		mCalendarAdapter = new CalendarAdapter(this, getResources(), mCurYear, mCurMonth,flipper.getHeight(), Constant.flagType);
	    mGridView.setAdapter(mCalendarAdapter);
	    
	    flipper.removeAllViews();
	    flipper.addView(mGridView,0);
        
	    if(preORnext == NEXT_MONTH){
	    	flipper.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.push_left_in));
	    	flipper.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.push_left_out));
	    }else{
	    	flipper.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.push_right_in));
			flipper.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.push_right_out));
	    }
	    
		flipper.setDisplayedChild(0);
		
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(mllHomePage.getVisibility() == View.VISIBLE){
			mllHomePage.setVisibility(View.GONE);
			mllCalendarPage.setVisibility(View.VISIBLE);
			MainScreen.eventService.onUpdateEvent(new EventArgs(EventTypes.SHOW_OR_HIDE_BUTTON_BACK));
			MainScreen.eventService.onUpdateEvent(new EventArgs(EventTypes.MAIN_SCREEN_UPDATE_TITLE).putExtra("title", mContext.getResources().getString(R.string.home_screen_title)));
		}else{
			super.onBackPressed();
		}
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		System.out.println("homescreen ondispatchKeyEvent");
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

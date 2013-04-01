package com.android.note.Screens;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

import com.android.note.NoteApplication;
import com.android.note.Adapter.CalendarAdapter;
import com.android.note.Adapter.LocalNoteAdapter;
import com.android.note.Adapter.LocalNoteOnedayAdapter;
import com.android.note.Events.EventArgs;
import com.android.note.Events.EventTypes;
import com.android.note.Events.IEventHandler;
import com.android.note.Provider.DatabaseHelper;
import com.android.note.Provider.DatabaseManager;
import com.android.note.Provider.LunarDatesDatabaseHelper;
import com.android.note.Services.EventService;
import com.android.note.Services.ExceptionService;
import com.android.note.Services.ServiceManager;
import com.android.note.Utils.Constant;
import com.android.note.Utils.DateTimeUtils;
import com.android.note.Utils.DensityUtil;
import com.android.note.Utils.DownloadApkHelper;
import com.android.note.Utils.NetworkUtils;
import com.android.note.Views.VerticalScrollView;
import com.archermind.note.R;

public class HomeScreen extends Screen implements IEventHandler,
		OnClickListener, OnItemClickListener, OnItemLongClickListener {
	/** Called when the activity is first created. */
	private static VerticalScrollView mllCalendarPage;
	private static FrameLayout mflHomePage;
	private static LinearLayout mListHeader;
	private TextView mTvMyNoteInfo;
	private static String tagCalendar = "calendar";
	private static String tagTimeList = "timelist";
	
	private FrameLayout mflMonthView;
	/*
	 * private Button mBtnPreMonth; private Button mBtnNextMonth;
	 */
	private Button mBtnBackCurmonth;
	private static int mCurMonth = 0;
	private static int mCurYear = 0;

	private static long mCurTime = 0;
	/*
	 * private Cursor mAllNotesCursor; public static int mCurPosition = 0;
	 */

	private ListView mlvMonthNotes;
	private Context mContext;
	private TextView mTvCurMonth;

	private ListView mlvDayNotes;
	private Button mbtnBackRecent;
	private long mLastestTime = 0;
	private long mEarlistTime = 0;
	private long mRecentTime = 0;

	private static long mNewNoteTime = -1;

	// calendar
	private ViewFlipper flipper = null;
	private CalendarAdapter mCalendarAdapter = null;
	private GridView mCalendarGridView0 = null;
	private GridView mCalendarGridView1 = null;

	public static int PRE_MONTH = 0;
	public static int NEXT_MONTH = 1;

	private TextView tvCalendarWeekday0;
	private TextView tvCalendarWeekday1;
	private TextView tvCalendarWeekday2;
	private TextView tvCalendarWeekday3;
	private TextView tvCalendarWeekday4;
	private TextView tvCalendarWeekday5;
	private TextView tvCalendarWeekday6;

	private TextView tvNoNoteCurMonth;
	private ImageView ivTrack;

	private LocalNoteOnedayAdapter mLocalNoteOnedayAdapter = null;
	private LocalNoteAdapter mLocalNoteAdapter = null;
	
	private LayoutAnimationController mControllerLeft;
	private LayoutAnimationController mControllerRight;

	private Cursor mCursor;

	private static boolean isFirst = true;
	private static int calendarHeight = 410;
	public static final EventService eventService = ServiceManager
			.getEventservice();

	private static boolean isClicked = false;

	public HomeScreen() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_screen);

		mContext = HomeScreen.this;

		mflHomePage = (FrameLayout) findViewById(R.id.fl_home_page);
		mllCalendarPage = (VerticalScrollView) findViewById(R.id.ll_calendar_page);
        mflMonthView = (FrameLayout) findViewById(R.id.fl_month_view);
		
		WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int Width = display.getWidth(); 
        //System.out.println("====width : " + Width);
		AnimationSet setLeft = new AnimationSet(false);  
		Animation animationLeft = new TranslateAnimation(Width, 0, 0, 0);    //TranslateAnimation  控制画面平移的动画效果  
		animationLeft.setDuration(300);  
		setLeft.addAnimation(animationLeft);  
		mControllerLeft = new LayoutAnimationController(setLeft, 1);
		
		AnimationSet setRight = new AnimationSet(false);  
		Animation animationRight = new TranslateAnimation(-Width, 0, 0, 0);    //TranslateAnimation  控制画面平移的动画效果  
		animationRight.setDuration(300);  
		setRight.addAnimation(animationRight);
		mControllerRight = new LayoutAnimationController(setRight, 1);
		
		mlvMonthNotes = (ListView) findViewById(R.id.lv_month_note_list);
		mlvMonthNotes.setOnItemClickListener(this);
		mlvMonthNotes.setOnItemLongClickListener(this);

		mlvDayNotes = (ListView) findViewById(R.id.lv_day_note_list);
		mlvDayNotes.setDividerHeight(DensityUtil.dip2px(mContext, 5));
		mlvDayNotes.setOnItemClickListener(this);
		mlvDayNotes.setOnItemLongClickListener(this);

		mListHeader = (LinearLayout) findViewById(R.id.ll_header);
		mTvMyNoteInfo = (TextView) findViewById(R.id.tv_my_note_info);

		mTvCurMonth = (TextView) findViewById(R.id.tv_cur_month);

		mBtnBackCurmonth = (Button)findViewById(R.id.btn_back_curmonth);
		mBtnBackCurmonth.setOnClickListener(this);

		mListHeader.setTag(tagCalendar);
		mListHeader.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String tag = mListHeader.getTag().toString();
				if (tag != null) {
					if (tag.equals(tagCalendar)) {
						mllCalendarPage.snapToPage(1);
						mTvMyNoteInfo.setVisibility(View.GONE);
						mListHeader.setTag(tagTimeList);
						mCursor = ServiceManager.getDbManager()
								.queryMonthLocalNOTES(mCurMonth, mCurYear);
						// startManagingCursor(mCursor);
						mLocalNoteAdapter.changeCursor(mCursor);
						mlvMonthNotes.setAdapter(mLocalNoteAdapter);
						if (mCursor.getCount() == 0) {
							tvNoNoteCurMonth.setVisibility(View.VISIBLE);
							ivTrack.setVisibility(View.GONE);
						} else {
							ivTrack.setVisibility(View.VISIBLE);
							tvNoNoteCurMonth.setVisibility(View.GONE);
						}
					} else if (tag.equals(tagTimeList)) {
						mllCalendarPage.snapToPage(0);
						mListHeader.setTag(tagCalendar);
						mTvMyNoteInfo.setVisibility(View.VISIBLE);
						showCalendarMonth(NEXT_MONTH, false);
					}
				}
			}

		});

		Calendar time = Calendar.getInstance(Locale.CHINA);
		time.setTimeInMillis(System.currentTimeMillis());
		mCurMonth = time.get(Calendar.MONTH);
		mCurYear = time.get(Calendar.YEAR);

		mTvCurMonth.setText(DateTimeUtils.time2String("yyyy年MM月",
				System.currentTimeMillis()));
		mCursor = ServiceManager.getDbManager().queryMonthLocalNOTES(mCurMonth,
				mCurYear);
		// startManagingCursor(mCursor);
		if (mLocalNoteAdapter == null) {
			mLocalNoteAdapter = new LocalNoteAdapter(mContext, mCursor);
		} else {
			mLocalNoteAdapter.changeCursor(mCursor);
		}
		mlvMonthNotes.setAdapter(mLocalNoteAdapter);

		eventService.add(this);

		flipper = (ViewFlipper) findViewById(R.id.flipper);
		flipper.removeAllViews();

		tvCalendarWeekday0 = (TextView) findViewById(R.id.tv_calendar_weekday0);
		tvCalendarWeekday1 = (TextView) findViewById(R.id.tv_calendar_weekday1);
		tvCalendarWeekday2 = (TextView) findViewById(R.id.tv_calendar_weekday2);
		tvCalendarWeekday3 = (TextView) findViewById(R.id.tv_calendar_weekday3);
		tvCalendarWeekday4 = (TextView) findViewById(R.id.tv_calendar_weekday4);
		tvCalendarWeekday5 = (TextView) findViewById(R.id.tv_calendar_weekday5);
		tvCalendarWeekday6 = (TextView) findViewById(R.id.tv_calendar_weekday6);

		mbtnBackRecent = (Button) findViewById(R.id.btn_back_recent);
		mbtnBackRecent.setOnClickListener(this);

		tvNoNoteCurMonth = (TextView) findViewById(R.id.tv_no_note);
		ivTrack = (ImageView) findViewById(R.id.timeline_track);
		ivTrack.setVisibility(View.GONE);

		if (android.os.Build.VERSION.SDK_INT > 8) {
			Typeface type = Typeface.createFromAsset(getAssets(), "xdxwzt.ttf");
			mTvMyNoteInfo.setTypeface(type);
			tvNoNoteCurMonth.setTypeface(type);
		}

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (!hasFocus) {
			return;
		}
	/*	System.out.println("=====onWindowFocusChanged flipper height : "
				+ flipper.getHeight() + " mCurMonth : " + mCurMonth);*/
		int height = flipper.getHeight();
		if (height != 0) {
			calendarHeight = height;
		}
		if (Constant.Firstday == 1) {
			tvCalendarWeekday0.setText(R.string.calendar_mon);
			tvCalendarWeekday1.setText(R.string.calendar_tue);
			tvCalendarWeekday2.setText(R.string.calendar_wed);
			tvCalendarWeekday3.setText(R.string.calendar_thu);
			tvCalendarWeekday4.setText(R.string.calendar_fri);
			tvCalendarWeekday5.setText(R.string.calendar_sat);
			tvCalendarWeekday6.setText(R.string.calendar_sun);
		} else {
			tvCalendarWeekday1.setText(R.string.calendar_mon);
			tvCalendarWeekday2.setText(R.string.calendar_tue);
			tvCalendarWeekday3.setText(R.string.calendar_wed);
			tvCalendarWeekday4.setText(R.string.calendar_thu);
			tvCalendarWeekday5.setText(R.string.calendar_fri);
			tvCalendarWeekday6.setText(R.string.calendar_sat);
			tvCalendarWeekday0.setText(R.string.calendar_sun);
		}

		if (isFirst) {
			if (NetworkUtils.getNetworkState(HomeScreen.this) != NetworkUtils.NETWORN_NONE
					&& NoteApplication.IS_AUTO_UPDATE) {
				DownloadApkHelper downloadApk = new DownloadApkHelper(mContext);
				downloadApk.checkUpdate();
			}
		}

		if (mllCalendarPage.getVisibility() == View.VISIBLE) {
			if (mListHeader.getTag().equals(tagTimeList)) {
				mCursor = ServiceManager.getDbManager().queryMonthLocalNOTES(
						mCurMonth, mCurYear);
				// startManagingCursor(mCursor);
				mLocalNoteAdapter.changeCursor(mCursor);
				mlvMonthNotes.setAdapter(mLocalNoteAdapter);
				if (mCursor.getCount() == 0) {
					tvNoNoteCurMonth.setVisibility(View.VISIBLE);
					ivTrack.setVisibility(View.GONE);
				} else {
					ivTrack.setVisibility(View.VISIBLE);
					tvNoNoteCurMonth.setVisibility(View.GONE);
				}
			} else {
				showCalendarMonth(NEXT_MONTH, false);
			}
			
		} else if (mflHomePage.getVisibility() == View.VISIBLE) {
			/*System.out.println(" before "
					+ DateTimeUtils.time2String("yyyyMMdd", mCurTime));*/
			mCursor = ServiceManager.getDbManager().queryTodayLocalNOTEs(
					mCurTime);
			// startManagingCursor(mCursor);
			mLocalNoteOnedayAdapter.changeCursor(mCursor);
			mlvDayNotes.setAdapter(mLocalNoteOnedayAdapter);

			/*System.out.println(" after");*/
			if (Integer.parseInt(DateTimeUtils
					.time2String("yyyyMMdd", mCurTime)) != Integer
					.parseInt(DateTimeUtils
							.time2String("yyyyMMdd", mRecentTime))) {
				mbtnBackRecent.setVisibility(View.VISIBLE);
			} else {
				mbtnBackRecent.setVisibility(View.GONE);
			}
		}

		if (isFirst) {
			isFirst = false;
		}
		Cursor localNotes = ServiceManager.getDbManager().queryLocalNotes();
		if (localNotes.getCount() != 0 && localNotes.moveToFirst()) {
			Long time = Long.parseLong(localNotes.getString(localNotes
					.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME)));
			mLastestTime = DateTimeUtils.getToday(Calendar.PM, time);
			Long today = (DateTimeUtils.getToday(Calendar.AM,
					System.currentTimeMillis()));
			if (time < today) {
				Calendar cal = Calendar.getInstance(Locale.CHINA);
				cal.setTimeInMillis(System.currentTimeMillis());
				int todayIndex = cal.get(Calendar.DAY_OF_YEAR);
				cal.setTimeInMillis(time);
				int lastDayIndex = cal.get(Calendar.DAY_OF_YEAR);
				int noNoteDays = todayIndex - lastDayIndex;
				mTvMyNoteInfo.setText(noNoteDays + "天没有写笔迹了哦");
			} else {
				Cursor cursor = ServiceManager.getDbManager()
						.queryTodayLocalNOTEs(System.currentTimeMillis());
				int count = cursor.getCount();
				cursor.close();
				if (count == 0) {
					mTvMyNoteInfo.setText("今天还没有写笔迹哦");
				} else {
					mTvMyNoteInfo.setText("今天写了" + count + "篇笔迹");
				}

			}
			if (localNotes.moveToLast()) {
				Long t = Long
						.parseLong(localNotes.getString(localNotes
								.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME)));
				mEarlistTime = DateTimeUtils.getToday(Calendar.AM, t);
			} else {
				mEarlistTime = System.currentTimeMillis();
			}
		} else {
			mLastestTime = System.currentTimeMillis();
			mEarlistTime = System.currentTimeMillis();
			mTvMyNoteInfo.setText("今天还没有写笔迹哦");
		}

		if (localNotes.getCount() != 0 && localNotes.moveToFirst()) {
			Long time = Long.parseLong(localNotes.getString(localNotes
					.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME)));
			long sub = Math.abs(Long.parseLong(localNotes.getString(localNotes
					.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME)))
					- System.currentTimeMillis());
			mRecentTime = time;
			while (localNotes.moveToNext()) {
				time = Long
						.parseLong(localNotes.getString(localNotes
								.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME)));
				long sub1 = Math.abs(time - System.currentTimeMillis());
				if (sub1 < sub) {
					mRecentTime = time;
					sub = sub1;
				}
			}
		}
		localNotes.close();

		mNewNoteTime = System.currentTimeMillis();
	}

	private GridView addGridView() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		// 取得屏幕的宽度和高度
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		int Width = display.getWidth();

		GridView gv = new GridView(this);
		gv.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gv.setNumColumns(7);
		gv.setColumnWidth(Width / 7 + 1);
		gv.setGravity(Gravity.CENTER);
		// mGridView.setSelector(getResources().getDrawable(R.drawable.calendar_item_selector));
		gv.setOnItemClickListener(new OnItemClickListener() {
			// gridView中的每一个item的点击事件
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// 点击任何一个item，得到这个item的日期（排除点击的是周日到周六（点击不响应））
				int startPosition = mCalendarAdapter.getStartPositon();
				int endPosition = mCalendarAdapter.getEndPosition();
				/*System.out.println(mCalendarAdapter.getToday());*/
				DecimalFormat df = new DecimalFormat();
				String style = "00";// 定义要显示的数字的格式
				df.applyPattern(style);//
				String clicking = "" + mCurYear + df.format(mCurMonth)
						+ df.format(position);
				if (startPosition <= position && position <= endPosition) {
					boolean lastIsToday = mCalendarAdapter.getToday().equals(
							mCalendarAdapter.getLastClickPosition());
					if (!lastIsToday) {
						RelativeLayout layout = (RelativeLayout) arg0
								.getChildAt(mCalendarAdapter.getLastClick());
						if (layout != null) {
							layout.setBackgroundResource(R.drawable.calendar_background);
						}
					} else if (lastIsToday) {
						RelativeLayout layout = (RelativeLayout) arg0
								.getChildAt(mCalendarAdapter.getLastClick());
						if (layout != null) {
							layout.setBackgroundResource(R.color.calendar_today);
						}
					}

					((RelativeLayout) arg1).setBackgroundColor(getResources()
							.getColor(R.color.calendar_selected));
					if (mCalendarAdapter.getLastClickPosition()
							.equals(clicking)
							&& mCalendarAdapter.getNoteInfo(position) != DatabaseManager.NO_NOTE) {
						HomeScreen.eventService.onUpdateEvent(new EventArgs(
								EventTypes.SHOW_ONEDAY_NOTES).putExtra("time",
								mCalendarAdapter.getTimeByClickItem(position)));
					}else if(!mCalendarAdapter.getLastClickPosition().endsWith(clicking)){
						if(mCalendarAdapter.getNoteInfo(position)== DatabaseManager.NO_NOTE){
							mTvMyNoteInfo.setText("这一天没有写笔迹哦");
						}else{
							Cursor cursor = ServiceManager.getDbManager()
									.queryTodayLocalNOTEs(mCalendarAdapter.getTimeByClickItem(position));
							int count = cursor.getCount();
							cursor.close();
							if (count == 0) {
								mTvMyNoteInfo.setText("这一天没有写笔迹哦");
							} else {
								mTvMyNoteInfo.setText("这一天写了" + count + "篇笔迹");
							}
						}
					}
					mCalendarAdapter.setLastClick(position);
					mCalendarAdapter.setLastClickPosition(clicking);
					mNewNoteTime = mCalendarAdapter
							.getTimeByClickItem(position);
				}
			}
		});
		gv.setLayoutParams(params);
		return gv;
	}

	@Override
	public boolean onEvent(Object sender, final EventArgs e) {
		// TODO Auto-generated method stub
		switch (e.getType()) {
		case HOMESCREEN_FLING:
			//System.out.println("==== fling =====");
			final String tag = mListHeader.getTag().toString();
			HomeScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					String directory = e.getExtra("direction").toString();
					if (directory.equals(MainScreen.LEFT)) {
						// 向左滑动
						if (mllCalendarPage.getVisibility() == View.VISIBLE) {
							gotoNextMonth();
							Calendar cal = Calendar.getInstance(Locale.CHINA);
							cal.setTimeInMillis(System.currentTimeMillis());
							// System.out.println("month : " +
							// cal.get(Calendar.MONTH) + ", year : " +
							// cal.get(Calendar.YEAR));
							if (mCurMonth != cal.get(Calendar.MONTH)
									|| mCurYear != cal.get(Calendar.YEAR)) {
								mBtnBackCurmonth.setVisibility(View.VISIBLE);
							} else {
								mBtnBackCurmonth.setVisibility(View.GONE);
							}
							if (tag.equals(tagCalendar)) {
								/*System.out.println("====next tagCalendar===="
										+ mCurMonth + ", year : " + mCurYear);*/
								showCalendarMonth(NEXT_MONTH, true);
							} else {
								/*System.out.println("====next taglistview===="
										+ mCurMonth);*/
								mCursor = ServiceManager.getDbManager()
										.queryMonthLocalNOTES(mCurMonth,
												mCurYear);
								// startManagingCursor(mCursor);
								mLocalNoteAdapter.changeCursor(mCursor);
								mlvMonthNotes.setAdapter(mLocalNoteAdapter);
								if (mCursor.getCount() == 0) {
									tvNoNoteCurMonth
											.setVisibility(View.VISIBLE);
									ivTrack.setVisibility(View.GONE);
								} else {
									ivTrack.setVisibility(View.VISIBLE);
									tvNoNoteCurMonth.setVisibility(View.GONE);
								}
								mflMonthView.setLayoutAnimation(mControllerLeft);
							}
						} else if (mflHomePage.getVisibility() == View.VISIBLE) {
							long time = DateTimeUtils.getToday(Calendar.PM,
									mCurTime) + 2000;
							boolean flag = false;
							// System.out.println("mCurTime-- " +
							// DateTimeUtils.time2String("yyyyMMdd hh:mm:ss",
							// mCurTime));
							while (time < mLastestTime) {
								mCursor = ServiceManager.getDbManager()
										.queryTodayLocalNOTEs(time);
								// startManagingCursor(mCursor);
								if (mCursor.getCount() == 0) {
									time = DateTimeUtils.getTomorrow(
											Calendar.AM, time) + 2000;
								} else {
									/*System.out.println("time "
											+ DateTimeUtils.time2String(
													"yyyyMMdd hh:mm:ss", time));*/
									mCurTime = time;
									mLocalNoteOnedayAdapter
											.changeCursor(mCursor);
									mlvDayNotes
											.setAdapter(mLocalNoteOnedayAdapter);
									mflHomePage.setLayoutAnimation(mControllerLeft);
									if (Integer.parseInt(DateTimeUtils
											.time2String("yyyyMMdd", mCurTime)) != Integer
											.parseInt(DateTimeUtils
													.time2String("yyyyMMdd",
															mRecentTime))) {
										mbtnBackRecent
												.setVisibility(View.VISIBLE);
									} else {
										mbtnBackRecent.setVisibility(View.GONE);
									}
									MainScreen.eventService
											.onUpdateEvent(new EventArgs(
													EventTypes.MAIN_SCREEN_UPDATE_TITLE)
													.putExtra(
															"title",
															DateTimeUtils
																	.time2String(
																			"yyyy.MM.dd",
																			mCurTime)));
									flag = true;
									break;
								}
							}
							if(!flag){
								Toast.makeText(mContext, "已经是最后一天", Toast.LENGTH_SHORT).show();
							}
						}
					} else {
						// 向右滑动
						if (mllCalendarPage.getVisibility() == View.VISIBLE) {
							gotoPreMonth();
							Calendar cal = Calendar.getInstance(Locale.CHINA);
							cal.setTimeInMillis(System.currentTimeMillis());
							// System.out.println("month : " +
							// cal.get(Calendar.MONTH) + ", year : " +
							// cal.get(Calendar.YEAR));
							if (mCurMonth != cal.get(Calendar.MONTH)
									|| mCurYear != cal.get(Calendar.YEAR)) {
								mBtnBackCurmonth.setVisibility(View.VISIBLE);
							} else {
								mBtnBackCurmonth.setVisibility(View.GONE);
							}
							if (tag.equals(tagCalendar)) {
								/*System.out.println("====pre tagCalendar===="
										+ mCurMonth + ", year : " + mCurYear);*/
								showCalendarMonth(PRE_MONTH, true);
							} else {
								/*System.out.println("====pre monthlist===="
										+ mCurMonth);*/
								mCursor = ServiceManager.getDbManager()
										.queryMonthLocalNOTES(mCurMonth,
												mCurYear);
								// //startManagingCursor(mCursor);
								mLocalNoteAdapter.changeCursor(mCursor);
								mlvMonthNotes.setAdapter(mLocalNoteAdapter);
								if (mCursor.getCount() == 0) {
									tvNoNoteCurMonth
											.setVisibility(View.VISIBLE);
									ivTrack.setVisibility(View.GONE);
								} else {
									ivTrack.setVisibility(View.VISIBLE);
									tvNoNoteCurMonth.setVisibility(View.GONE);
								}
								mflMonthView.setLayoutAnimation(mControllerRight);
							}
						} else if (mflHomePage.getVisibility() == View.VISIBLE) {
							long time = DateTimeUtils.getToday(Calendar.AM,
									mCurTime) - 2000;
							/*System.out.println("mCurTime-- "
									+ DateTimeUtils.time2String(
											"yyyyMMdd hh:mm:ss", mCurTime));
							System.out.println("mEarlistTime "
									+ DateTimeUtils.time2String(
											"yyyyMMdd hh:mm:ss", mEarlistTime));*/
							boolean flag = false;
							while (time > mEarlistTime) {
								System.out.println("time-- "
										+ DateTimeUtils.time2String(
												"yyyyMMdd hh:mm:ss", time));
								mCursor = ServiceManager.getDbManager()
										.queryTodayLocalNOTEs(time);
								// //startManagingCursor(mCursor);
								if (mCursor.getCount() == 0) {
									time = DateTimeUtils.getYesterday(
											Calendar.PM, time) - 2000;
								/*	System.out.println("this day has no note");*/
								} else {
									/*System.out.println("this day has note");*/
									mCurTime = time;
									mLocalNoteOnedayAdapter
											.changeCursor(mCursor);
									mlvDayNotes
											.setAdapter(mLocalNoteOnedayAdapter);
									mflHomePage.setLayoutAnimation(mControllerRight);
									if (Integer.parseInt(DateTimeUtils
											.time2String("yyyyMMdd", mCurTime)) != Integer
											.parseInt(DateTimeUtils
													.time2String("yyyyMMdd",
															mRecentTime))) {
										mbtnBackRecent
												.setVisibility(View.VISIBLE);
									} else {
										mbtnBackRecent.setVisibility(View.GONE);
									}
									MainScreen.eventService
											.onUpdateEvent(new EventArgs(
													EventTypes.MAIN_SCREEN_UPDATE_TITLE)
													.putExtra(
															"title",
															DateTimeUtils
																	.time2String(
																			"yyyy.MM.dd",
																			mCurTime)));
									flag = true;
									break;
								}
							}
							
							if(!flag){
								Toast.makeText(mContext, "已经是最前一天", Toast.LENGTH_SHORT).show();
							}
						}
					}
				}
			});
			break;
		/*case NOTE_INSERT_TO_DATABASE:
			ContentValues contentValues = new ContentValues();
			String noteTitle = (String) e.getExtra("noteTitle");
			long updateTime = (Long) e.getExtra("updateTime");
			String diaryPath = (String) e.getExtra("diaryPath");
			contentValues.put(DatabaseHelper.COLUMN_NOTE_TITLE, noteTitle);
			contentValues.put(DatabaseHelper.COLUMN_NOTE_USER_ID, 1000);
			contentValues.put(DatabaseHelper.COLUMN_NOTE_CREATE_TIME,
					MainScreen.snoteCreateTime);
			contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT_SIGNED, 1);
			contentValues.put(DatabaseHelper.COLUMN_NOTE_LOCAL_CONTENT,
					diaryPath);
			contentValues.put(DatabaseHelper.COLUMN_NOTE_UPDATE_TIME,
					updateTime);
			long id = ServiceManager.getDbManager().insertLocalNotes(
					contentValues);
			break;*/
		case NOTE_UPDATE_TO_DATABASE:
			String noteTitle2 = (String) e.getExtra("noteTitle");
			long updateTime2 = (Long) e.getExtra("updateTime");
			int noteId = (Integer) e.getExtra("noteID");

			ContentValues contentValues2 = new ContentValues();
			contentValues2.put(DatabaseHelper.COLUMN_NOTE_TITLE, noteTitle2);
			contentValues2.put(DatabaseHelper.COLUMN_NOTE_UPDATE_TIME,
					updateTime2);
			ServiceManager.getDbManager().updateLocalNotes(contentValues2,
					noteId);
			break;
		case SHOW_ONEDAY_NOTES:
			final long time = Long.parseLong(e.getExtra("time").toString());
			mCurTime = time;
			HomeScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mflHomePage.setVisibility(View.VISIBLE);
					mllCalendarPage.setVisibility(View.GONE);
					mCursor = ServiceManager.getDbManager()
							.queryTodayLocalNOTEs(time);
					// //startManagingCursor(mCursor);
					if (mLocalNoteOnedayAdapter == null) {
						mLocalNoteOnedayAdapter = new LocalNoteOnedayAdapter(
								mContext, mCursor);
					} else {
						mLocalNoteOnedayAdapter.changeCursor(mCursor);
					}
					mlvDayNotes.setAdapter(mLocalNoteOnedayAdapter);
					if (Integer.parseInt(DateTimeUtils.time2String("yyyyMMdd",
							mCurTime)) != Integer.parseInt(DateTimeUtils
							.time2String("yyyyMMdd", mRecentTime))) {
						mbtnBackRecent.setVisibility(View.VISIBLE);
					} else {
						mbtnBackRecent.setVisibility(View.GONE);
					}
					MainScreen.eventService.onUpdateEvent(new EventArgs(
							EventTypes.SHOW_OR_HIDE_BUTTON_BACK).putExtra(
							"isVisible", View.VISIBLE));
					MainScreen.eventService.onUpdateEvent(new EventArgs(
							EventTypes.MAIN_SCREEN_UPDATE_TITLE).putExtra(
							"title",
							DateTimeUtils.time2String("yyyy.MM.dd", time)));
				}
			});
			break;
		case HOME_SCREEN_ONEDAY_NOTE_BACK_PRESSED:
			HomeScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showCalendarMonth(NEXT_MONTH, false);

					mllCalendarPage.setVisibility(View.VISIBLE);	
					mflHomePage.setVisibility(View.GONE);
					MainScreen.eventService.onUpdateEvent(new EventArgs(
							EventTypes.SHOW_OR_HIDE_BUTTON_BACK).putExtra("isVisible",
							View.GONE));
				}
			});
			break;
		}
		return true;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		int resId = arg0.getId();
		switch (resId) {
		case R.id.btn_back_recent:
			mCursor = ServiceManager.getDbManager().queryTodayLocalNOTEs(
					mRecentTime);
			// //startManagingCursor(mCursor);
			mLocalNoteOnedayAdapter.changeCursor(mCursor);
			mlvDayNotes.setAdapter(mLocalNoteOnedayAdapter);
			Calendar calendar = Calendar.getInstance(Locale.CHINA);
			calendar.setTimeInMillis(mCurTime);
			int curYear = calendar.get(Calendar.YEAR);
			int curMonth = calendar.get(Calendar.MONTH);
			int curDay = calendar.get(Calendar.DATE);
			calendar.setTimeInMillis(mRecentTime);
			int recentYear = calendar.get(Calendar.YEAR);
			int recentMonth = calendar.get(Calendar.MONTH);
			int recentDay = calendar.get(Calendar.DATE);
			boolean isNextDay = curYear > recentYear ||( curYear>=recentYear && curMonth > recentMonth) || (curYear>=recentYear && curMonth>=recentMonth && curDay>recentDay);
			if(isNextDay){
				mflHomePage.setLayoutAnimation(mControllerRight);
			}else{
				mflHomePage.setLayoutAnimation(mControllerLeft);
			}
			mCurTime = mRecentTime;
			mbtnBackRecent.setVisibility(View.GONE);
			MainScreen.eventService.onUpdateEvent(new EventArgs(
					EventTypes.MAIN_SCREEN_UPDATE_TITLE).putExtra("title",
					DateTimeUtils.time2String("yyyy.MM.dd", mRecentTime)));
			break;
		case R.id.btn_back_curmonth:
			backToCurMonth();
			break;
		default:

		}
	}

	
	private void backToCurMonth(){
		String tag = mListHeader.getTag().toString();
		Calendar cal = Calendar.getInstance(Locale.CHINA);
		cal.setTimeInMillis(System.currentTimeMillis());
		boolean isPre = mCurYear > cal.get(Calendar.YEAR) || (mCurYear >= cal.get(Calendar.YEAR)&& mCurMonth > cal.get(Calendar.MONTH));

		mCurMonth = cal.get(Calendar.MONTH);
		mCurYear = cal.get(Calendar.YEAR);
		mBtnBackCurmonth.setVisibility(View.GONE);
		mTvCurMonth.setText(DateTimeUtils.time2String("yyyy年MM月",
				System.currentTimeMillis()));
		if (tag.equals(tagCalendar)) {
			if (isPre) {
				showCalendarMonth(PRE_MONTH, true);
			} else {
				showCalendarMonth(NEXT_MONTH, true);
			}
		} else {
			mCursor = ServiceManager.getDbManager().queryMonthLocalNOTES(
					mCurMonth, mCurYear);
			// startManagingCursor(mCursor);
			mLocalNoteAdapter.changeCursor(mCursor);
			mlvMonthNotes.setAdapter(mLocalNoteAdapter);
			if (mCursor.getCount() == 0) {
				tvNoNoteCurMonth.setVisibility(View.VISIBLE);
				ivTrack.setVisibility(View.GONE);
			} else {
				ivTrack.setVisibility(View.VISIBLE);
				tvNoNoteCurMonth.setVisibility(View.GONE);
			}
			if(isPre){
				mflMonthView.setLayoutAnimation(mControllerLeft);
			}else{
				mflMonthView.setLayoutAnimation(mControllerRight);
			}
		}	
	}
	
	private void gotoPreMonth() {
		/*System.out.println("====gotoPreMonth====" + mCurMonth);*/
		if (mCurMonth == Calendar.JANUARY) {
			mCurMonth = Calendar.DECEMBER;
			mCurYear = mCurYear - 1;
		} else {
			mCurMonth = mCurMonth - 1;
		}

		Calendar time = Calendar.getInstance(Locale.CHINA);
		time.set(Calendar.YEAR, mCurYear);
		time.set(Calendar.MONTH, mCurMonth);
		time.set(Calendar.DATE, 15);
		mTvCurMonth.setText(DateTimeUtils.time2String("yyyy年MM月",
				time.getTimeInMillis()));
	}

	private void gotoNextMonth() {
		/*System.out.println("====gotoNextMonth====" + mCurMonth);*/		
		if (mCurMonth == Calendar.DECEMBER) {
			mCurMonth = Calendar.JANUARY;
			mCurYear = mCurYear + 1;
		} else {
			mCurMonth = mCurMonth + 1;
		}
		Calendar time = Calendar.getInstance(Locale.CHINA);
		time.set(Calendar.YEAR, mCurYear);
		time.set(Calendar.MONTH, mCurMonth);
		time.set(Calendar.DATE, 15);
		mTvCurMonth.setText(DateTimeUtils.time2String("yyyy年MM月",
				time.getTimeInMillis()));
	}

	private static int flipper_flag = 1;

	private void showCalendarMonth(int preORnext, boolean needAnimation) {
		/*System.out.println("====showCalendarMonth====" + mCurMonth + "=="
				+ needAnimation);*/
		if(mCurYear < LunarDatesDatabaseHelper.earlistYear || mCurYear > LunarDatesDatabaseHelper.latestYear){
			backToCurMonth();
			return;
		}
		if (needAnimation) {
			if (preORnext == NEXT_MONTH) {
				flipper.setInAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_right_in));
				flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_left_out));
			} else {
				flipper.setInAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_left_in));
				flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_right_out));
			}
		} else {
			flipper.setInAnimation(null);
			flipper.setOutAnimation(null);
		}

		if (mCalendarAdapter == null) {
			mCalendarAdapter = new CalendarAdapter(this, getResources(),
					mCurYear, mCurMonth, calendarHeight);
		} else {
			mCalendarAdapter.changeData(mCurYear, mCurMonth, calendarHeight);
		}

		if (flipper_flag == 1) {
			if (mCalendarGridView0 == null) {
				mCalendarGridView0 = addGridView(); // 添加一个gridview
			}
			mCalendarGridView0.setAdapter(mCalendarAdapter);
			flipper.addView(mCalendarGridView0);
			flipper_flag = 0;
			flipper.setDisplayedChild(flipper.getChildCount() - 1);
			if (!isFirst) {
				flipper.removeView(mCalendarGridView1);
			}
		} else {
			if (mCalendarGridView1 == null) {
				mCalendarGridView1 = addGridView(); // 添加一个gridview
			}
			mCalendarGridView1.setAdapter(mCalendarAdapter);
			flipper.addView(mCalendarGridView1);
			flipper_flag = 1;
			flipper.setDisplayedChild(flipper.getChildCount() - 1);
			if (!isFirst) {
				flipper.removeView(mCalendarGridView0);
			}
		}
	}

	public static long getCurtime() {
		return mCurTime;
	}

	public static long getNewNoteTime() {
		return mNewNoteTime;
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (mflHomePage.getVisibility() == View.VISIBLE) {
			mflHomePage.setVisibility(View.GONE);
			mllCalendarPage.setVisibility(View.VISIBLE);
			showCalendarMonth(NEXT_MONTH, false);
			MainScreen.eventService.onUpdateEvent(new EventArgs(
					EventTypes.SHOW_OR_HIDE_BUTTON_BACK).putExtra("isVisible",
					View.GONE));
			MainScreen.eventService.onUpdateEvent(new EventArgs(
					EventTypes.MAIN_SCREEN_UPDATE_TITLE).putExtra(
					"title",
					mContext.getResources().getString(
							R.string.home_screen_title)));
		} else if(mllCalendarPage.getVisibility() == View.VISIBLE){
			//System.out.println("=========calendar page=====");
			String tag = mListHeader.getTag().toString();
			if (tag != null && tag.equals(tagTimeList)) {
					mllCalendarPage.snapToPage(0);
					mListHeader.setTag(tagCalendar);
					mTvMyNoteInfo.setVisibility(View.VISIBLE);
					showCalendarMonth(NEXT_MONTH, false);
			}
		} else{
			super.onBackPressed();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// System.out.println("homescreen ondispatchKeyEvent");
		if (mflHomePage.getVisibility() == View.VISIBLE
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_UP) {
			//System.out.println("homescreen ondispatchKeyEvent000");
			this.onBackPressed();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	public static boolean isSubPage() {
		boolean isSub = false;
		if(mllCalendarPage.getVisibility() == View.VISIBLE){
			String tag = mListHeader.getTag().toString();
			if (tag != null && tag.equals(tagTimeList)) {
				isSub = true;
				}
		}else if(mflHomePage.getVisibility() == View.VISIBLE){
			isSub = true;
		}
		return isSub;
	}
	
	public static boolean isHomePage(){
		boolean isSub = false;
		if(mflHomePage.getVisibility() == View.VISIBLE){
			isSub = true;
		}
		return isSub;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

		eventService.remove(this);
		if (mLocalNoteAdapter != null && mLocalNoteAdapter.getCursor() != null) {
			mLocalNoteAdapter.getCursor().close();
		}
		if (mLocalNoteOnedayAdapter != null
				&& mLocalNoteOnedayAdapter.getCursor() != null) {
			mLocalNoteOnedayAdapter.getCursor().close();
		}
		super.onDestroy();
	}

	public static void setIsClicked() {
		isClicked = true;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

		//System.out.println("==== click =====");
		if (!isClicked) {
			return;
		}

		String path = null;
		String title = null;
		int id = 0;
		int index = arg2;
		mCursor.moveToPosition(index);
		path = mCursor.getString(mCursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_LOCAL_CONTENT));
		id = mCursor.getInt(mCursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_ID));
		title = mCursor.getString(mCursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_TITLE));

		if (path != null && title != null) {
			Intent intent = new Intent();
			intent.putExtra("notePath", path);
			intent.putExtra("isNewNote", false);
			intent.putExtra("noteID", id);
			intent.putExtra("title", title);
			intent.setClass(MainScreen.mContext, EditNoteScreen.class);
			MainScreen.mContext.startActivity(intent);
		}

		isClicked = false;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		final String notePath = mCursor.getString(mCursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_LOCAL_CONTENT));
		final String title = mCursor.getString(mCursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_TITLE));
		final int id = mCursor.getInt(mCursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_ID));
		final int isSigned = mCursor.getInt(mCursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CONTENT_SIGNED));
		//
		// CharSequence[] items = null;
		// if(isSigned == 1){
		// items = transactItemsUnsign;
		// }else{
		// items = transactItemsSign;
		// }
		// mbuilder.setItems(items, new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int item) {
		// System.out.println(item + "ID : " + id + " isSigned : " + isSigned);
		// dialog.dismiss();
		// switch (item) {
		// case 0:
		// if (!ServiceManager.isLogin()) {
		// Toast.makeText(mContext, R.string.no_login_info,
		// Toast.LENGTH_SHORT).show();
		// Intent intent = new Intent(HomeScreen.this, LoginScreen.class);
		// startActivity(intent);
		// return;
		// }
		// ArrayList<String> picPathList =
		// EditNoteScreen.getNotePictureFromZip(mCursor.getString(mCursor
		// .getColumnIndex(DatabaseHelper.COLUMN_NOTE_LOCAL_CONTENT)));
		// String sid = mCursor.getString((mCursor
		// .getColumnIndex(DatabaseHelper.COLUMN_NOTE_SERVICE_ID)));
		// String action = sid == null ? "A" : "M";
		// Intent intent = new Intent(mContext,
		// ShareScreen.class);
		// intent.putStringArrayListExtra("picpathlist",
		// picPathList);
		// intent.putExtra("noteid", String.valueOf(id));
		// intent.putExtra("title", title);
		// intent.putExtra("action", action);
		// intent.putExtra("sid", sid);
		// mContext.startActivity(intent);
		// break;
		// case 1:
		// ContentValues contentValues = new ContentValues();
		// contentValues = new ContentValues();
		// contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT_SIGNED,isSigned==0);
		// ServiceManager.getDbManager().updateLocalNotes(contentValues, id);
		// break;
		// case 2:
		// ServiceManager.getDbManager().deleteLocalNOTEs(id);
		// if (notePath != null) {
		// File file = new File(notePath);
		// if (file.exists()) {
		// file.delete();
		// }
		// }
		// break;
		// default:
		// break;
		// }
		// }
		// });
		// madTransact = mbuilder.create();
		// madTransact.show();
		final Dialog dialog = new Dialog(this, R.style.CornerDialog);
		dialog.setContentView(R.layout.dialog_note_menu);
		TextView shareView = (TextView) dialog
				.findViewById(R.id.dialog_item_share);
		shareView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (!ServiceManager.isLogin()) {
					Toast.makeText(mContext, R.string.no_login_info,
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(HomeScreen.this,
							LoginScreen.class);
					startActivity(intent);
					return;
				}
				String diaryFilePath = mCursor.getString(mCursor
						.getColumnIndex(DatabaseHelper.COLUMN_NOTE_LOCAL_CONTENT))
						+ ".note";
				ArrayList<String> picPathList = EditNoteScreen
						.getNotePictureFromZip(diaryFilePath);
				picPathList.add(diaryFilePath);//将笔记压缩包的路径也传递过去
				String sid = mCursor.getString((mCursor
						.getColumnIndex(DatabaseHelper.COLUMN_NOTE_SERVICE_ID)));
				int pages = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_PAGES));
				String action = sid == null ? "A" : "M";
				Intent intent = new Intent(mContext, ShareScreen.class);
				intent.putStringArrayListExtra("picpathlist", picPathList);
				intent.putExtra("noteid", String.valueOf(id));
				intent.putExtra("title", title);
				intent.putExtra("action", action);
				intent.putExtra("sid", sid);
				intent.putExtra("pages", pages);
				mContext.startActivity(intent);
			}
		});

		TextView signView = (TextView) dialog
				.findViewById(R.id.dialog_item_sign);
		signView.setText(isSigned == 1 ? R.string.dialog_item_txt_unsign
				: R.string.dialog_item_txt_sign);
		signView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				ContentValues contentValues = new ContentValues();
				contentValues = new ContentValues();
				contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT_SIGNED,
						isSigned == 0);
				ServiceManager.getDbManager().updateLocalNotes(contentValues,
						id);
			}
		});

		TextView deleteView = (TextView) dialog
				.findViewById(R.id.dialog_item_delete);
		deleteView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				dialog.setContentView(R.layout.dialog_ok_cancel);
				TextView titleView = (TextView) dialog
						.findViewById(R.id.dialog_title);
				titleView.setText(R.string.dialog_title_tips);
				TextView msgView = (TextView) dialog
						.findViewById(R.id.dialog_message);
				msgView.setText(R.string.dialog_title_delete);
				Button btn_ok = (Button) dialog
						.findViewById(R.id.dialog_btn_ok);
				btn_ok.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
						ServiceManager.getDbManager().deleteLocalNOTEs(id);
						if (notePath != null) {
							File file = new File(notePath);
							if (file.exists()) {
								file.delete();
							}
						}
					}
				});
				Button btn_cancel = (Button) dialog
						.findViewById(R.id.dialog_btn_cancel);
				btn_cancel.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
		dialog.show();
		return false;
	}

}

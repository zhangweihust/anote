package com.archermind.note.Screens;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Adapter.MenuRightListAdapter;
import com.archermind.note.Adapter.MoreAdapter;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Events.EventTypes;
import com.archermind.note.Events.IEventHandler;
import com.archermind.note.Services.EventService;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.DensityUtil;
import com.archermind.note.Utils.NetworkUtils;
import com.archermind.note.Utils.PreferencesHelper;
import com.archermind.note.Utils.ServerInterface;
import com.archermind.note.Views.MenuRightHorizontalScrollView;

public class MainScreen extends TabActivity implements OnTabChangeListener,
		OnClickListener,OnGestureListener, IEventHandler {
	/** Called when the activity is first created. */
	private TabHost mTabHost;
	private final int INIT_SELECT = 0;
	private final int MENU_RIGHT_WIDTH_DP = 70;
	private int MENU_RIGHT_WIDTH_PX;
	private boolean flag = false;
	private String TAB_HOME = "home";
	private String TAB_PLAZA = "plaza";

/*	private Button mbtnTitleBarCalendar;*/
	private Button mbtnNewNote;
/*	private Button mbtnTitleBarNotebook;*/
	private TextView mtvTitleBarTitle;/*
	private ListView mMenuList;
	private MenuRightHorizontalScrollView mScrollMenu;*/
	
	private Button mbtnMore;
	private Button mbtnBack;
	
	public static String TYPE_NOTE = "note";
	public static String TYPE_CALENDAR = "calender";
	
	public static String RIGHT = "right";
	public static String LEFT = "left";
	
	private static String type;
	
	public static Context mContext;
	
	private ListView mlvSetting;
	private PopupWindow mMorePopupWindow;
	
	public static GestureDetector mGestureDetector = null;
	public static long snoteCreateTime = 0;
	
	public static final EventService eventService = ServiceManager.getEventservice();

	public MainScreen(){
		super();
		mContext = this;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_screen);
		
		
		mTabHost = this.getTabHost();
		mTabHost.addTab(buildTabSpec(TAB_HOME,
				R.drawable.tabhost_home_selector, new Intent(this,
						HomeScreen.class)));
		mTabHost.addTab(buildTabSpec(TAB_PLAZA,
				R.drawable.tabhost_plaza_selector, new Intent(this,
						PlazaScreen.class)));
		mTabHost.setCurrentTab(INIT_SELECT);
		mTabHost.setOnTabChangedListener(this);
		
/*		mbtnTitleBarCalendar = (Button) findViewById(R.id.btn_title_bar_calendar);
		mbtnTitleBarCalendar.setOnClickListener(this);*/

		mbtnNewNote = (Button) findViewById(R.id.btn_new_note);
		mbtnNewNote.setOnClickListener(this);

	/*	mbtnTitleBarNotebook = (Button) findViewById(R.id.btn_title_bar_notebook);
		mbtnTitleBarNotebook.setOnClickListener(this);*/

		mtvTitleBarTitle = (TextView) findViewById(R.id.tv_title_bar_title);

		mbtnMore = (Button)findViewById(R.id.btn_more);
		mbtnMore.setOnClickListener(this);
		
/*		mMenuList = (ListView)findViewById(R.id.menuList);
		mMenuList.setAdapter(new MenuRightListAdapter(this));
		
		mScrollMenu = (MenuRightHorizontalScrollView) findViewById(R.id.scroll_menu);*/
		MENU_RIGHT_WIDTH_PX = (int) (getResources().getDisplayMetrics().density
				* MENU_RIGHT_WIDTH_DP + 0.5f);
		type = TYPE_NOTE;
		
		mGestureDetector = new GestureDetector(this);
		
		mbtnBack = (Button)findViewById(R.id.btn_title_bar_back);
		mbtnBack.setOnClickListener(this);
		eventService.add(this);
		initPopupwindow();
		autoLogin();// 自动登录
	}

	private TabSpec buildTabSpec(String tag, int iconId, Intent intent) {
		View tabSpecView;
		tabSpecView = (LinearLayout) LayoutInflater.from(this).inflate(
				R.layout.tab_item_view, null);
		ImageView icon = (ImageView) tabSpecView.findViewById(R.id.imageview);
		icon.setImageResource(iconId);
		if(iconId == R.drawable.tabhost_home_selector){
			tabSpecView.setPadding(DensityUtil.dip2px(mContext, 20), 0, 0, 0);
		}else{
			tabSpecView.setPadding(DensityUtil.dip2px(mContext, 102), 0, 0, 0);
		}
		TabSpec tabSpec = this.mTabHost.newTabSpec(tag).setIndicator(
				tabSpecView).setContent(intent);
		return tabSpec;
	}

	
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		NoteApplication.LogD(getClass(), "onWindowFocusChanged");
		if (!flag) {
			flag = true;
		}
	}

	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		if (tabId.equalsIgnoreCase(TAB_HOME)) {
			MainScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (type.equals(TYPE_CALENDAR)) {
						mtvTitleBarTitle.setText(MainScreen.this.getResources()
								.getText(R.string.home_screen_calendar_page_title));
					}else{
						mtvTitleBarTitle.setText(MainScreen.this.getResources()
								.getText(R.string.home_screen_title));
					}
				}
			});
		}else {
			MainScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					mtvTitleBarTitle.setText(MainScreen.this.getResources()
							.getText(R.string.plaza_screen_title));

				}
			});
		}

	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
/*		case R.id.btn_title_bar_calendar:
			MainScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					type = TYPE_CALENDAR;
					mbtnTitleBarCalendar.setVisibility(View.GONE);
					mbtnTitleBarNotebook.setVisibility(View.VISIBLE);
					mtvTitleBarTitle.setText(MainScreen.this.getResources()
							.getText(R.string.home_screen_calendar_page_title));
				}
			});
			HomeScreen.eventService.onUpdateEvent(new EventArgs(
					EventTypes.TITLE_BAR_CALENDER_CLICKED));
			break;*/
		case R.id.btn_new_note:
			Intent intent = new Intent();
			intent.setClass(mContext, EditNoteScreen.class);
			intent.putExtra("isNewNote", true);
			mContext.startActivity(intent);
			snoteCreateTime = System.currentTimeMillis();
			break;
		case R.id.btn_title_bar_back:
			MainScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					mbtnBack.setVisibility(View.GONE);
					mtvTitleBarTitle.setText(mContext.getResources().getString(R.string.home_screen_title));
				}
			});
			HomeScreen.eventService.onUpdateEvent(new EventArgs(
					EventTypes.HOME_SCREEN_ONEDAY_NOTE_BACK_PRESSED));
			break;
		case R.id.btn_more:
			MainScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
/*					if (mScrollMenu.MenuOut()) {
						mScrollMenu.scrollBy(-MENU_RIGHT_WIDTH_PX, 0);
						mScrollMenu.MenuOut(false);
					} else {
						mScrollMenu.scrollBy(MENU_RIGHT_WIDTH_PX, 0);
						mScrollMenu.MenuOut(true);
					}*/
					System.out.println("more is clicked");
						if (mMorePopupWindow.isShowing()) {
							mMorePopupWindow.dismiss();
						} else {
							mMorePopupWindow.showAsDropDown(mbtnMore, 0, -3);
						}
					
					
			}
			});
			break;
		default:

		}
	}	

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub 
	    mGestureDetector.onTouchEvent(ev);
	    return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		System.out.println("mainscreen onfling ");
		if(mTabHost.getCurrentTabTag().equalsIgnoreCase(TAB_HOME)){
			 String direction = null;
			if (arg0.getX() - arg1.getX() > 50) {
				direction = this.LEFT;
				HomeScreen.eventService.onUpdateEvent(new EventArgs(
						EventTypes.HOMESCREEN_FLING).putExtra("direction", direction));
			}else if(arg0.getX() - arg1.getX() < -50){
				direction = this.RIGHT;
				HomeScreen.eventService.onUpdateEvent(new EventArgs(
						EventTypes.HOMESCREEN_FLING).putExtra("direction", direction));
			}
			
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void initPopupwindow() {
		
		View view = getLayoutInflater().inflate(R.layout.more_popup_window,
				null);
		mlvSetting = (ListView) view.findViewById(R.id.lv_setting);
		MoreAdapter adapter = new MoreAdapter(mContext);
		mlvSetting.setAdapter(adapter);
		mlvSetting.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_MENU
						|| keyCode == KeyEvent.KEYCODE_BACK) {
					if (mMorePopupWindow.isShowing()
							&& event.getAction() != KeyEvent.ACTION_UP) {
						mMorePopupWindow.dismiss();
					}
				}
				return false;
			}
		});
	mlvSetting.setOnItemClickListener(new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			if(arg2 == 1){
			Intent i = new Intent(MainScreen.this, PreferencesScreen.class);
			mContext.startActivity(i);
			}
		}
	});
	DisplayMetrics dm = new DisplayMetrics();
	dm = mContext.getApplicationContext().getResources().getDisplayMetrics();
	mMorePopupWindow = new PopupWindow(view, DensityUtil.dip2px(mContext, 150),
			(int) (52 * dm.density * 2), true);
	mMorePopupWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.more_pop));
	mMorePopupWindow.setOutsideTouchable(true);
	}

	@Override
	public boolean onEvent(Object sender, final EventArgs e) {
		// TODO Auto-generated method stub
		System.out.println("-----------main onEvent" + e.getType());
		switch (e.getType()) {
		case SHOW_OR_HIDE_BUTTON_BACK:
			MainScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(mbtnBack.getVisibility() != View.VISIBLE){
						mbtnBack.setVisibility(View.VISIBLE);
					}else{
						mbtnBack.setVisibility(View.GONE);
					}
				}
			});
			break;
		case MAIN_SCREEN_UPDATE_TITLE:
			MainScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mtvTitleBarTitle.setText(e.getExtra("title").toString());
				}
			});
			break;
		default:
			break;
		}
		return false;
	}

	private boolean mExit_Flag;// 退出标记
	private long mExit_time = 0; // 第一次点击退出的时间

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		System.out.println("mainscreen ondispatchKeyEvent");
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_UP) {
			if (HomeScreen.isSubPage() == View.VISIBLE) {
				return super.dispatchKeyEvent(event);
			} else if (mExit_Flag
					&& (System.currentTimeMillis() - mExit_time < 3000)) {
				this.finish();
				ServiceManager.exit();
			} else {
				Toast.makeText(this, getString(R.string.exit),
						Toast.LENGTH_SHORT).show();
				mExit_Flag = true;
				mExit_time = System.currentTimeMillis();
			}
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	private void autoLogin() {
		if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {

			new Thread() {

				@Override
				public void run() {
					SharedPreferences sp = PreferencesHelper
							.getSharedPreferences(MainScreen.this, 0);
					String username = sp.getString(
							PreferencesHelper.XML_USER_ACCOUNT, null);
					String password = sp.getString(
							PreferencesHelper.XML_USER_PASSWD, null);
					if (username != null && password != null) {
						String result = ServerInterface.login(username,
								password);
						try {
							JSONObject jsonObject = new JSONObject(result);
							if (jsonObject.optString("flag").equals(
									"" + ServerInterface.SUCCESS)) {
								// 保存至Application
								NoteApplication noteApplication = NoteApplication
										.getInstance();
								noteApplication.setUserName(jsonObject
										.optString("email"));
								noteApplication.setUserId(jsonObject
										.optInt("user_id"));
								noteApplication.setLogin(true);
								Log.i("MainScreen", "autologin success");
							}
						} catch (JSONException e) {
							e.printStackTrace();
							Log.i("MainScreen", "autologin failed");
						}
					}
				}

		}.start();
	}
	}
}

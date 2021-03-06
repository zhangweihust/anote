package com.android.note.Screens;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.android.note.NoteApplication;
import com.android.note.Adapter.MenuRightListAdapter;
import com.android.note.Adapter.MoreAdapter;
import com.android.note.Events.EventArgs;
import com.android.note.Events.EventTypes;
import com.android.note.Events.IEventHandler;
import com.android.note.Services.EventService;
import com.android.note.Services.ExceptionService;
import com.android.note.Services.ServiceManager;
import com.android.note.Task.SendCrashReportsTask;
import com.android.note.Utils.DateTimeUtils;
import com.android.note.Utils.DensityUtil;
import com.android.note.Utils.NetworkUtils;
import com.android.note.Utils.PreferencesHelper;
import com.android.note.Utils.ServerInterface;
import com.android.note.Views.MenuRightHorizontalScrollView;
import com.archermind.note.R;

public class MainScreen extends TabActivity implements OnTabChangeListener,
		OnClickListener, OnGestureListener, IEventHandler {
	/** Called when the activity is first created. */
	private TabHost mTabHost;
	private final int MENU_RIGHT_WIDTH_DP = 70;
	// private int MENU_RIGHT_WIDTH_PX;
	private String TAB_HOME = "home";
	private String TAB_PLAZA = "plaza";

	/* private Button mbtnTitleBarCalendar; */
	private Button mbtnNewNote;
	/* private Button mbtnTitleBarNotebook; */
	private TextView mtvTitleBarTitle;/*
									 * private ListView mMenuList; private
									 * MenuRightHorizontalScrollView
									 * mScrollMenu;
									 */
	private FrameLayout mflTabhost;
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

	private Handler handler;
	public static GestureDetector mGestureDetector = null;
	//public static long snoteCreateTime = 0;
	
	private boolean isPlazaFirst = true;

	public static final EventService eventService = ServiceManager
			.getEventservice();

	public MainScreen() {
		super();
		mContext = this;
		handler = new Handler();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_screen);
		ServiceManager.setTopWindowContext(this);
		ServiceManager.setHandler(handler);
		
		mTabHost = this.getTabHost();
		mTabHost.addTab(buildTabSpec(TAB_HOME,
				R.drawable.tabhost_home_selector, new Intent(this,
						HomeScreen.class)));
		mTabHost.addTab(buildTabSpec(TAB_PLAZA,
				R.drawable.tabhost_plaza_selector, new Intent(this,
						PlazaScreen.class)));
		mTabHost.setCurrentTab(0);
		mTabHost.setOnTabChangedListener(this);

		mbtnNewNote = (Button) findViewById(R.id.btn_new_note);
		mbtnNewNote.setOnClickListener(this);

		mtvTitleBarTitle = (TextView) findViewById(R.id.tv_title_bar_title);

		mbtnMore = (Button) findViewById(R.id.btn_more);
		mbtnMore.setOnClickListener(this);

		type = TYPE_NOTE;

		mGestureDetector = new GestureDetector(this);

		mbtnBack = (Button) findViewById(R.id.btn_title_bar_back);
		mbtnBack.setOnClickListener(this);
		eventService.add(this);
		initPopupwindow();
		autoLogin();// 自动登录
		ServerInterface.initAmtCloud(this);// 初始化云服务
		mflTabhost = (FrameLayout) findViewById(R.id.fl_tabhost);
		if(NetworkUtils.getNetworkState(NoteApplication.getContext()) != NetworkUtils.NETWORN_NONE){
			//System.out.println("======= MainScreen ===========");
			SendCrashReportsTask task = new SendCrashReportsTask();
			task.execute();
		}     
	}

	private TabSpec buildTabSpec(String tag, int iconId, Intent intent) {
		View tabSpecView;
		WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int Width = display.getWidth(); 
        
		tabSpecView = (LinearLayout) LayoutInflater.from(this).inflate(
				R.layout.tab_item_view, null);
		ImageView icon = (ImageView) tabSpecView.findViewById(R.id.imageview);
		icon.setImageResource(iconId);
		//System.out.println("====width : " + Width + ", iconWidth : " + icon.getWidth() + icon.getDrawable().getIntrinsicWidth());
		if (iconId == R.drawable.tabhost_home_selector) {
			tabSpecView.setPadding((int)(Width*0.22*0.5 - icon.getDrawable().getIntrinsicWidth()*0.5), 0, 0, 0);
		} else {
			tabSpecView.setPadding((int)(Width*0.78*0.5 - icon.getDrawable().getIntrinsicWidth()*0.5), 0, 0, 0);
		}
		TabSpec tabSpec = this.mTabHost.newTabSpec(tag)
				.setIndicator(tabSpecView).setContent(intent);
		return tabSpec;
	}

	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		if (tabId.equalsIgnoreCase(TAB_HOME)) {
			MainScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mflTabhost
							.setBackgroundResource(R.drawable.tab_bottom_background_note);
					if (type.equals(TYPE_CALENDAR)) {
						mbtnBack.setVisibility(View.GONE);
						mbtnBack.setText(getResources().getString(R.string.back));
						mtvTitleBarTitle
								.setText(MainScreen.this
										.getResources()
										.getText(
												R.string.home_screen_calendar_page_title));
					} else {
						if (HomeScreen.isHomePage()){
							mbtnBack.setVisibility(View.VISIBLE);
							mbtnBack.setText(getResources().getString(R.string.back));
							mtvTitleBarTitle.setText(DateTimeUtils.time2String("yyyy.MM.dd", HomeScreen.getCurtime()));
						}else{
							mbtnBack.setVisibility(View.GONE);
							mbtnBack.setText(getResources().getString(R.string.back));
							mtvTitleBarTitle.setText(MainScreen.this.getResources()
									.getText(R.string.home_screen_title));
						}
					}
				}
			});
		} else {
			MainScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
				/*	mtvTitleBarTitle.setText(MainScreen.this.getResources()
							.getText(R.string.plaza_screen_title));*/
					try{
					mflTabhost
							.setBackgroundResource(R.drawable.tab_bottom_background_plaza);
					mbtnBack.setVisibility(View.VISIBLE);
					mbtnBack.setText(getResources().getString(R.string.refresh));
					
					String url = ServerInterface.URL_WEBSITE;
					if(getIntent().getExtras()!=null && getIntent().getExtras().containsKey("url")){
						url = getIntent().getExtras().getString("url");
					}
					if(url.equals(ServerInterface.URL_WEBSITE)){
						mtvTitleBarTitle.setText(MainScreen.this.getResources()
								.getText(R.string.plaza_screen_title));
					}
					if(isPlazaFirst){
						isPlazaFirst = false;
						PlazaScreen.eventService.onUpdateEvent(new EventArgs(
								EventTypes.REFRESH_WEBVIEW).putExtra("url", url));
					}
					}catch (OutOfMemoryError e) {
						// TODO: handle exception
					}
				}
			});
		}

	}
	
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//System.out.println("==main screen resumed");
		if(getIntent().getExtras() != null && getIntent().getExtras().containsKey("url")){
			//System.out.println("====== jump to plaza");
			mTabHost.setCurrentTab(1);
			String url = getIntent().getExtras().getString("url");			
			mtvTitleBarTitle.setText(MainScreen.this.getResources()
					.getText(R.string.plaza_screen_title_note));
			PlazaScreen.eventService.onUpdateEvent(new EventArgs(
					EventTypes.REFRESH_WEBVIEW).putExtra("url", url));
			getIntent().removeExtra("url");
		}
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		/*
		 * case R.id.btn_title_bar_calendar: MainScreen.this.runOnUiThread(new
		 * Runnable() {
		 * 
		 * @Override public void run() { // TODO Auto-generated method stub type
		 * = TYPE_CALENDAR; mbtnTitleBarCalendar.setVisibility(View.GONE);
		 * mbtnTitleBarNotebook.setVisibility(View.VISIBLE);
		 * mtvTitleBarTitle.setText(MainScreen.this.getResources()
		 * .getText(R.string.home_screen_calendar_page_title)); } });
		 * HomeScreen.eventService.onUpdateEvent(new EventArgs(
		 * EventTypes.TITLE_BAR_CALENDER_CLICKED)); break;
		 */
		case R.id.btn_new_note:
			Intent intent = new Intent();
			intent.setClass(mContext, EditNoteScreen.class);
			intent.putExtra("isNewNote", true);
			if (HomeScreen.getNewNoteTime() != -1) {
				intent.putExtra("time", HomeScreen.getNewNoteTime());
			}
			mContext.startActivity(intent);
			//snoteCreateTime = System.currentTimeMillis();
			break;
		case R.id.btn_title_bar_back:
			MainScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (mTabHost.getCurrentTabTag() == TAB_HOME) {
						HomeScreen.eventService
								.onUpdateEvent(new EventArgs(
										EventTypes.HOME_SCREEN_ONEDAY_NOTE_BACK_PRESSED));
					} else if (mTabHost.getCurrentTabTag() == TAB_PLAZA) {
						PlazaScreen.eventService.onUpdateEvent(new EventArgs(
								EventTypes.REFRESH_WEBVIEW));
					}
				}
			});
			break;
		case R.id.btn_more:
			MainScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					/*
					 * if (mScrollMenu.MenuOut()) {
					 * mScrollMenu.scrollBy(-MENU_RIGHT_WIDTH_PX, 0);
					 * mScrollMenu.MenuOut(false); } else {
					 * mScrollMenu.scrollBy(MENU_RIGHT_WIDTH_PX, 0);
					 * mScrollMenu.MenuOut(true); }
					 */
					//System.out.println("more is clicked");
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
		//System.out.println("==mainscreen onfling " + (arg0.getX() - arg1.getX()));
		if (mTabHost.getCurrentTabTag().equalsIgnoreCase(TAB_HOME)) {
			String direction = null;
			if (arg0.getX() - arg1.getX() > 130) {
				direction = this.LEFT;
				HomeScreen.eventService.onUpdateEvent(new EventArgs(
						EventTypes.HOMESCREEN_FLING).putExtra("direction",
						direction));
			} else if (arg0.getX() - arg1.getX() < -130) {
				direction = this.RIGHT;
				HomeScreen.eventService.onUpdateEvent(new EventArgs(
						EventTypes.HOMESCREEN_FLING).putExtra("direction",
						direction));
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
		//System.out.println("==mainscreen onScroll ");
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
		//System.out.println("==mainscreen onSingleTapUp ");
		if(mTabHost.getCurrentTabTag().equals(TAB_HOME)){
		 HomeScreen.setIsClicked();
		}
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
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				// TODO Auto-generated method stub
				if (arg2.getKeyCode() == KeyEvent.KEYCODE_MENU
						&& arg2.getAction() == KeyEvent.ACTION_UP
						&& mMorePopupWindow.isShowing()) {
					mMorePopupWindow.dismiss();
				}
				return false;
			}
		});
		mlvSetting.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				mMorePopupWindow.dismiss();
				if (arg2 == 1) {
					Intent i = new Intent(mContext, PreferencesScreen.class);
					mContext.startActivity(i);
				} else if (arg2 == 0) {
					Intent i = new Intent(mContext, InformationScreen.class);
					mContext.startActivity(i);
				}
			}
		});
		DisplayMetrics dm = new DisplayMetrics();
		dm = mContext.getApplicationContext().getResources()
				.getDisplayMetrics();
		mMorePopupWindow = new PopupWindow(view, DensityUtil.dip2px(mContext,
				128), (int) (92 * dm.density ), true);
		mMorePopupWindow.setBackgroundDrawable(mContext.getResources()
				.getDrawable(R.drawable.more_pop));
		mMorePopupWindow.setOutsideTouchable(true);
	}

	@Override
	public boolean onEvent(Object sender, final EventArgs e) {
		// TODO Auto-generated method stub
		//System.out.println("-----------main onEvent" + e.getType());
		switch (e.getType()) {
		case SHOW_OR_HIDE_BUTTON_BACK:
			MainScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (Integer.parseInt(e.getExtra("isVisible").toString()) == View.VISIBLE) {
						mbtnBack.setVisibility(View.VISIBLE);
					} else {
						mtvTitleBarTitle.setText(mContext.getResources()
								.getString(R.string.home_screen_title));
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
		/*System.out.println("=====mainscreen==dispatchKeyEvent=====");
		System.out.println("mainscreen ondispatchKeyEvent" + event.getKeyCode()
				+ ", " + event.getAction() + ", " + mExit_time + ", "
				+ mExit_Flag);*/
		// System.out.println(mTabHost.getCurrentTabTag() +
		// " , PlazaScreen.isFirstPage : " + PlazaScreen.isFirstPage);
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_UP) {
			if ((mTabHost.getCurrentTabTag().equalsIgnoreCase(TAB_HOME) && HomeScreen.isSubPage())
					|| (mTabHost.getCurrentTabTag().equalsIgnoreCase(TAB_PLAZA) && !PlazaScreen.isFirstPage)) {
				return super.dispatchKeyEvent(event);
			} else if (mExit_Flag
					&& (System.currentTimeMillis() - mExit_time < 3000)) {			
				eventService.remove(this);
				this.finish();
				ServiceManager.exit();
			} else {
				Toast.makeText(this, getString(R.string.exit),
						Toast.LENGTH_SHORT).show();
				mExit_Flag = true;
				mExit_time = System.currentTimeMillis();
			}
			return true;
		} else if (event.getKeyCode() == KeyEvent.KEYCODE_MENU
				&& event.getAction() == KeyEvent.ACTION_UP) {
			//System.out.println("on menu click");

			MainScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (mMorePopupWindow.isShowing()) {
						mMorePopupWindow.dismiss();
					} else {
						mMorePopupWindow.showAsDropDown(mbtnMore, 0, -3);
					}
				}
			});
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	private void autoLogin() {
		if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
			if (PreferencesHelper.getSharedPreferences(this, 0).getBoolean(
					PreferencesHelper.XML_AUTOLOGIN, false)) {
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
									ServiceManager.setUserName(jsonObject
											.optString("email"));
									ServiceManager.setUserId(jsonObject
											.optInt("user_id"));
									ServiceManager.setmAvatarurl(jsonObject
											.optString("portrait"));
									ServiceManager.setmNickname(jsonObject
											.optString("nickname"));
									ServiceManager.setmSex(jsonObject
											.optString("gender"));
									ServiceManager.setmRegion(jsonObject
											.optString("region"));
									ServiceManager
											.setmSina_nickname(jsonObject
													.optString("flag_sina"));
									ServiceManager.setmQQ_nickname(jsonObject
											.optString("flag_qq"));
									ServiceManager
											.setmRenren_nickname(jsonObject
													.optString("flag_renren"));
									ServiceManager.setLogin(true);
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
	
	protected void onNewIntent(Intent intent) {
		  super.onNewIntent(intent);
		  setIntent(intent);// must store the new intent unless getIntent() will
		  // return the old one
		  }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		eventService.remove(this);
	}

}

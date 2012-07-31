package com.archermind.note.Screens;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Adapter.MenuRightListAdapter;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Events.EventTypes;
import com.archermind.note.Events.IEventHandler;
import com.archermind.note.Services.EventService;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Views.MenuRightHorizontalScrollView;

public class MainScreen extends TabActivity implements OnTabChangeListener,
		OnClickListener {
	/** Called when the activity is first created. */
	private TabHost mTabHost;
	private final int INIT_SELECT = 0;
	private final int MENU_RIGHT_WIDTH_DP = 70;
	private int MENU_RIGHT_WIDTH_PX;
	private boolean flag = false;
	private String TAB_HOME = "home";
	private String TAB_PLAZA = "plaza";

	private Button mbtnTitleBarCalendar;
	private Button mbtnTitleBarAddMenu;
	private Button mbtnTitleBarNotebook;
	private TextView mtvTitleBarTitle;
	private ListView mMenuList;
	private MenuRightHorizontalScrollView mScrollMenu;
	
	private Button mbtnMore;
	
	public static String TYPE_NOTE = "note";
	public static String TYPE_CALENDAR = "calender";
	private static String type;
	
	private static Context mContext;


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
		
		mbtnTitleBarCalendar = (Button) findViewById(R.id.btn_title_bar_calendar);
		mbtnTitleBarCalendar.setOnClickListener(this);

		mbtnTitleBarAddMenu = (Button) findViewById(R.id.btn_title_bar_add_menu);
		mbtnTitleBarAddMenu.setOnClickListener(this);

		mbtnTitleBarNotebook = (Button) findViewById(R.id.btn_title_bar_notebook);
		mbtnTitleBarNotebook.setOnClickListener(this);

		mtvTitleBarTitle = (TextView) findViewById(R.id.tv_title_bar_title);

		mbtnMore = (Button)findViewById(R.id.btn_more);
		mbtnMore.setOnClickListener(this);
		
		mMenuList = (ListView)findViewById(R.id.menuList);
		mMenuList.setAdapter(new MenuRightListAdapter(this));
		
		mScrollMenu = (MenuRightHorizontalScrollView) findViewById(R.id.scroll_menu);
		MENU_RIGHT_WIDTH_PX = (int) (getResources().getDisplayMetrics().density
				* MENU_RIGHT_WIDTH_DP + 0.5f);
		type = TYPE_NOTE;
	}

	private TabSpec buildTabSpec(String tag, int iconId, Intent intent) {
		View tabSpecView;
		tabSpecView = (LinearLayout) LayoutInflater.from(this).inflate(
				R.layout.tab_item_view, null);
		ImageView icon = (ImageView) tabSpecView.findViewById(R.id.imageview);
		icon.setImageResource(iconId);
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
		case R.id.btn_title_bar_calendar:
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
			break;
		case R.id.btn_title_bar_add_menu:
			Intent intent = new Intent();
			intent.setClass(mContext, EditNoteScreen.class);
			mContext.startActivity(intent);
			break;
		case R.id.btn_title_bar_notebook:
			MainScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					type = TYPE_NOTE;
					mbtnTitleBarCalendar.setVisibility(View.VISIBLE);
					mbtnTitleBarNotebook.setVisibility(View.GONE);
					mtvTitleBarTitle.setText(MainScreen.this.getResources()
							.getText(R.string.home_screen_title));
				}
			});
			HomeScreen.eventService.onUpdateEvent(new EventArgs(
					EventTypes.TITLE_BAR_NOTEBOOK_CLICKED));
			break;
		case R.id.btn_more:
			MainScreen.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (mScrollMenu.MenuOut()) {
						mScrollMenu.scrollBy(-MENU_RIGHT_WIDTH_PX, 0);
						mScrollMenu.MenuOut(false);
					} else {
						mScrollMenu.scrollBy(MENU_RIGHT_WIDTH_PX, 0);
						mScrollMenu.MenuOut(true);
					}
			}
			});
			break;
		default:

		}
	}

}

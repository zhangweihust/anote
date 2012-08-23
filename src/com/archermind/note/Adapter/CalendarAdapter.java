package com.archermind.note.Adapter;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.archermind.note.R;
import com.archermind.note.R.color;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Events.EventTypes;
import com.archermind.note.Provider.DatabaseManager;
import com.archermind.note.Screens.HomeScreen;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.calendar.LunarCalendar;
import com.archermind.note.calendar.SpecialCalendar;

import android.R.integer;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 日历gridview中的每一个item显示的textview
 * @author jack_peng
 *
 */
public class CalendarAdapter extends BaseAdapter {

	private boolean isLeapyear = false;  //是否为闰年
	private int daysOfMonth = 0;      //某月的天数
	private int dayOfWeek = 0;        //具体某一天是星期几
	private int lastDaysOfMonth = 0;  //上一个月的总天数
	private Context context;
	private String[] dayNumber = null;  //一个gridview中的日期存入此数组中
	private LunarCalendar lc = null; 
	private Resources res = null;
	
	private int showYear;
	private int showMonth;
	private int lastMonth;
	private int nextMonth;
	
	private int currentFlag = -1;     //用于标记当天
	private int[] noteFlag = null;  //存储当月所有的日程日期
	
	private String animalsYear = ""; 
	private String leapMonth = "";   //闰哪一个月
	private String cyclical = "";   //天干地支
	//系统当前时间 
	private int sys_year;
	private int sys_month;
	private int sys_day;
	
	private int flipperHeight = 0;
	private int constantFlag = 0;
	
	public int lastClick = -1 ; 
	public long lastClickTime = -1;
	
	public CalendarAdapter(){
		Calendar time = Calendar.getInstance(Locale.CHINA); 
		sys_year = time.get(Calendar.YEAR);
		sys_month = time.get(Calendar.MONTH);
		sys_day = time.get(Calendar.DATE);
		
	}
	
	public CalendarAdapter(Context context,Resources rs,int year, int month, int height, int flagType){
		this();
		this.context= context;
		lc = new LunarCalendar();
		this.res = rs;
		System.out.println("year : " + year + ", month : " + month);
		showYear = year;;  //得到跳转到的年份
		showMonth = month;  //得到跳转到的月份		
		flipperHeight = height;
		constantFlag = flagType;
		getCalendar(showYear, showMonth, flagType);
		
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return dayNumber.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {	
		ViewItem item = null;
		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.calendar_item, null);
			item = new ViewItem();
			item.tvDate = (TextView) convertView.findViewById(R.id.tv_date);
			item.ivHasNote = (ImageView) convertView.findViewById(R.id.iv_has_note);
			convertView.setTag(item);
		 }else{
			 item = (ViewItem)convertView.getTag();
		 }
		
		//System.out.println("dayNumber : " + dayNumber[position]);
		
		if(position < 7){
			item.tvDate.setHeight(flipperHeight/6 + flipperHeight%6);
		}else{
			item.tvDate.setHeight(flipperHeight/6);
		}
		
		String day = dayNumber[position].split("\\.")[0];
		String lunarDay = dayNumber[position].split("\\.")[1];
		String holiday = null;
		int length = dayNumber[position].length();
		if(lunarDay.contains(LunarCalendar.suffix)){
			length -= 1;
			holiday = lunarDay.substring(0, lunarDay.indexOf(LunarCalendar.suffix));
		}else{
			holiday = lunarDay;
		}
		
		SpannableString sp = new SpannableString(day + "\n" + holiday);
		sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, day.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		sp.setSpan(new RelativeSizeSpan(1.2f) , 0, day.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		if(holiday != null || holiday != ""){
            sp.setSpan(new RelativeSizeSpan(0.75f), day.length()+1, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		item.tvDate.setText(sp);
		item.tvDate.setTextColor(res.getColor(R.color.other_month_days_color));
		
		
		if (position < daysOfMonth + dayOfWeek && position >= dayOfWeek) {
			if(constantFlag == 0){
				if(lunarDay.contains(LunarCalendar.suffix) || (position+1)%7 == 0 || (position+1)%7 == 1 ){
					item.tvDate.setTextColor(res.getColor(R.color.holiday_color));
				}else{
					sp.setSpan(new ForegroundColorSpan(Color.BLACK), 0, day.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					sp.setSpan(new ForegroundColorSpan(res.getColor(R.color.lunarday_color)), day.length()+1, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					item.tvDate.setText(sp);
				}
			}else{
				if(lunarDay.contains(LunarCalendar.suffix) || (position+1)%7 == 6 || (position+1)%7 == 0 ){
					item.tvDate.setTextColor(res.getColor(R.color.holiday_color));
				}else{
					sp.setSpan(new ForegroundColorSpan(Color.BLACK), 0, day.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					sp.setSpan(new ForegroundColorSpan(res.getColor(R.color.lunarday_color)), day.length()+1, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					item.tvDate.setText(sp);
				}
			}
			
			if(noteFlag != null && noteFlag.length >0){
					if(noteFlag[position] == DatabaseManager.HAS_NOTE ){
						item.ivHasNote.setVisibility(View.VISIBLE);
						item.ivHasNote.setImageResource(R.drawable.unsigned);
					}else if(noteFlag[position] == DatabaseManager.HAS_SIGNED){
						item.ivHasNote.setVisibility(View.VISIBLE);
						item.ivHasNote.setImageResource(R.drawable.signed);
					}else{
						item.ivHasNote.setVisibility(View.GONE);
					}
			}
			
/*			final int fHasNote = hasNote;
			Calendar time = Calendar.getInstance(Locale.CHINA); 
			time.set(Calendar.YEAR, showYear);
			time.set(Calendar.MONTH, showMonth);
			time.set(Calendar.DATE, Integer.parseInt(day));
			time.set(Calendar.HOUR, 10);
			final long t = time.getTimeInMillis();
			
			item.tvDate.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if(fHasNote == 1){
						System.out.println("time is " + t);
						HomeScreen.eventService.onUpdateEvent(new EventArgs(
								EventTypes.SHOW_ONEDAY_NOTES).putExtra("time", t));
					}else{
						arg0.setBackgroundColor(res.getColor(R.color.calendar_selected));
						lastClick = position;
					}
				}
			});*/

		}

		
		if(currentFlag == position){ 
			//设置当天的背景
			//item.tvDate.setBackgroundResource(R.drawable.calendar_pressed);
			lastClick = position;
			lastClickTime = System.currentTimeMillis();
			convertView.setBackgroundColor(res.getColor(R.color.calendar_today));
		}
		
		return convertView;
	}
	
	//得到某年的某月的天数且这月的第一天是星期几
	public void getCalendar(int year, int month, int flagType){
		isLeapyear = SpecialCalendar.isLeapYear(year);              //是否为闰年
		daysOfMonth = SpecialCalendar.getDaysOfMonth(isLeapyear, month);  //某月的总天数
		dayOfWeek = SpecialCalendar.getWeekdayOfMonth(year, month);      //某月第一天为星期几
		
		if(month == Calendar.JANUARY){
			lastMonth = Calendar.DECEMBER;
			nextMonth = month + 1;
		}else if(month == Calendar.DECEMBER){
			lastMonth = month - 1;
			nextMonth = Calendar.JANUARY;
		}else{
			lastMonth = month - 1;
			nextMonth = month + 1;
		}
		
		lastDaysOfMonth = SpecialCalendar.getDaysOfMonth(isLeapyear, lastMonth);  //上一个月的总天数
		getweek(year, month, flagType);
	}
	
	//将一个月中的每一天的值添加入数组dayNumber中
	private void getweek(int year, int month, int flagType) {
		if(flagType == 1){
			dayOfWeek -= 1;
			if(dayOfWeek < 0){
				dayOfWeek = 6;
			}
		}
		int nextMonthDate = 1; //下个月的日期计数，起始为1号
		String lunarDay = "";
		
		//得到当前月是否有笔记
		Cursor mCursor = ServiceManager.getDbManager().queryMonthLocalNOTES(month, year);		
		if(mCursor.getCount() > 0){
			noteFlag = new int[42];
		}
		
		dayNumber = new String[42];
		
		for (int i = 0; i < dayNumber.length; i++) {
			int k = 1; //因程序计数从0开始，而日期计数从1开始，所以有个差值1			
			if(i < dayOfWeek){  //前一个月
				int day = lastDaysOfMonth - dayOfWeek + k + i;
				lunarDay = lc.getLunarDate(year, lastMonth + k, day,false);
				dayNumber[i] = day + "." + lunarDay;
			}else if(i < daysOfMonth + dayOfWeek){   //本月
				int day = i - dayOfWeek + k;   //得到的日期
				lunarDay = lc.getLunarDate(year, month + k, day,false);
				dayNumber[i] = day +"."+lunarDay;
				//对于当前月才去标记当前日期
				
				if(sys_year == year && sys_month == month && sys_day == day){
					//笔记当前日期
					currentFlag = i;
				}
				Calendar time = Calendar.getInstance(Locale.CHINA); 
				time.set(Calendar.YEAR, year);
				time.set(Calendar.MONTH, month);
				time.set(Calendar.DATE, day);
				time.set(Calendar.HOUR, 10);
				if(noteFlag != null){
					noteFlag[i] = ServiceManager.getDbManager().queryTodayLocalNotesStatus(time.getTimeInMillis());
				}
				
/*				setAnimalsYear(lc.animalsYear(year));
				setLeapMonth(lc.leapMonth == 0?"":String.valueOf(lc.leapMonth));
				setCyclical(lc.cyclical(year));*/
			}else{   //下一个月	
				lunarDay = lc.getLunarDate(year, nextMonth + 1, nextMonthDate,false);
				dayNumber[i] = nextMonthDate + "." + lunarDay;
				nextMonthDate ++;
			}
		}
        
/*       String abc = "";
        for(int i = 0; i < dayNumber.length; i++){
        	 abc = abc+dayNumber[i]+":";
        }
        Log.e("DAYNUMBER",abc);*/


	}
	
	
	/**
	 * 点击每一个item时返回item中的时间
	 * @param position
	 * @return
	 */
	public long getTimeByClickItem(int position){
		String day = dayNumber[position].split("\\.")[0];
		Calendar time = Calendar.getInstance(Locale.CHINA); 
		time.set(Calendar.YEAR, showYear);
		time.set(Calendar.MONTH, showMonth);
		time.set(Calendar.DATE, Integer.parseInt(day));
		time.set(Calendar.HOUR, 10);
		return time.getTimeInMillis();
		
	}
	
	/**
	 * 在点击gridView时，得到这个月中第一天的位置
	 * @return
	 */
	public int getStartPositon(){
		return dayOfWeek;
	}
	
	/**
	 *  在点击gridView时，得到这个月中最后一天的位置
	 * @return
	 */
	public int getEndPosition(){
		return  (dayOfWeek+daysOfMonth)-1;
	}
	
	public int getNoteInfo(int position){
		if(noteFlag != null && noteFlag.length >0){
			return noteFlag[position];
		}
		return DatabaseManager.NO_NOTE;
	}
	
	public String getAnimalsYear() {
		return animalsYear;
	}

	public void setAnimalsYear(String animalsYear) {
		this.animalsYear = animalsYear;
	}
	
	public String getLeapMonth() {
		return leapMonth;
	}

	public void setLeapMonth(String leapMonth) {
		this.leapMonth = leapMonth;
	}
	
	public String getCyclical() {
		return cyclical;
	}

	public void setCyclical(String cyclical) {
		this.cyclical = cyclical;
	}
	
	private class ViewItem{
		private TextView tvDate;
		private ImageView ivHasNote;
	}
}

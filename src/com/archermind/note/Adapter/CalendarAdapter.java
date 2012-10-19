package com.archermind.note.Adapter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.archermind.note.R;
import com.archermind.note.R.color;
import com.archermind.note.R.string;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Events.EventTypes;
import com.archermind.note.Provider.DatabaseManager;
import com.archermind.note.Provider.LunarDatesDatabaseHelper;
import com.archermind.note.Screens.HomeScreen;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.Constant;
import com.archermind.note.Utils.DateTimeUtils;

import android.R.integer;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard.Key;
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
 * 
 * @author jack_peng
 * 
 */
public class CalendarAdapter extends BaseAdapter {

	private int curMonthStart = -1; // 当前月第一天的index
	private int curMonthEnd = -1; // 当前月最后一天的index
	private Context context;
	private String[] dayNumber = null; // 一个gridview中的日期存入此数组中
	private Resources res = null;

	private int[] noteFlag = null; // 存储当月所有的日程日期

	// 系统当前时间
	private int sys_year;
	private int sys_month;
	private int sys_day;
	
	private int cur_year;
	private int cur_month;

	private int flipperHeight = 0;

	private int lastClick = -1;
	private String lastClickPosition = "";
	private String today = "";
	public static String suffix = "_";

	public CalendarAdapter(Context context, Resources rs, int year, int month,
			int height) {
		this.context = context;
		this.res = rs;
		Calendar time = Calendar.getInstance(Locale.CHINA);
		sys_year = time.get(Calendar.YEAR);
		sys_month = time.get(Calendar.MONTH);
		sys_day = time.get(Calendar.DATE);
		cur_year = year;
		cur_month = month;
		flipperHeight = height;
		getCalendar(year, month+1);
		getNoteFlags(year, month);
	}

	public void changeData(int year, int month, int height) {
		flipperHeight = height;
		cur_year = year;
		cur_month = month;
		getCalendar(year, month+1);
		getNoteFlags(year, month);
		notifyDataSetChanged();
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
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.calendar_item, null);
			item = new ViewItem();
			item.tvDate = (TextView) convertView.findViewById(R.id.tv_date);
			item.ivHasNote = (ImageView) convertView
					.findViewById(R.id.iv_has_note);
			convertView.setTag(item);
		} else {
			item = (ViewItem) convertView.getTag();
		}

		// System.out.println("dayNumber : " + dayNumber[position]);

		if (position < 7) {
			item.tvDate.setHeight(flipperHeight / 6 + flipperHeight % 6);
		} else {
			item.tvDate.setHeight(flipperHeight / 6);
		}

		String day = dayNumber[position].split("\\.")[0];
		String lunarDay = dayNumber[position].split("\\.")[1];
		String holiday = null;
		int length = dayNumber[position].length();
		if (lunarDay.contains(suffix)) {
			length -= 1;
			holiday = lunarDay.substring(0,
					lunarDay.indexOf(suffix));
		} else {
			holiday = lunarDay;
		}

		SpannableString sp = new SpannableString(day + "\n" + holiday);
		sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0,
				day.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		sp.setSpan(new RelativeSizeSpan(1.2f), 0, day.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (holiday != null || holiday != "") {
			sp.setSpan(new RelativeSizeSpan(0.75f), day.length() + 1, length,
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		item.tvDate.setText(sp);
		item.tvDate.setTextColor(res.getColor(R.color.other_month_days_color));

		if (position < curMonthEnd && position >= curMonthStart) {
			if (Constant.Firstday == 7) {
				if (lunarDay.contains(suffix)
						|| (position + 1) % 7 == 0 || (position + 1) % 7 == 1) {
					item.tvDate.setTextColor(res
							.getColor(R.color.holiday_color));
				} else {
					sp.setSpan(new ForegroundColorSpan(Color.BLACK), 0,
							day.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					sp.setSpan(
							new ForegroundColorSpan(res
									.getColor(R.color.lunarday_color)), day
									.length() + 1, length,
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					item.tvDate.setText(sp);
				}
			} else {
				if (lunarDay.contains(suffix)
						|| (position + 1) % 7 == 6 || (position + 1) % 7 == 0) {
					item.tvDate.setTextColor(res
							.getColor(R.color.holiday_color));
				} else {
					sp.setSpan(new ForegroundColorSpan(Color.BLACK), 0,
							day.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					sp.setSpan(
							new ForegroundColorSpan(res
									.getColor(R.color.lunarday_color)), day
									.length() + 1, length,
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					item.tvDate.setText(sp);
				}
			}

			if (noteFlag != null && noteFlag.length > 0) {
				if (noteFlag[position] == DatabaseManager.HAS_NOTE) {
					item.ivHasNote.setVisibility(View.VISIBLE);
					item.ivHasNote.setImageResource(R.drawable.unsigned);
				} else if (noteFlag[position] == DatabaseManager.HAS_SIGNED) {
					item.ivHasNote.setVisibility(View.VISIBLE);
					item.ivHasNote.setImageResource(R.drawable.signed);
				} else {
					item.ivHasNote.setVisibility(View.GONE);
				}
			}

		}

		//System.out.println("cur day" + cur_month + " " + cur_year + " " + getTodayPosition(cur_year, cur_month));
		//System.out.println("sys day" + sys_month + " " + sys_year + " " + getTodayPosition(cur_year, cur_month));
		
		if (sys_year == cur_year && sys_month == cur_month
				&& getTodayPosition(cur_year, cur_month) == position) {
			// 设置当天的背景
			// item.tvDate.setBackgroundResource(R.drawable.calendar_pressed);
			DecimalFormat df = new DecimalFormat();
			String style = "00";// 定义要显示的数字的格式
			df.applyPattern(style);//
			lastClickPosition = "" + cur_year + df.format(cur_month)
					+ df.format(position);
			today = "" + cur_year + df.format(cur_month) + df.format(position);
			lastClick = position;
			convertView
					.setBackgroundColor(res.getColor(R.color.calendar_today));
		}

		return convertView;
	}

	// 得到某年的某月的天数且这月的第一天是星期几
	public void getCalendar(int year, int month) {
		System.out.println("getCalendar : " + year + ", " + month);
		Cursor cursorCurMonth = ServiceManager.getDbManager().queryLunarDate(
				year + "." + month);
		if (!cursorCurMonth.moveToFirst()) {
			cursorCurMonth.close();
			return;
		}
		String lunarDate = cursorCurMonth
				.getString(cursorCurMonth
						.getColumnIndex(LunarDatesDatabaseHelper.COLUMN_CALENDAR_LUNARDATE));
		String weekDate = cursorCurMonth
				.getString(cursorCurMonth
						.getColumnIndex(LunarDatesDatabaseHelper.COLUMN_CALENDAR_DAYOFWEEK));
		cursorCurMonth.close();
		if (lunarDate == null || weekDate == null) {
			return;
		}
		lunarDate = lunarDate.substring(0, lunarDate.length() - 1);
		int firstDayWeek = Integer.parseInt(weekDate.substring(0, 1)); // 某月第一天为星期几

		int lastMonth = -1;
		int nextMonth = -1;
		int lastYear = -1;
		int nextYear = -1;
		if (month == Calendar.JANUARY + 1) {
			lastMonth = Calendar.DECEMBER + 1;
			nextMonth = month + 1;
			lastYear = year -1;
			nextYear = year;
		} else if (month == Calendar.DECEMBER + 1) {
			lastMonth = month - 1;
			nextMonth = Calendar.JANUARY + 1;
			lastYear = year;
			nextYear = year+1;
		} else {
			lastMonth = month - 1;
			nextMonth = month + 1;
			lastYear = year;
			nextYear = year;
		}
		Cursor cursorLastMonth = ServiceManager.getDbManager().queryLunarDate(
				lastYear + "." + lastMonth);
		if(!cursorLastMonth.moveToFirst()){
			cursorCurMonth.close();
			return;
		}
		String lastLunarDate = cursorLastMonth
				.getString(cursorLastMonth
						.getColumnIndex(LunarDatesDatabaseHelper.COLUMN_CALENDAR_LUNARDATE));
		String lastWeekDate = cursorLastMonth
				.getString(cursorLastMonth
						.getColumnIndex(LunarDatesDatabaseHelper.COLUMN_CALENDAR_DAYOFWEEK));
		cursorLastMonth.close();
		if (lastLunarDate == null || lastWeekDate == null) {
			cursorLastMonth.close();
			return;
		}
		lastLunarDate = lastLunarDate.substring(0, lastLunarDate.length() - 1);

		Cursor cursorNextMonth = ServiceManager.getDbManager().queryLunarDate(
				nextYear + "." + nextMonth);
		System.out.println("" + nextYear + "." + nextMonth + " " + cursorNextMonth.getCount());
		if(!cursorNextMonth.moveToFirst()){
			cursorNextMonth.close();
			return;
		}
		String nextLunarDate = cursorNextMonth
				.getString(cursorNextMonth
						.getColumnIndex(LunarDatesDatabaseHelper.COLUMN_CALENDAR_LUNARDATE));
		String nextWeekDate = cursorNextMonth
				.getString(cursorNextMonth
						.getColumnIndex(LunarDatesDatabaseHelper.COLUMN_CALENDAR_DAYOFWEEK));
		cursorNextMonth.close();
		if (nextLunarDate == null || nextWeekDate == null) {
			return;
		}
		nextLunarDate = nextLunarDate.substring(0, nextLunarDate.length() - 1);


		if (dayNumber == null) {
			dayNumber = new String[42];
		} else {
			for (int i = 0; i < dayNumber.length; i++) {
				dayNumber[i] = "";
			}
		}

		String[] lastLunarDateS = lastLunarDate.split(",");
		String[] lunarDateS = lunarDate.split(",");
		String[] nextLunarDateS = nextLunarDate.split(",");

		int sub = 7 - Constant.Firstday;
		if (firstDayWeek == Constant.Firstday) {
			curMonthStart = 0;			
			int i = 0;
			for (; i < lunarDateS.length; i++) {
				dayNumber[i] = lunarDateS[i];
			}
			curMonthEnd = i;
			int j = 0;
			for (; i < dayNumber.length; i++) {
				dayNumber[i] = nextLunarDateS[j];
				j++;
			}
		} else {
			int i = 0;
			int lastDays = firstDayWeek + sub;
			for (; i < lastDays; i++) {
				dayNumber[i] = lastLunarDateS[lastLunarDateS.length - lastDays
						+ i];
			}
			curMonthStart = i;
			for (int j = 0; j < lunarDateS.length; j++) {
				dayNumber[i] = lunarDateS[j];
				i++;
			}

			curMonthEnd = i;
			
			int k = 0;
			for (; i < dayNumber.length; i++) {
				dayNumber[i] = nextLunarDateS[k];
				k++;
			}

		}
		
	}

	private int getTodayPosition(int year, int month) {
		if (sys_year == year && sys_month == month) {
			for (int i = 0; i < dayNumber.length; i++) {
				if (dayNumber[i].split("\\.")[0].equals("" + sys_day)) {
					// 笔记当前日期
					return i;
				}
			}
		}
		return -1;
	}

	private void getNoteFlags(int year, int month) {
		Cursor cursorNote = ServiceManager.getDbManager().queryMonthLocalNOTES(
				month, year);
		if (noteFlag == null) {
			noteFlag = new int[42];
		} else {
			for (int i = 0; i < noteFlag.length; i++) {
				noteFlag[i] = 0;
			}
		}
		
		if(cursorNote == null || cursorNote.getCount()==0){		
			cursorNote.close();
			return;
		}
		cursorNote.close();
		
		int day=1;
		for(int i=curMonthStart; i< curMonthEnd; i++){
			Calendar time = Calendar.getInstance(Locale.CHINA);
			time.set(Calendar.YEAR, year);
			time.set(Calendar.MONTH, month);
			time.set(Calendar.DATE, day);
			time.set(Calendar.HOUR, 10);
			noteFlag[i] = ServiceManager.getDbManager()
						.queryTodayLocalNotesStatus(time.getTimeInMillis());
			day++;
		}
	}

	/*// 将一个月中的每一天的值添加入数组dayNumber中
	private void getweek(int year, int month, int flagType) {
		if (flagType == 1) {
			dayOfWeek -= 1;
			if (dayOfWeek < 0) {
				dayOfWeek = 6;
			}
		}
		int nextMonthDate = 1; // 下个月的日期计数，起始为1号
		String lunarDay = "";

		// 得到当前月是否有笔记
		Cursor mCursor = ServiceManager.getDbManager().queryMonthLocalNOTES(
				month, year);

		if (noteFlag == null) {
			noteFlag = new int[42];
		} else {
			for (int i = 0; i < noteFlag.length; i++) {
				noteFlag[i] = 0;
			}
		}

		mCursor.close();
		if (dayNumber == null) {
			dayNumber = new String[42];
		} else {
			for (int i = 0; i < dayNumber.length; i++) {
				dayNumber[i] = "";
			}
		}

		for (int i = 0; i < dayNumber.length; i++) {
			int k = 1; // 因程序计数从0开始，而日期计数从1开始，所以有个差值1
			if (i < dayOfWeek) { // 前一个月
				int day = lastDaysOfMonth - dayOfWeek + k + i;
				lunarDay = lc.getLunarDate(year, lastMonth + k, day, false);
				dayNumber[i] = day + "." + lunarDay;
			} else if (i < daysOfMonth + dayOfWeek) { // 本月
				int day = i - dayOfWeek + k; // 得到的日期
				lunarDay = lc.getLunarDate(year, month + k, day, false);
				dayNumber[i] = day + "." + lunarDay;
				// 对于当前月才去标记当前日期

				if (sys_year == year && sys_month == month && sys_day == day) {
					// 笔记当前日期
					currentFlag = i;
				}
				Calendar time = Calendar.getInstance(Locale.CHINA);
				time.set(Calendar.YEAR, year);
				time.set(Calendar.MONTH, month);
				time.set(Calendar.DATE, day);
				time.set(Calendar.HOUR, 10);
				if (noteFlag != null) {
					noteFlag[i] = ServiceManager.getDbManager()
							.queryTodayLocalNotesStatus(time.getTimeInMillis());
				}

				setAnimalsYear(lc.animalsYear(year));
				setLeapMonth(lc.leapMonth == 0 ? "" : String
						.valueOf(lc.leapMonth));
				setCyclical(lc.cyclical(year));
			} else { // 下一个月
				lunarDay = lc.getLunarDate(year, nextMonth + 1, nextMonthDate,
						false);
				dayNumber[i] = nextMonthDate + "." + lunarDay;
				nextMonthDate++;
			}
		}

		String abc = "";
		for (int i = 0; i < dayNumber.length; i++) {
			abc = abc + dayNumber[i] + ":";
		}
		Log.e("DAYNUMBER", abc);

	}
*/
	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		lastClick = -1;
		lastClickPosition = "";
		super.notifyDataSetChanged();
	}

	/**
	 * 点击每一个item时返回item中的时间
	 * 
	 * @param position
	 * @return
	 */
	public long getTimeByClickItem(int position) {
		String day = dayNumber[position].split("\\.")[0];
		Calendar time = Calendar.getInstance(Locale.CHINA);
		time.set(Calendar.YEAR, cur_year);
		time.set(Calendar.MONTH, cur_month);
		time.set(Calendar.DATE, Integer.parseInt(day));
		time.set(Calendar.HOUR, 10);
		return time.getTimeInMillis();

	}

	/**
	 * 在点击gridView时，得到这个月中第一天的位置
	 * 
	 * @return
	 */
	public int getStartPositon() {
		return curMonthStart;
	}

	/**
	 * 在点击gridView时，得到这个月中最后一天的位置
	 * 
	 * @return
	 */
	public int getEndPosition() {
		return curMonthEnd;
	}

	public int getNoteInfo(int position) {
		if (noteFlag != null && noteFlag.length > 0) {
			return noteFlag[position];
		}
		return DatabaseManager.NO_NOTE;
	}

	public String getToday() {
		return today;
	}

	public int getLastClick() {
		return lastClick;
	}

	public void setLastClick(int p) {
		lastClick = p;
	}

	public String getLastClickPosition() {
		return lastClickPosition;
	}

	public void setLastClickPosition(String lastP) {
		lastClickPosition = lastP;
	}

	private class ViewItem {
		private TextView tvDate;
		private ImageView ivHasNote;
	}
}

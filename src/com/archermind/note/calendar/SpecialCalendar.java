package com.archermind.note.calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SpecialCalendar {

	// 判断是否为闰年
	public static boolean isLeapYear(int year) {
		if (year % 100 == 0 && year % 400 == 0) {
			return true;
		} else if (year % 100 != 0 && year % 4 == 0) {
			return true;
		}
		return false;
	}

	// 得到某月又多少天
	public static int getDaysOfMonth(boolean isLeapyear, int month) {
		int daysOfMonth = 0; // 某月的天数
		switch (month) {
		case 0:
		case 2:
		case 4:
		case 6:
		case 7:
		case 9:
		case 11:
			daysOfMonth = 31;
			break;
		case 3:
		case 5:
		case 8:
		case 10:
			daysOfMonth = 30;
			break;
		case 1:
			if (isLeapyear) {
				daysOfMonth = 29;
			} else {
				daysOfMonth = 28;
			}
            break;
		}
		return daysOfMonth;
	}

	//指定某年中的某月的第一天是星期几
	public  static int getWeekdayOfMonth(int year, int month) {
		int dayOfWeek = 0;
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, 0);
		dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		return dayOfWeek;
	}

	// 实现给定某日期，判断是星期几
	public static String getWeekDay(int year,int month,int day) {// 必须yyyy-MM-dd
		String date = year+"-"+month+"-"+day;
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdw = new SimpleDateFormat("E");
		Date d = null;
		try {
			d = sd.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return sdw.format(d);
	}
	
	public static String getRealWeekDay(int weekDay){
		String[] week = {"周日","周一","周二","周三","周四","周五","周六"};
		return week[weekDay];
	}

}

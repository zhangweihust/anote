package com.archermind.note.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.archermind.note.NoteApplication;

public class DateTimeUtils {
	public static String time2String(String formatter, long date) {
		Date d = new Date(date);
		SimpleDateFormat dateFormat = new SimpleDateFormat(formatter);
		String dateTime = dateFormat.format(d);
		NoteApplication.LogD(DateTimeUtils.class, "time=" + dateTime);
		return dateTime;
	}
	
	public static long getDayOfWeek(int week, long timeInMillis) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a EEEE"); 
		Calendar time = Calendar.getInstance(Locale.CHINA); 
		time.setTimeInMillis(timeInMillis);
		NoteApplication.LogD(DateTimeUtils.class, "当前时间:"+sdf.format(time.getTime()));
		if(week == Calendar.SUNDAY){
			int nowDay = time.get(Calendar.DAY_OF_MONTH); 
			time.set(Calendar.DATE, nowDay+7);
			time.set(Calendar.HOUR_OF_DAY,23);
			time.set(Calendar.MINUTE,59);
			time.set(Calendar.SECOND,59);
			NoteApplication.LogD(DateTimeUtils.class, "本周日时间:"+sdf.format(time.getTime()));
		} else if(week == Calendar.MONDAY){
			time.set(Calendar.HOUR_OF_DAY,0);
			time.set(Calendar.MINUTE,0);
			time.set(Calendar.SECOND,0);
		}
		time.set(Calendar.DAY_OF_WEEK, week);
		NoteApplication.LogD(DateTimeUtils.class, "时间:"+sdf.format(time.getTime()));
		return time.getTimeInMillis();
	}
	
	public static long getThreeDaysBefore(long timeInMillis) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a EEEE"); 
		Calendar time = Calendar.getInstance(Locale.CHINA); 
		time.setTimeInMillis(timeInMillis);
		NoteApplication.LogD(DateTimeUtils.class, "当前时间:"+sdf.format(time.getTime()));
		int nowDay = time.get(Calendar.DAY_OF_MONTH);
		time.set(Calendar.DATE, nowDay - 3);
		time.set(Calendar.HOUR_OF_DAY,0);
		time.set(Calendar.MINUTE,0);
		time.set(Calendar.SECOND,0);
		NoteApplication.LogD(DateTimeUtils.class, "3天前开始时间:"+sdf.format(time.getTime()));
		return time.getTimeInMillis();
	}
	
	public static long getYesterday(int amORpm, long timeInMillis) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a EEEE"); 
		Calendar time = Calendar.getInstance(Locale.CHINA); 
		time.setTimeInMillis(timeInMillis);
		NoteApplication.LogD(DateTimeUtils.class, "当前时间:"+sdf.format(time.getTime()));
		int nowDay = time.get(Calendar.DAY_OF_MONTH);
		time.set(Calendar.DATE, nowDay - 1);
		if(amORpm == Calendar.AM){
			time.set(Calendar.HOUR_OF_DAY,0);
			time.set(Calendar.MINUTE,0);
			time.set(Calendar.SECOND,0);
			NoteApplication.LogD(DateTimeUtils.class, "今天开始时间:"+sdf.format(time.getTime()));
		} else {
			time.set(Calendar.HOUR_OF_DAY,23);
			time.set(Calendar.MINUTE,59);
			time.set(Calendar.SECOND,59);
			NoteApplication.LogD(DateTimeUtils.class, "今天结束时间:"+sdf.format(time.getTime()));
		}
		return time.getTimeInMillis();
	}
	
	
	public static long getToday(int amORpm, long timeInMillis) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a EEEE"); 
		Calendar time = Calendar.getInstance(Locale.CHINA); 
		time.setTimeInMillis(timeInMillis);
		NoteApplication.LogD(DateTimeUtils.class, "当前时间:"+sdf.format(time.getTime()));
		if(amORpm == Calendar.AM){
			time.set(Calendar.HOUR_OF_DAY,0);
			time.set(Calendar.MINUTE,0);
			time.set(Calendar.SECOND,0);
			NoteApplication.LogD(DateTimeUtils.class, "今天开始时间:"+sdf.format(time.getTime()));
		} else {
			time.set(Calendar.HOUR_OF_DAY,23);
			time.set(Calendar.MINUTE,59);
			time.set(Calendar.SECOND,59);
			NoteApplication.LogD(DateTimeUtils.class, "今天结束时间:"+sdf.format(time.getTime()));
		}
		return time.getTimeInMillis();
	}
	
	public static long getTimeOfOneDay(long firstMillisTime,long secondMillisTime) {
		Calendar time = Calendar.getInstance(Locale.CHINA); 
		
		Calendar time1 = Calendar.getInstance(Locale.CHINA); 
		time1.setTimeInMillis(firstMillisTime);
		
		time.set(Calendar.YEAR, time1.get(Calendar.YEAR));
		time.set(Calendar.MONTH, time1.get(Calendar.MONTH));
		time.set(Calendar.DATE, time1.get(Calendar.DATE));
		
		Calendar time2 = Calendar.getInstance(Locale.CHINA); 
		time2.setTimeInMillis(secondMillisTime);
		
		time.set(Calendar.HOUR_OF_DAY,time2.get(Calendar.HOUR_OF_DAY));
		time.set(Calendar.MINUTE,time2.get(Calendar.MINUTE));
		time.set(Calendar.SECOND,time2.get(Calendar.SECOND));
		return time.getTimeInMillis();
	}
	

	public static long getTomorrow(int amORpm, long timeInMillis) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a EEEE"); 
		Calendar time = Calendar.getInstance(Locale.CHINA); 
		time.setTimeInMillis(timeInMillis);
		NoteApplication.LogD(DateTimeUtils.class, "当前时间:"+sdf.format(time.getTime()));
		int nowDay = time.get(Calendar.DAY_OF_MONTH);
		time.set(Calendar.DATE, nowDay + 1);
		if(amORpm == Calendar.AM){
			time.set(Calendar.HOUR_OF_DAY,0);
			time.set(Calendar.MINUTE,0);
			time.set(Calendar.SECOND,0);
			NoteApplication.LogD(DateTimeUtils.class, "明天开始时间:"+sdf.format(time.getTime()));
		} else {
			time.set(Calendar.HOUR_OF_DAY,23);
			time.set(Calendar.MINUTE,59);
			time.set(Calendar.SECOND,59);
			NoteApplication.LogD(DateTimeUtils.class, "明天结束时间:"+sdf.format(time.getTime()));
		}
		return time.getTimeInMillis();
	}
	
	
	public static long getMonthStart(int month, int year){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a EEEE"); 
		Calendar time = Calendar.getInstance(Locale.CHINA); 
		time.set(Calendar.YEAR, year);
		time.set(Calendar.MONTH, month);
		time.set(Calendar.DATE, 1);
		time.set(Calendar.HOUR_OF_DAY,0);
		time.set(Calendar.MINUTE,0);
		time.set(Calendar.SECOND,0);
		NoteApplication.LogD(DateTimeUtils.class, "本月初:"+sdf.format(time.getTime()));
		return time.getTimeInMillis();
	}
	
}

package com.archermind.note.Utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class ServerInterface {
	
	public static final int SUCCESS = 0;
	
	public static final int ERROR_ACCOUNT_OR_PASSWORD_EMPTY = 1;
	public static final int ERROR_ACCOUNT_EXIST     = 2;
	
	public static final int ERROR_SYNC_FAILED       = 3;
	public static final int ERROR_UPLOAD_FAILED     = 4;
	public static final int ERROR_DATABASE_INTERNAL = 5;
	
	public static final int ERROR_EMAIL_INVALID     = 6;
	public static final int ERROR_PASSWORD_INVALID  = 7;
	
	public static final int ERROR_WEB_ERROR  = 8;
	
	public static final int ERROR_SERVER_N0_REPLY = 100;
	public static final int ERROR_SERVER_INTERNAL = 101;
	public static final int ERROR_SERVER_REFUSE   = 102;
	
	public static final int ERROR_PASSWORD_WRONG   = 201;
	public static final int ERROR_USER_NOT_EXIST   = 202;
	public static final int ERROR_USER_NOT_BIND   = 203;
//	public static DatabaseManager mDatabaseManager= new DatabaseManager(ServerInterfaceActivity.getContext());;

//----------------- 静 态 方 法、工 具 函 数  -----------------
	
	/**************************************
	*   判断Email地址合法性
	*  (32 byte)@(31 byte).(6 byte)
	*  true : 合法
	*  false: 非法
    ***************************************/
	public boolean isEmail(String email){
	   String emailPattern = "[a-zA-Z0-9][a-zA-Z0-9._-]{2,30}[a-zA-Z0-9]@[a-zA-Z0-9]{2,31}.[a-zA-Z0-9]{3,4}"; 
	   boolean result = Pattern.matches(emailPattern, email); 
	   return result;
	}
	
	
	/**************************************
	*   判断密码合法性
	*   长度与复杂度判断,特殊字符过滤
	*  true : 合法
	*  false: 非法
    ***************************************/
	public boolean isPswdValid(String pass){
		if (pass.length() < 6 || (pass.length() > 15) ||
				 !(Pattern.matches(".*[0-9]+.*",pass) && Pattern.matches(".*[a-zA-Z]+.*",pass)))
		{
			return false;
		} 
		else{ 
			return true;
		}
	}
	
	/*
	public String getWeather(){
		return null;
	}*/
//-----------------  帐号注册、登录、管理、绑定  -----------------
	/**************************************
	*   用户注册
	*  return:
	*     0 : success
	*     n : ......
	*       ......
    ***************************************/
//	public int login(String email,String pass){
//		return SUCCESS;
//	}
//	
//	public int register(String email,String pass/*,String imsi,...*/){
//		return SUCCESS;
//	}
//	
//	public int pswdModify(String oldpass,String newpass){
//		return SUCCESS;
//	}
	   //注意有新浪邮箱绑定的操作
	/*用户注册函数
	 * 输入参数：登录的帐号类型（人人，腾讯，sina等等），登录的帐号
	 * 返回值 
       // 0 —— success
       //ERROR_USER_NOT_BIND —— 没有记录，需要绑定
       //ERROR_WEB_ERROR —— 网络异常或其他原因注册失败，让用户重新尝试
	 **/
	public int checkBinding(String type,String username){
		//查询数据库看是否有绑定的帐号，数据库表为登录类型，登录帐号，我们的注册帐号；我们的注册帐号栏如果不为空，则说明已经绑定，否则要进行绑定
		return SUCCESS;
	}
	/*用户注册函数
	 * 输入参数：用户名，用户密码，imsi号，手机号，图像url，用户详细信息，注册时间
	 * 返回值 
       // 0 —— success
       //ERROR_ACCOUNT_OR_PASSWORD_EMPTY —— empty account or pswd
       //ERROR_ACCOUNT_EXIST —— account already exist
	   //ERROR_WEB_ERROR —— 网络异常或其他原因注册失败
	 **/
	public int register(String username,String password,String imsi,String tel,String photo_id,String info,String registtime){
		username =username.replace(" ", "");
		password =password.replace(" ", "");
		
		if(username.length()==0 || password.length()==0 ){           
            return ERROR_ACCOUNT_OR_PASSWORD_EMPTY;//帐号或密码为空
        }
		if(!isEmail(username)){
			return ERROR_EMAIL_INVALID;
		}
		if(!isPswdValid(password)){
			return ERROR_PASSWORD_INVALID;
		}
		Map<String,String> map = new HashMap<String, String>();
		map.put("user", username);
    	map.put("password", password);
//    	HttpUtils mhttp =new HttpUtils();
//    	mhttp.SetMap(map);
//    	mhttp.Seturl("http://player.archermind.com/ci/index.php/aschedule/register");
//    	new Thread(mhttp).start();
//		查询数据库看该用户名是否被注册
//		if(被注册){
//	       return ERROR_ACCOUNT_EXIST;
//	    }
		//向数据库中插入注册的信息，并判断是否插入成功
//		if(不成功){
//			return ERROR_WEB_ERROR;
//		}
       	String ret = HttpUtils.doPost(map, "http://player.archermind.com/ci/index.php/aschedule/register");
    	if(Integer.parseInt(ret) >=0){
    		return SUCCESS;
    	}else{
    		System.out.println("login-----"+ret);
    		return -1;
    	}
		//return SUCCESS;
	}
	/*用户修改密码函数
	 * 输入参数：用户名，用户旧密码，新密码
	 * 返回值 
       // 0 —— success
       //ERROR_PASSWORD_WRONG —— check  oldpswd is wrong 
	   //ERROR_WEB_ERROR —— 网络异常或其他原因修改密码失败
	 **/
	public int modifyPassword(String username,String oldpassword,String newpassword){
		username =username.replace(" ", "");
		oldpassword =oldpassword.replace(" ", "");
		newpassword =newpassword.replace(" ", "");
		//查询数据库判断旧密码是否正确，查询的时候如果异常返回-2
		if(newpassword.length()==0 || oldpassword.length()==0 /*||旧密码不对*/){           
            return ERROR_PASSWORD_WRONG;
        }
		//向数据库中插入更新的信息，并判断是否插入成功
//		if(不成功){
//			return ERROR_PASSWORD_WRONG;
//		}
		Map<String,String> map = new HashMap<String, String>();
		map.put("user", username);
    	map.put("password", oldpassword);
    	map.put("newpass", newpassword);
    	HttpUtils mhttp =new HttpUtils();
    	mhttp.SetMap(map);
    	mhttp.Seturl("http://player.archermind.com/ci/index.php/aschedule/pswdModify");
    	new Thread(mhttp).start();
		return SUCCESS;
	}
	/*用户登录函数
	 * 输入参数：用户名，用户密码，imsi号
	 * 返回值 
       // 0 —— success
       //ERROR_USER_NOT_EXIST —— check  user is not exist 
       //ERROR_PASSWORD_WRONG —— check  pswd is wrong
	   //ERROR_WEB_ERROR —— 网络异常或其他原因注册失败
	 **/
	public int login(String username,String password,String imsi){
		username =username.replace(" ", "");
		password =password.replace(" ", "");
		imsi =imsi.replace(" ", "");
		Map<String,String> map = new HashMap<String, String>();
		map.put("user", username);
    	map.put("password", password);
//    	HttpUtils mhttp =new HttpUtils();
//    	mhttp.SetMap(map);
//    	mhttp.Seturl("http://player.archermind.com/ci/index.php/aschedule/login");
//    	new Thread(mhttp).start();
		//查询数据库判断用户是否存在，密码是否正确，imsi是否改变，查询的时候如果异常返回-3
//		if(用户不存在){           
//            return ERROR_USER_NOT_EXIST;
//        }
//		if(密码不对){           
//            return ERROR_PASSWORD_WRONG;
//        }
//		if(imsi改变){           
//          记录一个状态，插入数据库，通知做其他操作；
//      }
		//向数据库中插入更新的信息，并判断是否插入成功
//		if(不成功){
//			return ERROR_WEB_ERROR;
//		}
    	String ret = HttpUtils.doPost(map, "http://player.archermind.com/ci/index.php/aschedule/login");
    	if(ret.equals("0")||ret.length() >20){
    		return SUCCESS;
    	}else{
    		System.out.println("login-----"+ret);
    		return -1;
    	}
    	//return SUCCESS;
	}
	/**************************************
	*   找回密码
	*   调用SDK发送密码至指定邮箱
	*   0 : 已发送至指定邮箱
	*  n : 错误码
    ***************************************/
	public int findPswd(String email){
		return SUCCESS;
	}
	
	/**************************************
	*   绑定手机号
	*   包含解除绑定(将手机号转换成16进制数： 15927130379 ——> 3B554B90B)
	*   0 : 已绑定
	*  n : 错误码
    ***************************************/
	public int telBind(int tel){
		return SUCCESS;
	}
	
	public int QQBind(){
		return SUCCESS;
	}
	
	public int SinaBind(){
		return SUCCESS;
	}
//----------------------  好友管理(邀请、删除) ----------------------
	public int inviteFriend(int frdTel){
		return SUCCESS;
	}
	
	public int removeFriend(int frdTel){
		return SUCCESS;
	}
//-----------------  本地数据与服务器同步、消息推拉  -----------------
	/**************************************
	*   上传联系人
	*   将手机号转换成16进制数： 15927130379 ——> 3B554B90B
	*   0 : 上传成功
	*  n : 错误码
    ***************************************/
	public int uploadContact(String user_id,String tellist){
		Map<String,String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
    	map.put("telist", tellist);
    	//map.put("password", pwdStr);
//    	HttpUtils mhttp =new HttpUtils();
//    	mhttp.SetMap(map);
//    	mhttp.Seturl("http://player.archermind.com/ci/index.php/aschedule/uploadContact");
//    	new Thread(mhttp).start();
    	String ret = HttpUtils.doPost(map, "http://player.archermind.com/ci/index.php/aschedule/uploadContact");
    	if(ret.equals("0")){
    		return SUCCESS;
    	}else{
    		System.out.println("uploadContact-----"+ret);
    		return -1;
    	}
		//return (ret != 0) ? ret : SUCCESS;
    	//return SUCCESS;
	}
	
	public String syncContact(){
		String telList = null;
		return telList;
	}

//	public static int uploadSchedule(/*String userID, int share,type, start_time, update_time, city, notice_time, notice_period, notice_week, notice_start, notice_end, content...*/){
//		int userID=3;
//		int share=1;
//		int type=4;
//		String start_time="13311111111";
//		String update_time="";
//		String city="";
//		int notice_time=0;
//		String notice_period="";
//		String notice_week="";
//		String notice_start="";
//		String notice_end="";
//		String content="xiaopashu test!";
//		String oper_flag="A"; 
////		ContentValues initialValues = new ContentValues(); 
////		initialValues.put(DatabaseHelper.COLUMN_SCHEDULE_USER_ID, userID);  
////		initialValues.put(DatabaseHelper.COLUMN_SCHEDULE_SHARE, share); 
////		initialValues.put(DatabaseHelper.COLUMN_SCHEDULE_TYPE,type);
////		initialValues.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME,start_time);
////		initialValues.put(DatabaseHelper.COLUMN_SCHEDULE_UPDATE_TIME,update_time);
////		initialValues.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_TIME,notice_time);
////		initialValues.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_PERIOD,notice_period);
////		initialValues.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_WEEK,notice_week);
////		initialValues.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_START,notice_start);
////		initialValues.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_END,notice_end);
////		initialValues.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_CONTENT,content);
////		initialValues.put(DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG,oper_flag);
////		
////		 //得到数据库文件  
////		mDatabaseManager.open();
////		mDatabaseManager.insertLocalSchedules(initialValues);
//		int maxtid =0;
//		Cursor cursor =mDatabaseManager.queryLocalSchedules();
//		if(cursor !=null){
//			if(cursor.moveToFirst()){
//				do{
//					Map<String, String> map = new HashMap<String, String>();
//					map.put("user_id",
//							Integer.toString(cursor.getInt(cursor
//									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_USER_ID))));
//					map.put("share",
//							Integer.toString(cursor.getInt(cursor
//									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_SHARE))));
//					map.put("type",
//							Integer.toString(cursor.getInt(cursor
//									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_TYPE))));
//					map.put("start_time",
//							cursor.getString(cursor
//									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME)));
//					map.put("notice_time",
//							Integer.toString(cursor.getInt(cursor
//									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_TIME))));
//					map.put("notice_period",
//							cursor.getString(cursor
//									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_PERIOD)));
//					map.put("city", "武汉");
//					map.put("notice_week",
//							cursor.getString(cursor
//									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_WEEK)));
//					map.put("notice_start",
//							cursor.getString(cursor
//									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_START)));
//					map.put("notice_end",
//							cursor.getString(cursor
//									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_END)));
//					map.put("content",
//							cursor.getString(cursor
//									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_CONTENT)));
//					map.put("action",
//							cursor.getString(cursor
//									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG)));
//					String text = cursor
//							.getString(cursor
//									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG));
//					if (text != null && text.equals("A")) {
//						Cursor cursor1 =mDatabaseManager.queryMaxTid();
//						if(cursor1 !=null){
//							if(cursor1.moveToFirst()){
//						       maxtid =cursor1.getInt(0);
//							}
//						}
//						cursor1.close();
//						String ret = HttpUtils
//								.doPost(map,
//										"http://player.archermind.com/ci/index.php/aschedule/uploadSchedule");
//						if (Integer.parseInt(ret) > 0) {
//							if (Integer.parseInt(ret) == 101) {
//								break;
//							} else {
//								ContentValues cv = new ContentValues();
//								cv.put("user_id",
//										cursor.getInt(cursor
//												.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_USER_ID)));
//								cv.put(DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG,
//										"N");
//								mDatabaseManager
//										.updateScheduleById(
//												cursor.getInt(cursor
//														.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_USER_ID)),
//												cv);
//								maxtid++;
//							}
//						}
//						System.out.println("xiaopashu test!");					
//					}
//					else if (text != null && text.equals("M")) {
//						String ret = HttpUtils
//								.doPost(map,
//										"http://player.archermind.com/ci/index.php/aschedule/uploadSchedule");
//
//						if (Integer.parseInt(ret) > 0) {
//							if (Integer.parseInt(ret) == 101) {
//								break;
//							} else {
//								ContentValues cv = new ContentValues();
//								cv.put(DatabaseHelper.COLUMN_SCHEDULE_UPDATE_TIME,
//								"当前毫秒时间");
//								cv.put(DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG,
//										"N");
//								mDatabaseManager
//										.updateScheduleById(
//												cursor.getInt(cursor
//														.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_USER_ID)),
//												cv);
//							}
//						}
//					}
//					else if (text != null && text.equals("D")) {
//						String ret = HttpUtils
//								.doPost(map,
//										"http://player.archermind.com/ci/index.php/aschedule/uploadSchedule");
//
//						if (Integer.parseInt(ret) > 0) {
//							if (Integer.parseInt(ret) == 101) {
//								break;
//							} else {
//								//删除当前记录
//							}
//						}
//					}
//				}while (cursor.moveToNext());
//			}
//		}
//		return SUCCESS;
//	}
	
	public String getWeather(String prov,String city){
		String weather = "";
		Map<String, String> map = new HashMap<String, String>();
		map.put("city",city);
		map.put("prov",prov);
		weather = HttpUtils.doPost(map,
				"http://player.archermind.com/ci/index.php/aschedule/getWeather");
		
		return weather;
	}
	public String syncSchedule(){
		String schList = null;
		return schList;
	}
	
	public int syncGmail(/*xxxx*/){
		return SUCCESS;
	}

	/**
	 * 按时间范围查询event find all events in a specified date/time range.
	 * 
	 * @param service
	 *            An authenticated CalendarService object.
	 * @param startTime
	 *            Start time (inclusive) of events to print.
	 * @param endTime
	 *            End time (exclusive) of events to print.
	 * @throws MalformedURLException
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
//	public static ArrayList<CalendarEventEntry> dateRangeQuery(
//			CalendarService service, DateTime startTime, DateTime endTime)
//			throws MalformedURLException {
//
//		List<CalendarEventEntry> allevents = new ArrayList<CalendarEventEntry>();
//		String surl = "http://www.google.com/calendar/feeds/xiaopashu@gmail.com/private/full";
//		URL url1 = new URL(surl);
//		CalendarQuery myQuery = new CalendarQuery(url1);
//		myQuery.setMinimumStartTime(startTime);
//		myQuery.setMaximumStartTime(endTime);
//
//		// Send the request and receive the response:
//		CalendarEventFeed resultFeed;
//		try {
//			resultFeed = service.query(myQuery, CalendarEventFeed.class);
//
//			for (int i = 0; i < resultFeed.getEntries().size(); i++) {
//				CalendarEventEntry entry = resultFeed.getEntries().get(i);
//				allevents.add(i, entry);
//				//System.out.println("\t" + entry.getTitle().getPlainText());
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ServiceException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return (ArrayList<CalendarEventEntry>) allevents;
//	}

//	public static DateTime createtime(int year, int month, int day, int hour,
//			int minute, int second) {
//		Calendar c = new GregorianCalendar();
//		c.set(year, month - 1, day, hour, minute, second); // 记住，Calendar类里的month是减1的，0代表一月
//		DateTime time = new DateTime(c.getTime(), TimeZone.getDefault()); // 这里设置时区
//		return time;
//	}
//	public static DateTime getInstanceTime(){
//	    Calendar c=Calendar.getInstance();//获得系统当前日期 
//	    DateTime time = new DateTime(c.getTime(), TimeZone.getDefault()); // 这里设置时区
//	    return time;
//	}


	// 读取日历数据
//	public static  String readCalendars(String userName, String userPassword,DateTime starttime,DateTime endtime)
//	{
//		String s = "http://www.google.com/calendar/feeds/default";
//		URL url=null;
//		try {
//			url = new URL(s);
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		CalendarService myService=null;
//		myService = new CalendarService("dcsCalendarServer");
//		//CalendarService myService = new CalendarService("Calendar");
//		try {
//			myService.setUserCredentials(userName, userPassword);
//		} catch (AuthenticationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		// CalendarFeed resultFeed = myService.getFeed(url,CalendarFeed.class);
//		CalendarFeed feeds=null;
//		try {
//			feeds = myService.getFeed(url, CalendarFeed.class);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ServiceException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		List<CalendarEntry> entrys = feeds.getEntries();
//		List<String> list = new ArrayList<String>(entrys.size());
//		for (CalendarEntry entry : entrys) {
//			// 标题
//			String title = entry.getTitle().getPlainText();
//			// 日历时区
//			String tz = entry.getTimeZone().getValue();
//			// 显示颜色
//			String color = entry.getColor().getValue(); // 访问级别
//			String level = entry.getAccessLevel().getValue();
//			// 创建者
//			List<Person> authors = entry.getAuthors();
//			String author = "";
//			for (Person p : authors) {
//				author += p.getName() + ",";
//			}
//			list.add(entry.getTitle().getPlainText());
//		}
//		// 仅简单的返回 标题，其余数据被忽略
//		String[] state = new String[list.size()];
//		state = list.toArray(state);
////		DateTime starttime;
////		starttime = createtime(2012, 7, 4, 11, 30, 30);
////		System.out.println("++++++++++++++++++++++++"+starttime);
////		DateTime endtime;
////		endtime = createtime(2012, 7, 24, 11, 30, 30);
////		System.out.println("++++++++++++++++++++++++"+endtime);
//		if(endtime ==null){
//			endtime =getInstanceTime();
//		}
//		List<CalendarEventEntry> entrys1 = null;
//		try {
//			entrys1 = dateRangeQuery(myService, starttime,
//					endtime);
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		String res ="";
//		for (CalendarEventEntry entry1 : entrys1) {
//			// 活动事件名称
//			String title = entry1.getTitle().getPlainText();
//			System.out.println("++++++++++++++++++++++++"+title);
//			// 活动描述
//			String memo = entry1.getTextContent().getContent().getPlainText();
//			System.out.println("++++++++++++++++++++++++"+memo);
//			// 活动地点：
//			List<Where> wList = entry1.getLocations();
//			String where = "";
//			for (Where w : wList) {
//				where += "," + w.getValueString();
//			}
//			// 活动时间；
//			When when = entry1.getTimes().get(0);			
//			String time = when.getStartTime() + "～" + when.getEndTime();
//			System.out.println("++++++++++++++++++++++++"+time);
//			// 参与者
//			List<EventWho> whos = entry1.getParticipants();
//			String who = "";
//			for (EventWho p : whos) {
//				who += p.getValueString();
//				System.out.println(who);
//			}
//			if(res.length() ==0){
//				res = time + " " + title + " " + memo + "\r\n";
//			}else{
//			    res =res + time + " " + title + " " + memo + "\r\n";
//			}
//		}
//		System.out.println(res);
//		return res;
//	}
	
//	public static void gettest(){
//		 CalendarService myService = new CalendarService("CalendarTest-1");
//	        try {
//	              myService.setUserCredentials("username", "password");
//	              //System.out.println("Printing all events");
//	              
//   
//	} catch (AuthenticationException e) {
//	              e.printStackTrace();
//	         } 
//	         catch (ServiceException e) {
//	             e.printStackTrace();
//	         }
//	}
}

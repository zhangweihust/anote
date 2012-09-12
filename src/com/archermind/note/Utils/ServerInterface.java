package com.archermind.note.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.amtcloud.mobile.android.business.AmtApplication;
import com.archermind.note.NoteApplication;
import com.archermind.note.Screens.LoginScreen;

import android.R.integer;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class ServerInterface {

	public static final String URL_SERVER = "http://note.archermind.com/";
	public static final String URL_LOGIN = URL_SERVER
			+ "ci/index.php/anote/login";
	public static final String URL_REGISTER = URL_SERVER
			+ "ci/index.php/anote/register";
	public static final String URL_CHECK = URL_SERVER
			+ "ci/index.php/anote/check_bin_acc";
	public static final String URL_BOUNDACCOUNT = URL_SERVER
			+ "ci/index.php/anote/bind_acc";
	public static final String URL_uploadAlbum = URL_SERVER
			+ "ci/index.php/anote/send_url";
	public static final String URL_getAlbumUrl = URL_SERVER
			+ "ci/index.php/anote/get_url";
	public static final String URL_setPhotoUrl = URL_SERVER
			+ "ci/index.php/anote/set_portrait_url";
	public static final String URL_getPhotoUrl = URL_SERVER
			+ "ci/index.php/anote/get_portrait_url";
	public static final String URL_get_info = URL_SERVER
			+ "ci/index.php/anote/get_info";
	public static final String URL_set_info = URL_SERVER
			+ "ci/index.php/anote/set_info";
	public static final String URL_upload_note = URL_SERVER
			+ "ci/index.php/anote/shareNote";
	public static final String URL_getReplyFromUser = URL_SERVER
			+ "ci/index.php/anote/getReplyFromUser";
	public static final String URL_MODIFYPASSWORD = URL_SERVER
			+ "ci/index.php/anote/pswModify";
	public static final String URL_FINDPASSWORD = URL_SERVER
			+ "ci/index.php/anote/findPassWord";
	// public static final String URL_get_version_info = URL_SERVER +
	// "ci/index.php/anote/get_version_info";
	// public static final String URL_get_version_info =
	// "http://10.52.31.90/CodeIgniter_2.1.2/index.php/anote/get_version_info";
	public static final String URL_get_version_info = URL_SERVER
			+ "ci/index.php/anote/get_version_info";
	public static final String URL_feedback = URL_SERVER
			+ "ci/index.php/anote/suggestionfeedback";
	// public static final String URL_send_reports = URL_SERVER +
	// "ci/index.php/anote/send_reports";
	public static final String URL_send_reports = URL_SERVER + "ci/index.php/anote/send_reports";
	public static final String APP_ID = "462b39f3eb7c4fb9a8e027473a6cd322";
	public static final String APP_SECRET = "482a4afe2f0e4020832078a4b4eeeae4";
	public static final String IMG_DOWADING_HEAD = "http://api.amtbaas.com/0/services/showPictrue?appId="
			+ APP_ID + "&appSecret=" + APP_SECRET + "&username=";
	public static final String URL_DEVICEINFO = URL_SERVER
			+ "ci/index.php/anote/setClientInfo";
	public static final String URL_USERACTIVEINFO = URL_SERVER
			+ "ci/index.php/anote/setUserActionInfo";
	// "http://219.138.163.58/"

	public static final int SUCCESS = 0;

	public static final int LOGIN_TYPE_SINA = 1;
	public static final int LOGIN_TYPE_QQ = 2;
	public static final int LOGIN_TYPE_RENREN = 3;

	public static final int ERROR_ACCOUNT_EXIST = -2;

	public static final int ERROR_SYNC_FAILED = 3;
	public static final int ERROR_UPLOAD_FAILED = 4;
	public static final int ERROR_DATABASE_INTERNAL = 5;

	public static final int ERROR_EMAIL_INVALID = 6;
	public static final int ERROR_PASSWORD_INVALID = 7;

	public static final int ERROR_WEB_ERROR = 8;

	public static final int ERROR_SERVER_N0_REPLY = 100;
	public static final int ERROR_SERVER_INTERNAL = 101;
	public static final int ERROR_SERVER_REFUSE = 102;

	public static final int ERROR_PASSWORD_WRONG = 201;
	public static final int ERROR_USER_NOT_EXIST = 202;
	public static final int USER_NOT_BIND = 203;
	public static final int ERROR_USER_BINDED = -5;

	public static final int COOKIES_ERROR = -600;
	
	public static final int BOUNDACCOUNT_FAILED = -1;
	public static final int BOUNDACCOUNT_FAILED_EXIST = -3;

	/**
	 * 用户注册 输入参数：用户名，用户密码 返回值： SUCCESS 注册成功
	 */
	public static String register(String type, String bin_uid,
			String bin_nickname, String username, String password,
			String nickname, String sex, String region) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user", username);
		// 将密码加密后存储到服务器
		try {
			map.put("password",
					CookieCrypt.encrypt(LoginScreen.USERINFO_KEY, password));
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		map.put("nickname", nickname);
		map.put("acc_type", type);
		map.put("bin_acc", bin_uid);
		map.put("bin_nickname", bin_nickname);
		map.put("gender", sex);
		map.put("region", region);
		return HttpUtils.doPost(map, URL_REGISTER);
	}

	/**
	 * 用户登录 输入参数：用户名，用户密码 返回值： SUCCESS 登录成功 ERROR_PASSWORD_WRONG 密码错误
	 */
	public static String login(String username, String password) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user", username);
		map.put("password", password);
		return HttpUtils.doPost(map, URL_LOGIN);

	}

	/**
	 * 检测用户是否绑定过新浪，腾讯，人人账号 输入参数：绑定账号的类型，绑定账号的uid 返回:被绑定账号的信息
	 */
	public static String checkBounding(int type, String uid) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("acc_type", String.valueOf(type));
		map.put("bin_acc", uid);
		return HttpUtils.doPost(map, URL_CHECK);
	}

	public static String boundAccount(int userId, int type, String bin_uid,
			String bin_nickname) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", String.valueOf(userId));
		map.put("acc_type", String.valueOf(type));
		map.put("bin_acc", bin_uid);
		map.put("bin_nickname", bin_nickname);
		return HttpUtils.doPost(map, URL_BOUNDACCOUNT);
//		try {
//			if (res.equals("0")) {
//				return 1;
//			} else if (res.equals("-3")) {
//				return -3;
//			} else if (Integer.parseInt(res) == COOKIES_ERROR) {
//				return COOKIES_ERROR;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return -1;
	}

	/**
	 * 修改密码 输入参数：用户名，原密码，新密码 返回值： SUCCESS 修改成功
	 */
	public static String modifyPassword(String username, String newpassword) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user", username);
		// map.put("password", oldpassword);
		map.put("newpass", newpassword);
		return HttpUtils.doPost(map, URL_MODIFYPASSWORD);
	}

	public static boolean isEmail(String email) {
		String emailPattern = "[a-zA-Z0-9][a-zA-Z0-9._-]{2,30}[a-zA-Z0-9]@[a-zA-Z0-9]{2,31}.[a-zA-Z0-9]{3,4}";
		boolean result = Pattern.matches(emailPattern, email);
		return result;
	}

	public static boolean isPswdValid(String pass) {
		if (pass.length() < 6
				|| (pass.length() > 16)
				|| !(Pattern.matches(".*[0-9]+.*", pass) && Pattern.matches(
						".*[a-zA-Z]+.*", pass))) {
			return false;
		} else {
			return true;
		}
	}

	public String getWeather(String prov, String city) {
		String weather = "";
		Map<String, String> map = new HashMap<String, String>();
		map.put("city", city);
		map.put("prov", prov);
		weather = HttpUtils.doPost(map, URL_SERVER
				+ "ci/index.php/aschedule/getWeather");
		return weather;
	}

	/*
	 * 初始化 在主界面启动的时候调用 参数 ：上下文参数(this) 返回值：void
	 */
	public static void initAmtCloud(Context context) {
		AmtApplication.amtAppInitialize(context, APP_ID, APP_SECRET);
	}

	/*
	 * 上传文件： 参数 ：上下文参数(this)，用户名，文件路径 返回值：void
	 */
	public static int uploadAvatar(String user_id, String picName) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("portrait", picName);
		String res = HttpUtils.doPost(map, URL_setPhotoUrl);
		int result = 0;
		try {
			result = Integer.parseInt(res);
		} catch (Exception e) {
			result = -3; // 其他异常情况
		}
		return result;
	}

	/**
	 * 上传相册 输入参数：用户id，相册名，用户名，文件路径，文件名，文件扩展名 返回值： 0 成功 -1 url为空 -2：数据库操作失败
	 */
	 public static int uploadAlbum(String user_id, String filename, String albumname) 
	 {
	 // String url =
//	 "http://yun.archermind.com/mobile/service/showMedia?appId="
	 // + app_id
	 // + "&userName="
	 // + username
	 // + "&mediaName="
	 // + filename
	 // + "&mediaType=" + expandname;
	 String url = "";
	 url = NoteApplication.getInstance().getUserName()
		+ "&filename=" + filename + "&album=" + albumname;
		 
		 
	 Map<String, String> map = new HashMap<String, String>();
	 map.put("user_id", user_id);
	 map.put("albumurl", url);
	 String res= HttpUtils.doPost(map, URL_uploadAlbum);
	
	 int result =0;
	 try{
	 result =Integer.parseInt(res);
	 }catch (Exception e){
	 result =-3; //其他异常情况
	 }
	 return result;
	 }
	/**
	 * 获取相册里的照片 输入参数：用户id，相册名 返回值： json 成功 -1 url为空 -2：数据库操作失败
	 */
	public static String getAlbumDownloadUrl(String user_id, String albumname) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("albumname", albumname);
		String res = HttpUtils.doPost(map, URL_getAlbumUrl);
		return res;
	}

	public static String update_info(String user_id, String nickname,
			String gender, String region, String avatar_url) {

		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("nickname", nickname);
		map.put("gender", gender);
		map.put("region", region);
		map.put("portrait", avatar_url);
		String res = HttpUtils.doPost(map, URL_set_info);
		return res;
	}

	public static int uploadNote(long id, String user_id, String nid,
			String action, String title, String content, String page) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("nid", nid);
		map.put("action", action);
		map.put("title", title);
		map.put("content", content);
		map.put("page", page);
		int res;
		try {
			res = Integer.parseInt(HttpUtils.doPost(map, URL_upload_note));
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

		if ("A".equals(action)) {
			if (res > 0) {
				return res;
			} else if (res == COOKIES_ERROR) {
				// cookies过期，由httpUtil发送event统一管理
				return COOKIES_ERROR;
			} else {
				return -1;
			}
		} else if ("M".equals(action)) {
			if (res == 0) {
				return 0;
			} else if (res == COOKIES_ERROR) {
				// cookies过期，由httpUtil发送event统一管理
				return COOKIES_ERROR;
			} else {
				return -1;
			}
		}
		return -1;
	}

	/**
	 * 获取用户信息 输入参数：用户id 返回值： json 成功 -1 url为空 -2：数据库操作失败
	 */
	public static String getReplyFromUser(int user_id, long date, int count) {

		Map<String, String> map = new HashMap<String, String>();
		System.out.println(user_id + " , " + date + ", " + count + ", ");
		map.put("user_id", user_id + "");
		map.put("date", date + "");
		map.put("items", count + "");
		String res = HttpUtils.doPost(map, URL_getReplyFromUser);
		return res;
	}

	/**
	 * 获取版本信息 返回值： json 成功 -1 url为空
	 */
	public static String get_version_info() {
		String res = HttpUtils.doPost(null, URL_get_version_info);
		return res;
	}

	public static int suggestionfeedback(String user_id, String tel,
			String suggestion) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("tel", tel);
		map.put("suggestion", suggestion);
		String ret = HttpUtils.doPost(map, URL_feedback);
		int result = 0;
		try {
			result = Integer.parseInt(ret);
		} catch (Exception e) {
			result = -3;
		}
		return result;
	}

	/**
	 * 找回密码 返回值： 0 成功，-1 用户名不存在， -2 邮件发送失败
	 */
	public static String FindPassword(String username) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("email", username);
		return HttpUtils.doPost(map, URL_FINDPASSWORD);
	}
}

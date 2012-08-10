package com.archermind.note.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.amtcloud.mobile.android.core.AmtApplication;
import com.amtcloud.mobile.android.file.AmtFileObject;

import android.R.integer;
import android.content.Context;
import android.util.Log;

public class ServerInterface {

	public static final String URL_LOGIN = "http://10.52.31.90/CodeIgniter_2.1.2/index.php/anote/login";
	public static final String URL_REGISTER = "http://10.52.31.90/CodeIgniter_2.1.2/index.php/anote/register";
	public static final String URL_CHECK = "http://10.52.31.90/CodeIgniter_2.1.2/index.php/anote/test_bin_acc";
	public static final String URL_MODIFYPASSWORD = "";

	public static final String app_id = "0ba7932602af4a45bd866bad93be0e50";
	public static final String app_secret = "2411edd1a2c44249a98e6451592062bc";

	public static final int SUCCESS = 0;

	public static final int LOGIN_TYPE_SINA = 10;

	public static final int ERROR_ACCOUNT_OR_PASSWORD_EMPTY = 1;
	public static final int ERROR_ACCOUNT_EXIST = 2;

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
	public static final int ERROR_USER_NOT_BIND = 203;
	public static final int USER_BINDED = 204;

	/**
	 * 用户注册 输入参数：用户名，用户密码 返回值： SUCCESS 注册成功
	 */
	public static int register(int type, String bin_uid, String username,
			String password, String nickname, int sex) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user", username);
		map.put("password", password);
		map.put("nickname", nickname);
		map.put("acc_type", String.valueOf(type));
		map.put("bin_acc", bin_uid);
		return Integer.valueOf(HttpUtils.doPost(map, URL_REGISTER));
	}

	/**
	 * 用户登录 输入参数：用户名，用户密码 返回值： SUCCESS 登录成功 ERROR_PASSWORD_WRONG 密码错误
	 */
	public static int login(String username, String password) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user", username);
		map.put("password", password);
		return Integer.valueOf(HttpUtils.doPost(map, URL_LOGIN));

	}

	public static String checkBinding(int type, String uid) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("acc_type", String.valueOf(type));
		map.put("bin_acc", uid);
		return HttpUtils.doPost(map, URL_CHECK);
	}

	/**
	 * 修改密码 输入参数：用户名，原密码，新密码 返回值： SUCCESS 修改成功
	 */
	public static int modifyPassword(String username, String oldpassword,
			String newpassword) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user", username);
		map.put("password", oldpassword);
		map.put("newpass", newpassword);
		return Integer.valueOf(HttpUtils.doPost(map, URL_MODIFYPASSWORD));
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
		weather = HttpUtils
				.doPost(map,
						"http://player.archermind.com/ci/index.php/aschedule/getWeather");
		return weather;
	}
	/*
	 * 初始化 在主界面启动的时候调用
	 * 参数 ：上下文参数(this)
	 * 返回值：void
	 */
	public void InitAmtCloud(Context context) {
		AmtApplication.amtAppInitialize(context, app_id, app_secret);
	}
	/*
	 * 上传文件：
	 * 参数 ：上下文参数(this)，用户名，文件路径
	 * 返回值：void
	 */
	public void updateFile(Context context, String username, String filepath) {
		AmtFileObject fileObj = new AmtFileObject(context);
		fileObj.uploadFile(app_id, username, filepath);
	}

	/*
	 * 获取文件的下载地址
	 * 参数 ：用户名，文件名（不带扩展名），扩展名
	 * 返回值：外链下载地址
	 */
	public String makeDownloadUrl(String username, String filename,
			String expandname) {
		String url = "http://yun.archermind.com/mobile/service/showMedia?appId="
				+ app_id
				+ "&userName="
				+ username
				+ "&mediaName="
				+ filename
				+ "&mediaType=" + expandname;
		return url;
	}
}

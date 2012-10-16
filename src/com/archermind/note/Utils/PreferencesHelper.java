package com.archermind.note.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.archermind.note.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.AvoidXfermode.Mode;
import android.graphics.Bitmap.Config;

public class PreferencesHelper {
	
	public static final String XML_NAME = "USER_PREFERENCE";
	public static final String XML_USER_AVATAR = "avatar";
	public static final String XML_USER_NAME = "name";
	public static final String XML_USER_SEX = "sex";
	public static final String XML_USER_REGION_PROVINCE = "region_province";
	public static final String XML_USER_REGION_CITY = "region_city";
	public static final String XML_USER_ACCOUNT = "account";
	public static final String XML_USER_PASSWD = "passwd";
	public static final String XML_AUTOLOGIN = "autologin";
	public static final String XML_SAVEPASSWORD = "savepasswd";
	public static final String XML_SINA_ACCESS_TOKEN = "sina_access_token";
	public static final String XML_QQ_ACCESS_TOKEN = "qq_access_token";
	public static final String XML_QQ_OPENID = "qq_openid";
	public static final String XML_RENREN_ACCESS_TOKEN = "renren_access_token";
	public static final String XML_COOKIES = "cookies";
	public static final String XML_GESTURE_THICKNESS = "gesture_thickness";
	public static final String XML_GESTURE_COLOR = "gesture_color";
	public static final String XML_GRAFFIT_THICKNESS = "graffit_thickness";
	public static final String XML_GRAFFIT_COLOR = "graffit_color";
	public static final String XML_DEFAULT_SHARE = "default_share";
	
	private static Bitmap mUserAvatarBitmap = null;
	private static ArrayList<Map<String, Object>> mProvinceLists;
	
	public static void UpdateAvatar(Context context, String oldAvatarPath, String newAvatarPath) {
		if (oldAvatarPath != null && !"".equals(oldAvatarPath)) {
			new File(oldAvatarPath).delete();
		}

		if (mUserAvatarBitmap != null) {
			mUserAvatarBitmap.recycle();
			mUserAvatarBitmap = null;
			System.gc();
		}
		
		SharedPreferences preferences = getSharedPreferences(context, Context.MODE_WORLD_WRITEABLE);
		preferences.edit().putString(XML_USER_AVATAR, newAvatarPath).commit();
	}
	
	public static Bitmap getAvatarBitmap(Context context) {
		if (mUserAvatarBitmap != null) {
			return mUserAvatarBitmap;
		}
		
		SharedPreferences sharedata = getSharedPreferences(context, 0);
		String filepath = sharedata.getString(XML_USER_AVATAR, null);
		
		if (filepath == null) {
			return null;
		}else{
			File file = new File(filepath);
			if (file.exists()) {
				mUserAvatarBitmap = BitmapFactory.decodeFile(filepath);
			}
		}		
		return mUserAvatarBitmap;
	}
	
	public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {  
		if (bitmap == null)
			return null;
        
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);  
        Canvas canvas = new Canvas(output);  
  
        final int color = 0xff424242;  
        final Paint paint = new Paint();  
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());  
        final RectF rectF = new RectF(rect);  
        final float roundPx = pixels;  
  
        paint.setAntiAlias(true);  
        canvas.drawARGB(0, 0, 0, 0);  
        paint.setColor(color);  
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);  

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));  
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();
  
        return output;  
    }
	
	public static SharedPreferences getSharedPreferences(Context context, int mode) {
		return context.getSharedPreferences(XML_NAME, mode);
	}
	
	public final static class ProvinceInfo {
		public ArrayList<Integer> cityLists = new ArrayList<Integer>();
	}
	
	public static String getProvinceName(Context context, int provinceId) {
		if (mProvinceLists == null) {
			getCitysList();
		}
		
		if (provinceId >= 0 && provinceId < mProvinceLists.size()) {
			return  context.getString((Integer)  mProvinceLists.get(provinceId).get("ProvinceId"));
		} else {
			return "";
		}
	}
	
	public static String getCityName(Context context, int provinceId,  int cityId) {
		if (mProvinceLists == null) {
			getCitysList();
		}
		
		ProvinceInfo province;
		if (provinceId >= 0 && provinceId < mProvinceLists.size()) {
			province = (ProvinceInfo) mProvinceLists.get(provinceId).get("ProvinceList");
		} else {
			return "";
		}
		
		if (cityId >= 0 && cityId < province.cityLists.size()) {
			return context.getString(province.cityLists.get(cityId));
		} else {
			return "";
		}
	}
	
	public static ArrayList<Map<String, Object>> getCitysList() {
		if (mProvinceLists != null) {
			return mProvinceLists;
		}
		
		mProvinceLists = new ArrayList<Map<String, Object>>();

		ProvinceInfo province[] = new ProvinceInfo[32];
		province[0] = new ProvinceInfo();
		province[1] = new ProvinceInfo();
		province[2] = new ProvinceInfo();
		province[3] = new ProvinceInfo();
		
		province[4] = new ProvinceInfo();
		province[4].cityLists.add(R.string.city_5_1);
		province[4].cityLists.add(R.string.city_5_2);
		province[4].cityLists.add(R.string.city_5_3);
		province[4].cityLists.add(R.string.city_5_4);
		province[4].cityLists.add(R.string.city_5_5);
		province[4].cityLists.add(R.string.city_5_6);
		province[4].cityLists.add(R.string.city_5_7);
		province[4].cityLists.add(R.string.city_5_8);
		province[4].cityLists.add(R.string.city_5_9);
		province[4].cityLists.add(R.string.city_5_10);
		province[4].cityLists.add(R.string.city_5_11);
		province[4].cityLists.add(R.string.city_5_12);
		province[4].cityLists.add(R.string.city_5_13);
		province[4].cityLists.add(R.string.city_5_14);
		province[4].cityLists.add(R.string.city_5_15);
		province[4].cityLists.add(R.string.city_5_16);

		province[5] = new ProvinceInfo();
		province[5].cityLists.add(R.string.city_6_1);
		province[5].cityLists.add(R.string.city_6_2);
		province[5].cityLists.add(R.string.city_6_3);
		province[5].cityLists.add(R.string.city_6_4);
		province[5].cityLists.add(R.string.city_6_5);
		province[5].cityLists.add(R.string.city_6_6);
		province[5].cityLists.add(R.string.city_6_7);
		province[5].cityLists.add(R.string.city_6_8);
		province[5].cityLists.add(R.string.city_6_9);

		province[6] = new ProvinceInfo();
		province[6].cityLists.add(R.string.city_7_1);
		province[6].cityLists.add(R.string.city_7_2);
		province[6].cityLists.add(R.string.city_7_3);
		province[6].cityLists.add(R.string.city_7_4);
		province[6].cityLists.add(R.string.city_7_5);
		province[6].cityLists.add(R.string.city_7_6);
		province[6].cityLists.add(R.string.city_7_7);
		province[6].cityLists.add(R.string.city_7_8);
		province[6].cityLists.add(R.string.city_7_9);
		province[6].cityLists.add(R.string.city_7_10);
		province[6].cityLists.add(R.string.city_7_11);
		province[6].cityLists.add(R.string.city_7_12);
		province[6].cityLists.add(R.string.city_7_13);

		province[7] = new ProvinceInfo();
		province[7].cityLists.add(R.string.city_8_1);
		province[7].cityLists.add(R.string.city_8_2);
		province[7].cityLists.add(R.string.city_8_3);
		province[7].cityLists.add(R.string.city_8_4);
		province[7].cityLists.add(R.string.city_8_5);
		province[7].cityLists.add(R.string.city_8_6);
		province[7].cityLists.add(R.string.city_8_7);
		province[7].cityLists.add(R.string.city_8_8);
		province[7].cityLists.add(R.string.city_8_9);
		province[7].cityLists.add(R.string.city_8_10);
		province[7].cityLists.add(R.string.city_8_11);

		province[8] = new ProvinceInfo();
		province[8].cityLists.add(R.string.city_9_1);
		province[8].cityLists.add(R.string.city_9_2);
		province[8].cityLists.add(R.string.city_9_3);
		province[8].cityLists.add(R.string.city_9_4);
		province[8].cityLists.add(R.string.city_9_5);
		province[8].cityLists.add(R.string.city_9_6);
		province[8].cityLists.add(R.string.city_9_7);
		province[8].cityLists.add(R.string.city_9_8);
		province[8].cityLists.add(R.string.city_9_9);
		province[8].cityLists.add(R.string.city_9_10);
		province[8].cityLists.add(R.string.city_9_11);

		province[9] = new ProvinceInfo();
		province[9].cityLists.add(R.string.city_10_1);
		province[9].cityLists.add(R.string.city_10_2);
		province[9].cityLists.add(R.string.city_10_3);
		province[9].cityLists.add(R.string.city_10_4);
		province[9].cityLists.add(R.string.city_10_5);
		province[9].cityLists.add(R.string.city_10_6);
		province[9].cityLists.add(R.string.city_10_7);
		province[9].cityLists.add(R.string.city_10_8);
		province[9].cityLists.add(R.string.city_10_9);
		province[9].cityLists.add(R.string.city_10_10);
		province[9].cityLists.add(R.string.city_10_11);
		province[9].cityLists.add(R.string.city_10_12);
		province[9].cityLists.add(R.string.city_10_13);
		province[9].cityLists.add(R.string.city_10_14);
		province[9].cityLists.add(R.string.city_10_15);
		province[9].cityLists.add(R.string.city_10_16);
		province[9].cityLists.add(R.string.city_10_17);
		province[9].cityLists.add(R.string.city_10_18);

		province[10] = new ProvinceInfo();
		province[10].cityLists.add(R.string.city_11_1);
		province[10].cityLists.add(R.string.city_11_2);
		province[10].cityLists.add(R.string.city_11_3);
		province[10].cityLists.add(R.string.city_11_4);
		province[10].cityLists.add(R.string.city_11_5);
		province[10].cityLists.add(R.string.city_11_6);
		province[10].cityLists.add(R.string.city_11_7);
		province[10].cityLists.add(R.string.city_11_8);
		province[10].cityLists.add(R.string.city_11_9);
		province[10].cityLists.add(R.string.city_11_10);
		province[10].cityLists.add(R.string.city_11_11);
		province[10].cityLists.add(R.string.city_11_12);
		province[10].cityLists.add(R.string.city_11_13);
		province[10].cityLists.add(R.string.city_11_14);
		province[10].cityLists.add(R.string.city_11_15);
		province[10].cityLists.add(R.string.city_11_16);
		province[10].cityLists.add(R.string.city_11_17);

		province[11] = new ProvinceInfo();
		province[11].cityLists.add(R.string.city_12_1);
		province[11].cityLists.add(R.string.city_12_2);
		province[11].cityLists.add(R.string.city_12_3);
		province[11].cityLists.add(R.string.city_12_4);
		province[11].cityLists.add(R.string.city_12_5);
		province[11].cityLists.add(R.string.city_12_6);
		province[11].cityLists.add(R.string.city_12_7);
		province[11].cityLists.add(R.string.city_12_8);
		province[11].cityLists.add(R.string.city_12_9);
		province[11].cityLists.add(R.string.city_12_10);
		province[11].cityLists.add(R.string.city_12_11);
		province[11].cityLists.add(R.string.city_12_12);
		province[11].cityLists.add(R.string.city_12_13);
		
		province[12] = new ProvinceInfo();
		province[12].cityLists.add(R.string.city_13_1);				
		province[12].cityLists.add(R.string.city_13_2);				
		province[12].cityLists.add(R.string.city_13_3);				
		province[12].cityLists.add(R.string.city_13_4);				
		province[12].cityLists.add(R.string.city_13_5);				
		province[12].cityLists.add(R.string.city_13_6);				
		province[12].cityLists.add(R.string.city_13_7);				
		province[12].cityLists.add(R.string.city_13_8);				
		province[12].cityLists.add(R.string.city_13_9);				
		province[12].cityLists.add(R.string.city_13_10);				
		province[12].cityLists.add(R.string.city_13_11);				
		province[12].cityLists.add(R.string.city_13_12);				
		province[12].cityLists.add(R.string.city_13_13);				
		province[12].cityLists.add(R.string.city_13_14);				
		province[12].cityLists.add(R.string.city_13_15);				
		province[12].cityLists.add(R.string.city_13_16);				
		province[12].cityLists.add(R.string.city_13_17);


		province[13] = new ProvinceInfo();
		province[13].cityLists.add(R.string.city_14_1);				
		province[13].cityLists.add(R.string.city_14_2);				
		province[13].cityLists.add(R.string.city_14_3);				
		province[13].cityLists.add(R.string.city_14_4);				
		province[13].cityLists.add(R.string.city_14_5);				
		province[13].cityLists.add(R.string.city_14_6);				
		province[13].cityLists.add(R.string.city_14_7);				
		province[13].cityLists.add(R.string.city_14_8);				
		province[13].cityLists.add(R.string.city_14_9);				
		province[13].cityLists.add(R.string.city_14_10);		

		
		province[14] = new ProvinceInfo();
		province[14].cityLists.add(R.string.city_15_1);				
		province[14].cityLists.add(R.string.city_15_2);				
		province[14].cityLists.add(R.string.city_15_3);				
		province[14].cityLists.add(R.string.city_15_4);				
		province[14].cityLists.add(R.string.city_15_5);				
		province[14].cityLists.add(R.string.city_15_6);				
		province[14].cityLists.add(R.string.city_15_7);				
		province[14].cityLists.add(R.string.city_15_8);				
		province[14].cityLists.add(R.string.city_15_9);				
		province[14].cityLists.add(R.string.city_15_10);
		
		
		province[15] = new ProvinceInfo();
		province[15].cityLists.add(R.string.city_16_1);				
		province[15].cityLists.add(R.string.city_16_2);				
		province[15].cityLists.add(R.string.city_16_3);				
		province[15].cityLists.add(R.string.city_16_4);				
		province[15].cityLists.add(R.string.city_16_5);				
		province[15].cityLists.add(R.string.city_16_6);				
		province[15].cityLists.add(R.string.city_16_7);				
		province[15].cityLists.add(R.string.city_16_8);				
		province[15].cityLists.add(R.string.city_16_9);	

		province[16] = new ProvinceInfo();
		province[16].cityLists.add(R.string.city_17_1);				
		province[16].cityLists.add(R.string.city_17_2);				
		province[16].cityLists.add(R.string.city_17_3);				
		province[16].cityLists.add(R.string.city_17_4);				
		province[16].cityLists.add(R.string.city_17_5);				
		province[16].cityLists.add(R.string.city_17_6);				
		province[16].cityLists.add(R.string.city_17_7);				
		province[16].cityLists.add(R.string.city_17_8);				
		province[16].cityLists.add(R.string.city_17_9);				
		province[16].cityLists.add(R.string.city_17_10);				
		province[16].cityLists.add(R.string.city_17_11);				
		province[16].cityLists.add(R.string.city_17_12);				
		province[16].cityLists.add(R.string.city_17_13);				
		province[16].cityLists.add(R.string.city_17_14);				
		province[16].cityLists.add(R.string.city_17_15);				
		province[16].cityLists.add(R.string.city_17_16);				
		province[16].cityLists.add(R.string.city_17_17);				
		province[16].cityLists.add(R.string.city_17_18);				
		province[16].cityLists.add(R.string.city_17_19);				
		province[16].cityLists.add(R.string.city_17_20);				
		province[16].cityLists.add(R.string.city_17_21);	

		
		province[17] = new ProvinceInfo();
		province[17].cityLists.add(R.string.city_18_1);				
		province[17].cityLists.add(R.string.city_18_2);	
		
		province[18] = new ProvinceInfo();
		province[18].cityLists.add(R.string.city_19_1);				
		province[18].cityLists.add(R.string.city_19_2);				
		province[18].cityLists.add(R.string.city_19_3);				
		province[18].cityLists.add(R.string.city_19_4);				
		province[18].cityLists.add(R.string.city_19_5);				
		province[18].cityLists.add(R.string.city_19_6);				
		province[18].cityLists.add(R.string.city_19_7);				
		province[18].cityLists.add(R.string.city_19_8);				
		province[18].cityLists.add(R.string.city_19_9);				
		province[18].cityLists.add(R.string.city_19_10);				
		province[18].cityLists.add(R.string.city_19_11);				
		province[18].cityLists.add(R.string.city_19_12);				
		province[18].cityLists.add(R.string.city_19_13);				
		province[18].cityLists.add(R.string.city_19_14);				
		province[18].cityLists.add(R.string.city_19_15);				
		province[18].cityLists.add(R.string.city_19_16);				
		province[18].cityLists.add(R.string.city_19_17);				
		province[18].cityLists.add(R.string.city_19_18);				
		province[18].cityLists.add(R.string.city_19_19);				
		province[18].cityLists.add(R.string.city_19_20);				
		province[18].cityLists.add(R.string.city_19_21);				
		province[18].cityLists.add(R.string.city_19_22);	

		
		province[19] = new ProvinceInfo();
		province[19].cityLists.add(R.string.city_20_1);				
		province[19].cityLists.add(R.string.city_20_2);				
		province[19].cityLists.add(R.string.city_20_3);				
		province[19].cityLists.add(R.string.city_20_4);				
		province[19].cityLists.add(R.string.city_20_5);				
		province[19].cityLists.add(R.string.city_20_6);				
		province[19].cityLists.add(R.string.city_20_7);				
		province[19].cityLists.add(R.string.city_20_8);				
		province[19].cityLists.add(R.string.city_20_9);
		
		province[20] = new ProvinceInfo();
		province[20].cityLists.add(R.string.city_21_1);				
		province[20].cityLists.add(R.string.city_21_2);				
		province[20].cityLists.add(R.string.city_21_3);				
		province[20].cityLists.add(R.string.city_21_4);				
		province[20].cityLists.add(R.string.city_21_5);				
		province[20].cityLists.add(R.string.city_21_6);				
		province[20].cityLists.add(R.string.city_21_7);				
		province[20].cityLists.add(R.string.city_21_8);				
		province[20].cityLists.add(R.string.city_21_9);				
		province[20].cityLists.add(R.string.city_21_10);				
		province[20].cityLists.add(R.string.city_21_11);				
		province[20].cityLists.add(R.string.city_21_12);				
		province[20].cityLists.add(R.string.city_21_13);				
		province[20].cityLists.add(R.string.city_21_14);				
		province[20].cityLists.add(R.string.city_21_15);	

		
		province[21] = new ProvinceInfo();
		province[21].cityLists.add(R.string.city_22_1);				
		province[21].cityLists.add(R.string.city_22_2);				
		province[21].cityLists.add(R.string.city_22_3);				
		province[21].cityLists.add(R.string.city_22_4);				
		province[21].cityLists.add(R.string.city_22_5);				
		province[21].cityLists.add(R.string.city_22_6);				
		province[21].cityLists.add(R.string.city_22_7);				
		province[21].cityLists.add(R.string.city_22_8);				
		province[21].cityLists.add(R.string.city_22_9);				
		province[21].cityLists.add(R.string.city_22_10);				
		province[21].cityLists.add(R.string.city_22_11);				
		province[21].cityLists.add(R.string.city_22_12);				
		province[21].cityLists.add(R.string.city_22_13);				
		province[21].cityLists.add(R.string.city_22_14);				
		province[21].cityLists.add(R.string.city_22_15);				
		province[21].cityLists.add(R.string.city_22_16);				
		province[21].cityLists.add(R.string.city_22_17);				
		province[21].cityLists.add(R.string.city_22_18);				
		province[21].cityLists.add(R.string.city_22_19);				
		province[21].cityLists.add(R.string.city_22_20);				
		province[21].cityLists.add(R.string.city_22_21);	

		province[22] = new ProvinceInfo();
		province[22].cityLists.add(R.string.city_23_1);				
		province[22].cityLists.add(R.string.city_23_2);				
		province[22].cityLists.add(R.string.city_23_3);				
		province[22].cityLists.add(R.string.city_23_4);				
		province[22].cityLists.add(R.string.city_23_5);				
		province[22].cityLists.add(R.string.city_23_6);				
		province[22].cityLists.add(R.string.city_23_7);				
		province[22].cityLists.add(R.string.city_23_8);				
		province[22].cityLists.add(R.string.city_23_9);				
		province[22].cityLists.add(R.string.city_23_10);				
		province[22].cityLists.add(R.string.city_23_11);				
		province[22].cityLists.add(R.string.city_23_12);				
		province[22].cityLists.add(R.string.city_23_13);				
		province[22].cityLists.add(R.string.city_23_14);	

		
		province[23] = new ProvinceInfo();
		province[23].cityLists.add(R.string.city_24_1);				
		province[23].cityLists.add(R.string.city_24_2);				
		province[23].cityLists.add(R.string.city_24_3);				
		province[23].cityLists.add(R.string.city_24_4);				
		province[23].cityLists.add(R.string.city_24_5);				
		province[23].cityLists.add(R.string.city_24_6);				
		province[23].cityLists.add(R.string.city_24_7);				
		province[23].cityLists.add(R.string.city_24_8);				
		province[23].cityLists.add(R.string.city_24_9);				
		province[23].cityLists.add(R.string.city_24_10);				
		province[23].cityLists.add(R.string.city_24_11);				
		province[23].cityLists.add(R.string.city_24_12);				
		province[23].cityLists.add(R.string.city_24_13);				
		province[23].cityLists.add(R.string.city_24_14);				
		province[23].cityLists.add(R.string.city_24_15);				
		province[23].cityLists.add(R.string.city_24_16);				
		province[23].cityLists.add(R.string.city_24_17);	

		
		province[24] = new ProvinceInfo();
		province[24].cityLists.add(R.string.city_25_1);				
		province[24].cityLists.add(R.string.city_25_2);				
		province[24].cityLists.add(R.string.city_25_3);				
		province[24].cityLists.add(R.string.city_25_4);				
		province[24].cityLists.add(R.string.city_25_5);				
		province[24].cityLists.add(R.string.city_25_6);				
		province[24].cityLists.add(R.string.city_25_7);				
		province[24].cityLists.add(R.string.city_25_8);				
		province[24].cityLists.add(R.string.city_25_9);				
		province[24].cityLists.add(R.string.city_25_10);	

		
		province[25] = new ProvinceInfo();
		province[25].cityLists.add(R.string.city_26_1);				
		province[25].cityLists.add(R.string.city_26_2);				
		province[25].cityLists.add(R.string.city_26_3);				
		province[25].cityLists.add(R.string.city_26_4);				
		province[25].cityLists.add(R.string.city_26_5);				
		province[25].cityLists.add(R.string.city_26_6);				
		province[25].cityLists.add(R.string.city_26_7);				
		province[25].cityLists.add(R.string.city_26_8);				
		province[25].cityLists.add(R.string.city_26_9);				
		province[25].cityLists.add(R.string.city_26_10);				
		province[25].cityLists.add(R.string.city_26_11);				
		province[25].cityLists.add(R.string.city_26_12);				
		province[25].cityLists.add(R.string.city_26_13);				
		province[25].cityLists.add(R.string.city_26_14);	

		
		province[26] = new ProvinceInfo();
		province[26].cityLists.add(R.string.city_27_1);				
		province[26].cityLists.add(R.string.city_27_2);				
		province[26].cityLists.add(R.string.city_27_3);				
		province[26].cityLists.add(R.string.city_27_4);				
		province[26].cityLists.add(R.string.city_27_5);				
		province[26].cityLists.add(R.string.city_27_6);				
		province[26].cityLists.add(R.string.city_27_7);				
		province[26].cityLists.add(R.string.city_27_8);
		
		province[27] = new ProvinceInfo();
		province[27].cityLists.add(R.string.city_28_1);				
		province[27].cityLists.add(R.string.city_28_2);				
		province[27].cityLists.add(R.string.city_28_3);				
		province[27].cityLists.add(R.string.city_28_4);				
		province[27].cityLists.add(R.string.city_28_5);				
		province[27].cityLists.add(R.string.city_28_6);				
		province[27].cityLists.add(R.string.city_28_7);				
		province[27].cityLists.add(R.string.city_28_8);				
		province[27].cityLists.add(R.string.city_28_9);				
		province[27].cityLists.add(R.string.city_28_10);				
		province[27].cityLists.add(R.string.city_28_11);
		
		province[28] = new ProvinceInfo();
		province[28].cityLists.add(R.string.city_29_1);				
		province[28].cityLists.add(R.string.city_29_2);				
		province[28].cityLists.add(R.string.city_29_3);				
		province[28].cityLists.add(R.string.city_29_4);				
		province[28].cityLists.add(R.string.city_29_5);				
		province[28].cityLists.add(R.string.city_29_6);
		
		province[29] = new ProvinceInfo();
		province[29].cityLists.add(R.string.city_30_1);				
		province[29].cityLists.add(R.string.city_30_2);				
		province[29].cityLists.add(R.string.city_30_3);				
		province[29].cityLists.add(R.string.city_30_4);				
		province[29].cityLists.add(R.string.city_30_5);				
		province[29].cityLists.add(R.string.city_30_6);				
		province[29].cityLists.add(R.string.city_30_7);				
		province[29].cityLists.add(R.string.city_30_8);				
		province[29].cityLists.add(R.string.city_30_9);				
		province[29].cityLists.add(R.string.city_30_10);				
		province[29].cityLists.add(R.string.city_30_11);				
		province[29].cityLists.add(R.string.city_30_12);				
		province[29].cityLists.add(R.string.city_30_13);				
		province[29].cityLists.add(R.string.city_30_14);				
		province[29].cityLists.add(R.string.city_30_15);				
		province[29].cityLists.add(R.string.city_30_16);				
		province[29].cityLists.add(R.string.city_30_17);				
		province[29].cityLists.add(R.string.city_30_18);
		
		province[30] = new ProvinceInfo();
		province[30].cityLists.add(R.string.city_31_1);				
		province[30].cityLists.add(R.string.city_31_2);				
		province[30].cityLists.add(R.string.city_31_3);				
		province[30].cityLists.add(R.string.city_31_4);				
		province[30].cityLists.add(R.string.city_31_5);				
		province[30].cityLists.add(R.string.city_31_6);				
		province[30].cityLists.add(R.string.city_31_7);				
		province[30].cityLists.add(R.string.city_31_8);				
		province[30].cityLists.add(R.string.city_31_9);				
		province[30].cityLists.add(R.string.city_31_10);				
		province[30].cityLists.add(R.string.city_31_11);				
		province[30].cityLists.add(R.string.city_31_12);				
		province[30].cityLists.add(R.string.city_31_13);				
		province[30].cityLists.add(R.string.city_31_14);	

		
		province[31] = new ProvinceInfo();
		province[31].cityLists.add(R.string.city_32);
		province[31].cityLists.add(R.string.city_32_1);
		province[31].cityLists.add(R.string.city_32_2);
		province[31].cityLists.add(R.string.city_32_3);
		province[31].cityLists.add(R.string.city_32_4);
		
		int []ids = {R.string.city_1, R.string.city_2, R.string.city_3, R.string.city_4
				, R.string.city_5, R.string.city_6, R.string.city_7, R.string.city_8
				, R.string.city_9, R.string.city_10, R.string.city_11, R.string.city_12
				, R.string.city_13, R.string.city_14, R.string.city_15, R.string.city_16
				, R.string.city_17, R.string.city_18, R.string.city_19, R.string.city_20
				, R.string.city_21, R.string.city_22, R.string.city_23, R.string.city_24
				, R.string.city_25, R.string.city_26, R.string.city_27, R.string.city_28
				, R.string.city_29, R.string.city_30, R.string.city_31, R.string.city_32};
		for (int i=0; i<32; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ProvinceId", ids[i]);
			map.put("ProvinceList", province[i]);
			mProvinceLists.add(map);
		}
		
		return mProvinceLists;
	}
}

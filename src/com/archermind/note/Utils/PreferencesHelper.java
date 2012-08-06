package com.archermind.note.Utils;

import java.io.File;

import com.archermind.note.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

public class PreferencesHelper {
	
	public static final String XML_NAME = "USER_PREFERENCE";
	public static final String XML_AVATAR = "avatar";
	public static final String XML_AVATAR_SOURCE = "avatar_source";
	
	private static Bitmap mUserAvatarBitmap;
	
	public static void UpdateAvatar(Context context, String newAvatarPath) {
		SharedPreferences sharedata = context.getSharedPreferences(XML_NAME, 0);
		String filepath = sharedata.getString(XML_AVATAR, null);
		
		if (filepath != null) {
			new File(filepath).delete();
		}
		
		SharedPreferences preferences = context.getSharedPreferences(XML_NAME,Context.MODE_WORLD_WRITEABLE);
		preferences.edit().putString(XML_AVATAR, newAvatarPath).commit();

		if (mUserAvatarBitmap != null) {
			mUserAvatarBitmap.recycle();
			mUserAvatarBitmap = null;
		}
	}
	
	public static Bitmap getAvatarBitmap(Context context) {
		if (mUserAvatarBitmap != null) {
			return mUserAvatarBitmap;
		}
		
		SharedPreferences sharedata = context.getSharedPreferences(XML_NAME, 0);
		String filepath = sharedata.getString(XML_AVATAR, null);
		
		if (filepath == null) {
			mUserAvatarBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.my_photo);
		}else{
			File file = new File(filepath);
			if (file.exists()) {
				mUserAvatarBitmap = BitmapFactory.decodeFile(filepath);
			}
		}
		return mUserAvatarBitmap;
	}
}

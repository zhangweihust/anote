package com.archermind.note.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentResolver;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.archermind.note.R;

public class ImageCapture {
	private Context mContext;
	private ContentResolver mContentResolver;

	private Uri mLastContentUri;

	// byte[] mCaptureOnlyData;
	public ImageCapture(Context context, ContentResolver contentResolver) {
		this.mContext = context;
		this.mContentResolver = contentResolver;
	}

	// Returns the rotation degree in the jpeg header.
	public int storeImage(byte[] data, Location loc) {
		try {
			long dateTaken = System.currentTimeMillis();
			String title = createName(dateTaken);
			String filename = title + ".jpg";
			int[] degree = new int[1];
			mLastContentUri = ImageManager.addImage(this.mContentResolver,
					title, dateTaken,
					loc, // location from gps/network
					ImageManager.CAMERA_IMAGE_BUCKET_NAME, filename, null,
					data, degree);
			return degree[0];
		} catch (Exception ex) {
			Log.e("ImageCapture", "Exception while compressing image.", ex);
			return 0;
		}
	}

	public String createName(long dateTaken) {
		Date date = new Date(dateTaken);
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				mContext.getString(R.string.image_file_name_format));

		return dateFormat.format(date);
	}

	public Uri getLastCaptureUri() {
		return mLastContentUri;
	}
}
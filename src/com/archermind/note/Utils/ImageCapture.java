package com.archermind.note.Utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.CallLog.Calls;
import android.util.Log;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;

public class ImageCapture {

	public static final String ALBUM_CACHE_PATH = Environment
			.getExternalStorageDirectory().toString() + "/anote/album_cache";
	
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;
	
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
		String username = NoteApplication.getInstance().getUserName();
		if (username == null) {
			username = "default";
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				mContext.getString(R.string.image_file_name_format));

		return username + dateFormat.format(date);
	}

	public Uri getLastCaptureUri() {
		return mLastContentUri;
	}
	
	public static void createCacheBitmapFromUrl(String url, String localpath) {
		System.out.println(url);
		Bitmap bitmap = null;
		try {
			URLConnection conn = new URL(url).openConnection();
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			conn.setReadTimeout(READ_TIMEOUT);
			bitmap = BitmapFactory
					.decodeStream((InputStream) conn.getContent());
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("ImageCapture", "Exception while read image.", e);
		}

		if (bitmap == null) {
			Log.e("ImageCapture", "Exception while read image.");
			return;
		}
		
		File directory = new File(ALBUM_CACHE_PATH);
		if (!directory.isDirectory()) {
			directory.mkdirs();
		}
		
		BufferedOutputStream ostream = null;
		try {
			ostream = new BufferedOutputStream(new FileOutputStream(new File(
					localpath)), 2 * 1024);
			bitmap.compress(CompressFormat.JPEG, 90, ostream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ostream != null) {
					ostream.flush();
					ostream.close();
				}
				if (bitmap != null) {
					bitmap.recycle();
					bitmap = null;
				}
			} catch (IOException e) {
			}
		}
	}
	
	public String createThumbnailFile(String srcFilePath) {
		String name = srcFilePath.substring(srcFilePath.lastIndexOf("/") + 1, srcFilePath.length());
		name = name.substring(0, name.lastIndexOf("."));
		String ThumbnailName = ALBUM_CACHE_PATH + "/" + name + "_thumbnail.jpg";
		
		Bitmap bitmap = BitmapCache.decodeBitmap(srcFilePath);
		int width = bitmap.getWidth() < bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight();
		Bitmap output = Bitmap.createBitmap(width, width, Config.ARGB_8888); 
		Canvas canvas = new Canvas(output);
 
        //final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(
        		(int)((bitmap.getWidth() - width)/2), 
        		(int)((bitmap.getHeight() - width)/2), width, width);

 
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        //paint.setColor(color);
        canvas.drawBitmap(bitmap, rect, new Rect(0, 0,  width, width), paint);
		
		File directory = new File(ALBUM_CACHE_PATH);
		if (!directory.isDirectory()) {
			directory.mkdirs();
		}
		
		BufferedOutputStream ostream = null;
		try {
			ostream = new BufferedOutputStream(new FileOutputStream(new File(
					ThumbnailName)), 2 * 1024);
			bitmap.compress(CompressFormat.JPEG, 90, ostream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ostream != null) {
					ostream.flush();
					ostream.close();
				}
				if (bitmap != null) {
					bitmap.recycle();
					bitmap = null;
				}
			} catch (IOException e) {
			}
		}
		
		return ThumbnailName;
	}
	
	/**
	 * ���Ƶ����ļ�
	 * 
	 * @param oldPath
	 *            String ԭ�ļ�·�� �磺c:/fqf.txt
	 * @param newPath
	 *            String ���ƺ�·�� �磺f:/fqf.txt
	 * @return boolean
	 */
	public static void copyFile(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { // �ļ�����ʱ
				InputStream inStream = new FileInputStream(oldPath); // ����ԭ�ļ�
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // �ֽ��� �ļ���С
					System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		} catch (Exception e) {
			System.out.println("copyFile error");
			e.printStackTrace();

		}
	} 
}
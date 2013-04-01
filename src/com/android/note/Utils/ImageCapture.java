package com.android.note.Utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.android.note.Services.ServiceManager;
import com.archermind.note.R;

public class ImageCapture {

	public static final String IMAGE_CACHE_PATH = Environment
			.getExternalStorageDirectory().toString() + "/anote/image_cache";
	
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;
    
    private static final int IMAGE_MAX_SIDELENGHTH = 200;
	
	private Context mContext;
	private ContentResolver mContentResolver;

	private Uri mLastContentUri;

	// byte[] mCaptureOnlyData;
	public ImageCapture(Context context, ContentResolver contentResolver) {
		this.mContext = context;
		this.mContentResolver = contentResolver;
	}

	// Returns the rotation degree in the jpeg header.
	public int storeImage(byte[] data, Location loc, CompressFormat format) {
		try {
			long dateTaken = System.currentTimeMillis();
			String title = createName(dateTaken);
			String filename = title + "." + mapToFormatStr(format);
			int[] degree = new int[1];
			mLastContentUri = ImageManager.addImage(this.mContentResolver,
					title, dateTaken,
					loc, // location from gps/network
					IMAGE_CACHE_PATH, filename, null,
					data, format, degree);
			return degree[0];
		} catch (Exception ex) {
			Log.e("ImageCapture", "Exception while compressing image.", ex);
			return 0;
		}
	}

	public String createName(long dateTaken) {
		Date date = new Date(dateTaken);
		String userid = String.valueOf(ServiceManager.getUserId());
		if (userid == null) {
			userid = "0";
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				mContext.getString(R.string.image_file_name_format));

		return userid + dateFormat.format(date);
	}

	public Uri getLastCaptureUri() {
		return mLastContentUri;
	}
	
	public static String mapToFormatStr(CompressFormat format) {
		if (CompressFormat.JPEG == format) {
			return "jpg";
		} else if (CompressFormat.PNG == format) {
			return "png";
		} 
		return "";
	}
	
	public static CompressFormat mapFormatStr(String formatStr) {
		CompressFormat format = CompressFormat.JPEG;
		
		if (formatStr == null || "".equals(formatStr)) {
			System.out.println("mapFormatStr: formatStr is null");
		} else if ("jpg".equals(formatStr)) {
			format = CompressFormat.JPEG;
		} else if ("png".equals(formatStr)) {
			format = CompressFormat.PNG;
		} else {
			System.out.println("mapFormatStr: error formatStr");
		}
		return format;
	}
	
	public static String getLocalCacheImageNameFromUrl(String url) {
		String []items = url.split("&");
		if (items.length == 0)
			return null;
		
//		String mediaName="";
//		String mediaType="jpg";
//		for (int i=0; i<items.length; i++) {
//			if (items[i].contains("mediaName=")) {
//				String []str=items[i].split("=");
//				mediaName = str[str.length-1];
//			}
//			
//			if (items[i].contains("mediaType=")) {
//				String []str=items[i].split("=");
//				mediaType = str[str.length-1].toLowerCase();
//			}
//		}
		String filename = "";
		for (int i=0; i<items.length; i++)
		{
			if (items[i].contains("filename="))
			{
				filename = items[i].replace("filename=", "");
				break;
			}
		}
		
		if ("".equals(filename))
			return null;
		else
			return ImageCapture.IMAGE_CACHE_PATH + "/" + filename;
	}
	
	/**
	 * *
	 * @param url
	 * @param localpath
	 * @return 0 success; -1 unknown Exception; -2 MalformedURLException; -3 FileNotFoundException
	 */
	public static int createLocalCacheImageFromUrl(String url, String localpath) {
		System.out.println("url:"+url);
		System.out.println("localpath:"+localpath);
		int retCode = 0;
		BufferedOutputStream ostream = null;

		File directory = new File(IMAGE_CACHE_PATH);
		if (!directory.isDirectory()) {
			directory.mkdirs();
		}
		
		try {
			URLConnection conn = new URL(url).openConnection();
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			conn.setReadTimeout(READ_TIMEOUT);
			InputStream is = (InputStream) conn.getContent();
			
			int DATA_BUFFER = 8192;
			ostream = new BufferedOutputStream(new FileOutputStream(new File(
					localpath)), DATA_BUFFER);
			
			byte buffer[] = new byte[DATA_BUFFER];
            int readSize = 0;
            while((readSize = is.read(buffer)) > 0){
            	ostream.write(buffer, 0, readSize);
            	//ostream.flush();
            }
            ostream.flush();
            ostream.close();
		} catch (MalformedURLException urlEx) {
			urlEx.printStackTrace();
			Log.e("ImageCapture", "Exception while read image.", urlEx);
			retCode = -2;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			retCode = -3;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("ImageCapture", "Exception while read image.", e);
			retCode = -1;
		} finally {
			try {
				if (ostream != null) {
					ostream.flush();
					ostream.close();
				}
			} catch (IOException e) {
			}
		}
		return retCode;
	}
	
	public String createThumbnailFile(String srcFilePath) {
		String name = srcFilePath.substring(srcFilePath.lastIndexOf("/") + 1, srcFilePath.length());
		name = name.substring(0, name.lastIndexOf("."));
		try{
		String ThumbnailName = IMAGE_CACHE_PATH + "/" + name + "_thumbnail.jpg";
		
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
		
		File directory = new File(IMAGE_CACHE_PATH);
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
		}catch (Exception e) {
			// TODO: handle exception
			return "";
		}catch (OutOfMemoryError e) {
			// TODO: handle exception
			e.printStackTrace();
			return "";
		}
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
		File directory = new File(IMAGE_CACHE_PATH);
		if (!directory.isDirectory()) {
			directory.mkdirs();
		}
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
				fs.close();
			}
		} catch (Exception e) {
			System.out.println("copyFile error");
			e.printStackTrace();

		}
	} 
	
	public boolean CompressionImage(String srcPath,String desPath,boolean delSrc)
    {
		File srcfile = new File(srcPath); 
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, options); //此时返回bm为空
		int lenValue = (options.outWidth > options.outHeight) ? options.outWidth : options.outHeight;
	  
	 	options.inJustDecodeBounds = false;
	 	//缩放比
	 	int be = lenValue / IMAGE_MAX_SIDELENGHTH;
	 	if (be <= 1)
	 		be = 1;
	 	options.inSampleSize = be;
	 	//重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false哦
	 	bitmap=BitmapFactory.decodeFile(srcPath,options);
	 	if (bitmap == null)
	 	{
	 		return false;
	 	}
	 	int w = bitmap.getWidth();
	 	int h = bitmap.getHeight();
	 	System.out.println("width = " + w + " height = " + h); 
	  
	 	if (delSrc)
	 	{
	 		srcfile.delete();
	 	}
	  
	 	File file=new File(desPath);
	 	try {
	 		FileOutputStream out=new FileOutputStream(file);
	 		if(bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)){
	 			out.flush();
	 			out.close();
	 		}
	 	} catch (FileNotFoundException e) {
	 		// TODO Auto-generated catch block
	 		e.printStackTrace();
	 	} catch (IOException e) {
	 		// TODO Auto-generated catch block
	 		e.printStackTrace();
	 	} 
	 	bitmap.recycle();
	 	
	 	return true;
    }
	
	// 将图片的四角圆化
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap,float roundPx) {
     try{
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), 
            bitmap.getHeight(), Config.ARGB_8888);
//        bitmap.recycle();
        //得到画布
        Canvas canvas = new Canvas(output);
    
       
       //将画布的四角圆化
        final int color = Color.RED; 
        final Paint paint = new Paint(); 
        //得到与图像相同大小的区域  由构造的四个值决定区域的位置以及大小
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()); 
        final RectF rectF = new RectF(rect); 

        paint.setAntiAlias(true); 
        canvas.drawARGB(0, 0, 0, 0); 
        paint.setColor(color); 
        //drawRoundRect的第2,3个参数一样则画的是正圆的一角，如果数值不同则是椭圆的一角,roundPx值越大角度越明显
        canvas.drawRoundRect(rectF, roundPx,roundPx, paint); 
      
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN)); 
        canvas.drawBitmap(bitmap, rect, rect, paint); 
      
        return output; 
        }
     catch (Exception e) {
		// TODO: handle exception
    	 return null;
	}catch (OutOfMemoryError e) {
		// TODO: handle exception
		e.printStackTrace();
		return null;
	}
      } 
    
  //放大缩小图片  
    public static Bitmap zoomBitmap(Bitmap bitmap,int w,int h){  
    	if (bitmap == null)
    	{
    		return null;
    	}
    	try{
        int width = bitmap.getWidth();  
        int height = bitmap.getHeight();  
        Matrix matrix = new Matrix();  
        float scaleWidht = ((float)w / width);  
        float scaleHeight = ((float)h / height);  
        matrix.postScale(scaleWidht, scaleHeight);  
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);  
        return newbmp;  
    	}catch (Exception e) {
			// TODO: handle exception
    		return null;
		}catch (OutOfMemoryError e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
    } 
}
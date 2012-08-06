package com.archermind.note.Utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;

public class ImageManager {

	private static final String TAG = "ImageManager";
	private static final Uri STORAGE_URI = Images.Media.EXTERNAL_CONTENT_URI;

	public static final String CAMERA_IMAGE_BUCKET_NAME = Environment
			.getExternalStorageDirectory().toString()
			+ "/DCIM/Camera";

	private ImageManager() {
	}

	private static boolean checkFsWritable() {
		// Create a temporary file to see whether a volume is really writeable.
		// It's important not to put it in the root directory which may have a
		// limit on the number of files.
		String directoryName = Environment.getExternalStorageDirectory()
				.toString()
				+ "/DCIM";
		File directory = new File(directoryName);
		if (!directory.isDirectory()) {
			if (!directory.mkdirs()) {
				return false;
			}
		}
		return directory.canWrite();
	}

	public static boolean hasStorage() {
		return hasStorage(true);
	}

	public static boolean hasStorage(boolean requireWriteAccess) {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			if (requireWriteAccess) {
				boolean writable = checkFsWritable();
				return writable;
			} else {
				return true;
			}
		} else if (!requireWriteAccess
				&& Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	public static int getExifOrientation(String filepath) {
		int degree = 0;
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(filepath);
		} catch (IOException ex) {
			Log.e(TAG, "cannot read exif", ex);
		}
		if (exif != null) {
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION, -1);
			if (orientation != -1) {
				// We only recognize a subset of orientation tag values.
				switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
				}
			}
		}
		return degree;
	}

	//
	// Stores a bitmap or a jpeg byte array to a file (using the specified
	// directory and filename). Also add an entry to the media store for
	// this picture. The title, dateTaken, location are attributes for the
	// picture. The degree is a one element array which returns the orientation
	// of the picture.
	//
	public static Uri addImage(ContentResolver cr, String title,
			long dateTaken, Location location, String directory,
			String filename, Bitmap source, byte[] jpegData, int[] degree) {
		// We should store image data earlier than insert it to ContentProvider,
		// otherwise we may not be able to generate thumbnail in time.
		OutputStream outputStream = null;
		String filePath = directory + "/" + filename;
		try {
			File dir = new File(directory);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Log.w(TAG, "not make dir :" + directory);
					throw new IOException();
				}
			}

			File file = new File(directory, filename);
			outputStream = new FileOutputStream(file);
			if (source != null) {
				source.compress(CompressFormat.JPEG, 100, outputStream);
				degree[0] = 0;
			} else {
				outputStream.write(jpegData);
				degree[0] = getExifOrientation(filePath);
			}
		} catch (FileNotFoundException ex) {
			Log.w(TAG, ex);
			return null;
		} catch (IOException ex) {
			Log.w(TAG, ex);
			return null;
		} finally {
			closeSilently(outputStream);
		}
		// Read back the compressed file size.
		long size = new File(directory, filename).length();
		ContentValues values = new ContentValues(9);
		values.put(Images.Media.TITLE, title);
		// That filename is what will be handed to Gmail when a user shares a
		// photo. Gmail gets the name of the picture attachment from the
		// "DISPLAY_NAME" field.
		values.put(Images.Media.DISPLAY_NAME, filename);
		values.put(Images.Media.DATE_TAKEN, dateTaken);
		values.put(Images.Media.MIME_TYPE, "image/jpeg");
		values.put(Images.Media.ORIENTATION, degree[0]);
		values.put(Images.Media.DATA, filePath);
		values.put(Images.Media.SIZE, size);
		if (location != null) {
			values.put(Images.Media.LATITUDE, location.getLatitude());
			values.put(Images.Media.LONGITUDE, location.getLongitude());
		}
		return cr.insert(STORAGE_URI, values);
	}

	public static void closeSilently(Closeable c) {
		if (c == null)
			return;
		try {
			c.close();
		} catch (Throwable t) {
			// do nothing
		}
	}

}

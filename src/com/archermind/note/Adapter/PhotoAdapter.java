package com.archermind.note.Adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.archermind.note.R;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Utils.BitmapCache;
import com.archermind.note.Utils.ImageCapture;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoAdapter extends BaseAdapter {
	
	private Class clazz;

	private Handler mHandler = new Handler();

	private List<Map> mList;
	private Context mContext;
	public static final int APP_PAGE_SIZE = 12;
	
	private List<String> mDownloadList;

	public PhotoAdapter(Context context, Class clazz, List<Map> list, int page) {
		this.mContext = context;
		this.clazz = clazz;
		mList = new ArrayList<Map>();
		
		if (this.clazz == GridView.class) {
			int i = page * APP_PAGE_SIZE;
			int iEnd = i+APP_PAGE_SIZE;
			while ((i<list.size()) && (i<iEnd)) {
				mList.add(list.get(i));
				i++;
			}
		} else {
			mList = list;
		}
		mDownloadList = new ArrayList<String>();
	}
	
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	public void addNewItem(Map map) {
		// TODO Auto-generated method stub
		mList.add(map);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Map appInfo = mList.get(position);
		final String title = appInfo.get("title").toString();
		final String fileurl = appInfo.get("fileurl").toString();
		final String filelocalpath = appInfo.get("filelocalpath").toString();
		//final String allName = appInfo.get("allName").toString();
		final ViewHolder viewHolder;
		if (convertView == null) {
			if (this.clazz == GridView.class) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.screen_photo_item, null);
				
				viewHolder = new ViewHolder(); 
		        viewHolder.title = (TextView) convertView.findViewById(R.id.title); 
		        viewHolder.image = (ImageView) convertView.findViewById(R.id.image); 
		        viewHolder.selectedImage = (ImageView) convertView.findViewById(R.id.selected); 
			} else {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.photo_item, null);
				
				viewHolder = new ViewHolder(); 
		        viewHolder.title = (TextView) convertView.findViewById(R.id.photo_name); 
		        viewHolder.image = (ImageView) convertView.findViewById(R.id.photo_icon); 
		        viewHolder.selectedImage = (ImageView) convertView.findViewById(R.id.photo_selected); 
			}
	        convertView.setTag(viewHolder); 
	        
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		viewHolder.title.setText(title);
		viewHolder.fileurl = fileurl;
		viewHolder.filelocalpath = filelocalpath;
		
		if (this.clazz == GridView.class) {
			Thread newThread = new Thread() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (mDownloadList.contains(viewHolder.fileurl)) {
						return;
					}
					
					mDownloadList.add(viewHolder.fileurl);
					loadBitmap(viewHolder);
					mDownloadList.remove(viewHolder.fileurl);
					
					mHandler.post(new Runnable() {
	
						@Override
						public void run() {
							// TODO Auto-generated method stub
							viewHolder.image.setImageBitmap(viewHolder.imageBitmap);
						}
					});
	
				}
			};
			newThread.start();
		} else {
			loadBitmap(viewHolder);
			viewHolder.image.setImageBitmap(viewHolder.imageBitmap);
		}
		viewHolder.selectedImage.setVisibility(View.GONE);
		
		return convertView;
	}

	private String getFileName(String fileurl) {
		String []items = fileurl.split("&");
		if (items.length == 0)
			return null;
		
		String mediaName="";
		String mediaType="jpg";
		for (int i=0; i<items.length; i++) {
			if (items[i].contains("mediaName=")) {
				String []str=items[i].split("=");
				mediaName = str[str.length-1];
			}
			
//			if (items[i].contains("mediaType=")) {
//				String []str=items[i].split("=");
//				mediaType = str[str.length-1];
//			}
		}
		
		if ("".equals(mediaName) || "".equals(mediaType))
			return null;
		else
			return ImageCapture.ALBUM_CACHE_PATH + "/" + mediaName + "." + mediaType;
	}
	
	private void loadBitmap(ViewHolder item) {
		File file;
		if (item.filelocalpath != null && !"".equals(item.filelocalpath)) {
			file = new File(item.filelocalpath);
			if (!file.exists()) {
				item.filelocalpath = null;
				loadBitmap(item);
				return;
			}
		} else {
			if (item.fileurl == null || "".equals(item.fileurl)) {
				return;
			}
			
			String filelocalpath = getFileName(item.fileurl);
			file = new File(filelocalpath);
			if (!file.exists()) {
				ImageCapture.createCacheBitmapFromUrl(item.fileurl, filelocalpath);
			}
			
			if (!new File(filelocalpath).exists()) {
				return;
			}
			item.filelocalpath = filelocalpath;
		}

		Bitmap image = null;
		if (BitmapCache.getInstance().getBitmapRefs().containsKey(item.filelocalpath)) {
			image = BitmapCache.getInstance().getBitmap(item.filelocalpath);
			if (image != null) {
				item.imageBitmap = image;
				item.uri = Uri.fromFile(file);
			}
		} else {
			image = BitmapCache.decodeBitmap(item.filelocalpath);
			if (image != null) {
				BitmapCache.getInstance().addCacheBitmap(image,
						item.filelocalpath);
				item.imageBitmap = image;
				item.uri = Uri.fromFile(file);
			}
		}
	}
	
	public class ViewHolder 
    { 
        public TextView title; 
        public ImageView image; 
        public ImageView selectedImage;
        public Uri uri;
        public String fileurl;
        public String filelocalpath;
        public Bitmap imageBitmap;
    } 
}

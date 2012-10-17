package com.archermind.note.Adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.archermind.note.R;
import com.archermind.note.Utils.BitmapCache;
import com.archermind.note.Utils.ImageCapture;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PhotoAdapter extends BaseAdapter {

	private Class clazz;

	private Handler mHandler = new Handler();

	private List<Map> mList;
	private Context mContext;
	public static final int APP_PAGE_SIZE = 12;

	public PhotoAdapter(Context context, Class clazz, List<Map> list, int page) {
		this.mContext = context;
		this.clazz = clazz;
		mList = new ArrayList<Map>();

		if (this.clazz == GridView.class) {
			int i = page * APP_PAGE_SIZE;
			int iEnd = i + APP_PAGE_SIZE;
			while ((i < list.size()) && (i < iEnd)) {
				mList.add(list.get(i));
				i++;
			}
		} else {
			mList.addAll(list);
		}
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
		final String filepath = appInfo.get("filepath").toString();
		final int isWebImage = Integer.parseInt(appInfo.get("isweb").toString());
		
		// final String allName = appInfo.get("allName").toString();
		final ViewHolder viewHolder;
		if (convertView == null) {
			if (this.clazz == GridView.class) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.screen_photo_item, null);

				viewHolder = new ViewHolder();
				viewHolder.title = (TextView) convertView
						.findViewById(R.id.title);
				viewHolder.image = (ImageView) convertView
						.findViewById(R.id.image);
				viewHolder.selectedImage = (ImageView) convertView
						.findViewById(R.id.selected);
				viewHolder.loadprogress = (LinearLayout) convertView
				.findViewById(R.id.loading);
			} else {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.photo_item, null);

				viewHolder = new ViewHolder();
				viewHolder.title = (TextView) convertView
						.findViewById(R.id.photo_name);
				viewHolder.image = (ImageView) convertView
						.findViewById(R.id.photo_icon);
				viewHolder.selectedImage = (ImageView) convertView
						.findViewById(R.id.photo_selected);
			}			
			viewHolder.finalfilepath = "";
			viewHolder.isLoading = false;
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		if (viewHolder.imageBitmap != null) {
			viewHolder.image
			.setImageBitmap(viewHolder.imageBitmap);
		}

		if (viewHolder.filepath != null && !"".equals(viewHolder.filepath)
				&& viewHolder.filepath.equals(filepath)) {
			return convertView;
		}
		
		viewHolder.title.setText(title);
		viewHolder.filepath = filepath;
		viewHolder.isWebImage = (isWebImage == 1 ? true : false);

		if (this.clazz == GridView.class) {
			if (viewHolder.isLoading == false) {
				viewHolder.image.setVisibility(View.INVISIBLE);
				viewHolder.loadprogress.setVisibility(View.VISIBLE);
			}
			
			Thread newThread = new Thread() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (viewHolder.isLoading == true) {
						return;
					}
					
					
					loadBitmap(viewHolder);

					mHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							viewHolder.loadprogress.setVisibility(View.INVISIBLE);
							viewHolder.image.setVisibility(View.VISIBLE);
							viewHolder.image
									.setImageBitmap(viewHolder.imageBitmap);
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

	private void loadBitmap(ViewHolder item) {
		File file;
		item.isLoading = true;

		if (item.filepath == null || "".equals(item.filepath)) {
			item.isLoading = false;
			return;
		}
		
		if (item.isWebImage == false) {
			file = new File(item.filepath);
			if (!file.exists()) {
				item.isLoading = false;
				return;
			} else {
				item.finalfilepath = item.filepath;
			}
		} else {
			String filelocalpath = ImageCapture
					.getLocalCacheImageNameFromUrl(item.filepath);
			if (filelocalpath == null) {
				System.out
						.println("getLocalCacheImageNameFromUrl error, item.filepath:"
								+ item.filepath);
				item.isLoading = false;
				return;
			}

			int count = 0;
			file = new File(filelocalpath);
			while(count < 3)
			{
				if (!file.exists()) {
					ImageCapture.createLocalCacheImageFromUrl(item.filepath,
							filelocalpath);
				}

				if (!new File(filelocalpath).exists()) {
					System.out
					.println("createLocalCacheImageFromUrl error, item.fileurl:"
							+ item.filepath);
//					item.isLoading = false;
//					return;
				}
				else
				{
					item.finalfilepath = filelocalpath;
					break;
				}
				count++;
				
				
				
			}
//			file = new File(filelocalpath);
//			if (!file.exists()) {
//				ImageCapture.createLocalCacheImageFromUrl(item.filepath,
//						filelocalpath);
//			}
//
//			if (!new File(filelocalpath).exists()) {
//				System.out
//				.println("createLocalCacheImageFromUrl error, item.fileurl:"
//						+ item.filepath);
//				item.isLoading = false;
//				return;
//			}
//			item.finalfilepath = filelocalpath;
		}
		System.out.println("=CCC=" + item.finalfilepath);
		Bitmap image = null;
		if (BitmapCache.getInstance().getBitmapRefs().containsKey(
				item.finalfilepath)) {
			image = BitmapCache.getInstance().getBitmap(item.finalfilepath);
			if (image != null) {
				image = ImageCapture.zoomBitmap(image,100,100);
				if(image!=null){
					image = ImageCapture.zoomBitmap(image,100,100);
					Bitmap cornerImage = ImageCapture.getRoundedCornerBitmap(image,10.0f);
					if(cornerImage != null){
						item.imageBitmap = cornerImage;
					}else{
						item.imageBitmap = image;
					}
					item.uri = Uri.fromFile(file);
				}
			}
			else
			{
				BitmapCache.getInstance().deleteCacheBitmap(item.finalfilepath);
				System.out.println("BitmapCache.size = " + BitmapCache.getInstance().size() + " mList.size = " + mList.size());
				image = ImageCapture.zoomBitmap(BitmapCache.decodeBitmap(item.finalfilepath),100,100);
				if (image != null) {
					BitmapCache.getInstance().addCacheBitmap(image,
							item.finalfilepath);
					Bitmap cornerImage = ImageCapture.getRoundedCornerBitmap(image,10.0f);
					if(cornerImage != null){
					item.imageBitmap = cornerImage;
					}else{
						item.imageBitmap = image;
					}
					item.uri = Uri.fromFile(file);
				}
			}
		} else {
			System.out.println("=CCC= insert" + item.finalfilepath);
			image = ImageCapture.zoomBitmap(BitmapCache.decodeBitmap(item.finalfilepath),100,100);
			if (image != null) {
				BitmapCache.getInstance().addCacheBitmap(image,
						item.finalfilepath);
				Bitmap cornerImage = ImageCapture.getRoundedCornerBitmap(image,10.0f);
				if(cornerImage != null){
				item.imageBitmap = cornerImage;
				}else{
					item.imageBitmap = image;
				}
				item.uri = Uri.fromFile(file);
			}
		}
		item.isLoading = false;
	}

	public class ViewHolder {
		public TextView title;
		public ImageView image;
		public ImageView selectedImage;
		public Uri uri;
		public String filepath;
		public String finalfilepath;
		public Bitmap imageBitmap;
		//public boolean mustLoad;
		public boolean isLoading;
		public boolean isWebImage;
		public LinearLayout loadprogress;
	}
}

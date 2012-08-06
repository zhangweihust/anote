package com.archermind.note.Adapter;

import java.io.File;

import com.archermind.note.R;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Utils.BitmapCache;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoAdapter extends CursorAdapter {
	
	private LayoutInflater listInflater;
	
	private Class clazz;

	private Handler mHandler = new Handler();

	public PhotoAdapter(Context context, Cursor c, Class clazz) {
		super(context, c);
		// TODO Auto-generated constructor stub
		this.listInflater = LayoutInflater.from(context);
		this.clazz = clazz;
	}

	private void loadBitmap(ViewHolder item) {
		Bitmap image = null;
		File file = new File(item.filepath);

		if (BitmapCache.getInstance().getBitmapRefs().containsKey(item.filepath)) {
			image = BitmapCache.getInstance().getBitmap(item.filepath);
			if (image != null) {
				item.imageBitmap = image;
				item.uri = Uri.fromFile(file);
			}
		} else {
			if (file.exists()) {
				image = BitmapCache.decodeBitmap(item.filepath);
				if (image != null) {
					BitmapCache.getInstance().addCacheBitmap(image,
							item.filepath);
					item.imageBitmap = image;
					item.uri = Uri.fromFile(file);
				}
			}
		}
	}
	
	@Override
	public void bindView(View view, Context c, Cursor cursor) {
		// TODO Auto-generated method stub
		final ViewHolder item = (ViewHolder) view.getTag();
		
		String title = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PHOTO_NAME));
		String filePath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PHOTO_FILEPATH));
		
		item.title.setText(title);
		item.filepath = filePath;
		
		if (this.clazz != android.widget.GridView.class) {
			loadBitmap(item);
			item.image.setImageBitmap(item.imageBitmap);
		} else {
			Thread newThread = new Thread() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					loadBitmap(item);
					
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							item.image.setImageBitmap(item.imageBitmap);
						}
					});
					
				}};
				newThread.start();
		}
		item.selectedImage.setVisibility(View.GONE);
		
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		View convertView = null;
		if (this.clazz == android.widget.GridView.class) {
			convertView = this.listInflater.inflate(R.layout.screen_photo_item, null);
			
			ViewHolder viewHolder = new ViewHolder(); 
	        viewHolder.title = (TextView) convertView.findViewById(R.id.title); 
	        viewHolder.image = (ImageView) convertView.findViewById(R.id.image); 
	        viewHolder.selectedImage = (ImageView) convertView.findViewById(R.id.selected); 
	        convertView.setTag(viewHolder); 
		} else {
			convertView = this.listInflater.inflate(R.layout.photo_item, null);
			
			ViewHolder viewHolder = new ViewHolder(); 
	        viewHolder.title = (TextView) convertView.findViewById(R.id.photo_name); 
	        viewHolder.image = (ImageView) convertView.findViewById(R.id.photo_icon);
	        viewHolder.selectedImage = (ImageView) convertView.findViewById(R.id.photo_selected); 
	        convertView.setTag(viewHolder); 
		}
		return convertView;
	}
	
	public class ViewHolder 
    { 
        public TextView title; 
        public ImageView image; 
        public ImageView selectedImage;
        public Uri uri;
        public String filepath;
        public Bitmap imageBitmap;
    } 
}

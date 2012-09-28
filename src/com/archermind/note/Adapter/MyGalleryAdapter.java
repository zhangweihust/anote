package com.archermind.note.Adapter;

import java.util.ArrayList;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * @author GG CopyRight @ GaoGe Sep 28, 2012
 */

public class MyGalleryAdapter extends BaseAdapter {
	// 用来设置ImageView的风格
	int mGalleryItemBackground;
	private Context context;
	private ArrayList<Bitmap> mImageBitmaps;

	// 构造函数
	public MyGalleryAdapter(Context context, ArrayList<Bitmap> imageBitmaps) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.mImageBitmaps = imageBitmaps;
	}

	// 返回所有图片的个数
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mImageBitmaps.size();
	}

	// 返回图片在资源的位置
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	// 返回图片在资源的位置
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	// 此方法是最主要的，他设置好的ImageView对象返回给Gallery
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// ImageView imageView = (ImageView)
		// LayoutInflater.from(context).inflate(
		// R.layout.share_gallery_item, null);
		// int maxWidth = ((Activity) context).getWindowManager()
		// .getDefaultDisplay().getWidth() / 2;
		// imageView.setMaxWidth(maxWidth);
		// imageView.setImageBitmap(mImageBitmaps.get(position));
		ImageView imageView = new ImageView(context);
		imageView.setImageBitmap(mImageBitmaps.get(position));
		imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		imageView.setMaxWidth(((Activity) context).getWindowManager()
				.getDefaultDisplay().getWidth() / 2);
		imageView.setAdjustViewBounds(true);
		return imageView;
	}

}

package com.archermind.note.Adapter;

import java.util.List;

import com.archermind.note.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MoreAdapter extends BaseAdapter
{
	private Context mCtx;
	private LayoutInflater inflater;		
	
	public MoreAdapter(Context ctx)
	{
		mCtx = ctx;
		inflater = LayoutInflater.from(ctx);
	}
	
	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if(convertView == null){
			convertView = LayoutInflater.from(mCtx).inflate(R.layout.more_popup_window_item, null);
		 }
		TextView tvItemTitle = (TextView) convertView.findViewById(R.id.tv_item_title);
		ImageView ivItemIcon = (ImageView) convertView.findViewById(R.id.iv_item_icon);
		if (position == 0) {
			tvItemTitle.setText(R.string.more_info);
			ivItemIcon.setImageResource(R.drawable.more_info);
		}else{
			tvItemTitle.setText(R.string.more_setting);
			ivItemIcon.setImageResource(R.drawable.more_setting);
		}
		return convertView;
	}

	
}
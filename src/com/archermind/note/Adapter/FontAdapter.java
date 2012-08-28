package com.archermind.note.Adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.archermind.note.R;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FontAdapter extends BaseAdapter {

	private Context mCtx;
	private List<Map<String, Object>> listItems;
	private static int mCount = 3;
	
	public FontAdapter(Context ctx)
	{
		mCtx = ctx;
		init();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.mCount;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
		    convertView = LayoutInflater.from(mCtx).inflate(R.layout.font_dialog_window_item, null);
		}
		
		TextView menuText = (TextView) convertView;
		String textStr = mCtx.getString((Integer)this.listItems.get(position).get("menuText"));
		if (textStr.equals(mCtx.getString(R.string.font_xdxwz))) {
			Typeface type = Typeface.createFromAsset(mCtx.getAssets(),"xdxwzt.ttf");
			menuText.setTypeface(type);
		} else if (textStr.equals(mCtx.getString(R.string.font_droidsans))) {
			menuText.setTypeface(Typeface.DEFAULT,Typeface.NORMAL);
		} else if (textStr.equals(mCtx.getString(R.string.font_droidserif_italic))) {
			menuText.setTypeface(Typeface.DEFAULT,Typeface.ITALIC);
		}
		menuText.setText(textStr);
		return convertView;
	}
	
	private void init() {
		this.listItems =  new ArrayList<Map<String, Object>>();
		for(int i = 0; i < this.mCount; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			if (i == 0) {
				map.put("menuText", R.string.font_xdxwz);
			} else if (i == 1) {
				map.put("menuText", R.string.font_droidsans);
			} else if (i == 2) {
				map.put("menuText", R.string.font_droidserif_italic);
			}
			this.listItems.add(map);
		}
	}
	
	public final class ListItemsView{
		public TextView menuText;
	}

}

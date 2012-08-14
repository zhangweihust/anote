package com.archermind.note.Adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private List<Map<String, Object>> listItems;
	private static int mCount = 2;
	
	public MoreAdapter(Context ctx)
	{
		mCtx = ctx;
		init();
	}
	
	@Override
	public int getCount() {
		return this.mCount;
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
		ListItemsView item = null;
		if(convertView == null){
			convertView = LayoutInflater.from(mCtx).inflate(R.layout.more_popup_window_item, null);
			item = new ListItemsView();
			item.menuIcon = (ImageView) convertView.findViewById(R.id.iv_item_icon);
			item.menuText = (TextView) convertView.findViewById(R.id.tv_item_title);
			convertView.setTag(item);
		}else{
			item = (ListItemsView)convertView.getTag();
		}
		item.menuText.setText(mCtx.getString((Integer)this.listItems.get(position).get("menuText")));
		item.menuIcon.setImageResource((Integer)this.listItems.get(position).get("menuIcon"));
		
		return convertView;
	}

	
	private void init(){
		this.listItems =  new ArrayList<Map<String, Object>>();
		for(int i = 0; i < this.mCount; i++){
			Map<String, Object> map = new HashMap<String, Object>();
			if(i == 0){
				map.put("menuIcon", R.drawable.more_info);
				map.put("menuText", R.string.more_info);
			}
			else if(i == 1){
				map.put("menuIcon", R.drawable.more_setting);
				map.put("menuText", R.string.more_setting);
			}
			this.listItems.add(map);
		}
	}
	
	public final class ListItemsView{
		public ImageView menuIcon;
		public TextView menuText;
	}
	
	
}
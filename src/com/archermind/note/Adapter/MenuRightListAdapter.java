package com.archermind.note.Adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.archermind.note.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuRightListAdapter extends BaseAdapter {

	private Context context;
	private List<Map<String, Object>> listItems;
	private int itemCount;
	private LayoutInflater listInflater;
	private boolean isPressed[];
	private final int COUNT = 4;
	
	/*一个menu item中包含一个imageView和一个TextView*/
	public final class ListItemsView{
		public ImageView menuIcon;
		public TextView menuText;
	}
	
	
	public MenuRightListAdapter(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.init();
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.itemCount;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final int po = position;
		ListItemsView listItemsView;
		if(convertView == null){
			listItemsView = new ListItemsView();
			convertView = this.listInflater.inflate(R.layout.menu_right_list_item, null);
			listItemsView.menuIcon = (ImageView)convertView.findViewById(R.id.menuIcon);
			listItemsView.menuText = (TextView)convertView.findViewById(R.id.menuText);
			convertView.setTag(listItemsView);
		}
		else{
			listItemsView = (ListItemsView)convertView.getTag();
		}
		
		listItemsView.menuIcon.setBackgroundResource((Integer)listItems.get(position).get("menuIcon"));
		String txt = context.getString((Integer) listItems.get(position).get("menuText"));
		listItemsView.menuText.setText(txt);
		
		convertView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				changeState(po);
				gotoActivity(po);
				//notifyDataSetInvalidated();
				new Handler().post(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
					}
					
				});
			}
		});
		
		return convertView;
	} 
	
	private void gotoActivity(int position){
		Intent intent = new Intent();
		switch(position){
		case 0:
			break;
		/*----------------------------------------------------*/	
		case 1:
			break;
		/*----------------------------------------------------*/
		case 2:
			break;
		/*----------------------------------------------------*/
		case 3:
			break;
		/*----------------------------------------------------*/
		default:
			//intent.setClass(context, XMUPageActivity.class);
			//context.startActivity(intent);
		}
	}
	
	private void changeState(int position){
		
		for(int i = 0; i < this.itemCount; i++){
			isPressed[i] = false;
		}
		isPressed[position] = true;
	}
	
	private void init(){
		
		this.itemCount = this.COUNT;
		this.listItems =  new ArrayList<Map<String, Object>>();
		this.isPressed = new boolean[this.itemCount];

		for(int i = 0; i < this.itemCount; i++){
			Map<String, Object> map = new HashMap<String, Object>();
			if(i == 0){
				map.put("menuIcon", R.drawable.menu_right_schedule);
				map.put("menuText", R.string.menu_right_schedule);
			}
			else if(i == 1){
				map.put("menuIcon", R.drawable.menu_right_diary);
				map.put("menuText", R.string.menu_right_diary);
			}
			else if(i == 2){
				map.put("menuIcon", R.drawable.menu_right_mood);
				map.put("menuText", R.string.menu_right_mood);
			}
			else{
				map.put("menuIcon", R.drawable.menu_right_activity);
				map.put("menuText", R.string.menu_right_activity);
			}
			this.listItems.add(map);
			this.isPressed[i] = false;
		}
		this.listInflater = LayoutInflater.from(context); 
	}
}

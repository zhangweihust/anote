package com.archermind.note.Adapter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import com.archermind.note.R;
import com.archermind.note.Adapter.MenuRightListAdapter.ListItemsView;
import com.archermind.note.Utils.PreferencesHelper;
import com.archermind.note.Utils.PreferencesHelper.ProvinceInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RegionInfoAdapter extends BaseAdapter {

	public final static int REGION_PROVINCE = 0;
	public final static int REGION_CITY = 1;
	
	private Context context;
	private ArrayList<Map<String, Object>> provinceLists;
	private ProvinceInfo province;
	private LayoutInflater listInflater;
	private int type;
	private int provincePos = 0;
	
	public RegionInfoAdapter(Context context, int type) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.provinceLists = PreferencesHelper.getCitysList();
		this.type = type;
		this.listInflater = LayoutInflater.from(context); 
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (this.type == REGION_CITY) {
			if (province == null) {
				return 0;
			} else {
				return province.cityLists.size();
			}
		} else {
			return provinceLists.size();
		}
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
		ViewHandle listItemsView;
		if(convertView == null){
			listItemsView = new ViewHandle();
			convertView = this.listInflater.inflate(R.layout.region_info_item, null);
			listItemsView.menuText = (TextView)convertView.findViewById(R.id.region_txt);
			System.out.println("=CCC----" + listItemsView.menuText + "  " + convertView);
			convertView.setTag(listItemsView);
		}
		else{
			listItemsView = (ViewHandle)convertView.getTag();
		}
		
		String txt="";
		if (this.type == REGION_CITY) {
			if (province != null) {
				txt = context.getString(province.cityLists.get(position));
			}
		} else {
			txt = context.getString((Integer)  provinceLists.get(position).get("ProvinceId"));
		}
		
		listItemsView.menuText.setText(txt);
		
		return convertView;
	}
	
	public void setProvince(int pos) {
		if (this.type == REGION_CITY) {
			province = (ProvinceInfo)provinceLists.get(pos).get("ProvinceList");
			provincePos = pos;
		}
	}
	
	public int getProvince() {
		return provincePos;
	}
	
	private final class ViewHandle {
		public TextView menuText;
	}
}

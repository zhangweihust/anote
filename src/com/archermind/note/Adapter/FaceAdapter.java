package com.archermind.note.Adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.archermind.note.R;

public class FaceAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<Integer> lyricFontColor = new ArrayList<Integer>();
	public FaceAdapter(Context context,ArrayList<Integer> lyricFontColor){
		this.context = context;
		this.lyricFontColor = lyricFontColor;
	}
	
	public ArrayList<Integer> getMenuData(){
		return this.lyricFontColor;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return lyricFontColor.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return lyricFontColor.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		HolderView holder;
		if(convertView==null){
			LayoutInflater inflater = LayoutInflater.from(context);
			holder = new HolderView();
			convertView = inflater.inflate(R.layout.face_item, null);
			holder.fongColorIcon = (ImageView) convertView.findViewById(R.id.fongColor_image);
			convertView.setTag(holder);
		}else{
			holder = (HolderView) convertView.getTag();
		}
		holder.fongColorIcon.setImageResource(lyricFontColor.get(position));
		holder.fongColorIcon.setTag(lyricFontColor.get(position));
		return convertView;
	}
	
	class HolderView {
		private ImageView fongColorIcon;
	}

}

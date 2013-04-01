package com.android.note.Adapter;

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
	private ArrayList<Integer> faces;
	
	public FaceAdapter(Context context){
		this.context = context;
		this.faces = new ArrayList<Integer>();
		faces.add(R.drawable.face_0);
		faces.add(R.drawable.face_1);
		faces.add(R.drawable.face_2);
		faces.add(R.drawable.face_3);
		faces.add(R.drawable.face_4);
		faces.add(R.drawable.face_5);
		faces.add(R.drawable.face_6);
		faces.add(R.drawable.face_7);
		faces.add(R.drawable.face_8);
		faces.add(R.drawable.face_9);
		faces.add(R.drawable.face_10);
		faces.add(R.drawable.face_11);
		faces.add(R.drawable.face_12);
		faces.add(R.drawable.face_13);
		faces.add(R.drawable.face_14);
		faces.add(R.drawable.face_15);
		faces.add(R.drawable.face_16);
		faces.add(R.drawable.face_17);
		faces.add(R.drawable.face_18);
		faces.add(R.drawable.face_19);
	}
	
	public ArrayList<Integer> getMenuData(){
		return this.faces;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return faces.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return faces.get(position);
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
		holder.fongColorIcon.setImageResource(faces.get(position));
		holder.fongColorIcon.setTag(faces.get(position));
		return convertView;
	}
	
	class HolderView {
		private ImageView fongColorIcon;
	}

}

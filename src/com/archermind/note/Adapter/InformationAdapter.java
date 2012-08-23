package com.archermind.note.Adapter;

import java.util.ArrayList;
import java.util.Calendar;

import com.archermind.note.R;
import com.archermind.note.Utils.DateTimeUtils;
import com.archermind.note.Utils.InformationData;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class InformationAdapter extends BaseAdapter
{
	private Context mCtx;
	private ArrayList<InformationData> mDatas;
	private boolean existsPrompt = false;
	
	public InformationAdapter(Context ctx, ArrayList<InformationData> data)
	{
		mCtx = ctx;
		mDatas = data;
	}
	
	@Override
	public int getCount() {
		return 0;
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
			convertView = LayoutInflater.from(mCtx).inflate(R.layout.information_listview_item, null);
			item = new ListItemsView();
			item.ivPhoto = (ImageView) convertView.findViewById(R.id.iv_photo);
			item.tvNickname = (TextView) convertView.findViewById(R.id.tv_nickname);
			item.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
			item.tvContent = (TextView) convertView.findViewById(R.id.tv_content);
			item.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
			convertView.setTag(item);
		}else{
			item = (ListItemsView)convertView.getTag();
		}
		
	    InformationData data = mDatas.get(position);
	    if(data.photo != null){
	    	item.ivPhoto.setImageBitmap(data.photo);
	    }
	    if(data.nickname != null){
	    	item.tvNickname.setText(data.nickname);
	    }else{
	    	item.tvNickname.setVisibility(View.GONE);
	    }
	    
	    if(data.title != null){
	    	item.tvTitle.setText(data.title);
	    }else{
	    	item.tvTitle.setVisibility(View.INVISIBLE);
	    }
	    
	    item.tvContent.setText(data.content);
	    if(data.time < DateTimeUtils.getToday(Calendar.AM, System.currentTimeMillis())){
	    	item.tvTime.setText(DateTimeUtils.time2String("yyyy年MM月dd日 HH:mm", data.time));
	    }else{
	    	item.tvTime.setText(DateTimeUtils.time2String("HH:mm", data.time));
	    }
	    
		
		return convertView;
	}

	public void setNoInformationPrompt(long time)
	{
		InformationData data = new InformationData();
		data.id = -1;
		data.content = "暂时没有人回复您的帖子，加油哦！";
 		data.time = time;
		
		mDatas.clear();
		mDatas.add(data);
		existsPrompt = true;
		notifyDataSetChanged();
	}
	
	public long getEarliestTime()
	{
		long time = 0;
		if (mDatas.size() > 0)
		{
			time = mDatas.get(0).time;
		}
		
		return time;
	}
	
	public long getlatestTime()
	{
		long time = 0;
		int size = mDatas.size();
		if (size > 0)
		{
			time = mDatas.get(size - 1).time;
		}
		
		return time;
	}
	
	public boolean isEmpty()
	{
		return mDatas.isEmpty();
	}
	
	
	public final class ListItemsView{
		public ImageView ivPhoto;
		public TextView tvNickname;
		public TextView tvTitle;
		public TextView tvContent;
		public TextView tvTime;
	}
	
	
}
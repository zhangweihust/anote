package com.archermind.note.Adapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.archermind.note.R;
import com.archermind.note.Utils.DateTimeUtils;
import com.archermind.note.Utils.InformationData;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class InformationAdapter extends BaseAdapter
{
	private Context mCtx;
	private ArrayList<InformationData> mDatas = new ArrayList<InformationData>();;
	private boolean existsPrompt = false;
	
	public InformationAdapter(Context ctx, ArrayList<InformationData> datas)
	{
		mCtx = ctx;
		mDatas.addAll(datas);
	}
	
	@Override
	public int getCount() {
		return mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return mDatas.get(position);
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
	    	Bitmap bitmap = getHttpBitmap(data.photo);
	    	if(bitmap != null){
	    		item.ivPhoto.setImageBitmap(bitmap);
	    	}
	    }
	    if(data.nickname != null){
	    	item.tvNickname.setText(data.nickname);
	    }else{
	    	item.tvNickname.setVisibility(View.GONE);
	    }
	    
	    if(data.title != null){
	    	item.tvTitle.setText("回复：" + data.title);
	    }else{
	    	item.tvTitle.setVisibility(View.INVISIBLE);
	    }
	    
	    item.tvContent.setText(data.content);
	    if(data.time < DateTimeUtils.getToday(Calendar.AM, System.currentTimeMillis())){
	    	item.tvTime.setText(DateTimeUtils.time2String("yyyy年MM月dd日 HH:mm", data.time*1000));
	    }else{
	    	item.tvTime.setText(DateTimeUtils.time2String("HH:mm", data.time));
	    }
	    
		
		return convertView;
	}

	public void setNoInformationPrompt(long time)
	{
		InformationData data = new InformationData();
		data.content = "暂时没有回复，下拉刷新！";
 		data.time = time/1000;
		
		mDatas.clear();
		mDatas.add(data);
		existsPrompt = true;
		notifyDataSetChanged();
	}
	
	public void addPreData(List<InformationData> predata)
	{
		int i = 0;
		int size = predata.size();
		if (size <= 0)
		{
			return;
		}
		if (existsPrompt)
		{
			mDatas.clear();
			existsPrompt = false;
		}
		while (i < size)
		{
			mDatas.add(i, predata.get(i));
			i++;
		}
		notifyDataSetChanged();
	}
	
	public void addAfterData(List<InformationData> afterdata)
	{
		if (afterdata.size() <= 0)
		{
			return;
		}
		if (existsPrompt)
		{
			mDatas.clear();
			existsPrompt = false;
		}
		mDatas.addAll(afterdata);
		notifyDataSetChanged();
	}
	
	public long getLatestTime()
	{
		long time = 0;
		if ((mDatas.size() > 0 && !existsPrompt)||(mDatas.size() > 1))
		{
			time = mDatas.get(0).time;
		}
		
		return time;
	}
	
	public long getEarlistTime()
	{
		long time = System.currentTimeMillis()/1000;
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
	
	public static Bitmap getHttpBitmap(String url) {
		
		Bitmap bitmap = null;
		try {
			URL myFileUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
			conn.setConnectTimeout(0);
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
			conn.disconnect();
		} catch (IOException e) {
			//e.printStackTrace();
		} catch (Exception e){
			//e.printStackTrace();
		}
			return bitmap;
		}
}
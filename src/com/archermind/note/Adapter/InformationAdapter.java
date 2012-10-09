package com.archermind.note.Adapter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.archermind.note.R;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Image.SmartImageView;
import com.archermind.note.Utils.DateTimeUtils;
import com.archermind.note.Utils.InformationData;

import android.R.bool;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
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
		//System.out.println("==================information adapter getView ==============");
		if(convertView == null){
			convertView = LayoutInflater.from(mCtx).inflate(R.layout.information_listview_item, null);
			item = new ListItemsView();
			item.ivPhoto = (SmartImageView) convertView.findViewById(R.id.iv_photo);
			item.tvNickname = (TextView) convertView.findViewById(R.id.tv_nickname);
			item.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
			item.tvContent = (TextView) convertView.findViewById(R.id.tv_content);
			item.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
			convertView.setTag(R.layout.information_listview_item, item);
		}else{
			item = (ListItemsView)convertView.getTag(R.layout.information_listview_item);
		}
	    InformationData data = mDatas.get(position);
	    if(data != null){
	    
	    if(data.photo != null){
	    	item.ivPhoto.setImageUrl(data.photo, R.drawable.default_photo, R.drawable.default_photo);
	    }
	    
	    item.tvNickname.setText(data.nickname);
	    
	    item.tvTitle.setText("回复：" + data.title);

	    
	   if(data.content.contains(":face")){
		    item.tvContent.setText("");
	    	Pattern p = Pattern.compile(":face_[0-9]{1,2}:");
	        Matcher m = p.matcher(data.content);
	        System.out.println("content lenght : " + data.content.length() + ", " + data.content);
	        Class drawable  =  R.drawable.class;
	        int lastIndex = 0;
	    	while(m.find()){		 
	         Field field = null;	         
	         try {
	        	 System.out.println(m.group() + ", " + m.start() + ", " + m.end());
	             field = drawable.getField(m.group().replaceAll(":", ""));
	             int r_id = field.getInt(field.getName());
	             Drawable d =  mCtx.getResources().getDrawable(r_id); 
	     		 d.setBounds(0, 0, 48, 48);
	     		 ImageSpan span = new ImageSpan(d);
	     		 SpannableString spanStr = new SpannableString(m.group());
	     		spanStr.setSpan(span, 0, spanStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	     		int startIndex = m.start();
	     		startIndex = startIndex < 0 ? 0 : startIndex;
	     		item.tvContent.append(data.content, lastIndex, startIndex);
	     		lastIndex = m.end();
	     		item.tvContent.append(spanStr);
	         }catch(Exception e){
	        	 
	         }
	           
	    	}
    	    if(lastIndex != data.content.length()){
	    		item.tvContent.append(data.content, lastIndex, data.content.length()-1);
	    	}
	    }else{
	    	item.tvContent.setText(data.content);
	    }
	    if(data.time*1000 < DateTimeUtils.getToday(Calendar.AM, System.currentTimeMillis())){
	    	item.tvTime.setText(DateTimeUtils.time2String("yyyy年MM月dd日 HH:mm  ", data.time*1000));
	    }else{
	    	item.tvTime.setText(DateTimeUtils.time2String("今天 HH:mm  ", data.time*1000));
	    }
	    EventArgs args = new EventArgs();
        args.putExtra("time", data.time);
        convertView.setTag(args);
	    }
		return convertView;
	}

	
	
	public void setNoInformationPrompt(long time)
	{
		InformationData data = new InformationData();
		data.content = "暂时没有回复，下拉刷新！";
 		data.time = time/1000;
		data.title = "无主题";
		mDatas.clear();
		mDatas.add(data);
		existsPrompt = true;
		notifyDataSetChanged();
	}
	
	public void addPreData(List<InformationData> predata)
	{

		if (predata==null ||predata.size() <= 0)
		{
			return;
		}
		if (existsPrompt)
		{
			mDatas.clear();
			existsPrompt = false;
		}
		int i = 0;
		while (i < predata.size())
		{
			mDatas.add(i, predata.get(i));
			i++;
		}
		notifyDataSetChanged();
	}
	
	public void addAfterData(List<InformationData> afterdata)
	{
		if (afterdata == null || afterdata.size() <= 0)
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
	

	public boolean isEmpty()
	{
		return mDatas.isEmpty();
	}
	
	public ArrayList<InformationData> getDatas(){
		return mDatas;
	}
	
	
	public long getLatestTime(){		
		if(mDatas != null && !mDatas.isEmpty()){
			InformationData in = mDatas.get(0);
			return in.time;
		}
		return 0;
	}
	
	public long getEarlistTime(){
		if(mDatas != null && !mDatas.isEmpty()){
			InformationData in = mDatas.get(mDatas.size()-1);
			return in.time;
		}
		return System.currentTimeMillis()/1000;
	}
	
	public final class ListItemsView{
		public SmartImageView ivPhoto;
		public TextView tvNickname;
		public TextView tvTitle;
		public TextView tvContent;
		public TextView tvTime;
	}

}
package com.archermind.note.Adapter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Provider.DatabaseManager;
import com.archermind.note.Screens.EditNoteScreen;
import com.archermind.note.Screens.MainScreen;
import com.archermind.note.Screens.HomeScreen;
import com.archermind.note.Screens.ShareScreen;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.DateTimeUtils;
import com.archermind.note.Utils.DensityUtil;
import com.archermind.note.Utils.SetSystemProperty;
import com.archermind.note.gesture.AmGesture;
import com.archermind.note.gesture.AmGestureLibraries;
import com.archermind.note.gesture.AmGestureLibrary;

import android.R.integer;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LocalNoteOnedayAdapter  extends CursorAdapter {
	private LayoutInflater mInflater;
	private static CharSequence[] transactItemsSign = {"分享", "标记", "删除"};
	private static CharSequence[] transactItemsUnsign = {"分享", "取消标记", "删除"};
	private AlertDialog.Builder mbuilder;
	private AlertDialog madTransact;
	private static DatabaseManager mDb = ServiceManager.getDbManager();
	private Context mContext;
	
	public LocalNoteOnedayAdapter(Context context, Cursor c) {
		super(context, c);
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mbuilder = new AlertDialog.Builder(context);  
		mbuilder.setTitle("请操作");
	}

	@Override
	public void bindView(View view, final Context context, final Cursor cursor) {
		final NoteOneDayItem item = (NoteOneDayItem) view.getTag(R.layout.note_oneday_listview_item);
		final String title = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_TITLE));
		System.out.println("LocalNoteOnedayAdapter " + title);
		final long time = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME));
		final boolean lastFlag = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_LAST_FLAG)) == 1;
		final int isSigned = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CONTENT_SIGNED));
		item.tvTitle.setText(title);
		item.tvTime.setText(DateTimeUtils.time2String("HH:mm", time));
		
		if(lastFlag){
			item.tvDays.setVisibility(View.VISIBLE);
			//item.tvDate.setVisibility(View.VISIBLE);
			long durationN = (time - DateTimeUtils.getToday(Calendar.PM, System.currentTimeMillis()));
			long durationP = (DateTimeUtils.getToday(Calendar.AM, System.currentTimeMillis()) - time);
			long tmp = 0;
			long duration = 0;
			if(durationN<=0 && durationP<=0){
				item.tvDays.setText("今天");
			}else if(durationP > 0){
				tmp = durationP%(1000 * 60 * 60 * 24);
				if(tmp == 0){
					duration = durationP/(1000 * 60 * 60 * 24);
				}else{
					duration = durationP/(1000 * 60 * 60 * 24) + 1;
				}
				item.tvDays.setText(duration+"天前");
			}else if(durationN > 0 ){
				tmp = durationN%(1000 * 60 * 60 * 24);
				if(tmp == 0){
					duration = durationN/(1000 * 60 * 60 * 24);
				}else{
					duration = durationN/(1000 * 60 * 60 * 24) + 1;
				}
				item.tvDays.setText(duration+"天后");
			}
			//item.tvWeekDay.setVisibility(View.VISIBLE);
/*			String weekday = DateTimeUtils.time2String("EEEE", time);
			String day = DateTimeUtils.time2String("dd", time);
			SpannableString sp = new SpannableString(day + "\n" + weekday);
			sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, day.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			sp.setSpan(new AbsoluteSizeSpan((int) context.getResources().getDimension(R.dimen.home_screen_date_info_text_size)) , 0, day.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			sp.setSpan(new AbsoluteSizeSpan((int) context.getResources().getDimension(R.dimen.home_screen_weekday_info_text_size)) , day.length() + 1, day.length()+weekday.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			item.tvDate.setText(sp);*/
/*			view.setPadding(0, DensityUtil.dip2px(context, 15), 0, 0);*/
		} else {
		/*	view.setPadding(0, 0, 0, 0);*/
			item.tvDays.setVisibility(View.GONE);
			//item.tvDate.setVisibility(View.INVISIBLE);
			//item.tvWeekDay.setVisibility(View.GONE);
			
		}
		if(isSigned == 1){
			item.ivIsSigned.setVisibility(View.VISIBLE);
		} else {
			item.ivIsSigned.setVisibility(View.GONE);
		}
		
		//item.rlNoteItem.setPadding(DensityUtil.dip2px(context, 5) + 1, DensityUtil.dip2px(context, 10), 0, 0);
		final int id = cursor.getInt((cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_ID)));
		final String notePath = cursor.getString((cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_LOCAL_CONTENT)));
		final long lastEditTime = Long.parseLong(cursor.getString((cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE_UPDATE_TIME))));
		if (lastEditTime != 0) {
			Log.d("=TTT=","lastEditTime = " + lastEditTime);
			Calendar timeCal = Calendar.getInstance(Locale.CHINA); 
			timeCal.setTimeInMillis(lastEditTime);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			String timeStr = "最后编辑于："+sdf.format(timeCal.getTime());
			item.tvLastEdit.setText(timeStr);
		}
		item.rlNoteInfo.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("=== longclick ===" + id + "is signed : " + isSigned + " lastFlag :" + lastFlag);

				CharSequence[] items = null;
				if(isSigned == 1){
					items = transactItemsUnsign;
				}else{
					items = transactItemsSign;
				}
				mbuilder.setItems(items, new DialogInterface.OnClickListener() {  
				    public void onClick(DialogInterface dialog, int item) {  
				        System.out.println(item + "ID : " + id + " isSigned : " + isSigned);
				        dialog.dismiss();
				        switch (item) {
						case 0:
							if (!NoteApplication.getInstance().isLogin()) {
								Toast.makeText(mContext, R.string.no_login_info, Toast.LENGTH_SHORT).show();
								return;
							}
							ArrayList<String> picPathList = EditNoteScreen.getNotePictureFromZip(cursor.getString(cursor
									.getColumnIndex(DatabaseHelper.COLUMN_NOTE_LOCAL_CONTENT)));
							String sid = cursor.getString((cursor
									.getColumnIndex(DatabaseHelper.COLUMN_NOTE_SERVICE_ID)));
							String action = sid == null ? "A" : "M";
							Intent intent = new Intent(mContext,
									ShareScreen.class);
							intent.putStringArrayListExtra("picpathlist",
									picPathList);
							intent.putExtra("noteid", String.valueOf(id));
							intent.putExtra("title", title);
							intent.putExtra("action", action);
							intent.putExtra("sid", sid);
							mContext.startActivity(intent);
							break;
						case 1:
							ContentValues contentValues = new ContentValues();					
							contentValues = new ContentValues();
							contentValues.put(DatabaseHelper.COLUMN_NOTE_CONTENT_SIGNED,isSigned==0);
							mDb.updateLocalNotes(contentValues, id);
							break;
						case 2:
							mDb.deleteLocalNOTEs(id,time);
							if (notePath != null) {
							    File file = new File(notePath);
							    if (file.exists()) {
							    	file.delete();
							    }
							}
							break;
						default:
							break;
						}
				    }  
				 });  
				madTransact = mbuilder.create(); 
				madTransact.show();
				return false;
			}
		});
		
		// 笔记预览
		
		String[] picIndex  = new String[1];
		String contextStr = EditNoteScreen.readTextFromZip(notePath,picIndex);
		String picStr = SetSystemProperty.readZipValue(notePath, picIndex[0]);
		item.tvNoteContent.setText("");
		if (contextStr != null) {
			if (picStr != null && !"".equals(picStr)) {
				Bitmap bmp = EditNoteScreen.decodeFile(new File(picStr),DensityUtil.dip2px(context,240),DensityUtil.dip2px(context,240));
				Drawable drawable = new BitmapDrawable(bmp);
			    drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
	            
	  		    ImageSpan span = new ImageSpan(drawable,ImageSpan.ALIGN_BOTTOM);
				SpannableString spanStr = new SpannableString("pic");
				spanStr.setSpan(span, 0, spanStr.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				item.tvNoteContent.append(spanStr);
				item.tvNoteContent.append("\n");
			}
		    InputStream in = new ByteArrayInputStream(contextStr.getBytes());
		    AmGestureLibrary store = EditNoteScreen.readGestureFromZip(notePath);
			if (store == null) {
			}
		    
		    InputStreamReader inr = new InputStreamReader(in);
	        BufferedReader reader = new BufferedReader(inr);
	        String line = null;
	        int wordNum = 0;
	        int retNum = 0;
	        try {
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("str:")) {
			    		String str = line.substring("str:".length()).replace("\\n", "\n");
			    		if (str.length() > 20 - wordNum) {
			    			str = str.substring(0,20 - wordNum);
			    			wordNum = 20;
			    		} else {
			    			wordNum += str.length();
			    		}
			    		for (int i = 0;i < str.length();i++) {
			    			if (str.charAt(i) == '\n') {
			    				if (++retNum >= 2) {
			    					if (i < 20 - wordNum) {
			    					    str = str.substring(0, i);
			    					}
			    					break;
			    				}
			    			}
			    		}
			    		item.tvNoteContent.append(str);
			    	} else if (line.startsWith("hw:")) {
			    		String value = line.substring("hw:".length(), line.length());
			        	
			        	if (store != null && store.getGestures(value) != null) {
				        	AmGesture gesture = store.getGestures(value).get(0);
				        	if (gesture != null) {
				        		Bitmap bmp = Bitmap.createBitmap(DensityUtil.dip2px(context,50), DensityUtil.dip2px(context,71), Bitmap.Config.ARGB_8888);;
					        	bmp.eraseColor(0x00000000);
					        	Canvas canvas = new Canvas(bmp);
					        	canvas.drawBitmap(gesture.toBitmap(DensityUtil.dip2px(context,44), DensityUtil.dip2px(context,44), 0, gesture.getGesturePaintColor()), DensityUtil.dip2px(context,3), DensityUtil.dip2px(context,20), null);
					        	Drawable drawable = new BitmapDrawable(bmp);
					            drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
					  		    ImageSpan span = new ImageSpan(drawable,ImageSpan.ALIGN_BOTTOM);
					  			SpannableString spanStr = new SpannableString(value);
					  			spanStr.setSpan(span, 0, spanStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					  			item.tvNoteContent.append(spanStr);
					  			wordNum++;
				        	}
			        	}
			    	} else if (line.startsWith("face:")) {
			    		String value = line.substring("face:".length(), line.length());
			    		if (value.endsWith("face_a1")) {
			    			appendFace(R.drawable.face_a1,item.tvNoteContent);
			    		} else if (value.endsWith("face_a2")) {
			    			appendFace(R.drawable.face_a2,item.tvNoteContent);
			    		} else if (value.endsWith("face_a3")) {
			    			appendFace(R.drawable.face_a3,item.tvNoteContent);
			    		} else if (value.endsWith("face_a4")) {
			    			appendFace(R.drawable.face_a4,item.tvNoteContent);
			    		} else if (value.endsWith("face_a5")) {
			    			appendFace(R.drawable.face_a5,item.tvNoteContent);
			    		} else if (value.endsWith("face_a6")) {
			    			appendFace(R.drawable.face_a6,item.tvNoteContent);
			    		} else if (value.endsWith("face_a7")) {
			    			appendFace(R.drawable.face_a7,item.tvNoteContent);
			    		} else if (value.endsWith("face_a8")) {
			    			appendFace(R.drawable.face_a8,item.tvNoteContent);
			    		}
			    		wordNum++;
			    	} else if (line.startsWith("gft:")) {
			    		break;
			    	}
					if (wordNum >= 20 || retNum >= 2) {
						break;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		item.rlNoteInfo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra("notePath", notePath);
				intent.putExtra("isNewNote", false);
				intent.putExtra("noteID", id);
				intent.putExtra("title", title);
				intent.setClass(MainScreen.mContext, EditNoteScreen.class);
				MainScreen.mContext.startActivity(intent);
			}
		});
		EventArgs args = new EventArgs();
		args.putExtra("time", time);
		args.putExtra("lastFlag", lastFlag);
		view.setTag(args);
	}

	
	private void appendFace(int id,TextView tv) {
		String fname = MainScreen.mContext.getResources().getResourceName(id);
        fname = fname.substring(fname.lastIndexOf("/") + 1, fname.length());
		Drawable drawable =  MainScreen.mContext.getResources().getDrawable(id); 
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		ImageSpan span = new ImageSpan(drawable);
		SpannableString spanStr = new SpannableString(fname);
		spanStr.setSpan(span, 0, spanStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.append(spanStr);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = mInflater.inflate(R.layout.note_oneday_listview_item, null);
		NoteOneDayItem item = new NoteOneDayItem();
		//item.tvDate = (TextView) view.findViewById(R.id.tv_oneday_date);
		item.tvDays = (TextView) view.findViewById(R.id.tv_oneday_days);
		item.tvTitle = (TextView) view.findViewById(R.id.tv_oneday_title);
		item.ivIsSigned = (ImageView) view.findViewById(R.id.iv_oneday_is_signed);
		item.tvTime = (TextView) view.findViewById(R.id.tv_oneday_time);
		//item.rlDay= (RelativeLayout) view.findViewById(R.id.rl_day);
		item.rlNoteInfo = (RelativeLayout) view.findViewById(R.id.rl_oneday_note_info);
		item.rlNoteItem = (RelativeLayout) view.findViewById(R.id.rl_note_item);
		item.tvNoteContent = (TextView) view.findViewById(R.id.tv_note_content);
		item.tvLastEdit = (TextView) view.findViewById(R.id.tv_note_last_edit);
		view.setTag(R.layout.note_oneday_listview_item,item);
		return view;
	}
	
	private class NoteOneDayItem{
		//private TextView tvDate;
		private TextView tvDays;
		private TextView tvTitle;
		private ImageView ivIsSigned;
		private TextView tvTime;
		private TextView tvNoteContent;
		private TextView tvLastEdit;
		//private RelativeLayout rlDay;
		private RelativeLayout rlNoteInfo;
		private RelativeLayout rlNoteItem;
	}


}

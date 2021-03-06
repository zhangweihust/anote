package com.android.note.Adapter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import com.android.note.Events.EventArgs;
import com.android.note.Provider.DatabaseHelper;
import com.android.note.Screens.EditNoteScreen;
import com.android.note.Screens.MainScreen;
import com.android.note.Utils.DateTimeUtils;
import com.android.note.Utils.DensityUtil;
import com.android.note.Utils.SetSystemProperty;
import com.android.note.gesture.AmGesture;
import com.android.note.gesture.AmGestureLibrary;
import com.archermind.note.R;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LocalNoteOnedayAdapter extends CursorAdapter {
	private LayoutInflater mInflater;
	private Context mContext;
	private int gesture_width;
	private int gesture_height; // 手势的大小
	private static final String TAG = "LocalNoteOnedayAdapter";

	public LocalNoteOnedayAdapter(Context context, Cursor c) {
		super(context, c);
		mInflater = LayoutInflater.from(context);
		mContext = context;
	}

	@Override
	public void bindView(View view, final Context context, final Cursor cursor) {
		NoteOneDayItem item = (NoteOneDayItem) view
				.getTag(R.layout.note_oneday_listview_item);
		String title = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_TITLE));
		System.out.println("LocalNoteOnedayAdapter " + title);
		long time = cursor.getLong(cursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CREATE_TIME));
		boolean lastFlag = cursor.getInt(cursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_LAST_FLAG)) == 1;
		int isSigned = cursor.getInt(cursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_CONTENT_SIGNED));
		item.tvTitle.setText(title);
		item.tvTime.setText(DateTimeUtils.time2String("HH:mm", time));
		// System.out.println("============local oneday adapter=============");
		if (lastFlag) {
			item.tvDays.setVisibility(View.VISIBLE);
			// item.tvDate.setVisibility(View.VISIBLE);
			long durationN = (time - DateTimeUtils.getToday(Calendar.PM,
					System.currentTimeMillis()));
			long durationP = (DateTimeUtils.getToday(Calendar.AM,
					System.currentTimeMillis()) - time);
			long tmp = 0;
			long duration = 0;
			if (durationN <= 0 && durationP <= 0) {
				item.tvDays.setText("今天");
			} else if (durationP > 0) {
				tmp = durationP % (1000 * 60 * 60 * 24);
				if (tmp == 0) {
					duration = durationP / (1000 * 60 * 60 * 24);
				} else {
					duration = durationP / (1000 * 60 * 60 * 24) + 1;
				}
				item.tvDays.setText(duration + "天前");
			} else if (durationN > 0) {
				tmp = durationN % (1000 * 60 * 60 * 24);
				if (tmp == 0) {
					duration = durationN / (1000 * 60 * 60 * 24);
				} else {
					duration = durationN / (1000 * 60 * 60 * 24) + 1;
				}
				item.tvDays.setText(duration + "天后");
			}
		} else {
			item.tvDays.setVisibility(View.GONE);
		}

		if (isSigned == 1) {
			item.ivIsSigned.setVisibility(View.VISIBLE);
		} else {
			item.ivIsSigned.setVisibility(View.GONE);
		}

		// item.rlNoteItem.setPadding(DensityUtil.dip2px(context, 5) + 1,
		// DensityUtil.dip2px(context, 10), 0, 0);
		String notePath = cursor.getString((cursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_LOCAL_CONTENT)))
				+ ".note";
		long lastEditTime = Long.parseLong(cursor.getString((cursor
				.getColumnIndex(DatabaseHelper.COLUMN_NOTE_UPDATE_TIME))));
		if (lastEditTime != 0) {
			Log.d("=TTT=", "lastEditTime = " + lastEditTime);
			Calendar timeCal = Calendar.getInstance(Locale.CHINA);
			timeCal.setTimeInMillis(lastEditTime);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			String timeStr = "最后编辑于：" + sdf.format(timeCal.getTime());
			item.tvLastEdit.setText(timeStr);
		}

		// 笔记预览

		ArrayList<String> noteInfoList = EditNoteScreen
				.readNoteInfoFromZip(notePath);
		if (noteInfoList != null) {
			for (String str : noteInfoList) {
				if (str.startsWith("gesture_width:")) {
					gesture_width = Integer.parseInt(str.substring(str
							.indexOf(":") + 1));
				} else if (str.startsWith("gesture_height:")) {
					gesture_height = Integer.parseInt(str.substring(str
							.indexOf(":") + 1));
				}
			}
			Log.i(TAG, "width:" + gesture_width + "    " + "height"
					+ gesture_height);
		} else {
			Log.e(TAG, "noteInfoList is null");
		}

		String[] picIndex = new String[1];
		String contextStr = EditNoteScreen.readTextFromZip(notePath, picIndex);
		Log.i("key", "" + picIndex[0]);
		// String picPath = SetSystemProperty.readZipValue(notePath,
		// picIndex[0]);
		// Log.i("path", ""+picPath);
		item.tvNoteContent.setText("");
		if (contextStr != null) {
			if (picIndex[0] != null && !"".equals(picIndex[0])) {
				Bitmap bmp = EditNoteScreen.readPicFromZip(notePath,
						picIndex[0]);
				item.ivPic.setVisibility(View.VISIBLE);
				item.ivPic.setImageBitmap(bmp);
			} else {
				item.ivPic.setVisibility(View.GONE);
			}
			InputStream in = new ByteArrayInputStream(contextStr.getBytes());
			AmGestureLibrary store = EditNoteScreen
					.readGestureFromZip(notePath);
			if (store == null) {
			}

			// 插入正文
			InputStreamReader inr = new InputStreamReader(in);
			BufferedReader reader = new BufferedReader(inr);
			String line = null;
			int wordNum = 0;
			int retNum = 0;
			try {
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("str:")) {
						String str = line.substring("str:".length()).replace(
								"\\n", "\n");
						if (str.length() > 20 - wordNum) {
							str = str.substring(0, 20 - wordNum);
							wordNum = 20;
						} else {
							wordNum += str.length();
						}
						for (int i = 0; i < str.length(); i++) {
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
						String value = line.substring("hw:".length(),
								line.length());

						if (store != null && store.getGestures(value) != null) {
							AmGesture gesture = store.getGestures(value).get(0);
							if (gesture != null) {
								int height = 581;
								int width = 480;
								if (gesture_height != 0 && gesture_width != 0) {
									height = gesture_height;
									width = gesture_width;
								}
                                try{
									Bitmap gestrueBmp = gesture.toBitmapWidthNotAve(
											DensityUtil.dip2px(mContext, AmGesture.PIC_HEIGHT), DensityUtil.dip2px(mContext, AmGesture.INSET),
											gesture.getGesturePaintColor(),
											height, width);
									Drawable drawable = new BitmapDrawable(gestrueBmp);
									drawable.setBounds(0, 0,
											drawable.getIntrinsicWidth(),
											drawable.getIntrinsicHeight());
									ImageSpan span = new ImageSpan(drawable,
											ImageSpan.ALIGN_BOTTOM);
									SpannableString spanStr = new SpannableString(
											value);
									spanStr.setSpan(span, 0, spanStr.length(),
											Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
									item.tvNoteContent.append(spanStr);
									wordNum++;
                                }catch (Exception e) {
									// TODO: handle exception
								}catch(OutOfMemoryError e){
					            	e.printStackTrace();
					            }
							}
						}
					} else if (line.startsWith("face:")) {
						String value = line.substring("face:".length(),
								line.length());
						if (value.endsWith("face_0")) {
							appendFace(R.drawable.face_0, item.tvNoteContent);
						} else if (value.endsWith("face_1")) {
							appendFace(R.drawable.face_1, item.tvNoteContent);
						} else if (value.endsWith("face_2")) {
							appendFace(R.drawable.face_2, item.tvNoteContent);
						} else if (value.endsWith("face_3")) {
							appendFace(R.drawable.face_3, item.tvNoteContent);
						} else if (value.endsWith("face_4")) {
							appendFace(R.drawable.face_4, item.tvNoteContent);
						} else if (value.endsWith("face_5")) {
							appendFace(R.drawable.face_5, item.tvNoteContent);
						} else if (value.endsWith("face_6")) {
							appendFace(R.drawable.face_6, item.tvNoteContent);
						} else if (value.endsWith("face_7")) {
							appendFace(R.drawable.face_7, item.tvNoteContent);
						} else if (value.endsWith("face_8")) {
							appendFace(R.drawable.face_8, item.tvNoteContent);
						} else if (value.endsWith("face_9")) {
							appendFace(R.drawable.face_9, item.tvNoteContent);
						} else if (value.endsWith("face_10")) {
							appendFace(R.drawable.face_10, item.tvNoteContent);
						} else if (value.endsWith("face_11")) {
							appendFace(R.drawable.face_11, item.tvNoteContent);
						} else if (value.endsWith("face_12")) {
							appendFace(R.drawable.face_12, item.tvNoteContent);
						} else if (value.endsWith("face_13")) {
							appendFace(R.drawable.face_13, item.tvNoteContent);
						} else if (value.endsWith("face_14")) {
							appendFace(R.drawable.face_14, item.tvNoteContent);
						} else if (value.endsWith("face_15")) {
							appendFace(R.drawable.face_15, item.tvNoteContent);
						} else if (value.endsWith("face_16")) {
							appendFace(R.drawable.face_16, item.tvNoteContent);
						} else if (value.endsWith("face_17")) {
							appendFace(R.drawable.face_17, item.tvNoteContent);
						} else if (value.endsWith("face_18")) {
							appendFace(R.drawable.face_18, item.tvNoteContent);
						} else if (value.endsWith("face_19")) {
							appendFace(R.drawable.face_19, item.tvNoteContent);
						}
						wordNum++;
					} else if (line.startsWith("gft:")) {
						break;
					}
					if (wordNum >= 20 || retNum >= 2) {
						break;
					}
				}
				reader.close();
				inr.close();
				in.close();
			} catch (IOException e) {
				try {
					reader.close();
					inr.close();
					in.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				try {
					reader.close();
					inr.close();
					in.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	private void appendFace(int id, TextView tv) {
		String fname = MainScreen.mContext.getResources().getResourceName(id);
		fname = fname.substring(fname.lastIndexOf("/") + 1, fname.length());
		Drawable drawable = MainScreen.mContext.getResources().getDrawable(id);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		ImageSpan span = new ImageSpan(drawable);
		SpannableString spanStr = new SpannableString(fname);
		spanStr.setSpan(span, 0, spanStr.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.append(spanStr);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = mInflater.inflate(R.layout.note_oneday_listview_item, null);
		NoteOneDayItem item = new NoteOneDayItem();
		item.tvDays = (TextView) view.findViewById(R.id.tv_oneday_days);
		item.tvTitle = (TextView) view.findViewById(R.id.tv_oneday_title);
		item.ivIsSigned = (ImageView) view
				.findViewById(R.id.iv_oneday_is_signed);
		item.tvTime = (TextView) view.findViewById(R.id.tv_oneday_time);
		item.tvNoteContent = (TextView) view.findViewById(R.id.tv_note_content);
		item.tvLastEdit = (TextView) view.findViewById(R.id.tv_note_last_edit);
		item.ivPic = (ImageView) view.findViewById(R.id.iv_oneday_pic);
		item.ivPic.setVisibility(View.GONE);
		view.setTag(R.layout.note_oneday_listview_item, item);
		return view;
	}

	private class NoteOneDayItem {
		private TextView tvDays;
		private TextView tvTitle;
		private ImageView ivIsSigned;
		private ImageView ivPic;
		private TextView tvTime;
		private TextView tvNoteContent;
		private TextView tvLastEdit;
	}

}

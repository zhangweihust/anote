package com.archermind.note.Screens;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.archermind.note.R;
import com.archermind.note.Adapter.FaceAdapter;
import com.archermind.note.Utils.DensityUtil;
import com.archermind.note.Utils.GenerateName;
import com.archermind.note.Utils.ServerInterface;
import com.archermind.note.Utils.SetSystemProperty;
import com.archermind.note.editnote.ColorPickerDialog;
import com.archermind.note.editnote.ColorPickerDialog.OnColorChangedListener;
import com.archermind.note.editnote.MyEditText;
import com.archermind.note.gesture.AmGesture;
import com.archermind.note.gesture.AmGestureLibraries;
import com.archermind.note.gesture.AmGestureLibrary;
import com.archermind.note.gesture.AmGestureOverlayView;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class EditNoteScreen  extends Screen implements OnClickListener {

	private AmGestureOverlayView gestureview = null;
	private MyEditText myEdit = null;
	
	//最底下一排的四个按钮
	private ImageButton edit_insert = null;
	private ImageButton edit_delete = null;
	private ImageButton edit_input_type = null;
	private ImageButton edit_setting = null;
	
    private ImageButton mWeatherImg = null;
    
    private ImageButton saveButton = null;
    
    private GridView faceGridview = null;
    private FaceAdapter faceAdapter = null;
    
    private SeekBar thickness_seekbar = null;
	
    private Button cameraButton = null; 
    private Button albumButton = null;
    
	private int inType = 0;
	private AmGesture mGesture;
	private static final float LENGTH_THRESHOLD = 40.0f;
	private InputMethodManager  imm = null;
	
	private LinearLayout edit_type = null; // 输入方式
	private LinearLayout pic_type = null; // 图片的类型：表情/图片
	private LinearLayout setting_type = null; //设置
	
    private LinearLayout edit_type_graffit;// 涂鸦输入
    private LinearLayout edit_type_soft;// 键盘输入
    private LinearLayout edit_type_handwrite;//手写输入
    private LinearLayout pic_type_face;//表情输入
    private LinearLayout pic_type_pic;//图片插入
    private LinearLayout setting_color;//画笔颜色设置
    private LinearLayout setting_thickness;//画笔粗细设置
    private LinearLayout edit_insert_space;//插入空格
    private LinearLayout edit_insert_newline;//插入空行
	
	private boolean isgraffit_erase = false;
	
	private boolean isInputTypeShow = false;
	private boolean isPicTypeShow = false;
	private boolean isSettingTypeShow = false;
	
	private Dialog thickness_dialog = null;
	private Dialog picchoose_dialog = null;
//	private Dialog diary_category_dialog = null;
	private Dialog weather_dialog = null;
	
	public static final int PICINSERTSTATE = 1;
	public static final int FACEINSERTSTATE = 2;
	public static final int SOFTINPUTSTATE = 3;
	public static final int GRAFFITINSERTSTATE = 4;
	public static final int HANDWRITINGSTATE = 5;
	
	private final int CAMERA_RESULT = 8888;
	private final int ALBUM_RESULT = 9999;
	
	private String imageFilePath = null;
	
	public static int mState = SOFTINPUTSTATE;
	
	private GesturesProcessorHandWrite handWriteListener;
	
	private final int MIN_STROKEWIDTH = 12;
	private final int MAX_STROKEWIDTH = 30;
	private float mStrokeWidth = MIN_STROKEWIDTH;
	
	private LinearLayout mWeatherLayout = null;
	
	private int mViewId = -1;
	
//	private TextView dropdown_tv = null;
	private TextView weather_tv = null;
	
	private Handler handler = new Handler();
	
	private GenerateName gestureName = new GenerateName();
	private GenerateName picName = new GenerateName();
	
	private File gestureFile = null;
	
	private AmGestureLibrary mStore = null;
	
	private LinkedHashMap<String, String> mPicMap = null;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.edit_note);
		gestureview = (AmGestureOverlayView) findViewById(R.id.gestureview);
		gestureview.setVisibility(View.GONE);
		gestureview.addOnGestureListener(new GesturesProcessorHandWrite()); 
		
		myEdit = (MyEditText) findViewById(R.id.editText_view);
		inType = myEdit.getInputType(); 
		
		edit_insert = (ImageButton) findViewById(R.id.edit_insert);
		edit_delete = (ImageButton) findViewById(R.id.edit_delete);
		edit_input_type = (ImageButton) findViewById(R.id.edit_inputtype);
		edit_setting = (ImageButton) findViewById(R.id.edit_setting);
		
		edit_insert.setOnClickListener(this);
		edit_delete.setOnClickListener(this);
		edit_input_type.setOnClickListener(this);
		edit_setting.setOnClickListener(this);
		
		edit_type = (LinearLayout) findViewById(R.id.edit_input_type);
		edit_type.setVisibility(View.GONE);
		
		pic_type = (LinearLayout) findViewById(R.id.edit_pic_type);
		pic_type.setVisibility(View.GONE);
		
		setting_type = (LinearLayout) findViewById(R.id.edit_setting_type);
		setting_type.setVisibility(View.GONE);
		
		edit_type_graffit = (LinearLayout) findViewById(R.id.edit_type_graffit);
		edit_type_soft = (LinearLayout) findViewById(R.id.edit_type_soft);
		edit_type_handwrite = (LinearLayout) findViewById(R.id.edit_type_handwrite);
		pic_type_face = (LinearLayout) findViewById(R.id.edit_insert_face);
		pic_type_pic = (LinearLayout) findViewById(R.id.edit_insert_pic);
		setting_color = (LinearLayout) findViewById(R.id.edit_setting_color);
		setting_thickness = (LinearLayout) findViewById(R.id.edit_setting_thickness);
		edit_insert_space = (LinearLayout) findViewById(R.id.edit_insert_space);
		edit_insert_newline = (LinearLayout) findViewById(R.id.edit_insert_newline);
		
		edit_type_graffit.setOnClickListener(this);
		edit_type_soft.setOnClickListener(this);
		edit_type_handwrite.setOnClickListener(this);
		pic_type_face.setOnClickListener(this);
		pic_type_pic.setOnClickListener(this);
		setting_color.setOnClickListener(this);
		setting_thickness.setOnClickListener(this);
		edit_insert_space.setOnClickListener(this);
		edit_insert_newline.setOnClickListener(this);
		
        initFaceAdapter();
        faceGridview.setVisibility(View.GONE);
        
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		
		weather_tv = (TextView) findViewById(R.id.edit_weather_textview);
		
		mWeatherLayout = (LinearLayout) findViewById(R.id.edit_weather_linearlayout);
		mWeatherLayout.setOnClickListener(this);
		mWeatherImg = (ImageButton) findViewById(R.id.edit_weather_image);
		new Thread(new Runnable() {
			public void run() {
				initWeather();
			}
		}).start();
		
		saveButton = (ImageButton) findViewById(R.id.screen_top_control_save);
		saveButton.setOnClickListener(this);
		reload();
	}
	
//	private void initSpinner(ListView lview) {
//		final List<String> data = new ArrayList<String>();
//        data.add(getString(R.string.diary_category1));
//        data.add(getString(R.string.diary_category2));
//        data.add(getString(R.string.diary_category3));
//        data.add(getString(R.string.diary_category4));
//        data.add(getString(R.string.diary_category5));
//        
//        lview.setAdapter(new ArrayAdapter<String>(this, R.layout.listview_item_dropdown,data));
//	    
//        lview.setOnItemClickListener(new OnItemClickListener(){
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position,
//					long id) {
//				// TODO Auto-generated method stub
////				dropdown_tv.setText(data.get(position));
////				diary_category_dialog.dismiss();
//			}
//        });
//	}
	
	private void initWeather() {
		ServerInterface sinterface = new ServerInterface();
		String prov = "湖北";
		String city = "武汉";
		String result = sinterface.getWeather(prov, city);
		result =result.replace("\\", "");
		//String reg = "weather1:";
        // 编译
	    Pattern pattern = Pattern.compile("weather1:([^,]+),");
	    Matcher matcher = pattern.matcher(result);
	    if(matcher.find()) {
	    	String ttt =matcher.group();
	    	ttt =ttt.replace("weather1:", "");
	    	ttt =ttt.replace(",", "");
	    	Log.d("=DDD=  ttt=",ttt);
	    	String weather = "";
	    	int imgId = 0;
	    	if (ttt.contains("雨")) {
	    		weather = "雨";
	    		imgId = R.drawable.weather_rain;
	    	} else if (ttt.contains("雪")) {
	    		weather = "雪";
	    		imgId = R.drawable.weather_snow;
	    	} else if (ttt.contains("多云") || ttt.contains("阴")|| ttt.contains("沙")) {
	    		weather = "多云";
	    		imgId = R.drawable.weather_cloudy;
	    	} else if (ttt.contains("晴")) {
	    		weather = "晴";
	    		imgId = R.drawable.weather_sunny;
	    	} else {
	    		weather = "多云";
	    		imgId = R.drawable.weather_cloudy;
	    	}
	    	final String weatherTemp = weather;
	    	final int imgIdTemp = imgId;
	    	
	    	handler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					weather_tv.setText(weatherTemp);
					mWeatherImg.setImageDrawable(getResources().getDrawable(imgIdTemp));
				}
			});
	    	
	    }
		Log.d("=DDD=",result);
	}
	
	private void resetState(View v) {
		if ((v.getId() != mViewId) &&(v.getId() == R.id.edit_insert
				||v.getId() == R.id.edit_delete
				||v.getId() == R.id.edit_inputtype
				||v.getId() == R.id.edit_setting)) {
			
			isInputTypeShow = false;
			isPicTypeShow = false;
			isSettingTypeShow = false;
			edit_type.setVisibility(View.GONE);
			pic_type.setVisibility(View.GONE);
			setting_type.setVisibility(View.GONE);
			faceGridview.setVisibility(View.GONE);
			mViewId = v.getId();
		}
		
		if (v.getId() == R.id.edit_type_graffit
				||v.getId() == R.id.edit_type_soft
				||v.getId() == R.id.edit_type_handwrite
				||v.getId() == R.id.edit_insert_face
				||v.getId() == R.id.edit_insert_pic) {
			faceGridview.setVisibility(View.GONE);
			if (v.getId() != R.id.edit_type_handwrite) {
				gestureview.setVisibility(View.GONE);
			}
	    }
	}
	
	private void initThicknessDialog() {
		thickness_dialog = new Dialog(this,R.style.CustomDialog);
		thickness_dialog.setContentView(R.layout.thickness_dialog);
		thickness_dialog.setCanceledOnTouchOutside(true);
		Window mWindow = thickness_dialog.getWindow();
		WindowManager.LayoutParams lp = mWindow.getAttributes();   
		lp.x = DensityUtil.dip2px(this,54);   //新位置X坐标
		lp.y = DensityUtil.dip2px(this,146); //新位置Y坐标
		thickness_dialog.onWindowAttributesChanged(lp);
		
		thickness_seekbar = (SeekBar) thickness_dialog.findViewById(R.id.thickness_setting_bar);
		thickness_seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				if (mState == HANDWRITINGSTATE) {
				    gestureview.setGestureStrokeWidth(arg1);
				} else if (mState == GRAFFITINSERTSTATE) {
					myEdit.getFingerPen().setStrokeWidth(arg1);
				}
			}

			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}

			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
        });
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		resetState(v);
		switch (v.getId()) {
		case R.id.edit_inputtype:
			if (isInputTypeShow) {
				edit_type.setVisibility(View.GONE);
				isInputTypeShow = false;
			} else {
			    edit_type.setVisibility(View.VISIBLE);
			    edit_type.bringToFront();
			    isInputTypeShow = true;
			}
			break;
		case R.id.edit_insert:
			faceGridview.setVisibility(View.GONE);
			if (isPicTypeShow) {
				pic_type.setVisibility(View.GONE);
				isPicTypeShow = false;
			} else {
			    pic_type.setVisibility(View.VISIBLE);
			    isPicTypeShow = true;
			}
			break;
		case R.id.edit_setting:
			if (isSettingTypeShow) {
				setting_type.setVisibility(View.GONE);
				isSettingTypeShow = false;
			} else {
				setting_type.setVisibility(View.VISIBLE);
			    isSettingTypeShow = true;
			}
			break;
		case R.id.edit_delete:
			if (mState != GRAFFITINSERTSTATE) {
				int delete_index = myEdit.getSelectionStart();
				Spanned s_delete = myEdit.getText();
				ImageSpan[] imageSpans_delete = s_delete.getSpans(0, delete_index, ImageSpan.class);
				if (imageSpans_delete.length == 0) {
					int delete_index_temp = delete_index -1  < 0 ? 0 : delete_index -1;
					myEdit.getText().delete(delete_index_temp, delete_index);
					return;
				}
				
				ImageSpan imgSpan_delete = imageSpans_delete[imageSpans_delete.length - 1];
				
				if ( s_delete.getSpanEnd(imgSpan_delete) != delete_index) {
					int delete_index_temp = delete_index -1  < 0 ? 0 : delete_index -1;
					myEdit.getText().delete(delete_index_temp, delete_index);
					return;
				}
				
				int start_index = s_delete.getSpanStart(imgSpan_delete);
				int end_index = s_delete.getSpanEnd(imgSpan_delete);
				String spanStr = s_delete.subSequence(start_index, end_index).toString();
				String [] arrayStr = spanStr.split("_");
				if (arrayStr.length == 2) {
					if (arrayStr[0].equals("hw")) {
						mStore.removeGesture(spanStr, mStore.getGestures(spanStr).get(0));
						mStore.save(false);
					} else if (arrayStr[0].equals("pic")) {
						mPicMap.remove(spanStr);
					}
				}
				
				Log.d("=NNN=","source = " + spanStr + " start = " + start_index + " end = " + end_index);
				myEdit.getText().delete(start_index, end_index);
			} else {
				if (isgraffit_erase) {
					break;
				}
				mStrokeWidth = myEdit.getFingerPen().getStrokeWidth();
				myEdit.getFingerPen().setStrokeWidth(MAX_STROKEWIDTH);
				myEdit.getFingerPen().setXfermode(new PorterDuffXfermode(
	                    PorterDuff.Mode.CLEAR));
				myEdit.setFingerColor(0x00000000);
				isgraffit_erase = true;
			}
			break;
		case R.id.edit_type_handwrite:
			Log.d("=MMM=","edit_type_handwrite");
			if (handWriteListener == null) {
			    handWriteListener = new GesturesProcessorHandWrite();
			}
			edit_type.setVisibility(View.GONE);
			isInputTypeShow = false;
			
			if (mState == HANDWRITINGSTATE) {
				break;
			}
			gestureview.removeAllOnGestureListeners();
			gestureview.addOnGestureListener(handWriteListener);
			gestureview.setVisibility(View.VISIBLE);
			edit_input_type.setImageDrawable(getResources().getDrawable(R.drawable.edit_handwrite_selector));
			edit_delete.setImageDrawable(getResources().getDrawable(R.drawable.edit_delete_1_selector));
			mState = HANDWRITINGSTATE;
			break;
		case R.id.edit_type_graffit:
			edit_type.setVisibility(View.GONE);
			isInputTypeShow = false;
			
			if (isgraffit_erase) {
				myEdit.getFingerPen().setStrokeWidth(mStrokeWidth);
				myEdit.getFingerPen().setXfermode(null);
				isgraffit_erase = false;
				break;
			}
			if (mState == GRAFFITINSERTSTATE) {
				break;
			}
			edit_input_type.setImageDrawable(getResources().getDrawable(R.drawable.edit_graffit_selector));
			edit_delete.setImageDrawable(getResources().getDrawable(R.drawable.edit_delete_selector));
			
			mState = GRAFFITINSERTSTATE;
			break;
		case R.id.edit_type_soft:
//			myEdit.setInputType(inType);
			edit_type.setVisibility(View.GONE);
			isInputTypeShow = false;
			if (mState == SOFTINPUTSTATE) {
				break;
			}
			edit_input_type.setImageDrawable(getResources().getDrawable(R.drawable.edit_softinput_selector));
			edit_delete.setImageDrawable(getResources().getDrawable(R.drawable.edit_delete_1_selector));
			imm.showSoftInput(myEdit, inType);
			mState = SOFTINPUTSTATE;
			break;
		case R.id.edit_insert_face:
		    pic_type.setVisibility(View.GONE);
			isPicTypeShow = false;
			faceGridview.setVisibility(View.VISIBLE);
			edit_delete.setImageDrawable(getResources().getDrawable(R.drawable.edit_delete_1_selector));
			myEdit.clearFocus(); 
			mState = FACEINSERTSTATE;
			break;
		case R.id.edit_insert_pic:
			if (picchoose_dialog == null) {
				picchoose_dialog = new Dialog(this);
				picchoose_dialog.setContentView(R.layout.picture_choose_dialog);
				picchoose_dialog.setTitle("请选择从哪里获取图片");
				picchoose_dialog.setCanceledOnTouchOutside(true);
				
				cameraButton = (Button) picchoose_dialog.findViewById(R.id.picfroecamera);
		        albumButton =  (Button) picchoose_dialog.findViewById(R.id.picfromalbum);
		        
		        cameraButton.setOnClickListener(this);
		        albumButton.setOnClickListener(this);
			}
			picchoose_dialog.show();
			
			if (mState == PICINSERTSTATE) {
				break;
			}
			pic_type.setVisibility(View.GONE);
			isPicTypeShow = false;
			edit_delete.setImageDrawable(getResources().getDrawable(R.drawable.edit_delete_1_selector));
			mState = PICINSERTSTATE;
			break;
		case R.id.edit_insert_space:
			int index = myEdit.getSelectionStart();
			index = index < 0 ? 0 : index;
			myEdit.getText().insert(index, " ");
			break;
		case R.id.edit_insert_newline:
			int index1 = myEdit.getSelectionStart();
			index1 = index1 < 0 ? 0 : index1;
			myEdit.getText().insert(index1, "\n");
			break;
//		case R.id.cursor_back:
//			int back_index = myEdit.getSelectionStart();
//			Spanned s_back = myEdit.getText();  
//			ImageSpan[] imageSpans_back = s_back.getSpans(0, back_index, ImageSpan.class);  
//			if (imageSpans_back.length == 0) {
//				int back_index_temp = back_index -1  < 0 ? 0 : back_index -1;
//				myEdit.setSelection(back_index_temp);
//				return;
//			}
//			ImageSpan imgSpan_back = imageSpans_back[imageSpans_back.length - 1];
//			if ( s_back.getSpanEnd(imgSpan_back) != back_index) {
//				int back_index_temp = back_index -1  < 0 ? 0 : back_index -1;
//				myEdit.setSelection(back_index_temp);
//				return;
//			}
//			myEdit.setSelection(s_back.getSpanStart(imgSpan_back));
//			break;
//		case R.id.cursor_forward:
//			int forward_index = myEdit.getSelectionStart();
//			Spanned s_forward = myEdit.getText();  
//			ImageSpan[] imageSpans = s_forward.getSpans(forward_index, myEdit.getEditableText().length(), ImageSpan.class);  
//			if (imageSpans.length == 0) {
//				int forward_index_temp = forward_index +1  > myEdit.getEditableText().length() ? myEdit.getEditableText().length() : forward_index +1;
//				myEdit.setSelection(forward_index_temp);
//				return;
//			}
//			ImageSpan imgSpan_forward = imageSpans[0];
//			if ( s_forward.getSpanStart(imgSpan_forward) != forward_index) {
//				int forward_index_temp = forward_index +1  > myEdit.getEditableText().length() ? myEdit.getEditableText().length() : forward_index +1;
//				myEdit.setSelection(forward_index_temp);
//				return;
//			}
//			myEdit.setSelection(s_forward.getSpanEnd(imgSpan_forward));
//			break;
//		case R.id.handwrite_delete:
//			int delete_index = myEdit.getSelectionStart();
//			Spanned s_delete = myEdit.getText();  
//			ImageSpan[] imageSpans_delete = s_delete.getSpans(0, delete_index, ImageSpan.class);
//			if (imageSpans_delete.length == 0) {
//				int delete_index_temp = delete_index -1  < 0 ? 0 : delete_index -1;
//				myEdit.getText().delete(delete_index_temp, delete_index);
//				return;
//			}
//			
//			ImageSpan imgSpan_delete = imageSpans_delete[imageSpans_delete.length - 1];
//			
//			if ( s_delete.getSpanEnd(imgSpan_delete) != delete_index) {
//				int delete_index_temp = delete_index -1  < 0 ? 0 : delete_index -1;
//				myEdit.getText().delete(delete_index_temp, delete_index);
//				return;reload
//			}
//			myEdit.getText().delete(s_delete.getSpanStart(imgSpan_delete), s_delete.getSpanEnd(imgSpan_delete));
//			break;
		case R.id.edit_setting_color:
			if (mState == HANDWRITINGSTATE) {
				OnColorChangedListener listener = new OnColorChangedListener() {
					public void colorChanged(int color) {
						gestureview.setGestureColor(color);
					}
		    	};
		    	new ColorPickerDialog(this, listener, gestureview.getGestureColor()).show();
			} else if (mState == GRAFFITINSERTSTATE) {
				new ColorPickerDialog(this, myEdit, myEdit.getFingerPen().getColor()).show();
			}
			break;
		case R.id.edit_setting_thickness:
			if (mState == HANDWRITINGSTATE || mState == GRAFFITINSERTSTATE) {
			    if (thickness_dialog == null) {
				    initThicknessDialog();
			    }
			    thickness_dialog.show();
			} else {
				
			}
			break;
		case R.id.picfroecamera:
			String pName = picName.generateName();
			imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aNote/pic/pic_" + pName + ".jpg";
			
			File imageFile = new File(imageFilePath);
			if (!imageFile.exists()) {
				try {
					imageFile.getParentFile().mkdirs();
					imageFile.createNewFile();
					Log.d("=NNN=","createfile successed");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Uri imageFileUri = Uri.fromFile(imageFile);
			Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,imageFileUri);
			startActivityForResult(i,CAMERA_RESULT);
			picchoose_dialog.dismiss();
			break;
		case R.id.picfromalbum:
			Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "使用以下内容完成操作"),ALBUM_RESULT);
            picchoose_dialog.dismiss();
            break;
		case R.id.edit_weather_linearlayout:
			if (weather_dialog == null) {
				weather_dialog = new Dialog(this,R.style.CustomDialog);
				weather_dialog.setContentView(R.layout.dialog_weather);
				Window mWindow_weather = weather_dialog.getWindow();     
				WindowManager.LayoutParams lp_weather = mWindow_weather.getAttributes();     
				lp_weather.x = DensityUtil.dip2px(this,70);   //新位置X坐标  
				lp_weather.y = -1 * DensityUtil.dip2px(this,140); //新位置Y坐标 
				weather_dialog.onWindowAttributesChanged(lp_weather);
				
				LinearLayout weather_sunny = (LinearLayout)weather_dialog.findViewById(R.id.weather_sunny);
				LinearLayout weather_cloudy = (LinearLayout)weather_dialog.findViewById(R.id.weather_cloudy);
				LinearLayout weather_rain = (LinearLayout)weather_dialog.findViewById(R.id.weather_rain);
				LinearLayout weather_snow = (LinearLayout)weather_dialog.findViewById(R.id.weather_snow);
				weather_sunny.setOnClickListener(this);
				weather_cloudy.setOnClickListener(this);
				weather_rain.setOnClickListener(this);
				weather_snow.setOnClickListener(this);
			}
			weather_dialog.show();
			break;
		case R.id.weather_sunny:
			weather_tv.setText(getString(R.string.weather_sunny));
			mWeatherImg.setImageDrawable(getResources().getDrawable(R.drawable.weather_sunny));
			weather_dialog.dismiss();
			break;
		case R.id.weather_cloudy:
			weather_tv.setText(getString(R.string.weather_cloudy));
			mWeatherImg.setImageDrawable(getResources().getDrawable(R.drawable.weather_cloudy));
			weather_dialog.dismiss();
			break;
		case R.id.weather_rain:
			weather_tv.setText(getString(R.string.weather_rain));
			mWeatherImg.setImageDrawable(getResources().getDrawable(R.drawable.weather_rain));
			weather_dialog.dismiss();
			break;
		case R.id.weather_snow:
			weather_tv.setText(getString(R.string.weather_snow));
			mWeatherImg.setImageDrawable(getResources().getDrawable(R.drawable.weather_snow));
			weather_dialog.dismiss();
			break;
		case R.id.screen_top_control_save:
			save();
			break;
		}
	}
	
	private void save() {
		writeText();
		writePicMap();
		
		Spanned s_save = myEdit.getText();
		ImageSpan[] imageSpans_save = s_save.getSpans(0, s_save.length(), ImageSpan.class);
		if (imageSpans_save.length != 0) {
			File f = new File("/sdcard/aNote/index");
			FileWriter fw=null;
			BufferedWriter bw = null;
			try {
				if (!f.exists()) {
					f.getParentFile().mkdir();
					f.createNewFile();
				}
			    fw=new FileWriter(f); 
			    bw = new BufferedWriter(fw);
			    bw.write("title:" + "2012.07.01");
			    bw.write("\n");
			    bw.write("page:" + "1");
			    bw.write("\n");
			    int start_index = 0;
			    int end_index = 0;
			    for (ImageSpan span : imageSpans_save) {
			    	start_index = s_save.getSpanStart(span);
			    	if (start_index != end_index) {
			    		bw.write("str:" + String.valueOf(end_index) + "-" + String.valueOf(start_index));
			    		bw.write("\n");
			    	}
			    	
					end_index = s_save.getSpanEnd(span);
					String spanStr = s_save.subSequence(start_index, end_index).toString();
					String [] arrayStr = spanStr.split("_");
					if (arrayStr.length == 2) {
						if (arrayStr[0].equals("hw")) {
							bw.write("hw:" + spanStr);
							bw.write("\n");
						} else if (arrayStr[0].equals("pic")) {
							bw.write("pic:" + spanStr);
							bw.write("\n");
						} else if (arrayStr[0].equals("face")) {
							bw.write("face:" + spanStr);
							bw.write("\n");
						}
					}
			    }
			    if (end_index != s_save.length()) {
			        bw.write("str:" + String.valueOf(end_index) + "-" + String.valueOf(s_save.length()));
			        bw.write("\n");
			    }
			    if (myEdit.save()) {
			    	bw.write("gft:" + "page1");
			    	bw.write("\n");
			    }
			    
			    bw.flush();
		    } catch (FileNotFoundException e) {
			    e.printStackTrace();
			} catch (IOException e) {
			    e.printStackTrace();
		    } finally {
			    try {
			        bw.close();
			    } catch (IOException e) {
			        e.printStackTrace();
			    }
			}
		}
		String [] fileNames = {"/sdcard/aNote/index","/sdcard/aNote/gesture","/sdcard/aNote/picmap","/sdcard/aNote/graffit","/sdcard/aNote/text"};
		zipFile(fileNames,"/sdcard/aNote/diary_1");
	}
	
	private void zipFile(String[] fileFroms, String fileTo) {  
        try {
        	FileOutputStream out = new FileOutputStream(fileTo);  
            ZipOutputStream zipOut = new ZipOutputStream(out);  
        	for (String fileFrom:fileFroms) {
	        	File file = new File(fileFrom);
	        	if (!file.exists()) {
	        		Log.d("=MMM=","file not exist");
	        		continue;
	        	}
	            FileInputStream in = new FileInputStream(fileFrom);  
	            
	            ZipEntry entry = new ZipEntry(fileFrom);  
	            zipOut.putNextEntry(entry);  
	            int nNumber;  
	            byte[] buffer = new byte[512];  
	            while ((nNumber = in.read(buffer)) != -1){  
	                zipOut.write(buffer, 0, nNumber);  
	                //countBuffer++;  
	            }
	            in.close();
	            file.delete();
        	}
            zipOut.close();  
  
            out.close();  
            //handlerCopy.sendEmptyMessage(2);  
        } catch (IOException e) {  
            //handlerCopy.sendEmptyMessage(3);  
            //System.out.println(e);
        	e.printStackTrace();
        }  
    }
	
	private void reload() {
		Unzip("/sdcard/aNote/diary_1","");
		File file = new File("/sdcard/aNote/index");
        BufferedReader reader = null;
        
        if (!file.exists()) {
        	return;
        }
        String textStr = "";
        FileInputStream in = null;
        try {
        	in = new FileInputStream("/sdcard/aNote/text");
        	byte[] readBytes = new byte[in.available()];
            in.read(readBytes);
            textStr = new String(readBytes);
            in.close();
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
        	if (in != null) {
        		try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
        
        try {
        	
            System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
            	if (tempString.startsWith("str:")) {
            		String value = tempString.substring("str:".length(), tempString.length());
            	    Log.d("=FFF=",value + "********");
            	    String [] arrayStr = value.split("-");
            	    if (arrayStr.length == 2) {
            	    	int first = Integer.parseInt(arrayStr[0]);
            	    	int second = Integer.parseInt(arrayStr[1]);
            	    	if (first > second) {
            	    		continue;
            	    	}
            	    	myEdit.getEditableText().insert(myEdit.getEditableText().length(), textStr.subSequence(first, second));
            	    }
            	} else if (tempString.startsWith("hw:")) {
            		String value = tempString.substring("hw:".length(), tempString.length());
            		if (gestureFile == null) {
                		gestureFile = new File("/sdcard/aNote/gesture");
                	}
                	
                	if (!gestureFile.exists()) {
                        continue;
            		}
                	
                	if (mStore == null) {
                        mStore = AmGestureLibraries.fromFile(gestureFile);
                        mStore.load(false);
                    }
                	
                	AmGesture gesture = mStore.getGestures(value).get(0);
                	addGesture(value, gesture);
                	int curNum = 0;
                	try {
                	    curNum = Integer.parseInt(value.substring(value.indexOf("_") + 1),value.length());
                	} catch (Exception e) {
                		curNum = 0;
                	}
                	gestureName.setCurNum(curNum + 1);
//                	Bitmap bmp = gesture.toBitmap(75, 110, 0, gesture.getGesturePaintColor());
//                    bmp.eraseColor(0xff000000);
//                	Bitmap bmp = Bitmap.createBitmap(DensityUtil.dip2px(EditNoteScreen.this,50), DensityUtil.dip2px(EditNoteScreen.this,71), Bitmap.Config.ARGB_8888);;
//                	bmp.eraseColor(0xffffffff);
//                	Canvas canvas = new Canvas(bmp);
//                	Paint pt = new Paint();
//                	pt.setColor(0xFF000000);
//                	canvas.drawRect(dip2px(3), dip2px(20), dip2px(47), dip2px(64), pt);
                	Bitmap bmp = Bitmap.createBitmap(DensityUtil.dip2px(EditNoteScreen.this,50), DensityUtil.dip2px(EditNoteScreen.this,71), Bitmap.Config.ARGB_8888);;
                	bmp.eraseColor(0x00000000);
                	Canvas canvas = new Canvas(bmp);
                	canvas.drawBitmap(gesture.toBitmap(dip2px(44), dip2px(44), 0, gesture.getGesturePaintColor()), dip2px(3), dip2px(20), null);
                	Drawable drawable = new BitmapDrawable(bmp);
	                drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
	      		    ImageSpan span = new ImageSpan(drawable,ImageSpan.ALIGN_BOTTOM);
	      			SpannableString spanStr = new SpannableString(value);
	      			spanStr.setSpan(span, 0, spanStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	      			myEdit.getEditableText().insert(myEdit.getEditableText().length(), spanStr);
            	} else if (tempString.startsWith("pic:")) {
            		String value = tempString.substring("pic:".length(), tempString.length());
            		String path = SetSystemProperty.getKeyValue(value);
            		if (path == null) {
            			continue;
            		}
            		if (mPicMap == null) {
            			mPicMap = new LinkedHashMap<String, String>();
            		}
            		int curNum = 0;
                	try {
                	    curNum = Integer.parseInt(value.substring(value.indexOf("_") + 1),value.length());
                	} catch (Exception e) {
                		curNum = 0;
                	}
                	gestureName.setCurNum(curNum + 1);
            		mPicMap.put(value, path);
            		Bitmap bmp = decodeFile(new File(path));
    		        ImageSpan span = new ImageSpan(bmp);
    				SpannableString spanStr = new SpannableString(value);
    				spanStr.setSpan(span, 0, spanStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    				myEdit.getText().insert(myEdit.getEditableText().length(), spanStr);
            	} else if (tempString.startsWith("face:")) {
            		String value = tempString.substring("face:".length(), tempString.length());
            		if (value.endsWith("face_a1")) {
            			insertFace(R.drawable.face_a1);
            		} else if (value.endsWith("face_a2")) {
            			insertFace(R.drawable.face_a2);
            		} else if (value.endsWith("face_a3")) {
            			insertFace(R.drawable.face_a3);
            		} else if (value.endsWith("face_a4")) {
            			insertFace(R.drawable.face_a4);
            		} else if (value.endsWith("face_a5")) {
            			insertFace(R.drawable.face_a5);
            		} else if (value.endsWith("face_a6")) {
            			insertFace(R.drawable.face_a6);
            		} else if (value.endsWith("face_a7")) {
            			insertFace(R.drawable.face_a7);
            		} else if (value.endsWith("face_a8")) {
            			insertFace(R.drawable.face_a8);
            		}
            	} else if (tempString.startsWith("gft:")) {
//            		myEdit.reloadGraffit();
            	}
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    String [] fileNames = {"/sdcard/aNote/index","/sdcard/aNote/gesture","/sdcard/aNote/picmap","/sdcard/aNote/text"};
                    deletefiles(fileNames);
                } catch (IOException e1) {
                }
            }
        }
	}
	
	private void deletefiles(String [] fileName) {
		for (String filename : fileName) {
			File file = new File(filename);
			if (file.exists()) {
				file.delete();
			}
		}
	}
	
	private void Unzip(String zipFile, String targetDir) {
     	int BUFFER = 4096; //这里缓冲区我们使用4KB，
     	String strEntry; //保存每个zip的条目名称
     	try {
     		BufferedOutputStream dest = null; //缓冲输出流
     	    FileInputStream fis = new FileInputStream(zipFile);
     	    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
     	    ZipEntry entry; //每个zip条目的实例
     	    while ((entry = zis.getNextEntry()) != null) {
     	    	try {
     	    		int count;
     	    		byte data[] = new byte[BUFFER];
     	    		strEntry = entry.getName();
     	    		File entryFile = new File(targetDir + strEntry);
     	    		File entryDir = new File(entryFile.getParent());
     	    		if (!entryDir.exists()) {
     	    			entryDir.mkdirs();
     	    		}
     	    		FileOutputStream fos = new FileOutputStream(entryFile);
     	    		entry.getSize();
     	    		dest = new BufferedOutputStream(fos, BUFFER);
     	    		while ((count = zis.read(data, 0, BUFFER)) != -1) {
     	    			dest.write(data, 0, count);
     	    		}
     	    		dest.flush();
     	    		dest.close();
     	    	} catch (Exception ex) {
     	    		ex.printStackTrace();
     	    	}
     	    }
     	    zis.close();
     	} catch (Exception cwj) {
     		cwj.printStackTrace();
     	}
 	}
	
	
	private void writeText() {
		File f = new File("/sdcard/aNote/text");
		FileWriter fw=null;
		BufferedWriter bw = null;
		try {
			if (!f.exists()) {
				f.getParentFile().mkdir();
				f.createNewFile();
			}
		    fw=new FileWriter(f); 
		    bw = new BufferedWriter(fw);
		    bw.write(myEdit.getText().toString());
  		    bw.flush();
	    } catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
	    } finally {
		    try {
		        bw.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
	}
	
	private void writePicMap() {
		if (mPicMap == null || mPicMap.size() == 0) {
			return;
		}
		for (String str : mPicMap.keySet()) {
			SetSystemProperty.writeProperties(str, mPicMap.get(str));
	    }
	}
	
	private class GesturesProcessorHandWrite implements AmGestureOverlayView.OnAmGestureListener {
        public void onGestureStarted(AmGestureOverlayView overlay, MotionEvent event) {
            mGesture = null;
        }

        public void onGesture(AmGestureOverlayView overlay, MotionEvent event) {
        }

        public void onGestureEnded(AmGestureOverlayView overlay, MotionEvent event) {
            mGesture = overlay.getGesture();
            if (mGesture.getLength() < LENGTH_THRESHOLD) {
                overlay.clear(false);
            }
        }
        
       

        public void onGestureCancelled(AmGestureOverlayView overlay, MotionEvent event) {
        	if (mGesture == null) {
        		return;
        	}
        	if (mGesture.getLength() < LENGTH_THRESHOLD) {
                return;
            }
        	int lineheight = DensityUtil.dip2px(EditNoteScreen.this, myEdit.getLineHeight() );
//        	Bitmap bmp = mGesture.toBitmap(lineheight, lineheight, 0, mGesture.getGesturePaintColor());
            Bitmap bmp = Bitmap.createBitmap(DensityUtil.dip2px(EditNoteScreen.this,50), DensityUtil.dip2px(EditNoteScreen.this,71), Bitmap.Config.ARGB_8888);;
        	bmp.eraseColor(0x00000000);
        	Canvas canvas = new Canvas(bmp);
        	canvas.drawBitmap(mGesture.toBitmap(dip2px(44), dip2px(44), 0, mGesture.getGesturePaintColor()), dip2px(3), dip2px(20), null);
//        	Bitmap bmp = Bitmap.createBitmap(DensityUtil.dip2px(EditNoteScreen.this,50), DensityUtil.dip2px(EditNoteScreen.this,71), Bitmap.Config.ARGB_8888);;
//        	bmp.eraseColor(0xff000000);
//        	Canvas canvas = new Canvas(bmp);
//        	Paint pt = new Paint();
//        	pt.setColor(0xFF000000);
//        	canvas.drawRect(dip2px(3), dip2px(20), dip2px(47), dip2px(64), pt);
        	Drawable drawable = new BitmapDrawable(bmp);
            drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
		    ImageSpan span = new ImageSpan(drawable,ImageSpan.ALIGN_BOTTOM);
		    String gName = gestureName.generateName();
			SpannableString spanStr = new SpannableString("hw_" + gName);
			addGesture("hw_" + gName,mGesture);
			spanStr.setSpan(span, 0, spanStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			int index = myEdit.getSelectionStart();
			index = index < 0 ? 0 : index;
			myEdit.getEditableText().insert(index, spanStr);
			overlay.clear(false);
        }
    }
	
	private void addGesture(String name,AmGesture gesture) {
    	if (gestureFile == null) {
    		gestureFile = new File("/sdcard/aNote/gesture");
    	}
    	
    	if (!gestureFile.exists()) {
			try {
				gestureFile.getParentFile().mkdirs();
				gestureFile.createNewFile();
				Log.d("=NNN=","createfile successed");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	
    	if (mStore == null) {
            mStore = AmGestureLibraries.fromFile(gestureFile);
        }
    	
    	mStore.addGesture(name, gesture);
    	mStore.save(false);
    }
	
	private int dip2px(int x) {
    	return DensityUtil.dip2px(EditNoteScreen.this,x);
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CAMERA_RESULT) {
			if (resultCode == RESULT_OK) {
		        Bitmap bmp = decodeFile(new File(imageFilePath));
		        ImageSpan span = new ImageSpan(bmp);
		        int index = myEdit.getSelectionStart();
		        myEdit.getText().insert(index, "\n");
		        String pName = imageFilePath.substring(imageFilePath.lastIndexOf('/') + 1,imageFilePath.lastIndexOf('.'));
		        Log.d("=DDD=","name = " + pName);
		        addMapItem(pName);
				SpannableString spanStr = new SpannableString(pName);
				spanStr.setSpan(span, 0, spanStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				index = myEdit.getSelectionStart();
				myEdit.getText().insert(index, spanStr);   
			}
		} else if (requestCode == ALBUM_RESULT) {
			if (data == null) {
				return;
			}
			Uri _uri = data.getData();   
            
            // this will be null if no image was selected...   
            if (_uri != null) {   
              // now we get the path to the image file   
	            Cursor cursor = getContentResolver().query(_uri, null,   
	                                              null, null, null);   
	            cursor.moveToFirst();   
	            imageFilePath = cursor.getString(1);
	            cursor.close();
	            
	            Bitmap bmp = decodeFile(new File(imageFilePath));
		        ImageSpan span = new ImageSpan(bmp);
		        int index = myEdit.getSelectionStart();
		        myEdit.getText().insert(index, "\n");
		        String pName = picName.generateName();
				SpannableString spanStr = new SpannableString("pic_" + pName);
				addMapItem("pic_" + pName);
				spanStr.setSpan(span, 0, spanStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				index = myEdit.getSelectionStart();
				myEdit.getText().insert(index, spanStr);
            }
		}
	}
	
	private void addMapItem(String name) {
		if (mPicMap == null) {
			mPicMap = new LinkedHashMap<String, String>();
		}
		mPicMap.put(name, imageFilePath);
	}
	
	private Bitmap decodeFile(File f){
        Bitmap b = null;
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            int scale = 1;
            scale = (int)(o.outHeight / (float)200);
            if (scale <= 0) {
            	scale = 1;
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (Exception e) {
        }
        return b;
    }
	
	public void makeAdapters() {
		ArrayList<Integer> faces = new ArrayList<Integer>();
		faces.add(R.drawable.face_a1);
		faces.add(R.drawable.face_a2);
		faces.add(R.drawable.face_a3);
		faces.add(R.drawable.face_a4);
		faces.add(R.drawable.face_a5);
		faces.add(R.drawable.face_a6);
		faces.add(R.drawable.face_a7);
		faces.add(R.drawable.face_a8);
		faceAdapter = new FaceAdapter(this, faces);
	}
	
	private void initFaceAdapter() {
		makeAdapters();
		faceGridview = (GridView) findViewById(R.id.face_gridview);
		faceGridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				int viewId = (Integer) ((FrameLayout) view).getChildAt(0)
				.getTag();
				View childView = ((FrameLayout) view).getChildAt(1);
				for(int index = 0; index < 8; index++){
					View tempView = parent.getChildAt(index);
					((FrameLayout) tempView).getChildAt(1).setVisibility(View.GONE);
				}
				childView.setVisibility(View.VISIBLE);
				
				switch (viewId) {
				case R.drawable.face_a1:
					insertFace(R.drawable.face_a1);
					break;
				case R.drawable.face_a2:
					insertFace(R.drawable.face_a2);
					break;
				case R.drawable.face_a3:
					insertFace(R.drawable.face_a3);
					break;
				case R.drawable.face_a4:
					insertFace(R.drawable.face_a4);
					break;
				case R.drawable.face_a5:
					insertFace(R.drawable.face_a5);
					break;
				case R.drawable.face_a6:
					insertFace(R.drawable.face_a6);
					break;
				case R.drawable.face_a7:
					insertFace(R.drawable.face_a7);
					break;
				case R.drawable.face_a8:
					insertFace(R.drawable.face_a8);
					break;
				}
			}
		});
		faceGridview.setAdapter(faceAdapter);
	}
	
	private void insertFace(int id) {
        String fname = this.getResources().getResourceName(id);
        fname = fname.substring(fname.lastIndexOf("/") + 1, fname.length());
        Log.d("=NNN=","name = " + fname);
		Drawable drawable =  this.getResources().getDrawable(id); 
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		ImageSpan span = new ImageSpan(drawable);
		SpannableString spanStr = new SpannableString(fname);
		spanStr.setSpan(span, 0, spanStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		int index = myEdit.getSelectionStart();
		index = index < 0 ? 0 : index;
		myEdit.getText().insert(index, spanStr);
	}
}

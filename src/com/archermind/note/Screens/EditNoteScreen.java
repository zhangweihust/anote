package com.archermind.note.Screens;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import com.archermind.note.R;
import com.archermind.note.Adapter.FaceAdapter;
import com.archermind.note.editnote.ColorPickerDialog;
import com.archermind.note.editnote.ColorPickerDialog.OnColorChangedListener;
import com.archermind.note.editnote.MyEditText;
import com.archermind.note.gesture.AmGesture;
import com.archermind.note.gesture.AmGestureOverlayView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class EditNoteScreen  extends Screen implements OnClickListener {

	private AmGestureOverlayView gestureview = null;
	private MyEditText myEdit = null;
	private ImageButton handWritingButton = null;
	private ImageButton softInputButton = null;
	private ImageButton picInsertButton = null;
	private ImageButton faceInsertButton = null;
	private ImageButton graffitInsertButton = null;
	
	private ImageButton cursor_back = null;
	private ImageButton cursor_forward = null;
	private ImageButton handwrite_delete = null;
    private ImageButton color_setting = null;
    private ImageButton thickness_setting = null;
    
    private ImageButton graffit_revocation = null;
    private ImageButton graffit_back = null;
    private ImageButton graffit_erase = null;
    private ImageButton graffit_color_setting = null;
    private ImageButton graffit_thickness_setting = null;
    
    
    private GridView faceGridview = null;
    private FaceAdapter faceAdapter = null;
    private boolean isFaceShowing = false;
    
    private SeekBar thickness_seekbar = null;
	
    private Button cameraButton = null; 
    private Button albumButton = null;
    
	private int inType = 0;
	private AmGesture mGesture;
	private static final float LENGTH_THRESHOLD = 40.0f;
	private InputMethodManager  imm = null;
//	private LinearLayout handwritelayout = null;
	private LinearLayout handwrite_show_hide = null;
	private LinearLayout handwrite_button_layout = null;
	
	private LinearLayout graffit_show_hide = null;
	private LinearLayout graffit_button_layout = null;
	
	private boolean ishandwrite_show = false;
	private boolean isthickness_show = false;
	private boolean isgraffit_show = false;
	private boolean isgraffit_erase = false;
	private Dialog thickness_dialog = null;
	private Dialog picchoose_dialog = null;
	
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
	private GesturesProcessorGraffit graffitListener;
	
	private final int MIN_STROKEWIDTH = 12;
	private final int MAX_STROKEWIDTH = 30;
	private float mStrokeWidth = MIN_STROKEWIDTH;
	
	private int mViewId = -1;
	
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
		
		
		handWritingButton = (ImageButton) findViewById(R.id.handwritinginsert);
		softInputButton = (ImageButton) findViewById(R.id.softinputinsert);
		picInsertButton = (ImageButton)findViewById(R.id.pictureinsert);
		faceInsertButton = (ImageButton)findViewById(R.id.facialexpressioninsert);
		graffitInsertButton = (ImageButton)findViewById(R.id.graffitiinsert);
		
		
		handWritingButton.setOnClickListener(this);
		softInputButton.setOnClickListener(this);
		picInsertButton.setOnClickListener(this);
		faceInsertButton.setOnClickListener(this);
		graffitInsertButton.setOnClickListener(this);
		
//		handwritelayout = (LinearLayout) findViewById(R.id.handwritingdialog);
		handwrite_show_hide = (LinearLayout) findViewById(R.id.handwrite_show_hide);
		handwrite_show_hide.setOnClickListener(this);
		handwrite_button_layout = (LinearLayout) findViewById(R.id.handwrite_button_layout);
		handwrite_show_hide.setVisibility(View.GONE);
		handwrite_button_layout.setVisibility(View.GONE);
		
		graffit_show_hide = (LinearLayout) findViewById(R.id.graffit_show_hide);
		graffit_show_hide.setOnClickListener(this);
		graffit_button_layout = (LinearLayout) findViewById(R.id.graffit_button_layout);
		graffit_show_hide.setVisibility(View.GONE);
		graffit_button_layout.setVisibility(View.GONE);
		
		cursor_back = (ImageButton) findViewById(R.id.cursor_back);
		cursor_forward = (ImageButton) findViewById(R.id.cursor_forward);
		handwrite_delete = (ImageButton) findViewById(R.id.handwrite_delete);
		color_setting = (ImageButton) findViewById(R.id.color_setting);
		thickness_setting = (ImageButton) findViewById(R.id.thickness_setting);
		
		cursor_back.setOnClickListener(this);
		cursor_forward.setOnClickListener(this);
		handwrite_delete.setOnClickListener(this);
		color_setting.setOnClickListener(this);
		thickness_setting.setOnClickListener(this);
		
		graffit_revocation = (ImageButton) findViewById(R.id.graffit_revocation);
		graffit_back = (ImageButton) findViewById(R.id.graffit_back);
		graffit_erase = (ImageButton) findViewById(R.id.graffit_erase);
		graffit_color_setting = (ImageButton) findViewById(R.id.graffit_color_setting);
		graffit_thickness_setting = (ImageButton) findViewById(R.id.graffit_thickness_setting);
		graffit_revocation.setOnClickListener(this);
		graffit_back.setOnClickListener(this);
		graffit_erase.setOnClickListener(this);
		graffit_color_setting.setOnClickListener(this);
		graffit_thickness_setting.setOnClickListener(this);
		
		thickness_dialog = new Dialog(this,R.style.CustomDialog);
		thickness_dialog.setContentView(R.layout.thickness_dialog);
		thickness_dialog.setCanceledOnTouchOutside(true);
		Window mWindow = thickness_dialog.getWindow();     
		WindowManager.LayoutParams lp = mWindow.getAttributes();     
		lp.x = 80;   //新位置X坐标  
		lp.y = 220; //新位置Y坐标 
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
		
		
		picchoose_dialog = new Dialog(this);
		picchoose_dialog.setContentView(R.layout.picture_choose_dialog);
		picchoose_dialog.setTitle("请选择从哪里获取图片");
		picchoose_dialog.setCanceledOnTouchOutside(true);
		
        cameraButton = (Button) picchoose_dialog.findViewById(R.id.picfroecamera);
        albumButton =  (Button) picchoose_dialog.findViewById(R.id.picfromalbum);
        
        cameraButton.setOnClickListener(this);
        albumButton.setOnClickListener(this);
        
        initFaceAdapter();
        faceGridview.setVisibility(View.GONE);
        
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	private void resetState(View v) {
		if (mViewId == v.getId()) {
			return;
		}
		if (v.getId() == R.id.handwritinginsert
				||v.getId() == R.id.graffitiinsert
				||v.getId() == R.id.softinputinsert
				||v.getId() == R.id.facialexpressioninsert
				||v.getId() == R.id.pictureinsert) {
			gestureview.setVisibility(View.GONE);
			faceGridview.setVisibility(View.GONE);
			graffit_show_hide.setVisibility(View.GONE);
			graffit_button_layout.setVisibility(View.GONE);
			handwrite_show_hide.setVisibility(View.GONE);
			handwrite_button_layout.setVisibility(View.GONE);
			imm.hideSoftInputFromWindow(myEdit.getWindowToken(), 0); 
			isFaceShowing = false;
			ishandwrite_show = false;
			isgraffit_show = false;
			isgraffit_erase = false;
			mViewId = v.getId();
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		resetState(v);
		switch (v.getId()) {
		case R.id.handwritinginsert:
			if (handWriteListener == null) {
			    handWriteListener = new GesturesProcessorHandWrite();
			}
			gestureview.removeAllOnGestureListeners();
			gestureview.addOnGestureListener(handWriteListener);
			
			gestureview.setVisibility(View.VISIBLE);
			
			mState = HANDWRITINGSTATE;
			handwrite_show_hide.setVisibility(View.VISIBLE);
			break;
		case R.id.graffitiinsert:
			mState = GRAFFITINSERTSTATE;
			graffit_show_hide.setVisibility(View.VISIBLE);
			
			myEdit.setIsGraffit(true);
//			if (graffitListener == null) {
//			    graffitListener = new GesturesProcessorGraffit();
//		    }
//			gestureview.setVisibility(View.VISIBLE);
//			gestureview.removeAllOnGestureListeners();
//			gestureview.addOnGestureListener(graffitListener);
			break;
		case R.id.softinputinsert:
			mState = SOFTINPUTSTATE;
//			myEdit.setInputType(inType);
			imm.showSoftInput(myEdit, inType);
			break;
		case R.id.facialexpressioninsert:
			mState = FACEINSERTSTATE;
			if (isFaceShowing) {
				faceGridview.setVisibility(View.GONE);
				isFaceShowing = false;
			} else {
			    faceGridview.setVisibility(View.VISIBLE);
			    isFaceShowing = true;
			}
			myEdit.clearFocus(); 
			break;
		case R.id.pictureinsert:
			mState = PICINSERTSTATE;
			picchoose_dialog.show();
			break;
		case R.id.handwrite_show_hide:
			if (ishandwrite_show) {
				handwrite_button_layout.setVisibility(View.GONE);
				ishandwrite_show = false;
			} else {
				handwrite_button_layout.setVisibility(View.VISIBLE);
				ishandwrite_show = true;
			}
			break;
		case R.id.cursor_back:
			int back_index = myEdit.getSelectionStart();
//			back_index = back_index -1  < 0 ? 0 : back_index -1;
			
			Spanned s_back = myEdit.getText();  
			ImageSpan[] imageSpans_back = s_back.getSpans(0, back_index, ImageSpan.class);  
			if (imageSpans_back.length == 0) {
				int back_index_temp = back_index -1  < 0 ? 0 : back_index -1;
				myEdit.setSelection(back_index_temp);
				return;
			}
			ImageSpan imgSpan_back = imageSpans_back[imageSpans_back.length - 1];
			if ( s_back.getSpanEnd(imgSpan_back) != back_index) {
				int back_index_temp = back_index -1  < 0 ? 0 : back_index -1;
				myEdit.setSelection(back_index_temp);
				return;
			}
			myEdit.setSelection(s_back.getSpanStart(imgSpan_back));
			break;
		case R.id.cursor_forward:
			int forward_index = myEdit.getSelectionStart();
			Spanned s_forward = myEdit.getText();  
			ImageSpan[] imageSpans = s_forward.getSpans(forward_index, myEdit.getEditableText().length(), ImageSpan.class);  
			if (imageSpans.length == 0) {
				int forward_index_temp = forward_index +1  > myEdit.getEditableText().length() ? myEdit.getEditableText().length() : forward_index +1;
				myEdit.setSelection(forward_index_temp);
				return;
			}
			ImageSpan imgSpan_forward = imageSpans[0];
			if ( s_forward.getSpanStart(imgSpan_forward) != forward_index) {
				int forward_index_temp = forward_index +1  > myEdit.getEditableText().length() ? myEdit.getEditableText().length() : forward_index +1;
				myEdit.setSelection(forward_index_temp);
				return;
			}
			myEdit.setSelection(s_forward.getSpanEnd(imgSpan_forward));
			break;
		case R.id.handwrite_delete:
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
			myEdit.getText().delete(s_delete.getSpanStart(imgSpan_delete), s_delete.getSpanEnd(imgSpan_delete));
			break;
		case R.id.color_setting:
			OnColorChangedListener listener = new OnColorChangedListener() {
				public void colorChanged(int color) {
					gestureview.setGestureColor(color);
				}
	    	};
	    	new ColorPickerDialog(this, listener, gestureview.getGestureColor()).show();
			break;
		case R.id.thickness_setting:
			thickness_dialog.show();
			break;
		case R.id.picfroecamera:
			imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/mypicture.jpg";
			File imageFile = new File(imageFilePath);
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
		case R.id.graffit_show_hide:
			if (isgraffit_show) {
				graffit_button_layout.setVisibility(View.GONE);
				isgraffit_show = false;
			} else {
				graffit_button_layout.setVisibility(View.VISIBLE);
				isgraffit_show = true;
			}
			break;
		case R.id.graffit_revocation:
			break;
		case R.id.graffit_back:
			myEdit.getFingerPen().setStrokeWidth(mStrokeWidth);
			myEdit.getFingerPen().setXfermode(null);
			isgraffit_erase = false;
			break;
		case R.id.graffit_erase:
			if (isgraffit_erase) {
				break;
			}
			mStrokeWidth = myEdit.getFingerPen().getStrokeWidth();
			myEdit.getFingerPen().setStrokeWidth(MAX_STROKEWIDTH);
			myEdit.getFingerPen().setXfermode(new PorterDuffXfermode(
                    PorterDuff.Mode.CLEAR));
			isgraffit_erase = true;
			break;
		case R.id.graffit_color_setting:
			new ColorPickerDialog(this, myEdit, myEdit.getFingerPen().getColor()).show();
			break;
		case R.id.graffit_thickness_setting:
			thickness_dialog.show();
			break;
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
        	
        	Bitmap bmp = mGesture.toBitmap(85, 85, 0, mGesture.getGesturePaintColor());
            Drawable drawable = new BitmapDrawable(bmp);
            drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
		    ImageSpan span = new ImageSpan(drawable,ImageSpan.ALIGN_BOTTOM);
			SpannableString spanStr = new SpannableString("[handwrite]");
			spanStr.setSpan(span, 0, spanStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			int index = myEdit.getSelectionStart();
			index = index < 0 ? 0 : index;
			myEdit.getEditableText().insert(index, spanStr);
			overlay.clear(false);
        }
    }
	
	private class GesturesProcessorGraffit implements AmGestureOverlayView.OnAmGestureListener {
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
        	
        	Bitmap bmp = mGesture.toBitmap(gestureview.getWidth(), gestureview.getHeight(), 0, mGesture.getGesturePaintColor());
        	String imgFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/graffit.png";

        	File myCaptureFile = new File(imgFilePath);
            
        	BufferedOutputStream bos;
			try {
				if (!myCaptureFile.exists()) {
	            	myCaptureFile.createNewFile();
	            }
				bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
				bmp.compress(Bitmap.CompressFormat.PNG, 100, bos);
	            bos.flush();
	            bos.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bmp.recycle();
			bmp = null;
			overlay.clear(false);
			
        	
			bmp = decodeFile(myCaptureFile);
	        ImageSpan span = new ImageSpan(bmp);
	        int index = myEdit.getSelectionStart();
			myEdit.getText().insert(index, "\n");   
			SpannableString spanStr = new SpannableString("[graffit]\n");
			spanStr.setSpan(span, 0, "[graffit]".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			index = myEdit.getSelectionStart();
			myEdit.getText().insert(index, spanStr);
        }
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
				SpannableString spanStr = new SpannableString("[pic]\n");
				spanStr.setSpan(span, 0, "[pic]".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
				SpannableString spanStr = new SpannableString("[pic]\n");
				spanStr.setSpan(span, 0, "[pic]".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				index = myEdit.getSelectionStart();
				myEdit.getText().insert(index, spanStr);
            }
		}
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
		Drawable drawable =  this.getResources().getDrawable(id); 
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		ImageSpan span = new ImageSpan(drawable);
        int index = myEdit.getSelectionStart();
		SpannableString spanStr = new SpannableString("[face]");
		spanStr.setSpan(span, 0, "[face]".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		index = myEdit.getSelectionStart();
		myEdit.getText().insert(index, spanStr);
	}
}

package com.android.note.Screens;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.android.note.NoteApplication;
import com.android.note.Utils.PreferencesHelper;
import com.archermind.note.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class RotateImageScreen extends Activity implements OnClickListener {

	private ImageView mRotateLeft;
	private ImageView mRotateRight;
	private ImageView mOriginalImage;
	private Button mConfirm;
	private String mPName;
	private Bitmap mBm;
	private Bitmap mResizedBitmap;
	private Matrix matrix;
	private int mIntentId = 0;
	private int mUploadcount=-1;
	private String mName;
	private String mExpandName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rotateimage_screen);
		initViews();
		mIntentId = getIntent().getIntExtra("id", 0);
		mUploadcount=getIntent().getIntExtra("uploadcount", -1);
		mName=getIntent().getStringExtra("name");
		mExpandName=getIntent().getStringExtra("expandname");
		mPName = getIntent().getStringExtra("pName");
		byte buff[] = getIntent().getByteArrayExtra("bm");
		if (buff != null) {
			mBm = BitmapFactory.decodeByteArray(buff, 0, buff.length);
		}
		mOriginalImage.setImageBitmap(mBm);
		matrix = new Matrix();
		mResizedBitmap = Bitmap.createBitmap(mBm, 0, 0, mBm.getWidth(),
				mBm.getHeight(), matrix, true);
	}

	private void initViews() {
		mRotateLeft = (ImageView) findViewById(R.id.rotate_left);
		mRotateLeft.setOnClickListener(this);
		mRotateRight = (ImageView) findViewById(R.id.rotate_right);
		mRotateRight.setOnClickListener(this);
		mOriginalImage = (ImageView) findViewById(R.id.originalimage);
		mConfirm = (Button) findViewById(R.id.confirm_rotate);
		mConfirm.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rotate_left:
			matrix.postRotate(-90);
			mResizedBitmap = Bitmap.createBitmap(mBm, 0, 0, mBm.getWidth(),
					mBm.getHeight(), matrix, true);
			mOriginalImage.setImageBitmap(mResizedBitmap);
			break;
		case R.id.rotate_right:
			matrix.postRotate(90);
			mResizedBitmap = Bitmap.createBitmap(mBm, 0, 0, mBm.getWidth(),
					mBm.getHeight(), matrix, true);
			mOriginalImage.setImageBitmap(mResizedBitmap);
			break;
		case R.id.confirm_rotate:

			switch (mIntentId) {
			case EditNoteScreen.INTENT_ID:
				Intent intent = new Intent(RotateImageScreen.this,
						EditNoteScreen.class);
				intent.putExtra("pName", mPName);
				intent.putExtra("bm", PreferencesHelper.Bitmap2Bytes(mResizedBitmap));
				setResult(RESULT_OK, intent);
				finish();
				break;
			case PersonInfoScreen.INTENT_ID:
				Intent intent2 = new Intent(RotateImageScreen.this,
						PersonInfoScreen.class);
				intent2.putExtra("bm", PreferencesHelper.Bitmap2Bytes(mResizedBitmap));
				setResult(RESULT_OK, intent2);
				finish();
				break;
			case AlbumScreen.INTENT_ID:

				try {
					bitmapToFile(mResizedBitmap);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}
	
	private void bitmapToFile(Bitmap bm) throws IOException{
    	String p=NoteApplication.savePath+"image_cache/"+mName+"."+mExpandName;
    	File f=new File(p);
    	if(f.exists()){
    		f.delete();
    	}
    	if(f.createNewFile()){
    		FileOutputStream fOut = null;
    		fOut = new FileOutputStream(f);
    		Intent intent3 = new Intent(RotateImageScreen.this,
					AlbumScreen.class);
    		if(bm.compress(Bitmap.CompressFormat.PNG, 100, fOut)){
				intent3.putExtra("filepath", p);
				intent3.putExtra("name", mName);
				intent3.putExtra("expandname", mExpandName);
				intent3.putExtra("uploadcount", mUploadcount);
    		}else{
    			Toast.makeText(RotateImageScreen.this, 
    					R.string.image_create_cache_file_failed_unknown, 
    					Toast.LENGTH_SHORT).show();
    		}
    		setResult(RESULT_OK, intent3);
			finish();
    		fOut.close();
    		
    	}
    }
}
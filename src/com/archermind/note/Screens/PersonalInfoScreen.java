package com.archermind.note.Screens;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.archermind.note.R;
import com.archermind.note.Utils.ImageCapture;
import com.archermind.note.Utils.PreferencesHelper;

public class PersonalInfoScreen extends Screen implements OnClickListener {

	private Context mContext;
	private ImageView mUserAvatar;
	
	private Dialog mPicChooseDialog;
	
	private static final int ALBUM_RESULT = 1;
	private static final int CAMERA_RESULT = 2;
	private static final int CROP_RESULT = 3;
	
	private ContentResolver mContentResolver;
	
	private String mCameraImageFilePath;
 
    private ImageCapture mImgCapture;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personalinfo_screen);

		mContext = PersonalInfoScreen.this;
		
		ImageButton btnback = (ImageButton) this.findViewById(R.id.back);
		btnback.setOnClickListener(this);
		
		View set_avatar = (View) this.findViewById(R.id.set_avatar_layout);
		set_avatar.setOnClickListener(this);

		mUserAvatar = (ImageView) this.findViewById(R.id.user_avatar);

		Bitmap image = PreferencesHelper.getAvatarBitmap(this);
		if (image != null) {
			mUserAvatar.setImageBitmap(image);
		}
		
		mContentResolver = getContentResolver();
		
		mImgCapture = new ImageCapture(this, mContentResolver);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Bitmap image = PreferencesHelper.getAvatarBitmap(this);
		if (image != null) {
			mUserAvatar.setImageBitmap(image);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		
		case ALBUM_RESULT:
			if(data != null){
				Uri uri = data.getData();
				startPhotoCROP(uri);
			}
			break;

		case CAMERA_RESULT:
			if (resultCode == RESULT_OK) {
				String filepath = mCameraImageFilePath;
				startPhotoCROP(Uri.fromFile(new File(mCameraImageFilePath)));
			}
			break;

		case CROP_RESULT:
			if(data != null){
				Bundle extras = data.getExtras();
				if (extras != null) {
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					Bitmap photo = extras.getParcelable("data");
					photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
					byte[] b = stream.toByteArray();
					this.mImgCapture.storeImage(b, null);
					String filepath = getFilepathFromUri(this.mImgCapture.getLastCaptureUri());
					PreferencesHelper.UpdateAvatar(this, filepath);
				}
			}
			
			break;
		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private String getFilepathFromUri(Uri uri) {
		//System.out.println("=CCC=" + uri);
		Cursor cursor = mContentResolver.query(uri, null,   
                null, null, null);   
		cursor.moveToFirst();   
		String filepath = cursor.getString(1);
		cursor.close();
		
		return filepath;
	}
	
	private void getNewImageFromLocal() {
		Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.photo_add_sel)), ALBUM_RESULT);	
	}
	
	private void getNewImageFromCamera() {
        long dateTaken = System.currentTimeMillis();
        String title = mImgCapture.createName(dateTaken);
        mCameraImageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() 
				+ "/"+ title +".jpg";
		File imageFile = new File(mCameraImageFilePath);
		Uri imageFileUri = Uri.fromFile(imageFile);
		Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,imageFileUri);
		startActivityForResult(intent, CAMERA_RESULT);	
	}
	
	private void startPhotoCROP(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");

		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);

		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, CROP_RESULT);
	}
	
	private void showSelImageDialog() {
		if (mPicChooseDialog == null) {
			mPicChooseDialog = new Dialog(this);
			mPicChooseDialog.setContentView(R.layout.picture_choose_dialog);
			mPicChooseDialog.setTitle("请选择从哪里获取图片");
			mPicChooseDialog.setCanceledOnTouchOutside(true);

			Button cameraButton = (Button) mPicChooseDialog
					.findViewById(R.id.picfroecamera);
			Button albumButton = (Button) mPicChooseDialog
					.findViewById(R.id.picfromalbum);

			albumButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					getNewImageFromLocal();
					mPicChooseDialog.dismiss();
				}

			});

			cameraButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					getNewImageFromCamera();
					mPicChooseDialog.dismiss();
				}

			});
		}
		mPicChooseDialog.show();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;
		case R.id.set_avatar_layout:
			showSelImageDialog();
			break;
		}
	}
}

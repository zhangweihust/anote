package com.archermind.note.Screens;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.archermind.note.R;
import com.archermind.note.Adapter.PhotoAdapter;
import com.archermind.note.Adapter.PhotoAdapter.ViewHolder;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.ImageCapture;
import com.archermind.note.Utils.PreferencesHelper;

public class AlbumScreen extends Screen implements OnClickListener {

	private Gallery mPhotoGallery;
	private GridView mPhotoView;
	private View mPhotoGalleryLayout;
	private View mPhotoGridLayout;
	
	private ImageButton mBtnGalleryBack;
	private ImageButton mBtnGridBack;
	private ImageButton mBtnGallerySetAvatar;
	private ImageButton mBtnGridInsertImage;
	
	private TextView mGalleryTitle;
	
	private Bitmap mCacheImage;
	
	private ViewHolder mLastSelItem;
	private ViewHolder mSelItem;
	
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
		setContentView(R.layout.album_screen);
		
		mPhotoGallery = (Gallery) findViewById(R.id.p_gallery_gallery);
		mPhotoGalleryLayout = (View) findViewById(R.id.p_gallery_layout);
		
		mPhotoView = (GridView) findViewById(R.id.p_grid_gridview);
		mPhotoView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mPhotoGridLayout = findViewById(R.id.p_grid_layout);
		
		mBtnGalleryBack = (ImageButton) findViewById(R.id.p_gallery_back);
		mBtnGridBack = (ImageButton) findViewById(R.id.p_grid_back);
		mBtnGallerySetAvatar = (ImageButton) findViewById(R.id.p_gallery_set_avatar);
		mBtnGridInsertImage = (ImageButton) findViewById(R.id.p_grid_insert_image);
		
		mBtnGalleryBack.setOnClickListener(this);
		mBtnGridBack.setOnClickListener(this);
		mBtnGallerySetAvatar.setOnClickListener(this);
		mBtnGridInsertImage.setOnClickListener(this);
		
		mGalleryTitle = (TextView) findViewById(R.id.p_gallery_activityTitle);
		
		
		mPhotoView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				mPhotoGallery.setSelection(arg2);
				mPhotoGalleryLayout.setVisibility(View.VISIBLE);
				mPhotoGridLayout.setVisibility(View.GONE);
			}});

		mPhotoGallery.setCallbackDuringFling(false);
		mPhotoGallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				mSelItem = (ViewHolder) arg1.getTag();
				Integer cur_num = (Integer) (arg2 + 1);
				Integer sum = (Integer) arg0.getCount();
				String title = String.format(
						getString(R.string.photo_gallery_title), cur_num, sum);
				mGalleryTitle.setText(title);

				AlbumScreen.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (mLastSelItem != null) {
							mLastSelItem.image.setImageBitmap(null);
						}
						
						if (mCacheImage != null) {
							mCacheImage.recycle();
						}

						File file = new File(mSelItem.filepath);
						if (file.exists()) {
							mCacheImage = BitmapFactory
									.decodeFile(mSelItem.filepath);
							if (mCacheImage != null) {
								mSelItem.image.setImageBitmap(mCacheImage);
							}
						}
						mLastSelItem = mSelItem;
					}
				});
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}});
		
		
		mContentResolver = getContentResolver();
		
		mImgCapture = new ImageCapture(this, mContentResolver);
		
		loadAlbumData();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		
		case ALBUM_RESULT:
			if(data != null){
				Uri uri = data.getData();
				if (uri != null) {
					String filepath = getFilepathFromUri(uri);
					System.out.println("=CCC=" + filepath);
					String name = filepath.substring(filepath.lastIndexOf("/") + 1, filepath.length());
					System.out.println("=CCC=" + name);
					name = name.substring(0, name.lastIndexOf("."));
					System.out.println("=CCC=" + name);
					insertNewImageToDB(name, filepath);
				}
			}
			break;

		case CAMERA_RESULT:
			if (resultCode == RESULT_OK) {
				String filepath = mCameraImageFilePath;
				String name = filepath.substring(filepath.lastIndexOf("/") + 1, filepath.length());
				name = name.substring(0, name.lastIndexOf("."));
				insertNewImageToDB(name, filepath);
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
					this.finish();
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
	
	private boolean insertNewImageToDB(String name, String filepath) {
		ContentValues cValue = new ContentValues();        
   
		cValue.put(DatabaseHelper.COLUMN_PHOTO_NAME, name);         
 
		cValue.put(DatabaseHelper.COLUMN_PHOTO_FILEPATH, filepath); 
		
		return ServiceManager.getDbManager().insertLocalPhoto(cValue);
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
	
	private void loadAlbumData() {

        Cursor c = ServiceManager.getDbManager().queryPhotoData();
        startManagingCursor(c);

        final PhotoAdapter adapter1 = new PhotoAdapter(this, c, Gallery.class);
		mPhotoGallery.setAdapter(adapter1);
		
		final PhotoAdapter adapter2 = new PhotoAdapter(this, c, GridView.class);
		mPhotoView.setAdapter(adapter2);
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
		case R.id.p_grid_back:
			this.finish();
			break;
		case R.id.p_gallery_back:
			mPhotoGridLayout.setVisibility(View.VISIBLE);
			mPhotoGalleryLayout.setVisibility(View.GONE);
			break;
		case R.id.p_gallery_set_avatar:
			startPhotoCROP(mSelItem.uri);
			break;
		case R.id.p_grid_insert_image:
			showSelImageDialog();
			break;
		}
	}
}


  

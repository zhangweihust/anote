package com.archermind.note.Screens;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Adapter.PhotoAdapter;
import com.archermind.note.Screens.AlbumScreen.MyThread;
import com.archermind.note.Utils.ImageCapture;
import com.archermind.note.Utils.PreferencesHelper;
import com.archermind.note.Utils.ServerInterface;

public class PersonalInfoScreen extends Screen implements OnClickListener {

	private Context mContext;
	private ImageView mUserAvatar;

	private EditText mUserName;

	private TextView mUserRegion;

	private RadioButton mRadioFemale;
	private RadioButton mRadioMale;

	private Dialog mPicChooseDialog;

	private static final int ALBUM_RESULT = 1;
	private static final int CAMERA_RESULT = 2;
	private static final int CROP_RESULT = 3;
	private static final int REGION_RESULT = 4;

	private static final int DOWNLOAD_PHOTO_JSON_OK = 1;
	private static final int DOWNLOAD_PHOTO_JSON_ERROR = 2;
	private static final int DOWNLOAD_INFO_JSON_OK = 3;
	private static final int DOWNLOAD_INFO_JSON_ERROR = 4;

	private static final int UPLOAD_PHOTO_OK = 5;
	private static final int UPLOAD_PHOTO_ERROR = 6;
	private static final int UPLOAD_INFO_OK = 7;
	private static final int UPLOAD_INFO_ERROR = 8;

	private static final int UPLOAD_PHOTO = 9;
	private static final int DOWNLOAD_PHOTO = 10;
	private static final int UPLOAD_INFO = 11;
	private static final int DOWNLOAD_INFO = 12;
	
	private ContentResolver mContentResolver;

	private String mCameraImageFilePath;
	private String mCropImageFilePath;
	private String mUserAvatarPath;
	private String mUserNameTxt;
	private String mRegionTxt;
	private int mSex;

	private ImageCapture mImgCapture;

	private SharedPreferences mPreferences;

	private ServerInterface serverInterface;

	private UpDownloadHandler handler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personalinfo_screen);

		mContext = PersonalInfoScreen.this;

		Button btnback = (Button) this.findViewById(R.id.back);
		btnback.setOnClickListener(this);

		View set_avatar = (View) this.findViewById(R.id.set_avatar_layout);
		set_avatar.setOnClickListener(this);

		mUserAvatar = (ImageView) this.findViewById(R.id.user_avatar);
		mUserName = (EditText) this.findViewById(R.id.user_name);
		mUserRegion = (TextView) this.findViewById(R.id.user_region);

		mRadioFemale = (RadioButton) this.findViewById(R.id.radioFemale);
		mRadioMale = (RadioButton) this.findViewById(R.id.radioMale);

		final Button btnConfirmChange = (Button) this
				.findViewById(R.id.confirm_change);
		btnConfirmChange.setOnClickListener(this);

		loadLocalPersonalInfo();

		View region = (View) this.findViewById(R.id.region_layout);
		region.setOnClickListener(this);

		Bitmap image = PreferencesHelper.getAvatarBitmap(this);
		if (image != null) {
			mUserAvatar.setImageBitmap(image);
		}

		mContentResolver = this.getContentResolver();
		mImgCapture = new ImageCapture(this, mContentResolver);

		serverInterface = new ServerInterface();
//		serverInterface.InitAmtCloud(mContext);

		handler = new UpDownloadHandler();

		downloadImage();

		downloadInfo();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case ALBUM_RESULT:
			if (data != null) {
				Uri uri = data.getData();
				startPhotoCROP(uri);
			}
			break;

		case CAMERA_RESULT:
			if (resultCode == RESULT_OK) {
				String filepath = mCameraImageFilePath;
				System.out.println("mCameraImageFilePath:" + mCameraImageFilePath != null ? "null" : mCameraImageFilePath);
				if (mCameraImageFilePath != null && !"".equals(mCameraImageFilePath)) {
					startPhotoCROP(Uri.fromFile(new File(mCameraImageFilePath)));
				}
			}
			break;

		case CROP_RESULT:
			if (data != null) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					Bitmap photo = extras.getParcelable("data");
					photo = PreferencesHelper.toRoundCorner(photo, 15);
					photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
					byte[] b = stream.toByteArray();
					this.mImgCapture.storeImage(b, null, Bitmap.CompressFormat.PNG);
					String filepath = getFilepathFromUri(this.mImgCapture
							.getLastCaptureUri());
					File file = new File(filepath);
					if (file.exists()) {
						filepath = file.getAbsolutePath();
						String name = filepath.substring(filepath
								.lastIndexOf("/") + 1, filepath.length());
						String expandname = filepath.substring(filepath
								.lastIndexOf(".") + 1, filepath.length());
						name = name.substring(0, name.lastIndexOf("."));
						uploadImage(name, expandname, filepath, 1);
					}
				}
			}

			break;
		case REGION_RESULT:
			if (data != null) {
				int provinceId = data.getIntExtra("province", 0);
				int cityId = data.getIntExtra("city", 0);
				String province = PreferencesHelper.getProvinceName(mContext,
						provinceId);
				String city = PreferencesHelper.getCityName(mContext,
						provinceId, cityId);

				mUserRegion.setText(province + " " + city);
			}
			break;

		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private String getFilepathFromUri(Uri uri) {
		Cursor cursor = mContentResolver.query(uri, null, null, null, null);
		cursor.moveToFirst();
		String filepath = cursor.getString(1);
		cursor.close();

		return filepath;
	}

	private void getNewImageFromLocal() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent,
				getString(R.string.photo_add_sel)), ALBUM_RESULT);
	}

	private void getNewImageFromCamera() {
		long dateTaken = System.currentTimeMillis();
		String title = mImgCapture.createName(dateTaken);
		mCameraImageFilePath = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ "/" + title + ".jpg";
		File imageFile = new File(mCameraImageFilePath);
		Uri imageFileUri = Uri.fromFile(imageFile);
		Intent intent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
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

	private void uploadImage(String name, String expandname, String filepath,
			int uploadcount) {
		Message msg = new Message();
		msg.getData().putString("name", name);
		msg.getData().putString("expandname", expandname);
		msg.getData().putString("filelocalpath", filepath);
		msg.getData().putInt("uploadcount", uploadcount);
		msg.what = UPLOAD_PHOTO;
		handler.sendMessage(msg);
	}

	private void downloadImage() {
		Message msg = new Message();
		msg.what = DOWNLOAD_PHOTO;
		handler.sendMessage(msg);
	}

	private void uploadInfo(String nickname,String gender,String region, 
			int uploadcount) {
		final String aNickname = nickname;
		final String aGender = gender;
		final String aRegion = region;
		final int aUploadCount = uploadcount;
		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				showProgress(null, getString(R.string.commiting_info));
				String user_id = String.valueOf(NoteApplication.getInstance()
						.getUserId());
				int result = serverInterface.set_info(user_id, aNickname,
						aGender, aRegion);

				Message msg = new Message();
				msg.getData().putString("nickname", aNickname);
				msg.getData().putString("gender", aGender);
				msg.getData().putString("region", aRegion);
				msg.getData().putInt("uploadcount", aUploadCount);

				if (result == 0) {
					msg.what = UPLOAD_INFO_OK;
				} else {
					msg.what = UPLOAD_INFO_ERROR;
				}
				handler.sendMessage(msg);
				dismissProgress();
			}
		});
	}
	
	private void downloadInfo() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String user_id = String.valueOf(NoteApplication.getInstance()
						.getUserId());

				Looper.prepare();
				String json = serverInterface.get_info(user_id);
				if (json == null || "".equals(json) || "-1".equals(json)
						|| "-2".equals(json)) {
					Message msg = new Message();
					msg.what = DOWNLOAD_INFO_JSON_ERROR;
					handler.sendMessage(msg);
					return;
				}

				String nickname = "";
				String gender = "";
				String region = "";
				try {
					JSONArray jsonArray = new JSONArray(json);

					if (jsonArray.length() > 0) {
						JSONObject jsonObject = (JSONObject) jsonArray.opt(0);
						nickname = jsonObject.getString("nickname");
						gender = jsonObject.getString("gender");
						region = jsonObject.getString("region");
						// System.out.println("nickname:" + nickname);
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Message msg = new Message();
					msg.what = DOWNLOAD_INFO_JSON_ERROR;
					handler.sendMessage(msg);
					e.printStackTrace();
					return;
				}
				Message msg = new Message();
				msg.what = DOWNLOAD_INFO_JSON_OK;
				msg.getData().putString("nickname", nickname);
				msg.getData().putString("gender", gender);
				msg.getData().putString("region", region);
				handler.sendMessage(msg);
			}

		}).start();
	}

	public class UpDownloadHandler extends Handler {

		UpDownloadHandler() {
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UPLOAD_PHOTO:
//				String user_id = String.valueOf(NoteApplication.getInstance()
//						.getUserId());
//				String username = NoteApplication.getInstance().getUserName();
//				String aName = msg.getData().getString("name");
//				String aExpandName = msg.getData().getString("expandname");
//				String aFilePath = msg.getData().getString("filelocalpath");
//				int uploadcount = msg.getData().getInt("uploadcount");
//
//				int result = serverInterface.uploadPhoto(mContext, user_id,
//						username, aFilePath, aName, aExpandName);
//
//				Message newmsg = new Message();
//				newmsg.getData().putString("name", aName);
//				newmsg.getData().putString("expandname", aExpandName);
//				newmsg.getData().putString("filelocalpath", aFilePath);
//				newmsg.getData().putInt("uploadcount", uploadcount);
//
//				if (result == 0) {
//					newmsg.what = UPLOAD_PHOTO_OK;
//				} else {
//					newmsg.what = UPLOAD_PHOTO_ERROR;
//				}
//				handler.sendMessage(newmsg);
				break;
			case UPLOAD_PHOTO_OK:
				PreferencesHelper.UpdateAvatar(mContext, mUserAvatarPath, msg
						.getData().getString("filelocalpath"));
				{
					Bitmap image = PreferencesHelper.getAvatarBitmap(mContext);
					if (image != null) {
						mUserAvatar.setImageBitmap(image);
					}
				}
				break;
			case UPLOAD_PHOTO_ERROR: {
				int uploadcount = msg.getData().getInt("uploadcount");
				if (uploadcount > 3 || uploadcount <= 0 || NoteApplication.networkIsOk == false ) {
					System.out.println("UPLOAD_ALBUM_ERROR");
					String aFilePath = msg.getData().getString("filelocalpath");
					new File(aFilePath).delete();
					Toast.makeText(PersonalInfoScreen.this,
							getString(R.string.image_upload_failed),
							Toast.LENGTH_SHORT).show();
				} else {
					System.out.println("UPLOAD_ALBUM_ERROR, try count : "
							+ String.valueOf(uploadcount + 1));
					String aName = msg.getData().getString("name");
					String aExpandName = msg.getData().getString("expandname");
					String aFilePath = msg.getData().getString("filelocalpath");
					uploadImage(aName, aExpandName, aFilePath, uploadcount + 1);
				}
			}
				break;
			case DOWNLOAD_PHOTO: {
				String user_id = String.valueOf(NoteApplication.getInstance()
						.getUserId());

				Message newmsg = new Message();
				String aPhotoUrl = serverInterface.getPhoto(user_id);
				if (aPhotoUrl == null || "".equals(aPhotoUrl)) {
					newmsg.what = DOWNLOAD_PHOTO_JSON_ERROR;
					handler.sendMessage(newmsg);
					return;
				}
				
				aPhotoUrl = aPhotoUrl.replace("\\", "").replace("\"", "");
				try {
					int retCode = Integer.valueOf(aPhotoUrl);
					if (retCode < 0) {
						newmsg.what = DOWNLOAD_PHOTO_JSON_ERROR;
						handler.sendMessage(newmsg);
						return;
					}
					if (retCode == 1) {
						aPhotoUrl = "";
					}
				} catch (NumberFormatException e) {
				}

				newmsg.what = DOWNLOAD_PHOTO_JSON_OK;
				newmsg.getData().putString("photourl", aPhotoUrl);
				handler.sendMessage(newmsg);
			}
				break;
			case DOWNLOAD_PHOTO_JSON_OK:
				System.out.println("DOWNLOAD_PHOTO_JSON_OK");
				String aPhotoUrl = msg.getData().getString("photourl");
				if (aPhotoUrl == null || "".equals(aPhotoUrl)) {
					return;
				}

				String filelocalpath = ImageCapture
						.getLocalCacheImageNameFromUrl(aPhotoUrl);
				if (filelocalpath == null) {
					System.out.println("getLocalCacheImageNameFromUrl error, aPhotoUrl:" + aPhotoUrl);
					return;
				}
				
				File file = new File(filelocalpath);
				int retCode = 0;
				if (!file.exists()) {
					retCode = ImageCapture.createLocalCacheImageFromUrl(aPhotoUrl,
							filelocalpath);
				}

				if (!new File(filelocalpath).exists()) {
					if (retCode != 0) {
						int resid;
						if (retCode == -1)
							resid = R.string.image_create_cache_file_failed_unknown;
						else if (retCode == -2)
							resid = R.string.image_create_cache_file_failed_web;
						else
							resid = R.string.image_create_cache_file_failed_io;
						
						Toast.makeText(PersonalInfoScreen.this,
								getString(resid), Toast.LENGTH_SHORT).show();
					}
					return;
				}

				if (!mUserAvatarPath.equals(filelocalpath)) {
					PreferencesHelper.UpdateAvatar(mContext, mUserAvatarPath,
							filelocalpath);
					Bitmap image = PreferencesHelper.getAvatarBitmap(mContext);
					if (image != null) {
						mUserAvatar.setImageBitmap(image);
					}
				}

				break;
			case DOWNLOAD_PHOTO_JSON_ERROR:
				System.out.println("DOWNLOAD_THUMB_ALBUM_JSON_ERROR");
				Toast.makeText(PersonalInfoScreen.this,
						getString(R.string.image_download_failed),
						Toast.LENGTH_SHORT).show();
				break;

			case UPLOAD_INFO_OK:
					System.out.println("UPLOAD_INFO_OK");
					Toast.makeText(NoteApplication.getContext(),
							getString(R.string.personal_info_set_succ),
							Toast.LENGTH_SHORT).show();
					PersonalInfoScreen.this.finish();

				break;
			case UPLOAD_INFO_ERROR: {
				int uploadcount = msg.getData().getInt("uploadcount");
				if (uploadcount > 3 || uploadcount <= 0 || NoteApplication.networkIsOk == false) {
					System.out.println("UPLOAD_INFO_ERROR");
					Toast.makeText(NoteApplication.getContext(),
							getString(R.string.personal_info_set_fail),
							Toast.LENGTH_SHORT).show();
					PersonalInfoScreen.this.finish();
				} else {
					System.out.println("UPLOAD_INFO_ERROR, try count : "
							+ String.valueOf(uploadcount + 1));
					String aNickname = msg.getData().getString("nickname");
					String aGender = msg.getData().getString("gender");
					String aRegion = msg.getData().getString("region");
					uploadInfo(aNickname, aGender, aRegion, uploadcount + 1);
				}
			}
				break;
			case DOWNLOAD_INFO_JSON_OK:
				System.out.println("DOWNLOAD_INFO_JSON_OK");

				mUserNameTxt = msg.getData().getString("nickname");
				mRegionTxt = msg.getData().getString("region");
				mSex = Integer.parseInt(msg.getData().getString("gender"));

				if (mSex == 2) {
					mRadioFemale.setChecked(true);
					mRadioMale.setChecked(false);
				} else {
					mRadioFemale.setChecked(false);
					mRadioMale.setChecked(true);
				}

				mUserName.setText(mUserNameTxt);

				mUserRegion.setText(mRegionTxt);

				break;
			case DOWNLOAD_INFO_JSON_ERROR:
				System.out.println("DOWNLOAD_INFO_JSON_ERROR");
				Toast.makeText(PersonalInfoScreen.this,
						getString(R.string.personal_info_get_fail),
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}

	private boolean loadLocalPersonalInfo() {
		mPreferences = PreferencesHelper.getSharedPreferences(mContext, 0);
		mUserAvatarPath = mPreferences.getString(
				PreferencesHelper.XML_USER_AVATAR, "");
		if (!new File(mUserAvatarPath).exists()) {
			mUserAvatarPath = "";
		}

		return true;
	}
	
	private boolean savePersonalInfo() {
		String aNickname = mUserName.getText().toString().trim();
		String aGender = mRadioFemale.isChecked() ? "2" : "1";
		String aRegion = mUserRegion.getText().toString();
		uploadInfo(aNickname, aGender, aRegion, 0);
		return true;
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
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;
		case R.id.set_avatar_layout:
			showSelImageDialog();
			break;
		case R.id.region_layout:
			Intent i = new Intent(mContext, PersonalInfoRegionScreen.class);
			startActivityForResult(i, REGION_RESULT);
			break;
		case R.id.confirm_change:
			savePersonalInfo();
			break;
		}
	}
	
//	  public boolean dispatchKeyEvent(KeyEvent event) {  
//		         System.out.println(KeyEvent.KEYCODE_BACK+"--------------------"+event.getKeyCode()+"---------------------"+event.getAction());        
//		         long exitTime = 0;           
//		             if(event.getKeyCode()==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN){  
//    
//		                 finish();   
//		                 return true;     
//		   
//		             }       
//		            
//		        return super.dispatchKeyEvent(event);  
//		     }  

}

package com.archermind.note.Screens;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Utils.ImageCapture;
import com.archermind.note.Utils.PreferencesHelper;
import com.archermind.note.Utils.ServerInterface;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterScreen extends Screen implements OnClickListener {

	private LinearLayout mSetAvatar;
	private ImageView mUserAvatar;
	private EditText mUserName;
	private EditText mNickName;
	private RadioGroup mSex;
	private TextView mRegion;
	private EditText mPassWord;
	private EditText mPswdConfirm;
	private Button mRegisterButton;
	private static final int ALBUM_RESULT = 1;
	private static final int CAMERA_RESULT = 2;
	private static final int CROP_RESULT = 3;
	private static final int REGION_RESULT = 4;
	private static final String UPLOAD_PHOTO_OK = "ok";
	private static final String UPLOAD_PHOTO_ERROR = "error";
	private SharedPreferences mPreferences;
	private Dialog mPicChooseDialog;
	private ContentResolver mContentResolver;
	private String mCameraImageFilePath;
	private ImageCapture mImgCapture;
	private String mAvatarPath;
	private static final String TAG = "RegisterScreen";
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String result = (String) msg.obj;
			if (result.equals("" + ServerInterface.ERROR_SERVER_INTERNAL)) {
				Toast.makeText(RegisterScreen.this,
						R.string.register_err_server_internal,
						Toast.LENGTH_SHORT).show();
				dismissProgress();
			} else if (result.equals("" + ServerInterface.ERROR_ACCOUNT_EXIST)) {
				Toast.makeText(RegisterScreen.this,
						R.string.register_err_account_exist, Toast.LENGTH_SHORT)
						.show();
				dismissProgress();
//			} else if (result.equals(UPLOAD_PHOTO_OK)) {
//				dismissProgress();
//				Toast.makeText(RegisterScreen.this, R.string.register_success,
//						Toast.LENGTH_SHORT).show();
//				Log.i(TAG, "upload avatar success");
//				finish();
//			} else if (result.equals(UPLOAD_PHOTO_ERROR)) {
//				int uploadcount = msg.getData().getInt("uploadcount");
//				if (uploadcount > 3 || uploadcount <= 0) {
//					dismissProgress();
//					Toast.makeText(RegisterScreen.this,
//							R.string.register_success_upload_photo_failed,
//							Toast.LENGTH_SHORT).show();
//					Log.i(TAG, "upload avatar failed");
//					finish();
//				} else {
//					Log.i(TAG,
//							"upload avatar---try count:"
//									+ String.valueOf(uploadcount + 1));
//					String aName = msg.getData().getString("name");
//					String aExpandName = msg.getData().getString("expandname");
//					String aFilePath = msg.getData().getString("filelocalpath");
//					uploadImage(aName, aExpandName, aFilePath, uploadcount + 1);
//				}
			} else {
				dismissProgress();
				try {
					JSONObject jsonObject = new JSONObject(result);
					if (jsonObject.optString("flag").equals(
							"" + ServerInterface.SUCCESS)) {
						// 保存本地
						Editor editor = mPreferences.edit();
						editor.putString(PreferencesHelper.XML_USER_ACCOUNT,
								jsonObject.optString("email"));
						editor.putString(PreferencesHelper.XML_USER_PASSWD,
								jsonObject.getString("pswd"));
						editor.commit();
						// 保存至Application
						NoteApplication noteApplication = NoteApplication
								.getInstance();
						noteApplication.setUserName(jsonObject
								.optString("email"));
						noteApplication.setUserId(jsonObject.optInt("user_id"));
						noteApplication.setmBound_Sina(jsonObject
								.optInt("flag_sina") == 0 ? false : true);
						noteApplication.setmBound_QQ(jsonObject
								.optInt("flag_qq") == 0 ? false : true);
						noteApplication.setmBound_Renren(jsonObject
								.optInt("flag_renren") == 0 ? false : true);
						noteApplication.setLogin(true);

						// 开始上传头像(待修改)
//						if (mAvatarPath != null) {
//							String name = mAvatarPath.substring(
//									mAvatarPath.lastIndexOf("/") + 1,
//									mAvatarPath.length());
//							String expandname = mAvatarPath.substring(
//									mAvatarPath.lastIndexOf(".") + 1,
//									mAvatarPath.length());
//							name = name.substring(0, name.lastIndexOf("."));
//							uploadImage(name, expandname, mAvatarPath, 1);
//							Log.i(TAG, "register sucess,start upload avatar");
//						} else {
							Toast.makeText(RegisterScreen.this,
									R.string.register_success,
									Toast.LENGTH_SHORT).show();
							Log.i(TAG, "register success");
							finish();
//						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		initViews();
		mContentResolver = getContentResolver();
		mImgCapture = new ImageCapture(this, mContentResolver);
		mPreferences = PreferencesHelper.getSharedPreferences(this, 0);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Bitmap image = PreferencesHelper.getAvatarBitmap(this);
		if (image != null) {
			mUserAvatar.setImageBitmap(image);
			if (mAvatarPath == null) {
				mAvatarPath = mPreferences.getString(
						PreferencesHelper.XML_USER_AVATAR, null);
			}
		}
	}

	private void initViews() {
		mSetAvatar = (LinearLayout) findViewById(R.id.register_set_avatar_layout);
		mSetAvatar.setOnClickListener(this);
		mUserAvatar = (ImageView) findViewById(R.id.register_imageview_avatar);
		mUserName = (EditText) findViewById(R.id.register_editText_username);
		mNickName = (EditText) findViewById(R.id.register_edittext_nickname);
		mSex = (RadioGroup) findViewById(R.id.register_ridiogroup_sex);
		mRegion = (TextView) findViewById(R.id.register_tv_region);
		mRegion.setOnClickListener(this);
		mPassWord = (EditText) findViewById(R.id.register_editText_password);
		mPswdConfirm = (EditText) findViewById(R.id.register_editText_pswdconfirm);
		mRegisterButton = (Button) findViewById(R.id.btn_register);
		mRegisterButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.register_set_avatar_layout:
			showSelImageDialog();
			break;
		case R.id.register_tv_region:
			Intent intent = new Intent(this, PersonalInfoRegionScreen.class);
			startActivityForResult(intent, REGION_RESULT);
			break;
		case R.id.btn_register:
			register();
			break;
		default:
			break;
		}
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
				startPhotoCROP(Uri.fromFile(new File(mCameraImageFilePath)));
			}
			break;

		case CROP_RESULT:
			if (data != null) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					Bitmap photo = extras.getParcelable("data");
					photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
					byte[] b = stream.toByteArray();
					this.mImgCapture.storeImage(b, null);
					mAvatarPath = getFilepathFromUri(this.mImgCapture
							.getLastCaptureUri());
					PreferencesHelper.UpdateAvatar(this, "", mAvatarPath);
				}
			}
			break;
		case REGION_RESULT:
			if (data != null) {
				int ProvinceId = data.getIntExtra("province", 0);
				int CityId = data.getIntExtra("city", 0);
				String province = PreferencesHelper.getProvinceName(this,
						ProvinceId);
				String city = PreferencesHelper.getCityName(this, ProvinceId,
						CityId);
				mRegion.setText(province + " " + city);
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
		startActivityForResult(
				Intent.createChooser(intent, getString(R.string.photo_add_sel)),
				ALBUM_RESULT);
	}

	private void getNewImageFromCamera() {
		long dateTaken = System.currentTimeMillis();
		String title = mImgCapture.createName(dateTaken);
		mCameraImageFilePath = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/" + title + ".jpg";
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

	private void register() {
		final String username = mUserName.getText().toString().trim();
		final String password = mPassWord.getText().toString().trim();
		final String pswdconfirm = mPswdConfirm.getText().toString().trim();
		final String nickname = mNickName.getText().toString().trim();
		if (!ServerInterface.isEmail(username)) {
			Toast.makeText(this, R.string.register_err_username_format,
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (!ServerInterface.isPswdValid(password)) {
			Toast.makeText(this, R.string.register_err_password_format,
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (!password.equals(pswdconfirm)) {
			Toast.makeText(this, R.string.register_err_pswdconfirm,
					Toast.LENGTH_SHORT).show();
			return;
		}
		showProgress(null, getString(R.string.register_dialog_msg));// 显示进度框
		new Thread() {

			@Override
			public void run() {
				String result = ServerInterface
						.register(
								getIntent().getExtras().getInt("type"),
								getIntent().getExtras().getString("uid"),
								username,
								password,
								nickname,
								mSex.getCheckedRadioButtonId() == R.id.register_ridiogroup_man ? 1
										: 2, mRegion.getText().toString());
				Message message = new Message();
				message.obj = result;
				mHandler.sendMessage(message);
			}

		}.start();
	}

	private void uploadImage(String name, String expandname, String filepath,
			int uploadcount) {
		final String aName = name;
		final String aExpandName = expandname;
		final String aFilePath = filepath;
		final int aUploadCount = uploadcount;
		new Thread() {
			@Override
			public void run() {
				String user_id = String.valueOf(NoteApplication.getInstance()
						.getUserId());
				String username = NoteApplication.getInstance().getUserName();
				Looper.prepare();
				int result = ServerInterface.uploadPhoto(RegisterScreen.this,
						user_id, username, aFilePath, aName, aExpandName);

				Message msg = new Message();
				msg.getData().putString("name", aName);
				msg.getData().putString("expandname", aExpandName);
				msg.getData().putString("filelocalpath", aFilePath);
				msg.getData().putInt("uploadcount", aUploadCount);
				if (result == 0) {
					msg.obj = String.valueOf(UPLOAD_PHOTO_OK);
				} else {
					msg.obj = String.valueOf(UPLOAD_PHOTO_ERROR);
				}
				mHandler.sendMessage(msg);
			}

		}.start();
	}

}

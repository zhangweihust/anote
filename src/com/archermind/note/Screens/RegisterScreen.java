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
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
	private ProgressDialog mProgressDialog;
	private static final int ALBUM_RESULT = 1;
	private static final int CAMERA_RESULT = 2;
	private static final int CROP_RESULT = 3;
	private SharedPreferences mPreferences;
	private Dialog mPicChooseDialog;
	private ContentResolver mContentResolver;
	private String mCameraImageFilePath;
	private ImageCapture mImgCapture;
	private static final String TAG = "RegisterScreen";
	private OnSharedPreferenceChangeListener spcListener = new OnSharedPreferenceChangeListener() {

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			if (key.equals(PreferencesHelper.XML_USER_REGION_PROVINCE)
					|| key.equals(PreferencesHelper.XML_USER_REGION_CITY)) {
				int provinceId = sharedPreferences.getInt(
						PreferencesHelper.XML_USER_REGION_PROVINCE, -1);
				int cityId = sharedPreferences.getInt(
						PreferencesHelper.XML_USER_REGION_CITY, -1);
				String province = PreferencesHelper.getProvinceName(
						RegisterScreen.this, provinceId);
				String city = PreferencesHelper.getCityName(
						RegisterScreen.this, provinceId, cityId);

				mRegion.setText(province + " " + city);
			}
		}
	};

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String result = (String) msg.obj;
			if (result.equals("" + ServerInterface.ERROR_SERVER_INTERNAL)) {
				Toast.makeText(RegisterScreen.this,
						R.string.register_err_server_internal,
						Toast.LENGTH_SHORT).show();
				mProgressDialog.dismiss();
			} else if (result.equals("" + ServerInterface.ERROR_ACCOUNT_EXIST)) {
				Toast.makeText(RegisterScreen.this,
						R.string.register_err_account_exist, Toast.LENGTH_SHORT)
						.show();
				mProgressDialog.dismiss();
			} else {
				mProgressDialog.dismiss();
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
						noteApplication.setLogin(true);
						Log.i(TAG, "register success");
						finish();
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
		mPreferences = getSharedPreferences(PreferencesHelper.XML_NAME, 0);
		mPreferences.registerOnSharedPreferenceChangeListener(spcListener);
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
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mPreferences.unregisterOnSharedPreferenceChangeListener(spcListener);
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
			startActivity(intent);
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
					String filepath = getFilepathFromUri(this.mImgCapture
							.getLastCaptureUri());
					PreferencesHelper.UpdateAvatar(this, filepath);
				}
			}

			break;
		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void showProgressDialog() {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage("正在登录...");
		mProgressDialog.show();
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
		showProgressDialog();// 显示进度框
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
										: 0,
								mPreferences
										.getInt(PreferencesHelper.XML_USER_REGION_PROVINCE,
												-1), mPreferences.getInt(
										PreferencesHelper.XML_USER_REGION_CITY,
										-1));
				Message message = new Message();
				message.obj = result;
				mHandler.sendMessage(message);
			}

		}.start();
	}

}

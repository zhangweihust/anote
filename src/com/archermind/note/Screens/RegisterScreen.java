package com.archermind.note.Screens;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.amtcloud.mobile.android.business.AmtAlbumObj;
import com.amtcloud.mobile.android.business.AmtApplication;
import com.amtcloud.mobile.android.business.MessageTypes;
import com.amtcloud.mobile.android.business.AmtAlbumObj.AlbumItem;
import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Utils.AlbumInfoUtil;
import com.archermind.note.Utils.ImageCapture;
import com.archermind.note.Utils.PreferencesHelper;
import com.archermind.note.Utils.ServerInterface;

import android.R.integer;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterScreen extends Screen implements OnClickListener {

	public static final String ALBUMNAME_AVATAR = "avatar";
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
	private SharedPreferences mPreferences;
	private Dialog mPicChooseDialog;
	private ContentResolver mContentResolver;
	private String mCameraImageFilePath;
	private ImageCapture mImgCapture;
	private String mAvatarPath;
	private AmtAlbumObj mAlbumObj;
	private static final String TAG = "RegisterScreen";
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// 处理图片上传过程发送的消息
			Log.i(TAG, "handler_message:" + msg.what);
			switch (msg.what) {
			case MessageTypes.ERROR_MESSAGE:
				Toast.makeText(RegisterScreen.this,
						R.string.register_success_upload_photo_failed,
						Toast.LENGTH_SHORT).show();
				dismissProgress();
				break;
			case MessageTypes.MESSAGE_CREATEALBUM:
				mAlbumObj.requestAlbumidInfo(NoteApplication.getInstance()
						.getUserName());
				break;
			case MessageTypes.MESSAGE_GETALBUM:
				AlbumItem[] albumItems = AlbumInfoUtil.getAlbumInfos(mAlbumObj,
						msg.obj);
				if (albumItems == null) {
					mAlbumObj.createAlbum(NoteApplication.getInstance()
							.getUserName(), ALBUMNAME_AVATAR);
					break;
				}
				int albumid = -1;
				for (int i = 0; i < albumItems.length; i++) {
					if (albumItems[i].albumname.equals(ALBUMNAME_AVATAR)) {
						albumid = albumItems[i].albumid;
					}
				}
				if (albumid == -1) {
					mAlbumObj.createAlbum(NoteApplication.getInstance()
							.getUserName(), ALBUMNAME_AVATAR);
				} else {
					ArrayList<String> picPath = new ArrayList<String>();
					picPath.add(mAvatarPath);
					ArrayList<String> picNames = new ArrayList<String>();
					picNames.add(mAvatarPath.substring(mAvatarPath
							.lastIndexOf("/") + 1));
					Log.i(TAG,
							"图片名称："
									+ mAvatarPath.substring(mAvatarPath
											.lastIndexOf("/") + 1));
					mAlbumObj.uploadPicFiles(picPath, picNames, albumid);
					Log.i(TAG, "albumid：" + albumid);
				}
				break;
			case MessageTypes.MESSAGE_UPLOADPIC:
				// 上传头像文件成功，开始执行插入数据库操作
				String url = NoteApplication.getInstance().getUserName()
						+ "&filename="
						+ mAvatarPath
								.substring(mAvatarPath.lastIndexOf("/") + 1)
						+ "&album=" + ALBUMNAME_AVATAR;
				UploadAvatarTask uploadAvatarTask = new UploadAvatarTask();
				uploadAvatarTask.execute(String.valueOf(NoteApplication
						.getInstance().getUserId()), url);
			default:
				break;
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
		Intent intent = getIntent();
		mSetAvatar = (LinearLayout) findViewById(R.id.register_set_avatar_layout);
		mSetAvatar.setOnClickListener(this);
		mUserAvatar = (ImageView) findViewById(R.id.register_imageview_avatar);
		mNickName = (EditText) findViewById(R.id.register_edittext_nickname);
		mNickName.setText(intent.getStringExtra("bin_nickname"));
		mSex = (RadioGroup) findViewById(R.id.register_ridiogroup_sex);
		if (intent.getIntExtra("type", 0) == ServerInterface.LOGIN_TYPE_SINA) {
			mSex.check(intent.getStringExtra("sex").equals("m") ? R.id.register_ridiogroup_man
					: R.id.register_ridiogroup_woman);
		} else if (intent.getIntExtra("type", 0) == ServerInterface.LOGIN_TYPE_QQ) {
			mSex.check(intent.getStringExtra("sex").equals("1") ? R.id.register_ridiogroup_man
					: R.id.register_ridiogroup_woman);
		} else if (intent.getIntExtra("type", 0) == ServerInterface.LOGIN_TYPE_RENREN) {
			mSex.check(intent.getStringExtra("sex").equals("1") ? R.id.register_ridiogroup_man
					: R.id.register_ridiogroup_woman);
		}
		mRegion = (TextView) findViewById(R.id.register_tv_region);
		mRegion.setText(intent.getStringExtra("location"));
		mRegion.setOnClickListener(this);
		mUserName = (EditText) findViewById(R.id.register_editText_username);
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
					photo = PreferencesHelper.toRoundCorner(photo, 15);
					photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
					byte[] b = stream.toByteArray();
					this.mImgCapture.storeImage(b, null,
							Bitmap.CompressFormat.PNG);
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
			mUserName.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.shake));
			Toast.makeText(this, R.string.register_err_username_format,
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (!ServerInterface.isPswdValid(password)) {
			mPassWord.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.shake));
			Toast.makeText(this, R.string.register_err_password_format,
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (!password.equals(pswdconfirm)) {
			mPswdConfirm.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.shake));
			Toast.makeText(this, R.string.register_err_pswdconfirm,
					Toast.LENGTH_SHORT).show();
			return;
		}
		RegisterTask registerTask = new RegisterTask();
		registerTask
				.execute(
						String.valueOf(getIntent().getIntExtra("type", 0)),
						getIntent().getStringExtra("bin_uid"),
						getIntent().getStringExtra("bin_nickname"),
						username,
						password,
						nickname,
						mSex.getCheckedRadioButtonId() == R.id.register_ridiogroup_man ? "1"
								: "2", mRegion.getText().toString());
		Log.i(TAG,
				"sex:"
						+ (mSex.getCheckedRadioButtonId() == R.id.register_ridiogroup_man ? "1"
								: "2"));
	}

	/*
	 * 注册异步封装类(数据库操作)
	 */
	class RegisterTask extends AsyncTask<String, integer, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgress(null, getString(R.string.register_dialog_msg));// 显示进度框
		}

		@Override
		protected String doInBackground(String... params) {
			String result = ServerInterface.register(params[0], params[1],
					params[2], params[3], params[4], params[5], params[6],
					params[7]);
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
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
			} else if (result.equals("" + ServerInterface.ERROR_USER_BINDED)) {
				Toast.makeText(RegisterScreen.this,
						R.string.account_bound_failed_exist, Toast.LENGTH_SHORT)
						.show();
				dismissProgress();
			} else {
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
						noteApplication.setmAvatarurl(jsonObject
								.optString("portrait"));
						noteApplication.setmNickname(jsonObject
								.optString("nickname"));
						noteApplication.setmSex(jsonObject.optString("gender"));
						noteApplication.setmRegion(jsonObject
								.optString("region"));
						noteApplication.setmSina_nickname(jsonObject
								.optString("flag_sina"));
						noteApplication.setmQQ_nickname(jsonObject
								.optString("flag_qq"));
						noteApplication.setmRenren_nickname(jsonObject
								.optString("flag_renren"));
						noteApplication.setLogin(true);

						// 文件操作：上传头像文件
						if (mAvatarPath != null) {
							AmtApplication.setAmtUserName(NoteApplication
									.getInstance().getUserName());
							mAlbumObj = new AmtAlbumObj();
							mAlbumObj.setHandler(mHandler);
							mAlbumObj.createAlbum(NoteApplication.getInstance()
									.getUserName(), ALBUMNAME_AVATAR);
							Log.i(TAG, "register sucess,start upload avatar");
						} else {
							Toast.makeText(RegisterScreen.this,
									R.string.register_success,
									Toast.LENGTH_SHORT).show();
							Log.i(TAG, "register success");
							dismissProgress();
							finish();
						}
					}
				} catch (JSONException e) {
					dismissProgress();
					Toast.makeText(RegisterScreen.this, R.string.register_failed,
							Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		}

	}

	/*
	 * 上传头像异步封装类(数据库操作:插入图片地址)
	 */
	class UploadAvatarTask extends AsyncTask<String, integer, String> {

		@Override
		protected String doInBackground(String... params) {
			int result = ServerInterface.uploadAvatar(params[0], params[1]);
			if (result == ServerInterface.SUCCESS) {
				return "success";
			} else {
				return "failed";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result.equals("success")) {
				Toast.makeText(RegisterScreen.this, R.string.register_success,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(RegisterScreen.this,
						R.string.register_success_upload_photo_failed,
						Toast.LENGTH_SHORT).show();
			}
			dismissProgress();
			finish();
		}

	}

	// class LoadImgTask extends AsyncTask<String, integer, Drawable>{
	//
	// @Override
	// protected Drawable doInBackground(String... params) {
	// // TODO Auto-generated method stub
	// try {
	// Log.i(TAG, params[0]);
	// Drawable drawable = Drawable.createFromStream(new
	// URL(params[0]).openStream(), "image.png");
	// return drawable;
	// } catch (MalformedURLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return null;
	// }
	//
	// @Override
	// protected void onPostExecute(Drawable result) {
	// super.onPostExecute(result);
	// mUserAvatar.setImageDrawable(result);
	// }
	// }

}

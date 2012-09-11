package com.archermind.note.Screens;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class PersonInfoScreen extends Screen implements OnClickListener {

	private Button mBackButton;
	private LinearLayout mSetAvatar;
	private ImageView mUserAvatar;
	private EditText mNickname;
	private RadioGroup mSex;
	private TextView mRegion;
	private Button mConfirmButton;
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
	private ProgressBar mProgressBar;
	private boolean ismodifyAvatar = false;
	private static final String TAG = "PersonInfoScreen";
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// 处理图片上传过程发送的消息
			Log.i(TAG, "handler_message:" + msg.what);
			switch (msg.what) {
			case MessageTypes.ERROR_MESSAGE:
				mProgressBar.setVisibility(View.GONE);
				Toast.makeText(PersonInfoScreen.this,
						R.string.update_failed,
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
							.getUserName(), RegisterScreen.ALBUMNAME_AVATAR);
					break;
				}
				int albumid = -1;
				for (int i = 0; i < albumItems.length; i++) {
					if (albumItems[i].albumname
							.equals(RegisterScreen.ALBUMNAME_AVATAR)) {
						albumid = albumItems[i].albumid;
					}
				}
				if (albumid == -1) {
					mAlbumObj.createAlbum(NoteApplication.getInstance()
							.getUserName(), RegisterScreen.ALBUMNAME_AVATAR);
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
						+ "&album=" + RegisterScreen.ALBUMNAME_AVATAR;
				UpdateTask updateTask = new UpdateTask();
				updateTask.execute(String.valueOf(NoteApplication.getInstance()
						.getUserId()), mNickname.getText().toString(), mSex
						.getCheckedRadioButtonId() == R.id.radioMale ? "1"
						: "2", mRegion.getText().toString(), url);
			default:
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personalinfo_screen);
		initViews();
		mContentResolver = getContentResolver();
		mImgCapture = new ImageCapture(this, mContentResolver);
		mPreferences = PreferencesHelper.getSharedPreferences(this, 0);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (mPreferences.getString(PreferencesHelper.XML_USER_AVATAR, null) != null) {
			mUserAvatar.setImageBitmap(PreferencesHelper.getAvatarBitmap(this));
		} else {
			String avatar_url = NoteApplication.getInstance().getmAvatarurl();
			if (!avatar_url.equals("")) {
				LoadImgTask loadImgTask = new LoadImgTask();
				loadImgTask.execute(ServerInterface.IMG_DOWADING_HEAD
						+ avatar_url);
			}
		}
	}

	private void initViews() {
		mBackButton = (Button) findViewById(R.id.screen_top_play_control_back);
		mBackButton.setOnClickListener(this);
		mProgressBar = (ProgressBar) findViewById(R.id.perosoninfo_progressBar);

		mSetAvatar = (LinearLayout) findViewById(R.id.set_avatar_layout);
		mSetAvatar.setOnClickListener(this);
		mUserAvatar = (ImageView) findViewById(R.id.user_avatar);

		mNickname = (EditText) findViewById(R.id.user_nickname);
		mNickname.setText(NoteApplication.getInstance().getmNickname());

		mSex = (RadioGroup) findViewById(R.id.radioGroup);
		mSex.check(NoteApplication.getInstance().getmSex().equals("1") ? R.id.radioMale
				: R.id.radioFemale);

		mRegion = (TextView) findViewById(R.id.user_region);
		mRegion.setText(NoteApplication.getInstance().getmRegion());
		mRegion.setOnClickListener(this);

		mConfirmButton = (Button) findViewById(R.id.confirm_change);
		mConfirmButton.setOnClickListener(this);
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
					mImgCapture.storeImage(b, null,
							Bitmap.CompressFormat.PNG);
					mAvatarPath = getFilepathFromUri(this.mImgCapture
							.getLastCaptureUri());
					PreferencesHelper.UpdateAvatar(this, "", mAvatarPath);
					ismodifyAvatar = true;
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.set_avatar_layout:
			showSelImageDialog();
			break;
		case R.id.user_region:
			Intent intent = new Intent(this, PersonalInfoRegionScreen.class);
			startActivityForResult(intent, REGION_RESULT);
			break;
		case R.id.confirm_change:
			Toast.makeText(this, R.string.update_progress,
					Toast.LENGTH_SHORT).show();
			mProgressBar.setVisibility(View.VISIBLE);
			if (ismodifyAvatar) {
				AmtApplication.setAmtUserName(NoteApplication.getInstance()
						.getUserName());
				mAlbumObj = new AmtAlbumObj();
				mAlbumObj.setHandler(mHandler);
				mAlbumObj.requestAlbumidInfo(NoteApplication.getInstance()
						.getUserName());
			} else {
				UpdateTask updateTask = new UpdateTask();
				updateTask.execute(String.valueOf(NoteApplication.getInstance()
						.getUserId()), mNickname.getText().toString(), mSex
						.getCheckedRadioButtonId() == R.id.radioMale ? "1"
						: "2", mRegion.getText().toString(), "");
			}
			break;
		case R.id.screen_top_play_control_back:
			finish();
			break;
		default:
			break;
		}
	}

	/*
	 * 修改个人信息（数据库操作）
	 */
	class UpdateTask extends AsyncTask<String, integer, String> {

		@Override
		protected String doInBackground(String... params) {
			return ServerInterface.update_info(params[0], params[1], params[2],
					params[3], params[4]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			mProgressBar.setVisibility(View.GONE);
			if (result.equals("" + ServerInterface.ERROR_SERVER_INTERNAL)) {
				Toast.makeText(PersonInfoScreen.this,
						R.string.register_err_server_internal,
						Toast.LENGTH_SHORT).show();
			} else if (result.equals("" + ServerInterface.COOKIES_ERROR)) {
				NoteApplication.getInstance().setLogin(false);
				Toast.makeText(NoteApplication.getContext(),
						R.string.cookies_error, Toast.LENGTH_SHORT).show();
			} else if (result.equals("" + ServerInterface.SUCCESS)) {
				Toast.makeText(PersonInfoScreen.this,
						R.string.update_success,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(PersonInfoScreen.this,
						R.string.update_failed,
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	/*
	 * 异步加载头像图片
	 */
	class LoadImgTask extends AsyncTask<String, integer, Bitmap> {

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bitmap = null;
			try {
				Log.i(TAG, "头像url:" + params[0]);
				URL url = new URL(params[0]);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				connection.setDoInput(true);
				connection.connect();
				bitmap = BitmapFactory
						.decodeStream(connection.getInputStream());
				return bitmap;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			if (result != null) {
				mUserAvatar.setImageBitmap(result);
				//储存下载下来的头像图片
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				result.compress(Bitmap.CompressFormat.PNG, 100, stream);
				byte[] b = stream.toByteArray();
				mImgCapture.storeImage(b, null,
						Bitmap.CompressFormat.PNG);
				mAvatarPath = getFilepathFromUri(mImgCapture
						.getLastCaptureUri());
				PreferencesHelper.UpdateAvatar(PersonInfoScreen.this, "", mAvatarPath);
			} else {
				Toast.makeText(PersonInfoScreen.this,
						R.string.personal_info_loadavatar_failed,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

}

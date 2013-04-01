package com.android.note.Screens;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import com.amtcloud.mobile.android.business.AmtAlbumObj;
import com.amtcloud.mobile.android.business.AmtApplication;
import com.amtcloud.mobile.android.business.MessageTypes;
import com.amtcloud.mobile.android.business.AmtAlbumObj.AlbumItem;
import com.android.note.NoteApplication;
import com.android.note.Services.ExceptionService;
import com.android.note.Services.ServiceManager;
import com.android.note.Utils.AlbumInfoUtil;
import com.android.note.Utils.ImageCapture;
import com.android.note.Utils.PreferencesHelper;
import com.android.note.Utils.ServerInterface;
import com.android.note.crop.CropImage;
import com.archermind.note.R;

import android.R.integer;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
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
	public static final int ROTATE_RESULT=5;
	private SharedPreferences mPreferences;
	private ContentResolver mContentResolver;
	private String mImageFilePath;
	private ImageCapture mImgCapture;
	private String mAvatarPath;
	private Bitmap mAvatarBitmap;
	private Dialog mPicChooseDialog;
	private AmtAlbumObj mAlbumObj;
//	private ProgressBar mProgressBar;
	private boolean ismodifyAvatar = false;
	private static final String TAG = "PersonInfoScreen";
	
	public static final int INTENT_ID=2;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// 处理图片上传过程发送的消息
			Log.i(TAG, "handler_message:" + msg.what);
			switch (msg.what) {
			case MessageTypes.ERROR_MESSAGE:
//				mProgressBar.setVisibility(View.GONE);
				dismissProgress();
				Toast.makeText(PersonInfoScreen.this, R.string.update_failed,
						Toast.LENGTH_SHORT).show();
				dismissProgress();
				break;
			case MessageTypes.MESSAGE_CREATEALBUM:
				try{
					mAlbumObj.requestAlbumidInfo(ServiceManager.getUserName());
				}catch (Exception e) {
					// TODO: handle exception
					ExceptionService.logException(e);
					Toast.makeText(PersonInfoScreen.this, R.string.update_failed,
							Toast.LENGTH_SHORT).show();
				}
				break;
			case MessageTypes.MESSAGE_GETALBUM:
				AlbumItem[] albumItems = AlbumInfoUtil.getAlbumInfos(mAlbumObj,
						msg.obj);
				String username = ServiceManager.getUserName();
				if(username != null){
					try {
						if (albumItems == null) {
							mAlbumObj.createAlbum(username,
									RegisterScreen.ALBUMNAME_AVATAR);
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
							mAlbumObj.createAlbum(username,
									RegisterScreen.ALBUMNAME_AVATAR);
						} else {
							// 先保存本地头像，然后上传文件
							ByteArrayOutputStream stream = new ByteArrayOutputStream();
							mAvatarBitmap.compress(Bitmap.CompressFormat.PNG, 100,
									stream);
							byte[] b = stream.toByteArray();
							mImgCapture.storeImage(b, null, Bitmap.CompressFormat.PNG);
							mAvatarPath = getFilepathFromUri(mImgCapture
									.getLastCaptureUri());

							ArrayList<String> picPath = new ArrayList<String>();
							picPath.add(mAvatarPath);
							ArrayList<String> picNames = new ArrayList<String>();
							picNames.add(mAvatarPath.substring(mAvatarPath
									.lastIndexOf("/") + 1));
							mAlbumObj.uploadPicFiles(username,picPath, picNames, albumid);
							Log.i(TAG, "albumid：" + albumid);
						}
					} catch (Exception e) {
						// TODO: handle exception
						ExceptionService.logException(e);
						Toast.makeText(PersonInfoScreen.this, R.string.update_failed,
								Toast.LENGTH_SHORT).show();
					}
				}else{
					Toast.makeText(PersonInfoScreen.this, R.string.update_failed,
							Toast.LENGTH_SHORT).show();
				}
				break;
			case MessageTypes.MESSAGE_UPLOADPIC:// 上传头像文件成功
				// 开始执行插入数据库操作
				if(ServiceManager.getUserName()!=null){
					try {
						String url = ServiceManager.getUserName()
								+ "&filename="
								+ mAvatarPath
										.substring(mAvatarPath.lastIndexOf("/") + 1)
								+ "&album=" + RegisterScreen.ALBUMNAME_AVATAR;
						UpdateTask updateTask = new UpdateTask();
						updateTask.execute(String.valueOf(ServiceManager.getUserId()),
								mNickname.getText().toString(),
								mSex.getCheckedRadioButtonId() == R.id.radioMale ? "1"
										: "2", mRegion.getText().toString(), url);
					} catch (Exception e) {
						// TODO: handle exception
						ExceptionService.logException(e);
					}
				}
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
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mAvatarBitmap != null) {
			mAvatarBitmap.recycle();
		}
	}

	private void initViews() {
		mBackButton = (Button) findViewById(R.id.screen_top_play_control_back);
		mBackButton.setOnClickListener(this);
//		mProgressBar = (ProgressBar) findViewById(R.id.perosoninfo_progressBar);

		mSetAvatar = (LinearLayout) findViewById(R.id.set_avatar_layout);
		mSetAvatar.setOnClickListener(this);
		mUserAvatar = (ImageView) findViewById(R.id.user_avatar);
		Bitmap image = PreferencesHelper.getAvatarBitmap(this);
		if (image != null) {
			mUserAvatar.setImageBitmap(image);
		} else {
			String avatar_url = ServiceManager.getmAvatarurl();
			if (!avatar_url.equals("")) {
				LoadImgTask loadImgTask = new LoadImgTask();
				loadImgTask.execute(ServerInterface.IMG_DOWADING_HEAD
						+ avatar_url);
			}
		}

		mNickname = (EditText) findViewById(R.id.user_nickname);
		mNickname.setText(ServiceManager.getmNickname());
		mNickname.addTextChangedListener(new myTextWatcher());

		mSex = (RadioGroup) findViewById(R.id.radioGroup);
		mSex.check(ServiceManager.getmSex().equals("1") ? R.id.radioMale
				: R.id.radioFemale);
		mSex.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				mConfirmButton.setEnabled(true);
			}
		});

		mRegion = (TextView) findViewById(R.id.user_region);
		mRegion.setText(ServiceManager.getmRegion());
		mRegion.setOnClickListener(this);
		mRegion.addTextChangedListener(new myTextWatcher());

		mConfirmButton = (Button) findViewById(R.id.confirm_change);
		mConfirmButton.setOnClickListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case ALBUM_RESULT:
				if (data != null) {
					Uri uri = data.getData();
					startPhotoCROP(uri);
				}
				break;

			case CAMERA_RESULT:
				startPhotoCROP(Uri.fromFile(new File(mImageFilePath)));
				break;

			case CROP_RESULT:
				// if (data != null) {
				// Bundle extras = data.getExtras();
				// if(extras != null){
				// mAvatarBitmap = extras.getParcelable("data");
				// mAvatarBitmap = PreferencesHelper.toRoundCorner(
				// mAvatarBitmap, 15);
				// mUserAvatar.setImageBitmap(mAvatarBitmap);
				// ismodifyAvatar = true;
				// }
				// }
				
				Bitmap bmp= BitmapFactory.decodeFile(mImageFilePath);
				Intent intent = new Intent(PersonInfoScreen.this,RotateImageScreen.class);
				intent.putExtra("bm", PreferencesHelper.Bitmap2Bytes(bmp));
				intent.putExtra("id", INTENT_ID);
				//startActivity(intent);
				startActivityForResult(intent,ROTATE_RESULT);
//				mAvatarBitmap = PreferencesHelper.toRoundCorner(
//						BitmapFactory.decodeFile(mImageFilePath), 10);
				//更新头像图片地址
//				PreferencesHelper.UpdateAvatar(PersonInfoScreen.this, "",
//						mImageFilePath);
//                if(mAvatarBitmap != null){
//					mUserAvatar.setImageBitmap(mAvatarBitmap);
//					ismodifyAvatar = true;
//					mConfirmButton.setEnabled(true);
//				}
				break;
			case REGION_RESULT:
				if (data != null) {
					int ProvinceId = data.getIntExtra("province", 0);
					int CityId = data.getIntExtra("city", 0);
					String province = PreferencesHelper.getProvinceName(this,
							ProvinceId);
					String city = PreferencesHelper.getCityName(this,
							ProvinceId, CityId);
					mRegion.setText(province + " " + city);
				}
				break;
			case ROTATE_RESULT:
				byte buff[] = data.getByteArrayExtra("bm");
				if(buff!=null){
					mAvatarBitmap = PreferencesHelper.toRoundCorner(
							BitmapFactory.decodeByteArray(buff, 0, buff.length), 10);
//					PreferencesHelper.UpdateAvatar(PersonInfoScreen.this, "",
//							mImageFilePath);
					 if(mAvatarBitmap != null){
							mUserAvatar.setImageBitmap(mAvatarBitmap);
							ismodifyAvatar = true;
							mConfirmButton.setEnabled(true);
						}
				}
				break;
			default:
				break;

			}
		}
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
		File imageFile = new File(mImageFilePath);
		Uri imageFileUri = Uri.fromFile(imageFile);
		Intent intent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
		startActivityForResult(intent, CAMERA_RESULT);
	}

	private void startPhotoCROP(Uri uri) {
		// Intent intent = new Intent("com.android.camera.action.CROP");
		// intent.setDataAndType(uri, "image/*");
		// intent.putExtra("crop", "true");
		// // aspectX aspectY 是宽高的比例
		// intent.putExtra("aspectX", 1);
		// intent.putExtra("aspectY", 1);
		// // outputX outputY 是裁剪图片宽高
		// intent.putExtra("outputX", 105);
		// intent.putExtra("outputY", 105);
		// intent.putExtra("noFaceDetection", true);
		// intent.putExtra("output", Uri.fromFile(new File(mImageFilePath)));
		// intent.putExtra("outputFormat", "JPEG");//返回格式
		Intent intent = new Intent(this, CropImage.class);
		intent.setDataAndType(uri, "image/*");
		Bundle extras = new Bundle();
		extras.putString("circleCrop", "true");
		extras.putInt("aspectX", 1);
		extras.putInt("aspectY", 1);
		extras.putInt("outputX", 105);
		extras.putInt("outputY", 105);
		extras.putString("output", mImageFilePath);
		extras.putBoolean("scale", true);
		intent.putExtras(extras);
		startActivityForResult(intent, CROP_RESULT);
	}

	private void showSelImageDialog() {
		if (mPicChooseDialog == null) {
			mPicChooseDialog = new Dialog(this, R.style.CornerDialog);
			mPicChooseDialog.setContentView(R.layout.dialog_pic_source);
			TextView cameraView = (TextView) mPicChooseDialog
					.findViewById(R.id.dialog_item_camera);
			cameraView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mPicChooseDialog.dismiss();
					getNewImageFromCamera();
				}
			});
			TextView localView = (TextView) mPicChooseDialog
					.findViewById(R.id.dialog_item_local);
			localView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mPicChooseDialog.dismiss();
					getNewImageFromLocal();
				}
			});
		}
		mPicChooseDialog.show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.set_avatar_layout:
			mImageFilePath = ImageCapture.IMAGE_CACHE_PATH + "/temp.jpg";
			showSelImageDialog();
			break;
		case R.id.user_region:
			Intent intent = new Intent(this, PersonalInfoRegionScreen.class);
			startActivityForResult(intent, REGION_RESULT);
			break;
		case R.id.confirm_change:
//			Toast.makeText(this, R.string.update_progress, Toast.LENGTH_SHORT)
//					.show();
//			mProgressBar.setVisibility(View.VISIBLE);
			showProgress(null, getString(R.string.update_progress));
			if (ismodifyAvatar && ServiceManager.getUserName()!=null) {
				try {
//					AmtApplication.setAmtUserName(ServiceManager.getUserName());
					mAlbumObj = new AmtAlbumObj();
					mAlbumObj.setHandler(mHandler);
					mAlbumObj.requestAlbumidInfo(ServiceManager.getUserName());
					//修改成功再修改temp图片
					PreferencesHelper.UpdateAvatar(PersonInfoScreen.this, "",
							mImageFilePath);
				} catch (Exception e) {
					// TODO: handle exception
					ExceptionService.logException(e);
				}
			} else {
				UpdateTask updateTask = new UpdateTask();
				updateTask.execute(String.valueOf(ServiceManager.getUserId()),
						mNickname.getText().toString(),
						mSex.getCheckedRadioButtonId() == R.id.radioMale ? "1"
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

	class myTextWatcher implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			mConfirmButton.setEnabled(true);
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
//			mProgressBar.setVisibility(View.GONE);
			dismissProgress();
			if (result.equals("" + ServerInterface.ERROR_SERVER_INTERNAL)) {
				Toast.makeText(PersonInfoScreen.this,
						R.string.register_err_server_internal,
						Toast.LENGTH_SHORT).show();
			} else if (result.equals("" + ServerInterface.COOKIES_ERROR)) {
				ServiceManager.setLogin(false);
				Toast.makeText(NoteApplication.getContext(),
						R.string.cookies_error, Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(PersonInfoScreen.this,
						LoginScreen.class);
				startActivity(intent);
			} else if (result.equals("" + ServerInterface.SUCCESS)) {
				if (ismodifyAvatar) {
					PreferencesHelper.UpdateAvatar(PersonInfoScreen.this, "",
							mAvatarPath);
				}
				ServiceManager.setmNickname(mNickname.getText().toString());
				ServiceManager
						.setmSex(mSex.getCheckedRadioButtonId() == R.id.radioMale ? "1"
								: "2");
				ServiceManager.setmRegion(mRegion.getText().toString());
				
				Toast.makeText(PersonInfoScreen.this, R.string.update_success,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(PersonInfoScreen.this, R.string.update_failed,
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
			} catch (Exception e) {
				e.printStackTrace();
			} catch(OutOfMemoryError e){
            	e.printStackTrace();
            }
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			if (result != null) {
				mUserAvatar.setImageBitmap(result);
				mUserAvatar.startAnimation(AnimationUtils.loadAnimation(
						PersonInfoScreen.this, R.anim.alpha));
				// 储存下载下来的头像图片
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				result.compress(Bitmap.CompressFormat.PNG, 100, stream);
				byte[] b = stream.toByteArray();
				mImgCapture.storeImage(b, null, Bitmap.CompressFormat.PNG);
				mAvatarPath = getFilepathFromUri(mImgCapture
						.getLastCaptureUri());
				PreferencesHelper.UpdateAvatar(PersonInfoScreen.this, "",
						mAvatarPath);
			} else {
				Toast.makeText(PersonInfoScreen.this,
						R.string.personal_info_loadavatar_failed,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

}

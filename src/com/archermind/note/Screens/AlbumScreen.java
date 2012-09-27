package com.archermind.note.Screens;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore.Audio;
import android.text.TextUtils;
import android.util.MonthDisplayHelper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.amtcloud.mobile.android.business.AmtAlbumObj;
import com.amtcloud.mobile.android.business.AmtApplication;
import com.amtcloud.mobile.android.business.MessageTypes;
import com.amtcloud.mobile.android.business.AmtAlbumObj.AlbumItem;
import com.amtcloud.mobile.android.core.processobject.AmtDataObject;
import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Adapter.PhotoAdapter;
import com.archermind.note.Adapter.PhotoAdapter.ViewHolder;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Screens.RegisterScreen.UploadAvatarTask;
import com.archermind.note.Services.ExceptionService;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.AlbumInfoUtil;
import com.archermind.note.Utils.ImageCapture;
import com.archermind.note.Utils.PreferencesHelper;
import com.archermind.note.Utils.ServerInterface;
import com.archermind.note.Views.AlbumScrollLayout;
import com.archermind.note.Views.AlbumScrollLayout.OnScreenChangeListener;
import com.archermind.note.Views.AlbumScrollLayout.OnScreenChangeListenerDataLoad;
import com.archermind.note.Views.PhotoGallery;

public class AlbumScreen extends Screen implements OnClickListener {

	private PhotoGallery mPhotoGallery;
	private static AlbumScrollLayout mPhotoView;
	private View mPhotoGalleryLayout;
	private View mPhotoGridLayout;
	private RelativeLayout mTitleLayout;
	
	private Button mBtnGalleryBack;
	private Button mBtnGridBack;
	private ImageButton mBtnGallerySetAvatar;
	private Button mBtnGridInsertImage;
	
	private TextView mGalleryTitle;
	
	private Bitmap mCacheImage;
	
	private ViewHolder mLastSelItem;
	private ViewHolder mSelItem;
	
	private Dialog mPicChooseDialog;
	
	private static final float APP_PAGE_SIZE = 12.0f;
	
	private static final int ALBUM_RESULT = 1;
	private static final int CAMERA_RESULT = 2;
	private static final int CROP_RESULT = 3;
	
	private static final int ALBUM_COOKIES_ERROR = -600;
	private static final int ALBUM_NO_INTERNET = 101;
	
	private static final int DOWNLOAD_THUMB_ALBUM_JSON_OK = 0;
	private static final int DOWNLOAD_THUMB_ALBUM_JSON_ERROR = -1;
	private static final int DOWNLOAD_ALL_ALBUM_OK = 1;
	private static final int DOWNLOAD_ALL_ALBUM_ERROR = 2;
	
	private static final int UPLOAD_ALBUM_OK = 3;
	private static final int UPLOAD_ALBUM_ERROR = 4;
	
	private static final int UPLOAD_ALBUM = 5;
	
	private static final int TITLE_GONE_TIME = 2 * 1000; 	/* 2秒后标题栏消失 */
	private static final int TITLE_GONE_MSG = 1;
	private static final int TITLE_GONE_CANCEL = 2;
	private static final int TITLE_GONE = 3;
	
	private int mPageIndex;
	
	private ContentResolver mContentResolver;
	
	private String mCacheImageFilePath;
 
    private ImageCapture mImgCapture;
    
    private ServerInterface serverInterface;
    
    private Context mContext;
    
	private MyHandler myHandler;
	private UpDownloadHandler handler;
	
	private Handler GalleryStatushandler;
	private DataLoading dataLoad;
	
	private static PhotoAdapter mLastChildAdapter;
	private static PhotoAdapter mGalleryPhotoAdapter;
	
	private String mAlbumUrllist;
	
	private String mAvatarPath;
	private AmtAlbumObj mAlbumObj;
	private static final String ALBUMNAME = "myalbumname";
	private Message uploadnewmsg = new Message();
	private AmtDataObject uploadTask = null;
	private boolean activityFirstStart = true;
	List<Map> ablumPhotoList = new ArrayList<Map>();
	private TextView NoPhotoPrompt;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// 处理图片上传过程发送的消息
			try {
				switch (msg.what) {
				case MessageTypes.ERROR_MESSAGE:
					dismissProgress();
					uploadnewmsg.what = UPLOAD_ALBUM_ERROR;
					handler.sendMessage(uploadnewmsg);
					break;
				case MessageTypes.MESSAGE_CREATEALBUM:
					mAlbumObj.requestAlbumidInfo(ServiceManager
							.getUserName());
					break;
				case MessageTypes.MESSAGE_GETALBUM:
					AlbumItem[] albumItems = AlbumInfoUtil.getAlbumInfos(mAlbumObj,
							msg.obj);
					if (albumItems == null) {
						mAlbumObj.createAlbum(ServiceManager
								.getUserName(), ALBUMNAME);
						break;
					}
					int albumid = -1;
					for (int i = 0; i < albumItems.length; i++) {
						if (albumItems[i].albumname.equals(ALBUMNAME)) {
							albumid = albumItems[i].albumid;
						}
					}
					if (albumid == -1) {
						mAlbumObj.createAlbum(ServiceManager
								.getUserName(), ALBUMNAME);
					} else {
						ArrayList<String> picPath = new ArrayList<String>();
						picPath.add(mAvatarPath);
						ArrayList<String> picNames = new ArrayList<String>();
						picNames.add(mAvatarPath.substring(mAvatarPath
								.lastIndexOf("/") + 1));
						uploadTask = mAlbumObj.uploadPicFiles(ServiceManager.getUserName(),picPath, picNames, albumid);
						showProgress(null, getString(R.string.photo_uploading));
					}
					break;
				case MessageTypes.MESSAGE_UPLOADPIC:
					uploadTask = null;
					dismissProgress();
					// 上传头像文件成功，开始执行插入数据库操作
					String filepath = mAvatarPath.substring(mAvatarPath.lastIndexOf("/") + 1);
					int ret = ServerInterface.uploadAlbum(String.valueOf(ServiceManager.getUserId()), filepath, ALBUMNAME);
					if (ret == ServerInterface.SUCCESS)
					{
						uploadnewmsg.what = UPLOAD_ALBUM_OK;
						handler.sendMessage(uploadnewmsg);
					}
					else if (ret == ALBUM_COOKIES_ERROR)
					{
						ServiceManager.setLogin(false);
						Toast.makeText(AlbumScreen.this, R.string.cookies_error, Toast.LENGTH_SHORT).show();
						Intent intent = new Intent(AlbumScreen.this,LoginScreen.class);
						startActivity(intent);
					}
					else
					{
						Toast.makeText(AlbumScreen.this, filepath + " " + getResources().getString(R.string.photo_upload_failed), Toast.LENGTH_SHORT).show();
					}
				default:
					break;
				}
			} catch (Exception e) {
				// TODO: handle exception
				ExceptionService.logException(e);
			}
		}

	};
    
    OnItemClickListener mItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			mPhotoGallery.setSelection(arg2
					+ (int) (mPageIndex * APP_PAGE_SIZE));
			//System.out.println(arg2 + " " + (int) (mPageIndex * APP_PAGE_SIZE) + " APP_PAGE_SIZE" +APP_PAGE_SIZE);
			mPhotoGalleryLayout.setVisibility(View.VISIBLE);
			mPhotoGridLayout.setVisibility(View.GONE);
		}
	};
    
	private Runnable titleGoneRunnable = new Runnable()
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			GalleryStatushandler.sendEmptyMessage(TITLE_GONE);
		}
		
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.album_screen);
		
		mContext = AlbumScreen.this;
		
//		String user_name = ServiceManager.getUserName();
//		AmtApplication.setAmtUserName(user_name);
		mAlbumObj = new AmtAlbumObj();
		mAlbumObj.setHandler(mHandler);
		
		GalleryStatushandler = new Handler()
		{
			public void handleMessage(Message msg) 
			{
				super.handleMessage(msg);
				switch(msg.what)
				{
				case TITLE_GONE_MSG:
					GalleryStatushandler.postDelayed(titleGoneRunnable, TITLE_GONE_TIME);
					break;
				case TITLE_GONE_CANCEL:
					GalleryStatushandler.removeCallbacks(titleGoneRunnable);
					break;
				case TITLE_GONE:
					setTitleMiss();
					break;
				default:
					break;
				}
			};
		};
		
		mTitleLayout = (RelativeLayout) findViewById(R.id.p_gallery_relativeLayout1);
		
		mPhotoGallery = (PhotoGallery) findViewById(R.id.p_gallery_gallery);
		mPhotoGalleryLayout = (View) findViewById(R.id.p_gallery_layout);
		
		mPhotoView = (AlbumScrollLayout) findViewById(R.id.ScrollLayoutTest);
		mPhotoGridLayout = findViewById(R.id.p_grid_layout);
		
		mBtnGalleryBack = (Button) findViewById(R.id.p_gallery_back);
		mBtnGridBack = (Button) findViewById(R.id.p_grid_back);
		mBtnGallerySetAvatar = (ImageButton) findViewById(R.id.p_gallery_set_avatar);
		mBtnGridInsertImage = (Button) findViewById(R.id.bt_insert_image);
		
		NoPhotoPrompt = (TextView) findViewById(R.id.NoPhotoPrompt);
		
		if (android.os.Build.VERSION.SDK_INT > 8) {
			Typeface type = Typeface.createFromAsset(getAssets(), "xdxwzt.ttf");
			NoPhotoPrompt.setTypeface(type);
		}
		
		mBtnGalleryBack.setOnClickListener(this);
		mBtnGridBack.setOnClickListener(this);
		mBtnGallerySetAvatar.setOnClickListener(this);
		mBtnGridInsertImage.setOnClickListener(this);
		
		mGalleryTitle = (TextView) findViewById(R.id.p_gallery_activityTitle);
		
		mPhotoView.setOnScreenChangeListener(new OnScreenChangeListener(){

			@Override
			public void onScreenChange(int currentIndex) {
				// TODO Auto-generated method stub
				//System.out.println("onScreenChange " + currentIndex);
				mPageIndex = currentIndex;
			}});

		mPhotoGallery.setAdapter(mGalleryPhotoAdapter);
		mPhotoGallery.setCallbackDuringFling(false);
		mPhotoGallery.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch(event.getAction())
				{
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
					setTitleAppear();
					GalleryStatushandler.sendEmptyMessage(TITLE_GONE_CANCEL);
					break;
				case MotionEvent.ACTION_UP:
					GalleryStatushandler.sendEmptyMessage(TITLE_GONE_MSG);
					break;
				default:
					break;
				}
				
				return false;
			}
		});
		mPhotoGallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				if (arg1 == null) {
					System.out.println("mPhotoGallery sel view is null!");
					return;
				}
				
				mSelItem = (ViewHolder) arg1.getTag();
				Integer cur_num = (Integer) (arg2 + 1);
				Integer sum = (Integer) arg0.getCount();
				String title = String.format(
						getString(R.string.album_gallery_title), cur_num, sum);
				mGalleryTitle.setText(title);
				setTitleAppear();
				GalleryStatushandler.sendEmptyMessage(TITLE_GONE_CANCEL);
				GalleryStatushandler.sendEmptyMessage(TITLE_GONE_MSG);

				AlbumScreen.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (mLastSelItem != null) {
							mLastSelItem.image.setImageBitmap(null);
						}
						
						if (mCacheImage != null) {
							mCacheImage.recycle();
						}

						
						File file = new File(mSelItem.finalfilepath);
						if (file.exists()) {
							mCacheImage = BitmapFactory
									.decodeFile(mSelItem.finalfilepath);
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
		
		serverInterface = new ServerInterface();
		
		if (savedInstanceState != null)
		{
			mCacheImageFilePath = savedInstanceState.getString("mCacheImageFilePath");
			ablumPhotoList = (List<Map>) savedInstanceState.get("ablumPhotoList");
			activityFirstStart = false;
			mPageIndex = savedInstanceState.getInt("mPageIndex");
			mGalleryPhotoAdapter = new PhotoAdapter(mContext, Gallery.class, ablumPhotoList, 0);
			setAblumLayout(ablumPhotoList);
			
			if (ablumPhotoList.size() <= 0)
			{
				NoPhotoPrompt.setVisibility(View.VISIBLE);
				mPhotoView.setVisibility(View.INVISIBLE);
			}
			
		}
		else
		{
			mPageIndex = 0;
		}
		
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
					File file = new File(filepath);
					if (file.exists()) {
				        long dateTaken = System.currentTimeMillis();
				        String title = mImgCapture.createName(dateTaken);
				        mCacheImageFilePath = mImgCapture.IMAGE_CACHE_PATH 
								+ "/"+ title +".jpg";
				        boolean ret = mImgCapture.CompressionImage(file.getAbsolutePath(), mCacheImageFilePath,false);
				        if (ret)
				        {
				        	File cachefile = new File(mCacheImageFilePath);
					        if (cachefile.exists()) {
								filepath = cachefile.getAbsolutePath();
								String name = filepath.substring(filepath.lastIndexOf("/") + 1, filepath.length());
								String expandname = filepath.substring(filepath.lastIndexOf(".") + 1, filepath.length()).toLowerCase();
								name = name.substring(0, name.lastIndexOf("."));
								uploadImage(name, expandname, filepath, 1);
					        } else {
					        	System.out.println("ALBUM create_cache_file_failed ");
					        	Toast.makeText(AlbumScreen.this, getString(R.string.image_create_cache_file_failed_io), Toast.LENGTH_SHORT).show();
					        }
				        }
				        else
				        {
				        	Toast.makeText(mContext, getString(R.string.select_file_not_image), Toast.LENGTH_SHORT).show();
				        }
					}
				}
			}
			break;

		case CAMERA_RESULT:
			if (resultCode == RESULT_OK) {
				String filepath = mCacheImageFilePath;
				File file = new File(filepath);
				if (file.exists()) {
					mImgCapture.CompressionImage(mCacheImageFilePath, mCacheImageFilePath,true);	/*生成新文件了，原本件被删掉*/
					if (file.exists())
					{
						filepath = file.getAbsolutePath();
						String name = filepath.substring(filepath.lastIndexOf("/") + 1, filepath.length());
						String expandname = filepath.substring(filepath.lastIndexOf(".") + 1, filepath.length());
						name = name.substring(0, name.lastIndexOf("."));
						uploadImage(name, expandname, filepath, 1);
					}
				}
			} else {
				if (resultCode != RESULT_CANCELED) {
				System.out.println("CAMERA create_cache_file_failed ");
					Toast
							.makeText(
									AlbumScreen.this,
									getString(R.string.image_create_cache_file_failed_io),
									Toast.LENGTH_SHORT).show();
				}
			}
			break;
			
		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putString("mCacheImageFilePath", mCacheImageFilePath);
		outState.putParcelableArrayList("ablumPhotoList", (ArrayList<? extends Parcelable>) ablumPhotoList);
		outState.putInt("mPageIndex", mPageIndex);
		
		super.onSaveInstanceState(outState);
	}
	
	private String getFilepathFromUri(Uri uri) {
		Cursor cursor = mContentResolver.query(uri, null,   
                null, null, null); 
		String filepath = "";
		try {
			cursor.moveToFirst();   
			filepath = cursor.getString(1);
			cursor.close();
		} catch (Exception e) {
			// TODO: handle exception
			ExceptionService.logException(e);
		}
		
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
        mCacheImageFilePath = mImgCapture.IMAGE_CACHE_PATH 
				+ "/"+ title +".jpg";
		File imageFile = new File(mCacheImageFilePath);
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

	// private boolean insertNewImageToDB(String name, String filepath) {
	// ContentValues cValue = new ContentValues();
	//
	// cValue.put(DatabaseHelper.COLUMN_PHOTO_NAME, name);
	//
	// cValue.put(DatabaseHelper.COLUMN_PHOTO_FILEPATH, filepath);
	//	
	// return ServiceManager.getDbManager().insertLocalPhoto(cValue);
	// }

	private void uploadImage(String name, String expandname, String filepath, int uploadcount) {
		Message msg = new Message();
		msg.getData().putString("name", name);
		msg.getData().putString("expandname", expandname);
		msg.getData().putString("filelocalpath", filepath);
		msg.getData().putInt("uploadcount", uploadcount);
		msg.what = UPLOAD_ALBUM;
		
		handler.sendMessage(msg);
	}


	// 更新后台数据
	class MyThread implements Runnable {
		public void run() {
			String msglist = "1";
			Message msg = new Message();
			Bundle b = new Bundle();// 存放数据
			b.putString("rmsg", msglist);
			msg.setData(b);
			AlbumScreen.this.myHandler.sendMessage(msg); // 向Handler发送消息,更新UI

		}
	}

	class MyHandler extends Handler {
		private AlbumScreen mContext;
		public MyHandler(Context context,int a) {
			mContext = (AlbumScreen)context;
		}

		public MyHandler(Looper L) {
			super(L);
		}

		// 子类必须重写此方法,接受数据
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			Bundle b = msg.getData();
			String rmsg = b.getString("rmsg");

			if ("1".equals(rmsg)) {
				if (mAlbumUrllist == null || "".equals(mAlbumUrllist)) {
					ParsePhotoJson();
				} else {
					String[] items;
					if (mAlbumUrllist != null && !"".equals(mAlbumUrllist)) {
						items = mAlbumUrllist.split(",");

						for (int i = 0; i < items.length; i++) {
							Map map = new HashMap();
							map.put("title", "");
							map.put("filepath", items[i]);
							map.put("isweb", 1);
							ablumPhotoList.add(map);
						}
					}
					setAblumLayout(ablumPhotoList);

					//dataLoad.bindScrollViewGroup(mPhotoView);
			     }
			     
				}
			}

		}
	
	
	
	public void ParsePhotoJson() {
		System.out.println("ParsePhotoJson");

		new Thread (new Runnable(){
			@Override
			public void run() {
				String user_id = String.valueOf(ServiceManager.getUserId());
				String username = ServiceManager.getUserName();
				
				//System.out.println("=CCC=" + user_id + "=CCC=" + username);
				String albumname = username;
				Looper.prepare();
				
				String json = serverInterface.getAlbumDownloadUrl(user_id, albumname);
				NoteApplication.LogD(AlbumScreen.class, json);
				if (json == null ||  "".equals(json) || "-1".equals(json) || "-2".equals(json)){
					Message msg = new Message();
					msg.what = DOWNLOAD_THUMB_ALBUM_JSON_ERROR;
					handler.sendMessage(msg);
					return;
				}
				else if ("1".equals(json))
				{
					mAlbumUrllist = "";
					Message msg = new Message();
					msg.what = DOWNLOAD_THUMB_ALBUM_JSON_OK;
					handler.sendMessage(msg);
					return;
				}
				else if (json.equals(String.valueOf(ALBUM_COOKIES_ERROR)))
				{
					Message msg = new Message();
					msg.what = ALBUM_COOKIES_ERROR;
					handler.sendMessage(msg);
					return;
				}
				else if (json.equals(String.valueOf(ALBUM_NO_INTERNET)))
				{
					Message msg = new Message();
					msg.what = ALBUM_NO_INTERNET;
					handler.sendMessage(msg);
					return;
				}

				if (!json.matches("[0-9]+"))
				{
					mAlbumUrllist = json;
					Message msg = new Message();
					msg.what = DOWNLOAD_THUMB_ALBUM_JSON_OK;
					handler.sendMessage(msg);
				}
				
			}
        	
        }).start();
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
//		if(mPhotoView.getChildCount() == 0){
//			ParsePhotoJson();
//		}
    }
	
	
	
	public class UpDownloadHandler extends Handler {
		
		UpDownloadHandler(){}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UPLOAD_ALBUM: {
				String user_id = String.valueOf(ServiceManager
						.getUserId());
				String username = ServiceManager.getUserName();
				String albumname = username;
				String aName = msg.getData().getString("name");
				String aExpandName = msg.getData().getString("expandname");
				String aFilePath = msg.getData().getString("filelocalpath");
				int aUploadCount = msg.getData().getInt("uploadcount");

				mAvatarPath = aFilePath;
				if (!TextUtils.isEmpty(albumname))
				{
					mAlbumObj.requestAlbumidInfo(albumname);
				}
				else
				{
					Toast.makeText(mContext, "邮箱帐号为空", Toast.LENGTH_SHORT).show();
				}

				uploadnewmsg = new Message();
				uploadnewmsg.getData().putString("name", aName);
				uploadnewmsg.getData().putString("expandname", aExpandName);
				uploadnewmsg.getData().putString("filelocalpath", aFilePath);
				uploadnewmsg.getData().putInt("uploadcount", aUploadCount);
				
			}
				break;
				
			case  UPLOAD_ALBUM_OK:
				System.out.println("UPLOAD_ALBUM_OK");
				Map map = new HashMap();
				map.put("title", "");
				map.put("filepath", msg.getData().getString("filelocalpath"));
				map.put("isweb", 0);
				ablumPhotoList.add(map);
				NoPhotoPrompt.setVisibility(View.INVISIBLE);
				mPhotoView.setVisibility(View.VISIBLE);
				System.out.println("UPLOAD_ALBUM_OK" + msg.getData().getString("filelocalpath"));
				if (mLastChildAdapter == null || mLastChildAdapter.getCount() == (int)APP_PAGE_SIZE) {
					
					List<Map> list = new ArrayList<Map>();
					list.add(map);

					GridView appPage = new GridView(mContext);
					mLastChildAdapter = new PhotoAdapter(mContext, GridView.class, list, 0);
					appPage.setAdapter(mLastChildAdapter);
					appPage.setNumColumns(3);
					appPage.setGravity(Gravity.CENTER);
					appPage.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
					appPage.setVerticalSpacing(10);
					appPage.setHorizontalSpacing(10);
					appPage.setColumnWidth(90);
					appPage.setOnItemClickListener(mItemClickListener);
					appPage.setSelector(new ColorDrawable(Color.TRANSPARENT));
					mPhotoView.addView(appPage);
				} else {
					mLastChildAdapter.addNewItem(map);
					mLastChildAdapter.notifyDataSetChanged();
				}
				
				mGalleryPhotoAdapter.addNewItem(map);
				mGalleryPhotoAdapter.notifyDataSetChanged();
				break;
			case  UPLOAD_ALBUM_ERROR:
				int uploadcount = msg.getData().getInt("uploadcount");
//				if (uploadcount > 3 || uploadcount <= 0 || NoteApplication.networkIsOk == false)  {
					System.out.println("UPLOAD_ALBUM_ERROR");
					String aFilePath = msg.getData().getString("filelocalpath");
					if (aFilePath != null && !aFilePath.equals(""))
					{
						new File(aFilePath).delete();
						Toast.makeText(AlbumScreen.this, getString(R.string.image_upload_failed), Toast.LENGTH_SHORT).show();
//					}
				} /*else {
					System.out.println("UPLOAD_ALBUM_ERROR, try count : " + String.valueOf(uploadcount+1));
					String aName = msg.getData().getString("name");
					String aExpandName = msg.getData().getString("expandname");
					String aFilePath = msg.getData().getString("filelocalpath");
					uploadImage(aName, aExpandName, aFilePath, uploadcount+1);
				}*/
				break;		
			case  DOWNLOAD_THUMB_ALBUM_JSON_OK:
				if (mGalleryPhotoAdapter == null) {
					List<Map> list = new ArrayList<Map>();
					mGalleryPhotoAdapter = new PhotoAdapter(mContext, Gallery.class, list, 0);
					mPhotoGallery.setAdapter(mGalleryPhotoAdapter);
				}
				if (mAlbumUrllist != null && !"".equals(mAlbumUrllist)) {
					MyThread m = new MyThread();
					new Thread(m).start();
					// mDialogCheckSignature.dismiss();
				}
				else
				{
					NoPhotoPrompt.setVisibility(View.VISIBLE);
					mPhotoView.setVisibility(View.INVISIBLE);
				}

				break;
			case  DOWNLOAD_THUMB_ALBUM_JSON_ERROR:
				if (mGalleryPhotoAdapter == null) {
					List<Map> list = new ArrayList<Map>();
					mGalleryPhotoAdapter = new PhotoAdapter(mContext, Gallery.class, list, 0);
					mPhotoGallery.setAdapter(mGalleryPhotoAdapter);
				}
				System.out.println("DOWNLOAD_THUMB_ALBUM_JSON_ERROR");
				Toast.makeText(AlbumScreen.this, getString(R.string.image_download_failed), Toast.LENGTH_SHORT).show();
				break;
			case  DOWNLOAD_ALL_ALBUM_OK:
				System.out.println("DOWNLOAD_ALL_ALBUM_OK");
				break;
			case  DOWNLOAD_ALL_ALBUM_ERROR:
				System.out.println("DOWNLOAD_ALL_ALBUM_ERROR");
				break;
			case ALBUM_COOKIES_ERROR:
				ServiceManager.setLogin(false);
				Toast.makeText(AlbumScreen.this, R.string.cookies_error, Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(AlbumScreen.this,LoginScreen.class);
				startActivity(intent);
				finish();
				break;
			case ALBUM_NO_INTERNET:
				Toast.makeText(AlbumScreen.this, R.string.network_none, Toast.LENGTH_SHORT).show();
				break;
			}
    	}
	}
	
	
	
	
	//分页数据
	class DataLoading {
		private int count;
		private AlbumScrollLayout mScrollViewGroup;
		public void bindScrollViewGroup(AlbumScrollLayout scrollViewGroup) {
			this.count=scrollViewGroup.getChildCount();
			this.mScrollViewGroup=scrollViewGroup;
			scrollViewGroup.setOnScreenChangeListenerDataLoad(new OnScreenChangeListenerDataLoad() {
				public void onScreenChange(int currentIndex) {
					generatePageControl(currentIndex);
				}
			});
		}
		
		private void generatePageControl(int currentIndex){
			//如果到最后一页，就加载24条记录
			if(count==currentIndex){
				mAlbumUrllist = "";
				mScrollViewGroup.removeAllViews();
				MyThread m = new MyThread();
				new Thread(m).start();
			}
		}
	}
	
	private void loadAlbumData() {
		dataLoad = new DataLoading();
		myHandler = new MyHandler(this,1);
		handler = new UpDownloadHandler();
		//起一个线程更新数据
		if (activityFirstStart)
		{
			MyThread m = new MyThread();
			new Thread(m).start();
		}
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
		case R.id.bt_insert_image:
			showSelImageDialog();
			break;
		}
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // KeyEvent.KEYCODE_BACK代表返回操作.
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if (mPhotoGalleryLayout.getVisibility() == View.VISIBLE) {
    			mPhotoGridLayout.setVisibility(View.VISIBLE);
    			mPhotoGalleryLayout.setVisibility(View.GONE);
        	} else {
	            // 处理返回操作.
	        	this.finish();
        	}
        }
        return true;
    }
	
	private void setTitleMiss()
	{
		mTitleLayout.setBackgroundResource(0);
//		mTitleLayout.setBackgroundColor(android.R.color.black);
		mBtnGalleryBack.setVisibility(View.INVISIBLE);
		mGalleryTitle.setVisibility(View.INVISIBLE);
	}
	
	private void setTitleAppear()
	{
		mTitleLayout.setBackgroundResource(R.drawable.title_bar_background);
		mBtnGalleryBack.setVisibility(View.VISIBLE);
		mGalleryTitle.setVisibility(View.VISIBLE);
	}
	
	public void setAblumLayout(List<Map> list)
	{
		int pageNo = (int)Math.ceil( list.size()/APP_PAGE_SIZE);
		for (int i = 0; i < pageNo; i++) {
			GridView appPage = new GridView(mContext);
			// get the "i" page data
			mLastChildAdapter = new PhotoAdapter(mContext, GridView.class, list,i);
			appPage.setAdapter(mLastChildAdapter);
			appPage.setNumColumns(3);
			appPage.setGravity(Gravity.CENTER);
			appPage.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			appPage.setVerticalSpacing(12);
			appPage.setHorizontalSpacing(10);
			appPage.setColumnWidth(90);
			appPage.setOnItemClickListener(mItemClickListener);
			appPage.setSelector(new ColorDrawable(Color.TRANSPARENT));
			mPhotoView.addView(appPage);
		}
		
		mPhotoView.setToScreen(mPageIndex);
		
		mGalleryPhotoAdapter = new PhotoAdapter(mContext, Gallery.class, list, 0);
		mPhotoGallery.setAdapter(mGalleryPhotoAdapter);
	}
}


  

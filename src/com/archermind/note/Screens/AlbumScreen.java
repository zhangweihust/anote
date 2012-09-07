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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Adapter.PhotoAdapter;
import com.archermind.note.Adapter.PhotoAdapter.ViewHolder;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.ImageCapture;
import com.archermind.note.Utils.PreferencesHelper;
import com.archermind.note.Utils.ServerInterface;
import com.archermind.note.Views.AlbumScrollLayout;
import com.archermind.note.Views.AlbumScrollLayout.OnScreenChangeListener;
import com.archermind.note.Views.AlbumScrollLayout.OnScreenChangeListenerDataLoad;

public class AlbumScreen extends Screen implements OnClickListener {

	private Gallery mPhotoGallery;
	private AlbumScrollLayout mPhotoView;
	private View mPhotoGalleryLayout;
	private View mPhotoGridLayout;
	private RelativeLayout mTitleLayout;
	
	private Button mBtnGalleryBack;
	private Button mBtnGridBack;
	private ImageButton mBtnGallerySetAvatar;
	private ImageButton mBtnGridInsertImage;
	
	private TextView mGalleryTitle;
	
	private Bitmap mCacheImage;
	
	private ViewHolder mLastSelItem;
	private ViewHolder mSelItem;
	
	private Dialog mPicChooseDialog;
	
	private static final float APP_PAGE_SIZE = 12.0f;
	
	private static final int ALBUM_RESULT = 1;
	private static final int CAMERA_RESULT = 2;
	private static final int CROP_RESULT = 3;
	
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
	
	private PhotoAdapter mLastChildAdapter;
	private PhotoAdapter mGalleryPhotoAdapter;
	
	private String mAlbumUrllist;
    
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
					mTitleLayout.setVisibility(View.GONE);
					break;
				default:
					break;
				}
			};
		};
		
		mTitleLayout = (RelativeLayout) findViewById(R.id.p_gallery_relativeLayout1);
		
		mPhotoGallery = (Gallery) findViewById(R.id.p_gallery_gallery);
		mPhotoGalleryLayout = (View) findViewById(R.id.p_gallery_layout);
		
		mPhotoView = (AlbumScrollLayout) findViewById(R.id.ScrollLayoutTest);
		mPhotoGridLayout = findViewById(R.id.p_grid_layout);
		
		mBtnGalleryBack = (Button) findViewById(R.id.p_gallery_back);
		mBtnGridBack = (Button) findViewById(R.id.p_grid_back);
		mBtnGallerySetAvatar = (ImageButton) findViewById(R.id.p_gallery_set_avatar);
		mBtnGridInsertImage = (ImageButton) findViewById(R.id.p_grid_insert_image);
		
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

		mPhotoGallery.setCallbackDuringFling(false);
		mPhotoGallery.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					mTitleLayout.setVisibility(View.VISIBLE);
					GalleryStatushandler.sendEmptyMessage(TITLE_GONE_CANCEL);
				}
				else if (event.getAction() == MotionEvent.ACTION_UP)
				{
//					mTitleLayout.setVisibility(View.GONE);
					GalleryStatushandler.sendEmptyMessage(TITLE_GONE_MSG);
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
						GalleryStatushandler.sendEmptyMessage(TITLE_GONE);
					}
				});
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}});
		
		mPageIndex = 0;
		
		mContentResolver = getContentResolver();
		
		mImgCapture = new ImageCapture(this, mContentResolver);
		
		serverInterface = new ServerInterface();
//		serverInterface.InitAmtCloud(mContext);
		
		mAlbumUrllist = Environment
		.getExternalStorageDirectory().toString() + "/anote/image_cache/0_20120907_114625.jpg";
		
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
				        mImgCapture.copyFile(file.getAbsolutePath(), mCacheImageFilePath);
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
				}
			}
			break;

		case CAMERA_RESULT:
			if (resultCode == RESULT_OK) {
				String filepath = mCacheImageFilePath;
				File file = new File(filepath);
				if (file.exists()) {
					filepath = file.getAbsolutePath();
					String name = filepath.substring(filepath.lastIndexOf("/") + 1, filepath.length());
					String expandname = filepath.substring(filepath.lastIndexOf(".") + 1, filepath.length());
					name = name.substring(0, name.lastIndexOf("."));
					uploadImage(name, expandname, filepath, 1);
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
	
	private String getFilepathFromUri(Uri uri) {
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
					List<Map> list = new ArrayList<Map>();
					String[] items;
					if (mAlbumUrllist != null && !"".equals(mAlbumUrllist)) {
						items = mAlbumUrllist.split(",");

						for (int i = 0; i < items.length; i++) {
							Map map = new HashMap();
							map.put("title", "");
							map.put("filepath", items[i]);
							map.put("isweb", 1);
							list.add(map);
						}
					}

			        int pageNo = (int)Math.ceil( list.size()/APP_PAGE_SIZE);
					for (int i = 0; i < pageNo; i++) {
						GridView appPage = new GridView(mContext);
						// get the "i" page data
						mLastChildAdapter = new PhotoAdapter(mContext, GridView.class, list, i);
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
					
					mGalleryPhotoAdapter = new PhotoAdapter(mContext, Gallery.class, list, 0);
					mPhotoGallery.setAdapter(mGalleryPhotoAdapter);

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
				String user_id = String.valueOf(NoteApplication.getInstance().getUserId());
				String username = NoteApplication.getInstance().getUserName();
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
				
				if ("1".equals(json)){
					mAlbumUrllist = "";
					Message msg = new Message();
					msg.what = DOWNLOAD_THUMB_ALBUM_JSON_OK;
					handler.sendMessage(msg);
					return;
				}

//				try {
					mAlbumUrllist = json;
//					JSONArray jsonArray = new JSONArray(json);
//
//					if (jsonArray.length() > 0) {
//						JSONObject jsonObject = (JSONObject) jsonArray.opt(0);
//						mAlbumUrllist = jsonObject.getString("album_url");
//					}
//
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					Message msg = new Message();
//					msg.what = DOWNLOAD_THUMB_ALBUM_JSON_ERROR;
//					handler.sendMessage(msg);
//					e.printStackTrace();
//					return;
//				}
					
				Message msg = new Message();
				msg.what = DOWNLOAD_THUMB_ALBUM_JSON_OK;
				handler.sendMessage(msg);
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
//				String user_id = String.valueOf(NoteApplication.getInstance()
//						.getUserId());
//				String username = NoteApplication.getInstance().getUserName();
//				String albumname = username;
//				String aName = msg.getData().getString("name");
//				String aExpandName = msg.getData().getString("expandname");
//				String aFilePath = msg.getData().getString("filelocalpath");
//				int aUploadCount = msg.getData().getInt("uploadcount");
//
//				int result = serverInterface.uploadAlbum(mContext, user_id,
//						albumname, username, aFilePath, aName, aExpandName);
//
//				Message newmsg = new Message();
//				newmsg.getData().putString("name", aName);
//				newmsg.getData().putString("expandname", aExpandName);
//				newmsg.getData().putString("filelocalpath", aFilePath);
//				newmsg.getData().putInt("uploadcount", aUploadCount);
//
//				if (result == 0) {
//					newmsg.what = UPLOAD_ALBUM_OK;
//				} else {
//					newmsg.what = UPLOAD_ALBUM_ERROR;
//				}
//				handler.sendMessage(newmsg);
			}
				break;
				
			case  UPLOAD_ALBUM_OK:
				System.out.println("UPLOAD_ALBUM_OK");
				Map map = new HashMap();
				map.put("title", "");
				map.put("filepath", msg.getData().getString("filelocalpath"));
				map.put("isweb", 0);
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
				if (uploadcount > 3 || uploadcount <= 0 || NoteApplication.networkIsOk == false)  {
					System.out.println("UPLOAD_ALBUM_ERROR");
					String aFilePath = msg.getData().getString("filelocalpath");
					new File(aFilePath).delete();
					Toast.makeText(AlbumScreen.this, getString(R.string.image_upload_failed), Toast.LENGTH_SHORT).show();
				} else {
					System.out.println("UPLOAD_ALBUM_ERROR, try count : " + String.valueOf(uploadcount+1));
					String aName = msg.getData().getString("name");
					String aExpandName = msg.getData().getString("expandname");
					String aFilePath = msg.getData().getString("filelocalpath");
					uploadImage(aName, aExpandName, aFilePath, uploadcount+1);
				}
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
		MyThread m = new MyThread();
		new Thread(m).start();
		
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
}


  

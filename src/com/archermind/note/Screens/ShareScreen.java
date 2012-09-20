package com.archermind.note.Screens;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONObject;

import com.amtcloud.mobile.android.business.AmtAlbumObj;
import com.amtcloud.mobile.android.business.AmtApplication;
import com.amtcloud.mobile.android.business.AmtAlbumObj.AlbumItem;
import com.amtcloud.mobile.android.business.MessageTypes;
import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Events.EventTypes;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Services.ExceptionService;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.AlbumInfoUtil;
import com.archermind.note.Utils.NetworkUtils;
import com.archermind.note.Utils.PreferencesHelper;
import com.archermind.note.Utils.ServerInterface;
import com.renren.api.connect.android.AsyncRenren;
import com.renren.api.connect.android.Renren;
import com.renren.api.connect.android.common.AbstractRequestListener;
import com.renren.api.connect.android.exception.RenrenError;
import com.renren.api.connect.android.feed.FeedPublishRequestParam;
import com.renren.api.connect.android.feed.FeedPublishResponseBean;
import com.tencent.weibo.api.TAPI;
import com.tencent.weibo.constants.OAuthConstants;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.oauthv2.OAuthV2Client;
import com.weibo.net.AsyncWeiboRunner;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;

import android.R.integer;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ShareScreen extends Screen implements OnClickListener {

	private SharedPreferences mPreferences;
	private Button mBackButton;
	private ImageView mImageView;
	private TextView mTextView;
//	private ProgressBar mProgressBar;
	private Button mReuploadButton;
	private LinearLayout mOthersLayout;
	private Button mSinaButton;
	private Button mQQButton;
	private Button mRenrenButton;
	private ArrayList<String> mPicPathList;
	private ArrayList<String> mPicnameList;
	private AmtAlbumObj mAlbumObj;
	private String imgurl_renren;// 用于分享到人人的图片url
	private static final String TAG = "ShareScreen";
	private static final String ALBUMNAME_SHARE = "share";
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// 处理图片上传过程发送的消息
			Log.i(TAG, "handler_message:" + msg.what);
			switch (msg.what) {
			case MessageTypes.ERROR_MESSAGE:
				dismssProgressBar(R.string.share_failed, false);
				mReuploadButton.setVisibility(View.VISIBLE);
				break;
			case MessageTypes.MESSAGE_CREATEALBUM:
				mAlbumObj.requestAlbumidInfo(ServiceManager.getUserName());
				break;
			case MessageTypes.MESSAGE_GETALBUM:
				AlbumItem[] albumItems = AlbumInfoUtil.getAlbumInfos(mAlbumObj,
						msg.obj);
				if (albumItems == null) {
					mAlbumObj.createAlbum(ServiceManager.getUserName(),
							ALBUMNAME_SHARE);
					break;
				}
				int albumid = -1;
				for (int i = 0; i < albumItems.length; i++) {
					if (albumItems[i].albumname.equals(ALBUMNAME_SHARE)) {
						albumid = albumItems[i].albumid;
					}
				}
				if (albumid == -1) {
					mAlbumObj.createAlbum(ServiceManager.getUserName(),
							ALBUMNAME_SHARE);
				} else {
					if (mPicPathList == null) {
						dismssProgressBar(R.string.share_failed, false);
						mReuploadButton.setVisibility(View.VISIBLE);
						Log.e(TAG, "mPicPathList is null");
						return;
					}
					mAlbumObj.uploadPicFiles(mPicPathList, mPicnameList,
							albumid);
					Log.i(TAG, "albumid：" + albumid);
				}
				break;
			case MessageTypes.MESSAGE_UPLOADPIC:
				// 上传笔记图片成功后，异步执行分享到广场
				SquareTask squareTask = new SquareTask();
				Intent intent = getIntent();
				squareTask.execute(intent.getStringExtra("noteid"),
						intent.getStringExtra("title"),
						intent.getStringExtra("action"),
						intent.getStringExtra("sid"));
				break;
			default:
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share);
		mPreferences = PreferencesHelper.getSharedPreferences(this, 0);
		mPicPathList = getIntent().getStringArrayListExtra("picpathlist");
		if (mPicPathList != null) {
			mPicnameList = new ArrayList<String>();
			for (String s : mPicPathList) {
				Log.i(TAG, "上传文件路径：" + s);
				mPicnameList.add(s.substring(s.lastIndexOf("/") + 1));
			}
		}

		mBackButton = (Button) findViewById(R.id.screen_top_play_control_back);
		mBackButton.setOnClickListener(this);

		mImageView = (ImageView) findViewById(R.id.share_image);
		mImageView.setMaxWidth(getWindowManager().getDefaultDisplay()
				.getWidth() / 2);
		try {
			mImageView.setImageBitmap(BitmapFactory.decodeFile(mPicPathList
					.get(0)));
		} catch (Exception e) {
			Log.e(TAG, "未找到该笔记图片");
			ExceptionService.logException(e);
		}

//		mProgressBar = (ProgressBar) findViewById(R.id.share_progressBar);
		mReuploadButton = (Button) findViewById(R.id.share_btn_reupload);
		mReuploadButton.setOnClickListener(this);
		mTextView = (TextView) findViewById(R.id.share_text);
		if (android.os.Build.VERSION.SDK_INT > 8) {
			Typeface type = Typeface.createFromAsset(getAssets(), "xdxwzt.ttf");
			mTextView.setTypeface(type);
		}

		mOthersLayout = (LinearLayout) findViewById(R.id.share_layout_others);

		mSinaButton = (Button) findViewById(R.id.btn_share_sina);
		mSinaButton.setOnClickListener(this);

		mQQButton = (Button) findViewById(R.id.btn_share_qq);
		mQQButton.setOnClickListener(this);

		mRenrenButton = (Button) findViewById(R.id.btn_share_renren);
		mRenrenButton.setOnClickListener(this);

		uploadNotePic();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!ServiceManager.getmSina_nickname().equals("")) {
			mSinaButton.setBackgroundResource(R.drawable.btn_sina_selector);
		} else {
			mSinaButton.setBackgroundResource(R.drawable.sina_gray);
		}

		if (!ServiceManager.getmQQ_nickname().equals("")) {
			mQQButton.setBackgroundResource(R.drawable.btn_qq_selector);
		} else {
			mQQButton.setBackgroundResource(R.drawable.qq_gray);
		}

		if (!ServiceManager.getmRenren_nickname().equals("")) {
			mRenrenButton.setBackgroundResource(R.drawable.btn_renren_selector);
		} else {
			mRenrenButton.setBackgroundResource(R.drawable.renren_gray);
		}
	}

	/*
	 * 用于分享到广场的异步类
	 */
	class SquareTask extends AsyncTask<String, integer, String> {

		@Override
		protected String doInBackground(String... params) {
			if (mPicPathList.size() < 2) {
				return "failed"; // 至少应该包含一个图片路径和压缩包路径，所以size >=2
			}
			String fristPicUrl = ServiceManager.getUserName() + "&filename="
					+ mPicnameList.get(0) + "&album=" + ALBUMNAME_SHARE;
			imgurl_renren = ServerInterface.IMG_DOWADING_HEAD + fristPicUrl;
			Log.i(TAG, "分享的笔记第一张图片url： " + fristPicUrl);

			String contentUrl = ServiceManager.getUserName() + "&filename="
					+ mPicnameList.get(mPicnameList.size() - 1) + "&album="
					+ ALBUMNAME_SHARE;
			Log.i(TAG, "分享的笔记压缩包url： " + contentUrl);

			int serviceId = ServerInterface.uploadNote(
					Long.parseLong(params[0]),
					String.valueOf(ServiceManager.getUserId()), params[3],
					params[2], params[1], fristPicUrl + "," + contentUrl + ",",
					String.valueOf(mPicnameList.size() - 1));
			if (serviceId == ServerInterface.COOKIES_ERROR) {
				return "cookies_error";
			} else {
				if ("A".equals(params[2])) {
					if (serviceId > 0) {
						ContentValues contentValues2 = new ContentValues();
						contentValues2.put(
								DatabaseHelper.COLUMN_NOTE_SERVICE_ID,
								String.valueOf(serviceId));
						contentValues2.put(DatabaseHelper.COLUMN_NOTE_USER_ID,
								ServiceManager.getUserId());
						ServiceManager.getDbManager().updateLocalNotes(
								contentValues2, Integer.parseInt(params[0]));
						return "success";
					} else {
						return "failed";
					}
				} else if ("M".equals(params[2])) {
					if (serviceId == 0) {
						return "success";
					} else {
						return "failed";
					}
				}
				return null;
			}

		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result.equals("success")) {
				ServiceManager.getEventservice().onUpdateEvent(
						new EventArgs(EventTypes.SHARE_NOTE_SUCCESSED));
				Cursor cursor = ServiceManager.getDbManager()
						.queryLocalNotesById(
								Integer.parseInt(getIntent().getStringExtra(
										"noteid")));
				if (cursor != null) {
					cursor.moveToFirst();
					String sid = cursor
							.getString((cursor
									.getColumnIndex(DatabaseHelper.COLUMN_NOTE_SERVICE_ID)));
					cursor.close();
					mTextView.append("?id=" + ServiceManager.getUserId()
							+ "&nid=" + sid);
				}
				// 随机动画
				mOthersLayout.setVisibility(View.VISIBLE);
				Random random = new Random();
				int animationid = random.nextInt(3);
				switch (animationid) {
				case 0:
					mOthersLayout.startAnimation(AnimationUtils.loadAnimation(
							ShareScreen.this, R.anim.up));
					break;
				case 1:
					mOthersLayout.startAnimation(AnimationUtils.loadAnimation(
							ShareScreen.this, R.anim.alpha));
					break;
				case 2:
					mOthersLayout.startAnimation(AnimationUtils.loadAnimation(
							ShareScreen.this, R.anim.scale));
					break;
				default:
					break;
				}

				dismssProgressBar(R.string.share_success, true);
				Log.i(TAG, "分享到广场成功!");
			} else if (result.equals("failed")) {
				dismssProgressBar(R.string.share_failed, false);
				mReuploadButton.setVisibility(View.VISIBLE);
				Log.i(TAG, "分享到广场失败!");
			} else if (result.equals("cookies_error")) {
				dismssProgressBar(R.string.cookies_error, false);
				mReuploadButton.setVisibility(View.VISIBLE);
				ServiceManager.setLogin(false);
				Intent intent = new Intent(ShareScreen.this, LoginScreen.class);
				startActivity(intent);
			}
		}

	}

	/*
	 * 上传笔记图片至服务器
	 */
	private void uploadNotePic() {
		showProgressBar(R.string.share_msg_square);
		if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
			AmtApplication.setAmtUserName(ServiceManager.getUserName());
			mAlbumObj = new AmtAlbumObj();
			mAlbumObj.setHandler(mHandler);
			mAlbumObj.requestAlbumidInfo(ServiceManager.getUserName());
		} else {
			dismssProgressBar(R.string.network_none, false);
			mReuploadButton.setVisibility(View.VISIBLE);
		}
	}

	private void shareToSina(String imgPath, String msg) {
		if (!ServiceManager.getmSina_nickname().equals("")) {
			String token = mPreferences.getString(
					PreferencesHelper.XML_SINA_ACCESS_TOKEN, null);
			if (token != null) {
				Weibo weibo = Weibo.getInstance();
				WeiboParameters bundle = new WeiboParameters();
				bundle.add("access_token", token);
				bundle.add("pic", imgPath);// 图片路径
				bundle.add("status", msg);
				String url = Weibo.SERVER + "statuses/upload.json";
				AsyncWeiboRunner weiboRunner = new AsyncWeiboRunner(weibo);
				showProgressBar(R.string.share_msg_sina);// 显示进度
				weiboRunner.request(this, url, bundle, Utility.HTTPMETHOD_POST,
						new AsyncWeiboRunner.RequestListener() {

							@Override
							public void onIOException(IOException e) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										dismssProgressBar(
												R.string.share_failed, false);
									}
								});
								e.printStackTrace();
							}

							@Override
							public void onError(WeiboException e) {
								final int statuscode = e.getStatusCode();
								if (e.getStatusCode() == 21332
										|| e.getStatusCode() == 21327) { // accessToken过期错误码
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
//											mProgressBar
//													.setVisibility(View.GONE);
											dismissProgress();
											showLoginAlertDialog(R.string.share_sina_expired);
										}
									});
								} else if (e.getStatusCode() == 20019) {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											dismssProgressBar(
													R.string.share_err_equal_weibo,
													false);
										}
									});

								} else {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											dismssProgressBar(
													R.string.share_failed,
													false);
											Toast.makeText(
													ShareScreen.this,
													"Sina Weibo错误码："
															+ statuscode,
													Toast.LENGTH_SHORT).show();
										}
									});

								}
								Log.e(TAG, "Sina Weibo错误码：" + e.getStatusCode());
							}

							@Override
							public void onComplete(String response) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										dismssProgressBar(
												R.string.share_success, true);
									}
								});
							}
						});
			} else {
				showLoginAlertDialog(R.string.share_sina_expired);
			}
		} else {
			showBoundAlertDialog(R.string.account_bound_sina_none);
		}
	}

	// QQ分享异步封装
	class QQTask extends AsyncTask<String, integer, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgressBar(R.string.share_msg_qq);
		}

		@Override
		protected String doInBackground(String... params) {
			// 关闭OAuthV2Client中的默认开启的QHttpClient。
			OAuthV2Client.getQHttpClient().shutdownConnection();
			OAuthV2 oAuthV2 = new OAuthV2("http://www.archermind.com");
			oAuthV2.setClientId(AccountScreen.APPKEY_QQ);
			oAuthV2.setAccessToken(params[0]);
			oAuthV2.setOpenid(params[1]);
			TAPI tapi = new TAPI(OAuthConstants.OAUTH_VERSION_2_A);
			String response = null;
			try {
				response = tapi.addPic(oAuthV2, "json", params[2], "127.0.0.1",
						params[3]);
				Log.i(TAG, response);
				return response;
			} catch (Exception e) {
				e.printStackTrace();
				return response;
			} finally {
				tapi.shutdownConnection();
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try {
				JSONObject jsonObject = new JSONObject(result);
				if (jsonObject.optInt("errcode") == 0) {
					dismssProgressBar(R.string.share_success, true);
				} else if (jsonObject.optInt("errcode") == 67) {
					dismssProgressBar(R.string.share_failed, false);
					Toast.makeText(ShareScreen.this,
							R.string.share_err_equal_weibo, Toast.LENGTH_SHORT)
							.show();
				} else if (jsonObject.optInt("errcode") == 37) { // accessToken过期错误码
//					mProgressBar.setVisibility(View.GONE);
					dismissProgress();
					showLoginAlertDialog(R.string.share_qq_expired);
				} else {
					dismssProgressBar(R.string.share_failed, false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				dismssProgressBar(R.string.share_failed, false);
			}

		}

	};

	private void shareToQQ(String imgPath, String msg) {
		if (!ServiceManager.getmQQ_nickname().equals("")) {
			String token = mPreferences.getString(
					PreferencesHelper.XML_QQ_ACCESS_TOKEN, null);
			String openid = mPreferences.getString(
					PreferencesHelper.XML_QQ_OPENID, null);
			if (token != null && openid != null) {
				QQTask qqTask = new QQTask();
				qqTask.execute(token, openid, msg, imgPath);
			} else {
				showLoginAlertDialog(R.string.share_qq_expired);
			}
		} else {
			showBoundAlertDialog(R.string.account_bound_qq_none);
		}
	}

	private void shareToRenren(String imgPath, String msg) {
		if (!ServiceManager.getmRenren_nickname().equals("")) {
			String token = mPreferences.getString(
					PreferencesHelper.XML_RENREN_ACCESS_TOKEN, null);
			if (token != null) {
				Renren renren = new Renren(AccountScreen.APPKEY_RENREN,
						AccountScreen.APPSECRET_RENREN,
						AccountScreen.APPID_RENREN, this);
				renren.updateAccessToken(token);
				AsyncRenren asyncRenren = new AsyncRenren(renren);
				FeedPublishRequestParam param = new FeedPublishRequestParam(
						getString(R.string.app_name), msg,
						ServerInterface.URL_SERVER, imgPath, null, null, null,
						null);
				AbstractRequestListener<FeedPublishResponseBean> listener = new AbstractRequestListener<FeedPublishResponseBean>() {

					@Override
					public void onRenrenError(RenrenError rre) {
						if (rre.getErrorCode() == 2002) { // accessToken过期错误码
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
//									mProgressBar.setVisibility(View.GONE);
									dismissProgress();
									showLoginAlertDialog(R.string.share_renren_expired);
								}
							});
						} else {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									dismssProgressBar(R.string.share_failed,
											false);
								}
							});
						}
					}

					@Override
					public void onFault(Throwable t) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								dismssProgressBar(R.string.share_failed, false);
							}
						});

					}

					@Override
					public void onComplete(FeedPublishResponseBean bean) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								dismssProgressBar(R.string.share_success, true);
							}
						});
					}
				};
				showProgressBar(R.string.share_msg_renren);
				asyncRenren.publishFeed(param, listener, true);

			} else {
				showLoginAlertDialog(R.string.share_renren_expired);
			}
		} else {
			showBoundAlertDialog(R.string.account_bound_renren_none);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.screen_top_play_control_back:
			finish();
			break;
		case R.id.btn_share_sina:
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				shareToSina(mPicPathList.get(0), mTextView.getText().toString());
			} else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.btn_share_qq:
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				shareToQQ(mPicPathList.get(0), mTextView.getText().toString());
			} else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.btn_share_renren:
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				shareToRenren(imgurl_renren, mTextView.getText().toString());
			} else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.share_btn_reupload:
			uploadNotePic();
			break;
		default:
			break;
		}
	}

	private void showProgressBar(int id) {
//		mProgressBar.setVisibility(View.VISIBLE);
		showProgress(null, getString(id));
		mReuploadButton.setVisibility(View.GONE);
//		Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
	}

	private void dismssProgressBar(int id, boolean isSuccessed) {
//		mProgressBar.setVisibility(View.GONE);
		dismissProgress();
		Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
	}

	private void showLoginAlertDialog(int id) {
		// new AlertDialog.Builder(ShareScreen.this)
		// .setTitle(R.string.setting_tips)
		// .setMessage(id)
		// .setPositiveButton(R.string.login_relogin,
		// new DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog,
		// int which) {
		// Intent intent = new Intent(ShareScreen.this,
		// LoginScreen.class);
		// startActivity(intent);
		// }
		// }).setNegativeButton(R.string.setting_cancel, null)
		// .create().show();
		final Dialog dialog = new Dialog(this, R.style.CornerDialog);
		dialog.setContentView(R.layout.dialog_ok_cancel);
		TextView titleView = (TextView) dialog.findViewById(R.id.dialog_title);
		titleView.setText(R.string.dialog_title_tips);
		TextView msgView = (TextView) dialog.findViewById(R.id.dialog_message);
		msgView.setText(id);
		Button btn_ok = (Button) dialog.findViewById(R.id.dialog_btn_ok);
		btn_ok.setText(R.string.login_relogin);
		btn_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ShareScreen.this, LoginScreen.class);
				startActivity(intent);
				dialog.dismiss();
			}
		});
		Button btn_cancel = (Button) dialog
				.findViewById(R.id.dialog_btn_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void showBoundAlertDialog(int id) {
		// new AlertDialog.Builder(ShareScreen.this)
		// .setTitle(R.string.setting_tips)
		// .setMessage(id)
		// .setPositiveButton(R.string.share_dialog_bound,
		// new DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog,
		// int which) {
		// Intent intent = new Intent(ShareScreen.this,
		// AccountScreen.class);
		// startActivity(intent);
		// }
		// }).setNegativeButton(R.string.setting_cancel, null)
		// .create().show();
		final Dialog dialog = new Dialog(this, R.style.CornerDialog);
		dialog.setContentView(R.layout.dialog_ok_cancel);
		TextView titleView = (TextView) dialog.findViewById(R.id.dialog_title);
		titleView.setText(R.string.dialog_title_tips);
		TextView msgView = (TextView) dialog.findViewById(R.id.dialog_message);
		msgView.setText(id);
		Button btn_ok = (Button) dialog.findViewById(R.id.dialog_btn_ok);
		btn_ok.setText(R.string.share_dialog_bound);
		btn_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ShareScreen.this,
						AccountScreen.class);
				startActivity(intent);
				dialog.dismiss();
			}
		});
		Button btn_cancel = (Button) dialog
				.findViewById(R.id.dialog_btn_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
}

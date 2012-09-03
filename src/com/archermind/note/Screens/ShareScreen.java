package com.archermind.note.Screens;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONObject;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Events.EventTypes;
import com.archermind.note.Provider.DatabaseHelper;
import com.archermind.note.Services.ServiceManager;
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
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ShareScreen extends Screen implements OnClickListener {

	private SharedPreferences mPreferences;
	private Button mBackButton;
	private ImageView mImageView;
	private EditText mEditText;
	private LinearLayout mProgressLayout;
	private TextView mUploadingView;
	private TextView mReuploadView;
	private Button mSinaButton;
	private Button mQQButton;
	private Button mRenrenButton;
	private ArrayList<String> mPicPathList;
	private static final String TAG = "ShareScreen";
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Log.i(TAG, "msg.what = " + msg.what);
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share);
		mPreferences = PreferencesHelper.getSharedPreferences(this, 0);
		Intent intent = getIntent();
		mPicPathList = intent.getStringArrayListExtra("picpathlist");

		mBackButton = (Button) findViewById(R.id.screen_top_play_control_back);
		mBackButton.setOnClickListener(this);

		mImageView = (ImageView) findViewById(R.id.share_image);
		mImageView
				.setImageBitmap(BitmapFactory.decodeFile(mPicPathList.get(0)));

		mProgressLayout = (LinearLayout) findViewById(R.id.share_progress_layout);
		mUploadingView = (TextView) findViewById(R.id.share_text_uploading);
		mReuploadView = (TextView) findViewById(R.id.share_text_reupload);
		mReuploadView.setOnClickListener(this);
		mEditText = (EditText) findViewById(R.id.share_edit);

		mSinaButton = (Button) findViewById(R.id.btn_share_sina);
		mSinaButton.setOnClickListener(this);

		mQQButton = (Button) findViewById(R.id.btn_share_qq);
		mQQButton.setOnClickListener(this);
		if (!NoteApplication.getInstance().ismBound_QQ()) {
			mQQButton.setBackgroundResource(R.drawable.qq_gray);
		}

		mRenrenButton = (Button) findViewById(R.id.btn_share_renren);
		mRenrenButton.setOnClickListener(this);

//		 //上传笔记图片至服务器（待修改）
//	
//		 for (int i = 0; i < mPicPathList.size(); i++) {
//		 ServerInterface.uploadFile(this,mHandler,
//		 NoteApplication.getInstance().getUserName(),
//		 mPicPathList.get(i));
//		 }

		// 异步执行分享到广场
		SquareTask squareTask = new SquareTask();
		squareTask.execute(intent.getStringExtra("noteid"),
				intent.getStringExtra("title"),
				intent.getStringExtra("action"), intent.getStringExtra("sid"));

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (NoteApplication.getInstance().ismBound_Sina()) {
			mSinaButton.setBackgroundResource(R.drawable.btn_sina_selector);
		} else {
			mSinaButton.setBackgroundResource(R.drawable.sina_gray);
		}

		if (NoteApplication.getInstance().ismBound_QQ()) {
			mQQButton.setBackgroundResource(R.drawable.btn_qq_selector);
		} else {
			mQQButton.setBackgroundResource(R.drawable.qq_gray);
		}

		if (NoteApplication.getInstance().ismBound_Renren()) {
			mRenrenButton.setBackgroundResource(R.drawable.btn_renren_selector);
		} else {
			mRenrenButton.setBackgroundResource(R.drawable.renren_gray);
		}
	}

	class SquareTask extends AsyncTask<String, integer, String> {

		@Override
		protected String doInBackground(String... params) {	
			String userIdStr = String.valueOf(NoteApplication.getInstance()
					.getUserId());
			int totalPage = 0;
			if (mPicPathList != null) {
				totalPage = mPicPathList.size();
			}

			String context = totalPage == 0 ? "" : mPicPathList.get(0);

			if (!"".equals(context)) {
				context = context.substring(context.lastIndexOf("/") + 1);
			}

			int serviceId = ServerInterface.uploadNote(
					Long.parseLong(params[0]), userIdStr, params[3], params[2],
					params[1], context, String.valueOf(totalPage));
			if(serviceId == ServerInterface.COOKIES_ERROR){
				return "cookis_error";
			}else {
				if ("A".equals(params[2])) {
					if (serviceId > 0) {
						ContentValues contentValues2 = new ContentValues();
						contentValues2.put(DatabaseHelper.COLUMN_NOTE_SERVICE_ID,
								String.valueOf(serviceId));
						contentValues2.put(DatabaseHelper.COLUMN_NOTE_USER_ID,
								Integer.parseInt(userIdStr));
						ServiceManager.getDbManager().updateLocalNotes(
								contentValues2, Integer.parseInt(params[0]));
						Log.i(TAG, "===============分享到广场成功===============");
						return "success";
					} else {
						Log.i(TAG, "===============分享到广场失败===============");
						return "failed";
					}
				} else if ("M".equals(params[2])) {
					if (serviceId == 0) {
						Log.i(TAG, "===============分享到广场成功===============");
						return "success";
					} else {
						Log.i(TAG, "===============分享到广场失败===============");
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
				dismssProgressBar(R.string.share_success, true);
				MainScreen.eventService.onUpdateEvent(new EventArgs(
						EventTypes.SHARE_NOTE_SUCCESSED));
			} else if (result.equals("failed")) {
				dismssProgressBar(R.string.share_failed, false);
				mReuploadView.setVisibility(View.VISIBLE);
				MainScreen.eventService.onUpdateEvent(new EventArgs(
						EventTypes.SHARE_NOTE_FAILED));
			} else if (result.equals("cookies_error")) {
				dismssProgressBar(R.string.share_failed, false);
			}
		}

	}

	private void shareToSina(String imgPath, String msg) {
		if (NoteApplication.getInstance().ismBound_Sina()) {
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
				showProgressBar(R.string.share_dialog_msg_sina);// 显示进度
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
								if (e.getStatusCode() == 21332) { // accessToken过期错误码
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											dismssProgressBar(
													R.string.share_failed,
													false);
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
			showProgressBar(R.string.share_dialog_msg_qq);
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
					dismssProgressBar(R.string.share_failed, false);
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
		if (NoteApplication.getInstance().ismBound_QQ()) {
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
		if (NoteApplication.getInstance().ismBound_Renren()) {
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
						"http://www.archermind.com", imgPath, null, null, null,
						null);
				AbstractRequestListener<FeedPublishResponseBean> listener = new AbstractRequestListener<FeedPublishResponseBean>() {

					@Override
					public void onRenrenError(RenrenError rre) {
						if (rre.getErrorCode() == 2002) { // accessToken过期错误码
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									dismssProgressBar(R.string.share_failed,
											false);
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
				showProgressBar(R.string.share_dialog_msg_renren);
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
				shareToSina(mPicPathList.get(0), mEditText.getText().toString());
			} else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.btn_share_qq:
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				shareToQQ(mPicPathList.get(0), mEditText.getText().toString());
			} else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.btn_share_renren:
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				shareToRenren(mPicPathList.get(0), mEditText.getText()
						.toString());
			} else {
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.share_text_reupload:
			showProgressBar(R.string.share_dialog_msg_square);
			mReuploadView.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}

	private void showProgressBar(int id) {
		mProgressLayout.setVisibility(View.VISIBLE);
		mUploadingView.setText(id);
	}

	private void dismssProgressBar(int id, boolean isSuccessed) {
		mProgressLayout.setVisibility(View.GONE);
		Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
	}

	private void showLoginAlertDialog(int id) {
		new AlertDialog.Builder(ShareScreen.this)
				.setTitle(R.string.setting_tips)
				.setMessage(id)
				.setPositiveButton(R.string.login_relogin,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent(ShareScreen.this,
										LoginScreen.class);
								startActivity(intent);
							}
						}).setNegativeButton(R.string.setting_cancel, null)
				.create().show();
	}

	private void showBoundAlertDialog(int id) {
		new AlertDialog.Builder(ShareScreen.this)
				.setTitle(R.string.setting_tips)
				.setMessage(id)
				.setPositiveButton(R.string.share_dialog_bound,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent(ShareScreen.this,
										AccountScreen.class);
								startActivity(intent);
							}
						}).setNegativeButton(R.string.setting_cancel, null)
				.create().show();
	}
}

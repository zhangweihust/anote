package com.android.note.Screens;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.android.note.NoteApplication;
import com.android.note.Events.EventArgs;
import com.android.note.Events.EventTypes;
import com.android.note.Events.IEventHandler;
import com.android.note.Services.EventService;
import com.android.note.Services.ServiceManager;
import com.android.note.Utils.HttpUtils;
import com.android.note.Utils.NetworkUtils;
import com.android.note.Utils.ServerInterface;
import com.archermind.note.R;

public class PlazaScreen extends Screen implements IEventHandler{
	
	private WebView mWebView;
	private TextView mTextView;
	public static boolean isFirstPage = true;
	private int mNetwork;
	private boolean mIsLogin = false;
	private int mUserId = -1;
	public static final EventService eventService = ServiceManager
			.getEventservice();
	private JsResult mResult = null;
	public static String USER_AGENT = "archermind-anote";

	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			dismissLoadingProgress();
			String filePath = msg.getData().getString("filepath");
			if(filePath != null && !filePath.equals("")){
				System.out.println("filePath: " + filePath);				
				Intent intent = new Intent();
				intent.setClass(PlazaScreen.this, EditNoteScreen.class);
				intent.putExtra("filePath", filePath);
				PlazaScreen.this.startActivity(intent);
		}else if(msg.what == 0){
			showToast(R.string.loadfailed);
		}
	}
	};

	
	
		@SuppressLint("SetJavaScriptEnabled")
		@Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.plaza_screen);
	        mWebView = (WebView) findViewById(R.id.plaza_page);
	        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
	        mTextView = (TextView) findViewById(R.id.tv_no_web);	
	        if(android.os.Build.VERSION.SDK_INT > 8){
		        Typeface type = Typeface.createFromAsset(getAssets(),"xdxwzt.ttf");
				mTextView.setTypeface(type);
	        }
	       	        
	        CookieSyncManager.createInstance(this);
			eventService.add(this);
			System.out.println("=== plazascreen onCreate");
		}
	 
		private void init(String url){
			mNetwork = NetworkUtils.getNetworkState(this);
			  if(mNetwork == NetworkUtils.NETWORN_NONE){
					showToast(R.string.network_none);
		        	mTextView.setVisibility(View.VISIBLE);
		        	mWebView.setVisibility(View.GONE);
		        	return;
		        }
		        mWebView.setVisibility(View.VISIBLE);
		        mTextView.setVisibility(View.GONE);
		        mWebView.getSettings().setJavaScriptEnabled(true); 
		       // mWebView.getSettings().setBuiltInZoomControls(true);
		        mWebView.getSettings().setUserAgentString(USER_AGENT);
		        mWebView.requestFocus();
		        if(ServiceManager.isLogin()){
	 		        CookieSyncManager.getInstance().startSync();
	 		        CookieManager.getInstance().setCookie(ServerInterface.URL_WEBSITE, "userid=" + ServiceManager.getUserId() + ";");
	 		        mIsLogin = true;
	 		        mUserId = ServiceManager.getUserId();
	 		    }else{
	 		    	CookieSyncManager.getInstance().startSync();
	 			    CookieManager.getInstance().removeSessionCookie();
	 			    mIsLogin = false;
	 			    mUserId = -1;
	 		    }
		        
		        mWebView.setWebViewClient(new WebViewClient(){

	            @Override
	            public boolean shouldOverrideUrlLoading(WebView view, String url) {
	                    // TODO Auto-generated method stub
	                    view.loadUrl(url);
	                    return true;
	            }
	            

				@Override
				public void onPageStarted(WebView view, String url,
						Bitmap favicon) {
					// TODO Auto-generated method stub
					super.onPageStarted(view, url, favicon);
					showLoadingProgress();
					if(view.getTitle()!=null&&!view.getTitle().equals("") && (view.getTitle().contains("主页")||view.getTitle().contains("相册")||view.getTitle().contains("广场")||view.getTitle().contains("作品"))){
					MainScreen.eventService.onUpdateEvent(new EventArgs(
							EventTypes.MAIN_SCREEN_UPDATE_TITLE).putExtra(
							"title",view.getTitle()));
					}
				}


				@Override
				public void onPageFinished(WebView view, String url) {
					// TODO Auto-generated method stub
					super.onPageFinished(view, url);
					if(view.getTitle()!=null&&!view.getTitle().equals("") && (view.getTitle().contains("主页")||view.getTitle().contains("相册")||view.getTitle().contains("广场")||view.getTitle().contains("作品"))){
						MainScreen.eventService.onUpdateEvent(new EventArgs(
								EventTypes.MAIN_SCREEN_UPDATE_TITLE).putExtra(
								"title",view.getTitle()));
						}
					dismissLoadingProgress();
					 //System.out.println("====" + getWindowManager().getDefaultDisplay().getHeight());

				}


				@Override
				public void onReceivedError(WebView view, int errorCode,
						String description, String failingUrl) {
					// TODO Auto-generated method stub
					super.onReceivedError(view, errorCode, description, failingUrl);

					dismissLoadingProgress();
				}	 
	            
	            
	         }); 
		       
		       mWebView.setWebChromeClient(new WebChromeClient(){

				@Override
				public boolean onJsAlert(WebView view, String url,
						String message, JsResult result) {
					// TODO Auto-generated method stub
					
					if(NetworkUtils.getNetworkState(PlazaScreen.this) == NetworkUtils.NETWORN_NONE){
						PlazaScreen.this.runOnUiThread(new Runnable() {									
							@Override
							public void run() {
								// TODO Auto-generated method stub
						    Toast.makeText(PlazaScreen.this, R.string.network_none,
										Toast.LENGTH_SHORT).show();
						    mTextView.setVisibility(View.VISIBLE);
				        	mWebView.setVisibility(View.GONE);
				        	mNetwork = NetworkUtils.getNetworkState(PlazaScreen.this);
							}
						});
						result.confirm();
						return true;
					}
										
					if(message.trim().equals("unlogin")){/*
						showToast(R.string.no_login_info);
						Intent intent = new Intent();
						intent.setClass(PlazaScreen.this, LoginScreen.class);
						PlazaScreen.this.startActivity(intent);*/
						result.confirm();
						return true;
					}else if(message.trim().equals("content_empty")){
						//showToast(R.string.content_empty);
						result.confirm();
						return true;
					}else if(message.trim().startsWith("reply")){
						if(!ServiceManager.isLogin()){																	
							showToast(R.string.no_login_info);
							result.confirm();
							return true;
						}
						System.out.println("===== after =====");
						String nid = message.trim().substring(message.trim().indexOf(":")+1);
						Intent intent = new Intent();
						intent.setClass(PlazaScreen.this, NoteReplyScreen.class);
						intent.putExtra("nid", nid);
						PlazaScreen.this.startActivityForResult(intent, 0);				       
						mResult = result;
						return true;
					}else if(message.trim().equals("album")){				
						Intent intent = new Intent();
						intent.setClass(PlazaScreen.this, AlbumScreen.class);
						PlazaScreen.this.startActivity(intent);						
						result.confirm();
						return true;
					}else if(message.trim().equals("portrait")){
						Intent intent = new Intent();
						intent.setClass(PlazaScreen.this, PersonInfoScreen.class);
						PlazaScreen.this.startActivity(intent);						
						result.confirm();
						return true;
					}else if(message.trim().startsWith("note")){
						final String nid = message.trim().substring(message.trim().indexOf(":")+1);
						System.out.println("====nid===" + nid + ", " + message);
						final String r = ServerInterface.getDiary(nid);
						if(r != null){
							if(r.equals(ServerInterface.COOKIES_ERROR+"")){
								showToast(R.string.cookies_error);
								ServiceManager.setLogin(false);
							}else if(r.equals("-1")){
								showToast(R.string.notfound);
							}else {
								if(r.contains("filename")){
									   showLoadingProgress();
										new Thread(new Runnable() {
											public void run() {
												try{
												String filename = r.substring(0, r.lastIndexOf("&"));
												filename = filename.substring(filename.lastIndexOf("=")+1); 
												System.out.println(filename);
												String filePath = NoteApplication.downloadPath + filename;
												if(filename!=null && !filename.equals("") && HttpUtils.DownloadFile(r, filePath) == 0 ){
													Message msg = new Message();
													 Bundle b = new Bundle();// 存放数据
											         b.putString("filepath", filePath);
											         msg.setData(b);
													 mHandler.sendMessage(msg);
												}else{
													mHandler.sendEmptyMessage(0);
												}
												}catch (Exception e) {
													// TODO: handle exception
													mHandler.sendEmptyMessage(0);
												}
											}
										}).start();
									
									
								}
							}
					    }		
						//dismissLoadingProgress();
						result.confirm();
						return true;
					}else if(message.trim().equals("startload")){
						showLoadingProgress();
						result.confirm();
						return true;
					}else if(message.trim().equals("finishload")){
						dismissLoadingProgress();
						result.confirm();
						return true;
					}else if(message.trim().startsWith("result:")){
						System.out.println(message);
						result.confirm();
						return true;
					}
					return super.onJsAlert(view, url, message, result);
				}
		       });
		    	
		       if(!url.equals("")){
		    	   mWebView.loadUrl(url);
		       }else{
		    	   mWebView.loadUrl(ServerInterface.URL_WEBSITE);
		       }
		        

		        
		}
		@Override
		public void onBackPressed() {
			// TODO Auto-generated method stub
			if(mTextView.getVisibility() == View.VISIBLE){
				isFirstPage = true;
				super.onBackPressed();
				return;
			}
			WebBackForwardList wl = mWebView.copyBackForwardList();
			System.out.println("wl.getCurrentIndex() : " + wl.getCurrentIndex());
			if(wl.getCurrentIndex() > 1){
	    		isFirstPage = false;
	    		System.out.println("go back 0");
	    		String urlBefore = wl.getCurrentItem().getUrl();
	    		String urlAfter = wl.getItemAtIndex(wl.getCurrentIndex()-1).getUrl();
	    		System.out.println(urlBefore);
	    		System.out.println(urlAfter);
	    		if(urlBefore.startsWith(urlAfter)){
	    			System.out.println("go back 2");
	    			mWebView.goBackOrForward(-2);
	    		}else{
	    			mWebView.goBack();
	    		}
	    	}else if(wl.getCurrentIndex() == 1 || wl.getCurrentIndex() == -1){
	    		isFirstPage = true;
	    		if(mWebView.canGoBack()){
	    			System.out.println("go back 1");
	    			mWebView.goBack();
	    		}
	    	}else{
	    		super.onBackPressed();
	    	}
		}
		
	 	@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
	 		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && mTextView.getVisibility() == View.VISIBLE){
	 			isFirstPage = true;
	 			return super.dispatchKeyEvent(event);
	 		}
			System.out.println("plazascreen dispatchKeyEvent : " + event.getKeyCode() + ", " + event.getAction());
			WebBackForwardList wl = mWebView.copyBackForwardList();
			System.out.println("wl.getCurrentIndex() : " + wl.getCurrentIndex());
			System.out.println(mWebView.getUrl());
			if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && mWebView.getUrl().equals(ServerInterface.URL_WEBSITE)){
				isFirstPage = true;
			}else if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && (wl.getCurrentIndex() > 0 || (!mWebView.canGoBack() && wl.getCurrentIndex() ==0 && mWebView.getUrl().contains("note")))){
				isFirstPage = false;
			}else if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && !mWebView.canGoBack() && wl.getCurrentIndex() == 0 && mWebView.getUrl().contains("note")){
	    		mWebView.loadUrl(ServerInterface.URL_WEBSITE);
	    		isFirstPage = true;
	    		return true;
	    	}
	    	
	    	if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && event.getRepeatCount() == 0) { 
		    	this.onBackPressed();
		        return true;  
		    }
		    return super.dispatchKeyEvent(event);
		    
		}
	 
	 	public void refresh(final String url){
	 		System.out.println("===refresh===" + mNetwork);
	 		System.out.println("CurrentUA : " + mWebView.getSettings().getUserAgentString());

			PlazaScreen.this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub	
	 	    if(NetworkUtils.getNetworkState(PlazaScreen.this) == NetworkUtils.NETWORN_NONE){
	 	    	showToast(R.string.network_none);
	        	mTextView.setVisibility(View.VISIBLE);
	        	mWebView.setVisibility(View.GONE);
	        	mNetwork = NetworkUtils.getNetworkState(PlazaScreen.this);
	        	return;
	        }else if(NetworkUtils.getNetworkState(PlazaScreen.this) != mNetwork){
	        	init(url);
	        }else{
        		if(ServiceManager.isLogin()){
	 		        CookieSyncManager.getInstance().startSync();
	 		        CookieManager.getInstance().setCookie(ServerInterface.URL_WEBSITE, "userid=" + ServiceManager.getUserId() + ";");
	 		        mIsLogin = true;
	 		        mUserId = ServiceManager.getUserId();
 		        }else{
 		        	CookieSyncManager.getInstance().startSync();
 			        CookieManager.getInstance().removeSessionCookie();
 			        mIsLogin = false;
 			        mUserId = -1;
 		        }
	        	mNetwork = NetworkUtils.getNetworkState(PlazaScreen.this);
		        mWebView.requestFocus();		        
		        try {
					Thread.sleep(50);
					if(url.equals("")){
						mWebView.reload();
					}else{
						 mWebView.loadUrl(url);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	}
	 	    }});
	 	}
	 	
	 	@Override
	 	protected void onResume() {
	 	  super.onResume();
		 	 System.out.println("===== resume =====");
		 	 mWebView.requestFocus();
		 	 if(NetworkUtils.getNetworkState(this) == NetworkUtils.NETWORN_NONE){
		 		showToast(R.string.network_none);
		        	mTextView.setVisibility(View.VISIBLE);
		        	mWebView.setVisibility(View.GONE);
		        	mNetwork = NetworkUtils.getNetworkState(this);
		        	return;
		     }
			if(mResult!=null){ 
				mResult.confirm();
			}

		 	 if(mWebView.getUrl()!= null && (ServiceManager.isLogin() != mIsLogin ||(ServiceManager.isLogin()==true && mUserId != ServiceManager.getUserId())) && (mWebView.getUrl().equals(ServerInterface.URL_WEBSITE)||mWebView.getUrl().startsWith(ServerInterface.URL_WEBSITE_NOTE))){
		 		 if(ServiceManager.isLogin()){
				 		System.out.println("===== logined =====");
				        CookieSyncManager.getInstance().startSync();
				        CookieManager.getInstance().setCookie(ServerInterface.URL_WEBSITE, "userid=" + ServiceManager.getUserId() + ";");
				        mIsLogin = true;
				        mUserId = ServiceManager.getUserId();
				    }else{
				    	CookieSyncManager.getInstance().startSync();
					    CookieManager.getInstance().removeSessionCookie();
					    mIsLogin = false;
					    mUserId = -1;
				    }
		 		 try {
					Thread.sleep(50);
					mWebView.reload();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		 		 
		 	 }
		 	 
		    CookieSyncManager.getInstance().stopSync();
		 	 

	 	}


	 	@Override
	 	protected void onPause() {
	 	  super.onPause();
	 	  CookieSyncManager.getInstance().sync();	 	 
	 	}

		@Override
		public boolean onEvent(Object sender, EventArgs e) {
			// TODO Auto-generated method stub
			switch(e.getType()){
			case REFRESH_WEBVIEW:
				String url = "";
				if(e.getExtra("url") != null && !e.getExtra("url").equals("")){
					url = (String)e.getExtra("url");
				}
				System.out.println("onEvent " + url);
				refresh(url);					
				break;
			case PLAZA_COOKIE_ERROR:
				mResult.confirm();
				break;
			}
			return false;
		}
		
		@Override
		protected void onDestroy() {
			// TODO Auto-generated method stub
			super.onDestroy();
			eventService.remove(this);
		}
		
		
		private void showToast(final int rid){
			PlazaScreen.this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(PlazaScreen.this, rid,
							Toast.LENGTH_SHORT).show();
					
				}
			});
		}
		
		private void showLoadingProgress(){
			PlazaScreen.this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					showProgress(null, getResources().getString(R.string.refreshing));								
				}
			});
		}
		
		private void dismissLoadingProgress(){
			PlazaScreen.this.runOnUiThread(new Runnable() {							
				@Override
				public void run() {
					// TODO Auto-generated method stub
					dismissProgress();								
				}
			});
		}

		@Override
		protected void onActivityResult(int requestCode, int resultCode,
				Intent data) {
			// TODO Auto-generated method stub
			if(mResult!=null && resultCode ==RESULT_OK){ 
				mResult.confirm();
			}else{
				
			}
			super.onActivityResult(requestCode, resultCode, data);
		}
		
		
}
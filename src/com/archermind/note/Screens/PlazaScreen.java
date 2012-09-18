package com.archermind.note.Screens;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
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

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Events.IEventHandler;
import com.archermind.note.Services.EventService;
import com.archermind.note.Services.ServiceManager;
import com.archermind.note.Utils.HttpUtils;
import com.archermind.note.Utils.NetworkUtils;
import com.archermind.note.Utils.ServerInterface;

public class PlazaScreen extends Screen implements IEventHandler{
	
	private WebView mWebView;
	private TextView mTextView;
	private static String url = "http://anote.archermind.com/web/index.php";
	//private static String url = "http://192.168.1.100";
	public static boolean isFirstPage = true;
	private int mNetwork;
	private boolean mIsLogin = false;
	public static final EventService eventService = ServiceManager
			.getEventservice();
	private JsResult mResult = null;
	
	
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
			init();
			eventService.add(this);
		}
	 
		private void init(){
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
		        mWebView.getSettings().setBuiltInZoomControls(true);
		        mWebView.requestFocus();
		        if(ServiceManager.isLogin()){
	 		        CookieSyncManager.getInstance().startSync();
	 		        CookieManager.getInstance().setCookie(url, "userid=" + ServiceManager.getUserId() + ";");
	 		        mWebView.clearCache(true);
	 		        mWebView.clearHistory();
	 		        mIsLogin = true;
	 		    }else{
	 		    	CookieSyncManager.getInstance().startSync();
	 			    CookieManager.getInstance().removeSessionCookie();
	 			    mIsLogin = false;
	 		    }
		        
		        mWebView.setWebViewClient(new WebViewClient(){

	            @Override
	            public boolean shouldOverrideUrlLoading(WebView view, String url) {
	                    // TODO Auto-generated method stub
	                    view.loadUrl(url);
	                    view.getSettings().setJavaScriptEnabled(true);  
	                    return true;
	            }

			/*	@Override
				public void onPageFinished(WebView view, String url) {
					// TODO Auto-generated method stub
					super.onPageFinished(view, url);
					dismissLoadingProgress();
				}	*/ 
	            
	            
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
										
					if(message.trim().equals("unlogin")){
						showToast(R.string.no_login_info);
						Intent intent = new Intent();
						intent.setClass(PlazaScreen.this, LoginScreen.class);
						PlazaScreen.this.startActivity(intent);
						result.confirm();
						return true;
					}else if(message.trim().equals("content_empty")){
						showToast(R.string.content_empty);
						result.confirm();
						return true;
					}else if(message.trim().startsWith("reply")){
						if(!ServiceManager.isLogin()){																	
							showToast(R.string.no_login_info);
							result.confirm();
							return true;
						}
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
					}else if(message.trim().startsWith("note")){
						final String nid = message.trim().substring(message.trim().indexOf(":")+1);
						System.out.println("====nid===" + nid + ", " + message);
						String r = ServerInterface.getDiary("44");
						if(r != null){
							if(r.equals(ServerInterface.COOKIES_ERROR+"")){
								showToast(R.string.cookies_error);
								ServiceManager.setLogin(false);
							}else if(r.equals("-1")){
								showToast(R.string.notfound);
							}else {
								if(r.contains("filename")){
									try{
									String filename = r.substring(0, r.lastIndexOf("&"));
									filename = filename.substring(filename.lastIndexOf("=")+1); 
									System.out.println(filename);
									String filePath = NoteApplication.downloadPath + filename;
									if(filename!=null && !filename.equals("") && HttpUtils.DownloadFile(r, filePath) == 0 ){
										Intent intent = new Intent();
										intent.setClass(PlazaScreen.this, EditNoteScreen.class);
										intent.putExtra("filepath", filePath);
										PlazaScreen.this.startActivity(intent);
									}
									}catch (Exception e) {
										// TODO: handle exception
									}
								}
							}
					    }				
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
					}
					return super.onJsAlert(view, url, message, result);
				}
		       });
		    	           
		        mWebView.loadUrl(url);
		        
		        mWebView.setOnTouchListener(new OnTouchListener() {
					
					@Override
					public boolean onTouch(View arg0, MotionEvent arg1) {
						// TODO Auto-generated method stub
						
						if(arg1.equals(MotionEvent.ACTION_MOVE)){
						System.out.println("montion Event" + arg1);
						}
						return false;
					}
				});
		        
		        
		}
		@Override
		public void onBackPressed() {
			// TODO Auto-generated method stub
			WebBackForwardList wl = mWebView.copyBackForwardList();
			System.out.println("wl.getCurrentIndex() : " + wl.getCurrentIndex());
			if(wl.getCurrentIndex() > 1){
	    		isFirstPage = false;
	    		mWebView.goBack();
	    	}else if(wl.getCurrentIndex() == 1 || wl.getCurrentIndex() == -1){
	    		isFirstPage = true;
	    		if(mWebView.canGoBack()){
	    			mWebView.goBack();
	    		}
	    	}else{
	    		super.onBackPressed();
	    	}
		}
		
	 	@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			System.out.println("plazascreen dispatchKeyEvent : " + event.getKeyCode() + ", " + event.getAction());
			WebBackForwardList wl = mWebView.copyBackForwardList();
			System.out.println("wl.getCurrentIndex() : " + wl.getCurrentIndex());
			if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && wl.getCurrentIndex() > 0){
				isFirstPage = false;
			}	
	    	System.out.println(mWebView.canGoBack() + ", " + event.getRepeatCount());	    
	    	if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && event.getRepeatCount() == 0) { 
		    	this.onBackPressed();
		        return true;  
		    }
		    return super.dispatchKeyEvent(event);
		    
		}
	 
	 	public  void refresh(){
	 		System.out.println("===refresh===" + mNetwork);
	 	    if(NetworkUtils.getNetworkState(this) == NetworkUtils.NETWORN_NONE){
	 	    	showToast(R.string.network_none);
	        	mTextView.setVisibility(View.VISIBLE);
	        	mWebView.setVisibility(View.GONE);
	        	mNetwork = NetworkUtils.getNetworkState(this);
	        	return;
	        }else if(NetworkUtils.getNetworkState(this) != mNetwork){
	        	init();
	        }else{
        		if(ServiceManager.isLogin()){
	 		        CookieSyncManager.getInstance().startSync();
	 		        CookieManager.getInstance().setCookie(url, "userid=" + ServiceManager.getUserId() + ";");
	 		        mIsLogin = true;
 		        }else{
 		        	CookieSyncManager.getInstance().startSync();
 			        CookieManager.getInstance().removeSessionCookie();
 			        mIsLogin = false;
 		        }
	        	mNetwork = NetworkUtils.getNetworkState(this);
		        mWebView.requestFocus();		        
		        try {
					Thread.sleep(50);
					mWebView.reload();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	
		 		
		 }
	 	}
	 	
	 	@Override
	 	protected void onResume() {
	 	  super.onResume();
		 	 System.out.println("===== resume =====");
		 	 if(NetworkUtils.getNetworkState(this) == NetworkUtils.NETWORN_NONE){
		 		showToast(R.string.network_none);
		        	mTextView.setVisibility(View.VISIBLE);
		        	mWebView.setVisibility(View.GONE);
		        	mNetwork = NetworkUtils.getNetworkState(this);
		        	return;
		     }
		 	 
		 	 if(ServiceManager.isLogin() != mIsLogin){
		 		 if(ServiceManager.isLogin()){
				 		System.out.println("===== logined =====");
				        CookieSyncManager.getInstance().startSync();
				        CookieManager.getInstance().setCookie(url, "userid=" + ServiceManager.getUserId() + ";");
				        mIsLogin = true;
				    }else{
				    	CookieSyncManager.getInstance().startSync();
					    CookieManager.getInstance().removeSessionCookie();
					    mIsLogin = false;
				    }
		 		 mWebView.requestFocus();
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
				PlazaScreen.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						refresh();
					}
				});
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
		

		
		public static boolean saveUrlAs(String path, String fileName) {
			//此方法只能用户HTTP协议
			    try {
			      URL url = new URL(path);
			      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			      DataInputStream in = new DataInputStream(connection.getInputStream());
			      DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
			      byte[] buffer = new byte[4096];
			      int count = 0;
			      while ((count = in.read(buffer)) > 0) {
			    	  out.write(buffer, 0, count);
			      	}
			      out.close();
			      in.close();
			      return true;
			    }catch (Exception e) {
			    	return false;
				}
			}

		@Override
		protected void onActivityResult(int requestCode, int resultCode,
				Intent data) {
			// TODO Auto-generated method stub
			if(mResult!=null && resultCode ==RESULT_OK){ 
				mResult.confirm();
			}
			super.onActivityResult(requestCode, resultCode, data);
		}
		
		
}
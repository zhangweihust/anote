package com.archermind.note.Screens;


import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
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
import com.archermind.note.Utils.NetworkUtils;

public class PlazaScreen extends Screen implements IEventHandler{
	
	private WebView mWebView;
	private TextView mTextView;
	private static String url = "http://note.archermind.com/web/index.php";
	//private static String url = "http://192.168.1.101";
	public static boolean isFirstPage = true;
	private int mNetwork;
	public static final EventService eventService = ServiceManager
			.getEventservice();
	
	
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
					Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT).show();
		        	mTextView.setVisibility(View.VISIBLE);
		        	mWebView.setVisibility(View.GONE);
		        	return;
		        }
		        mWebView.setVisibility(View.VISIBLE);
		        mTextView.setVisibility(View.GONE);
		        mWebView.getSettings().setJavaScriptEnabled(true); 
		        mWebView.getSettings().setBuiltInZoomControls(true);
		        mWebView.requestFocus();

		        if(NoteApplication.getInstance().isLogin()){
		        CookieSyncManager.getInstance().startSync();
		        CookieManager.getInstance().setCookie(url, "userid=" + NoteApplication.getInstance().getUserId() + ";");
		        mWebView.clearCache(true);
		        mWebView.clearHistory();
		        }else{
			        CookieManager.getInstance().removeSessionCookie();
		        }

		       System.out.println("=== cookie is " + CookieManager.getInstance().getCookie(url));
		       mWebView.setWebViewClient(new WebViewClient(){

	            @Override
	            public boolean shouldOverrideUrlLoading(WebView view, String url) {
	                    // TODO Auto-generated method stub
	                    view.loadUrl(url);
	                    view.getSettings().setJavaScriptEnabled(true);  
	                    return true;
	            }	            
	         }); 
		       
		       mWebView.setWebChromeClient(new WebChromeClient(){

				@Override
				public boolean onJsAlert(WebView view, String url,
						String message, JsResult result) {
					// TODO Auto-generated method stub
					if(message.trim().equals("unlogin")){
						PlazaScreen.this.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(PlazaScreen.this, R.string.no_login_info,
										Toast.LENGTH_SHORT).show();
							}
						});
						Intent intent = new Intent();
						intent.setClass(PlazaScreen.this, LoginScreen.class);
						PlazaScreen.this.startActivity(intent);
						return true;
					}else if(message.trim().equals("content_empty")){
						PlazaScreen.this.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(PlazaScreen.this, R.string.content_empty,
										Toast.LENGTH_SHORT).show();
							}
						});
						mWebView.requestFocus();
						return true;
					}
					return super.onJsAlert(view, url, message, result);
				}
		       });
		    	           
		        
		        mWebView.loadUrl(url);
		        
		        
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
	 		System.out.println("===refresh===" + mNetwork);/*
	 	    if(NetworkUtils.getNetworkState(this) == NetworkUtils.NETWORN_NONE){
				Toast.makeText(this, R.string.network_none, Toast.LENGTH_SHORT).show();
	        	mTextView.setVisibility(View.VISIBLE);
	        	mWebView.setVisibility(View.GONE);
	        	mNetwork = NetworkUtils.getNetworkState(this);
	        	return;
	        }else if(NetworkUtils.getNetworkState(this) != mNetwork){*/
	        	init();
	      /*  }else{
	        	mNetwork = NetworkUtils.getNetworkState(this);
		        mWebView.requestFocus();
		 		mWebView.reload();
	        }*/
	 	}
	 	
	 	@Override
	 	protected void onResume() {
	 	  super.onResume();
	 	 if(NoteApplication.getInstance().isLogin()){
	 	  CookieSyncManager.getInstance().stopSync();
	 	 }
	 	 mWebView.requestFocus();
	 	}


	 	@Override
	 	protected void onPause() {
	 	  super.onPause();
	 	 if(NoteApplication.getInstance().isLogin()){
	 	  CookieSyncManager.getInstance().sync();
	 	 }
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
}
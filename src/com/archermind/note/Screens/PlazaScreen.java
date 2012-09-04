package com.archermind.note.Screens;


import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.archermind.note.NoteApplication;
import com.archermind.note.R;
import com.archermind.note.Events.EventArgs;
import com.archermind.note.Events.IEventHandler;
import com.archermind.note.Services.EventService;
import com.archermind.note.Services.ServiceManager;

public class PlazaScreen extends Screen implements IEventHandler{
	
	private WebView mWebView;
	private static String url = "http://note.archermind.com/web/index.php";
	public static boolean isFirstPage = true;
	public static final EventService eventService = ServiceManager
			.getEventservice();
	
		@SuppressLint("SetJavaScriptEnabled")
		@Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	      //  setContentView(R.layout.plaza_screen);
	        //mWebView = (WebView) findViewById(R.id.plaza_page);
	        mWebView = new WebView(this);
	        setContentView(mWebView);
	        mWebView.getSettings().setJavaScriptEnabled(true); 
	        mWebView.getSettings().setBuiltInZoomControls(true);
	        mWebView.requestFocus();
	        
	        if(NoteApplication.getInstance().isLogin()){
	        CookieSyncManager.createInstance(this);
	        CookieSyncManager.getInstance().startSync();
	     //    CookieManager.getInstance().removeSessionCookie();
	       // CookieManager.getInstance().removeAllCookie();
	       // System.out.println(CookieManager.getInstance().getCookie(url) + "~~~~~~~~~~~~~~~~~~");
	        CookieManager.getInstance().setCookie(url, "userid=" + NoteApplication.getInstance().getUserId() + ";");
	        mWebView.clearCache(true);
	        mWebView.clearHistory();
	      // System.out.println(CookieManager.getInstance().getCookie(url) + "~~~~~~~~~~~~~~~~~~");
	        }

	       
	       mWebView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // TODO Auto-generated method stub
                    view.loadUrl(url);
                    view.getSettings().setJavaScriptEnabled(true);  
                    return true;
            }
         });    
	        
	        mWebView.loadUrl(url);
	        eventService.add(this);
			
		}
	 
		@Override
		public void onBackPressed() {
			// TODO Auto-generated method stub
			WebBackForwardList wl = mWebView.copyBackForwardList();
			System.out.println("wl.getCurrentIndex() : " + wl.getCurrentIndex());
			if(wl.getCurrentIndex() > 1){
	    		isFirstPage = false;
	    		mWebView.goBack();
	    	}else if(wl.getCurrentIndex() == 1){
	    		isFirstPage = true;
	    		mWebView.goBack();
	    	}else{
	    		super.onBackPressed();
	    	}
		}
		
	 	@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			System.out.println("plazascreen dispatchKeyEvent : " + event.getKeyCode() + ", " + event.getAction());
			WebBackForwardList wl = mWebView.copyBackForwardList();
			System.out.println("wl.getCurrentIndex() : " + wl.getCurrentIndex());
			if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && wl.getCurrentIndex()!= 0){
				isFirstPage = false;
			}	
	    	System.out.println(mWebView.canGoBack() + ", " + event.getRepeatCount());	    
	    	if (mWebView.canGoBack() && event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && event.getRepeatCount() == 0) { 
		    	this.onBackPressed();
		        return true;  
		    }
		    return super.dispatchKeyEvent(event);
		    
		}
	 
	 	public  void refresh(){
	 		System.out.println("===refresh===");

	        mWebView.getSettings().setJavaScriptEnabled(true); 
	        mWebView.getSettings().setBuiltInZoomControls(true);
	        mWebView.requestFocus();
	 		mWebView.reload();
	 	}
	 	
	 	@Override
	 	protected void onResume() {
	 	  super.onResume();
	 	 if(NoteApplication.getInstance().isLogin()){
	 	  CookieSyncManager.getInstance().stopSync();
	 	 }
	 	}

	 	@Override
	 	protected void onStop() {
	 	  super.onStop();
	 	  mWebView.destroy();
	 	  this.finish();
	 	  
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
				refresh();
				break;
			}
			return false;
		}
}
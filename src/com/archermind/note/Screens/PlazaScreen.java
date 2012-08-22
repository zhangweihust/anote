package com.archermind.note.Screens;


import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.archermind.note.R;

public class PlazaScreen extends Screen {
	
	private WebView mWebView;
	private static String url = "http://10.52.31.122/";
	public static boolean isFirstPage = false;
	
		@Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.plaza_screen);
	        mWebView = (WebView) findViewById(R.id.plaza_page);
	        mWebView.getSettings().setJavaScriptEnabled(true);  
	        mWebView.requestFocus();
	        
/*	        CookieSyncManager.createInstance(this);
	        CookieSyncManager.getInstance().startSync();
	        CookieManager.getInstance().removeSessionCookie();
	        CookieManager.getInstance().setCookie(url, value)
	        mWebView.clearCache(true);
	        mWebView.clearHistory();*/
	        
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
	 
	 	
	 	@Override
	 	protected void onResume() {
	 	  super.onResume();
	 	 // CookieSyncManager.getInstance().stopSync();
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
	 	 // CookieSyncManager.getInstance().sync();
	 	}
}
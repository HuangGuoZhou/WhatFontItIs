package com.advl.whatfontis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class StepTwo extends Activity {
	
	private String steptwo_html = "";
	public String deviceId ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.steptwo);
		
		WebView webview_step_two = (WebView) findViewById(R.id.webView_step_two);
		
		WebSettings webSettings = webview_step_two.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);  
		webSettings.setAppCacheMaxSize(1024*1024*12);
		webSettings.setDomStorageEnabled(true);

		
		Bundle extras = getIntent().getExtras();
		if(extras != null){
			deviceId = extras.getString("deviceId");			
		}else{
			Log.v("!!??","NU EXISTA EXTRAS ?");
		}
		
		
		Log.v("URL of the Image","http://dan.za-erz.de/android/whatfontis/r"+deviceId+".jpg");
		
  	    //webview_step_two.addJavascriptInterface(new JavaScriptInterface(this),"Android"); // webpage support ?
		//webview_step_two.setWebViewClient(new MyWebViewClient()); // <--- scoate commentul la sfarsit
		webview_step_two.setBackgroundColor(Color.BLACK);
		

		try {
			webview_step_two.loadUrl("http://m.whatfontis.com/?img=http://dan.za-erz.de/android/whatfontis/r"+deviceId+".jpg");
			Log.v("URL step2","http://m.whatfontis.com/?img=http://dan.za-erz.de/android/whatfontis/r"+deviceId+".jpg");
		} catch (Exception e) {
			Log.v("Loading Webview Failed", "Loading the webview Failed");
			Toast.makeText(StepTwo.this, "server overloaded. please try again later", 2000).show(); // --- some message...
		}

		
		
	}
	
	// ================= don't let load other url besides ofertaweb.ro SECURITY MEASURE TO BE IMPLEMENTED AT THE END	
	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (Uri.parse(url).getHost().equals("ofertaweb.ro")) {
				// This is my web site, so do not override; let my WebView load
				// the page
				return false;
			}
			// Otherwise, the link is not for a page on my site, so launch
			// another Activity that handles URLs
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
			return true;
		}
	}
	
	public class JavaScriptInterface {
		Context mContext;

		/** Instantiate the interface and set the context */
		JavaScriptInterface(Context c) {
			mContext = c;
		}

		/** Show a toast from the web page */
		public void showToast(String toast) {
			Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
		}
	}
	
}

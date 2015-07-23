package com.pazdarke.courtpocket1.activity;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnKeyListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class NewsActivity extends Activity {
	
	WebView wv_news;
	ProgressBar pb_news; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_news);
		
		AppManager.getAppManager().addActivity(this);
		
		//生成水平进度条  
        pb_news = (ProgressBar)findViewById(R.id.pb_news);
		
		wv_news=(WebView)findViewById(R.id.wv_news);
		String link =getIntent().getStringExtra("link");
		wv_news.setInitialScale(100);
		WebSettings websetting = wv_news.getSettings();
		websetting.setJavaScriptEnabled(true);
		// 实现放大或缩小
		websetting.setSupportZoom(true);
		websetting.setBuiltInZoomControls(true); //实现放大或缩小
		//实现双击放大，放大后又双击缩小
		websetting.setUseWideViewPort(true);
		wv_news.loadUrl(link);
		wv_news.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				view.loadUrl(url);
				return super.shouldOverrideUrlLoading(view, url);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				// TODO Auto-generated method stub
				super.onReceivedError(view, errorCode, description, failingUrl);
				Toast.makeText(NewsActivity.this, "网络连接失败，请检查网络！",
						Toast.LENGTH_SHORT).show();
			}
			
			
		});
		
		wv_news.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				// TODO Auto-generated method stub
				super.onProgressChanged(view, newProgress);

				if (newProgress == 0) {
					pb_news.setVisibility(View.VISIBLE);
				}
				if (newProgress == 100) {
					pb_news.setVisibility(View.GONE);
				}
				pb_news.setProgress(newProgress);
				//pb_news.postInvalidate();

			}
		});
		
		wv_news.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_BACK && wv_news.canGoBack()) {
					wv_news.goBack();
					return true; // 表明已经处理了
				} else if (keyCode == KeyEvent.KEYCODE_BACK
						&& !wv_news.canGoBack()) {
					finish();
					return true;
				}
				return false;
			}
		});
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		
		Intent i = getBaseContext().getPackageManager()
				.getLaunchIntentForPackage(getBaseContext().getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		AppManager.getAppManager().AppExit(this);
	}
	
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
}

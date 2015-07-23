package com.pazdarke.courtpocket1.activity;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

public class WelcomeActivity extends Activity {

	MyHandler myHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_welcome);

		AppManager.getAppManager().addActivity(this);

		// 获取屏幕参数，用于动态改变UI参数
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		Data.SCREENHEIGHT = metric.heightPixels;
		Data.SCREENWIDTH = metric.widthPixels;

		/*
		 * ImageView iv_welcome=(ImageView)findViewById(R.id.iv_welcome);
		 * iv_welcome.getLayoutParams().width=Data.SCREENWIDTH;
		 * iv_welcome.getLayoutParams().height=Data.SCREENHEIGHT;
		 */

		myHandler = new MyHandler();

		new Thread(r).start();

	}

	Runnable r = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				Thread.sleep(3000);
				Bundle b = new Bundle();
				Message msg = new Message();
				msg.setData(b);
				myHandler.sendMessage(msg);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			// 判断是不是第一次启动
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(WelcomeActivity.this);
			Boolean user_first = prefs.getBoolean("FIRST", true);
			if (user_first) {// 第一次
				Editor pEdit = prefs.edit();
				pEdit.putBoolean("FIRST", false);
				pEdit.commit();
				startActivity(new Intent(WelcomeActivity.this,
						GuideActivity.class));
				finish();
			} else {
				startActivity(new Intent(WelcomeActivity.this,
						MainActivity.class));
				finish();
			}

		}
	}

	public static String getDeviceInfo(Context context) {
		try {
			org.json.JSONObject json = new org.json.JSONObject();
			android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			String device_id = tm.getDeviceId();

			android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);

			String mac = wifi.getConnectionInfo().getMacAddress();
			json.put("mac", mac);

			if (TextUtils.isEmpty(device_id)) {
				device_id = mac;
			}

			if (TextUtils.isEmpty(device_id)) {
				device_id = android.provider.Settings.Secure.getString(
						context.getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);
			}

			json.put("device_id", device_id);

			return json.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	public void onDestroy() {
		super.onDestroy();
		try {
			ImageView iv_welcome = (ImageView) findViewById(R.id.iv_welcome);
			if (iv_welcome != null) {
				BitmapDrawable bd = (BitmapDrawable) iv_welcome.getDrawable();
				Bitmap bm = bd.getBitmap();
				if (bm != null & !bm.isRecycled()) {
					bm.recycle();
					bm = null;
				}
				System.gc();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// do nothing
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}

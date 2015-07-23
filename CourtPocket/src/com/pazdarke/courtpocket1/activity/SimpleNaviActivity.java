package com.pazdarke.courtpocket1.activity;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.R.id;
import com.pazdarke.courtpocket1.R.layout;
import com.pazdarke.courtpocket1.R.menu;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.pazdarke.courtpocket1.tools.map.Utils;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

public class SimpleNaviActivity extends Activity implements
		AMapNaviViewListener {

	// ����View
	private AMapNaviView mAmapAMapNaviView;
	// �Ƿ�Ϊģ�⵼��
	private boolean mIsEmulatorNavi = true;
	// ��¼���ĸ�ҳ����ת�����������ؼ�
	private int mCode = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_simple_navi);
		
		AppManager.getAppManager().addActivity(this);

		Bundle bundle = getIntent().getExtras();
		processBundle(bundle);
		init(savedInstanceState);

	}

	private void processBundle(Bundle bundle) {
		if (bundle != null) {
			mIsEmulatorNavi = bundle.getBoolean(Utils.ISEMULATOR, false);
			mCode = bundle.getInt(Utils.ACTIVITYINDEX);
		}
	}

	/**
	 * ��ʼ��
	 * 
	 * @param savedInstanceState
	 */
	@SuppressWarnings("deprecation")
	private void init(Bundle savedInstanceState) {
		
		mAmapAMapNaviView = (AMapNaviView) findViewById(R.id.simplenavimap);
		mAmapAMapNaviView.onCreate(savedInstanceState);
		mAmapAMapNaviView.setAMapNaviViewListener(this);
		//TTSController.getInstance(this).startSpeaking();
	    // ����ʵʱ����
		AMapNavi.getInstance(this).startNavi(AMapNavi.GPSNaviMode);
		
	}

	// -----------------------------��������ص��¼�------------------------
	/**
	 * �������淵�ذ�ť����
	 * */
	@Override
	public void onNaviCancel() {
		/*Intent intent = new Intent(SimpleNaviActivity.this,
				SimpleNaviRouteActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);*/
		finish();
	}

	@Override
	public void onNaviSetting() {

	}

	@Override
	public void onNaviMapMode(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNaviTurnClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNextRoadClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScanViewButtonClick() {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * ���ؼ������¼�
	 * */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mCode == Utils.SIMPLEROUTENAVI) {
				/*Intent intent = new Intent(SimpleNaviActivity.this,
						SimpleNaviRouteActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);*/
				finish();

			} else if (mCode == Utils.SIMPLEGPSNAVI) {
				/*Intent intent = new Intent(SimpleNaviActivity.this,
						SimpleGPSNaviActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);*/
				finish();
			} else {
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	// ------------------------------�������ڷ���---------------------------
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mAmapAMapNaviView.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();
		mAmapAMapNaviView.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mAmapAMapNaviView.onPause();
		AMapNavi.getInstance(this).stopNavi();
		MobclickAgent.onPause(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mAmapAMapNaviView.onDestroy();

		//TTSController.getInstance(this).stopSpeaking();

	}

	@Override
	public void onLockMap(boolean arg0) {

		// TODO Auto-generated method stub

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

}

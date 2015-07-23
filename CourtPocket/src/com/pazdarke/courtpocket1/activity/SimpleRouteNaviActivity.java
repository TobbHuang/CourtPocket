package com.pazdarke.courtpocket1.activity;

import java.util.ArrayList;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.pazdarke.courtpocket1.tools.map.MainApplication;
import com.pazdarke.courtpocket1.tools.map.Utils;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SimpleRouteNaviActivity extends Activity implements
		AMapNaviListener {
	
	ProgressDialog progressDialog;

	private TextView mStartPointTextView;
	private TextView mEndPointTextView;

	private MapView mMapView;
	private AMap mAMap;
	private AMapNavi mAMapNavi;

	private NaviLatLng mNaviStart;
	private NaviLatLng mNaviEnd;
	// 璧风偣缁堢偣鍒楄〃
	private ArrayList<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();
	private ArrayList<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();
	// 瑙勫垝绾胯矾
	private RouteOverLay mRouteOverLay;
	Intent intent;
	int mode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.activity_simple_route_navi);
			
			AppManager.getAppManager().addActivity(this);

			intent=getIntent();

			initView(savedInstanceState);
			MainApplication.getInstance().addActivity(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initView(Bundle savedInstanceState) {
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("正在规划路线...");
		progressDialog.setCancelable(true);

		progressDialog.show();

		mMapView = (MapView) findViewById(R.id.simple_route_map);
		mMapView.onCreate(savedInstanceState);
		mAMap = mMapView.getMap();
		mAMap.moveCamera(CameraUpdateFactory
				.newCameraPosition(new CameraPosition(new LatLng(Data.LATITUDE,
						Data.LONGITUDE), 15, 0, 30)));
		mRouteOverLay = new RouteOverLay(mAMap, null);

		mAMapNavi = AMapNavi.getInstance(this);
		mAMapNavi.setAMapNaviListener(this);

		mNaviStart = new NaviLatLng(Data.LATITUDE, Data.LONGITUDE);
		mNaviEnd = new NaviLatLng(Double.parseDouble(intent
				.getStringExtra("Latitude")), Double.parseDouble(intent
				.getStringExtra("Longitude")));
		mStartPoints.add(mNaviStart);
		mEndPoints.add(mNaviEnd);
		mStartPointTextView = (TextView) findViewById(R.id.start_position_textview);
		mEndPointTextView = (TextView) findViewById(R.id.end_position_textview);

		mStartPointTextView.setText(Data.ADDRESS);
		mEndPointTextView.setText(intent.getStringExtra("GymName"));

		TextView tv_title=(TextView)findViewById(R.id.tv_simpleroute_title);
		mode = intent.getIntExtra("Mode", 1);
		switch (mode) {
		case 1:
			tv_title.setText("路线规划（驾车）");
			calculateDriveRoute();
			break;
		case 2:
			tv_title.setText("路线规划（步行）");
			calculateFootRoute();
			break;
		}
		 
		TextView tv_navi=(TextView)findViewById(R.id.tv_simpleroute_navi);
		tv_navi.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mode==0){
					startGPSNavi(true);
				} else{
					startGPSNavi(false);
				}
			}
		});
		
		ImageView iv_leftarrow=(ImageView)findViewById(R.id.iv_simpleroute_leftarrow);
		iv_leftarrow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
	}

	// 计算驾车路线
	private void calculateDriveRoute() {
		boolean isSuccess = mAMapNavi.calculateDriveRoute(mStartPoints,
				mEndPoints, null, AMapNavi.DrivingDefault);
		progressDialog.dismiss();
		if (!isSuccess) {
			showToast("路线计算失败,检查参数情况");
		}

	}

	// 计算步行路线
	private void calculateFootRoute() {
		boolean isSuccess = mAMapNavi.calculateWalkRoute(mNaviStart, mNaviEnd);
		progressDialog.dismiss();
		if (!isSuccess) {
			showToast("路线计算失败,检查参数情况");
		}
	}

	private void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@SuppressWarnings("deprecation")
	private void startGPSNavi(boolean isDrive) {
		boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(
				getContentResolver(), LocationManager.GPS_PROVIDER);
		if (!gpsEnabled) {
			AlertDialog dialog = new AlertDialog.Builder(
					SimpleRouteNaviActivity.this)
					.setMessage("为了导航的准确性，请打开GPS功能")
					.setPositiveButton("前往设置GPS",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									Intent callGPSSettingIntent = new Intent(
											android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
									startActivity(callGPSSettingIntent);
								}

							})
					.setNegativeButton("直接进入导航",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									Intent gpsIntent = new Intent(
											SimpleRouteNaviActivity.this,
											SimpleNaviActivity.class);
									Bundle bundle = new Bundle();
									bundle.putBoolean(Utils.ISEMULATOR, false);
									bundle.putInt(Utils.ACTIVITYINDEX,
											Utils.SIMPLEROUTENAVI);
									gpsIntent.putExtras(bundle);
									gpsIntent
											.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
									startActivity(gpsIntent);
								}
							}).create();
			dialog.show();
		} else{
			Intent gpsIntent = new Intent(
					SimpleRouteNaviActivity.this,
					SimpleNaviActivity.class);
			Bundle bundle = new Bundle();
			bundle.putBoolean(Utils.ISEMULATOR, false);
			bundle.putInt(Utils.ACTIVITYINDEX,
					Utils.SIMPLEROUTENAVI);
			gpsIntent.putExtras(bundle);
			gpsIntent
					.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(gpsIntent);
		}

	}
	 
	// -------------------------返回键监听事件---------------------------------

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(SimpleRouteNaviActivity.this,
					CourtinfoActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			finish();
			MainApplication.getInstance().deleteActivity(this);

		}
		return super.onKeyDown(keyCode, event);
	}

	// --------------------导航监听回调事件-----------------------------
	@Override
	public void onArriveDestination() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onArrivedWayPoint(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCalculateRouteFailure(int arg0) {
		showToast("路径规划出错" + arg0);
	}

	@Override
	public void onCalculateRouteSuccess() {
		AMapNaviPath naviPath = mAMapNavi.getNaviPath();
		if (naviPath == null) {
			return;
		}
		// 获取路径规划线路，显示到地图上
		mRouteOverLay.setRouteInfo(naviPath);
		mRouteOverLay.addToMap();
	}

	@Override
	public void onEndEmulatorNavi() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGetNavigationText(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGpsOpenStatus(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInitNaviFailure() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInitNaviSuccess() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChange(AMapNaviLocation arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNaviInfoUpdated(AMapNaviInfo arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReCalculateRouteForTrafficJam() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReCalculateRouteForYaw() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStartNavi(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTrafficStatusUpdate() {
		// TODO Auto-generated method stub

	}

	// ------------------生命周期重写函数---------------------------

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
		MobclickAgent.onResume(this);

	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
		MobclickAgent.onPause(this);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
		// 删除监听
		AMapNavi.getInstance(this).removeAMapNaviListener(this);

	}

	@Override
	public void onNaviInfoUpdate(NaviInfo arg0) {

		// TODO Auto-generated method stub

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

}

package com.pazdarke.courtpocket1.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.AMap.InfoWindowAdapter;
import com.amap.api.maps2d.AMap.OnInfoWindowClickListener;
import com.amap.api.maps2d.AMap.OnMarkerClickListener;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.pazdarke.courtpocket1.tools.listview.AsyncViewTask;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class NearbyMapActivity extends Activity implements LocationSource,
		AMapLocationListener, OnInfoWindowClickListener, InfoWindowAdapter,
		OnMarkerClickListener {

	private MapView mapView;
	private AMap aMap;
	private OnLocationChangedListener mListener;
	private LocationManagerProxy mAMapLocationManager;

	RelativeLayout rl_leftarrow;

	int sports;// 0是全部，1是足球,2是篮球

	boolean hasOverlay = false;

	Spinner spinner;
	JSONObject gymID;
	JSONObject overlayInfo;// 用于infowindow的信息检索和点击事件

	TimeoutHandler timeoutHandler;
	ListHandler listHandler;

	ExecutorService pool;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_nearby_map);

		AppManager.getAppManager().addActivity(this);

		pool = Executors.newFixedThreadPool(10);

		sports = this.getIntent().getExtras().getInt("sports", 0);

		mapView = (MapView) findViewById(R.id.map_nearby_map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写

		rl_leftarrow = (RelativeLayout) findViewById(R.id.rl_map_leftarrow);
		rl_leftarrow.setOnClickListener(onClickListener);

		init();

		init_spinner();

		timeoutHandler = new TimeoutHandler();
		listHandler = new ListHandler();

		new Thread(r_RequestList).start();

	}

	@SuppressLint("NewApi")
	private void init_spinner() {
		// TODO Auto-generated method stub
		ArrayList<String> list = new ArrayList<String>();
		list.add("所有运动");
		list.add("足球");
		// list.add("篮球");
		// list.add("排球");
		// list.add("乒乓球");
		list.add("网球");
		// list.add("桌球");
		list.add("游泳");
		list.add("羽毛球");
		// list.add("健身");
		// list.add("极限运动");
		// list.add("高尔夫");
		// list.add("更多运动");

		spinner = (Spinner) findViewById(R.id.sp_map_sports);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.layout_map_spinner, R.id.tv_map_spinnerlayout, list) {
			@Override
			public View getDropDownView(int position, View convertView,
					ViewGroup parent) {
				// TODO Auto-generated method stub
				if (convertView == null) {
					convertView = getLayoutInflater().inflate(
							R.layout.item_courtlist_spinner, parent, false);
				}
				TextView label = (TextView) convertView
						.findViewById(R.id.tv_courtlistitem);
				label.setText(getItem(position));
				label.setTextColor(spinner.getSelectedItemPosition() == position ? getResources()
						.getColor(R.color.blue) : getResources().getColor(
						R.color.darkGrey));
				ImageView icon = (ImageView) convertView
						.findViewById(R.id.iv_courtlistitem);

				switch (position) {
				case 0:
					icon.setImageDrawable(spinner.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_allsports2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_allsports1));
					break;
				case 1:
					icon.setImageDrawable(spinner.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_soccer2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_soccer1));
					break;
				case 2:
					icon.setImageDrawable(spinner.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_tennis2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_tennis1));
					break;
				case 3:
					icon.setImageDrawable(spinner.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_swimming2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_swimming1));
					break;
				case 4:
					icon.setImageDrawable(spinner.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_badminton2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_badminton1));
					break;
				/*
				 * case 5:
				 * icon.setImageDrawable(spinner.getSelectedItemPosition() ==
				 * position ? getResources()
				 * .getDrawable(R.drawable.ic_courtlist_tennis2) :
				 * getResources().getDrawable(
				 * R.drawable.ic_courtlist_tennis1)); break; case 6:
				 * icon.setImageDrawable(spinner.getSelectedItemPosition() ==
				 * position ? getResources()
				 * .getDrawable(R.drawable.ic_courtlist_badminton2) :
				 * getResources().getDrawable(
				 * R.drawable.ic_courtlist_badminton1)); break; case 7:
				 * icon.setImageDrawable(spinner.getSelectedItemPosition() ==
				 * position ? getResources()
				 * .getDrawable(R.drawable.ic_courtlist_billiards2) :
				 * getResources().getDrawable(
				 * R.drawable.ic_courtlist_billiards1)); break; case 8:
				 * icon.setImageDrawable(spinner.getSelectedItemPosition() ==
				 * position ? getResources()
				 * .getDrawable(R.drawable.ic_courtlist_swimming2) :
				 * getResources().getDrawable(
				 * R.drawable.ic_courtlist_swimming1)); break; case 9:
				 * icon.setImageDrawable(spinner.getSelectedItemPosition() ==
				 * position ? getResources()
				 * .getDrawable(R.drawable.ic_courtlist_bodybuilding2) :
				 * getResources().getDrawable(
				 * R.drawable.ic_courtlist_bodybuilding1)); break; case 10:
				 * icon.setImageDrawable(spinner.getSelectedItemPosition() ==
				 * position ? getResources()
				 * .getDrawable(R.drawable.ic_courtlist_xgames2) :
				 * getResources().getDrawable(
				 * R.drawable.ic_courtlist_xgames1)); break; case 11:
				 * icon.setImageDrawable(spinner.getSelectedItemPosition() ==
				 * position ? getResources()
				 * .getDrawable(R.drawable.ic_courtlist_golf2) :
				 * getResources().getDrawable( R.drawable.ic_courtlist_golf1));
				 * break; case 12:
				 * icon.setImageDrawable(spinner.getSelectedItemPosition() ==
				 * position ? getResources()
				 * .getDrawable(R.drawable.ic_courtlist_moresports2) :
				 * getResources().getDrawable(
				 * R.drawable.ic_courtlist_moresports1)); break;
				 */
				}

				return convertView;
			}
		};

		/*
		 * Animation scaleAnimation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f,
		 * Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
		 * //设置动画时间 scaleAnimation.setDuration(500);
		 */

		spinner.setDropDownWidth(Data.SCREENWIDTH);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				aMap.clear();

				// 上面那个clear方法把定位的蓝色图标也删掉了，这里重新添加一下
				MyLocationStyle myLocationStyle = new MyLocationStyle();
				myLocationStyle.myLocationIcon(BitmapDescriptorFactory
						.fromResource(R.drawable.location_marker));// 设置小蓝点的图标
				myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
				myLocationStyle.radiusFillColor(Color.argb(100, 204, 204, 204));// 设置圆形的填充颜色
				myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
				aMap.setMyLocationStyle(myLocationStyle);

				new Thread(r_RequestList).start();
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
			}
		});

		// spinner.setSelection(this.getIntent().getIntExtra("sports", 0));
		switch (this.getIntent().getIntExtra("sports", 0)) {
		case 1:
			spinner.setSelection(1);
			break;
		case 5:
			spinner.setSelection(2);
			break;
		case 6:
			spinner.setSelection(4);
			break;
		case 8:
			spinner.setSelection(3);
			break;
		}
	}

	/**
	 * 初始化AMap对象
	 */
	private void init() {

		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
	}

	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {
		// 自定义系统定位小蓝点
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory
				.fromResource(R.drawable.location_marker));// 设置小蓝点的图标
		myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
		myLocationStyle.radiusFillColor(Color.argb(100, 204, 204, 204));// 设置圆形的填充颜色
		myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
		aMap.setMyLocationStyle(myLocationStyle);
		aMap.setLocationSource(this);// 设置定位监听
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		aMap.moveCamera(CameraUpdateFactory.zoomTo(15));

		aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
		aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式
		aMap.setOnMarkerClickListener(this);

	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
		MobclickAgent.onResume(this);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
		deactivate();
		MobclickAgent.onPause(this);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	/**
	 * 此方法已经废弃
	 */
	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	/**
	 * 定位成功后回调函数
	 */
	@Override
	public void onLocationChanged(AMapLocation aLocation) {
		if (mListener != null && aLocation != null) {
			mListener.onLocationChanged(aLocation);// 显示系统小蓝点
		}
	}

	/**
	 * 激活定位
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			/*
			 * mAMapLocManager.setGpsEnable(false);
			 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
			 * API定位采用GPS和网络混合定位方式
			 * ，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
			 */
			mAMapLocationManager.requestLocationUpdates(
					LocationProviderProxy.AMapNetwork, 2000, 10, this);
		}
	}

	/**
	 * 停止定位
	 */
	@Override
	public void deactivate() {
		mListener = null;
		if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destory();
		}
		mAMapLocationManager = null;
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.rl_map_leftarrow:
				finish();
				break;
			}
		}
	};

	void showOverlay(double Latitude, double Longitude, int gymID, String sports) {
		MarkerOptions markerOption = new MarkerOptions();
		markerOption.position(new LatLng(Latitude, Longitude));
		markerOption.title(gymID + "");
		markerOption.draggable(false);

		int type = Integer.valueOf(sports).intValue();
		int icon = 0;
		switch (type) {
		case 1:
			icon = R.drawable.ic_map_soccer;
			break;
		case 2:
			icon = R.drawable.ic_map_basketball;
			break;
		case 3:
			icon = R.drawable.ic_map_volleyball;
			break;
		case 4:
			icon = R.drawable.ic_map_tabletennis;
			break;
		case 5:
			icon = R.drawable.ic_map_tennis;
			break;
		case 6:
			icon = R.drawable.ic_map_badminton;
			break;
		case 7:
			icon = R.drawable.ic_map_billiards;
			break;
		case 8:
			icon = R.drawable.ic_map_swimming;
			break;
		case 9:
			icon = R.drawable.ic_map_bodybuilding;
			break;
		case 10:
			icon = R.drawable.ic_map_xgames;
			break;
		case 11:
			icon = R.drawable.ic_map_golf;
			break;
		case 12:
			icon = R.drawable.ic_map_moresports;
			break;
		}

		markerOption.icon(BitmapDescriptorFactory.fromResource(icon));

		aMap.addMarker(markerOption);
	}

	@Override
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		// TODO Auto-generated method stub
		View infoWindow = getLayoutInflater().inflate(
				R.layout.layout_map_infowindow, null);
		render(marker, infoWindow);
		return infoWindow;
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		// TODO Auto-generated method stub
		/*
		 * Toast.makeText(NearbyMapActivity.this, "你点击的是" + arg0.getTitle(),
		 * Toast.LENGTH_SHORT).show();
		 */
		Intent intent = new Intent(NearbyMapActivity.this,
				CourtinfoActivity.class);
		intent.putExtra("ID", marker.getTitle());
		intent.putExtra("gymType", spinner.getSelectedItemPosition() + "");
		startActivity(intent);

	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub

		hasOverlay = true;

		List<Marker> marker = aMap.getMapScreenMarkers();
		for (int i = 0; i < marker.size(); i++) {
			marker.get(i).hideInfoWindow();
		}

		arg0.showInfoWindow();

		return true;
	}

	/**
	 * 自定义infowinfow窗口
	 */
	public void render(Marker marker, View view) {
		String title = marker.getTitle();
		// 在这里绘制每一个marker的infowindow = =
		try {
			JSONObject json = new JSONObject(overlayInfo.getString(title));
			ImageView gymlogo = (ImageView) view
					.findViewById(R.id.iv_mapinfowindow_gymlogo);
			TextView name = (TextView) view
					.findViewById(R.id.tv_mapinfowindow_gymname);
			TextView location = (TextView) view
					.findViewById(R.id.tv_mapinfowindow_gymlocation);
			TextView price = (TextView) view
					.findViewById(R.id.tv_mapinfowindow_gymprice);

			name.setText(json.getString("GymName"));
			location.setText(json.getString("GymLocation"));
			price.setText("￥" + Data.doubleTrans(json.getDouble("Price")));

			if (json.has("Path")) {
				new AsyncViewTask(NearbyMapActivity.this,
						json.getString("Path"), gymlogo, 5).execute(gymlogo);// 异步加载图片
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (hasOverlay) {

				List<Marker> marker = aMap.getMapScreenMarkers();
				for (int i = 0; i < marker.size(); i++) {
					marker.get(i).hideInfoWindow();
				}

				hasOverlay = false;
			} else {
				finish();
			}
		}

		return false;

	}

	Runnable r_RequestList = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			overlayInfo = new JSONObject();

			int type = 0;
			switch (spinner.getSelectedItemPosition()) {
			case 0:
				type = 0;
				break;
			case 1:
				type = 1;
				break;
			case 2:
				type = 5;
				break;
			case 3:
				type = 8;
				break;
			case 4:
				type = 6;
				break;
			}

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "RequestListByType"));
			params.add(new BasicNameValuePair("Type", type + ""));
			params.add(new BasicNameValuePair("City", Data.CITY));
			params.add(new BasicNameValuePair("Order", "0"));
			params.add(new BasicNameValuePair("Longitude", Data.LONGITUDE + ""));
			params.add(new BasicNameValuePair("Latitude", Data.LATITUDE + ""));

			try {

				String result = new HttpPostConnection("GymInfoServer", params)
						.httpConnection();

				if (result.equals("timeout")) {
					Message msg = new Message();
					Bundle b = new Bundle();
					msg.setData(b);
					timeoutHandler.sendMessage(msg);
				} else {

					gymID = new JSONObject(result);
					new Thread(r_RequestAllGyms).start();
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};

	Runnable r_RequestAllGyms = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				for (int i = 0; i < gymID.getInt("GymNum"); i++) {
					pool.execute(new GymThread(i));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

	class GymThread extends Thread {

		int i;

		GymThread(int i) {
			this.i = i;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("Request", "RequestByID"));
				params.add(new BasicNameValuePair("GymDetailLevel", "1"));
				params.add(new BasicNameValuePair("GymID", gymID.getInt("GymID"
						+ i)
						+ ""));

				String result = new HttpPostConnection("GymInfoServer", params)
						.httpConnection();

				Message msg = new Message();
				Bundle b = new Bundle();

				if (result.equals("timeout")) {
					msg.setData(b);
					timeoutHandler.sendMessage(msg);
					return;
				}

				JSONObject json = new JSONObject(result);

				b.putString("info", json.toString());
				b.putInt("ID", gymID.getInt("GymID" + i));
				msg.setData(b);
				listHandler.sendMessage(msg);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class ListHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();
			try {
				JSONObject json = new JSONObject(b.getString("info"));
				showOverlay(json.getDouble("GymLatitude"),
						json.getDouble("GymLongitude"), b.getInt("ID"),
						json.getString("GymMainType"));

				// ID作为标识符，存储gym信息
				overlayInfo.put(b.getInt("ID") + "", b.getString("info"));

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	class TimeoutHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Toast.makeText(NearbyMapActivity.this, "网络出问题了", Toast.LENGTH_SHORT)
					.show();

		}
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

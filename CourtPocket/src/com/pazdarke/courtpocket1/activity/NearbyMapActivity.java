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

	int sports;// 0��ȫ����1������,2������

	boolean hasOverlay = false;

	Spinner spinner;
	JSONObject gymID;
	JSONObject overlayInfo;// ����infowindow����Ϣ�����͵���¼�

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
		mapView.onCreate(savedInstanceState);// �˷���������д

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
		list.add("�����˶�");
		list.add("����");
		// list.add("����");
		// list.add("����");
		// list.add("ƹ����");
		list.add("����");
		// list.add("����");
		list.add("��Ӿ");
		list.add("��ë��");
		// list.add("����");
		// list.add("�����˶�");
		// list.add("�߶���");
		// list.add("�����˶�");

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
		 * //���ö���ʱ�� scaleAnimation.setDuration(500);
		 */

		spinner.setDropDownWidth(Data.SCREENWIDTH);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				aMap.clear();

				// �����Ǹ�clear�����Ѷ�λ����ɫͼ��Ҳɾ���ˣ������������һ��
				MyLocationStyle myLocationStyle = new MyLocationStyle();
				myLocationStyle.myLocationIcon(BitmapDescriptorFactory
						.fromResource(R.drawable.location_marker));// ����С�����ͼ��
				myLocationStyle.strokeColor(Color.BLACK);// ����Բ�εı߿���ɫ
				myLocationStyle.radiusFillColor(Color.argb(100, 204, 204, 204));// ����Բ�ε������ɫ
				myLocationStyle.strokeWidth(1.0f);// ����Բ�εı߿��ϸ
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
	 * ��ʼ��AMap����
	 */
	private void init() {

		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
	}

	/**
	 * ����һЩamap������
	 */
	private void setUpMap() {
		// �Զ���ϵͳ��λС����
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory
				.fromResource(R.drawable.location_marker));// ����С�����ͼ��
		myLocationStyle.strokeColor(Color.BLACK);// ����Բ�εı߿���ɫ
		myLocationStyle.radiusFillColor(Color.argb(100, 204, 204, 204));// ����Բ�ε������ɫ
		myLocationStyle.strokeWidth(1.0f);// ����Բ�εı߿��ϸ
		aMap.setMyLocationStyle(myLocationStyle);
		aMap.setLocationSource(this);// ���ö�λ����
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// ����Ĭ�϶�λ��ť�Ƿ���ʾ
		aMap.setMyLocationEnabled(true);// ����Ϊtrue��ʾ��ʾ��λ�㲢�ɴ�����λ��false��ʾ���ض�λ�㲢���ɴ�����λ��Ĭ����false
		aMap.moveCamera(CameraUpdateFactory.zoomTo(15));

		aMap.setOnInfoWindowClickListener(this);// ���õ��infoWindow�¼�������
		aMap.setInfoWindowAdapter(this);// �����Զ���InfoWindow��ʽ
		aMap.setOnMarkerClickListener(this);

	}

	/**
	 * ����������д
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
		MobclickAgent.onResume(this);
	}

	/**
	 * ����������д
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
		deactivate();
		MobclickAgent.onPause(this);
	}

	/**
	 * ����������д
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * ����������д
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	/**
	 * �˷����Ѿ�����
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
	 * ��λ�ɹ���ص�����
	 */
	@Override
	public void onLocationChanged(AMapLocation aLocation) {
		if (mListener != null && aLocation != null) {
			mListener.onLocationChanged(aLocation);// ��ʾϵͳС����
		}
	}

	/**
	 * ���λ
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			/*
			 * mAMapLocManager.setGpsEnable(false);
			 * 1.0.2�汾��������������true��ʾ��϶�λ�а���gps��λ��false��ʾ�����綨λ��Ĭ����true Location
			 * API��λ����GPS�������϶�λ��ʽ
			 * ����һ�������Ƕ�λprovider���ڶ�������ʱ�������2000���룬������������������λ���ף����ĸ������Ƕ�λ������
			 */
			mAMapLocationManager.requestLocationUpdates(
					LocationProviderProxy.AMapNetwork, 2000, 10, this);
		}
	}

	/**
	 * ֹͣ��λ
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
		 * Toast.makeText(NearbyMapActivity.this, "��������" + arg0.getTitle(),
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
	 * �Զ���infowinfow����
	 */
	public void render(Marker marker, View view) {
		String title = marker.getTitle();
		// ���������ÿһ��marker��infowindow = =
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
			price.setText("��" + Data.doubleTrans(json.getDouble("Price")));

			if (json.has("Path")) {
				new AsyncViewTask(NearbyMapActivity.this,
						json.getString("Path"), gymlogo, 5).execute(gymlogo);// �첽����ͼƬ
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

				// ID��Ϊ��ʶ�����洢gym��Ϣ
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

			Toast.makeText(NearbyMapActivity.this, "�����������", Toast.LENGTH_SHORT)
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

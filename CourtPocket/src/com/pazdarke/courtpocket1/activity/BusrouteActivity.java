package com.pazdarke.courtpocket1.activity;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.AMap.InfoWindowAdapter;
import com.amap.api.maps2d.AMap.OnInfoWindowClickListener;
import com.amap.api.maps2d.AMap.OnMapClickListener;
import com.amap.api.maps2d.AMap.OnMarkerClickListener;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.overlay.BusRouteOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.BusRouteQuery;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.WalkRouteResult;
import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

public class BusrouteActivity extends Activity implements OnMarkerClickListener,
OnMapClickListener, OnInfoWindowClickListener, InfoWindowAdapter,
 OnRouteSearchListener {
	
	ProgressDialog progressDialog;

	private AMap aMap;
	private MapView mapView;
	
	private int busMode = RouteSearch.BusDefault;// ����Ĭ��ģʽ
	private BusRouteResult busRouteResult;// ����ģʽ��ѯ���
	private LatLonPoint startPoint = null;
	private LatLonPoint endPoint = null;
	
	private RouteSearch routeSearch;
	public ArrayAdapter<String> aAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_busroute);
		
		AppManager.getAppManager().addActivity(this);
		
		ImageView iv_leftarrow=(ImageView)findViewById(R.id.iv_busroute_leftarrow);
		iv_leftarrow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		progressDialog=new ProgressDialog(this);
		progressDialog.setMessage("�����������ҹ�����Ϣ...");
		progressDialog.setCancelable(false);
		
		mapView = (MapView) findViewById(R.id.map_busroute);
		mapView.onCreate(savedInstanceState);// �˷���������д
		init();
		
		searchRoute();
		
	}
	
	/**
	 * ��ʼ��AMap����
	 */
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
			//registerListener();
		}
		routeSearch = new RouteSearch(this);
		routeSearch.setRouteSearchListener(this);
		aMap.moveCamera(CameraUpdateFactory
				.newCameraPosition(new CameraPosition(new LatLng(Data.LATITUDE,
						Data.LONGITUDE), 15, 0, 30)));
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
	 * ��ʼRoute����
	 */
	public void searchRoute() {
		startPoint = new LatLonPoint(Data.LATITUDE, Data.LONGITUDE);
		endPoint = new LatLonPoint(Double.parseDouble(BusrouteActivity.this
				.getIntent().getStringExtra("Latitude")),
				Double.parseDouble(BusrouteActivity.this.getIntent()
						.getStringExtra("Longitude")));
		progressDialog.show();
		searchRouteResult(startPoint,endPoint);
	}
	
	/**
	 * ��ʼ����·���滮����
	 */
	public void searchRouteResult(LatLonPoint startPoint, LatLonPoint endPoint) {
		final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
				startPoint, endPoint);
			BusRouteQuery query = new BusRouteQuery(fromAndTo, busMode, "�ɶ�", 0);// ��һ��������ʾ·���滮�������յ㣬�ڶ���������ʾ������ѯģʽ��������������ʾ������ѯ�������ţ����ĸ�������ʾ�Ƿ����ҹ�೵��0��ʾ������
			routeSearch.calculateBusRouteAsyn(query);// �첽·���滮����ģʽ��ѯ
	}

	/**
	 * ����·�߲�ѯ�ص�
	 */
	@Override
	public void onBusRouteSearched(BusRouteResult result, int rCode) {
		progressDialog.dismiss();
		if (rCode == 0) {
			if (result != null && result.getPaths() != null
					&& result.getPaths().size() > 0) {
				busRouteResult = result;
				BusPath busPath = busRouteResult.getPaths().get(0);
				aMap.clear();// �����ͼ�ϵ����и�����
				BusRouteOverlay routeOverlay = new BusRouteOverlay(this, aMap,
						busPath, busRouteResult.getStartPos(),
						busRouteResult.getTargetPos());
				routeOverlay.removeFromMap();
				routeOverlay.addToMap();
				routeOverlay.zoomToSpan();
			} else {
				Toast.makeText(BusrouteActivity.this, "û���ҵ�������(�Уߩ�)",
						Toast.LENGTH_SHORT).show();
			}
		} else if (rCode == 27) {
			Toast.makeText(BusrouteActivity.this, "����ʧ��,�����������ӣ�",
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(BusrouteActivity.this, "δ֪�������Ժ�����!������Ϊ" + rCode,
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onDriveRouteSearched(DriveRouteResult arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWalkRouteSearched(WalkRouteResult arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onInfoWindowClick(Marker arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub
		return false;
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

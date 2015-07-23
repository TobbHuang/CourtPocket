package com.pazdarke.courtpocket1.activity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.ant.liao.GifView;
import com.ant.liao.GifView.GifImageType;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.pazdarke.courtpocket1.tools.listview.CourtlistAdapter;
import com.pazdarke.courtpocket1.view.AlwaysMarqueeTextView;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CourtlistActivity extends Activity implements
		AMapLocationListener, Runnable {

	RelativeLayout rl_gifcontainer;

	ImageView iv_leftarrow;

	ListView lv_list;
	PullToRefreshListView mPullToRefreshListView;
	ArrayList<HashMap<String, Object>> mylist;
	JSONObject courtInfo;
	CourtlistAdapter adapter;

	private LocationManagerProxy aMapLocManager = null;
	private AMapLocation aMapLocation;// 用于判断定位超时
	private Handler handler = new Handler();
	TextView tv_locationtitle;
	AlwaysMarqueeTextView tv_location;
	ImageView iv_refresh;
	int locationTime = 0;// 记录刷新次数

	Spinner spinner_sports, spinner_location, spinner_order;

	View footView;

	boolean firstLaunched1 = true, firstLaunched2 = true,
			firstLaunched3 = true;

	JSONObject gymID;
	int sumPage = 0, currentPage = 0, lastPageItemNum;
	boolean hasMore = true;
	int courtLoadNum;// 请求item为异步，判断是否加载完毕

	CourtlistHandler courtlistHandler;
	ListHandler listHandler;
	AddFootviewHandler addFootviewHandler;
	TimeoutHandler timeoutHandler;

	ExecutorService pool;

	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_courtlist);

		AppManager.getAppManager().addActivity(this);

		pool = Executors.newFixedThreadPool(10);

		iv_leftarrow = (ImageView) findViewById(R.id.iv_courtlist_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		RelativeLayout rl_searchbar = (RelativeLayout) findViewById(R.id.rl_courtlist_searchbar);
		rl_searchbar.setOnClickListener(onClickListener);

		RelativeLayout rl_location = (RelativeLayout) findViewById(R.id.rl_courtlist_map);
		rl_location.setOnClickListener(onClickListener);

		/*
		 * rl_sportsspinner=(RelativeLayout)findViewById(R.id.
		 * rl_courtlist_sportsspinner);
		 * rl_sportsspinner.setOnClickListener(onClickListener);
		 */
		/*
		 * tv_sportsspinner=(TextView)findViewById(R.id.tv_courtlist_sportsspinner
		 * );
		 * iv_sportsspinner=(ImageView)findViewById(R.id.iv_courtlist_sportsspinner
		 * );
		 */

		mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_courtlist_list);
		mPullToRefreshListView
				.setOnRefreshListener(new OnRefreshListener<ListView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						String label = DateUtils.formatDateTime(
								getApplicationContext(),
								System.currentTimeMillis(),
								DateUtils.FORMAT_SHOW_TIME
										| DateUtils.FORMAT_SHOW_DATE
										| DateUtils.FORMAT_ABBREV_ALL);

						// Update the LastUpdatedLabel
						refreshView.getLoadingLayoutProxy()
								.setLastUpdatedLabel(label);

						// Do work to refresh the list here.
						lv_list.removeFooterView(footView);
						lv_list.addFooterView(footView);
						new GetDataTask().execute();
					}
				});

		lv_list = mPullToRefreshListView.getRefreshableView();
		// lv_list.setDivider(null);
		footView = getLayoutInflater().inflate(
				R.layout.layout_courtlist_footview, null);
		lv_list.addFooterView(footView);

		mylist = new ArrayList<HashMap<String, Object>>();
		mylist.clear();

		init_list();
		init_gif();

		tv_locationtitle = (TextView) findViewById(R.id.tv_courtlist_locationtitle);
		tv_location = (AlwaysMarqueeTextView) findViewById(R.id.tv_courtlist_location);
		iv_refresh = (ImageView) findViewById(R.id.iv_courtlist_refresh);
		iv_refresh.setOnClickListener(onClickListener);

		// 定位
		location();

		init_spinner_sports();
		init_spinner_location();
		init_spinner_order();

		courtlistHandler=new CourtlistHandler();
		listHandler = new ListHandler();
		addFootviewHandler=new AddFootviewHandler();
		timeoutHandler = new TimeoutHandler();

	}

	void init_list() {
		try {
			adapter = new CourtlistAdapter(CourtlistActivity.this, mylist,
					R.layout.item_court, new String[] { "courtlogo",
							"courtname", "rate", "price", "location", "meters",
							"service1", "service2", "service3", "service4",
							"service5", "service6" }, new int[] {
							R.id.iv_courtitem_courtlogo,
							R.id.tv_courtitem_courtname,
							R.id.tv_courtitem_rate, R.id.tv_courtitem_price,
							R.id.tv_courtitem_location,
							R.id.tv_courtitem_meters,
							R.id.iv_courtitem_service1,
							R.id.iv_courtitem_service2,
							R.id.iv_courtitem_service3,
							R.id.iv_courtitem_service4,
							R.id.iv_courtitem_service5,
							R.id.iv_courtitem_service6 });

			lv_list.setAdapter(adapter);
			lv_list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(CourtlistActivity.this,
							CourtinfoActivity.class);
					try {
						intent.putExtra("ID",
								gymID.getInt("GymID" + (position - 1)) + "");

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					startActivity(intent);
				}

			});

			lv_list.setOnScrollListener(new OnScrollListener() {

				@Override
				public void onScrollStateChanged(AbsListView view,
						int scrollState) {
					// TODO Auto-generated method stub
					// 当不滚动时
					if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
						// 判断是否滚动到底部
						if (view.getLastVisiblePosition() == view.getCount() - 1) {
							// 加载更多功能的代码
							if (hasMore) {
								new Thread(r_RequestEachPage).start();
							} else {
								lv_list.removeFooterView(footView);
							}
						}
					}
				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					// TODO Auto-generated method stub

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("deprecation")
	void location() {
		// 定位
		aMapLocManager = LocationManagerProxy.getInstance(this);

		/*
		 * mAMapLocManager.setGpsEnable(false);//
		 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
		 * API定位采用GPS和网络混合定位方式
		 * ，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
		 */

		aMapLocation = null;

		aMapLocManager.requestLocationUpdates(
				LocationProviderProxy.AMapNetwork, 2000, 10, this);
		handler.postDelayed(this, 5000);// 设置超过5秒还没有定位到就停止定位
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_courtlist_leftarrow:
				finish();
				break;
			case R.id.rl_courtlist_searchbar:
				Intent intent = new Intent(CourtlistActivity.this,
						SearchActivity.class);
				startActivity(intent);
				break;
			case R.id.rl_courtlist_map:
				Intent intent1 = new Intent(CourtlistActivity.this,
						NearbyMapActivity.class);
				intent1.putExtra("sports",
						spinner_sports.getSelectedItemPosition());
				startActivity(intent1);
				break;
			case R.id.iv_courtlist_refresh:
				tv_location.setText("正在重新定位...");
				tv_locationtitle.setText("");
				location();
				break;
			}
		}
	};

	@SuppressLint("NewApi")
	void init_spinner_sports() {
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

		spinner_sports = (Spinner) findViewById(R.id.sp_courtlist_sports);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.layout_courtlist_spinner, R.id.tv_spinnerlayout, list) {
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
				label.setTextColor(spinner_sports.getSelectedItemPosition() == position ? getResources()
						.getColor(R.color.blue) : getResources().getColor(
						R.color.darkGrey));
				ImageView icon = (ImageView) convertView
						.findViewById(R.id.iv_courtlistitem);

				switch (position) {
				case 0:
					icon.setImageDrawable(spinner_sports
							.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_allsports2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_allsports1));
					break;
				case 1:
					icon.setImageDrawable(spinner_sports
							.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_soccer2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_soccer1));
					break;
				case 2:
					icon.setImageDrawable(spinner_sports
							.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_tennis2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_tennis1));
					break;
				case 3:
					icon.setImageDrawable(spinner_sports
							.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_swimming2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_swimming1));
					break;
				case 4:
					icon.setImageDrawable(spinner_sports
							.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_badminton2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_badminton1));
					break;
				/*
				 * case 5: icon.setImageDrawable(spinner_sports
				 * .getSelectedItemPosition() == position ? getResources()
				 * .getDrawable(R.drawable.ic_courtlist_tennis2) :
				 * getResources().getDrawable(
				 * R.drawable.ic_courtlist_tennis1)); break; case 6:
				 * icon.setImageDrawable(spinner_sports
				 * .getSelectedItemPosition() == position ? getResources()
				 * .getDrawable(R.drawable.ic_courtlist_badminton2) :
				 * getResources().getDrawable(
				 * R.drawable.ic_courtlist_badminton1)); break; case 7:
				 * icon.setImageDrawable(spinner_sports
				 * .getSelectedItemPosition() == position ? getResources()
				 * .getDrawable(R.drawable.ic_courtlist_billiards2) :
				 * getResources().getDrawable(
				 * R.drawable.ic_courtlist_billiards1)); break; case 8:
				 * icon.setImageDrawable(spinner_sports
				 * .getSelectedItemPosition() == position ? getResources()
				 * .getDrawable(R.drawable.ic_courtlist_swimming2) :
				 * getResources().getDrawable(
				 * R.drawable.ic_courtlist_swimming1)); break; case 9:
				 * icon.setImageDrawable(spinner_sports
				 * .getSelectedItemPosition() == position ? getResources()
				 * .getDrawable(R.drawable.ic_courtlist_bodybuilding2) :
				 * getResources().getDrawable(
				 * R.drawable.ic_courtlist_bodybuilding1)); break; case 10:
				 * icon.setImageDrawable(spinner_sports
				 * .getSelectedItemPosition() == position ? getResources()
				 * .getDrawable(R.drawable.ic_courtlist_xgames2) :
				 * getResources().getDrawable(
				 * R.drawable.ic_courtlist_xgames1)); break; case 11:
				 * icon.setImageDrawable(spinner_sports
				 * .getSelectedItemPosition() == position ? getResources()
				 * .getDrawable(R.drawable.ic_courtlist_golf2) :
				 * getResources().getDrawable( R.drawable.ic_courtlist_golf1));
				 * break; case 12: icon.setImageDrawable(spinner_sports
				 * .getSelectedItemPosition() == position ? getResources()
				 * .getDrawable(R.drawable.ic_courtlist_moresports2) :
				 * getResources().getDrawable(
				 * R.drawable.ic_courtlist_moresports1)); break;
				 */
				}

				return convertView;
			}
		};

		spinner_sports.setDropDownWidth(Data.SCREENWIDTH);
		spinner_sports.setAdapter(adapter);
		spinner_sports
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// TODO Auto-generated method stub
						if (!firstLaunched1) {
							// 初始化各种参数，加载动画设置可见，启动加载线程，同下
							currentPage = 0;
							sumPage = 0;
							hasMore = true;
							rl_gifcontainer.setVisibility(View.VISIBLE);
							init_list();
							lv_list.removeFooterView(footView);
							new Thread(r_RequestList).start();
						} else {
							firstLaunched1 = false;
						}
					}

					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub
					}
				});

		// spinner_sports.setSelection(this.getIntent().getIntExtra("sports",
		// 0));
		switch (this.getIntent().getIntExtra("sports", 0)) {
		case 1:
			spinner_sports.setSelection(1);
			break;
		case 5:
			spinner_sports.setSelection(2);
			break;
		case 6:
			spinner_sports.setSelection(4);
			break;
		case 8:
			spinner_sports.setSelection(3);
			break;
		}

	}

	@SuppressLint("NewApi")
	void init_spinner_location() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("所有区域");
		list.add("成华区");
		list.add("金牛区");
		list.add("武侯区");
		list.add("锦江区");
		list.add("青羊区");
		list.add("高新区");
		list.add("新都区");
		list.add("温江区");
		list.add("龙泉驿区");
		list.add("青白江区");
		list.add("双流县");
		list.add("郫县");

		spinner_location = (Spinner) findViewById(R.id.sp_courtlist_location);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.layout_courtlist_spinner, R.id.tv_spinnerlayout, list) {
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
				label.setTextColor(spinner_location.getSelectedItemPosition() == position ? getResources()
						.getColor(R.color.blue) : getResources().getColor(
						R.color.darkGrey));
				ImageView icon = (ImageView) convertView
						.findViewById(R.id.iv_courtlistitem);
				icon.setImageDrawable(spinner_location
						.getSelectedItemPosition() == position ? getResources()
						.getDrawable(R.drawable.ic_courtlist_location2)
						: getResources().getDrawable(
								R.drawable.ic_courtlist_location1));

				return convertView;
			}
		};

		spinner_location.setDropDownWidth(Data.SCREENWIDTH);
		spinner_location.setAdapter(adapter);
		spinner_location
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// TODO Auto-generated method stub
						if (!firstLaunched2) {
							currentPage = 0;
							sumPage = 0;
							hasMore = true;
							lv_list.removeFooterView(footView);
							rl_gifcontainer.setVisibility(View.VISIBLE);
							new Thread(r_RequestList).start();
						} else {
							firstLaunched2 = false;
						}
					}

					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub
					}
				});

	}

	@SuppressLint("NewApi")
	void init_spinner_order() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("默认排序");
		list.add("由近到远");
		list.add("评分高低");
		list.add("价格高低");
		list.add("可预订");

		spinner_order = (Spinner) findViewById(R.id.sp_courtlist_order);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.layout_courtlist_spinner, R.id.tv_spinnerlayout, list) {
			@SuppressLint("NewApi")
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
				label.setTextColor(spinner_order.getSelectedItemPosition() == position ? getResources()
						.getColor(R.color.blue) : getResources().getColor(
						R.color.darkGrey));
				ImageView icon = (ImageView) convertView
						.findViewById(R.id.iv_courtlistitem);

				switch (position) {
				case 0:
					icon.setImageDrawable(spinner_order
							.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_order2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_order1));
					break;
				case 1:
					icon.setImageDrawable(spinner_order
							.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_distance2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_distance1));
					break;
				case 2:
					icon.setImageDrawable(spinner_order
							.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_mark2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_mark1));
					break;
				case 3:
					icon.setImageDrawable(spinner_order
							.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_money2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_money1));
					break;
				case 4:
					icon.setImageDrawable(spinner_order
							.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_bookable2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_bookable1));
					break;
				}

				return convertView;
			}
		};

		spinner_order.setDropDownWidth(Data.SCREENWIDTH);
		spinner_order.setAdapter(adapter);
		spinner_order
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// TODO Auto-generated method stub
						if (!firstLaunched3) {
							currentPage = 0;
							sumPage = 0;
							hasMore = true;
							lv_list.removeFooterView(footView);
							rl_gifcontainer.setVisibility(View.VISIBLE);
							new Thread(r_RequestList).start();
						} else {
							firstLaunched3 = false;
						}
					}

					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub
					}
				});

	}

	void init_gif() {
		GifView gif_loading = new GifView(this);
		gif_loading.setGifImage(R.drawable.gif_loading);
		gif_loading.setShowDimension((int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		gif_loading.setGifImageType(GifImageType.SYNC_DECODER);

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_courtlist_gifcontainer);
		LayoutParams p = new LayoutParams((int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		p.addRule(RelativeLayout.CENTER_HORIZONTAL);
		p.setMargins(0, (int) (Data.SCREENHEIGHT * 0.22), 0, 0);
		rl_gifcontainer.addView(gif_loading, p);

	}

	// 一些在刷新的时候做的事情
	private class GetDataTask extends AsyncTask<Void, Void, String[]> {

		@Override
		protected String[] doInBackground(Void... params) {
			// Simulates a background job.
			currentPage = 0;
			sumPage = 0;
			hasMore = true;
			//lv_list.removeFooterView(footView);
			//lv_list.addFooterView(footView);
			new Thread(r_RequestList).start();
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {

			// Call onRefreshComplete when the list has been refreshed.

			super.onPostExecute(result);
		}
	}

	/**
	 * 销毁定位
	 */
	@SuppressWarnings("deprecation")
	private void stopLocation() {
		if (aMapLocManager != null) {
			aMapLocManager.removeUpdates(this);
			aMapLocManager.destory();
		}
		aMapLocManager = null;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		locationTime++;
		
		//System.out.println("run"+locationTime);

		if (locationTime == 1) {
			// 第一次定位动作结束后启动线程
			new Thread(r_RequestList).start();
		}

		if (aMapLocation == null) {
			tv_locationtitle.setText("无法定位，请检查您的网络设置");
			tv_location.setText("");
			stopLocation();// 销毁掉定位
		}
	}

	@Override
	public void onLocationChanged(AMapLocation location) {
		// TODO Auto-generated method stub
		locationTime++;
		
		//System.out.println("onLocationChanged"+locationTime);

		if (locationTime == 1) {
			// 第一次定位动作结束后启动线程
			new Thread(r_RequestList).start();
		}

		if (location != null) {
			this.aMapLocation = location;// 判断超时机制
			Data.LATITUDE = location.getLatitude();
			Data.LONGITUDE = location.getLongitude();
			// Data.CITY = location.getCity();
			Data.ADDRESS = location.getAddress();
			tv_locationtitle.setText("当前位置");
			tv_location.setText(location.getAddress());
			stopLocation();

			if (!Data.ISTRUELOCATION) {
				Data.ISTRUELOCATION = true;
				if (locationTime > 1) {
					Toast.makeText(CourtlistActivity.this, "定位成功，可下拉刷新列表",
							Toast.LENGTH_SHORT).show();
				}
			}

		}

	}

	Runnable r_RequestList = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			int type = 0;
			switch (spinner_sports.getSelectedItemPosition()) {
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
			if (spinner_location.getSelectedItemPosition() != 0)
				params.add(new BasicNameValuePair("Area",
						(String) spinner_location.getSelectedItem()));
			params.add(new BasicNameValuePair("Order", spinner_order
					.getSelectedItemPosition() + ""));
			params.add(new BasicNameValuePair("Longitude", Data.LONGITUDE + ""));
			params.add(new BasicNameValuePair("Latitude", Data.LATITUDE + ""));

			try {

				String result = new HttpPostConnection("GymInfoServer", params)
						.httpConnection();

				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("result", result);
				msg.setData(b);
				courtlistHandler.sendMessage(msg);

				return;
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};

	Runnable r_RequestEachPage = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			courtLoadNum = 0;
			courtInfo = new JSONObject();

			int j;
			if (currentPage < sumPage)
				j = 10;
			else {
				j = lastPageItemNum;
				hasMore = false;
			}

			for (int i = 0; i < j; i++) {
				int k = currentPage * 10 + i;
				RequestEachCourtThread thread = new RequestEachCourtThread(
						gymID, k, j, i);
				pool.execute(thread);
			}

			currentPage++;
		}
	};

	class RequestEachCourtThread extends Thread {
		JSONObject gymID;
		int i;
		int num;
		int order;

		RequestEachCourtThread(JSONObject gymID, int i, int num, int order) {
			this.gymID = gymID;
			this.i = i;
			this.num = num;
			this.order = order;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				Message msg = new Message();
				Bundle b = new Bundle();

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("Request", "RequestByID"));
				params.add(new BasicNameValuePair("GymDetailLevel", "1"));
				params.add(new BasicNameValuePair("GymID", gymID.getInt("GymID"
						+ i)
						+ ""));
				params.add(new BasicNameValuePair("Longitude", Data.LONGITUDE
						+ ""));
				params.add(new BasicNameValuePair("Latitude", Data.LATITUDE
						+ ""));

				String result = new HttpPostConnection("GymInfoServer", params)
						.httpConnection();

				// System.out.println(result);

				if (result.equals("timeout")) {
					msg.setData(b);
					timeoutHandler.sendMessage(msg);
					return;
				}

				JSONObject json = new JSONObject(result);
				b.putString("info", json.toString());
				b.putInt("i", order);
				b.putInt("num", num);
				msg.setData(b);
				listHandler.sendMessage(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	class CourtlistHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			try {

				String result = msg.getData().getString("result");

				if (result.equals("timeout")) {
					Toast.makeText(CourtlistActivity.this, "连接服务器超时，请检查您的网络设置",
							Toast.LENGTH_SHORT).show();
					rl_gifcontainer.setVisibility(View.GONE);
					mPullToRefreshListView.onRefreshComplete();
					return;
				}

				gymID = new JSONObject(result);
				sumPage = (int) (gymID.getInt("GymNum") / 10);
				lastPageItemNum = gymID.getInt("GymNum") - sumPage * 10;
				mylist.clear();
				if (gymID.getInt("GymNum") == 0) {
					lv_list.removeFooterView(footView);
					rl_gifcontainer.setVisibility(View.GONE);
					return;
				} else if (gymID.getInt("GymNum") > 10) {
					//lv_list.removeFooterView(footView);
					//lv_list.addFooterView(footView);
				}
				new Thread(r_RequestEachPage).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressLint("HandlerLeak")
	class ListHandler extends Handler {
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();

			if (b.getInt("num") < 10) {
				lv_list.removeFooterView(footView);
			}

			try {
				int i = b.getInt("i");

				courtInfo.put("info" + i, new JSONObject(b.getString("info")));

				courtLoadNum++;
				if (courtLoadNum >= b.getInt("num")) {

					for (int j = 0; j < b.getInt("num"); j++) {
						JSONObject json = courtInfo.getJSONObject("info" + j);

						HashMap<String, Object> map = new HashMap<String, Object>();
						if (json.has("Path")) {
							map.put("courtlogo", json.getString("Path"));
							map.put("hasPath", true);
						} else {
							map.put("courtlogo", R.color.lightGrey);
							map.put("hasPath", false);
						}

						map.put("courtname", json.getString("GymName"));

						map.put("ratenum", json.getInt("RateNum"));
						if (json.getInt("RateNum") != 0) {
							double f = json.getDouble("Rate");
							BigDecimal b1 = new BigDecimal(f);
							double f1 = b1
									.setScale(1, BigDecimal.ROUND_HALF_UP)
									.doubleValue();
							map.put("rate", f1 + "");
						} else {
							map.put("rate", "0");
						}

						map.put("price", Data.doubleTrans(json.getDouble("Price")));
						map.put("location", json.getString("GymArea"));

						double meters = json.getDouble("Distance");
						if (meters < 500)
							map.put("meters", "<500m");
						else if (meters < 1000)
							map.put("meters", (int) (meters / 100) + "00m");
						else if (meters < 10000)
							map.put("meters", ((double) ((int) (meters / 100)))
									/ 10 + "km");
						else
							map.put("meters", (int) (meters / 1000) + "km");

						if (json.has("Service")) {
							String[] service = json.getString("Service").split(
									",");
							int serviceNum = service.length;
							for (int k = 0; k < serviceNum; k++) {
								char temp = service[k].charAt(0);
								switch (temp) {
								case '1':
									map.put("service" + (k + 1),
											R.drawable.ic_courtlist_parking);
									break;
								case '2':
									map.put("service" + (k + 1),
											R.drawable.ic_courtlist_wifi);
									break;
								case '3':
									map.put("service" + (k + 1),
											R.drawable.ic_courtlist_shower);
									break;
								case '4':
									map.put("service" + (k + 1),
											R.drawable.ic_courtlist_store);
									break;
								case '5':
									map.put("service" + (k + 1),
											R.drawable.ic_courtlist_drink);
									break;
								case '6':
									map.put("service" + (k + 1),
											R.drawable.ic_courtlist_food);
									break;
								}
							}
						}

						mylist.add(map);
					}

					rl_gifcontainer.setVisibility(View.GONE);

					adapter.notifyDataSetChanged();
					mPullToRefreshListView.onRefreshComplete();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	class AddFootviewHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			//lv_list.removeFooterView(footView);
			lv_list.addFooterView(footView);
		}
	}

	@SuppressLint("HandlerLeak")
	class TimeoutHandler extends Handler {
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Toast.makeText(CourtlistActivity.this, "连接服务器超时，请检查您的网络设置",
					Toast.LENGTH_SHORT).show();
			rl_gifcontainer.setVisibility(View.GONE);
			mPullToRefreshListView.onRefreshComplete();
		}
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
		stopLocation();
		MobclickAgent.onPause(this);
	}

}

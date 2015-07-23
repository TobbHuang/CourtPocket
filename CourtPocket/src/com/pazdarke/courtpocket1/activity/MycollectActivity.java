package com.pazdarke.courtpocket1.activity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.ant.liao.GifView;
import com.ant.liao.GifView.GifImageType;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.pazdarke.courtpocket1.tools.listview.CourtlistAdapter;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;

public class MycollectActivity extends Activity {

	RelativeLayout rl_gifcontainer;

	ListView lv_list;
	PullToRefreshListView mPullToRefreshListView;
	ArrayList<HashMap<String, Object>> mylist;
	JSONObject courtInfo;
	CourtlistAdapter adapter;
	
	View footView;

	JSONObject gymID;
	int sumPage = 0, currentPage = 0, lastPageItemNum;
	boolean hasMore = true;
	int courtLoadNum;// 请求item为异步，判断是否加载完毕

	ListHandler listHandler;
	CloseGifHandler closeGifHandler;
	AddFootviewHandler addFootviewHandler=new AddFootviewHandler();
	TimeoutHandler timeoutHandler;

	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_mycollect);
		
		AppManager.getAppManager().addActivity(this);

		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_mycollect_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_mycollect_list);
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
		footView = getLayoutInflater().inflate(
				R.layout.layout_courtlist_footview, null);
		lv_list.addFooterView(footView);

		mylist = new ArrayList<HashMap<String, Object>>();

		init_list();
		init_gif();

		listHandler = new ListHandler();
		closeGifHandler=new CloseGifHandler();
		timeoutHandler = new TimeoutHandler();

		new Thread(r_RequestList).start();
		
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_mycollect_leftarrow:
				finish();
				break;
			}
		}
	};

	void init_list() {
		adapter = new CourtlistAdapter(MycollectActivity.this, mylist,
				R.layout.item_court, new String[] { "courtlogo", "courtname",
						"rate", "price", "location", "meters", "service1",
						"service2", "service3", "service4", "service5",
						"service6" }, new int[] { R.id.iv_courtitem_courtlogo,
						R.id.tv_courtitem_courtname, R.id.tv_courtitem_rate,
						R.id.tv_courtitem_price, R.id.tv_courtitem_location,
						R.id.tv_courtitem_meters, R.id.iv_courtitem_service1,
						R.id.iv_courtitem_service2, R.id.iv_courtitem_service3,
						R.id.iv_courtitem_service4, R.id.iv_courtitem_service5,
						R.id.iv_courtitem_service6 });

		lv_list.setAdapter(adapter);
		lv_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MycollectActivity.this,
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
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				// 当不滚动时
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// 判断是否滚动到底部
					if (view.getLastVisiblePosition() == view.getCount() - 1) {
						// 加载更多功能的代码
						if (hasMore) {
							//lv_list.addFooterView(footView);
							new Thread(r_RequestAllGym).start();
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

	}

	void init_gif() {
		GifView gif_loading = new GifView(this);
		gif_loading.setGifImage(R.drawable.gif_loading);
		gif_loading.setShowDimension((int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		gif_loading.setGifImageType(GifImageType.SYNC_DECODER);

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_mycollect_gifcontainer);
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
			try {
				currentPage = 0;
				sumPage = 0;
				hasMore = true;
				//lv_list.removeFooterView(footView);
				//lv_list.addFooterView(footView);
				new Thread(r_RequestList).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {

			// Call onRefreshComplete when the list has been refreshed.

			super.onPostExecute(result);
		}
	}

	Runnable r_RequestList = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "MyCollect"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));

			try {

				String result = new HttpPostConnection("CollectServer", params)
						.httpConnection();

				if (result.equals("timeout")) {
					Message msg = new Message();
					Bundle b = new Bundle();
					msg.setData(b);
					timeoutHandler.sendMessage(msg);
					return;
				}

				gymID = new JSONObject(result);
				sumPage = (int) (gymID.getInt("GymNum") / 10);
				lastPageItemNum = gymID.getInt("GymNum") - sumPage * 10;
				mylist.clear();
				if(gymID.getInt("GymNum")==0){
					Message msg=new Message();
					Bundle b=new Bundle();
					msg.setData(b);
					closeGifHandler.sendMessage(msg);
					return;
				} else if (gymID.getInt("GymNum") > 10) {
					Message msg = new Message();
					Bundle b = new Bundle();
					msg.setData(b);
					//addFootviewHandler.sendMessage(msg);
				}
				new Thread(r_RequestAllGym).start();

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};

	Runnable r_RequestAllGym = new Runnable() {

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
				thread.start();
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

				System.out.println(result);

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
							map.put("courtlogo", R.drawable.icon);
							map.put("hasPath", false);
						}

						map.put("courtname", json.getString("GymName"));
						
						map.put("ratenum", json.getInt("RateNum"));
						if (json.getInt("RateNum") != 0){
							double f = json.getDouble("Rate");
							BigDecimal b1 = new BigDecimal(f);
							double f1 = b1.setScale(1, BigDecimal.ROUND_HALF_UP)
									.doubleValue();
							map.put("rate", f1+"");
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

						String[] service = json.getString("Service").split(",");
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
						mylist.add(map);
					}

					System.out.println("GIF给老子滚...");
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
	
	@SuppressLint("HandlerLeak")
	class CloseGifHandler extends Handler {
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			lv_list.removeFooterView(footView);
			rl_gifcontainer.setVisibility(View.GONE);
		}
	}
	
	@SuppressLint("HandlerLeak")
	class AddFootviewHandler extends Handler {
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			lv_list.removeFooterView(footView);
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

			Toast.makeText(MycollectActivity.this, "网络出问题咯(+﹏+)~",
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
		MobclickAgent.onPause(this);
	}
	
}

package com.pazdarke.courtpocket1.activity;

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
import com.pazdarke.courtpocket1.tools.listview.FightbillAdapter;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class FightbillActivity extends Activity {

	Intent intent;

	RelativeLayout rl_gifcontainer;

	ListView lv_list;
	PullToRefreshListView mPullToRefreshListView;
	ArrayList<HashMap<String, Object>> mylist;
	FightbillAdapter adapter;

	JSONObject billID;
	int sumPage = 0, currentPage = 0, lastPageItemNum;
	boolean hasMore = true;
	int loadNum;// 请求item为异步，判断是否加载完毕
	JSONObject billInfo;
	JSONObject allBillInfo;

	View footView;

	ListHandler listHandler;
	CloseGifHandler closeGifHandler;
	AddFootviewHandler addFootviewHandler=new AddFootviewHandler();
	TimeoutHandler timeoutHandler;
	
	TextView tv_error;
	
	//public static Activity instance_mybill;
	
	

	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_fightbill);
		
		AppManager.getAppManager().addActivity(this);
		
		//instance_mybill=this;

		intent = getIntent();

		init_spinner();

		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_fightbill_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);
		
		tv_error=(TextView)findViewById(R.id.tv_fightbill_error);
		
		TextView tv_teamname=(TextView)findViewById(R.id.tv_fightbill_teamname);
		tv_teamname.setText(intent.getStringExtra("TeamName"));

		mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_fightbill_list);
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
		lv_list.setDivider(null);
		footView = getLayoutInflater().inflate(
				R.layout.layout_courtlist_footview, null);
		lv_list.addFooterView(footView);

		mylist = new ArrayList<HashMap<String, Object>>();

		init_list();
		init_gif();
		
		IntentFilter filter = new IntentFilter("RefreshFightbill"); 
        registerReceiver(broadcastReceiver, filter); 

		listHandler = new ListHandler();
		closeGifHandler = new CloseGifHandler();
		timeoutHandler = new TimeoutHandler();

		new Thread(r_GetBillID).start();

	}

	private void init_spinner() {
		// TODO Auto-generated method stub

		ArrayList<String> list = new ArrayList<String>();
		list.add("全部");
		list.add("三天内");
		list.add("一周内");

		Spinner mSpinner = (Spinner) findViewById(R.id.sp_fightbill);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				FightbillActivity.this, R.layout.layout_mybill_spinner,
				R.id.tv_mybillspinnerlayout, list) {
			@Override
			public View getDropDownView(int position, View convertView,
					ViewGroup parent) {
				// TODO Auto-generated method stub

				if (convertView == null) {
					convertView = getLayoutInflater().inflate(
							R.layout.item_mybill_spinner, parent, false);
				}
				TextView label = (TextView) convertView
						.findViewById(R.id.tv_mybillitem);
				label.setText(getItem(position));

				return convertView;
			}
		};
		mSpinner.setAdapter(adapter);

		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		mSpinner.setSelection(0);

	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_fightbill_leftarrow:
				finish();
				break;
			}
		}
	};

	void init_list() {
		adapter = new FightbillAdapter(FightbillActivity.this, mylist,
				R.layout.item_fight);

		lv_list.setAdapter(adapter);
		lv_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				/*Intent intent = new Intent(YuepaolistActivity.this,
						MybillInfoActivity.class);
				try {
					intent.putExtra("ID",
							billID.getString("BillID" + (position - 1)));
					intent.putExtra("info",
							allBillInfo.getString("info" + (position - 1)));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				startActivity(intent);*/
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
							new Thread(r_GetEachPageBill).start();
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

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_fightbill_gifcontainer);
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
			new Thread(r_GetBillID).start();
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {

			// Call onRefreshComplete when the list has been refreshed.

			super.onPostExecute(result);
		}
	}

	Runnable r_GetBillID = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "MyFight"));
			params.add(new BasicNameValuePair("UserID", intent.getStringExtra("UserID")));
			params.add(new BasicNameValuePair("Passcode", intent.getStringExtra("Passcode")));
			params.add(new BasicNameValuePair("TeamID", intent.getStringExtra("TeamID")));

			String result = new HttpPostConnection("FightServer", params)
					.httpConnection();

			if (result.equals("timeout")) {
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("result", "网络好像出问题了呢...");
				msg.setData(b);
				timeoutHandler.sendMessage(msg);
				return;
			}

			try {
				billID = new JSONObject(result);
				allBillInfo = new JSONObject();
				sumPage = (int) (billID.getInt("FightBillIDNum") / 10);
				lastPageItemNum = billID.getInt("FightBillIDNum") - sumPage * 10;
				mylist.clear();
				if (billID.getInt("FightBillIDNum")==0) {
					Message msg = new Message();
					Bundle b = new Bundle();
					msg.setData(b);
					closeGifHandler.sendMessage(msg);
					return;
				} else if (billID.getInt("FightBillIDNum") > 10) {
					Message msg = new Message();
					Bundle b = new Bundle();
					msg.setData(b);
					//addFootviewHandler.sendMessage(msg);
				}
				new Thread(r_GetEachPageBill).start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("result", "加载失败");
				msg.setData(b);
				timeoutHandler.sendMessage(msg);
			}

		}
	};

	Runnable r_GetEachPageBill = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			loadNum = 0;
			billInfo = new JSONObject();

			int j;
			if (currentPage < sumPage)
				j = 10;
			else {
				j = lastPageItemNum;
				hasMore = false;
			}

			for (int i = 0; i < j; i++) {
				int k = currentPage * 10 + i;
				RequestEachBillThread thread = new RequestEachBillThread(
						 k, j, i);
				thread.start();
			}

			currentPage++;

		}

	};

	class RequestEachBillThread extends Thread {
		int i;
		int num;
		int order;

		RequestEachBillThread(int i, int num, int order) {
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
				params.add(new BasicNameValuePair("Request", "FightDetail"));
				params.add(new BasicNameValuePair("UserID", intent.getStringExtra("UserID")));
				params.add(new BasicNameValuePair("Passcode", intent.getStringExtra("Passcode")));
				params.add(new BasicNameValuePair("FightBillID", billID.getString("FightBillID"+i)));

				String result = new HttpPostConnection("FightServer", params)
						.httpConnection();

				System.out.println(result);

				if (result.equals("timeout")) {
					b.putString("result", "网络好像出问题了呢...");
					msg.setData(b);
					timeoutHandler.sendMessage(msg);
					return;
				}

				JSONObject json = new JSONObject(result);
				b.putString("info", json.toString());
				b.putInt("i", order);
				b.putInt("j", i);
				b.putInt("num", num);
				msg.setData(b);
				listHandler.sendMessage(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("result", "加载失败");
				msg.setData(b);
				timeoutHandler.sendMessage(msg);
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

			int i = b.getInt("i");// 页内顺序
			int k = b.getInt("j");// 总顺序

			try {
				billInfo.put("info" + i, new JSONObject(b.getString("info")));
				allBillInfo.put("info" + k, b.getString("info"));
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			loadNum++;
			if (loadNum >= b.getInt("num")) {
				for (int j = 0; j < b.getInt("num"); j++) {
					try {
						JSONObject json = billInfo.getJSONObject("info" + j);

						HashMap<String, Object> map = new HashMap<String, Object>();
						if (json.has("TeamPic")) {
							map.put("TeamPic", json.getString("TeamPic"));
							map.put("hasPath1", true);
						} else {
							map.put("hasPath1", false);
						}
						if (json.has("GymPic")) {
							map.put("GymPic", json.getString("GymPic"));
							map.put("hasPath3", true);
						} else {
							map.put("hasPath3", false);
						}
						map.put("Status", json.getInt("Status"));
						
						map.put("TeamName", json.getString("TeamName"));

						map.put("Time", json.getInt("Time"));
						map.put("Date", json.getString("Date"));
						
						map.put("GymName", json.getString("GymName"));
						map.put("GymID", json.getInt("GymID"));
						
						map.put("CourtName", json.getString("CourtName"));
						
						map.put("Price", json.getDouble("Price"));
						
						map.put("TeamID", json.getInt("TeamID"));
						
						map.put("GenerateTime", json.getString("GenerateTime"));
						
						map.put("BillID", billID.getString("FightBillID"+j));
						
						map.put("weight", json.getInt("Weight"));
						
						if(json.getInt("Status")>=3){
							map.put("oTeamID", json.getInt("oTeamID"));
							map.put("oTeamName", json.getString("oTeamName"));
							
							if (json.has("oTeamPic")) {
								map.put("oTeamPic", json.getString("oTeamPic"));
								map.put("hasPath2", true);
							} else {
								map.put("hasPath2", false);
							}
							
						}
						
						if(json.getInt("Status")==4){
							map.put("Code", json.getString("Code"));
						}
						
						if (json.has("GymPic")) {
							map.put("GymPic", json.getString("GymPic"));
							map.put("hasPath3", true);
						} else {
							map.put("hasPath3", false);
						}

						mylist.add(map);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				rl_gifcontainer.setVisibility(View.GONE);

				adapter.notifyDataSetChanged();
				mPullToRefreshListView.onRefreshComplete();
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
			tv_error.setVisibility(View.VISIBLE);
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
			Bundle b=msg.getData();

			Toast.makeText(FightbillActivity.this, b.getString("result"),
					Toast.LENGTH_SHORT).show();
			lv_list.removeFooterView(footView);
			rl_gifcontainer.setVisibility(View.GONE);
			mPullToRefreshListView.onRefreshComplete();
		}
	}
	
	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			try {
				
				rl_gifcontainer.setVisibility(View.VISIBLE);
				currentPage = 0;
				sumPage = 0;
				hasMore = true;
				new Thread(r_GetBillID).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
	};
	
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

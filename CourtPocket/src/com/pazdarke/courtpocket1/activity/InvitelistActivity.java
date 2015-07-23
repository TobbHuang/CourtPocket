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
import com.pazdarke.courtpocket1.tools.listview.TeammemberlistAdapter;
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

public class InvitelistActivity extends Activity {
	
	JSONObject userInfo;
	
	RelativeLayout rl_gifcontainer;

	ListView lv_list;
	PullToRefreshListView mPullToRefreshListView;
	ArrayList<HashMap<String, Object>> mylist;
	TeammemberlistAdapter adapter;
	
	View footView;

	JSONObject userID;
	int sumPage = 0, currentPage = 0, lastPageItemNum;
	boolean hasMore = true;
	int userLoadNum;// 请求item为异步，判断是否加载完毕

	ListHandler listHandler;
	CloseGifHandler closeGifHandler;
	TimeoutHandler timeoutHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_invitelist);
		
		AppManager.getAppManager().addActivity(this);
		
		try {
			userID=new JSONObject(getIntent().getStringExtra("UserInfo"));
			sumPage = (int) (userID.getInt("UserNum") / 10);
			lastPageItemNum = userID.getInt("UserNum") - sumPage * 10;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		initView();
		
		listHandler = new ListHandler();
		closeGifHandler=new CloseGifHandler();
		timeoutHandler = new TimeoutHandler();
		
		new Thread(r_RequestAllUser).start();
	}

	private void initView() {
		// TODO Auto-generated method stub
		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_invitelist_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_invitelist_list);
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
						try {
							if (userID.getInt("UserNum") > 10) {
								lv_list.removeFooterView(footView);
								lv_list.addFooterView(footView);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						new GetDataTask().execute();
					}
				});

		lv_list = mPullToRefreshListView.getRefreshableView();
		footView = getLayoutInflater().inflate(
				R.layout.layout_courtlist_footview, null);
		try {
			if (userID.getInt("UserNum") > 10)
				lv_list.addFooterView(footView);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mylist = new ArrayList<HashMap<String, Object>>();

		init_list();
		init_gif();

		
	}
	
	void init_list() {
		adapter = new TeammemberlistAdapter(InvitelistActivity.this, mylist,
				R.layout.item_userinfo);

		lv_list.setAdapter(adapter);
		lv_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(InvitelistActivity.this,
						UserinfoActivity.class);
				try {
					intent.putExtra("UserID",
							userID.getInt("UserID" + (position - 1)));
					intent.putExtra("TeamID", InvitelistActivity.this.getIntent().getStringExtra("TeamID"));
					intent.putExtra("isInvite", true);
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
							new Thread(r_RequestAllUser).start();
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

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_invitelist_gifcontainer);
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
			hasMore = true;
			mylist.clear();
			
			new Thread(r_RequestAllUser).start();
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {

			// Call onRefreshComplete when the list has been refreshed.

			super.onPostExecute(result);
		}
	}
	
	OnClickListener onClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_invitelist_leftarrow:
				finish();
				break;
			}
		}
	};
	
	Runnable r_RequestAllUser = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			userLoadNum = 0;
			userInfo = new JSONObject();

			int j;
			if (currentPage < sumPage)
				j = 10;
			else {
				j = lastPageItemNum;
				hasMore = false;
			}

			for (int i = 0; i < j; i++) {
				int k = currentPage * 10 + i;
				RequestEachUserThread thread = new RequestEachUserThread(
						userID, k, j, i);
				thread.start();
			}

			currentPage++;
		}
	};
	
	class RequestEachUserThread extends Thread {
		JSONObject userID;
		int i;
		int num;
		int order;

		RequestEachUserThread(JSONObject gymID, int i, int num, int order) {
			this.userID = gymID;
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
				params.add(new BasicNameValuePair("Request", "GetUserInfo"));
				params.add(new BasicNameValuePair("UserID", userID.getInt("UserID"
						+ i)
						+ ""));

				String result = new HttpPostConnection("UserInfoServer", params)
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

				userInfo.put("info" + i, new JSONObject(b.getString("info")));

				userLoadNum++;
				if (userLoadNum >= b.getInt("num")) {

					for (int j = 0; j < b.getInt("num"); j++) {
						JSONObject json = userInfo.getJSONObject("info" + j);

						HashMap<String, Object> map = new HashMap<String, Object>();
						if(json.has("Path")){
							map.put("hasPath", true);
							map.put("path", json.getString("Path"));
						}else{
							map.put("hasPath", false);
						}
						
						map.put("isLeader", false);
						map.put("isDelete", false);
						
						map.put("username", json.getString("Name"));
						map.put("ratenum", json.getInt("RateNum"));
						if(json.getInt("RateNum")!=0){
							double f = json.getDouble("Rate");
							BigDecimal b1 = new BigDecimal(f);
							double f1 = b1.setScale(1, BigDecimal.ROUND_HALF_UP)
									.doubleValue();
							map.put("rate", f1);
						}
						
						if(json.has("Sex")){
							map.put("Sex", json.getInt("Sex"));
						} else{
							map.put("Sex", 2);
						}
						
						if(json.has("Introduction")){
							map.put("introduction", "简介："+json.getString("Introduction"));
						} else{
							map.put("introduction", "未填写个人简介");
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
	class TimeoutHandler extends Handler {
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Toast.makeText(InvitelistActivity.this, "网络出问题咯(+﹏+)~",
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

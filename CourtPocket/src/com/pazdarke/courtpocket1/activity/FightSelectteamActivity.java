package com.pazdarke.courtpocket1.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import com.pazdarke.courtpocket1.tools.listview.TeamlistAdapter;
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

public class FightSelectteamActivity extends Activity {

	JSONObject teamID;
	//int sumPage = 0, currentPage = 0, lastPageItemNum;
	//boolean hasMore = true;
	int teamLoadNum;// 请求item为异步，判断是否加载完毕
	JSONObject teamInfo;
	JSONObject allTeamInfo;
	boolean isRefresh=false;

	ListView lv_list;
	PullToRefreshListView mPullToRefreshListView;
	ArrayList<HashMap<String, Object>> mylist;
	TeamlistAdapter teamAdapter;
	View footView;
	ExecutorService pool;
	
	RelativeLayout rl_gifcontainer;
	
	TeaminfoHandler teaminfoHandler;
	TimeoutHandler timeoutHandler;
	
	int selectableTeam=0,unselectableTeam=0;
	
	public static Activity instance_fightselectteam;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.activity_fight_selectteam);
			
			AppManager.getAppManager().addActivity(this);
			
			instance_fightselectteam=this;

			pool = Executors.newFixedThreadPool(10);

			teamID = new JSONObject(getIntent().getStringExtra("teamID"));
			//sumPage = (int) (teamID.getInt("TeamNum") / 10);
			//lastPageItemNum = teamID.getInt("TeamNum") - sumPage * 10;
			allTeamInfo = new JSONObject();

			initView();

			new Thread(r_TeamInfo).start();
			
			teaminfoHandler = new TeaminfoHandler();
			timeoutHandler = new TimeoutHandler();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initView() {
		// TODO Auto-generated method stub
		
		ImageView iv_leftarrow=(ImageView)findViewById(R.id.iv_fightselectteam_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);
		
		init_list();
		init_gif();
	}
	
	@SuppressLint("InflateParams")
	void init_list() {
		try {
			
			mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_fightselectteam_list);
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
							new GetDataTask().execute();
						}
					});
			
			mylist = new ArrayList<HashMap<String, Object>>();
			mylist.clear();
			lv_list = mPullToRefreshListView.getRefreshableView();
			lv_list.setDivider(null);
			footView = getLayoutInflater().inflate(
					R.layout.layout_courtlist_footview, null);
			lv_list.addFooterView(footView);
			
			teamAdapter = new TeamlistAdapter(FightSelectteamActivity.this, mylist,
					R.layout.item_team, new String[] { "teamlogo", "teamtype",
							"teamname", "teamleadername", "teammembernum",
							"teamrate", "teamratenum" }, new int[] {
							R.id.iv_teamitem_logo, R.id.iv_teamitem_teamtype,
							R.id.tv_teamitem_teamname,
							R.id.tv_teamitem_leadername,
							R.id.tv_teamitem_membernum, R.id.tv_teamitem_rate,
							R.id.tv_teamitem_ratenum });

			lv_list.setAdapter(teamAdapter);
			lv_list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					 if((position-1)<selectableTeam){
						if (!FightSelectteamActivity.this.getIntent()
								.getBooleanExtra("isAcceptFight", false)) {
							Intent intent = new Intent(
									FightSelectteamActivity.this,
									FightbookActivity.class);
							intent.putExtra("ID", FightSelectteamActivity.this
									.getIntent().getStringExtra("gymID"));
							intent.putExtra("gymName",
									FightSelectteamActivity.this.getIntent()
											.getStringExtra("gymName"));
							intent.putExtra("gymType",
									FightSelectteamActivity.this.getIntent()
											.getIntExtra("gymType", 1));
							intent.putExtra(
									"teamName",
									(String) mylist.get(position - 1).get(
											"teamname"));
							intent.putExtra(
									"teamID",
									(Integer) mylist.get(position - 1).get(
											"teamID")
											+ "");
							intent.putExtra("startTime",
									FightSelectteamActivity.this.getIntent()
											.getIntExtra("startTime", 0));
							intent.putExtra("endTime",
									FightSelectteamActivity.this.getIntent()
											.getIntExtra("endTime", 47));
							intent.putExtra("weight",
									FightSelectteamActivity.this.getIntent()
											.getIntExtra("weight", 1));
							startActivity(intent);
						} else {
							Intent intent = new Intent(
									FightSelectteamActivity.this,
									AcceptfightConfirmActivity.class);
							intent.putExtra("FightID",FightSelectteamActivity.this.getIntent().getIntExtra("FightID",0));
							intent.putExtra("GymName",
									FightSelectteamActivity.this.getIntent()
											.getStringExtra("gymName"));
							intent.putExtra("GymType",
									FightSelectteamActivity.this.getIntent()
											.getIntExtra("gymType", 1));
							intent.putExtra("oTeamName",
									FightSelectteamActivity.this.getIntent()
											.getStringExtra("TeamName"));
							intent.putExtra(
									"TeamName",
									(String) mylist.get(position - 1).get(
											"teamname"));
							intent.putExtra("Date",
									FightSelectteamActivity.this.getIntent()
											.getStringExtra("Date"));
							intent.putExtra("CourtNum",
									FightSelectteamActivity.this.getIntent()
											.getIntExtra("CourtNum", 1));
							intent.putExtra("Time0",
									FightSelectteamActivity.this.getIntent()
											.getIntExtra("Time", 0));
							intent.putExtra("CourtName0",
									FightSelectteamActivity.this.getIntent()
											.getStringExtra("CourtName"));
							intent.putExtra("Price0",
									FightSelectteamActivity.this.getIntent()
											.getDoubleExtra("Price", 0)+"");
							intent.putExtra("Money",
									FightSelectteamActivity.this.getIntent()
											.getDoubleExtra("Price", 0));
							intent.putExtra(
									"TeamID",
									(Integer) mylist.get(position - 1).get(
											"teamID"));
							intent.putExtra("weight",
									FightSelectteamActivity.this.getIntent()
											.getIntExtra("weight", 1));
							startActivity(intent);
						}
					}
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
							//if (hasMore) {
							//	lv_list.addFooterView(footView);
							//	new Thread(r_TeamInfo).start();
							//} else {
								lv_list.removeFooterView(footView);
							//}
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
	
	// 一些在刷新的时候做的事情
	private class GetDataTask extends AsyncTask<Void, Void, String[]> {

		@Override
		protected String[] doInBackground(Void... params) {
			// Simulates a background job.
			//currentPage = 0;
			//sumPage = 0;
			//hasMore = true;
			isRefresh=true;
			selectableTeam=0;
			unselectableTeam=0;
			new Thread(r_TeamInfo).start();
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {

			// Call onRefreshComplete when the list has been refreshed.

			super.onPostExecute(result);
		}
	}
	
	void init_gif() {
		GifView gif_loading = new GifView(this);
		gif_loading.setGifImage(R.drawable.gif_loading);
		gif_loading.setShowDimension((int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		gif_loading.setGifImageType(GifImageType.SYNC_DECODER);

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_fightselectteam_gifcontainer);
		LayoutParams p = new LayoutParams((int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		p.addRule(RelativeLayout.CENTER_HORIZONTAL);
		p.setMargins(0, (int) (Data.SCREENHEIGHT * 0.22), 0, 0);
		rl_gifcontainer.addView(gif_loading, p);

	}

	OnClickListener onClickListener =new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.iv_fightselectteam_leftarrow:
				finish();
				break;
			}
		}
	};
	
	Runnable r_TeamInfo = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			teamLoadNum = 0;
			teamInfo = new JSONObject();

			int j = 0;
			/*if (currentPage < sumPage)
				j = 10;
			else {
				j = lastPageItemNum;
				hasMore = false;
			}*/
			try {
				j=teamID.getInt("TeamNum");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (int i = 0; i < j; i++) {
				//int k = currentPage * 10 + i;
				int k=i;
				RequestEachTeamThread thread = new RequestEachTeamThread( k, j, i);
				pool.execute(thread);
			}

			//currentPage++;
		}
	};

	class RequestEachTeamThread extends Thread {

		int i;
		int num;
		int order;

		RequestEachTeamThread(int i, int num, int order) {
			this.i = i;
			this.num = num;
			this.order = order;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();

			try {
				Message msg = new Message();
				Bundle b = new Bundle();

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("Request", "TeamInfo"));
				params.add(new BasicNameValuePair("TeamID", teamID
						.getInt("TeamID" + i) + ""));

				String result = new HttpPostConnection("TeamServer", params)
						.httpConnection();

				System.out.println(result);

				if (result.equals("timeout")) {
					msg.setData(b);
					timeoutHandler.sendMessage(msg);
					return;
				}

				allTeamInfo.put("info" + i, result);

				JSONObject json = new JSONObject(result);
				b.putString("info", json.toString());
				b.putInt("i", order);
				b.putInt("num", num);
				msg.setData(b);
				teaminfoHandler.sendMessage(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	@SuppressLint("HandlerLeak")
	class TeaminfoHandler extends Handler {
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();

			/*if (b.getInt("num") < 10) {
				lv_list.removeFooterView(footView);
			}*/

			lv_list.removeFooterView(footView);
			
			try {
				int i = b.getInt("i");

				teamInfo.put("info" + i, new JSONObject(b.getString("info")));

				teamLoadNum++;
				if (teamLoadNum >= b.getInt("num")) {

					if(isRefresh){
						isRefresh=false;
						mylist.clear();
					}

					for (int j = 0; j < b.getInt("num"); j++) {
						JSONObject json = teamInfo.getJSONObject("info" + j);

						HashMap<String, Object> map = new HashMap<String, Object>();
						if (json.has("Path")) {
							map.put("teamlogo", json.getString("Path"));
							map.put("hasPath", true);
						} else {
							map.put("hasPath", false);
						}

						map.put("teamtype", json.getInt("Type"));
						map.put("teamname", json.getString("TeamName"));
						map.put("teamleadername", json.getString("LeaderName"));
						map.put("teammembernum", json.getInt("MemberNum"));
						map.put("teamratenum", json.getInt("RateNum"));
						map.put("teamrate",
								json.getInt("RateNum") > 0 ? json
										.getDouble("Rate") : 0);
						map.put("teamID", teamID.getInt("TeamID" + j));

						// 如果自己是队长，球队往前排
						if (Data.USERID
								.equals(json.getInt("LeaderUserID") + "")) {
							mylist.add(selectableTeam, map);
							selectableTeam++;
						} else {

							mylist.add(mylist.size(), map);
							unselectableTeam++;
						}

					}

					rl_gifcontainer.setVisibility(View.GONE);
					
					teamAdapter.notifyDataSetChanged();
					mPullToRefreshListView.onRefreshComplete();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	@SuppressLint("HandlerLeak")
	class TimeoutHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			rl_gifcontainer.setVisibility(View.GONE);
			
			Toast.makeText(FightSelectteamActivity.this, "网络出问题了...", Toast.LENGTH_SHORT).show();
			
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

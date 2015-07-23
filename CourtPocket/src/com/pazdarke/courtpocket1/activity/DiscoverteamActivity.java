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
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class DiscoverteamActivity extends Activity {

	RelativeLayout rl_gifcontainer;
	JSONObject teamID;
	int sumPage = 0, currentPage = 0, lastPageItemNum;
	boolean hasMore = true;
	int teamLoadNum;// 请求item为异步，判断是否加载完毕
	JSONObject teamInfo;
	JSONObject allTeamInfo;

	Spinner sp_sports;

	ListView lv_list;
	PullToRefreshListView mPullToRefreshListView;
	ArrayList<HashMap<String, Object>> mylist;
	TeamlistAdapter teamAdapter;
	View footView;
	ExecutorService pool;

	TeamlistHandler teamlistHandler;
	TeaminfoHandler teaminfoHandler;
	TimeoutHandler timeoutHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_discoverteam);
		
		AppManager.getAppManager().addActivity(this);

		pool = Executors.newFixedThreadPool(10);

		initView();

		teamlistHandler = new TeamlistHandler();
		teaminfoHandler = new TeaminfoHandler();
		timeoutHandler = new TimeoutHandler();

		new Thread(r_TeamList).start();

	}

	@SuppressLint("InflateParams")
	private void initView() {
		// TODO Auto-generated method stub

		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_discoverteam_leftarrow);
		iv_leftarrow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		//init_sp_sports();
		init_gif();

		mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_discoverteam_list);
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

		mylist = new ArrayList<HashMap<String, Object>>();
		mylist.clear();
		lv_list = mPullToRefreshListView.getRefreshableView();
		init_list();
		lv_list.setDivider(null);
		footView = getLayoutInflater().inflate(
				R.layout.layout_courtlist_footview, null);
		lv_list.addFooterView(footView);
	}

	/*@SuppressLint("NewApi")
	void init_sp_sports() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("所有球队");
		list.add("足球队");
		//list.add("篮球队");
		//list.add("排球队");

		sp_sports = (Spinner) findViewById(R.id.sp_discoverteam_sports);
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
				label.setTextColor(sp_sports.getSelectedItemPosition() == position ? getResources()
						.getColor(R.color.blue) : getResources().getColor(
						R.color.darkGrey));
				ImageView icon = (ImageView) convertView
						.findViewById(R.id.iv_courtlistitem);

				switch (position) {
				case 0:
					icon.setImageDrawable(sp_sports.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_allsports2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_allsports1));
					break;
				case 1:
					icon.setImageDrawable(sp_sports.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_soccer2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_soccer1));
					break;
				case 2:
					icon.setImageDrawable(sp_sports.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_basketball2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_basketball1));
					break;
				case 3:
					icon.setImageDrawable(sp_sports.getSelectedItemPosition() == position ? getResources()
							.getDrawable(R.drawable.ic_courtlist_volleyball2)
							: getResources().getDrawable(
									R.drawable.ic_courtlist_volleyball1));
					break;
				}

				return convertView;
			}
		};

		sp_sports.setDropDownWidth(Data.SCREENWIDTH);
		sp_sports.setAdapter(adapter);
		sp_sports
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// TODO Auto-generated method stub
						// 初始化各种参数，加载动画设置可见，启动加载线程，同下
						currentPage = 0;
						sumPage = 0;
						hasMore = true;
						rl_gifcontainer.setVisibility(View.VISIBLE);
						new Thread(r_TeamList).start();

					}

					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub
					}
				});

		sp_sports.setSelection(0);

	}*/

	void init_gif() {
		GifView gif_loading = new GifView(this);
		gif_loading.setGifImage(R.drawable.gif_loading);
		gif_loading.setShowDimension((int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		gif_loading.setGifImageType(GifImageType.SYNC_DECODER);

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_discoverteam_gifcontainer);
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
				(int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		p.addRule(RelativeLayout.CENTER_HORIZONTAL);
		p.setMargins(0, (int) (Data.SCREENHEIGHT * 0.22), 0, 0);
		rl_gifcontainer.addView(gif_loading, p);

	}

	void init_list() {
		try {
			teamAdapter = new TeamlistAdapter(DiscoverteamActivity.this,
					mylist, R.layout.item_team, new String[] { "teamlogo",
							"teamtype", "teamname", "teamleadername",
							"teammembernum", "teamrate", "teamratenum" },
					new int[] { R.id.iv_teamitem_logo,
							R.id.iv_teamitem_teamtype,
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

					Intent intent = new Intent(DiscoverteamActivity.this,
							TeamInfoActivity.class);
					try {
						intent.putExtra("ID",
								teamID.getInt("TeamID" + (position - 1)) + "");
						intent.putExtra("info",
								allTeamInfo.getString("info" + (position - 1)));
						intent.putExtra("isMyTeam", false);

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
								new Thread(r_TeamInfo).start();
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
			new Thread(r_TeamList).start();
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {

			// Call onRefreshComplete when the list has been refreshed.

			super.onPostExecute(result);
		}
	}

	Runnable r_TeamList = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "FindTeam"));
			params.add(new BasicNameValuePair("Type", "1"));

			String result = new HttpPostConnection("TeamServer", params)
					.httpConnection();

			//System.out.println(result);

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			teamlistHandler.sendMessage(msg);
		}
	};

	Runnable r_TeamInfo = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			teamLoadNum = 0;
			teamInfo = new JSONObject();

			int j;
			if (currentPage < sumPage)
				j = 10;
			else {
				j = lastPageItemNum;
				hasMore = false;
			}

			for (int i = 0; i < j; i++) {
				int k = currentPage * 10 + i;
				RequestEachTeamThread thread = new RequestEachTeamThread(
						teamID, k, j, i);
				pool.execute(thread);
			}

			currentPage++;
		}
	};

	class RequestEachTeamThread extends Thread {

		JSONObject teamID;
		int i;
		int num;
		int order;

		RequestEachTeamThread(JSONObject teamID, int i, int num, int order) {
			this.teamID = teamID;
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

	class TeamlistHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();
			String result = b.getString("result");
			if (result.equals("timeout")) {
				rl_gifcontainer.setVisibility(View.GONE);
				Toast.makeText(DiscoverteamActivity.this, "网络出问题咯~>_<~+",
						Toast.LENGTH_SHORT).show();
				return;
			}

			try {
				teamID = new JSONObject(result);

				int teamNum = teamID.getInt("TeamNum");
				if (teamNum == 0) {
					rl_gifcontainer.setVisibility(View.GONE);
					lv_list.removeFooterView(footView);
					mylist.clear();
					teamAdapter.notifyDataSetChanged();
					mPullToRefreshListView.onRefreshComplete();
				} else {
					sumPage = (int) (teamNum / 10);
					lastPageItemNum = teamNum - sumPage * 10;
					mylist.clear();
					allTeamInfo = new JSONObject();

					new Thread(r_TeamInfo).start();
				}

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

			if (b.getInt("num") < 10) {
				lv_list.removeFooterView(footView);
			}

			try {
				int i = b.getInt("i");

				teamInfo.put("info" + i, new JSONObject(b.getString("info")));

				teamLoadNum++;
				if (teamLoadNum >= b.getInt("num")) {

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
						if(json.getInt("RateNum") > 0){
							double f = json.getDouble("Rate");
							BigDecimal b1 = new BigDecimal(f);
							double f1 = b1.setScale(1, BigDecimal.ROUND_HALF_UP)
									.doubleValue();
							map.put("teamrate", f1);
						} else{
							map.put("teamrate", 0d);
						}

						mylist.add(map);

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

	class TimeoutHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			rl_gifcontainer.setVisibility(View.GONE);
			Toast.makeText(DiscoverteamActivity.this, "网络出问题咯~>_<~+",
					Toast.LENGTH_SHORT).show();

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

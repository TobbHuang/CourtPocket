package com.pazdarke.courtpocket1.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import com.pazdarke.courtpocket1.tools.listview.TeamapplicationAdapter;
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
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;

public class TeammessageActivity extends Activity {

	String teamID;

	int titleSelection = 0;// 0 申请 1 邀请

	TextView tv_application, tv_invitation;
	TextView tv_cursor;

	RelativeLayout rl_gifcontainer;

	ListView lv_application;
	PullToRefreshListView mPullToRefreshListView_application;
	ArrayList<HashMap<String, Object>> mylist_application;
	JSONObject applicationInfo;
	JSONObject allApplicationInfo;
	TeamapplicationAdapter applicationAdapter;
	View footView1;
	JSONObject applicationID;
	int sumPage1 = 0, currentPage1 = 0, lastPageItemNum1;
	boolean hasMore1 = true;
	int applicationLoadNum;// 请求item为异步，判断是否加载完毕
	TextView tv_error;

	ExecutorService pool;

	ApplicationListHandler applicationListHandler;
	ApplicationinfoHandler applicationinfoHandler;
	TimeoutHandler timeoutHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_teammessage);

		AppManager.getAppManager().addActivity(this);

		pool = Executors.newFixedThreadPool(10);

		teamID = getIntent().getStringExtra("ID");

		IntentFilter filter = new IntentFilter("RefreshTeammessage");
		registerReceiver(refreshlistBroadcast, filter);

		initView();

		applicationListHandler = new ApplicationListHandler();
		applicationinfoHandler = new ApplicationinfoHandler();
		timeoutHandler = new TimeoutHandler();

		new Thread(r_ApplicationList).start();

	}

	@SuppressLint("InflateParams")
	private void initView() {
		// TODO Auto-generated method stub

		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_teammessage_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);
		
		tv_error=(TextView)findViewById(R.id.tv_teammessage_error);

		/*
		 * tv_application=(TextView)findViewById(R.id.tv_teammessage_application)
		 * ; tv_application.setOnClickListener(onClickListener);
		 * 
		 * tv_invitation=(TextView)findViewById(R.id.tv_teammessage_invitation);
		 * tv_invitation.setOnClickListener(onClickListener);
		 * 
		 * tv_cursor=(TextView)findViewById(R.id.tv_teammessage_cursor);
		 */

		mPullToRefreshListView_application = (PullToRefreshListView) findViewById(R.id.lv_teammessage_application);
		mPullToRefreshListView_application
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
						lv_application.removeFooterView(footView1);
						lv_application.addFooterView(footView1);
						new GetDataTask().execute();
					}
				});

		lv_application = mPullToRefreshListView_application
				.getRefreshableView();
		footView1 = getLayoutInflater().inflate(
				R.layout.layout_courtlist_footview, null);
		lv_application.addFooterView(footView1);

		mylist_application = new ArrayList<HashMap<String, Object>>();

		init_applicationlist();

		init_gif();

	}

	void init_applicationlist() {
		applicationAdapter = new TeamapplicationAdapter(
				TeammessageActivity.this, mylist_application,
				R.layout.item_teamapplication, new String[] { "logo",
						"username", "content", "result", "date" }, new int[] {
						R.id.iv_itemteamapplication_logo,
						R.id.tv_itemteamapplication_username,
						R.id.tv_itemteamapplication_content,
						R.id.tv_itemteamapplication_result,
						R.id.tv_itemteamapplication_date });

		lv_application.setAdapter(applicationAdapter);
		lv_application.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				try {
					int status = allApplicationInfo.getJSONObject(
							"info" + (position - 1)).getInt("Status");
					if (status == 1) {
						Intent intent = new Intent(TeammessageActivity.this,
								UserinfoActivity.class);

						intent.putExtra(
								"ApplicationID",
								applicationID.getInt("ApplicationID"
										+ (position - 1))
										+ "");
						JSONObject json = applicationInfo.getJSONObject("info"
								+ (position - 1));
						intent.putExtra("Content", json.getString("Content"));
						intent.putExtra("UserID", json.getInt("UserID"));
						intent.putExtra("isTeamApplication", true);
						startActivity(intent);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		});

		lv_application.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				// 当不滚动时
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// 判断是否滚动到底部
					if (view.getLastVisiblePosition() == view.getCount() - 1) {
						// 加载更多功能的代码
						if (hasMore1) {
							//lv_application.addFooterView(footView1);
							new Thread(r_ApplicationList).start();
						} else {
							lv_application.removeFooterView(footView1);
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

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_teammessage_gifcontainer);
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
			currentPage1 = 0;
			sumPage1 = 0;
			hasMore1 = true;
			//lv_application.removeFooterView(footView1);
			//lv_application.addFooterView(footView1);
			tv_error.setVisibility(View.GONE);
			new Thread(r_ApplicationList).start();
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {

			// Call onRefreshComplete when the list has been refreshed.

			super.onPostExecute(result);
		}
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_teammessage_leftarrow:
				finish();
				break;
			/*
			 * case R.id.tv_teammessage_application: if (titleSelection == 1) {
			 * tv_application.setTextColor(getResources().getColor(
			 * R.color.blue));
			 * tv_invitation.setTextColor(getResources().getColor(
			 * R.color.darkGrey));
			 * 
			 * lv_application.setVisibility(View.VISIBLE);
			 * 
			 * 
			 * Animation animation = new TranslateAnimation( (float)
			 * (Data.SCREENWIDTH * 0.5), 0, 0, 0);
			 * animation.setFillAfter(true);// True:图片停在动画结束位置
			 * animation.setDuration(300); tv_cursor.startAnimation(animation);
			 * 
			 * titleSelection = 0;
			 * 
			 * } break; case R.id.tv_teammessage_invitation: if (titleSelection
			 * == 0) { tv_application.setTextColor(getResources().getColor(
			 * R.color.darkGrey));
			 * tv_invitation.setTextColor(getResources().getColor(
			 * R.color.blue));
			 * 
			 * lv_application.setVisibility(View.GONE);
			 * 
			 * Animation animation = new TranslateAnimation(0, (float)
			 * (Data.SCREENWIDTH * 0.5), 0, 0); animation.setFillAfter(true);//
			 * True:图片停在动画结束位置 animation.setDuration(300);
			 * tv_cursor.startAnimation(animation);
			 * 
			 * titleSelection = 1;
			 * 
			 * } break;
			 */
			}
		}
	};

	Runnable r_ApplicationList = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "TeamApplication"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("TeamID", teamID));

			String result = new HttpPostConnection("ApplicationServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			applicationListHandler.sendMessage(msg);
		}
	};

	Runnable r_ApplicationInfo = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			applicationLoadNum = 0;
			applicationInfo = new JSONObject();

			int j;
			if (currentPage1 < sumPage1)
				j = 10;
			else {
				j = lastPageItemNum1;
				hasMore1 = false;
			}

			for (int i = 0; i < j; i++) {
				int k = currentPage1 * 10 + i;
				RequestEachApplicationThread thread = new RequestEachApplicationThread(
						applicationID, k, j, i);
				pool.execute(thread);
			}

			currentPage1++;
		}
	};

	class RequestEachApplicationThread extends Thread {

		JSONObject applicationID;
		int i;
		int num;
		int order;

		RequestEachApplicationThread(JSONObject applicationID, int i, int num,
				int order) {
			this.applicationID = applicationID;
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
				params.add(new BasicNameValuePair("Request", "ApplicationInfo"));
				params.add(new BasicNameValuePair("ApplicationID",
						applicationID.getInt("ApplicationID" + i) + ""));

				String result = new HttpPostConnection("ApplicationServer",
						params).httpConnection();

				System.out.println(result);

				if (result.equals("timeout")) {
					msg.setData(b);
					timeoutHandler.sendMessage(msg);
					return;
				}

				allApplicationInfo.put("info" + i, new JSONObject(result));

				JSONObject json = new JSONObject(result);
				b.putString("info", json.toString());
				b.putInt("i", order);
				b.putInt("num", num);
				msg.setData(b);
				applicationinfoHandler.sendMessage(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	@SuppressLint("HandlerLeak")
	class ApplicationListHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();
			String result = b.getString("result");
			if (result.equals("timeout")) {
				rl_gifcontainer.setVisibility(View.GONE);
				Toast.makeText(TeammessageActivity.this, "网络出问题咯~>_<~+",
						Toast.LENGTH_SHORT).show();
				return;
			}

			try {
				applicationID = new JSONObject(result);

				int applicationNum = applicationID.getInt("ApplicationNum");
				if (applicationNum == 0) {
					lv_application.removeFooterView(footView1);
					rl_gifcontainer.setVisibility(View.GONE);
					mylist_application.clear();
					applicationAdapter.notifyDataSetChanged();
					mPullToRefreshListView_application.onRefreshComplete();
					tv_error.setVisibility(View.VISIBLE);
					mPullToRefreshListView_application.setVisibility(View.GONE);
				} else {
					sumPage1 = (int) (applicationNum / 10);
					if(sumPage1>1){
						lv_application.addFooterView(footView1);
					}
					lastPageItemNum1 = applicationNum - sumPage1 * 10;
					mylist_application.clear();
					allApplicationInfo = new JSONObject();

					new Thread(r_ApplicationInfo).start();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				rl_gifcontainer.setVisibility(View.GONE);
				mylist_application.clear();
				applicationAdapter.notifyDataSetChanged();
				mPullToRefreshListView_application.onRefreshComplete();
				Toast.makeText(TeammessageActivity.this, "身份验证过期，请重新登录",
						Toast.LENGTH_SHORT).show();
			}

		}
	}

	@SuppressLint("HandlerLeak")
	class ApplicationinfoHandler extends Handler {
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();

			if (b.getInt("num") < 10) {
				lv_application.removeFooterView(footView1);
			}

			try {
				int i = b.getInt("i");

				applicationInfo.put("info" + i,
						new JSONObject(b.getString("info")));

				applicationLoadNum++;
				if (applicationLoadNum >= b.getInt("num")) {

					for (int j = 0; j < b.getInt("num"); j++) {
						JSONObject json = applicationInfo.getJSONObject("info"
								+ j);

						HashMap<String, Object> map = new HashMap<String, Object>();
						if (json.has("UserPic")) {
							map.put("logo", json.getString("UserPic"));
							map.put("hasPath", true);
						} else {
							map.put("hasPath", false);
						}

						map.put("username", json.getString("UserName"));

						if (json.getString("Content").length() == 0) {
							map.put("content", "无");
						} else {
							map.put("content", json.getString("Content"));
						}

						map.put("result", json.getInt("Status"));
						map.put("date", json.getString("Date"));

						mylist_application.add(map);

					}

					rl_gifcontainer.setVisibility(View.GONE);

					applicationAdapter.notifyDataSetChanged();
					mPullToRefreshListView_application.onRefreshComplete();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	@SuppressLint("HandlerLeak")
	class TimeoutHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			rl_gifcontainer.setVisibility(View.GONE);
			Toast.makeText(TeammessageActivity.this, "网络出问题咯~>_<~+",
					Toast.LENGTH_SHORT).show();

		}
	}

	BroadcastReceiver refreshlistBroadcast = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			rl_gifcontainer.setVisibility(View.VISIBLE);
			currentPage1 = 0;
			sumPage1 = 0;
			hasMore1 = true;
			new Thread(r_ApplicationList).start();
		}
	};

	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(refreshlistBroadcast);
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

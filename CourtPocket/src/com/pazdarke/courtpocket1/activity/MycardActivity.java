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
import com.pazdarke.courtpocket1.tools.listview.MycardAdapter;
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
import android.view.Window;
import android.view.View.OnClickListener;
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

public class MycardActivity extends Activity {

	Intent intent;

	RelativeLayout rl_gifcontainer;

	ListView lv_list;
	PullToRefreshListView mPullToRefreshListView;
	ArrayList<HashMap<String, Object>> mylist;
	MycardAdapter adapter;

	JSONObject cardID;
	int sumPage = 0, currentPage = 0, lastPageItemNum;
	boolean hasMore = true;
	int loadNum;// 请求item为异步，判断是否加载完毕
	JSONObject cardInfo;
	JSONObject allCardInfo;
	TextView tv_error;

	View footView;

	ListHandler listHandler;
	CloseGifHandler closeGifHandler;
	AddFootviewHandler addFootviewHandler=new AddFootviewHandler();
	TimeoutHandler timeoutHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_mycard);

		AppManager.getAppManager().addActivity(this);

		IntentFilter filter = new IntentFilter("RefreshMycard");
		registerReceiver(broadcastReceiver, filter);

		initView();

		listHandler = new ListHandler();
		closeGifHandler = new CloseGifHandler();
		timeoutHandler = new TimeoutHandler();

		new Thread(r_GetCardID).start();

	}

	@SuppressLint("InflateParams")
	private void initView() {
		// TODO Auto-generated method stub
		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_mycard_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);
		
		tv_error=(TextView)findViewById(R.id.tv_mycard_error);

		mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_mycard_list);
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

	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_mycard_leftarrow:
				finish();
				break;
			}
		}
	};

	void init_list() {
		adapter = new MycardAdapter(MycardActivity.this, mylist);

		lv_list.setAdapter(adapter);
		lv_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Intent intent;
				try {
					JSONObject json = new JSONObject(allCardInfo
							.getString("info" + (position - 1)));
					int status = json.getInt("Status");
					switch (status) {
					case 1:
						intent = new Intent(MycardActivity.this,
								BuycardActivity.class);
						intent.putExtra("gymName", json.getString("GymName"));
						intent.putExtra("cardType", json.getInt("Type"));
						intent.putExtra("cardName", json.getString("Name"));
						intent.putExtra("content", json.getString("Content"));
						intent.putExtra("money", json.getInt("Price"));
						intent.putExtra("BillID",
								cardID.getString("CardID" + (position - 1)));
						intent.putExtra("isPayFromMycard", true);
						startActivity(intent);
						break;
					case 2:
						intent = new Intent(MycardActivity.this,
								MycardInfoActivity.class);
						intent.putExtra("info",
								allCardInfo.getString("info" + (position - 1)));
						intent.putExtra("CardID",
								cardID.getString("CardID" + (position - 1)));
						startActivity(intent);
						break;
					case 3:
						intent = new Intent(MycardActivity.this,
								CommitGymcommentActivity.class);
						intent.putExtra("info",
								allCardInfo.getString("info" + (position - 1)));
						intent.putExtra("billID",
								cardID.getString("CardID" + (position - 1)));
						intent.putExtra("isSwimming", true);
						startActivity(intent);
						break;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
							new Thread(r_GetEachPageCard).start();
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

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_mycard_gifcontainer);
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
			new Thread(r_GetCardID).start();
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {

			// Call onRefreshComplete when the list has been refreshed.

			super.onPostExecute(result);
		}
	}

	Runnable r_GetCardID = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "MyCard"));
			params.add(new BasicNameValuePair("UserID", MycardActivity.this
					.getIntent().getStringExtra("UserID")));
			params.add(new BasicNameValuePair("Passcode", MycardActivity.this
					.getIntent().getStringExtra("Passcode")));

			String result = new HttpPostConnection("CardServer", params)
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
				cardID = new JSONObject(result);
				allCardInfo = new JSONObject();
				sumPage = (int) (cardID.getInt("CardNum") / 10);
				lastPageItemNum = cardID.getInt("CardNum") - sumPage * 10;
				mylist.clear();
				if (cardID.getInt("CardNum") == 0) {
					Message msg = new Message();
					Bundle b = new Bundle();
					msg.setData(b);
					closeGifHandler.sendMessage(msg);
					return;
				} else if (cardID.getInt("CardNum") > 10) {
					Message msg = new Message();
					Bundle b = new Bundle();
					msg.setData(b);
					//addFootviewHandler.sendMessage(msg);
				}
				
				new Thread(r_GetEachPageCard).start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("result", "身份验证失败，请重新登录…");
				msg.setData(b);
				timeoutHandler.sendMessage(msg);
			}

		}
	};

	Runnable r_GetEachPageCard = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			loadNum = 0;
			cardInfo = new JSONObject();

			int j;
			if (currentPage < sumPage)
				j = 10;
			else {
				j = lastPageItemNum;
				hasMore = false;
			}

			for (int i = 0; i < j; i++) {
				int k = currentPage * 10 + i;
				RequestEachCardThread thread = new RequestEachCardThread(k, j,
						i);
				thread.start();
			}

			currentPage++;

		}

	};

	class RequestEachCardThread extends Thread {
		int i;
		int num;
		int order;

		RequestEachCardThread(int i, int num, int order) {
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
				params.add(new BasicNameValuePair("Request", "CardInfo"));
				params.add(new BasicNameValuePair("UserID", MycardActivity.this
						.getIntent().getStringExtra("UserID")));
				params.add(new BasicNameValuePair("Passcode",
						MycardActivity.this.getIntent().getStringExtra(
								"Passcode")));
				params.add(new BasicNameValuePair("CardID", cardID
						.getString("CardID" + i)));

				String result = new HttpPostConnection("CardServer", params)
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
				b.putString("result", "身份验证失败，请重新登录…");
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
				cardInfo.put("info" + i, new JSONObject(b.getString("info")));
				allCardInfo.put("info" + k, b.getString("info"));
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			loadNum++;
			if (loadNum >= b.getInt("num")) {
				for (int j = 0; j < b.getInt("num"); j++) {
					try {
						JSONObject json = cardInfo.getJSONObject("info" + j);

						HashMap<String, Object> map = new HashMap<String, Object>();

						map.put("Name", json.getString("Name"));

						map.put("Content", json.getString("Content"));

						map.put("Type", json.getInt("Type"));

						map.put("Price", json.getDouble("Price"));

						map.put("Status", json.getInt("Status"));

						map.put("Left", json.getInt("Left"));

						map.put("GymName", json.getString("GymName"));

						if (json.has("ValidityDate")) {
							map.put("ValidityDate",
									json.getString("ValidityDate"));
						} else {
							map.put("ValidityDate", "");
						}

						if (json.has("GenerateTime")) {
							map.put("GenerateTime",
									json.getString("GenerateTime").substring(0,
											19));
						} else {
							map.put("GenerateTime", "");
						}

						mylist.add(map);
					} catch (Exception e) {
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
			mPullToRefreshListView.setVisibility(View.GONE);
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
			Bundle b = msg.getData();

			Toast.makeText(MycardActivity.this, b.getString("result"),
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
				new Thread(r_GetCardID).start();
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

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
import com.pazdarke.courtpocket1.tools.listview.CardAdapter;
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
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;

public class CardActivity extends Activity {
	
	RelativeLayout rl_gifcontainer;
	
	String gymID;

	ListView lv_list;
	PullToRefreshListView mPullToRefreshListView;
	ArrayList<HashMap<String, Object>> mylist;
	JSONObject cardInfo;
	CardAdapter adapter;

	JSONObject cardID;
	int loadNum;// 请求item为异步，判断是否加载完毕
	
	ListHandler listHandler;
	TimeoutHandler timeoutHandler;
	CloseGifHandler closeGifHandler;
	
	public static Activity instance_card;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_card);
		
		instance_card=this;
		
		AppManager.getAppManager().addActivity(this);
		
		gymID=getIntent().getStringExtra("ID");
		
		initView();
		
		listHandler=new ListHandler();
		timeoutHandler=new TimeoutHandler();
		closeGifHandler=new CloseGifHandler();
		
		new Thread(r_RequestList).start();
		
	}

	private void initView() {
		// TODO Auto-generated method stub
		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_card_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_card_list);
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

		lv_list = mPullToRefreshListView.getRefreshableView();

		mylist = new ArrayList<HashMap<String, Object>>();
		
		init_list();
		init_gif();
		
	}
	
	void init_list() {
		adapter = new CardAdapter(CardActivity.this, mylist);

		lv_list.setAdapter(adapter);
		lv_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(CardActivity.this,
						BuycardActivity.class);
				try {
					JSONObject json = cardInfo.getJSONObject("info"
							+ (position - 1));
					intent.putExtra("gymName", json.getString("GymName"));
					intent.putExtra("cardType", json.getInt("Type"));
					intent.putExtra("cardName", json.getString("Name"));
					intent.putExtra("content", json.getString("Content"));
					intent.putExtra("money", json.getDouble("Price"));
					intent.putExtra("gymCardID",
							cardID.getInt("GymCardID" + (position - 1)) + "");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				startActivity(intent);
			}

		});

	}

	void init_gif() {
		GifView gif_loading = new GifView(this);
		gif_loading.setGifImage(R.drawable.gif_loading);
		gif_loading.setShowDimension((int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		gif_loading.setGifImageType(GifImageType.SYNC_DECODER);

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_card_gifcontainer);
		LayoutParams p = new LayoutParams((int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		p.addRule(RelativeLayout.CENTER_HORIZONTAL);
		p.setMargins(0, (int) (Data.SCREENHEIGHT * 0.22), 0, 0);
		rl_gifcontainer.addView(gif_loading, p);

	}
	
	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_card_leftarrow:
				finish();
				break;
			}
		}
	};
	
	// 一些在刷新的时候做的事情
	private class GetDataTask extends AsyncTask<Void, Void, String[]> {

		@Override
		protected String[] doInBackground(Void... params) {
			// Simulates a background job.
			new Thread(r_RequestList).start();
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
			params.add(new BasicNameValuePair("Request", "GymCard"));
			params.add(new BasicNameValuePair("GymID", gymID));

			try {

				String result = new HttpPostConnection("CardServer", params)
						.httpConnection();

				if (result.equals("timeout")) {
					Message msg = new Message();
					Bundle b = new Bundle();
					msg.setData(b);
					timeoutHandler.sendMessage(msg);
					return;
				}
		
				cardID = new JSONObject(result);
				mylist.clear();
				if(cardID.getInt("GymCardNum")==0){
					Message msg=new Message();
					Bundle b=new Bundle();
					msg.setData(b);
					closeGifHandler.sendMessage(msg);
					return;
				}
				new Thread(r_RequestAllCard).start();

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};

	Runnable r_RequestAllCard = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			loadNum = 0;
			cardInfo = new JSONObject();

			try {
				int j = cardID.getInt("GymCardNum");

				for (int i = 0; i < j; i++) {
					RequestEachCardThread thread = new RequestEachCardThread(
							 i, j, i);
					thread.start();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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
				params.add(new BasicNameValuePair("Request", "GymCardInfo"));
				params.add(new BasicNameValuePair("GymCardID", cardID.getInt("GymCardID"
						+ i)
						+ ""));

				String result = new HttpPostConnection("CardServer", params)
						.httpConnection();

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

			try {
				int i = b.getInt("i");

				cardInfo.put("info" + i, new JSONObject(b.getString("info")));

				loadNum++;
				if (loadNum >= b.getInt("num")) {

					for (int j = 0; j < b.getInt("num"); j++) {
						JSONObject json = cardInfo.getJSONObject("info" + j);

						HashMap<String, Object> map = new HashMap<String, Object>();
						
						map.put("Type", json.getInt("Type"));
						
						map.put("Name", json.getString("Name"));
						
						map.put("Content", json.getString("Content"));
						
						map.put("Price", json.getDouble("Price"));
						
						map.put("OPrice", json.getDouble("OPrice"));
						
						map.put("SellNum", json.getInt("SellNum"));
						
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

			Toast.makeText(CardActivity.this, "网络出问题咯(+﹏+)~",
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

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
import com.pazdarke.courtpocket1.tools.listview.FightlistAdapter;
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

public class FightlistActivity extends Activity {

	Intent intent;

	RelativeLayout rl_gifcontainer;

	ListView lv_list;
	PullToRefreshListView mPullToRefreshListView;
	ArrayList<HashMap<String, Object>> mylist;
	FightlistAdapter adapter;

	JSONObject billID;
	int sumPage = 0, currentPage = 0, lastPageItemNum;
	boolean hasMore = true;
	int loadNum;// ����itemΪ�첽���ж��Ƿ�������
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
		setContentView(R.layout.activity_fightlist);
		
		AppManager.getAppManager().addActivity(this);
		
		//instance_mybill=this;

		intent = getIntent();

		init_spinner();

		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_fightlist_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);
		
		tv_error=(TextView)findViewById(R.id.tv_fightlist_error);

		mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_fightlist_list);
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
		
		//IntentFilter filter = new IntentFilter("RefreshBilllist"); 
        //registerReceiver(broadcastReceiver, filter); 

		listHandler = new ListHandler();
		closeGifHandler = new CloseGifHandler();
		timeoutHandler = new TimeoutHandler();

		new Thread(r_GetBillID).start();

	}

	private void init_spinner() {
		// TODO Auto-generated method stub

		ArrayList<String> list = new ArrayList<String>();
		list.add("ȫ��");
		list.add("������");
		list.add("һ����");

		Spinner mSpinner = (Spinner) findViewById(R.id.sp_fightlist);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				FightlistActivity.this, R.layout.layout_mybill_spinner,
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
			case R.id.iv_fightlist_leftarrow:
				finish();
				break;
			}
		}
	};

	void init_list() {
		adapter = new FightlistAdapter(FightlistActivity.this, mylist,
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
				// ��������ʱ
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// �ж��Ƿ�������ײ�
					if (view.getLastVisiblePosition() == view.getCount() - 1) {
						// ���ظ��๦�ܵĴ���
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

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_fightlist_gifcontainer);
		LayoutParams p = new LayoutParams((int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		p.addRule(RelativeLayout.CENTER_HORIZONTAL);
		p.setMargins(0, (int) (Data.SCREENHEIGHT * 0.22), 0, 0);
		rl_gifcontainer.addView(gif_loading, p);

	}

	// һЩ��ˢ�µ�ʱ����������
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
			params.add(new BasicNameValuePair("Request", "SearchFight"));
			params.add(new BasicNameValuePair("Type", "1"));

			String result = new HttpPostConnection("FightServer", params)
					.httpConnection();

			if (result.equals("timeout")) {
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("result", "����������������...");
				msg.setData(b);
				timeoutHandler.sendMessage(msg);
				return;
			}

			try {
				billID = new JSONObject(result);
				allBillInfo = new JSONObject();
				sumPage = (int) (billID.getInt("FightNum") / 10);
				lastPageItemNum = billID.getInt("FightNum") - sumPage * 10;
				mylist.clear();
				if (billID.getInt("FightNum")==0) {
					Message msg = new Message();
					Bundle b = new Bundle();
					msg.setData(b);
					closeGifHandler.sendMessage(msg);
					return;
				} else if (billID.getInt("FightNum") > 10) {
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
				b.putString("result", "����ʧ��");
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
				params.add(new BasicNameValuePair("Request", "FightInfo"));
				params.add(new BasicNameValuePair("FightID", billID.getInt("FightID"+i)+""));

				String result = new HttpPostConnection("FightServer", params)
						.httpConnection();

				if (result.equals("timeout")) {
					b.putString("result", "����������������...");
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
				b.putString("result", "����ʧ��");
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

			int i = b.getInt("i");// ҳ��˳��
			int k = b.getInt("j");// ��˳��

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
						
						map.put("Status", 0);
						
						map.put("TeamName", json.getString("TeamName"));

						map.put("Time", json.getInt("Time"));
						map.put("Date", json.getString("Date"));
						
						map.put("GymName", json.getString("GymName"));
						
						map.put("CourtName", json.getString("CourtName"));
						
						map.put("Price", json.getDouble("Price"));
						
						map.put("TeamID", json.getInt("TeamID"));
						
						map.put("GymID", json.getInt("GymID"));
						
						map.put("weight", json.getInt("Weight"));
						
						map.put("GymType", 1);

						map.put("FightID", billID.getInt("FightID"+j));
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
			adapter.notifyDataSetChanged();
			mPullToRefreshListView.onRefreshComplete();
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

			Toast.makeText(FightlistActivity.this, b.getString("result"),
					Toast.LENGTH_SHORT).show();
			rl_gifcontainer.setVisibility(View.GONE);
			mPullToRefreshListView.onRefreshComplete();
		}
	}
	
	/*BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
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
	};*/

	protected void onDestroy() {
		super.onDestroy();
		//unregisterReceiver(broadcastReceiver);
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

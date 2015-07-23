package com.pazdarke.courtpocket1.activity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.ant.liao.GifView;
import com.ant.liao.GifView.GifImageType;
import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.pazdarke.courtpocket1.tools.listview.TeammemberlistAdapter;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

public class ManageTeammemberActivity extends Activity {
	
	ProgressDialog progressDialog;
	
	RelativeLayout rl_gifcontainer;
	
	Intent intent;
	String teamID;
	
	ArrayList<HashMap<String, Object>> mylist;
	TeammemberlistAdapter adapter;
	JSONObject userInfo;
	JSONObject userID;
	int loadNum=0;// 用于判断是否加载完毕

	UserlistHandler userlistHandler;
	UserinfoHandler userinfoHandler;
	TimeoutHandler timeoutHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_manage_teammember);
		
		AppManager.getAppManager().addActivity(this);
		
		intent=getIntent();
		teamID=intent.getStringExtra("TeamID");
		
		progressDialog=new ProgressDialog(this);
		progressDialog.setMessage("正在提交...");
		progressDialog.setCancelable(true);
		
		IntentFilter filter = new IntentFilter("RefreshManageTeammember"); 
        registerReceiver(refreshlistBroadcast, filter); 
		
		initView();
		
		userlistHandler=new UserlistHandler();
		userinfoHandler=new UserinfoHandler();
		timeoutHandler=new TimeoutHandler();
		
		new Thread(r_Userlist).start();
	}

	private void initView() {
		// TODO Auto-generated method stub
		
		ImageView iv_leftarrow=(ImageView)findViewById(R.id.iv_manageteammember_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);
		
		initGif();
		initList();

	}

	void initGif() {
		GifView gif_loading = new GifView(this);
		gif_loading.setGifImage(R.drawable.gif_loading);
		gif_loading.setShowDimension((int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		gif_loading.setGifImageType(GifImageType.SYNC_DECODER);

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_manageteammember_gifcontainer);
		LayoutParams p = new LayoutParams((int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		p.addRule(RelativeLayout.CENTER_HORIZONTAL);
		p.setMargins(0, (int) (Data.SCREENHEIGHT * 0.22), 0, 0);
		rl_gifcontainer.addView(gif_loading, p);

	}

	private void initList() {
		// TODO Auto-generated method stub
		mylist = new ArrayList<HashMap<String, Object>>();
		adapter = new TeammemberlistAdapter(this, mylist,
				R.layout.item_userinfo);

		ListView lv_list = (ListView) findViewById(R.id.lv_manageteammember_list);
		lv_list.setAdapter(adapter);

		/*lv_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				// TODO Auto-generated method stub
				
			}
		});*/
		
	}

	OnClickListener onClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.iv_manageteammember_leftarrow:
				finish();
				break;
			}
		}
	};
	
	Runnable r_Userlist=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "TeamMember"));
			params.add(new BasicNameValuePair("TeamID", teamID));

			String result = new HttpPostConnection("TeamServer", params)
					.httpConnection();

			System.out.println(result);
			
			Message msg=new Message();
			Bundle b=new Bundle();
			b.putString("result",result);
			msg.setData(b);
			userlistHandler.sendMessage(msg);
			
		}
	};

	class UserinfoThread extends Thread {

		int order;

		UserinfoThread(int order) {
			this.order = order;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("Request", "GetUserInfo"));

				params.add(new BasicNameValuePair("UserID", userID
						.getInt("UserID" + order) + ""));

				String result = new HttpPostConnection("UserInfoServer", params)
						.httpConnection();
				
				if(result.equals("timeout")){
					Message msg=new Message();
					Bundle b=new Bundle();
					msg.setData(b);
					timeoutHandler.sendMessage(msg);
					return;
				}
				
				userInfo.put("info"+order, result);
				
				Message msg=new Message();
				Bundle b=new Bundle();
				msg.setData(b);
				userinfoHandler.sendMessage(msg);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class UserlistHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			try {
				Bundle b = msg.getData();
				String result = b.getString("result");

				if (result.equals("timeout")) {
					rl_gifcontainer.setVisibility(View.GONE);
					Toast.makeText(ManageTeammemberActivity.this, "网络似乎出问题了...",
							Toast.LENGTH_SHORT).show();
					return;
				}

				userInfo=new JSONObject();
				
				userID = new JSONObject(result);
				for(int i=0;i<userID.getInt("UserNum");i++){
					new UserinfoThread(i).start();
				}
				

			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}

	class UserinfoHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			try {

				loadNum++;
				if (loadNum >= userID.getInt("UserNum")){
					
					for(int i=0;i<userID.getInt("UserNum");i++){
						
						String result=userInfo.getString("info"+i);
						
						HashMap<String, Object> map = new HashMap<String, Object>();
						
						JSONObject json=new JSONObject(result);
						if(json.has("Path")){
							map.put("hasPath", true);
							map.put("path", json.getString("Path"));
						}else{
							map.put("hasPath", false);
						}
						
						if(i==0){
							map.put("isLeader", true);
						} else{
							map.put("isLeader", false);
						}
						map.put("isDelete", true);
						
						map.put("username", json.getString("Name"));
						map.put("ratenum", json.getInt("RateNum"));
						if(json.getInt("RateNum")!=0){
							double f = json.getDouble("Rate");
							BigDecimal b1 = new BigDecimal(f);
							double f1 = b1.setScale(1, BigDecimal.ROUND_HALF_UP)
									.doubleValue();
							map.put("rate",f1);
						}
						
						if(json.has("Sex")){
							map.put("Sex", json.getInt("Sex"));
						} else{
							map.put("Sex", 2);
						}
						
						map.put("TeamID", teamID);
						map.put("oUserID", userID.getInt("UserID" + i) + "");
						
						if(json.has("Introduction")){
							map.put("introduction", "简介："+json.getString("Introduction"));
						} else{
							map.put("introduction", "未填写个人简介");
						}
						
						System.out.println("UserID"+i+":"+userID.getInt("UserID" + i));
						
						mylist.add(map);
					}
					
					adapter.notifyDataSetChanged();
					rl_gifcontainer.setVisibility(View.GONE);
				}

			} catch (Exception e) {
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
			Toast.makeText(ManageTeammemberActivity.this, "网络似乎出问题了...",
					Toast.LENGTH_SHORT).show();

		}
	}

	BroadcastReceiver refreshlistBroadcast = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			rl_gifcontainer.setVisibility(View.VISIBLE);
			mylist.clear();
			new Thread(r_Userlist).start();
		}
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

package com.pazdarke.courtpocket1.activity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.pazdarke.courtpocket1.tools.listview.AsyncViewTask;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class UserinfoActivity extends Activity {
	
	ProgressDialog progressDialog;
	
	Intent intent;
	
	UserinfoHandler userinfoHandler;
	ApplicationHandler applicationHandler;
	InviteHandler inviteHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_userinfo);
		
		AppManager.getAppManager().addActivity(this);
		
		progressDialog=new ProgressDialog(this);
		progressDialog.setMessage("正在加载中...");
		progressDialog.setCancelable(false);
		
		intent=getIntent();
		
		initView();
		
		userinfoHandler=new UserinfoHandler();
		applicationHandler=new ApplicationHandler();
		inviteHandler=new InviteHandler();
		
		progressDialog.show();
		
		new Thread(r_Userinfo).start();
		
	}

	private void initView() {
		// TODO Auto-generated method stub
		
		ImageView iv_leftarrow=(ImageView)findViewById(R.id.iv_userinfo_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);
		
		if(intent.getBooleanExtra("isTeamApplication", false)){
			
			TextView tv_content=(TextView)findViewById(R.id.tv_userinfo_content);
			tv_content.setVisibility(View.VISIBLE);
			tv_content.setText("验证消息："+intent.getStringExtra("Content"));
			
			LinearLayout ll_application=(LinearLayout)findViewById(R.id.ll_userinfo_application);
			ll_application.setVisibility(View.VISIBLE);
			
			Button btn_agree=(Button)findViewById(R.id.btn_userinfo_agreeapplication);
			btn_agree.setOnClickListener(onClickListener);
			
			Button btn_refuse=(Button)findViewById(R.id.btn_userinfo_refuseapplication);
			btn_refuse.setOnClickListener(onClickListener);
			
		} else if(intent.getBooleanExtra("isInvite", false)){
			Button btn_invite=(Button)findViewById(R.id.btn_userinfo_invite);
			btn_invite.setVisibility(View.VISIBLE);
			btn_invite.setOnClickListener(onClickListener);
		}
	}
	
	OnClickListener onClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.iv_userinfo_leftarrow:
				finish();
				break;
			case R.id.btn_userinfo_invite:
				progressDialog.show();
				new Thread(r_Invite).start();
				break;
			case R.id.btn_userinfo_agreeapplication:
				progressDialog.show();
				new ApplicationThread(1).start();
				break;
			case R.id.btn_userinfo_refuseapplication:
				progressDialog.show();
				new ApplicationThread(0).start();
				break;
			}
		}
	};
	
	Runnable r_Userinfo=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "GetUserInfo"));
			params.add(new BasicNameValuePair("UserID", intent.getIntExtra("UserID", 0)+""));

			String result = new HttpPostConnection("UserInfoServer", params)
					.httpConnection();
			
			Message msg=new Message();
			Bundle b=new Bundle();
			b.putString("result", result);
			msg.setData(b);
			userinfoHandler.sendMessage(msg);
		}
	};
	
	class ApplicationThread extends Thread{
		
		int Op;
		
		ApplicationThread(int Op){
			this.Op=Op;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "HandleApply"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("ApplicationID", intent.getStringExtra("ApplicationID")));
			params.add(new BasicNameValuePair("Op", Op+""));

			String result = new HttpPostConnection("ApplicationServer", params)
					.httpConnection();
			
			Message msg=new Message();
			Bundle b=new Bundle();
			b.putString("result", result);
			msg.setData(b);
			applicationHandler.sendMessage(msg);
		}
	}
	
	Runnable r_Invite=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "Invite"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("TeamID", intent.getStringExtra("TeamID")));
			params.add(new BasicNameValuePair("sUserID", intent.getIntExtra("UserID", 0)+""));

			String result = new HttpPostConnection("ApplicationServer", params)
					.httpConnection();
			
			Message msg=new Message();
			Bundle b=new Bundle();
			b.putString("result", result);
			msg.setData(b);
			inviteHandler.sendMessage(msg);
		}
	};

	@SuppressLint("HandlerLeak")
	class UserinfoHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			try {

				String result = msg.getData().getString("result");

				if (result.equals("timeout")) {
					Toast.makeText(UserinfoActivity.this, "网络异常，用户信息获取失败",
							Toast.LENGTH_SHORT).show();
					return;
				}

				JSONObject json = new JSONObject(result);
				if (json.has("Path")) {
					ImageView iv_logo = (ImageView) findViewById(R.id.iv_userinfo_logo);
					new AsyncViewTask(UserinfoActivity.this,
							json.getString("Path"), iv_logo,2).execute(iv_logo);// 异步加载图片
				}
				
				if (json.has("Sex")) {
					int sex=json.getInt("Sex");
					if(sex==0||sex==1){
						ImageView iv_sex = (ImageView) findViewById(R.id.iv_userinfo_sex);
						int[] ic_sex={R.drawable.ic_male,R.drawable.ic_female};
						iv_sex.setImageResource(ic_sex[sex]);
					}
				}
				
				TextView tv_name=(TextView)findViewById(R.id.tv_userinfo_username);
				tv_name.setText(json.getString("Name"));
				
				TextView tv_rate=(TextView)findViewById(R.id.tv_userinfo_rate);
				TextView tv_ratenum=(TextView)findViewById(R.id.tv_userinfo_ratenum);
				int ratenum=json.getInt("RateNum");
				if(ratenum==0){
					tv_rate.setText("0分");
					tv_ratenum.setText("(0人评分过)");
				} else{
					double f = json.getDouble("Rate");
					BigDecimal b1 = new BigDecimal(f);
					double f1 = b1.setScale(1, BigDecimal.ROUND_HALF_UP)
							.doubleValue();
					tv_rate.setText(f1+"分");
					tv_ratenum.setText("("+ratenum+"人评分过)");
				}
				
				TextView tv_introduction=(TextView)findViewById(R.id.tv_userinfo_introduction);
				if(json.has("Introduction")){
					tv_introduction.setText("个人简介："+json.getString("Introduction"));
				} else{
					tv_introduction.setText("暂无个人简介");
				}
					
				progressDialog.dismiss();

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	
	@SuppressLint("HandlerLeak")
	class ApplicationHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			try {

				progressDialog.dismiss();

				Bundle b = msg.getData();
				String result = b.getString("result");

				if (result.equals("timeout")) {
					Toast.makeText(UserinfoActivity.this, "网络出问题了~>_<~+",
							Toast.LENGTH_SHORT).show();
					return;
				} 
				
				result=new JSONObject(result).getString("Result");
				
				if (result.equals("操作成功")) {
					Toast.makeText(UserinfoActivity.this, "操作成功",
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent("RefreshTeammessage");
					sendBroadcast(intent);
					finish();
				} else {
					Toast.makeText(UserinfoActivity.this, result,
							Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	class InviteHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			try {

				progressDialog.dismiss();

				Bundle b = msg.getData();
				String result = b.getString("result");

				if (result.equals("timeout")) {
					Toast.makeText(UserinfoActivity.this, "网络出问题了~>_<~+",
							Toast.LENGTH_SHORT).show();
					return;
				} 
				
				result=new JSONObject(result).getString("Result");
				
				if (result.equals("邀请成功")) {
					Toast.makeText(UserinfoActivity.this, "邀请成功",
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent("RefreshTeammessage");
					sendBroadcast(intent);
					InviteActivity.instance_Invite.finish();
					finish();
				} else {
					Toast.makeText(UserinfoActivity.this, result,
							Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
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

package com.pazdarke.courtpocket1.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class InviteActivity extends Activity {
	
	ProgressDialog progressDialog;
	
	EditText et_content;

	InviteHandler inviteHandler;
	
	public static Activity instance_Invite;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_invite);
		
		AppManager.getAppManager().addActivity(this);
		
		instance_Invite=this;
		
		progressDialog=new ProgressDialog(this);
		progressDialog.setMessage("正在搜索...");
		progressDialog.setCancelable(false);
		
		initView();
		
		inviteHandler=new InviteHandler();
		
	}

	private void initView() {
		// TODO Auto-generated method stub
		
		ImageView iv_leftarrow=(ImageView)findViewById(R.id.iv_invite_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);
		
		et_content=(EditText)findViewById(R.id.et_invite_content);
		
		ImageView iv_clear=(ImageView)findViewById(R.id.iv_invite_clear);
		iv_clear.setOnClickListener(onClickListener);
		
		TextView tv_commit=(TextView)findViewById(R.id.tv_invite_commit);
		tv_commit.setOnClickListener(onClickListener);
	}
	
	OnClickListener onClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.iv_invite_leftarrow:
				finish();
				break;
			case R.id.iv_invite_clear:
				et_content.setText("");
				break;
			case R.id.tv_invite_commit:
				if(!et_content.getText().toString().equals("")){
					progressDialog.show();
					new Thread(r_Invite).start();
				}
				break;
			}
		}
	};
	
	Runnable r_Invite=new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "SearchUser"));
			params.add(new BasicNameValuePair("Data", et_content.getText()
					.toString()));

			String result = new HttpPostConnection("UserInfoServer", params)
					.httpConnection();
			
			Message msg=new Message();
			Bundle b=new Bundle();
			b.putString("result", result);
			msg.setData(b);
			inviteHandler.sendMessage(msg);
		}
	};
	
	class InviteHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			try {

				progressDialog.dismiss();

				String result = msg.getData().getString("result");

				if (result.equals("timeout")) {
					Toast.makeText(InviteActivity.this, "网络似乎有问题呢",
							Toast.LENGTH_SHORT).show();
					return;
				}

				JSONObject json = new JSONObject(result);
				if(json.getInt("UserNum")==0){
					Toast.makeText(InviteActivity.this, "没有搜索结果",
							Toast.LENGTH_SHORT).show();
				} else{
					Intent intent=new Intent(InviteActivity.this,InvitelistActivity.class);
					intent.putExtra("UserInfo", json.toString());
					intent.putExtra("TeamID", InviteActivity.this.getIntent().getStringExtra("ID"));
					intent.putExtra("isInvite", true);
					startActivity(intent);
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

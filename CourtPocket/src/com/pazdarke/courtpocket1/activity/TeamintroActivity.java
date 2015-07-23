package com.pazdarke.courtpocket1.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TeamintroActivity extends Activity {
	
	ProgressDialog progressDialog;
	
	String teamID,introduction;
	EditText et_content;
	
	UpdateHandler updateHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_teamintro);
		
		AppManager.getAppManager().addActivity(this);
		
		teamID=getIntent().getStringExtra("TeamID");
		introduction=getIntent().getStringExtra("Introduction");
		
		progressDialog=new ProgressDialog(this);
		progressDialog.setCancelable(true);
		progressDialog.setMessage("正在提交...");
		
		initView();
		
		updateHandler=new UpdateHandler();
		
	}

	private void initView() {
		// TODO Auto-generated method stub
		et_content=(EditText)findViewById(R.id.et_teamintro_content);	
		et_content.setText(introduction);
		//Selection.selectAll(et_content.getText());
		
		ImageView iv_clear=(ImageView)findViewById(R.id.iv_teamintro_clear);
		iv_clear.setOnClickListener(onClickListener);
		
		ImageView iv_leftarrow=(ImageView)findViewById(R.id.iv_teamintro_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);
		
		TextView tv_commit=(TextView)findViewById(R.id.tv_teamintro_commit);
		tv_commit.setOnClickListener(onClickListener);
		
	}
	
	OnClickListener onClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.iv_teamintro_leftarrow:
				finish();
				break;
			case R.id.iv_teamintro_clear:
				et_content.setText("");
				break;
			case R.id.tv_teamintro_commit:
				if(!et_content.getText().toString().equals("")){
					progressDialog.show();
					new Thread(r_Update).start();
				}
				break;
			}
		}
	};
	
	Runnable r_Update=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "UpdateIntroduction"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("TeamID", teamID));
			params.add(new BasicNameValuePair("Introduction", et_content.getText().toString()));

			String result = new HttpPostConnection("TeamServer", params)
					.httpConnection();
			
			Message msg=new Message();
			Bundle b=new Bundle();
			b.putString("result", result);
			msg.setData(b);
			updateHandler.sendMessage(msg);
		}
	};
	
	@SuppressLint("HandlerLeak")
	class UpdateHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			progressDialog.dismiss();
			
			String result=msg.getData().getString("result");
			
			if(result.equals("timeout")){
				Toast.makeText(TeamintroActivity.this, "网络出问题了", Toast.LENGTH_SHORT).show();
				return;
			}
			
			try {
				JSONObject json=new JSONObject(result);
				Toast.makeText(TeamintroActivity.this, json.getString("Result"), Toast.LENGTH_SHORT).show();
				
				if(json.getString("Result").equals("操作成功")){
					Intent intent=new Intent("RefreshTeaminfo");
					sendBroadcast(intent);
					finish(); 
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
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

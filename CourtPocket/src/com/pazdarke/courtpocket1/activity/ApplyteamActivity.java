package com.pazdarke.courtpocket1.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.R.id;
import com.pazdarke.courtpocket1.R.layout;
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

public class ApplyteamActivity extends Activity {
	
	EditText et_content;
	
	ProgressDialog progressDialog;
	
	ApplyHandler applyHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_applyteam);
		
		AppManager.getAppManager().addActivity(this);
		
		progressDialog=new ProgressDialog(this);
		progressDialog.setMessage("正在努力提交请求中…");
		progressDialog.setCancelable(false);
		
		ImageView iv_leftarrow=(ImageView)findViewById(R.id.iv_applyteam_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);
		
		et_content=(EditText)findViewById(R.id.et_applyteam_content);
		
		TextView tv_commit=(TextView)findViewById(R.id.tv_applyteam_commit);
		tv_commit.setOnClickListener(onClickListener);
		
		applyHandler=new ApplyHandler();
		
	}
	
	OnClickListener onClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.iv_applyteam_leftarrow:
				finish();
				break;
			case R.id.tv_applyteam_commit:
				if(et_content.getText().toString().equals("")){
					Toast.makeText(ApplyteamActivity.this, "请输入验证消息", Toast.LENGTH_SHORT).show();
				} else{
					progressDialog.show();
					new Thread(r_Apply).start();
				}
				break;
			}
		}
	};
	
	Runnable r_Apply=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "Apply"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("TeamID", ApplyteamActivity.this.getIntent().getStringExtra("ID")));
			params.add(new BasicNameValuePair("Content", et_content.getText().toString()));
			
			String result = new HttpPostConnection("ApplicationServer", params)
					.httpConnection();
			
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			applyHandler.sendMessage(msg);
		}
	};
	
	@SuppressLint("HandlerLeak")
	class ApplyHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			progressDialog.dismiss();
			
			Bundle b=msg.getData();
			String result=b.getString("result");
			
			if(result.equals("timeout")){
				Toast.makeText(ApplyteamActivity.this, "网络出问题啦~>_<~+", Toast.LENGTH_SHORT).show();
				return;
			}
			
			try{
				result=new JSONObject(result).getString("Result");
				
				if(result.equals("申请成功")){
					Toast.makeText(ApplyteamActivity.this, "申请成功(^_^)∠※", Toast.LENGTH_SHORT).show();
					Intent intent=new Intent();
					intent.putExtra("isApply", true);
					setResult(RESULT_OK, intent);
					finish();
				} else{
					Toast.makeText(ApplyteamActivity.this, result, Toast.LENGTH_SHORT).show();
				}
			}catch(Exception e){
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

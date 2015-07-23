package com.pazdarke.courtpocket1.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.MD5.Encrypt;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	
	ProgressDialog progressDialog;

	ImageView iv_leftarrow;
	Button btn_register;
	Button btn_sendkey;

	EditText et_phone, et_password, et_key;

	int i = 60;// 用来读秒

	TimeHandler timeHandler;
	KeyHandler keyHandler;
	RegisterHandler registerHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_register);
		
		AppManager.getAppManager().addActivity(this);
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("玩命加载数据中...");
		progressDialog.setCancelable(true);

		iv_leftarrow = (ImageView) findViewById(R.id.iv_register_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		et_phone = (EditText) findViewById(R.id.et_register_phone);
		
		et_password = (EditText) findViewById(R.id.et_register_password);
		
		et_key=(EditText)findViewById(R.id.et_register_key);

		btn_register = (Button) findViewById(R.id.btn_register_register);
		btn_register.setOnClickListener(onClickListener);

		btn_sendkey = (Button) findViewById(R.id.btn_register_sendkey);
		btn_sendkey.setOnClickListener(onClickListener);
		
		TextView tv_law=(TextView)findViewById(R.id.tv_register_law);
		tv_law.setOnClickListener(onClickListener);

		timeHandler = new TimeHandler();
		keyHandler =new KeyHandler();
		registerHandler=new RegisterHandler();

	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_register_leftarrow:
				finish();
				break;
			case R.id.btn_register_sendkey:
				String phone=et_phone.getText().toString();
				if(phone.length()!=11){
					Toast.makeText(RegisterActivity.this, "手机号输入有误",
							Toast.LENGTH_SHORT).show();
				} else{
					i = 60;
					btn_sendkey.setClickable(false);
					new Thread(r_sendkey).start();
				}
				break;
			case R.id.btn_register_register:
				String password=et_password.getText().toString();
				String key=et_key.getText().toString();
				if(password.length()<6){
					Toast.makeText(RegisterActivity.this, "密码不能少于6位", Toast.LENGTH_SHORT).show();
				} else if(key.length()!=4){
					Toast.makeText(RegisterActivity.this, "请填写正确的验证码", Toast.LENGTH_SHORT).show();
				} else{
					progressDialog.show();
					new Thread(r_register).start();
				}
				break;
			case R.id.tv_register_law:
				Intent intent=new Intent(RegisterActivity.this,RegisterLawActivity.class);
				startActivity(intent);
				break;
			}
		}
	};

	Runnable r_time = new Runnable() {

		@SuppressLint("HandlerLeak")
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				if (i > 0) {
					i--;
					Bundle b = new Bundle();
					Message msg = new Message();
					msg.setData(b);
					timeHandler.sendMessage(msg);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					Bundle b = new Bundle();
					Message msg = new Message();
					msg.setData(b);
					timeHandler.sendMessage(msg);
					break;
				}
			}
		}
	};
	
	Runnable r_sendkey=new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "GetSignUpCode"));
			params.add(new BasicNameValuePair("Phone", et_phone.getText().toString()));
			
			String result=new HttpPostConnection("SignServer",params).httpConnection();
			
			if(result.equals("timeout")){
				JSONObject json=new JSONObject();
				try {
					json.put("Result", "timeout");
					result=json.toString();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			Message msg=new Message();
			Bundle b=new Bundle();
			b.putString("result", result);
			msg.setData(b);
			keyHandler.sendMessage(msg);
			
		}
		
	};
	
	Runnable r_register=new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "SignUp"));
			params.add(new BasicNameValuePair("Phone", et_phone.getText().toString()));
			// 手机号+密码 MD5混合加密
			params.add(new BasicNameValuePair("Password", Encrypt.MD5(et_phone
					.getText().toString() + et_password.getText().toString())));
			params.add(new BasicNameValuePair("Code", et_key.getText().toString()));
			
			String result=new HttpPostConnection("SignServer",params).httpConnection();
			
			if(result.equals("timeout")){
				JSONObject json=new JSONObject();
				try {
					json.put("Result", "timeout");
					result=json.toString();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			Message msg=new Message();
			Bundle b=new Bundle();
			b.putString("result", result);
			msg.setData(b);
			registerHandler.sendMessage(msg);
			
		}
		
	};

	@SuppressLint("HandlerLeak")
	class TimeHandler extends Handler {
		@SuppressWarnings("deprecation")
		@SuppressLint("NewApi")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			if (i > 0) {
				btn_sendkey.setBackgroundColor(getResources().getColor(
						R.color.grey));
				btn_sendkey.setTextColor(getResources().getColor(
						R.color.darkGrey));
				btn_sendkey.setText("重新发送(" + i + "s)");
			} else {
				btn_sendkey.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.selector_drawable_register_sendkey));
				btn_sendkey.setClickable(true);
				btn_sendkey.setText("重新发送");
				btn_sendkey.setTextColor(getResources().getColor(R.color.blue));
			}

		}
	}
	
	class KeyHandler extends Handler{
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			Bundle b=msg.getData();
			JSONObject json;
			String result = null;
			try {
				json = new JSONObject(b.getString("result"));
				result=json.getString("Result");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(result.equals("验证码已发送")){
				Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_SHORT).show();
				new Thread(r_time).start();
			} else if(result.equals("验证码发送失败")){
				Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_SHORT).show();
			} else{
				Toast.makeText(RegisterActivity.this, "验证短信发送失败，请检查您的网络设置", Toast.LENGTH_SHORT).show();
			}
			
		}
	}
	
	@SuppressLint("HandlerLeak")
	class RegisterHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			progressDialog.dismiss();
			
			Bundle b=msg.getData();
			JSONObject json;
			String result = null;
			try {
				json = new JSONObject(b.getString("result"));
				result=json.getString("Result");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_SHORT).show();
			
			if(result.equals("注册成功")){
				Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_SHORT).show();
				finish();
			} else if(result.equals("timeout")){
				Toast.makeText(RegisterActivity.this, "验证短信发送失败，请检查您的网络设置", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_SHORT).show();
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

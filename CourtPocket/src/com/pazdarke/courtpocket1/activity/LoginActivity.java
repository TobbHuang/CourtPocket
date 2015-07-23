package com.pazdarke.courtpocket1.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.httpConnection.HttpGetConnection;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.MD5.Encrypt;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

	ProgressDialog progressDialog;

	ImageView iv_leftarrow;
	TextView tv_register, tv_resetpw;
	EditText et_phone, et_password;
	Button btn_login;

	Tencent mTencent;
	WeiboAuth mWeiboAuth;
	SsoHandler mSsoHandler;

	// 第三方登录数据
	String access_token;
	String uid;
	String way;
	String code;// 傲娇的微信专用

	LoginHandler loginHandler;
	TLoginHandler tLoginHandler;
	GetWeiboUidHandler getWeiboUidHandler;
	GetWechatIdHandler getWechatIdHandler;
	
	IWXAPI wechatapi;
	
	public static Activity instance_login;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);

		AppManager.getAppManager().addActivity(this);
		
		// 微信初始化
		wechatapi=WXAPIFactory.createWXAPI(this, Data.WECHATAPPID, true);
		wechatapi.registerApp(Data.WECHATAPPID);
		
		instance_login=this;

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("加载中，请稍候...");
		progressDialog.setCancelable(true);
		
		IntentFilter filter = new IntentFilter("WechatLogin"); 
        registerReceiver(wechatloginBroadcast, filter); 

		iv_leftarrow = (ImageView) findViewById(R.id.iv_login_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		tv_register = (TextView) findViewById(R.id.tv_login_register);
		tv_register.setOnClickListener(onClickListener);

		tv_resetpw = (TextView) findViewById(R.id.tv_login_resetpw);
		tv_resetpw.setOnClickListener(onClickListener);

		et_phone = (EditText) findViewById(R.id.et_login_phone);

		et_password = (EditText) findViewById(R.id.et_login_password);

		btn_login = (Button) findViewById(R.id.btn_login_login);
		btn_login.setOnClickListener(onClickListener);

		RelativeLayout rl_wechat = (RelativeLayout) findViewById(R.id.rl_login_wechat);
		rl_wechat.setOnClickListener(onClickListener);

		RelativeLayout rl_qq = (RelativeLayout) findViewById(R.id.rl_login_qq);
		rl_qq.setOnClickListener(onClickListener);

		RelativeLayout rl_weibo = (RelativeLayout) findViewById(R.id.rl_login_weibo);
		rl_weibo.setOnClickListener(onClickListener);

		loginHandler = new LoginHandler();
		tLoginHandler = new TLoginHandler();
		getWeiboUidHandler = new GetWeiboUidHandler();
		getWechatIdHandler=new GetWechatIdHandler();

	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_login_leftarrow:
				finish();
				break;
			case R.id.tv_login_register:
				startActivity(new Intent(LoginActivity.this,
						RegisterActivity.class));
				break;
			case R.id.tv_login_resetpw:
				startActivity(new Intent(LoginActivity.this,
						ResetpwActivity.class));
				break;
			case R.id.btn_login_login:
				if (et_phone.getText().toString().length() != 11) {
					Toast.makeText(LoginActivity.this, "手机号输入有误",
							Toast.LENGTH_SHORT).show();
				} else {
					new Thread(r_login).start();
				}
				break;
			case R.id.rl_login_wechat:
				way="Wechat";
				progressDialog.show();
				
				if (wechatapi.isWXAppInstalled()) {
					SendAuth.Req req = new SendAuth.Req();
					req.openId = Data.WECHATAPPID;
					req.scope = "snsapi_userinfo";
					req.state = "CourtPocket";
					wechatapi.sendReq(req);
				} else {
					progressDialog.dismiss();
					Toast.makeText(LoginActivity.this, "您暂未安装最新版微信客户端",
							Toast.LENGTH_SHORT).show();
				}
				
				break;
			case R.id.rl_login_qq:
				way="QQ";
				progressDialog.show();
				if (mTencent == null) {
					mTencent = Tencent.createInstance("1104718886",
							LoginActivity.this.getApplicationContext());
				}
				BaseUiListener listener = new BaseUiListener();
				mTencent.login(LoginActivity.this, "get_simple_userinfo",
							listener);
				
				break;
			case R.id.rl_login_weibo:
				way="Sina";
				progressDialog.show();
				mWeiboAuth = new WeiboAuth(LoginActivity.this, "1901486666",
						"https://api.weibo.com/oauth2/default.html",
						"account/get_uid");
				mSsoHandler = new SsoHandler(LoginActivity.this, mWeiboAuth);
				mSsoHandler.authorize(new AuthListener());
				break;
			}
		}
	};

	Runnable r_login = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String phone = et_phone.getText().toString();
			String password = et_password.getText().toString();

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "Login"));
			params.add(new BasicNameValuePair("Phone", phone));
			params.add(new BasicNameValuePair("Password", Encrypt.MD5(phone
					+ password)));

			String result = new HttpPostConnection("SignServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			loginHandler.sendMessage(msg);

		}
	};

	Runnable r_getWeiboUid = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String result = new HttpGetConnection()
					.httpConnectionFullUrl("https://api.weibo.com/2/account/get_uid.json?access_token="
							+ access_token);

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			getWeiboUidHandler.sendMessage(msg);

		}
	};

	Runnable r_getWechatId = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String result = new HttpGetConnection()
					.httpConnectionFullUrl("https://api.weixin.qq.com/sns/oauth2/access_token?appid="
							+ Data.WECHATAPPID
							+ "&secret="
							+ "9128371a3aa9c3891c3ed9e4abc9484c"
							+ "&code="
							+ code + "&grant_type=authorization_code");
			
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			getWechatIdHandler.sendMessage(msg);
		}
	};

	class TLoginThread extends Thread {

		String way;
		String UID;

		TLoginThread(String way, String UID) {
			this.way = way;
			this.UID = UID;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "TLogin"));
			params.add(new BasicNameValuePair("Way", way));
			params.add(new BasicNameValuePair("UID", UID));

			String result = new HttpPostConnection("SignServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			tLoginHandler.sendMessage(msg);
		}
	}

	class LoginHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();
			try {
				JSONObject json = new JSONObject(b.getString("result"));

				String result = json.getString("Result");
				// Toast.makeText(LoginActivity.this, result,
				// Toast.LENGTH_SHORT).show();
				if (result.equals("登录成功")) {
					Data.ISLOGIN = true;
					Data.USERID = json.getString("UserID");
					Data.PASSCODE = json.getString("Passcode");
					Data.PHONE = et_phone.getText().toString();

					// 帐号注册信鸽推送
					XGPushManager.registerPush(LoginActivity.this, "account"
							+ json.getString("UserID"));

					SharedPreferences sp = PreferenceManager
							.getDefaultSharedPreferences(LoginActivity.this);
					Editor pEdit = sp.edit();
					pEdit.putBoolean("isLogin", true);
					pEdit.putString("UserID", Data.USERID);
					pEdit.putString("Passcode", Data.PASSCODE);
					pEdit.putString("Phone", Data.PHONE);
					pEdit.commit();

					setResult(RESULT_OK, LoginActivity.this.getIntent());
					finish();
				} else {
					Toast.makeText(LoginActivity.this, result,
							Toast.LENGTH_SHORT).show();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	class GetWeiboUidHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			String result = msg.getData().getString("result");

			if (result.equals("timeout")) {
				progressDialog.dismiss();
				Toast.makeText(LoginActivity.this, "网络出问题了...",
						Toast.LENGTH_SHORT).show();
				return;
			}

			try {
				JSONObject json = new JSONObject(result);

				if (json.has("uid")) {
					uid = json.getString("uid");
					
					new TLoginThread("Sina", uid).start();
					
				} else {
					progressDialog.dismiss();
					Toast.makeText(LoginActivity.this, json.getString("error"),
							Toast.LENGTH_SHORT).show();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	class GetWechatIdHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			String result = msg.getData().getString("result");

			if (result.equals("timeout")) {
				progressDialog.dismiss();
				Toast.makeText(LoginActivity.this, "网络出问题了...",
						Toast.LENGTH_SHORT).show();
				return;
			}

			try {
				JSONObject json = new JSONObject(result);

				if (json.has("access_token")) {
					access_token = json.getString("access_token");
					uid=json.getString("openid");
					
					new TLoginThread("Wechat", uid).start();
					
				} else {
					progressDialog.dismiss();
					Toast.makeText(LoginActivity.this, json.getString("errcode")+" "+json.getString("errmsg"),
							Toast.LENGTH_SHORT).show();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	class TLoginHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			String result = msg.getData().getString("result");

			if (result.equals("timeout")) {
				progressDialog.dismiss();
				Toast.makeText(LoginActivity.this, "网络出问题了...",
						Toast.LENGTH_SHORT).show();
				return;
			}

			try {
				JSONObject json = new JSONObject(result);
				
				int userID = json.getInt("UserID");
				if (userID == -1) {
					Intent intent=new Intent(LoginActivity.this,BindActivity.class);
					intent.putExtra("access_token", access_token);
					intent.putExtra("uid", uid);
					intent.putExtra("way", way);
					startActivity(intent);
					progressDialog.dismiss();
				} else {
					progressDialog.dismiss();
					Data.USERID = userID + "";
					Data.PASSCODE = json.getString("Passcode");
					Data.ISLOGIN = true;
					Data.PHONE = json.getString("Phone");

					// 帐号注册信鸽推送
					XGPushManager.registerPush(LoginActivity.this, "account"
							+ userID);

					SharedPreferences sp = PreferenceManager
							.getDefaultSharedPreferences(LoginActivity.this);
					Editor pEdit = sp.edit();
					pEdit.putBoolean("isLogin", true);
					pEdit.putString("UserID", Data.USERID);
					pEdit.putString("Passcode", Data.PASSCODE);
					pEdit.putString("Phone", Data.PHONE);
					pEdit.commit();

					setResult(RESULT_OK, LoginActivity.this.getIntent());
					finish();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	// QQ登录
	class BaseUiListener implements IUiListener {

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
		}

		@Override
		public void onComplete(Object arg0) {
			// TODO Auto-generated method stub
			try{
				JSONObject json=new JSONObject(arg0.toString());
				
				if(json.getInt("ret")==0){
					uid=json.getString("openid");
					access_token=json.getString("access_token");
					
					new TLoginThread("QQ", uid).start();
					
				} else{
					Toast.makeText(LoginActivity.this, json.getString("msg"),
							Toast.LENGTH_SHORT).show();
					progressDialog.dismiss();
				}
				
			} catch(Exception e){
				e.printStackTrace();
			}
		}

		@Override
		public void onError(UiError arg0) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
		}

	}

	// 微博登录
	class AuthListener implements WeiboAuthListener {

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
		}

		@Override
		public void onComplete(Bundle values) {
			// TODO Auto-generated method stub
			Oauth2AccessToken mAccessToken = Oauth2AccessToken
					.parseAccessToken(values);
			if (mAccessToken.isSessionValid()) {
				System.out.println("weibo login success");
				System.out.println(mAccessToken.getToken());

				access_token = mAccessToken.getToken();
				new Thread(r_getWeiboUid).start();
			} else {
				progressDialog.dismiss();
				String code = values.getString("code", "");
				Toast.makeText(LoginActivity.this, "微博登录失败 code:" + code,
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// SSO 授权回调
		// 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResult
		if (mSsoHandler != null) {
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}
	
	BroadcastReceiver wechatloginBroadcast=new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getIntExtra("result", 0) == 0) {
				code=intent.getStringExtra("code");
				
				new Thread(r_getWechatId).start();
				
			} else{
				progressDialog.dismiss();
			}
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

package com.pazdarke.courtpocket1.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.R.id;
import com.pazdarke.courtpocket1.R.layout;
import com.pazdarke.courtpocket1.R.menu;
import com.pazdarke.courtpocket1.activity.ResetpwActivity.KeyHandler;
import com.pazdarke.courtpocket1.activity.ResetpwActivity.TimeHandler;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.httpConnection.HttpGetConnection;
import com.pazdarke.courtpocket1.httpConnection.HttpGetPicConnection;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.pazdarke.courtpocket1.tools.pic.BitmapToString;
import com.tencent.android.tpush.XGPushManager;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class BindActivity extends Activity {

	ProgressDialog progressDialog;

	EditText et_phone;
	EditText et_key;
	Button btn_sendkey;
	CheckBox cb_info;

	int i = 60;// 用来读秒

	String nickname = "", introduction = "", picstr = "";
	String access_token, uid, way;

	TimeHandler timeHandler;
	KeyHandler keyHandler;
	BindHandler bindHandler;
	GetInfoHandler getInfoHandler;
	TLoginHandler tLoginHandler;
	HeadpicHandler headpicHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_bind);

		AppManager.getAppManager().addActivity(this);

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("玩命加载数据中...");
		progressDialog.setCancelable(true);

		initView();

		timeHandler = new TimeHandler();
		keyHandler = new KeyHandler();
		bindHandler = new BindHandler();
		getInfoHandler = new GetInfoHandler();
		tLoginHandler = new TLoginHandler();
		headpicHandler = new HeadpicHandler();

	}

	private void initView() {
		// TODO Auto-generated method stub

		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_bind_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		et_phone = (EditText) findViewById(R.id.et_bind_phone);
		et_key = (EditText) findViewById(R.id.et_bind_key);

		TextView tv_submit = (TextView) findViewById(R.id.tv_bind_submit);
		tv_submit.setOnClickListener(onClickListener);

		btn_sendkey = (Button) findViewById(R.id.btn_bind_sendkey);
		btn_sendkey.setOnClickListener(onClickListener);

		cb_info = (CheckBox) findViewById(R.id.cb_bind_info);

		Intent intent = getIntent();
		access_token = intent.getStringExtra("access_token");
		uid = intent.getStringExtra("uid");
		way = intent.getStringExtra("way");

	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_bind_leftarrow:
				finish();
				break;
			case R.id.btn_bind_sendkey:
				String phone = et_phone.getText().toString();
				if (phone.length() != 11) {
					Toast.makeText(BindActivity.this, "手机号输入有误",
							Toast.LENGTH_SHORT).show();
				} else {
					i = 60;
					btn_sendkey.setClickable(false);
					new Thread(r_sendkey).start();
				}
				break;
			case R.id.tv_bind_submit:
				progressDialog.show();
				if (cb_info.isChecked()) {
					new Thread(r_getInfo).start();
				} else {
					new Thread(r_bind).start();
				}
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

	Runnable r_sendkey = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "GetSignUpCode"));
			params.add(new BasicNameValuePair("Phone", et_phone.getText()
					.toString()));

			String result = new HttpPostConnection("SignServer", params)
					.httpConnection();

			if (result.equals("timeout")) {
				JSONObject json = new JSONObject();
				try {
					json.put("Result", "timeout");
					result = json.toString();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			keyHandler.sendMessage(msg);

		}

	};

	Runnable r_bind = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "Bind"));
			params.add(new BasicNameValuePair("Way", way));
			params.add(new BasicNameValuePair("UID", uid));
			params.add(new BasicNameValuePair("Phone", et_phone.getText()
					.toString()));
			params.add(new BasicNameValuePair("Code", et_key.getText()
					.toString()));
			if (cb_info.isChecked()) {
				if (!introduction.equals(""))
					params.add(new BasicNameValuePair("Introduction",
							introduction));
				if (!nickname.equals(""))
					params.add(new BasicNameValuePair("Nickname", nickname));
				if (!picstr.equals(""))
					params.add(new BasicNameValuePair("PicStr", picstr));
			}

			String result = new HttpPostConnection("SignServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			bindHandler.sendMessage(msg);

		}
	};

	Runnable r_getInfo = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String result = null;
			if (way.equals("Sina")) {
				result = new HttpGetConnection()
						.httpConnectionFullUrl("https://api.weibo.com/2/users/show.json?access_token="
								+ access_token + "&uid=" + uid);
			} else if (way.equals("QQ")) {
				result = new HttpGetConnection()
						.httpConnectionFullUrl("https://graph.qq.com/user/get_simple_userinfo?access_token="
								+ access_token
								+ "&openid="
								+ uid
								+ "&oauth_consumer_key=" + "1104718886");
			} else if (way.equals("Wechat")) {
				result = new HttpGetConnection()
						.httpConnectionFullUrl("https://api.weixin.qq.com/sns/userinfo?access_token="
								+ access_token + "&openid=" + uid);
			}

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			getInfoHandler.sendMessage(msg);

		}

	};

	Runnable r_tLogin = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "TLogin"));
			params.add(new BasicNameValuePair("Way", way));
			params.add(new BasicNameValuePair("UID", uid));

			String result = new HttpPostConnection("SignServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			tLoginHandler.sendMessage(msg);
		}
	};

	Runnable r_Headpic = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				Bitmap bm = new HttpGetPicConnection()
						.httpConnection(picstr, 1,BindActivity.this);
				picstr = BitmapToString.bitmaptoString(bm);
				bm.recycle();
				bm = null;
			} catch (Exception e) {
				e.printStackTrace();
				picstr = "";
			}

			Message msg = new Message();
			Bundle b = new Bundle();
			msg.setData(b);
			headpicHandler.sendMessage(msg);

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

	class KeyHandler extends Handler {
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();
			JSONObject json;
			String result = null;
			try {
				json = new JSONObject(b.getString("result"));
				result = json.getString("Result");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (result.equals("验证码已发送")) {
				Toast.makeText(BindActivity.this, result, Toast.LENGTH_SHORT)
						.show();
				new Thread(r_time).start();
			} else if (result.equals("验证码发送失败")) {
				Toast.makeText(BindActivity.this, result, Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(BindActivity.this, "验证短信发送失败，请检查您的网络设置",
						Toast.LENGTH_SHORT).show();
			}

		}
	}

	class BindHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			String result = msg.getData().getString("result");

			if (result.equals("timeout")) {
				progressDialog.dismiss();
				Toast.makeText(BindActivity.this, "网络出问题了...",
						Toast.LENGTH_SHORT).show();
				return;
			}

			try {
				JSONObject json = new JSONObject(result);

				if (json.getString("Result").equals("绑定成功")) {
					new Thread(r_tLogin).start();
				} else {
					progressDialog.dismiss();
					Toast.makeText(BindActivity.this, json.getString("Result"),
							Toast.LENGTH_SHORT).show();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	class GetInfoHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			String result = msg.getData().getString("result");

			if (result.equals("timeout")) {
				progressDialog.dismiss();
				Toast.makeText(BindActivity.this, "网络出问题了...",
						Toast.LENGTH_SHORT).show();
				return;
			}

			try {
				JSONObject json = new JSONObject(result);

				if (way.equals("Sina")) {
					if (json.has("error")) {
						progressDialog.dismiss();
						Toast.makeText(BindActivity.this,
								json.getString("error"), Toast.LENGTH_SHORT)
								.show();
						return;
					}

					if (json.has("screen_name")) {
						nickname = json.getString("screen_name");
					}

					if (json.has("description")) {
						introduction = json.getString("description");
					}

					if (json.has("profile_image_url")) {
						picstr = json.getString("profile_image_url");
						new Thread(r_Headpic).start();
					} else {
						new Thread(r_bind).start();
					}

				} else if (way.equals("QQ")) {
					if (json.getInt("ret") == 0) {
						if (json.has("nickname")) {
							nickname = json.getString("nickname");
						}

						introduction = "";

						if (json.has("figureurl")) {
							picstr = json.getString("figureurl");
							new Thread(r_Headpic).start();
						} else {
							new Thread(r_bind).start();
						}
					} else {
						progressDialog.dismiss();
						Toast.makeText(BindActivity.this,
								json.getString("msg"), Toast.LENGTH_SHORT)
								.show();
					}
				} else if (way.equals("Wechat")) {
					if (json.has("openid")) {
						
						if (json.has("nickname")) {
							nickname = json.getString("nickname");
						}

						introduction = "";

						if (json.has("headimgurl")) {
							picstr = json.getString("headimgurl");
							picstr.substring(0, picstr.length() - 1);
							picstr += "46";
							new Thread(r_Headpic).start();
						} else {
							new Thread(r_bind).start();
						}
					} else {
						progressDialog.dismiss();
						Toast.makeText(BindActivity.this,
								json.getString("errcode")+" "+json.getString("errmsg"), Toast.LENGTH_SHORT)
								.show();
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	class TLoginHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			progressDialog.dismiss();

			String result = msg.getData().getString("result");

			if (result.equals("timeout")) {
				Toast.makeText(BindActivity.this, "网络出问题了...",
						Toast.LENGTH_SHORT).show();
				return;
			}

			try {
				JSONObject json = new JSONObject(result);

				int userID = json.getInt("UserID");

				progressDialog.dismiss();
				Data.USERID = userID + "";
				Data.PASSCODE = json.getString("Passcode");
				Data.ISLOGIN = true;
				Data.PHONE = json.getString("Phone");

				// 帐号注册信鸽推送
				XGPushManager.registerPush(BindActivity.this, "account"
						+ userID);

				SharedPreferences sp = PreferenceManager
						.getDefaultSharedPreferences(BindActivity.this);
				Editor pEdit = sp.edit();
				pEdit.putBoolean("isLogin", true);
				pEdit.putString("UserID", Data.USERID);
				pEdit.putString("Passcode", Data.PASSCODE);
				pEdit.putString("Phone", Data.PHONE);
				pEdit.commit();

				Intent intent = new Intent("RefreshUserinfo");
				sendBroadcast(intent);

				LoginActivity.instance_login.finish();
				finish();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	class HeadpicHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			new Thread(r_bind).start();

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

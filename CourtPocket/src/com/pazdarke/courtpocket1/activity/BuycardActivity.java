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
import com.pingplusplus.android.PaymentActivity;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BuycardActivity extends Activity {

	ProgressDialog progressDialog;

	Intent intent;
	int payMode = 0;// 0支付宝 1微信支付 2银联
	String BillID;
	Boolean isPay = false;
	Boolean isPayFromMycard = false;

	ImageView iv_yuecheck;
	RelativeLayout rl_yuecheck;
	LinearLayout ll_yuestatus;
	TextView tv_yue;
	boolean isUseYuE = false;
	double myMoney;

	LinearLayout ll_payway;
	TextView tv_paymoney;
	double payMoney;

	RelativeLayout rl_alipay, rl_wechatpay, rl_visa;
	ImageView iv_alipaycheck, iv_wechatpaycheck, iv_visacheck;

	MymoneyHandler mymoneyHandler;
	CreateBillHandler createBillHandler;
	PayResultHandler payResultHandler;
	TimeoutHandler timeoutHandler;

	public static Activity instance_buycard;// 用于关闭

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_buycard);
		
		AppManager.getAppManager().addActivity(this);

		instance_buycard = this;

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("玩命加载数据中...");
		progressDialog.setCancelable(false);

		intent = getIntent();

		init();

		mymoneyHandler = new MymoneyHandler();
		createBillHandler = new CreateBillHandler();
		payResultHandler = new PayResultHandler();
		timeoutHandler = new TimeoutHandler();

		new Thread(r_Mymoney).start();

	}

	private void init() {
		// TODO Auto-generated method stub

		isPayFromMycard = intent.getBooleanExtra("isPayFromMycard", false);
		if (isPayFromMycard) {
			BillID = intent.getStringExtra("BillID");
		}

		/*
		 * int[] type = { R.drawable.ic_bill_soccer,
		 * R.drawable.ic_bill_basketball, R.drawable.ic_bill_volleyball,
		 * R.drawable.ic_bill_tabletennis, R.drawable.ic_bill_tennis,
		 * R.drawable.ic_bill_badminton, R.drawable.ic_bill_billiards,
		 * R.drawable.ic_bill_swimming, R.drawable.ic_bill_bodybuilding,
		 * R.drawable.ic_bill_xgames, R.drawable.ic_bill_golf,
		 * R.drawable.ic_bill_moresports }; ImageView
		 * iv_type=(ImageView)findViewById(R.id.iv_buycard_type);
		 * iv_type.setImageResource(type[intent.getIntExtra("gymType", 1)-1]);
		 */

		TextView tv_gymname = (TextView) findViewById(R.id.tv_buycard_gymname);
		tv_gymname.setText(intent.getStringExtra("gymName"));

		ImageView iv_type = (ImageView) findViewById(R.id.iv_buycard_cardtype);
		int type = intent.getIntExtra("cardType", 11);
		if (type == 11) {
			iv_type.setImageResource(R.drawable.ic_adultcard_once);
		} else if (type == 12) {
			iv_type.setImageResource(R.drawable.ic_adultcard_twice);
		} else if (type == 21) {
			iv_type.setImageResource(R.drawable.ic_childcard_once);
		} else if (type == 22) {
			iv_type.setImageResource(R.drawable.ic_childcard_twice);
		}

		TextView tv_cardname = (TextView) findViewById(R.id.tv_buycard_cardname);
		tv_cardname.setText(intent.getStringExtra("cardName"));

		TextView tv_content = (TextView) findViewById(R.id.tv_buycard_content);
		tv_content.setText(intent.getStringExtra("content"));

		payMoney = intent.getDoubleExtra("money", 0);

		TextView tv_money = (TextView) findViewById(R.id.tv_buycard_money);
		tv_money.setText(Data.doubleTrans(payMoney) + "元");

		tv_paymoney = (TextView) findViewById(R.id.tv_buycard_paymoney);
		tv_paymoney.setText(Data.doubleTrans(payMoney) + "元");

		rl_yuecheck = (RelativeLayout) findViewById(R.id.rl_buycard_yuecheck);
		ll_yuestatus = (LinearLayout) findViewById(R.id.ll_buycard_yuestatus);
		// rl_yuecheck.setOnClickListener(onClickListener);
		iv_yuecheck = (ImageView) findViewById(R.id.iv_buycard_yuecheck);
		tv_yue = (TextView) findViewById(R.id.tv_buycard_yue);

		ll_payway = (LinearLayout) findViewById(R.id.ll_buycard_payway);

		rl_alipay = (RelativeLayout) findViewById(R.id.rl_buycard_alipay);
		rl_alipay.setOnClickListener(payOnClickListener);
		rl_wechatpay = (RelativeLayout) findViewById(R.id.rl_buycard_wechatpay);
		rl_wechatpay.setOnClickListener(payOnClickListener);
		rl_visa = (RelativeLayout) findViewById(R.id.rl_buycard_visa);
		rl_visa.setOnClickListener(payOnClickListener);

		iv_alipaycheck = (ImageView) findViewById(R.id.iv_buycard_alipaycheck);
		iv_wechatpaycheck = (ImageView) findViewById(R.id.iv_buycard_wechatpaycheck);
		iv_visacheck = (ImageView) findViewById(R.id.iv_buycard_visacheck);

		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_buycard_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		Button btn_submit = (Button) findViewById(R.id.btn_buycard_submit);
		btn_submit.setOnClickListener(onClickListener);

	}

	OnClickListener payOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			iv_alipaycheck.setImageResource(R.drawable.ic_bookconfirm_check1);
			iv_wechatpaycheck
					.setImageResource(R.drawable.ic_bookconfirm_check1);
			iv_visacheck.setImageResource(R.drawable.ic_bookconfirm_check1);

			switch (v.getId()) {
			case R.id.rl_buycard_alipay:
				iv_alipaycheck
						.setImageResource(R.drawable.ic_bookconfirm_check2);
				payMode = 0;
				break;
			case R.id.rl_buycard_wechatpay:
				iv_wechatpaycheck
						.setImageResource(R.drawable.ic_bookconfirm_check2);
				payMode = 1;
				break;
			case R.id.rl_buycard_visa:
				iv_visacheck.setImageResource(R.drawable.ic_bookconfirm_check2);
				payMode = 2;
				break;
			}
		}
	};

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_buycard_leftarrow:
				finish();
				break;
			case R.id.rl_buycard_yuecheck:
				if (isUseYuE) {
					isUseYuE = false;
					iv_yuecheck
							.setImageResource(R.drawable.ic_bookconfirm_check1);
					ll_payway.setVisibility(View.VISIBLE);
					tv_paymoney.setText(Data.doubleTrans(payMoney) + "元");
				} else {
					isUseYuE = true;
					iv_yuecheck
							.setImageResource(R.drawable.ic_bookconfirm_check2);
					if (myMoney >= payMoney) {
						tv_paymoney.setText("0元");
						ll_payway.setVisibility(View.GONE);
					} else {
						tv_paymoney.setText(Data.doubleTrans(payMoney - myMoney) + "元");
						ll_payway.setVisibility(View.VISIBLE);
					}
				}
				break;
			case R.id.btn_buycard_submit:
				progressDialog.show();
				if (isPay || isPayFromMycard) {
					new Thread(r_Pay).start();
				} else {
					new Thread(r_CreateBill).start();
				}
				break;
			}
		}
	};

	Runnable r_Mymoney = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "MyMoney"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			String result = new HttpPostConnection("UserInfoServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();

			if (result.equals("timeout")) {
				msg.setData(b);
				timeoutHandler.sendMessage(msg);
				return;
			}

			b.putString("result", result);
			msg.setData(b);
			mymoneyHandler.sendMessage(msg);
		}
	};

	Runnable r_CreateBill = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("Request", "BuyCard"));
				params.add(new BasicNameValuePair("UserID", Data.USERID));
				params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
				params.add(new BasicNameValuePair("GymCardID", intent
						.getStringExtra("gymCardID")));

				String result = new HttpPostConnection("CardServer", params)
						.httpConnection();

				if (result.equals("timeout")) {
					Message msg = new Message();
					Bundle b = new Bundle();
					msg.setData(b);
					timeoutHandler.sendMessage(msg);
					return;
				}

				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("result", result);
				msg.setData(b);
				createBillHandler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	Runnable r_Pay = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("BillID", BillID));
			if (isUseYuE)
				params.add(new BasicNameValuePair("Priority", "0"));
			else
				params.add(new BasicNameValuePair("Priority", "1"));
			params.add(new BasicNameValuePair("Way", payMode + ""));

			String result = new HttpPostConnection("PayServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			payResultHandler.sendMessage(msg);
		}
	};

	class MymoneyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();

			try {
				JSONObject json = new JSONObject(b.getString("result"));
				double money = json.getDouble("Money");
				myMoney = money;
				tv_yue.setText(Data.doubleTrans(money) + "元");

				if (money > 0) {
					ll_yuestatus.setVisibility(View.VISIBLE);
					rl_yuecheck.setOnClickListener(onClickListener);
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	@SuppressLint("HandlerLeak")
	class CreateBillHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();
			String result = b.getString("result");
			try {
				JSONObject json = new JSONObject(result);

				if (json.getString("Result").equals("成功")) {
					BillID = json.getString("CardID");
					isPay = true;
					new Thread(r_Pay).start();

				} else {
					Toast.makeText(BuycardActivity.this,
							json.getString("Result"), Toast.LENGTH_SHORT)
							.show();
					progressDialog.dismiss();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				progressDialog.dismiss();
				Toast.makeText(BuycardActivity.this, b.getString("result"),
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}

		}
	}

	class PayResultHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();

			try {
				
				System.out.println(b.getString("result"));

				if (b.getString("result").equals("timeout")) {
					progressDialog.dismiss();
					Toast.makeText(BuycardActivity.this, "订单成功生成，支付超时",
							Toast.LENGTH_SHORT).show();
					return;
				}

				JSONObject json = new JSONObject(b.getString("result"));
				String payresult = json.getString("Result");

				if (payresult.equals("使用第三方支付")) {
					JSONObject charge = json.getJSONObject("Charge");

					String packageName = getPackageName();
					ComponentName componentName = new ComponentName(
							packageName, packageName
									+ ".wxapi.WXPayEntryActivity");
					Intent intent = new Intent();
					intent.setComponent(componentName);
					intent.putExtra(PaymentActivity.EXTRA_CHARGE,
							charge.toString());
					// progressDialog.dismiss();
					startActivityForResult(intent, 300);
				} else if (payresult.equals("余额支付成功")) {
					Toast.makeText(BuycardActivity.this, "支付成功，祝您运动愉快(^_^)",
							Toast.LENGTH_SHORT).show();
					progressDialog.dismiss();
					if (isPay) {
						Intent intent = new Intent(BuycardActivity.this,
								MycardActivity.class);
						startActivity(intent);
						intent.putExtra("UserID", Data.USERID);
						intent.putExtra("Passcode", Data.PASSCODE);
						CardActivity.instance_card.finish();
						finish();
					} else {
						Intent intent = new Intent("RefreshMycard");
						intent.putExtra("UserID", Data.USERID);
						intent.putExtra("Passcode", Data.PASSCODE);
						sendBroadcast(intent);
						finish();
					}
				} else {
					Toast.makeText(BuycardActivity.this, "订单成功生成，支付超时",
							Toast.LENGTH_SHORT).show();
					progressDialog.dismiss();
				}

			} catch (Exception e) {
				progressDialog.dismiss();
				Toast.makeText(BuycardActivity.this, b.getString("result"),
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}

		}
	}

	class TimeoutHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Toast.makeText(BuycardActivity.this, "网络出问题咩(+﹏+)~",
					Toast.LENGTH_SHORT).show();
			progressDialog.dismiss();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 支付页面返回处理
		if (requestCode == 300) {
			if (resultCode == Activity.RESULT_OK) {
				String result = data.getExtras().getString("pay_result");
				/*
				 * 处理返回值 "success" - payment succeed "fail" - payment failed
				 * "cancel" - user canceld "invalid" - payment plugin not
				 * installed
				 * 
				 * 如果是银联渠道返回 invalid，调用 UPPayAssistEx.installUPPayPlugin(this);
				 * 安装银联安全支付控件。
				 */
				/*
				 * String errorMsg = data.getExtras().getString("error_msg"); //
				 * 错误信息 Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
				 */
				progressDialog.dismiss();
				if (result.equals("success")) {
					Toast.makeText(BuycardActivity.this, "支付成功，祝您运动愉快(^_^)",
							Toast.LENGTH_SHORT).show();
					if (isPay) {
						Intent intent = new Intent(BuycardActivity.this,
								MycardActivity.class);
						intent.putExtra("UserID", Data.USERID);
						intent.putExtra("Passcode", Data.PASSCODE);
						startActivity(intent);
						CardActivity.instance_card.finish();
						finish();
					} else {
						Intent intent = new Intent("RefreshMycard");
						intent.putExtra("UserID", Data.USERID);
						intent.putExtra("Passcode", Data.PASSCODE);
						sendBroadcast(intent);
						finish();
					}
					/*
					 * Intent intent = new Intent(BuycardActivity.this,
					 * MybillActivity.class); intent.putExtra("BillID", BillID +
					 * ""); intent.putExtra("Priority", 1);
					 * startActivity(intent);
					 * BookActivity.instance_book.finish();
					 * BuycardActivity.instance_buycard.finish();
					 */
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
				//Toast.makeText(this, "User canceled", Toast.LENGTH_SHORT).show();
			} else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
				//Toast.makeText(this, "An invalid Credential was submitted.",Toast.LENGTH_SHORT).show();
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

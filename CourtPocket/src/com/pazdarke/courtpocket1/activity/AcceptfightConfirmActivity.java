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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AcceptfightConfirmActivity extends Activity {

	ProgressDialog progressDialog;

	Intent intent;
	int payMode = 0;// 0支付宝 1微信支付 2银联
	String BillID;
	int FightID;

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
	AcceptfightHandler acceptfightHandler;
	PayResultHandler payResultHandler;
	TimeoutHandler timeoutHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_acceptfight_confirm);

		AppManager.getAppManager().addActivity(this);

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("玩命加载数据中...");
		progressDialog.setCancelable(false);

		intent = getIntent();

		init();

		mymoneyHandler = new MymoneyHandler();
		acceptfightHandler = new AcceptfightHandler();
		payResultHandler = new PayResultHandler();
		timeoutHandler = new TimeoutHandler();

		new Thread(r_Mymoney).start();

	}

	private void init() {
		// TODO Auto-generated method stub

		FightID = intent.getIntExtra("FightID", 0);

		int[] type = { R.drawable.ic_bill_soccer,
				R.drawable.ic_bill_basketball, R.drawable.ic_bill_volleyball,
				R.drawable.ic_bill_tabletennis, R.drawable.ic_bill_tennis,
				R.drawable.ic_bill_badminton, R.drawable.ic_bill_billiards,
				R.drawable.ic_bill_swimming, R.drawable.ic_bill_bodybuilding,
				R.drawable.ic_bill_xgames, R.drawable.ic_bill_golf,
				R.drawable.ic_bill_moresports };
		ImageView iv_type = (ImageView) findViewById(R.id.iv_acceptfightconfirm_type);
		iv_type.setImageResource(type[intent.getIntExtra("GymType", 1) - 1]);

		TextView tv_gymname = (TextView) findViewById(R.id.tv_acceptfightconfirm_gymname);
		tv_gymname.setText(intent.getStringExtra("GymName"));

		TextView tv_teamname = (TextView) findViewById(R.id.tv_acceptfightconfirm_teamname);
		tv_teamname.setText("本方球队：" + intent.getStringExtra("TeamName"));

		TextView tv_oteamname = (TextView) findViewById(R.id.tv_acceptfightconfirm_oteamname);
		tv_oteamname.setText("对方球队：" + intent.getStringExtra("oTeamName"));

		TextView tv_date = (TextView) findViewById(R.id.tv_acceptfightconfirm_date);
		String[] date = intent.getStringExtra("Date").split("-");
		tv_date.setText(date[1] + "月" + date[2] + "日");

		LinearLayout ll_datecontainer = (LinearLayout) findViewById(R.id.ll_acceptfightconfirm_datecontainer);
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		int weight = intent.getIntExtra("weight", 1);

		for (int i = 0; i < intent.getIntExtra("CourtNum", 0); i++) {
			LayoutInflater mLi = LayoutInflater
					.from(AcceptfightConfirmActivity.this);
			View view = mLi.inflate(R.layout.layout_bookconfirm_court, null);

			int time = intent.getIntExtra("Time" + i, 0);
			TextView tv_time = (TextView) view
					.findViewById(R.id.tv_bookconfirm_courttime);
			tv_time.setText(BookActivity.minuteToClock(time * 30) + "-"
					+ BookActivity.minuteToClock((time + weight) * 30));

			TextView tv_courtname = (TextView) view
					.findViewById(R.id.tv_bookconfirm_courtname);
			tv_courtname.setText(intent.getStringExtra("CourtName" + i));

			TextView tv_price = (TextView) view
					.findViewById(R.id.tv_bookconfirm_courtprice);
			tv_price.setText(intent.getStringExtra("Price" + i) + "元");

			ll_datecontainer.addView(view, p);
		}

		payMoney = intent.getDoubleExtra("Money", 0);

		TextView tv_money = (TextView) findViewById(R.id.tv_acceptfightconfirm_money);
		tv_money.setText(Data.doubleTrans(payMoney) + "元");

		tv_paymoney = (TextView) findViewById(R.id.tv_acceptfightconfirm_paymoney);
		tv_paymoney.setText(Data.doubleTrans(payMoney) + "元");

		rl_yuecheck = (RelativeLayout) findViewById(R.id.rl_acceptfightconfirm_yuecheck);
		ll_yuestatus = (LinearLayout) findViewById(R.id.ll_acceptfightconfirm_yuestatus);
		// rl_yuecheck.setOnClickListener(onClickListener);
		iv_yuecheck = (ImageView) findViewById(R.id.iv_acceptfightconfirm_yuecheck);
		tv_yue = (TextView) findViewById(R.id.tv_acceptfightconfirm_yue);

		ll_payway = (LinearLayout) findViewById(R.id.ll_acceptfightconfirm_payway);

		rl_alipay = (RelativeLayout) findViewById(R.id.rl_acceptfightconfirm_alipay);
		rl_alipay.setOnClickListener(payOnClickListener);
		rl_wechatpay = (RelativeLayout) findViewById(R.id.rl_acceptfightconfirm_wechatpay);
		rl_wechatpay.setOnClickListener(payOnClickListener);
		rl_visa = (RelativeLayout) findViewById(R.id.rl_acceptfightconfirm_visa);
		rl_visa.setOnClickListener(payOnClickListener);

		iv_alipaycheck = (ImageView) findViewById(R.id.iv_acceptfightconfirm_alipaycheck);
		iv_wechatpaycheck = (ImageView) findViewById(R.id.iv_acceptfightconfirm_wechatpaycheck);
		iv_visacheck = (ImageView) findViewById(R.id.iv_acceptfightconfirm_visacheck);

		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_acceptfightconfirm_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		Button btn_submit = (Button) findViewById(R.id.btn_acceptfightconfirm_submit);
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
			case R.id.rl_acceptfightconfirm_alipay:
				iv_alipaycheck
						.setImageResource(R.drawable.ic_bookconfirm_check2);
				payMode = 0;
				break;
			case R.id.rl_acceptfightconfirm_wechatpay:
				iv_wechatpaycheck
						.setImageResource(R.drawable.ic_bookconfirm_check2);
				payMode = 1;
				break;
			case R.id.rl_acceptfightconfirm_visa:
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
			case R.id.iv_acceptfightconfirm_leftarrow:
				finish();
				break;
			case R.id.rl_acceptfightconfirm_yuecheck:
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
			case R.id.btn_acceptfightconfirm_submit:
				progressDialog.show();
				new AcceptFightThread(intent.getIntExtra("TeamID", 0), FightID)
						.start();
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

	class AcceptFightThread extends Thread {

		int TeamID;
		int FightID;

		AcceptFightThread(int TeamID, int FightID) {
			this.TeamID = TeamID;
			this.FightID = FightID;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "AcceptFight"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("TeamID", TeamID + ""));
			params.add(new BasicNameValuePair("FightID", FightID + ""));

			String result = new HttpPostConnection("FightServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			acceptfightHandler.sendMessage(msg);
		}
	}

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

	class AcceptfightHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			String result = msg.getData().getString("result");

			if (result.equals("timeout")) {
				progressDialog.dismiss();
				Toast.makeText(AcceptfightConfirmActivity.this, "网络出问题了",
						Toast.LENGTH_SHORT).show();
				return;
			}

			try {
				JSONObject json = new JSONObject(result);

				if (json.getString("Result").equals("成功")) {
					BillID = json.getString("FightBillID");
					new Thread(r_Pay).start();
				} else {
					progressDialog.dismiss();
					Toast.makeText(AcceptfightConfirmActivity.this,
							json.getString("Result"), Toast.LENGTH_SHORT)
							.show();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
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

			System.out.println(b.getString("result"));

			try {

				if (b.getString("result").equals("timeout")) {
					progressDialog.dismiss();
					AlertDialog dialog = new AlertDialog.Builder(
							AcceptfightConfirmActivity.this)
							.setMessage("支付信息获取超时，请重试  -_-。sorry！")
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											dialog.dismiss();
										}
									}).create();
					dialog.show();
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
					progressDialog.dismiss();
					Toast.makeText(AcceptfightConfirmActivity.this,
							"支付成功，祝您运动愉快(^_^)", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(AcceptfightConfirmActivity.this,
							FightbillActivity.class);
					intent.putExtra("TeamID", AcceptfightConfirmActivity.this
							.getIntent().getIntExtra("TeamID", 0) + "");
					intent.putExtra("UserID", Data.USERID);
					intent.putExtra("Passcode", Data.PASSCODE);
					intent.putExtra("TeamName", AcceptfightConfirmActivity.this
							.getIntent().getStringExtra("TeamName"));
					startActivity(intent);
					FightSelectteamActivity.instance_fightselectteam.finish();
					finish();
				} else {
					AlertDialog dialog = new AlertDialog.Builder(
							AcceptfightConfirmActivity.this)
							.setMessage("支付信息获取超时，请重试  -_-。sorry！")
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											dialog.dismiss();
										}
									}).create();
					dialog.show();
					progressDialog.dismiss();
				}

			} catch (Exception e) {
				progressDialog.dismiss();
				Toast.makeText(AcceptfightConfirmActivity.this,
						b.getString("result"), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}

		}
	}

	class TimeoutHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Toast.makeText(AcceptfightConfirmActivity.this, "网络出问题咩(+﹏+)~",
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
					Toast.makeText(this, "支付成功，祝您运动愉快(^_^)", Toast.LENGTH_SHORT)
							.show();
					Intent intent = new Intent(AcceptfightConfirmActivity.this,
							FightbillActivity.class);
					intent.putExtra("TeamID", AcceptfightConfirmActivity.this
							.getIntent().getIntExtra("TeamID", 0) + "");
					intent.putExtra("UserID", Data.USERID);
					intent.putExtra("Passcode", Data.PASSCODE);
					intent.putExtra("TeamName", AcceptfightConfirmActivity.this
							.getIntent().getStringExtra("TeamName"));
					startActivity(intent);
					FightSelectteamActivity.instance_fightselectteam.finish();
					finish();
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

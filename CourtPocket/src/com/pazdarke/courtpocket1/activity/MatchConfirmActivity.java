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

public class MatchConfirmActivity extends Activity {
	
	ProgressDialog progressDialog;
	
	Intent intent;
	int payMode=0;// 0支付宝 1微信支付 2银联
	String BillID;
	
	ImageView iv_yuecheck;
	RelativeLayout rl_yuecheck;
	LinearLayout ll_yuestatus;
	TextView tv_yue;
	boolean isUseYuE=false;
	double myMoney;
	
	LinearLayout ll_payway;
	TextView tv_paymoney;
	double payMoney;
	
	RelativeLayout rl_alipay,rl_wechatpay,rl_visa;
	ImageView iv_alipaycheck,iv_wechatpaycheck,iv_visacheck;
	
	MymoneyHandler mymoneyHandler;
	PayResultHandler payResultHandler;
	TimeoutHandler timeoutHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_book_confirm);
		
		AppManager.getAppManager().addActivity(this);
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("玩命加载数据中...");
		progressDialog.setCancelable(false);
		
		intent=getIntent();
		
		init();
		
		mymoneyHandler=new MymoneyHandler();
		payResultHandler=new PayResultHandler();
		timeoutHandler=new TimeoutHandler();
		
		new Thread(r_Mymoney).start();
		
	}

	private void init() {
		// TODO Auto-generated method stub
		
		BillID=intent.getStringExtra("BillID");
		
		int[] type = { R.drawable.ic_bill_soccer,
				R.drawable.ic_bill_basketball,
				R.drawable.ic_bill_volleyball,
				R.drawable.ic_bill_tabletennis, R.drawable.ic_bill_tennis,
				R.drawable.ic_bill_badminton, R.drawable.ic_bill_billiards,
				R.drawable.ic_bill_swimming,
				R.drawable.ic_bill_bodybuilding, R.drawable.ic_bill_xgames,
				R.drawable.ic_bill_golf, R.drawable.ic_bill_moresports };
		ImageView iv_type=(ImageView)findViewById(R.id.iv_bookconfirm_type);
		iv_type.setImageResource(type[intent.getIntExtra("gymType", 1)-1]);
		
		TextView tv_gymname=(TextView)findViewById(R.id.tv_bookconfirm_gymname);
		tv_gymname.setText(intent.getStringExtra("gymName"));
		
		TextView tv_date=(TextView)findViewById(R.id.tv_bookconfirm_date);
		String[] date=intent.getStringExtra("date").split("-");
		tv_date.setText(date[1]+"月"+date[2]+"日");
		
		LinearLayout ll_datecontainer = (LinearLayout) findViewById(R.id.ll_bookconfirm_datecontainer);
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	
		for(int i=0;i<intent.getIntExtra("courtNum", 0);i++){
			LayoutInflater mLi = LayoutInflater.from(MatchConfirmActivity.this);
			View view = mLi.inflate(R.layout.layout_bookconfirm_court, null);
			
			int time = intent.getIntExtra("time" + i, 0);
			TextView tv_time = (TextView) view
					.findViewById(R.id.tv_bookconfirm_courttime);
			tv_time.setText(BookActivity.minuteToClock(time * 30)
					+ "-"
					+ BookActivity.minuteToClock((time + intent.getIntExtra(
							"weight", 0)) * 30));
			
			TextView tv_courtname = (TextView) view
					.findViewById(R.id.tv_bookconfirm_courtname);
			tv_courtname.setText(intent.getStringExtra("name"+i));
			
			TextView tv_price = (TextView) view
					.findViewById(R.id.tv_bookconfirm_courtprice);
			tv_price.setText(intent.getStringExtra("price"+i)+"元");
			
			ll_datecontainer.addView(view,p);
		}
		
		payMoney=intent.getDoubleExtra("money",0);
		
		TextView tv_money=(TextView)findViewById(R.id.tv_bookconfirm_money);
		tv_money.setText(Data.doubleTrans(payMoney)+"元");
		
		tv_paymoney=(TextView)findViewById(R.id.tv_bookconfirm_paymoney);
		tv_paymoney.setText(Data.doubleTrans(payMoney)+"元");
		
		rl_yuecheck=(RelativeLayout)findViewById(R.id.rl_bookconfirm_yuecheck);
		ll_yuestatus=(LinearLayout)findViewById(R.id.ll_bookconfirm_yuestatus);
		//rl_yuecheck.setOnClickListener(onClickListener);
		iv_yuecheck=(ImageView)findViewById(R.id.iv_bookconfirm_yuecheck);
		tv_yue=(TextView)findViewById(R.id.tv_bookconfirm_yue);
		
		ll_payway=(LinearLayout)findViewById(R.id.ll_bookconfirm_payway);
		
		rl_alipay=(RelativeLayout)findViewById(R.id.rl_bookconfirm_alipay);
		rl_alipay.setOnClickListener(payOnClickListener);
		rl_wechatpay=(RelativeLayout)findViewById(R.id.rl_bookconfirm_wechatpay);
		rl_wechatpay.setOnClickListener(payOnClickListener);
		rl_visa=(RelativeLayout)findViewById(R.id.rl_bookconfirm_visa);
		rl_visa.setOnClickListener(payOnClickListener);
		
		iv_alipaycheck=(ImageView)findViewById(R.id.iv_bookconfirm_alipaycheck);
		iv_wechatpaycheck=(ImageView)findViewById(R.id.iv_bookconfirm_wechatpaycheck);
		iv_visacheck=(ImageView)findViewById(R.id.iv_bookconfirm_visacheck);
		
		ImageView iv_leftarrow=(ImageView)findViewById(R.id.iv_bookconfirm_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);
		
		Button btn_submit=(Button)findViewById(R.id.btn_bookconfirm_submit);
		btn_submit.setOnClickListener(onClickListener);
		
	}
	
	OnClickListener payOnClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			iv_alipaycheck.setImageResource(R.drawable.ic_bookconfirm_check1);
			iv_wechatpaycheck.setImageResource(R.drawable.ic_bookconfirm_check1);
			iv_visacheck.setImageResource(R.drawable.ic_bookconfirm_check1);
			
			switch(v.getId()){
			case R.id.rl_bookconfirm_alipay:
				iv_alipaycheck.setImageResource(R.drawable.ic_bookconfirm_check2);
				payMode=0;
				break;
			case R.id.rl_bookconfirm_wechatpay:
				iv_wechatpaycheck.setImageResource(R.drawable.ic_bookconfirm_check2);
				payMode=1;
				break;
			case R.id.rl_bookconfirm_visa:
				iv_visacheck.setImageResource(R.drawable.ic_bookconfirm_check2);
				payMode=2;
				break;
			}
		}
	};
	
	OnClickListener onClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.iv_bookconfirm_leftarrow:
				finish();
				break;
			case R.id.rl_bookconfirm_yuecheck:
				if(isUseYuE){
					isUseYuE=false;
					iv_yuecheck.setImageResource(R.drawable.ic_bookconfirm_check1);
					ll_payway.setVisibility(View.VISIBLE);
					tv_paymoney.setText(Data.doubleTrans(payMoney)+"元");
				} else{
					isUseYuE=true;
					iv_yuecheck.setImageResource(R.drawable.ic_bookconfirm_check2);
					if(myMoney>=payMoney){
						tv_paymoney.setText("0元");
						ll_payway.setVisibility(View.GONE);
					} else{
						tv_paymoney.setText(Data.doubleTrans(payMoney-myMoney)+"元");
						ll_payway.setVisibility(View.VISIBLE);
					}
				}
				break;
			case R.id.btn_bookconfirm_submit:
				progressDialog.show();
				new Thread(r_Pay).start();
				break;
			}
		}
	};
	
	Runnable r_Mymoney=new Runnable() {
		
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
	
	Runnable r_Pay=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("UserID",Data.USERID));
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
				myMoney=money;
				tv_yue.setText(Data.doubleTrans(money)+"元");
				
				if(money>0){
					ll_yuestatus.setVisibility(View.VISIBLE);
					rl_yuecheck.setOnClickListener(onClickListener);
				}
				
			} catch (JSONException e) {
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

			try {
				
				if (b.getString("result").equals("timeout")) {
					progressDialog.dismiss();
					AlertDialog dialog = new AlertDialog.Builder(
							MatchConfirmActivity.this)
							.setMessage(
									"支付失败，请检查您的网络状态  -_-。sorry！(timeout)")
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
					Intent intent=new Intent();
					intent.setComponent(componentName);
					intent.putExtra(PaymentActivity.EXTRA_CHARGE,
							charge.toString());
					startActivityForResult(intent, 300);
				} else if (payresult.equals("余额支付成功")) {
					Toast.makeText(MatchConfirmActivity.this,
							"支付成功，祝您运动愉快(^_^)", Toast.LENGTH_SHORT).show();
					progressDialog.dismiss();
					Intent intent = new Intent("RefreshMatchBilllist");
					intent.putExtra("BillID", BillID + "");
					intent.putExtra("Priority", 1);
					intent.putExtra("UserID", Data.USERID);
					intent.putExtra("Passcode", Data.PASSCODE);
					sendBroadcast(intent);
					MatchbillInfoActivity.instance_matchbillinfo.finish();

					finish();
				} else {
					AlertDialog dialog = new AlertDialog.Builder(
							MatchConfirmActivity.this)
							.setMessage(
									"支付失败，请检查您的网络状态  -_-。sorry！ error:"+payresult)
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
				Toast.makeText(MatchConfirmActivity.this,
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

			Toast.makeText(MatchConfirmActivity.this, "获取用户余额失败 (+﹏+)~",
					Toast.LENGTH_SHORT).show();
			progressDialog.dismiss();
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 支付页面返回处理
		if (requestCode == 300) {
			progressDialog.dismiss(); 
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
				/*String errorMsg = data.getExtras().getString("error_msg"); // 错误信息
				Toast.makeText(this, result, Toast.LENGTH_SHORT).show();*/
				progressDialog.dismiss();
				if (result.equals("success")) {
					Toast.makeText(this, "支付成功，祝您运动愉快(^_^)", Toast.LENGTH_SHORT)
							.show();

					Intent intent = new Intent("RefreshMatchBilllist");
					intent.putExtra("BillID", BillID + "");
					intent.putExtra("Priority", 1);
					intent.putExtra("UserID", Data.USERID);
					intent.putExtra("Passcode", Data.PASSCODE);
					sendBroadcast(intent);
					MatchbillInfoActivity.instance_matchbillinfo.finish();

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

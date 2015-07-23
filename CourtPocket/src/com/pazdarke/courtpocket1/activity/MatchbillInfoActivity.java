package com.pazdarke.courtpocket1.activity;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.pazdarke.courtpocket1.tools.listview.AsyncViewTask;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

public class MatchbillInfoActivity extends Activity {

	ProgressDialog progressDialog;

	JSONObject billInfo;
	String billID;

	public static Activity instance_matchbillinfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_matchbill_info);

		AppManager.getAppManager().addActivity(this);

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("正在取消匹配...");
		progressDialog.setCancelable(false);

		instance_matchbillinfo = this;

		try {
			billInfo = new JSONObject(this.getIntent().getStringExtra("info"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		billID = this.getIntent().getStringExtra("ID");

		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_matchbillinfo_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		initView();

	}

	private void initView() {
		// TODO Auto-generated method stub
		try {
			ImageView iv_courtlogo = (ImageView) findViewById(R.id.iv_matchbillinfo_courtlogo);
			if (billInfo.has("GymPath"))
				new AsyncViewTask(MatchbillInfoActivity.this,
						billInfo.getString("GymPath"), iv_courtlogo, 5)
						.execute(iv_courtlogo);// 异步加载图片
			else
				iv_courtlogo.setImageResource(R.color.lightGrey);
			iv_courtlogo.setOnClickListener(onClickListener);

			((TextView) findViewById(R.id.tv_matchbillinfo_gymname))
					.setText(billInfo.getString("GymName"));

			String[] gymtype = { "足球场", "篮球场", "排球场", "乒乓球场", "网球场", "羽毛球场",
					"桌球场", "游泳馆", "健身房", "极限运动", "高尔夫球场", "其他运动场馆" };
			((TextView) findViewById(R.id.tv_matchbillinfo_gymtype))
					.setText("场馆类型："
							+ gymtype[billInfo.getInt("GymMainType") - 1]);

			TextView tv_price = (TextView) findViewById(R.id.tv_matchbillinfo_money);
			if (billInfo.getDouble("Price") != 0) {
				tv_price.setText("￥" + Data.doubleTrans(billInfo.getDouble("Price")));
				tv_price.setTextColor(getResources().getColor(R.color.blue));
			} else {
				tv_price.setText("暂无");
				tv_price.setTextColor(getResources().getColor(R.color.darkGrey));
			}

			((TextView) findViewById(R.id.tv_matchbillinfo_generatetime))
					.setText("下单时间："
							+ billInfo.getString("GenerateTime").substring(0,
									19));

			TextView tv_status = (TextView) findViewById(R.id.tv_matchbillinfo_status);
			TextView tv_courtname = (TextView) findViewById(R.id.tv_matchbillinfo_courtname);
			TextView tv_time = (TextView) findViewById(R.id.tv_matchbillinfo_time);
			LinearLayout ll_smallbill = (LinearLayout) findViewById(R.id.ll_matchbillinfo_samllbill);
			TextView tv_checkstatus = (TextView) findViewById(R.id.tv_matchbillinfo_checkstatus);
			TextView tv_checkcode = (TextView) findViewById(R.id.tv_matchbillinfo_checkcode);
			String[] time = { "早上", "下午", "晚上" };
			Button btn_button = (Button) findViewById(R.id.btn_matchbillinfo_button);
			switch (billInfo.getInt("Status")) {
			case -1:
				tv_status.setText("订单异常");
				tv_status.setTextColor(getResources().getColor(R.color.red));
				tv_courtname.setText(billInfo.getString("CourtName"));
				tv_time.setText(billInfo.getString("MatchDate")
						+ " "
						+ BookActivity.minuteToClock(billInfo
								.getInt("MatchTime") * 30)
						+ "-"
						+ BookActivity.minuteToClock((billInfo
								.getInt("MatchTime") + billInfo
								.getInt("Weight")) * 30));
				tv_checkstatus.setText("未验证 验证码");
				tv_checkcode.setText(billInfo.getString("CheckCode"));
				btn_button.setVisibility(View.GONE);
				break;
			case 0:
				tv_status.setText("订单关闭");
				tv_status.setTextColor(getResources()
						.getColor(R.color.darkGrey));
				tv_courtname.setText("未分配场地");
				tv_time.setText(billInfo.getString("MatchDate") + " "
						+ time[billInfo.getInt("Time")]);
				ll_smallbill.setVisibility(View.GONE);
				btn_button.setVisibility(View.GONE);
				break;
			case 1:
				tv_status.setText("正在寻找对手");
				tv_status.setTextColor(getResources().getColor(R.color.blue));
				tv_courtname.setText("未分配场地");
				tv_time.setText(billInfo.getString("MatchDate") + " "
						+ time[billInfo.getInt("Time")]);
				ll_smallbill.setVisibility(View.GONE);

				btn_button.setVisibility(View.VISIBLE);
				btn_button.setText("退出匹配");
				btn_button
						.setBackgroundResource(R.drawable.selector_drawable_red_orange);
				btn_button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						progressDialog.show();

						new Thread(r_CancelMatch).start();

					}
				});
				break;
			case 2:
				tv_status.setText("待付款");
				tv_status.setTextColor(getResources().getColor(R.color.red));
				tv_courtname.setText(billInfo.getString("CourtName"));
				tv_time.setText(billInfo.getString("MatchDate")
						+ " "
						+ BookActivity.minuteToClock(billInfo
								.getInt("MatchTime") * 30)
						+ "-"
						+ BookActivity.minuteToClock((billInfo
								.getInt("MatchTime") + billInfo
								.getInt("Weight")) * 30));
				tv_checkstatus.setText("未付款");
				tv_checkcode.setVisibility(View.GONE);

				btn_button.setText("立即付款");
				btn_button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(MatchbillInfoActivity.this,
								MatchConfirmActivity.class);
						try {
							intent.putExtra("BillType", 1);
							intent.putExtra("gymType",
									billInfo.getInt("GymMainType"));
							intent.putExtra("gymName",
									billInfo.getString("GymName"));
							intent.putExtra("date",
									billInfo.getString("MatchDate"));
							intent.putExtra("courtNum", 1);
							intent.putExtra("ID", billInfo.getInt("GymID") + "");
							intent.putExtra("BillID", billID);
							intent.putExtra("time0",
									billInfo.getInt("MatchTime"));
							intent.putExtra("name0",
									billInfo.getString("CourtName"));
							intent.putExtra("price0", Data.doubleTrans(billInfo.getDouble("Price"))
									+ "");
							intent.putExtra("weight", billInfo.getInt("Weight"));

							intent.putExtra("money", billInfo.getDouble("Price"));
							startActivity(intent);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});

				break;
			case 3:
				tv_status.setText("等待对手付款");
				tv_status.setTextColor(getResources().getColor(R.color.orange));
				tv_courtname.setText(billInfo.getString("CourtName"));
				tv_time.setText(billInfo.getString("MatchDate")
						+ " "
						+ BookActivity.minuteToClock(billInfo
								.getInt("MatchTime") * 30)
						+ "-"
						+ BookActivity.minuteToClock((billInfo
								.getInt("MatchTime") + billInfo
								.getInt("Weight")) * 30));
				tv_checkstatus.setText("对手未付款");
				tv_checkcode.setVisibility(View.GONE);
				btn_button.setVisibility(View.GONE);
				break;
			case 4:
				tv_status.setText("匹配成功 待验证");
				tv_status.setTextColor(getResources().getColor(R.color.yellow));
				tv_courtname.setText(billInfo.getString("CourtName"));
				tv_time.setText(billInfo.getString("MatchDate")
						+ " "
						+ BookActivity.minuteToClock(billInfo
								.getInt("MatchTime") * 30)
						+ "-"
						+ BookActivity.minuteToClock((billInfo
								.getInt("MatchTime") + billInfo
								.getInt("Weight")) * 30));
				tv_checkstatus.setText("未验证 验证码");
				tv_checkcode.setText(billInfo.getString("CheckCode"));
				btn_button.setVisibility(View.GONE);
				break;
			case 5:
				tv_status.setText("待评价");
				tv_status.setTextColor(getResources().getColor(R.color.yellow));
				tv_courtname.setText(billInfo.getString("CourtName"));
				tv_time.setText(billInfo.getString("MatchDate")
						+ " "
						+ BookActivity.minuteToClock(billInfo
								.getInt("MatchTime") * 30)
						+ "-"
						+ BookActivity.minuteToClock((billInfo
								.getInt("MatchTime") + billInfo
								.getInt("Weight")) * 30));
				tv_checkstatus.setText("已验证");
				tv_checkcode.setVisibility(View.GONE);
				btn_button.setVisibility(View.VISIBLE);
				btn_button.setText("立即评价");
				btn_button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(MatchbillInfoActivity.this,
								CommitMatchcommentActivity.class);
						intent.putExtra("info", billInfo.toString());
						intent.putExtra("billID", billID);
						startActivity(intent);

					}
				});
				break;
			case 6:
				tv_status.setText("已评价");
				tv_status.setTextColor(getResources()
						.getColor(R.color.darkGrey));
				tv_courtname.setText(billInfo.getString("CourtName"));
				tv_time.setText(billInfo.getString("MatchDate")
						+ " "
						+ BookActivity.minuteToClock(billInfo
								.getInt("MatchTime") * 30)
						+ "-"
						+ BookActivity.minuteToClock((billInfo
								.getInt("MatchTime") + billInfo
								.getInt("Weight")) * 30));
				tv_checkstatus.setText("已验证");
				tv_checkcode.setVisibility(View.GONE);
				btn_button.setVisibility(View.GONE);
				break;
			case 11:
				tv_status.setText("重新寻找对手");
				tv_status.setTextColor(getResources().getColor(R.color.blue));
				tv_courtname.setText("未分配场地");
				tv_time.setText(billInfo.getString("MatchDate") + " "
						+ time[billInfo.getInt("Time")]);
				ll_smallbill.setVisibility(View.GONE);
				btn_button.setVisibility(View.GONE);
				break;
			}

			ImageView iv_opponentlogo = (ImageView) findViewById(R.id.iv_matchbillinfo_opponentlogo);
			TextView tv_opponentname = (TextView) findViewById(R.id.tv_matchbillinfo_opponentname);
			ImageView iv_opponentsex = (ImageView) findViewById(R.id.iv_matchbillinfo_opponentsex);
			TextView tv_opponentqq = (TextView) findViewById(R.id.tv_matchbillinfo_opponentqq);
			TextView tv_opponentintro = (TextView) findViewById(R.id.tv_matchbillinfo_opponentintro);
			LinearLayout ll_opponentrate = (LinearLayout) findViewById(R.id.ll_matchbillinfo_opponentrate);
			RatingBar rb_opponentrate = (RatingBar) findViewById(R.id.rb_matchbillinfo_opponentrate);
			TextView tv_opponentrate = (TextView) findViewById(R.id.tv_matchbillinfo_opponentrate);
			TextView tv_opponentratenum = (TextView) findViewById(R.id.tv_matchbillinfo_opponentratenum);

			if (billInfo.has("OpponentUserID")) {

				if (billInfo.has("OpponentPath"))
					new AsyncViewTask(MatchbillInfoActivity.this,
							billInfo.getString("OpponentPath"),
							iv_opponentlogo, 3).execute(iv_opponentlogo);// 异步加载图片

				tv_opponentname.setText(billInfo.getString("OpponentNickname"));

				if (billInfo.has("OpponentSex")) {
					switch (billInfo.getInt("OpponentSex")) {
					case 0:
						iv_opponentsex.setImageResource(R.drawable.ic_male);
						iv_opponentsex.setVisibility(View.VISIBLE);
						break;
					case 1:
						iv_opponentsex.setImageResource(R.drawable.ic_female);
						iv_opponentsex.setVisibility(View.VISIBLE);
						break;
					}
				}

				if (billInfo.has("OpponentQQ")) {
					tv_opponentqq.setText("QQ:"
							+ billInfo.getString("OpponentQQ"));
				} else {
					tv_opponentqq.setText("暂无QQ信息");
				}

				if (billInfo.has("OpponentIntroduction")) {
					tv_opponentintro.setText("个人简介:"
							+ billInfo.getString("OpponentIntroduction"));
				} else {
					tv_opponentintro.setText("暂无个人简介");
				}

				ll_opponentrate.setVisibility(View.VISIBLE);

				tv_opponentratenum.setText("("
						+ billInfo.getInt("OpponentRateNum") + "人评价过)");
				if (billInfo.getInt("OpponentRateNum") == 0) {
					tv_opponentrate.setText("0分");
				} else {
					BigDecimal b = new BigDecimal(
							billInfo.getDouble("OpponentRate"));
					rb_opponentrate.setRating((float) billInfo
							.getDouble("OpponentRate"));
					tv_opponentrate.setText(b.setScale(1,
							BigDecimal.ROUND_HALF_UP).doubleValue()
							+ "分");
				}

			} else {
				tv_opponentname.setText("暂无对手信息");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_matchbillinfo_leftarrow:
				finish();
				break;
			case R.id.iv_matchbillinfo_courtlogo:
				Intent intent = new Intent(MatchbillInfoActivity.this,
						CourtinfoActivity.class);
				try {
					intent.putExtra("ID", billInfo.getInt("GymID") + "");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				startActivity(intent);
				break;
			}
		}
	};

	Runnable r_CancelMatch = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "CancelMatchBill"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("MatchBillID", billID));

			String result = new HttpPostConnection("MatchServer", params)
					.httpConnection();

			System.out.println(result);

			progressDialog.dismiss();

			Intent intent = new Intent("RefreshMatchBilllist");
			sendBroadcast(intent);

			finish();

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

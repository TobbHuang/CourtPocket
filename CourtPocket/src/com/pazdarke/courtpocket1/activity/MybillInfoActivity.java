package com.pazdarke.courtpocket1.activity;

import org.json.JSONException;
import org.json.JSONObject;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.pazdarke.courtpocket1.tools.listview.AsyncViewTask;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MybillInfoActivity extends Activity {

	JSONObject billInfo;
	String billID;

	public static Activity instance_mybillinfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_mybill_info);

		AppManager.getAppManager().addActivity(this);

		instance_mybillinfo = this;

		Intent intent = getIntent();
		billID = intent.getStringExtra("ID");
		try {
			billInfo = new JSONObject(intent.getStringExtra("info"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_mybillinfo_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		init_billinfo();

	}

	private void init_billinfo() {
		// TODO Auto-generated method stub
		try {

			ImageView iv_courtlogo = (ImageView) findViewById(R.id.iv_mybillinfo_courtlogo);
			if (billInfo.has("Path"))
				new AsyncViewTask(MybillInfoActivity.this,
						billInfo.getString("Path"), iv_courtlogo, 5)
						.execute(iv_courtlogo);// 异步加载图片
			else
				iv_courtlogo.setImageResource(R.color.lightGrey);
			iv_courtlogo.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MybillInfoActivity.this,
							CourtinfoActivity.class);
					try {
						intent.putExtra("ID", billInfo.getInt("GymID") + "");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					startActivity(intent);
				}
			});

			((TextView) findViewById(R.id.tv_mybillinfo_gymname))
					.setText(billInfo.getString("GymName"));

			String[] gymtype = { "足球场", "篮球场", "排球场", "乒乓球场", "网球场", "羽毛球场",
					"桌球场", "游泳馆", "健身房", "极限运动", "高尔夫球场", "其他运动场馆" };
			((TextView) findViewById(R.id.tv_mybillinfo_gymtype))
					.setText("场馆类型："
							+ gymtype[billInfo.getInt("GymMainType") - 1]);

			((TextView) findViewById(R.id.tv_mybillinfo_money)).setText("￥"
					+ Data.doubleTrans(billInfo.getDouble("Price")));
			((TextView) findViewById(R.id.tv_mybillinfo_generatetime))
					.setText("下单时间："
							+ billInfo.getString("GenerateTime").substring(0,
									19));

			TextView tv_status = (TextView) findViewById(R.id.tv_mybillinfo_status);
			switch (billInfo.getInt("Status")) {
			case -1:
				tv_status.setText("订单异常");
				tv_status.setTextColor(getResources().getColor(R.color.red));
				break;
			case 0:
				tv_status.setText("订单关闭");
				break;
			case 1:
				tv_status.setText("待付款");
				tv_status.setTextColor(getResources().getColor(R.color.red));
				break;
			case 2:
				tv_status.setText("已付款");
				break;
			case 3:
				tv_status.setText("待评价");
				break;
			case 4:
				tv_status.setText("已评价");
				break;
			}

			int smallbillNum = billInfo.getInt("SmallBillNum");
			if (smallbillNum >= 1) {
				((LinearLayout) findViewById(R.id.ll_mybillinfo_smallbill1))
						.setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.tv_mybillinfo_courtname1))
						.setText(billInfo.getString("CourtName0"));
				((TextView) findViewById(R.id.tv_mybillinfo_smallbilltime1))
						.setText(billInfo.getString("SmallBillDate0")
								+ " "
								+ BookActivity.minuteToClock(billInfo
										.getInt("SmallBillTime0") * 30)
								+ "-"
								+ BookActivity.minuteToClock((billInfo
										.getInt("SmallBillTime0") + billInfo
										.getInt("Weight")) * 30));

				TextView tv_checkstatus = (TextView) findViewById(R.id.tv_mybillinfo_checkstatus1);
				TextView tv_checkcode = (TextView) findViewById(R.id.tv_mybillinfo_checkcode1);
				Button btn_changecourt = (Button) findViewById(R.id.btn_mybillinfo_changecourt1);
				switch (billInfo.getInt("Status")) {
				case -1:
					tv_checkstatus.setVisibility(View.GONE);
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				case 0:
					tv_checkstatus.setVisibility(View.GONE);
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				case 1:
					tv_checkstatus.setVisibility(View.GONE);
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				case 2:
					if (billInfo.getInt("SmallBillStatus0") == 0) {
						tv_checkstatus.setText("未验证  验证码");
						tv_checkcode.setText(billInfo.getString("CheckCode0"));
						// btn_changecourt.setVisibility(View.VISIBLE);
						btn_changecourt.setVisibility(View.GONE);
					} else {
						tv_checkstatus.setText("已验证");
						tv_checkcode.setVisibility(View.GONE);
						btn_changecourt.setVisibility(View.GONE);
					}
					break;
				case 3:
					tv_checkstatus.setText("已验证");
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				case 4:
					tv_checkstatus.setText("已验证");
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				}
			}
			if (smallbillNum >= 2) {
				((LinearLayout) findViewById(R.id.ll_mybillinfo_smallbill2))
						.setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.tv_mybillinfo_courtname2))
						.setText(billInfo.getString("CourtName1"));
				((TextView) findViewById(R.id.tv_mybillinfo_smallbilltime2))
						.setText(billInfo.getString("SmallBillDate1")
								+ " "
								+ BookActivity.minuteToClock(billInfo
										.getInt("SmallBillTime1") * 30)
								+ "-"
								+ BookActivity.minuteToClock((billInfo
										.getInt("SmallBillTime1") + billInfo
										.getInt("Weight")) * 30));

				TextView tv_checkstatus = (TextView) findViewById(R.id.tv_mybillinfo_checkstatus2);
				TextView tv_checkcode = (TextView) findViewById(R.id.tv_mybillinfo_checkcode2);
				Button btn_changecourt = (Button) findViewById(R.id.btn_mybillinfo_changecourt2);
				switch (billInfo.getInt("Status")) {
				case -1:
					tv_checkstatus.setVisibility(View.GONE);
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				case 0:
					tv_checkstatus.setVisibility(View.GONE);
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				case 1:
					tv_checkstatus.setVisibility(View.GONE);
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				case 2:
					if (billInfo.getInt("SmallBillStatus1") == 0) {
						tv_checkstatus.setText("未验证  验证码");
						tv_checkcode.setText(billInfo.getString("CheckCode1"));
						//btn_changecourt.setVisibility(View.VISIBLE);
						btn_changecourt.setVisibility(View.GONE);
					} else {
						tv_checkstatus.setText("已验证");
						tv_checkcode.setVisibility(View.GONE);
						btn_changecourt.setVisibility(View.GONE);
					}
					break;
				case 3:
					tv_checkstatus.setText("已验证");
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				case 4:
					tv_checkstatus.setText("已验证");
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				}

			}
			if (smallbillNum >= 3) {
				((LinearLayout) findViewById(R.id.ll_mybillinfo_smallbill3))
						.setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.tv_mybillinfo_courtname3))
						.setText(billInfo.getString("CourtName2"));
				((TextView) findViewById(R.id.tv_mybillinfo_smallbilltime3))
						.setText(billInfo.getString("SmallBillDate2")
								+ " "
								+ BookActivity.minuteToClock(billInfo
										.getInt("SmallBillTime2") * 30)
								+ "-"
								+ BookActivity.minuteToClock((billInfo
										.getInt("SmallBillTime2") + billInfo
										.getInt("Weight")) * 30));
				TextView tv_checkstatus = (TextView) findViewById(R.id.tv_mybillinfo_checkstatus3);
				TextView tv_checkcode = (TextView) findViewById(R.id.tv_mybillinfo_checkcode3);
				Button btn_changecourt = (Button) findViewById(R.id.btn_mybillinfo_changecourt3);
				switch (billInfo.getInt("Status")) {
				case -1:
					tv_checkstatus.setVisibility(View.GONE);
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				case 0:
					tv_checkstatus.setVisibility(View.GONE);
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				case 1:
					tv_checkstatus.setVisibility(View.GONE);
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				case 2:
					if (billInfo.getInt("SmallBillStatus2") == 0) {
						tv_checkstatus.setText("未验证  验证码");
						tv_checkcode.setText(billInfo.getString("CheckCode2"));
						//btn_changecourt.setVisibility(View.VISIBLE);
						btn_changecourt.setVisibility(View.GONE);
					} else {
						tv_checkstatus.setText("已验证");
						tv_checkcode.setVisibility(View.GONE);
						btn_changecourt.setVisibility(View.GONE);
					}
					break;
				case 3:
					tv_checkstatus.setText("已验证");
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				case 4:
					tv_checkstatus.setText("已验证");
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				}
			}
			if (smallbillNum >= 4) {
				((LinearLayout) findViewById(R.id.ll_mybillinfo_smallbill4))
						.setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.tv_mybillinfo_courtname4))
						.setText(billInfo.getString("CourtName3"));
				((TextView) findViewById(R.id.tv_mybillinfo_smallbilltime4))
						.setText(billInfo.getString("SmallBillDate3")
								+ " "
								+ BookActivity.minuteToClock(billInfo
										.getInt("SmallBillTime3") * 30)
								+ "-"
								+ BookActivity.minuteToClock((billInfo
										.getInt("SmallBillTime3") + billInfo
										.getInt("Weight")) * 30));
				TextView tv_checkstatus = (TextView) findViewById(R.id.tv_mybillinfo_checkstatus4);
				TextView tv_checkcode = (TextView) findViewById(R.id.tv_mybillinfo_checkcode4);
				Button btn_changecourt = (Button) findViewById(R.id.btn_mybillinfo_changecourt4);
				switch (billInfo.getInt("Status")) {
				case -1:
					tv_checkstatus.setVisibility(View.GONE);
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				case 0:
					tv_checkstatus.setVisibility(View.GONE);
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				case 1:
					tv_checkstatus.setVisibility(View.GONE);
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				case 2:
					if (billInfo.getInt("SmallBillStatus3") == 0) {
						tv_checkstatus.setText("未验证  验证码");
						tv_checkcode.setText(billInfo.getString("CheckCode3"));
						//btn_changecourt.setVisibility(View.VISIBLE);
						btn_changecourt.setVisibility(View.GONE);
					} else {
						tv_checkstatus.setText("已验证");
						tv_checkcode.setVisibility(View.GONE);
						btn_changecourt.setVisibility(View.GONE);
					}
					break;
				case 3:
					tv_checkstatus.setText("已验证");
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				case 4:
					tv_checkstatus.setText("已验证");
					tv_checkcode.setVisibility(View.GONE);
					btn_changecourt.setVisibility(View.GONE);
					break;
				}
			}

			Button btn_button = (Button) findViewById(R.id.btn_mybillinfo_button);
			if (billInfo.getInt("Status") == 1) {
				btn_button.setText("立即付款");
				btn_button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(MybillInfoActivity.this,
								BookConfirmActivity.class);
						try {
							intent.putExtra("isFromBill", true);
							intent.putExtra("gymType",
									billInfo.getInt("GymMainType") + "");
							intent.putExtra("gymName",
									billInfo.getString("GymName"));
							intent.putExtra("date",
									billInfo.getString("SmallBillDate0"));
							intent.putExtra("courtNum",
									billInfo.getInt("SmallBillNum"));
							intent.putExtra("ID", billInfo.getInt("GymID") + "");
							intent.putExtra("BillID", billID);
							intent.putExtra("weight", billInfo.getInt("Weight"));
							for (int i = 0; i < billInfo.getInt("SmallBillNum"); i++) {
								intent.putExtra("time" + i,
										billInfo.getInt("SmallBillTime" + i));
								intent.putExtra("name" + i,
										billInfo.getString("CourtName" + i));
								intent.putExtra(
										"price" + i,
										billInfo.getDouble("SmallBillPrice" + i)
												+ "");
							}
							intent.putExtra("money",
									billInfo.getDouble("Price"));
							startActivity(intent);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				});
			} else if (billInfo.getInt("Status") == 3) {
				btn_button.setText("立即评价");
				btn_button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(MybillInfoActivity.this,
								CommitGymcommentActivity.class);
						intent.putExtra("info", billInfo.toString());
						intent.putExtra("billID", billID);
						startActivity(intent);
					}
				});
			} else {
				btn_button.setVisibility(View.GONE);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_mybillinfo_leftarrow:
				finish();
				break;
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

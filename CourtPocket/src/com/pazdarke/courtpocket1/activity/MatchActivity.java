package com.pazdarke.courtpocket1.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MatchActivity extends Activity {
	
	Intent intent;
	ProgressDialog progressDialog;
	
    ImageView iv_leftarrow;
	
	Button btn_submit;
	
	LinearLayout ll_date1, ll_date2, ll_date3, ll_date4, ll_date5, ll_date6,
			ll_date7;
	TextView tv_week1, tv_week2, tv_week3, tv_week4, tv_week5, tv_week6,
			tv_week7;
	TextView tv_date1, tv_date2, tv_date3, tv_date4, tv_date5, tv_date6,
			tv_date7;
	LinearLayout[] ll_date;
	TextView[] tv_week;
	TextView[] tv_date;
	String currentSelectDate;
	int currentSelectTime;// 0早上 1下午 2晚上
	
	SimpleDateFormat dateFormat;
	SimpleDateFormat weekFormat;
	SimpleDateFormat fullDateFormat;
	List<Date> days;
	
	TextView tv_gymname, tv_gymtype, tv_currentdate, tv_morning, tv_afternoon,
			tv_evening;
	
	MatchresultHandler matchresultHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_match);
		
		AppManager.getAppManager().addActivity(this);
		
		progressDialog=new ProgressDialog(this);
		progressDialog.setMessage("正在努力提交匹配请求...");
		progressDialog.setCancelable(false);
		
		iv_leftarrow=(ImageView)findViewById(R.id.iv_match_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);
		
		btn_submit=(Button)findViewById(R.id.btn_match_submit);
		btn_submit.setOnClickListener(onClickListener);
		
		initView();
		
		matchresultHandler=new MatchresultHandler();
		
	}
	
	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_match_leftarrow:
				finish();
				break;
			case R.id.btn_match_submit:
				progressDialog.show();
				new Thread(r_Match).start();
				break;
			}
		}
	};
	
	void initView(){
		
		init_date();
		
		tv_gymname=(TextView)findViewById(R.id.tv_match_gymname);
		tv_gymtype=(TextView)findViewById(R.id.tv_match_gymtype);
		tv_currentdate=(TextView)findViewById(R.id.tv_match_date);
		tv_morning=(TextView)findViewById(R.id.tv_match_morning);
		tv_afternoon=(TextView)findViewById(R.id.tv_match_afternoon);
		tv_evening=(TextView)findViewById(R.id.tv_match_evening);
		
		intent=getIntent();
		tv_gymname.setText("选择场馆："+intent.getStringExtra("gymName"));
		
		String str_type=null;
		switch(intent.getIntExtra("gymType", 1)){
		case 1:
			str_type="足球场";
			break;
		case 2:
			str_type="篮球场";
			break;
		case 3:
			str_type="排球场";
			break;
		case 4:
			str_type="乒乓球场";
			break;
		case 5:
			str_type="网球场";
			break;
		case 6:
			str_type="羽毛球场";
			break;
		case 7:
			str_type="桌球场";
			break;
		case 8:
			str_type="游泳场";
			break;
		case 9:
			str_type="健身房";
			break;
		case 10:
			str_type="极限运动";
			break;
		case 11:
			str_type="高尔夫球场";
			break;
		}
		tv_gymtype.setText("选择项目："+str_type);
		
		tv_currentdate.setText("日期："+currentSelectDate);
		
		tv_morning.setOnClickListener(timeClickListener);
		tv_afternoon.setOnClickListener(timeClickListener);
		tv_evening.setOnClickListener(timeClickListener);
		
		currentSelectTime=0;
		
	}
	
	void init_date() {

		// 获取未来七天的日期
		dateFormat = new SimpleDateFormat("MM月dd日");
		weekFormat = new SimpleDateFormat("EEE");
		fullDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date currentDate = new Date();
		days = dateToWeek(currentDate);

		ll_date1 = (LinearLayout) findViewById(R.id.ll_match_date1);
		ll_date1.setOnClickListener(dateOnClickListener);
		ll_date2 = (LinearLayout) findViewById(R.id.ll_match_date2);
		ll_date2.setOnClickListener(dateOnClickListener);
		ll_date3 = (LinearLayout) findViewById(R.id.ll_match_date3);
		ll_date3.setOnClickListener(dateOnClickListener);
		ll_date4 = (LinearLayout) findViewById(R.id.ll_match_date4);
		ll_date4.setOnClickListener(dateOnClickListener);
		ll_date5 = (LinearLayout) findViewById(R.id.ll_match_date5);
		ll_date5.setOnClickListener(dateOnClickListener);
		ll_date6 = (LinearLayout) findViewById(R.id.ll_match_date6);
		ll_date6.setOnClickListener(dateOnClickListener);
		ll_date7 = (LinearLayout) findViewById(R.id.ll_match_date7);
		ll_date7.setOnClickListener(dateOnClickListener);

		tv_week1 = (TextView) findViewById(R.id.tv_match_week1);
		tv_week2 = (TextView) findViewById(R.id.tv_match_week2);
		tv_week2.setText(weekFormat.format(days.get(1)) + "");
		tv_week3 = (TextView) findViewById(R.id.tv_match_week3);
		tv_week3.setText(weekFormat.format(days.get(2)) + "");
		tv_week4 = (TextView) findViewById(R.id.tv_match_week4);
		tv_week4.setText(weekFormat.format(days.get(3)) + "");
		tv_week5 = (TextView) findViewById(R.id.tv_match_week5);
		tv_week5.setText(weekFormat.format(days.get(4)) + "");
		tv_week6 = (TextView) findViewById(R.id.tv_match_week6);
		tv_week6.setText(weekFormat.format(days.get(5)) + "");
		tv_week7 = (TextView) findViewById(R.id.tv_match_week7);
		tv_week7.setText(weekFormat.format(days.get(6)) + "");

		tv_date1 = (TextView) findViewById(R.id.tv_match_date1);
		tv_date1.setText(dateFormat.format(days.get(0)) + "");
		tv_date2 = (TextView) findViewById(R.id.tv_match_date2);
		tv_date2.setText(dateFormat.format(days.get(1)) + "");
		tv_date3 = (TextView) findViewById(R.id.tv_match_date3);
		tv_date3.setText(dateFormat.format(days.get(2)) + "");
		tv_date4 = (TextView) findViewById(R.id.tv_match_date4);
		tv_date4.setText(dateFormat.format(days.get(3)) + "");
		tv_date5 = (TextView) findViewById(R.id.tv_match_date5);
		tv_date5.setText(dateFormat.format(days.get(4)) + "");
		tv_date6 = (TextView) findViewById(R.id.tv_match_date6);
		tv_date6.setText(dateFormat.format(days.get(5)) + "");
		tv_date7 = (TextView) findViewById(R.id.tv_match_date7);
		tv_date7.setText(dateFormat.format(days.get(6)) + "");

		ll_date = new LinearLayout[7];
		ll_date[0] = ll_date1;
		ll_date[1] = ll_date2;
		ll_date[2] = ll_date3;
		ll_date[3] = ll_date4;
		ll_date[4] = ll_date5;
		ll_date[5] = ll_date6;
		ll_date[6] = ll_date7;

		tv_week = new TextView[7];
		tv_week[0] = tv_week1;
		tv_week[1] = tv_week2;
		tv_week[2] = tv_week3;
		tv_week[3] = tv_week4;
		tv_week[4] = tv_week5;
		tv_week[5] = tv_week6;
		tv_week[6] = tv_week7;

		tv_date = new TextView[7];
		tv_date[0] = tv_date1;
		tv_date[1] = tv_date2;
		tv_date[2] = tv_date3;
		tv_date[3] = tv_date4;
		tv_date[4] = tv_date5;
		tv_date[5] = tv_date6;
		tv_date[6] = tv_date7;

		currentSelectDate = fullDateFormat.format(days.get(0));
		

	}
	
	OnClickListener dateOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			for (int i = 0; i < 7; i++) {
				ll_date[i].setBackgroundResource(R.drawable.shape_date);
				tv_week[i].setTextColor(getResources().getColor(R.color.black));
				tv_date[i].setTextColor(getResources().getColor(
						R.color.darkGrey));
			}
			v.setBackgroundResource(R.drawable.shape_date_selected);
			switch (v.getId()) {
			case R.id.ll_match_date1:
				tv_week[0].setTextColor(getResources().getColor(R.color.white));
				tv_date[0].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(0));
				break;
			case R.id.ll_match_date2:
				tv_week[1].setTextColor(getResources().getColor(R.color.white));
				tv_date[1].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(1));
				break;
			case R.id.ll_match_date3:
				tv_week[2].setTextColor(getResources().getColor(R.color.white));
				tv_date[2].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(2));
				break;
			case R.id.ll_match_date4:
				tv_week[3].setTextColor(getResources().getColor(R.color.white));
				tv_date[3].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(3));
				break;
			case R.id.ll_match_date5:
				tv_week[4].setTextColor(getResources().getColor(R.color.white));
				tv_date[4].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(4));
				break;
			case R.id.ll_match_date6:
				tv_week[5].setTextColor(getResources().getColor(R.color.white));
				tv_date[5].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(5));
				break;
			case R.id.ll_match_date7:
				tv_week[6].setTextColor(getResources().getColor(R.color.white));
				tv_date[6].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(6));
				break;
			}
			tv_currentdate.setText("日期："+currentSelectDate);
		}
	};

	/**
	 * 根据日期获得所在周的日期
	 * 
	 * @param mdate
	 * @return
	 */
	public static List<Date> dateToWeek(Date mdate) {
		Date fdate;
		List<Date> list = new ArrayList<Date>();
		Long fTime = mdate.getTime();
		for (int a = 1; a <= 7; a++) {
			fdate = new Date();
			fdate.setTime(fTime + (a * 24 * 3600000));
			list.add(a-1, fdate);
		}
		return list;
	}
	
	OnClickListener timeClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			tv_morning.setBackgroundResource(R.drawable.shape_match_time);
			tv_morning.setTextColor(getResources().getColor(R.color.blue));
			tv_afternoon.setBackgroundResource(R.drawable.shape_match_time);
			tv_afternoon.setTextColor(getResources().getColor(R.color.blue));
			tv_evening.setBackgroundResource(R.drawable.shape_match_time);
			tv_evening.setTextColor(getResources().getColor(R.color.blue));
			
			switch(v.getId()){
			case R.id.tv_match_morning:
				tv_morning.setBackgroundResource(R.drawable.shape_match_time_selected);
				tv_morning.setTextColor(getResources().getColor(R.color.white));
				currentSelectTime=0;
				break;
			case R.id.tv_match_afternoon:
				tv_afternoon.setBackgroundResource(R.drawable.shape_match_time_selected);
				tv_afternoon.setTextColor(getResources().getColor(R.color.white));
				currentSelectTime=1;
				break;
			case R.id.tv_match_evening:
				tv_evening.setBackgroundResource(R.drawable.shape_match_time_selected);
				tv_evening.setTextColor(getResources().getColor(R.color.white));
				currentSelectTime=2;
				break;
			}
			
		}
	};
	
	Runnable r_Match=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request",
					"NewMatch"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("GymID", intent.getStringExtra("ID")));
			params.add(new BasicNameValuePair("Type", intent.getIntExtra("gymType", 1)+""));
			params.add(new BasicNameValuePair("Time", currentSelectTime+""));
			params.add(new BasicNameValuePair("Date", currentSelectDate));

			String result = new HttpPostConnection("MatchServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			matchresultHandler.sendMessage(msg);

		}
	};

	class MatchresultHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			progressDialog.dismiss();

			Bundle b = msg.getData();
			String result = b.getString("result");
			try {
				if (result.equals("timeout")) {
					Toast.makeText(MatchActivity.this, "网络好像出问题了呢",
							Toast.LENGTH_SHORT).show();
					return;
				}

				JSONObject json = new JSONObject(result);
				result = json.getString("Result");
				if (result.equals("现已加入肯德基豪华午餐") || result.equals("找到对手")) {
					Toast.makeText(MatchActivity.this, result,
							Toast.LENGTH_SHORT).show();
					Intent intent=new Intent(MatchActivity.this,
							MatchlistActivity.class);
					intent.putExtra("UserID", Data.USERID);
					intent.putExtra("Passcode", Data.PASSCODE);
					startActivity(intent);
					finish();
				} else {
					Toast.makeText(MatchActivity.this, result,
							Toast.LENGTH_SHORT).show();
				}

			} catch (Exception e) {
				Toast.makeText(MatchActivity.this, result, Toast.LENGTH_SHORT)
						.show();
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

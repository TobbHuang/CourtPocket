package com.pazdarke.courtpocket1.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.ant.liao.GifView;
import com.ant.liao.GifView.GifImageType;
import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.activity.BookActivity.MyOnClickListener;
import com.pazdarke.courtpocket1.activity.BookActivity.TableAdapter;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.pazdarke.courtpocket1.view.BaseTableAdapter;
import com.pazdarke.courtpocket1.view.TableFixHeaders;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class FightbookActivity extends Activity {

	ImageView iv_leftarrow;

	RelativeLayout rl_gifcontainer;
	TextView tv_error;

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

	SimpleDateFormat dateFormat;
	SimpleDateFormat weekFormat;
	SimpleDateFormat fullDateFormat;
	List<Date> days;

	TableFixHeaders ll_table;

	boolean[][] courtIsSelected;
	int countIsSelected = 0;
	LinearLayout ll_instruction, ll_selection;

	int courtNum;
	String[] courtID;
	String[] courtName;
	String[][] courtPrice;
	boolean[][] isfightbooked;
	TextView[][] courtItem;
	double money;
	int startTime, endTime, weight;

	Button btn_submit;

	CourtHandler courtHandler;
	TimeoutHandler timeoutHandler;

	int loadTime = 0;// 多线程加载数据，用于判断是否加载完毕
	ExecutorService pool;
	private Lock lock = new ReentrantLock();// 锁对象

	public static Activity instance_fightbook;// 用于关闭

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_fightbook);

		AppManager.getAppManager().addActivity(this);

		pool = Executors.newFixedThreadPool(10);

		instance_fightbook = this;

		Intent intent = getIntent();
		startTime = intent.getIntExtra("startTime", 0);
		endTime = intent.getIntExtra("endTime", 47);
		weight = intent.getIntExtra("weight", 1);

		init_date();
		init_gif();

		tv_error = (TextView) findViewById(R.id.tv_fightbook_error);
		tv_error.setOnClickListener(onClickListener);

		iv_leftarrow = (ImageView) findViewById(R.id.iv_fightbook_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		ll_table = (TableFixHeaders) findViewById(R.id.ll_fightbook_table);
		ll_table.getLayoutParams().height = (int) (Data.SCREENHEIGHT * 0.5);

		ll_instruction = (LinearLayout) findViewById(R.id.ll_fightbook_instruction);
		ll_selection = (LinearLayout) findViewById(R.id.ll_fightbook_selection);

		btn_submit = (Button) findViewById(R.id.btn_fightbook_submit);
		btn_submit.setOnClickListener(onClickListener);

		courtHandler = new CourtHandler();
		timeoutHandler = new TimeoutHandler();

		new Thread(r_GetCourtInfo).start();

	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_fightbook_leftarrow:
				finish();
				break;
			case R.id.tv_fightbook_error:
				rl_gifcontainer.setVisibility(View.VISIBLE);
				tv_error.setVisibility(View.GONE);
				new Thread(r_GetCourtInfo).start();
				break;
			case R.id.btn_fightbook_submit:
				if (countIsSelected != 0) {
					Intent intent = new Intent(FightbookActivity.this,
							FightbookConfirmActivity.class);
					intent.putExtra("gymType", FightbookActivity.this
							.getIntent().getIntExtra("gymType", 1));
					intent.putExtra("gymName", FightbookActivity.this
							.getIntent().getStringExtra("gymName"));
					intent.putExtra("teamName", FightbookActivity.this
							.getIntent().getStringExtra("teamName"));
					intent.putExtra("date", currentSelectDate);
					intent.putExtra("courtNum", countIsSelected);
					intent.putExtra("ID", getIntent().getStringExtra("ID"));
					intent.putExtra("teamID",
							getIntent().getStringExtra("teamID"));
					intent.putExtra("weight", weight);

					// 按球场名、时间排序，体验效果更好，在订单页不会乱
					int k = 0;
					for (int i = 0; i < courtNum; i++)
						for (int j = 0; j < 48; j++) {
							if (courtIsSelected[i][j]) {
								intent.putExtra("time" + k, j);
								intent.putExtra("courtID" + k, courtID[i]);
								intent.putExtra("name" + k, courtName[i]);
								intent.putExtra("price" + k, courtPrice[i][j]);
								k++;
							}
						}

					intent.putExtra("money", money);
					startActivity(intent);
				}
				break;
			}
		}
	};

	void init_date() {

		// 获取未来七天的日期
		dateFormat = new SimpleDateFormat("MM月dd日");
		weekFormat = new SimpleDateFormat("EEE");
		fullDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date currentDate = new Date();
		days = dateToWeek(currentDate);

		ll_date1 = (LinearLayout) findViewById(R.id.ll_fightbook_date1);
		// ll_date1.setOnClickListener(dateOnClickListener);
		ll_date2 = (LinearLayout) findViewById(R.id.ll_fightbook_date2);
		// ll_date2.setOnClickListener(dateOnClickListener);
		ll_date3 = (LinearLayout) findViewById(R.id.ll_fightbook_date3);
		// ll_date3.setOnClickListener(dateOnClickListener);
		ll_date4 = (LinearLayout) findViewById(R.id.ll_fightbook_date4);
		// ll_date4.setOnClickListener(dateOnClickListener);
		ll_date5 = (LinearLayout) findViewById(R.id.ll_fightbook_date5);
		// ll_date5.setOnClickListener(dateOnClickListener);
		ll_date6 = (LinearLayout) findViewById(R.id.ll_fightbook_date6);
		// ll_date6.setOnClickListener(dateOnClickListener);
		ll_date7 = (LinearLayout) findViewById(R.id.ll_fightbook_date7);
		// ll_date7.setOnClickListener(dateOnClickListener);

		tv_week1 = (TextView) findViewById(R.id.tv_fightbook_week1);
		tv_week2 = (TextView) findViewById(R.id.tv_fightbook_week2);
		tv_week2.setText(weekFormat.format(days.get(1)) + "");
		tv_week3 = (TextView) findViewById(R.id.tv_fightbook_week3);
		tv_week3.setText(weekFormat.format(days.get(2)) + "");
		tv_week4 = (TextView) findViewById(R.id.tv_fightbook_week4);
		tv_week4.setText(weekFormat.format(days.get(3)) + "");
		tv_week5 = (TextView) findViewById(R.id.tv_fightbook_week5);
		tv_week5.setText(weekFormat.format(days.get(4)) + "");
		tv_week6 = (TextView) findViewById(R.id.tv_fightbook_week6);
		tv_week6.setText(weekFormat.format(days.get(5)) + "");
		tv_week7 = (TextView) findViewById(R.id.tv_fightbook_week7);
		tv_week7.setText(weekFormat.format(days.get(6)) + "");

		tv_date1 = (TextView) findViewById(R.id.tv_fightbook_date1);
		tv_date1.setText(dateFormat.format(days.get(0)) + "");
		tv_date2 = (TextView) findViewById(R.id.tv_fightbook_date2);
		tv_date2.setText(dateFormat.format(days.get(1)) + "");
		tv_date3 = (TextView) findViewById(R.id.tv_fightbook_date3);
		tv_date3.setText(dateFormat.format(days.get(2)) + "");
		tv_date4 = (TextView) findViewById(R.id.tv_fightbook_date4);
		tv_date4.setText(dateFormat.format(days.get(3)) + "");
		tv_date5 = (TextView) findViewById(R.id.tv_fightbook_date5);
		tv_date5.setText(dateFormat.format(days.get(4)) + "");
		tv_date6 = (TextView) findViewById(R.id.tv_fightbook_date6);
		tv_date6.setText(dateFormat.format(days.get(5)) + "");
		tv_date7 = (TextView) findViewById(R.id.tv_fightbook_date7);
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
			case R.id.ll_fightbook_date1:
				tv_week[0].setTextColor(getResources().getColor(R.color.white));
				tv_date[0].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(0));
				break;
			case R.id.ll_fightbook_date2:
				tv_week[1].setTextColor(getResources().getColor(R.color.white));
				tv_date[1].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(1));
				break;
			case R.id.ll_fightbook_date3:
				tv_week[2].setTextColor(getResources().getColor(R.color.white));
				tv_date[2].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(2));
				break;
			case R.id.ll_fightbook_date4:
				tv_week[3].setTextColor(getResources().getColor(R.color.white));
				tv_date[3].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(3));
				break;
			case R.id.ll_fightbook_date5:
				tv_week[4].setTextColor(getResources().getColor(R.color.white));
				tv_date[4].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(4));
				break;
			case R.id.ll_fightbook_date6:
				tv_week[5].setTextColor(getResources().getColor(R.color.white));
				tv_date[5].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(5));
				break;
			case R.id.ll_fightbook_date7:
				tv_week[6].setTextColor(getResources().getColor(R.color.white));
				tv_date[6].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(6));
				break;
			}
			rl_gifcontainer.setVisibility(View.VISIBLE);
			tv_error.setVisibility(View.GONE);
			btn_submit.setText("提交订单");
			ll_selection.removeAllViews();
			ll_selection.setVisibility(View.GONE);
			ll_instruction.setVisibility(View.VISIBLE);
			countIsSelected = 0;
			money = 0;
			loadTime = 0;
			ll_date1.setOnClickListener(null);
			ll_date2.setOnClickListener(null);
			ll_date3.setOnClickListener(null);
			ll_date4.setOnClickListener(null);
			ll_date5.setOnClickListener(null);
			ll_date6.setOnClickListener(null);
			ll_date7.setOnClickListener(null);
			new Thread(r_GetCourtInfo).start();
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
		for (int a = 0; a <= 6; a++) {
			fdate = new Date();
			fdate.setTime(fTime + (a * 24 * 3600000));
			list.add(a, fdate);
		}
		return list;
	}

	@SuppressWarnings("deprecation")
	private void makeItems() {

		/*
		 * mItemRoom = new HScrollView(this, mGestureDetector);
		 * mItemRoom.setListener(this); LinearLayout VLayout = new
		 * LinearLayout(this); VLayout.setOrientation(LinearLayout.VERTICAL);
		 * 
		 * for (int i = 0; i < 16; i++) { LinearLayout HLayout = new
		 * LinearLayout(this); for (int j = 0; j < courtNum; j++) { TextView
		 * item = new TextView(this);
		 * 
		 * if (!isfightbooked[j][i + Data.COURTTIME]) { item.setText("￥" +
		 * courtPrice[j][i + Data.COURTTIME]);
		 * item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		 * item.setGravity(Gravity.CENTER);
		 * item.setBackgroundDrawable(getResources().getDrawable(
		 * R.drawable.shape_bookitem)); item.setTextColor(Color.WHITE);
		 * item.setOnClickListener(new MyOnClickListener(j, i +
		 * Data.COURTTIME));
		 * 
		 * courtItem[j][i+Data.COURTTIME]=item;
		 * 
		 * } else { item.setText("");
		 * item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		 * item.setGravity(Gravity.CENTER);
		 * item.setBackgroundDrawable(getResources().getDrawable(
		 * R.drawable.shape_bookitem_noselected));
		 * item.setTextColor(Color.WHITE); item.setClickable(false); }
		 * 
		 * LinearLayout.LayoutParams params = new LayoutParams( (int)
		 * (Data.SCREENWIDTH * 0.2), (int) (Data.SCREENHEIGHT * 0.0625)); //
		 * params.setMargins(3, 3, 3, 3); HLayout.addView(item, params); }
		 * VLayout.addView(HLayout); }
		 * 
		 * mItemRoom.addView(VLayout);
		 * 
		 * mVertical = new ScrollView(this);
		 * mVertical.setVerticalScrollBarEnabled(false); LinearLayout hlayout =
		 * new LinearLayout(this); LinearLayout vlayout = new
		 * LinearLayout(this); vlayout.setOrientation(LinearLayout.VERTICAL);
		 * for (int i = 0; i < 16; i++) { TextView item = new TextView(this);
		 * item.setBackgroundColor(Color.WHITE); item.setText((i+Data.COURTTIME)
		 * + ":00-"); // item.setGravity(Gravity.RIGHT);
		 * item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		 * LinearLayout.LayoutParams params = new LayoutParams(
		 * LayoutParams.WRAP_CONTENT, (int) (Data.SCREENHEIGHT * 0.0625)); if (i
		 * == 0) { params.setMargins(0, -(int) (Data.SCREENHEIGHT * 0.0625 *
		 * 0.1), 5, 0); } else if (i == 1) { params.setMargins(0, -(int)
		 * (Data.SCREENHEIGHT * 0.0625 * 0.2), 5, 0); } else {
		 * params.setMargins(0, 0, 5, 0); } vlayout.addView(item, params); }
		 * hlayout.addView(vlayout); hlayout.addView(mItemRoom);
		 * 
		 * mVertical.addView(hlayout);
		 * 
		 * LinearLayout tophlayout = new LinearLayout(this); mTimeItem = new
		 * HScrollView(this, mGestureDetector); mTimeItem.setListener(this);
		 * LinearLayout layout = new LinearLayout(this); for (int i = 0; i <
		 * courtNum; i++) { TextView item = new TextView(this);
		 * item.setText(courtName[i]); item.setGravity(Gravity.BOTTOM);
		 * item.setBackgroundColor(Color.WHITE);
		 * item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		 * LinearLayout.LayoutParams params = new LayoutParams( (int)
		 * (Data.SCREENWIDTH * 0.2) - 20, LayoutParams.WRAP_CONTENT);
		 * params.setMargins(10, 0, 10, 5); layout.addView(item, params); }
		 * mTimeItem.addView(layout); TextView item = new TextView(this);
		 * item.setBackgroundColor(Color.WHITE); int w =
		 * View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		 * int h = View.MeasureSpec.makeMeasureSpec(0,
		 * View.MeasureSpec.UNSPECIFIED); vlayout.measure(w, h); int width =
		 * vlayout.getMeasuredWidth(); LinearLayout.LayoutParams params = new
		 * LayoutParams(width, LayoutParams.WRAP_CONTENT);
		 * tophlayout.addView(item, params); tophlayout.addView(mTimeItem);
		 * 
		 * mContain = new LinearLayout(this);
		 * mContain.setOrientation(LinearLayout.VERTICAL);
		 * mContain.addView(tophlayout); mContain.addView(mVertical);
		 * 
		 * ll_table.removeAllViews(); ll_table.addView(mContain);
		 */

		ll_table.setAdapter(new TableAdapter());

	}

	class MyOnClickListener implements OnClickListener {

		int i, j;

		MyOnClickListener(int i, int j) {
			this.i = i;
			this.j = j;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			if (courtIsSelected[i][j]) {
				v.setBackgroundResource(R.color.blue);
				courtIsSelected[i][j] = false;
				countIsSelected--;

				money = money - Double.parseDouble(courtPrice[i][j]);
				btn_submit.setText("￥" + Data.doubleTrans(money) + " 提交订单");

				if (countIsSelected == 0) {
					ll_instruction.setVisibility(View.VISIBLE);
					ll_selection.setVisibility(View.GONE);
					btn_submit.setText("提交订单");
				}
				// fightbookitem反选
				for (int k = 0; k < ll_selection.getChildCount(); k++) {
					// System.out.println(k + "");
					View view = ll_selection.getChildAt(k);
					TextView tv_describe = (TextView) view
							.findViewById(R.id.tv_bookitem_describe);
					String[] temp = tv_describe.getText().toString().split("-");
					if (temp[0].equals(i + "") && temp[1].equals(j + "")) {
						ll_selection.removeView(view);
						break;
					}
				}

				// 打包反选
				/*
				 * int k; if(gymType==1){ if(j%2==0){ k=j-1; } else{ k=j+1; }
				 * 
				 * courtItem[i][k].setBackgroundResource(R.color.blue);
				 * courtIsSelected[i][k] = false; countIsSelected--;
				 * 
				 * money = money - Integer.parseInt(courtPrice[i][k]);
				 * btn_submit.setText("￥" + money + " 提交订单");
				 * 
				 * if (countIsSelected == 0) {
				 * ll_instruction.setVisibility(View.VISIBLE);
				 * ll_selection.setVisibility(View.GONE);
				 * btn_submit.setText("提交订单"); } // bookitem反选 for (int k1 = 0;
				 * k1 < ll_selection.getChildCount(); k1++) { View view =
				 * ll_selection.getChildAt(k1); TextView tv_describe =
				 * (TextView) view .findViewById(R.id.tv_bookitem_describe);
				 * String[] temp = tv_describe.getText().toString().split("-");
				 * if (temp[0].equals(i + "") && temp[1].equals(k + "")) {
				 * ll_selection.removeView(view); break; } }
				 * 
				 * }
				 */

			} else {
				if (countIsSelected < 1) {
					v.setBackgroundResource(R.color.yellow);
					courtIsSelected[i][j] = true;
					countIsSelected++;
					ll_instruction.setVisibility(View.GONE);
					ll_selection.setVisibility(View.VISIBLE);

					LayoutInflater mLi = LayoutInflater
							.from(FightbookActivity.this);
					View view = mLi.inflate(R.layout.layout_bookitem, null);
					TextView tv_time = (TextView) view
							.findViewById(R.id.tv_bookitem_time);
					TextView tv_courtname = (TextView) view
							.findViewById(R.id.tv_bookitem_courtname);
					TextView tv_describe = (TextView) view
							.findViewById(R.id.tv_bookitem_describe);

					tv_time.setText(BookActivity.minuteToClock(j * 30) + "-"
							+ BookActivity.minuteToClock((j + weight) * 30));
					tv_courtname.setText(courtName[i]);
					// 用于标识，方便反选
					tv_describe.setText(i + "-" + j);

					LinearLayout.LayoutParams p = new LayoutParams(
							(int) (Data.SCREENWIDTH * 0.23),
							LayoutParams.MATCH_PARENT);
					p.setMargins((int) (Data.SCREENWIDTH * 0.016), 0, 0, 0);

					ll_selection.addView(view, p);

					money = money + Double.parseDouble(courtPrice[i][j]);
					btn_submit.setText("￥" + Data.doubleTrans(money) + " 提交订单");

					// 如果是足球，则一次最少选两个
					/*
					 * int k; if(gymType==1){ if(j%2==0){ k=j-1; } else{ k=j+1;
					 * }
					 * 
					 * courtItem[i][k].setBackgroundResource(R.color.yellow);
					 * courtIsSelected[i][k] = true; countIsSelected++;
					 * ll_instruction.setVisibility(View.GONE);
					 * ll_selection.setVisibility(View.VISIBLE);
					 * 
					 * mLi = LayoutInflater.from(FightbookActivity.this); view =
					 * mLi.inflate(R.layout.layout_bookitem, null); tv_time =
					 * (TextView) view .findViewById(R.id.tv_bookitem_time);
					 * tv_courtname = (TextView) view
					 * .findViewById(R.id.tv_bookitem_courtname); tv_describe =
					 * (TextView) view .findViewById(R.id.tv_bookitem_describe);
					 * 
					 * tv_time.setText(k + ":00-" + (k + 1) + ":00");
					 * tv_courtname.setText(courtName[i]); // 用于标识，方便反选
					 * tv_describe.setText(i + "-" + k);
					 * 
					 * ll_selection.addView(view, p);
					 * 
					 * money = money + Integer.parseInt(courtPrice[i][j]);
					 * btn_submit.setText("￥" + money + " 提交订单");
					 * 
					 * }
					 */

				} else
					Toast.makeText(FightbookActivity.this, "只能选择一个喔~",
							Toast.LENGTH_SHORT).show();
			}

		}

	}

	void init_gif() {
		GifView gif_loading = new GifView(this);
		gif_loading.setGifImage(R.drawable.gif_loading);
		gif_loading.setShowDimension((int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		gif_loading.setGifImageType(GifImageType.SYNC_DECODER);

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_fightbook_gifcontainer);
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
				(int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		p.addRule(RelativeLayout.CENTER_HORIZONTAL);
		p.setMargins(0, (int) (Data.SCREENHEIGHT * 0.22), 0, 0);
		rl_gifcontainer.addView(gif_loading, p);

	}

	Runnable r_GetCourtInfo = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				// 查询场地的数量、名称、ID，并初始化各个状态参数
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("Request",
						"RequestListByGymID"));
				params.add(new BasicNameValuePair("GymID",
						FightbookActivity.this.getIntent().getStringExtra("ID")));

				String result = new HttpPostConnection("GymInfoServer", params)
						.httpConnection();

				if (result.equals("timeout")) {
					Message msg = new Message();
					Bundle b = new Bundle();
					msg.setData(b);
					timeoutHandler.handleMessage(msg);
					return;
				}

				JSONObject json_courtinfo = new JSONObject(result);
				courtNum = (json_courtinfo.length() - 3) / 2;
				courtIsSelected = new boolean[courtNum][48];
				courtID = new String[courtNum];
				courtName = new String[courtNum];
				courtPrice = new String[courtNum][48];
				isfightbooked = new boolean[courtNum][48];
				courtItem = new TextView[courtNum][48];

				for (int i = 0; i < courtNum; i++) {
					for (int j = 0; j < 48; j++) {
						isfightbooked[i][j] = false;
						courtIsSelected[i][j] = false;
					}
				}

				for (int i = 0; i < courtNum; i++) {
					courtID[i] = json_courtinfo.getInt("Court" + i) + "";
					courtName[i] = json_courtinfo.getString("Name" + i) + "";
				}

				// 启动线程获取场地占用情况、价格
				for (int i = 0; i < courtNum; i++) {
					pool.execute(new TimeThread(i));
					pool.execute(new PriceThread(i));
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

	class TimeThread extends Thread {

		int i;

		TimeThread(int i) {
			this.i = i;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// 查询场地的占用情况
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "RequestTimeByCourtID"));
			params.add(new BasicNameValuePair("CourtID", courtID[i]));
			params.add(new BasicNameValuePair("Date", currentSelectDate));

			String result = new HttpPostConnection("GymInfoServer", params)
					.httpConnection();

			if (result.equals("timeout")) {
				Message msg = new Message();
				Bundle b = new Bundle();
				msg.setData(b);
				timeoutHandler.handleMessage(msg);
				return;
			}

			// 时间过了场地自动变为不可预订
			Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			String day = c.get(Calendar.DATE) + "";
			if (Integer.parseInt(day) < 10)
				day = "0" + day;
			String[] str = currentSelectDate.split("-");
			if (str[2].equals(day)) {

				for (int j = 0; j < 48; j++) {
					if ((hour * 2 + 1) > j)
						isfightbooked[i][j] = true;
				}

			}

			// 根据服务器传回的数据设定是否可预定
			try {
				JSONObject json = new JSONObject(result);

				for (int j = 0; j < 48; j++) {

					if (json.has("Time" + j)) {
						isfightbooked[i][j] = true;
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			lock.lock();
			loadTime++;
			if (loadTime >= courtNum * 2) {
				Message msg = new Message();
				Bundle b = new Bundle();
				msg.setData(b);
				courtHandler.sendMessage(msg);
			}
			lock.unlock();

		}
	}

	class PriceThread extends Thread {

		int i;

		PriceThread(int i) {
			this.i = i;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// 查询court的价格
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request",
					"RequestPriceByCourtID"));
			params.add(new BasicNameValuePair("CourtID", courtID[i]));
			params.add(new BasicNameValuePair("Date", currentSelectDate));

			String result = new HttpPostConnection("GymInfoServer", params)
					.httpConnection();

			if (result.equals("timeout")) {
				Message msg = new Message();
				Bundle b = new Bundle();
				msg.setData(b);
				timeoutHandler.handleMessage(msg);
				return;
			}

			try {
				JSONObject json = new JSONObject(result);

				for (int j = 0; j < 48; j++) {
					if (json.has(j + ""))
						courtPrice[i][j] = Data.doubleTrans(json.getDouble(j + ""));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			lock.lock();
			loadTime++;
			if (loadTime >= courtNum * 2) {
				Message msg = new Message();
				Bundle b = new Bundle();
				msg.setData(b);
				courtHandler.sendMessage(msg);
			}
			lock.unlock();

		}
	}

	class CourtHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			makeItems();

			rl_gifcontainer.setVisibility(View.GONE);
			ll_date1.setOnClickListener(dateOnClickListener);
			ll_date2.setOnClickListener(dateOnClickListener);
			ll_date3.setOnClickListener(dateOnClickListener);
			ll_date4.setOnClickListener(dateOnClickListener);
			ll_date5.setOnClickListener(dateOnClickListener);
			ll_date6.setOnClickListener(dateOnClickListener);
			ll_date7.setOnClickListener(dateOnClickListener);

		}
	}

	class TimeoutHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Toast.makeText(FightbookActivity.this, "网络出问题咩(+﹏+)~",
					Toast.LENGTH_SHORT).show();
			rl_gifcontainer.setVisibility(View.GONE);
			tv_error.setVisibility(View.VISIBLE);
			ll_date1.setOnClickListener(dateOnClickListener);
			ll_date2.setOnClickListener(dateOnClickListener);
			ll_date3.setOnClickListener(dateOnClickListener);
			ll_date4.setOnClickListener(dateOnClickListener);
			ll_date5.setOnClickListener(dateOnClickListener);
			ll_date6.setOnClickListener(dateOnClickListener);
			ll_date7.setOnClickListener(dateOnClickListener);
		}
	}

	class TableAdapter extends BaseTableAdapter {

		@Override
		public int getRowCount() {
			// TODO Auto-generated method stub
			return (endTime - startTime) / weight;
		}

		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return courtName.length;
		}

		@Override
		public View getView(int row, int column, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			try {
				// if (convertView == null) {
				convertView = new LinearLayout(FightbookActivity.this);
				((LinearLayout) convertView)
						.setOrientation(LinearLayout.VERTICAL);
				// }

				if (column == -1) {
					if (row != -1) {
						TextView tv = new TextView(FightbookActivity.this);
						tv.setText(BookActivity.minuteToClock((startTime + row
								* weight) * 30)
								+ "ˉ");
						tv.setTextColor(FightbookActivity.this.getResources()
								.getColor(R.color.black));
						tv.setBackgroundResource(R.color.white);
						tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
						// tv.setGravity(Gravity.END);

						LinearLayout.LayoutParams p = new LayoutParams(
								LayoutParams.WRAP_CONTENT,
								LayoutParams.MATCH_PARENT);
						p.gravity = Gravity.END;
						// p.setMargins(0, -(int) (Data.SCREENHEIGHT * 0.005),
						// 0, 0);
						((LinearLayout) convertView).addView(tv, p);
					}
				} else if (row == -1) {
					if (column != -1) {

						TextView tv = new TextView(FightbookActivity.this);

						tv.setText(courtName[column]);
						tv.setTextColor(FightbookActivity.this.getResources()
								.getColor(R.color.black));
						tv.setBackgroundResource(R.color.white);
						tv.setGravity(Gravity.CENTER);
						tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

						LinearLayout.LayoutParams p = new LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.MATCH_PARENT);
						((LinearLayout) convertView).addView(tv, p);
					}
				} else {
					TextView tv = new TextView(FightbookActivity.this);

					if (!isfightbooked[column][startTime + row * weight]) {

						tv.setText("￥"
								+ courtPrice[column][startTime + row * weight]);
						tv.setTextColor(FightbookActivity.this.getResources()
								.getColor(R.color.white));
						if (courtIsSelected[column][startTime + row * weight]) {
							tv.setBackgroundResource(R.color.yellow);
						} else {
							tv.setBackgroundResource(R.color.blue);
						}
						tv.setGravity(Gravity.CENTER);
						tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

						tv.setOnClickListener(new MyOnClickListener(column,
								startTime + row * weight));

					} else {
						tv.setBackgroundResource(R.color.grey);
					}

					LinearLayout.LayoutParams p = new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT);
					p.setMargins(3, 3, 3, 3);
					((LinearLayout) convertView).addView(tv, p);

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return convertView;
		}

		@Override
		public int getWidth(int column) {
			// TODO Auto-generated method stub
			if (column == -1) {
				return (int) (Data.SCREENWIDTH * 0.12);
			} else {
				return (int) (Data.SCREENWIDTH * 0.2);
			}
		}

		@Override
		public int getHeight(int row) {
			// TODO Auto-generated method stub
			if (row == -1) {
				return (int) (Data.SCREENHEIGHT * 0.05);
			} else {
				return (int) (Data.SCREENHEIGHT * 0.0625);
			}
		}

		@Override
		public int getItemViewType(int row, int column) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getViewTypeCount() {
			// TODO Auto-generated method stub
			return 1;
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

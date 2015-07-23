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
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.pazdarke.courtpocket1.view.BaseTableAdapter;
import com.pazdarke.courtpocket1.view.TableAdapter;
import com.pazdarke.courtpocket1.view.TableFixHeaders;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
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

public class BookActivity extends Activity {

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
	boolean[][] isBooked;
	LinearLayout[][] courtItem;
	double money;
	int startTime,endTime,weight;

	Button btn_submit;

	CourtHandler courtHandler;
	TimeoutHandler timeoutHandler;

	int loadTime = 0;// ���̼߳������ݣ������ж��Ƿ�������
	ExecutorService pool;
	private Lock lock = new ReentrantLock();// ������

	public static Activity instance_book;// ���ڹر�

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_book);

		AppManager.getAppManager().addActivity(this);

		pool = Executors.newFixedThreadPool(10);

		instance_book = this;
		
		Intent intent=getIntent();
		startTime=intent.getIntExtra("StartTime", 0);
		endTime=intent.getIntExtra("EndTime", 47);
		weight=intent.getIntExtra("Weight", 1);

		init_date();
		init_gif();

		tv_error = (TextView) findViewById(R.id.tv_book_error);
		tv_error.setOnClickListener(onClickListener);

		iv_leftarrow = (ImageView) findViewById(R.id.iv_book_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		TextView tv_mybill = (TextView) findViewById(R.id.tv_book_mybill);
		tv_mybill.setOnClickListener(onClickListener);

		ll_table = (TableFixHeaders) findViewById(R.id.ll_book_table);
		ll_table.getLayoutParams().height = (int) (Data.SCREENHEIGHT * 0.5);

		ll_instruction = (LinearLayout) findViewById(R.id.ll_book_instruction);
		ll_selection = (LinearLayout) findViewById(R.id.ll_book_selection);

		btn_submit = (Button) findViewById(R.id.btn_book_submit);
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
			case R.id.iv_book_leftarrow:
				finish();
				break;
			case R.id.tv_book_mybill:
				startActivity(new Intent(BookActivity.this,
						MybillActivity.class));
				break;
			case R.id.tv_book_error:
				rl_gifcontainer.setVisibility(View.VISIBLE);
				tv_error.setVisibility(View.GONE);
				new Thread(r_GetCourtInfo).start();
				break;
			case R.id.btn_book_submit:
				if (countIsSelected != 0) {
					Intent intent = new Intent(BookActivity.this,
							BookConfirmActivity.class);
					intent.putExtra("gymType", BookActivity.this.getIntent()
							.getIntExtra("gymType", 0));
					intent.putExtra("gymName", BookActivity.this.getIntent()
							.getStringExtra("gymName"));
					intent.putExtra("date", currentSelectDate);
					intent.putExtra("courtNum", countIsSelected);
					intent.putExtra("ID", getIntent().getStringExtra("ID"));
					intent.putExtra("weight", weight);

					// ��ѡ��˳��
					/*
					 * for(int i=0;i<countIsSelected;i++){ View
					 * view_temp=ll_selection.getChildAt(i); TextView
					 * tv_temp=(TextView
					 * )view_temp.findViewById(R.id.tv_bookitem_describe);
					 * String[]
					 * str_temp=tv_temp.getText().toString().split("-");
					 * intent.putExtra("time"+i, Integer.parseInt(str_temp[1]));
					 * intent.putExtra("ID"+i,
					 * courtID[Integer.parseInt(str_temp[0])]);
					 * intent.putExtra("name"+i,
					 * courtName[Integer.parseInt(str_temp[0])]);
					 * intent.putExtra("price" + i, courtPrice[Integer
					 * .parseInt(str_temp[0])][Integer .parseInt(str_temp[1])]);
					 * }
					 */

					// ��������ʱ����������Ч�����ã��ڶ���ҳ������
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

		// ��ȡδ�����������
		dateFormat = new SimpleDateFormat("MM��dd��");
		weekFormat = new SimpleDateFormat("EEE");
		fullDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date currentDate = new Date();
		days = dateToWeek(currentDate);

		ll_date1 = (LinearLayout) findViewById(R.id.ll_book_date1);
		// ll_date1.setOnClickListener(dateOnClickListener);
		ll_date2 = (LinearLayout) findViewById(R.id.ll_book_date2);
		// ll_date2.setOnClickListener(dateOnClickListener);
		ll_date3 = (LinearLayout) findViewById(R.id.ll_book_date3);
		// ll_date3.setOnClickListener(dateOnClickListener);
		ll_date4 = (LinearLayout) findViewById(R.id.ll_book_date4);
		// ll_date4.setOnClickListener(dateOnClickListener);
		ll_date5 = (LinearLayout) findViewById(R.id.ll_book_date5);
		// ll_date5.setOnClickListener(dateOnClickListener);
		ll_date6 = (LinearLayout) findViewById(R.id.ll_book_date6);
		// ll_date6.setOnClickListener(dateOnClickListener);
		ll_date7 = (LinearLayout) findViewById(R.id.ll_book_date7);
		// ll_date7.setOnClickListener(dateOnClickListener);

		tv_week1 = (TextView) findViewById(R.id.tv_book_week1);
		tv_week2 = (TextView) findViewById(R.id.tv_book_week2);
		tv_week2.setText(weekFormat.format(days.get(1)) + "");
		tv_week3 = (TextView) findViewById(R.id.tv_book_week3);
		tv_week3.setText(weekFormat.format(days.get(2)) + "");
		tv_week4 = (TextView) findViewById(R.id.tv_book_week4);
		tv_week4.setText(weekFormat.format(days.get(3)) + "");
		tv_week5 = (TextView) findViewById(R.id.tv_book_week5);
		tv_week5.setText(weekFormat.format(days.get(4)) + "");
		tv_week6 = (TextView) findViewById(R.id.tv_book_week6);
		tv_week6.setText(weekFormat.format(days.get(5)) + "");
		tv_week7 = (TextView) findViewById(R.id.tv_book_week7);
		tv_week7.setText(weekFormat.format(days.get(6)) + "");

		tv_date1 = (TextView) findViewById(R.id.tv_book_date1);
		tv_date1.setText(dateFormat.format(days.get(0)) + "");
		tv_date2 = (TextView) findViewById(R.id.tv_book_date2);
		tv_date2.setText(dateFormat.format(days.get(1)) + "");
		tv_date3 = (TextView) findViewById(R.id.tv_book_date3);
		tv_date3.setText(dateFormat.format(days.get(2)) + "");
		tv_date4 = (TextView) findViewById(R.id.tv_book_date4);
		tv_date4.setText(dateFormat.format(days.get(3)) + "");
		tv_date5 = (TextView) findViewById(R.id.tv_book_date5);
		tv_date5.setText(dateFormat.format(days.get(4)) + "");
		tv_date6 = (TextView) findViewById(R.id.tv_book_date6);
		tv_date6.setText(dateFormat.format(days.get(5)) + "");
		tv_date7 = (TextView) findViewById(R.id.tv_book_date7);
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
			case R.id.ll_book_date1:
				tv_week[0].setTextColor(getResources().getColor(R.color.white));
				tv_date[0].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(0));
				break;
			case R.id.ll_book_date2:
				tv_week[1].setTextColor(getResources().getColor(R.color.white));
				tv_date[1].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(1));
				break;
			case R.id.ll_book_date3:
				tv_week[2].setTextColor(getResources().getColor(R.color.white));
				tv_date[2].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(2));
				break;
			case R.id.ll_book_date4:
				tv_week[3].setTextColor(getResources().getColor(R.color.white));
				tv_date[3].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(3));
				break;
			case R.id.ll_book_date5:
				tv_week[4].setTextColor(getResources().getColor(R.color.white));
				tv_date[4].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(4));
				break;
			case R.id.ll_book_date6:
				tv_week[5].setTextColor(getResources().getColor(R.color.white));
				tv_date[5].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(5));
				break;
			case R.id.ll_book_date7:
				tv_week[6].setTextColor(getResources().getColor(R.color.white));
				tv_date[6].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(6));
				break;
			}
			rl_gifcontainer.setVisibility(View.VISIBLE);
			tv_error.setVisibility(View.GONE);
			btn_submit.setText("�ύ����");
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
	 * �������ڻ�������ܵ�����
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
		 * if (!isBooked[j][i + Data.COURTTIME]) { item.setText("��" +
		 * courtPrice[j][i + Data.COURTTIME]);
		 * item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		 * item.setGravity(Gravity.CENTER);
		 * item.setBackgroundDrawable(getResources().getDrawable(
		 * R.drawable.shape_bookitem)); item.setTextColor(Color.WHITE);
		 * item.setOnClickListener(new MyOnClickListener(j, i +
		 * Data.COURTTIME)); courtItem[j][i+Data.COURTTIME]=item; } else {
		 * item.setText(""); item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
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
			try {

				int gymType = BookActivity.this.getIntent().getIntExtra(
						"gymType", 1);

				if (courtIsSelected[i][j]) {
					v.setBackgroundResource(R.color.blue);
					courtIsSelected[i][j] = false;
					countIsSelected--;

					money = money - Double.parseDouble(courtPrice[i][j]);
					btn_submit.setText("��" + Data.doubleTrans(money) + " �ύ����");

					if (countIsSelected == 0) {
						ll_instruction.setVisibility(View.VISIBLE);
						ll_selection.setVisibility(View.GONE);
						btn_submit.setText("�ύ����");
					}
					// bookitem��ѡ
					for (int k = 0; k < ll_selection.getChildCount(); k++) {
						// System.out.println(k + "");
						View view = ll_selection.getChildAt(k);
						TextView tv_describe = (TextView) view
								.findViewById(R.id.tv_bookitem_describe);
						String[] temp = tv_describe.getText().toString()
								.split("-");
						if (temp[0].equals(i + "") && temp[1].equals(j + "")) {
							ll_selection.removeView(view);
							break;
						}
					}

					// �����ѡ
					/*int k;
					if (gymType == 1) {
						if (j % 2 == 0) {
							k = j - 1;
						} else {
							k = j + 1;
						}

						courtItem[i][k].setBackgroundResource(R.color.blue);
						courtIsSelected[i][k] = false;
						countIsSelected--;

						money = money - Integer.parseInt(courtPrice[i][k]);
						btn_submit.setText("��" + money + " �ύ����");

						if (countIsSelected == 0) {
							ll_instruction.setVisibility(View.VISIBLE);
							ll_selection.setVisibility(View.GONE);
							btn_submit.setText("�ύ����");
						}
						// bookitem��ѡ
						for (int k1 = 0; k1 < ll_selection.getChildCount(); k1++) {
							View view = ll_selection.getChildAt(k1);
							TextView tv_describe = (TextView) view
									.findViewById(R.id.tv_bookitem_describe);
							String[] temp = tv_describe.getText().toString()
									.split("-");
							if (temp[0].equals(i + "")
									&& temp[1].equals(k + "")) {
								ll_selection.removeView(view);
								break;
							}
						}

					}*/

				} else {
					if (countIsSelected < 4) {
						v.setBackgroundResource(R.color.yellow);
						courtIsSelected[i][j] = true;
						countIsSelected++;
						ll_instruction.setVisibility(View.GONE);
						ll_selection.setVisibility(View.VISIBLE);

						LayoutInflater mLi = LayoutInflater
								.from(BookActivity.this);
						View view = mLi.inflate(R.layout.layout_bookitem, null);
						TextView tv_time = (TextView) view
								.findViewById(R.id.tv_bookitem_time);
						TextView tv_courtname = (TextView) view
								.findViewById(R.id.tv_bookitem_courtname);
						TextView tv_describe = (TextView) view
								.findViewById(R.id.tv_bookitem_describe);

						tv_time.setText(minuteToClock(j*30)+"-"+minuteToClock((j+weight)*30));
						tv_courtname.setText(courtName[i]);
						// ���ڱ�ʶ�����㷴ѡ
						tv_describe.setText(i + "-" + j);

						LinearLayout.LayoutParams p = new LayoutParams(
								(int) (Data.SCREENWIDTH * 0.23),
								LayoutParams.MATCH_PARENT);
						p.setMargins((int) (Data.SCREENWIDTH * 0.016), 0, 0, 0);

						ll_selection.addView(view, p);

						money = money + Double.parseDouble(courtPrice[i][j]);
						btn_submit.setText("��" + Data.doubleTrans(money) + " �ύ����");

						// �����������һ������ѡ����
						/*int k;
						if (gymType == 1) {
							if (j % 2 == 0) {
								k = j - 1;
							} else {
								k = j + 1;
							}

							courtItem[i][k]
									.setBackgroundResource(R.color.yellow);
							courtIsSelected[i][k] = true;
							countIsSelected++;
							ll_instruction.setVisibility(View.GONE);
							ll_selection.setVisibility(View.VISIBLE);

							mLi = LayoutInflater.from(BookActivity.this);
							view = mLi.inflate(R.layout.layout_bookitem, null);
							tv_time = (TextView) view
									.findViewById(R.id.tv_bookitem_time);
							tv_courtname = (TextView) view
									.findViewById(R.id.tv_bookitem_courtname);
							tv_describe = (TextView) view
									.findViewById(R.id.tv_bookitem_describe);

							tv_time.setText(k + ":00-" + (k + 1) + ":00");
							tv_courtname.setText(courtName[i]);
							// ���ڱ�ʶ�����㷴ѡ
							tv_describe.setText(i + "-" + k);

							ll_selection.addView(view, p);

							money = money + Integer.parseInt(courtPrice[i][j]);
							btn_submit.setText("��" + money + " �ύ����");

						}*/

					} else
						Toast.makeText(BookActivity.this, "һ���������ֻ�ܰ����ĸ����~",
								Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	void init_gif() {
		GifView gif_loading = new GifView(this);
		gif_loading.setGifImage(R.drawable.gif_loading);
		gif_loading.setShowDimension((int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		gif_loading.setGifImageType(GifImageType.SYNC_DECODER);

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_book_gifcontainer);
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
				// ��ѯ���ص����������ơ�ID������ʼ������״̬����
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("Request",
						"RequestListByGymID"));
				params.add(new BasicNameValuePair("GymID", BookActivity.this
						.getIntent().getStringExtra("ID")));

				String result = new HttpPostConnection("GymInfoServer", params)
						.httpConnection();

				if (result.equals("timeout")) {
					Message msg = new Message();
					Bundle b = new Bundle();
					msg.setData(b);
					timeoutHandler.handleMessage(msg);
					return;
				}
				
				System.out.println(result);

				JSONObject json_courtinfo = new JSONObject(result);
				courtNum = (json_courtinfo.length()-3) / 2;
				courtIsSelected = new boolean[courtNum][48];
				courtID = new String[courtNum];
				courtName = new String[courtNum];
				courtPrice = new String[courtNum][48];
				isBooked = new boolean[courtNum][48];
				courtItem = new LinearLayout[courtNum][48];

				for (int i = 0; i < courtNum; i++) {
					for (int j = 0; j < 48; j++) {
						isBooked[i][j] = false;
						courtIsSelected[i][j] = false;
					}
				}

				for (int i = 0; i < courtNum; i++) {
					courtID[i] = json_courtinfo.getInt("Court" + i) + "";
					courtName[i] = json_courtinfo.getString("Name" + i) + "";
				}

				// �����̻߳�ȡ����ռ��������۸�
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
			// ��ѯ���ص�ռ�����
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

			// ʱ����˳����Զ���Ϊ����Ԥ��
			Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			String day = c.get(Calendar.DATE) + "";
			if (Integer.parseInt(day) < 10)
				day = "0" + day;
			String[] str = currentSelectDate.split("-");
			if (str[2].equals(day)) {

				for (int j = 0; j < 48; j++) {
					if ((hour*2 + 1) > j)
						isBooked[i][j] = true;
				}
			}

			// ���ݷ��������ص������趨�Ƿ��Ԥ��
			try {
				JSONObject json = new JSONObject(result);

				for (int j = 0; j < 48; j++) {

					if (json.has("Time" + j)) {
						isBooked[i][j] = true;
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
			// ��ѯcourt�ļ۸�
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

			Toast.makeText(BookActivity.this, "�����������(+�n+)~",
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
			return (endTime-startTime)/weight;
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
				convertView = new LinearLayout(BookActivity.this);
				((LinearLayout) convertView)
						.setOrientation(LinearLayout.VERTICAL);
				// }

				if (column == -1) {
					if (row != -1) {
						TextView tv = new TextView(BookActivity.this);
						tv.setText(minuteToClock((startTime + row * weight) * 30)
								+ "��");
						tv.setTextColor(BookActivity.this.getResources()
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

						TextView tv = new TextView(BookActivity.this);

						tv.setText(courtName[column]);
						tv.setTextColor(BookActivity.this.getResources()
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
					TextView tv = new TextView(BookActivity.this);

					if (!isBooked[column][startTime+row*weight]) {

						tv.setText("��" + courtPrice[column][startTime+row*weight]);
						tv.setTextColor(BookActivity.this.getResources()
								.getColor(R.color.white));
						if (courtIsSelected[column][startTime+row*weight]) {
							tv.setBackgroundResource(R.color.yellow);
						} else {
							tv.setBackgroundResource(R.color.blue);
						}
						tv.setGravity(Gravity.CENTER);
						tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

						tv.setOnClickListener(new MyOnClickListener(column,
								startTime+row*weight));

					} else {
						tv.setBackgroundResource(R.color.grey);
					}

					LinearLayout.LayoutParams p = new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT);
					p.setMargins((int) (Data.SCREENHEIGHT * 0.003),
							(int) (Data.SCREENHEIGHT * 0.006),
							(int) (Data.SCREENHEIGHT * 0.003), 0);
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
	
	public static String minuteToClock(int sumMinute){
		int hour=sumMinute/60;
		int minute=sumMinute-hour*60;
		if(minute==0){
			return hour+":00";
		} else{
			return hour+":30";
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

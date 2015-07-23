package com.pazdarke.courtpocket1.activity;

import java.util.ArrayList;
import java.util.List;

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
import com.pazdarke.courtpocket1.tools.listview.AsyncViewTask;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CourtCoachActivity extends Activity {
	
	RelativeLayout rl_gifcontainer;
	TextView tv_error;

	LinearLayout ll_coachcontainer;

	int sumPage = 0, currentPage = 0, lastPageItemNum;
	boolean hasMore = true;
	LinearLayout ll_progressbar;
	JSONObject coachID;
	JSONObject coachInfo;
	
	int coachLoadNum;// 请求item为异步，判断是否加载完毕

	TimeoutHandler timeoutHandler;
	CloseGifHandler closeGifHandler;
	CloseProgressHandler closeProgressHandler;
	CoachHandler coachHandler;
	
	int index=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_court_coach);
		
		AppManager.getAppManager().addActivity(this);
		
		init_gif();

		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_courtcoach_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		TextView tv_gymname = (TextView) findViewById(R.id.tv_courtcoach_gymname);
		tv_gymname.setText(this.getIntent().getExtras().getString("GymName"));

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_courtcoach_gifcontainer);

		tv_error = (TextView) findViewById(R.id.tv_courtcoach_error);
		tv_error.setOnClickListener(onClickListener);

		ll_coachcontainer = (LinearLayout) findViewById(R.id.ll_courtcoach_container);
		
		ll_progressbar=(LinearLayout)findViewById(R.id.ll_courtcoach_progressbar);
		
		// 滑动监听，滑动到底部自动加载更多
		ScrollView sv_container=(ScrollView)findViewById(R.id.sv_courtcoach_container);
		sv_container.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_MOVE:
					index++;
					break;
				}
				if (event.getAction() == MotionEvent.ACTION_UP && index > 0) {
					index = 0;
					View view = ((ScrollView) v).getChildAt(0);
					if (view.getMeasuredHeight() <= v.getScrollY()
							+ v.getHeight()) {
						// 加载数据代码
						if (hasMore)
							new Thread(r_EachPageCoach).start();
						else
							ll_progressbar.setVisibility(View.GONE);
					}
				}

				return false;
			}
		});

		timeoutHandler = new TimeoutHandler();
		closeGifHandler=new CloseGifHandler();
		closeProgressHandler=new CloseProgressHandler();
		coachHandler = new CoachHandler();

		new Thread(r_CoachList).start();
		
	}
	
	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_courtcoach_leftarrow:
				finish();
				break;
			case R.id.tv_courtcoach_error:
				tv_error.setVisibility(View.GONE);
				rl_gifcontainer.setVisibility(View.VISIBLE);
				currentPage = 0;
				sumPage = 0;
				hasMore = true;
				ll_progressbar.setVisibility(View.VISIBLE);
				new Thread(r_CoachList).start();
				break;
			}
		}
	};

	void init_gif() {
		GifView gif_loading = new GifView(this);
		gif_loading.setGifImage(R.drawable.gif_loading);
		gif_loading.setShowDimension((int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		gif_loading.setGifImageType(GifImageType.SYNC_DECODER);

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_courtcoach_gifcontainer);
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
				(int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		p.addRule(RelativeLayout.CENTER_HORIZONTAL);
		p.setMargins(0, (int) (Data.SCREENHEIGHT * 0.22), 0, 0);
		rl_gifcontainer.addView(gif_loading, p);

	}

	Runnable r_CoachList = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "CoachList"));
			params.add(new BasicNameValuePair("GymID",
					CourtCoachActivity.this.getIntent().getStringExtra("ID")));

			String result = new HttpPostConnection("CoachServer", params)
					.httpConnection();

			if (result.equals("timeout")) {
				Message msg = new Message();
				Bundle b = new Bundle();
				msg.setData(b);
				timeoutHandler.sendMessage(msg);
			} else {

				try {
					coachID = new JSONObject(result);

					sumPage = (int) (coachID.getInt("CoachNum") / 10);
					lastPageItemNum = coachID.getInt("CoachNum")
							- sumPage * 10;
					if(coachID.getInt("CoachNum")==0){
						Message msg=new Message();
						Bundle b=new Bundle();
						msg.setData(b);
						closeGifHandler.sendMessage(msg);
						
						msg=new Message();
						b=new Bundle();
						msg.setData(b);
						closeProgressHandler.sendMessage(msg);
						return;
					}
					new Thread(r_EachPageCoach).start();

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			

		}
	};
	
	Runnable r_EachPageCoach = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			coachInfo=new JSONObject();
			coachLoadNum=0;

			int j;
			if (currentPage < sumPage)
				j = 10;
			else {
				j = lastPageItemNum;
				hasMore = false;
			}

			for (int i = 0; i < j; i++) {
				int k = currentPage * 10 + i;
				RequestEachCoachThread thread = new RequestEachCoachThread(
						coachID, k, j, i);
				thread.start();
			}

			currentPage++;
		}
	};
	
	class RequestEachCoachThread extends Thread {
		JSONObject coachID;
		int i;
		int num;
		int order;

		RequestEachCoachThread(JSONObject coachID, int i, int num,int order) {
			this.coachID = coachID;
			this.i = i;
			this.num = num;
			this.order=order;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				Message msg = new Message();
				Bundle b = new Bundle();

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("Request",
						"CoachDetail"));
				params.add(new BasicNameValuePair("CoachID",
						coachID.getInt("CoachID" + i) + ""));

				String result = new HttpPostConnection("CoachServer",
						params).httpConnection();

				if (result.equals("timeout")) {
					msg.setData(b);
					timeoutHandler.sendMessage(msg);
					return;
				}

				JSONObject json = new JSONObject(result);
				coachInfo.put("info"+order, json);
				b.putInt("num", num);
				msg.setData(b);
				coachHandler.sendMessage(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@SuppressLint("HandlerLeak")
	class CoachHandler extends Handler {
		@SuppressLint("InflateParams")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();
			
			try {
				TextView tv_coachnum = (TextView) findViewById(R.id.tv_courtcoach_coachnum);
				tv_coachnum.setText(coachID.getInt("CoachNum") + "");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			coachLoadNum++;

			if (b.getInt("num") < 10) {
				ll_progressbar.setVisibility(View.GONE);
			}

			if (coachLoadNum >= b.getInt("num")) {
				for (int i = 0; i < b.getInt("num"); i++) {

					LayoutInflater mLi = LayoutInflater
							.from(CourtCoachActivity.this);
					View view = mLi.inflate(R.layout.layout_courtinfo_coach,
							null);

					try {
						JSONObject json = coachInfo.getJSONObject("info"+i);

						TextView name = (TextView) view
								.findViewById(R.id.tv_coach_name);
						name.setText(json.getString("CoachName"));

						TextView title = (TextView) view
								.findViewById(R.id.tv_coach_title);
						title.setText(json.getString("Introduction"));

						TextView intro = (TextView) view
								.findViewById(R.id.tv_coach_intro);
						intro.setText(json.getString("Detail"));

						ImageView head = (ImageView) view
								.findViewById(R.id.iv_coach_head);
						if (json.has("Path")) {
							String path = json.getString("Path");
							new AsyncViewTask(CourtCoachActivity.this, path,
									head,3).execute(head);// 异步加载图片
						} else {
							head.setImageResource(R.drawable.ic_mainpage_tab3_defaultphoto);
						}

						// 图片显示，暂时不加入这个功能，接口预留
						/*
						 * LinearLayout ll_photo = (LinearLayout) view
						 * .findViewById(R.id.ll_comment_photo);
						 * LinearLayout.LayoutParams p = new
						 * LinearLayout.LayoutParams( 200, 200);
						 * p.setMargins((int) (Data.SCREENWIDTH * 0.04), 0, 0,
						 * 0);
						 * 
						 * for (int j = 0; j < json.getInt("CoachPicNum"); j++)
						 * { ImageView iv = new
						 * ImageView(CourtCoachActivity.this);
						 * iv.setScaleType(ScaleType.FIT_XY);
						 * iv.setLayoutParams(p); iv.setOnClickListener(new
						 * PicOnClickListener(json .getInt("CoachPicNum"), json
						 * .getInt("CoachPicNum"), j, json,
						 * CourtCoachActivity.this, PhotoActivity.class));
						 * ll_photo.addView(iv); new
						 * AsyncViewTask(CourtCoachActivity.this,
						 * json.getString("CoachPic" + j), iv) .execute(iv);//
						 * 异步加载图片 }
						 */

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT);
					ll_coachcontainer.addView(view, p);
				}

				rl_gifcontainer.setVisibility(View.GONE);
			}
		}
	}

	@SuppressLint("HandlerLeak")
	class CloseGifHandler extends Handler {
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			rl_gifcontainer.setVisibility(View.GONE);
		}
	}
	
	@SuppressLint("HandlerLeak")
	class CloseProgressHandler extends Handler {
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			ll_progressbar.setVisibility(View.GONE);
		}
	}
	
	@SuppressLint("HandlerLeak")
	class TimeoutHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			/*
			 * Toast.makeText(CourtinfoActivity.this, "网络出问题咩(+﹏+)~",
			 * Toast.LENGTH_SHORT).show();
			 */
			rl_gifcontainer.setVisibility(View.GONE);
			tv_error.setVisibility(View.VISIBLE);
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

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
import com.pazdarke.courtpocket1.view.PicOnClickListener;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class CourtCommentActivity extends Activity {

	RelativeLayout rl_gifcontainer;
	TextView tv_error;

	LinearLayout ll_commentcontainer;

	int sumPage = 0, currentPage = 0, lastPageItemNum;
	boolean hasMore = true;
	LinearLayout ll_progressbar;
	JSONObject commentID;
	JSONObject commentInfo;
	int eachPageNum = 8;

	int commentLoadNum;// 请求item为异步，判断是否加载完毕

	TimeoutHandler timeoutHandler;
	CloseGifHandler closeGifHandler;
	CloseProgressHandler closeProgressHandler;
	CommentHandler commentHandler;

	int index = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_court_comment);

		AppManager.getAppManager().addActivity(this);

		init_gif();

		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_courtcomment_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		TextView tv_gymname = (TextView) findViewById(R.id.tv_courtcomment_gymname);
		tv_gymname.setText(this.getIntent().getExtras().getString("GymName"));

		TextView tv_mark = (TextView) findViewById(R.id.tv_courtcomment_mark);
		tv_mark.setText(this.getIntent().getExtras().getString("Mark"));

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_courtcomment_gifcontainer);

		tv_error = (TextView) findViewById(R.id.tv_courtcomment_error);
		tv_error.setOnClickListener(onClickListener);

		ll_commentcontainer = (LinearLayout) findViewById(R.id.ll_courtcomment_container);

		ll_progressbar = (LinearLayout) findViewById(R.id.ll_courtcomment_progressbar);

		// 滑动监听，滑动到底部自动加载更多
		ScrollView sv_container = (ScrollView) findViewById(R.id.sv_courtcomment_container);
		sv_container.setOnTouchListener(new OnTouchListener() {

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
							new Thread(r_EachPageComment).start();
						else
							ll_progressbar.setVisibility(View.GONE);
					}
				}

				return false;
			}
		});

		timeoutHandler = new TimeoutHandler();
		closeGifHandler = new CloseGifHandler();
		closeProgressHandler = new CloseProgressHandler();
		commentHandler = new CommentHandler();

		new Thread(r_CommentList).start();

	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_courtcomment_leftarrow:
				finish();
				break;
			case R.id.tv_courtcomment_error:
				tv_error.setVisibility(View.GONE);
				rl_gifcontainer.setVisibility(View.VISIBLE);
				currentPage = 0;
				sumPage = 0;
				hasMore = true;
				ll_progressbar.setVisibility(View.VISIBLE);
				new Thread(r_CommentList).start();
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

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_courtcomment_gifcontainer);
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
				(int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		p.addRule(RelativeLayout.CENTER_HORIZONTAL);
		p.setMargins(0, (int) (Data.SCREENHEIGHT * 0.22), 0, 0);
		rl_gifcontainer.addView(gif_loading, p);

	}

	Runnable r_CommentList = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "RequestCommentList"));
			params.add(new BasicNameValuePair("GymID",
					CourtCommentActivity.this.getIntent().getStringExtra("ID")));

			String result = new HttpPostConnection("CommentServer", params)
					.httpConnection();

			if (result.equals("timeout")) {
				Message msg = new Message();
				Bundle b = new Bundle();
				msg.setData(b);
				timeoutHandler.sendMessage(msg);
			} else {

				try {
					commentID = new JSONObject(result);

					sumPage = (int) (commentID.getInt("CommentNum") / eachPageNum);
					lastPageItemNum = commentID.getInt("CommentNum") - sumPage
							* eachPageNum;
					if (commentID.getInt("CommentNum") == 0) {
						Message msg = new Message();
						Bundle b = new Bundle();
						msg.setData(b);
						closeGifHandler.sendMessage(msg);

						msg = new Message();
						b = new Bundle();
						msg.setData(b);
						closeProgressHandler.sendMessage(msg);
						return;
					}
					new Thread(r_EachPageComment).start();

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	};

	Runnable r_EachPageComment = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			commentInfo = new JSONObject();
			commentLoadNum = 0;

			int j;
			if (currentPage < sumPage)
				j = eachPageNum;
			else {
				j = lastPageItemNum;
				hasMore = false;
			}

			for (int i = 0; i < j; i++) {
				int k = currentPage * eachPageNum + i;
				RequestEachCommentThread thread = new RequestEachCommentThread(
						commentID, k, j, i);
				thread.start();
			}

			currentPage++;
		}
	};

	class RequestEachCommentThread extends Thread {
		JSONObject commentID;
		int i;
		int num;
		int order;

		RequestEachCommentThread(JSONObject commentID, int i, int num, int order) {
			this.commentID = commentID;
			this.i = i;
			this.num = num;
			this.order = order;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				Message msg = new Message();
				Bundle b = new Bundle();

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("Request",
						"RequestCommentDetail"));
				params.add(new BasicNameValuePair("CommentID", commentID
						.getInt("CommentID" + i) + ""));

				String result = new HttpPostConnection("CommentServer", params)
						.httpConnection();

				if (result.equals("timeout")) {
					msg.setData(b);
					timeoutHandler.sendMessage(msg);
					return;
				}

				JSONObject json = new JSONObject(result);
				commentInfo.put("info" + order, json);
				b.putInt("num", num);
				msg.setData(b);
				commentHandler.sendMessage(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class CommentHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();

			if (b.getInt("num") < eachPageNum) {
				ll_progressbar.setVisibility(View.GONE);
			}

			commentLoadNum++;

			if (commentLoadNum >= b.getInt("num")) {

				for (int i = 0; i < b.getInt("num"); i++) {

					LayoutInflater mLi = LayoutInflater
							.from(CourtCommentActivity.this);
					View view = mLi.inflate(R.layout.layout_courtinfo_comment,
							null);

					try {
						JSONObject json = commentInfo.getJSONObject("info" + i);

						TextView name = (TextView) view
								.findViewById(R.id.tv_comment_name);
						name.setText(json.getString("Nickname"));
						TextView content = (TextView) view
								.findViewById(R.id.tv_comment_comment);
						content.setText(json.getString("Content"));
						TextView date = (TextView) view
								.findViewById(R.id.tv_comment_time);
						date.setText(json.getString("Date"));
						RatingBar star = (RatingBar) view
								.findViewById(R.id.rb_comment_mark);
						star.setRating(json.getInt("Rate"));

						ImageView head = (ImageView) view
								.findViewById(R.id.iv_comment_head);
						if (json.has("Path")) {
							String path = json.getString("Path");
							new AsyncViewTask(CourtCommentActivity.this, path,
									head, 3).execute(head);// 异步加载图片
						} else {
							head.setImageResource(R.drawable.ic_mainpage_tab3_defaultphoto);
						}

						LinearLayout ll_photo = (LinearLayout) view
								.findViewById(R.id.ll_comment_photo);
						LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
								200, 200);
						p.setMargins((int) (Data.SCREENWIDTH * 0.04), 0, 0, 0);

						for (int j = 0; j < json.getInt("CommentPicNum"); j++) {
							ImageView iv = new ImageView(
									CourtCommentActivity.this);
							iv.setScaleType(ScaleType.FIT_CENTER);
							iv.setLayoutParams(p);
							iv.setImageResource(R.color.lightGrey);
							iv.setOnClickListener(new PicOnClickListener(json
									.getInt("CommentPicNum"), json
									.getInt("CommentPicNum"), j, json,
									CourtCommentActivity.this,
									PhotoActivity.class));
							ll_photo.addView(iv);
							new AsyncViewTask(CourtCommentActivity.this,
									json.getString("CommentPic" + j), iv, 8)
									.execute(iv);// 异步加载图片
						}

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT);
					ll_commentcontainer.addView(view, p);
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

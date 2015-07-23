package com.pazdarke.courtpocket1.activity;

import java.math.BigDecimal;
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
import com.pazdarke.courtpocket1.httpConnection.HttpGetPicConnection;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.pazdarke.courtpocket1.tools.listview.AsyncGyminfoViewTask;
import com.pazdarke.courtpocket1.tools.listview.AsyncViewTask;
import com.pazdarke.courtpocket1.view.PicOnClickListener;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

public class CourtinfoActivity extends Activity {

	ProgressDialog progressDialog;

	JSONObject json_courtInfo;

	RelativeLayout rl_gifcontainer;

	ImageView iv_leftarrow;
	ImageView iv_collect;
	boolean isCollect;
	ImageView iv_share;
	RelativeLayout rl_collect;
	RelativeLayout rl_share;

	Button btn_match, btn_book;

	private ViewPager adViewPager;
	private LinearLayout pagerLayout;
	private List<View> pageViews;
	private ImageView[] imageViews;
	private AdPageAdapter adapter;
	// Bitmap[] bm_gympic;
	public static ImageView[] iv_headpic;
	public static ImageView[] iv_photo;

	LinearLayout ll_commentcontainer;
	LinearLayout ll_coachcontainer;
	TextView tv_error;

	Intent intent;

	TimeoutHandler timeoutHandler;
	CourtinfoHandler courtinfoHandler;
	CoachHandler coachHandler;
	CommentHandler commentHandler;
	CollectHandler collectHandler;
	CloseProgressbarHandler closePbHandler;
	TeamlistHandler teamlistHandler;

	int loadNum;// 此页面一共需要三次加载数据，loadNum==3代表加载完毕，gif可以滚了

	String str_gymName, str_mark;// 用于跳转到全部评论�?�教练的信息传�??

	int gymType;
	String gymID;

	// List<AsyncViewTask> task=new ArrayList<AsyncViewTask>();
	public static AsyncGyminfoViewTask[] task;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_courtinfo);

		IntentFilter filter = new IntentFilter("RefreshGyminfoPhoto");
		registerReceiver(broadcastReceiver, filter);

		// bitmap内存优化
		if (task != null) {
			for (int i = 0; i < task.length; i++) {
				if (task[i].bm != null && !task[i].bm.isRecycled()) {
					task[i].bm.recycle();
					task[i].bm = null;
				}
			}
			System.gc();
			task = null;
		}

		AppManager.getAppManager().addActivity(this);

		gymID = getIntent().getStringExtra("ID");

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("正在检查登录状态");
		progressDialog.setCancelable(true);

		init_gif();

		iv_leftarrow = (ImageView) findViewById(R.id.iv_courtinfo_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		iv_collect = (ImageView) findViewById(R.id.iv_courtinfo_collect);
		rl_collect = (RelativeLayout) findViewById(R.id.rl_courtinfo_collect);
		rl_collect.setOnClickListener(onClickListener);

		iv_share = (ImageView) findViewById(R.id.iv_courtinfo_share);
		rl_share = (RelativeLayout) findViewById(R.id.rl_courtinfo_share);
		rl_share.setOnClickListener(onClickListener);

		btn_match = (Button) findViewById(R.id.btn_courtinfo_match);
		btn_match.setOnClickListener(onClickListener);

		btn_book = (Button) findViewById(R.id.btn_courtinfo_book);
		btn_book.setOnClickListener(onClickListener);

		RelativeLayout rl_location = (RelativeLayout) findViewById(R.id.rl_courtinfo_location);
		rl_location.setOnClickListener(onClickListener);

		ImageView iv_phone = (ImageView) findViewById(R.id.iv_courtinfo_phone);
		iv_phone.setOnClickListener(onClickListener);

		intent = new Intent(CourtinfoActivity.this, PhotoActivity.class);

		// 初始化图片展示栏
		pagerLayout = (LinearLayout) findViewById(R.id.ll_courtinfo_viwepager);
		adViewPager = new ViewPager(this);
		adViewPager.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		pagerLayout.addView(adViewPager);

		ll_commentcontainer = (LinearLayout) findViewById(R.id.ll_courtinfo_commentcontainer);
		LinearLayout ll_morecomments = (LinearLayout) findViewById(R.id.ll_courtinfo_morecomments);
		ll_morecomments.setOnClickListener(onClickListener);

		ll_coachcontainer = (LinearLayout) findViewById(R.id.ll_courtinfo_coachcontainer);
		LinearLayout ll_morecoach = (LinearLayout) findViewById(R.id.ll_courtinfo_morecoach);
		ll_morecoach.setOnClickListener(onClickListener);

		tv_error = (TextView) findViewById(R.id.tv_courtinfo_error);
		tv_error.setOnClickListener(onClickListener);

		loadNum = 0;

		timeoutHandler = new TimeoutHandler();
		courtinfoHandler = new CourtinfoHandler();
		coachHandler = new CoachHandler();
		commentHandler = new CommentHandler();
		collectHandler = new CollectHandler();
		closePbHandler = new CloseProgressbarHandler();
		teamlistHandler = new TeamlistHandler();

		new Thread(r_Courtinfo).start();
		new Thread(r_CoachList).start();
		new Thread(r_CommentList).start();

	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_courtinfo_leftarrow:
				finish();
				break;
			case R.id.rl_courtinfo_collect:
				if (Data.ISLOGIN) {
					new Thread(r_Collect).start();
				} else {
					Intent intent = new Intent(CourtinfoActivity.this,
							LoginActivity.class);
					startActivityForResult(intent, 102);
				}
				break;
			case R.id.rl_courtinfo_share:
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				try {
					sendIntent.putExtra(
							Intent.EXTRA_TEXT,
							json_courtInfo.getString("GymName")
									+ "还不错哦（来自场兜 下载地址 "
									+ getResources().getString(
											R.string.downloadUrl));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sendIntent.setType("text/plain");
				startActivity(Intent.createChooser(sendIntent, "推荐运动场给你的朋友吧"));
				break;
			case R.id.tv_courtinfo_error:
				loadNum = 0;
				tv_error.setVisibility(View.GONE);
				rl_gifcontainer.setVisibility(View.VISIBLE);
				new Thread(r_Courtinfo).start();
				new Thread(r_CoachList).start();
				new Thread(r_CommentList).start();
				break;
			case R.id.btn_courtinfo_match:
				System.out.println("onClickListener");
				if (Data.ISLOGIN) {
					progressDialog.show();
					if (gymType == 4 || gymType == 5 || gymType == 6
							|| gymType == 7) {
						new CheckPasscodeThread(0).start();
					} else if (gymType == 1 || gymType == 2 || gymType == 3) {
						new CheckPasscodeThread(2).start();
					}
				} else {
					Intent intent1 = new Intent(CourtinfoActivity.this,
							LoginActivity.class);
					startActivityForResult(intent1, 102);
				}
				break;
			case R.id.btn_courtinfo_book:
				if (Data.ISLOGIN) {
					progressDialog.show();
					if (gymType == 8) {
						// 游泳�?
						new CheckPasscodeThread(3).start();
					} else {
						new CheckPasscodeThread(1).start();
					}
				} else {
					Intent intent1 = new Intent(CourtinfoActivity.this,
							LoginActivity.class);
					startActivityForResult(intent1, 102);
				}
				break;
			case R.id.rl_courtinfo_location:
				if (Data.ISTRUELOCATION) {
					String[] str = new String[] { "公交", "驾车", "步行" };
					Dialog dialog = new AlertDialog.Builder(
							CourtinfoActivity.this).setItems(str,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									selectNavi(which);
								}
							}).create();
					dialog.show();
				} else {
					Toast.makeText(CourtinfoActivity.this,
							"定位失败，无法进入路线规划界面呐~>_<~+", Toast.LENGTH_SHORT)
							.show();
				}
				break;
			case R.id.iv_courtinfo_phone:
				try {
					if (json_courtInfo.has("Phone")) {

						Uri uri = Uri.parse("tel:"
								+ json_courtInfo.getString("Phone"));
						Intent intent1 = new Intent(Intent.ACTION_DIAL, uri);
						startActivity(intent1);

					} else {
						Toast.makeText(CourtinfoActivity.this, "该场馆没有绑定联系方�?",
								Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case R.id.ll_courtinfo_morecoach:
				Intent intent2 = new Intent(CourtinfoActivity.this,
						CourtCoachActivity.class);
				intent2.putExtra("ID", CourtinfoActivity.this.getIntent()
						.getStringExtra("ID"));
				intent2.putExtra("GymName", str_gymName);
				startActivity(intent2);
				break;
			case R.id.ll_courtinfo_morecomments:
				Intent intent3 = new Intent(CourtinfoActivity.this,
						CourtCommentActivity.class);
				intent3.putExtra("ID", CourtinfoActivity.this.getIntent()
						.getStringExtra("ID"));
				intent3.putExtra("GymName", str_gymName);
				intent3.putExtra("Mark", str_mark);
				startActivity(intent3);
				break;
			}
		}
	};

	OnClickListener picClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			startActivity(intent);
		}
	};

	void init_gif() {
		GifView gif_loading = new GifView(this);
		gif_loading.setGifImage(R.drawable.gif_loading);
		gif_loading.setShowDimension((int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		gif_loading.setGifImageType(GifImageType.SYNC_DECODER);

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_courtinfo_gifcontainer);
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
				(int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		p.addRule(RelativeLayout.CENTER_HORIZONTAL);
		p.setMargins(0, (int) (Data.SCREENHEIGHT * 0.22), 0, 0);
		rl_gifcontainer.addView(gif_loading, p);

	}

	private void initPageAdapter(int picNum) {
		try {

			RelativeLayout rl_picture = (RelativeLayout) findViewById(R.id.rl_courtinfo_picture);
			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
					Data.SCREENWIDTH, (int) (Data.SCREENWIDTH / 3 * 2));
			rl_picture.setLayoutParams(p);

			pageViews = new ArrayList<View>();

			if (picNum > 0) {
				iv_headpic = new ImageView[picNum];
				iv_photo = new ImageView[picNum];
			}

			for (int i = 0; i < picNum; i++) {
				iv_headpic[i] = new ImageView(this);
				iv_headpic[i].setScaleType(ScaleType.FIT_XY);
				iv_headpic[i].setImageResource(R.color.lightGrey);
				// iv_headpic[i].setOnClickListener(picClickListener);
				pageViews.add(iv_headpic[i]);

				iv_photo[i] = new ImageView(this);
				iv_photo[i].setScaleType(ScaleType.CENTER_INSIDE);
				iv_photo[i].setImageResource(R.color.lightGrey);

				/*
				 * System.out.println(i);
				 * 
				 * Bundle bundle = new Bundle();
				 * bundle.putSerializable("Path"+i, temp);
				 * intent.putExtras(bundle);
				 */

			}

			if (picNum >= 2) {
				task[0] = new AsyncGyminfoViewTask(CourtinfoActivity.this,
						json_courtInfo.getString("HeadPic" + 0), iv_headpic[0],
						iv_photo[0]);
				task[0].execute(iv_headpic[0]);

				task[1] = new AsyncGyminfoViewTask(CourtinfoActivity.this,
						json_courtInfo.getString("HeadPic" + 1), iv_headpic[1],
						iv_photo[1]);
				task[1].execute(iv_headpic[1]);
			} else if (picNum == 1) {
				task[0] = new AsyncGyminfoViewTask(CourtinfoActivity.this,
						json_courtInfo.getString("HeadPic" + 0), iv_headpic[0],
						iv_photo[0]);
				task[0].execute(iv_headpic[0]);
			}

			intent.putExtra("picNum", pageViews.size());
			intent.putExtra("info", json_courtInfo.toString());

			/*
			 * Bundle bundle = new Bundle(); bundle.putSerializable("photo",
			 * (Serializable)task); intent.putExtras(bundle);
			 */

			if (loadNum < 3)
				initCirclePoint(picNum);

			adapter = new AdPageAdapter(pageViews);
			adViewPager.setAdapter(adapter);
			adViewPager.setOnPageChangeListener(new AdPageChangeListener());

			LinearLayout ll_promotioninfo = (LinearLayout) findViewById(R.id.ll_courtinfo_promotioninfo);
			RelativeLayout.LayoutParams p1 = new RelativeLayout.LayoutParams(
					Data.SCREENWIDTH, (int) (Data.SCREENWIDTH / 3 * 2 / 4));
			p1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			ll_promotioninfo.setLayoutParams(p1);
			ll_promotioninfo.bringToFront();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initCirclePoint(int picNum) {
		ViewGroup group = (ViewGroup) findViewById(R.id.ll_courtinfo_viewGroup);
		imageViews = new ImageView[pageViews.size()];
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20);
		params.setMargins(10, 0, 10, 0);
		for (int i = 0; i < pageViews.size(); i++) {
			ImageView imageView = new ImageView(this);
			imageView.setLayoutParams(params);
			imageViews[i] = imageView;

			if (i == 0) {
				imageViews[i].setBackgroundResource(R.drawable.point_focused);
			} else {
				imageViews[i].setBackgroundResource(R.drawable.point_unfocused);
			}
			group.addView(imageViews[i]);
		}

		if (picNum == 1)
			group.setVisibility(View.GONE);

	}

	private final class AdPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int arg0) {
			// atomicInteger.getAndSet(arg0);

			intent.putExtra("currentPicNum", arg0);

			for (int i = 0; i < imageViews.length; i++) {
				imageViews[arg0]
						.setBackgroundResource(R.drawable.point_focused);
				if (arg0 != i) {
					imageViews[i]
							.setBackgroundResource(R.drawable.point_unfocused);
				}
			}

			try {
				// 动�?�回收bitmap内存，减少OOM的概�?
				if (arg0 == 0) {
					if (task.length >= 3) {
						if (task[2] != null && task[2].bm != null
								&& !task[2].bm.isRecycled()) {
							task[2].bm.recycle();
							task[2].bm = null;
							HttpGetPicConnection.mImageCache
									.remove(json_courtInfo
											.getString("HeadPic" + 2));
							System.gc();
						}
					}
				} else if (arg0 == 1) {
					if (task.length >= 4) {
						if (task[3] != null && task[3].bm != null
								&& !task[3].bm.isRecycled()) {
							task[3].bm.recycle();
							task[3].bm = null;
							HttpGetPicConnection.mImageCache
									.remove(json_courtInfo
											.getString("HeadPic" + 3));
							System.gc();
						}
					}

					if (task.length >= 3) {
						if (task[2] == null || task[2].bm == null
								|| task[2].bm.isRecycled()) {
							task[2] = new AsyncGyminfoViewTask(
									CourtinfoActivity.this,
									json_courtInfo.getString("HeadPic2"),
									iv_headpic[2], iv_photo[2]);
							task[2].execute(iv_headpic[2]);
						}
					}

					if (task[0] == null || task[0].bm == null
							|| task[0].bm.isRecycled()) {
						task[0] = new AsyncGyminfoViewTask(
								CourtinfoActivity.this,
								json_courtInfo.getString("HeadPic0"),
								iv_headpic[0], iv_photo[0]);
						task[0].execute(iv_headpic[0]);
					}

				} else if (arg0 == imageViews.length - 1) {
					if (task.length >= 3) {
						if (task[imageViews.length - 3] != null
								&& task[imageViews.length - 3].bm != null
								&& !task[imageViews.length - 3].bm.isRecycled()) {
							task[imageViews.length - 3].bm.recycle();
							task[imageViews.length - 3].bm = null;
							HttpGetPicConnection.mImageCache
									.remove(json_courtInfo.getString("HeadPic"
											+ (imageViews.length - 3)));
							System.gc();
						}
					}
				} else if (arg0 == imageViews.length - 2) {
					if (task.length >= 4) {
						if (task[imageViews.length - 4] != null
								&& task[imageViews.length - 4].bm != null
								&& !task[imageViews.length - 4].bm.isRecycled()) {
							task[imageViews.length - 4].bm.recycle();
							task[imageViews.length - 4].bm = null;
							HttpGetPicConnection.mImageCache
									.remove(json_courtInfo.getString("HeadPic"
											+ (imageViews.length - 4)));
							System.gc();
						}
					}

					if (task.length >= 3) {
						if (task[imageViews.length - 3] == null
								|| task[imageViews.length - 3].bm == null
								|| task[imageViews.length - 3].bm.isRecycled()) {
							task[imageViews.length - 3] = new AsyncGyminfoViewTask(
									CourtinfoActivity.this,
									json_courtInfo.getString("HeadPic"
											+ (imageViews.length - 3)),
									iv_headpic[imageViews.length - 3],
									iv_photo[imageViews.length - 3]);
							task[imageViews.length - 3]
									.execute(iv_headpic[imageViews.length - 3]);
						}
					}

					if (task[imageViews.length - 1] == null
							|| task[imageViews.length - 1].bm == null
							|| task[imageViews.length - 1].bm.isRecycled()) {
						task[imageViews.length - 1] = new AsyncGyminfoViewTask(
								CourtinfoActivity.this,
								json_courtInfo.getString("HeadPic"
										+ (imageViews.length - 1)),
								iv_headpic[imageViews.length - 1],
								iv_photo[imageViews.length - 1]);
						task[imageViews.length - 1]
								.execute(iv_headpic[imageViews.length - 1]);
					}

				} else {
					if (task[arg0 - 2] != null && task[arg0 - 2].bm != null
							&& !task[arg0 - 2].bm.isRecycled()) {
						task[arg0 - 2].bm.recycle();
						task[arg0 - 2].bm = null;
						HttpGetPicConnection.mImageCache.remove(json_courtInfo
								.getString("HeadPic" + (arg0 - 2)));
						System.gc();
					}

					if (task[arg0 + 2] != null && task[arg0 + 2].bm != null
							&& !task[arg0 + 2].bm.isRecycled()) {
						task[arg0 + 2].bm.recycle();
						task[arg0 + 2].bm = null;
						HttpGetPicConnection.mImageCache.remove(json_courtInfo
								.getString("HeadPic" + (arg0 + 2)));
						System.gc();
					}

					if (task[arg0 - 1] == null || task[arg0 - 1].bm == null
							|| task[arg0 - 1].bm.isRecycled()) {
						task[arg0 - 1] = new AsyncGyminfoViewTask(
								CourtinfoActivity.this,
								json_courtInfo
										.getString("HeadPic" + (arg0 - 1)),
								iv_headpic[arg0 - 1], iv_photo[arg0 - 1]);
						task[arg0 - 1].execute(iv_headpic[arg0 - 1]);
					}

					if (task[arg0 + 1] == null || task[arg0 + 1].bm == null
							|| task[arg0 + 1].bm.isRecycled()) {
						task[arg0 + 1] = new AsyncGyminfoViewTask(
								CourtinfoActivity.this,
								json_courtInfo
										.getString("HeadPic" + (arg0 + 1)),
								iv_headpic[arg0 + 1], iv_photo[arg0 + 1]);
						task[arg0 + 1].execute(iv_headpic[arg0 + 1]);
					}

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private final class AdPageAdapter extends PagerAdapter {
		private List<View> views = null;

		public AdPageAdapter(List<View> views) {
			this.views = views;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(views.get(position));
		}

		@Override
		public int getCount() {
			return views.size();
		}

		@Override
		public Object instantiateItem(View container, int position) {
			((ViewPager) container).addView(views.get(position), 0);
			return views.get(position);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}

	void selectNavi(int which) {
		try {
			if (which == 0) {
				Intent intent = new Intent(CourtinfoActivity.this,
						BusrouteActivity.class);
				intent.putExtra("Latitude",
						json_courtInfo.getString("GymLatitude"));
				intent.putExtra("Longitude",
						json_courtInfo.getString("GymLongitude"));
				startActivity(intent);
			} else {
				Intent routeIntent = new Intent(CourtinfoActivity.this,
						SimpleRouteNaviActivity.class);
				routeIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

				routeIntent.putExtra("GymName",
						json_courtInfo.getString("GymName"));

				routeIntent.putExtra("Latitude",
						json_courtInfo.getString("GymLatitude"));
				routeIntent.putExtra("Longitude",
						json_courtInfo.getString("GymLongitude"));
				routeIntent.putExtra("Mode", which);
				System.out.println("Courtinfo");
				startActivity(routeIntent);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class CheckPasscodeThread extends Thread {

		// 0匹配 1普�?�订�? 2约战 3游泳�?
		int i;

		CheckPasscodeThread(int i) {
			this.i = i;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("UserID", Data.USERID));
				params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));

				String result = new HttpPostConnection("CheckPasscode", params)
						.httpConnection();
				if (result.equals("timeout")) {
					Message msg = new Message();
					Bundle b = new Bundle();
					msg.setData(b);
					timeoutHandler.sendMessage(msg);
					return;
				}

				JSONObject json = new JSONObject(result);
				if (json.getBoolean("Result")) {
					Intent intent;
					switch (i) {
					case 0:
						intent = new Intent(CourtinfoActivity.this,
								MatchActivity.class);

						intent.putExtra("gymName",
								json_courtInfo.getString("GymName"));
						intent.putExtra("gymType",
								json_courtInfo.getInt("GymMainType"));
						intent.putExtra("ID", gymID);
						intent.putExtra("StartTime",
								json_courtInfo.getInt("StartTime"));
						intent.putExtra("EndTime",
								json_courtInfo.getInt("EndTime"));
						intent.putExtra("Weight", Integer
								.parseInt(json_courtInfo.getString("Weight")));

						Message msg = new Message();
						Bundle b = new Bundle();
						msg.setData(b);
						closePbHandler.sendMessage(msg);

						startActivity(intent);
						break;
					case 1:
						intent = new Intent(CourtinfoActivity.this,
								BookActivity.class);
						intent.putExtra("ID", gymID);
						intent.putExtra("gymType",
								json_courtInfo.getInt("GymMainType"));
						intent.putExtra("gymName",
								json_courtInfo.getString("GymName"));
						intent.putExtra("StartTime",
								json_courtInfo.getInt("StartTime"));
						intent.putExtra("EndTime",
								json_courtInfo.getInt("EndTime"));
						intent.putExtra("Weight", Integer
								.parseInt(json_courtInfo.getString("Weight")));

						Message msg1 = new Message();
						Bundle b1 = new Bundle();
						msg1.setData(b1);
						closePbHandler.sendMessage(msg1);

						startActivity(intent);
						break;
					case 2:
						new Thread(r_TeamList).start();
						break;
					case 3:
						intent = new Intent(CourtinfoActivity.this,
								CardActivity.class);
						intent.putExtra("ID", gymID);

						Message msg2 = new Message();
						Bundle b2 = new Bundle();
						msg2.setData(b2);
						closePbHandler.sendMessage(msg2);

						startActivity(intent);
						break;
					}
				} else {

					Message msg = new Message();
					Bundle b = new Bundle();
					msg.setData(b);
					closePbHandler.sendMessage(msg);

					Intent intent = new Intent(CourtinfoActivity.this,
							LoginActivity.class);
					startActivityForResult(intent, 102);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

	Runnable r_Courtinfo = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "RequestByID"));
			params.add(new BasicNameValuePair("GymDetailLevel", "2"));
			params.add(new BasicNameValuePair("GymID", gymID));
			params.add(new BasicNameValuePair("Longitude", Data.LONGITUDE + ""));
			params.add(new BasicNameValuePair("Latitude", Data.LATITUDE + ""));
			if (Data.ISLOGIN) {
				params.add(new BasicNameValuePair("UserID", Data.USERID));
			}

			String result = new HttpPostConnection("GymInfoServer", params)
					.httpConnection();

			if (result.equals("timeout")) {
				Message msg = new Message();
				Bundle b = new Bundle();
				msg.setData(b);
				timeoutHandler.sendMessage(msg);
			} else {

				System.out.println(result);

				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("courtinfo", result);
				b.putBoolean("isLoaded", true);
				msg.setData(b);
				courtinfoHandler.sendMessage(msg);

			}

		}
	};

	Runnable r_CommentList = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "RequestCommentList"));
			params.add(new BasicNameValuePair("GymID", gymID));

			String result = new HttpPostConnection("CommentServer", params)
					.httpConnection();

			if (result.equals("timeout")) {
				Message msg = new Message();
				Bundle b = new Bundle();
				msg.setData(b);
				timeoutHandler.sendMessage(msg);
			} else {
				try {
					JSONObject json = new JSONObject(result);

					int commentNum = json.getInt("CommentNum");
					int i;
					if (commentNum >= 2)
						i = 2;
					else
						i = commentNum;

					if (i == 0)
						loadNum++;

					for (int j = 0; j < i; j++) {

						params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("Request",
								"RequestCommentDetail"));
						params.add(new BasicNameValuePair("CommentID", json
								.getInt("CommentID" + j) + ""));

						result = new HttpPostConnection("CommentServer", params)
								.httpConnection();

						if (result.equals("timeout")) {
							Message msg = new Message();
							Bundle b = new Bundle();
							msg.setData(b);
							timeoutHandler.sendMessage(msg);
						} else {

							Message msg = new Message();
							Bundle b = new Bundle();
							b.putString("info", result);
							b.putInt("CommentNum", commentNum);
							if (j == i - 1)
								b.putBoolean("isLoaded", true);
							else
								b.putBoolean("isLoaded", false);
							msg.setData(b);
							commentHandler.sendMessage(msg);
						}
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	};

	Runnable r_CoachList = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "CoachList"));
			params.add(new BasicNameValuePair("GymID", gymID));

			String result = new HttpPostConnection("CoachServer", params)
					.httpConnection();

			if (result.equals("timeout")) {
				Message msg = new Message();
				Bundle b = new Bundle();
				msg.setData(b);
				timeoutHandler.sendMessage(msg);
			} else {
				try {
					JSONObject json = new JSONObject(result);

					int coachNum = json.getInt("CoachNum");
					int i;
					if (coachNum >= 2)
						i = 2;
					else
						i = coachNum;

					if (i == 0)
						loadNum++;

					for (int j = 0; j < i; j++) {

						params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("Request",
								"CoachDetail"));
						params.add(new BasicNameValuePair("CoachID", json
								.getInt("CoachID" + j) + ""));

						result = new HttpPostConnection("CoachServer", params)
								.httpConnection();

						if (result.equals("timeout")) {
							Message msg = new Message();
							Bundle b = new Bundle();
							msg.setData(b);
							timeoutHandler.sendMessage(msg);
						} else {

							Message msg = new Message();
							Bundle b = new Bundle();
							b.putString("info", result);
							b.putInt("CoachNum", coachNum);
							if (j == i - 1)
								b.putBoolean("isLoaded", true);
							else
								b.putBoolean("isLoaded", false);
							msg.setData(b);
							coachHandler.sendMessage(msg);
						}
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	};

	Runnable r_Collect = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			if (isCollect) {
				params.add(new BasicNameValuePair("Request", "UnCollect"));
				params.add(new BasicNameValuePair("UserID", Data.USERID));
				params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
				params.add(new BasicNameValuePair("GymID", gymID));
			} else {
				params.add(new BasicNameValuePair("Request", "Collect"));
				params.add(new BasicNameValuePair("UserID", Data.USERID));
				params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
				params.add(new BasicNameValuePair("GymID", gymID));
			}
			String result = new HttpPostConnection("CollectServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			collectHandler.sendMessage(msg);

		}
	};

	Runnable r_TeamList = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("Request", "MyTeam"));
				params.add(new BasicNameValuePair("UserID", Data.USERID));

				String result = new HttpPostConnection("TeamServer", params)
						.httpConnection();

				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("result", result);
				msg.setData(b);
				teamlistHandler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	class CourtinfoHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();

			try {
				json_courtInfo = new JSONObject(b.getString("courtinfo"));

				gymType = json_courtInfo.getInt("GymMainType");

				if (Data.ISLOGIN) {
					if (json_courtInfo.getBoolean("Collect")) {
						iv_collect
								.setImageResource(R.drawable.ic_courtinfo_collect2);
						isCollect = true;
					} else {
						iv_collect
								.setImageResource(R.drawable.ic_courtinfo_collect1);
						isCollect = false;
					}
				} else {
					iv_collect
							.setImageResource(R.drawable.ic_courtinfo_collect1);
					isCollect = false;
				}

				rl_collect.setVisibility(View.VISIBLE);
				rl_share.setVisibility(View.VISIBLE);

				TextView gymName = (TextView) findViewById(R.id.tv_courtinfo_gymname);
				gymName.setText(json_courtInfo.getString("GymName"));
				str_gymName = json_courtInfo.getString("GymName");
				
				TextView tv_notice=(TextView)findViewById(R.id.tv_courtinfo_notice);
				if(json_courtInfo.has("Notice")){
					tv_notice.setText(json_courtInfo.getString("Notice"));
				}

				RatingBar rb_rate = (RatingBar) findViewById(R.id.rb_courtinfo_rate);
				TextView tv_rate = (TextView) findViewById(R.id.tv_courtinfo_rate);
				TextView tv_ratenum = (TextView) findViewById(R.id.tv_courtinfo_ratenum);
				tv_ratenum
						.setText("(" + json_courtInfo.getInt("RateNum") + ")");
				if (json_courtInfo.getInt("RateNum") == 0) {
					tv_rate.setText("0分");
					str_mark = "0";
					rb_rate.setRating(0);
				} else {
					double f = json_courtInfo.getDouble("Rate");
					BigDecimal b1 = new BigDecimal(f);
					double f1 = b1.setScale(1, BigDecimal.ROUND_HALF_UP)
							.doubleValue();
					tv_rate.setText(f1 + "分");
					str_mark = json_courtInfo.getDouble("Rate") + "";
					rb_rate.setRating((float) json_courtInfo.getDouble("Rate"));
				}

				TextView price = (TextView) findViewById(R.id.tv_courtinfo_price);
				price.setText(Data.doubleTrans(json_courtInfo.getDouble("Price")));

				int type = Integer.valueOf(
						json_courtInfo.getString("GymMainType")).intValue();
				if (type == 4 || type == 5 || type == 6 || type == 7) {
					/*
					 * TextView tv_match = (TextView)
					 * findViewById(R.id.tv_courtinfo_match);
					 * tv_match.setVisibility(View.VISIBLE);
					 */
					btn_match.setVisibility(View.VISIBLE);
					/*
					 * btn_match.setOnClickListener(new OnClickListener() {
					 * 
					 * @Override public void onClick(View v) { // TODO
					 * Auto-generated method stub if (Data.ISLOGIN) {
					 * progressDialog.show(); new
					 * CheckPasscodeThread(0).start(); } else { Intent intent1 =
					 * new Intent( CourtinfoActivity.this, LoginActivity.class);
					 * startActivityForResult(intent1, 102); } } });
					 */
				} else if (type == 1 || type == 2 || type == 3) {
					btn_match.setVisibility(View.VISIBLE);
					btn_match.setText("发布约战");
				} else {
					btn_match.setVisibility(View.GONE);
					TextView tv_match = (TextView) findViewById(R.id.tv_courtinfo_match);
					tv_match.setVisibility(View.VISIBLE);
				}

				TextView address = (TextView) findViewById(R.id.tv_courtinfo_address);
				address.setText(json_courtInfo.getString("Address"));

				TextView distance = (TextView) findViewById(R.id.tv_courtinfo_distance);
				double meters = json_courtInfo.getDouble("Distance");
				String distance1;
				if (meters < 500)
					distance1 = "<500m";
				else if (meters < 1000)
					distance1 = (int) (meters / 100) + "00m";
				else if (meters < 10000)
					distance1 = ((double) ((int) (meters / 100))) / 10 + "km";
				else
					distance1 = (int) (meters / 1000) + "km";
				distance.setText(distance1);

				//LinearLayout ll_gyminfo = (LinearLayout) findViewById(R.id.ll_courtinfo_gyminfocontainer);
				//ll_gyminfo.removeAllViews();
				TextView tv_feature=(TextView)findViewById(R.id.tv_courtinfo_gyminfocontainer);

				LinearLayout ll_service = (LinearLayout) findViewById(R.id.ll_courtinfo_servicecontainer);
				ll_service.removeAllViews();

				LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(
						(int) (Data.SCREENWIDTH / 6),
						(int) (Data.SCREENWIDTH / 12));
				p1.setMargins(Data.SCREENWIDTH * (1 / 5), 0, 0, 0);
				ImageView iv;
				String info = json_courtInfo.getString("Feature");
				String[] info1 = info.split(",");
				/*int[] feature = { R.drawable.ic_gyminfo1,
						R.drawable.ic_gyminfo2, R.drawable.ic_gyminfo3,
						R.drawable.ic_gyminfo4, R.drawable.ic_gyminfo5,
						R.drawable.ic_gyminfo6, R.drawable.ic_gyminfo7,
						R.drawable.ic_gyminfo8, R.drawable.ic_gyminfo9,
						R.drawable.ic_gyminfo10, R.drawable.ic_gyminfo11,
						R.drawable.ic_gyminfo12, R.drawable.ic_gyminfo13,
						R.drawable.ic_gyminfo14, R.drawable.ic_gyminfo15,
						R.drawable.ic_gyminfo16, R.drawable.ic_gyminfo17,
						R.drawable.ic_gyminfo18, R.drawable.ic_gyminfo19 };
				for (int i = 0; i < info1.length; i++) {
					iv = new ImageView(CourtinfoActivity.this);

					iv.setImageDrawable(getResources().getDrawable(
							feature[Integer.parseInt(info1[i]) - 1]));
					iv.setLayoutParams(p1);
					ll_gyminfo.addView(iv);

				}*/
				String[] feature = { "▪层高8m      ", "▪层高9m      ",
						"▪层高10m      ", "▪层高11m      ", "▪侧灯      ",
						"▪吊灯      ", "▪胶地板      ", "▪木地板      ", "▪人造草皮      ",
						"▪水泥地      ", "▪天然草皮      ", "▪透明篮板      ",
						"▪11人制      ", "▪七人制      ", "▪五人制     ", "▪室内      ",
						"▪室外      ", "▪美式      ", "▪斯诺克      " };
				String str="";
				for (int i = 0; i < info1.length; i++) {
					str+=feature[Integer.parseInt(info1[i])];
				}
				tv_feature.setText(str);

				LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(
						(int) (Data.SCREENWIDTH / 6),
						(int) (Data.SCREENWIDTH / 8));
				p2.setMargins(Data.SCREENWIDTH * (1 / 15), 0, 0, 0);
				String service = json_courtInfo.getString("Service");
				String[] service1 = service.split(",");
				int[] service2 = { R.drawable.ic_courtinfo_parking,
						R.drawable.ic_courtinfo_wifi,
						R.drawable.ic_courtinfo_shower,
						R.drawable.ic_courtinfo_store,
						R.drawable.ic_courtinfo_drink,
						R.drawable.ic_courtinfo_food };
				for (int i = 0; i < service1.length; i++) {
					iv = new ImageView(CourtinfoActivity.this);

					if (Integer.parseInt(service1[i]) <= 6) {

						iv.setImageDrawable(getResources().getDrawable(
								service2[Integer.parseInt(service1[i]) - 1]));
						iv.setLayoutParams(p2);
						ll_service.addView(iv);
					}
				}
				
				TextView intro = (TextView) findViewById(R.id.tv_courtinfo_intro);
				intro.setText(json_courtInfo.getString("GymIntroduction"));

				int picNum = json_courtInfo.getInt("HeadPicNum");
				// bm_gympic = new Bitmap[picNum];
				task = new AsyncGyminfoViewTask[picNum];
				initPageAdapter(picNum);

				if (b.getBoolean("isLoaded"))
					loadNum++;
				if (loadNum >= 3)
					rl_gifcontainer.setVisibility(View.GONE);

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

			LayoutInflater mLi = LayoutInflater.from(CourtinfoActivity.this);
			View view = mLi.inflate(R.layout.layout_courtinfo_comment, null);

			try {
				final JSONObject json = new JSONObject(b.getString("info"));

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
					new AsyncViewTask(CourtinfoActivity.this, path, head,3)
							.execute(head);// 异步加载图片
				} else {
					head.setImageResource(R.drawable.ic_mainpage_tab3_defaultphoto);
				}

				LinearLayout ll_photo = (LinearLayout) view
						.findViewById(R.id.ll_comment_photo);
				LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
						200, 200);
				p.setMargins((int) (Data.SCREENWIDTH * 0.04), 0, 0, 0);

				for (int i = 0; i < json.getInt("CommentPicNum"); i++) {
					ImageView iv = new ImageView(CourtinfoActivity.this);
					iv.setScaleType(ScaleType.FIT_CENTER);
					iv.setLayoutParams(p);
					iv.setOnClickListener(new PicOnClickListener(json
							.getInt("CommentPicNum"), json
							.getInt("CommentPicNum"), i, json,
							CourtinfoActivity.this, PhotoActivity.class));
					ll_photo.addView(iv);
					new AsyncViewTask(CourtinfoActivity.this,
							json.getString("CommentPic" + i), iv,8).execute(iv);// 异步加载图片
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			ll_commentcontainer.addView(view, p);

			TextView tv_commentnum = (TextView) CourtinfoActivity.this
					.findViewById(R.id.tv_courtinfo_commentnum);
			tv_commentnum.setText(b.getInt("CommentNum") + "");

			if (b.getBoolean("isLoaded"))
				loadNum++;
			if (loadNum >= 3)
				rl_gifcontainer.setVisibility(View.GONE);

		}
	}

	class CoachHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();

			LayoutInflater mLi = LayoutInflater.from(CourtinfoActivity.this);
			View view = mLi.inflate(R.layout.layout_courtinfo_coach, null);

			try {
				JSONObject json = new JSONObject(b.getString("info"));

				TextView name = (TextView) view
						.findViewById(R.id.tv_coach_name);
				name.setText(json.getString("CoachName"));
				TextView title = (TextView) view
						.findViewById(R.id.tv_coach_title);
				title.setText(json.getString("Introduction"));
				TextView detail = (TextView) view
						.findViewById(R.id.tv_coach_intro);
				detail.setText(json.getString("Detail"));

				ImageView head = (ImageView) view
						.findViewById(R.id.iv_coach_head);
				if (json.has("Path")) {
					String path = (String) json.getString("Path");
					new AsyncViewTask(CourtinfoActivity.this, path, head,2)
							.execute(head);// 异步加载图片
				} else {
					head.setImageResource(R.drawable.ic_mainpage_tab3_defaultphoto);
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			ll_coachcontainer.addView(view, p);

			TextView tv_coachnum = (TextView) CourtinfoActivity.this
					.findViewById(R.id.tv_courtinfo_coachnum);
			tv_coachnum.setText(b.getInt("CoachNum") + "");

			if (b.getBoolean("isLoaded"))
				loadNum++;
			if (loadNum >= 3)
				rl_gifcontainer.setVisibility(View.GONE);
		}
	}

	class CollectHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();
			String result = b.getString("result");

			if (result.equals("timeout")) {
				Toast.makeText(CourtinfoActivity.this, "网络出问题咯(+﹏+)~",
						Toast.LENGTH_SHORT).show();
				return;
			}
			try {
				JSONObject json = new JSONObject(result);

				if (json.getString("Result").equals("身份验证失败")) {
					Data.ISLOGIN = false;
					Toast.makeText(CourtinfoActivity.this, "登录信息过期咯，请重新登录~",
							Toast.LENGTH_SHORT).show();
					startActivity(new Intent(CourtinfoActivity.this,
							LoginActivity.class));
				} else if (json.getString("Result").equals("收藏成功")) {
					Toast.makeText(CourtinfoActivity.this, "收藏成功╰(￣▽￣)╮",
							Toast.LENGTH_SHORT).show();
					iv_collect
							.setImageResource(R.drawable.ic_courtinfo_collect2);
					isCollect = true;
				} else {
					Toast.makeText(CourtinfoActivity.this, "取消收藏成功╭(′▽`)╯",
							Toast.LENGTH_SHORT).show();
					iv_collect
							.setImageResource(R.drawable.ic_courtinfo_collect1);
					isCollect = false;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	class TimeoutHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			/*
			 * Toast.makeText(CourtinfoActivity.this, "网络出问题咩(+�?+)~",
			 * Toast.LENGTH_SHORT).show();
			 */
			rl_gifcontainer.setVisibility(View.GONE);
			tv_error.setVisibility(View.VISIBLE);
		}
	}

	class CloseProgressbarHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			progressDialog.dismiss();
		}
	}

	class TeamlistHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			try {

				progressDialog.dismiss();

				String result = msg.getData().getString("result");

				System.out.println(result);

				if (result.equals("timeout")) {
					Toast.makeText(CourtinfoActivity.this, "网络出问题咩(+﹏+)~",
							Toast.LENGTH_SHORT).show();
					return;
				}

				JSONObject json = new JSONObject(result);
				if (json.getInt("TeamNum") == 0) {
					Toast.makeText(CourtinfoActivity.this, "您还没有加入1支球",
							Toast.LENGTH_SHORT).show();
				} else {
					Intent intent = new Intent(CourtinfoActivity.this,
							FightSelectteamActivity.class);
					intent.putExtra("teamID", result);
					intent.putExtra("gymID", gymID);
					intent.putExtra("gymType", gymType);
					intent.putExtra("gymName",
							json_courtInfo.getString("GymName"));
					intent.putExtra("startTime",
							json_courtInfo.getInt("StartTime"));
					intent.putExtra("endTime",
							json_courtInfo.getInt("EndTime"));
					intent.putExtra("weight", Integer
							.parseInt(json_courtInfo.getString("Weight")));

					startActivity(intent);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// requestCode 102 登录后个人界面信息更�?
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 102:
				new Thread(r_Courtinfo).start();
				break;
			}
		}
	}

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			adViewPager.setCurrentItem(intent.getIntExtra("currentPic", 0));
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

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (task != null) {
			for (int i = 0; i < task.length; i++) {
				if (task[i] != null && task[i].bm != null
						&& !task[i].bm.isRecycled()) {
					task[i].bm.recycle();
					task[i].bm = null;
					try {
						HttpGetPicConnection.mImageCache.remove(json_courtInfo
								.getString("HeadPic" + i));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("回收图片" + i);
				}
			}
			System.gc();
			task = null;
		}
		super.onDestroy();
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

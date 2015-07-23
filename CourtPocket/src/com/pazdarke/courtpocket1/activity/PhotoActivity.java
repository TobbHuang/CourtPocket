package com.pazdarke.courtpocket1.activity;

import java.util.ArrayList;
import java.util.List;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.httpConnection.HttpGetPicConnection;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.pazdarke.courtpocket1.tools.listview.AsyncViewTask;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class PhotoActivity extends Activity {

	LinearLayout ll_container;

	int picNum;// 记录照片总数,从1开始
	int currentPicNum;// 进入activity时是第几张照片，从0开始

	ViewPager viewPager;
	ArrayList<View> pageViews;
	AdPageAdapter adapter;
	AsyncViewTask[] task;
	ImageView[] iv;

	TextView tv_num;

	Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.activity_photo);

			AppManager.getAppManager().addActivity(this);

			intent = getIntent();
			picNum = intent.getIntExtra("picNum", 0);
			task = new AsyncViewTask[picNum];
			iv = new ImageView[picNum];

			currentPicNum = intent.getIntExtra("currentPicNum", 0);

			tv_num = (TextView) findViewById(R.id.tv_photo_num);
			tv_num.setText((currentPicNum + 1) + "/" + picNum);

			initViewPager();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initViewPager() {
		// TODO Auto-generated method stub

		ll_container = (LinearLayout) findViewById(R.id.ll_photo_container);

		viewPager = new ViewPager(this);

		// 动态修改栏宽、高度
		viewPager.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		ll_container.addView(viewPager);

		initPageAdapter();

		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(new AdPageChangeListener());
		viewPager.setCurrentItem(currentPicNum);

	}

	private void initPageAdapter() {
		pageViews = new ArrayList<View>();

		for (int i = 0; i < intent.getIntExtra("picNum", 0); i++) {

			iv[i] = new ImageView(this);
			iv[i].setImageResource(R.color.lightGrey);
			iv[i].setScaleType(ScaleType.FIT_CENTER);
			iv[i].setOnClickListener(onClickListener);

			pageViews.add(iv[i]);
		}

		if (currentPicNum == 0) {
			task[0] = new AsyncViewTask(this, intent.getStringExtra("Path0")
					.replace("\\", "-").replace("/", "-"), iv[0], 1);
			task[0].execute(iv[0]);

			if (picNum >= 2) {
				task[1] = new AsyncViewTask(this, intent
						.getStringExtra("Path1").replace("\\", "-")
						.replace("/", "-"), iv[1], 1);
				task[1].execute(iv[1]);
			}
		} else if (currentPicNum == picNum - 1) {
			task[picNum - 1] = new AsyncViewTask(this, intent
					.getStringExtra("Path" + (picNum - 1)).replace("\\", "-")
					.replace("/", "-"), iv[picNum - 1], 1);
			task[picNum - 1].execute(iv[picNum - 1]);

			task[picNum - 2] = new AsyncViewTask(this, intent
					.getStringExtra("Path" + (picNum - 2)).replace("\\", "-")
					.replace("/", "-"), iv[picNum - 2], 1);
			task[picNum - 2].execute(iv[picNum - 2]);
		} else {
			task[currentPicNum - 1] = new AsyncViewTask(this, intent
					.getStringExtra("Path" + (currentPicNum - 1))
					.replace("\\", "-").replace("/", "-"),
					iv[currentPicNum - 1], 1);
			task[currentPicNum - 1].execute(iv[currentPicNum - 1]);

			task[currentPicNum + 1] = new AsyncViewTask(this, intent
					.getStringExtra("Path" + (currentPicNum + 1))
					.replace("\\", "-").replace("/", "-"),
					iv[currentPicNum + 1], 1);
			task[currentPicNum + 1].execute(iv[currentPicNum + 1]);

			task[currentPicNum] = new AsyncViewTask(this, intent
					.getStringExtra("Path" + (currentPicNum))
					.replace("\\", "-").replace("/", "-"), iv[currentPicNum], 1);
			task[currentPicNum].execute(iv[currentPicNum]);
		}

		adapter = new AdPageAdapter(pageViews);
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
			tv_num.setText((arg0 + 1) + "/" + picNum);

			try {
				// 动态回收bitmap内存，减少OOM的概率
				if (arg0 == 0) {
					if (task.length >= 3) {
						if (task[2] != null && task[2].bm != null
								&& !task[2].bm.isRecycled()) {
							task[2].bm.recycle();
							task[2].bm = null;
							HttpGetPicConnection.mImageCache.remove(intent
									.getStringExtra("Path" + 2)
									.replace("\\", "-").replace("/", "-"));
							System.gc();
						}
					}
				} else if (arg0 == 1) {
					if (task.length >= 4) {
						if (task[3] != null && task[3].bm != null
								&& !task[3].bm.isRecycled()) {
							task[3].bm.recycle();
							task[3].bm = null;
							HttpGetPicConnection.mImageCache.remove(intent
									.getStringExtra("Path" + 3)
									.replace("\\", "-").replace("/", "-"));
							System.gc();
						}
					}

					if (task.length >= 3) {
						if (task[2] == null || task[2].bm == null
								|| task[2].bm.isRecycled()) {
							task[2] = new AsyncViewTask(PhotoActivity.this,
									intent.getStringExtra("Path" + (2))
											.replace("\\", "-")
											.replace("/", "-"), iv[2], 1);
							task[2].execute(iv[2]);
						}
					}

					if (task[0] == null || task[0].bm == null
							|| task[0].bm.isRecycled()) {
						task[0] = new AsyncViewTask(PhotoActivity.this, intent
								.getStringExtra("Path" + (0))
								.replace("\\", "-").replace("/", "-"), iv[0], 1);
						task[0].execute(iv[0]);
					}

				} else if (arg0 == task.length - 1) {
					if (task.length >= 3) {
						if (task[task.length - 3] != null
								&& task[task.length - 3].bm != null
								&& !task[task.length - 3].bm.isRecycled()) {
							task[task.length - 3].bm.recycle();
							task[task.length - 3].bm = null;
							HttpGetPicConnection.mImageCache.remove(intent
									.getStringExtra("Path" + (task.length - 3))
									.replace("\\", "-").replace("/", "-"));
							System.gc();
						}
					}
				} else if (arg0 == task.length - 2) {
					if (task.length >= 4) {
						if (task[task.length - 4] != null
								&& task[task.length - 4].bm != null
								&& !task[task.length - 4].bm.isRecycled()) {
							task[task.length - 4].bm.recycle();
							task[task.length - 4].bm = null;
							HttpGetPicConnection.mImageCache.remove(intent
									.getStringExtra("Path" + (task.length - 4))
									.replace("\\", "-").replace("/", "-"));
							System.gc();
						}
					}

					if (task.length >= 3) {
						if (task[task.length - 3] == null
								|| task[task.length - 3].bm == null
								|| task[task.length - 3].bm.isRecycled()) {
							task[task.length - 3] = new AsyncViewTask(
									PhotoActivity.this, intent
											.getStringExtra(
													"Path" + (task.length - 3))
											.replace("\\", "-")
											.replace("/", "-"),
									iv[task.length - 3], 1);
							task[task.length - 3].execute(iv[task.length - 3]);
						}
					}

					if (task[task.length - 1] == null
							|| task[task.length - 1].bm == null
							|| task[task.length - 1].bm.isRecycled()) {
						task[task.length - 1] = new AsyncViewTask(
								PhotoActivity.this, intent
										.getStringExtra(
												"Path" + (task.length - 1))
										.replace("\\", "-").replace("/", "-"),
								iv[task.length - 1], 1);
						task[task.length - 1].execute(iv[task.length - 1]);
					}

				} else {
					if (task[arg0 - 2] != null && task[arg0 - 2].bm != null
							&& !task[arg0 - 2].bm.isRecycled()) {
						task[arg0 - 2].bm.recycle();
						task[arg0 - 2].bm = null;
						HttpGetPicConnection.mImageCache.remove(intent
								.getStringExtra("Path" + (arg0 - 2))
								.replace("\\", "-").replace("/", "-"));
						System.gc();
					}

					if (task[arg0 + 2] != null && task[arg0 + 2].bm != null
							&& !task[arg0 + 2].bm.isRecycled()) {
						task[arg0 + 2].bm.recycle();
						task[arg0 + 2].bm = null;
						HttpGetPicConnection.mImageCache.remove(intent
								.getStringExtra("Path" + (arg0 + 2))
								.replace("\\", "-").replace("/", "-"));
						System.gc();
					}

					if (task[arg0 - 1] == null || task[arg0 - 1].bm == null
							|| task[arg0 - 1].bm.isRecycled()) {
						task[arg0 - 1] = new AsyncViewTask(PhotoActivity.this,
								intent.getStringExtra("Path" + (arg0 - 1))
										.replace("\\", "-").replace("/", "-"),
								iv[arg0 - 1], 1);
						task[arg0 - 1].execute(iv[arg0 - 1]);
					}

					if (task[arg0 + 1] == null || task[arg0 + 1].bm == null
							|| task[arg0 + 1].bm.isRecycled()) {
						task[arg0 + 1] = new AsyncViewTask(PhotoActivity.this,
								intent.getStringExtra("Path" + (arg0 + 1))
										.replace("\\", "-").replace("/", "-"),
								iv[arg0 + 1], 1);
						task[arg0 + 1].execute(iv[arg0 + 1]);
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

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
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

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (task != null) {
			for (int i = 0; i < task.length; i++) {
				if (task[i] != null && task[i].bm != null
						&& !task[i].bm.isRecycled()) {
					task[i].bm.recycle();
					task[i].bm = null;
					HttpGetPicConnection.mImageCache.remove(intent
							.getStringExtra("Path" + i).replace("\\", "-")
							.replace("/", "-"));
					System.out.println("回收图片" + i);
				}
			}
			System.gc();
			task = null;
		}
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

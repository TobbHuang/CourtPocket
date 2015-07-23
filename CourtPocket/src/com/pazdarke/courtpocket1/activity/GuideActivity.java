package com.pazdarke.courtpocket1.activity;

import java.util.ArrayList;
import java.util.List;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class GuideActivity extends Activity {

	ViewPager viewPager;
	ArrayList<View> pageViews;
	AdPageAdapter adapter;
	ImageView[] imageViews = new ImageView[4];
	ImageView[] iv = new ImageView[4];

	LinearLayout ll_container;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_guide);

		AppManager.getAppManager().addActivity(this);

		initViewPager();

	}

	private void initViewPager() {
		// TODO Auto-generated method stub

		ll_container = (LinearLayout) findViewById(R.id.ll_guide_pic);

		viewPager = new ViewPager(this);

		// 动态修改栏宽、高度
		viewPager.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		ll_container.addView(viewPager);

		initPageAdapter();

		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(new AdPageChangeListener());
		viewPager.setCurrentItem(0);

	}

	private void initPageAdapter() {
		pageViews = new ArrayList<View>();

		for (int i = 0; i < 3; i++) {

			int[] ic = { R.drawable.ic_guide1, R.drawable.ic_guide2,
					R.drawable.ic_guide3, R.drawable.ic_guide4 };

			iv[i] = new ImageView(this);
			iv[i].setImageResource(ic[i]);
			iv[i].setScaleType(ScaleType.FIT_XY);

			pageViews.add(iv[i]);
		}

		LayoutInflater mLi = LayoutInflater.from(this);
		View view = mLi.inflate(R.layout.view_guide4, null);
		pageViews.add(view);
		iv[3] = (ImageView) view.findViewById(R.id.iv_guide4_bg);

		Button btn_register = (Button) view
				.findViewById(R.id.btn_guide_register);
		btn_register.setOnClickListener(onClickListener);

		Button btn_Login = (Button) view.findViewById(R.id.btn_guide_login);
		btn_Login.setOnClickListener(onClickListener);

		TextView tv_enter = (TextView) view.findViewById(R.id.tv_guide_enter);
		tv_enter.setOnClickListener(onClickListener);

		adapter = new AdPageAdapter(pageViews);

		initCirclePoint();

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
			for (int i = 0; i < imageViews.length; i++) {
				imageViews[arg0]
						.setBackgroundResource(R.drawable.point_focused_guide);
				if (arg0 != i) {
					imageViews[i]
							.setBackgroundResource(R.drawable.point_unfocused);
				}
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

	private void initCirclePoint() {
		ViewGroup group = (ViewGroup) findViewById(R.id.ll_guide_plot);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20);
		params.setMargins(10, 0, 10, 0);
		for (int i = 0; i < pageViews.size(); i++) {
			ImageView imageView = new ImageView(this);
			imageView.setLayoutParams(params);
			imageViews[i] = imageView;

			if (i == 0) {
				imageViews[i]
						.setBackgroundResource(R.drawable.point_focused_guide);
			} else {
				imageViews[i].setBackgroundResource(R.drawable.point_unfocused);
			}
			group.addView(imageViews[i]);
		}

	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			switch (view.getId()) {
			case R.id.btn_guide_register:
				startActivity(new Intent(GuideActivity.this, MainActivity.class));
				startActivity(new Intent(GuideActivity.this, LoginActivity.class));
				startActivity(new Intent(GuideActivity.this, RegisterActivity.class));
				finish();
				break;
			case R.id.btn_guide_login:
				startActivity(new Intent(GuideActivity.this, MainActivity.class));
				startActivity(new Intent(GuideActivity.this, LoginActivity.class));
				finish();
				break;
			case R.id.tv_guide_enter:
				startActivity(new Intent(GuideActivity.this, MainActivity.class));
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

	public void onDestroy() {
		super.onDestroy();
		try {
			for (int i = 0; i < 4; i++) {
				if (iv[i] != null) {
					BitmapDrawable bd = (BitmapDrawable) iv[i].getDrawable();
					Bitmap bm = bd.getBitmap();
					if (bm != null & !bm.isRecycled()) {
						bm.recycle();
						bm = null;
					}
				}
			}
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

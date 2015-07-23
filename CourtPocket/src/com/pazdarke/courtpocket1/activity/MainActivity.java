package com.pazdarke.courtpocket1.activity;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.ant.liao.GifView;
import com.ant.liao.GifView.GifImageType;
import com.blueware.agent.android.BlueWare;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.httpConnection.HttpGetConnection;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.pazdarke.courtpocket1.tools.listview.AsyncViewTask;
import com.pazdarke.courtpocket1.tools.listview.TeamlistAdapter;
import com.pazdarke.courtpocket1.tools.rss.RSSFeed;
import com.pazdarke.courtpocket1.tools.rss.RSSHandler;
import com.pazdarke.courtpocket1.tools.rss.RSSItem;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnTouchListener,
		AMapLocationListener, Runnable {

	// 首页滚动广告相关
	private ViewPager adViewPager;
	private LinearLayout pagerLayout;
	private List<View> pageViews;
	private ImageView[] imageViews;
	private ImageView imageView;
	private AdPageAdapter adapter;
	private AtomicInteger atomicInteger = new AtomicInteger(0);
	private boolean isContinue = true;
	JSONObject json_adinfo;

	// 定位相关
	private LocationManagerProxy aMapLocManager = null;
	private AMapLocation aMapLocation;// 用于判断定位超时
	private Handler handler = new Handler();
	String city;
	boolean isLocationSuccessful;

	LinearLayout ll_tab1_city;

	PopupWindow pop_tabteam_more;
	LinearLayout ll_tabteam_view1, ll_tabteam_view2;
	RelativeLayout rl_tabteam_gifcontainer;
	boolean isTeamRefresh = false;
	JSONObject teamID;
	int sumPage = 0, currentPage = 0, lastPageItemNum;
	boolean hasMore = true;
	int teamLoadNum;// 请求item为异步，判断是否加载完毕
	JSONObject teamInfo;
	JSONObject allTeamInfo;

	ListView lv_list;
	PullToRefreshListView mPullToRefreshListView;
	ArrayList<HashMap<String, Object>> mylist;
	TeamlistAdapter teamAdapter;
	View footView;
	ExecutorService pool;

	ImageView iv_soccer, iv_basketball, iv_volleyball, iv_tabletennis,
			iv_tennis, iv_badminton, iv_billiards, iv_swimming,
			iv_bodybuilding, iv_golf, iv_xgames, iv_moresports;
	LinearLayout ll_tab2_error;
	PullToRefreshListView lv1, lv2, lv3, lv4, lv5, lv6, lv7;

	RelativeLayout.LayoutParams p1, p2;

	public final String RSS_SPORTS = "http://sports.qq.com/rss_newssports.xml";
	public final String RSS_ISOCCER = "http://sports.qq.com/isocce/rss_isocce.xml";
	public final String RSS_CSOCCER = "http://sports.qq.com/csocce/rss_csocce.xml";
	public final String RSS_NBA = "http://sports.qq.com/basket/nba/nbarep/rss_nbarep.xml";
	public final String RSS_CBA = "http://sports.qq.com/basket/bskb/cba/rss_cba.xml";
	public final String RSS_TENNIS = "http://sports.qq.com/tennis/rss_tennis.xml";
	public final String RSS_OTHERS = "http://sports.qq.com/others/rss_others.xml";

	public final String tag = "RSSReader";
	private RSSFeed feedSports = null;
	private RSSFeed feedIsoccer = null;
	private RSSFeed feedCsoccer = null;
	private RSSFeed feedNBA = null;
	private RSSFeed feedCBA = null;
	private RSSFeed feedTennis = null;
	private RSSFeed feedOthers = null;

	HorizontalScrollView hsv_tab2_title;
	int currentScrollLocation;
	ViewPager vp_tab2_rss;
	ListView lv_viewpager_sports, lv_viewpager_isoccer, lv_viewpager_csoccer,
			lv_viewpager_nba, lv_viewpager_cba, lv_viewpager_tennis,
			lv_viewpager_others;
	private TextView cursor;// 动画图片
	TextView tv_tab2_title1, tv_tab2_title2, tv_tab2_title3, tv_tab2_title4,
			tv_tab2_title5, tv_tab2_title6, tv_tab2_title7;// 页卡头标

	private int currIndex = 0;// 当前页卡编号
	int offset;
	int cursorWidth, titleWidth;
	float scale;// 单位转换相关

	RelativeLayout rl_tab3_login;
	LinearLayout ll_tab3_mybill;
	LinearLayout ll_tab3_matchbill;
	LinearLayout ll_tab3_mycard;
	LinearLayout ll_tab3_mymessage;
	TextView tv_tab3_newmessage;
	LinearLayout ll_tab3_mycollect;

	Ad1Handler ad1Handler;

	TeamlistHandler teamlistHandler;
	TeaminfoHandler teaminfoHandler;

	CheckNewVersionHandler checkNewVersionHandler;
	RssHandler rssHandler;
	RefreshRssHandler refreshRssHandler;
	UserinfoHandler userinfoHandler;
	TimeoutHandler timeoutHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		// Activity入栈
		AppManager.getAppManager().addActivity(this);

		// 信鸽初始化
		// XGPushConfig.enableDebug(this, true);
		Context context = getApplicationContext();
		XGPushManager.registerPush(context);
		// XGPushManager.registerPush(context,"account5");
		// XGPushManager.registerPush(context,"*");

		// oneapm初始化
		BlueWare.withApplicationToken("5D161BE7C5F3D3A78A54F9A45D8FB5F148")
				.start(this.getApplication());

		pool = Executors.newFixedThreadPool(10);

		// 默认位置信息为成都最中心
		Data.LONGITUDE = 104.071947;
		Data.LATITUDE = 30.66404;
		Data.CITY = "成都市";

		IntentFilter filter1 = new IntentFilter("RefreshTeamlist");
		registerReceiver(broadcast_team, filter1);

		IntentFilter filter2 = new IntentFilter("RefreshUserinfo");
		registerReceiver(broadcast_userinfo, filter2);

		initView();

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(MainActivity.this);
		Data.ISLOGIN = sp.getBoolean("isLogin", false);
		if (Data.ISLOGIN) {
			Data.USERID = sp.getString("UserID", "");
			XGPushManager.registerPush(context, "account" + Data.USERID);
			Data.PASSCODE = sp.getString("Passcode", "");
			Data.PHONE = sp.getString("Phone", "");
		} else {
			rl_tabteam_gifcontainer.setVisibility(View.GONE);
		}

		// 检查更新
		checkNewVersion();

		ad1Handler = new Ad1Handler();

		teamlistHandler = new TeamlistHandler();
		teaminfoHandler = new TeaminfoHandler();

		checkNewVersionHandler = new CheckNewVersionHandler();
		rssHandler = new RssHandler();
		refreshRssHandler = new RefreshRssHandler();
		userinfoHandler = new UserinfoHandler();
		timeoutHandler = new TimeoutHandler();

		new Thread(r_Ad1).start();
		new Thread(r_rss).start();

	}

	private void checkNewVersion() {
		// TODO Auto-generated method stub
		new Thread(r_CheckNewVersion).start();
	}

	void initView() {
		try {
			// init tabhost
			initTabHost();

			// init滚动广告栏
			pagerLayout = (LinearLayout) findViewById(R.id.view_pager_content);
			adViewPager = new ViewPager(this);
			adViewPager.setLayoutParams(new LayoutParams(Data.SCREENWIDTH,
					(int) (Data.SCREENWIDTH * 0.3)));
			pagerLayout.addView(adViewPager);

			initColorIcon();

			location();

			initNewsViewPager();

			ll_tab1_city = (LinearLayout) findViewById(R.id.ll_mainpage_tab1_city);
			ll_tab1_city.setOnClickListener(onClickListener);

			RelativeLayout rl_tab1_map = (RelativeLayout) findViewById(R.id.rl_mainpage_tab1_map);
			rl_tab1_map.setOnClickListener(onClickListener);

			RelativeLayout rl_searchbar = (RelativeLayout) findViewById(R.id.rl_mainpage_tab1_searchbar);
			rl_searchbar.setOnClickListener(onClickListener);

			init_gif();
			init_pop();
			TextView tv_fight = (TextView) findViewById(R.id.tv_mainpage_tabteam_fight);
			tv_fight.setOnClickListener(onClickListener);

			ll_tabteam_view1 = (LinearLayout) findViewById(R.id.ll_mainpage_tabteam_view1);
			ll_tabteam_view2 = (LinearLayout) findViewById(R.id.ll_mainpage_tabteam_view2);

			Button btn_tabteam_createteam = (Button) findViewById(R.id.btn_mainpage_tabteam_createteam);
			btn_tabteam_createteam.setOnClickListener(onClickListener);

			Button btn_tabteam_jointeam = (Button) findViewById(R.id.btn_mainpage_tabteam_jointeam);
			btn_tabteam_jointeam.setOnClickListener(onClickListener);

			mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_mainpage_tabteam_list);
			mPullToRefreshListView
					.setOnRefreshListener(new OnRefreshListener<ListView>() {
						@Override
						public void onRefresh(
								PullToRefreshBase<ListView> refreshView) {
							String label = DateUtils.formatDateTime(
									getApplicationContext(),
									System.currentTimeMillis(),
									DateUtils.FORMAT_SHOW_TIME
											| DateUtils.FORMAT_SHOW_DATE
											| DateUtils.FORMAT_ABBREV_ALL);

							// Update the LastUpdatedLabel
							refreshView.getLoadingLayoutProxy()
									.setLastUpdatedLabel(label);

							// Do work to refresh the list here.
							new GetDataTask().execute();
						}
					});

			mylist = new ArrayList<HashMap<String, Object>>();
			mylist.clear();
			lv_list = mPullToRefreshListView.getRefreshableView();
			init_list();
			lv_list.setDivider(null);
			footView = getLayoutInflater().inflate(
					R.layout.layout_courtlist_footview, null);

			ll_tab2_error = (LinearLayout) findViewById(R.id.ll_mainpage_tab2_error);

			scale = this.getResources().getDisplayMetrics().density;

			rl_tab3_login = (RelativeLayout) findViewById(R.id.rl_mainpage_tab3_login);
			rl_tab3_login.setOnClickListener(onClickListener);

			ll_tab3_mybill = (LinearLayout) findViewById(R.id.ll_mainpage_tab3_mybill);
			ll_tab3_mybill.setOnClickListener(onClickListener);

			ll_tab3_matchbill = (LinearLayout) findViewById(R.id.ll_mainpage_tab3_mymatchbill);
			ll_tab3_matchbill.setOnClickListener(onClickListener);

			ll_tab3_mycard = (LinearLayout) findViewById(R.id.ll_mainpage_tab3_mycard);
			ll_tab3_mycard.setOnClickListener(onClickListener);

			ll_tab3_mymessage = (LinearLayout) findViewById(R.id.ll_mainpage_tab3_mymessage);
			ll_tab3_mymessage.setOnClickListener(onClickListener);

			tv_tab3_newmessage = (TextView) findViewById(R.id.tv_mainpage_tab3_newmessage);

			ll_tab3_mycollect = (LinearLayout) findViewById(R.id.ll_mainpage_tab3_mycollect);
			ll_tab3_mycollect.setOnClickListener(onClickListener);

			LinearLayout ll_tab3_setting = (LinearLayout) findViewById(R.id.ll_mainpage_tab3_setting);
			ll_tab3_setting.setOnClickListener(onClickListener);

			LinearLayout ll_tab3_phone = (LinearLayout) findViewById(R.id.ll_mainpage_tab3_phone);
			ll_tab3_phone.setOnClickListener(onClickListener);

			LinearLayout ll_tab3_share = (LinearLayout) findViewById(R.id.ll_mainpage_tab3_share);
			ll_tab3_share.setOnClickListener(onClickListener);

			LinearLayout ll_tab3_feedback = (LinearLayout) findViewById(R.id.ll_mainpage_tab3_feedback);
			ll_tab3_feedback.setOnClickListener(onClickListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void init_gif() {
		GifView gif_loading = new GifView(this);
		gif_loading.setGifImage(R.drawable.gif_loading);
		gif_loading.setShowDimension((int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		gif_loading.setGifImageType(GifImageType.SYNC_DECODER);

		rl_tabteam_gifcontainer = (RelativeLayout) findViewById(R.id.rl_mainpage_tabteam_gifcontainer);
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
				(int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		p.addRule(RelativeLayout.CENTER_HORIZONTAL);
		p.setMargins(0, (int) (Data.SCREENHEIGHT * 0.22), 0, 0);
		rl_tabteam_gifcontainer.addView(gif_loading, p);

	}

	@SuppressWarnings("deprecation")
	void init_pop() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater
				.inflate(R.layout.layout_mainpage_tabteam_pop, null);
		pop_tabteam_more = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		pop_tabteam_more.setBackgroundDrawable(new BitmapDrawable());
		pop_tabteam_more.setOutsideTouchable(true);
		pop_tabteam_more.setFocusable(true);

		LinearLayout ll_tabteam_pop = (LinearLayout) findViewById(R.id.ll_mainpage_tabteam_more);
		ll_tabteam_pop.setOnClickListener(onClickListener);

		TextView tv_createteam = (TextView) view
				.findViewById(R.id.tv_mainpage_tabteam_createteam);
		tv_createteam.setOnClickListener(onClickListener);

		TextView tv_discoverteam = (TextView) view
				.findViewById(R.id.tv_mainpage_tabteam_discoverteam);
		tv_discoverteam.setOnClickListener(onClickListener);

	}

	void location() {
		// 定位
		aMapLocManager = LocationManagerProxy.getInstance(this);

		/*
		 * mAMapLocManager.setGpsEnable(false);//
		 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
		 * API定位采用GPS和网络混合定位方式
		 * ，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
		 */

		city = "正在定位...";
		this.aMapLocation = null;

		aMapLocManager.requestLocationUpdates(
				LocationProviderProxy.AMapNetwork, 2000, 10, this);
		handler.postDelayed(this, 5000);// 设置超过12秒还没有定位到就停止定位

	}

	@SuppressWarnings("deprecation")
	void initTabHost() {
		final TabHost tabHost = (TabHost) findViewById(R.id.tabhost);

		tabHost.setup();
		tabHost.addTab(tabHost
				.newTabSpec("tab1")
				.setIndicator("",
						getResources().getDrawable(R.drawable.selector_tab1))
				.setContent(R.id.ll_mainpage_view1));

		tabHost.addTab(tabHost
				.newTabSpec("tabteam")
				.setIndicator("",
						getResources().getDrawable(R.drawable.selector_tabteam))
				.setContent(R.id.rl_mainpage_viewteam));

		tabHost.addTab(tabHost
				.newTabSpec("tab2")
				.setIndicator("",
						getResources().getDrawable(R.drawable.selector_tab2))
				.setContent(R.id.rl_mainpage_view2));

		tabHost.addTab(tabHost
				.newTabSpec("tab3")
				.setIndicator("",
						getResources().getDrawable(R.drawable.selector_tab3))
				.setContent(R.id.ll_mainpage_view3));

		final TabWidget tabWidget = tabHost.getTabWidget();

		tabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
				// TODO Auto-generated method stub
				if (tabId.equals("tab3")) {
					if (Data.ISLOGIN) {
						// 每次进入个人信息页都重新获取个人信息，主要为了在其他地方登录后及时更新UI
						TextView tv_tab3_title = (TextView) findViewById(R.id.tv_mainpage_tab3_title);
						tv_tab3_title.setText("加载中...");

						TextView tv_tab3_content = (TextView) findViewById(R.id.tv_mainpage_tab3_content);
						tv_tab3_content.setText(Data.PHONE);

						TextView tv_tab3_introduction = (TextView) findViewById(R.id.tv_mainpage_tab3_introduction);
						tv_tab3_introduction.setVisibility(View.VISIBLE);
						tv_tab3_introduction.setText("个人简介：...");

						ImageView iv_tab3_money = (ImageView) findViewById(R.id.iv_mainpage_tab3_money);
						iv_tab3_money.setVisibility(View.INVISIBLE);
						TextView tv_tab3_money = (TextView) findViewById(R.id.tv_mainpage_tab3_money);
						tv_tab3_money.setVisibility(View.VISIBLE);
						tv_tab3_money.setText("...");

						ImageView iv_tab3_redbag = (ImageView) findViewById(R.id.iv_mainpage_tab3_redbag);
						iv_tab3_redbag.setVisibility(View.INVISIBLE);
						TextView tv_tab3_redbag = (TextView) findViewById(R.id.tv_mainpage_tab3_redbag);
						tv_tab3_redbag.setVisibility(View.VISIBLE);
						tv_tab3_redbag.setText("...");

						ImageView iv_tab3_mark = (ImageView) findViewById(R.id.iv_mainpage_tab3_mark);
						iv_tab3_mark.setVisibility(View.INVISIBLE);
						TextView tv_tab3_mark = (TextView) findViewById(R.id.tv_mainpage_tab3_mark);
						tv_tab3_mark.setVisibility(View.VISIBLE);
						tv_tab3_mark.setText("...");

						rl_tab3_login.setOnClickListener(null);
						ll_tab3_mybill.setOnClickListener(null);
						ll_tab3_matchbill.setOnClickListener(null);
						ll_tab3_mycard.setOnClickListener(null);
						ll_tab3_mymessage.setOnClickListener(null);
						ll_tab3_mycollect.setOnClickListener(null);

						new Thread(r_Userinfo).start();
					} else {
						TextView tv_tab3_title = (TextView) findViewById(R.id.tv_mainpage_tab3_title);
						tv_tab3_title.setText("登录/注册");

						TextView tv_tab3_content = (TextView) findViewById(R.id.tv_mainpage_tab3_content);
						tv_tab3_content.setText("登录后会发现更多惊喜哦！");

						TextView tv_tab3_introduction = (TextView) findViewById(R.id.tv_mainpage_tab3_introduction);
						tv_tab3_introduction.setVisibility(View.GONE);

						ImageView iv_tab3_money = (ImageView) findViewById(R.id.iv_mainpage_tab3_money);
						iv_tab3_money.setVisibility(View.VISIBLE);
						TextView tv_tab3_money = (TextView) findViewById(R.id.tv_mainpage_tab3_money);
						tv_tab3_money.setVisibility(View.INVISIBLE);

						ImageView iv_tab3_redbag = (ImageView) findViewById(R.id.iv_mainpage_tab3_redbag);
						iv_tab3_redbag.setVisibility(View.VISIBLE);
						TextView tv_tab3_redbag = (TextView) findViewById(R.id.tv_mainpage_tab3_redbag);
						tv_tab3_redbag.setVisibility(View.INVISIBLE);

						ImageView iv_tab3_mark = (ImageView) findViewById(R.id.iv_mainpage_tab3_mark);
						iv_tab3_mark.setVisibility(View.VISIBLE);
						TextView tv_tab3_mark = (TextView) findViewById(R.id.tv_mainpage_tab3_mark);
						tv_tab3_mark.setVisibility(View.INVISIBLE);
					}
				} else if (tabId.equals("tabteam")) {
					if (Data.ISLOGIN && !isTeamRefresh) {
						// 此时刷新
						TextView tv_error = (TextView) findViewById(R.id.tv_mainpage_tabteam_error);
						tv_error.setVisibility(View.GONE);
						rl_tabteam_gifcontainer.setVisibility(View.VISIBLE);
						currentPage = 0;
						sumPage = 0;
						hasMore = true;
						new Thread(r_TeamList).start();
					} else if (!Data.ISLOGIN) {
						rl_tabteam_gifcontainer.setVisibility(View.GONE);
						ll_tabteam_view1.setVisibility(View.VISIBLE);
						ll_tabteam_view2.setVisibility(View.GONE);
					}
				}
			}
		});

		for (int currentTab = 0; currentTab < tabWidget.getChildCount(); currentTab++) {
			View v = tabWidget.getChildAt(currentTab);
			v.setBackgroundDrawable(getResources().getDrawable(R.color.white));
		}

	}

	// 首页色块初始化
	void initColorIcon() {
		// 初始化相对布局的参数，用于磁贴的点击效果
		p1 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		p1.setMargins(10, 10, 10, 10);
		p2 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		LinearLayout ll_tab1_firstrow = (LinearLayout) findViewById(R.id.ll_mainpage_tab1_firstrow);
		LayoutParams p = ll_tab1_firstrow.getLayoutParams();
		p.height = (int) (Data.SCREENWIDTH * 0.30);
		ll_tab1_firstrow.setLayoutParams(p);

		LinearLayout ll_tab1_secondrow = (LinearLayout) findViewById(R.id.ll_mainpage_tab1_secondrow);
		p = ll_tab1_secondrow.getLayoutParams();
		p.height = (int) (Data.SCREENWIDTH * 0.30);
		ll_tab1_secondrow.setLayoutParams(p);

		LinearLayout ll_tab1_thridrow = (LinearLayout) findViewById(R.id.ll_mainpage_tab1_thridrow);
		p = ll_tab1_thridrow.getLayoutParams();
		p.height = (int) (Data.SCREENWIDTH * 0.30);
		ll_tab1_thridrow.setLayoutParams(p);

		LinearLayout ll_tab1_fourthrow = (LinearLayout) findViewById(R.id.ll_mainpage_tab1_fourthrow);
		p = ll_tab1_fourthrow.getLayoutParams();
		p.height = (int) (Data.SCREENWIDTH * 0.30);
		ll_tab1_fourthrow.setLayoutParams(p);

		// 磁贴点击效果
		iv_soccer = (ImageView) findViewById(R.id.iv_mainpage_tab1_soccer);
		iv_soccer.setOnTouchListener(this);

		iv_basketball = (ImageView) findViewById(R.id.iv_mainpage_tab1_basketball);
		// iv_basketball.setOnTouchListener(this);

		iv_volleyball = (ImageView) findViewById(R.id.iv_mainpage_tab1_volleyball);
		// iv_volleyball.setOnTouchListener(this);

		iv_tabletennis = (ImageView) findViewById(R.id.iv_mainpage_tab1_tabletennis);
		// iv_tabletennis.setOnTouchListener(this);

		iv_tennis = (ImageView) findViewById(R.id.iv_mainpage_tab1_tennis);
		iv_tennis.setOnTouchListener(this);

		iv_badminton = (ImageView) findViewById(R.id.iv_mainpage_tab1_badminton);
		iv_badminton.setOnTouchListener(this);

		iv_billiards = (ImageView) findViewById(R.id.iv_mainpage_tab1_billiards);
		// iv_billiards.setOnTouchListener(this);

		iv_swimming = (ImageView) findViewById(R.id.iv_mainpage_tab1_swimming);
		iv_swimming.setOnTouchListener(this);

		iv_bodybuilding = (ImageView) findViewById(R.id.iv_mainpage_tab1_bodybuilding);
		// iv_bodybuilding.setOnTouchListener(this);

		iv_golf = (ImageView) findViewById(R.id.iv_mainpage_tab1_golf);
		// iv_golf.setOnTouchListener(this);

		iv_xgames = (ImageView) findViewById(R.id.iv_mainpage_tab1_xgames);
		// iv_xgames.setOnTouchListener(this);

		iv_moresports = (ImageView) findViewById(R.id.iv_mainpage_tab1_moresports);
		// iv_moresports.setOnTouchListener(this);

		int w = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		iv_soccer.measure(w, h);
		int height = iv_soccer.getMeasuredWidth();

	}

	private void initNewsViewPager() {
		// TODO Auto-generated method stub
		vp_tab2_rss = (ViewPager) findViewById(R.id.vp_mainpage_tab2_rss);

		// 将要分页显示的View装入数组中
		LayoutInflater mLi = LayoutInflater.from(this);
		View view1 = mLi.inflate(R.layout.viewpager_sports, null);
		View view2 = mLi.inflate(R.layout.viewpager_isoccer, null);
		View view3 = mLi.inflate(R.layout.viewpager_csoccer, null);
		View view4 = mLi.inflate(R.layout.viewpager_nba, null);
		View view5 = mLi.inflate(R.layout.viewpager_cba, null);
		View view6 = mLi.inflate(R.layout.viewpager_tennis, null);
		View view7 = mLi.inflate(R.layout.viewpager_others, null);

		lv1 = (PullToRefreshListView) view1
				.findViewById(R.id.lv_mainpage_viewpager_sports);
		lv_viewpager_sports = lv1.getRefreshableView();
		lv1.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(
						getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				// Do work to refresh the list here.
				new GetDataTaskRss(0).execute();
			}
		});

		lv2 = (PullToRefreshListView) view2
				.findViewById(R.id.lv_mainpage_viewpager_isoccer);
		lv_viewpager_isoccer = lv2.getRefreshableView();
		lv2.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(
						getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				// Do work to refresh the list here.
				new GetDataTaskRss(1).execute();
			}
		});

		lv3 = (PullToRefreshListView) view3
				.findViewById(R.id.lv_mainpage_viewpager_csoccer);
		lv_viewpager_csoccer = lv3.getRefreshableView();
		lv3.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(
						getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				// Do work to refresh the list here.
				new GetDataTaskRss(2).execute();
			}
		});

		lv4 = (PullToRefreshListView) view4
				.findViewById(R.id.lv_mainpage_viewpager_nba);
		lv_viewpager_nba = lv4.getRefreshableView();
		lv4.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(
						getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				// Do work to refresh the list here.
				new GetDataTaskRss(3).execute();
			}
		});

		lv5 = (PullToRefreshListView) view5
				.findViewById(R.id.lv_mainpage_viewpager_cba);
		lv_viewpager_cba = lv5.getRefreshableView();
		lv5.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(
						getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				// Do work to refresh the list here.
				new GetDataTaskRss(4).execute();
			}
		});

		lv6 = (PullToRefreshListView) view6
				.findViewById(R.id.lv_mainpage_viewpager_tennis);
		lv_viewpager_tennis = lv6.getRefreshableView();
		lv6.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(
						getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				// Do work to refresh the list here.
				new GetDataTaskRss(5).execute();
			}
		});

		lv7 = (PullToRefreshListView) view7
				.findViewById(R.id.lv_mainpage_viewpager_others);
		lv_viewpager_others = lv7.getRefreshableView();
		lv7.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(
						getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				// Do work to refresh the list here.
				new GetDataTaskRss(6).execute();
			}
		});

		// 每个页面的Title数据
		final ArrayList<View> views = new ArrayList<View>();
		views.add(view1);
		views.add(view2);
		views.add(view3);
		views.add(view4);
		views.add(view5);
		views.add(view6);
		views.add(view7);

		final ArrayList<String> titles = new ArrayList<String>();
		titles.add("体育要闻");
		titles.add("国际足球");
		titles.add("中国足球");
		titles.add("NBA");
		titles.add("CBA");
		titles.add("网球");
		titles.add("综合体育");

		// 填充ViewPager的数据适配器
		PagerAdapter mPagerAdapter = new PagerAdapter() {

			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}

			@Override
			public int getCount() {
				return views.size();
			}

			@Override
			public void destroyItem(View container, int position, Object object) {
				((ViewPager) container).removeView(views.get(position));
			}

			@Override
			public CharSequence getPageTitle(int position) {
				return titles.get(position);
			}

			@Override
			public Object instantiateItem(View container, int position) {
				((ViewPager) container).addView(views.get(position));
				return views.get(position);
			}
		};

		hsv_tab2_title = (HorizontalScrollView) findViewById(R.id.hsv_mainpage_tab2_title);
		currentScrollLocation = Data.SCREENWIDTH;

		vp_tab2_rss.setAdapter(mPagerAdapter);
		vp_tab2_rss.setOnPageChangeListener(new MyOnPageChangeListener());

		tv_tab2_title1 = (TextView) findViewById(R.id.tv_mainpage_tab2_sports);
		tv_tab2_title2 = (TextView) findViewById(R.id.tv_mainpage_tab2_isoccer);
		tv_tab2_title3 = (TextView) findViewById(R.id.tv_mainpage_tab2_csoccer);
		tv_tab2_title4 = (TextView) findViewById(R.id.tv_mainpage_tab2_nba);
		tv_tab2_title5 = (TextView) findViewById(R.id.tv_mainpage_tab2_cba);
		tv_tab2_title6 = (TextView) findViewById(R.id.tv_mainpage_tab2_tennis);
		tv_tab2_title7 = (TextView) findViewById(R.id.tv_mainpage_tab2_others);

		tv_tab2_title1.setOnClickListener(new MyOnClickListener(0));
		tv_tab2_title2.setOnClickListener(new MyOnClickListener(1));
		tv_tab2_title3.setOnClickListener(new MyOnClickListener(2));
		tv_tab2_title4.setOnClickListener(new MyOnClickListener(3));
		tv_tab2_title5.setOnClickListener(new MyOnClickListener(4));
		tv_tab2_title6.setOnClickListener(new MyOnClickListener(5));
		tv_tab2_title7.setOnClickListener(new MyOnClickListener(6));

		cursor = (TextView) findViewById(R.id.tv_mainpage_tab2_cursor);

		int w = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		cursor.measure(w, h);
		cursorWidth = cursor.getMeasuredWidth();
		hsv_tab2_title.measure(w, h);
		titleWidth = hsv_tab2_title.getMeasuredWidth();

	}

	// 这是首页广告viewpager相关的
	private void initViewPager(int picNum) {

		pageViews = new ArrayList<View>();
		ImageView iv = null;
		for (int i = 0; i < picNum; i++) {
			iv = new ImageView(this);
			iv.setImageResource(R.color.lightGrey);
			iv.setScaleType(ScaleType.FIT_XY);

			try {
				// 异步加载图片
				new AsyncViewTask(MainActivity.this,
						json_adinfo.getString("Path" + i), iv, 2).execute(iv);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			pageViews.add(iv);
		}

		adapter = new AdPageAdapter(pageViews);

		initCirclePoint();

		adViewPager.setAdapter(adapter);
		adViewPager.setOnPageChangeListener(new AdPageChangeListener());

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (isContinue) {
						viewHandler.sendEmptyMessage(atomicInteger.get());
						atomicOption();
					}
				}
			}
		}).start();
	}

	private void atomicOption() {
		atomicInteger.incrementAndGet();
		if (atomicInteger.get() > imageViews.length - 1) {
			atomicInteger.getAndAdd(-pageViews.size());
		}
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {

		}
	}

	@SuppressLint("HandlerLeak")
	private final Handler viewHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			adViewPager.setCurrentItem(msg.what);
			super.handleMessage(msg);
		}

	};

	private void initCirclePoint() {
		ViewGroup group = (ViewGroup) findViewById(R.id.viewGroup);
		imageViews = new ImageView[pageViews.size()];
		for (int i = 0; i < pageViews.size(); i++) {
			imageView = new ImageView(this);
			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(15, 15);
			p.setMargins(0, 0, 10, 0);
			imageView.setLayoutParams(p);
			imageViews[i] = imageView;

			if (i == 0) {
				imageViews[i].setBackgroundResource(R.drawable.point_focused);
			} else {
				imageViews[i].setBackgroundResource(R.drawable.point_unfocused);
			}
			group.addView(imageViews[i]);
		}
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
			atomicInteger.getAndSet(arg0);
			for (int i = 0; i < imageViews.length; i++) {
				imageViews[arg0]
						.setBackgroundResource(R.drawable.point_focused);
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

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.iv_mainpage_tab1_soccer
				|| v.getId() == R.id.iv_mainpage_tab1_tennis
				|| v.getId() == R.id.iv_mainpage_tab1_swimming
				|| v.getId() == R.id.iv_mainpage_tab1_badminton)
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				v.setLayoutParams(p1);
			} else if (event.getAction() == MotionEvent.ACTION_UP) {

				v.setLayoutParams(p2);
				Intent intent = new Intent(MainActivity.this,
						CourtlistActivity.class);
				switch (v.getId()) {
				case R.id.iv_mainpage_tab1_soccer:
					intent.putExtra("sports", 1);
					break;
				/*
				 * case R.id.iv_mainpage_tab1_basketball:
				 * intent.putExtra("sports", 2); break; case
				 * R.id.iv_mainpage_tab1_volleyball: intent.putExtra("sports",
				 * 3); break; case R.id.iv_mainpage_tab1_tabletennis:
				 * intent.putExtra("sports", 4); break;
				 */
				case R.id.iv_mainpage_tab1_tennis:
					intent.putExtra("sports", 5);
					break;
				case R.id.iv_mainpage_tab1_badminton:
					intent.putExtra("sports", 6);
					break;
				/*
				 * case R.id.iv_mainpage_tab1_billiards:
				 * intent.putExtra("sports", 7); break;
				 */
				case R.id.iv_mainpage_tab1_swimming:
					intent.putExtra("sports", 8);
					break;
				/*
				 * case R.id.iv_mainpage_tab1_bodybuilding:
				 * intent.putExtra("sports", 9); break; case
				 * R.id.iv_mainpage_tab1_xgames: intent.putExtra("sports", 10);
				 * break; case R.id.iv_mainpage_tab1_golf:
				 * intent.putExtra("sports", 11); break; case
				 * R.id.iv_mainpage_tab1_moresports: intent.putExtra("sports",
				 * 12); break;
				 */
				}
				startActivity(intent);
			} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
				v.setLayoutParams(p2);
			}

		return true;
	}

	private RSSFeed getFeed(String urlString) // 该方法通过url获得xml并解析xml内容为RSSFeed对象
	{
		try {
			URL url = new URL(urlString);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			XMLReader xmlreader = parser.getXMLReader();
			RSSHandler rssHandler = new RSSHandler();
			xmlreader.setContentHandler(rssHandler);

			// GBK
			/*
			 * InputStream stream = null; stream = url.openStream(); //
			 * 通过InputStreamReader设定编码方式 InputStreamReader streamReader = new
			 * InputStreamReader(stream, "GBK"); InputSource inputSource = new
			 * InputSource(streamReader); xmlreader.parse(inputSource);
			 */

			// UTF-8
			InputSource is = new InputSource(url.openStream());
			xmlreader.parse(is);
			return rssHandler.getFeed();
		} catch (Exception e) {
			Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG)
					.show();
			return null;
		}
	}

	private void showListView(ListView lv_rss, final RSSFeed feed) {
		if (feed == null) {
			Toast.makeText(MainActivity.this, "访问RSS无效", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		SimpleAdapter adapter = new SimpleAdapter(MainActivity.this,
				feed.getAllItemsForListView(), R.layout.item_news,
				new String[] { RSSItem.TITLE, RSSItem.PUBDATE }, new int[] {
						R.id.tv_newsitem_title, R.id.tv_newsitem_time });
		lv_rss.setAdapter(adapter); // listview绑定适配器
		lv_rss.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				/*
				 * Toast.makeText(MainActivity.this,
				 * feed.getAllItems().get(position).getLink(),
				 * Toast.LENGTH_SHORT).show();
				 */
				Intent intent = new Intent(MainActivity.this,
						NewsActivity.class);
				intent.putExtra("link", feed.getAllItems().get(position)
						.getLink());
				startActivity(intent);
			}
		}); // 设置itemclick事件代理

	}

	void initCityDialog() {
		final AlertDialog alertDialog = new AlertDialog.Builder(
				MainActivity.this).create();
		alertDialog.setCanceledOnTouchOutside(true);
		alertDialog.show();
		Window window = alertDialog.getWindow();
		window.setContentView(R.layout.dialog_mainpage_selectcity);

		WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
		lp.width = (int) (Data.SCREENWIDTH * 0.6);// 定义宽度
		lp.height = (int) (Data.SCREENHEIGHT * 0.5);// 定义高度

		alertDialog.getWindow().setAttributes(lp);

		TextView tv_location = (TextView) window
				.findViewById(R.id.tv_citydialog_location);
		// 这里是定位
		if (city.equals("成都市")) {
			tv_location.setText(city);
		} else {
			if (isLocationSuccessful)
				tv_location.setText(city + "（暂未开通）");
			else
				tv_location.setText(city);
		}

		RelativeLayout rl_location = (RelativeLayout) window
				.findViewById(R.id.rl_citydialog_location);
		rl_location.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 这里应该加一条判断此城市是否开通
				if (isLocationSuccessful) {
					if (city.equals("成都市"))
						alertDialog.dismiss();
					else
						Toast.makeText(MainActivity.this, city + "暂未开通喔，敬请期待",
								Toast.LENGTH_SHORT).show();
				} else {
					location();
					alertDialog.dismiss();
				}
			}
		});

		ListView lv_citylist = (ListView) window
				.findViewById(R.id.lv_citydialog_citylist);
		ArrayList<HashMap<String, Object>> mylist = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("city", "成都市");
		mylist.add(map);

		SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, mylist,
				R.layout.item_citydialog, new String[] { "city" },
				new int[] { R.id.tv_citydialog_city });
		lv_citylist.setAdapter(adapter);
		lv_citylist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
			}

		});
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent;
			switch (v.getId()) {
			case R.id.ll_mainpage_tab1_city:
				initCityDialog();
				break;
			case R.id.rl_mainpage_tab1_searchbar:
				startActivity(new Intent(MainActivity.this,
						SearchActivity.class));
				break;
			case R.id.rl_mainpage_tab1_map:
				intent = new Intent(MainActivity.this, NearbyMapActivity.class);
				intent.putExtra("sports", 0);
				startActivity(intent);
				break;
			case R.id.btn_mainpage_tabteam_createteam:
				if (!Data.ISLOGIN) {
					intent = new Intent(MainActivity.this, LoginActivity.class);
					startActivityForResult(intent, 103);
				} else {
					intent = new Intent(MainActivity.this,
							CreateTeamActivity.class);
					startActivity(intent);
				}
				break;
			case R.id.btn_mainpage_tabteam_jointeam:
				if (!Data.ISLOGIN) {
					intent = new Intent(MainActivity.this, LoginActivity.class);
					startActivityForResult(intent, 103);
				} else {
					intent = new Intent(MainActivity.this,
							DiscoverteamActivity.class);
					startActivity(intent);
				}
				break;
			case R.id.ll_mainpage_tabteam_more:
				pop_tabteam_more.showAsDropDown(v);
				break;
			case R.id.tv_mainpage_tabteam_createteam:
				pop_tabteam_more.dismiss();
				if (!Data.ISLOGIN) {
					intent = new Intent(MainActivity.this, LoginActivity.class);
					startActivityForResult(intent, 103);
				} else {
					intent = new Intent(MainActivity.this,
							CreateTeamActivity.class);
					startActivity(intent);
				}
				break;
			case R.id.tv_mainpage_tabteam_discoverteam:
				pop_tabteam_more.dismiss();
				if (!Data.ISLOGIN) {
					intent = new Intent(MainActivity.this, LoginActivity.class);
					startActivityForResult(intent, 103);
				} else {
					intent = new Intent(MainActivity.this,
							DiscoverteamActivity.class);
					startActivity(intent);
				}
				break;
			case R.id.tv_mainpage_tabteam_fight:
				if (!Data.ISLOGIN) {
					intent = new Intent(MainActivity.this, LoginActivity.class);
					startActivityForResult(intent, 103);
				} else {
					intent = new Intent(MainActivity.this,
							FightlistActivity.class);
					startActivity(intent);
				}
				break;
			case R.id.rl_mainpage_tab3_login:
				if (!Data.ISLOGIN) {
					intent = new Intent(MainActivity.this, LoginActivity.class);
					startActivityForResult(intent, 100);
				} else {
					intent = new Intent(MainActivity.this, MyinfoActivity.class);
					startActivityForResult(intent, 101);
				}
				break;
			case R.id.ll_mainpage_tab3_mybill:
				if (!Data.ISLOGIN) {
					intent = new Intent(MainActivity.this, LoginActivity.class);
					startActivityForResult(intent, 100);
				} else {
					intent = new Intent(MainActivity.this, MybillActivity.class);
					intent.putExtra("UserID", Data.USERID);
					intent.putExtra("Passcode", Data.PASSCODE);
					startActivity(intent);
				}
				break;
			case R.id.ll_mainpage_tab3_mymatchbill:
				if (!Data.ISLOGIN) {
					intent = new Intent(MainActivity.this, LoginActivity.class);
					startActivityForResult(intent, 100);
				} else {
					intent = new Intent(MainActivity.this,
							MatchlistActivity.class);
					intent.putExtra("UserID", Data.USERID);
					intent.putExtra("Passcode", Data.PASSCODE);
					startActivity(intent);
				}
				break;
			case R.id.ll_mainpage_tab3_mycard:
				if (!Data.ISLOGIN) {
					intent = new Intent(MainActivity.this, LoginActivity.class);
					startActivityForResult(intent, 100);
				} else {
					intent = new Intent(MainActivity.this, MycardActivity.class);
					intent.putExtra("UserID", Data.USERID);
					intent.putExtra("Passcode", Data.PASSCODE);
					startActivity(intent);
				}
				break;
			case R.id.ll_mainpage_tab3_mymessage:
				if (!Data.ISLOGIN) {
					intent = new Intent(MainActivity.this, LoginActivity.class);
					startActivityForResult(intent, 100);
				} else {
					tv_tab3_newmessage.setVisibility(View.GONE);
					intent = new Intent(MainActivity.this,
							MymessageActivity.class);
					startActivity(intent);
				}
				break;
			case R.id.ll_mainpage_tab3_mycollect:
				if (!Data.ISLOGIN) {
					intent = new Intent(MainActivity.this, LoginActivity.class);
					startActivityForResult(intent, 100);
				} else {
					intent = new Intent(MainActivity.this,
							MycollectActivity.class);
					startActivity(intent);
				}
				break;
			case R.id.ll_mainpage_tab3_phone:
				Uri uri = Uri.parse("tel:" + "02885570071");
				intent = new Intent(Intent.ACTION_DIAL, uri);
				System.out.println(intent.toUri(Intent.URI_INTENT_SCHEME));
				startActivity(intent);
				break;
			case R.id.ll_mainpage_tab3_share:
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, getResources()
						.getString(R.string.share));
				sendIntent.setType("text/plain");
				startActivity(Intent.createChooser(sendIntent, getResources()
						.getText(R.string.send_to)));
				break;
			case R.id.ll_mainpage_tab3_setting:
				intent = new Intent(MainActivity.this, SettingActivity.class);
				startActivityForResult(intent, 102);
				break;
			case R.id.ll_mainpage_tab3_feedback:
				intent = new Intent(MainActivity.this, FeedbackActivity.class);
				startActivity(intent);
				break;
			}
		}
	};

	void init_list() {
		try {
			teamAdapter = new TeamlistAdapter(MainActivity.this, mylist,
					R.layout.item_team, new String[] { "teamlogo", "teamtype",
							"teamname", "teamleadername", "teammembernum",
							"teamrate", "teamratenum" }, new int[] {
							R.id.iv_teamitem_logo, R.id.iv_teamitem_teamtype,
							R.id.tv_teamitem_teamname,
							R.id.tv_teamitem_leadername,
							R.id.tv_teamitem_membernum, R.id.tv_teamitem_rate,
							R.id.tv_teamitem_ratenum });

			lv_list.setAdapter(teamAdapter);
			lv_list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub

					Intent intent = new Intent(MainActivity.this,
							TeamInfoActivity.class);
					try {
						intent.putExtra("ID",
								teamID.getInt("TeamID" + (position - 1)) + "");
						intent.putExtra("info",
								allTeamInfo.getString("info" + (position - 1)));
						intent.putExtra("isMyTeam", true);

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					startActivity(intent);

				}

			});

			lv_list.setOnScrollListener(new OnScrollListener() {

				@Override
				public void onScrollStateChanged(AbsListView view,
						int scrollState) {
					// TODO Auto-generated method stub
					// 当不滚动时
					if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
						// 判断是否滚动到底部
						if (view.getLastVisiblePosition() == view.getCount() - 1) {
							// 加载更多功能的代码
							if (hasMore) {
								lv_list.addFooterView(footView);
								new Thread(r_TeamInfo).start();
							} else {
								lv_list.removeFooterView(footView);
							}
						}
					}
				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					// TODO Auto-generated method stub

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 一些在刷新的时候做的事情
	private class GetDataTask extends AsyncTask<Void, Void, String[]> {

		@Override
		protected String[] doInBackground(Void... params) {
			// Simulates a background job.
			currentPage = 0;
			sumPage = 0;
			hasMore = true;
			new Thread(r_TeamList).start();
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {

			// Call onRefreshComplete when the list has been refreshed.

			super.onPostExecute(result);
		}
	}

	private class GetDataTaskRss extends AsyncTask<Void, Void, String[]> {

		int type;

		GetDataTaskRss(int type) {
			this.type = type;
		}

		@Override
		protected String[] doInBackground(Void... params) {
			// Simulates a background job.
			new RefreshRssThread(type).start();
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {

			// Call onRefreshComplete when the list has been refreshed.

			super.onPostExecute(result);
		}
	}

	Runnable r_CheckNewVersion = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String result = new HttpGetConnection().httpConnection("GetUpdate");

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			checkNewVersionHandler.sendMessage(msg);
		}
	};

	Runnable r_Ad1 = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				String result = new HttpGetConnection()
						.httpConnection("GetHeadPic");
				System.out.println(result);
				if (result.equals("timeout")) {
					Message msg = new Message();
					Bundle b = new Bundle();
					msg.setData(b);
					timeoutHandler.sendMessage(msg);
				} else {

					json_adinfo = new JSONObject(result);

					int picNum = json_adinfo.getInt("PicNum");

					Message msg = new Message();
					Bundle b = new Bundle();
					b.putInt("PicNum", picNum);
					msg.setData(b);
					ad1Handler.sendMessage(msg);

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	Runnable r_TeamList = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "MyTeam"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));

			String result = new HttpPostConnection("TeamServer", params)
					.httpConnection();

			System.out.println(result);

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			teamlistHandler.sendMessage(msg);
		}
	};

	Runnable r_TeamInfo = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			teamLoadNum = 0;
			teamInfo = new JSONObject();

			int j;
			if (currentPage < sumPage)
				j = 10;
			else {
				j = lastPageItemNum;
				hasMore = false;
			}

			for (int i = 0; i < j; i++) {
				int k = currentPage * 10 + i;
				RequestEachTeamThread thread = new RequestEachTeamThread(
						teamID, k, j, i);
				pool.execute(thread);
			}

			currentPage++;
		}
	};

	class RequestEachTeamThread extends Thread {

		JSONObject teamID;
		int i;
		int num;
		int order;

		RequestEachTeamThread(JSONObject teamID, int i, int num, int order) {
			this.teamID = teamID;
			this.i = i;
			this.num = num;
			this.order = order;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();

			try {
				Message msg = new Message();
				Bundle b = new Bundle();

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("Request", "TeamInfo"));
				params.add(new BasicNameValuePair("TeamID", teamID
						.getInt("TeamID" + i) + ""));

				String result = new HttpPostConnection("TeamServer", params)
						.httpConnection();

				System.out.println(result);

				if (result.equals("timeout")) {
					msg.setData(b);
					timeoutHandler.sendMessage(msg);
					return;
				}

				allTeamInfo.put("info" + i, result);

				JSONObject json = new JSONObject(result);
				b.putString("info", json.toString());
				b.putInt("i", order);
				b.putInt("num", num);
				msg.setData(b);
				teaminfoHandler.sendMessage(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	Runnable r_rss = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Bundle b = new Bundle();
			try {
				feedSports = getFeed(RSS_SPORTS);// 调用getFeed方法,从服务器取得rss提要
				feedIsoccer = getFeed(RSS_ISOCCER);
				feedCsoccer = getFeed(RSS_CSOCCER);
				feedNBA = getFeed(RSS_NBA);
				feedCBA = getFeed(RSS_CBA);
				feedTennis = getFeed(RSS_TENNIS);
				feedOthers = getFeed(RSS_OTHERS);

				b.putString("result", "success");

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				b.putString("result", "error");
			}
			Message msg = new Message();
			msg.setData(b);
			rssHandler.sendMessage(msg);
		}
	};

	class RefreshRssThread extends Thread {

		int type;

		public RefreshRssThread(int type) {
			this.type = type;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub

			Bundle b = new Bundle();
			try {
				switch (type) {
				case 0:
					feedSports = getFeed(RSS_SPORTS);
					break;
				case 1:
					feedIsoccer = getFeed(RSS_ISOCCER);
					break;
				case 2:
					feedIsoccer = getFeed(RSS_CSOCCER);
					break;
				case 3:
					feedIsoccer = getFeed(RSS_NBA);
					break;
				case 4:
					feedIsoccer = getFeed(RSS_CBA);
					break;
				case 5:
					feedIsoccer = getFeed(RSS_TENNIS);
					break;
				case 6:
					feedIsoccer = getFeed(RSS_OTHERS);
					break;
				}

				b.putString("result", "success");

			} catch (Exception e) {
				e.printStackTrace();
				b.putString("result", "error");
			}

			b.putInt("type", type);

			Message msg = new Message();
			msg.setData(b);
			refreshRssHandler.sendMessage(msg);

		}
	}

	Runnable r_Userinfo = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "MyInfo"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));

			String result = new HttpPostConnection("UserInfoServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			userinfoHandler.sendMessage(msg);

		}
	};

	class CheckNewVersionHandler extends Handler {
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();
			String result = b.getString("result");

			if (result.equals("timeout")) {
				return;
			}

			try {

				PackageManager manager = MainActivity.this.getPackageManager();
				PackageInfo info = manager.getPackageInfo(
						MainActivity.this.getPackageName(), 0);
				String version = info.versionName;

				final JSONObject json = new JSONObject(result);

				if (!json.getString("Version").equals(version)) {
					final AlertDialog alertDialog = new AlertDialog.Builder(
							MainActivity.this).create();
					alertDialog.setCanceledOnTouchOutside(false);
					alertDialog.show();
					Window window = alertDialog.getWindow();
					window.setContentView(R.layout.dialog_newversion);

					WindowManager.LayoutParams lp = alertDialog.getWindow()
							.getAttributes();
					lp.width = (int) (Data.SCREENWIDTH * 0.75);// 定义宽度
					lp.height = (int) (Data.SCREENHEIGHT * 0.7);// 定义高度

					alertDialog.getWindow().setAttributes(lp);

					TextView tv_version = (TextView) window
							.findViewById(R.id.tv_dialognewversion_version);
					tv_version.setText(json.getString("Version"));

					TextView tv_time = (TextView) window
							.findViewById(R.id.tv_dialognewversion_time);
					tv_time.setText(json.getString("Date"));

					TextView tv_content = (TextView) window
							.findViewById(R.id.tv_dialognewversion_content);
					tv_content.setText(json.getString("Info"));

					TextView tv_size = (TextView) window
							.findViewById(R.id.tv_dialognewversion_pacsize);
					tv_size.setText(json.getDouble("Size") + "M");

					Button btn_positive = (Button) window
							.findViewById(R.id.btn_dialognewversion_posbtn);
					btn_positive.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							try {
								Intent intent = new Intent();
								intent.setAction("android.intent.action.VIEW");
								Uri content_url = Uri.parse(json
										.getString("Addr"));

								intent.setData(content_url);
								startActivity(intent);

								if (json.getInt("Force") == 0) {
									alertDialog.dismiss();
								}

							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});

					Button btn_negative = (Button) window
							.findViewById(R.id.btn_dialognewversion_negbtn);
					if (json.getInt("Force") == 1) {
						btn_negative.setText("退出应用");
					}
					btn_negative.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							try {
								if (json.getInt("Force") == 0) {
									alertDialog.dismiss();
								} else {
									AppManager.getAppManager().AppExit(
											MainActivity.this);
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					});

				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	class Ad1Handler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();

			initViewPager(b.getInt("PicNum"));

		}
	}

	class TeamlistHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();
			String result = b.getString("result");
			System.out.println(result);
			if (result.equals("timeout")) {
				final TextView tv_error = (TextView) findViewById(R.id.tv_mainpage_tabteam_error);
				tv_error.setText("网络出问题咯(+﹏+)~\n点击刷新");
				tv_error.setVisibility(View.VISIBLE);
				rl_tabteam_gifcontainer.setVisibility(View.GONE);
				ll_tabteam_view1.setVisibility(View.GONE);
				ll_tabteam_view2.setVisibility(View.GONE);

				tv_error.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						rl_tabteam_gifcontainer.setVisibility(View.VISIBLE);
						tv_error.setVisibility(View.GONE);
						currentPage = 0;
						sumPage = 0;
						hasMore = true;
						new Thread(r_TeamList).start();
					}
				});
				return;
			}

			try {
				teamID = new JSONObject(result);

				int teamNum = teamID.getInt("TeamNum");
				TextView tv_error = (TextView) findViewById(R.id.tv_mainpage_tabteam_error);
				tv_error.setVisibility(View.GONE);
				if (teamNum == 0) {
					rl_tabteam_gifcontainer.setVisibility(View.GONE);
					ll_tabteam_view1.setVisibility(View.VISIBLE);
					ll_tabteam_view2.setVisibility(View.GONE);
					isTeamRefresh = true;
					mylist.clear();
					teamAdapter.notifyDataSetChanged();
					mPullToRefreshListView.onRefreshComplete();
				} else {
					sumPage = (int) (teamNum / 10);
					lastPageItemNum = teamNum - sumPage * 10;
					mylist.clear();
					allTeamInfo = new JSONObject();

					new Thread(r_TeamInfo).start();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				TextView tv_error = (TextView) findViewById(R.id.tv_mainpage_tabteam_error);
				tv_error.setText("出问题啦(+﹏+)~\n点击刷新");
				tv_error.setVisibility(View.VISIBLE);
				rl_tabteam_gifcontainer.setVisibility(View.GONE);
				ll_tabteam_view1.setVisibility(View.GONE);
				ll_tabteam_view2.setVisibility(View.GONE);
			}

		}
	}

	@SuppressLint("HandlerLeak")
	class TeaminfoHandler extends Handler {
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();

			if (b.getInt("num") < 10) {
				lv_list.removeFooterView(footView);
			}

			try {
				int i = b.getInt("i");

				teamInfo.put("info" + i, new JSONObject(b.getString("info")));

				teamLoadNum++;
				if (teamLoadNum >= b.getInt("num")) {

					for (int j = 0; j < b.getInt("num"); j++) {
						JSONObject json = teamInfo.getJSONObject("info" + j);

						HashMap<String, Object> map = new HashMap<String, Object>();
						if (json.has("Path")) {
							map.put("teamlogo", json.getString("Path"));
							map.put("hasPath", true);
						} else {
							map.put("hasPath", false);
						}

						map.put("teamtype", json.getInt("Type"));
						map.put("teamname", json.getString("TeamName"));
						map.put("teamleadername", json.getString("LeaderName"));
						map.put("teammembernum", json.getInt("MemberNum"));
						map.put("teamratenum", json.getInt("RateNum"));
						map.put("teamrate",
								json.getInt("RateNum") > 0 ? json
										.getDouble("Rate") : 0);

						mylist.add(map);

					}

					rl_tabteam_gifcontainer.setVisibility(View.GONE);
					ll_tabteam_view1.setVisibility(View.GONE);
					ll_tabteam_view2.setVisibility(View.VISIBLE);

					isTeamRefresh = true;

					teamAdapter.notifyDataSetChanged();
					mPullToRefreshListView.onRefreshComplete();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	class RssHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();

			if (b.getString("result").equals("error")) {
				ll_tab2_error.setVisibility(View.VISIBLE);
				return;
			}

			try {
				showListView(lv_viewpager_sports, feedSports);
				showListView(lv_viewpager_isoccer, feedIsoccer);
				showListView(lv_viewpager_csoccer, feedCsoccer);
				showListView(lv_viewpager_nba, feedNBA);
				showListView(lv_viewpager_cba, feedCBA);
				showListView(lv_viewpager_tennis, feedTennis);
				showListView(lv_viewpager_others, feedOthers);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	class RefreshRssHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();

			if (b.getString("result").equals("error")) {
				Toast.makeText(MainActivity.this, "网络出问题了...",
						Toast.LENGTH_SHORT).show();
				return;
			}

			try {
				switch (b.getInt("type")) {
				case 0:
					showListView(lv_viewpager_sports, feedSports);
					lv1.onRefreshComplete();
					break;
				case 1:
					showListView(lv_viewpager_isoccer, feedIsoccer);
					lv2.onRefreshComplete();
					break;
				case 2:
					showListView(lv_viewpager_csoccer, feedCsoccer);
					lv3.onRefreshComplete();
					break;
				case 3:
					showListView(lv_viewpager_nba, feedNBA);
					lv4.onRefreshComplete();
					break;
				case 4:
					showListView(lv_viewpager_cba, feedCBA);
					lv5.onRefreshComplete();
					break;
				case 5:
					showListView(lv_viewpager_tennis, feedTennis);
					lv6.onRefreshComplete();
					break;
				case 6:
					showListView(lv_viewpager_others, feedOthers);
					lv7.onRefreshComplete();
					break;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	class UserinfoHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();

			String result = b.getString("result");

			if (result.equals("timeout")) {
				Toast.makeText(MainActivity.this, "网络出问题咩(+﹏+)~",
						Toast.LENGTH_SHORT).show();
				TextView tv_tab3_title = (TextView) findViewById(R.id.tv_mainpage_tab3_title);
				tv_tab3_title.setText("加载失败，请检查网络设置");

				tv_tab3_newmessage.setVisibility(View.GONE);
				return;
			}

			try {
				System.out.println(result);
				JSONObject json = new JSONObject(result);

				if (json.has("Result")) {
					if (json.getString("Result").equals("身份验证失败")) {

						Data.ISLOGIN = false;

						TextView tv_tab3_title1 = (TextView) findViewById(R.id.tv_mainpage_tab3_title);
						tv_tab3_title1.setText("登录/注册");

						TextView tv_tab3_content1 = (TextView) findViewById(R.id.tv_mainpage_tab3_content);
						tv_tab3_content1.setText("登录信息过期啦，请重新登录~");

						TextView tv_tab3_introduction = (TextView) findViewById(R.id.tv_mainpage_tab3_introduction);
						tv_tab3_introduction.setVisibility(View.GONE);

						ImageView iv_tab3_money1 = (ImageView) findViewById(R.id.iv_mainpage_tab3_money);
						iv_tab3_money1.setVisibility(View.VISIBLE);
						TextView tv_tab3_money1 = (TextView) findViewById(R.id.tv_mainpage_tab3_money);
						tv_tab3_money1.setVisibility(View.INVISIBLE);

						ImageView iv_tab3_redbag1 = (ImageView) findViewById(R.id.iv_mainpage_tab3_redbag);
						iv_tab3_redbag1.setVisibility(View.VISIBLE);
						TextView tv_tab3_redbag1 = (TextView) findViewById(R.id.tv_mainpage_tab3_redbag);
						tv_tab3_redbag1.setVisibility(View.INVISIBLE);

						ImageView iv_tab3_mark1 = (ImageView) findViewById(R.id.iv_mainpage_tab3_mark);
						iv_tab3_mark1.setVisibility(View.VISIBLE);
						TextView tv_tab3_mark1 = (TextView) findViewById(R.id.tv_mainpage_tab3_mark);
						tv_tab3_mark1.setVisibility(View.INVISIBLE);

						ImageView iv_head = (ImageView) findViewById(R.id.iv_mainpage_tab3_userimage);
						iv_head.setImageResource(R.drawable.ic_mainpage_tab3_defaultphoto);

						tv_tab3_newmessage.setVisibility(View.GONE);
					}
				} else {

					TextView tv_tab3_title = (TextView) findViewById(R.id.tv_mainpage_tab3_title);
					tv_tab3_title.setText(json.getString("Nickname"));

					TextView tv_tab3_introduction = (TextView) findViewById(R.id.tv_mainpage_tab3_introduction);
					if (json.has("Introduction"))
						tv_tab3_introduction.setText("个人简介："
								+ json.getString("Introduction"));
					else
						tv_tab3_introduction.setText("暂无个人简介");

					TextView tv_tab3_money = (TextView) findViewById(R.id.tv_mainpage_tab3_money);
					tv_tab3_money.setText(Data.doubleTrans(json
							.getDouble("Money")));

					TextView tv_tab3_redbag = (TextView) findViewById(R.id.tv_mainpage_tab3_redbag);
					tv_tab3_redbag.setText(json.getInt("Redbag") + "");

					TextView tv_tab3_mark = (TextView) findViewById(R.id.tv_mainpage_tab3_mark);
					tv_tab3_mark.setText(json.getInt("Point") + "");

					if (json.has("Path")) {
						ImageView iv_tab3_head = (ImageView) findViewById(R.id.iv_mainpage_tab3_userimage);
						new AsyncViewTask(MainActivity.this,
								json.getString("Path"), iv_tab3_head, 2)
								.execute(iv_tab3_head);
						// Uri uri = Uri.parse(Data.URL+json.getString("Path"));
						// iv_tab3_head.setImageURI(uri);
					}

					if (json.getInt("Msg") == 0) {
						tv_tab3_newmessage.setVisibility(View.GONE);
					} else {
						tv_tab3_newmessage.setVisibility(View.VISIBLE);
						if (json.getInt("Msg") <= 9) {
							tv_tab3_newmessage.setText(json.getInt("Msg") + "");
						} else {
							tv_tab3_newmessage.setText("9+");
						}
					}

				}

				rl_tab3_login.setOnClickListener(onClickListener);
				ll_tab3_mybill.setOnClickListener(onClickListener);
				ll_tab3_matchbill.setOnClickListener(onClickListener);
				ll_tab3_mycard.setOnClickListener(onClickListener);
				ll_tab3_mymessage.setOnClickListener(onClickListener);
				ll_tab3_mycollect.setOnClickListener(onClickListener);

			} catch (Exception e) {
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

			Toast.makeText(MainActivity.this, "网络出问题咩(+﹏+)~",
					Toast.LENGTH_SHORT).show();
		}
	}

	// tab2标题栏监听
	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			vp_tab2_rss.setCurrentItem(index);
		}
	}

	/**
	 * 页卡切换监听
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int arg0) {
			Animation animation = null;

			// 这里的滑动的距离算不准啊，呀咩爹。。
			animation = new TranslateAnimation((int) ((90 * currIndex + 1)
					* scale + 0.5f), (int) ((90 * arg0 + 1) * scale + 0.5f), 0,
					0);

			currIndex = arg0;
			animation.setFillAfter(true);// True:图片停在动画结束位置
			animation.setDuration(300);
			cursor.startAnimation(animation);

			// 这里是可以优化的，但是这点效率的浪费对于现在的手机来说应该是微不足道，所以等我下次看到再谈优化的事啦。。先不管了
			TextView[] title = { tv_tab2_title1, tv_tab2_title2,
					tv_tab2_title3, tv_tab2_title4, tv_tab2_title5,
					tv_tab2_title6, tv_tab2_title7 };

			for (int i = 0; i < 7; i++) {
				title[i].setTextColor(getResources().getColor(R.color.darkGrey));
			}
			title[arg0].setTextColor(getResources().getColor(R.color.blue));

			// hsv_tab2_title.scrollTo(100, 0);

			if ((arg0 + 1) * (titleWidth / 7) > currentScrollLocation) {
				currentScrollLocation += (titleWidth / 7);
				hsv_tab2_title.scrollTo(currentScrollLocation
						- Data.SCREENWIDTH, 0);
			} else if ((arg0) * (titleWidth / 7) < currentScrollLocation
					- Data.SCREENWIDTH) {
				currentScrollLocation -= (titleWidth / 7);
				hsv_tab2_title.scrollTo(currentScrollLocation
						- Data.SCREENWIDTH, 0);
			}

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (aMapLocation == null) {
			city = "定位失败,点击刷新";
			isLocationSuccessful = false;
			stopLocation();// 销毁掉定位
		}
	}

	@Override
	public void onLocationChanged(AMapLocation location) {
		// TODO Auto-generated method stub
		if (location != null) {
			this.aMapLocation = location;// 判断超时机制
			Data.LATITUDE = location.getLatitude();
			Data.LONGITUDE = location.getLongitude();
			// Data.CITY = location.getCity();
			city = location.getCity();
			Data.ADDRESS = location.getAddress();
			isLocationSuccessful = true;
			Data.ISTRUELOCATION = true;
			stopLocation();
		}
	}

	/**
	 * 销毁定位
	 */
	private void stopLocation() {
		if (aMapLocManager != null) {
			aMapLocManager.removeUpdates(this);
			aMapLocManager.destory();
		}
		aMapLocManager = null;
	}

	// requestCode 100 登录后个人界面信息更新 101修改资料 后信息更新 102退出登录 103在tabteam页面登录
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 100:
				TextView tv_tab3_title = (TextView) findViewById(R.id.tv_mainpage_tab3_title);
				tv_tab3_title.setText("加载中...");

				TextView tv_tab3_content = (TextView) findViewById(R.id.tv_mainpage_tab3_content);
				tv_tab3_content.setText(Data.PHONE);

				TextView tv_tab3_introduction = (TextView) findViewById(R.id.tv_mainpage_tab3_introduction);
				tv_tab3_introduction.setVisibility(View.VISIBLE);
				tv_tab3_introduction.setText("个人简介：...");

				ImageView iv_tab3_money = (ImageView) findViewById(R.id.iv_mainpage_tab3_money);
				iv_tab3_money.setVisibility(View.INVISIBLE);
				TextView tv_tab3_money = (TextView) findViewById(R.id.tv_mainpage_tab3_money);
				tv_tab3_money.setVisibility(View.VISIBLE);
				tv_tab3_money.setText("...");

				ImageView iv_tab3_redbag = (ImageView) findViewById(R.id.iv_mainpage_tab3_redbag);
				iv_tab3_redbag.setVisibility(View.INVISIBLE);
				TextView tv_tab3_redbag = (TextView) findViewById(R.id.tv_mainpage_tab3_redbag);
				tv_tab3_redbag.setVisibility(View.VISIBLE);
				tv_tab3_redbag.setText("...");

				ImageView iv_tab3_mark = (ImageView) findViewById(R.id.iv_mainpage_tab3_mark);
				iv_tab3_mark.setVisibility(View.INVISIBLE);
				TextView tv_tab3_mark = (TextView) findViewById(R.id.tv_mainpage_tab3_mark);
				tv_tab3_mark.setVisibility(View.VISIBLE);
				tv_tab3_mark.setText("...");

				new Thread(r_Userinfo).start();

				isTeamRefresh = false;

				break;
			case 101:
				new Thread(r_Userinfo).start();
				break;
			case 102:
				TextView tv_tab3_title1 = (TextView) findViewById(R.id.tv_mainpage_tab3_title);
				tv_tab3_title1.setText("登录/注册");

				TextView tv_tab3_content1 = (TextView) findViewById(R.id.tv_mainpage_tab3_content);
				tv_tab3_content1.setText("登录后会发现更多惊喜哦！");

				TextView tv_tab3_introduction1 = (TextView) findViewById(R.id.tv_mainpage_tab3_introduction);
				tv_tab3_introduction1.setVisibility(View.GONE);

				ImageView iv_tab3_money1 = (ImageView) findViewById(R.id.iv_mainpage_tab3_money);
				iv_tab3_money1.setVisibility(View.VISIBLE);
				TextView tv_tab3_money1 = (TextView) findViewById(R.id.tv_mainpage_tab3_money);
				tv_tab3_money1.setVisibility(View.INVISIBLE);

				ImageView iv_tab3_redbag1 = (ImageView) findViewById(R.id.iv_mainpage_tab3_redbag);
				iv_tab3_redbag1.setVisibility(View.VISIBLE);
				TextView tv_tab3_redbag1 = (TextView) findViewById(R.id.tv_mainpage_tab3_redbag);
				tv_tab3_redbag1.setVisibility(View.INVISIBLE);

				ImageView iv_tab3_mark1 = (ImageView) findViewById(R.id.iv_mainpage_tab3_mark);
				iv_tab3_mark1.setVisibility(View.VISIBLE);
				TextView tv_tab3_mark1 = (TextView) findViewById(R.id.tv_mainpage_tab3_mark);
				tv_tab3_mark1.setVisibility(View.INVISIBLE);

				ImageView iv_head = (ImageView) findViewById(R.id.iv_mainpage_tab3_userimage);
				iv_head.setImageResource(R.drawable.ic_mainpage_tab3_defaultphoto);

				tv_tab3_newmessage.setVisibility(View.GONE);

				SharedPreferences sp = PreferenceManager
						.getDefaultSharedPreferences(MainActivity.this);
				Editor pEdit = sp.edit();
				pEdit.putBoolean("isLogin", false);
				pEdit.commit();

				rl_tabteam_gifcontainer.setVisibility(View.GONE);
				ll_tabteam_view1.setVisibility(View.VISIBLE);
				ll_tabteam_view2.setVisibility(View.GONE);

				break;
			case 103:
				rl_tabteam_gifcontainer.setVisibility(View.VISIBLE);
				currentPage = 0;
				sumPage = 0;
				hasMore = true;
				new Thread(r_TeamList).start();
				break;
			}

		}
	}

	BroadcastReceiver broadcast_team = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// 刷新球队
			rl_tabteam_gifcontainer.setVisibility(View.VISIBLE);
			currentPage = 0;
			sumPage = 0;
			hasMore = true;
			new Thread(r_TeamList).start();
		}

	};

	BroadcastReceiver broadcast_userinfo = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// 刷新个人信息
			TextView tv_tab3_title = (TextView) findViewById(R.id.tv_mainpage_tab3_title);
			tv_tab3_title.setText("加载中...");

			TextView tv_tab3_content = (TextView) findViewById(R.id.tv_mainpage_tab3_content);
			tv_tab3_content.setText(Data.PHONE);

			TextView tv_tab3_introduction = (TextView) findViewById(R.id.tv_mainpage_tab3_introduction);
			tv_tab3_introduction.setVisibility(View.VISIBLE);
			tv_tab3_introduction.setText("个人简介：...");

			ImageView iv_tab3_money = (ImageView) findViewById(R.id.iv_mainpage_tab3_money);
			iv_tab3_money.setVisibility(View.INVISIBLE);
			TextView tv_tab3_money = (TextView) findViewById(R.id.tv_mainpage_tab3_money);
			tv_tab3_money.setVisibility(View.VISIBLE);
			tv_tab3_money.setText("...");

			ImageView iv_tab3_redbag = (ImageView) findViewById(R.id.iv_mainpage_tab3_redbag);
			iv_tab3_redbag.setVisibility(View.INVISIBLE);
			TextView tv_tab3_redbag = (TextView) findViewById(R.id.tv_mainpage_tab3_redbag);
			tv_tab3_redbag.setVisibility(View.VISIBLE);
			tv_tab3_redbag.setText("...");

			ImageView iv_tab3_mark = (ImageView) findViewById(R.id.iv_mainpage_tab3_mark);
			iv_tab3_mark.setVisibility(View.INVISIBLE);
			TextView tv_tab3_mark = (TextView) findViewById(R.id.tv_mainpage_tab3_mark);
			tv_tab3_mark.setVisibility(View.VISIBLE);
			tv_tab3_mark.setText("...");

			new Thread(r_Userinfo).start();

			isTeamRefresh = false;
		}

	};

	// 返回键监听
	private long exitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(), "再按一次退出应用",
						Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcast_team);
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
		stopLocation();
		MobclickAgent.onPause(this);
	}

}

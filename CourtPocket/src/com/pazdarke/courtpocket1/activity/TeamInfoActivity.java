package com.pazdarke.courtpocket1.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.pazdarke.courtpocket1.tools.listview.AsyncViewTask;
import com.pazdarke.courtpocket1.tools.pic.BitmapToString;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TeamInfoActivity extends Activity {

	ProgressDialog progressDialog;

	RelativeLayout rl_gifcontainer;

	JSONObject info;
	String teamID;
	String teamName;
	boolean isMyTeam;
	boolean isInvite;

	Bitmap bm = null;
	long lastModified = 0;// 记录temp图片文件的最后修改时间
	String imgStr = Environment.getExternalStorageDirectory()
			+ "/CourtPocket/temp.jpg";

	TeaminfoHandler teaminfoHandler;
	PicHandler picHandler;
	HeadPicHandler headPicHandler;
	InviteHandler inviteHandler;
	QuitTeamHandler quitTeamHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_team_info);

		AppManager.getAppManager().addActivity(this);

		IntentFilter filter = new IntentFilter("RefreshTeaminfo");
		registerReceiver(broadcastReceiver, filter);

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("玩命加载数据中...");
		progressDialog.setCancelable(false);

		// info=new JSONObject(getIntent().getStringExtra("info"));
		teamID = getIntent().getStringExtra("ID");
		isMyTeam = getIntent().getBooleanExtra("isMyTeam", false);
		isInvite = getIntent().getBooleanExtra("isInvite", false);

		initView();

		new Thread(r_Teaminfo).start();

		teaminfoHandler = new TeaminfoHandler();
		picHandler = new PicHandler();
		headPicHandler = new HeadPicHandler();
		inviteHandler = new InviteHandler();
		quitTeamHandler = new QuitTeamHandler();

	}

	private void initView() {
		// TODO Auto-generated method stub
		try {

			ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_teaminfo_leftarrow);
			iv_leftarrow.setOnClickListener(onClickListener);

			RelativeLayout rl_bg = (RelativeLayout) findViewById(R.id.rl_teaminfo_bg);
			LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, (int) (Data.SCREENWIDTH * 0.4));
			rl_bg.setLayoutParams(p1);

			RelativeLayout rl_teammember = (RelativeLayout) findViewById(R.id.rl_teaminfo_teammember);
			rl_teammember.setOnClickListener(onClickListener);

			RelativeLayout rl_record = (RelativeLayout) findViewById(R.id.rl_teaminfo_record);
			rl_record.setOnClickListener(onClickListener);

			init_gif();

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

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_teaminfo_gifcontainer);
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
				(int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		p.addRule(RelativeLayout.CENTER_HORIZONTAL);
		p.setMargins(0, (int) (Data.SCREENHEIGHT * 0.22), 0, 0);
		rl_gifcontainer.addView(gif_loading, p);

	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			try {
				Intent intent;
				switch (v.getId()) {
				case R.id.iv_teaminfo_leftarrow:
					finish();
					break;
				case R.id.iv_teaminfo_headpic:
					if (Data.USERID.equals(info.getInt("LeaderUserID") + "")) {
						String[] picSrc = new String[] { "拍照", "相册" };
						Dialog picDialog = new AlertDialog.Builder(
								TeamInfoActivity.this)
								.setItems(picSrc,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												selectHeadPic(which);
											}
										}).setTitle("更换背景").create();
						picDialog.show();
					} else {
						Toast.makeText(TeamInfoActivity.this, "只有队长才有权限操作哦",
								Toast.LENGTH_SHORT).show();
					}
					break;
				case R.id.iv_teaminfo_logo:
					if (Data.USERID.equals(info.getInt("LeaderUserID") + "")) {
						String[] picSrc = new String[] { "拍照", "相册" };
						Dialog picDialog = new AlertDialog.Builder(
								TeamInfoActivity.this)
								.setItems(picSrc,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												selectPic(which);
											}
										}).setTitle("更换球队图标").create();
						picDialog.show();
					} else {
						Toast.makeText(TeamInfoActivity.this, "只有队长才有权限操作哦",
								Toast.LENGTH_SHORT).show();
					}
					break;
				case R.id.tv_teaminfo_manage:
					if (Data.USERID.equals(info.getInt("LeaderUserID") + "")) {
						intent = new Intent(TeamInfoActivity.this,
								ManageteamActivity.class);
						intent.putExtra("ID", teamID);
						intent.putExtra("TeamName", teamName);
						if (info.has("Introduction")) {
							intent.putExtra("Introduction",
									info.getString("Introduction"));
						} else {
							intent.putExtra("Introduction", "");
						}
						startActivity(intent);
					} else {
						Toast.makeText(TeamInfoActivity.this, "只有队长才有权限操作哦",
								Toast.LENGTH_SHORT).show();
					}
					break;
				case R.id.rl_teaminfo_teammember:
					intent = new Intent(TeamInfoActivity.this,
							TeammemberlistActivity.class);
					intent.putExtra("ID", teamID);
					intent.putExtra("TeamName", teamName);
					startActivity(intent);
					break;
				case R.id.rl_teaminfo_record:
					intent = new Intent(TeamInfoActivity.this,
							FightrecordActivity.class);
					intent.putExtra("TeamID", teamID);
					intent.putExtra("isMyTeam", isMyTeam);
					intent.putExtra("TeamName", teamName);
					startActivity(intent);
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	void selectPic(int which) {
		Intent intent;
		File file = new File(imgStr);
		if (file.exists())
			file.delete();

		switch (which) {
		case 0:
			intent = new Intent("android.media.action.IMAGE_CAPTURE");
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			startActivityForResult(intent, 200);
			break;
		case 1:
			intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");// 相片类型
			intent.putExtra("output", Uri.fromFile(file));
			intent.putExtra("crop", "true");
			intent.putExtra("aspectX", 1);// 裁剪框比例
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 180);// 输出图片大小
			intent.putExtra("outputY", 180);
			intent.putExtra("return-data", true);
			startActivityForResult(intent, 201);
			break;
		}
	}

	void selectHeadPic(int which) {
		Intent intent;
		File file = new File(imgStr);
		if (file.exists())
			file.delete();

		switch (which) {
		case 0:
			intent = new Intent("android.media.action.IMAGE_CAPTURE");
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			startActivityForResult(intent, 300);
			break;
		case 1:
			intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");// 相片类型
			intent.putExtra("output", Uri.fromFile(file));
			intent.putExtra("crop", "true");
			intent.putExtra("aspectX", 5);// 裁剪框比例
			intent.putExtra("aspectY", 2);
			intent.putExtra("outputX", 400);// 输出图片大小
			intent.putExtra("outputY", 160);
			intent.putExtra("return-data", true);
			startActivityForResult(intent, 301);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 100:
			if (data != null) {
				Button btn_btn = (Button) findViewById(R.id.btn_teaminfo_btn);
				btn_btn.setClickable(false);
				btn_btn.setBackgroundResource(R.color.grey);
				btn_btn.setTextColor(getResources().getColor(R.color.darkGrey));
			}
			break;
		case 200:
			try {
				File file = new File(imgStr);
				if (!file.exists())
					return;
				if (file.lastModified() == lastModified)
					return;

				Uri uri = Uri.fromFile(file);

				Intent intent = new Intent();
				intent.setAction("com.android.camera.action.CROP");
				intent.setDataAndType(uri, "image/*");// mUri是已经选择的图片Uri
				intent.putExtra("crop", "true");
				intent.putExtra("aspectX", 1);// 裁剪框比例
				intent.putExtra("aspectY", 1);
				intent.putExtra("outputX", 180);// 输出图片大小
				intent.putExtra("outputY", 180);
				intent.putExtra("return-data", true);
				TeamInfoActivity.this.startActivityForResult(intent, 202);

			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case 201:
			File file = new File(imgStr);
			if (!file.exists())
				return;
			if (file.lastModified() == lastModified)
				return;

			Uri uri = data.getData();
			if (uri == null) {
				Bundle bundle = data.getExtras();
				if (bundle != null) {
					bm = (Bitmap) bundle.get("data");
				}
			} else {
				ContentResolver resolver = getContentResolver();
				try {
					bm = MediaStore.Images.Media.getBitmap(resolver, uri);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (bm != null) {
				new Thread(r_Pic).start();
				progressDialog.show();
			}
			break;
		case 202:
			try {
				bm = data.getParcelableExtra("data");
				if (bm != null) {
					new Thread(r_Pic).start();
					progressDialog.show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case 300:
			try {
				File file1 = new File(imgStr);
				if (!file1.exists())
					return;
				if (file1.lastModified() == lastModified)
					return;

				Uri uri1 = Uri.fromFile(file1);

				Intent intent = new Intent();
				intent.setAction("com.android.camera.action.CROP");
				intent.setDataAndType(uri1, "image/*");// mUri是已经选择的图片Uri
				intent.putExtra("crop", "true");
				intent.putExtra("aspectX", 5);// 裁剪框比例
				intent.putExtra("aspectY", 2);
				intent.putExtra("outputX", 400);// 输出图片大小
				intent.putExtra("outputY", 160);
				intent.putExtra("return-data", true);
				TeamInfoActivity.this.startActivityForResult(intent, 302);

			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case 301:
			File file1 = new File(imgStr);
			if (!file1.exists())
				return;
			if (file1.lastModified() == lastModified)
				return;

			Uri uri1 = data.getData();
			if (uri1 == null) {
				Bundle bundle = data.getExtras();
				if (bundle != null) {
					bm = (Bitmap) bundle.get("data");
				}
			} else {
				ContentResolver resolver = getContentResolver();
				try {
					bm = MediaStore.Images.Media.getBitmap(resolver, uri1);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (bm != null) {
				new Thread(r_HeadPic).start();
				progressDialog.show();
			}
			break;
		case 302:
			try {
				bm = data.getParcelableExtra("data");
				if (bm != null) {
					new Thread(r_HeadPic).start();
					progressDialog.show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}

	}

	Runnable r_Teaminfo = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "TeamInfo"));
			params.add(new BasicNameValuePair("TeamID", teamID));

			String result = new HttpPostConnection("TeamServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			teaminfoHandler.sendMessage(msg);
		}
	};

	Runnable r_Pic = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String str = BitmapToString.bitmaptoString(bm);

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "UpdatePic"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("TeamID", teamID));
			params.add(new BasicNameValuePair("PicStr", str));

			String result = new HttpPostConnection("TeamServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			picHandler.sendMessage(msg);
		}
	};

	Runnable r_HeadPic = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String str = BitmapToString.bitmaptoString(bm);

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "UpdateHeadPic"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("TeamID", teamID));
			params.add(new BasicNameValuePair("PicStr", str));

			String result = new HttpPostConnection("TeamServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			headPicHandler.sendMessage(msg);
		}
	};

	Runnable r_QuitTeam = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "QuitTeam"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("TeamID", teamID));

			String result = new HttpPostConnection("TeamServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			quitTeamHandler.sendMessage(msg);
		}
	};

	class InviteThread extends Thread {

		int Op;

		InviteThread(int Op) {
			this.Op = Op;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "HandleInvite"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("ApplicationID",
					TeamInfoActivity.this.getIntent().getStringExtra(
							"ApplicationID")));
			params.add(new BasicNameValuePair("Op", Op + ""));

			String result = new HttpPostConnection("ApplicationServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			inviteHandler.sendMessage(msg);
		}
	}

	class TeaminfoHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			try {

				Bundle b = msg.getData();

				String result = b.getString("result");
				
				System.out.println(result);

				if (result.equals("timeout")) {
					Toast.makeText(TeamInfoActivity.this, "网络出问题了~>_<~+",
							Toast.LENGTH_SHORT).show();

					final TextView tv_error = (TextView) findViewById(R.id.tv_teaminfo_error);
					tv_error.setVisibility(View.VISIBLE);
					tv_error.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							tv_error.setVisibility(View.GONE);
							rl_gifcontainer.setVisibility(View.VISIBLE);
							new Thread(r_Teaminfo);
						}
					});

					return;
				}

				info = new JSONObject(result);

				ImageView iv_headpic = (ImageView) findViewById(R.id.iv_teaminfo_headpic);
				ImageView iv_logo = (ImageView) findViewById(R.id.iv_teaminfo_logo);
				TextView tv_teamname = (TextView) findViewById(R.id.tv_teaminfo_teamname);
				TextView tv_leadername = (TextView) findViewById(R.id.tv_teaminfo_leadername);
				TextView tv_teamtype = (TextView) findViewById(R.id.tv_teaminfo_teamtype);
				TextView tv_intro = (TextView) findViewById(R.id.tv_teaminfo_intro);
				TextView tv_membernum = (TextView) findViewById(R.id.tv_teaminfo_membernum);

				RatingBar rb_rate = (RatingBar) findViewById(R.id.rb_teaminfo_rate);
				TextView tv_rate = (TextView) findViewById(R.id.tv_teaminfo_rate);
				TextView tv_ratenum = (TextView) findViewById(R.id.tv_teaminfo_ratenum);
				TextView tv_averageage = (TextView) findViewById(R.id.tv_teaminfo_averageage);
				Button btn_btn = (Button) findViewById(R.id.btn_teaminfo_btn);
				TextView tv_manage = (TextView) findViewById(R.id.tv_teaminfo_manage);

				if (info.has("HeadPic")) {
					new AsyncViewTask(TeamInfoActivity.this,
							info.getString("HeadPic"), iv_headpic,2)
							.execute(iv_headpic);
				}

				if (info.has("Path")) {
					new AsyncViewTask(TeamInfoActivity.this,
							info.getString("Path"), iv_logo,6).execute(iv_logo);
				}

				iv_logo.getLayoutParams().width = (int) (Data.SCREENWIDTH * 0.2);
				iv_logo.getLayoutParams().height = (int) (Data.SCREENWIDTH * 0.2);

				tv_teamname.setText(info.getString("TeamName"));
				teamName = info.getString("TeamName");
				tv_leadername.setText("队长：" + info.getString("LeaderName"));

				String[] type = { "足球队", "篮球队", "排球队" };
				tv_teamtype.setText(type[info.getInt("Type") - 1]);

				tv_ratenum.setText("(" + info.getInt("RateNum") + "队评价过)");
				double rate;
				if (info.getInt("RateNum") == 0)
					rate = 0;
				else
					rate = info.getDouble("Rate");
				BigDecimal b1 = new BigDecimal(rate);
				rate = b1.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
				rb_rate.setRating((float) rate);
				tv_rate.setText(rate + "分");

				if (info.has("Introduction")) {
					tv_intro.setText(info.getString("Introduction"));
				} else {
					tv_intro.setText("");
				}

				tv_membernum.setText(info.getInt("MemberNum") + "人");

				int age = (int) info.getDouble("Age");
				if (age == 0) {
					tv_averageage.setText("暂无数据");
				} else {
					tv_averageage.setText(age + "岁");
				}

				if (info.getInt("MemberNum") == 0) {
					tv_membernum.setText("球队已解散");
					btn_btn.setVisibility(View.GONE);
				} else {
					if (isMyTeam) {
						btn_btn.setVisibility(View.VISIBLE);
						btn_btn.setBackgroundResource(R.drawable.selector_drawable_red_orange);
						btn_btn.setText("退出球队");
						btn_btn.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								try {
									if (Data.USERID.equals(info
											.getInt("LeaderUserID") + "")) {
										AlertDialog dialog = new AlertDialog.Builder(
												TeamInfoActivity.this)
												.setMessage(
														"由于您是队长，退出后球队将被解散。确定您要退出球队吗？")
												.setPositiveButton(
														"确定退出",
														new DialogInterface.OnClickListener() {

															@Override
															public void onClick(
																	DialogInterface dialog,
																	int which) {
																// TODO
																// Auto-generated
																// method stub
																dialog.dismiss();
																progressDialog
																		.show();
																new Thread(
																		r_QuitTeam)
																		.start();
															}

														})
												.setNegativeButton(
														"取消",
														new DialogInterface.OnClickListener() {

															@Override
															public void onClick(
																	DialogInterface dialog,
																	int which) {
																// TODO
																// Auto-generated
																// method stub
																dialog.dismiss();
															}
														}).create();
										dialog.show();
									} else {
										progressDialog.show();
										new Thread(r_QuitTeam).start();
									}
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});

						tv_manage.setVisibility(View.VISIBLE);
						tv_manage.setOnClickListener(onClickListener);

						iv_headpic.setOnClickListener(onClickListener);
						iv_logo.setOnClickListener(onClickListener);

					} else if (isInvite) {
						btn_btn.setBackgroundResource(R.drawable.selector_drawable_yellow_orange);
						btn_btn.setVisibility(View.GONE);
						tv_manage.setVisibility(View.GONE);

						LinearLayout ll_invite = (LinearLayout) findViewById(R.id.ll_teaminfo_invite);
						ll_invite.setVisibility(View.VISIBLE);

						Button btn_refuse = (Button) findViewById(R.id.btn_teaminfo_inviterefuse);
						btn_refuse.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								progressDialog.show();
								new InviteThread(0).start();
							}
						});

						Button btn_agree = (Button) findViewById(R.id.btn_teaminfo_inviteagree);
						btn_agree.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								progressDialog.show();
								new InviteThread(1).start();
							}
						});

					} else {
						btn_btn.setVisibility(View.VISIBLE);
						btn_btn.setBackgroundResource(R.drawable.selector_drawable_yellow_orange);
						btn_btn.setText("申请加入");
						tv_manage.setVisibility(View.GONE);
						btn_btn.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								Intent intent = new Intent(
										TeamInfoActivity.this,
										ApplyteamActivity.class);
								intent.putExtra("ID", teamID);
								startActivityForResult(intent, 100);
							}
						});
					}
				}

				rl_gifcontainer.setVisibility(View.GONE);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class PicHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			progressDialog.dismiss();

			Bundle b = msg.getData();

			String result = b.getString("result");

			if (result.equals("timeout")) {
				Toast.makeText(TeamInfoActivity.this, "网络出问题了~>_<~+",
						Toast.LENGTH_SHORT).show();
				return;
			}

			try {
				JSONObject json = new JSONObject(result);
				result = json.getString("Result");

				if (result.equals("头像更新成功")) {
					((ImageView) findViewById(R.id.iv_teaminfo_logo))
							.setImageBitmap(bm);
				} else {
					Toast.makeText(TeamInfoActivity.this, result,
							Toast.LENGTH_SHORT).show();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	class HeadPicHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			progressDialog.dismiss();

			Bundle b = msg.getData();

			String result = b.getString("result");

			if (result.equals("timeout")) {
				Toast.makeText(TeamInfoActivity.this, "网络出问题了~>_<~+",
						Toast.LENGTH_SHORT).show();
				return;
			}

			try {
				JSONObject json = new JSONObject(result);
				result = json.getString("Result");

				if (result.equals("背景图更新成功")) {
					((ImageView) findViewById(R.id.iv_teaminfo_headpic))
							.setImageBitmap(bm);
				} else {
					Toast.makeText(TeamInfoActivity.this, result,
							Toast.LENGTH_SHORT).show();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	class InviteHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			try {

				progressDialog.dismiss();

				Bundle b = msg.getData();
				String result = b.getString("result");

				if (result.equals("timeout")) {
					Toast.makeText(TeamInfoActivity.this, "网络出问题了~>_<~+",
							Toast.LENGTH_SHORT).show();
					return;
				}

				result = new JSONObject(result).getString("Result");

				if (result.equals("操作成功")) {
					Intent intent = new Intent("RefreshMymessage");
					sendBroadcast(intent);
					finish();
				} else {
					Toast.makeText(TeamInfoActivity.this, result,
							Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class QuitTeamHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			progressDialog.dismiss();

			String result = msg.getData().getString("result");

			if (result.equals("timeout")) {
				Toast.makeText(TeamInfoActivity.this, "网络出问题了~>_<~+",
						Toast.LENGTH_SHORT).show();
				return;
			}

			try {
				JSONObject json = new JSONObject(result);
				Toast.makeText(TeamInfoActivity.this, json.getString("Result"),
						Toast.LENGTH_SHORT).show();
				if (json.getString("Result").equals("退出球队成功")
						|| json.getString("Result").equals("球队已被解散")) {
					Intent intent = new Intent("RefreshTeamlist");
					sendBroadcast(intent);
					finish();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			try {
				new Thread(r_Teaminfo).start();
			} catch (Exception e) {
				e.printStackTrace();
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

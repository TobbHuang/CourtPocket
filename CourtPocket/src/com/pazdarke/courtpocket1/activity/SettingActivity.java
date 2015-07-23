package com.pazdarke.courtpocket1.activity;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.httpConnection.HttpGetConnection;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.tencent.android.tpush.XGPushManager;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends Activity {

	ProgressDialog progressDialog;

	TextView tv_storage;
	TextView tv_notification;

	CheckNewVersionHandler checkNewVersionHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_setting);

		AppManager.getAppManager().addActivity(this);

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("正在检查更新…");
		progressDialog.setCancelable(true);

		initView();

		checkNewVersionHandler = new CheckNewVersionHandler();

	}

	private void initView() {
		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_setting_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		RelativeLayout rl_version = (RelativeLayout) findViewById(R.id.rl_setting_version);
		rl_version.setOnClickListener(onClickListener);

		TextView tv_version = (TextView) findViewById(R.id.tv_setting_version);
		tv_version.setText(getVersion());

		RelativeLayout rl_storage = (RelativeLayout) findViewById(R.id.rl_setting_storage);
		rl_storage.setOnClickListener(onClickListener);

		tv_storage = (TextView) findViewById(R.id.tv_setting_storage);
		tv_storage.setText(getAutoFileOrFilesSize(Environment
				.getExternalStorageDirectory() + "/CourtPocket"));

		RelativeLayout rl_notification = (RelativeLayout) findViewById(R.id.rl_setting_notification);
		rl_notification.setOnClickListener(onClickListener);

		tv_notification = (TextView) findViewById(R.id.tv_setting_notification);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(SettingActivity.this);
		Boolean isNotificationOpen = prefs.getBoolean("isNotificationOpen",
				true);
		if (isNotificationOpen) {
			tv_notification.setText("打开");
		} else {
			tv_notification.setText("关闭");
		}

		Button btn_quitlogin = (Button) findViewById(R.id.btn_setting_quitlogin);
		if (!Data.ISLOGIN) {
			btn_quitlogin.setVisibility(View.GONE);
		}
		btn_quitlogin.setOnClickListener(onClickListener);
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_setting_leftarrow:
				finish();
				break;
			case R.id.rl_setting_version:
				progressDialog.show();
				new Thread(r_CheckNewVersion).start();
				break;
			case R.id.rl_setting_storage:
				File file = new File(Environment.getExternalStorageDirectory()
						+ "/CourtPocket");
				if (file.exists()) {
					File flist[] = file.listFiles();
					for (int i = 0; i < flist.length; i++) {
						flist[i].delete();
					}
					tv_storage.setText("0KB");
				}
				break;
			case R.id.rl_setting_notification:
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(SettingActivity.this);
				Boolean isNotificationOpen = prefs.getBoolean(
						"isNotificationOpen", true);
				if(isNotificationOpen){
					tv_notification.setText("关闭");
					Editor pEdit = prefs.edit();
		            pEdit.putBoolean("isNotificationOpen", false);
		            pEdit.commit();
				} else{
					tv_notification.setText("打开");
					Editor pEdit = prefs.edit();
		            pEdit.putBoolean("isNotificationOpen", true);
		            pEdit.commit();
				}
				break;
			case R.id.btn_setting_quitlogin:
				Data.ISLOGIN = false;

				// 帐号反注册信鸽推送
				XGPushManager.registerPush(SettingActivity.this, "*");

				setResult(RESULT_OK, SettingActivity.this.getIntent());
				finish();
				break;
			}
		}
	};

	/**
	 * 调用此方法自动计算指定文件或指定文件夹的大小
	 * 
	 * @param filePath
	 *            文件路径
	 * @return 计算好的带B、KB、MB、GB的字符串
	 */
	public static String getAutoFileOrFilesSize(String filePath) {
		File file = new File(filePath);
		long blockSize = 0;
		try {
			if (file.isDirectory()) {
				blockSize = getFileSizes(file);
			} else {
				blockSize = getFileSize(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return FormetFileSize(blockSize);
	}

	/**
	 * 获取指定文件大小
	 * 
	 * @param f
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	private static long getFileSize(File file) throws Exception {
		long size = 0;
		if (file.exists()) {
			FileInputStream fis = null;
			fis = new FileInputStream(file);
			size = fis.available();
		} else {
			file.createNewFile();
		}
		return size;
	}

	/**
	 * 获取指定文件夹
	 * 
	 * @param f
	 * @return
	 * @throws Exception
	 */
	private static long getFileSizes(File f) throws Exception {
		long size = 0;
		File flist[] = f.listFiles();
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getFileSizes(flist[i]);
			} else {
				size = size + getFileSize(flist[i]);
			}
		}
		return size;
	}

	/**
	 * 转换文件大小
	 * 
	 * @param fileS
	 * @return
	 */
	private static String FormetFileSize(long fileS) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		String wrongSize = "0B";
		if (fileS == 0) {
			return wrongSize;
		}
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "KB";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "MB";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "GB";
		}
		return fileSizeString;
	}

	/**
	 * 获取版本号
	 * 
	 * @return 当前应用的版本号
	 */
	public String getVersion() {
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			String version = info.versionName;
			return "v" + version + " 爆炸版";
		} catch (Exception e) {
			e.printStackTrace();
			return "版本号获取失败";
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

	class CheckNewVersionHandler extends Handler {
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			progressDialog.dismiss();

			Bundle b = msg.getData();
			String result = b.getString("result");

			if (result.equals("timeout")) {
				Toast.makeText(SettingActivity.this, "网络出问题咯...",
						Toast.LENGTH_SHORT).show();
				return;
			}

			try {

				PackageManager manager = SettingActivity.this
						.getPackageManager();
				PackageInfo info = manager.getPackageInfo(
						SettingActivity.this.getPackageName(), 0);
				String version = info.versionName;

				final JSONObject json = new JSONObject(result);

				if (!json.getString("Version").equals(version)) {
					final AlertDialog alertDialog = new AlertDialog.Builder(
							SettingActivity.this).create();
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
											SettingActivity.this);
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					});

				} else {
					Toast.makeText(SettingActivity.this, "当前已是最新版本~",
							Toast.LENGTH_SHORT).show();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
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

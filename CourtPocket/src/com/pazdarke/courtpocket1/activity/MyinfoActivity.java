package com.pazdarke.courtpocket1.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.text.format.Time;
import android.text.method.DigitsKeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

public class MyinfoActivity extends Activity {

	ProgressDialog progressDialog;
	
	Bitmap bm = null;
	long lastModified=0;// 记录temp图片文件的最后修改时间
	String imgStr = Environment.getExternalStorageDirectory() + "/CourtPocket/temp.jpg";

	RelativeLayout rl_gifcontainer;
	TextView tv_error;

	ImageView iv_head;
	TextView tv_nickname,tv_introduction, tv_phone, tv_sex, tv_birthday, tv_qq, tv_mail,
			tv_address;

	String str_nickname = "", str_phone = "", str_birthday = "", str_qq = "",
			str_mail = "", str_address = "", str_introduction="";
	int int_sex;

	int UpdateType;// 0Nickname 1Sex 2Birthday 3QQ 4Email 5Address
	TextView tv_dialog_title;
	EditText et_dialog;
	ImageView iv_dialog_clear;
	Button btn_dialog_negative;
	Button btn_dialog_position;
	
	int i;
	DatePickerDialog datePickerDialog;
	boolean isDateShow;

	InfoHandler infoHandler;
	UpdateInfoHandler updateInfoHandler;
	PicHandler picHandler;
	
	
	// 正则表达式验证字符串是否为邮箱格式
	String emailStrPattern = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.activity_myinfo);
			
			AppManager.getAppManager().addActivity(this);

			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("玩命加载数据中...");
			progressDialog.setCancelable(true);

			init_gif();

			tv_error = (TextView) findViewById(R.id.tv_myinfo_error);
			tv_error.setOnClickListener(onClickListener);

			ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_myinfo_leftarrow);
			iv_leftarrow.setOnClickListener(onClickListener);

			iv_head = (ImageView) findViewById(R.id.iv_myinfo_head);

			RelativeLayout rl_head = (RelativeLayout) findViewById(R.id.rl_myinfo_head);
			rl_head.setOnClickListener(onClickListener);

			tv_nickname = (TextView) findViewById(R.id.tv_myinfo_nickname);

			RelativeLayout rl_nickname = (RelativeLayout) findViewById(R.id.rl_myinfo_nickname);
			rl_nickname.setOnClickListener(onClickListener);
			
			tv_introduction=(TextView)findViewById(R.id.tv_myinfo_introduction);
			
			RelativeLayout rl_introduction = (RelativeLayout) findViewById(R.id.rl_myinfo_introduction);
			rl_introduction.setOnClickListener(onClickListener);

			tv_phone = (TextView) findViewById(R.id.tv_myinfo_phone);

			RelativeLayout rl_phone = (RelativeLayout) findViewById(R.id.rl_myinfo_phone);
			rl_phone.setOnClickListener(onClickListener);

			tv_sex = (TextView) findViewById(R.id.tv_myinfo_sex);

			RelativeLayout rl_sex = (RelativeLayout) findViewById(R.id.rl_myinfo_sex);
			rl_sex.setOnClickListener(onClickListener);

			tv_birthday = (TextView) findViewById(R.id.tv_myinfo_birthday);

			RelativeLayout rl_birthday = (RelativeLayout) findViewById(R.id.rl_myinfo_birthday);
			rl_birthday.setOnClickListener(onClickListener);

			tv_qq = (TextView) findViewById(R.id.tv_myinfo_qq);

			RelativeLayout rl_qq = (RelativeLayout) findViewById(R.id.rl_myinfo_qq);
			rl_qq.setOnClickListener(onClickListener);

			tv_mail = (TextView) findViewById(R.id.tv_myinfo_mail);

			RelativeLayout rl_mail = (RelativeLayout) findViewById(R.id.rl_myinfo_mail);
			rl_mail.setOnClickListener(onClickListener);

			tv_address = (TextView) findViewById(R.id.tv_myinfo_address);

			RelativeLayout rl_address = (RelativeLayout) findViewById(R.id.rl_myinfo_address);
			rl_address.setOnClickListener(onClickListener);

			infoHandler = new InfoHandler();
			updateInfoHandler = new UpdateInfoHandler();
			picHandler = new PicHandler();

			new Thread(r_Info).start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_myinfo_leftarrow:
				setResult(RESULT_OK, MyinfoActivity.this.getIntent());
				finish();
				break;
			case R.id.tv_myinfo_error:
				rl_gifcontainer.setVisibility(View.VISIBLE);
				tv_error.setVisibility(View.GONE);
				new Thread(r_Info).start();
				break;
			case R.id.rl_myinfo_head:
				String[] picSrc = new String[] { "拍照", "相册" };
				Dialog picDialog = new AlertDialog.Builder(
						MyinfoActivity.this).setItems(picSrc,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								selectPic(which);
							}
						}).create();
				picDialog.show();
				break;
			case R.id.rl_myinfo_nickname:
				initDialog(0);
				break;
			case R.id.rl_myinfo_introduction:
				initDialog(4);
				break;
			case R.id.rl_myinfo_phone:
				break;
			case R.id.rl_myinfo_sex:
				String[] sex = new String[] { "男", "女", "保密" };
				Dialog sexDialog = new AlertDialog.Builder(MyinfoActivity.this)
						.setItems(sex, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								int_sex = which;
								UpdateType = 1;
								progressDialog.show();
								new Thread(r_Update).start();
							}
						}).create();
				sexDialog.show();
				break;
			case R.id.rl_myinfo_birthday:

				int yy,mm,dd;
				if (str_birthday.equals("")) {
					yy = 1990;
					mm = 1;
					dd = 1;
				} else {
					String[] date = str_birthday.split("-");
					yy = Integer.parseInt(date[0]);
					mm = Integer.parseInt(date[1]);
					dd = Integer.parseInt(date[2]);
				}

				i = 0;// 解决OnDateSet可能执行两次的bug
				isDateShow = true;
				datePickerDialog = new DatePickerDialog(MyinfoActivity.this,
						new DatePickerDialog.OnDateSetListener() {

							@Override
							public void onDateSet(DatePicker arg0, int arg1,
									int arg2, int arg3) {
								// TODO Auto-generated method stub

								if (i == 0) {
									i++;
									Time t = new Time("GMT+8");
									t.setToNow();
									// 检测日期是否选择有误，即不能大于当前日期
									if (arg1 > t.year
											|| (arg1 == t.year && (arg2 + 1) > (t.month + 1))
											|| (arg1 == t.year
													&& (arg2 + 1) == (t.month + 1) && arg3 > t.monthDay)) {
										Toast.makeText(MyinfoActivity.this,
												"选择日期不可大于当前日期",
												Toast.LENGTH_SHORT).show();
									} else {

										UpdateType = 2;
										
										// 格式化日期格式，主要是加 0
										str_birthday = (arg1) + "-";
										if(arg2<=8){
											str_birthday+="0";
										}
										str_birthday+=(arg2+1);
										str_birthday+="-";
										if(arg3<10){
											str_birthday+="0";
										}
										str_birthday+=arg3;
										
										isDateShow = false;

										progressDialog.show();
										
										new Thread(r_Update).start();

									}
								}
							}

						}, yy, mm - 1, dd);
				datePickerDialog.show();

				break;
			case R.id.rl_myinfo_qq:
				initDialog(1);
				break;
			case R.id.rl_myinfo_mail:
				initDialog(2);
				break;
			case R.id.rl_myinfo_address:
				initDialog(3);
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

		rl_gifcontainer = (RelativeLayout) findViewById(R.id.rl_myinfo_gifcontainer);
		LayoutParams p = new LayoutParams((int) (Data.SCREENWIDTH * 0.19),
				(int) (Data.SCREENHEIGHT * 0.2));
		p.addRule(RelativeLayout.CENTER_HORIZONTAL);
		p.setMargins(0, (int) (Data.SCREENHEIGHT * 0.22), 0, 0);
		rl_gifcontainer.addView(gif_loading, p);

	}
	
	void selectPic(int which){
		Intent intent;
		File file = new File(imgStr);
		if (file.exists())
			file.delete();
		
		switch(which){
		case 0:
			intent = new Intent("android.media.action.IMAGE_CAPTURE");
			intent.putExtra(
					MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(file));
			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); 
			startActivityForResult(intent, 200);
			break;
		case 1:
			intent = new Intent(Intent.ACTION_PICK);  
			intent.setType("image/*");//相片类型
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

	// type 0Nickname 1QQ 2Email 3Address 4Introduction
	void initDialog(int type) {
		final AlertDialog alertDialog = new AlertDialog.Builder(
				MyinfoActivity.this).create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
		Window window = alertDialog.getWindow();
		window.setContentView(R.layout.dialog_updateinfo);

		WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
		lp.width = (int) (Data.SCREENWIDTH * 0.9);// 定义宽度
		lp.height = (int) (Data.SCREENWIDTH * 0.6);// 定义高度

		alertDialog.getWindow().setAttributes(lp);

		tv_dialog_title = (TextView) window
				.findViewById(R.id.tv_updatedialog_title);
		et_dialog = (EditText) window.findViewById(R.id.et_updatedialog);
		et_dialog.setFocusable(true);
		window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		iv_dialog_clear = (ImageView) window
				.findViewById(R.id.iv_updatedialog_clear);
		btn_dialog_negative = (Button) window
				.findViewById(R.id.btn_updatedialog_negative);
		btn_dialog_position = (Button) window
				.findViewById(R.id.btn_updatedialog_position);
		iv_dialog_clear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				et_dialog.setText("");
			}
		});

		btn_dialog_negative.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
			}
		});

		switch (type) {
		case 0:
			tv_dialog_title.setText("编辑昵称");
			et_dialog.setHint("请输入昵称");
			et_dialog
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							10) });
			et_dialog.setText(str_nickname);
			UpdateType = 0;
			break;
		case 1:
			tv_dialog_title.setText("编辑QQ");
			et_dialog.setHint("请输入QQ");
			et_dialog
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							10) });
			String digits = "0123456789";
			et_dialog.setKeyListener(DigitsKeyListener.getInstance(digits));
			et_dialog.setInputType(InputType.TYPE_CLASS_NUMBER);
			et_dialog.setText(str_qq);
			UpdateType = 3;
			break;
		case 2:
			tv_dialog_title.setText("编辑邮箱");
			et_dialog.setHint("请输入邮箱");
			UpdateType = 4;
			et_dialog.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
			et_dialog.setText(str_mail);
			break;
		case 3:
			tv_dialog_title.setText("编辑地址");
			et_dialog.setHint("请输入地址");
			et_dialog.setText(str_address);
			UpdateType = 5;
			break;
		case 4:
			tv_dialog_title.setText("编辑简介");
			et_dialog.setHint("请输入简介");
			et_dialog.setText(str_introduction);
			UpdateType = 6;
			break;
		}

		btn_dialog_position.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!et_dialog.getText().toString().equals("")) {
					switch (UpdateType) {
					case 0:
						str_nickname = et_dialog.getText().toString();
						break;
					case 3:
						str_qq = et_dialog.getText().toString();
						if(str_qq.length()<6){
							Toast.makeText(MyinfoActivity.this, "请输入正确的QQ",
									Toast.LENGTH_SHORT).show();
							return;
						}
						break;
					case 4:
						str_mail = et_dialog.getText().toString();
						Pattern p = Pattern.compile(emailStrPattern);
					    Matcher m = p.matcher(str_mail);
					    if(!m.matches()){
							Toast.makeText(MyinfoActivity.this, "请输入正确的邮箱地址",
									Toast.LENGTH_SHORT).show();
					    	return;
					    }
						break;
					case 5:
						str_address = et_dialog.getText().toString();
						break;
					case 6:
						str_introduction=et_dialog.getText().toString();
						break;
					}
					new Thread(r_Update).start();
					progressDialog.show();
					alertDialog.dismiss();
				}
			}
		});

	}

	Runnable r_Info = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "MyFurtherInfo"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));

			String result = new HttpPostConnection("UserInfoServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			infoHandler.sendMessage(msg);

		}
	};

	Runnable r_Pic=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
            String str=BitmapToString.bitmaptoString(bm);
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("ImgStr", str));
			
			String result = new HttpPostConnection("UpdateUserPicServer", params)
					.httpConnection();
			
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			picHandler.sendMessage(msg);
		}
	};
	
	Runnable r_Update = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			switch (UpdateType) {
			case 0:
				params.add(new BasicNameValuePair("Update", "Nickname"));
				params.add(new BasicNameValuePair("Nickname", str_nickname));
				break;
			case 1:
				params.add(new BasicNameValuePair("Update", "Sex"));
				params.add(new BasicNameValuePair("Sex", int_sex + ""));
				break;
			case 2:
				params.add(new BasicNameValuePair("Update", "Birthday"));
				params.add(new BasicNameValuePair("Birthday", str_birthday));
				break;
			case 3:
				params.add(new BasicNameValuePair("Update", "QQ"));
				params.add(new BasicNameValuePair("QQ", str_qq));
				break;
			case 4:
				params.add(new BasicNameValuePair("Update", "Email"));
				params.add(new BasicNameValuePair("Email", str_mail));
				break;
			case 5:
				params.add(new BasicNameValuePair("Update", "Address"));
				params.add(new BasicNameValuePair("Address", str_address));
				break;
			case 6:
				params.add(new BasicNameValuePair("Update", "Introduction"));
				params.add(new BasicNameValuePair("Introduction", str_introduction));
				break;
			}

			String result = new HttpPostConnection("UpdateUserInfoServer",
					params).httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			updateInfoHandler.sendMessage(msg);

		}
	};

	@SuppressLint("HandlerLeak")
	class InfoHandler extends Handler {
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();

			String result = b.getString("result");

			if (result.equals("timeout")) {
				rl_gifcontainer.setVisibility(View.GONE);
				tv_error.setVisibility(View.VISIBLE);
				tv_error.setOnClickListener(onClickListener);
			} else {
				rl_gifcontainer.setVisibility(View.GONE);
				try {
					JSONObject json = new JSONObject(result);
					
					System.out.println(result);

					tv_nickname.setText(json.getString("Nickname"));
					str_nickname = json.getString("Nickname");
					
					if (json.has("Introduction")) {
						tv_introduction.setText(json.getString("Introduction"));
						str_introduction=json.getString("Introduction");
					}

					if (json.has("Path"))
						new AsyncViewTask(MyinfoActivity.this,
								json.getString("Path"), iv_head,2)
								.execute(iv_head);

					tv_phone.setText(Data.PHONE);

					if (json.has("Sex")) {
						String[] sex = { "男", "女", "秘密" };
						tv_sex.setText(sex[json.getInt("Sex")]);
					}

					if (json.has("Birthday")) {
						tv_birthday.setText(json.getString("Birthday"));
						str_birthday = json.getString("Birthday");
					}

					if (json.has("QQ")) {
						tv_qq.setText(json.getString("QQ"));
						str_qq = json.getString("QQ");
					}

					if (json.has("Email")) {
						tv_mail.setText(json.getString("Email"));
						str_mail = json.getString("Email");
					}

					if (json.has("Address")) {
						tv_address.setText(json.getString("Address"));
						str_address = json.getString("Address");
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					rl_gifcontainer.setVisibility(View.GONE);
					Toast.makeText(MyinfoActivity.this, "身份验证失败，请重新登录...", Toast.LENGTH_SHORT).show();
				}
			}

		}
	}

	@SuppressLint("HandlerLeak")
	class UpdateInfoHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();

			String result = b.getString("result");

			if (result.equals("timeout")) {
				Toast.makeText(MyinfoActivity.this, "网络出问题咯(+﹏+）~",
						Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
				return;
			} 
			
			try {
				JSONObject json = new JSONObject(result);

				if (json.getString("Result").equals("身份验证失败")) {
					progressDialog.dismiss();
					Toast.makeText(MyinfoActivity.this, "身份验证失败，请重新登录...", Toast.LENGTH_SHORT).show();
				} else {
					System.out.println(json.toString());
					progressDialog.dismiss();
					switch (UpdateType) {
					case 0:
						tv_nickname.setText(str_nickname);
						break;
					case 1:
						String[] sex = { "男", "女", "保密" };
						tv_sex.setText(sex[int_sex] + "");
						break;
					case 2:
						tv_birthday.setText(str_birthday);
						break;
					case 3:
						tv_qq.setText(str_qq);
						break;
					case 4:
						tv_mail.setText(str_mail);
						break;
					case 5:
						tv_address.setText(str_address);
						break;
					case 6:
						tv_introduction.setText(str_introduction);
						break;
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	@SuppressLint("HandlerLeak")
	class PicHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();

			String result = b.getString("result");

			if (result.equals("timeout")) {
				Toast.makeText(MyinfoActivity.this, "网络出问题咯(+﹏+）~",
						Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
				return;
			}
			
			try {
				JSONObject json = new JSONObject(result);

				if (json.getString("Result").equals("身份验证失败")) {
					progressDialog.dismiss();
					Toast.makeText(MyinfoActivity.this, "身份验证失败，请重新登录...", Toast.LENGTH_SHORT).show();
				} else {
					progressDialog.dismiss();
					iv_head.setImageBitmap(bm);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isDateShow) {
				isDateShow = false;
				datePickerDialog.dismiss();
			} else {
				setResult(RESULT_OK, MyinfoActivity.this.getIntent());
				finish();
			}
		}

		return false;

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 200) {
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
				MyinfoActivity.this.startActivityForResult(intent, 202);

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (requestCode == 201) {
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

		} else if (requestCode == 202) {
			try {
				bm = data.getParcelableExtra("data");
				if (bm != null) {
					new Thread(r_Pic).start();
					progressDialog.show();
				}
			} catch (Exception e) {
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

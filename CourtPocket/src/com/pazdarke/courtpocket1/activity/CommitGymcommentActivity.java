package com.pazdarke.courtpocket1.activity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.choosepics.ImgFileListActivity;
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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar.OnRatingBarChangeListener;

public class CommitGymcommentActivity extends Activity {

	ProgressDialog progressDialog;

	JSONObject info;
	String billID;
	Boolean isSwimming;

	EditText et_comment;
	RatingBar rb_rate;
	TextView tv_rate;

	LinearLayout ll_photocontainer;
	ArrayList<Bitmap> photoBm;
	File file;

	public static int photoNum;

	String imgStr = Environment.getExternalStorageDirectory()
			+ "/CourtPocket/temp.jpg";

	ToastHandler toastHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_commit_gymcomment);

		AppManager.getAppManager().addActivity(this);

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("正在用力提交您的评论...");
		progressDialog.setCancelable(false);

		photoNum = 0;
		photoBm = new ArrayList<Bitmap>();

		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_commitgymcomment_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		ll_photocontainer = (LinearLayout) findViewById(R.id.ll_commitgymcomment_photocontainer);

		Intent intent = getIntent();
		try {
			info = new JSONObject(intent.getStringExtra("info"));
			billID = intent.getStringExtra("billID");
			isSwimming = intent.getBooleanExtra("isSwimming", false);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (!isSwimming)
			init();
		else
			init_swimming();

		RelativeLayout rl_camera = (RelativeLayout) findViewById(R.id.rl_commitgymcomment_camera);
		rl_camera.setOnClickListener(onClickListener);

		IntentFilter filter = new IntentFilter("RefreshPhoto");
		registerReceiver(broadcastReceiver, filter);

		et_comment = (EditText) findViewById(R.id.et_commitgymcomment_comment);
		rb_rate = (RatingBar) findViewById(R.id.rb_commitgymcomment_rate);
		tv_rate = (TextView) findViewById(R.id.tv_commitgymcomment_rate);
		rb_rate.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating,
					boolean fromUser) {
				// TODO Auto-generated method stub
				try {
					switch ((int) rating) {
					case 1:
						tv_rate.setText("太烂了，下次再也不来了");
						break;
					case 2:
						tv_rate.setText("不满意，不太想再来了");
						break;
					case 3:
						tv_rate.setText("一般，很多地方需要改善");
						break;
					case 4:
						tv_rate.setText("还不错，各方面都比较满意");
						break;
					case 5:
						tv_rate.setText("非常棒，期待下一次再来运动");
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		Button btn_submit = (Button) findViewById(R.id.btn_commitgymcomment_submit);
		btn_submit.setOnClickListener(onClickListener);

		toastHandler = new ToastHandler();

	}

	void init() {
		try {
			ImageView iv_logo = (ImageView) findViewById(R.id.iv_commitgymcomment_gymlogo);
			if (info.has("Path")) {
				new AsyncViewTask(CommitGymcommentActivity.this,
						info.getString("Path"), iv_logo, 5)
						.execute(iv_logo);
				// 异步加载图片
			} else {
				iv_logo.setImageResource(R.color.lightGrey);
			}
			iv_logo.setOnClickListener(onClickListener);

			((TextView) findViewById(R.id.tv_commitgymcomment_gymname))
					.setText(info.getString("GymName"));
			((TextView) findViewById(R.id.tv_commitgymcomment_price))
					.setText("总价：￥" + Data.doubleTrans(info.getDouble("Price")));
			((TextView) findViewById(R.id.tv_commitgymcomment_generatetime))
					.setText("下单时间："
							+ info.getString("GenerateTime").substring(0, 19));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void init_swimming() {
		try {
			ImageView iv_logo = (ImageView) findViewById(R.id.iv_commitgymcomment_gymlogo);
			if (info.has("GymPic")) {
				new AsyncViewTask(CommitGymcommentActivity.this,
						info.getString("GymPic"), iv_logo, 5)
						.execute(iv_logo);
				// 异步加载图片
			}
			iv_logo.setOnClickListener(onClickListener);

			((TextView) findViewById(R.id.tv_commitgymcomment_gymname))
					.setText(info.getString("GymName"));
			((TextView) findViewById(R.id.tv_commitgymcomment_price))
					.setText("价格：￥" + Data.doubleTrans(info.getDouble("Price")));
			((TextView) findViewById(R.id.tv_commitgymcomment_generatetime))
					.setText("购买时间："
							+ info.getString("GenerateTime").substring(0, 19));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_commitgymcomment_leftarrow:
				finish();
				break;
			case R.id.iv_commitgymcomment_gymlogo:
				Intent intent = new Intent(CommitGymcommentActivity.this,
						CourtinfoActivity.class);
				try {
					intent.putExtra("ID", info.getInt("GymID") + "");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				startActivity(intent);
				break;
			case R.id.rl_commitgymcomment_camera:
				if (photoBm.size() < 3) {
					String[] picSrc = new String[] { "拍照", "相册" };
					Dialog picDialog = new AlertDialog.Builder(
							CommitGymcommentActivity.this).setItems(picSrc,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
										file = new File(imgStr);
										if (file.exists())
											file.delete();

										Intent intent = new Intent(
												"android.media.action.IMAGE_CAPTURE");
										intent.putExtra(
												MediaStore.EXTRA_OUTPUT,
												Uri.fromFile(file));
										intent.putExtra(
												MediaStore.EXTRA_VIDEO_QUALITY,
												1);
										startActivityForResult(intent, 10);
										break;
									case 1:
										Intent intent1 = new Intent(
												CommitGymcommentActivity.this,
												ImgFileListActivity.class);
										photoNum = photoBm.size();
										startActivity(intent1);
										break;
									}
								}
							}).create();
					picDialog.show();

				} else {
					Toast.makeText(CommitGymcommentActivity.this,
							"已不能添加更多照片啦~", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.btn_commitgymcomment_submit:
				if (!et_comment.getText().toString().equals("")) {
					if (!isSwimming)
						new Thread(r_Comment).start();
					else
						new Thread(r_CommentSwimming).start();
					progressDialog.show();
				} else {
					Toast.makeText(CommitGymcommentActivity.this,
							"您还没有填写评论内容...", Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	Runnable r_Comment = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "NewComment"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("Content", et_comment.getText()
					.toString()));
			params.add(new BasicNameValuePair("Rate", rb_rate.getRating() + ""));
			params.add(new BasicNameValuePair("BillID", billID));
			if (photoBm.size() > 0) {
				params.add(new BasicNameValuePair("PicNum", photoBm.size() + ""));
				for (int i = 0; i < photoBm.size(); i++) {
					params.add(new BasicNameValuePair("PicStr" + i,
							BitmapToString.bitmaptoString(photoBm.get(i))));
				}
			}

			String result = new HttpPostConnection("CommentServer", params)
					.httpConnection();

			if (result.equals("timeout")) {
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("message", "您的网络好像有问题呢~>_<~+");
				msg.setData(b);
				toastHandler.sendMessage(msg);
				return;
			}

			try {
				JSONObject json = new JSONObject(result);
				String commentResult = json.getString("Result");
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("message", commentResult);
				msg.setData(b);
				toastHandler.sendMessage(msg);

				if (commentResult.equals("评论成功")) {
					Intent intent = new Intent("RefreshBilllist");
					sendBroadcast(intent);
					MybillInfoActivity.instance_mybillinfo.finish();
					finish();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("message", "身份验证失败，请重新登录...");
				msg.setData(b);
				toastHandler.sendMessage(msg);
			}

		}
	};

	Runnable r_CommentSwimming = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "Comment"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("Content", et_comment.getText()
					.toString()));
			params.add(new BasicNameValuePair("Rate", rb_rate.getRating() + ""));
			params.add(new BasicNameValuePair("CardID", billID));
			if (photoBm.size() > 0) {
				params.add(new BasicNameValuePair("PicNum", photoBm.size() + ""));
				for (int i = 0; i < photoBm.size(); i++) {
					params.add(new BasicNameValuePair("PicStr" + i,
							BitmapToString.bitmaptoString(photoBm.get(i))));
				}
			}

			String result = new HttpPostConnection("CardServer", params)
					.httpConnection();

			if (result.equals("timeout")) {
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("message", "您的网络好像有问题呢~>_<~+");
				msg.setData(b);
				toastHandler.sendMessage(msg);
				return;
			}

			try {
				JSONObject json = new JSONObject(result);
				String commentResult = json.getString("Result");
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("message", commentResult);
				msg.setData(b);
				toastHandler.sendMessage(msg);

				if (commentResult.equals("评论成功")) {
					Intent intent = new Intent("RefreshMycard");
					sendBroadcast(intent);
					finish();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("message", "身份验证失败，请重新登录...");
				msg.setData(b);
				toastHandler.sendMessage(msg);
			}

		}
	};

	class ToastHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Bundle b = msg.getData();

			progressDialog.dismiss();
			Toast.makeText(CommitGymcommentActivity.this,
					b.getString("message"), Toast.LENGTH_LONG).show();
		}
	}

	// 10 拍照后回调
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {

			if (!file.exists())
				return;

			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
					(int) (Data.SCREENWIDTH * 0.2),
					(int) (Data.SCREENWIDTH * 0.2));
			p.setMargins((int) (Data.SCREENWIDTH * 0.02), 0, 0, 0);

			switch (requestCode) {
			case 10:
				Bitmap bm = compressImage(compressImageFromFile(imgStr));
				photoBm.add(bm);
				ImageView iv = new ImageView(this);
				iv.setLayoutParams(p);
				iv.setImageBitmap(bm);
				ll_photocontainer.addView(iv);
				iv.setOnClickListener(new PicOnClickListener(bm));

				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class PicOnClickListener implements OnClickListener {

		Bitmap bm;

		PicOnClickListener(Bitmap bm) {
			this.bm = bm;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			ll_photocontainer.removeView(v);
			photoBm.remove(bm);
		}

	}

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			try {
				Bundle bundle = intent.getExtras();
				ArrayList<String> listfile = bundle.getStringArrayList("files");

				LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
						(int) (Data.SCREENWIDTH * 0.2),
						(int) (Data.SCREENWIDTH * 0.2));
				p.setMargins((int) (Data.SCREENWIDTH * 0.02), 0, 0, 0);
				// 转成Bitmap存储到数组中
				Bitmap[] temp = new Bitmap[listfile.size()];
				for (int i = 0; i < listfile.size(); i++) {
					temp[i] = compressImage(compressImageFromFile(listfile
							.get(i)));
					photoBm.add(temp[i]);
				}
				for (int i = 0; i < listfile.size(); i++) {
					ImageView iv1 = new ImageView(CommitGymcommentActivity.this);
					iv1.setLayoutParams(p);
					iv1.setImageBitmap(temp[i]);
					ll_photocontainer.addView(iv1);
					iv1.setOnClickListener(new PicOnClickListener(temp[i]));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
	};

	private Bitmap compressImageFromFile(String srcPath) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;// 只读边,不读内容
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		float hh = 400f;
		float ww = 300f;
		int be = 1;
		if (w > h && w > ww) {
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// 设置采样率

		newOpts.inPreferredConfig = Config.ARGB_8888;// 该模式是默认的,可不设
		newOpts.inPurgeable = true;// 同时设置才会有效
		newOpts.inInputShareable = true;// 。当系统内存不够时候图片自动被回收

		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return bitmap;
	}

	private Bitmap compressImage(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 80, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 80;
		System.out.println(baos.toByteArray().length / 1024);
		while (baos.toByteArray().length / 1024 > 60 && options > 5) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			options -= 5;// 每次都减少10
			System.out.println(baos.toByteArray().length / 1024);
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		return bitmap;
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

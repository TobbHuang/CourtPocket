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
import android.net.rtp.RtpStream;
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
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CommitMatchcommentActivity extends Activity {

	ProgressDialog progressDialog;

	JSONObject info;
	String billID;

	EditText et_comment;
	RatingBar rb_gymrate;
	TextView tv_gymrate;
	RatingBar rb_opponentrate;
	TextView tv_opponentrate;

	LinearLayout ll_photocontainer;
	ArrayList<Bitmap> photoBm;
	File file;

	public static int photoNum;

	String imgStr = Environment.getExternalStorageDirectory()
			+ "/CourtPocket/temp.jpg";

	ToastHandler toastHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.activity_commit_matchcomment);

			AppManager.getAppManager().addActivity(this);

			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���������ύ��������...");
			progressDialog.setCancelable(false);

			photoNum = 0;
			photoBm = new ArrayList<Bitmap>();

			ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_commitmatchcomment_leftarrow);
			iv_leftarrow.setOnClickListener(onClickListener);

			ll_photocontainer = (LinearLayout) findViewById(R.id.ll_commitmatchcomment_photocontainer);

			Intent intent = getIntent();
			try {
				info = new JSONObject(intent.getStringExtra("info"));
				billID = intent.getStringExtra("billID");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			init();

			RelativeLayout rl_camera = (RelativeLayout) findViewById(R.id.rl_commitmatchcomment_camera);
			rl_camera.setOnClickListener(onClickListener);

			IntentFilter filter = new IntentFilter("RefreshPhoto");
			registerReceiver(broadcastReceiver, filter);

			et_comment = (EditText) findViewById(R.id.et_commitmatchcomment_comment);
			rb_gymrate = (RatingBar) findViewById(R.id.rb_commitmatchcomment_gymrate);
			tv_gymrate = (TextView) findViewById(R.id.tv_commitmatchcomment_gymrate);
			rb_gymrate
					.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

						@Override
						public void onRatingChanged(RatingBar ratingBar,
								float rating, boolean fromUser) {
							// TODO Auto-generated method stub
							try {
								switch ((int) rating) {
								case 1:
									tv_gymrate.setText("̫���ˣ��´���Ҳ������");
									break;
								case 2:
									tv_gymrate.setText("�����⣬��̫��������");
									break;
								case 3:
									tv_gymrate.setText("һ�㣬�ܶ�ط���Ҫ����");
									break;
								case 4:
									tv_gymrate.setText("�����������涼�Ƚ�����");
									break;
								case 5:
									tv_gymrate.setText("�ǳ������ڴ���һ�������˶�");
									break;
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

			rb_opponentrate = (RatingBar) findViewById(R.id.rb_commitmatchcomment_opponentrate);
			tv_opponentrate = (TextView) findViewById(R.id.tv_commitmatchcomment_opponentrate);
			rb_opponentrate
					.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

						@Override
						public void onRatingChanged(RatingBar ratingBar,
								float rating, boolean fromUser) {
							// TODO Auto-generated method stub
							switch ((int) rating) {
							case 1:
								tv_opponentrate.setText("���ʲ�̫�ã�������");
								break;
							case 2:
								tv_opponentrate.setText("������Է���һЩ����");
								break;
							case 3:
								tv_opponentrate.setText("һ�㣬���Խ��ܶԷ��ı���");
								break;
							case 4:
								tv_opponentrate.setText("�ܲ���һ���˶��ܿ���");
								break;
							case 5:
								tv_opponentrate.setText("�ǳ������ڴ���һ��һ���˶�");
								break;
							}
						}
					});

			Button btn_submit = (Button) findViewById(R.id.btn_commitmatchcomment_submit);
			btn_submit.setOnClickListener(onClickListener);

			toastHandler = new ToastHandler();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	void init() {
		try {
			ImageView iv_logo = (ImageView) findViewById(R.id.iv_commitmatchcomment_gymlogo);
			if (info.has("GymPath")) {
				new AsyncViewTask(CommitMatchcommentActivity.this,
						info.getString("GymPath"), iv_logo, 5).execute(iv_logo);
				// �첽����ͼƬ
			} else {
				iv_logo.setImageResource(R.color.lightGrey);
			}
			iv_logo.setOnClickListener(onClickListener);

			((TextView) findViewById(R.id.tv_commitmatchcomment_gymname))
					.setText(info.getString("GymName"));
			((TextView) findViewById(R.id.tv_commitmatchcomment_price))
					.setText("�ܼۣ���" + Data.doubleTrans(info.getDouble("Price")));
			((TextView) findViewById(R.id.tv_commitmatchcomment_generatetime))
					.setText("�µ�ʱ�䣺"
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
			case R.id.iv_commitmatchcomment_leftarrow:
				finish();
				break;
			case R.id.iv_commitmatchcomment_gymlogo:
				Intent intent = new Intent(CommitMatchcommentActivity.this,
						CourtinfoActivity.class);
				try {
					intent.putExtra("ID", info.getInt("GymID") + "");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				startActivity(intent);
				break;
			case R.id.rl_commitmatchcomment_camera:
				if (photoBm.size() < 3) {
					String[] picSrc = new String[] { "����", "���" };
					Dialog picDialog = new AlertDialog.Builder(
							CommitMatchcommentActivity.this).setItems(picSrc,
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
												CommitMatchcommentActivity.this,
												ImgFileListActivity.class);
										photoNum = photoBm.size();
										startActivity(intent1);
										break;
									}
								}
							}).create();
					picDialog.show();

				} else {
					Toast.makeText(CommitMatchcommentActivity.this,
							"�Ѳ�����Ӹ�����Ƭ��~", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.btn_commitmatchcomment_submit:
				if (!et_comment.getText().toString().equals("")) {
					new Thread(r_Comment).start();
					progressDialog.show();
				} else {
					Toast.makeText(CommitMatchcommentActivity.this,
							"����û����д��������...", Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	Runnable r_Comment = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("Request", "NewComment"));
				params.add(new BasicNameValuePair("UserID", Data.USERID));
				params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
				params.add(new BasicNameValuePair("Content", et_comment
						.getText().toString()));
				params.add(new BasicNameValuePair("Rate", rb_gymrate
						.getRating() + ""));
				params.add(new BasicNameValuePair("BillID", billID));
				params.add(new BasicNameValuePair("OpponentUserID", info
						.getInt("OpponentUserID") + ""));
				params.add(new BasicNameValuePair("OpponentRate",
						rb_opponentrate.getRating() + ""));
				if (photoBm.size() > 0) {
					params.add(new BasicNameValuePair("PicNum", photoBm.size()
							+ ""));
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
					b.putString("message", "�������������������~>_<~+");
					msg.setData(b);
					toastHandler.sendMessage(msg);
					return;
				}

				JSONObject json = new JSONObject(result);
				String commentResult = json.getString("Result");
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("message", commentResult);
				msg.setData(b);
				toastHandler.sendMessage(msg);

				if (commentResult.equals("���۳ɹ�")) {
					Intent intent = new Intent("RefreshMatchBilllist");
					sendBroadcast(intent);
					MatchbillInfoActivity.instance_matchbillinfo.finish();
					finish();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("message", "�����֤ʧ�ܣ������µ�¼...");
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
			Toast.makeText(CommitMatchcommentActivity.this,
					b.getString("message"), Toast.LENGTH_LONG).show();
		}
	}

	// 10 ���պ�ص�
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
				// ת��Bitmap�洢��������
				Bitmap[] temp = new Bitmap[listfile.size()];
				for (int i = 0; i < listfile.size(); i++) {
					temp[i] = compressImage(compressImageFromFile(listfile
							.get(i)));
					photoBm.add(temp[i]);
				}
				for (int i = 0; i < listfile.size(); i++) {
					ImageView iv1 = new ImageView(
							CommitMatchcommentActivity.this);
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
		// unregisterReceiver(broadcastReceiver);
	};

	private Bitmap compressImageFromFile(String srcPath) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;// ֻ����,��������
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
		newOpts.inSampleSize = be;// ���ò�����

		newOpts.inPreferredConfig = Config.ARGB_8888;// ��ģʽ��Ĭ�ϵ�,�ɲ���
		newOpts.inPurgeable = true;// ͬʱ���òŻ���Ч
		newOpts.inInputShareable = true;// ����ϵͳ�ڴ治��ʱ��ͼƬ�Զ�������

		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return bitmap;
	}

	private Bitmap compressImage(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 80, baos);// ����ѹ������������100��ʾ��ѹ������ѹ��������ݴ�ŵ�baos��
		int options = 80;
		System.out.println(baos.toByteArray().length / 1024);
		while (baos.toByteArray().length / 1024 > 60 && options > 5) { // ѭ���ж����ѹ����ͼƬ�Ƿ����100kb,���ڼ���ѹ��
			baos.reset();// ����baos�����baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// ����ѹ��options%����ѹ��������ݴ�ŵ�baos��
			options -= 5;// ÿ�ζ�����10
			System.out.println(baos.toByteArray().length / 1024);
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// ��ѹ���������baos��ŵ�ByteArrayInputStream��
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// ��ByteArrayInputStream��������ͼƬ
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

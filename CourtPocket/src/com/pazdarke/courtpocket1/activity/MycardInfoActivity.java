package com.pazdarke.courtpocket1.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.pazdarke.courtpocket1.tools.listview.AsyncViewTask;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class MycardInfoActivity extends Activity {
	
	JSONObject info;
	
	CardUseHandler cardUseHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_mycard_info);
		
		AppManager.getAppManager().addActivity(this);
		
		initView();
		
		cardUseHandler=new CardUseHandler();
		
		new Thread(r_CardUse).start();
		
	}

	private void initView() {
		// TODO Auto-generated method stub
		try{
			
			ImageView iv_leftarrow=(ImageView)findViewById(R.id.iv_mycardinfo_leftarrow);
			iv_leftarrow.setOnClickListener(onClickListener);
			
			info=new JSONObject(getIntent().getStringExtra("info"));
			
			ImageView iv_gymlogo=(ImageView)findViewById(R.id.iv_mycardinfo_gymlogo);
			iv_gymlogo.setOnClickListener(onClickListener);
			if(info.has("GymPic")){
				String path = info.getString("GymPic");
				new AsyncViewTask(this, path, iv_gymlogo,5).execute(iv_gymlogo);
			}
			
			int type=info.getInt("Type");
			ImageView iv_type=(ImageView)findViewById(R.id.iv_mycardinfo_type);
			switch(type){
			case 11:
				iv_type.setImageResource(R.drawable.ic_adultcard_once);
				break;
			case 12:
				iv_type.setImageResource(R.drawable.ic_adultcard_twice);
				break;
			case 21:
				iv_type.setImageResource(R.drawable.ic_childcard_once);
				break;
			case 22:
				iv_type.setImageResource(R.drawable.ic_childcard_once);
				break;
			}
			
			TextView tv_content=(TextView)findViewById(R.id.tv_mycardinfo_content);
			tv_content.setText(info.getString("Content"));
			
			TextView tv_gymname=(TextView)findViewById(R.id.tv_mycardinfo_gymname);
			tv_gymname.setText(info.getString("GymName"));
			
			TextView tv_price=(TextView)findViewById(R.id.tv_mycardinfo_price);
			tv_price.setText("￥"+Data.doubleTrans(info.getDouble("Price")));
			
			TextView tv_generatetime=(TextView)findViewById(R.id.tv_mycardinfo_generatetime);
			tv_generatetime.setText("购买时间："+info.getString("GenerateTime").substring(0, 19));
			
			TextView tv_left=(TextView)findViewById(R.id.tv_mycardinfo_left);
			int left=info.getInt("Left");
			if(left==-1){
				tv_left.setText("不限次数");
			} else{
				tv_left.setText("剩余"+left+"次");
			}
			
			if (info.has("ValidityDate")) {
				TextView tv_validitydate = (TextView) findViewById(R.id.tv_mycardinfo_validitydate);
				tv_validitydate.setText(info.getString("ValidityDate")+"过期");
			}
			
			TextView tv_code=(TextView)findViewById(R.id.tv_mycardinfo_code);
			tv_code.setText(info.getString("Code"));
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	OnClickListener onClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.iv_mycardinfo_leftarrow:
				finish();
				break;
			case R.id.iv_mycardinfo_gymlogo:
				try {
					Intent intent = new Intent(MycardInfoActivity.this,
							CourtinfoActivity.class);
					intent.putExtra("ID", info.getInt("GymID") + "");
					startActivity(intent);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			}
		}
	};
	
	Runnable r_CardUse =new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "CardUse"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("CardID", MycardInfoActivity.this
					.getIntent().getStringExtra("CardID")));

			String result = new HttpPostConnection("CardServer", params)
					.httpConnection();
			
			Message msg=new Message();
			Bundle b=new Bundle();
			b.putString("result", result);
			msg.setData(b);
			cardUseHandler.sendMessage(msg);
			
		}
	};
	
	@SuppressLint("HandlerLeak")
	class CardUseHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			TextView tv_coderecord = (TextView) findViewById(R.id.tv_mycardinfo_coderecord);

			String result = msg.getData().getString("result");

			if (result.equals("timeout")) {
				tv_coderecord.setText("网络异常，暂无法获取卡卷验证记录");
				return;
			}

			try {

				JSONObject json = new JSONObject(result);

				int i = 0;
				String str = "";
				while (true) {
					if (json.has("UseTime" + i)) {
						str += json.getString("UseTime" + i).substring(0, 19)
								+ " 第" + (i + 1) + "次被验证\n";
						i++;
					} else {
						if (i == 0) {
							str = "暂无验证记录";
						}
						break;
					}
				}

				tv_coderecord.setText(str);
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

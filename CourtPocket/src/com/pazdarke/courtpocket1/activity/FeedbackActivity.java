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
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FeedbackActivity extends Activity {
	
	ProgressDialog progressDialog;
	
	EditText et_content,et_phone;
	
	FeedbackHandler feedbackHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_feedback);
		
		AppManager.getAppManager().addActivity(this);
		
		progressDialog=new ProgressDialog(this);
		progressDialog.setCancelable(true);
		progressDialog.setMessage("正在努力提交...");
		
		initView();
		
		feedbackHandler=new FeedbackHandler();
		
	}

	private void initView() {
		// TODO Auto-generated method stub
		
		ImageView iv_leftarrow=(ImageView)findViewById(R.id.iv_feedback_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);
		
		et_content=(EditText)findViewById(R.id.et_feedback_content);
		
		et_phone=(EditText)findViewById(R.id.et_feedback_phone);
		
		ImageView iv_clear=(ImageView)findViewById(R.id.iv_feedback_clear);
		iv_clear.setOnClickListener(onClickListener);
		
		ImageView iv_clear2=(ImageView)findViewById(R.id.iv_feedback_clear2);
		iv_clear2.setOnClickListener(onClickListener);
		
		TextView tv_commit=(TextView)findViewById(R.id.tv_feedback_commit);
		tv_commit.setOnClickListener(onClickListener);
		
	}

	OnClickListener onClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.iv_feedback_leftarrow:
				finish();
				break;
			case R.id.iv_feedback_clear:
				et_content.setText("");
				break;
			case R.id.iv_feedback_clear2:
				et_phone.setText("");
				break;
			case R.id.tv_feedback_commit:
				if(!et_content.getText().toString().equals("")){
					progressDialog.show();
					new Thread(r_Feedback).start();
				}
				break;
			}
		}
	};
	
    Runnable r_Feedback=new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				
				String contact;
				if (!et_phone.getText().toString().equals("")) {
					contact="Contact:"+et_phone
							.getText().toString()+" ";
				} else {
					contact = "Contact:未填 ";
				}

				if (Data.ISLOGIN)
					contact+="Phone:"+Data.PHONE;
				else
					contact+="Phone:未登录";
				
				params.add(new BasicNameValuePair("Contact", contact));

				params.add(new BasicNameValuePair("Content", et_content
						.getText().toString()));

				String result = new HttpPostConnection("FeedbackServer", params)
						.httpConnection();

				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("result", result);
				msg.setData(b);
				feedbackHandler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	@SuppressLint("HandlerLeak")
	class FeedbackHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			try {

				progressDialog.dismiss();

				System.out.println(msg.getData().getString("result"));

				String result = msg.getData().getString("result");

				if (result.equals("timeout")) {
					Toast.makeText(FeedbackActivity.this, "网络出问题了",
							Toast.LENGTH_SHORT).show();
					return;
				}

				JSONObject json = new JSONObject(result);

				if (json.getBoolean("Result")) {
					Toast.makeText(FeedbackActivity.this, "感谢您的反馈(*^__^*)",
							Toast.LENGTH_SHORT).show();
					finish();
				} else {
					Toast.makeText(FeedbackActivity.this,
							"提交反馈失败，是不是姿势不对呢(+﹏+)~", Toast.LENGTH_SHORT).show();
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

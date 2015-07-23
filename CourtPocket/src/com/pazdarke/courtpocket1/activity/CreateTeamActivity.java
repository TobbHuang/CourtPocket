package com.pazdarke.courtpocket1.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

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
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CreateTeamActivity extends Activity {
	
	Spinner spinner;
	
	EditText et_teamname;
	
	CreateteamHandler createteamHandler;
	
	ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_create_team);
		
		AppManager.getAppManager().addActivity(this);
		
		progressDialog=new ProgressDialog(this);
		progressDialog.setMessage("正在努力创建中…");
		progressDialog.setCancelable(false);
		
		initView();
		
		createteamHandler=new CreateteamHandler();
		
	}

	private void initView() {
		// TODO Auto-generated method stub
		
		et_teamname=(EditText)findViewById(R.id.et_createteam_teamname);
		
		ArrayList<String> list = new ArrayList<String>();
		list.add("请选择球队类型");
		list.add("足球队");
		//list.add("篮球队");
		//list.add("排球队");
		
		spinner=(Spinner)findViewById(R.id.sp_createteam_teamtype);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.layout_createteam_spinner, R.id.tv_spinnerlayout, list) {
			@Override
			public View getDropDownView(int position, View convertView,
					ViewGroup parent) {
				// TODO Auto-generated method stub
				
				if (convertView == null) {
					convertView = getLayoutInflater().inflate(
							R.layout.item_courtlist_spinner, parent, false);
				}
				TextView label = (TextView) convertView
						.findViewById(R.id.tv_courtlistitem);
				label.setText(getItem(position));
				label.setTextColor(getResources().getColor(R.color.darkGrey));
				ImageView icon = (ImageView) convertView
						.findViewById(R.id.iv_courtlistitem);
				switch (position) {
				case 0:
					icon.setVisibility(View.GONE);
					label.setTextColor(getResources().getColor(R.color.blue));
					break;
				case 1:
					icon.setVisibility(View.VISIBLE);
					icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_courtlist_soccer1));
					break;
				/*case 2:
					icon.setVisibility(View.VISIBLE);
					icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_courtlist_basketball1));
					break;
				case 3:
					icon.setVisibility(View.VISIBLE);
					icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_courtlist_volleyball1));
					break;*/

				}

				return convertView;
			}
		};

		spinner.setAdapter(adapter);
		
		ImageView iv_leftarrow=(ImageView)findViewById(R.id.iv_createteam_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);
		
		TextView tv_create=(TextView)findViewById(R.id.tv_createteam_create);
		tv_create.setOnClickListener(onClickListener);
		

	}
	
	OnClickListener onClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.iv_createteam_leftarrow:
				finish();
				break;
			case R.id.tv_createteam_create:
				if(et_teamname.getText().toString().equals("")){
					Toast.makeText(CreateTeamActivity.this, "请填写球队名称", Toast.LENGTH_SHORT).show();
				} else if(spinner.getSelectedItemPosition()==0){
					Toast.makeText(CreateTeamActivity.this, "请选择球队类型", Toast.LENGTH_SHORT).show();
				} else{
					progressDialog.show();
					new Thread(r_CreateTeam).start();
				}
				break;
			}
		}
	};

	Runnable r_CreateTeam = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "NewTeam"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("Type", (spinner
					.getSelectedItemPosition()) + ""));
			params.add(new BasicNameValuePair("Name", et_teamname.getText()
					.toString()));

			String result = new HttpPostConnection("TeamServer", params)
					.httpConnection();
			
			Message msg=new Message();
			Bundle b=new Bundle();
			b.putString("result", result);
			msg.setData(b);
			createteamHandler.sendMessage(msg);
		}
	};
	
	@SuppressLint("HandlerLeak")
	class CreateteamHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			String result=msg.getData().getString("result");
			System.out.println(result);
			
			progressDialog.dismiss();
			if(result.equals("timeout")){
				Toast.makeText(CreateTeamActivity.this, "网络状况似乎不太好…", Toast.LENGTH_SHORT).show();
				return;
			}
			
			Toast.makeText(CreateTeamActivity.this, "创建成功(^_^)", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent("RefreshTeamlist");
			sendBroadcast(intent);
			finish();
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

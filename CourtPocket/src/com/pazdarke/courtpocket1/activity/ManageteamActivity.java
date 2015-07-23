package com.pazdarke.courtpocket1.activity;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ManageteamActivity extends Activity {
	
	String teamID;
	String teamName;
	public static Activity instance_manageteam;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_manageteam);
		
		AppManager.getAppManager().addActivity(this);
		
		instance_manageteam=this;
		
		teamID=getIntent().getStringExtra("ID");
		teamName=getIntent().getStringExtra("TeamName");
		
		initView();
		
	}

	private void initView() {
		// TODO Auto-generated method stub
		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_manageteam_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		RelativeLayout rl_member = (RelativeLayout) findViewById(R.id.rl_manageteam_member);
		rl_member.setOnClickListener(onClickListener);

		RelativeLayout rl_invite = (RelativeLayout) findViewById(R.id.rl_manageteam_invite);
		rl_invite.setOnClickListener(onClickListener);
		
		RelativeLayout rl_intro = (RelativeLayout) findViewById(R.id.rl_manageteam_intro);
		rl_intro.setOnClickListener(onClickListener);

		RelativeLayout rl_message = (RelativeLayout) findViewById(R.id.rl_manageteam_message);
		rl_message.setOnClickListener(onClickListener);
		
		RelativeLayout rl_fightbill = (RelativeLayout) findViewById(R.id.rl_manageteam_fightbill);
		rl_fightbill.setOnClickListener(onClickListener);

		RelativeLayout rl_changeleader = (RelativeLayout) findViewById(R.id.rl_manageteam_changeleader);
		rl_changeleader.setOnClickListener(onClickListener);
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent;
			switch (v.getId()) {
			case R.id.iv_manageteam_leftarrow:
				finish();
				break;
			case R.id.rl_manageteam_member:
				intent=new Intent(ManageteamActivity.this,ManageTeammemberActivity.class);
				intent.putExtra("TeamID", teamID);
				startActivity(intent);
				break;
			case R.id.rl_manageteam_intro:
				intent=new Intent(ManageteamActivity.this,TeamintroActivity.class);
				intent.putExtra("TeamID", teamID);
				intent.putExtra("Introduction", ManageteamActivity.this.getIntent().getStringExtra("Introduction"));
				startActivity(intent);
				break;
			case R.id.rl_manageteam_invite:
				intent=new Intent(ManageteamActivity.this,InviteActivity.class);
				intent.putExtra("ID", teamID);
				startActivity(intent);
				break;
			case R.id.rl_manageteam_message:
				intent=new Intent(ManageteamActivity.this,TeammessageActivity.class);
				intent.putExtra("ID", teamID);
				startActivity(intent);
				break;
			case R.id.rl_manageteam_fightbill:
				intent=new Intent(ManageteamActivity.this,FightbillActivity.class);
				intent.putExtra("TeamID", teamID);
				intent.putExtra("TeamName", teamName);
				intent.putExtra("UserID", Data.USERID);
				intent.putExtra("Passcode", Data.PASSCODE);
				startActivity(intent);
				break;
			case R.id.rl_manageteam_changeleader:
				intent=new Intent(ManageteamActivity.this,TransforLeaderActivity.class);
				intent.putExtra("TeamID", teamID);
				startActivity(intent);
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

}

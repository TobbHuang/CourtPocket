package com.pazdarke.courtpocket1.tools.listview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.activity.BookActivity;
import com.pazdarke.courtpocket1.activity.CourtinfoActivity;
import com.pazdarke.courtpocket1.activity.FightSelectteamActivity;
import com.pazdarke.courtpocket1.activity.LoginActivity;
import com.pazdarke.courtpocket1.activity.TeamInfoActivity;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.listview.FightbillAdapter.MyOnclickListener;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FightlistAdapter extends BaseAdapter {

	ProgressDialog progressDialog;
	CheckPasscodeHandler checkPasscodeHandler;
	TeamlistHandler teamlistHandler;

	Context context;
	ArrayList<HashMap<String, Object>> mylist;
	int layoutID;

	public FightlistAdapter(Context context,
			ArrayList<HashMap<String, Object>> mylist, int layoutID) {
		this.context = context;
		this.mylist = mylist;
		this.layoutID = layoutID;

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mylist.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		try {
			TeamlistViewHolder holder;

			if (convertView == null) {

				convertView = LayoutInflater.from(context).inflate(layoutID,
						null);
				holder = new TeamlistViewHolder();
				holder.iv_teamlogo1 = (ImageView) convertView
						.findViewById(R.id.iv_itemfight_teamlogo1);
				holder.tv_teamname1 = (TextView) convertView
						.findViewById(R.id.tv_itemfight_teamname1);
				holder.tv_sports = (TextView) convertView
						.findViewById(R.id.tv_itemfight_sports);
				holder.iv_teamlogo2 = (ImageView) convertView
						.findViewById(R.id.iv_itemfight_teamlogo2);
				holder.tv_teamname2 = (TextView) convertView
						.findViewById(R.id.tv_itemfight_teamname2);
				holder.iv_gymlogo = (ImageView) convertView
						.findViewById(R.id.iv_itemfight_gymlogo);
				holder.tv_gamedate = (TextView) convertView
						.findViewById(R.id.tv_itemfight_gamedate);
				holder.tv_gamegym = (TextView) convertView
						.findViewById(R.id.tv_itemfight_gamegym);
				holder.tv_gameprice = (TextView) convertView
						.findViewById(R.id.tv_itemfight_gameprice);
				holder.btn_fight = (Button) convertView
						.findViewById(R.id.btn_itemfight_fight);

				convertView.setTag(holder);
			} else {
				holder = (TeamlistViewHolder) convertView.getTag();
			}

			if ((Boolean) mylist.get(position).get("hasPath1")) {
				String path = (String) mylist.get(position).get("TeamPic");
				new AsyncViewTask(context, path, holder.iv_teamlogo1, 2)
						.execute(holder.iv_teamlogo1);// 异步加载图片
			}

			if ((Boolean) mylist.get(position).get("hasPath3")) {
				String path = (String) mylist.get(position).get("GymPic");
				new AsyncViewTask(context, path, holder.iv_gymlogo, 4)
						.execute(holder.iv_gymlogo);// 异步加载图片
			}
			holder.iv_gymlogo.setOnClickListener(new MyOnclickListener(mylist,
					position));

			holder.iv_teamlogo1.setOnClickListener(new MyOnclickListener(
					mylist, position));

			holder.tv_teamname1.setText((String) mylist.get(position).get(
					"TeamName"));

			int status = (Integer) mylist.get(position).get("Status");
			if (status >= 3) {
				holder.iv_teamlogo2
						.setImageResource(R.drawable.ic_mainpage_tab3_defaultphoto);
				if ((Boolean) mylist.get(position).get("hasPath2")) {
					String path = (String) mylist.get(position).get("oTeamPic");
					new AsyncViewTask(context, path, holder.iv_teamlogo2, 2)
							.execute(holder.iv_teamlogo2);// 异步加载图片
				}

				holder.tv_teamname2.setText((String) mylist.get(position).get(
						"oTeamName"));

				holder.iv_teamlogo2.setOnClickListener(new MyOnclickListener(
						mylist, position));
			}

			int time = (Integer) mylist.get(position).get("Time");
			holder.tv_gamedate.setText((String) mylist.get(position)
					.get("Date")
					+ " "
					+ BookActivity.minuteToClock(time * 30)
					+ "-"
					+ BookActivity.minuteToClock((time + (Integer) mylist.get(
							position).get("weight")) * 30));

			holder.tv_gamegym.setText((String) mylist.get(position).get(
					"GymName"));
			// holder.tv_gamegym.setOnClickListener(new
			// MyOnclickListener(mylist, position));

			holder.tv_gameprice.setText("￥"
					+ Data.doubleTrans((Double) mylist.get(position).get(
							"Price")));

			holder.btn_fight.setVisibility(View.VISIBLE);
			holder.btn_fight.setOnClickListener(new MyOnclickListener(mylist,
					position));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return convertView;
	}

	static class TeamlistViewHolder {
		ImageView iv_teamlogo1;
		TextView tv_teamname1;
		TextView tv_sports;
		ImageView iv_teamlogo2;
		TextView tv_teamname2;
		ImageView iv_gymlogo;
		TextView tv_gamedate;
		TextView tv_gamegym;
		TextView tv_gameprice;
		Button btn_fight;
	}

	class MyOnclickListener implements OnClickListener {

		ArrayList<HashMap<String, Object>> mylist;
		int i;

		MyOnclickListener(ArrayList<HashMap<String, Object>> mylist,
				int position) {
			this.mylist = mylist;
			i = position;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent;
			switch (v.getId()) {
			case R.id.iv_itemfight_teamlogo1:
				intent = new Intent(context, TeamInfoActivity.class);
				intent.putExtra("ID", (Integer) mylist.get(i).get("TeamID")
						+ "");
				context.startActivity(intent);
				break;
			case R.id.iv_itemfight_teamlogo2:
				intent = new Intent(context, TeamInfoActivity.class);
				intent.putExtra("ID", (Integer) mylist.get(i).get("oTeamID")
						+ "");
				context.startActivity(intent);
				break;
			case R.id.btn_itemfight_fight:
				progressDialog = new ProgressDialog(context);
				progressDialog.setMessage("正在验证登录状态…");
				progressDialog.setCancelable(true);
				progressDialog.show();

				checkPasscodeHandler = new CheckPasscodeHandler();
				teamlistHandler = new TeamlistHandler();

				new CheckPasscodeThread(i).start();
				break;
			case R.id.iv_itemfight_gymlogo:
				intent = new Intent(context, CourtinfoActivity.class);
				intent.putExtra("ID", (Integer) mylist.get(i).get("GymID") + "");
				context.startActivity(intent);
				break;
			}
		}

	}

	class CheckPasscodeThread extends Thread {

		int i;

		CheckPasscodeThread(int i) {
			this.i = i;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("UserID", Data.USERID));
				params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));

				String result = new HttpPostConnection("CheckPasscode", params)
						.httpConnection();

				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("result", result);
				b.putInt("i", i);
				msg.setData(b);
				checkPasscodeHandler.sendMessage(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

	class TeamListThread extends Thread {

		int i;

		TeamListThread(int i) {
			this.i = i;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("Request", "MyTeam"));
				params.add(new BasicNameValuePair("UserID", Data.USERID));

				String result = new HttpPostConnection("TeamServer", params)
						.httpConnection();

				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("result", result);
				b.putInt("i", i);
				msg.setData(b);
				teamlistHandler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class CheckPasscodeHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			try {

				String result = msg.getData().getString("result");

				if (result.equals("timeout")) {
					Toast.makeText(context, "网络出问题了", Toast.LENGTH_SHORT)
							.show();
					progressDialog.dismiss();
					return;
				}

				JSONObject json = new JSONObject(result);
				if (!json.getBoolean("Result")) {
					Toast.makeText(context, "登录超时，请重新登录", Toast.LENGTH_SHORT)
							.show();
					progressDialog.dismiss();
					context.startActivity(new Intent(context,
							LoginActivity.class));
				} else {
					new TeamListThread(msg.getData().getInt("i")).start();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class TeamlistHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			progressDialog.dismiss();

			int i = 0;

			try {
				String result = msg.getData().getString("result");
				i = msg.getData().getInt("i");

				if (result.equals("timeout")) {
					Toast.makeText(context, "网络出问题了", Toast.LENGTH_SHORT)
							.show();
					return;
				}

				JSONObject json = new JSONObject(result);
				if (json.getInt("TeamNum") == 0) {
					Toast.makeText(context, "您还没有加入一支球队", Toast.LENGTH_SHORT)
							.show();
				} else {
					Intent intent = new Intent(context,
							FightSelectteamActivity.class);
					intent.putExtra("teamID", result);
					intent.putExtra("gymID",
							(Integer) mylist.get(i).get("GymID") + "");
					intent.putExtra("gymType",
							(Integer) mylist.get(i).get("GymType"));
					intent.putExtra("gymName",
							(String) mylist.get(i).get("GymName"));
					intent.putExtra("FightID",
							(Integer) mylist.get(i).get("FightID"));
					intent.putExtra("TeamName",
							(String) mylist.get(i).get("TeamName"));
					intent.putExtra("Date", (String) mylist.get(i).get("Date"));
					intent.putExtra("CourtNum", 1);
					intent.putExtra("Time", (Integer) mylist.get(i).get("Time"));
					intent.putExtra("CourtName",
							(String) mylist.get(i).get("CourtName"));
					intent.putExtra("Price", (Double) mylist.get(i)
							.get("Price"));
					intent.putExtra("weight",
							(Integer) mylist.get(i).get("weight"));

					intent.putExtra("isAcceptFight", true);

					context.startActivity(intent);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}

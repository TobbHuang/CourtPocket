package com.pazdarke.courtpocket1.tools.listview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.activity.BookActivity;
import com.pazdarke.courtpocket1.activity.CommitFightcommentActivity;
import com.pazdarke.courtpocket1.activity.CourtinfoActivity;
import com.pazdarke.courtpocket1.activity.FightbookConfirmActivity;
import com.pazdarke.courtpocket1.activity.TeamInfoActivity;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket1.tools.listview.TeammemberlistAdapter.DeleteHandler;
import com.pazdarke.courtpocket1.tools.listview.TeammemberlistAdapter.DeleteThread;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FightbillAdapter extends BaseAdapter {
	
	ProgressDialog progressDialog;

	Context context;
	ArrayList<HashMap<String, Object>> mylist;
	int layoutID;
	
	CancelFightHandler cancelFightHandler;

	public FightbillAdapter(Context context,
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
				holder.ll_status = (LinearLayout) convertView
						.findViewById(R.id.ll_itemfight_status);
				holder.tv_status = (TextView) convertView
						.findViewById(R.id.tv_itemfight_status);
				holder.ll_checkcode = (LinearLayout) convertView
						.findViewById(R.id.ll_itemfight_checkcode);
				holder.tv_checkcode = (TextView) convertView
						.findViewById(R.id.tv_itemfight_checkcode);
				holder.btn_fight = (Button) convertView
						.findViewById(R.id.btn_itemfight_fight);
				holder.tv_generatetime = (TextView) convertView
						.findViewById(R.id.tv_itemfight_generatetime);

				convertView.setTag(holder);
			} else {
				holder = (TeamlistViewHolder) convertView.getTag();
			}

			if ((Boolean) mylist.get(position).get("hasPath1")) {
				String path = (String) mylist.get(position).get("TeamPic");
				new AsyncViewTask(context, path, holder.iv_teamlogo1,2)
						.execute(holder.iv_teamlogo1);// 异步加载图片
			}
			
			if ((Boolean) mylist.get(position).get("hasPath3")) {
				String path = (String) mylist.get(position).get("GymPic");
				new AsyncViewTask(context, path, holder.iv_gymlogo,2)
						.execute(holder.iv_gymlogo);// 异步加载图片
			}
			holder.iv_gymlogo.setOnClickListener(new MyOnclickListener(mylist, position));

			holder.iv_teamlogo1.setOnClickListener(new MyOnclickListener(
					mylist, position));

			holder.tv_teamname1.setText((String) mylist.get(position).get(
					"TeamName"));

			holder.ll_status.setVisibility(View.VISIBLE);

			int status = (Integer) mylist.get(position).get("Status");
			switch (status) {
			case -1:
				holder.tv_status.setText("订单异常");
				holder.tv_status.setTextColor(context.getResources().getColor(
						R.color.red));
				holder.btn_fight.setVisibility(View.GONE);
				break;
			case 0:
				holder.tv_status.setText("订单关闭");
				holder.tv_status.setTextColor(context.getResources().getColor(
						R.color.darkGrey));
				holder.btn_fight.setVisibility(View.GONE);
				break;
			case 1:
				holder.tv_status.setText("待付款");
				holder.tv_status.setTextColor(context.getResources().getColor(
						R.color.red));
				holder.btn_fight.setVisibility(View.VISIBLE);
				holder.btn_fight.setText("付款");
				holder.btn_fight.setOnClickListener(new MyOnclickListener(
						mylist, position));
				break;
			case 2:
				holder.tv_status.setText("等待对手");
				holder.tv_status.setTextColor(context.getResources().getColor(
						R.color.yellow));
				holder.btn_fight.setVisibility(View.VISIBLE);
				holder.btn_fight.setText("取消");
				holder.btn_fight.setOnClickListener(new MyOnclickListener(
						mylist, position));
				break;
			case 3:
				holder.tv_status.setText("待对手付款");
				holder.tv_status.setTextColor(context.getResources().getColor(
						R.color.orange));
				holder.btn_fight.setVisibility(View.GONE);
				break;
			case 4:
				holder.tv_status.setText("等待比赛");
				holder.tv_status.setTextColor(context.getResources().getColor(
						R.color.blue));
				holder.ll_checkcode.setVisibility(View.VISIBLE);
				holder.tv_checkcode.setText((String) mylist.get(position).get(
						"Code"));
				holder.btn_fight.setVisibility(View.GONE);
				break;
			case 5:
				holder.tv_status.setText("待评价");
				holder.tv_status.setTextColor(context.getResources().getColor(
						R.color.yellow));
				holder.ll_checkcode.setVisibility(View.GONE);
				holder.btn_fight.setVisibility(View.VISIBLE);
				holder.btn_fight
						.setBackgroundResource(R.drawable.selector_drawable_yellow_orange);
				holder.btn_fight.setText("评价");
				holder.btn_fight.setOnClickListener(new MyOnclickListener(
						mylist, position));
				break;
			case 6:
				holder.tv_status.setText("已评价");
				holder.tv_status.setTextColor(context.getResources().getColor(
						R.color.darkGrey));
				holder.btn_fight.setVisibility(View.GONE);
				break;

			}
			if (status >= 3) {
				holder.iv_teamlogo2
						.setImageResource(R.drawable.ic_mainpage_tab3_defaultphoto);
				if ((Boolean) mylist.get(position).get("hasPath2")) {
					String path = (String) mylist.get(position).get("oTeamPic");
					new AsyncViewTask(context, path, holder.iv_teamlogo2,4)
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

			holder.tv_gamegym.setText((String) mylist.get(position).get("GymName"));
			/*holder.tv_gamegym.setOnClickListener(new MyOnclickListener(mylist,
					position));*/

			holder.tv_gameprice.setText("￥"
					+ Data.doubleTrans((Double) mylist.get(position).get(
							"Price")));

			holder.tv_generatetime.setVisibility(View.VISIBLE);
			holder.tv_generatetime.setText("创建于"
					+ ((String) mylist.get(position).get("GenerateTime"))
							.substring(0, 19));

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
		LinearLayout ll_status;
		TextView tv_status;
		LinearLayout ll_checkcode;
		TextView tv_checkcode;
		Button btn_fight;
		TextView tv_generatetime;
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
				switch ((Integer) mylist.get(i).get("Status")) {
				case 1:
					intent = new Intent(context,
							FightbookConfirmActivity.class);
					intent.putExtra("isFromBill", true);
					intent.putExtra("BillID",
							(String) mylist.get(i).get("BillID"));
					intent.putExtra("gymType", 1);
					intent.putExtra("gymName",
							(String) mylist.get(i).get("GymName"));
					intent.putExtra("teamName",
							(String) mylist.get(i).get("TeamName"));
					intent.putExtra("date", (String) mylist.get(i).get("Date"));
					intent.putExtra("courtNum", 1);
					intent.putExtra("time0", (Integer) mylist.get(i)
							.get("Time"));
					intent.putExtra("name0",
							(String) mylist.get(i).get("CourtName"));
					intent.putExtra("price0",
							(Double) mylist.get(i).get("Price") + "");
					intent.putExtra("money", (Double) mylist.get(i)
							.get("Price"));
					intent.putExtra("weight", (Integer) mylist.get(i)
							.get("weight"));
					context.startActivity(intent);
					break;
				case 2:
					AlertDialog dialog = new AlertDialog.Builder(
							context)
							.setMessage(
									"确定取消订单吗？ ")
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											dialog.dismiss();
											progressDialog = new ProgressDialog(
													context);
											progressDialog.setMessage("正在删除…");
											progressDialog.setCancelable(true);
											progressDialog.show();

											cancelFightHandler = new CancelFightHandler();

											new CancelFightThread(
													(String) mylist.get(i).get(
															"BillID")).start();
										}

									})
							.setNegativeButton("取消",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											dialog.dismiss();
										}
									}).create();
					dialog.show();
					break;
				case 5:
					intent = new Intent(context,
							CommitFightcommentActivity.class);
					intent.putExtra("BillID",
							(String) mylist.get(i).get("BillID"));
					JSONObject json = new JSONObject();
					try {
						json.put("GymName",
								(String) mylist.get(i).get("GymName"));
						json.put("Price", (Double) mylist.get(i).get("Price"));
						json.put("GenerateTime",
								(String) mylist.get(i).get("GenerateTime"));
						json.put("GymID", (Integer) mylist.get(i).get("GymID"));
						if ((Boolean) mylist.get(i).get("hasPath3")) {
							json.put("GymPic",
									(String) mylist.get(i).get("GymPic"));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					intent.putExtra("info", json.toString());
					context.startActivity(intent);
					break;
				}
				break;
			case R.id.iv_itemfight_gymlogo:
				intent=new Intent(context,CourtinfoActivity.class);
				intent.putExtra("ID", (Integer) mylist.get(i).get("GymID")+"");
				context.startActivity(intent);
				break;
			}
		}

	}
	
	class CancelFightThread extends Thread{
		
		String BillID;
		
		CancelFightThread(String BillID){
			this.BillID=BillID;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "CancelFight"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("FightBillID", BillID));

			String result = new HttpPostConnection("FightServer", params)
					.httpConnection();
			
			Message msg=new Message();
			Bundle b=new Bundle();
			b.putString("result", result);
			msg.setData(b);
			cancelFightHandler.sendMessage(msg);
		}
	}
	
	class CancelFightHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			progressDialog.dismiss();
			
			String result=msg.getData().getString("result");
			
			if(result.equals("timeout")){
				Toast.makeText(context, "网络出问题了", Toast.LENGTH_SHORT).show();
				return;
			}
			
			try {
				JSONObject json=new JSONObject(result);
				if(json.getString("Result").equals("比赛已取消")){
					Toast.makeText(context, "操作成功", Toast.LENGTH_SHORT).show();
					Intent intent=new Intent("RefreshFightbill");
					context.sendBroadcast(intent);
				} else{
					Toast.makeText(context, json.getString("Result"), Toast.LENGTH_SHORT).show();
				}
					
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

}

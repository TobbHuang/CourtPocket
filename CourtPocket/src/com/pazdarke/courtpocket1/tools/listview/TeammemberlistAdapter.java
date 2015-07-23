package com.pazdarke.courtpocket1.tools.listview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.httpConnection.HttpPostConnection;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TeammemberlistAdapter extends BaseAdapter {
	
	ProgressDialog progressDialog;

	Context context;
	ArrayList<HashMap<String, Object>> mylist;
	int layoutID;
	
	DeleteHandler deleteHandler;

	public TeammemberlistAdapter(Context context,
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
				holder.iv_logo = (ImageView) convertView
						.findViewById(R.id.iv_itemuserinfo_logo);
				holder.rl_leader=(RelativeLayout)convertView.findViewById(R.id.rl_itemuserinfo_leader);
				holder.tv_username = (TextView) convertView
						.findViewById(R.id.tv_itemuserinfo_username);
				holder.iv_sex=(ImageView)convertView.findViewById(R.id.iv_itemuserinfo_sex);
				holder.rb_rate = (RatingBar) convertView
						.findViewById(R.id.rb_itemuserinfo_rate);
				holder.tv_rate = (TextView) convertView
						.findViewById(R.id.tv_itemuserinfo_rate);
				holder.tv_ratenum = (TextView) convertView.findViewById(R.id.tv_itemuserinfo_ratenum);
				holder.iv_delete=(ImageView)convertView.findViewById(R.id.iv_itemuserinfo_delete);
				holder.tv_introduction = (TextView) convertView.findViewById(R.id.tv_itemuserinfo_introduction);

				convertView.setTag(holder);
			} else {
				holder = (TeamlistViewHolder) convertView.getTag();
			}

			if ((Boolean) mylist.get(position).get("hasPath")) {
				String path = (String) mylist.get(position).get("path");
				new AsyncViewTask(context, path, holder.iv_logo,2)
						.execute(holder.iv_logo);// 异步加载图片
			} else {
				holder.iv_logo.setImageResource(R.drawable.ic_mainpage_tab3_defaultphoto);
			}

			holder.tv_username.setText((String) mylist.get(position).get(
					"username"));
			
			if((Boolean)mylist.get(position).get("isLeader")){
				holder.rl_leader.setVisibility(View.VISIBLE);
			}
			
			int sex=(Integer)mylist.get(position).get("Sex");
			if(sex==0||sex==1){
				int[] ic_sex={R.drawable.ic_male,R.drawable.ic_female};
				holder.iv_sex.setImageResource(ic_sex[sex]);
			}
			
			int ratenum=(Integer)mylist.get(position).get("ratenum");
			holder.tv_ratenum.setText("("+ratenum+"条评价)");
			if(ratenum==0){
				holder.rb_rate.setRating(0);
				holder.tv_rate.setText("0分");
			} else{
				double rate=(Double)mylist.get(position).get("rate");
				holder.rb_rate.setRating((float) rate);
				holder.tv_rate.setText(rate+"分");
			}
			
			if((Boolean)mylist.get(position).get("isDelete")&&!(Boolean)mylist.get(position).get("isLeader")){
				holder.iv_delete.setVisibility(View.VISIBLE);
				holder.iv_delete.setOnClickListener(new MyOnclickListener(position));
			} else if((Boolean)mylist.get(position).get("isDelete")&&(Boolean)mylist.get(position).get("isLeader")){
				holder.iv_delete.setVisibility(View.INVISIBLE);
			}
			
			holder.tv_introduction.setText((String)mylist.get(position).get("introduction"));
				

		} catch (Exception e) {
			e.printStackTrace();
		}

		return convertView;
	}

	static class TeamlistViewHolder {
		ImageView iv_logo;
		RelativeLayout rl_leader;
		TextView tv_username;
		ImageView iv_sex;
		RatingBar rb_rate;
		TextView tv_rate;
		TextView tv_ratenum;
		ImageView iv_delete;
		TextView tv_introduction;
	}
	
	class MyOnclickListener implements OnClickListener{
		
		int i;
		
		MyOnclickListener(int position){
			i=position;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.iv_itemuserinfo_delete:
				AlertDialog dialog = new AlertDialog.Builder(
						context)
						.setMessage(
								"确定将 "
										+ (String) mylist.get(i)
												.get("username") + " 移除出球队吗？")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(
											DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dialog.dismiss();
										progressDialog=new ProgressDialog(context);
										progressDialog.setMessage("正在删除…");
										progressDialog.setCancelable(true);
										progressDialog.show();
										
										deleteHandler=new DeleteHandler();

										new DeleteThread((String) mylist.get(i)
												.get("TeamID"), (String) mylist
												.get(i).get("oUserID")).start();
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
			}
		}
		
	}
	
	class DeleteThread extends Thread{
		
		String TeamID;
		String oUserID;
		
		DeleteThread(String TeamID,String oUserID){
			this.TeamID=TeamID;
			this.oUserID=oUserID;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "DeleteMember"));
			params.add(new BasicNameValuePair("UserID", Data.USERID));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("TeamID", TeamID));
			params.add(new BasicNameValuePair("oUserID", oUserID));
			
			System.out.println(Data.USERID);
			System.out.println(oUserID);

			String result = new HttpPostConnection("TeamServer", params)
					.httpConnection();
			
			Message msg=new Message();
			Bundle b=new Bundle();
			b.putString("result", result);
			msg.setData(b);
			deleteHandler.sendMessage(msg);
		}
	}
	
	@SuppressLint("HandlerLeak")
	class DeleteHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			progressDialog.dismiss();
			
			String result=msg.getData().getString("result");
			
			if(result.equals("timeout")){
				Toast.makeText(context, "网络出问题咯", Toast.LENGTH_SHORT).show();
				return;
			}
			
			try {
				JSONObject json=new JSONObject(result);
				
				Toast.makeText(context, json.getString("Result"), Toast.LENGTH_SHORT).show();
				
				if(json.getString("Result").equals("操作成功")){
					Intent intent=new Intent("RefreshManageTeammember");
					context.sendBroadcast(intent);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

}

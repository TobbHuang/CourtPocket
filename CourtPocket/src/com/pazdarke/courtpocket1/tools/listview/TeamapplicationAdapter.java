package com.pazdarke.courtpocket1.tools.listview;

import java.util.ArrayList;
import java.util.HashMap;

import com.pazdarke.courtpocket1.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TeamapplicationAdapter extends BaseAdapter {

	Context context;
	ArrayList<HashMap<String, Object>> mylist;
	int layoutID;
	String[] data;
	int[] viewID;

	public TeamapplicationAdapter(Context context,
			ArrayList<HashMap<String, Object>> mylist, int layoutID,
			String[] data, int[] viewID) {
		this.context = context;
		this.mylist = mylist;
		this.layoutID = layoutID;
		this.data = data;
		this.viewID = viewID;

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
						.findViewById(viewID[0]);
				holder.tv_username = (TextView) convertView
						.findViewById(viewID[1]);
				holder.tv_content = (TextView) convertView
						.findViewById(viewID[2]);
				holder.tv_result = (TextView) convertView
						.findViewById(viewID[3]);
				holder.tv_date = (TextView) convertView.findViewById(viewID[4]);
				holder.tv_text1 = (TextView) convertView
						.findViewById(R.id.tv_itemteamapplication_text1);
				holder.tv_text2 = (TextView) convertView
						.findViewById(R.id.tv_itemteamapplication_text2);

				convertView.setTag(holder);
			} else {
				holder = (TeamlistViewHolder) convertView.getTag();
			}

			if ((Boolean) mylist.get(position).get("hasPath")) {
				String path = (String) mylist.get(position).get(data[0]);
				new AsyncViewTask(context, path, holder.iv_logo,2)
						.execute(holder.iv_logo);// 异步加载图片
			} else {
				holder.iv_logo.setImageResource(R.color.lightGrey);
			}

			holder.tv_username.setText((String) mylist.get(position).get(
					data[1]));
			

			

			int result = (Integer) mylist.get(position).get(data[3]);
			// 如果是申请消息
			if (result == 0 || result == 1 || result == 2) {
				String[] str_result = { "已拒绝", "待处理", "已通过" };
				int[] color = { R.color.darkGrey, R.color.red, R.color.blue };
				holder.tv_result.setText(str_result[result]);
				holder.tv_result.setTextColor(context.getResources().getColor(
						color[result]));
				holder.tv_content.setText("验证消息："
						+ (String) mylist.get(position).get(data[2]));
			} else{
				// 邀请消息
				String[] str_result = { "被拒绝", "邀请已发送", "对方已加入" };
				int[] color = { R.color.darkGrey, R.color.yellow, R.color.blue };
				holder.tv_result.setText(str_result[result-10]);
				holder.tv_result.setTextColor(context.getResources().getColor(
						color[result-10]));
				holder.tv_text1.setVisibility(View.VISIBLE);
				holder.tv_text2.setText(" 加入球队");
				holder.tv_content.setVisibility(View.INVISIBLE);
			}

			holder.tv_date.setText(((String) mylist.get(position).get(data[4]))
					.substring(0, 10));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return convertView;
	}

	static class TeamlistViewHolder {
		ImageView iv_logo;
		TextView tv_text1;
		TextView tv_text2;
		TextView tv_username;
		TextView tv_content;
		TextView tv_result;
		TextView tv_date;
	}

}

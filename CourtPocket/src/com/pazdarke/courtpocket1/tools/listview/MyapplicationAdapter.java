package com.pazdarke.courtpocket1.tools.listview;

import java.util.ArrayList;
import java.util.HashMap;

import com.pazdarke.courtpocket1.R;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyapplicationAdapter extends BaseAdapter {

	Context context;
	ArrayList<HashMap<String, Object>> mylist;
	int layoutID;
	String[] data;
	int[] viewID;

	public MyapplicationAdapter(Context context,
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
				holder.tv_invite=(TextView)convertView.findViewById(R.id.tv_itemmyapplication_invite);
				holder.tv_teamname = (TextView) convertView
						.findViewById(viewID[1]);
				holder.tv_content = (TextView) convertView
						.findViewById(viewID[2]);
				holder.tv_result = (TextView) convertView
						.findViewById(viewID[3]);
				holder.tv_date = (TextView) convertView.findViewById(viewID[4]);

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

			/*holder.tv_teamname.setText((String) mylist.get(position).get(
					data[1]));
			holder.tv_content.setText("验证消息："
					+ (String) mylist.get(position).get(data[2]));

			String[] result = { "申请被拒绝", "申请已发送", "申请通过" };
			int[] color = { R.color.red, R.color.darkGrey, R.color.blue };
			holder.tv_result.setText(result[(Integer) mylist.get(position).get(
					data[3])]);
			holder.tv_result.setTextColor(context.getResources().getColor(
					color[(Integer) mylist.get(position).get(data[3])]));*/
			
			int result = (Integer) mylist.get(position).get(data[3]);
			// 如果是申请消息
			if (result == 0 || result == 1 || result == 2) {
				String[] str_result = { "被拒绝", "申请已发送", "已加入" };
				int[] color = { R.color.darkGrey, R.color.yellow, R.color.blue };
				holder.tv_result.setText(str_result[result]);
				holder.tv_result.setTextColor(context.getResources().getColor(
						color[result]));
				holder.tv_content.setText("验证消息："
						+ (String) mylist.get(position).get(data[2]));

				SpannableStringBuilder builder = new SpannableStringBuilder(
						"申请加入球队 " + (String) mylist.get(position).get(data[1]));
				ForegroundColorSpan blackSpan = new ForegroundColorSpan(context
						.getResources().getColor(R.color.black));
				ForegroundColorSpan blueSpan = new ForegroundColorSpan(context
						.getResources().getColor(R.color.blue));
				builder.setSpan(blackSpan, 0, 7,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				builder.setSpan(blueSpan, 7, 7 + ((String) mylist
						.get(position).get(data[1])).length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				holder.tv_teamname.setText(builder);
			} else {
				// 邀请消息
				String[] str_result = { "已拒绝", "待处理", "已同意" };
				int[] color = { R.color.darkGrey, R.color.red, R.color.blue };
				holder.tv_result.setText(str_result[result-10]);
				holder.tv_result.setTextColor(context.getResources().getColor(
						color[result - 10]));
				holder.tv_content.setVisibility(View.GONE);
				holder.tv_teamname.setVisibility(View.GONE);
				holder.tv_invite.setVisibility(View.VISIBLE);

				String leadername = (String) mylist.get(position).get(
						"leadername");
				String teamname = (String) mylist.get(position).get(data[1]);

				SpannableStringBuilder builder = new SpannableStringBuilder(
						leadername + " 邀请你加入球队 " + teamname);
				ForegroundColorSpan blackSpan = new ForegroundColorSpan(context
						.getResources().getColor(R.color.black));
				ForegroundColorSpan blueSpan = new ForegroundColorSpan(context
						.getResources().getColor(R.color.blue));
				builder.setSpan(blueSpan, 0, leadername.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				builder.setSpan(blackSpan, leadername.length(),
						leadername.length() + 9,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				builder.setSpan(blueSpan, leadername.length() + 9,
						leadername.length() + 9 + teamname.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				holder.tv_invite.setText(builder);
				
				
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
		TextView tv_invite;
		TextView tv_teamname;
		TextView tv_content;
		TextView tv_result;
		TextView tv_date;
	}

}

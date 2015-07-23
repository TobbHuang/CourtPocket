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

public class TeamlistAdapter extends BaseAdapter {

	Context context;
	ArrayList<HashMap<String, Object>> mylist;
	int layoutID;
	String[] data;
	int[] viewID;

	public TeamlistAdapter(Context context,
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
				holder.iv_teamlogo = (ImageView) convertView
						.findViewById(viewID[0]);
				holder.iv_teamtype = (ImageView) convertView
						.findViewById(viewID[1]);
				holder.tv_teamname = (TextView) convertView
						.findViewById(viewID[2]);
				holder.tv_leadername = (TextView) convertView
						.findViewById(viewID[3]);
				holder.tv_membernum = (TextView) convertView
						.findViewById(viewID[4]);
				holder.tv_rate = (TextView) convertView.findViewById(viewID[5]);
				holder.tv_ratenum = (TextView) convertView
						.findViewById(viewID[6]);

				convertView.setTag(holder);
			} else {
				holder = (TeamlistViewHolder) convertView.getTag();
			}

			if ((Boolean) mylist.get(position).get("hasPath")) {
				String path = (String) mylist.get(position).get(data[0]);
				new AsyncViewTask(context, path, holder.iv_teamlogo,2)
						.execute(holder.iv_teamlogo);// 异步加载图片
			} else {
				holder.iv_teamlogo.setImageResource(R.color.lightGrey);
			}

			int[] type={R.drawable.ic_bill_soccer,R.drawable.ic_bill_basketball,R.drawable.ic_bill_volleyball};
			holder.iv_teamtype.setImageResource(type[(Integer) mylist.get(position).get(
					data[1])-1]);
			
			holder.tv_teamname.setText((String) mylist.get(position).get(
					data[2]));
			holder.tv_leadername.setText((String) mylist.get(position).get(data[3]));
			holder.tv_membernum.setText((Integer) mylist.get(position).get(data[4])+"人");
			holder.tv_rate.setText((Double) mylist.get(position).get(
					data[5])+"分");
			holder.tv_ratenum
					.setText("("+(Integer) mylist.get(position).get(data[6])+"队评价过)");


		} catch (Exception e) {
			e.printStackTrace();
		}

		return convertView;
	}

	static class TeamlistViewHolder {
		ImageView iv_teamlogo;
		ImageView iv_teamtype;
		TextView tv_teamname;
		TextView tv_leadername;
		TextView tv_membernum;
		TextView tv_rate;
		TextView tv_ratenum;
	}

}

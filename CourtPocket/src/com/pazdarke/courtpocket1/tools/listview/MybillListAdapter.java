package com.pazdarke.courtpocket1.tools.listview;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.pazdarke.courtpocket1.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MybillListAdapter extends BaseAdapter {

	Context context;
	ArrayList<HashMap<String, Object>> mylist;
	int layoutID;
	String[] data;
	int[] viewID;

	public MybillListAdapter(Context context,
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
			BilllistViewHolder holder;

			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(layoutID,
						null);
				holder = new BilllistViewHolder();
				holder.iv_logo = (ImageView) convertView
						.findViewById(viewID[0]);
				holder.tv_courtname = (TextView) convertView
						.findViewById(viewID[1]);
				holder.tv_billstatus = (TextView) convertView
						.findViewById(viewID[2]);
				holder.tv_generatetime = (TextView) convertView
						.findViewById(viewID[3]);
				holder.tv_smallbillnum = (TextView) convertView
						.findViewById(viewID[4]);
				holder.tv_usedate = (TextView) convertView
						.findViewById(viewID[5]);
				holder.tv_money = (TextView) convertView
						.findViewById(viewID[6]);
				holder.iv_type = (ImageView) convertView
						.findViewById(viewID[7]);
				convertView.setTag(holder);
			} else {
				holder = (BilllistViewHolder) convertView.getTag();
			}

			if ((Boolean) mylist.get(position).get("hasPath")) {
				String path = (String) mylist.get(position).get(data[0]);
				new AsyncViewTask(context, path, holder.iv_logo,5)
						.execute(holder.iv_logo);// 异步加载图片
			} else {
				holder.iv_logo.setImageResource(R.color.lightGrey);
			}

			holder.tv_courtname.setText((String) mylist.get(position).get(
					data[1]));

			String str_status = (String) mylist.get(position).get(data[2]);
			holder.tv_billstatus.setText(str_status);
			if (str_status.equals("待付款") || str_status.equals("订单异常")) {
				holder.tv_billstatus.setTextColor(context.getResources()
						.getColor(R.color.red));
			} else if(str_status.equals("待验证")){
				holder.tv_billstatus.setTextColor(context.getResources()
						.getColor(R.color.orange));
			} else if(str_status.equals("待评价")){
				holder.tv_billstatus.setTextColor(context.getResources()
						.getColor(R.color.yellow));
			} else{
				holder.tv_billstatus.setTextColor(context.getResources()
						.getColor(R.color.darkGrey));
			}

			Calendar cal = Calendar.getInstance();// 使用日历类
			int year = cal.get(Calendar.YEAR);// 得到年

			String[] generatetime = ((String) mylist.get(position).get(data[3]))
					.split("-");
			if (generatetime[0].equals(year + ""))
				holder.tv_generatetime.setText("创建于" + generatetime[1] + "月"
						+ generatetime[2] + "日");
			else
				holder.tv_generatetime.setText("创建于"
						+ (String) mylist.get(position).get(data[3]));
			
			holder.tv_smallbillnum.setText((String) mylist.get(position).get(
					data[4]));
			
			String[] usetime = ((String) mylist.get(position).get(data[5]))
					.split("-");
			if (usetime[0].equals(year + ""))
				holder.tv_usedate.setText(usetime[1] + "月" + usetime[2] + "日");
			else
				holder.tv_usedate.setText((String) mylist.get(position).get(
						data[5]));
			
			holder.tv_money.setText((String) mylist.get(position).get(data[6]));

			// int type=(Integer) mylist.get(position).get(data[7]);
			int[] type = { R.drawable.ic_bill_soccer,
					R.drawable.ic_bill_basketball,
					R.drawable.ic_bill_volleyball,
					R.drawable.ic_bill_tabletennis, R.drawable.ic_bill_tennis,
					R.drawable.ic_bill_badminton, R.drawable.ic_bill_billiards,
					R.drawable.ic_bill_swimming,
					R.drawable.ic_bill_bodybuilding, R.drawable.ic_bill_xgames,
					R.drawable.ic_bill_golf, R.drawable.ic_bill_moresports };
			holder.iv_type.setImageResource(type[(Integer) mylist.get(position)
					.get(data[7]) - 1]);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return convertView;
	}

	static class BilllistViewHolder {
		ImageView iv_logo;
		TextView tv_courtname;
		TextView tv_billstatus;
		TextView tv_generatetime;
		TextView tv_smallbillnum;
		TextView tv_usedate;
		TextView tv_money;
		ImageView iv_type;
	}

}

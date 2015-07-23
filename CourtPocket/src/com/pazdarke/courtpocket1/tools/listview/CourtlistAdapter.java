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

public class CourtlistAdapter extends BaseAdapter {

	Context context;
	ArrayList<HashMap<String, Object>> mylist;
	int layoutID;
	String[] data;
	int[] viewID;

	public CourtlistAdapter(Context context,
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
			CourtlistViewHolder holder;

			if (convertView == null) {

				convertView = LayoutInflater.from(context).inflate(layoutID,
						null);
				holder = new CourtlistViewHolder();
				holder.iv_logo = (ImageView) convertView
						.findViewById(viewID[0]);
				holder.tv_courtname = (TextView) convertView
						.findViewById(viewID[1]);
				holder.tv_rate = (TextView) convertView.findViewById(viewID[2]);
				holder.tv_ratestatus = (TextView) convertView.findViewById(R.id.tv_courtitem_ratestatus);
				holder.tv_ratenum = (TextView) convertView.findViewById(R.id.tv_courtitem_ratenum);
				holder.tv_price = (TextView) convertView
						.findViewById(viewID[3]);
				holder.tv_location = (TextView) convertView
						.findViewById(viewID[4]);
				holder.tv_meters = (TextView) convertView
						.findViewById(viewID[5]);
				holder.iv_service1 = (ImageView) convertView
						.findViewById(viewID[6]);
				holder.iv_service2 = (ImageView) convertView
						.findViewById(viewID[7]);
				holder.iv_service3 = (ImageView) convertView
						.findViewById(viewID[8]);
				holder.iv_service4 = (ImageView) convertView
						.findViewById(viewID[9]);
				holder.iv_service5 = (ImageView) convertView
						.findViewById(viewID[10]);
				holder.iv_service6 = (ImageView) convertView
						.findViewById(viewID[11]);

				convertView.setTag(holder);
			} else {
				holder = (CourtlistViewHolder) convertView.getTag();
			}

			if ((Boolean) mylist.get(position).get("hasPath")) {
				String path = (String) mylist.get(position).get(data[0]);
				new AsyncViewTask(context, path, holder.iv_logo,6)
						.execute(holder.iv_logo);// Òì²½¼ÓÔØÍ¼Æ¬
			} else {
				holder.iv_logo.setImageResource(R.color.lightGrey);
			}

			holder.tv_courtname.setText((String) mylist.get(position).get(
					data[1]));
			
			if((Integer) mylist.get(position).get("ratenum")!=0){
				holder.tv_rate.setText((String) mylist.get(position).get(data[2]));
				holder.tv_ratestatus.setText("·Ö");
				holder.tv_ratenum.setText("("+(Integer) mylist.get(position).get("ratenum")+")");
			}
			
			holder.tv_price.setText((String) mylist.get(position).get(data[3]));
			holder.tv_location.setText((String) mylist.get(position).get(
					data[4]));
			holder.tv_meters
					.setText((String) mylist.get(position).get(data[5]));

			if (mylist.get(position).size() >= 9)
				holder.iv_service1.setImageResource((Integer) mylist.get(
						position).get(data[6]));
			if (mylist.get(position).size() >= 10)
				holder.iv_service2.setImageResource((Integer) mylist.get(
						position).get(data[7]));
			if (mylist.get(position).size() >= 11)
				holder.iv_service3.setImageResource((Integer) mylist.get(
						position).get(data[8]));
			if (mylist.get(position).size() >= 12)
				holder.iv_service4.setImageResource((Integer) mylist.get(
						position).get(data[9]));
			if (mylist.get(position).size() >= 13)
				holder.iv_service5.setImageResource((Integer) mylist.get(
						position).get(data[10]));
			if (mylist.get(position).size() >= 14)
				holder.iv_service6.setImageResource((Integer) mylist.get(
						position).get(data[11]));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return convertView;
	}

	static class CourtlistViewHolder {
		ImageView iv_logo;
		TextView tv_courtname;
		TextView tv_rate;
		TextView tv_ratestatus;
		TextView tv_ratenum;
		TextView tv_price;
		TextView tv_location;
		TextView tv_meters;
		ImageView iv_service1;
		ImageView iv_service2;
		ImageView iv_service3;
		ImageView iv_service4;
		ImageView iv_service5;
		ImageView iv_service6;
	}

}

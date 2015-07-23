package com.pazdarke.courtpocket1.tools.listview;

import java.util.ArrayList;
import java.util.HashMap;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MycardAdapter extends BaseAdapter {

	Context context;
	ArrayList<HashMap<String, Object>> mylist;

	public MycardAdapter(Context context,
			ArrayList<HashMap<String, Object>> mylist) {
		this.context = context;
		this.mylist = mylist;

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

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		try {
			TeamlistViewHolder holder;

			if (convertView == null) {

				convertView = LayoutInflater.from(context).inflate(
						R.layout.item_mycard, null);
				holder = new TeamlistViewHolder();
				holder.iv_cardtype = (ImageView) convertView
						.findViewById(R.id.iv_itemmycard_cardtype);
				holder.tv_cardname = (TextView) convertView
						.findViewById(R.id.tv_itemmycard_cardname);
				holder.iv_cardage = (ImageView) convertView
						.findViewById(R.id.iv_itemmycard_cardage);
				holder.tv_content = (TextView) convertView
						.findViewById(R.id.tv_itemmycard_content);
				holder.tv_gymname = (TextView) convertView
						.findViewById(R.id.tv_itemmycard_gymname);
				holder.tv_price = (TextView) convertView
						.findViewById(R.id.tv_itemmycard_price);
				holder.tv_status = (TextView) convertView
						.findViewById(R.id.tv_itemmycard_status);
				holder.tv_validitydate = (TextView) convertView
						.findViewById(R.id.tv_itemmycard_validitydate);
				holder.tv_generatetime = (TextView) convertView
						.findViewById(R.id.tv_itemmycard_date);

				convertView.setTag(holder);
			} else {
				holder = (TeamlistViewHolder) convertView.getTag();
			}

			int type = (Integer) mylist.get(position).get("Type");
			if (type == 11) {
				holder.iv_cardtype
						.setImageResource(R.drawable.ic_adultcard_once);
				holder.iv_cardage.setImageResource(R.drawable.ic_card_adult);
			} else if (type == 12) {
				holder.iv_cardtype
						.setImageResource(R.drawable.ic_adultcard_twice);
				holder.iv_cardage.setImageResource(R.drawable.ic_card_adult);
			} else if (type == 21) {
				holder.iv_cardtype
						.setImageResource(R.drawable.ic_childcard_once);
				holder.iv_cardage.setImageResource(R.drawable.ic_card_child);
			} else if (type == 22) {
				holder.iv_cardtype
						.setImageResource(R.drawable.ic_childcard_twice);
				holder.iv_cardage.setImageResource(R.drawable.ic_card_child);
			}

			/*
			 * if((Boolean)mylist.get(position).get("hasPath")){ String path =
			 * (String) mylist.get(position).get("GymPic"); new
			 * AsyncViewTask(context, path, holder.iv_teamlogo1)
			 * .execute(holder.iv_teamlogo1);// 异步加载图片 }
			 */

			holder.tv_cardname.setText((String) mylist.get(position)
					.get("Name"));

			holder.tv_content.setText((String) mylist.get(position).get(
					"Content"));

			holder.tv_gymname.setText((String) mylist.get(position).get(
					"GymName"));

			holder.tv_price.setText("￥"
					+ Data.doubleTrans((Double) mylist.get(position).get(
							"Price")));

			int status = (Integer) mylist.get(position).get("Status");
			switch (status) {
			case 0:
				holder.tv_status.setText("订单关闭");
				holder.tv_status.setTextColor(context.getResources().getColor(
						R.color.darkGrey));

				holder.tv_validitydate.setText("");
				break;
			case 1:
				holder.tv_status.setText("待付款");
				holder.tv_status.setTextColor(context.getResources().getColor(
						R.color.red));

				holder.tv_validitydate.setText("");
				break;
			case 2:
				String str = "";
				int times = (Integer) mylist.get(position).get("Left");
				if (times == -1) {
					str += "无次数限制";
				} else {
					str += "剩余" + times + "次";
				}
				holder.tv_status.setText(str);
				holder.tv_status.setTextColor(context.getResources().getColor(
						R.color.blue));

				if (!((String) mylist.get(position).get("ValidityDate"))
						.equals("")) {
					holder.tv_validitydate.setText("("
							+ (String) mylist.get(position).get("ValidityDate")
							+ "过期)");
				} else {
					holder.tv_validitydate.setText("");
				}

				break;
			case 3:
				holder.tv_status.setText("已使用完/过期");
				holder.tv_status.setTextColor(context.getResources().getColor(
						R.color.darkGrey));

				holder.tv_validitydate.setText("待评价");
				holder.tv_validitydate.setTextColor(context.getResources()
						.getColor(R.color.orange));
				break;
			case 4:
				holder.tv_status.setText("已评价");
				holder.tv_status.setTextColor(context.getResources().getColor(
						R.color.darkGrey));

				holder.tv_validitydate.setText("");
				break;
			}

			holder.tv_generatetime.setText((String) mylist.get(position).get(
					"GenerateTime"));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return convertView;
	}

	static class TeamlistViewHolder {
		ImageView iv_cardtype;
		TextView tv_cardname;
		ImageView iv_cardage;
		TextView tv_content;
		TextView tv_gymname;
		TextView tv_price;
		TextView tv_status;
		TextView tv_validitydate;
		TextView tv_generatetime;
	}

}

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
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CardAdapter extends BaseAdapter {

	Context context;
	ArrayList<HashMap<String, Object>> mylist;

	public CardAdapter(Context context,
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
						R.layout.item_card, null);
				holder = new TeamlistViewHolder();
				holder.iv_cardtype = (ImageView) convertView
						.findViewById(R.id.iv_itemcard_cardtype);
				holder.tv_cardname = (TextView) convertView
						.findViewById(R.id.tv_itemcard_cardname);
				holder.iv_cardage = (ImageView) convertView
						.findViewById(R.id.iv_itemcard_cardage);
				holder.tv_content = (TextView) convertView
						.findViewById(R.id.tv_itemcard_content);
				holder.tv_price = (TextView) convertView
						.findViewById(R.id.tv_itemcard_price);
				holder.tv_oprice = (TextView) convertView
						.findViewById(R.id.tv_itemcard_oprice);
				holder.tv_sellnum = (TextView) convertView
						.findViewById(R.id.tv_itemcard_sellnum);

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

			holder.tv_cardname.setText((String) mylist.get(position)
					.get("Name"));

			holder.tv_content.setText((String) mylist.get(position).get(
					"Content"));

			holder.tv_price.setText("£§"
					+ Data.doubleTrans((Double) mylist.get(position).get(
							"Price")));

			if ((Double) mylist.get(position).get("OPrice") == 0) {
				RelativeLayout rl_oprice = (RelativeLayout) convertView
						.findViewById(R.id.rl_itemcard_oprice);
				rl_oprice.setVisibility(View.GONE);
			} else {
				holder.tv_oprice.setText("£§"
						+ Data.doubleTrans((Double) mylist.get(position).get(
								"OPrice")));
			}

			holder.tv_sellnum.setText("“— € "
					+ (Integer) mylist.get(position).get("SellNum"));

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
		TextView tv_price;
		TextView tv_oprice;
		TextView tv_sellnum;
	}

}

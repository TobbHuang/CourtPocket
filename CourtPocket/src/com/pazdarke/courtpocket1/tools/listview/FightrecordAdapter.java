package com.pazdarke.courtpocket1.tools.listview;

import java.util.ArrayList;
import java.util.HashMap;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.activity.BookActivity;
import com.pazdarke.courtpocket1.activity.CourtinfoActivity;
import com.pazdarke.courtpocket1.activity.TeamInfoActivity;
import com.pazdarke.courtpocket1.data.Data;
import com.pazdarke.courtpocket1.tools.listview.FightbillAdapter.MyOnclickListener;

import android.content.Context;
import android.content.Intent;
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

public class FightrecordAdapter extends BaseAdapter {

	Context context;
	ArrayList<HashMap<String, Object>> mylist;
	int layoutID;

	public FightrecordAdapter(Context context,
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
				holder.ll_gameprice=(LinearLayout)convertView.findViewById(R.id.ll_itemfight_gameprice);
				holder.tv_gameprice = (TextView) convertView
						.findViewById(R.id.tv_itemfight_gameprice);
				holder.btn_fight = (Button) convertView
						.findViewById(R.id.btn_itemfight_fight);

				convertView.setTag(holder);
			} else {
				holder = (TeamlistViewHolder) convertView.getTag();
			}

			holder.btn_fight.setVisibility(View.GONE);
			
			if (!(Boolean) mylist.get(position).get("isMyTeam")) {
				holder.ll_gameprice.setVisibility(View.GONE);
			}

			if ((Boolean) mylist.get(position).get("hasPath1")) {
				String path = (String) mylist.get(position).get("TeamPic");
				new AsyncViewTask(context, path, holder.iv_teamlogo1,2)
						.execute(holder.iv_teamlogo1);// “Ï≤Ωº”‘ÿÕº∆¨
			}
			
			if ((Boolean) mylist.get(position).get("hasPath3")) {
				String path = (String) mylist.get(position).get("GymPic");
				new AsyncViewTask(context, path, holder.iv_gymlogo,4)
						.execute(holder.iv_gymlogo);// “Ï≤Ωº”‘ÿÕº∆¨
			}
			holder.iv_gymlogo.setOnClickListener(new MyOnclickListener(mylist, position));

			holder.iv_teamlogo1.setOnClickListener(new MyOnclickListener(
					mylist, position));

			holder.tv_teamname1.setText((String) mylist.get(position).get(
					"TeamName"));

			holder.iv_teamlogo2
					.setImageResource(R.drawable.ic_mainpage_tab3_defaultphoto);
			if ((Boolean) mylist.get(position).get("hasPath2")) {
				String path = (String) mylist.get(position).get("oTeamPic");
				new AsyncViewTask(context, path, holder.iv_teamlogo2,2)
						.execute(holder.iv_teamlogo2);// “Ï≤Ωº”‘ÿÕº∆¨
			}

			holder.tv_teamname2.setText((String) mylist.get(position).get(
					"oTeamName"));

			holder.iv_teamlogo2.setOnClickListener(new MyOnclickListener(
					mylist, position));

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

			holder.tv_gameprice.setText("£§"
					+ Data.doubleTrans((Double) mylist.get(position).get("Price")));

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
		LinearLayout ll_gameprice;
		TextView tv_gameprice;
		Button btn_fight;
	}

	class MyOnclickListener implements OnClickListener {

		ArrayList<HashMap<String, Object>> mylist;
		int i;

		MyOnclickListener(ArrayList<HashMap<String, Object>> mylist, int position) {
			this.mylist=mylist;
			i = position;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent;
			switch (v.getId()) {
			case R.id.iv_itemfight_teamlogo1:
				intent = new Intent(context, TeamInfoActivity.class);
				intent.putExtra("ID",
						(Integer) mylist.get(i).get("TeamID") + "");
				context.startActivity(intent);
				break;
			case R.id.iv_itemfight_teamlogo2:
				intent = new Intent(context, TeamInfoActivity.class);
				intent.putExtra("ID",
						(Integer) mylist.get(i).get("oTeamID") + "");
				context.startActivity(intent);
				break;
			case R.id.iv_itemfight_gymlogo:
				intent=new Intent(context,CourtinfoActivity.class);
				intent.putExtra("ID", (Integer) mylist.get(i).get("GymID")+"");
				context.startActivity(intent);
				break;
			}
		}

	}

}

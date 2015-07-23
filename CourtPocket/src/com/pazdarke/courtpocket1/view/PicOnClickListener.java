package com.pazdarke.courtpocket1.view;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class PicOnClickListener implements OnClickListener {

	int picNum, allPicNum, currentPicNum;
	JSONObject json;
	Context context;
	Class<?> class1;

	public PicOnClickListener(int picNum, int allPicNum, int currentPicNum,
			JSONObject json, Context context, Class<?> class1) {
		this.picNum = picNum;
		this.allPicNum = allPicNum;
		this.currentPicNum = currentPicNum;
		this.json = json;
		this.context = context;
		this.class1 = class1;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(context, class1);
		intent.putExtra("picNum", picNum);
		intent.putExtra("allPicNum", allPicNum);
		intent.putExtra("currentPicNum", currentPicNum);
		try {
			for (int i = 0; i < json.getInt("CommentPicNum"); i++) {
				intent.putExtra("Path" + i, json.getString("CommentPic" + i));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		context.startActivity(intent);
	}

}

package com.pazdarke.courtpocket1.choosepics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

public class ImgFileListActivity extends Activity implements OnItemClickListener{

	ListView listView;
	Util util;
	ImgFileListAdapter listAdapter;
	List<FileTraversal> locallist;
	
	public static Activity instance_imgFileList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_imgfilelist);
		
		instance_imgFileList=this;
		
		ImageView iv_leftarrow=(ImageView)findViewById(R.id.iv_imgfilelist_leftarrow);
		iv_leftarrow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		listView=(ListView) findViewById(R.id.listView1);
		util=new Util(this);
		locallist=util.LocalImgFileList();
		List<HashMap<String, String>> listdata=new ArrayList<HashMap<String,String>>();
		Bitmap bitmap[] = null;
		if (locallist!=null) {
			bitmap=new Bitmap[locallist.size()];
			for (int i = 0; i < locallist.size(); i++) {
				HashMap<String, String> map=new HashMap<String, String>();
				map.put("filecount", locallist.get(i).filecontent.size()+"уе");
				map.put("imgpath", locallist.get(i).filecontent.get(0)==null?null:(locallist.get(i).filecontent.get(0)));
				map.put("filename", locallist.get(i).filename);
				listdata.add(map);
			}
		}
		listAdapter=new ImgFileListAdapter(this, listdata);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(this);
		
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Intent intent=new Intent(this,ImgsActivity.class);
		Bundle bundle=new Bundle();
		bundle.putParcelable("data", locallist.get(arg2));
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
}

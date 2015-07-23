package com.pazdarke.courtpocket1.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.tools.appmanager.AppManager;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SearchActivity extends Activity {
	
	int i=0;// 不知道为什么点击键盘的“搜索” 监听里的代码会执行两边，所以用这个来暂时解决这个问题
	
	ImageView iv_leftarrow;
	
	EditText et_searchtext;
	ImageView iv_editclear;
	
	ListView lv_pastsearch;
	ArrayList<HashMap<String, Object>> mylist_pastsearch;
	SimpleAdapter adapter_pastsearch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search);
		
		AppManager.getAppManager().addActivity(this);
		
		iv_leftarrow=(ImageView)findViewById(R.id.iv_search_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);
		
		iv_editclear=(ImageView)findViewById(R.id.iv_search_editclear);
		iv_editclear.setOnClickListener(onClickListener);
		
		TextView tv_search=(TextView)findViewById(R.id.tv_search_search);
		tv_search.setOnClickListener(onClickListener);
		
		et_searchtext=(EditText)findViewById(R.id.et_search_searchtext);
		et_searchtext.addTextChangedListener(textWatcher);
		et_searchtext.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				
				if(keyCode==KeyEvent.KEYCODE_ENTER){
					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(SearchActivity.this
									.getCurrentFocus().getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);
					
					if (i == 0) {
						
						i++;

						String searchtext = et_searchtext.getText().toString();
						if (!searchtext.equals("")) {

							Intent intent = new Intent(SearchActivity.this,
									SearchresultActivity.class);
							intent.putExtra("GymName", et_searchtext.getText()
									.toString());
							startActivity(intent);

							SharedPreferences sp = PreferenceManager
									.getDefaultSharedPreferences(SearchActivity.this);
							String str_searchhistory = sp.getString(
									"SearchHistory", "");
							str_searchhistory = searchtext + ","
									+ str_searchhistory;
							Editor pEdit = sp.edit();
							pEdit.putString("SearchHistory", str_searchhistory);
							pEdit.commit();
							mylist_pastsearch.clear();
							init_pastlist();
						}
					} else{
						i=0;
					}
				} else{
					v.requestFocus();
				}

				return false;
			}
		});

		TextView tv_clearhistory = (TextView) findViewById(R.id.tv_search_clearhistory);
		tv_clearhistory.setOnClickListener(onClickListener);

		lv_pastsearch = (ListView) findViewById(R.id.lv_search_pastsearchlist);
		mylist_pastsearch = new ArrayList<HashMap<String, Object>>();

		init_pastlist();

		et_searchtext.setFocusable(true);
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			public void run() {
				InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			}

		}, 400);
		
	}
	
	void init_pastlist(){
		
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(SearchActivity.this);		
		String str_searchhistory=sp.getString("SearchHistory", "");
		final String[] array_searchhistory=str_searchhistory.split(",");
		
		for(int i=0;i<array_searchhistory.length;i++){
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("item", array_searchhistory[i]);
			mylist_pastsearch.add(map);
		}
		
		adapter_pastsearch = new SimpleAdapter(SearchActivity.this, mylist_pastsearch,
				R.layout.item_pastsearch, new String[] { "item"}, new int[] {
						R.id.tv_pastsearch_item});

		lv_pastsearch.setAdapter(adapter_pastsearch);
		lv_pastsearch.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				// Toast.makeText(SearchActivity.this, array_searchhistory[position], Toast.LENGTH_SHORT).show();
				String GymName=((TextView)view.findViewById(R.id.tv_pastsearch_item)).getText().toString();
				Intent intent=new Intent(SearchActivity.this,SearchresultActivity.class);
	            intent.putExtra("GymName", GymName);
	            startActivity(intent);
			}

		});
	}
	
	OnClickListener onClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.iv_search_leftarrow:
				finish();
				break;
			case R.id.iv_search_editclear:
				et_searchtext.setText("");
				iv_editclear.setVisibility(View.GONE);
				break;
			case R.id.tv_search_search:
				String searchtext=et_searchtext.getText().toString();
				if(!searchtext.equals("")){
		            
		            Intent intent=new Intent(SearchActivity.this,SearchresultActivity.class);
		            intent.putExtra("GymName", et_searchtext.getText().toString());
		            startActivity(intent);
		            
		            SharedPreferences sp = PreferenceManager
							.getDefaultSharedPreferences(SearchActivity.this);		
					String str_searchhistory=sp.getString("SearchHistory", "");
					str_searchhistory=searchtext+","+str_searchhistory;
					Editor pEdit = sp.edit();
		            pEdit.putString("SearchHistory",str_searchhistory);
		            pEdit.commit();
		            mylist_pastsearch.clear();
					init_pastlist();
				}
				break;
			case R.id.tv_search_clearhistory:
				SharedPreferences sp = PreferenceManager
						.getDefaultSharedPreferences(SearchActivity.this);
				Editor pEdit = sp.edit();
				pEdit.putString("SearchHistory", "");
				pEdit.commit();
				mylist_pastsearch.clear();
				init_pastlist();
				break;
			}
		}
	};
	
	TextWatcher textWatcher = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			if(et_searchtext.getText().toString().equals("")){
				iv_editclear.setVisibility(View.GONE);
			} else{
				iv_editclear.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			if(et_searchtext.getText().toString().equals("")){
				iv_editclear.setVisibility(View.GONE);
			} else{
				iv_editclear.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			if(et_searchtext.getText().toString().equals("")){
				iv_editclear.setVisibility(View.GONE);
			} else{
				iv_editclear.setVisibility(View.VISIBLE);
			}
		}
	
	};
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		
		Intent i = getBaseContext().getPackageManager()
				.getLaunchIntentForPackage(getBaseContext().getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		AppManager.getAppManager().AppExit(this);
	}
	
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
}

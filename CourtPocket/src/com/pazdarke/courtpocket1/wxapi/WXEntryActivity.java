package com.pazdarke.courtpocket1.wxapi;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{
	
	IWXAPI api;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_wxentry);
		
		api = WXAPIFactory.createWXAPI(this, Data.WECHATAPPID, false);
		
		api.handleIntent(getIntent(), this);
		
		TextView tv_click=(TextView)findViewById(R.id.tv_wxentry_click);
		tv_click.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
	}

	@Override
	public void onReq(BaseReq req) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResp(BaseResp resp) {
		// TODO Auto-generated method stub
		Intent intent=new Intent("WechatLogin");
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			intent.putExtra("result", 0);
			intent.putExtra("code", ((SendAuth.Resp) resp).code);
			sendBroadcast(intent);finish();
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			intent.putExtra("result", 1);
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			intent.putExtra("result", 2);
			break;
		}
		finish();
	}
	
}

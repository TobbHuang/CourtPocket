package com.pazdarke.courtpocket1.tools.listview;

import com.pazdarke.courtpocket1.httpConnection.HttpGetPicConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

public class AsyncGyminfoViewTask extends AsyncTask<View, Void, Bitmap> {
	ImageView view1;
	ImageView view2;
	String path;
	public Bitmap bm;
	Context context;

	public AsyncGyminfoViewTask(Context mContext, String path, ImageView view1,
			ImageView view2) {
		this.path = path;
		this.view1 = view1;
		this.view2 = view2;
		this.context = mContext;
	}

	protected Bitmap doInBackground(View... views) {

		try {
			bm = new HttpGetPicConnection().httpConnection(path, 3, context);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			System.out.println("ÄÚ´æ±¬Õ¨À²");
			e.printStackTrace();
		}

		return bm;
	}

	protected void onPostExecute(Bitmap bitmap) {

		if (bm != null) {
			view1.setImageBitmap(bm);
			view2.setImageBitmap(bm);
			// bm.recycle();
			// bm=null;
		}
	}

}
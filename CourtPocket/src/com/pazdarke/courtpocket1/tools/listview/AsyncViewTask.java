package com.pazdarke.courtpocket1.tools.listview;

import com.pazdarke.courtpocket1.httpConnection.HttpGetPicConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

public class AsyncViewTask extends AsyncTask<View, Void, Bitmap> {
	ImageView view;
	String path;
	int sampleSize;
	public Bitmap bm;
	Context context;

	public AsyncViewTask(Context mContext, String path, ImageView view,
			int sampleSize) {
		this.path = path;
		this.view = view;
		this.sampleSize = sampleSize;
		this.context=mContext;
	}

	protected Bitmap doInBackground(View... views) {

		try {
			bm = new HttpGetPicConnection().httpConnection(path, sampleSize,
					context);
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
			view.setImageBitmap(bm);
			// bm.recycle();
			// bm=null;
		}
	}

}
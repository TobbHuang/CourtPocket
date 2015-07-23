package com.pazdarke.courtpocket1.httpConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.pazdarke.courtpocket1.R;
import com.pazdarke.courtpocket1.data.Data;

public class HttpGetPicConnection {

	public static HashMap<String, SoftReference<Bitmap>> mImageCache = new HashMap<String, SoftReference<Bitmap>>();

	/*public Bitmap httpConnection(String url) {
		Bitmap bitmap = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 1;
		try {

			if (mImageCache.containsKey(url)) {
				SoftReference<Bitmap> softReference = mImageCache.get(url);
				bitmap = softReference.get();
				if (null != bitmap)
					return bitmap;
			}

			// 检查本地是否有图片的存储
			File dir = new File(Environment.getExternalStorageDirectory()
					+ "/CourtPocket");

			if (!dir.exists()) {
				dir.mkdir();
			}

			String imgFilePath = Environment.getExternalStorageDirectory()
					+ "/CourtPocket/" + url.replace("\\", "-").replace("/", "-");
			File file = new File(imgFilePath);
			if (file.exists()) {
				bitmap = BitmapFactory.decodeFile(imgFilePath,opts);

				mImageCache.put(url, new SoftReference<Bitmap>(bitmap));
				
				return bitmap;
			}

			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setIntParameter(
					CoreConnectionPNames.SO_TIMEOUT, 10000); // 超时设置
			httpClient.getParams().setIntParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);// 连接超时

			HttpGet httpGet = new HttpGet(Data.URL + url.replace("\\", "/"));

			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 取得相关信息 取得HttpEntiy
				HttpEntity httpEntity = httpResponse.getEntity();
				// 获得一个输入流
				InputStream is = httpEntity.getContent();
				bitmap = BitmapFactory.decodeStream(is,null,opts);
				mImageCache.put(url, new SoftReference<Bitmap>(bitmap));
				is.close();

				if (bitmap != null) {
					FileOutputStream out = new FileOutputStream(file);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
					out.flush();
					out.close();
				}

			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bitmap;

	}*/
	
	public Bitmap httpConnection(String url,int sampleSize,Context context) {
		Bitmap bitmap = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = sampleSize;
		try {

			if (mImageCache.containsKey(url)) {
				SoftReference<Bitmap> softReference = mImageCache.get(url);
				bitmap = softReference.get();
				if (null != bitmap)
					return bitmap;
			}

			// 检查本地是否有图片的存储
			File dir = new File(Environment.getExternalStorageDirectory()
					+ "/CourtPocket");

			if (!dir.exists()) {
				dir.mkdir();
			}

			String imgFilePath = Environment.getExternalStorageDirectory()
					+ "/CourtPocket/" + url.replace("\\", "-").replace("/", "-");
			File file = new File(imgFilePath);
			if (file.exists()) {
				bitmap = BitmapFactory.decodeFile(imgFilePath,opts);

				mImageCache.put(url, new SoftReference<Bitmap>(bitmap));
				
				return bitmap;
			}

			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setIntParameter(
					CoreConnectionPNames.SO_TIMEOUT, 10000); // 超时设置
			httpClient.getParams().setIntParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);// 连接超时

			HttpGet httpGet = new HttpGet(Data.URL + url.replace("\\", "/"));

			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 取得相关信息 取得HttpEntiy
				HttpEntity httpEntity = httpResponse.getEntity();
				// 获得一个输入流
				InputStream is = httpEntity.getContent();
				
				FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                is.close();
                fos.close();
                
                bitmap = BitmapFactory.decodeFile(imgFilePath,opts);
				mImageCache.put(url, new SoftReference<Bitmap>(bitmap));

				/*if (bitmap != null) {
					FileOutputStream out = new FileOutputStream(file);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
					out.flush();
					out.close();
				}*/

			}

		} catch (Exception e) {
			e.printStackTrace();
			bitmap = BitmapFactory.decodeResource(context.getResources(),
					R.color.lightGrey);
		}

		return bitmap;

	}

}

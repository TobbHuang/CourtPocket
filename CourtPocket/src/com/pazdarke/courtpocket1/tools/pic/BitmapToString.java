package com.pazdarke.courtpocket1.tools.pic;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Base64;

public class BitmapToString {
	public static String bitmaptoString(Bitmap bitmap) {

		// ½«Bitmap×ª»»³É×Ö·û´®

		String string = null;

		ByteArrayOutputStream bStream = new ByteArrayOutputStream();

		bitmap.compress(CompressFormat.JPEG, 100, bStream);

		byte[] bytes = bStream.toByteArray();

		string = Base64.encodeToString(bytes, Base64.DEFAULT);

		return string;

	}
}

package com.pazdarke.courtpocket1.data;

public class Data {

	// 手机分辨率信息
	public static int SCREENWIDTH;
	public static int SCREENHEIGHT;

	public static String CITY;// 考虑到可能定位失败，默认是成都
	public static String ADDRESS;
	public static double LATITUDE;// 纬度
	public static double LONGITUDE;// 经度
	public static boolean ISTRUELOCATION;// 标记当前定位信息是否为真实信息，定位失败时默认为成都最中心的经纬度

	public static String URL = "http://120.25.123.42/CourtPocket/";

	public static boolean ISLOGIN = false;
	public static String USERID;
	public static String PASSCODE;
	public static String PHONE;
	public static String NICKNAME;
	public static String MONEY;

	public static String WECHATAPPID = "wx7c7d19c133fac1e9";

	// double小数点优化
	public static String doubleTrans(double num) {
		if (num % 1.0 == 0) {
			return String.valueOf((long) num);
		}
		return String.valueOf(num);
	}

}

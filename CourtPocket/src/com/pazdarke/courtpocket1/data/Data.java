package com.pazdarke.courtpocket1.data;

public class Data {

	// �ֻ��ֱ�����Ϣ
	public static int SCREENWIDTH;
	public static int SCREENHEIGHT;

	public static String CITY;// ���ǵ����ܶ�λʧ�ܣ�Ĭ���ǳɶ�
	public static String ADDRESS;
	public static double LATITUDE;// γ��
	public static double LONGITUDE;// ����
	public static boolean ISTRUELOCATION;// ��ǵ�ǰ��λ��Ϣ�Ƿ�Ϊ��ʵ��Ϣ����λʧ��ʱĬ��Ϊ�ɶ������ĵľ�γ��

	public static String URL = "http://120.25.123.42/CourtPocket/";

	public static boolean ISLOGIN = false;
	public static String USERID;
	public static String PASSCODE;
	public static String PHONE;
	public static String NICKNAME;
	public static String MONEY;

	public static String WECHATAPPID = "wx7c7d19c133fac1e9";

	// doubleС�����Ż�
	public static String doubleTrans(double num) {
		if (num % 1.0 == 0) {
			return String.valueOf((long) num);
		}
		return String.valueOf(num);
	}

}

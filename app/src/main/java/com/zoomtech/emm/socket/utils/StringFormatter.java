package com.zoomtech.emm.socket.utils;

import java.text.SimpleDateFormat;

public class StringFormatter
{
	final static String TAG = "StringFormatter";
	private static char[] ff = { 65, 66, 67, 68, 69, 70 };
	private static SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String FormatDate(int paramInt1, int paramInt2, int paramInt3)
	{
		StringBuilder localStringBuilder = new StringBuilder();
		localStringBuilder.append(paramInt1);
		int i = localStringBuilder.length();
		localStringBuilder.append(paramInt2 + 100);
		localStringBuilder.setCharAt(i, '-');
		int j = localStringBuilder.length();
		localStringBuilder.append(paramInt3 + 100);
		localStringBuilder.setCharAt(j, '-');
		return localStringBuilder.toString();
	}

	public static String FormatDec(Byte paramByte)
	{
		char c;
		if (paramByte.byteValue() >= 10) {
            c = ff[(-10 + paramByte.byteValue())];
        } else {
            c = (char) paramByte.byteValue();
        }
		return "" + c;
	}

	// 将浮点数转化 为指定小数位数的 字符串
	public static String FormatFloatData(float value, int decimal )
	{
		int[] arrayOfInt = { 1, 10, 100, 1000, 10000, 100000 };

		StringBuilder sb = new StringBuilder();
		int i = (int)(0.5D + value * arrayOfInt[decimal]);
		sb.append(i / arrayOfInt[decimal]);
		int j = sb.length();
		sb.append(i % arrayOfInt[decimal] + arrayOfInt[decimal]);
		sb.setCharAt(j, '.');
		return sb.toString();
	}

	public static String FormatToString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex;
		}
		return ret;
	}

	// 替换原来的程序
	public static String FormatToString(byte[] buffer, int len ) {
		StringBuffer sb = new StringBuffer(buffer.length * 2);

		int j = 0 ;
		for (int i = 0; i < len; i++) {
			sb.append("");
			sb.append(  Character.forDigit((buffer[i] & 240) >> 4, 16));
			sb.append(  Character.forDigit(buffer[i] & 15, 16) );
			sb.append(" ");
			if (( (i+1) % 8 ) == 0 ){
				sb.append("\n");
			}
		}
		String str = sb.toString();
		str = str.toUpperCase();
		return str ;
	}

	// 将 4 位的家庭网络地址转换为字符串
	public static String net2str(byte[] buffer  ) {
		StringBuffer sb = new StringBuffer(4 * 2);

		int j = 0 ;
		for (int i = 0; i < 4; i++) {
			sb.append("");
			sb.append(  Character.forDigit((buffer[i] & 240) >> 4, 16));
			sb.append(  Character.forDigit(buffer[i] & 15, 16) );
			sb.append(" ");
		}
		String str = sb.toString();
		str = str.toUpperCase();
		return str ;
	}

	// 将 5 位的地址转换为字符串
	public static String addr2str(byte[] buffer  ) {
		StringBuffer sb = new StringBuffer(5 * 2);

		int j = 0 ;
		for (int i = 0; i < 5; i++) {
			sb.append("");
			sb.append(  Character.forDigit((buffer[i] & 240) >> 4, 16));
			sb.append(  Character.forDigit(buffer[i] & 15, 16) );
			if(i!=4) {
                sb.append(":");
            }
		}
		String str = sb.toString();
		str = str.toUpperCase();
		return str ;
	}

	// 将 字符串转换为   5 位的地址
	public static byte[] str2addr(String s_addr) {
		byte[] addr = new byte[5];
		s_addr = s_addr.trim();
		String str[] = s_addr.split(":");
		for (int i = 0; i < 5; i++) {
			addr[i] = (byte) Integer.parseInt(str[i], 16);
		}
		return addr;
	}


	public static String FormatToString(byte paramByte)
	{
		int i = (paramByte & 0xF0) >> 4;
		byte b = (byte) (paramByte & 0x0F);
		return FormatDec( Byte.valueOf((byte) i) ) + FormatDec(Byte.valueOf(b));
	}

	public static String byte2str(byte bb)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("0x" );
		sb.append(Character.forDigit((bb & 240) >> 4, 16));
		sb.append(Character.forDigit(bb & 15, 16) );
		String str = sb.toString();
		str = str.toUpperCase();
		return str ;
	}

}


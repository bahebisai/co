package com.xiaomi.emm.socket.tcp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteOrder;
import java.util.Enumeration;

/**
 * 网络相关工具类
 */
public class NetworkUtils {
	private static final String TAG = "NetworkUtils";
	public static final int TYPE_NONE = 0;
	public static final int TYPE_WIFI = 1;
	public static final int TYPE_NET = 2;
	public static final int TYPE_WAP = 3;
	public static final int TYPE_ALL = 4;

	private static final int STARTACTIVITY_REQUEST_CODE = 110;

	public static boolean hasInternet(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			return false;
		}
		return true;
	}

	public static void showNetworkDialog(final Context context,
										 DialogInterface.OnClickListener... params) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("没有可用的网络");
		builder.setMessage("请开启您的网络连接");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				((Activity) context).startActivityForResult(intent,
						STARTACTIVITY_REQUEST_CODE);
			}
		});

		DialogInterface.OnClickListener listener = null;
		if (null != params && params.length > 0) {
			listener = params[0];
		}
		builder.setNegativeButton("取消", listener);
		try {
			builder.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean checkNetwork(Context context) {
		return hasInternet(context);
	}

	public static int getNetWorkType(NetworkInfo info) {
		if (info == null || !info.isAvailable()) {
			return TYPE_NONE;
		}
		String typeName = info == null ? "cmnet" : info.getTypeName();
		if ("wifi".equalsIgnoreCase(typeName)) {
			return TYPE_WIFI;

		} else if (typeName.toLowerCase().indexOf("net") != -1) {
			return TYPE_NET;
		} else {

			return TYPE_WAP;
		}
	}

	public static int getNetWorkType(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info == null || !info.isAvailable()) {
			return TYPE_NONE;
		}
		String typeName = info == null ? "cmnet" : info.getTypeName();
		if ("wifi".equalsIgnoreCase(typeName)) {
			return TYPE_WIFI;

		} else if (typeName.toLowerCase().indexOf("net") != -1) {
			return TYPE_NET;
		} else {
			return TYPE_WAP;
		}
	}

	public static int getNetWorkType_2(Context context) {
		// 获取系统的连接服务
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		// 获取网络的连接情况
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

		// 无网络连接
		if (networkInfo == null || !networkInfo.isAvailable()) {
			return TYPE_NONE;
		}

		// WIFI链接
		if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return TYPE_WIFI;

			// 移动数据链接
		} else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
			return TYPE_WAP;

			// 以太网数据连接
		} else if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
			return TYPE_NET;

		} else {
			return TYPE_WAP;
		}
	}

	// 当前网络连接类型， 以 strng 方式返回
	public static String getNetWorkTypeStr(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info == null || !info.isAvailable()) {
			return "未连网";
		} else {
			return info.getTypeName();
		}

	}

	public static boolean isWIFI(Context context) {
		int type = getNetWorkType(context);
		return (type == TYPE_WIFI);
	}

	public static String getWifiSSID(Context context) {
		String connectedSsid = "";
		if (context == null) {
			return connectedSsid;
		}
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if(wifiManager.isWifiEnabled()){
			WifiInfo WifiInfo = wifiManager.getConnectionInfo();
			connectedSsid = WifiInfo.getSSID();
			if (!TextUtils.isEmpty(connectedSsid)
					&& connectedSsid.startsWith("\"")
					&& connectedSsid.endsWith("\"")){
				connectedSsid = connectedSsid.substring(1, connectedSsid.length() - 1);
			}
		}
		return connectedSsid;
	}

	public static int getWifiSignalStrength(Context context) {
		int strength = 0;

		final WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		strength = info.getRssi();

		return strength;
	}

	public static String getWifiMacAddress(Context context) {
		try {
			WifiManager wm = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			Log.w(TAG, "getWifiMacAddress() WifiManager = " + wm);
			int wifi_state = wm.getWifiState();
			Log.w(TAG, "getWifiMacAddress() wifi_state = " + wifi_state);
			if (wifi_state == WifiManager.WIFI_STATE_ENABLED) {
				WifiInfo info = wm.getConnectionInfo();
				Log.w(TAG, "getWifiMacAddress() WifiInfo = " + info);
				String mac_addr = info.getMacAddress();
				Log.w(TAG, "getWifiMacAddress() mac_addr = " + mac_addr);
				String lower_addr = mac_addr.toLowerCase();
				return lower_addr;
			} else {
				Log.w(TAG, "getWifiMacAddress() wifi 状态  : " + wifi_state
						+ " , 不可用  ");
			}
		} catch (Exception e) {
			// 模拟器或某些平板可能为空
			e.printStackTrace();
		}

		return null;
	}

	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
			Log.w(TAG, "Fail to get IpAddress:" + ex.toString());
		}
		return null;
	}

	public static boolean isLittleEndian() {
		return (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN);
	}

	public static boolean isBigEndian() {
		return (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);
	}

	public static boolean isEthernetDataEnable(Context context)
			throws Exception {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(/*"connectivity"*/Context.CONNECTIVITY_SERVICE);
		NetworkInfo eth_info = manager
				.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
		Log.w(TAG, "isEthernetDataEnable()  eth_info = " + eth_info);
		if (eth_info == null) {
			Log.w(TAG, "isEthernetDataEnable()  eth_info is null  !!! ");
			return false;
		}

		boolean b_ret = eth_info.isConnectedOrConnecting();
		return b_ret;
	}

	public static boolean isNetworkAvailable(Context paramContext) {
		ConnectivityManager localConnectivityManager = (ConnectivityManager) paramContext
				.getSystemService(/*"connectivity"*/Context.CONNECTIVITY_SERVICE);
		if (localConnectivityManager == null) {
			Log.w(TAG, "couldn't get connectivity manager");
			Log.w(TAG, "network is not available");
			return false;
		}

		NetworkInfo[] arrayOfNetworkInfo = localConnectivityManager
				.getAllNetworkInfo();
		if (arrayOfNetworkInfo == null) {
			Log.w(TAG, "couldn't get connectivity manager");
			return false;
		}

		for (int i = 0; i < arrayOfNetworkInfo.length; i++) {

			if ((!arrayOfNetworkInfo[i].isAvailable())
					|| (!arrayOfNetworkInfo[i].isConnected())) {
                continue;
            }

			Log.w(TAG, "network is available");
			return true;
		}

		return false;

	}

	public static boolean isWifiDataEnable(Context paramContext)
			throws Exception {
		return ((ConnectivityManager) paramContext
				.getSystemService(/*"connectivity"*/Context.CONNECTIVITY_SERVICE)).getNetworkInfo(1)
				.isConnectedOrConnecting();
	}

	/**
	 * 判断wifi是否有密码
	 */
	public static boolean isWifiHasPassword(ScanResult scanResult){
		String capabilities = scanResult.capabilities.trim();
		if (TextUtils.isEmpty(capabilities)
				|| !capabilities.contains("WPA")) {
			return false;
		}
		return true;
	}

	/**
	 * 获取手机连接的wifi局域网的IP
	 * @param context
	 * @return
	 */
	public static String getWlanIp(Context context) {
		WifiManager wifiService = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiinfo = wifiService.getConnectionInfo();
		int ipInt = wifiinfo.getIpAddress();
		String ip = (ipInt & 0xFF) + "." + ((ipInt >> 8) & 0xFF) + "." + ((ipInt >> 16) & 0xFF)
				+ "." + (ipInt >> 24 & 0xFF);
		return ip;
	}
}

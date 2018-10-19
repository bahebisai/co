package com.xiaomi.emm.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

import static android.content.Context.TELEPHONY_SERVICE;

public class PhoneUtils {
    private static final String TAG = PhoneUtils.class.getName();
    static final int CURRENT_NETWORK_STATES_NO = -1; //没有网络
    static final int CURRENT_NETWORK_STATES_MOBILE = 0; //Mobile
    static final int CURRENT_NETWORK_STATES_WIFI = 1; //WIFI

    //获得网络状态
    public static int getNetWorkState(Context context) {
        int type = CURRENT_NETWORK_STATES_NO;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                type = CURRENT_NETWORK_STATES_WIFI;
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                type = CURRENT_NETWORK_STATES_MOBILE;
            }
        }
        return type;
    }

    /**
     * 获取当前的网络状态: 0：没有网络 1：WIFI网络 2：WAP网络 3：NET网络
     *
     * @return
     */
    public static int getNetworkType(Context context) {//todo baii 1.里面的1.2.3换掉 2.和getNetWorkState是否合并
        int netType = 0;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            String extraInfo = networkInfo.getExtraInfo();
            if (!TextUtils.isEmpty(extraInfo)) {
                if ("cmnet".equals(extraInfo.toLowerCase())) {
                    netType = 3;
                } else {
                    netType = 2;
                }
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = 1;
        }
        return netType;
    }

    //获得当前设置的电话号码
    public static String getTelePhonyNumber(Context context) {//todo baii 拿不到电话？？？，登录的时候触发上传
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Log.d("baii", "tel number " + telephonyManager.getLine1Number());
        return telephonyManager.getLine1Number();
    }

    /**
     * 获得sim卡imsi
     *
     * @return
     */
    public static String[] getSubscriberId(Context context) {//todo baii util device or phone
        String[] imsis = new String[2];
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getSubscriberId = telephonyManager.getClass().getMethod("getSubscriberId", int.class);
            if (getSubscriberId != null) {
                imsis[0] = (String) getSubscriberId.invoke(telephonyManager, 0);
                imsis[1] = (String) getSubscriberId.invoke(telephonyManager, 1);
            }
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "getSimSerialNumber " + e.toString());
        }
        return imsis;
    }

    /**
     * 获得电话号码
     *
     * @return
     */
    public static String[] getLine1Number(Context context) {//todo baii 获取不到电话号码
        String[] nums = new String[2];
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getLine1Number = telephonyManager.getClass().getMethod("getLine1Number", int.class);
            if (getLine1Number != null) {
                nums[0] = (String) getLine1Number.invoke(telephonyManager, 0);
                nums[1] = (String) getLine1Number.invoke(telephonyManager, 1);
            }
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "getSimSerialNumber " + e.toString());
        }
        return nums;
    }

    /**
     * 网络是否已经连接，连接有网络且能通信
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    /**
     * 判断是否有网络可用，连接有网络即可用，但不一定能通信
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {//todo baii sure to use available?
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 已有网络连接或正在连接返回true
     */
    public static boolean isNetworkConnectedOrConnecting(Context context) {//todo baii util phone
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    public static String getIccid(Context context) {//todo baii util device
        String iccid = null;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getSimSerialNumber = telephonyManager.getClass().getMethod("getSimSerialNumber", int.class);
            if (getSimSerialNumber != null) {
                iccid = (String) getSimSerialNumber.invoke(telephonyManager, 0);
            }
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "getSimSerialNumber1 " + e.toString());
        }
        return iccid;
    }

    public static String getIccid1(Context context) {//todo baii util device
        String iccid = null;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getSimSerialNumber = telephonyManager.getClass().getMethod("getSimSerialNumber", int.class);
            if (getSimSerialNumber != null) {
                iccid = (String) getSimSerialNumber.invoke(telephonyManager, 1);
            }
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "getSimSerialNumber2 " + e.toString());
        }
        return iccid;
    }

    public static String getIccid2(Context context) {//todo baii util device
        String iccid = null;
        //   List<SubscriptionInfo> list = SubscriptionManager.from(mContext).getActiveSubscriptionInfoList();
        try {
            Method getSubId = SubscriptionManager.class.getMethod("getSubId", int.class);
            //   Log.w(TAG," ---list.sub= "+list.get(0).getSimSlotIndex());
            int[] sub = (int[]) getSubId.invoke(null, 1 /*list.get(0).getSimSlotIndex()*/);
            if (sub != null) {
                for (int i = 0; i < sub.length; i++) {
                    Log.w(TAG, "---sub= " + sub[i]);
                    //  String iccid1 = null;
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    try {
                        Method getSimSerialNumber = telephonyManager.getClass().getMethod("getSimSerialNumber", int.class);
                        if (getSimSerialNumber != null) {
                            iccid = (String) getSimSerialNumber.invoke(telephonyManager, sub[i]);
                        }
                    } catch (Exception e) {
                        LogUtil.writeToFile(TAG, "getSimSerialNumber2 " + e.toString());
                    }
                    Log.w(TAG, " getIccid2-2 =" + iccid);
                }
            }
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "getSimSerialNumber " + e.toString());
        }
        return iccid;
    }

    /**
     * 获得sim卡imsi1
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public static String getSubscriberId1(Context context) {//todo baii util device
        String imsi = null;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        try {
            Method getSubId = SubscriptionManager.class.getMethod("getSubId", int.class);
            //参数是卡槽1
            int[] sub = (int[]) getSubId.invoke(null, 0);
            if (sub != null) {
                for (int i = 0; i < sub.length; i++) {
                    // Log.w(TAG, "---sub= " + sub[i]);
                    Method getSubscriberId = telephonyManager.getClass()
                            .getMethod("getSubscriberId", int.class);
                    if (getSubscriberId != null) {
                        imsi = (String) getSubscriberId.invoke(telephonyManager, sub[i]);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "getSimSerialNumber " + e.toString());
        }
        Log.w(TAG, "getSubscriberId1 " + imsi);
        return imsi;
    }

    /**
     * 获得sim卡imsi2
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public static String getSubscriberId2(Context context) {//todo baii util device
        String imsi = null;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        try {
            Method getSubId = SubscriptionManager.class.getMethod("getSubId", int.class);
            //参数是卡槽2
            int[] sub = (int[]) getSubId.invoke(null, 1);
            if (sub != null) {
                for (int i = 0; i < sub.length; i++) {
                    Log.w(TAG, "---sub= " + sub[i]);
                    Method getSubscriberId = telephonyManager.getClass().getMethod("getSubscriberId", int.class);
                    if (getSubscriberId != null) {
                        imsi = (String) getSubscriberId.invoke(telephonyManager, sub[i]);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "getSimSerialNumber " + e.toString());
        }
        Log.w(TAG, "getSubscriberId2 " + imsi);
        return imsi;
    }
}

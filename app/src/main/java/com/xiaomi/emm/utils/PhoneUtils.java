package com.xiaomi.emm.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.xiaomi.emm.features.presenter.TheTang;

import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import static android.content.Context.BATTERY_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Phone相关工具类。包括手机网络信息、SIM卡信息等。
 */
public class PhoneUtils {
    private static final String TAG = PhoneUtils.class.getName();
    public static final int CURRENT_NETWORK_STATES_NO = -1; //没有网络
    public static final int CURRENT_NETWORK_STATES_MOBILE = 0; //Mobile
    public static final int CURRENT_NETWORK_STATES_WIFI = 1; //WIFI


    /**
     * 获得网络状态,
     * {@link PhoneUtils#CURRENT_NETWORK_STATES_MOBILE}
     * {@link PhoneUtils#CURRENT_NETWORK_STATES_WIFI}
     * {@link PhoneUtils#CURRENT_NETWORK_STATES_NO}
     *
     * @param context
     * @return
     */
    public static int getNetWorkState(Context context) {
        int type = CURRENT_NETWORK_STATES_NO;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                type = CURRENT_NETWORK_STATES_WIFI;
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                type = CURRENT_NETWORK_STATES_MOBILE;
                /*String extraInfo = networkInfo.getExtraInfo();//判断具体是wap还是net网络
                if (!TextUtils.isEmpty(extraInfo)) {
                    if ("cmnet".equals(extraInfo.toLowerCase())) {
                        type = 3;
                    } else {
                        type = 2;
                    }
                }*/
            }
        }
        return type;
    }

    /**
     * 获得当前手机的电话号码，小米测试此方法拿不到
     *
     * @param context
     * @return
     */
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
    public static String[] getSubscriberId(Context context) {
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
    public static boolean isNetworkAvailable(Context context) {
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
    public static boolean isNetworkConnectedOrConnecting(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    /**
     * 获取slot id 0的SIM卡 iccid
     *
     * @param context
     * @return
     */
    public static String getIccid(Context context, int slotId) {
        String iccid = "";
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getSimSerialNumber = telephonyManager.getClass().getMethod("getSimSerialNumber", int.class);
            if (getSimSerialNumber != null) {
                iccid = (String) getSimSerialNumber.invoke(telephonyManager, slotId);
            }
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "getSimSerialNumber " + slotId + " " + e.toString());
        }
        return iccid;
    }

    /**
     * 通过SubscriptionManager获取slotId为1的iccid
     *
     * @param context
     * @return
     */
    public static String getIccid2(Context context) {
        String iccid = "";
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
    public static String getSubscriberId1(Context context) {
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
    public static String getSubscriberId2(Context context) {
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

    /**
     * 获得剩余手机电量
     *
     * @return
     */
    public static String getPowerStatus(Context context) {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        return String.valueOf(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
    }

    /**
     * 获得Wifi MAC地址
     *
     * @return
     */
    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!"wlan0".equalsIgnoreCase(nif.getName())) {
                    continue;
                }
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    /**
     * 获得运营商名称
     *
     * @return
     */
    public static String[] getCarrierName() {
        String[] imsis = PhoneUtils.getSubscriberId(TheTang.getSingleInstance().getContext());
        String[] operaters = new String[2];
        if (imsis != null && imsis.length > 0) {
            for (int i = 0; i < imsis.length; i++) {
                if (imsis[i] != null) {
                    if (imsis[i].startsWith("46000") || imsis[i].startsWith("46002") || imsis[i].startsWith("46004")
                            || imsis[i].startsWith("46007") || imsis[i].startsWith("46008")) {
                        operaters[i] = "中国移动";
                    } else if (imsis[i].startsWith("46001") || imsis[i].startsWith("46006") || imsis[i].startsWith("46009") || imsis[i].startsWith("46010")) {
                        operaters[i] = "中国联通";
                    } else if (imsis[i].startsWith("46003") || imsis[i].startsWith("46005") || imsis[i].startsWith("46011")) {
                        operaters[i] = "中国电信";
                    }
                }
            }
        }
        return operaters;
    }
}
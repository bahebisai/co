package com.xiaomi.emm.utils;

import android.annotation.TargetApi;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.xiaomi.emm.features.presenter.TheTang;

import static android.content.Context.NETWORK_STATS_SERVICE;

/**
 * 数据流量工具类，包括根据时间、应用、类型获取使用的流量数据
 */
@TargetApi(Build.VERSION_CODES.M)
public class DataFlowStatsHelper {
    private static final String TAG = DataFlowStatsHelper.class.getName();
    private static NetworkStatsManager networkStatsManager = (NetworkStatsManager) TheTang.getSingleInstance().getContext().getSystemService(NETWORK_STATS_SERVICE);

    /**
     * 月初到此刻的使用的wifi
     *
     * @return 流量，byte
     */
    public static long getAllMonthWifi() {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, "", TimeUtils.getFirstDayTimeOfMonth(), System.currentTimeMillis());
        } catch (RemoteException e) {
            return -1;
        }
        return bucket.getRxBytes() + bucket.getRxBytes();
    }

    /**
     * 获取接收的所有WiFi使用数据
     *
     * @return wifi流量，byte
     */
    public long getAllRxBytesWifi() {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, "", 0, System.currentTimeMillis());
        } catch (RemoteException e) {
            return -1;
        }
        return bucket.getRxBytes();
    }

    /**
     * 获取传输的所有WiFi使用数据
     *
     * @return wifi流量，byte
     */
    public long getAllTxBytesWifi() {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, "", 0, System.currentTimeMillis());

        } catch (RemoteException e) {
            return -1;
        }
        return bucket.getTxBytes();
    }

    /**
     * 获取指定应用的接收的手机流量
     *
     * @param context
     * @param packageUid
     * @return 手机流量 byte
     */
    public long getPackageRxBytesMobile(Context context, int packageUid) {
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetailsForUid(
                    ConnectivityManager.TYPE_MOBILE,
                    PhoneUtils.getSubscriberId1(context),
                    0,
                    System.currentTimeMillis(),
                    packageUid);
        } catch (RemoteException e) {
            return -1;
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);
        networkStats.getNextBucket(bucket);
        return bucket.getRxBytes();
    }

    /**
     * 获取指定应用传输的手机流量
     *
     * @param context
     * @param packageUid
     * @return 手机流量 byte
     */
    public long getPackageTxBytesMobile(Context context, int packageUid) {
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetailsForUid(ConnectivityManager.TYPE_MOBILE,
                    PhoneUtils.getSubscriberId1(context), 0, System.currentTimeMillis(), packageUid);
        } catch (RemoteException e) {
            return -1;
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);
        return bucket.getTxBytes();
    }

    /**
     * 获取指定应用接收的WiFi流量
     *
     * @param packageUid
     * @return WiFi流量 byte
     */
    public long getPackageRxBytesWifi(int packageUid) {
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetailsForUid(ConnectivityManager.TYPE_WIFI, "", 0,
                    System.currentTimeMillis(), packageUid);
        } catch (RemoteException e) {
            return -1;
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);
        return bucket.getRxBytes();
    }

    /**
     * 获取指定应用传输的WiFi流量
     *
     * @param packageUid
     * @return WiFi流量 byte
     */
    public long getPackageTxBytesWifi(int packageUid) {
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetailsForUid(ConnectivityManager.TYPE_WIFI, "", 0, System.currentTimeMillis(),
                    packageUid);
        } catch (RemoteException e) {
            return -1;
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);
        return bucket.getTxBytes();
    }

    /**
     * 获取本周周一到当前的wifi数据流量
     *
     * @return WiFi流量 byte
     */
    public long getWorkWIFI() {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, "", TimeUtils.getMondayTime(), System.currentTimeMillis());
            return bucket.getRxBytes() + bucket.getTxBytes();
        } catch (RemoteException e) {
            return 0;
        }
    }

    /**
     * 获取本周周一到当前的Mobile数据流量
     *
     * @return 手机流量 byte
     */
    public static long getWorkMobile(Context context, String subscriberId) {
        NetworkStats.Bucket bucket;
        try {
            if (TextUtils.isEmpty(subscriberId)) {
                bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, PhoneUtils.getSubscriberId1(context),
                        TimeUtils.getMondayTime(),
                        System.currentTimeMillis());
            } else {
                bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, subscriberId,
                        TimeUtils.getMondayTime(),
                        System.currentTimeMillis());
            }
        } catch (RemoteException e) {
            return 0;
        }
        return bucket.getRxBytes() + bucket.getTxBytes();
    }

    /**
     * 获取今天截止到现在的Mobile数据流量
     *
     * @return 手机流量 byte
     */
    public static long getAllTodayMobile(Context context, String subscriberId) {
        NetworkStats.Bucket bucket;
        try {
            if (TextUtils.isEmpty(subscriberId)) {
                if (networkStatsManager == null) {
                    networkStatsManager = (NetworkStatsManager) TheTang.getSingleInstance().getContext().getSystemService(NETWORK_STATS_SERVICE);
                }
                bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, PhoneUtils.getSubscriberId1(context),
                        TimeUtils.getTimeOfMonth(), System.currentTimeMillis());
            } else {
                if (networkStatsManager == null) {
                    networkStatsManager = (NetworkStatsManager) TheTang.getSingleInstance().getContext().getSystemService(NETWORK_STATS_SERVICE);
                }
                bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, subscriberId,
                        TimeUtils.getTimeOfMonth(),
                        System.currentTimeMillis());
            }
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException");
            return 0;
        }
        return bucket.getTxBytes() + bucket.getRxBytes();
    }

    /**
     * 获取今天截止到现在的wifi数据流量
     *
     * @return WiFi流量 byte
     */
    public long getAllTodayWifi() {
        NetworkStats.Bucket bucket;
        try {
            if (networkStatsManager == null) {
                networkStatsManager = (NetworkStatsManager) TheTang.getSingleInstance().getContext().getSystemService(NETWORK_STATS_SERVICE);
            }
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, "", TimeUtils.getTimeOfMonth(), System.currentTimeMillis());
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException");
            return 0;
        }
        //getFlowData();
        return bucket.getTxBytes() + bucket.getRxBytes();
    }

    /**
     * 获取本月到当前的Mobile数据流量
     *
     * @return 手机流量 byte
     */
    public static long getAllMonthMobile(Context context, String subscriberId) {
        NetworkStats.Bucket bucket;
        try {
            if (TextUtils.isEmpty(subscriberId)) {
                if (networkStatsManager == null) {
                    networkStatsManager = (NetworkStatsManager) TheTang.getSingleInstance().getContext().getSystemService(NETWORK_STATS_SERVICE);
                }
                bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE,
                        PhoneUtils.getSubscriberId1(context),
                        TimeUtils.getFirstDayTimeOfMonth(),
                        System.currentTimeMillis());
            } else {
                if (networkStatsManager == null) {
                    networkStatsManager = (NetworkStatsManager) TheTang.getSingleInstance().getContext().getSystemService(NETWORK_STATS_SERVICE);
                }
                bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE,
                        subscriberId,
                        TimeUtils.getFirstDayTimeOfMonth(),
                        System.currentTimeMillis());
            }
        } catch (RemoteException e) {
            return -1;
        }
        return bucket.getRxBytes() + bucket.getTxBytes();
    }

    /**
     * 获取各类流量描述
     *
     * @return 显示String
     */
    public static String getFlowData() {
        String[] imsis = PhoneUtils.getSubscriberId(TheTang.getSingleInstance().getContext());
        StringBuffer buffer = new StringBuffer();
        if (imsis != null && imsis.length > 0) {
            for (int i = 0; i < imsis.length; i++) {
                Log.w(TAG, imsis.length + "imsis[i]==" + imsis[i]);
                if (!TextUtils.isEmpty(imsis[i])) {
                    if (i == 0) {
                        buffer.append(ConvertUtils.convertTraffic(getAllTodayMobile(TheTang.getSingleInstance().getContext(), imsis[i])) + ",");
                        buffer.append(ConvertUtils.convertTraffic(getWorkMobile(TheTang.getSingleInstance().getContext(), imsis[i])) + ",");
                        buffer.append(ConvertUtils.convertTraffic(getAllMonthMobile(TheTang.getSingleInstance().getContext(), imsis[i])) + ",");
                    } else if (i == 1) {
                        if (imsis[i].equals(imsis[i - 1])) {
                            buffer.append(ConvertUtils.convertTraffic(0) + ",");
                            buffer.append(ConvertUtils.convertTraffic(0) + ",");
                            buffer.append(ConvertUtils.convertTraffic(0) + ",");
                        } else {
                            buffer.append(ConvertUtils.convertTraffic(getAllTodayMobile(TheTang.getSingleInstance().getContext(), imsis[i])) + ",");
                            buffer.append(ConvertUtils.convertTraffic(getWorkMobile(TheTang.getSingleInstance().getContext(), imsis[i])) + ",");
                            buffer.append(ConvertUtils.convertTraffic(getAllMonthMobile(TheTang.getSingleInstance().getContext(), imsis[i])) + ",");
                        }
                    }
                }
            }
        }
        if (TextUtils.isEmpty(buffer.toString())) {
            Log.w(TAG, imsis.length + "getFlowData()获取数据流量为空==" + buffer.toString());
            return null;
        } else {
            String substring = buffer.toString().substring(0, buffer.toString().length());
            Log.w(TAG, imsis.length + "-----" + buffer.toString() + "getFlowData()==" + substring);
            return substring;
        }
    }
}

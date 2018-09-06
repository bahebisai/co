package com.xiaomi.emm.utils;

import android.annotation.TargetApi;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.content.Context.NETWORK_STATS_SERVICE;

/**
 * Created by lenovo on 2017/10/9.
 */
@TargetApi(Build.VERSION_CODES.M)
public class NetworkStatsHelper {
    private static final String TAG = "MainActivity";

    private static NetworkStatsManager networkStatsManager = (NetworkStatsManager) TheTang.getSingleInstance().getContext().getSystemService( NETWORK_STATS_SERVICE );
    int packageUid;

    public NetworkStatsHelper(NetworkStatsManager networkStatsManager) {

    }

    public NetworkStatsHelper(NetworkStatsManager networkStatsManager, int packageUid) {
        this.packageUid = packageUid;
    }


    /**
     * 月初到此刻的wifi
     *
     * @return
     */
    public static long getAllMonthWifi() {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice( ConnectivityManager.TYPE_WIFI, "", getTimesMonthmorning(), System.currentTimeMillis() );
        } catch (RemoteException e) {
            return -1;
        }
        return bucket.getRxBytes() + bucket.getRxBytes();
    }

    public long getAllRxBytesWifi() {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice( ConnectivityManager.TYPE_WIFI, "", 0, System.currentTimeMillis() );
        } catch (RemoteException e) {
            return -1;
        }
        return bucket.getRxBytes();
    }

    public long getAllTxBytesWifi() {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice( ConnectivityManager.TYPE_WIFI, "", 0, System.currentTimeMillis() );

        } catch (RemoteException e) {
            return -1;
        }
        return bucket.getTxBytes();
    }

    public long getPackageRxBytesMobile(Context context) {
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetailsForUid(
                    ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId( context, ConnectivityManager.TYPE_MOBILE ),
                    0,
                    System.currentTimeMillis(),
                    packageUid );
        } catch (RemoteException e) {
            return -1;
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket( bucket );
        networkStats.getNextBucket( bucket );
        return bucket.getRxBytes();
    }

    public long getPackageTxBytesMobile(Context context) {
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetailsForUid( ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId( context, ConnectivityManager.TYPE_MOBILE ), 0, System.currentTimeMillis(), packageUid );
        } catch (RemoteException e) {
            return -1;
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket( bucket );
        return bucket.getTxBytes();
    }

    public long getPackageRxBytesWifi() {
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetailsForUid( ConnectivityManager.TYPE_WIFI, "", 0,
                    System.currentTimeMillis(), packageUid );
        } catch (RemoteException e) {
            return -1;
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket( bucket );
        return bucket.getRxBytes();
    }

    public long getPackageTxBytesWifi() {
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetailsForUid( ConnectivityManager.TYPE_WIFI, "", 0, System.currentTimeMillis(),
                    packageUid );
        } catch (RemoteException e) {
            return -1;
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket( bucket );
        return bucket.getTxBytes();
    }

    private static String getSubscriberId(Context context, int networkType) {
        if (ConnectivityManager.TYPE_MOBILE == networkType) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService( Context.TELEPHONY_SERVICE );

            if (tm != null) {
                try {

                    String subscriberId = tm.getSubscriberId();

                    return tm.getSubscriberId();
                } catch (Exception e) {

                    Log.w( TAG, "subscriberId===异常" );
                }
            } else {
                Log.w( TAG, "subscriberId===为空" );
            }
        }
        return "";
    }

    /**
     * 获取当天的零点时间
     *
     * @return
     */
    public static long getTimesmorning() {
        Calendar cal = Calendar.getInstance();
        cal.set( Calendar.HOUR_OF_DAY, 0 );
        cal.set( Calendar.SECOND, 0 );
        cal.set( Calendar.MINUTE, 0 );
        cal.set( Calendar.MILLISECOND, 0 );
        SimpleDateFormat mm = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
        String format1 = mm.format( cal.getTime() );

        return (cal.getTimeInMillis());
        //  return new Date().getTime();
    }

    //获得本月第一天0点时间
    public static long getTimesMonthmorning() {
        Calendar cal = Calendar.getInstance();
        cal.set( cal.get( Calendar.YEAR ), cal.get( Calendar.MONTH ), cal.get( Calendar.DAY_OF_MONTH ), 0, 0, 0 );
        cal.set( Calendar.DAY_OF_MONTH, cal.getActualMinimum( Calendar.DAY_OF_MONTH ) );

        SimpleDateFormat mm = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
        String format1 = mm.format( cal.getTime() );
        long time = 0;
        try {
            time = mm.parse( format1 ).getTime();

        } catch (ParseException e) {

            return time;

        }

        return time;
    }


    /**
     * 获得本周日24点时间
     *
     * @return
     */
    public static int getTimesWeeknight() {

        Calendar cal = Calendar.getInstance();
        cal.set( cal.get( Calendar.YEAR ), cal.get( Calendar.MONTH ), cal.get( Calendar.DAY_OF_MONTH ), 0, 0, 0 );
        cal.set( Calendar.DAY_OF_WEEK, Calendar.MONDAY );
        return (int) ((cal.getTime().getTime() + (7 * 24 * 60 * 60 * 1000)) / 1000);
    }


    /**
     * //获得当天24点时间
     *
     * @return
     */
    public static int getTimesnight() {
        Calendar cal = Calendar.getInstance();
        cal.set( Calendar.HOUR_OF_DAY, 24 );
        cal.set( Calendar.SECOND, 0 );
        cal.set( Calendar.MINUTE, 0 );
        cal.set( Calendar.MILLISECOND, 0 );
        SimpleDateFormat mm = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
        String format1 = mm.format( cal.getTime() );


        return (int) (cal.getTimeInMillis() / 1000);
    }


    /**
     * 获得本周一0点时间
     *
     * @return
     */
    public static long getWorkFirstDay() {

        Calendar cal = Calendar.getInstance();
        cal.set( cal.get( Calendar.YEAR ), cal.get( Calendar.MONTH ), cal.get( Calendar.DAY_OF_MONTH ), 0, 0, 0 );
        cal.set( Calendar.DAY_OF_WEEK, Calendar.MONDAY );
        SimpleDateFormat mm = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
        String format1 = mm.format( cal.getTime() );

        try {

            return mm.parse( format1 ).getTime();
        } catch (ParseException e) {
            return 0;
        }

    }

    /**
     * 获取本周第一天到当前的wifi数据流量
     *
     * @return
     */
    public long getWorkWIFI() {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice( ConnectivityManager.TYPE_WIFI, "", getWorkFirstDay(), System.currentTimeMillis() );

            return bucket.getRxBytes() + bucket.getTxBytes();
        } catch (RemoteException e) {
            return 0;
        }

    }

    /**
     * 获取本周第一天到当前的Mobile数据流量
     *
     * @return
     */
    public static long getWorkMobile(Context context, String subscriberId) {
        NetworkStats.Bucket bucket;
        try {
            if (TextUtils.isEmpty( subscriberId )) {

                bucket = networkStatsManager.querySummaryForDevice( ConnectivityManager.TYPE_MOBILE, getSubscriberId( context, ConnectivityManager.TYPE_MOBILE ),
                        getWorkFirstDay(),
                        System.currentTimeMillis() );
            } else {
                bucket = networkStatsManager.querySummaryForDevice( ConnectivityManager.TYPE_MOBILE, subscriberId,
                        getWorkFirstDay(),
                        System.currentTimeMillis() );
            }
        } catch (RemoteException e) {
            return 0;
        }
        return bucket.getRxBytes() + bucket.getTxBytes();

    }

    /**
     * 获取当天到当前的Mobile数据流量
     *
     * @return
     */
    public static long getAllTodayMobile(Context context, String subscriberId) {
        NetworkStats.Bucket bucket;
        try {
            if (TextUtils.isEmpty( subscriberId )) {
                if (networkStatsManager == null) {
                    networkStatsManager = (NetworkStatsManager) TheTang.getSingleInstance().getContext().getSystemService( NETWORK_STATS_SERVICE );
                }
                bucket = networkStatsManager.querySummaryForDevice( ConnectivityManager.TYPE_MOBILE, getSubscriberId( context, ConnectivityManager.TYPE_MOBILE ),
                        getTimesmorning(), System.currentTimeMillis() );
            } else {
                if (networkStatsManager == null) {
                    networkStatsManager = (NetworkStatsManager) TheTang.getSingleInstance().getContext().getSystemService( NETWORK_STATS_SERVICE );
                }
                bucket = networkStatsManager.querySummaryForDevice( ConnectivityManager.TYPE_MOBILE, subscriberId,
                        getTimesmorning(),
                        System.currentTimeMillis() );
            }
        } catch (RemoteException e) {
            Log.w( TAG, "RemoteException" );
            return 0;
        }
        return bucket.getTxBytes() + bucket.getRxBytes();
    }

    /**
     * 获取当天到当前的wifi数据流量
     *
     * @return
     */
    public long getAllTodayWifi() {
        NetworkStats.Bucket bucket;
        try {
            if (networkStatsManager == null) {
                networkStatsManager = (NetworkStatsManager) TheTang.getSingleInstance().getContext().getSystemService( NETWORK_STATS_SERVICE );
            }
            bucket = networkStatsManager.querySummaryForDevice( ConnectivityManager.TYPE_WIFI, "", getTimesmorning(), System.currentTimeMillis() );


        } catch (RemoteException e) {
            Log.w( TAG, "RemoteException" );
            return 0;
        }
        //getFlowData();
        return bucket.getTxBytes() + bucket.getRxBytes();
    }


    /**
     * 获取当yue到当前的Mobile数据流量
     *
     * @return
     */
    public static long getAllMonthMobile(Context context, String subscriberId) {
        NetworkStats.Bucket bucket;
        try {
            if (TextUtils.isEmpty( subscriberId )) {
                if (networkStatsManager == null) {
                    networkStatsManager = (NetworkStatsManager) TheTang.getSingleInstance().getContext().getSystemService( NETWORK_STATS_SERVICE );
                }
                bucket = networkStatsManager.querySummaryForDevice( ConnectivityManager.TYPE_MOBILE,
                        getSubscriberId( context, ConnectivityManager.TYPE_MOBILE ),
                        getTimesMonthmorning(),
                        System.currentTimeMillis() );
            } else {
                if (networkStatsManager == null) {
                    networkStatsManager = (NetworkStatsManager) TheTang.getSingleInstance().getContext().getSystemService( NETWORK_STATS_SERVICE );
                }
                bucket = networkStatsManager.querySummaryForDevice( ConnectivityManager.TYPE_MOBILE,
                        subscriberId,
                        getTimesMonthmorning(),
                        System.currentTimeMillis() );
            }

        } catch (RemoteException e) {
            return -1;
        }
        return bucket.getRxBytes() + bucket.getTxBytes();
    }

    public static String getFlowData() {
        String[] imsis = TheTang.getSingleInstance().getSubscriberId();
        StringBuffer buffer = new StringBuffer();
        if (imsis != null && imsis.length > 0) {
            for (int i = 0; i < imsis.length; i++) {
                Log.w( TAG, imsis.length+"imsis[i]==" + imsis[i] );
                if (!TextUtils.isEmpty( imsis[i] )) {

                    if (i == 0){
                        buffer.append( TheTang.getSingleInstance().convertTraffic( getAllTodayMobile( TheTang.getSingleInstance().getContext(), imsis[i] ) ) + "," );
                        buffer.append( TheTang.getSingleInstance().convertTraffic( getWorkMobile( TheTang.getSingleInstance().getContext(), imsis[i] ) ) + "," );
                        buffer.append( TheTang.getSingleInstance().convertTraffic( getAllMonthMobile( TheTang.getSingleInstance().getContext(), imsis[i] ) ) + "," );

                    }else if (i == 1) {
                        if (imsis[i].equals(imsis[i -1 ])){

                            buffer.append( TheTang.getSingleInstance().convertTraffic( 0) + "," );
                            buffer.append( TheTang.getSingleInstance().convertTraffic( 0 ) + "," );
                            buffer.append( TheTang.getSingleInstance().convertTraffic( 0) + "," );
                        }else {
                            buffer.append( TheTang.getSingleInstance().convertTraffic( getAllTodayMobile( TheTang.getSingleInstance().getContext(), imsis[i] ) ) + "," );
                            buffer.append( TheTang.getSingleInstance().convertTraffic( getWorkMobile( TheTang.getSingleInstance().getContext(), imsis[i] ) ) + "," );
                            buffer.append( TheTang.getSingleInstance().convertTraffic( getAllMonthMobile( TheTang.getSingleInstance().getContext(), imsis[i] ) ) + "," );
                        }

                    }

                }
            }
        }
        if (TextUtils.isEmpty( buffer.toString() )) {
            Log.w( TAG, imsis.length+"getFlowData()获取数据流量为空==" + buffer.toString() );
            return null;
        } else {

            String substring = buffer.toString().substring( 0, buffer.toString().length() );
            Log.w( TAG, imsis.length+"-----"+buffer.toString() + "getFlowData()==" + substring );
            return substring;
        }
    }
}

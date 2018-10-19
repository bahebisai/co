package com.xiaomi.emm.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class DeviceUtils {
    private static final String TAG = DeviceUtils.class.getName();

    /**
     * 获得剩余存储
     *
     * @return
     */
    public static float getRemainStorage() {
        //外部存储大小
        return getAvailSpace(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    /**
     * 获得剩余存储
     *
     * @param path 根路径
     * @return
     */
    public static long getAvailSpace(String path) {
        StatFs statfs = null;
        try {
            statfs = new StatFs(path);
        } catch (Exception e) {
            return 0;
        }
        long size = statfs.getBlockSize();//获取分区的大小
        long count = statfs.getAvailableBlocks();//获取可用分区块的个数
        return size * count;
    }

    /**
     * 获得总存储
     *
     * @return
     */
    public static float getTotalStorage() {//todo baii util device or phone
        //外部存储大小
        return getTotalSpace(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    /**
     * 获得总存储
     *
     * @param path 根路径
     * @return
     */
    public static long getTotalSpace(String path) {//todo baii util device or phone
        StatFs statfs = null;
        try {
            statfs = new StatFs(path);
        } catch (Exception e) {
            return 0;
        }

        long size = statfs.getBlockSize();//获取分区的大小
        long count = statfs.getBlockCount();//获取分区块的个数
        return size * count;
    }

    /**
     * 获得SD卡cid
     *
     * @return
     */
    public static String getSdcardCid() {//todo baii util device
        Object localOb = null; // SD Card ID
        String sd_cid = null;
        try {
            localOb = new FileReader("/sys/block/mmcblk1/device/" + "cid");
            sd_cid = new BufferedReader((Reader) localOb).readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sd_cid;
    }

    public static String getImei(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            String imei = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                imei = telephonyManager.getImei(0);
            }
            LogUtil.writeToFile(TAG, "getImei1 = " + imei);
            return imei;
        } else {
            LogUtil.writeToFile(TAG, "getImei without permission!!!");
            return "";
        }
    }

    public static String getImei1(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            String imei = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                imei = telephonyManager.getImei(1);
            }
            LogUtil.writeToFile(TAG, "getImei2 = " + imei);
            return imei;
        } else {
            LogUtil.writeToFile(TAG, "getImei without permission!!!");
            return "";
        }
    }

    //获得Ram
    public static String getTotalRam() {//GB
        String path = "/proc/meminfo";
        String firstLine = null;
        int totalRam = 0;
        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader, 8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (firstLine != null) {
            totalRam = (int) Math.ceil((new Float(Float.valueOf(firstLine) / (1024 * 1024)).doubleValue()));
        }
        return totalRam + "GB";//返回1GB/2GB/3GB/4GB
    }

    //获得手机分辨率
    public static String getResolution(Context context) {//todo baii util device or view
        DisplayMetrics mDisplayMetrics = context.getResources().getDisplayMetrics();
        String resolution = mDisplayMetrics.widthPixels + "*" + mDisplayMetrics.heightPixels;
        return resolution;
    }

    //获得厂商
    public static String getManufacturers() {//todo baii util device
        return Build.BRAND;
    }

    //获得设备型号
    public static String getModel() {//todo baii util device
        return Build.MODEL;
    }

    //获得手机版本号
    public static String getAndroidVersion() {//todo baii util device
        return Build.VERSION.RELEASE;
    }

    //获得系统版本
    public static String getSystemVersion() {
        //  return Build.DISPLAY;
        return Build.VERSION.RELEASE;
    }
}

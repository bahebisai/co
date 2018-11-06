package com.xiaomi.emm.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import com.xiaomi.emm.features.manager.PreferencesManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 设备信息相关的工具类，如型号、内存等
 */
public class DeviceUtils {
    private static final String TAG = DeviceUtils.class.getName();

    /**
     * 获得总存储
     *
     * @return
     */
    public static float getTotalStorage() {
        //外部存储大小
        return getTotalSpace(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

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
     * 获得sd卡剩余容量
     *
     * @return
     */
    public static String getSdCardAvailableCapacity() {
        long storage = DeviceUtils.getAvailSpace("/mnt/ext_sdcard");
        if (storage == 0) {
            return "";
        } else {
            return ConvertUtils.formatFileSize(storage);
        }
    }

    /**
     * 获得sd卡总容量
     *
     * @return
     */
    public static String getSdCardTotalCapacity() {
        long storage = DeviceUtils.getTotalSpace("/mnt/ext_sdcard");
        if (storage == 0) {
            return "";
        } else {
            return ConvertUtils.formatFileSize(storage);
        }
    }

    /**
     * 获得总存储
     *
     * @param path 根路径
     * @return
     */
    public static long getTotalSpace(String path) {
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
     * 获得设备型号
     *
     * @return
     */
    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * 获得设备名称
     *
     * @return
     */
    public static String getDeviceName() {
        return Build.DEVICE;
    }

    /**
     * 获取厂商
     *
     * @return brand
     */
    public static String getManufacturers() {
        return Build.BRAND;
    }

    /**
     * 获得安卓版本
     *
     * @return eg.8.1.0
     */
    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE;//8.1.0
    }

    /**
     * 获得软件版本号
     *
     * @return eg.ZTE V890AMV1.0.0B02
     */
    public static String getSystemVersion() {
        return Build.DISPLAY;
//        return Build.VERSION.RELEASE;
    }

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 获得cpu型号
     *
     * @return
     */
    public static String getCpu() {
        return android.os.Build.CPU_ABI;
    }

    /**
     * 获得开机时长
     *
     * @return
     */
    public static String getBootTime() {
        return ConvertUtils.formatTimeLength(SystemClock.elapsedRealtime());
    }

    /**
     * 获得唯一标识符
     *
     * @return
     */
    public static String getUUIDinDB() {
        String uuid = PreferencesManager.getSingleInstance().getData("udid");
        if (uuid == null) {
            uuid = DeviceUtils.getUUID();
            PreferencesManager.getSingleInstance().setOtherData("udid", uuid);
        }
        return uuid;
    }

    /**
     * 获取相机像素
     *
     * @param cameraId camera id，前置和后置，一般0,1
     * @return
     */
    public static String getCameraPixelInDB(int cameraId) {
//        String cameraPixel = PreferencesManager.getSingleInstance().getData("camera");
        String cameraPixel = null;
        if (cameraPixel == null) {
            cameraPixel = DeviceUtils.getCameraPixels(cameraId);
            PreferencesManager.getSingleInstance().setOtherData("camera", cameraPixel);
        }
        return cameraPixel;
    }

    /**
     * 相机像素
     *
     * @param paramInt camera id, eg 0
     * @return
     */
    public static String getCameraPixels(int paramInt) {
        Camera localCamera = null;
        try {
            localCamera = Camera.open(paramInt); //在相机disable的情况下 会抛出异常
        } catch (Exception e) {
            return null;
        }
        Camera.Parameters localParameters = localCamera.getParameters();
        localParameters.set("camera-id", 1);
        List<Camera.Size> localList = localParameters.getSupportedPictureSizes();
        if (localList != null) {
            int heights[] = new int[localList.size()];
            int widths[] = new int[localList.size()];
            int[] mutiple = new int[localList.size()];
            for (int i = 0; i < localList.size(); i++) {
                Camera.Size size = (Camera.Size) localList.get(i);
                int sizehieght = size.height;
                int sizewidth = size.width;
                heights[i] = sizehieght;
                widths[i] = sizewidth;
                mutiple[i] = sizehieght * sizewidth;
            }
            int pixels = getMaxNumber(heights) * getMaxNumber(widths);
            localCamera.release();
            return String.valueOf(pixels / 10000) + " 万";
        }
        return null;
    }

    private static int getMaxNumber(int[] paramArray) {//todo baii util
        int temp = paramArray[0];
        for (int i = 0; i < paramArray.length; i++) {
            if (temp < paramArray[i]) {
                temp = paramArray[i];
            }
        }
        return temp;
    }

    /**
     * 获得SD卡cid
     *
     * @return
     */
    public static String getSdcardCid() {
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

    /**
     * @param context
     * @param slotId  卡槽id, 0, 1
     * @return
     */
    public static String getImei(Context context, int slotId) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            String imei = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                imei = telephonyManager.getImei(slotId);
            }
            LogUtil.writeToFile(TAG, "getImei " + slotId + " = " + imei);
            return imei;
        } else {
            LogUtil.writeToFile(TAG, "getImei without permission!!!");
            return "";
        }
    }

    /**
     * 获取RAM大小
     *
     * @return *GB
     */
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

    /**
     * 获取手机分辨率
     *
     * @param context
     * @return
     */
    public static String getResolution(Context context) {
        DisplayMetrics mDisplayMetrics = context.getResources().getDisplayMetrics();
        String resolution = mDisplayMetrics.widthPixels + "*" + mDisplayMetrics.heightPixels;
        return resolution;
    }

    /**
     * 获取CPU核心数
     *
     * @return
     */
    public static int getNumCores() {
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                // Check if filename is "cpu", followed by a single digit number
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }
        try {
            // Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            // Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            // Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            e.printStackTrace();
            // Default to return 1 core
            return 1;
        }
    }

    /**
     * 获取设备是否root
     *
     * @return true 已root, false 没root
     */
    public static boolean isDeviceRooted() {
        if (checkRootMethod1() || checkRootMethod2() || checkRootMethod3()) {
            return true;
        }
        return false;
    }

    /**
     * 获取外置SD卡UUID，不通过反射，效果同{@link DeviceUtils#getSDCardId(Context)}
     *
     * @param context
     * @return
     */
    public static String getSDCardUUID(Context context) {
        String uuid = "";
        StorageManager manager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            List<StorageVolume> volumes = manager.getStorageVolumes();
            for (StorageVolume volume : volumes) {
                if (volume.isRemovable()) {
                    uuid = volume.getUuid();//eg.0403-0201
                    break;
                }
            }
        }
        return uuid;
    }

    /**
     * 通过反射获取外置SD卡UUID
     *
     * @param context
     * @return
     */
    public static String getSDCardId(Context context) {
        String sdCardId = null;
        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        List<StorageVolume> mList = mStorageManager.getStorageVolumes();
        for (StorageVolume mStorageVolume : mList) {
            try {
                Method getPath = mStorageVolume.getClass().getDeclaredMethod("getPath");
                Method isRemovable = mStorageVolume.getClass().getDeclaredMethod("isRemovable");
                getPath.setAccessible(true);
                isRemovable.setAccessible(true);
                String path = (String) getPath.invoke(mStorageVolume);
                boolean removable = (boolean) isRemovable.invoke(mStorageVolume);
                if (removable) {
                    String[] paths = path.split("/");
                    sdCardId = paths[paths.length - 1];
                    break;
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return sdCardId;
    }

    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true;
        }
        return false;
    }

    private static boolean checkRootMethod2() {
        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    private static boolean checkRootMethod3() {
        if (new ExecShell().executeCommand(ExecShell.SHELL_CMD.check_su_binary) != null) {
            return true;
        } else {
            return false;
        }
    }
}

package com.xiaomi.emm.utils;

import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.RemoteException;
import android.os.storage.StorageManager;
import android.util.ArraySet;
import android.util.Log;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.model.APPInfo;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AppUtils {
    public static final String TAG = AppUtils.class.getName();

    public static void getInstalledApps(Context context) {
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
        for (PackageInfo info : packages) {
            Log.d("baii", "app name  " + info.applicationInfo.name);
        }
    }

    public static List<APPInfo> getAllAppsOnLauncher(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(resolveIntent, 0);//获取所有显示在桌面的activity
        Set<String> launcherPkgs = new ArraySet<>();
        for (ResolveInfo info : resolveInfoList) {
            launcherPkgs.add(info.activityInfo.packageName);//获取所有桌面的包名
        }
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        List<APPInfo> appInfos = new ArrayList<>();
        for (PackageInfo info : packages) {
            if (launcherPkgs.contains(info.packageName)) {
                Log.d("baii", "app name  " + info.applicationInfo.loadLabel(pm));
                APPInfo appInfo = new APPInfo();
                appInfo.setAppName(info.applicationInfo.loadLabel(pm).toString());
                appInfo.setPackageName(info.packageName);
                appInfo.setSize(getAppSize(context, info.packageName));
                appInfo.setVersion(info.versionName);
                if (isSystemApp(info.applicationInfo.flags)) {
                    appInfo.setType(APPInfo.TYPE_SYSTEM);
                } else if (isPushApp(info.packageName)) {
                    appInfo.setType(APPInfo.TYPE_PUSH);
                } else {
                    appInfo.setType(APPInfo.TYPE_USER);
                }
                appInfo.setAppId(getPushAppInfo(info.packageName).getAppId());

                appInfos.add(appInfo);
            }
        }
        return appInfos;
    }

    private static boolean isSystemApp(int flag) {
        if ((flag & ApplicationInfo.FLAG_SYSTEM) != 0) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isPushApp(String pkgName) {
        if (DatabaseOperate.getSingleInstance().queryAppInfo(pkgName) != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取服务器下发应用的信息
     * @param pkgName
     * @return
     */
    public static APPInfo getPushAppInfo(String pkgName) {
        return DatabaseOperate.getSingleInstance().queryAppInfo(pkgName);
    }

    /**
     * 获取应用大小：包括code, data, cache
     */
    static String appSize;

    public static String getAppSize(Context context, String pkgName) {//todo baii util app
        if (pkgName != null) {
            if (Build.VERSION.SDK_INT < 26) {
                PackageManager mPackageManager = context.getPackageManager();  //得到pm对象
                try {
                    Method getPackageSizeInfo = mPackageManager.getClass().getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                    getPackageSizeInfo.invoke(mPackageManager, pkgName, new IPackageStatsObserver.Stub() {
                        @Override
                        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException { //异步执行
                            if (succeeded && pStats != null) {
                                appSize = TheTang.getSingleInstance().formatFileSize(pStats.codeSize
                                        + pStats.dataSize + pStats.cacheSize);
                            }
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                StorageStatsManager mStorageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
                UUID uuid = StorageManager.UUID_DEFAULT;
                StorageStats mStorageStats = null;
                try {
                    mStorageStats = mStorageStatsManager.queryStatsForUid(uuid, getAppUid(context, pkgName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                appSize = TheTang.getSingleInstance().formatFileSize(
                        mStorageStats.getAppBytes() + mStorageStats.getCacheBytes() + mStorageStats.getDataBytes());
            }
        }
        if (appSize == null) {
            appSize = "0B";
        }
        return appSize;
    }

    /**
     * 获取应用UID
     *
     * @param packageName
     * @return
     */
    public static int getAppUid(Context context, String packageName) {
        PackageManager packageManager = getPackageManager(context);
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int uid = packageInfo.applicationInfo.uid;
        return uid;
    }

    /**
     * 获得已安装的非系统APP个数
     *
     * @return
     */
    public static List<PackageInfo> getNoSystemApp(Context context) {//todo baii util app
        List<PackageInfo> packageList = getPackageManager(context).getInstalledPackages(0);
//        Log.d("baii", "before size " + packageList.size());
        if (packageList != null && packageList.size() > 0) {
            //note bai: for 循环不能边删除边遍历
            //modify:1.remove之后i不能增加;2.遍历时记录所有需要删除的，遍历完成之后统一删除
            for (int i = 0; i < packageList.size(); i++) {
                PackageInfo packageInfo = packageList.get(i);
                if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    packageList.remove(i);
                    i--;//bai for note
                    continue;
                }
                if (packageInfo.packageName.equals(Common.packageName)) {
                    packageList.remove(i);
                    i--;
                    continue;
                }
            }
        }
        for (PackageInfo info:packageList) {
            Log.d("baii", info.packageName);
        }
        return packageList;
    }

    /**
     * 获得包管理器
     *
     * @returnAppTask
     */
    public static PackageManager getPackageManager(Context context) {//todo baii util app
        PackageManager packageManager = null;
        try {
            packageManager = context.getPackageManager();
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, e.getCause().toString());
        }
        return packageManager;
    }
}

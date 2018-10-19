package com.xiaomi.emm.utils;

import android.app.ActivityManager;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
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
     *
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

    public static String getAppSize(Context context, String pkgName) {
        if (pkgName != null) {
            appSize = "";
            if (Build.VERSION.SDK_INT < 26) {
                PackageManager mPackageManager = context.getPackageManager();  //得到pm对象
                try {
                    Method getPackageSizeInfo = mPackageManager.getClass().getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                    getPackageSizeInfo.invoke(mPackageManager, pkgName, new IPackageStatsObserver.Stub() {
                        @Override
                        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException { //异步执行
                            if (succeeded && pStats != null) {
                                appSize = ConvertUtils.formatFileSize(pStats.codeSize
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
                appSize = ConvertUtils.formatFileSize(
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
        PackageManager packageManager = context.getPackageManager();
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
    public static List<PackageInfo> getNoSystemApp(Context context) {
        List<PackageInfo> packageList = context.getPackageManager().getInstalledPackages(0);
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
        return packageList;
    }

    /**
     * 获得已安装的应用的图标
     *
     * @param info
     * @return
     */
    public static Drawable getAppIcon(Context context, ApplicationInfo info) {
        PackageManager packageManager = context.getPackageManager();
        Drawable drawable = info.loadIcon(packageManager);
        return drawable;
    }

    /**
     * 通过包名获得应用的图标
     *
     * @param packageName
     * @return
     */
    public static Drawable getAppIcon(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo info = null;
        try {
            info = packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (info == null) {
            return null;
        }
        return info.loadIcon(packageManager);
    }

    /**
     * 获得已安装的应用的名称
     *
     * @param packageName
     * @return
     */
    public static String getAppLabel(Context context, String packageName) {//todo baii util app
        String label = null;
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            label = (String) packageManager.getApplicationLabel(packageInfo.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return label;
    }

    /**
     * 获取Launcher Apps
     *
     * @return
     */
    public static List getLauncherApps() {//todo baii util app
        LauncherApps apps = (LauncherApps) TheTang.getSingleInstance().getContext().getSystemService(Context.LAUNCHER_APPS_SERVICE);
        UserManager mUserManager = (UserManager) TheTang.getSingleInstance().getContext().getSystemService(Context.USER_SERVICE);
        final List<UserHandle> profiles = mUserManager.getUserProfiles();
        //单用户情况下
        UserHandle user = profiles.get(0);
        return apps.getActivityList(null, user);
    }

    /**
     * 获得Launcher非系统app
     *
     * @return
     */
    public static List getLauncherNoSystemApp() {//todo baii util app
        List<LauncherActivityInfo> appList = getLauncherApps();
        if (appList == null) {
            return null;
        }
        List<LauncherActivityInfo> apps = new ArrayList<>();
        for (LauncherActivityInfo launcherActivityInfo : appList) {
            if ((launcherActivityInfo.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                apps.add(launcherActivityInfo);
            }
        }
        appList.removeAll(apps);
        return appList;
    }

    /**
     * 方法描述：判断某一应用是否正在运行
     *
     * @param packageName 应用的包名
     * @return true 表示正在运行，false表示没有运行
     */
    public boolean isAppRunning(Context context, String packageName) {//todo baii util app
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        if (list.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.baseActivity.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 方法描述：判断某一Service是否正在运行
     *
     * @param serviceName Service的全路径： 包名 + service的类名
     * @return true 表示正在运行，false 表示没有运行
     */
    public boolean isServiceRunning(Context context, String serviceName) {//todo baii util ??? or app
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(200);
        if (runningServiceInfos.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo serviceInfo : runningServiceInfos) {
            if (serviceInfo.service.getClassName().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取应用版本号
     *
     * @param packageName
     * @return
     */
    public static String getAppVersion(Context context, String packageName) {//todo baii util app
        String version = null;

        try {
            version = context.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }
}

package com.zoomtech.emm.utils;

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

import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.features.db.DatabaseOperate;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.model.APPInfo;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AppUtils {
    public static final String TAG = AppUtils.class.getName();

    /**
     * 系统应用
     */
    public static final int TYPE_SYSTEM = 0;

    /**
     * 用户安装应用
     */
    public static final int TYPE_USER = 1;

    /**
     * 商店应用，即后台下发
     */
    public static final int TYPE_PUSH = 2;

    /**
     * 获取所有桌面应用信息
     *
     * @param context
     * @return AppInfo
     */
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
                appInfo.setType(getAppType(info));
                appInfo.setAppId(getPushAppInfo(info.packageName).getAppId());
                appInfos.add(appInfo);
            }
        }
        return appInfos;
    }

    /**
     * 查询数据库获取服务器下发应用的信息
     *
     * @param pkgName 要查询应用的包名
     * @return AppInfo
     */
    public static APPInfo getPushAppInfo(String pkgName) {
        return DatabaseOperate.getSingleInstance().queryAppInfo(pkgName);
    }

    /**
     * 获取应用大小：包括code, data, cache
     */
    private static String appSize;

    /**
     * 获取应用大小(占用内存)：包括code, data, cache
     *
     * @param context
     * @param pkgName 要查询应用的包名
     * @return app大小，带单位
     */
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
     * @param packageName 应用包名
     * @return uid
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
     * 获得已安装的非系统APP信息
     *
     * @return List<PackageInfo>
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
     * 获得应用的图标
     *
     * @param info 应用的ApplicationInfo
     * @return Drawable图标
     */
    public static Drawable getAppIcon(Context context, ApplicationInfo info) {
        PackageManager packageManager = context.getPackageManager();
        Drawable drawable = info.loadIcon(packageManager);
        return drawable;
    }

    /**
     * 通过包名获得应用的图标
     *
     * @param packageName 应用包名
     * @return Drawable图标
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
     * 获得应用名称
     *
     * @param packageName 应用包名
     * @return 应用名字app label
     */
    public static String getAppLabel(Context context, String packageName) {
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
     * 获取桌面应用信息，返回List可能有重复项
     *
     * @return List<LauncherActivityInfo>
     */
    public static List<LauncherActivityInfo> getLauncherApps() {
        LauncherApps apps = (LauncherApps) TheTang.getSingleInstance().getContext().getSystemService(Context.LAUNCHER_APPS_SERVICE);
        UserManager mUserManager = (UserManager) TheTang.getSingleInstance().getContext().getSystemService(Context.USER_SERVICE);
        final List<UserHandle> profiles = mUserManager.getUserProfiles();
        //单用户情况下
        UserHandle user = profiles.get(0);
        return apps.getActivityList(null, user);
    }

    /**
     * 获取桌面应用包名，无重复项
     *
     * @return HashSet<String>
     */
    public static HashSet<String> getUniqueLauncherApps() {
        HashSet<String> apps = new HashSet<>();
        List<LauncherActivityInfo> infos = getLauncherApps();
        for (LauncherActivityInfo info : infos) {
            apps.add(info.getApplicationInfo().packageName);
        }
        return apps;
    }

    /**
     * 获取桌面非系统应用信息
     *
     * @return List<LauncherActivityInfo>
     */
    public static List<LauncherActivityInfo> getLauncherNoSystemApp() {
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
     * 判断应用是否正在运行
     *
     * @param packageName 应用包名
     * @return true 表示正在运行，false表示没有运行
     */
    public boolean isAppRunning(Context context, String packageName) {
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
     * 判断Service是否正在运行
     *
     * @param serviceName Service的全路径： 包名 + service的类名
     * @return true 表示正在运行，false 表示没有运行
     */
    public boolean isServiceRunning(Context context, String serviceName) {
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
     * @return 应用版本号
     */
    public static int getAppVersionCode(Context context, String packageName) {
        int version = -1;
        try {
            version = context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 获取应用版本名
     *
     * @param packageName
     * @return 应用版本名
     */
    public static String getAppVersionName(Context context, String packageName) {
        String versionName = "";
        try {
            versionName = context.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 判断应用是否在运行（或存活）
     * 如果需要判断本应用是否在后台还是前台用getRunningTask
     *
     * @param mContext
     * @param packageName 应用包名
     * @return true 在运行；false 没有运行
     */
    public static boolean isAPPALive(Context mContext, String packageName) {
        boolean isAPPRunning = false;
        // 获取activity管理对象
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        // 获取所有正在运行的app
        List<ActivityManager.RunningAppProcessInfo> appProcessInfoList = activityManager.getRunningAppProcesses();
        // 遍历，进程名即包名
        for (ActivityManager.RunningAppProcessInfo appInfo : appProcessInfoList) {
            if (packageName.equals(appInfo.processName)) {
                isAPPRunning = true;
                break;
            }
        }
        return isAPPRunning;
    }

    /**
     * 从数据库中获取appId, 若是后台下发的app，则有appId，otherwise,没有appId，返回""
     *
     * @return 数据库id如果存在，否则返回""
     */
    public static String getAppId(String packageName) {
        APPInfo appInfo = DatabaseOperate.getSingleInstance().queryAppInfo(packageName);
        if (appInfo != null) {
            return appInfo.getAppId();
        } else {
            return "";
        }
    }

    /**
     * * 获取应用的类型，关联数据库
     *
     * @param packageInfo
     * @return 应用类型，系统、后台下发或其他
     */
    public static int getAppType(PackageInfo packageInfo) {
        if (Common.packageName.equals(packageInfo.packageName) || (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
            return TYPE_SYSTEM;
        } else {
            if (DatabaseOperate.getSingleInstance().queryAppInfo(packageInfo.packageName) != null) {
                return TYPE_USER;
            } else {
                return TYPE_PUSH;
            }
        }
    }
}

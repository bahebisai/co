package com.xiaomi.emm.features.silent;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.xiaomi.emm.base.BaseApplication;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.OrderConfig;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.event.APKEvent;
import com.xiaomi.emm.features.event.CompleteEvent;
import com.xiaomi.emm.model.APPInfo;
import com.xiaomi.emm.model.DownLoadEntity;
import com.xiaomi.emm.utils.AppUtils;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.MDM;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duan on 17/6/18.
 */

public class AppTask {

    private static final String TAG = "AppTask";
    PackageManager packageManager = null;
    DownLoadEntity downLoadEntity = null;

    public AppTask() {
        packageManager = AppUtils.getPackageManager(TheTang.getSingleInstance().getContext());
    }

    /**
     * 判断是安装还是卸载
     * EventBus
     *
     * @param event
     */
    public void onSilentExcutor(final APKEvent event) {
        downLoadEntity = event.getDownLoadEntity();
        if (event.getCode() == OrderConfig.SilentInstallAppication) {
            whetherHadBlackWhiteList(downLoadEntity);
            onSilentInstall(downLoadEntity);
        } else if (event.getCode() == OrderConfig.SilentUninstallAppication) {
            onSilentUninstall(downLoadEntity);
        }
    }

    /**
     * 添加黑白名单判断,将下发应用添加到白名单
     *
     * @param downLoadEntity
     */
    private void whetherHadBlackWhiteList(DownLoadEntity downLoadEntity) {
        String type = PreferencesManager.getSingleInstance().getOtherData(Common.appManagerType);

        if (type == null) {
            return;
        }

        PackageManager mPackageManager = TheTang.getSingleInstance().getContext().getPackageManager();
        PackageInfo mPackageInfo = mPackageManager.getPackageArchiveInfo(BaseApplication.baseAppsPath + downLoadEntity.saveName, PackageManager.GET_ACTIVITIES);
        if (mPackageInfo != null) {
            String packageName = mPackageInfo.packageName;

            List<String> appList = DatabaseOperate.getSingleInstance().queryAllApp();

            switch (Integer.parseInt(type)) {
                //白名单
                case 1:
                    if (!appList.contains(packageName)) {
                        List<String> whiteList = new ArrayList<>();
                        whiteList.add(packageName);
                        DatabaseOperate.getSingleInstance().addAppWhiteList(whiteList);
                    }
                    break;
                //应用合规
                case 2:

                    if ("1".equals(PreferencesManager.getSingleInstance().getComplianceData(Common.appType))) {
                        if (!appList.contains(packageName)) {
                            List<String> whiteList = new ArrayList<>();
                            whiteList.add(packageName);
                            DatabaseOperate.getSingleInstance().addAppWhiteList(whiteList);
                        }
                    }

                    break;
                //安全桌面
                case 3:
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * 静默安装
     *
     * @param downLoadEntity
     */
    private void onSilentInstall(final DownLoadEntity downLoadEntity) {
        /*if (Common.packageName.equals(downLoadEntity.packageName)) {
            Uri apkUri = FileProvider.getUriForFile(TheTang.getSingleInstance().getContext(), Common.packageName + ".fileprovider",
                    new File(BaseApplication.baseAppsPath + File.separator + downLoadEntity.saveName));
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            TheTang.getSingleInstance().getContext().startActivity(install);
        } else {*/
        installAPK(downLoadEntity.saveName);

        //用于设备更新时，关闭应用
        if (Common.packageName.equals(downLoadEntity.packageName)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                        MDM.killProcess(Common.packageName);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            //}
        }
    }

    /**
     * 静默卸载
     *
     * @param downLoadEntity
     */
    public void onSilentUninstall(DownLoadEntity downLoadEntity) {
        String code = String.valueOf(OrderConfig.SilentUninstallAppication);
        APPInfo app = DatabaseOperate.getSingleInstance().queryAppInfo(downLoadEntity.app_id);

        if (app == null) {
            EventBus.getDefault().post(new CompleteEvent(code, "true", downLoadEntity.sendId));
            return;
        }

        TheTang.getSingleInstance().addMessage(code, app.getAppName());
        unInstallAPK(code, app.getPackageName());

        EventBus.getDefault().post(new CompleteEvent(code, "true", downLoadEntity.sendId));
    }

    /**
     * 安装APK
     *
     * @param appName
     */
    private void installAPK(final String appName) {
        LogUtil.writeToFile(TAG, "silentInstall = " + BaseApplication.baseAppsPath + File.separator + appName);
        Log.w("DownLoadRequest", "silentInstall = " + BaseApplication.baseAppsPath + File.separator + appName);
        MDM.silentInstall(BaseApplication.baseAppsPath + File.separator + appName);
    }

    /**
     * 卸载APK
     *
     * @param packageName
     */
    private void unInstallAPK(String code, String packageName) {
        if (packageName != null) {
            MDM.deleteAppFromUninstallList(packageName);
            //用于判断应用是否安装成功
            try {
                packageManager.getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                LogUtil.writeToFile(TAG, e.toString());
            }
            MDM.silentUninstall(packageName);
        } else {
        }
    }
}

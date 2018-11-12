package com.zoomtech.emm.features.policy.app;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zoomtech.emm.R;
import com.zoomtech.emm.base.BaseApplication;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.definition.OrderConfig;
import com.zoomtech.emm.features.db.DatabaseOperate;
import com.zoomtech.emm.features.event.CompleteEvent;
import com.zoomtech.emm.features.event.NotifyEvent;
import com.zoomtech.emm.features.event.NotifySafedesk;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.model.APPInfo;
import com.zoomtech.emm.model.CompleteMessageData;
import com.zoomtech.emm.model.DownLoadEntity;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.view.activity.SafeDeskActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/28.
 */

public class AppIntentService extends IntentService {

    final static String TAG = "AppIntentService";

    PackageManager packageManager = null;
    List<APPInfo> installAPPInfoLists = new ArrayList<>();
    String action = null;

    //用于黑白名单删除应用
    static Handler appHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };

    public AppIntentService() {
        super("AppIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //获得包管理器
        packageManager = getPackageManager();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

        action = intent.getStringExtra("action");

        if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
            TheTang.getSingleInstance().startForeground(this, getResources().getString(R.string.app_install_package, intent.getStringExtra("packageName"))
                    , "EMM", 13);
        } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            TheTang.getSingleInstance().startForeground(this, getResources().getString(R.string.app_uninstall_package, intent.getStringExtra("packageName"))
                    , "EMM", 13);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //安装应用
        addApp(intent);

        //黑白名单与合规处理
        blackWhiteListAndCompliance(intent);

        //后台推送应用删除
        appDeleteByBackground(intent);

        //通知前台更新数据
        EventBus.getDefault().post(new NotifyEvent());

        if (BaseApplication.getNewsLifecycleHandler().isSameClassName(SafeDeskActivity.class.getSimpleName())) {
            EventBus.getDefault().post(new NotifySafedesk(Common.safeActicivty_flush));
        }
    }

    /**
     * 安装应用
     *
     * @param intent
     */
    private void addApp(Intent intent) {

        //String action = intent.getStringExtra( "action" );

        //对新安装的应用进行应用合规处理
        if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {

            String packageName = intent.getStringExtra("packageName");
            LogUtil.writeToFile(TAG, "addApp = " + packageName);
            DownLoadEntity mDownLoadEntity = DatabaseOperate.getSingleInstance().queryDownLoadFileByPackageName(packageName);

            if (mDownLoadEntity != null) {

                installApp(mDownLoadEntity);

                //添加应用上网
                if ("0".equals(mDownLoadEntity.internet)) {
                    MDM.getSingleInstance().forbiddenAppNetwork(packageName);
                }

                //添加到防卸载白名单
                if ("0".equals(mDownLoadEntity.uninstall)) {
                    MDM.getSingleInstance().addAppTONoUninstallList(packageName);
                }

                //安装完成后，删除应用安装包
                MDM.getSingleInstance().deleteFile(new File(BaseApplication.baseAppsPath + File.separator + mDownLoadEntity.saveName));

                DatabaseOperate.getSingleInstance().deleteDownLoadFile(mDownLoadEntity);

                CompleteMessageData mCompleteMessageData = DatabaseOperate.getSingleInstance()
                        .queryCompleteResultSql(String.valueOf(OrderConfig.SilentInstallAppication), mDownLoadEntity.sendId);

                if (mCompleteMessageData == null)
                    return;
                Log.w(TAG, "sendId = " + mCompleteMessageData.id);

                EventBus.getDefault().post(new CompleteEvent(String.valueOf(OrderConfig.SilentInstallAppication),
                        "true", mCompleteMessageData.id));

            }
        }
    }

    /**
     * 应用黑白名单与合规
     *
     * @param intent
     */
    private void blackWhiteListAndCompliance(Intent intent) {

        String type = PreferencesManager.getSingleInstance().getOtherData(Common.appManagerType);

        LogUtil.writeToFile(TAG, "type " + type);

        if (type == null)
            return;

        //应用包名
        String packageName = intent.getStringExtra("packageName");
        String action = intent.getStringExtra("action");

        //对新安装的应用进行应用合规处理
        if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {

            LogUtil.writeToFile(TAG, "ACTION_PACKAGE_ADDED ：" + packageName);

            List<String> appList = DatabaseOperate.getSingleInstance().queryAllApp();

            List<APPInfo> appInfos = DatabaseOperate.getSingleInstance().queryInstallAppInfo();

            switch (Integer.parseInt(type)) {
                case 0: //黑名单
                    appList.remove(TheTang.getSingleInstance().getContext().getPackageName());

                    if (appInfos != null) {
                        for (APPInfo appInfo : appInfos) {
                            appList.remove(appInfo.getPackageName());
                        }
                    }

                    if (appList.contains(packageName)) {
                        MDM.getSingleInstance().silentUninstall(packageName);
                    }

                    break;
                case 1: //白名单

                    appList.add(TheTang.getSingleInstance().getContext().getPackageName());

                    if (appInfos != null) {
                        for (APPInfo appInfo : appInfos) {
                            appList.add(appInfo.getPackageName());
                        }
                    }

                    if (!appList.contains(packageName)) {
                        MDM.getSingleInstance().silentUninstall(packageName);
                    }
                    break;
                case 2: //应用合规

                    List<String> apps = new ArrayList<>();
                    apps.add(packageName);

                    if ("1".equals(PreferencesManager.getSingleInstance().getComplianceData(Common.appType))) {

                        TheTang.getSingleInstance().appWhiteListCompliance(appList, apps);

                    } else {

                        TheTang.getSingleInstance().appBlackListCompliance(appList, apps);

                    }
                    break;
               /* case 3: //安全桌面
                    break;*/
                default:
                    break;
            }
        }


        if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {

            List<String> appList = DatabaseOperate.getSingleInstance().queryAllApp();

            switch (Integer.parseInt(type)) {
                case 0: //白名单
                    /*不做处理*/
                    break;
                case 1: //黑名单
                    /*不做处理*/
                    break;
                case 2: //应用合规
                    if (appList.contains(packageName)) {
                        TheTang.getSingleInstance().appComplianceExcute(this, packageName);
                    }
                    break;
                /*case 3: //安全桌面
                    break;*/
                default:
                    break;
            }
        }
    }

    /**
     * 删除后台推送的应用需要删除数据库中对应的数据
     *
     * @param intent
     */
    private void appDeleteByBackground(Intent intent) {

        if (intent.getStringExtra("action").equals(Intent.ACTION_PACKAGE_REMOVED)) {

            //应用包名
            String packageName = intent.getStringExtra("packageName");
            LogUtil.writeToFile(TAG, "appDeleteByBackground = " + packageName);
            EventBus.getDefault().post(new NotifyEvent());
        }
    }

    /**
     * 获得安装的APP安装包的相关信息（如包名、app名、版本号、大小等）并保存到数据库
     */

    private void installApp(DownLoadEntity mDownLoadEntity/*String saveName, String appId, String network, String uninstall*/) {
        PackageManager packageManager = getPackageManager(); //获得包管理器
        String appName = null;

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            PackageInfo info = packageManager.getPackageInfo(mDownLoadEntity.packageName, PackageManager.GET_ACTIVITIES);
            appName = packageManager.getApplicationLabel(info.applicationInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TheTang.getSingleInstance().addMessage(OrderConfig.SilentInstallAppication + "", appName + "--"
                + TheTang.getSingleInstance().getNetworkInfo(mDownLoadEntity.internet) + "," + TheTang.getSingleInstance().getUninstallInfo(mDownLoadEntity.uninstall));

        APPInfo installAPPInfo = new APPInfo();

        installAPPInfo.setAppId(mDownLoadEntity.app_id);
        installAPPInfo.setAppName(appName);
        installAPPInfo.setPackageName(mDownLoadEntity.packageName);

        LogUtil.writeToFile(TAG, "addInstallAppInfo");
        installAPPInfoLists.add(installAPPInfo);
        DatabaseOperate.getSingleInstance().addInstallAppInfo(installAPPInfoLists);
    }

    /**
     * 删除应用逻辑
     *
     * @param param
     */
    private void deleteAppInfo(String param) {


        LogUtil.writeToFile(TAG, "deleteAppInfo");
        //如果不存在，表示卸载成功
        DatabaseOperate.getSingleInstance().deleteInstallAppInfo(param);
        LogUtil.writeToFile(TAG, "卸载成功：" + param);

        //删除时，将应用从防卸载名单中删除
        //TheTang.getSingleInstance().feedBack( (String) params[2], downLoadEntity.app_id, "true" );
        // }
        EventBus.getDefault().post(new NotifyEvent());  //通知前台更新数据
    }


}

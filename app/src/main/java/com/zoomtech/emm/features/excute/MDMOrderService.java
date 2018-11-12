package com.zoomtech.emm.features.excute;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.zoomtech.emm.ITwoProcessAidlInterface;
import com.zoomtech.emm.R;
import com.zoomtech.emm.base.BaseApplication;
import com.zoomtech.emm.base.NetWorkReceiver;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.definition.OrderConfig;
import com.zoomtech.emm.features.ScreenOffReceiver;
import com.zoomtech.emm.features.SwitchLogReceiver;
import com.zoomtech.emm.features.db.DatabaseOperate;
import com.zoomtech.emm.features.event.APKEvent;
import com.zoomtech.emm.features.event.NotifyEvent;
import com.zoomtech.emm.features.keepalive.PlayerMusicService;
import com.zoomtech.emm.features.lockscreen.NewsLifecycleHandler;
import com.zoomtech.emm.features.policy.app.AppReceiver;
import com.zoomtech.emm.features.policy.appFence.AppFenceExcute;
import com.zoomtech.emm.features.policy.compliance.LostComplianceReceiver;
import com.zoomtech.emm.features.policy.compliance.SystemComplianceService;
import com.zoomtech.emm.features.policy.fence.FenceManager;
import com.zoomtech.emm.features.policy.fence.GaodeGeographicalFenceService;
import com.zoomtech.emm.features.policy.fence.TimeFenceReceiver;
import com.zoomtech.emm.features.policy.phoneCall.CallRecorderManager;
import com.zoomtech.emm.features.policy.sms.SmsManager;
import com.zoomtech.emm.features.policy.trajectory.TimeUtils;
import com.zoomtech.emm.features.policy.trajectory.TrajectoryPolice;
import com.zoomtech.emm.features.service.WatchingOrderService;
import com.zoomtech.emm.features.silent.AppTask;
import com.zoomtech.emm.model.DownLoadEntity;
import com.zoomtech.emm.socket.bean.UserMgr;
import com.zoomtech.emm.socket.service.TVBoxService;
import com.zoomtech.emm.utils.ConvertUtils;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.view.activity.MainActivity;
import com.zoomtech.emm.view.activity.SafeDeskActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Administrator on 2017/7/12.
 */

public class MDMOrderService extends Service{
    public static final String TAG = "MDMOrderService";
    AppReceiver mAppReceiver;
    NetWorkReceiver mNetWorkReceiver;
    ScreenOffReceiver mScreenOffReceiver;
    //MachineCardBindingReceiver mMachineCardBindingReceiver;

    SwitchLogReceiver mSwitchLogReceiver;
    LostComplianceReceiver mLostComplianceReceiver;
    private InnerRecevier innerReceivers;

    private RemoteBilder mBilder;

    private MDM mMDM;

    @Override
    public IBinder onBind(Intent intent) {
        //if (mBilder == null)
        //    mBilder = new RemoteBilder();

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMDM = MDM.getSingleInstance();
        TheTang.getSingleInstance().startForeground(this,getResources().getString(R.string.emm_runing),"EMM",14);
        //创建广播
        innerReceivers = new InnerRecevier();
        //动态注册广播
        IntentFilter intentFilters = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        //启动广播
        registerReceiver(innerReceivers, intentFilters);


        TheTang.getSingleInstance().initImplTwo();

        TheTang.getSingleInstance().startService( new Intent( this, WatchingOrderService.class ) );

        cancelBroadcast( PreferencesManager.getSingleInstance() );

        //初始化长连接
        initLongLink();
        initStratege();

        //EventBus注册
/*        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register( this );
        }*/

        mAppReceiver = new AppReceiver();
        IntentFilter appFilter = new IntentFilter();
        appFilter.addAction( "android.intent.action.PACKAGE_ADDED" );
        appFilter.addAction( "android.intent.action.PACKAGE_REMOVED" );
        appFilter.addAction( "android.intent.action.MY_PACKAGE_REPLACED" );
        appFilter.addDataScheme("package");
        registerReceiver(mAppReceiver,appFilter);

        mNetWorkReceiver = new NetWorkReceiver();
        IntentFilter networkFilter = new IntentFilter();
        networkFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkFilter.addAction( "android.net.wifi.WIFI_STATE_CHANGED" );
        registerReceiver(mNetWorkReceiver,networkFilter);

        mScreenOffReceiver = new ScreenOffReceiver();
        IntentFilter screenFilter = new IntentFilter(  );
        screenFilter.addAction( Intent.ACTION_SCREEN_ON );
        screenFilter.addAction( Intent.ACTION_SCREEN_OFF );
        registerReceiver(mScreenOffReceiver, screenFilter);

        /*mMachineCardBindingReceiver = new MachineCardBindingReceiver();
        IntentFilter intentFilter = new IntentFilter(  );
        intentFilter.addAction( "android.intent.action.SIM_STATE_CHANGED" );
        registerReceiver( mMachineCardBindingReceiver, intentFilter );*/

        mSwitchLogReceiver = new SwitchLogReceiver();
        IntentFilter switchFilter = new IntentFilter();
        switchFilter.addAction("com.android.server.vp_switch_start");
        switchFilter.addAction("com.android.server.back_vpswitch_end");
        registerReceiver( mSwitchLogReceiver, switchFilter );

        mLostComplianceReceiver = new LostComplianceReceiver();
        IntentFilter lostFilter = new IntentFilter();
        lostFilter.addAction("lost_compliance");
        registerReceiver(mLostComplianceReceiver, lostFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        /*if (isServiceExisted(this, "PlayerMusicService")) {
            bindService(new Intent(this, PlayerMusicService.class),
                    connection, Context.BIND_AUTO_CREATE);
        } else {
            startService( new Intent(this, PlayerMusicService.class) );
            //绑定远程服务
            bindService(new Intent(this, PlayerMusicService.class),
                    connection, Context.BIND_AUTO_CREATE);
        }*/
        return START_REDELIVER_INTENT;
    }

    /**
     * 初始化相关功能
     */
    private void initStratege() {
        TheTang.getSingleInstance().getThreadPoolObject().submit( new Runnable() {
            @Override
            public void run() {
                //取消时间工具的广播--
              TimeUtils.cancelAllReceive();
                //初始化时间工具
                TimeUtils.setAlltimeReceivers();

                //扫描应用包名
                scanAppsPackage();

                //获得应用更新时间
                final PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
                if (preferencesManager.getOtherData( "client_last_update_time" ) == null) {
                    preferencesManager.setOtherData("client_last_update_time", System.currentTimeMillis() + "");
                }

                //获取默认限制策略
                String defaultPolicy = preferencesManager.getPolicyData(Common.default_policy);
                if (defaultPolicy == null) {
                    storageLimitPolicy();
                }

                //执行时间围栏广播策略
                String startTimeRage = preferencesManager.getFenceData( Common.startimeRage);
                String endTimeRage =  preferencesManager.getFenceData( Common.endTimeRage);

                String codeSafeDesk = preferencesManager.getSafedesktopData("code");
                if (endTimeRage != null && !TextUtils.isEmpty( endTimeRage )) {
                    Log.w(TAG,"重启服务 有时间围栏数据FenceManager.doSendBroadcast(); ");
                    FenceManager.doSendBroadcast();
                }else  if (codeSafeDesk != null && !TextUtils.isEmpty( codeSafeDesk )){
                    //安全桌面策略
                    // ExcuteSafeDesktop.excute_SafeDesktop();
                }
                if (/*preferencesManager.getConfiguration("isVpn") !=null && */!TextUtils.isEmpty( preferencesManager.getConfiguration("isVpn") )){
                    // ConfigurationPolicy.doConfigurationPolicy();
                }

                //如果有网络白名单，在关机后需重新设置
                if (!TextUtils.isEmpty( preferencesManager.getComplianceData( Common.securityChrome ) )) {
                    Map<String, String> sec_white_list = new HashMap<>();
                    sec_white_list = ConvertUtils.jsonStringToMap( preferencesManager.getComplianceData( Common.securityChrome_list ) );
                    MDM.getSingleInstance().excuteChrome( sec_white_list );
                }

                //执行sd卡违规
                if ("true".equals(preferencesManager.getComplianceData(Common.system_sd))) {
                    Intent intent = new Intent(TheTang.getSingleInstance().getContext(), SystemComplianceService.class);
                    TheTang.getSingleInstance().startService(intent);
                }

                //执行SIM卡违规
                if ("true".equals(preferencesManager.getComplianceData(Common.system_sim))) {
                    mMDM.excuteMachineCard(false);
                }

                //如果有地理围栏，则启动地理围栏
                String geographical = preferencesManager.getFenceData( Common.geographical_fence );
                if (!TextUtils.isEmpty( geographical ) && "true".equals( geographical )) {
                    mMDM.forceLocationService();
                    TheTang.getSingleInstance().startService( new Intent( TheTang.getSingleInstance().getContext(), GaodeGeographicalFenceService.class ) );
                }

                //如果有系统违规，则启动系统违规
                /*String system = preferencesManager.getComplianceData( Common.system_compliance );
                if (!TextUtils.isEmpty( system ) && "true".equals( system )) {
                    TheTang.getSingleInstance().startService( new Intent( TheTang.getSingleInstance().getContext(), SystemComplianceService.class ) );
                }*/

                //开启应用围栏
                AppFenceExcute.excuteFence( preferencesManager );

                SmsManager.newInstance().checkSmsPolicy();
                CallRecorderManager.newInstance().checkCallRecorderPolicy();

                if (!TextUtils.isEmpty(  preferencesManager.getTrajectoryData( Common.trajectoryName) )){
                   TrajectoryPolice.doTrajectoryPolice(preferencesManager);
                }
            }
        });
    }

    /**
     * 初始化长连接
     */
    private void initLongLink() {
        if (TVBoxService.getInstance() == null || TVBoxService.getInstance().getConnTask( ) == null){
            //初始化本地长连接
            Log.e(TAG,"初始化本地长连接");
            UserMgr.createInstance(this);
            Intent integer = new Intent(this, TVBoxService.class);
            TheTang.getSingleInstance().startService(integer);
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        if (innerReceivers != null) {
            unregisterReceiver( innerReceivers );
        }

        if (mAppReceiver != null) {
            unregisterReceiver( mAppReceiver );
        }

        if (mNetWorkReceiver != null) {
            unregisterReceiver( mNetWorkReceiver );
        }

        if (mScreenOffReceiver != null) {
            unregisterReceiver( mScreenOffReceiver );
        }

        /*if (mMachineCardBindingReceiver != null) {
            unregisterReceiver( mMachineCardBindingReceiver );
        }*/

        if ( mSwitchLogReceiver != null ) {
            unregisterReceiver(mSwitchLogReceiver);
        }

        if ( mLostComplianceReceiver != null ) {
            unregisterReceiver(mLostComplianceReceiver);
        }

        if ( EventBus.getDefault().isRegistered( this )) {
            EventBus.getDefault().unregister( this );
        }

        TheTang.getSingleInstance().cancelNotification(0);

        //防止应用被回收
        //TheTang.getSingleInstance().startService( new Intent( this, SelfStartService.class ) );
    }

    /**
     * 默认策略缓存
     */
    private static void storageLimitPolicy() {
        MDM mdm = MDM.getSingleInstance();
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

        if (mdm.isSoundRecordingEnabled()) {
            preferencesManager.setPolicyData(Common.default_allowSoundRecording,"1");
        } else {
            preferencesManager.setPolicyData(Common.default_allowSoundRecording,"0");
        }

        //if (MDM.isDataConnectivityOpen()) {
            preferencesManager.setPolicyData(Common.default_allowMobileData,"1");
        //} else {
        //    preferencesManager.setPolicyData(Common.default_allowMobileData,"0");
        //}

        if (mdm.isCameraEnabled()) {
            preferencesManager.setPolicyData(Common.default_allowCamera,"1");
        } else {
            preferencesManager.setPolicyData(Common.default_allowCamera,"0");
        }

        if (mdm.isUsbEnabled()) {
            preferencesManager.setPolicyData(Common.default_allowUsb,"1");
        } else {
            preferencesManager.setPolicyData(Common.default_allowUsb,"0");
        }

        if (mdm.isLocationServiceEnabled()) {
            preferencesManager.setPolicyData(Common.default_allowLocation,"1");
        } else {
            preferencesManager.setPolicyData(Common.default_allowLocation,"0");
        }

        if (mdm.isWifiEnabled()) {
            preferencesManager.setPolicyData(Common.default_allowWifi,"1");
        } else {
            preferencesManager.setPolicyData(Common.default_allowWifi,"0");
        }

        if (mdm.isSmsEnabled()) {
            preferencesManager.setPolicyData(Common.default_allowMessage,"1");
        } else {
            preferencesManager.setPolicyData(Common.default_allowMessage,"0");
        }

        if (mdm.isBluetoothEnabled()) {
            preferencesManager.setPolicyData(Common.default_allowBluetooth,"1");
        } else {
            preferencesManager.setPolicyData(Common.default_allowBluetooth,"0");
        }

        if (mdm.isWifiAPEnabled()) {
            preferencesManager.setPolicyData(Common.default_allowWifiAP,"1");
        } else {
            preferencesManager.setPolicyData(Common.default_allowWifiAP,"0");
        }

        if (mdm.isDropdownEnabled()) {
            preferencesManager.setPolicyData(Common.default_allowDropdown,"1");
        } else {
            preferencesManager.setPolicyData(Common.default_allowDropdown,"0");
        }

        if (mdm.isResetEnabled()) {
            preferencesManager.setPolicyData(Common.default_allowReset,"1");
        } else {
            preferencesManager.setPolicyData(Common.default_allowReset,"0");
        }

        if (mdm.isNfcEnabled(null) ){
            preferencesManager.setPolicyData(Common.default_allowNFC,"1");
        } else {
            preferencesManager.setPolicyData(Common.default_allowNFC,"0");
        }

        if (mdm.isModifySystemtimeEnabled()) {
            preferencesManager.setPolicyData(Common.default_allowModifySystemtime,"1");
        } else {
            preferencesManager.setPolicyData(Common.default_allowModifySystemtime,"0");
        }

        if (mdm.isScreenShotEnabled()) {
            preferencesManager.setPolicyData(Common.default_allowScreenshot,"1");
        } else {
            preferencesManager.setPolicyData(Common.default_allowScreenshot,"0");
        }

        //preferencesManager.setPolicyData(Common.default_allowSdCard,policyData.allowSdCard);
        //preferencesManager.setPolicyData(Common.default_allowMobileHotspot,policyData.allowMobileHotspot);
        //preferencesManager.setPolicyData(Common.default_allowRestoreFactorySettings,policyData.allowRestoreFactorySettings);
        //preferencesManager.setPolicyData(Common.default_allowUpdateTime,policyData.allowUpdateTime);

        if (mdm.isTelephoneEnabled()) {
            preferencesManager.setPolicyData(Common.default_allowTelephone,"1");
        } else {
            preferencesManager.setPolicyData(Common.default_allowTelephone,"0");
        }

        if ("true".equals(preferencesManager.getOtherData(Common.white_phone))) {
            preferencesManager.setPolicyData(Common.default_allowTelephoneWhiteList,"1");
        } else {
            preferencesManager.setPolicyData(Common.default_allowTelephoneWhiteList,"0");
        }
        preferencesManager.setPolicyData(Common.default_policy,"true");
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
    public void notifyData(NotifyEvent event) {
        final Context context = TheTang.getSingleInstance().getContext();
        Log.w(TAG,"start-------------"+event.getMsg());
        if (event.getMsg() != null && "start".equals(event.getMsg())){

            final PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

            if (preferencesManager.getFenceData(Common.setToSecureDesktop) != null &&
                    "1".equals(preferencesManager.getFenceData(Common.setToSecureDesktop)) ||
                    !TextUtils.isEmpty( preferencesManager.getSafedesktopData("code")
                    )||(!TextUtils.isEmpty( preferencesManager.getSecurityData( Common.safetyTosecureFlag)) &&
                    !TextUtils.isEmpty(preferencesManager.getSecurityData( Common.secureDesktopFlag )) )) {

                TheTang.getSingleInstance().getThreadPoolObject().submit(new Runnable() {
                    @Override
                    public void run() {
                        preferencesManager.setLockFlag("unLockScreen",true);
                        while (true){
                            List<String> homePackageNames = null;
                            boolean home = isHome(homePackageNames);
                            ActivityManager mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                            List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
                            Log.w(TAG,"判断当前界面是否是桌面-===-" + rti.get(0).topActivity.getPackageName());

                            Log.w(TAG,"判断当前界面是否是桌面" + home);

                            if (home){
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Log.w(TAG,"如果当前界面是否是----"+ rti.get(0).topActivity.getPackageName()+"切换到安全桌面");
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                String packageName = TheTang.getSingleInstance().getContext().getPackageName();
                                String className = SafeDeskActivity.class.getName();
                                ComponentName cn = new ComponentName(packageName, className);
                                intent.setComponent(cn);
                                context.startActivity(intent);
                                break;
                            }else if(  NewsLifecycleHandler.resumed > NewsLifecycleHandler.paused){
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if ( NewsLifecycleHandler.resumed > NewsLifecycleHandler.paused){
                                    Log.w(TAG,"判断------当前界面是否是----"+ rti.get(0).topActivity.getPackageName());
                                    if (rti.get(0).topActivity.getPackageName().equals(TheTang.getSingleInstance().getContext().getPackageName()) && (BaseApplication.getNewsLifecycleHandler().isSameClassName(SafeDeskActivity.class.getSimpleName())||BaseApplication.getNewsLifecycleHandler().isSameClassName(MainActivity.class.getSimpleName()))){
                                        Log.w(TAG,"当前界面是否是----"+ rti.get(0).topActivity.getPackageName()+"  退出线程");
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 获得属于桌面的应用的应用包名称
     * @return 返回包含所有包名的字符串列表
     */
    private List<String> getHomes() {
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = this.getPackageManager();
        //属性
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for(ResolveInfo ri : resolveInfo){
            names.add(ri.activityInfo.packageName);
            System.out.println(ri.activityInfo.packageName);
        }
        return names;
    }

    /**
     * 判断当前界面是否是桌面
     * @param homePackageNames
     */
    public boolean isHome(List<String> homePackageNames){
        ActivityManager mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        boolean contains = "com.huawei.android.launcher".equals(rti.get(0).topActivity.getPackageName());//homePackageNames.contains(rti.get(0).topActivity.getPackageName());
        if (contains){
            Log.w(TAG,"判断当前界面是否是桌面" + rti.get(0).topActivity.getPackageName());
        }
        return contains;
    }

    /**
     * 清除下载的安装包
     */
    private void scanAppsPackage() {

        File appsDir = new File( BaseApplication.baseAppsPath );
        PackageManager pm = this.getPackageManager();

        if (appsDir.exists()) {

            File[] files = appsDir.listFiles();
            PackageInfo info = null;

            if (files != null && files.length > 0) {

                for (int i = 0; i < files.length; i++) {

                    info = pm.getPackageArchiveInfo( BaseApplication.baseAppsPath + File.separator + files[i].getName(), PackageManager.GET_ACTIVITIES );

                    if (info != null) {
                        DownLoadEntity mDownLoadEntity = DatabaseOperate.getSingleInstance().queryDownLoadFileByPackageName(info.packageName);
                        //如果没有该文件的下载信息，则直接删除
                        if (mDownLoadEntity == null) {
                            MDM.getSingleInstance().deleteFile( new File( BaseApplication.baseAppsPath + File.separator + files[i].getName() ) );
                            continue;
                        }

                        //表示应用未下载完成
                        if (files[i].length() < mDownLoadEntity.total) {
                            continue;
                        }

                        //表示没有安装成功，或者安装成功没有及时删除相关数据
                        String version = MDM.getSingleInstance().judgmentAppHadInstall( info.packageName );

                        if (version != null) {
                            if (!MDM.getSingleInstance().isAppNewVersion( version, info.versionName )) {
                                DatabaseOperate.getSingleInstance().deleteDownLoadFile( mDownLoadEntity );
                                MDM.getSingleInstance().deleteFile( new File( BaseApplication.baseAppsPath + File.separator + files[i].getName() ) );
                            } else {
                                final APKEvent event = new APKEvent(mDownLoadEntity, OrderConfig.SilentInstallAppication);
                                AppTask appTask = new AppTask();
                                appTask.onSilentExcutor(event);
                            }
                        } else {
                            final APKEvent event = new APKEvent(mDownLoadEntity, OrderConfig.SilentInstallAppication);
                            AppTask appTask = new AppTask();
                            appTask.onSilentExcutor(event);
                        }
                    }
                }
            }
        }
    }

    public  class InnerRecevier extends BroadcastReceiver {

        final String SYSTEM_DIALOG_REASON_KEY = "reason";

        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                Log.w(TAG,"reason="+reason);
                if (reason != null) {
                    if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {

                        Log.w(TAG,"Home键被监听");
                        NewsLifecycleHandler.LockFlag = true;
                        } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {

                        NewsLifecycleHandler.LockFlag = true;
                        Log.w(TAG, "多任务键被监听");
                    }
                }
            }
        }
    }


    public static class RemoteBilder extends ITwoProcessAidlInterface.Stub {

        @Override
        public void doSomething() throws RemoteException {
        }

    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG, "RemoteService被杀死了！");
            Intent localService = new Intent(MDMOrderService.this, PlayerMusicService.class);
            startService( localService );
            bindService(localService, connection, Context.BIND_AUTO_CREATE);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.w(TAG, "RemoteService连接成功！");
        }
    };

    /**
     * 判断时间围栏是否已经超出范围
     *
     * @param preferencesManager
     */
    private void cancelBroadcast(PreferencesManager preferencesManager) {
        //获取时间日期范围
        String startTimeRage = preferencesManager.getFenceData( Common.startimeRage );
        String endTimeRage = preferencesManager.getFenceData( Common.endTimeRage );

        if (startTimeRage != null && endTimeRage != null) {
            SimpleDateFormat formats = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );

            Date parse1 = null;
            try {
                parse1 = formats.parse( endTimeRage + " 23:59" );
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (parse1 != null) {
                AlarmManager am = (AlarmManager) TheTang.getSingleInstance().getContext().getSystemService( ALARM_SERVICE );
                /**如果当前时间已经超过时间范围则返回*/
                if (System.currentTimeMillis() > parse1.getTime()) {
                    Log.w( TAG, "如果当前时间已经超过时间围栏设置的结束时间范围，不知执行闹钟" );
                    LogUtil.writeToFile( TAG, "当前时间已经超过时间围栏设置的结束时间范围，不知执行闹钟" );
                    /**结束所有的广播，同时取消策略(可以把本地的时间围栏数据删除掉)*/
                    Intent intent_startTimeRage = new Intent( TheTang.getSingleInstance().getContext(), TimeFenceReceiver.class );
                    PendingIntent pi = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(),
                            0, intent_startTimeRage, PendingIntent.FLAG_UPDATE_CURRENT );
                    am.cancel( pi );
                    preferencesManager.clearTimefenceData();
                }
            }
        }
    }

    /**
     * 判断服务是否存在
     * @param context
     * @param className
     * @return
     */
    public static boolean isServiceExisted(Context context, String className) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
        if(!(serviceList.size() > 0)) {
            return false;
        }
        for(int i = 0; i < serviceList.size(); i++) {
            ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
            ComponentName serviceName = serviceInfo.service;
            if(serviceName.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }
}

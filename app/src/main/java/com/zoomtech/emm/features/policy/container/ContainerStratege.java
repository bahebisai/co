package com.zoomtech.emm.features.policy.container;

import android.content.ComponentName;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.zoomtech.emm.base.BaseApplication;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.features.event.NotifyEvent;
import com.zoomtech.emm.features.event.NotifySafedesk;
import com.zoomtech.emm.features.lockscreen.TimeFenceService;
import com.zoomtech.emm.features.policy.app.ExcuteSafeDesktop;
import com.zoomtech.emm.features.policy.device.ExcuteLimitPolicy;
import com.zoomtech.emm.features.policy.fence.FenceExcute;
import com.zoomtech.emm.features.policy.fence.FenceManager;
import com.zoomtech.emm.features.policy.fence.GaodeGeographicalFenceService;
import com.zoomtech.emm.features.service.NetWorkChangeService;
import com.zoomtech.emm.model.PolicyData;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.view.activity.MainActivity;
import com.zoomtech.emm.view.activity.SafeDeskActivity;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2017/11/7.
 */

public class ContainerStratege {

    public final static String TAG = "ContainerStratege";

    /**
     * 切换到安全域策略
     */
    public static void excuteSecurityContainerStratege() {

        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

        //停止时间围栏
        if (!TextUtils.isEmpty( preferencesManager.getFenceData( Common.endTimeRage ) )) {
            //停止时间围栏的服务
            Intent intentTime = new Intent( TheTang.getSingleInstance().getContext(), TimeFenceService.class );
            TheTang.getSingleInstance().getContext().stopService( intentTime );
            //取消时间围栏的广播
            FenceManager.cancelTimeRecevie();

            FenceExcute.excuteGeographicalFence( false, true );
        }

        //停止地理围栏
        if (!TextUtils.isEmpty( preferencesManager.getFenceData( Common.latitude ) )) {
            Intent intent = new Intent( TheTang.getSingleInstance().getContext(), GaodeGeographicalFenceService.class );
            TheTang.getSingleInstance().getContext().stopService( intent );
            //初始化之前的一般状态
            FenceExcute.excuteGeographicalFence( false, true );
        }
        preferencesManager.setSecurityData( Common.securityContainer, "true" );
        String banExitSecurityDomain = preferencesManager.getSecurityData( Common.banExitSecurityDomain );

        MDM mdm = MDM.getSingleInstance();
        if (!TextUtils.isEmpty( banExitSecurityDomain ) && "0".equals( banExitSecurityDomain )) {
            //用于完成域的切换与禁止
            if (mdm.isInFgContainer()) {
                mdm.disableSwitching();
            } else {
                try {
                    mdm.enableSwitching();
                    Thread.sleep( 1000 );
                    mdm.toSecurityContainer();
                    Thread.sleep( 1100 );
                    mdm.disableSwitching();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } else {
            mdm.toSecurityContainer();//切换到安全域
        }

        threadStart();

        if ("0".equals( preferencesManager.getSecurityData( Common.banCamera ) )) {
            mdm.enableCamera( false );
        } else {
            mdm.enableCamera( true );
        }

        if ("0".equals( preferencesManager.getSecurityData( Common.banWifi ) )) {
            mdm.enableWifi( false );
        } else {
            mdm.enableWifi( true );
        }

        if ("0".equals( preferencesManager.getSecurityData( Common.banBluetooth ) )) {
            mdm.enableBluetooth( false );
        } else {
            mdm.enableBluetooth( true );
        }

        if ("0".equals( preferencesManager.getSecurityData( Common.banLocation ) )) {
            mdm.enableLocationService( false );
        } else {
            mdm.enableLocationService( true );
        }

        if ("0".equals( preferencesManager.getSecurityData( Common.banMtp ) )) {
            mdm.enableUsb( false );
        } else {
            mdm.enableUsb( true );
        }

        if ("0".equals( preferencesManager.getSecurityData( Common.banSoundRecord ) )) {
            mdm.enableSoundRecording( false );
        } else {
            mdm.enableSoundRecording( true );
        }

        if ("0".equals( preferencesManager.getSecurityData( Common.banScreenshot ) )) {
            mdm.disableScreenShot();
        } else {
            mdm.enableScreenShot();
        }

        if ("0".equals( preferencesManager.getSecurityData( Common.allowDropdown ) )) {
            mdm.disableDropdown();
        } else {
            mdm.enableDropdown();
        }

        if ("0".equals( preferencesManager.getSecurityData( Common.allowReset ) )) {
            mdm.disableReset();
        } else {
            mdm.enableReset();
        }

        if ("0".equals( preferencesManager.getSecurityData( Common.allowNFC ) )) {
            mdm.disableNfc(null);
        } else {
            mdm.enableNfc(null);
        }

        if ("0".equals( preferencesManager.getSecurityData( Common.allowModifySystemtime ) )) {
            mdm.disableModifySystemtime();
        } else {
            mdm.enableModifySystemtime();
        }

        if ("0".equals( preferencesManager.getSecurityData( Common.banTelephone ) )) {
            mdm.enableTelePhone(false);
        } else {
            mdm.enableTelePhone(true);
        }

        if ("0".equals( preferencesManager.getSecurityData( Common.banTelephoneWhiteList ) )) {
            mdm.startPhoneWhite();
        } else {
            mdm.stopPhoneWhite();
        }

        if ("0".equals( preferencesManager.getSecurityData( Common.banMobileHotspot ) )) {
            mdm.enableWifiAP(false);
        } else {
            mdm.enableWifiAP(true);
        }

        if ("0".equals( preferencesManager.getSecurityData( Common.banShortMessage ) )) {
            mdm.enableSms(false);
        } else {
            mdm.enableSms(true);
        }

        Log.w(TAG,"启用安全桌面==Common.secureDesktop=="+preferencesManager.getSecurityData( Common.secureDesktop ));
        //启用安全桌面
        if ("1".equals( preferencesManager.getSecurityData( Common.secureDesktop ) )) {
            //当第一次下发安全桌面下来，执行后，防止还在安全配置下的桌面的状态下，手机关机重启,后会根据这个状态来是否进入安全桌面
            preferencesManager.setSecurityData( Common.secureDesktopFlag,"true" );
            Log.w(TAG,"启用安全桌面====");
            if (BaseApplication.getNewsLifecycleHandler().isSameClassName( SafeDeskActivity.class.getSimpleName() )) {
                //刷新界面
                EventBus.getDefault().post(new NotifySafedesk(Common.safeActicivty_flush));
                /**设置虚拟按键是不可见*/
               // MDM.setRecentKeyVisible( false );
               // MDM.setHomeKeyVisible( false );
            } else {

                Log.w(TAG,"unLockScreen  ===启用安全桌面====");
                preferencesManager.setLockFlag( "unLockScreen", true );
                Intent intent = new Intent( Intent.ACTION_MAIN );
                intent.addCategory( Intent.CATEGORY_LAUNCHER );
                String packageName = TheTang.getSingleInstance().getContext().getPackageName();//"com.zoomtech.emm";
                String className = SafeDeskActivity.class.getName();
                ComponentName cn = new ComponentName( packageName, className );
                intent.setComponent( cn );
                TheTang.getSingleInstance().getContext().startActivity( intent );
            }
        }else {
            if (BaseApplication.getNewsLifecycleHandler().isSameClassName(MainActivity.class.getSimpleName())){


            } else {

                Intent intent = new Intent( Intent.ACTION_MAIN );
                intent.addCategory( Intent.CATEGORY_LAUNCHER );
                String packageName = TheTang.getSingleInstance().getContext().getPackageName();//"com.zoomtech.emm";
                String className = MainActivity.class.getName();//"com.zoomtech.emm.view.activity.SafeDeskActivity";
                ComponentName cn = new ComponentName( packageName, className );
                intent.setComponent( cn );
                TheTang.getSingleInstance().getContext().startActivity( intent );

            }
            mdm.enableFingerNavigation(true);
            mdm.setRecentKeyVisible( true );
            mdm.setHomeKeyVisible( true );
            EventBus.getDefault().post( new NotifySafedesk(Common.safeActicivty_finsh) ); //关闭安全桌面

        }
    }

    private static void threadStart() {
        //解决跳转失败的问题
        TheTang.getSingleInstance().getThreadPoolObject().submit( new Runnable() {
            @Override
            public void run() {
                while (true) {
                    //用于完成安全域的切换与禁止
                    try {
                        Thread.sleep( 5000 );
                        NetWorkChangeService.sendFeedBackFalie();

                        if ("true".equals(  PreferencesManager.getSingleInstance()
                                .getSecurityData( Common.securityContainer))) { //如果离开安全区域
                            break;
                        } else {
                            if (!MDM.getSingleInstance().isInFgContainer()) {
                                //MDM.playMusic();
                                MDM.getSingleInstance().enableSwitching();
                                Thread.sleep( 2000 );
                                MDM.getSingleInstance().toSecurityContainer();
                                Thread.sleep( 1100 );
                                MDM.getSingleInstance().disableSwitching();
                                NetWorkChangeService.sendFeedBackFalie();
                            } else {
                                break;
                            }
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                LogUtil.writeToFile( TAG, "我已经结束了..." + System.currentTimeMillis() );
            }
        } );
    }

    /**
     * 切换到生活域策略
     */
    public static void excuteLifeContainerStratege() {

        PreferencesManager.getSingleInstance().setSecurityData( Common.securityContainer, "false" );

        try {
            MDM.getSingleInstance().enableSwitching();
            Thread.sleep( 2000 );
            MDM.getSingleInstance().toLifeContainer();
            Thread.sleep( 1300 );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

        //切换到生活域，则删除安全桌面状态
        preferencesManager.removeSecurityData( Common.secureDesktopFlag );

        if (preferencesManager.getPolicyData( Common.middle_policy ) != null) { //如果有下发限制策略，则恢复到限制策略
            PolicyData policyData = ExcuteLimitPolicy.queryLimitPolicy();
            ExcuteLimitPolicy.limitPolicy( policyData );
        } else {
            ExcuteLimitPolicy.limitDefaultPolicy();//如果没有下发限制策略，则恢复到默认策略
        }

        //如果下发机卡绑定策略，则关闭机卡绑定服务
        //preferencesManager.setSecurityData( Common.machineCardBind, "0" ); //不管是否限制机卡绑定，在退出限制区域时，都将该参数设为0


        //如果没有安全策略，则关闭
        if (TextUtils.isEmpty( preferencesManager.getSafedesktopData( Common.CODE ) )) {
            LogUtil.writeToFile( TAG, "---1" );
            String secureDesktop = preferencesManager.getFenceData( Common.setToSecureDesktop );

            if (TextUtils.isEmpty( secureDesktop ) || "2".equals( secureDesktop )) {
                MDM.getSingleInstance().enableFingerNavigation(true);
                MDM.getSingleInstance().setRecentKeyVisible( true );
                MDM.getSingleInstance().setHomeKeyVisible( true );
                EventBus.getDefault().post( new NotifySafedesk(Common.safeActicivty_finsh) ); //关闭安全桌面
            } else {
                String inside = preferencesManager.getFenceData( Common.insideAndOutside );

                if (TextUtils.isEmpty( inside ) || "false".equals( inside )) {
                    MDM.getSingleInstance().enableFingerNavigation(true);
                    MDM.getSingleInstance().setRecentKeyVisible( true );
                    MDM.getSingleInstance().setHomeKeyVisible( true );
                    EventBus.getDefault().post( new NotifySafedesk(Common.safeActicivty_finsh) ); //关闭安全桌面
                }
            }

            EventBus.getDefault().post( new NotifyEvent() );
        } else {

            if (TextUtils.isEmpty( PreferencesManager.getSingleInstance().getFenceData( Common.setToSecureDesktop ) ) ||
                    "2".equals( PreferencesManager.getSingleInstance().getFenceData( Common.setToSecureDesktop ) )  ||
                    TextUtils.isEmpty( preferencesManager.getFenceData( Common.insideAndOutside) ) ||
                    "false".equals( preferencesManager.getFenceData( Common.insideAndOutside )) ) {

                //如果没有时间围栏策略，或者当前处于时间围栏外，则可以执行安全桌面策略
                ExcuteSafeDesktop.excute_SafeDesktop();

            }
        }

        //恢复时间围栏
        if (!TextUtils.isEmpty( preferencesManager.getFenceData( Common.endTimeRage ) )) {
            //取消时间围栏的广播
            FenceManager.doSendBroadcast();
        }

        //恢复地理围栏
        if (!TextUtils.isEmpty( preferencesManager.getFenceData( Common.latitude ) )) {
            MDM.getSingleInstance().forceLocationService();
            Intent intent = new Intent( TheTang.getSingleInstance().getContext(), GaodeGeographicalFenceService.class );
            TheTang.getSingleInstance().startService( intent );
            //初始化之前的一般状态
        }
    }
}

package com.xiaomi.emm.features;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import com.miui.enterprise.sdk.ApplicationManager;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.excute.MDMOrderService;
import com.xiaomi.emm.features.excute.XiaomiMDMController;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.MDM;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;
import com.xiaomi.emm.view.activity.MainActivity;
import com.xiaomi.emm.view.activity.SafeDeskActivity;

/**
 * Created by Administrator on 2017/8/16.
 */

public class SelfStartReceiver extends BroadcastReceiver {

    final static String TAG = "SelfStartReceiver";

    PreferencesManager mPreferencesManager = PreferencesManager.getSingleInstance();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        XiaomiMDMController.getSingleInstance().setApplicationSettings();

        TheTang.getSingleInstance().isLostCompliance(false);

        /*if ("true".equals( mPreferencesManager.getComplianceData( Common.lost_compliance ))) {
            String lost_time_frame = mPreferencesManager.getComplianceData( Common.lost_time_frame );

            if (!TextUtils.isEmpty(lost_time_frame)) {
                String lost_time = mPreferencesManager.getComplianceData( Common.lost_time );
                if ((System.currentTimeMillis() - Double.valueOf(lost_time_frame)) > (Double.valueOf(lost_time) * 24 * 3600 * 1000)) {
                    MDM.setScreenLock(null);
                } else {
                    //mPreferencesManager.removeComplianceData( Common.lost_time );
                    //启动定时器
                }
            }
        }*/

        LogUtil.writeToFile( TAG, "SelfStartService onHandleIntent!" );
        //如果未登录则弹出登录界面
        if (mPreferencesManager.getData( Common.alias ) == null
                || mPreferencesManager.getLockPassword( "password" ) == null) {
            //startActivity(context);
            return;
        } else {

            if (!TextUtils.isEmpty( mPreferencesManager.getSecurityData( Common.safetyTosecureFlag ) )) {
                if (!TextUtils.isEmpty( mPreferencesManager.getSecurityData( Common.secureDesktopFlag ) )) {
                    startActivity(context);
                } else {
                    startService(context);
                }

            } else if (TextUtils.isEmpty( mPreferencesManager.getFenceData( Common.insideAndOutside ) ) ||
                    "false".equals( mPreferencesManager.getFenceData( Common.insideAndOutside ) )) {

                if (!TextUtils.isEmpty( mPreferencesManager.getSafedesktopData( "code" ) )) {
                    startActivity(context);
                } else {
                    startService(context);
                }
            } else if ( mPreferencesManager.getFenceData( Common.insideAndOutside ) != null && "true".
                            equals( mPreferencesManager.getFenceData( Common.insideAndOutside ) )) {

                if (!TextUtils.isEmpty( mPreferencesManager.getFenceData( Common.setToSecureDesktop ) ) &&
                        !"2".equals( mPreferencesManager.getFenceData( Common.setToSecureDesktop ) )) {
                    startActivity(context);
                } else if ((TextUtils.isEmpty( mPreferencesManager.getFenceData( Common.setToSecureDesktop ) ) ||
                        "2".equals( mPreferencesManager.getFenceData( Common.setToSecureDesktop ) ))) {
                    if (!TextUtils.isEmpty( mPreferencesManager.getSafedesktopData( "code" ) )) {
                        startActivity(context);
                    } else {
                        startService(context);
                    }

                }
            } else {
                startService(context);
            }
        }


    }

    private void startActivity(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent startIntent = packageManager.getLaunchIntentForPackage( context.getPackageName() );
        context.startActivity( startIntent );
    }

    private void startService(Context context) {
        Intent startIntent = new Intent( context, MDMOrderService.class );
        if (startIntent != null) {
            TheTang.getSingleInstance().startService( startIntent );
        }
    }

}

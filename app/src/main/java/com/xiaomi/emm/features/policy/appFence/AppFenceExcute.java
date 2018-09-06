package com.xiaomi.emm.features.policy.appFence;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.xiaomi.emm.R;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.OrderConfig;
import com.xiaomi.emm.features.event.NotifySafedesk;
import com.xiaomi.emm.model.AppFenceData;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import static android.content.Context.ALARM_SERVICE;


/**
 * Created by Administrator on 2018/1/16.
 */

public class AppFenceExcute {

    private final static String TAG = "AppFenceExcute";
    private static Context mContext =  TheTang.getSingleInstance().getContext();

    public static boolean whetherAppInSide = false;

    public static void excuteAppFence(AppFenceData appFenceData) {

        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

        if (appFenceData == null) {

            if (TextUtils.isEmpty(  preferencesManager.getAppFenceData( Common.appFenceName ) )){
                LogUtil.writeToFile(TAG,"本地已经没有应用围栏数据");
                return;
            }
            mContext.stopService( new Intent( mContext, WatchingAppStartService.class ) );
            mContext.stopService( new Intent( mContext, AppFenceService.class ) );

            TheTang.getSingleInstance().addMessage( String.valueOf( OrderConfig.unstall_application_fence ),
                    preferencesManager.getAppFenceData( Common.appFenceName ) );
            TheTang.getSingleInstance().deleteStrategeInfo( String.valueOf( OrderConfig.put_down_application_fence ) );

            clearAppFenceData(preferencesManager);
            cancelBrodcast();

        } else {

            TheTang.getSingleInstance().addMessage( String.valueOf( OrderConfig.put_down_application_fence ),
                    appFenceData.name );

            TheTang.getSingleInstance().addStratege( String.valueOf( OrderConfig.put_down_application_fence ),
                    appFenceData.name, System.currentTimeMillis() + "" );

            clearAppFenceData(preferencesManager);
            cancelBrodcast();

            storageAppFenceData(preferencesManager, appFenceData);

            if ("1".equals( appFenceData.noticeMessage )) {
                TheTang.getSingleInstance().addMessage( String.valueOf( OrderConfig.put_down_application_fence_message ),
                        appFenceData.noticeMessageContent );
                TheTang.getSingleInstance().showNotification(appFenceData.noticeMessageContent, mContext.getResources().getString(R.string.appfence_message1), 1001);
            }


            EventBus.getDefault().post(new NotifySafedesk("fulsh"));
            excuteFence( preferencesManager );
        }
    }

    public static void excuteFence(PreferencesManager preferencesManager) {

        mContext.stopService( new Intent( mContext, WatchingAppStartService.class ) );
        mContext.stopService( new Intent( mContext, AppFenceService.class ) );

        String limitType = preferencesManager.getAppFenceData( Common.appFenceLimitType );

        if(limitType == null)
            return;

        switch(Integer.valueOf( limitType )) {

            case 0:

                Intent intent = new Intent( mContext, AppFenceService.class );

                //如果地理围栏与时间围栏同时下发，需同时满足
                if (!TextUtils.isEmpty( preferencesManager.getAppFenceData( Common.appFenceCoordinate ) )) {
                    if (!TextUtils.isEmpty( preferencesManager.getAppFenceData( Common.appFenceStartDateRange ) )) {
                        intent.putExtra( "fenceType","0" );//地理围栏与时间围栏同时满足

                    } else {
                        intent.putExtra( "fenceType","1" );//地理围栏满足
                    }
                } else {
                    if (!TextUtils.isEmpty( preferencesManager.getAppFenceData( Common.appFenceStartDateRange ) )) {
                        intent.putExtra( "fenceType","2" );//时间围栏满足
                    }
                }

                TheTang.getSingleInstance().startService( intent );

                break;
            case 1:

                Log.w( TAG, "允许应用" );
                whetherAppInSide = false;
                cancelBrodcast();
                break;
            case 2:
                whetherAppInSide = true;
                Log.w( TAG, "禁止应用" );
                TheTang.getSingleInstance().startService( new Intent( mContext, WatchingAppStartService.class ) );
                cancelBrodcast();
                break;

        }
    }

    /*private static void cancelFence() {

        //WatchingAppStartService.getSingleInstance().cancelTimer();

        //mContext.stopService( new Intent( mContext, AppFenceService.class ) );
        cancelBrodcast();
    }*/

    private static void storageAppFenceData(PreferencesManager preferencesManager, AppFenceData appFenceData) {

        preferencesManager.setAppFenceData( Common.appFenceName, appFenceData.name );
        preferencesManager.setAppFenceData( Common.appFenceCoordinate, appFenceData.coordinate );
        preferencesManager.setAppFenceData( Common.appFenceRadius, appFenceData.radius );
        preferencesManager.setAppFenceData( Common.appFenceStartDateRange, appFenceData.startDateRange );
        preferencesManager.setAppFenceData( Common.appFenceEndDateRange, appFenceData.endDateRange );
        preferencesManager.setAppFenceData( Common.appFenceNoticeMessage, appFenceData.noticeMessage );
        preferencesManager.setAppFenceData( Common.appFenceNoticeBell, appFenceData.noticeBell );
        preferencesManager.setAppFenceData( Common.appFenceLimitType, appFenceData.limitType );
        preferencesManager.setAppFenceData( Common.appFenceMessageContent, appFenceData.noticeMessageContent );

        if (appFenceData.timeUnit != null) {
            String json = new Gson().toJson( appFenceData.timeUnit  );
            preferencesManager.setAppFenceData( Common.timeUnit, json );
        } else {
            preferencesManager.setAppFenceData( Common.timeUnit, null );
        }

        List<String> packageNames = appFenceData.packageNames;
        String packages = null;
        if (packageNames != null) {
            for ( String packageName : packageNames) {
                if (packages == null) {
                    packages = packageName;
                } else {
                    packages = packages + "," + packageName;
                }
            }
        }

        preferencesManager.setAppFenceData( Common.appFenceAppPageName, packages );
    }

    private static void clearAppFenceData(PreferencesManager preferencesManager) {
        preferencesManager.clearAppFenceData();
    }

    public static void cancelBrodcast() {
        Intent intent_CancleReceiver = new Intent( );
        PendingIntent pendingIntent = PendingIntent.getBroadcast( TheTang.getSingleInstance().getContext(), 1, intent_CancleReceiver, PendingIntent.FLAG_UPDATE_CURRENT );
        AlarmManager alarmManager = (AlarmManager) TheTang.getSingleInstance().getContext().getSystemService( ALARM_SERVICE );
        alarmManager.cancel( pendingIntent );
    }

}

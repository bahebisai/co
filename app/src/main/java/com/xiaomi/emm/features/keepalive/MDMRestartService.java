package com.xiaomi.emm.features.keepalive;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Created by Administrator on 2018/1/15.
 */

public class MDMRestartService extends NotificationListenerService {
    public final static String TAG = "MDMRestartService";
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted( sbn );
        Log.w(TAG,"onNotificationPosted");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved( sbn );
        Log.w(TAG,"onNotificationRemoved");
    }
}

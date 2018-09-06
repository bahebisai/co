package com.xiaomi.emm.features;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.xiaomi.emm.features.lockscreen.NewsLifecycleHandler;
import com.xiaomi.emm.utils.LogUtil;

/**
 * Created by Administrator on 2017/8/30.
 */

public class ScreenOffReceiver extends BroadcastReceiver {

    final static String TAG = "ScreenOffReceiver";
    PowerManager.WakeLock wakeLock = null;
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals( Intent.ACTION_SCREEN_OFF )) {
            NewsLifecycleHandler.LockFlag = true ;
            acquireWakeLock(context);
        }

        if (intent.getAction().equals( Intent.ACTION_SCREEN_ON )) {
            releaseWakeLock();
        }
    }

    //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    private void acquireWakeLock(Context context) {
        LogUtil.writeToFile( TAG, "acquireWakeLock" );
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) context.getSystemService( Context.POWER_SERVICE );
            wakeLock = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "ScreenOffReceiver" );
            if (null != wakeLock) {
                wakeLock.acquire();
            }
        }
    }

    //释放设备电源锁
    private void releaseWakeLock() {
        if (null != wakeLock) {
            wakeLock.release();
            wakeLock = null;
        }
    }
}

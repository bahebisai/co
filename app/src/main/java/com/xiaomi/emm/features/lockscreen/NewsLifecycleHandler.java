package com.xiaomi.emm.features.lockscreen;

/**
 * Created by EdisonXu on 2017/4/11.
 */

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.features.manager.PreferencesManager;
import com.xiaomi.emm.features.presenter.TheTang;
import com.xiaomi.emm.view.activity.SafeDeskActivity;

import java.util.List;


/**
 * 用于判断App是否在后台
 */
public class NewsLifecycleHandler implements Application.ActivityLifecycleCallbacks {

    // I use four separate variables here. You can, of course, just use two and
    // increment/decrement them instead of using four and incrementing them all.
    public static int resumed;
    public static int paused;
    private static int started;
    private static int stopped;
    public static boolean LockFlag = true;
    private Activity activity;
    private final static String TAG = "NewsLifecycleHandler";

    public static boolean setFlag = false;

    public NewsLifecycleHandler() {
        resetVariables();
    }

    public void resetVariables() {
        resumed = 0;
        paused = 0;
        started = 0;
        stopped = 0;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.w( TAG, activity.getClass().getSimpleName() + " is in onActivityCreated:-1---" );
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.w( TAG, activity.getClass().getSimpleName() + " is in onActivityDestroyed: " );
        activity = null;
    }

    //除了第一次登陆成功后，然后就开始锁屏
    @Override
    public void onActivityResumed(Activity activity) {
        this.activity = activity;
        ++resumed;

        //LogUtil.writeToFile( TAG, activity.getClass().getSimpleName() + " is in resumed: " + (resumed) );
        //LogUtil.writeToFile( TAG, activity.getClass().getSimpleName() + " is in resumed LockFlag=: " + LockFlag );

        if (LockFlag
                && !TextUtils.isEmpty( PreferencesManager.getSingleInstance().getLockPassword( "password" ) )
                && TheTang.getSingleInstance().getUsageStats()) {

            //LogUtil.writeToFile( TAG, activity.getClass().getSimpleName() + " is in resumed LockFlag=2: " + LockFlag );

            Log.w( TAG, activity.getClass().getSimpleName() + " is in : ===---" );
            //如果进入的界面不是保护界面，则进入
            String currentActivity = activity.getClass().getSimpleName();
            if (!currentActivity.equals( Lock2Activity.class.getSimpleName()) && !currentActivity.equals(SafeDeskActivity.class.getSimpleName())) {

                Log.w( TAG, activity.getClass().getSimpleName() + " is in resumed: ===setFlag-然后就开始锁屏--=-" + PreferencesManager.getSingleInstance().getLockFlag( "LockFlag" ) );

                Intent intent = new Intent( activity, Lock2Activity.class );//LockScreenActivity
                intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );//这个flags表示如果已经有这个
                activity.startActivity( intent );
            } else {
                Log.w( TAG, activity.getClass().getSimpleName() + " 当前为锁屏" + Lock2Activity.class.getSimpleName() );
                LogUtil.writeToFile( TAG, activity.getClass().getSimpleName() + " 当前为锁屏=====" + Lock2Activity.class.getSimpleName() );
            }
        }

        LockFlag = false;
        PreferencesManager.getSingleInstance().removeLockFlag( "LockFlag" );
        //LogUtil.writeToFile( TAG, activity.getClass().getSimpleName() + " is in resumed LockFlag=3: " + LockFlag );

    }

    @Override
    public void onActivityPaused(Activity activity) {
        ++paused;
        Log.w( TAG, activity.getClass().getSimpleName() + " is in foreground: " + !(resumed > paused) );

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        Log.w( TAG, activity.getClass().getSimpleName() + " is in onActivitySaveInstanceState: " );
        // outState.putBoolean("setFlag",true);
    }


    @Override
    public void onActivityStarted(Activity activity) {
        ++started;
        LogUtil.writeToFile( TAG, activity.getClass().getSimpleName() + " is onActivityStarted: " + (started) );
    }

    @Override
    public void onActivityStopped(Activity activity) {

        ++stopped;
        Log.w( TAG, activity.getClass().getSimpleName() + " is visible: " + !(started > stopped) );
        LogUtil.writeToFile( TAG, activity.getClass().getSimpleName() + " is visible: stopped=" + stopped );

        ActivityManager mActivityManager = (ActivityManager) activity.getSystemService( Context.ACTIVITY_SERVICE );
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks( 1 );

    }

    // If you want a static function you can use to check if your application is
    // foreground/background, you can use the following:


    public static boolean isApplicationVisible() {
        return started > stopped;
    }

    public static boolean isApplicationInForeground() {
        return resumed > paused;
    }

    public static boolean isApplicationInBackground() {
        return started == stopped;
    }

    public Activity getActivity() {
        return activity;
    }

    public String getActivityName() {
        return activity.getClass().getSimpleName();
    }

    public boolean isSameClassName(String classSimpleName) {
        if (activity != null) {

            return classSimpleName.equals( activity.getClass().getSimpleName() );
        }

        return false;


    }

}
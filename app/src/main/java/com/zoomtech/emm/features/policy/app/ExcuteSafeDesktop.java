package com.zoomtech.emm.features.policy.app;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.features.event.NotifySafedesk;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.view.activity.SafeDeskActivity;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by lenovo on 2017/8/24.
 */

public class ExcuteSafeDesktop {

    private static final String TAG = "ExcuteSafeDesktop";

    public static void excute_SafeDesktop() {
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        if (preferencesManager == null) {
            LogUtil.writeToFile(TAG, "preferencesManager 为空");
            return;
        }

/*        *//**允许通知*//*
        if (!TextUtils.isEmpty(preferencesManager.getSafedesktopData("allowNotice")) && "1".equals(preferencesManager.getSafedesktopData("allowNotice"))) {
            Log.w(TAG, "setFlag=====不允许通知");
            MDM.mMDMController.disableDropdown();
        } else {
            Log.w(TAG, "setFlag=====允许通知");
            MDM.mMDMController.enableDropdown();
        }*/

        //默认安全桌面--工作台--把三个虚拟机隐藏掉
        if (!TextUtils.isEmpty(preferencesManager.getSafedesktopData("code"))) {
            MDM.getSingleInstance().disableDropdown();
            //不锁屏的标志
            PreferencesManager.getSingleInstance().setLockFlag( "unLockScreen", true );
            Context context = TheTang.getSingleInstance().getContext();
            Intent intent = new Intent(context, SafeDeskActivity.class);
            context.startActivity(intent);
            EventBus.getDefault().post(new NotifySafedesk(Common.safeActicivty_flush));
            //如果时前台不需要执行，如果时后台
        }
    }

    public static void excute_IntentWorkFragment() {
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        if (preferencesManager == null) {
            Log.w(TAG, "preferencesManager 为空");
            return;
        }

        /**允许通知*/
        if (TextUtils.isEmpty(preferencesManager.getSafedesktopData("allowNotice")) || "0".equals(preferencesManager.getSafedesktopData("allowNotice"))) {
            Log.w(TAG, "=====允许下拉通知栏");
            MDM.getSingleInstance().enableDropdown();
        }

        //转到工作台
        if (TextUtils.isEmpty(preferencesManager.getSafedesktopData("code"))) {
            EventBus.getDefault().post(new NotifySafedesk(Common.safeActicivty_finsh));
        }
    }

    enum StatusBarCmd {
        //禁止下拉
        DISABLE_EXPAND,
        //解除下拉
        DISABLE_NONE,
        DISABLE_RECENT,
        DISABLE_NOTIFICATION_ICONS
    }

    private static void disableStatusBar(StatusBarCmd cmd) {
        Context context = TheTang.getSingleInstance().getContext();
        try {
            Object service = context.getSystemService("statusbar");
            Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
            Method disable = statusbarManager.getMethod("disable", int.class);
            disable.setAccessible(true);
            Field disable_expand = statusbarManager.getField(cmd.name());
            disable_expand.setAccessible(true);
            int disable_code = disable_expand.getInt(statusbarManager);
            Log.w(TAG, "disable code = " + disable_code);
            disable.invoke(service, disable_code);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

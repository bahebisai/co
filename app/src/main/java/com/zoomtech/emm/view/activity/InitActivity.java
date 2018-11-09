package com.zoomtech.emm.view.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.zoomtech.emm.R;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.features.excute.MDMOrderService;
import com.zoomtech.emm.features.keepalive.JobSchedulerManager;
import com.zoomtech.emm.features.keepalive.PlayerMusicService;
import com.zoomtech.emm.utils.ActivityCollector;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.view.listener.PermissionListener;

import java.util.HashMap;
import java.util.List;

/**
 * 初始化界面，判断应用是否已经登录
 * 1、登录 跳转到MainActivity
 * 2、未登录 跳转到LoginActivity
 * Created by Administrator on 2017/6/1.
 */

public class InitActivity extends BaseActivity {
    private final static String TAG = "InitActivity";

    private JobSchedulerManager mJobManager;

    String[] mPermissions = new String[]{

            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_SMS,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.CALL_PHONE,

            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,

            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,

            Manifest.permission.CAMERA,

            Manifest.permission.CHANGE_WIFI_STATE,

            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,

            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CALL_LOG
    };

    private void initActivity() {
        final PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        //判断是否已经登录过
        boolean whetherLogin = false;
        whetherLogin = preferencesManager.getData( Common.token ) != null && preferencesManager.getData( Common.alias ) != null;

        if (whetherLogin) {
            //是否已经设置应用锁
            String password = preferencesManager.getLockPassword( "password" );
            if (TextUtils.isEmpty( password )) {
                openActivity( FirstScreenPasswordActivity.class );
            } else {
                HashMap<Class<?>, Activity> activitys = ActivityCollector.getActivity();
                if (activitys != null ||  activitys.size() < 2) {
                    TheTang.getSingleInstance().startService(new Intent(InitActivity.this, MDMOrderService.class) );
                    displayDesktop( preferencesManager );
                }
            }
        } else {
            openActivity( LoginActivity.class );
        }

        finish();
    }

    /**
     * 启动EMM显示的界面判断
     *
     * @param preferencesManager
     */
    private void displayDesktop(PreferencesManager preferencesManager) {

        if (!TextUtils.isEmpty( preferencesManager.getSecurityData( Common.safetyTosecureFlag ) )) {
            if (!TextUtils.isEmpty( preferencesManager.getSecurityData( Common.secureDesktopFlag ) )) {
                //防止手机在安全局域下的安全桌面的中，手机关机重启
                openActivity( SafeDeskActivity.class );
            } else {
                openActivity( MainActivity.class );
            }

        } else if (TextUtils.isEmpty( preferencesManager.getFenceData( Common.insideAndOutside ) ) ||
                "false".equals( preferencesManager.getFenceData( Common.insideAndOutside ) )) {

            if (!TextUtils.isEmpty( preferencesManager.getSafedesktopData( "code" ) )) {
                Log.w( TAG, preferencesManager.getSafedesktopData( "code" ) + "---隐藏虚拟机" );
                Intent intent = new Intent( this, SafeDeskActivity.class );
                startActivity( intent );
            } else {
                Intent intent = new Intent( this, MainActivity.class );
                startActivity( intent );

                MDM.enableFingerNavigation(true);
                MDM.setRecentKeyVisible( true );
                MDM.setHomeKeyVisible( true );
            }
        } else if (
                preferencesManager.getFenceData( Common.insideAndOutside ) != null && "true".
                        equals( preferencesManager.getFenceData( Common.insideAndOutside ) )) {

            if (!TextUtils.isEmpty( preferencesManager.getFenceData( Common.setToSecureDesktop ) ) &&
                    !"2".equals( preferencesManager.getFenceData( Common.setToSecureDesktop ) )) {
                Intent intent = new Intent( this, SafeDeskActivity.class );
                startActivity( intent );
            } else if ((TextUtils.isEmpty( preferencesManager.getFenceData( Common.setToSecureDesktop ) ) ||
                    "2".equals( preferencesManager.getFenceData( Common.setToSecureDesktop ) ))) {
                Intent intent = new Intent( this, MainActivity.class );
                startActivity( intent );
            }
        }else {
            Intent intent = new Intent( this, MainActivity.class );
            startActivity( intent );

        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_init;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1101) {
            if (!hasPermission()) {
                //若用户未开启权限，则引导用户开启“Apps with usage access”权限
                Toast.makeText(this, getResources().getString(R.string.usage_access), Toast.LENGTH_LONG ).show();
                finish();
            }
        }
    }

    //检测用户是否对本app开启了Apps with usage access权限
    private boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager)
                getSystemService( Context.APP_OPS_SERVICE );
        int mode = 0;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            mode = appOps.checkOpNoThrow( AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), getPackageName() );
        }
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!hasPermission()) {
            Toast.makeText( TheTang.getSingleInstance().getContext(), getResources().getString(R.string.usage_permission_tip), Toast.LENGTH_LONG ).show();

            startActivityForResult( new Intent( Settings.ACTION_USAGE_ACCESS_SETTINGS ), 1101 );
        } else {

            //启动JobScheduler
            //mJobManager = JobSchedulerManager.getJobSchedulerInstance( this );
            //mJobManager.startJobScheduler();

            //启动播放音乐Service
            //startPlayMusicService();

            requestRuntimePermissions( mPermissions, new PermissionListener() {
                @Override
                public void onGranted() {
                    initActivity();
                }

                @Override
                public void onDenied(List<String> permissions) {
                    Toast.makeText( InitActivity.this, getResources().getString(R.string.permissions_tips, permissions.toString()), Toast.LENGTH_LONG ).show();
                    finish();
                }
            } );
        }
    }

    private void startPlayMusicService() {
        Intent intent = new Intent( this, PlayerMusicService.class );
        TheTang.getSingleInstance().startService( intent );
    }

}

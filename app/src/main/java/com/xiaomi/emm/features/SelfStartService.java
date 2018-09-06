package com.xiaomi.emm.features;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.xiaomi.emm.utils.LogUtil;

/**
 * Created by Administrator on 2017/8/31.
 */

public class SelfStartService extends IntentService {
    final static String TAG = "SelfStartService";
    public SelfStartService( ) {
        super( "SelfStartService" );
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtil.writeToFile( TAG, "SelfStartService onHandleIntent!" );
        PackageManager packageManager = this.getPackageManager();
        Intent startIntent = packageManager.getLaunchIntentForPackage( getPackageName() );
        if (startIntent != null) {
            this.startActivity(startIntent);
        }
    }
}

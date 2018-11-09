package com.zoomtech.emm.features.policy.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.TheTang;

/**
 * Created by Administrator on 2017/6/20.
 */

public class AppReceiver extends BroadcastReceiver {
    public String TAG = "AppReceiver";

    String packageName = null;
    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        packageName = intent.getData().getSchemeSpecificPart();

        if (packageName == null || packageName.length() == 0) {
            // they sent us a bad intent
            LogUtil.writeToFile( TAG, "They sent us a bad intent！" );
            return;
        }

        Intent intentService = new Intent(  );
        intentService.putExtra( "packageName", packageName );
        LogUtil.writeToFile( TAG, "action = " + intent.getAction());
        intentService.putExtra( "action", intent.getAction() );
        intentService.setClass( mContext, AppIntentService.class );
        TheTang.getSingleInstance().startService( intentService );
    }
}

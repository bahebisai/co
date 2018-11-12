package com.zoomtech.emm.features.policy.compliance;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.zoomtech.emm.R;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.features.presenter.TheTang;

/**
 * Created by Administrator on 2017/8/8.
 */

public class SystemComplianceService extends Service {

    private final static String TAG = "SystemComplianceService";
    SystemComplianceReceiver mSystemComplianceReceiver = null;

    @Override
    public void onCreate() {
        super.onCreate();
        TheTang.getSingleInstance().startForeground(this,getResources().getString(R.string.system_service),"EMM",4);

        LogUtil.writeToFile(TAG,"SystemComplianceService onCreate!");
        //注册sd卡监听
        mSystemComplianceReceiver = new SystemComplianceReceiver();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        mIntentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        mIntentFilter.addDataScheme("file");
        registerReceiver(mSystemComplianceReceiver,mIntentFilter);

        //防止关机换卡
        MDM.getSingleInstance().mountSDCard();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSystemComplianceReceiver);
        TheTang.getSingleInstance().cancelNotification(4);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

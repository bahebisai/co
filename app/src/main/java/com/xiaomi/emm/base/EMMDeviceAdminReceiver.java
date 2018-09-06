package com.xiaomi.emm.base;

import android.content.Context;
import android.content.Intent;

import com.xiaomi.emm.R;

/**
 * Created by Administrator on 2017/12/4.
 */

public class EMMDeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDisabled(Context context, Intent intent) {

    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        // TODO Auto-generated method stub
        return context.getString(R.string.disable_warning);
    }


    


}
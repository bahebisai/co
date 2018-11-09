package com.zoomtech.emm.features;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.features.impl.SendMessageManager;
import com.zoomtech.emm.model.MessageSendData;
import com.zoomtech.emm.utils.JsonGenerateUtil;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.utils.PhoneUtils;
import com.zoomtech.emm.features.manager.PreferencesManager;

import java.text.SimpleDateFormat;

/**
 * Created by Administrator on 2017/9/12.
 */

public class SwitchLogReceiver extends BroadcastReceiver {

    final static String TAG = "SwitchLogReceiver";
    String logKey = "switchLog";
    String otherKey = "switchByOrder";

    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    public void onReceive(Context context, Intent intent) {

        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

        String switchLog = preferencesManager.getLogData("switchLog"); //记录
        //前台切换到后台
        if ("com.android.server.vp_switch_start".equals(intent.getAction())) {

            LogUtil.writeToFile( TAG, "前台切换到后台" );
            if ( switchLog == null ) {
                if (preferencesManager.getOtherData( otherKey ) != null) {
                    preferencesManager.setLogData( logKey,
                            mSimpleDateFormat.format( System.currentTimeMillis() ) + "/1/" + "安全域切换到生活域" );
                    preferencesManager.removeOtherData( otherKey );
                } else {
                    //LogUtil.writeToFile( TAG, "前台切换到后台" );
                    preferencesManager.setLogData( logKey,
                            mSimpleDateFormat.format( System.currentTimeMillis() ) + "/0/" + "安全域切换到生活域" );
                }

            } else {
                if (preferencesManager.getOtherData( otherKey ) != null) {
                    preferencesManager.setLogData( logKey, switchLog + ","
                            + mSimpleDateFormat.format( System.currentTimeMillis() ) + "/1/" +  "安全域切换到生活域" );
                    preferencesManager.removeOtherData( otherKey );
                } else {
                    preferencesManager.setLogData( logKey, switchLog + ","
                            + mSimpleDateFormat.format( System.currentTimeMillis() ) + "/0/" + "安全域切换到生活域" );
                }
            }
        }

        //后台切换到前台
        if ("com.android.server.back_vpswitch_end".equals(intent.getAction())) {

            LogUtil.writeToFile( TAG, "后台切换到前台" );
            if ( switchLog == null ) {
                if (preferencesManager.getOtherData( otherKey ) != null) {
                    preferencesManager.setLogData( logKey,
                            mSimpleDateFormat.format( System.currentTimeMillis() ) + "/1/" + "生活域切换到安全域" );
                    preferencesManager.removeOtherData( otherKey );
                } else {
                    //LogUtil.writeToFile( TAG, "后台切换到前台" );
                    preferencesManager.setLogData( logKey,
                            mSimpleDateFormat.format( System.currentTimeMillis() ) + "/0/" + "生活域切换到安全域" );
                }
            } else {
                if (preferencesManager.getOtherData( otherKey ) != null) {
                preferencesManager.setLogData( logKey, switchLog + ","
                        + mSimpleDateFormat.format( System.currentTimeMillis() ) + "/1/" + "生活域切换到安全域" );
                preferencesManager.removeOtherData( otherKey );
            } else {
                preferencesManager.setLogData( logKey,switchLog + "," +
                        mSimpleDateFormat.format( System.currentTimeMillis() ) + "/0/" + "生活域切换到安全域" );
            }
        }
        }

        upLoadSwitchLog(context);
    }

    private void upLoadSwitchLog( Context context) {
        int state = PhoneUtils.getNetWorkState(context);
        if ( state == 0 || state == 1) {
            sendSwitchLog(context);
        }
    }

    /**
     * 发送switch log并清除log
     * @param context
     */
    private void sendSwitchLog(Context context) {
        String switchLog = PreferencesManager.getSingleInstance().getLogData("switchLog");
        if (switchLog != null) {
/*            SwitchLogImpl mSwitchLogImpl = new SwitchLogImpl(context);
            mSwitchLogImpl.sendSwitchLog(switchLog);*/

            //todo baii impl aaaaaaaaaaaaaaaaaaaaaaa
            String logJsonString =  JsonGenerateUtil.jsonSwitchLog(switchLog);
            MessageSendData data = new MessageSendData(Common.switch_log_impl, logJsonString, false);
            SendMessageManager manager = new SendMessageManager(context);
            manager.setSendListener(new SendMessageManager.SendListener() {
                @Override
                public void onSuccess() {
                    PreferencesManager.getSingleInstance().removeLogData("switchLog");//清除数据
                }

                @Override
                public void onFailure() {

                }

                @Override
                public void onError() {

                }
            });
            manager.sendMessage(data);
        }
    }
}

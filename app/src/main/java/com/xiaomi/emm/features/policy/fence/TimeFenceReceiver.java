package com.xiaomi.emm.features.policy.fence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.xiaomi.emm.features.lockscreen.TimeFenceService;
import com.xiaomi.emm.utils.TheTang;

/**
 * Created by lenovo on 2017/8/30.
 */

public class TimeFenceReceiver extends BroadcastReceiver {
    private static final String TAG ="TimeFenceReceiver" ;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() == null) {
            Log.w(TAG, "到这里闹钟AlarmReceiver1===intent.getAction()为空");
            return;
        }
        Log.w(TAG,"时间围栏接收过来的广播"+intent.getAction().toString());
        Intent i=new Intent(context,TimeFenceService.class);
        if("startTimeRage".equals(intent.getAction())) {

            i.putExtra("TimeFenceReceiver",intent.getAction().toString());

        }else  if("endTimeRage".equals(intent.getAction())) {

            i.putExtra("TimeFenceReceiver",intent.getAction().toString());

        }else if("alarm_start".equals(intent.getAction())){

            i.putExtra("TimeFenceReceiver",intent.getAction().toString());

        }else if("alarm_end".equals(intent.getAction())){

            i.putExtra("TimeFenceReceiver",intent.getAction().toString());
        }
        TheTang.getSingleInstance().startService(i);
    }
}

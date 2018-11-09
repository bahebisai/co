package com.zoomtech.emm.features.lockscreen.flowreceive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;

import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by lenovo on 2017/8/15.
 */

public class FlowTotalReceiver extends BroadcastReceiver {

    public static boolean isShutDown = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.writeToFile("FlowTotalReceiver",Intent.ACTION_SHUTDOWN);

        if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
            TheTang.getSingleInstance().isLostCompliance(false);

            isShutDown = true;
       /* //1.获取一个包管理器。
        PackageManager pm = TheTang.getSingleInstance().getPackageManager();
        //2.遍历手机操作系统 获取所有的应用程序的uid
        List<ApplicationInfo> appliactaionInfos = pm.getInstalledApplications(0);
        for(ApplicationInfo applicationInfo : appliactaionInfos){
            int uid = applicationInfo.uid;    // 获得软件uid
            //proc/uid_stat/10086
            long tx = TrafficStats.getUidTxBytes(uid);//发送的 上传的流量byte
            long rx = TrafficStats.getUidRxBytes(uid);//下载的流量 byte
            //方法返回值 -1 代表的是应用程序没有产生流量 或者操作系统不支持流量统计
        }*/
        long mobileTxBytes = TrafficStats.getMobileTxBytes();//获取手机3g/2g网络上传的总流量
        long mobileRxBytes = TrafficStats.getMobileRxBytes();//手机2g/3g下载的总流量

        long l = mobileTxBytes + mobileRxBytes;

        long totalTxBytes = TrafficStats.getTotalTxBytes();//手机全部网络接口 包括wifi，3g、2g上传的总流量
        long totalRxBytes = TrafficStats.getTotalRxBytes();//手机全部网络接口 包括wifi，3g、2g下载的总流量
        long l1 = totalTxBytes + totalRxBytes;
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String last = format.format(ca.getTime());
        l=l+preferencesManager.getTraffictotal("mobileTotal");

        l1=l1+preferencesManager.getTraffictotal("trafficTotal");
        //action.ACTION_SHUTDOWN

            preferencesManager.setTraffictotal("mobileTotal",l);
            preferencesManager.setTraffictotal("trafficTotal",l1);
        }
    }
}

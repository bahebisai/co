package com.xiaomi.emm.features.policy.fence;

import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.xiaomi.emm.definition.OrderConfig;
import com.xiaomi.emm.features.presenter.MDM;
import com.xiaomi.emm.features.presenter.TheTang;
import com.xiaomi.emm.model.WifiFenceData;
import com.xiaomi.emm.utils.DataParseUtil;
import com.xiaomi.emm.utils.WifiHelper;

import java.util.List;

public class WifiFenceManager {
    private static final int MSG_SCAN_WIFI = 0;
    private static final int SCAN_INTERVAL = 35000;

    private boolean mHasSwitchToInFence;

    private WifiFenceData mWifiFenceData;
    private List<WifiFenceData.WifiPolicyBean.WifiBean> mWifiBeans;
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private WifiFenceManager() {
//        init();
    }

    private static class SingletonHolder {
        public static WifiFenceManager instance = new WifiFenceManager();
    }

    public static WifiFenceManager newInstance() {
        return WifiFenceManager.SingletonHolder.instance;
    }

    public void exeWifiFence(String json, boolean isFromOrder) {
        mWifiFenceData = DataParseUtil.jsonToData(WifiFenceData.class, json);
        mWifiBeans = mWifiFenceData.getPolicy().get(0).getWifiBean();

        startWifiScanThread();

        if (isFromOrder) {
            TheTang.getSingleInstance().addMessage(String.valueOf(OrderConfig.SEND_WIFI_FENCE), mWifiFenceData.getPolicy().get(0).getName());//todo baii ??? list
            TheTang.getSingleInstance().addStratege(String.valueOf(OrderConfig.SEND_WIFI_FENCE), mWifiFenceData.getPolicy().get(0).getName(), System.currentTimeMillis() + "");
        }
    }

    public void exeDeleteWifiFence() {
        Log.d("baii", "delete wifi fence");
        stopWifiScanThread();
        TheTang.getSingleInstance().addMessage(String.valueOf(OrderConfig.DELETE_WIFI_FENCE), mWifiFenceData.getPolicy().get(0).getName());//todo baii ??? list
        TheTang.getSingleInstance().deleteStrategeInfo(String.valueOf(OrderConfig.SEND_WIFI_FENCE), mWifiFenceData.getPolicy().get(0).getName());
    }

    private void saveToSP() {
        //todo baii wifi
    }

    private void startWifiScanThread() {
        MDM.forceLocationService();//扫描WiFi需要打开定位, todo baii reset to default
        if (mHandlerThread == null) {
            mHandlerThread = new HandlerThread("wifi_fence");
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    mHandler.sendEmptyMessageDelayed(MSG_SCAN_WIFI, SCAN_INTERVAL);

                    boolean isInWifiFence = isInWifiFence(mWifiBeans);
                    Log.d("baii", "is in " + isInWifiFence);
                    if (isInWifiFence != mHasSwitchToInFence) {//检测到在WiFi围栏内但还没有手机还没有切换到围栏内或相反
                        exeWifiFencePolicy(isInWifiFence);
                        mHasSwitchToInFence = isInWifiFence;
                    }
                }
            };
            mHandler.sendEmptyMessage(MSG_SCAN_WIFI);
        }
    }

    public void stopWifiScanThread() {
        if (mHandlerThread != null && mHandlerThread.isAlive()) {
            mHandlerThread.quitSafely();
            mHandlerThread = null;
        }
    }

    private boolean isInWifiFence(List<WifiFenceData.WifiPolicyBean.WifiBean> wifiBeans) {
        if (wifiBeans == null) {
            return false;
        }
        List<ScanResult> results = WifiHelper.getWifiList();
        for (ScanResult result : results) {
            for (WifiFenceData.WifiPolicyBean.WifiBean wifiBean : wifiBeans) {
                if (result.SSID.equals(wifiBean.getSsid()) && result.BSSID.equals(wifiBean.getMacAddress())) {
                    Log.d("baii", "ssid " + result.SSID);//wifi名字，eg.thetang2.4
                    Log.d("baii", "bssid " + result.BSSID);//被连接WiFi的mac地址
                    return true;
                }
            }
        }
        return false;
    }

    private void exeWifiFencePolicy(boolean isInFence) {
        Log.d("baii", "exe wifi policy in fence " + isInFence);
    }
}

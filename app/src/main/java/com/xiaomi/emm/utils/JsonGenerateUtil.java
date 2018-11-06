package com.xiaomi.emm.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.manager.PreferencesManager;
import com.xiaomi.emm.features.presenter.MDM;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

/**
 * 生成返回给后台的Json数据类
 */
public class JsonGenerateUtil {
    /**
     * 解析Switch Log
     *
     * @param log
     * @return
     */
    public static String jsonSwitchLog(String log) {
        JSONObject logObject = new JSONObject();
        try {
            logObject.put("alias", PreferencesManager.getSingleInstance().getData(Common.alias));
            JSONArray jsonArray = new JSONArray();
            String[] logList = log.split(",");
            for (int i = 0; i < logList.length; i++) {
                JSONObject switchLog = new JSONObject();
                String[] logString = logList[i].split("/");
                switchLog.put("create_time", logString[0]);
                switchLog.put("type", logString[1]);
                switchLog.put("switch_direction", logString[2]);
                jsonArray.put(switchLog);
            }
            logObject.put("list", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return logObject.toString();
    }

    /**
     * 获取所有的设备信息
     *
     * @param context
     * @return json string
     */
    public static String getDeviceInfoString(Context context) {
        long storage = (long) DeviceUtils.getRemainStorage();
        String rom_total_capacity = ConvertUtils.formatFileSize((long) DeviceUtils.getTotalStorage());
        String rom_available_capacity = ConvertUtils.formatFileSize(storage);
        JSONObject deviceObject = new JSONObject();
        try {
            deviceObject.put(Common.alias, PreferencesManager.getSingleInstance().getData(Common.alias));
            deviceObject.put("device_type", 0);
            deviceObject.put("rom_total_capacity", rom_total_capacity);
            deviceObject.put("rom_available_capacity", rom_available_capacity);
            deviceObject.put("device_model", DeviceUtils.getDeviceModel());
            deviceObject.put("device_name", DeviceUtils.getDeviceName());
            deviceObject.put("apps", getLauncherAppsJson(context));
            deviceObject.put("app_security_password", PreferencesManager.getSingleInstance().getLockPassword("password"));
            deviceObject.put("bluetooth_mac_address", "");//todo baii 蓝牙地址获取，当前代码无方法
            deviceObject.put("boot_time", DeviceUtils.getBootTime());
            deviceObject.put("camera", DeviceUtils.getCameraPixelInDB(0));//优先从数据库中获取
            deviceObject.put("cpu", DeviceUtils.getCpu());
            deviceObject.put("is_root", DeviceUtils.isDeviceRooted() ? "1" : "0");
            deviceObject.put("power_status", PhoneUtils.getPowerStatus(context) + "%");
            deviceObject.put("sd_card_available_capacity", DeviceUtils.getSdCardAvailableCapacity());
            deviceObject.put("sd_card_total_capacity", DeviceUtils.getSdCardTotalCapacity());
            deviceObject.put("sd_card_serial_number", DeviceUtils.getSDCardUUID(context));
            deviceObject.put("udid", DeviceUtils.getUUIDinDB());
            deviceObject.put("wifi_mac", PhoneUtils.getMacAddr());
            deviceObject.put("client_last_update_time", PreferencesManager.getSingleInstance().getOtherData("client_last_update_time"));// 获得上次客户端安装时间
            deviceObject.put("main_operator_info", PhoneUtils.getCarrierName()[0]);
            deviceObject.put("vice_operator_info", PhoneUtils.getCarrierName()[1]);
            deviceObject.put("main_phone_number", PhoneUtils.getLine1Number(context)[0]);
            deviceObject.put("vice_phone_number", PhoneUtils.getLine1Number(context)[1]);

            List<String> deviceInfo = MDM.getDeviceInfo();
            if (deviceInfo != null && deviceInfo.size() > 0) {
                for (int i = 0; i < (deviceInfo.size() >= Common.deviceInfo1.length ? Common.deviceInfo1.length : deviceInfo.size()); i++) {
                    deviceObject.put(Common.deviceInfo1[i], deviceInfo.get(i));
                }
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    String mobileData = DataFlowStatsHelper.getFlowData();//querySummaryForDevice, must be used in main thread for xiaom
                    if (mobileData != null) {
                        String[] datas = mobileData.split(",");
                        for (int i = 0; i < datas.length; i++) {
                            try {
                                deviceObject.put(Common.mobileDatas[i], datas[i]);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return deviceObject.toString();
    }

    /**
     * 获得所有桌面应用信息，返回JSONArray
     *
     * @return
     */
    public static JSONArray getLauncherAppsJson(Context context) {
        Set<String> launcherApps = AppUtils.getUniqueLauncherApps();
        PackageManager mPackageManager = context.getPackageManager();
        JSONArray appArray = new JSONArray();
        for (String packageName : launcherApps) {
            JSONObject appObject = new JSONObject();
            try {
                PackageInfo packageInfo = null;
                try {
                    packageInfo = mPackageManager.getPackageInfo(packageName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                appObject.put("version", packageInfo.versionName);
                appObject.put("size", AppUtils.getAppSize(context, packageName));
                appObject.put("name", packageInfo.applicationInfo.loadLabel(mPackageManager));
                appObject.put("package_name", packageName);
                appObject.put("appId", AppUtils.getAppId(packageName));
                appObject.put("last_update_tim", packageInfo.lastUpdateTime);
                appObject.put("type", AppUtils.getAppType(packageInfo));
                appArray.put(appObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return appArray;
    }
}

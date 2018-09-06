package com.xiaomi.emm.model;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/8.
 */

public class GeographicalFenceData {
    public String geographical_fence;
    public String geographical_fence_name;
    //public String fence_descripution;
    public String fence_longitude;
    public String fence_latitude;
    public String radius;
    public String geo_id;

    //设备配置
    //public class Device {
    public String allowDevice;
    public String lockScreen;
    public String lockPassword;
    public String allowMobileData;
    public String allowOpenWifi;
    public String allowCloseWifi;
    public String allowConfigureWifi;
    public String configureWifi;
    public String allowAutomaticJoin;
    public String hiddenNetwork;
    public String allowCamera;
    public String allowBluetooth;
    public String allowContainSwitching;
    public String safeType;
    public String ssid;
    public String wifiPassword;

    public String mobileHotspot;
    public String locationService;
    public String matTransmission;
    public String shortMessage;
    public String soundRecording;

    public String banScreenshot;
    public String allowDropdown;
    public String allowReset;
    public String allowNFC;
    public String allowModifySystemtime;

    public String telephone;
    public String telephoneWhiteList;
    //}

    //安装浏览器
    //public class SecurityChrome {
    public String allowChrome;
    public List<String> webPageList;
    //}

    //安全桌面
    //public class CustomizeDesktop {
    public String allowDesktop;
    public String displayCall;
    public String displayContacts;
    public String displayMessage;
    public List<String> app_list;
    public  String setToSecureDesktop;
    public  String json_Apploication;
    //}

    //双域
    //public class DoubleDomain {
    public String allowDoubleDomain;
    public String twoDomainControl;
    //}
}

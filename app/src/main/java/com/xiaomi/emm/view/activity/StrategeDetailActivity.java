package com.xiaomi.emm.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaomi.emm.R;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.OrderConfig;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.policy.phoneCall.CallRecorderManager;
import com.xiaomi.emm.features.policy.sms.SmsManager;
import com.xiaomi.emm.model.ClearDeskData;
import com.xiaomi.emm.model.ConfigureStrategyData;
import com.xiaomi.emm.model.TimeFenceData;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StrategeDetailActivity extends BaseActivity {

    private final static String TAG = "StrategeDetailActivity";

    //String[] limit_state = {"允许", "禁止"};
    //String[] confirm_state = {"是", "否"};
    Toolbar toolbar;
    TextView mStrategeName;
    TextView getmStrategeAbout;
    String detail_about = null;
    PreferencesManager preferencesManager = null;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            getmStrategeAbout.setText(detail_about);
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_stratege_detail;
    }

    @Override
    protected void initData() {
        Intent mIntent = getIntent();
        int strategy_id = mIntent.getIntExtra("strategy_id",0);
        String strategyName = mIntent.getStringExtra("strategy_name");
        String strategy_type_name = mIntent.getStringExtra("strategy_type_name");
        mStrategeName.setText(strategy_type_name);

        readStrategeDetail(strategy_id, strategyName);
    }

    @Override
    protected void initView() {
        preferencesManager = PreferencesManager.getSingleInstance();

        toolbar = mViewHolder.get(R.id.toolbar);

        toolbar.setPadding(
                toolbar.getPaddingLeft(),
                toolbar.getPaddingTop() + TheTang.getSingleInstance().getStatusBarHeight(this),
                toolbar.getPaddingRight(),
                toolbar.getPaddingBottom());

        toolbar.setTitle(getResources().getString(R.string.message_detail));

        mStrategeName = mViewHolder.get(R.id.stratege_name);
        getmStrategeAbout = mViewHolder.get(R.id.stratege_detail_about);
    }

    private void readStrategeDetail(final int stratege_id, final String strategyName) {
        LogUtil.writeToFile(TAG,"STRATEGY_CODE == " + stratege_id);
        new Thread(new Runnable() {
            @Override
            public void run() {
                switch (stratege_id) {
                    case OrderConfig.send_system_strategy:
                        detail_about = getResources().getString(R.string.sd_change, "true".equals(preferencesManager.getComplianceData(Common.system_sd)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.machine_card, "true".equals(preferencesManager.getComplianceData(Common.system_sim)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no));
                        break;
                    case OrderConfig.send_app_strategy:
                        if (preferencesManager.getComplianceData(Common.appType).equals("0")) {
                            detail_about = getResources().getString(R.string.black_list, DatabaseOperate.getSingleInstance().queryAllApp().toString());
                        } else {
                            detail_about = getResources().getString(R.string.white_list, DatabaseOperate.getSingleInstance().queryAllApp().toString());
                        }
                        break;
                    case OrderConfig.send_loseCouplet_strategy:
                        String complianceData = preferencesManager.getComplianceData(Common.lost_time);
                        if (TextUtils.isEmpty( complianceData )){

                            detail_about = getResources().getString(R.string.lost_time,"空");
                        }else {
                            detail_about = getResources().getString(R.string.lost_time,formatDuring( Long.parseLong(complianceData)));

                        }
                        break;
                    case OrderConfig.send_geographical_Fence:
                        detail_about = getResources().getString(R.string.longitude, preferencesManager.getFenceData( Common.longitude )) + "\n"
                                + getResources().getString(R.string.latitude, preferencesManager.getFenceData( Common.latitude )) + "\n"
                                + getResources().getString(R.string.radius, preferencesManager.getFenceData( Common.radius )) + "\n" + "\n"

                                + getResources().getString(R.string.lock_screen, "1".equals(preferencesManager.getFenceData(Common.lockScreen)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.mobile_data, "1".equals(preferencesManager.getFenceData(Common.allowMobileData)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.wifi_config, "1".equals(preferencesManager.getFenceData(Common.lockScreen)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.mobile_ap, "1".equals(preferencesManager.getFenceData(Common.mobileHotspot)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.usb_config, "1".equals(preferencesManager.getFenceData(Common.matTransmission)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.sms_config, "1".equals(preferencesManager.getFenceData(Common.shortMessage)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.reconder, "1".equals(preferencesManager.getFenceData(Common.soundRecording)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.camera, "1".equals(preferencesManager.getFenceData(Common.allowCamera)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.bluetooth, "1".equals(preferencesManager.getFenceData(Common.allowBluetooth)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.drop_status, "1".equals(preferencesManager.getFenceData(Common.allowDropdown)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.reset, "1".equals(preferencesManager.getFenceData(Common.allowReset)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.NFC, "1".equals(preferencesManager.getFenceData(Common.allowNFC)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.screen_shot, "1".equals(preferencesManager.getFenceData(Common.banScreenshot)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.modify_sys_time, "1".equals(preferencesManager.getFenceData(Common.allowModifySystemtime)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.domain_switch, "1".equals(preferencesManager.getFenceData(Common.allowContainSwitching)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.two_domain, "1".equals(preferencesManager.getFenceData(Common.twoDomainControl)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"

                                + getResources().getString(R.string.allow_telephone, "1".equals(preferencesManager.getFenceData(Common.geo_telephone)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.open_telephone_white, "1".equals(preferencesManager.getFenceData(Common.geo_telephoneWhiteList)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"

                                + getResources().getString(R.string.security_chrome_config, "1".equals(preferencesManager.getFenceData(Common.allowChrome)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"

                                + getResources().getString(R.string.set_security_desk, "1".equals(preferencesManager.getFenceData(Common.setToSecureDesktop)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.resume_phone, "1".equals(preferencesManager.getFenceData(Common.displayCall)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.resume_contacts, "1".equals(preferencesManager.getFenceData(Common.displayContacts)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.resume_sms, "1".equals(preferencesManager.getFenceData(Common.displayMessage)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no));

                        break;
                    case OrderConfig.send_White_list:
                        detail_about = getResources().getString(R.string.white_list, DatabaseOperate.getSingleInstance().queryAllApp().toString());
                        break;
                    case OrderConfig.send_black_list:
                        detail_about = getResources().getString(R.string.black_list, DatabaseOperate.getSingleInstance().queryAllApp().toString());
                        break;
                    case OrderConfig.send_limit_strategy:
                        detail_about = getResources().getString(R.string.reconder, "1".equals(preferencesManager.getPolicyData(Common.middle_allowSoundRecording)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.mobile_data, "1".equals(preferencesManager.getPolicyData(Common.middle_allowMobileData)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.camera, "1".equals(preferencesManager.getPolicyData(Common.middle_allowCamera)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.usb_config, "1".equals(preferencesManager.getPolicyData(Common.middle_allowUsb)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.allow_location, "1".equals(preferencesManager.getPolicyData(Common.middle_allowLocation)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.wifi_config, "1".equals(preferencesManager.getPolicyData(Common.middle_allowWifi)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.sms_config, "1".equals(preferencesManager.getPolicyData(Common.middle_allowMessage)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.bluetooth, "1".equals(preferencesManager.getPolicyData(Common.middle_allowBluetooth)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.mobile_ap, "1".equals(preferencesManager.getPolicyData(Common.middle_allowMobileHotspot)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.screen_shot, "1".equals(preferencesManager.getPolicyData(Common.middle_allowScreenshot)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.drop_status, "1".equals(preferencesManager.getPolicyData(Common.middle_allowDropdown)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.reset, "1".equals(preferencesManager.getPolicyData(Common.middle_allowReset)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.NFC, "1".equals(preferencesManager.getPolicyData(Common.middle_allowNFC)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.modify_sys_time, "1".equals(preferencesManager.getPolicyData(Common.middle_allowModifySystemtime)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden))  + "\n"
                                + getResources().getString(R.string.allow_telephone, "1".equals(preferencesManager.getPolicyData(Common.middle_telephone)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.open_telephone_white, "1".equals(preferencesManager.getPolicyData(Common.middle_telephoneWhiteList)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden));

                        break;
                    case OrderConfig.send_configure_Strategy:
                        // wifi config
                        String wifiConfig = preferencesManager.getConfiguration( "WifiConfig" );
                        Type wifi_type = new TypeToken<ArrayList<ConfigureStrategyData.ConfigureStrategyBean.WifiListBean>>() {}.getType();
                        List<ConfigureStrategyData.ConfigureStrategyBean.WifiListBean> mWifilist = new Gson().fromJson( wifiConfig, wifi_type );
                        String wifi_list = "WIFI：";
                        if (mWifilist != null) {
                            for (ConfigureStrategyData.ConfigureStrategyBean.WifiListBean mWifiListBean : mWifilist) {
                                wifi_list = wifi_list + mWifiListBean.getSsid() + " ";
                            }
                        }

                        // vpn config
                        String vpnConfig = preferencesManager.getConfiguration( "VpnConfig" );
                        Type vpn_type = new TypeToken<ArrayList<ConfigureStrategyData.ConfigureStrategyBean.VpnListBean>>() {}.getType();
                        ArrayList<ConfigureStrategyData.ConfigureStrategyBean.VpnListBean> mVPNList = new Gson().fromJson( vpnConfig, vpn_type );
                        String vpn_list = "VPN：";
                        if (mVPNList != null) {
                            for (ConfigureStrategyData.ConfigureStrategyBean.VpnListBean mVpnListBean : mVPNList) {
                                vpn_list = vpn_list + mVpnListBean.getVpnConnectionName();
                            }
                        }

                        // webclip config
                        String webclipConfig = preferencesManager.getConfiguration( "WebclipConfig" );
                        Type webcliptype = new TypeToken<ArrayList<ConfigureStrategyData.ConfigureStrategyBean.WebclipListBean>>() {}.getType();
                        ArrayList<ConfigureStrategyData.ConfigureStrategyBean.WebclipListBean> mWebclipList = new Gson().fromJson( webclipConfig, webcliptype );
                        String webclip_list = "Webclip：";
                        if (mWebclipList != null) {
                            for (ConfigureStrategyData.ConfigureStrategyBean.WebclipListBean mWebclipListBean : mWebclipList) {
                                webclip_list = webclip_list + mWebclipListBean.getWebClipName();
                            }
                        }

                        // apn config
                        String apnConfig = preferencesManager.getConfiguration( "ApnConfig" );
                        Type apntype = new TypeToken<ArrayList<ConfigureStrategyData.ConfigureStrategyBean.ApnListBean>>() {}.getType();
                        ArrayList<ConfigureStrategyData.ConfigureStrategyBean.ApnListBean> mApnList = new Gson().fromJson( apnConfig, apntype );
                        String apn_list = "APN：";
                        if (mApnList != null) {
                            for (ConfigureStrategyData.ConfigureStrategyBean.ApnListBean mApnListBean : mApnList) {
                                apn_list = apn_list + mApnListBean.getApnName();
                            }
                        }

                        detail_about = wifi_list + "\n"
                                + vpn_list + "\n"
                                + webclip_list + "\n"
                                + apn_list + "\n";
                        break;
                    /*case OrderConfig.machine_card_binding:
                        detail_about = "ICCID：" + preferencesManager.getComplianceData( Common.iccid_card );
                        break;*/
                    case OrderConfig.send_safe_desk:

                        List<String> safe_list = new ArrayList<>();
                        String safe_application = preferencesManager.getSafedesktopData(Common.applicationProgram);
                        Type listType = new TypeToken<ArrayList<ClearDeskData.PolicyBean.ApplicationProgramBean>>() { }.getType();
                        if (!TextUtils.isEmpty(safe_application)) {
                            ArrayList<ClearDeskData.PolicyBean.ApplicationProgramBean> applicationList = new Gson().fromJson(safe_application, listType);

                            for (ClearDeskData.PolicyBean.ApplicationProgramBean mApplicationProgramBean : applicationList) {
                                safe_list.add(mApplicationProgramBean.getAppName());
                            }
                        }
                        detail_about = getResources().getString(R.string.resume_phone, "1".equals(preferencesManager.getSafedesktopData(Common.displayCall)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.resume_contacts, "1".equals(preferencesManager.getSafedesktopData(Common.displayContacts)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.resume_sms, "1".equals(preferencesManager.getSafedesktopData(Common.displayMessage)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.about_app, (safe_list.size() > 0 ? safe_list.toString() : ""));
                        break;
                    case OrderConfig.send_time_Frence:

                        String fenceData = preferencesManager.getFenceData(Common.timeUnit);
                        String timeUnit = getTimeUnit(fenceData);

                        detail_about = getResources().getString(R.string.time_range, (preferencesManager.getFenceData( Common.startimeRage) + "  " +   preferencesManager.getFenceData( Common.endTimeRage)) + "\n"
                                + timeUnit + "\n"
                                + getResources().getString(R.string.lock_screen, "1".equals(preferencesManager.getFenceData(Common.lockScreen)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.mobile_data, "1".equals(preferencesManager.getFenceData(Common.allowMobileData)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.wifi_config, "1".equals(preferencesManager.getFenceData(Common.lockScreen)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.mobile_ap, "1".equals(preferencesManager.getFenceData(Common.mobileHotspot)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.usb_config, "1".equals(preferencesManager.getFenceData(Common.matTransmission)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.sms_config, "1".equals(preferencesManager.getFenceData(Common.shortMessage)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.reconder, "1".equals(preferencesManager.getFenceData(Common.soundRecording)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.camera, "1".equals(preferencesManager.getFenceData(Common.allowCamera)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.bluetooth, "1".equals(preferencesManager.getFenceData(Common.allowBluetooth)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.drop_status, "1".equals(preferencesManager.getFenceData(Common.allowDropdown)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.reset, "1".equals(preferencesManager.getFenceData(Common.allowReset)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.NFC, "1".equals(preferencesManager.getFenceData(Common.allowNFC)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.screen_shot, "1".equals(preferencesManager.getFenceData(Common.banScreenshot)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.modify_sys_time, "1".equals(preferencesManager.getFenceData(Common.allowModifySystemtime)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.domain_switch, "1".equals(preferencesManager.getFenceData(Common.allowContainSwitching)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.two_domain, "1".equals(preferencesManager.getFenceData(Common.twoDomainControl)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.allow_location, "1".equals(preferencesManager.getFenceData(Common.locationService)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"

                                + getResources().getString(R.string.allow_telephone, "1".equals(preferencesManager.getFenceData(Common.geo_telephone)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.open_telephone_white, "1".equals(preferencesManager.getFenceData(Common.geo_telephoneWhiteList)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no))) + "\n"

                                + getResources().getString(R.string.security_chrome_config, "1".equals(preferencesManager.getFenceData(Common.allowChrome)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"

                                + getResources().getString(R.string.set_security_desk, "1".equals(preferencesManager.getFenceData(Common.setToSecureDesktop)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.resume_phone, "1".equals(preferencesManager.getFenceData(Common.displayCall)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.resume_contacts, "1".equals(preferencesManager.getFenceData(Common.displayContacts)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.resume_sms, "1".equals(preferencesManager.getFenceData(Common.displayMessage)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no));

                        break;
                    case OrderConfig.security_chrome:
                        String list = preferencesManager.getComplianceData( Common.securityChrome_list );
                        if (list == null) {
                            detail_about = "";
                            return;
                        }

                        Map<String, String> sec_white_list = new HashMap<>();
                        sec_white_list = TheTang.getSingleInstance().formatMapFromString( list );

                        List<String> url_list = new ArrayList<>();
                        Iterator<Map.Entry<String, String>> iterator = sec_white_list.entrySet().iterator();

                        while (iterator.hasNext()) {
                            Map.Entry<String, String> entry = iterator.next();
                            url_list.add( entry.getValue() );//获得url的域名
                        }
                        detail_about = getResources().getString(R.string.white_list, url_list.toString());
                        break;
                    case OrderConfig.put_down_application_fence:
                        String limitType = preferencesManager.getAppFenceData( Common.appFenceLimitType );
                        if(limitType == null)
                            return;
                        switch(Integer.valueOf( limitType )) {
                            case 0:

                                detail_about = getResources().getString(R.string.fence_allow) + "\n"
                                    + getResources().getString(R.string.voice_notice, "1".equals( preferencesManager.getAppFenceData( Common.appFenceNoticeBell )) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                    + getResources().getString(R.string.msg_notice, "1".equals( preferencesManager.getAppFenceData( Common.appFenceNoticeMessage )) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n";

                                if ("1".equals( preferencesManager.getAppFenceData( Common.appFenceNoticeMessage ))) {
                                   detail_about = detail_about +  getResources().getString(R.string.msg_notice_content, preferencesManager.getAppFenceData( Common.appFenceMessageContent )) + "\n";
                                }

                                String[] coordinate = null;

                                if (!TextUtils.isEmpty(preferencesManager.getAppFenceData( Common.appFenceCoordinate ))) {
                                    coordinate = preferencesManager.getAppFenceData(Common.appFenceCoordinate).split(",");
                                }

                                if (coordinate != null) {
                                    detail_about = detail_about + getResources().getString(R.string.geo_setting) + "\n"
                                            + getResources().getString(R.string.longitude, coordinate[0]) + "\n"
                                            + getResources().getString(R.string.latitude, coordinate[1]) + "\n"
                                            + getResources().getString(R.string.radius, preferencesManager.getAppFenceData( Common.appFenceRadius )) + "\n";
                                }

                                String appFenceStartDateRange = preferencesManager.getAppFenceData( Common.appFenceStartDateRange );
                                if (appFenceStartDateRange != null) {
                                    String appTimeUnit = getTimeUnit(preferencesManager.getAppFenceData( Common.timeUnit));
                                    detail_about = detail_about + getResources().getString(R.string.time_range, (appFenceStartDateRange + "  " +   preferencesManager.getAppFenceData( Common.appFenceEndDateRange ))) + "\n"
                                            + appTimeUnit + "\n";
                                }
                                break;
                            case 1:
                                detail_about = getResources().getString(R.string.always_allow) + "\n";
                                break;
                            case 2:
                                detail_about = getResources().getString(R.string.always_forbidden) + "\n";
                                break;
                        }

                        detail_about = detail_about +  getResources().getString(R.string.about_app, preferencesManager.getAppFenceData( Common.appFenceAppPageName)) ;
                        break;
                    case OrderConfig.security_manager:

                        detail_about = getResources().getString(R.string.reconder, "1".equals(preferencesManager.getSecurityData(Common.banSoundRecord)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.mobile_data, "1".equals(preferencesManager.getSecurityData(Common.banMobileData)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.camera, "1".equals(preferencesManager.getSecurityData(Common.banCamera)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.usb_config, "1".equals(preferencesManager.getSecurityData(Common.banMtp)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.allow_location, "1".equals(preferencesManager.getSecurityData(Common.banLocation)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.wifi_config, "1".equals(preferencesManager.getSecurityData(Common.banWifi)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.bluetooth, "1".equals(preferencesManager.getSecurityData(Common.banBluetooth)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.screen_shot, "1".equals(preferencesManager.getSecurityData(Common.banScreenshot)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.drop_status, "1".equals(preferencesManager.getSecurityData(Common.allowDropdown)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.reset, "1".equals(preferencesManager.getSecurityData(Common.allowReset)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.NFC, "1".equals(preferencesManager.getSecurityData(Common.allowNFC)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.modify_sys_time, "1".equals(preferencesManager.getSecurityData(Common.allowModifySystemtime)) ? getResources().getString(R.string.allow) : getResources().getString(R.string.forbidden)) + "\n"
                                + getResources().getString(R.string.allow_telephone) + ":"
                                + ("1".equals(preferencesManager.getSecurityData(Common.banTelephone)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"

                                + getResources().getString(R.string.mobile_ap) + ":"
                                + ("1".equals(preferencesManager.getSecurityData(Common.banMobileHotspot)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"

                                + getResources().getString(R.string.sms_config) + ":"
                                + ("1".equals(preferencesManager.getSecurityData(Common.banShortMessage)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"

                                + getResources().getString(R.string.open_telephone_white) + ":"
                                + ("1".equals(preferencesManager.getSecurityData(Common.banTelephoneWhiteList)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.exit_security,
                                        "1".equals(preferencesManager.getSecurityData(Common.banExitSecurityDomain)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n"
                                + getResources().getString(R.string.use_security_desk,
                                                "1".equals(preferencesManager.getSecurityData(Common.secureDesktop)) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n" + "\n"

                                + getResources().getString(R.string.about_app, preferencesManager.getSecurityData( Common.safetyLimitDesktops));

                        break;
                    case OrderConfig.SEND_SENSITIVE_WORD_POLICY:
                        detail_about = DatabaseOperate.getSingleInstance().querySensitiveWord(strategyName);
                        break;
                    case OrderConfig.SEND_SMS_BACKUP_POLICY:
                        SharedPreferences sharedPreferences = getSharedPreferences(SmsManager.SMS_SP_NAME, MODE_PRIVATE);
                        detail_about = sharedPreferences.getString(SmsManager.SMS_DISPLAY_TIME_STRING, "");
                        break;
                    case OrderConfig.SEND_CALL_RECORDER_BACKUP_POLICY:
                        SharedPreferences callSP = getSharedPreferences(CallRecorderManager.CALL_RECORDER_SP_NAME, MODE_PRIVATE);
                        detail_about = callSP.getString(CallRecorderManager.CALL_RECORDER_DISPLAY_TIME_STRING, "");
                        break;
                    case OrderConfig.send_trajectory_Strategy:
                        String trajectoryData = preferencesManager.getTrajectoryData(Common.timeUnit);
                        String timeUnits = getTimeUnit(trajectoryData);
                        detail_about =
                                getResources().getString(R.string.time_range, (preferencesManager.getTrajectoryData( Common.startimeRage) + "  " +   preferencesManager.getTrajectoryData( Common.endTimeRage)) + "\n"
                                        + timeUnits + "\n"+
                                        getResources().getString(R.string.frequency, preferencesManager.getTrajectoryData(Common.frequency)));
                        break;
                    default:
                        break;
                }
                handler.sendMessage(new Message());
            }
        }).start();
    }


    private String  formatDuring(long mss) {
        StringBuffer time = new StringBuffer();
        long days = mss / (1000 * 60 * 60 * 24);
        if (days!=0){
            time.append(days);
            time.append(" 天 ");
        }
        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        if (hours!=0){
            time.append(hours);
            time.append(" 时 ");
        }
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);

        if (minutes!=0){
            time.append(minutes);
            time.append(" 分 ");
        }
        long seconds = (mss % (1000 * 60)) / 1000;

        if (seconds!=0){
            time.append(seconds);
            time.append(" 秒 ");
        }
        return time.toString()/*days + " 天 " + hours + " 时 " + minutes + " 分 " + seconds + " 秒 "*/;
    }


    @NonNull
    private String getTimeUnit(String fenceData) {
        String timeUnit = "";
        if (! TextUtils.isEmpty(fenceData) ) {
            Type type = new TypeToken<ArrayList<TimeFenceData.PolicyBean.TimeUnitBean>>() { }.getType();
            ArrayList<TimeFenceData.PolicyBean.TimeUnitBean> listTimeUnitBean = new Gson().fromJson(fenceData, type);
          if (listTimeUnitBean != null && listTimeUnitBean.size() > 0) {
              for (int i = 0; i < listTimeUnitBean.size(); i++) {
                  TimeFenceData.PolicyBean.TimeUnitBean bean = listTimeUnitBean.get(i);
                  switch (bean.getUnitType()) {
                      case "1":
                          timeUnit = timeUnit + getResources().getString(R.string.every_day) + bean.getStartTime() + " " + bean.getEndTime() + "\n";
                          break;
                      case "2":
                          switch (bean.getTypeDate()) {
                              case "1":
                                  timeUnit = timeUnit + getResources().getString(R.string.every_week) + " " + getResources().getString(R.string.week_one) + bean.getStartTime() + " " + bean.getEndTime() + "\n";
                                  break;
                              case "2":
                                  timeUnit = timeUnit + getResources().getString(R.string.every_week) + " " + getResources().getString(R.string.week_two) + bean.getStartTime() + " " + bean.getEndTime() + "\n";
                                  break;
                              case "3":
                                  timeUnit = timeUnit + getResources().getString(R.string.every_week) + " " + getResources().getString(R.string.week_three) + bean.getStartTime() + " " + bean.getEndTime() + "\n";
                                  break;
                              case "4":
                                  timeUnit = timeUnit + getResources().getString(R.string.every_week) + " " + getResources().getString(R.string.week_four) + bean.getStartTime() + " " + bean.getEndTime() + "\n";
                                  break;
                              case "5":
                                  timeUnit = timeUnit + getResources().getString(R.string.every_week) + " " + getResources().getString(R.string.week_five) + bean.getStartTime() + " " + bean.getEndTime() + "\n";
                                  break;
                              case "6":
                                  timeUnit = timeUnit + getResources().getString(R.string.every_week) + " " + getResources().getString(R.string.week_six) + bean.getStartTime() + " " + bean.getEndTime() + "\n";
                                  break;
                              case "7":
                                  timeUnit = timeUnit + getResources().getString(R.string.every_week) + " " + getResources().getString(R.string.week_seven) + bean.getStartTime() + " " + bean.getEndTime() + "\n";
                                  break;
                          }
                          break;
                      case "3":
                          timeUnit = timeUnit + getResources().getString(R.string.time_work) +  bean.getStartTime() + " " + bean.getEndTime() + "\n";
                          break;
                      case "4":
                          timeUnit = timeUnit + getResources().getString(R.string.special_time) +  bean.getStartTime() + " " + bean.getEndTime() + "\n";
                          break;
                  }
              }
          }
        }
        return timeUnit;
    }


}

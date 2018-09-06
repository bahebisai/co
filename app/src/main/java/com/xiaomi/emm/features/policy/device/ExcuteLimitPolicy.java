package com.xiaomi.emm.features.policy.device;


import android.text.TextUtils;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.OrderConfig;
import com.xiaomi.emm.model.PolicyData;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.MDM;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

/**
 * Created by Administrator on 2017/8/4.
 */

public class ExcuteLimitPolicy {
    static PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
    /**
     * 限制策略
     * @param policyData
     */
    public static void excuteLimitPolicy(PolicyData policyData) {

        if (policyData == null) {

            String limit_name = preferencesManager.getPolicyData(Common.limit_name);

            if ( limit_name == null) {
                return;
            }

            TheTang.getSingleInstance().addMessage( String.valueOf( OrderConfig.delete_limit_strategy ), limit_name );

            TheTang.getSingleInstance().deleteStrategeInfo( String.valueOf( OrderConfig.send_limit_strategy ) );

            limitDefaultPolicy();

            deleteLimitPolicy();

        } else {
            TheTang.getSingleInstance().addMessage( String.valueOf( OrderConfig.send_limit_strategy ), policyData.name );

            TheTang.getSingleInstance().addStratege( String.valueOf( OrderConfig.send_limit_strategy ), policyData.name,System.currentTimeMillis() + "");

            storageLimitPolicy(policyData);

            limitPolicy(policyData);
        }
    }

    /**
     * 限制策略
     * @param policyData
     */
    public static void limitPolicy(PolicyData policyData) {

        String securityContainer = preferencesManager.getSecurityData( Common.securityContainer);

        if (TextUtils.isEmpty( securityContainer ) || "false".equals( securityContainer ) ) {


            if (preferencesManager.getFenceData( Common.allowBluetooth ) != null) {

                if ("false".equals( preferencesManager.getFenceData(Common.insideAndOutside) ) || TextUtils.isEmpty( preferencesManager.getFenceData(Common.insideAndOutside) )){
                    excutePolicy(policyData);
                }

            } else {
                excutePolicy(policyData);
            }
        }

    }

    private static void storageLimitPolicy(PolicyData policyData) {

        preferencesManager.setPolicyData(Common.middle_policy,"true");
        preferencesManager.setPolicyData(Common.limit_name,policyData.name);
        preferencesManager.setPolicyData( Common.limit_id,policyData.id );
        preferencesManager.setPolicyData(Common.middle_allowSoundRecording, policyData.allowSoundRecording);
        preferencesManager.setPolicyData(Common.middle_allowMobileData, policyData.allowMobileData);
        preferencesManager.setPolicyData(Common.middle_allowCamera, policyData.allowCamera);
        preferencesManager.setPolicyData(Common.middle_allowUsb,policyData.allowUsb);
        preferencesManager.setPolicyData(Common.middle_allowLocation, policyData.allowLocation);
        preferencesManager.setPolicyData(Common.middle_allowWifi, policyData.allowWifi);
        preferencesManager.setPolicyData(Common.middle_allowMessage, policyData.allowMessage);
        preferencesManager.setPolicyData(Common.middle_allowBluetooth, policyData.allowBluetooth);
        //没有存热点
        //preferencesManager.setPolicyData(Common.middle_allowSdCard, policyData.allowSdCard);
        preferencesManager.setPolicyData(Common.middle_allowMobileHotspot, policyData.allowMobileHotspot);
        //preferencesManager.setPolicyData(Common.middle_allowRestoreFactorySettings, policyData.allowRestoreFactorySettings);
        //preferencesManager.setPolicyData(Common.middle_allowUpdateTime, policyData.allowUpdateTime);
        preferencesManager.setPolicyData(Common.middle_allowScreenshot, policyData.allowScreenshot);

        preferencesManager.setPolicyData(Common.middle_allowDropdown, policyData.allowDropdown);
        preferencesManager.setPolicyData(Common.middle_allowReset, policyData.allowReset);
        preferencesManager.setPolicyData(Common.middle_allowNFC, policyData.allowNFC);
        preferencesManager.setPolicyData(Common.middle_allowModifySystemtime, policyData.allowModifySystemtime);

        preferencesManager.setPolicyData(Common.middle_telephoneWhiteList, policyData.allowTelephone);
        preferencesManager.setPolicyData(Common.middle_telephone, policyData.allowTelephoneWhiteList);
    }

    public static PolicyData queryLimitPolicy() {

        PolicyData policyData = new PolicyData();

        policyData.allowSoundRecording = preferencesManager.getPolicyData(Common.middle_allowSoundRecording);
        policyData.allowMobileData = preferencesManager.getPolicyData(Common.middle_allowMobileData);
        policyData.allowCamera = preferencesManager.getPolicyData(Common.middle_allowCamera);
        policyData.allowUsb = preferencesManager.getPolicyData(Common.middle_allowUsb);
        policyData.allowLocation = preferencesManager.getPolicyData(Common.middle_allowLocation);
        policyData.allowWifi = preferencesManager.getPolicyData(Common.middle_allowWifi);
        policyData.allowMessage = preferencesManager.getPolicyData(Common.middle_allowMessage);
        policyData.allowBluetooth = preferencesManager.getPolicyData(Common.middle_allowBluetooth);
        policyData.name = preferencesManager.getPolicyData(Common.limit_name);
        //preferencesManager.setPolicyData(Common.middle_allowSdCard, policyData.allowSdCard);
        policyData.allowMobileHotspot = preferencesManager.getPolicyData(Common.middle_allowMobileHotspot);
        //preferencesManager.setPolicyData(Common.middle_allowRestoreFactorySettings, policyData.allowRestoreFactorySettings);
        //preferencesManager.setPolicyData(Common.middle_allowUpdateTime, policyData.allowUpdateTime);
        policyData.allowScreenshot = preferencesManager.getPolicyData(Common.middle_allowScreenshot);

        policyData.allowDropdown = preferencesManager.getPolicyData(Common.middle_allowDropdown);
        policyData.allowReset = preferencesManager.getPolicyData(Common.middle_allowReset);
        policyData.allowNFC = preferencesManager.getPolicyData(Common.middle_allowNFC);
        policyData.allowModifySystemtime = preferencesManager.getPolicyData(Common.middle_allowModifySystemtime);

        policyData.allowTelephone = preferencesManager.getPolicyData(Common.middle_telephone);
        policyData.allowTelephoneWhiteList = preferencesManager.getPolicyData(Common.middle_telephoneWhiteList);
        return policyData;
    }

    private static void deleteLimitPolicy() {

        preferencesManager.removePolicyData(Common.limit_name);
        preferencesManager.removePolicyData( Common.limit_id );
        preferencesManager.removePolicyData(Common.middle_policy);
        preferencesManager.removePolicyData(Common.middle_allowSoundRecording);
        preferencesManager.removePolicyData(Common.middle_allowMobileData);
        preferencesManager.removePolicyData(Common.middle_allowCamera);
        preferencesManager.removePolicyData(Common.middle_allowUsb);
        preferencesManager.removePolicyData(Common.middle_allowLocation);
        preferencesManager.removePolicyData(Common.middle_allowWifi);
        preferencesManager.removePolicyData(Common.middle_allowMessage);
        preferencesManager.removePolicyData(Common.middle_allowBluetooth);

        //preferencesManager.removePolicyData(Common.middle_allowSdCard);
        preferencesManager.removePolicyData(Common.middle_allowMobileHotspot);
        //preferencesManager.removePolicyData(Common.middle_allowRestoreFactorySettings);
        //preferencesManager.removePolicyData(Common.middle_allowUpdateTime);
        preferencesManager.removePolicyData(Common.middle_allowScreenshot);

        preferencesManager.removePolicyData(Common.middle_allowDropdown);
        preferencesManager.removePolicyData(Common.middle_allowReset);
        preferencesManager.removePolicyData(Common.middle_allowNFC);
        preferencesManager.removePolicyData(Common.middle_allowModifySystemtime);
        preferencesManager.removePolicyData(Common.middle_telephone);
        preferencesManager.removePolicyData(Common.middle_telephoneWhiteList);
    }

    /**
     * 默认限制策略
     */
    public static void limitDefaultPolicy() {
        PolicyData policyData = getDefaultPolicy();
        String securityContainer = preferencesManager.getSecurityData( Common.securityContainer);

        if (TextUtils.isEmpty( securityContainer ) || "false".equals( securityContainer ) ) {
            if (preferencesManager.getFenceData( Common.allowBluetooth ) != null) {
                if ("false".equals( preferencesManager.getFenceData(Common.insideAndOutside) ) ){
                    excutePolicy(policyData);
                }
            } else {
                excutePolicy(policyData);
            }
        }
    }

    /**
     * 获得默认限制策略
     * @return
     */
    private static PolicyData getDefaultPolicy() {
        PolicyData policyData = new PolicyData();

        policyData.allowSoundRecording = preferencesManager.getPolicyData(Common.default_allowSoundRecording);
        LogUtil.writeToFile("ExcuteLimitPolicy", "isSoundRecordingEnabled = " + policyData.allowSoundRecording);
        policyData.allowMobileData = preferencesManager.getPolicyData(Common.default_allowMobileData);
        policyData.allowCamera = preferencesManager.getPolicyData(Common.default_allowCamera);
        policyData.allowUsb = preferencesManager.getPolicyData(Common.default_allowUsb);
        policyData.allowLocation = preferencesManager.getPolicyData(Common.default_allowLocation);
        policyData.allowWifi = preferencesManager.getPolicyData(Common.default_allowWifi);
        policyData.allowMessage = preferencesManager.getPolicyData(Common.default_allowMessage);
        policyData.allowBluetooth = preferencesManager.getPolicyData(Common.default_allowBluetooth);

        //policyData.allowSdCard = preferencesManager.getPolicyData(Common.allowSdCard);
        policyData.allowMobileHotspot = preferencesManager.getPolicyData(Common.default_allowWifiAP);
        //policyData.allowRestoreFactorySettings = preferencesManager.getPolicyData(Common.allowRestoreFactorySettings);
        //policyData.allowUpdateTime = preferencesManager.getPolicyData(Common.allowUpdateTime);
        policyData.allowScreenshot = preferencesManager.getPolicyData(Common.default_allowScreenshot);

        policyData.allowDropdown = preferencesManager.getPolicyData(Common.default_allowDropdown);
        policyData.allowReset = preferencesManager.getPolicyData(Common.default_allowReset);
        policyData.allowNFC = preferencesManager.getPolicyData(Common.default_allowNFC);
        policyData.allowModifySystemtime = preferencesManager.getPolicyData(Common.default_allowModifySystemtime);
        policyData.allowTelephone = preferencesManager.getPolicyData(Common.default_allowTelephone);
        policyData.allowTelephoneWhiteList = preferencesManager.getPolicyData(Common.default_allowTelephoneWhiteList);
        return policyData;
    }

    /**
     * 执行限制策略
     * @param policyData
     */
    private static void excutePolicy(PolicyData policyData) {
        //允许wifi
        if ("0".equals(policyData.allowWifi)) {
            MDM.enableWifi(false);
        } else {
            MDM.enableWifi(true);
        }

        if ("0".equals(policyData.allowMobileHotspot)) { //没有
            MDM.enableWifiAP(false);
        } else {
            MDM.enableWifiAP(true);
        }

        //允许录音
        if ("0".equals(policyData.allowSoundRecording)) {
            MDM.enableSoundRecording(false);
        } else {
            MDM.enableSoundRecording(true);
        }

        //打开移动数据
        /*if ("0".equals(policyData.allowMobileData)) { //没有
            MDM.openDataConnectivity(false);
        } else {
            MDM.openDataConnectivity(true);
        }*/

        //允许照相机
        if ("0".equals(policyData.allowCamera)) {
            MDM.enableCamera(false);
        } else {
            MDM.enableCamera(true);
        }

        //允许定位
        if (preferencesManager.getAppFenceData( Common.appFenceRadius ) == null ||
                "0".equals( preferencesManager.getAppFenceData( Common.appFenceRadius ) )) {
            if (PreferencesManager.getSingleInstance().getFenceData( Common.geographical_fence ) == null) {
                if ("0".equals( policyData.allowLocation )) {
                    MDM.enableLocationService( false );
                } else {
                    MDM.enableLocationService( true );
                }
            }
        }

        //允许SD卡
        /*if (policyData.allowSdCard.equals("0")) {
            MDM.enableSD();
        } else {
            MDM.disableSD();
        }*/

        //打开移动热点
        if ("0".equals(policyData.allowMobileHotspot)) { //没有
            MDM.enableWifiAP(false);
        } else {
            MDM.enableWifiAP(true);
        }

        //允许短信
        if ("0".equals(policyData.allowMessage)) {
            MDM.enableSms(false);
        } else {
            MDM.enableSms(true);
        }

        //允许蓝牙
        if ("0".equals(policyData.allowBluetooth)) {
            LogUtil.writeToFile("ExcuteLimitPolicy",  "allowBluetooth");
            MDM.enableBluetooth(false);
        } else {
            MDM.enableBluetooth(true);
        }

        //允许USB
        if ("0".equals(policyData.allowUsb)) {
            MDM.enableUsb(false);
        } else {
            MDM.enableUsb(true);
        }

        //允许截屏
        if ("0".equals(policyData.allowScreenshot)) {
            MDM.disableScreenShot();
        } else {
            MDM.enableScreenShot();
        }

        //允许复位
        if ("0".equals(policyData.allowReset)) {
            MDM.disableReset();
        } else {
            MDM.enableReset();
        }

        //允许下拉栏
        if ("0".equals(policyData.allowDropdown)) {
            MDM.disableDropdown();
        } else {
            MDM.enableDropdown();
        }

        //允许NFC
        if ("0".equals(policyData.allowNFC)) {
            MDM.disableNfc(null);
        } else {
            MDM.enableNfc(null);
        }

        //允许修改日期
        if ("0".equals(policyData.allowModifySystemtime)) {
            MDM.disableModifySystemtime();
        } else {
            MDM.enableModifySystemtime();
        }

        if ("0".equals(policyData.allowTelephone)) {
            MDM.enableTelePhone(false);
        } else {
            MDM.enableTelePhone(true);
        }

        if ("0".equals(policyData.allowTelephoneWhiteList)) {
            MDM.stopPhoneWhite();
        } else {
            MDM.startPhoneWhite();
        }
    }
}

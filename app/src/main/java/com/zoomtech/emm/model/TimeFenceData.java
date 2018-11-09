package com.zoomtech.emm.model;

/**
 * Created by Administrator on 2017/8/9.
 */

import java.util.List;

/**
 * Created by lenovo on 2017/8/17.
 */

public class TimeFenceData {


    /**
     * alias : 20170817084941725
     * code : 136
     * policy : [{"allowAutomaticJoin":1,"allowBluetooth":1,"allowCamera":1,"allowCloseWifi":1,"allowDomainSwitching":1,"allowMobileData":0,"allowOpenWifi":0,"applicationProgram":[{"appName":"fgfggf","appPageName":"1232233243"}],"configureWifi":1,"displayCall":1,"displayContacts":1,"displayMessage":1,"endTimeRage":"2017-08-22T00:11:00.000Z","hiddenNetwork":1,"lockScreen":1,"name":"xxx1","safeType":2,"startTimeRage":"2017-08-21T00:11:00.000Z","timeUnit":[{"endTime":"23:59","startTime":"00:00","typeDate":"","unitType":1},{"endTime":"23:59","startTime":"02:00","typeDate":"1","unitType":2},{"endTime":"21:01","startTime":"02:02","typeDate":"","unitType":3},{"endTime":"06:02","startTime":"03:02","typeDate":"2017-08-22T00:11:00.000Z","unitType":4}],"twoDomainControl":1,"webpageList":1}]
     * "sendId":"a74a0a53-7b06-4060-9dc6-3ab27345b6b7"
     */

    private String alias;
    private String code;
    private String id;
    private List<PolicyBean> policy;

    //  private String sendId;
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    /* public void setSendId(String sendId){
         this.sendId=sendId;
     }

     public String getSendId(){
         return sendId;
     }*/
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<PolicyBean> getPolicy() {
        return policy;
    }

    public void setPolicy(List<PolicyBean> policy) {
        this.policy = policy;
    }

    public static class PolicyBean {
        /**
         * allowAutomaticJoin : 1
         * allowBluetooth : 1
         * allowCamera : 1
         * allowCloseWifi : 1
         * allowDomainSwitching : 1
         * allowMobileData : 0
         * allowOpenWifi : 0
         * applicationProgram : [{"appName":"fgfggf","appPageName":"1232233243"}]
         * configureWifi : 1
         * displayCall : 1
         * displayContacts : 1
         * displayMessage : 1
         * endTimeRage : 2017-08-22T00:11:00.000Z
         * hiddenNetwork : 1
         * lockScreen : 1
         * name : xxx1
         * safeType : 2
         * startTimeRage : 2017-08-21T00:11:00.000Z
         * timeUnit : [{"endTime":"23:59","startTime":"00:00","typeDate":"","unitType":1},{"endTime":"23:59","startTime":"02:00","typeDate":"1","unitType":2},{"endTime":"21:01","startTime":"02:02","typeDate":"","unitType":3},{"endTime":"06:02","startTime":"03:02","typeDate":"2017-08-22T00:11:00.000Z","unitType":4}]
         * twoDomainControl : 1
         * webpageList : 1
         * setToSecureDesktop:1
         * id:94
         * type:0
         */

        private String allowAutomaticJoin;
        private String allowBluetooth;
        private String allowCamera;
        private String allowCloseWifi;
        private String allowDomainSwitching;
        private String allowMobileData;
        private String allowOpenWifi;
        private String configureWifi;
        private String ssid;
        private String displayCall;
        private String displayContacts;
        private String displayMessage;
        private String endTimeRage;
        private String startTimeRage;
        private String hiddenNetwork;
        private String lockScreen;
        public String lockPwd;
        private String wifiPassword;
        private String name;
        private String safeType;
        private String twoDomainControl;
        private String webpageList;
        private String setToSecureDesktop;
        private String allowConfigureWifi;
        private String type;
        private String mobileHotspot;//移动热点 围栏内停用

        private String locationService;//定位服务围栏内停用

        private String matTransmission;//USB传输MTP数据围栏内停用

        private String shortMessage;//短信围栏内停用

        private String allowDropdown;

        private String allowReset;

        private String allowNFC;

        private String banScreenshot;

        private String allowModifySystemtime;

        private String telephone;

        private String telephoneWhiteList;

        public String getMobileHotspot() {
            return mobileHotspot;
        }

        public void setMobileHotspot(String mobileHotspot) {
            this.mobileHotspot = mobileHotspot;
        }

        public String getLocationService() {
            return locationService;
        }

        public void setLocationService(String locationService) {
            this.locationService = locationService;
        }

        public String getMatTransmission() {
            return matTransmission;
        }

        public void setMatTransmission(String matTransmission) {
            this.matTransmission = matTransmission;
        }

        public String getShortMessage() {
            return shortMessage;
        }

        public void setShortMessage(String shortMessage) {
            this.shortMessage = shortMessage;
        }

        public String getAllowDropdown() {
            return allowDropdown;
        }

        public void setAllowDropdown(String allowDropdown) {
            this.allowDropdown = allowDropdown;
        }

        public String getAllowReset() {
            return allowReset;
        }

        public void setAllowReset(String allowReset) {
            this.allowReset = allowReset;
        }

        public String getAllowNFC() {
            return allowNFC;
        }

        public void setAllowNFC(String allowNFC) {
            this.allowNFC = allowNFC;
        }

        public String getBanScreenshot() {
            return banScreenshot;
        }

        public void setBanScreenshot(String banScreenshot) {
            this.banScreenshot = banScreenshot;
        }

        public String getAllowModifySystemtime() {
            return allowModifySystemtime;
        }

        public void setAllowModifySystemtime(String allowModifySystemtime) {
            this.allowModifySystemtime = allowModifySystemtime;
        }

        public String getTelephone() {
            return telephone;
        }

        public void setTelephone(String telephone) {
            this.telephone = telephone;
        }

        public String getTelephoneWhiteList() {
            return telephoneWhiteList;
        }

        public void setTelephoneWhiteList(String telephoneWhiteList) {
            this.telephoneWhiteList = telephoneWhiteList;
        }

        public String getSoundRecording() {
            return soundRecording;
        }

        public void setSoundRecording(String soundRecording) {
            this.soundRecording = soundRecording;
        }

        private String soundRecording; //录音围栏内停用

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getAllowConfigureWifi() {
            return allowConfigureWifi;
        }

        public void setAllowConfigureWifi(String allowConfigureWifi) {
            this.allowConfigureWifi = allowConfigureWifi;
        }

        public String getSetToSecureDesktop() {
            return setToSecureDesktop;
        }

        public void setSetToSecureDesktop(String setToSecureDesktop) {
            this.setToSecureDesktop = setToSecureDesktop;
        }


        private List<ApplicationProgramBean> applicationProgram;
        private List<TimeUnitBean> timeUnit;

        public String getAllowAutomaticJoin() {
            return allowAutomaticJoin;
        }

        public void setAllowAutomaticJoin(String allowAutomaticJoin) {
            this.allowAutomaticJoin = allowAutomaticJoin;
        }

        public String getAllowBluetooth() {
            return allowBluetooth;
        }

        public void setAllowBluetooth(String allowBluetooth) {
            this.allowBluetooth = allowBluetooth;
        }

        public String getAllowCamera() {
            return allowCamera;
        }

        public void setAllowCamera(String allowCamera) {
            this.allowCamera = allowCamera;
        }

        public String getAllowCloseWifi() {
            return allowCloseWifi;
        }

        public void setAllowCloseWifi(String allowCloseWifi) {
            this.allowCloseWifi = allowCloseWifi;
        }

        public String getAllowDomainSwitching() {
            return allowDomainSwitching;
        }

        public void setAllowDomainSwitching(String allowDomainSwitching) {
            this.allowDomainSwitching = allowDomainSwitching;
        }

        public String getAllowMobileData() {
            return allowMobileData;
        }

        public void setAllowMobileData(String allowMobileData) {
            this.allowMobileData = allowMobileData;
        }

        public String getAllowOpenWifi() {
            return allowOpenWifi;
        }

        public void setAllowOpenWifi(String allowOpenWifi) {
            this.allowOpenWifi = allowOpenWifi;
        }

        public String getConfigureWifi() {
            return configureWifi;
        }

        public void setConfigureWifi(String configureWifi) {
            this.configureWifi = configureWifi;
        }

        public String getSsid() {
            return ssid;
        }

        public void setSsid(String ssid) {
            this.ssid = ssid;
        }

        public String getDisplayCall() {
            return displayCall;
        }

        public void setDisplayCall(String displayCall) {
            this.displayCall = displayCall;
        }

        public String getDisplayContacts() {
            return displayContacts;
        }

        public void setDisplayContacts(String displayContacts) {
            this.displayContacts = displayContacts;
        }

        public String getDisplayMessage() {
            return displayMessage;
        }

        public void setDisplayMessage(String displayMessage) {
            this.displayMessage = displayMessage;
        }

        public String getEndTimeRage() {
            return endTimeRage;
        }

        public void setEndTimeRage(String endTimeRage) {
            this.endTimeRage = endTimeRage;
        }

        public String getHiddenNetwork() {
            return hiddenNetwork;
        }

        public void setHiddenNetwork(String hiddenNetwork) {
            this.hiddenNetwork = hiddenNetwork;
        }

        public String getLockScreen() {
            return lockScreen;
        }

        public void setLockScreen(String lockScreen) {
            this.lockScreen = lockScreen;
        }

        public String getLockPwd() {
            return lockPwd;
        }

        public void setLockPwd(String lockPwd) {
            this.lockPwd = lockPwd;
        }

        public void setWifiPassword(String wifiPassword) {
            this.wifiPassword = wifiPassword;
        }

        public String getWifiPassword() {
            return wifiPassword;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSafeType() {
            return safeType;
        }

        public void setSafeType(String safeType) {
            this.safeType = safeType;
        }

        public String getStartTimeRage() {
            return startTimeRage;
        }

        public void setStartTimeRage(String startTimeRage) {
            this.startTimeRage = startTimeRage;
        }

        public String getTwoDomainControl() {
            return twoDomainControl;
        }

        public void setTwoDomainControl(String twoDomainControl) {
            this.twoDomainControl = twoDomainControl;
        }

        public String getWebpageList() {
            return webpageList;
        }

        public void setWebpageList(String webpageList) {
            this.webpageList = webpageList;
        }

        public List<ApplicationProgramBean> getApplicationProgram() {
            return applicationProgram;
        }

        public void setApplicationProgram(List<ApplicationProgramBean> applicationProgram) {
            this.applicationProgram = applicationProgram;
        }

        public List<TimeUnitBean> getTimeUnit() {
            return timeUnit;
        }

        public void setTimeUnit(List<TimeUnitBean> timeUnit) {
            this.timeUnit = timeUnit;
        }

        public static class ApplicationProgramBean {
            /**
             * appName : fgfggf
             * appPageName : 1232233243
             */

            private String appName;
            private String packageName;

            public String getAppName() {
                return appName;
            }

            public void setAppName(String appName) {
                this.appName = appName;
            }

            public String getAppPageName() {
                return packageName;
            }

            public void setAppPageName(String appPageName) {
                this.packageName = appPageName;
            }
        }

        public static class TimeUnitBean {
            /**
             * endTime : 23:59
             * startTime : 00:00
             * typeDate :
             * unitType : 1
             */
            private String typeDate;
            private String unitType;
            private String endTime;
            private String startTime;


            public String getEndTime() {
                return endTime;
            }

            public void setEndTime(String endTime) {
                this.endTime = endTime;
            }

            public String getStartTime() {
                return startTime;
            }

            public void setStartTime(String startTime) {
                this.startTime = startTime;
            }

            public String getTypeDate() {
                return typeDate;
            }

            public void setTypeDate(String typeDate) {
                this.typeDate = typeDate;
            }

            public String getUnitType() {
                return unitType;
            }

            public void setUnitType(String unitType) {
                this.unitType = unitType;
            }

            @Override
            public String toString() {
                return "TimeUnitBean{" +
                        "startTime='" + startTime + '\'' +
                        ", endTime='" + endTime + '\'' +
                        ", typeDate='" + typeDate + '\'' +
                        ", unitType='" + unitType + '\'' +
                        '}';
            }
        }
    }
}

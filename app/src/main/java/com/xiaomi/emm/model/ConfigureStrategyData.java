package com.xiaomi.emm.model;

import java.util.List;

/**
 * Created by lenovo on 2017/8/30.
 */

public class ConfigureStrategyData {


    /**
     * ConfigureStrategy : {"adminId":1,"adminName":"admin","apnList":[{"apnName":"123456","apnPassword":"","apnPort":"","apnServerAddress":"","apnUsername":"","configId":0,"id":30}],"configId":221,"isAllowWifiConfig":1,"isApn":1,"isVpn":1,"isWebclip":1,"isWifi":1,"issuedNumber":0,"lastUpdateTime":{"date":21,"day":4,"hours":9,"minutes":57,"month":8,"seconds":3,"time":1505959023000,"timezoneOffset":-480,"year":117},"name":"lxk","platformType":0,"remark":"lxk","useNumber":0,"vpnList":[{"configId":221,"id":45,"vpnAccount":"lxk","vpnConnectionName":"lxk","vpnConnectionType":"1","vpnEncryptionLevel":0,"vpnIsEncryption":0,"vpnPassword":"123456","vpnServerAddress":"192.168.1.194","vpnSharedKey":""},{"configId":221,"id":46,"vpnAccount":"lxk","vpnConnectionName":"lxk2","vpnConnectionType":"1","vpnEncryptionLevel":0,"vpnIsEncryption":0,"vpnPassword":"123456","vpnServerAddress":"192.168.1.194","vpnSharedKey":""}],"webclipList":[{"configId":221,"id":40,"webClipImgPath":"uil","webClipName":"baidu","webClipUrl":"www.baidu.com"},{"configId":221,"id":41,"webClipImgPath":"UIL","webClipName":"hao123","webClipUrl":"www.hao123.com"}],"wifiList":[{"configId":221,"isAutoJoin":1,"isHiddenNetwork":1,"macAddress":"50:fa:84:1e:95:12","password":"thetang2307","securityType":2,"ssid":"thetang2.4","wifiConfigId":240}]}
     * code : 147
     * id:0
     */

    private ConfigureStrategyBean ConfigureStrategy;
    private String code;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;

    public ConfigureStrategyBean getConfigureStrategy() {
        return ConfigureStrategy;
    }

    public void setConfigureStrategy(ConfigureStrategyBean ConfigureStrategy) {
        this.ConfigureStrategy = ConfigureStrategy;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static class ConfigureStrategyBean {
        /**
         * adminId : 1
         * adminName : admin
         * apnList : [{"apnName":"123456","apnPassword":"","apnPort":"","apnServerAddress":"","apnUsername":"","configId":0,"id":30}]
         * configId : 221
         * isAllowWifiConfig : 1
         * isApn : 1
         * isVpn : 1
         * isWebclip : 1
         * isWifi : 1
         * issuedNumber : 0
         * lastUpdateTime : {"date":21,"day":4,"hours":9,"minutes":57,"month":8,"seconds":3,"time":1505959023000,"timezoneOffset":-480,"year":117}
         * name : lxk
         * platformType : 0
         * remark : lxk
         * useNumber : 0
         * vpnList : [{"configId":221,"id":45,"vpnAccount":"lxk","vpnConnectionName":"lxk","vpnConnectionType":"1","vpnEncryptionLevel":0,"vpnIsEncryption":0,"vpnPassword":"123456","vpnServerAddress":"192.168.1.194","vpnSharedKey":""},{"configId":221,"id":46,"vpnAccount":"lxk","vpnConnectionName":"lxk2","vpnConnectionType":"1","vpnEncryptionLevel":0,"vpnIsEncryption":0,"vpnPassword":"123456","vpnServerAddress":"192.168.1.194","vpnSharedKey":""}]
         * webclipList : [{"configId":221,"id":40,"webClipImgPath":"uil","webClipName":"baidu","webClipUrl":"www.baidu.com"},{"configId":221,"id":41,"webClipImgPath":"UIL","webClipName":"hao123","webClipUrl":"www.hao123.com"}]
         * wifiList : [{"configId":221,"isAutoJoin":1,"isHiddenNetwork":1,"macAddress":"50:fa:84:1e:95:12","password":"thetang2307","securityType":2,"ssid":"thetang2.4","wifiConfigId":240}]
         */

        private String adminId;
        private String adminName;
        private String configId;
        private String isAllowWifiConfig;
        private String isApn;
        private String isVpn;
        private String isWebclip;
        private String isWifi;
        private String issuedNumber;
       // private LastUpdateTimeBean lastUpdateTime;
       // private String lastUpdateTime;
        private String name;
        private String platformType;
        private String remark;
        private String useNumber;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        private String type;
        private List<ApnListBean> apnList;
        private List<VpnListBean> vpnList;
        private List<WebclipListBean> webclipList;
        private List<WifiListBean> wifiList;

        public String getAdminId() {
            return adminId;
        }

        public void setAdminId(String adminId) {
            this.adminId = adminId;
        }

        public String getAdminName() {
            return adminName;
        }

        public void setAdminName(String adminName) {
            this.adminName = adminName;
        }

        public String getConfigId() {
            return configId;
        }

        public void setConfigId(String configId) {
            this.configId = configId;
        }

        public String getIsAllowWifiConfig() {
            return isAllowWifiConfig;
        }

        public void setIsAllowWifiConfig(String isAllowWifiConfig) {
            this.isAllowWifiConfig = isAllowWifiConfig;
        }

        public String getIsApn() {
            return isApn;
        }

        public void setIsApn(String isApn) {
            this.isApn = isApn;
        }

        public String getIsVpn() {
            return isVpn;
        }

        public void setIsVpn(String isVpn) {
            this.isVpn = isVpn;
        }

        public String getIsWebclip() {
            return isWebclip;
        }

        public void setIsWebclip(String isWebclip) {
            this.isWebclip = isWebclip;
        }

        public String getIsWifi() {
            return isWifi;
        }

        public void setIsWifi(String isWifi) {
            this.isWifi = isWifi;
        }

        public String getIssuedNumber() {
            return issuedNumber;
        }

        public void setIssuedNumber(String issuedNumber) {
            this.issuedNumber = issuedNumber;
        }

       /* public String getLastUpdateTime() {
            return lastUpdateTime;
        }

        public void setLastUpdateTime(String lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
        }*/

      /*  public LastUpdateTimeBean getLastUpdateTime() {
            return lastUpdateTime;
        }

        public void setLastUpdateTime(LastUpdateTimeBean lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
        }*/

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPlatformType() {
            return platformType;
        }

        public void setPlatformType(String platformType) {
            this.platformType = platformType;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getUseNumber() {
            return useNumber;
        }

        public void setUseNumber(String useNumber) {
            this.useNumber = useNumber;
        }

        public List<ApnListBean> getApnList() {
            return apnList;
        }

        public void setApnList(List<ApnListBean> apnList) {
            this.apnList = apnList;
        }

        public List<VpnListBean> getVpnList() {
            return vpnList;
        }

        public void setVpnList(List<VpnListBean> vpnList) {
            this.vpnList = vpnList;
        }

        public List<WebclipListBean> getWebclipList() {
            return webclipList;
        }

        public void setWebclipList(List<WebclipListBean> webclipList) {
            this.webclipList = webclipList;
        }

        public List<WifiListBean> getWifiList() {
            return wifiList;
        }

        public void setWifiList(List<WifiListBean> wifiList) {
            this.wifiList = wifiList;
        }

       /* public static class LastUpdateTimeBean {
            *//**
             * date : 21
             * day : 4
             * hours : 9
             * minutes : 57
             * month : 8
             * seconds : 3
             * time : 1505959023000
             * timezoneOffset : -480
             * year : 117
             *//*

            private String date;
            private String day;
            private String hours;
            private String minutes;
            private String month;
            private String seconds;
            private long time;
            private String timezoneOffset;
            private String year;

            public String getDate() {
                return date;
            }

            public void setDate(String date) {
                this.date = date;
            }

            public String getDay() {
                return day;
            }

            public void setDay(String day) {
                this.day = day;
            }

            public String getHours() {
                return hours;
            }

            public void setHours(String hours) {
                this.hours = hours;
            }

            public String getMinutes() {
                return minutes;
            }

            public void setMinutes(String minutes) {
                this.minutes = minutes;
            }

            public String getMonth() {
                return month;
            }

            public void setMonth(String month) {
                this.month = month;
            }

            public String getSeconds() {
                return seconds;
            }

            public void setSeconds(String seconds) {
                this.seconds = seconds;
            }

            public long getTime() {
                return time;
            }

            public void setTime(long time) {
                this.time = time;
            }

            public String getTimezoneOffset() {
                return timezoneOffset;
            }

            public void setTimezoneOffset(String timezoneOffset) {
                this.timezoneOffset = timezoneOffset;
            }

            public String getYear() {
                return year;
            }

            public void setYear(String year) {
                this.year = year;
            }
        }
*/
        public static class ApnListBean {
            /**
             * apnName : 123456
             * apnPassword :
             * apnPort :
             * apnServerAddress :
             * apnUsername :
             * configId : 0
             * id : 30
             */

            private String apnName;
            private String apnPassword;
            private String apnPort;
            private String apnServerAddress;
            private String apnUsername;
            private String configId;
            private String id;
            private String apn;

            public String getApn() {
                return apn;
            }

            public void setApn(String apn) {
                this.apn = apn;
            }

            public String getApnName() {
                return apnName;
            }

            public void setApnName(String apnName) {
                this.apnName = apnName;
            }

            public String getApnPassword() {
                return apnPassword;
            }

            public void setApnPassword(String apnPassword) {
                this.apnPassword = apnPassword;
            }

            public String getApnPort() {
                return apnPort;
            }

            public void setApnPort(String apnPort) {
                this.apnPort = apnPort;
            }

            public String getApnServerAddress() {
                return apnServerAddress;
            }

            public void setApnServerAddress(String apnServerAddress) {
                this.apnServerAddress = apnServerAddress;
            }

            public String getApnUsername() {
                return apnUsername;
            }

            public void setApnUsername(String apnUsername) {
                this.apnUsername = apnUsername;
            }

            public String getConfigId() {
                return configId;
            }

            public void setConfigId(String configId) {
                this.configId = configId;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            @Override
            public String toString() {
                return "ApnListBean{" +
                        "apnName='" + apnName + '\'' +
                        ", apnPassword='" + apnPassword + '\'' +
                        ", apnPort='" + apnPort + '\'' +
                        ", apnServerAddress='" + apnServerAddress + '\'' +
                        ", apnUsername='" + apnUsername + '\'' +
                        ", configId='" + configId + '\'' +
                        ", id='" + id + '\'' +
                        ", apn='" + apn + '\'' +
                        '}';
            }
        }

        public static class VpnListBean {
            /**
             * configId : 221
             * id : 45
             * vpnAccount : lxk
             * vpnConnectionName : lxk
             * vpnConnectionType : 1
             * vpnEncryptionLevel : 0
             * vpnIsEncryption : 0
             * vpnPassword : 123456
             * vpnServerAddress : 192.168.1.194
             * vpnSharedKey :
             */

            private String configId;
            private String id;
            private String vpnAccount;
            private String vpnConnectionName;
            private String vpnConnectionType;
            private String vpnEncryptionLevel;
            private String vpnIsEncryption;
            private String vpnPassword;
            private String vpnServerAddress;
            private String vpnSharedKey;

            public String getConfigId() {
                return configId;
            }

            public void setConfigId(String configId) {
                this.configId = configId;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getVpnAccount() {
                return vpnAccount;
            }

            public void setVpnAccount(String vpnAccount) {
                this.vpnAccount = vpnAccount;
            }

            public String getVpnConnectionName() {
                return vpnConnectionName;
            }

            public void setVpnConnectionName(String vpnConnectionName) {
                this.vpnConnectionName = vpnConnectionName;
            }

            public String getVpnConnectionType() {
                return vpnConnectionType;
            }

            public void setVpnConnectionType(String vpnConnectionType) {
                this.vpnConnectionType = vpnConnectionType;
            }

            public String getVpnEncryptionLevel() {
                return vpnEncryptionLevel;
            }

            public void setVpnEncryptionLevel(String vpnEncryptionLevel) {
                this.vpnEncryptionLevel = vpnEncryptionLevel;
            }

            public String getVpnIsEncryption() {
                return vpnIsEncryption;
            }

            public void setVpnIsEncryption(String vpnIsEncryption) {
                this.vpnIsEncryption = vpnIsEncryption;
            }

            public String getVpnPassword() {
                return vpnPassword;
            }

            public void setVpnPassword(String vpnPassword) {
                this.vpnPassword = vpnPassword;
            }

            public String getVpnServerAddress() {
                return vpnServerAddress;
            }

            public void setVpnServerAddress(String vpnServerAddress) {
                this.vpnServerAddress = vpnServerAddress;
            }

            public String getVpnSharedKey() {
                return vpnSharedKey;
            }

            public void setVpnSharedKey(String vpnSharedKey) {
                this.vpnSharedKey = vpnSharedKey;
            }
        }

        public static class WebclipListBean {
            /**
             * configId : 221
             * id : 40
             * webClipImgPath : \fileupload\config_img\3d1a57cc-8dd5-4fb4-b390-d30b30462ac4.png
             * webClipName : baidu
             * webClipUrl : www.baidu.com
             */

            private String configId;
            private String id;
            private String webClipImgPath;
            private String webClipName;
            private String webClipUrl;

            public String getConfigId() {
                return configId;
            }

            public void setConfigId(String configId) {
                this.configId = configId;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getWebClipImgPath() {
                return webClipImgPath;
            }

            public void setWebClipImgPath(String webClipImgPath) {
                this.webClipImgPath = webClipImgPath;
            }

            public String getWebClipName() {
                return webClipName;
            }

            public void setWebClipName(String webClipName) {
                this.webClipName = webClipName;
            }

            public String getWebClipUrl() {
                return webClipUrl;
            }

            public void setWebClipUrl(String webClipUrl) {
                this.webClipUrl = webClipUrl;
            }
        }

        public static class WifiListBean {
            /**
             * configId : 221
             * isAutoJoin : 1
             * isHiddenNetwork : 1
             * macAddress : 50:fa:84:1e:95:12
             * password : thetang2307
             * securityType : 2
             * ssid : thetang2.4
             * wifiConfigId : 240
             */

            private String configId;
            private String isAutoJoin;
            private String isHiddenNetwork;
            private String macAddress;
            private String password;
            private String securityType;
            private String ssid;
            private String wifiConfigId;

            public String getConfigId() {
                return configId;
            }

            public void setConfigId(String configId) {
                this.configId = configId;
            }

            public String getIsAutoJoin() {
                return isAutoJoin;
            }

            public void setIsAutoJoin(String isAutoJoin) {
                this.isAutoJoin = isAutoJoin;
            }

            public String getIsHiddenNetwork() {
                return isHiddenNetwork;
            }

            public void setIsHiddenNetwork(String isHiddenNetwork) {
                this.isHiddenNetwork = isHiddenNetwork;
            }

            public String getMacAddress() {
                return macAddress;
            }

            public void setMacAddress(String macAddress) {
                this.macAddress = macAddress;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }

            public String getSecurityType() {
                return securityType;
            }

            public void setSecurityType(String securityType) {
                this.securityType = securityType;
            }

            public String getSsid() {
                return ssid;
            }

            public void setSsid(String ssid) {
                this.ssid = ssid;
            }

            public String getWifiConfigId() {
                return wifiConfigId;
            }

            public void setWifiConfigId(String wifiConfigId) {
                this.wifiConfigId = wifiConfigId;
            }
        }
    }
}

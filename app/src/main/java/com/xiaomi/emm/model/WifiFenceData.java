package com.xiaomi.emm.model;

import java.util.List;

public class WifiFenceData {
    private String alias;
    private String code;
    private String id;
    private List<WifiPolicyBean> policy;

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

    public List<WifiPolicyBean> getPolicy() {
        return policy;
    }

    public void setPolicy(List<WifiPolicyBean> policy) {
        this.policy = policy;
    }

    public static class WifiPolicyBean extends FenceBasePolicyData {
        private List<WifiBean> wifiUnit;

        public List<WifiBean> getWifiBean() {
            return wifiUnit;
        }

        public void setWifiBean(List<WifiBean> wifiUnit) {
            this.wifiUnit = wifiUnit;
        }

        public static class WifiBean {
            /**
             * ssid : wifi name
             * macAddress: bssid
             * wifiUnitDescribe
             */
            private String ssid;
            private String macAddress;
            private String wifiUnitDescribe;

            public String getSsid() {
                return ssid;
            }

            public void setSsid(String ssid) {
                this.ssid = ssid;
            }

            public String getMacAddress() {
                return macAddress;
            }

            public void setMacAddress(String macAddress) {
                this.macAddress = macAddress;
            }

            public String getWifiUnitDescribe() {
                return wifiUnitDescribe;
            }

            public void setWifiUnitDescribe(String wifiUnitDescribe) {
                this.wifiUnitDescribe = wifiUnitDescribe;
            }
        }
    }
}

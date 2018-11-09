package com.zoomtech.emm.model;

import java.util.List;

/**
 * Created by lenovo on 2017/8/22.
 * 安全桌面策略bean
 */

public class ClearDeskData {


    /**
     * code : 139
     * policy : [
     * {"allowNotice":1,
     * "applicationProgram":
     * [{"appName":"33333","appPageName":"44444444444"}],"
     * defaultDesktop":1,
     * "name":"zzxx",
     * "password":"zzxx",
     * "passwordOrNot":1}]
     * sendId : ccb5da7d-4527-4c38-8ef3-8876b6a5c94d
     */
    private String code;
    //private String sendId;
    private String id;
    private List<PolicyBean> policy;


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
         * allowNotice : 1
         * applicationProgram : [{"appName":"33333","appPageName":"44444444444"}]
         * defaultDesktop : 1
         * name : zzxx
         * password : zzxx
         * passwordOrNot : 1
         * id"94
         */

        private String allowNotice;
        private String defaultDesktop;
        private String name;
        private String password;
        private String passwordOrNot;
        private String displayMessage;
        private String displayCall ;
        private String displayContacts;

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

        private List<ApplicationProgramBean> applicationProgram;

        public String getAllowNotice() {
            return allowNotice;
        }

        public void setAllowNotice(String allowNotice) {
            this.allowNotice = allowNotice;
        }

        public String getDefaultDesktop() {
            return defaultDesktop;
        }

        public void setDefaultDesktop(String defaultDesktop) {
            this.defaultDesktop = defaultDesktop;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPasswordOrNot() {
            return passwordOrNot;
        }

        public void setPasswordOrNot(String passwordOrNot) {
            this.passwordOrNot = passwordOrNot;
        }

        public List<ApplicationProgramBean> getApplicationProgram() {
            return applicationProgram;
        }

        public void setApplicationProgram(List<ApplicationProgramBean> applicationProgram) {
            this.applicationProgram = applicationProgram;
        }

        public static class ApplicationProgramBean {
            /**
             * appName : 33333
             * packageName : 44444444444
             */

            private String appName;
            private String packageName;

            public String getAppName() {
                return appName;
            }

            public void setAppName(String appName) {
                this.appName = appName;
            }

            public String getPackageName() {
                return packageName;
            }

            public void setPackageName(String packageName) {
                this.packageName = packageName;
            }

            @Override
            public String toString() {
                return "ApplicationProgramBean{" +
                        "appName='" + appName + '\'' +
                        ", packageName='" + packageName + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "PolicyBean{" +
                    "allowNotice='" + allowNotice + '\'' +
                    ", defaultDesktop='" + defaultDesktop + '\'' +
                    ", name='" + name + '\'' +
                    ", password='" + password + '\'' +
                    ", passwordOrNot='" + passwordOrNot + '\'' +
                    ", applicationProgram=" + applicationProgram +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ClearDeskData{" +
                ", code='" + code + '\'' +
                ", policy=" + policy +
                '}';
    }
}

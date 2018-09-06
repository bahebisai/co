package com.xiaomi.emm.model;

/**
 * Created by Administrator on 2017/6/13.
 */
public class TelephoyWhiteUser {
    String userName;
    String userId;
    String userAddress;
    String telephonyNumber;
    String shortPhoneNum;
    String loginName;

    public String getShortPhoneNum() {
        return shortPhoneNum;
    }

    public void setShortPhoneNum(String shortPhoneNum) {
        this.shortPhoneNum = shortPhoneNum;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getUserAddress() {
        return this.userAddress;
    }

    public void setTelephonyNumber(String telephonyNumber) {
        this.telephonyNumber = telephonyNumber;
    }

    public String getTelephonyNumber() {
        return this.telephonyNumber;
    }

}

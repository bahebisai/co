package com.xiaomi.emm.model;

import java.util.ArrayList;

/**
 * Created by lenovo on 2017/9/22.
 */

public class PhoneLog {

    //  incomingNumber     System.currentTimeMillis()
    // PreferencesManager.getSingleInstance().getData(Common.alias);

    private String alias;       //被呼叫用户


    public ArrayList<PhoneData> getPhoneBeanList() {
        return phoneBeanList;
    }

    public void setPhoneBeanList(ArrayList<PhoneData> phoneBeanList) {
        this.phoneBeanList = phoneBeanList;
    }

    private ArrayList<PhoneData> phoneBeanList;



    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public static class  PhoneData{
        private String callNumber; //来电号码
        private String callTime;   //来电时间

        public void setCallNumber(String callNumber) {
            this.callNumber = callNumber;
        }


        public String getCallNumber() {
            return callNumber;
        }

        public String getCallTime() {
            return callTime;
        }

        public void setCallTime(String callTime) {
            this.callTime = callTime;
        }

    }
}

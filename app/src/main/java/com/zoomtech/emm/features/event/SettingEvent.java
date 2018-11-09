package com.zoomtech.emm.features.event;

/**
 * Created by Administrator on 2017/10/16.
 */

public class SettingEvent {
    public SettingEvent() {
    }

    private String msg;
    public  SettingEvent(String msg){
        this.msg=msg;
    }

    public String getMsg() {
        return msg;
    }
}

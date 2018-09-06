package com.xiaomi.emm.features.event;

/**
 * Created by Administrator on 2017/7/28.
 */

public class NotifyEvent {
    public NotifyEvent() {
    }

    private String msg;
    public  NotifyEvent(String msg){
        this.msg=msg;
    }

    public String getMsg() {
        return msg;
    }
}

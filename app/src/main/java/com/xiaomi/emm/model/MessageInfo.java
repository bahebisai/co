package com.xiaomi.emm.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/14.
 */

public class MessageInfo implements Serializable {
    String message_icon = null;
    String message_from = null;
    String message_time = null;
    String message_id = null;
    String message_about = null;

    public void setMessage_icon(String message_icon) {
        this.message_icon = message_icon;
    }

    public String getMessage_icon() {
        return message_icon;
    }

    public void setMessage_from(String message_from) {
        this.message_from = message_from;
    }

    public String getMessage_from() {
        return message_from;
    }

    public void setMessage_time(String message_time) {
        this.message_time = message_time;
    }

    public String getMessage_time() {
        return message_time;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_about(String message_about) {
        this.message_about = message_about;
    }

    public String getMessage_about() {
        return message_about;
    }
}

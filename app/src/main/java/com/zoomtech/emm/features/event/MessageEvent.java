package com.zoomtech.emm.features.event;

import com.zoomtech.emm.model.MessageInfo;

/**
 * Created by Administrator on 2017/7/18.
 */

public class MessageEvent {
    private MessageInfo messageInfo;

    public MessageEvent(){

    }
    public MessageEvent(MessageInfo messageInfo) {
        this.messageInfo = messageInfo;
    }

    public MessageInfo getMessageInfo() {
        return messageInfo;
    }
}

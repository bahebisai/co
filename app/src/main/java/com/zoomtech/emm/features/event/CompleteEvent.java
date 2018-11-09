package com.zoomtech.emm.features.event;

import com.zoomtech.emm.model.DownLoadEntity;

/**
 * Created by Administrator on 2018/1/13.
 */

public class CompleteEvent {

    String type;
    String result;
    String id;

    public CompleteEvent(String type, String result, String id) {
        this.type = type;
        this.result = result;
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public String getResult() {
        return result;
    }

    public String getId() {
        return id;
    }
}

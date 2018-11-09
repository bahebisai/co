package com.zoomtech.emm.features.event;

import com.zoomtech.emm.model.DownLoadEntity;

/**
 * Created by Administrator on 2017/7/20.
 */

public class APKEvent {
    DownLoadEntity downLoadEntity;
    int code;
    public APKEvent(DownLoadEntity downLoadEntity ,int code) {
        this.downLoadEntity = downLoadEntity;
        this.code = code;
    }

    public DownLoadEntity getDownLoadEntity() {
        return downLoadEntity;
    }

    public int getCode() {
        return code;
    }
}

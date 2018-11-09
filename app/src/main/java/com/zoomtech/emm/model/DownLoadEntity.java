package com.zoomtech.emm.model;

import java.util.List;

/**
 * Created by Administrator on 2017/7/14.
 */

public class DownLoadEntity {
    public int dataId;
    /**
     * 0: app
     * 1: 文件
     * 2: 图片
     */
    public String type;
    public String app_id;
    public String sendId;
    public String code;
    public long start;
    //public int end;
    public long downed;
    public long total;
    public String saveName;
    public String internet;
    public String uninstall;
    //public List<DownLoadEntity> mMuliteList;

    public String packageName;
    public String version;
}

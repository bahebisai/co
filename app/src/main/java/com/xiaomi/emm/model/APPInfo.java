package com.xiaomi.emm.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/6/20.
 */

public class APPInfo implements Serializable {
    String app_id = null;
    String appName = null;
    String packageName = null;
    String version = null;
    String size = null;
    public void setAppId(String app_id) {
        this.app_id = app_id;
    }

    public String getAppId() {
        return app_id;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppName() {
        return this.appName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSize() {
        return size;
    }
    @Override
    public String toString() {
        return "APPInfo{" +
                "app_id='" + app_id + '\'' +
                ", appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", version='" + version + '\'' +
                ", size=" + size +
                '}';
    }
}

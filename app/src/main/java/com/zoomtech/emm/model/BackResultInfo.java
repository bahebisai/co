package com.zoomtech.emm.model;

/**
 * Created by lenovo on 2017/10/19.
 */

public class BackResultInfo {

    private String id;
    private  String type;
    private String data;  //json格式


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BackResultInfo{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", data='" + data + '\'' +
                '}';
    }

}

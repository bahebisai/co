package com.zoomtech.emm.model;

import com.zoomtech.emm.model.Token;

import java.io.Serializable;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;

/**
 * Created by Administrator on 2017/6/14.
 */

public class LoginBackData extends ResponseBody implements Serializable {
    private int result;
    private Token message;
    private String serializedName;
    private String timeStamp;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public Token getToken() {
        return message;
    }

    public void setToken(Token message) {
        this.message = message;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public void setSerializedName(String result) {
        this.serializedName = serializedName;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public MediaType contentType() {
        return null;
    }

    @Override
    public long contentLength() {
        return 0;
    }

    @Override
    public BufferedSource source() {
        return null;
    }
}

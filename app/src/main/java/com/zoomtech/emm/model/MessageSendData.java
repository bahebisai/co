package com.zoomtech.emm.model;

/**
 * Created by Administrator on 2017/10/27.
 */

public class MessageSendData {
    private String mDbId;
    private String mUrl;
    private int mSendCode;
    private String mJsonContent;
    private boolean mNeedResend;//add to db to resend

    public MessageSendData(int sendCode, String jsonContent, boolean needResend) {
        mSendCode = sendCode;
        mJsonContent = jsonContent;
        mNeedResend = needResend;
    }

    public void setId(String id) {
        mDbId = id;
    }

    public String getId() {
        return mDbId;
    }

    public int getSendCode() {
        return mSendCode;
    }

    public String getJsonContent() {
        return mJsonContent;
    }

    public boolean needResend() {
        return mNeedResend;
    }

}

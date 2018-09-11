package com.xiaomi.emm.model;

import com.xiaomi.emm.features.impl.SendMessageManager.MessageSendListener;
import com.xiaomi.emm.features.resend.MessageResendManager.MessageResendListener;

/**
 * Created by Administrator on 2017/10/27.
 */

public class MessageSendData {
    private String resend_id;
    private String mUrl;
    private int mSendCode;
    private String mJsonContent;
    private boolean mNeedResend;//add to db to resend
    private MessageResendListener mResendListener;
    private MessageSendListener mSendListener;

    public MessageSendData(int orderCode, String jsonContent, boolean needResend, MessageSendListener sendListener, MessageResendListener resendListener) {
        mSendCode = orderCode;
        mJsonContent = jsonContent;
        mNeedResend = needResend;
        mSendListener = sendListener;
        mResendListener = resendListener;
    }

    public int getOrderCode() {
        return mSendCode;
    }

    public String getJsonContent() {
        return mJsonContent;
    }

    public boolean needResend() {
        return mNeedResend;
    }

    public MessageResendListener getResendListener() {
        return mResendListener;
    }

    public MessageSendListener getSendListener() {
         return mSendListener;
    }
}

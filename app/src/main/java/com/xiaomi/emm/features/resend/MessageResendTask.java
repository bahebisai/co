package com.xiaomi.emm.features.resend;

import android.content.Context;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.impl.AppImpl;
import com.xiaomi.emm.features.impl.CallRecorderUploadImpl;
import com.xiaomi.emm.features.impl.DeviceImpl;
import com.xiaomi.emm.features.impl.DeviceUpdateImpl;
import com.xiaomi.emm.features.impl.ExcuteCompleteImpl;
import com.xiaomi.emm.features.impl.FeedBackImpl;
import com.xiaomi.emm.features.impl.IccidImpl;
import com.xiaomi.emm.features.impl.PasswordImpl;
import com.xiaomi.emm.features.impl.SmsBackupImpl;
import com.xiaomi.emm.features.impl.SystemImpl;
import com.xiaomi.emm.features.impl.TelephoneWhiteListImpl;
import com.xiaomi.emm.features.impl.WhiteTelephoneImpl;
import com.xiaomi.emm.model.MessageResendData;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.TheTang;

import okhttp3.RequestBody;

/**
 * Created by Administrator on 2017/10/26.
 */

public class MessageResendTask {

    public static final String TAG = "MessageResendTask";

    MessageResendData mMessageResendData = null;
    MessageResendManager.MessageResendListener mMessageResendListener = null;
    static Context mContext = TheTang.getSingleInstance().getContext();

    public MessageResendTask( MessageResendData mMessageResendData, MessageResendManager.MessageResendListener mMessageResendListener) {
        this.mMessageResendData = mMessageResendData;
        this.mMessageResendListener = mMessageResendListener;
    }

    /**
     * 反馈
     */
    public void resendBack() {

        String type = mMessageResendData.resend_type;

        LogUtil.writeToFile( TAG, "resend type = " + type );

        switch(Integer.valueOf( type )) {

            case Common.app_impl:
                resendApp();
                break;
            /*case Common.coming_number_impl:
                resendComingNumber();
                break;*/
            case Common.device_impl:
                resendDevice();
                break;
            case Common.device_update:
                resendDeviceUpdate();
                break;
            /*case Common.download_impl:
                //resendDownload(messageResendData);
                break;*/
            case Common.iccid_impl:
                resendIccid();
                break;
            /*case Common.log_upload_impl:
                resendLogUpload();
                break;*/
            case Common.password_impl:
                resendPassword();
                break;
            /*case Common.switch_log_impl:
                resendSwitch();
                break;*/
            /*case Common.sync_data_impl:
                resendSyncData();
                break;*/
            case Common.sd_card:
                resendSystem();
                break;
            case Common.machine_card:
                resendSystem();
                break;
            /*case Common.webclip_image_impl:
                resendWebclip(messageResendData);
                break;*/
            case Common.get_telephone_white:
                resendGetTelephoneWhite();
                break;
            case Common.white_telephone_status:
                resendWhiteTelephoneStatus();
                break;
            case Common.SMS_BACKUP:
                resendSmsInfo();
                break;
            case Common.CALL_RECORDER_BACKUP:
                resendCallRecorder();
                break;
            default:
                break;
        }
    }

    /**
     * feedback重发
     */
    private void resendFeedback() {

        RequestBody body = TheTang.getSingleInstance().jsonToRequestBody(mMessageResendData.resend_content);

        FeedBackImpl feedBack = new FeedBackImpl(mContext);
        feedBack.reSendFeedback( mMessageResendListener, body);
    }

    /**
     * app重发
     */
    private void resendApp() {

        if (mMessageResendData.resend_content == null) {
            return;
        }

        //解决有多个应用的情况
        int position = mMessageResendData.resend_content.indexOf( "," );

        String type = mMessageResendData.resend_content.substring( 0, position );
        String names = mMessageResendData.resend_content.substring( position + 1 );
        AppImpl appImpl = new AppImpl(mContext);
        appImpl.reSendAppCompliance( mMessageResendListener, type, names);
    }

    /**
     * 电话记录重发
     */
   /* private void resendComingNumber() {
        RequestBody body = TheTang.getSingleInstance().jsonToRequestBody(mMessageResendData.resend_content);
        ComingNumberLogImpl comingNumberLogImpl = new ComingNumberLogImpl(mContext);
        comingNumberLogImpl.reSendComingNumberLog( mMessageResendListener, body );
    }*/

    /**
     * 设备详情重发
     */
    private void resendDevice() {
        RequestBody body = TheTang.getSingleInstance().jsonToRequestBody(mMessageResendData.resend_content);
        DeviceImpl deviceImpl = new DeviceImpl(mContext);
        deviceImpl.reSendDeviceInfo( mMessageResendListener, body);
    }

    /**
     * 设备更新重发
     */
    private void resendDeviceUpdate() {
        DeviceUpdateImpl deviceUpdateImpl = new DeviceUpdateImpl(mContext);
        deviceUpdateImpl.reSendDeviceUpdate( mMessageResendListener);
    }

    /**
     * Iccid重发
     */
    private void resendIccid() {
        RequestBody body = TheTang.getSingleInstance().jsonToRequestBody(mMessageResendData.resend_content);
        IccidImpl iccidImpl = new IccidImpl(mContext);
        iccidImpl.reSendIccid( mMessageResendListener, body);
    }

    /**
     * SmsBackupInfo重发
     */
    private void resendSmsInfo() {
        RequestBody body = TheTang.getSingleInstance().jsonToRequestBody(mMessageResendData.resend_content);
        SmsBackupImpl smsBackup = new SmsBackupImpl(mContext);
        smsBackup.resendSmsInfo(mMessageResendListener, body);
    }

    /**
     * CallRecorder重发
     */
    private void resendCallRecorder() {
        CallRecorderUploadImpl callRecorderUpload = new CallRecorderUploadImpl(mContext);
        callRecorderUpload.resendCallRecorder(mMessageResendListener, mMessageResendData.resend_content);
    }

    /**
     * Log上传重发
     */
    private void resendLogUpload() {
        ///////
    }

    /**
     * 密码重发
     */
    private void resendPassword() {

        if (mMessageResendData.resend_content == null) {
            return;
        }

        String password = mMessageResendData.resend_content;

        PasswordImpl passwordImpl = new PasswordImpl(mContext);
        passwordImpl.reSendFeedbackPassword( mMessageResendListener, password );
    }

    /**
     * 域切换记录重发
     */
    private void resendSwitch() {
        ///
    }

    /**
     * 数据同步重发
     */
    private void resendSyncData() {

    }

    /**
     * system重发
     */
    private void resendSystem() {
        if (mMessageResendData.resend_content == null) {
            return;
        }

        String[] data = mMessageResendData.resend_content.split(",");
        String type = data[0];
        String state = data[1];
        SystemImpl systemImpl = new SystemImpl(mContext);
        systemImpl.reSendSystemCompliance( mMessageResendListener, type, state);
    }

    /**
     * 执行重发
     */
    private void resendExcute() {
        if (mMessageResendData.resend_content == null) {
            return;
        }

        RequestBody body = TheTang.getSingleInstance().jsonToRequestBody(mMessageResendData.resend_content);
        ExcuteCompleteImpl excuteCompleteImpl = new ExcuteCompleteImpl(mContext);
        //excuteCompleteImpl.reSendExcuteComplete( mMessageResendListener, body);
    }

    /**
     * 命令执行完成返回
     */
    private void resendComplete() {
        if (mMessageResendData.resend_content == null) {
            return;
        }

        String[] data = mMessageResendData.resend_content.split(",");
        String orderCode = data[0];
        String result = data[1];

        //TheTang.getSingleInstance().sendExcuteComplete( orderCode, result );

    }

    /**
     * 电话白名单请求重发
     */
    private void resendGetTelephoneWhite() {
        RequestBody body = TheTang.getSingleInstance().jsonToRequestBody(mMessageResendData.resend_content);
        TelephoneWhiteListImpl mTelephoneWhiteListImpl = new TelephoneWhiteListImpl(mContext);
        mTelephoneWhiteListImpl.reSendGetTelephoneWhiteList(mMessageResendListener, body);
    }

    private void resendWhiteTelephoneStatus() {
        if (mMessageResendData.resend_content == null) {
            return;
        }

        String status = mMessageResendData.resend_content;

        WhiteTelephoneImpl mWhiteTelephoneImpl = new WhiteTelephoneImpl(mContext);
        mWhiteTelephoneImpl.reSendWhiteTelephoneStatus(mMessageResendListener, status);
    }


}

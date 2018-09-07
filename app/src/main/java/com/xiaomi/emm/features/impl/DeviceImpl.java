package com.xiaomi.emm.features.impl;


import android.content.Context;

import com.xiaomi.emm.definition.UrlConst;
import com.xiaomi.emm.features.http.RequestService;
import com.xiaomi.emm.features.resend.MessageResendManager;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.TheTang;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/9/6.
 */

public class DeviceImpl extends BaseImpl<RequestService>{

    private static final String TAG = "DeviceImpl";
    Context mContext;

    public DeviceImpl(Context context) {
        super();
        this.mContext = context;
    }

    public void sendDeviceInfo(final String deviceInfo) {

        RequestBody body = TheTang.getSingleInstance().jsonToRequestBody( deviceInfo );
        LogUtil.writeToFile( TAG,deviceInfo );
//        mService.postDeviceInfo( body ).enqueue( new Callback<ResponseBody>() {
        mService.uploadInfo(UrlConst.DEVICE_INFO, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!TheTang.getSingleInstance().whetherSendSuccess(response)) {
                    //DatabaseOperate.getSingleInstance().add_backResult_sql(Common.device_impl + "", deviceInfo);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //DatabaseOperate.getSingleInstance().add_backResult_sql(Common.device_impl + "", deviceInfo);
            }
        } );
    }

    /**
     * 重发
     * @param deviceInfo
     */
    public void reSendDeviceInfo(final MessageResendManager.ResendListener listener, RequestBody deviceInfo) {
//        mService.postDeviceInfo( deviceInfo ).enqueue( new Callback<ResponseBody>() {
        mService.uploadInfo(UrlConst.DEVICE_INFO, deviceInfo).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (TheTang.getSingleInstance().whetherSendSuccess(response)) {
                    listener.resendSuccess();
                } else {
                    listener.resendError();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.resendFail();
            }
        } );
    }
}

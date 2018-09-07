package com.xiaomi.emm.features.impl;

import android.content.Context;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.UrlConst;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.http.RequestService;
import com.xiaomi.emm.features.resend.MessageResendManager;
import com.xiaomi.emm.utils.TheTang;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/9/27.
 */

public class DeviceUpdateImpl extends BaseImpl<RequestService> {
    private static final String TAG = "DeviceUpdateImpl";
    Context mContext;

    public DeviceUpdateImpl(Context context) {
        super();
        this.mContext = context;
    }

    public void deviceUpdate() {
//        mService.deviceUpdate().enqueue(new Callback<ResponseBody>() {
        mService.getInfo(UrlConst.DEVICE_UPDATE).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!TheTang.getSingleInstance().whetherSendSuccess(response)) {
                    DatabaseOperate.getSingleInstance().add_backResult_sql(Common.device_update + "", "null");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                DatabaseOperate.getSingleInstance().add_backResult_sql(Common.device_update + "", "null");
            }
        });
    }

    /**
     * 重发
     */
    public void reSendDeviceUpdate(final MessageResendManager.ResendListener listener) {
//        mService.deviceUpdate().enqueue(new Callback<ResponseBody>() {
        mService.getInfo(UrlConst.DEVICE_UPDATE).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (TheTang.getSingleInstance().whetherSendSuccess(response)) {
                    listener.resendSuccess(  );
                } else {
                    listener.resendError();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.resendFail();
            }
        });
    }
}

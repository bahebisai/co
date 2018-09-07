package com.xiaomi.emm.features.impl;

import android.content.Context;
import android.util.ArrayMap;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.UrlConst;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.http.RequestService;
import com.xiaomi.emm.features.resend.MessageResendManager;
import com.xiaomi.emm.utils.TheTang;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WhiteTelephoneImpl extends BaseImpl<RequestService> {

    private static final String TAG = "WhiteTelephoneImpl";
    Context mContext;

    public WhiteTelephoneImpl(Context context) {
        super();
        this.mContext = context;
    }

    public void sendWhiteTelephoneStatus(final String status) {
        Map<String, String> map = new ArrayMap<>();
        map.put("status", status);
//        mService.sendWhiteTelephoneStatus(status).enqueue(new Callback<ResponseBody>() {
        mService.getInfo(UrlConst.TELE_WHITELIST_STATUS, map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!TheTang.getSingleInstance().whetherSendSuccess(response)) {
                    DatabaseOperate.getSingleInstance().add_backResult_sql(Common.white_telephone_status + "", status);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                DatabaseOperate.getSingleInstance().add_backResult_sql(Common.white_telephone_status + "", status);
            }
        });

    }

    /**
     * 重发
     *
     * @param listener
     * @param status
     */
    public void reSendWhiteTelephoneStatus(final MessageResendManager.ResendListener listener, String status) {
        Map<String, String> map = new ArrayMap<>();
        map.put("status", status);
//        mService.sendWhiteTelephoneStatus( status ).enqueue( new Callback<ResponseBody>() {
        mService.getInfo(UrlConst.TELE_WHITELIST_STATUS, map).enqueue(new Callback<ResponseBody>() {
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
        });
    }
}

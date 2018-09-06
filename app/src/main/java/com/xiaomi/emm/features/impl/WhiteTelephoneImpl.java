package com.xiaomi.emm.features.impl;

import android.content.Context;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.http.WhiteTelephoneService;
import com.xiaomi.emm.features.resend.MessageResendManager;
import com.xiaomi.emm.utils.TheTang;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WhiteTelephoneImpl extends BaseImpl<WhiteTelephoneService> {

    private static final String TAG = "WhiteTelephoneImpl";
    Context mContext;

    public WhiteTelephoneImpl(Context context) {
        super();
        this.mContext = context;
    }

    public void sendWhiteTelephoneStatus(final String status) {

        mService.sendWhiteTelephoneStatus(status).enqueue(new Callback<ResponseBody>() {
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
     * @param listener
     * @param status
     */
    public void reSendWhiteTelephoneStatus(final MessageResendManager.ResendListener listener, String status) {

        mService.sendWhiteTelephoneStatus( status ).enqueue( new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (TheTang.getSingleInstance().whetherSendSuccess( response )) {
                    listener.resendSuccess(  );
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

package com.xiaomi.emm.features.impl;

import android.app.Service;
import android.content.Context;
import android.telephony.TelephonyManager;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.UrlConst;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.http.RequestService;
import com.xiaomi.emm.features.resend.MessageResendManager;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/9/15.
 */

public class IccidImpl extends BaseImpl<RequestService> {

    private static final String TAG = "IccidImpl";
    Context mContext;

    public IccidImpl(Context context) {
        super();
        this.mContext = context;
    }

    public void sendIccid(String imsi, String iccid) {

        if (iccid == null) {
            return;
        }

        final JSONObject logObject = new JSONObject(  );
        try {
            logObject.put( "alias", PreferencesManager.getSingleInstance().getData( Common.alias ) );
            logObject.put( "iccid", iccid );
            logObject.put( "imsi", imsi );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final RequestBody body = RequestBody.create( okhttp3.MediaType.parse(
                "application/json;charset=UTF-8" ), logObject.toString() );

//        mService.sendIccid( body ).enqueue( new Callback<ResponseBody>() {
        mService.uploadInfo(UrlConst.PHONE_INFO, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (!TheTang.getSingleInstance().whetherSendSuccess(response)) {
                    DatabaseOperate.getSingleInstance().add_backResult_sql(Common.iccid_impl + "",logObject.toString());
                    LogUtil.writeToFile( TAG, "upload fail iccid = " + logObject.toString() );
                }
                LogUtil.writeToFile( TAG, "upload success iccid = " + logObject.toString() );
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                DatabaseOperate.getSingleInstance().add_backResult_sql(Common.iccid_impl + "",logObject.toString());
                LogUtil.writeToFile( TAG, "upload fail iccid = " + logObject.toString() );
            }
        } );
    }

    /**
     * 重发
     * @param listener
     * @param body
     */
    public void reSendIccid(final MessageResendManager.ResendListener listener, RequestBody body) {

//        mService.sendIccid( body ).enqueue( new Callback<ResponseBody>() {
        mService.uploadInfo(UrlConst.PHONE_INFO, body).enqueue(new Callback<ResponseBody>() {
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

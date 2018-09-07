package com.xiaomi.emm.features.impl;

import android.content.Context;
import android.util.Log;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.UrlConst;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.http.RequestService;
import com.xiaomi.emm.features.policy.sms.SmsBackupInfo;
import com.xiaomi.emm.features.resend.MessageResendManager;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.TheTang;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/9/15.
 */

public class SmsBackupImpl extends BaseImpl<RequestService> {

    private static final String TAG = "SmsBackup";
    Context mContext;

    public SmsBackupImpl(Context context) {
        super();
        this.mContext = context;
    }

    public void sendSmsInfo(String id, SmsBackupInfo info) {

        if (info == null) {
            return;
        }

        final JSONObject smsObject = new JSONObject();
        try {
            smsObject.put("strategyId", id);
            smsObject.put("smsTime", info.getDate());
            smsObject.put("content", info.getBody());
            smsObject.put("communicationName", info.getPerson());
            smsObject.put("communicationNumber", info.getAddress());
            smsObject.put("type", info.getType());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final RequestBody body = RequestBody.create(okhttp3.MediaType.parse(
                "application/json;charset=UTF-8"), smsObject.toString());
//        mService.sendSmsInfo(body).enqueue( new Callback<ResponseBody>() {
        mService.uploadInfo(UrlConst.SMS, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                Log.d("baii", "upload json " + smsObject.toString());
                LogUtil.writeToFile(TAG, "sms upload json = " + smsObject.toString());
                if (!TheTang.getSingleInstance().whetherSendSuccess(response)) {
                    DatabaseOperate.getSingleInstance().add_backResult_sql(Common.SMS_BACKUP + "", smsObject.toString());
                    LogUtil.writeToFile(TAG, "sms backup failed 111 = " + smsObject.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                DatabaseOperate.getSingleInstance().add_backResult_sql(Common.SMS_BACKUP + "", smsObject.toString());
                LogUtil.writeToFile(TAG, "sms backup failed  = " + smsObject.toString());
            }
        });
    }

    /**
     * 重发
     *
     * @param listener
     * @param body
     */
    public void resendSmsInfo(final MessageResendManager.ResendListener listener, RequestBody body) {
//        Log.d("baii", "resend sms info");
        LogUtil.writeToFile(TAG, "resend sms info");
        mService.uploadInfo(UrlConst.SMS, body).enqueue(new Callback<ResponseBody>() {
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

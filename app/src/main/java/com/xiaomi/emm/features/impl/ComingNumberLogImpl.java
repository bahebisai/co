package com.xiaomi.emm.features.impl;

import android.content.Context;
import android.util.Log;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.http.ComingNumberLogService;
import com.xiaomi.emm.features.resend.MessageResendManager;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by lenovo on 2017/9/22.
 */

public class ComingNumberLogImpl extends BaseImpl<ComingNumberLogService> {

    private static final String TAG = "SwitchLogImpl";
    Context mContext;

    public ComingNumberLogImpl(Context mContext) {
        this.mContext = mContext;
    }

    public void sendComingNumberLog( String log ) {

        final RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"), log);

        mService.sendComingNumberLog(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.w(TAG, "---" + response.toString());
                if (TheTang.getSingleInstance().whetherSendSuccess(response)) {
                    PreferencesManager.getSingleInstance().clearComingNumberLog();//清除数据
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.w(TAG, "---上传来电显示日志失败");
            }
        });
    }
}

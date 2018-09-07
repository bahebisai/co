package com.xiaomi.emm.features.impl;

import android.content.Context;
import android.util.ArrayMap;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.UrlConst;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.http.RequestService;
import com.xiaomi.emm.features.resend.MessageResendManager;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/10/16.
 */

public class PasswordImpl extends BaseImpl<RequestService> {
    private static final String TAG = "PasswordImpl";
    Context mContext;

    public PasswordImpl(Context context) {
        super();
        this.mContext = context;
    }

    public void feedbackPassword(final String password ) {
//        mService.feedbackPassword( PreferencesManager.getSingleInstance().getData( "alias" ), password )
        Map<String, String> map = new ArrayMap<>();
        map.put("alias", PreferencesManager.getSingleInstance().getData( "alias"));
        map.put("password", password);
        mService.getInfo(UrlConst.FEEDBACK_PASSWORD, map)
                .enqueue( new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (!TheTang.getSingleInstance().whetherSendSuccess(response)) {
                            DatabaseOperate.getSingleInstance().add_backResult_sql(Common.password_impl + "", password);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        DatabaseOperate.getSingleInstance().add_backResult_sql(Common.password_impl + "", password);
                    }
                } );
    }

    /**
     * 重发
     * @param password
     */
    public void reSendFeedbackPassword(final MessageResendManager.ResendListener listener, final String password ) {
//        mService.feedbackPassword( PreferencesManager.getSingleInstance().getData( "alias" ), password )
        Map<String, String> map = new ArrayMap<>();
        map.put("alias", PreferencesManager.getSingleInstance().getData( "alias"));
        map.put("password", password);
        mService.getInfo(UrlConst.FEEDBACK_PASSWORD, map)
                .enqueue( new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (TheTang.getSingleInstance().whetherSendSuccess( response )) {
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

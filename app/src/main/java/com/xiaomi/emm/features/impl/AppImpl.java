package com.xiaomi.emm.features.impl;

import android.content.Context;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.http.AppComplianceService;
import com.xiaomi.emm.features.resend.MessageResendManager;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/8/22.
 */

public class AppImpl extends BaseImpl<AppComplianceService> {
    private static final String TAG = "AppImpl";
    Context mContext;

    String alias = PreferencesManager.getSingleInstance().getData( Common.alias );
    String appComplianceId = PreferencesManager.getSingleInstance().getComplianceData( Common.app_compliance_id );

    public AppImpl(Context context) {
        super();
        this.mContext = context;
    }

    public void sendAppCompliance(final String type, final String names) {

        mService.appCompliance( alias, Integer.parseInt( appComplianceId ), type, names ).enqueue( new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!TheTang.getSingleInstance().whetherSendSuccess( response )) {
                    DatabaseOperate.getSingleInstance().add_backResult_sql(Common.app_impl + "", type + "," + names);
                }

                //违规时执行
                if ("0".equals(type)) {
                    TheTang.getSingleInstance().excuteAppCompliance();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                DatabaseOperate.getSingleInstance().add_backResult_sql(Common.app_impl + "", type + "," + names);
                //违规时执行
                if ("0".equals(type)) {
                    TheTang.getSingleInstance().excuteAppCompliance();
                }
            }
        } );
    }

    /**
     * 重发
     * @param type
     * @param names
     */
    public void reSendAppCompliance(final MessageResendManager.ResendListener listener, final String type, final String names) {

        mService.appCompliance( alias, Integer.parseInt( appComplianceId ), type, names ).enqueue( new Callback<ResponseBody>() {
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

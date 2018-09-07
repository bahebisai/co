package com.xiaomi.emm.features.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.UrlConst;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.http.RequestService;
import com.xiaomi.emm.features.resend.MessageResendManager;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.MDM;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/8/15.
 */

public class SystemImpl extends BaseImpl<RequestService> {
    private static final String TAG = "SystemImpl";
    Context mContext;
    String alias = PreferencesManager.getSingleInstance().getData( Common.alias);
    String systemComplianceId = PreferencesManager.getSingleInstance().getComplianceData(Common.system_compliance_id);

    public SystemImpl(Context context) {
        super();
        this.mContext = context;
    }

    public void sendSystemCompliance(final int system_type, final String type, final String state) {

        //added by duanxin for bug146 on 2017/09/29
        if (systemComplianceId == null || alias == null) {
            return;
        }
//        public Call<ResponseBody> systemCompliance(@Query("alias") String alias, @Query("systemComplianceId") int systemComplianceId,  @Query("state") String state, @Query("type") String type);
        Map<String, String> map = new ArrayMap<>();
        map.put("alias", alias);
        map.put("systemComplianceId", systemComplianceId);
        map.put("state", state);
        map.put("type", type);
//        mService.systemCompliance(alias,Integer.parseInt(systemComplianceId),state,type).enqueue( new Callback<ResponseBody>() {
        mService.getInfo(UrlConst.SYSTEM_COMPLIANCE, map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!TheTang.getSingleInstance().whetherSendSuccess(response)) {
                    DatabaseOperate.getSingleInstance().add_backResult_sql(system_type + "", type + "," + state);
                }

                //违规时执行
                if ("0".equals(type)) {
                    LogUtil.writeToFile( TAG, "systemCompliance!" );
                    MDM.excuteSystemCompliance();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                DatabaseOperate.getSingleInstance().add_backResult_sql(system_type + "", type + "," + state);
                //违规时执行
                if ("0".equals(type)) {
                    MDM.excuteSystemCompliance();
                }
            }
        } );
    }

    /**
     * 重发
     * @param listener
     * @param type
     * @param state
     */
    public void reSendSystemCompliance(final MessageResendManager.ResendListener listener, String type, String state) {

        //added by duanxin for bug146 on 2017/09/29
        if (systemComplianceId == null || alias == null) {
            return;
        }
        Map<String, String> map = new ArrayMap<>();
        map.put("alias", alias);
        map.put("systemComplianceId", systemComplianceId);
        map.put("state", state);
        map.put("type", type);
//        mService.systemCompliance(alias,Integer.parseInt(systemComplianceId),state,type).enqueue( new Callback<ResponseBody>() {
        mService.getInfo(UrlConst.SYSTEM_COMPLIANCE, map).enqueue(new Callback<ResponseBody>() {
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

package com.xiaomi.emm.features.impl;

import android.content.Context;
import android.util.ArrayMap;
import android.util.Log;

import com.xiaomi.emm.definition.Common;
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
 * Created by Administrator on 2018/2/27.
 */

public class UpdateAPPVersionImpl extends BaseImpl<RequestService>{
    private static final String TAG = "UpdateAPPVersionImpl";
    Context mContext;

    String alias = PreferencesManager.getSingleInstance().getData( Common.alias );
    String newVersion = TheTang.getSingleInstance().getAppVersion( Common.packageName );

    public UpdateAPPVersionImpl(Context context) {
        super();
        this.mContext = context;
    }

    public void sendUpdateAppVersion( ) {
        Map<String, String> map = new ArrayMap<>();
        map.put("alias", alias);
        map.put("appVersion", newVersion);
        String url = "/app/updateAppVersion";
//        mService.sendUpdateAppVersion( alias, newVersion ).enqueue( new Callback<ResponseBody>() {
        mService.getInfo( url, map).enqueue( new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (TheTang.getSingleInstance().whetherSendSuccess( response )) {
                    Log.w(TAG, "sendUpdateAppVersion success!");
                    PreferencesManager.getSingleInstance().setData( Common.appVersion, TheTang.getSingleInstance().getAppVersion( mContext.getPackageName() ) );
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        } );
    }
}

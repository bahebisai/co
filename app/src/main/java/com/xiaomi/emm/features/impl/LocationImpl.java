package com.xiaomi.emm.features.impl;

import android.content.Context;
import android.util.Log;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.excute.MDMOrderMessageManager;
import com.xiaomi.emm.features.http.SendLocationService;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2018/1/4.
 */

public class LocationImpl extends BaseImpl<SendLocationService> {
    private static final String TAG = "LocationImpl";
    Context mContext;

    String alias = PreferencesManager.getSingleInstance().getData( Common.alias );

    public LocationImpl(Context context) {
        super();
        this.mContext = context;
    }

    public void sendLocation(final String feedback_code, final String result) {

        JSONObject json = new JSONObject();
        try {
            json.put( "feedback_code", feedback_code );
            json.put( "alias", alias );
            json.put( "result", result );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody description = RequestBody.create( okhttp3.MediaType.parse(
                "application/json;charset=UTF-8" ), json.toString() );

        mService.sendLocationData( description ).enqueue( new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        } );
    }
}

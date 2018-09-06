package com.xiaomi.emm.features.http;

import android.text.TextUtils;
import android.util.Log;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.model.LoginBackData;
import com.xiaomi.emm.features.event.LoginEvent;
import com.xiaomi.emm.model.Token;
import com.xiaomi.emm.utils.DataParseUtil;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.PreferencesManager;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;


/**
 * Created by Administrator on 2017/5/26.
 */
public class LoginCallBack extends BaseCallBack<ResponseBody> {

    public static final String TAG = "LoginCallBack";
    public LoginCallBack(LoginEvent loginEvent) {
        super(loginEvent);
    }

    @Override
    public void onResponse(Call call, Response response) {

        Log.w(TAG,"stopCall = " + System.currentTimeMillis() );

        ResponseBody body = (ResponseBody) response.body();
        LogUtil.writeToFile(TAG,"Login response!");
        if (body == null) {
            EventBus.getDefault().post(event.setEvent(response.code(), null));
            LogUtil.writeToFile(TAG,"response body is null!");
            return;
        }
        JSONObject object = null;
        int resultCode = 0;
        try {
            object = new JSONObject(body.string());
            if (object == null) {
                LogUtil.writeToFile(TAG,"body object is null!");
                return;
            }
            resultCode = Integer.valueOf(object.getString("result"));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (resultCode == 200) {
            LoginBackData loginBackData = new LoginBackData();
            Token token = new Token();
            Log.w(TAG,"startParse = " + System.currentTimeMillis() );
            token = DataParseUtil.loginBackParse(object);
            if (token != null) {
                //PreferencesManager.setData("token", token.getAccess_token());
                //PreferencesManager.setData("alias", token.getUser_alias());
                loginBackData.setToken(token);


                if (TextUtils.isEmpty( token.getAccess_token() ) &&
                        TextUtils.isEmpty( token.getUser_alias() ) &&
                        TextUtils.isEmpty( token.getKeepAliveHost() ) &&
                        TextUtils.isEmpty( token.getKeepAlivePort() ) ) {
                    EventBus.getDefault().post(event.setEvent(0, null));
                }

                EventBus.getDefault().post(event.setEvent(resultCode, loginBackData));
                LogUtil.writeToFile(TAG,"LoginBackData!");
            } else {
                EventBus.getDefault().post(event.setEvent(1004, null));
            }
        } else {
            EventBus.getDefault().post(event.setEvent(resultCode, null));
        }
    }
}

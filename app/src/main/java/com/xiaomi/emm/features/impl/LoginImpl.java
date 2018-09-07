package com.xiaomi.emm.features.impl;

import android.content.Context;
import android.util.Log;

import com.xiaomi.emm.definition.UrlConst;
import com.xiaomi.emm.features.http.RequestService;
import com.xiaomi.emm.utils.DataParseUtil;
import com.xiaomi.emm.features.http.LoginCallBack;
import com.xiaomi.emm.features.event.LoginEvent;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.MDM;
import com.xiaomi.emm.utils.TheTang;
import com.xiaomi.emm.utils.UUIDGenerator;

import cn.jpush.android.api.JPushInterface;
import okhttp3.RequestBody;

/**
 * 登录实现类
 * 1、登录
 * 2、是否登录
 * 3、退出
 * Created by Administrator on 2017/5/26.
 */

public class LoginImpl extends BaseImpl<RequestService> {

    Context context;
    public LoginImpl(Context context) {
        super();
        this.context = context;
    }

    //登录
    public void login(String username, String passWord) {
        String uuid = UUIDGenerator.getUUID();
        LogUtil.writeToFile(TAG,"Login request!");
        RequestBody body = DataParseUtil.loginToJson(username,passWord,MDM.getDeviceInfo());

        if (mService != null) {
            LoginCallBack callBack = new LoginCallBack(new LoginEvent(uuid));
            Log.w(TAG,"startCall = " + System.currentTimeMillis() );
//            mService.getToken(body).enqueue(callBack);
            mService.uploadInfo(UrlConst.LOGIN, body).enqueue(callBack);
        }
    }

}

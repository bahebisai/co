package com.zoomtech.emm.features.impl;

import android.content.Context;
import android.util.Log;

import com.zoomtech.emm.definition.UrlConst;
import com.zoomtech.emm.features.event.LoginEvent;
import com.zoomtech.emm.features.http.LoginCallBack;
import com.zoomtech.emm.features.http.RequestService;
import com.zoomtech.emm.utils.DataParseUtil;
import com.zoomtech.emm.utils.DeviceUtils;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.MDM;

import okhttp3.RequestBody;

/**
 * 登录实现类
 * 1、登录
 * 2、是否登录
 * 3、退出
 * Created by Administrator on 2017/5/26.
 */
//todo baii impl remain for LoginCallBack
public class LoginImpl extends BaseImpl<RequestService> {

    Context mContext;
    public LoginImpl(Context context) {
        super();
        this.mContext = context;
    }

    //登录
    public void login(String username, String passWord) {
        String uuid = DeviceUtils.getUUID();
        LogUtil.writeToFile(TAG,"Login request!");
        RequestBody body = DataParseUtil.loginToJson(mContext, username, passWord, MDM.getSingleInstance().getDeviceInfo());

        if (mService != null) {
            LoginCallBack callBack = new LoginCallBack(new LoginEvent(uuid));
            Log.w(TAG,"startCall = " + System.currentTimeMillis() );
//            mService.getToken(body).enqueue(callBack);
            mService.uploadInfo(UrlConst.LOGIN, body).enqueue(callBack);
        }
    }

}

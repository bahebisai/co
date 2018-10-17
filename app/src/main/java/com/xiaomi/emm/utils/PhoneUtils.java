package com.xiaomi.emm.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class PhoneUtils {
    static final int CURRENT_NETWORK_STATES_NO = -1; //没有网络
    static final int CURRENT_NETWORK_STATES_MOBILE = 0; //Mobile
    static final int CURRENT_NETWORK_STATES_WIFI = 1; //WIFI

    private Context mContext;
    //获得网络状态
    public static int getNetWorkState(Context context) {//todo baii util phone
        int type = CURRENT_NETWORK_STATES_NO;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                type = CURRENT_NETWORK_STATES_WIFI;
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                type = CURRENT_NETWORK_STATES_MOBILE;
            }
        }
        return type;
    }
}

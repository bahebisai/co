package com.zoomtech.emm.view.photoview.constants;

import android.content.Context;

/**
 * Created by lenovo on 2017/11/8.
 * 用于解决provider冲突的util
 */


public class ProviderUtil {

    public static String getFileProviderName(Context context){
        return context.getPackageName()+".fileprovider";
    }
}

package com.xiaomi.emm.utils;

import android.text.TextUtils;
import android.util.Log;
import com.xiaomi.emm.features.impl.WebclipImageImpl;
import com.xiaomi.emm.features.policy.device.ShortcutUtils;
import com.xiaomi.emm.utils.TheTang;
import java.io.File;


/**
 * Created by lenovo on 2017/9/29.
 */

public class ShortCutManager {

    private static final String TAG = "ShortCutManager";

    //webClipImgPath 下载图片的路径 webClipName 图片的名字 webClipUrl 网站的url
    public static void doDeskShortCut(final String webClipImgPath, final String webClipName, final String webClipUrl) {

        if (TextUtils.isEmpty(webClipImgPath)|| TextUtils.isEmpty(webClipName) || TextUtils.isEmpty(webClipUrl)){
            Log.w(TAG,"webClipImgPath 或者 webClipName 或者 webClipUrl 下载图片路径为空返回");
            return;
        }

        //获取图片的名称
        Log.w(TAG,"===获取图片的名称---"+webClipImgPath.split("\\\\")[webClipImgPath.split("\\\\").length - 1]);
        Log.w(TAG,"===-url名称--"+webClipName);
        //加载图片
        final WebclipImageImpl webclipImage = new WebclipImageImpl(TheTang.getSingleInstance().getContext());
        String picName = webClipImgPath.split("\\\\")[webClipImgPath.split("\\\\").length - 1];
        webclipImage.downloadPicFromNet(webClipImgPath,webClipName,webClipUrl,picName);

    }

    /**
     * 删除桌面快捷键并删除存储在本地的图片
     * @param webClipUrl  指定的要访问url
     * @param webClipName  指定url的名字
     * @param picName  下载的图片
     */

    public static void deleteShortCut( String webClipUrl, String webClipName, String picName) {
        String IN_PATH = "/MDM/Files/images/";
        String savePath = TheTang.getSingleInstance().getContext().getApplicationContext().getFilesDir().getAbsolutePath()+ IN_PATH;
        //删除桌面快捷键
        ShortcutUtils.removeShortcut(TheTang.getSingleInstance().getContext(),ShortcutUtils.getShortCutIntent(webClipUrl), webClipName);
        File file = new File(savePath + picName);
        if (file.exists()) {
            file.delete();
        }
    }
}

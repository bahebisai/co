package com.xiaomi.emm.features.policy.device;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;
import com.xiaomi.emm.R;
import com.xiaomi.emm.base.BaseApplication;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.TheTang;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import okhttp3.ResponseBody;

/**
 * Created by xuyisheng on 15/10/30.
 * Version 1.0
 */
public  class ShortcutUtils {
    private final static String TAG="ShortcutUtils";

    // Action 添加Shortcut
    public static final String ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    // Action 移除Shortcut
    public static final String ACTION_REMOVE_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT";

    private ShortcutUtils() throws InstantiationException {
        throw new InstantiationException("This class is not for instantiation");
    }

    /**
     * 添加快捷方式
     *
     * @param context      context
     * @param actionIntent 要启动的Intent
     * @param name         name
     * @param allowRepeat  是否允许重复
     * @param iconBitmap   快捷方式图标
     */
    public static void addShortcut(Context context, Intent actionIntent, String name,
                                   boolean allowRepeat, Bitmap iconBitmap) {
        if (Build.VERSION.SDK_INT < 26) {
            Intent addShortcutIntent = new Intent( ACTION_ADD_SHORTCUT );
            // 是否允许重复创建
            addShortcutIntent.putExtra( "duplicate", allowRepeat );
            // 快捷方式的标题
            addShortcutIntent.putExtra( Intent.EXTRA_SHORTCUT_NAME, name );
            // 快捷方式的图标
            addShortcutIntent.putExtra( Intent.EXTRA_SHORTCUT_ICON, iconBitmap );
            // 快捷方式的动作
            addShortcutIntent.putExtra( Intent.EXTRA_SHORTCUT_INTENT, actionIntent );
            context.sendBroadcast( addShortcutIntent );
        } else {
            ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);

            if (shortcutManager.isRequestPinShortcutSupported()) {
                actionIntent.setAction(Intent.ACTION_VIEW); //action必须设置，不然报错

                ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(context, name)
                        .setIcon( Icon.createWithBitmap(iconBitmap))
                        .setShortLabel(name)
                        .setIntent(actionIntent)
                        .build();
//                shortcutManager.addDynamicShortcuts( Arrays.asList(info));
                PendingIntent shortcutCallbackIntent = PendingIntent.getBroadcast(context, 0,/* new Intent(this, MainActivity.class)*/actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                shortcutManager.requestPinShortcut(shortcutInfo, shortcutCallbackIntent.getIntentSender());
            }
        }

    }

    /**
     * 移除快捷方式
     *
     * @param context      context
     * @param actionIntent 要启动的Intent
     * @param name         name
     */
    public static void removeShortcut(Context context, Intent actionIntent, String name) {
        if (Build.VERSION.SDK_INT < 26) {
            Intent intent = new Intent( ACTION_REMOVE_SHORTCUT );
            intent.putExtra( Intent.EXTRA_SHORTCUT_NAME, name );
            intent.putExtra( "duplicate", false );
            intent.putExtra( Intent.EXTRA_SHORTCUT_INTENT, actionIntent );
            context.sendBroadcast( intent );
        } else {
            ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
            shortcutManager.removeDynamicShortcuts(Arrays.asList(name));

        }
    }

    public static Intent getShortCutIntent(String webClipUrl) {

        // 使用MAIN，可以避免部分手机(比如华为、HTC部分机型)删除应用时无法删除快捷方式的问题
        Intent intent = new Intent(/*Intent.ACTION_MAIN*/);
        //intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setPackage("com.android.browser");//.setClassName("com.android.browser", "com.uc.browser.InnerUCMobile");
        //intent.putExtra("url", webClipUrl);//"http://wwww.baidu.com"  --PreferencesManager.getSingleInstance().getConfiguration("webClipUrl")
        intent.setData(Uri.parse(webClipUrl));
        LogUtil.writeToFile(TAG,"===="+webClipUrl);
        return intent;
    }

    /**
     * 保存下载的图片流写入SD卡文件
     * @param imageName  xxx.jpg
     * @param body  image stream
     */
    public static void writeResponseBodyToDisk(String imageName, ResponseBody body) {

        Context mContext = TheTang.getSingleInstance().getContext();
        String APP_IMAGE_DIR;
        if (Environment.MEDIA_MOUNTED.equals( Environment.MEDIA_MOUNTED ) || !Environment.isExternalStorageRemovable()) {
            // /如果外部储存可用
                APP_IMAGE_DIR= mContext.getExternalFilesDir( null ).getPath();//获得外部存储路径,默认路径为 /storage/emulated/0/Android/data/com.zoomtech.emm/files//MDM/Logs/.log
            } else {

                String basePath= mContext.getFilesDir().getPath();//直接存在/data/data里，非root手机是看不到的
                APP_IMAGE_DIR= basePath + "/images/";
            }

            if(body==null){
                Toast.makeText(mContext,"图片源错误",Toast.LENGTH_LONG).show();
            }
            try {
                InputStream is = body.byteStream();
                File fileDr = new File(APP_IMAGE_DIR);
                Log.w(TAG,APP_IMAGE_DIR+"/download.jpg===="+fileDr.getAbsolutePath());
                if (!fileDr.exists()) {
                    //fileDr.mkdir();
                    fileDr.createNewFile();
                }
                File file = new File(APP_IMAGE_DIR, imageName);
                if (file.exists()) {
                    Log.w(TAG,APP_IMAGE_DIR+"/download.jpg存在");
                    file.delete();
                    file =  new File(APP_IMAGE_DIR, imageName );
                }
                FileOutputStream fos = new FileOutputStream(file);
                BufferedInputStream bis = new BufferedInputStream(is);
                Log.w(TAG,APP_IMAGE_DIR+"/download.jpg不存在");
                byte[] buffer = new byte[1024];
                int len;
                while ((len = bis.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();
                fos.close();
                bis.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

    }


    /**
     * 保存bitmap到本地
     *
     * @param context
     * @param mBitmap
     * @return
     */
    public static String saveBitmap(Context context, Bitmap mBitmap,String picName) {
         String SD_PATH = Environment.getExternalStorageDirectory().getPath() + "/images/pic/";
         String IN_PATH = "/MDM/Files/images/";
        String savePath;
        File filePic;
        String logPath = BaseApplication.baseLogsPath;//获得文件储存路径,在后面加"/Logs"建立子文件夹


        savePath = context.getApplicationContext().getFilesDir()
                    .getAbsolutePath() + IN_PATH;
        try {
            filePic = new File(savePath + picName);
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return filePic.getAbsolutePath();
    }


    /**
     * 获得文件存储路径
     *
     * @return
     */
    private static String getFilePath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals( Environment.MEDIA_MOUNTED ) || !Environment.isExternalStorageRemovable()) {//如果外部储存可用
            return context.getExternalFilesDir( null ).getPath();//获得外部存储路径,默认路径为 /storage/emulated/0/Android/data/com.zoomtech.emm/files//MDM/Logs/.log
        } else {
            return context.getFilesDir().getPath();//直接存在/data/data里，非root手机是看不到的
        }
    }

    private static void createDeskShortCut() {
        //创建快捷方式的Intent
        Intent shortcutIntent = new Intent( "com.android.launcher.action.INSTALL_SHORTCUT");
        //不允许重复创建  ，如果重复的话就会有多个快捷方式了
        shortcutIntent.putExtra("duplicate",false);

        //这个就是应用程序图标下面的名称
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "webClipName");//  //EXTRA_SHORTCUT_NAME
        Intent intent= new Intent(); //  ---这个MainActivity是调用此方法的Activity

        intent.setAction("android.intent.action.VIEW");
        String webClipUrl = "";
        Log.w(TAG,"url==="+ "\""+ webClipUrl +"\""+"----"+"http://www.baidu.com");
        Log.w(TAG,"urlsss==="+ webClipUrl );
        Uri content_url = Uri.parse( webClipUrl);//"http://www.baidu.com"
        intent.setData(content_url);
        //快捷图片
        Parcelable icon = Intent.ShortcutIconResource.fromContext(TheTang.getSingleInstance().getContext(), R.mipmap.phone_android);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,icon);//EXTRA_SHORTCUT_ICON   //EXTRA_SHORTCUT_ICON_RESOURCE
        //点击快捷图片，运行的程序主入口
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,intent);
        //最后一步就是发送广播
           TheTang.getSingleInstance().getContext().sendBroadcast(shortcutIntent);
    }
}

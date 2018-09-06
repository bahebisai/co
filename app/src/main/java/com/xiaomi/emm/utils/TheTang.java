package com.xiaomi.emm.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.NetworkStatsManager;
//import android.app.usage.StorageStats;
//import android.app.usage.StorageStatsManager;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.xiaomi.emm.R;
import com.xiaomi.emm.base.BaseApplication;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.OrderConfig;
import com.xiaomi.emm.features.complete.CompleteMessageManager;
import com.xiaomi.emm.features.event.MessageEvent;
import com.xiaomi.emm.features.impl.AppImpl;
import com.xiaomi.emm.features.impl.ExcuteCompleteImpl;
import com.xiaomi.emm.features.impl.FeedBackImpl;
import com.xiaomi.emm.features.location.LocationService;
import com.xiaomi.emm.features.lockscreen.Lock2Activity;
import com.xiaomi.emm.features.policy.compliance.ExcuteCompliance;
import com.xiaomi.emm.features.service.NetWorkChangeService;
import com.xiaomi.emm.model.APPInfo;
import com.xiaomi.emm.features.impl.LoginImpl;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.model.AppBlackWhiteData;
import com.xiaomi.emm.model.MessageInfo;
import com.xiaomi.emm.model.SecurityChromeData;
import com.xiaomi.emm.model.SettingAboutData;
import com.xiaomi.emm.model.StrategeInfo;
import com.xiaomi.emm.features.event.StrategeEvent;
import com.xiaomi.emm.view.activity.InitActivity;
import com.xiaomi.emm.view.activity.MainActivity;
import com.xiaomi.emm.view.activity.MessageActivity;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.NETWORK_STATS_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;

/**
 * MDM功能类
 * Created by Administrator on 2017/5/26.
 */

public class TheTang {

    public static final String TAG = "TheTang";

    protected static LoginImpl mLoginImpl;
    protected static FeedBackImpl mFeedBackImpl;
    public ExcuteCompleteImpl mExcuteCompleteImpl;
    static PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
    //单例
    private volatile static TheTang mTheTang;
    private Context mContext;
    private AlertDialog.Builder builder;

    static ExecutorService mExecutorService;

    static ExecutorService mExecutorServiceForDownload;

    private TheTang() {
    }

    /**
     * 单例
     *
     * @return
     */
    public static TheTang getSingleInstance() {
        if (null == mTheTang) {
            synchronized (TheTang.class) {
                if (null == mTheTang) {
                    mTheTang = new TheTang();
                }
            }
        }
        return mTheTang;
    }

    /**
     * Thetang初始化
     *
     * @param context
     * @return
     */
    public TheTang init(Context context) {
        mContext = context;
        initImpl();
        return getSingleInstance();
    }

    /**
     * Impl初始化
     */
    private static void initImpl() {
        mExecutorService = Executors.newFixedThreadPool(ToolUtils.getNumCores() + 1);

        //用于下载
        mExecutorServiceForDownload = Executors.newFixedThreadPool(1);
    }

    public void initImplTwo() {
        mFeedBackImpl = new FeedBackImpl(mContext);
    }

    /***********************************设置别名***************************************************/
    /**
     * 设置别名
     *
     * @param delayTime
     * @param alias
     */
    public void setAlias(final int delayTime, final String alias) {
        JPushInterface.setAliasAndTags(mContext, alias, null, mAliasCallback);
    }

    /**
     * 设置别名返回
     */
    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {
        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs;
            switch (code) {
                case 0:
                    logs = "Set tag and alias success";
                    break;
                case 6002:
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    // 延迟 60 秒 设置别名
                    setAlias(60 * 1000, alias);
                    //JPushInterface.setAliasAndTags(getApplicationContext(),alias,null,mAliasCallback);
                    break;
                default:
                    logs = "Failed with errorCode = " + code;
            }
            Toast.makeText(mContext, logs, Toast.LENGTH_LONG).show();
            LogUtil.writeToFile(TAG, logs);
            LogUtil.writeToFile(TAG, "RegistrationID = " + JPushInterface.getRegistrationID(mContext));
        }
    };

    /***********************************登录相关***************************************************/
    /**
     * 登录
     *
     * @param userName
     * @param passWord
     */
    public void login(String userName, String passWord) {
        mLoginImpl = new LoginImpl(mContext);
        mLoginImpl.login(userName, passWord);
    }

    /***********************************
     * 执行反馈
     ***************************************************/
    public void sendExcuteComplete(CompleteMessageManager.SendListener listener, String code, String result, String id) {
        if (mExcuteCompleteImpl == null)
            mExcuteCompleteImpl = new ExcuteCompleteImpl(mContext);

        mExcuteCompleteImpl.sendExcuteComplete(listener, code, result, id);
    }

    /**
     * 获得剩余存储
     *
     * @return
     */
    public float getRemainStorage() {
        //外部存储大小
        return getAvailSpace(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    /**
     * 获得总存储
     *
     * @return
     */
    public float getTotalStorage() {
        //外部存储大小
        return getTotalSpace(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    /**
     * 获得剩余存储
     *
     * @param path 根路径
     * @return
     */
    public long getAvailSpace(String path) {
        StatFs statfs = null;
        try {
            statfs = new StatFs(path);
        } catch (Exception e) {
            return 0;
        }
        long size = statfs.getBlockSize();//获取分区的大小
        long count = statfs.getAvailableBlocks();//获取可用分区块的个数
        return size * count;
    }

    /**
     * 获得总存储
     *
     * @param path 根路径
     * @return
     */
    public long getTotalSpace(String path) {
        StatFs statfs = null;
        try {
            statfs = new StatFs(path);
        } catch (Exception e) {
            return 0;
        }

        long size = statfs.getBlockSize();//获取分区的大小
        long count = statfs.getBlockCount();//获取分区块的个数
        return size * count;
    }

    /**
     * 获得应用的数据大小
     *
     * @param pkgName
     * @return
     * @throws Exception
     */
    static String appSize = null;

    public String queryPackageSize(String pkgName) throws Exception {

        appSize = null;

        if (pkgName != null) {

            if (Build.VERSION.SDK_INT < 26) {
                PackageManager mPackageManager = TheTang.getSingleInstance().getPackageManager();  //得到pm对象
                try {
                    Method getPackageSizeInfo = mPackageManager.getClass().getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                    getPackageSizeInfo.invoke(mPackageManager, pkgName, new IPackageStatsObserver.Stub() {
                        @Override
                        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException { //异步执行
                            if (succeeded && pStats != null) {
                                appSize = TheTang.getSingleInstance().formatFileSize(pStats.codeSize
                                        + pStats.dataSize + pStats.cacheSize);
                            }
                        }
                    });
                } catch (Exception ex) {
                    Log.e(TAG, "NoSuchMethodException");
                    ex.printStackTrace();
                }
            } else {
                StorageStatsManager mStorageStatsManager = (StorageStatsManager) mContext.getSystemService(Context.STORAGE_STATS_SERVICE);
                StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
                UUID uuid = StorageManager.UUID_DEFAULT;
                StorageStats mStorageStats = mStorageStatsManager.queryStatsForUid(uuid, getAppUid(pkgName));
                appSize = TheTang.getSingleInstance().formatFileSize(
                        mStorageStats.getAppBytes() + mStorageStats.getCacheBytes() + mStorageStats.getDataBytes());
            }
        }

        if (appSize == null) {
            appSize = "0B";
        }

        return appSize;
    }

    /**
     * 月份与时间格式转换
     *
     * @param time 时间戳
     * @return
     */
    /*modify by duanxin on 2017/08/31*/
    public String formatTime(long time) {
        Date date = new Date(time);
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return mSimpleDateFormat.format(date);
    }

    /**
     * 将时间转为时长
     *
     * @param time
     * @return
     */
    public String formatTimeLength(long time) {
        String date = null;
        String hour = null;
        String minute = null;
        String timeResult = null;

        if (time > 24 * 3600 * 1000) {
            date = time / (24 * 3600 * 1000) + "天";
        }

        time = time % (24 * 3600 * 1000);

        if (time > 3600 * 1000) {
            hour = time / (3600 * 1000) + "小时";
        }

        time = time % (3600 * 1000);

        if (time > 60 * 1000) {
            minute = time / (60 * 1000) + "分钟";
        }

        if (date != null) {
            timeResult = date;
        }

        if (hour != null) {
            if (timeResult == null) {
                timeResult = hour;
            } else {
                timeResult += hour;
            }
        }

        if (minute != null) {
            if (timeResult == null) {
                timeResult = minute;
            } else {
                timeResult += minute;
            }
        }

        if (timeResult == null) {
            timeResult = "0分钟";
        }

        return timeResult;
    }

    /**
     * 带单位的数据格式转换
     *
     * @param fileS 文件大小  单位为byte
     * @return
     */
    public String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 数据单位转换
     *
     * @param fileS 文件大小  单位为byte
     * @return
     */
    public String formatFile(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS);//+ "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024); //+ "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576); //+ "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824);//+ "GB";
        }
        return fileSizeString;
    }

    /**
     * 获取数据单位
     *
     * @param size 文件大小  单位为byte
     * @return
     */
    public String getUnit(float size) {
        String unit = "";
        if (size < 1024) {
            unit = "B";
        } else if (size < 1048576) {
            unit = "KB";
        } else if (size < 1073741824) {
            unit = "MB";
        } else {
            unit = "GB";
        }
        return unit;
    }


    /**
     * 获取定位
     */
    public void getLocation() {
        Intent service_intent = new Intent(mContext, LocationService.class);
        startService(service_intent);
    }

    //获得当前设置的电话号码
    public String getTelePhonyNumber() {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getLine1Number();
    }

    /**
     * 获取文件大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static long getFileSizes(File file) throws Exception {
        return file.length();
    }

    /**
     * 获得文件后缀
     *
     * @param fileName
     * @param node
     * @return
     */
    public String getFileEnds(String fileName, String node) {
        if (fileName != null) {
            int position = fileName.indexOf(node);
            return fileName.substring(position + 1, fileName.length());
        }
        return null;
    }

    /**
     * 获取已安装App信息
     *
     * @return
     */
    public List<APPInfo> getInstallAppInfo() {
        return DatabaseOperate.getSingleInstance().queryInstallAppInfo();
    }

    /**
     * 获得已安装的应用的图标
     *
     * @param info
     * @return
     */
    public Drawable getAppIcon(ApplicationInfo info) {
        Drawable drawable = null;
        PackageManager packageManager = getPackageManager();

        drawable = info.loadIcon(packageManager);
        return drawable;
    }

    /**
     * 通过包名获得应用的图标
     *
     * @param packageName
     * @return
     */
    public Drawable getAppIcon(String packageName) {
        PackageManager packageManager = getPackageManager();
        ApplicationInfo info = null;
        try {
            info = packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (info == null) {
            return null;
        }

        return info.loadIcon(packageManager);
    }

    /**
     * 获得已安装的应用的名称
     *
     * @param packageName
     * @return
     */
    public String getAppLabel(String packageName) {
        String label = null;
        PackageManager packageManager = getPackageManager();

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            label = (String) packageManager.getApplicationLabel(packageInfo.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return label;
    }

    /**
     * 获得包管理器
     *
     * @returnAppTask
     */
    public PackageManager getPackageManager() {
        PackageManager packageManager = null;
        try {
            packageManager = mContext.getPackageManager();
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, e.getCause().toString());
        }
        return packageManager;
    }

    /**
     * 获得线程池对象
     *
     * @return
     */
    public ExecutorService getThreadPoolObject() {
        return mExecutorService;
    }

    /**
     * 获得专用下载线程池对象
     *
     * @return
     */
    public ExecutorService getThreadPoolObjectForDownload() {
        return mExecutorServiceForDownload;
    }
    /**
     * 存储后台传递过来的命令通知
     *
     * @param orderCode
     */
    public void addMessageInfo(String orderCode) {
        switch (Integer.parseInt(orderCode)) {
            case OrderConfig.SilentInstallAppication:
                break;
            case OrderConfig.SilentUninstallAppication:
                break;
            case OrderConfig.IssuedFile:
                break;
            case OrderConfig.DeleteIssuedFile:
                break;
            case OrderConfig.Send_Message:
                break;
            case OrderConfig.send_system_strategy:
                break;
            case OrderConfig.delete_system_strategy:
                break;
            case OrderConfig.send_app_strategy:
                break;
            case OrderConfig.delete_app_strategy:
                break;
            case OrderConfig.send_black_White_list:
                break;
            case OrderConfig.delete_black_White_list:
                break;
            case OrderConfig.send_limit_strategy:
                break;
            case OrderConfig.delete_limit_strategy:
                break;
            case OrderConfig.send_safe_desk:  //下发安全桌面
                break;
            case OrderConfig.delete_safe_desk:  //删除安全桌面
                break;
            case OrderConfig.revocation_safe_desk:  //撤销安全桌面
                break;
            case OrderConfig.send_time_Frence:  //时间围栏
                break;
            case OrderConfig.delete_time_Frence: //删除时间围栏
                break;
            case OrderConfig.revocation_time_Frence: //卸载时间围栏
                break;
            case OrderConfig.send_configure_Strategy:// 下发配置策略
                break;
            case OrderConfig.delete_configure_Strategy:// 删除配置策略
                break;
            case OrderConfig.send_geographical_Fence:// 下发地理围栏策略
                break;
            case OrderConfig.delete_geographical_Fence:// 删除地理围栏策略
                break;
            case OrderConfig.delete_app://删除应用
                break;
            case OrderConfig.security_chrome:
                break;
            case OrderConfig.delete_security_chrome:  //
                break;
            case OrderConfig.get_setting_about://获取设置相关数据
                break;
            case OrderConfig.send_loseCouplet_strategy://下发失联违规
                break;
            case OrderConfig.delete_loseCouplet_strategy://删除失联违规
                break;
            case OrderConfig.HadLink://长连接登录成功
                break;
            case OrderConfig.device_update:
                break;
            case OrderConfig.put_down_application_fence:
                break;
            case OrderConfig.unstall_application_fence:
                break;
            case OrderConfig.AddTelephonyWhiteList:
                break;
            case OrderConfig.SEND_SENSITIVE_WORD_POLICY://下发敏感词策略
                break;
            case OrderConfig.DELETE_SENSITIVE_WORD://删除敏感词策略
                break;
            case OrderConfig.SEND_SMS_BACKUP_POLICY:
                break;
            case OrderConfig.DELETE_SMS_BACKUP_POLICY:
                break;
            case OrderConfig.SEND_CALL_RECORDER_BACKUP_POLICY:
                break;
            case OrderConfig.DELETE_CALL_RECORDER_BACKUP_POLICY:
                break;
            case OrderConfig.SEND_EntranceGuard_POLICY:  //下发门禁策略
                break;
            case OrderConfig.DELETE_EntranceGuard_POLICY:  //下发删除门禁策略
                break;
            case OrderConfig.SEND_ENTRANCE_GUARD_KEY:     //下发门禁KEY
                break;
            case OrderConfig.DELETE_ENTRANCE_GUARD_KEY:    //删除门禁KEY
                break;
            case OrderConfig.send_trajectory_Strategy:   //下发轨迹策略
                break;
            case OrderConfig.delete_trajectory_Strategy:  //删除下发轨迹
                break;
            default:
                addMessage(orderCode, "");
                break;
        }
    }

    /**
     * 添加消息通知
     *
     * @param orderCode
     * @param about
     */
    public void addMessage(String orderCode, String about) {
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessage_icon("true");
        messageInfo.setMessage_id(orderCode);
        messageInfo.setMessage_from("MDM");
        messageInfo.setMessage_time(System.currentTimeMillis() + "");
        messageInfo.setMessage_about(about);
        DatabaseOperate.getSingleInstance().addMessageInfo(messageInfo);
        EventBus.getDefault().post(new MessageEvent(messageInfo));
        showMessage(messageInfo);
    }

    /**
     * 在通知栏显示
     *
     * @param messageInfo
     */
    private void showMessage(MessageInfo messageInfo) {
        showNotification(getMeaasgeInfo(messageInfo.getMessage_id()) + messageInfo.getMessage_about(),
                mContext.getResources().getString(R.string.message1), 1001);
    }

    public void showNotification(String content, String title, int id) {
        Intent intent = new Intent(mContext, MessageActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //int id = 1001;

        if (Build.VERSION.SDK_INT >= 26) {

            NotificationChannel channel = new NotificationChannel("notification", "notification", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Notification notification = new Notification.Builder(mContext, "notification")
                    .setContentIntent(pendingIntent)
                    .setContentTitle(title)
                    .setSmallIcon(R.mipmap.mi8sesplit8split1)
//                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setContentText(content)
                    .build();
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(id, notification);

        } else {

            NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            //实例化NotificationCompat.Builde并设置相关属性
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                    .setContentIntent(pendingIntent)
                    //设置小图标
                    .setSmallIcon(R.mipmap.mi8sesplit8split1)
                    //设置通知标题
                    .setContentTitle(title)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    //设置通知内容
                    .setContentText(content);

            notifyManager.notify(id, builder.build());
        }
        //通过builder.build()方法生成Notification对象,并发送通知,id=1*/

    }

    public void cancelNotification(int id) {

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);
    }

    /**
     * 获得id相对应命令
     *
     * @param message_id
     * @return
     */
    public String getMeaasgeInfo(String message_id) {
        String message = null;
        if (message_id != null) {
            for (int i = 0; i < Common.message_info.length; i++) {
                if (message_id.equals(String.valueOf(Common.message_info[i][0]))) {
                    message = mContext.getResources().getString((int) Common.message_info[i][1]);
                    break;
                }
            }
        }
        return message;
    }

    /**
     * 获得是否允许上网
     *
     * @param net_id
     * @return
     */
    public String getNetworkInfo(String net_id) {
        if ("0".equals(net_id)) {
            return mContext.getResources().getString((int) Common.net_info[0]);
        }
        return mContext.getResources().getString((int) Common.net_info[1]);
    }

    /**
     * 获得是否允许卸载
     *
     * @param un_id
     * @return
     */
    public String getUninstallInfo(String un_id) {
        if ("0".equals(un_id)) {
            return mContext.getResources().getString((int) Common.uninstall_info[0]);
        }
        return mContext.getResources().getString((int) Common.uninstall_info[1]);
    }

    /**
     * 添加策略
     *
     * @param orderCode
     * @param name
     * @param time
     */
    public void addStratege(String orderCode, String name, String time) {
        StrategeInfo strategeInfo = new StrategeInfo();
        strategeInfo.strategeId = orderCode;
        strategeInfo.strategeName = name;
        strategeInfo.strategeTime = time;

        DatabaseOperate.getSingleInstance().addStrategeInfo(strategeInfo);
        EventBus.getDefault().post(new StrategeEvent());
    }

    /**
     * 删除策略
     *
     * @param code
     */
    public void deleteStrategeInfo(String code) {
        DatabaseOperate.getSingleInstance().deleteSimpleStrategeInfo(code);
        EventBus.getDefault().post(new StrategeEvent());
    }

    /**
     * 删除策略
     *for sensitive word
     * @param code
     *
     */
    public void deleteStrategeInfo(String code, String strategyName) {
        DatabaseOperate.getSingleInstance()
                .deleteSimpleStrategeInfoByName(code, strategyName);
        EventBus.getDefault()
                .post(new StrategeEvent());
    }

    /**
     * 获取系统服务
     *
     * @param service
     * @return
     */
    public Object getSystemService(String service) {
        return mContext.getSystemService(service);
    }

    /**
     * 获取Application上下文
     *
     * @return
     */
    public Context getContext() {
        return mContext;
    }

    final int CURRENT_NETWORK_STATES_NO = -1; //没有网络
    final int CURRENT_NETWORK_STATES_MOBILE = 0; //Mobile
    final int CURRENT_NETWORK_STATES_WIFI = 1; //WIFI

    //获得网络状态
    public int getNetWorkState() {
        int type = CURRENT_NETWORK_STATES_NO;
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
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

    /**
     * 获得已安装的非系统APP个数
     *
     * @return
     */
    public List<PackageInfo> getNoSystemApp() {
        List<PackageInfo> packageList = getPackageManager().getInstalledPackages(0);
//        Log.d("baii", "before size " + packageList.size());
        if (packageList != null && packageList.size() > 0) {
            //note bai: for 循环不能边删除边遍历
            //modify:1.remove之后i不能增加;2.遍历时记录所有需要删除的，遍历完成之后统一删除
            for (int i = 0; i < packageList.size(); i++) {
                PackageInfo packageInfo = packageList.get(i);
                if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    packageList.remove(i);
                    i--;//bai for note
                    continue;
                }
               // if (packageInfo.packageName.contains("emm")) {//todo bai to delete
                if (packageInfo.packageName.equals(Common.packageName)) {
                    packageList.remove(i);
                    i--;
                    continue;
                }
            }
        }
        return packageList;
    }

    /**
     * 应用违规处理
     *
     * @param context
     * @param names
     */
    public void appViolationExcute(Context context, List<String> names) {
        Map<String, String> deny_apps = new HashMap<>();

        /**
         * 存入数据库
         */
        for (String name : names) {
            try {
                ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(name, 0);
                deny_apps.put((String) getPackageManager().getApplicationLabel(applicationInfo), name);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }

        DatabaseOperate.getSingleInstance().addAppDenyList(deny_apps);
        List<String> list = DatabaseOperate.getSingleInstance().queryDenyAppByType("0");
        sendAppDeny(context, "0", list);
    }

    /**
     * 执行应用违规
     */
    public void excuteAppCompliance() {
        String pwd = preferencesManager.getComplianceData(Common.app_compliance_pwd);
        if (TextUtils.isEmpty(pwd)) {
            MDM.setFactoryReset();
        } else {
            MDM.forceLockScreen(Common.lockTypes[1], pwd);
        }
    }

    /**
     * 应用合规处理
     *
     * @param context
     * @param packageName
     */
    public void appComplianceExcute(Context context, String packageName) {
        DatabaseOperate.getSingleInstance().updateDenyApp(packageName, "1");

        List<String> list = DatabaseOperate.getSingleInstance().queryDenyAppByType("0");

        if (list == null || list.size() == 0) {
            //List<String> denyApps = DatabaseOperate.getSingleInstance().queryAllDenyApp();
            sendAppUnDeny(context, "1");
            DatabaseOperate.getSingleInstance().deleteAllDenyApp();
        }
    }

    /**
     * 应用违规反馈
     * @param context
     * @param type
     * @param names
     */
    private void sendAppDeny(Context context, String type, List<String> names) {
        AppImpl appImpl = new AppImpl( context );
        appImpl.sendAppCompliance( type, names.toString() );
    }

    /**
     * 应用合规反馈
     * @param context
     * @param type
     */
    private void sendAppUnDeny(Context context, String type) {
        AppImpl appImpl = new AppImpl( context );
        appImpl.sendAppCompliance( type, "null" );
    }

    /**
     * 应用黑白名单
     *
     * @param appBlackWhiteData
     */
    public void storageBlackWhiteList(AppBlackWhiteData appBlackWhiteData) {

        if (preferencesManager.getOtherData(Common.appManagerType) != null) {
            ExcuteCompliance.deleteAppCompliance();
        }

        if ("0".equals(appBlackWhiteData.type)) {
            preferencesManager.setOtherData(Common.appManagerType, "0");
        } else {
            preferencesManager.setOtherData(Common.appManagerType, "1");
        }

        preferencesManager.setComplianceData(Common.app_compliance_id, appBlackWhiteData.id);

        if ("0".equals(appBlackWhiteData.type)) {
            preferencesManager.setComplianceData(Common.app_compliance_name, appBlackWhiteData.name);

        } else {
            preferencesManager.setComplianceData(Common.app_compliance_name, appBlackWhiteData.name);
        }

        DatabaseOperate.getSingleInstance().addAppWhiteList(appBlackWhiteData.appList);
    }

    /**
     * 获得SD卡cid
     *
     * @return
     */
    public String getSdcardCid() {
        Object localOb = null; // SD Card ID
        String sd_cid = null;
        try {
            localOb = new FileReader("/sys/block/mmcblk1/device/" + "cid");
            sd_cid = new BufferedReader((Reader) localOb).readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sd_cid;
    }

    /**
     * 解决List重复
     *
     * @param list
     * @return
     */
    public List<LauncherActivityInfo> removeDuplicateWithOrder(List<LauncherActivityInfo> list) {
        Set set = new HashSet();
        List<LauncherActivityInfo> newList = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            LauncherActivityInfo element = (LauncherActivityInfo) iter.next();
            if (set.add(element.getApplicationInfo().packageName)) {
                newList.add(element);
            }
        }
        return newList;
    }

    /**
     * 解决List重复
     *
     * @param list
     * @return
     */
    public List<ApplicationInfo> removeApplicate(Collection<ApplicationInfo> list) {
        Set set = new HashSet();
        List<ApplicationInfo> newList = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {

            ApplicationInfo element = (ApplicationInfo) iter.next();
            if (set.add(element.packageName)) {
                newList.add(element);
            }
        }
        return newList;
    }

    /**
     * 获取Launcher Apps
     *
     * @return
     */
    public List getLauncherApps() {
        LauncherApps apps = (LauncherApps) TheTang.getSingleInstance().getContext().getSystemService(Context.LAUNCHER_APPS_SERVICE);

        UserManager mUserManager = (UserManager) TheTang.getSingleInstance().getContext().getSystemService(Context.USER_SERVICE);

        final List<UserHandle> profiles = mUserManager.getUserProfiles();

        //单用户情况下
        UserHandle user = profiles.get(0);

        return apps.getActivityList(null, user);
    }

    /**
     * 获得Launcher非系统app
     *
     * @return
     */
    public List getLauncherNoSystemApp() {
        List<LauncherActivityInfo> appList = getLauncherApps();

        if (appList == null) {
            return null;
        }

        List<LauncherActivityInfo> apps = new ArrayList<>();

        for (LauncherActivityInfo launcherActivityInfo : appList) {
            if ((launcherActivityInfo.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                apps.add(launcherActivityInfo);
            }
        }

        appList.removeAll(apps);

        return appList;
    }

    /**
     * 应用黑名单处理
     *
     * @param appBlackWhiteDataList
     * @param launcherNoSystemApps
     */
    public void appBlackListCompliance(List<String> appBlackWhiteDataList, List<String> launcherNoSystemApps) {

        List<APPInfo> appInfos = DatabaseOperate.getSingleInstance().queryInstallAppInfo();


        if (appInfos != null && appInfos.size() > 0) {
            for (APPInfo appInfo : appInfos) {
                if (appBlackWhiteDataList.contains(appInfo.getPackageName())) {
                    appBlackWhiteDataList.remove(appInfo.getPackageName());
                }
            }
        }

        //EMM不违规
        appBlackWhiteDataList.remove(getContext().getPackageName());

        List<String> names = new ArrayList<>();

        //判断系统中的应用是否在黑名单中
        for (String packageName : launcherNoSystemApps) {
            if (appBlackWhiteDataList.contains(packageName)) {
                names.add(packageName);
            }
        }

        if (names != null && names.size() > 0) {
            appViolationExcute(getContext(), names);
        }
    }

    /**
     * 应用白名单处理
     *
     * @param appBlackWhiteDataList
     * @param launcherNoSystemApps
     */
    public void appWhiteListCompliance(List<String> appBlackWhiteDataList, List<String> launcherNoSystemApps) {

        List<APPInfo> appInfos = DatabaseOperate.getSingleInstance().queryInstallAppInfo();

        //将下发应用添加到白名单中
        if (appInfos != null && appInfos.size() > 0) {
            for (APPInfo appInfo : appInfos) {
                appBlackWhiteDataList.add(appInfo.getPackageName());
            }
        }

        //EMM不违规
        appBlackWhiteDataList.add(getContext().getPackageName());

        List<String> names = new ArrayList<>();

        for (String packageName : launcherNoSystemApps) {
            if (!appBlackWhiteDataList.contains(packageName)) {
                names.add(packageName);
            }
        }

        if (names != null && names.size() > 0) {
            appViolationExcute(getContext(), names);
        }
    }

    public double x_pi = 3.14159265358979324 * 3000.0 / 180.0;

    /**
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 将 GCJ-02 坐标转换成 BD-09 坐标
     *
     * @param lat
     * @param lon
     */
    public double[] gcj02_To_Bd09(double lat, double lon) {
        double x = lon, y = lat;
        //DecimalFormat df = new DecimalFormat("######.000000");
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        double tempLon = z * Math.cos(theta) + 0.0065;
        double tempLat = z * Math.sin(theta) + 0.006;
        double[] gps = {/*Double.parseDouble( df.format(*/ tempLat /*) )*/, /*Double.parseDouble( df.format( */tempLon /*) )*/};
        return gps;
    }

    /**
     * * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 * * 将 BD-09 坐标转换成GCJ-02 坐标 * * @param
     * bd_lat * @param bd_lon * @return
     */
    public double[] bd09_To_Gcj02(double lat, double lon) {
        //DecimalFormat df = new DecimalFormat("######.000000");
        double x = lon - 0.0065, y = lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        double tempLon = z * Math.cos(theta);
        double tempLat = z * Math.sin(theta);
        double[] gps = {tempLat, tempLon};
        return gps;
    }

    /**
     * 获取ResponseBody的内容
     *
     * @param response
     * @return
     */
    public String getResponseBodyString(Response<ResponseBody> response) {
        ResponseBody body = (ResponseBody) response.body();
        Log.w(TAG, "response body is null!----" + response.code());

        if (body == null) {
            LogUtil.writeToFile(TAG, "response body is null!" + response.code());
            return null;
        }

        String content = null;

        try {
            content = body.string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }


    /**
     * 判断发送是否成功
     *
     * @return
     */
    public boolean whetherSendSuccess(String content) {

        LogUtil.writeToFile(TAG, "content = " + content);

        if (TextUtils.isEmpty(content)) {
            return false;
        }

        JSONObject object = null;
        int resultCode = 0;

        try {
            object = new JSONObject(content);
            if (object == null) {
                LogUtil.writeToFile(TAG, "body object is null!");
                return false;
            }
            resultCode = Integer.valueOf(object.getString("result"));
            LogUtil.writeToFile(TAG, "resultCode = " + resultCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (resultCode == 200) {
            return true;
        }

        return false;
    }


    /**
     * 判断发送是否成功
     *
     * @return
     */
    public boolean whetherSendSuccess(Response<ResponseBody> response) {

        ResponseBody body = (ResponseBody) response.body();
        Log.w(TAG, "response body is null!----" + response.code());
        LogUtil.writeToFile(TAG, "response body is null!----" + response.code());
        if (body == null) {
            LogUtil.writeToFile(TAG, "response body is null!" + response.code());
            return false;
        }

        JSONObject object = null;
        int resultCode = 0;

        try {
            object = new JSONObject(body.string());
            if (object == null) {
                LogUtil.writeToFile(TAG, "body object is null!");
                return false;
            }
            resultCode = Integer.valueOf(object.getString("result"));
            LogUtil.writeToFile(TAG, "resultCode = " + resultCode);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (resultCode == 200) {
            return true;
        }

        return false;
    }


    /**
     * 获取应用UID
     *
     * @param packageName
     * @return
     */
    public int getAppUid(String packageName) {
        PackageManager packageManager = TheTang.getSingleInstance().getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int uid = packageInfo.applicationInfo.uid;
        return uid;
    }

    /**
     * 判断用户访问权限的状态
     *
     * @return
     */
    public boolean getUsageStats() {

        AppOpsManager appOps = (AppOpsManager) getContext().getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), getContext().getPackageName());
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
        return granted;
    }

    /**
     * 删除安全浏览器数据
     */
    public void deleteSecurityChrome() {
        preferencesManager.removeComplianceData(Common.securityChrome);
        preferencesManager.removeComplianceData(Common.securityChrome_name);
        preferencesManager.removeComplianceData(Common.securityChrome_id);
        preferencesManager.removeComplianceData(Common.securityChrome_list);
    }

    /**
     * 存储安全浏览器数据
     *
     * @param securityChromeData
     */
    public void storageSecurityChrome(SecurityChromeData securityChromeData) {
        preferencesManager.setComplianceData(Common.securityChrome, "true");
        preferencesManager.setComplianceData(Common.securityChrome_name, securityChromeData.sec_name);
        preferencesManager.setComplianceData(Common.securityChrome_id, securityChromeData.sec_id);
        preferencesManager.setComplianceData(Common.securityChrome_list, TheTang.getSingleInstance().formatStringFromMap(securityChromeData.sec_white_list));
    }

    /**
     * 提取Host
     *
     * @param url
     * @return
     */
    public String getHost(String url) {
        Pattern p = Pattern.compile("(http://|https://)?([^/]*)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(url);
        return m.find() ? m.group(2) : url;
    }

    /**
     * 获取网页图标url
     *
     * @param url
     * @return
     */
    public String getUrlForWebClip(String url) {
        Document doc = null;
        String web_url = null;

        if (url.contains("www")) {
            url = url.replaceFirst("www", "wap");
        } else {
            url = url.replaceFirst("//", "//wap.");
        }

        Log.w(TAG, "getUrlForWebClip：" + url);

        doc = getUrl(url);

        if (doc == null) {
            return null;
        }

        Elements element = doc.select("head").select("link");
        for (Element links : element) {
            String target = links.attr("rel").toString();

            if (target != null) {
                if ("shortcut icon".equals(target.toLowerCase())) {
                    web_url = links.attr("href").toString();
                    break;
                }
            }
        }
        Log.w("MainActivity", url);

        return web_url;
    }

    private Document getUrl(String url) {
        Document doc = null;

        try {
            doc = Jsoup.connect(url).get();//timeout(5000).post();
        } catch (Exception e) {

        }
        return doc;
    }

    /**
     * 将Map转String
     *
     * @param data
     */
    public String formatStringFromMap(Map<String, String> data) {

        if (data == null)
            return null;

        JSONArray mJsonArray = new JSONArray();
        Iterator<Map.Entry<String, String>> iterator = data.entrySet().iterator();

        JSONObject object = new JSONObject();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            try {
                object.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {

            }
        }
        mJsonArray.put(object);
        return mJsonArray.toString();
    }

    /**
     * String转Map
     *
     * @param result
     * @return
     */
    public Map<String, String> formatMapFromString(String result) {

        if (result == null)
            return null;

        Map<String, String> data = new HashMap<String, String>();

        try {
            JSONArray array = new JSONArray(result);
            for (int i = 0; i < array.length(); i++) {
                JSONObject itemObject = array.getJSONObject(i);
                JSONArray names = itemObject.names();
                if (names != null) {
                    for (int j = 0; j < names.length(); j++) {
                        String name = names.getString(j);
                        String value = itemObject.getString(name);
                        data.put(name, value);
                    }
                }
            }
        } catch (JSONException e) {
            LogUtil.writeToFile(TAG, " formatMapFromString: " + e.getCause().toString());
        }
        return data;
    }

    // 流量转化
    public String convertTraffic(long traffic) {
        BigDecimal trafficKB;
        BigDecimal trafficMB;
        BigDecimal trafficGB;

        BigDecimal temp = new BigDecimal(traffic);
        BigDecimal divide = new BigDecimal(1000);
        trafficKB = temp.divide(divide, 2, 1);
        if (trafficKB.compareTo(divide) > 0) {
            trafficMB = trafficKB.divide(divide, 2, 1);
            if (trafficMB.compareTo(divide) > 0) {
                trafficGB = trafficMB.divide(divide, 2, 1);
                return trafficGB.doubleValue() + "GB";
            } else {
                return trafficMB.doubleValue() + "MB";
            }
        } else {
            return trafficKB.doubleValue() + "KB";
        }
    }


    public void noticDilag(Activity activity) {
        if (getNetworkType() == 0 || getNetworkType() == 1) {
            return;
        }


        if (BaseApplication.getNewsLifecycleHandler().isSameClassName(Lock2Activity.class.getSimpleName()) || BaseApplication.getNewsLifecycleHandler().isSameClassName(InitActivity.class.getSimpleName())) {
            return;

        }

        if (TheTang.getSingleInstance().getUsageStats()) {
            long quota = preferencesManager.getTraffictotal("quota");
            NetworkStatsManager networkStatsManager = (NetworkStatsManager) getContext().getSystemService(NETWORK_STATS_SERVICE);
            final NetworkStatsHelper networkStatsHelper = new NetworkStatsHelper(networkStatsManager);
            if (quota > 0 && (quota * 1024 * 1024) < (networkStatsHelper.getAllMonthMobile(getContext(), null))) {
                if (0 == (preferencesManager.getTraffictotal("quota_flag"))) {
                    if (builder == null) {

                        builder = new AlertDialog.Builder(activity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                        final AlertDialog normalDialog = builder.create();
                    }

                    builder.setIcon(R.drawable.dingzhi);
                    builder.setTitle("额定流量");
                    builder.setMessage("您当前的移动流量为" + TheTang.getSingleInstance().convertTraffic(networkStatsHelper.getAllMonthMobile(getContext(), null)) + ",已经超过设置的额定移动流量" + quota + "M,是否关闭移动数据? \n  ");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MDM.openDataConnectivity(false);
                            preferencesManager.setTraffictotal("quota_flag_s", 0);
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            preferencesManager.removeTraffictotal("quota");
                            preferencesManager.setTraffictotal("quota_flag", 1);
                            preferencesManager.setTraffictotal("quota_flag_s", 0);
                        }
                    });

                    builder.setCancelable(false);
                    if (builder != null && preferencesManager.getTraffictotal("quota_flag_s") != 1) {
                        preferencesManager.setTraffictotal("quota_flag_s", 1);
                        AlertDialog dialog = builder.show();
                    }

                }

            }

        }
    }

    /**
     * 存储设置相关数据
     *
     * @param settingAboutData
     */
    public void storageSettingAboutData(SettingAboutData settingAboutData) {
        preferencesManager.setSettingData(Common.setting_help, settingAboutData.messageForHelp);
        preferencesManager.setSettingData(Common.setting_agreement, settingAboutData.agreementLicense);
        preferencesManager.setSettingData(Common.setting_stand_by, settingAboutData.supportContent);
    }

    /**
     * 方法描述：判断某一应用是否正在运行
     *
     * @param packageName 应用的包名
     * @return true 表示正在运行，false表示没有运行
     */
    public boolean isAppRunning(String packageName) {
        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        if (list.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.baseActivity.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 方法描述：判断某一Service是否正在运行
     *
     * @param serviceName Service的全路径： 包名 + service的类名
     * @return true 表示正在运行，false 表示没有运行
     */
    public boolean isServiceRunning(String serviceName) {
        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(200);
        if (runningServiceInfos.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo serviceInfo : runningServiceInfos) {
            if (serviceInfo.service.getClassName().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获得sim卡imsi
     *
     * @return
     */
    public String[] getSubscriberId() {
        String[] imsis = new String[2];
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getSubscriberId = telephonyManager.getClass().getMethod("getSubscriberId", int.class);
            if (getSubscriberId != null) {
                imsis[0] = (String) getSubscriberId.invoke(telephonyManager, 0);
                imsis[1] = (String) getSubscriberId.invoke(telephonyManager, 1);
            }
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "getSimSerialNumber " + e.toString());
        }
        return imsis;
    }

    /**
     * 获得电话号码
     *
     * @return
     */
    public String[] getLine1Number() {
        String[] nums = new String[2];
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getLine1Number = telephonyManager.getClass().getMethod("getLine1Number", int.class);
            if (getLine1Number != null) {
                nums[0] = (String) getLine1Number.invoke(telephonyManager, 0);
                nums[1] = (String) getLine1Number.invoke(telephonyManager, 1);
            }
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "getSimSerialNumber " + e.toString());
        }
        return nums;
    }

    /**
     * 将JSON字符串转为RequestBody
     *
     * @param data
     */
    public RequestBody jsonToRequestBody(String data) {
        return RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"), data);
    }

    /**
     * 获取异常的详细信息
     *
     * @param e
     * @return
     */
    public static String getExceptionInfo(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

    /**
     * 回收bitmap
     *
     * @param bitmap
     */
    public static void gcBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    /**
     * 在子线程中显示Toast
     *
     * @param context
     * @param text
     * @param duration
     */
    public void showToastByRunnable(final Context context, final CharSequence text, final int duration) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                MagicToast magicToast = new MagicToast(context);
                magicToast.makeText(context, text).show();
            }
        });
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static boolean copyFile(String oldPath, String newPath) {
        if (TextUtils.isEmpty(oldPath) && TextUtils.isEmpty(newPath)) {
            return false;
        }

        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();

            }
        } catch (Exception e) {
            Log.e(TAG, "复制单个文件操作出错");
            e.printStackTrace();
            return false;

        }

        return true;
    }

    /**
     * 更换文件名
     *
     * @param basePath
     * @param oldName
     * @param newName
     */
    public void renameFile(String basePath, String oldName, String newName) {
        File mFile = new File(basePath + File.separator + oldName);
        mFile.renameTo(new File(basePath + File.separator + newName));
    }

    /**
     * 启动NetWorkChangeService
     */
    public void startNetWorkService() {
        Intent intentService = new Intent(mContext, NetWorkChangeService.class);
        startService(intentService);
    }

    /**
     * 网络是否已经连接
     */
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    /**
     * 判断是否有网络连接
     *
     * @param context
     * @return
     */
    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();

            //  NetworkCapabilities networkCapabilities = mConnectivityManager.getNetworkCapabilities(mConnectivityManager.getActiveNetwork());

            //  boolean b = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

            //  Log.i("Avalible", "NetworkCapalbilities:"+networkCapabilities.toString()+"  hasCapability=="+b);
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }


        return false;
    }

    /**
     * 是否有可用网络
     */
    public boolean isNetworkAvaliable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    /**
     * 获取当前的网络状态: 0：没有网络 1：WIFI网络 2：WAP网络 3：NET网络
     *
     * @return
     */
    public int getNetworkType() {
        int netType = 0;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            String extraInfo = networkInfo.getExtraInfo();
            if (!TextUtils.isEmpty(extraInfo)) {
                if ("cmnet".equals(extraInfo.toLowerCase())) {
                    netType = 3;
                } else {
                    netType = 2;
                }
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = 1;
        }
        return netType;
    }


    /**
     * 获得状态栏高度
     *
     * @param context
     * @return
     */
    public int getStatusBarHeight(Context context) {
        // 反射运行的类：android.R.dimen.status_bar_height.
        int mStatusHeight = -1;
        try {
            Class<?> mClass = Class.forName("com.android.internal.R$dimen");
            Object object = mClass.newInstance();
            String heightStr = mClass.getField("status_bar_height").get(object).toString();
            int height = Integer.valueOf(heightStr);
            //dp--->px
            mStatusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mStatusHeight;
    }

    /**
     * 获取应用版本号
     *
     * @param packageName
     * @return
     */
    public String getAppVersion(String packageName) {
        String version = null;

        try {
            version = mContext.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public String getImei() {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(mContext.TELEPHONY_SERVICE);
        String imei = telephonyManager.getImei(0);
        LogUtil.writeToFile(TAG, "getImei1 = " + imei);
        return imei;
    }

    public String getImei1() {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(mContext.TELEPHONY_SERVICE);
        String imei = telephonyManager.getImei(1);
        LogUtil.writeToFile(TAG, "getImei2 = " + imei);
        return imei;
    }

    //获得Ram
    public String getTotalRam() {//GB
        String path = "/proc/meminfo";
        String firstLine = null;
        int totalRam = 0;
        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader, 8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (firstLine != null) {
            totalRam = (int) Math.ceil((new Float(Float.valueOf(firstLine) / (1024 * 1024)).doubleValue()));
        }

        return totalRam + "GB";//返回1GB/2GB/3GB/4GB
    }

    //获得手机分辨率
    public String getResolution() {
        DisplayMetrics mDisplayMetrics = mContext.getResources().getDisplayMetrics();
        String resolution = mDisplayMetrics.widthPixels + "*" + mDisplayMetrics.heightPixels;
        return resolution;
    }

    //获得厂商
    public String getManufacturers() {
        return Build.BRAND;
    }

    //获得设备型号
    public String getModel() {
        return Build.MODEL;
    }

    //获得手机版本号
    public String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    //获得系统版本
    public String getSystemVersion() {
        //  return Build.DISPLAY;
        return Build.VERSION.RELEASE;
    }

    public String getIccid() {
        String iccid = null;
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getSimSerialNumber = telephonyManager.getClass().getMethod("getSimSerialNumber", int.class);
            if (getSimSerialNumber != null) {
                iccid = (String) getSimSerialNumber.invoke(telephonyManager, 0);
            }
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "getSimSerialNumber1 " + e.toString());
        }
        return iccid;
    }

    public String getIccid1() {
        String iccid = null;
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getSimSerialNumber = telephonyManager.getClass().getMethod("getSimSerialNumber", int.class);
            if (getSimSerialNumber != null) {
                iccid = (String) getSimSerialNumber.invoke(telephonyManager, 1);
            }
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "getSimSerialNumber2 " + e.toString());
        }
        return iccid;
    }


    public String getIccid2() {
        String iccid = null;
        //   List<SubscriptionInfo> list = SubscriptionManager.from(mContext).getActiveSubscriptionInfoList();
        try {

            Method getSubId = SubscriptionManager.class.getMethod("getSubId", int.class);
            //   Log.w(TAG," ---list.sub= "+list.get(0).getSimSlotIndex());
            int[] sub = (int[]) getSubId.invoke(null, 1 /*list.get(0).getSimSlotIndex()*/);
            if (sub != null) {
                for (int i = 0; i < sub.length; i++) {
                    Log.w(TAG, "---sub= " + sub[i]);

                    //  String iccid1 = null;
                    TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                    try {
                        Method getSimSerialNumber = telephonyManager.getClass().getMethod("getSimSerialNumber", int.class);
                        if (getSimSerialNumber != null) {
                            iccid = (String) getSimSerialNumber.invoke(telephonyManager, sub[i]);
                        }
                    } catch (Exception e) {
                        LogUtil.writeToFile(TAG, "getSimSerialNumber2 " + e.toString());
                    }
                    Log.w(TAG, " getIccid2-2 =" + iccid);
                }
            }
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "getSimSerialNumber " + e.toString());
        }
        return iccid;
    }

    /**
     * 获得sim卡imsi1
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public String getSubscriberId1() {
        String imsi = null;
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(TELEPHONY_SERVICE);
        try {
            Method getSubId = SubscriptionManager.class.getMethod("getSubId",  int.class);
            //参数是卡槽1
            int[] sub= (int[]) getSubId.invoke(null,0);
            if (sub !=null) {
                for (int i = 0; i < sub.length; i++) {
                    // Log.w(TAG, "---sub= " + sub[i]);
                    Method getSubscriberId = telephonyManager.getClass()
                            .getMethod("getSubscriberId", int.class);
                    if (getSubscriberId != null) {
                        imsi = (String) getSubscriberId.invoke(telephonyManager, sub[i]);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "getSimSerialNumber " + e.toString());
        }
        Log.w(TAG, "getSubscriberId1 " + imsi);
        return imsi;
    }

    /**
     * 获得sim卡imsi2
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public String getSubscriberId2() {
        String imsi = null;
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(
                TELEPHONY_SERVICE);
        try {
            Method getSubId = SubscriptionManager.class.getMethod("getSubId",  int.class);
            //参数是卡槽2
            int[] sub= (int[]) getSubId.invoke(null,1);
            if (sub !=null) {
                for (int i = 0; i < sub.length; i++) {
                    Log.w(TAG, "---sub= " + sub[i]);

                    Method getSubscriberId = telephonyManager.getClass().getMethod("getSubscriberId", int.class);
                    if (getSubscriberId != null) {
                        imsi = (String) getSubscriberId.invoke(telephonyManager, sub[i]);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.writeToFile(TAG, "getSimSerialNumber " + e.toString());
        }
        Log.w(TAG, "getSubscriberId2 " + imsi);
        return imsi;
    }

    /**
     * 获取文件中的数据，转字符，并添加到String
     *
     * @param filePath
     * @return
     */
    public String getFileData(String filePath) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 启动服务
     *
     * @param intent
     */
    public void startService(Intent intent) {

        if (Build.VERSION.SDK_INT >= 26) {
            mContext.startForegroundService(intent);
        } else {
            mContext.startService(intent);
        }
    }

    public void startForeground(Service service, String content, String title, int id) {
        try {

            Intent intent1 = new Intent(mContext, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 1, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = null;

            if (Build.VERSION.SDK_INT >= 26) {

                NotificationChannel channel = new NotificationChannel("notification1", "notification1", NotificationManager.IMPORTANCE_HIGH);

                ((NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
                notification = new Notification.Builder(service, "notification1")
                        .setContentIntent(pendingIntent)
                        .setContentTitle(title)
                        .setSmallIcon(R.mipmap.mi8sesplit8split1)
                        .setContentText(content)
                        .build();

            } else {

                notification = new Notification.Builder(service)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setSmallIcon(R.mipmap.mi8sesplit8split1)
                        .setContentIntent(pendingIntent)
                        .build();
            }

            notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
            notification.flags |= Notification.FLAG_NO_CLEAR;
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            //让该service前台运行，避免手机休眠时系统自动杀掉该服务
            //如果 id 为 0 ，那么状态栏的 notification 将不会显示。
            service.startForeground(id, notification);
        } catch (Exception e) {
            Log.w(TAG, e.toString());
        }
    }

    /**
     * 判断是否需要清除密码
     *
     * @param lockType
     */
    public static void whetherCancelLock(int lockType) {

        int hadLockTypes = 0;
        String lockTypeState = preferencesManager.getData(Common.lockTypes[lockType]);

        for (String type : Common.lockTypes) {
            if ("true".equals(preferencesManager.getData(type))) {
                ++hadLockTypes;
            }
        }

        if (hadLockTypes == 1) {
            if ("true".equals(lockTypeState)) {
                MDM.setPasswordNone();
            }
        }

        preferencesManager.removeData(Common.lockTypes[lockType]);
    }

    /**
     * 在下发解锁命令时，需清除所有锁屏状态
     */
    public static void clearLockState() {
        for (String type : Common.lockTypes) {
            preferencesManager.removeData(type);
        }

        preferencesManager.removeComplianceData(Common.lost_time_frame);
    }

    /**
     * 保存锁屏类型状态
     *
     * @param lockType
     */
    public static void storyLockType(String lockType) {
        preferencesManager.setData(lockType, "true");
    }

    /**
     * 用于判断离线与在线时是否失联
     * @param isLink
     */
    public synchronized void isLostCompliance(boolean isLink) {
        //获取调取函数所在类名称
        String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
        String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
        int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
        Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers );
        LogUtil.writeToFile(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers );

        if ("true".equals(preferencesManager.getComplianceData(Common.lost_compliance))) {

            //已失联时间
            String lost_time_frame = preferencesManager.getComplianceData(Common.lost_time_frame);
            //运行失联最长时间
            String lost_time = preferencesManager.getComplianceData(Common.lost_time);

            //在线
            if (isLink) {
               if (!TextUtils.isEmpty(lost_time_frame)) {
                    if ((System.currentTimeMillis() - Double.valueOf(lost_time_frame)) > (Double.valueOf(lost_time))) {
                        excuteLostCompliance();
                        LogUtil.writeToFile(TAG,"isLostCompliance2");

                        preferencesManager.removeComplianceData(Common.lost_time_frame);
                        cancelLostAlarm();
                    } else {
                        LogUtil.writeToFile(TAG,"isLostCompliance3");
                        Log.w(TAG,"isLostCompliance3");
                        preferencesManager.removeComplianceData(Common.lost_time_frame);
                        cancelLostAlarm();
                    }
                }
            //离线
            } else {
                if (!TextUtils.isEmpty(lost_time_frame)) {
                    if ((System.currentTimeMillis() - Double.valueOf(lost_time_frame)) > (Double.valueOf(lost_time) )) {
                        excuteLostCompliance();
                        LogUtil.writeToFile(TAG,"isLostCompliance4");

                        preferencesManager.removeComplianceData(Common.lost_time_frame);
                        cancelLostAlarm();
                    } else {
                        LogUtil.writeToFile(TAG,"isLostCompliance5");

                        startLostAlarm();
                    }

                } else {
                    LogUtil.writeToFile(TAG,"isLostCompliance6");

                    preferencesManager.setComplianceData(Common.lost_time_frame, System.currentTimeMillis() + "");
                    startLostAlarm();
                }
            }
        }

    }

    /**
     * 失联违规执行
     */
    public static void excuteLostCompliance() {
        String pwd = PreferencesManager.getSingleInstance().getComplianceData(Common.lost_password);
        if (TextUtils.isEmpty( pwd )) {
            MDM.setFactoryReset();
        } else {
            MDM.forceLockScreen(Common.lockTypes[5], pwd);
        }
    }

    /**
     * 取消失联闹钟
     */
    private void cancelLostAlarm() {
        Intent intent_cancleReceiver = new Intent( );
        intent_cancleReceiver.setAction( "lost_compliance" );
        PendingIntent pendingIntent = PendingIntent.getBroadcast( mContext, 11, intent_cancleReceiver, PendingIntent.FLAG_UPDATE_CURRENT );
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService( ALARM_SERVICE );
        alarmManager.cancel( pendingIntent );
    }

    /**
     * 启动失联闹钟
     */
    private void startLostAlarm() {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService( ALARM_SERVICE );
        Intent intent = new Intent(  );
        intent.setAction( "lost_compliance" );
        PendingIntent pi = PendingIntent.getBroadcast( mContext, 11, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        alarmManager.setExact( AlarmManager.RTC_WAKEUP, /*24 * 3600 **/System.currentTimeMillis() + 60 * 1000, pi ); //执行一次
    }

    public String getSDCardId() {

        String sdCardId = null;

        StorageManager mStorageManager = (StorageManager) TheTang.getSingleInstance().getContext().getSystemService(Context.STORAGE_SERVICE);

        List<StorageVolume> mList = mStorageManager.getStorageVolumes();

        for (StorageVolume mStorageVolume : mList) {

            try {
                Method getPath = mStorageVolume.getClass().getDeclaredMethod("getPath");
                Method isRemovable = mStorageVolume.getClass().getDeclaredMethod("isRemovable");
                getPath.setAccessible(true);
                isRemovable.setAccessible(true);
                String path = (String)getPath.invoke(mStorageVolume);
                boolean removable = (boolean) isRemovable.invoke(mStorageVolume);

                if (removable) {
                    String[] paths = path.split("/");
                    sdCardId  = paths[paths.length - 1];
                    break;
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }
        return sdCardId;
    }

    /**
     * 信息加密
     * {MD2、MD5、SHA-1、SHA-256、SHA-384、SHA-512}
     * @param encryptResouce
     * @param encryptAlgorithm
     * @return
     */
    public String getMessageEncrypt(String encryptResouce, String encryptAlgorithm) {
        String result = null;
        /*try {
            MessageDigest mMessageDigest = MessageDigest.getInstance(encryptAlgorithm);
            mMessageDigest.update(encryptResouce.getBytes());

            byte[] middleResult = mMessageDigest.digest();

            if (middleResult != null && middleResult.length > 0) {
                StringBuffer stringBuffer = new StringBuffer();
                for(int i = 0; i < middleResult.length; i ++){
                    stringBuffer.append(middleResult[i]);
                }
                result = stringBuffer.toString();
            }

        } catch (NoSuchAlgorithmException e) {
            LogUtil.writeToFile(TAG,"该算法不合法！");
            e.printStackTrace();
        }*/
        return result;
    }
}

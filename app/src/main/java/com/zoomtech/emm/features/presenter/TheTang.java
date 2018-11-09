package com.zoomtech.emm.features.presenter;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.zoomtech.emm.R;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.definition.OrderConfig;
import com.zoomtech.emm.features.db.DatabaseOperate;
import com.zoomtech.emm.features.event.MessageEvent;
import com.zoomtech.emm.features.event.StrategeEvent;
import com.zoomtech.emm.features.impl.LoginImpl;
import com.zoomtech.emm.features.impl.SendMessageManager;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.policy.compliance.ExcuteCompliance;
import com.zoomtech.emm.model.APPInfo;
import com.zoomtech.emm.model.AppBlackWhiteData;
import com.zoomtech.emm.model.MessageInfo;
import com.zoomtech.emm.model.MessageSendData;
import com.zoomtech.emm.model.SecurityChromeData;
import com.zoomtech.emm.model.SettingAboutData;
import com.zoomtech.emm.model.StrategeInfo;
import com.zoomtech.emm.utils.ConvertUtils;
import com.zoomtech.emm.utils.DeviceUtils;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.view.activity.MainActivity;
import com.zoomtech.emm.view.activity.MessageActivity;
import com.zoomtech.emm.utils.viewUtils.MagicToast;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

import static android.content.Context.ALARM_SERVICE;

//import android.app.usage.StorageStats;
//import android.app.usage.StorageStatsManager;

/**
 * MDM功能类
 * Created by Administrator on 2017/5/26.
 */

public class TheTang {
    public static final String TAG = "TheTang";

    protected static LoginImpl mLoginImpl;
    /*    protected static FeedBackImpl mFeedBackImpl;
        public ExcuteCompleteImpl mExcuteCompleteImpl;*/
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
        mExecutorService = Executors.newFixedThreadPool(DeviceUtils.getNumCores() + 1);
        //用于下载
        mExecutorServiceForDownload = Executors.newFixedThreadPool(1);
    }

    public void initImplTwo() {
//        mFeedBackImpl = new FeedBackImpl(mContext);
    }

    /***********************************设置别名***************************************************/
    /**
     * 设置别名
     *
     * @param delayTime
     * @param alias
     */
    public void setAlias(final int delayTime, final String alias) {//todo baii util ???
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
    public void login(String userName, String passWord) {//todo baii util http
        mLoginImpl = new LoginImpl(mContext);
        mLoginImpl.login(userName, passWord);
    }

    /**
     * 月份与时间格式转换
     *
     * @param time 时间戳
     * @return
     */
    /*modify by duanxin on 2017/08/31*/
    public String formatTime(long time) {//todo baii util time
        Date date = new Date(time);
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return mSimpleDateFormat.format(date);
    }

    /**
     * 获取已安装App信息
     *
     * @return
     */
    public List<APPInfo> getInstallAppInfo() {//todo baii util app
        return DatabaseOperate.getSingleInstance().queryInstallAppInfo();
    }

    /**
     * 获得线程池对象
     *
     * @return
     */
    public ExecutorService getThreadPoolObject() {//todo baii util thread
        return mExecutorService;
    }

    /**
     * 获得专用下载线程池对象
     *
     * @return
     */
    public ExecutorService getThreadPoolObjectForDownload() {//todo baii util thread
        return mExecutorServiceForDownload;
    }

    /**
     * 存储后台传递过来的命令通知
     *
     * @param orderCode
     */
    public void addMessageInfo(String orderCode) {//todo baii util ???
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
            case OrderConfig.SEND_WIFI_FENCE: //下发WiFi围栏
                break;
            case OrderConfig.DELETE_WIFI_FENCE: //删除WiFi围栏
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
    public void addMessage(String orderCode, String about) {//todo baii util ???
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
    private void showMessage(MessageInfo messageInfo) {//todo baii util ???
        showNotification(getMeaasgeInfo(messageInfo.getMessage_id()) + messageInfo.getMessage_about(),
                mContext.getResources().getString(R.string.message1), 1001);
    }

    public void showNotification(String content, String title, int id) {//todo baii util ???
        Intent intent = new Intent(mContext, MessageActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //int id = 1001;

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("notification", "notification", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Notification notification = new Notification.Builder(mContext, "notification")
                    .setContentIntent(pendingIntent)
                    .setContentTitle(title)
                    .setSmallIcon(R.mipmap.mi8sesplit8split1)
//                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setContentText(content)
                    .build();
            ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).notify(id, notification);
        } else {
            NotificationManager notifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
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

    public void cancelNotification(int id) {//todo baii util ???
        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);
    }

    /**
     * 获得id相对应命令
     *
     * @param message_id
     * @return
     */
    public String getMeaasgeInfo(String message_id) {//todo baii util ???
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
    public String getNetworkInfo(String net_id) {//todo baii util ???
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
    public String getUninstallInfo(String un_id) {//todo baii util ???
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
    public void addStratege(String orderCode, String name, String time) {//todo baii util ???
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
    public void deleteStrategeInfo(String code) {//todo baii util ???
        DatabaseOperate.getSingleInstance().deleteSimpleStrategeInfo(code);
        EventBus.getDefault().post(new StrategeEvent());
    }

    /**
     * 删除策略
     * for sensitive word
     *
     * @param code
     */
    public void deleteStrategeInfo(String code, String strategyName) {//todo baii util ???
        DatabaseOperate.getSingleInstance()
                .deleteSimpleStrategeInfoByName(code, strategyName);
        EventBus.getDefault()
                .post(new StrategeEvent());
    }

    /**
     * 获取Application上下文
     *
     * @return
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * 应用违规处理
     *
     * @param context
     * @param names
     */
    public void appViolationExcute(Context context, List<String> names) {//todo baii util ???
        Map<String, String> deny_apps = new HashMap<>();
        /**
         * 存入数据库
         */
        for (String name : names) {
            try {
                ApplicationInfo applicationInfo = mContext.getPackageManager().getApplicationInfo(name, 0);
                deny_apps.put((String) mContext.getPackageManager().getApplicationLabel(applicationInfo), name);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        DatabaseOperate.getSingleInstance().addAppDenyList(deny_apps);
        List<String> list = DatabaseOperate.getSingleInstance().queryDenyAppByType("0");
        sendAppDeny(context, "0", list);
        excuteAppCompliance();
    }

    /**
     * 执行应用违规
     */
    public void excuteAppCompliance() {//todo baii util ???
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
    public void appComplianceExcute(Context context, String packageName) {//todo baii util ???
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
     *
     * @param context
     * @param type
     * @param names
     */
    private void sendAppDeny(Context context, String type, List<String> names) {//todo baii util ???
        //baii impl 000000000000000000000000000000000000
        sendAppPolicyFeedback(context, type, names.toString());
    }

    /**
     * 应用合规反馈
     *
     * @param context
     * @param type
     */
    private void sendAppUnDeny(Context context, String type) {
//baii impl 000000000000000000000000000000000000
        sendAppPolicyFeedback(context, type, "null");
    }

    private void sendAppPolicyFeedback(Context context, String type, String names) {//todo baii util ???
        SendMessageManager manager = new SendMessageManager(context);
        String alias = PreferencesManager.getSingleInstance().getData(Common.alias);
        String appComplianceId = PreferencesManager.getSingleInstance().getComplianceData(Common.app_compliance_id);
        final JSONObject appObject = new JSONObject();
        try {
            appObject.put("alias", alias);
            appObject.put("appComplianceId", appComplianceId);
            appObject.put("type", type);
            appObject.put("names", names);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MessageSendData data = new MessageSendData(Common.app_impl, appObject.toString(), true);
        manager.sendMessage(data);
    }

    /**
     * 应用黑白名单
     *
     * @param appBlackWhiteData
     */
    public void storageBlackWhiteList(AppBlackWhiteData appBlackWhiteData) {//todo baii util ???
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
     * 解决List重复
     *
     * @param list
     * @return
     */
    public List<ApplicationInfo> removeApplicate(Collection<ApplicationInfo> list) {//todo baii util ???
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
     * 应用黑名单处理
     *
     * @param appBlackWhiteDataList
     * @param launcherNoSystemApps
     */
    public void appBlackListCompliance(List<String> appBlackWhiteDataList, List<String> launcherNoSystemApps) {//todo baii util ???
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
    public void appWhiteListCompliance(List<String> appBlackWhiteDataList, List<String> launcherNoSystemApps) {//todo baii util ???
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


    /**
     * 判断用户访问权限的状态
     *
     * @return
     */
    public boolean getUsageStats() {//todo baii util permission
        AppOpsManager appOps = (AppOpsManager) getContext().getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), getContext().getPackageName());
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
        return granted;
    }

    /**
     * 删除安全浏览器数据
     */
    public void deleteSecurityChrome() {//todo baii util ???
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
    public void storageSecurityChrome(SecurityChromeData securityChromeData) {//todo baii util ???
        preferencesManager.setComplianceData(Common.securityChrome, "true");
        preferencesManager.setComplianceData(Common.securityChrome_name, securityChromeData.sec_name);
        preferencesManager.setComplianceData(Common.securityChrome_id, securityChromeData.sec_id);
        preferencesManager.setComplianceData(Common.securityChrome_list, ConvertUtils.mapToString(securityChromeData.sec_white_list));
    }

    /**
     * 存储设置相关数据
     *
     * @param settingAboutData
     */
    public void storageSettingAboutData(SettingAboutData settingAboutData) {//todo baii util ???
        preferencesManager.setSettingData(Common.setting_help, settingAboutData.messageForHelp);
        preferencesManager.setSettingData(Common.setting_agreement, settingAboutData.agreementLicense);
        preferencesManager.setSettingData(Common.setting_stand_by, settingAboutData.supportContent);
    }

    /**
     * 回收bitmap
     *
     * @param bitmap
     */
    public static void gcBitmap(Bitmap bitmap) {//todo baii util ???
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
    public void showToastByRunnable(final Context context, final CharSequence text, final int duration) {//todo baii util ???
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
     * 获得状态栏高度
     *
     * @param context
     * @return
     */
    public int getStatusBarHeight(Context context) {//todo baii util view
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
     * 启动服务
     *
     * @param intent
     */
    public void startService(Intent intent) {//todo baii util ???
        if (Build.VERSION.SDK_INT >= 26) {
            mContext.startForegroundService(intent);
        } else {
            mContext.startService(intent);
        }
    }

    public void startForeground(Service service, String content, String title, int id) {//todo baii util ???
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
    public static void whetherCancelLock(int lockType) {//todo baii util ???
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
    public static void clearLockState() {//todo baii util ???
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
    public static void storyLockType(String lockType) {//todo baii util ???
        preferencesManager.setData(lockType, "true");
    }

    /**
     * 用于判断离线与在线时是否失联
     *
     * @param isLink
     */
    public synchronized void isLostCompliance(boolean isLink) {//todo baii util ???
        //获取调取函数所在类名称
        String classNames = Thread.currentThread().getStackTrace()[3].getClassName();
        String methodNames = Thread.currentThread().getStackTrace()[3].getMethodName();
        int lineNumbers = Thread.currentThread().getStackTrace()[3].getLineNumber();
        Log.w(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers);
        LogUtil.writeToFile(TAG, "---classNames= " + classNames + "---methodNames= " + methodNames + "---lineNumbers= " + lineNumbers);
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
                        LogUtil.writeToFile(TAG, "isLostCompliance2");
                        preferencesManager.removeComplianceData(Common.lost_time_frame);
                        cancelLostAlarm();
                    } else {
                        LogUtil.writeToFile(TAG, "isLostCompliance3");
                        Log.w(TAG, "isLostCompliance3");
                        preferencesManager.removeComplianceData(Common.lost_time_frame);
                        cancelLostAlarm();
                    }
                }
                //离线
            } else {
                if (!TextUtils.isEmpty(lost_time_frame)) {
                    if ((System.currentTimeMillis() - Double.valueOf(lost_time_frame)) > (Double.valueOf(lost_time))) {
                        excuteLostCompliance();
                        LogUtil.writeToFile(TAG, "isLostCompliance4");
                        preferencesManager.removeComplianceData(Common.lost_time_frame);
                        cancelLostAlarm();
                    } else {
                        LogUtil.writeToFile(TAG, "isLostCompliance5");
                        startLostAlarm();
                    }
                } else {
                    LogUtil.writeToFile(TAG, "isLostCompliance6");
                    preferencesManager.setComplianceData(Common.lost_time_frame, System.currentTimeMillis() + "");
                    startLostAlarm();
                }
            }
        }
    }

    /**
     * 失联违规执行
     */
    public static void excuteLostCompliance() {//todo baii util ???
        String pwd = PreferencesManager.getSingleInstance().getComplianceData(Common.lost_password);
        if (TextUtils.isEmpty(pwd)) {
            MDM.setFactoryReset();
        } else {
            MDM.forceLockScreen(Common.lockTypes[5], pwd);
        }
    }

    /**
     * 取消失联闹钟
     */
    private void cancelLostAlarm() {//todo baii util ???
        Intent intent_cancleReceiver = new Intent();
        intent_cancleReceiver.setAction("lost_compliance");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 11, intent_cancleReceiver, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    /**
     * 启动失联闹钟
     */
    private void startLostAlarm() {//todo baii util ???
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction("lost_compliance");
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 11, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, /*24 * 3600 **/System.currentTimeMillis() + 60 * 1000, pi); //执行一次
    }

    /**
     * 信息加密
     * {MD2、MD5、SHA-1、SHA-256、SHA-384、SHA-512}
     *
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

package com.zoomtech.emm.view.activity;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.miui.enterprise.sdk.ApplicationManager;
import com.zoomtech.emm.R;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.features.event.NotifySafedesk;
import com.zoomtech.emm.model.APPInfo;
import com.zoomtech.emm.model.ClearDeskData;
import com.zoomtech.emm.model.ConfigureStrategyData;
import com.zoomtech.emm.utils.AppUtils;
import com.zoomtech.emm.utils.ConvertUtils;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.view.adapter.AppsLauncherAdapter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by lenovo on 2017/9/12.
 */

public class SafeDeskActivity extends BaseActivity {
    private static final String TAG = "SafeDeskActivity";
    private List<ApplicationInfo> mApps; //推送的已安装的App信息  //mApps

    List<APPInfo> appList = new ArrayList<>();
    List<PackageInfo> packageInfoList = new ArrayList<>();

    private AppsLauncherAdapter launcherAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_safedesk;
    }

    @Override
    protected void initData() {
        mApps = new ArrayList<>();
        LoadAppTask loadAppTask = new LoadAppTask();
        loadAppTask.execute("load");
        mRecyclerView.setAdapter(launcherAdapter);
        PreferencesManager.getSingleInstance().setLockFlag("inetntSafeDesk", true);
    }

    @Override
    protected void initView() {

        mRecyclerView = mViewHolder.get(R.id.app_launcher);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        launcherAdapter = new AppsLauncherAdapter(this);

        initFlash();
    }

    private void initFlash() {
        MDM.mMDMController.setHome(TheTang.getSingleInstance().getContext().getPackageName());
        Log.d("baii", "sethome " + TheTang.getSingleInstance().getContext().getPackageName());
        MDM.mMDMController.disableDropdown();
        // 隐藏虚拟机
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        // == null
        if (!TextUtils.isEmpty(preferencesManager.getSecurityData(Common.safetyTosecureFlag)) &&
                !TextUtils.isEmpty(preferencesManager.getSecurityData(Common.secureDesktopFlag))) {
            MDM.enableFingerNavigation(false);
            MDM.setKeyVisible(true);
            MDM.setRecentKeyVisible(false);
            MDM.setHomeKeyVisible(false);

        } else {

            if ((TextUtils.isEmpty(preferencesManager.getFenceData(Common.setToSecureDesktop)) ||
                    "2".equals(preferencesManager.getFenceData(Common.setToSecureDesktop))
                    || TextUtils.isEmpty(preferencesManager.getFenceData(Common.insideAndOutside))
                    || "false".equals(preferencesManager.getFenceData(Common.insideAndOutside))) &&
                    !TextUtils.isEmpty(preferencesManager.getSafedesktopData("code"))) {

                Log.w(TAG, preferencesManager.getSafedesktopData("code") + "---隐藏虚拟机");
                MDM.enableFingerNavigation(false);
                MDM.setKeyVisible(true);
                MDM.setRecentKeyVisible(false);
                MDM.setHomeKeyVisible(false);

            } else if ("true".equals(preferencesManager.getFenceData(Common.insideAndOutside))) {

                if ("1".equals(preferencesManager.getFenceData(Common.setToSecureDesktop))) {
                    MDM.enableFingerNavigation(false);
                    MDM.setKeyVisible(true);
                    MDM.setRecentKeyVisible(false);
                    MDM.setHomeKeyVisible(false);

                } else if ("0".equals(preferencesManager.getFenceData(Common.setToSecureDesktop))) {
                    MDM.enableFingerNavigation(true);
                    MDM.setRecentKeyVisible(true);
                    MDM.setHomeKeyVisible(true);
                }

            }
        }
    }


    /**
     * AppAsnycTask 用于APP安装数据存储到数据库
     */
    public class LoadAppTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            loadApps();
            //getApp();
            getStrategyAppInfo();
            Log.w(TAG, "setFlag===App==更新安全桌面");
            return "load";
        }

        @Override
        protected void onPostExecute(Object o) {

            super.onPostExecute(o);
            launcherAdapter.setData(mApps);
            launcherAdapter.notifyDataSetChanged();

//            setDisallowedRunningApps();
        }
    }


    /**
     * 获取已安装应用信息
     */
    private void loadApps() {
        if (mApps.size() > 0) {
            mApps.clear();
        }

        if (packageInfoList.size() > 0) {
            packageInfoList.clear();
        }

        if (appList.size() > 0) {
            appList.clear();
        }

        packageInfoList = AppUtils.getNoSystemApp(this);

     //   appList = TheTang.getSingleInstance().getInstallAppInfo();

        //添加应用围栏app
        String appPackages = PreferencesManager.getSingleInstance().getAppFenceData(Common.appFenceAppPageName);
        List<String> mPackageNames = new ArrayList<>();

        if (appPackages != null) {

            String[] packages = appPackages.split(",");

            if (packages != null) {

                for (int i = 0; i < packages.length; i++) {
                    mPackageNames.add(packages[i]);
                }
            }
        }

        LogUtil.writeToFile(TAG, "mPackageNames = " + appPackages);

        //去掉重复的
        if (appList != null && appList.size() > 0) {
            if (mPackageNames != null && mPackageNames.size() > 0) {
                for (APPInfo app : appList) {
                    if (mPackageNames.contains(app.getPackageName())) {
                        mPackageNames.remove(app.getPackageName());
                    }
                }

                for (String packageName : mPackageNames) {
                    APPInfo appInfo = new APPInfo();
                    appInfo.setPackageName(packageName);
                    appList.add(appInfo);
                }
            }
        } else {
            for (String packageName : mPackageNames) {
                APPInfo appInfo = new APPInfo();
                appInfo.setPackageName(packageName);
                appList.add(appInfo);
            }
        }

        for (APPInfo app : appList) {
            LogUtil.writeToFile(TAG, "appList = " + app.getPackageName());
        }

        PackageManager packageManager = getPackageManager();

        if (appList != null && appList.size() > 0) {
            for (APPInfo app : appList) {
                try {
                    String str = app.getPackageName();
                    ApplicationInfo info = packageManager.getApplicationInfo(str, 0);
                    if (info != null) {
                        mApps.add(info);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    LogUtil.writeToFile(TAG, e.toString());
                    return;
                }
            }

        }

        for (ApplicationInfo info : mApps) {
            LogUtil.writeToFile(TAG, "mApps = " + info.packageName);
        }

    }

    /**
     * 获取策略的应用
     */
    public void getStrategyAppInfo() {
        List<ApplicationInfo> hashSet = new ArrayList<>(mApps);
        PackageManager packageManager = TheTang.getSingleInstance().getContext().getPackageManager();
        // hashSet.addAll();
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();


        if (!TextUtils.isEmpty(preferencesManager.getSecurityData(Common.safetyTosecureFlag)) &&
                !TextUtils.isEmpty(preferencesManager.getSecurityData(Common.secureDesktopFlag))) {

            String securityData = preferencesManager.getSecurityData(Common.safetyLimitDesktops);

            LogUtil.writeToFile(TAG, "securityData==" + securityData);
            if (!TextUtils.isEmpty(securityData)) {
                String[] split = securityData.split(",");
                for (int i = 0; i < split.length; i++) {
                    try {
                        hashSet.add(packageManager.getApplicationInfo(split[i], 0));
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {

            Type listType = new TypeToken<ArrayList<ClearDeskData.PolicyBean.ApplicationProgramBean>>() {
            }.getType();
            ArrayList<ClearDeskData.PolicyBean.ApplicationProgramBean> lists = new ArrayList<>();


            String timefenceData = preferencesManager.getTimefenceData(Common.applicationProgram);


            //如果是在时间围栏外或者没有时间围栏策略，则只需安全桌面策略
            //preferencesManager.getFenceData(Common.setToSecureDesktop) == null ||
            if (TextUtils.isEmpty(preferencesManager.getFenceData(Common.setToSecureDesktop)) ||
                    "2".equals(preferencesManager.getFenceData(Common.setToSecureDesktop)) ||
                    TextUtils.isEmpty(preferencesManager.getFenceData(Common.insideAndOutside)) ||
                    "false".equals(preferencesManager.getFenceData(Common.insideAndOutside))) {

                String extar = preferencesManager.getSafedesktopData(Common.applicationProgram);
                Log.w(TAG, ".getSafedesktopData==" + extar);

                if (!TextUtils.isEmpty(extar)) {
                    ArrayList<ClearDeskData.PolicyBean.ApplicationProgramBean> list1 = new Gson().fromJson(extar, listType);
                    Log.w(TAG, ".getSafedesktopData=list1=" + list1.toString());
                    lists.addAll(list1);

                }

                String displayMessage = preferencesManager.getSafedesktopData(Common.displayMessage);
                String displayContacts = preferencesManager.getSafedesktopData(Common.displayContacts);
                String displayCall = preferencesManager.getSafedesktopData(Common.displayCall);

                try {
                    if (!TextUtils.isEmpty(displayCall) && "1".equals(displayCall)) {

                        hashSet.add(packageManager.getApplicationInfo(Common.callPackageName, 0));

                    }
                    if ("1".equals(displayContacts)) {
                        hashSet.add(packageManager.getApplicationInfo(Common.contactsPackageName, 0));

                    }
                    if ("1".equals(displayMessage)) {
                        hashSet.add(packageManager.getApplicationInfo(Common.messagePackageName, 0));

                    }

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                for (ClearDeskData.PolicyBean.ApplicationProgramBean s : lists) {
                    String AppPageName = s.getPackageName();

                    if (!TextUtils.isEmpty(AppPageName)) {
                        try {
                            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(AppPageName, 0);
                            if (applicationInfo != null) {

                                hashSet.add(applicationInfo);
                            }
                        } catch (PackageManager.NameNotFoundException e) {

                        }
                    }
                }

            } else if (!TextUtils.isEmpty(preferencesManager.getFenceData(Common.insideAndOutside)) &&
                    "true".equals(preferencesManager.getFenceData(Common.insideAndOutside))) {

                //围栏 内 的桌面应用数据
                if (timefenceData != null && !timefenceData.isEmpty()) {
                    ArrayList<ClearDeskData.PolicyBean.ApplicationProgramBean> list2 = new Gson().fromJson(timefenceData, listType);
                    lists.addAll(list2);
                }


                for (ClearDeskData.PolicyBean.ApplicationProgramBean s : lists) {
                    String AppPageName = s.getPackageName();

                    if (!TextUtils.isEmpty(AppPageName)) {
                        try {
                            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(AppPageName, 0);
                            if (applicationInfo != null) {

                                hashSet.add(applicationInfo);
                            }
                        } catch (PackageManager.NameNotFoundException e) {

                        }
                    }
                }

                String displayMessage = preferencesManager.getTimefenceData(Common.displayMessage);
                String displayContacts = preferencesManager.getTimefenceData(Common.displayContacts);
                String displayCall = preferencesManager.getTimefenceData(Common.displayCall);
                try {
                    if (displayCall != null && !displayCall.isEmpty()) {
                        hashSet.add(packageManager.getApplicationInfo(displayCall, 0));
                        Log.w(TAG, "displayCall==" + displayCall);
                    }
                    if (displayContacts != null && !displayContacts.isEmpty()) {
                        hashSet.add(packageManager.getApplicationInfo(displayContacts, 0));
                        Log.w(TAG, "displayContacts==" + displayContacts);
                    }
                    if (displayMessage != null && !displayMessage.isEmpty()) {
                        hashSet.add(packageManager.getApplicationInfo(displayMessage, 0));
                        Log.w(TAG, "displayMessage==" + displayMessage);
                    }


                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }


            }
            Log.w(TAG, "getTimefenceData.PolicyBean.ApplicationProgramBean==" + timefenceData);
        }


        mApps.clear();


        try {
            //加入自己的
            mApps.add(packageManager.getApplicationInfo(getPackageName(), 0));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String webclipConfig = preferencesManager.getConfiguration("WebclipConfig");
        Log.w(TAG, "======webclipConfig====" + webclipConfig);
        String IN_PATH = "/MDM/Files/images/";
        if (!TextUtils.isEmpty(webclipConfig)) {

            Type type = new TypeToken<ArrayList<ConfigureStrategyData.ConfigureStrategyBean.WebclipListBean>>() {
            }.getType();

            ArrayList<ConfigureStrategyData.ConfigureStrategyBean.WebclipListBean> webclipListBeen = new Gson().fromJson(webclipConfig, type);

            Log.w(TAG, webclipListBeen.get(0).getWebClipName() + "======picName====--" + webclipListBeen.size());

            String savePath = TheTang.getSingleInstance().getContext().getApplicationContext().getFilesDir().getAbsolutePath() + IN_PATH;
            for (int i = 0; i < webclipListBeen.size(); i++) {
                ConfigureStrategyData.ConfigureStrategyBean.WebclipListBean bean = webclipListBeen.get(i);
                String webClipImgPath = bean.getWebClipImgPath();
                Log.w(TAG, webClipImgPath + "======bean.getWebClipImgPath()====" + bean.getWebClipImgPath());
                String[] split = webClipImgPath.split("\\\\");
                String picName = split[webClipImgPath.split("\\\\").length - 1];
                File file = new File(savePath + picName);
                Log.w(TAG, webClipImgPath + "======picName====" + picName + "--" + webClipImgPath.split("\\\\").length);
                if (file.exists()) {

                    ApplicationInfo applicationInfo = new ApplicationInfo();
                    applicationInfo.packageName = "com.huawei.fido.uafclient-" + i;//todo bai

                    applicationInfo.name = bean.getWebClipUrl();
                    mApps.add(applicationInfo);

                }
            }

        }

        //这是安全浏览器的白名单
        if ((!TextUtils.isEmpty(preferencesManager.getComplianceData(Common.securityChrome_list))
                && "true".equals(preferencesManager.getFenceData(Common.allowChrome)))
                || (("2".equals(preferencesManager.getFenceData(Common.allowChrome)) ||
                TextUtils.isEmpty(preferencesManager.getFenceData(Common.allowChrome))) &&
                !TextUtils.isEmpty(preferencesManager.getComplianceData(Common.securityChrome_list)))) {


            Map<String, String> sec_white_list = new HashMap<>();
            sec_white_list = ConvertUtils.jsonStringToMap(preferencesManager.getComplianceData(Common.securityChrome_list));

            List<String> url_list = new ArrayList<>();
            List<String> name_list = new ArrayList<>();
            Iterator<Map.Entry<String, String>> iterator = sec_white_list.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                //获得url的域名
                url_list.add(entry.getValue());
                name_list.add(entry.getKey());
            }

            for (int i = 0; i < url_list.size(); i++) {
                ApplicationInfo applicationInfo = new ApplicationInfo();
                applicationInfo.packageName = "com.huawei.fido.safe-" + name_list.get(i);
                applicationInfo.name = url_list.get(i);
                mApps.add(applicationInfo);
            }
        }

        hashSet = TheTang.getSingleInstance().removeApplicate(hashSet);


        Collections.sort(hashSet, new Comparator<ApplicationInfo>() {
            @Override
            public int compare(ApplicationInfo o1, ApplicationInfo o2) {
                String appLabel = AppUtils.getAppLabel(SafeDeskActivity.this, o1.packageName);
                String appLabel1 = AppUtils.getAppLabel(SafeDeskActivity.this, o2.packageName);
                return appLabel.compareTo(appLabel1);
            }
        });


        mApps.addAll(hashSet);

        for (ApplicationInfo inf : mApps) {
            Log.w(TAG, "---mApps---" + inf.packageName);
        }
    }

   /* @Subscribe(threadMode = ThreadMode.MAIN)
    @Override
    public void notifyData(NotifyEvent event) {

        LoadAppTask loadAppTask = new LoadAppTask();
        loadAppTask.execute("load");
        initFlash();
        Log.w(TAG, "load  加载notifyData");
    }
*/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void NotifySafe(NotifySafedesk event) {
        switch (event.getMsg()) {
            case "finsh":
                MDM.mMDMController.setHome("com.miui.home");//todo baii 替换掉硬编码
                Log.d("baii", "sethome com.miui.home");
/*                if (disallowedApps != null) {
                    disallowedApps.clear();
                    ApplicationManager.getInstance().setDisallowedRunningAppList(disallowedApps);
                }*/
                MDM.setRecentKeyVisible(true);//todo baii
                finish();//todo baii 桌面不能finish
                break;
            case "fulsh":
                LoadAppTask loadAppTask = new LoadAppTask();
                loadAppTask.execute("load");
                initFlash();
                Log.w(TAG, "load  刷新SafeDeskActivity");
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        return;
    }

    List<String> disallowedApps;
    private void setDisallowedRunningApps() {
        disallowedApps = new ArrayList<>();
        PackageManager packageManager = null;
        try {
            packageManager = getPackageManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<PackageInfo> allApps = packageManager.getInstalledPackages(0);
        for (PackageInfo info : allApps) {
            disallowedApps.add(info.packageName);
        }


        disallowedApps.remove(getPackageName());
        disallowedApps.remove("com.example.libai.xiaomidemo");
        List<APPInfo> installAppInfo = TheTang.getSingleInstance().getInstallAppInfo();
        if (installAppInfo != null && installAppInfo.size() > 0) {
            for (APPInfo appInfo : installAppInfo) {
                if (disallowedApps.contains(appInfo.getPackageName())) {
                    disallowedApps.remove(appInfo.getPackageName());
                }
            }
        }
        if (mApps != null && mApps.size() > 0) {
            for (ApplicationInfo appInfo : mApps) {
                if (disallowedApps.contains(appInfo.packageName)) {
                    disallowedApps.remove(appInfo.packageName);
                }
            }
        }
        ApplicationManager.getInstance().setDisallowedRunningAppList(disallowedApps);
    }


}

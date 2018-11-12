package com.zoomtech.emm.view.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.usage.NetworkStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.zoomtech.emm.R;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.features.event.NotifyEvent;
import com.zoomtech.emm.features.lockscreen.Lock2Activity;
import com.zoomtech.emm.features.lockscreen.NewsLifecycleHandler;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.utils.ActivityCollector;
import com.zoomtech.emm.utils.ConvertUtils;
import com.zoomtech.emm.utils.DataFlowStatsHelper;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.utils.PhoneUtils;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.view.ViewHolder;
import com.zoomtech.emm.view.listener.PermissionListener;
import com.zoomtech.emm.view.viewutils.DialogInfos;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Administrator on 2017/5/26.
 */

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    public ViewHolder mViewHolder;
    public TheTang theTang;
    // LoginOutReceiver mLoginOutReceiver;
    PermissionListener permissionListener = null;
    private DialogInfos mMInfos, mMInfo;
    private boolean flow_flag = false;
    private Dialog dialogNew = null;

    //切换字体
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //禁止截屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        //EventBus
        if (!InitActivity.class.getSimpleName().equals(this.getClass().getSimpleName()) && !EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this); //EventBus注册
        }
        mViewHolder = new ViewHolder(LayoutInflater.from(this), null, getLayoutId());
        theTang = TheTang.getSingleInstance();

        View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(option);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(mViewHolder.getRootView());

        // mLoginOutReceiver = null ;//new LoginOutReceiver();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("login_out");
        // registerReceiver( mLoginOutReceiver, mIntentFilter );

        initAction(mViewHolder);

        ActivityCollector.addActivity(this, getClass());

        Log.w(TAG, " add Activity = " + ActivityCollector.getActivity().keySet().toString());
        LogUtil.writeToFile(TAG, "add Activity =" + ActivityCollector.getActivity().keySet().toString());

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        long quota = PreferencesManager.getSingleInstance().getTraffictotal("quota");

        if (TheTang.getSingleInstance().getUsageStats()) {
            NetworkStatsManager networkStatsManager = (NetworkStatsManager) theTang.getContext().getSystemService(NETWORK_STATS_SERVICE);
            if (quota > 0 && (quota * 1024 * 1024) < (DataFlowStatsHelper.getAllMonthMobile(theTang.getContext(), null))) {
                if (0 == (PreferencesManager.getSingleInstance().getTraffictotal("quota_flag"))) {
                    if (!flow_flag) {

                        flow_flag = true;

                        noticeDilag();
                    }
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        initData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void notifyData(NotifyEvent event) {
        if (event.getMsg() != null && "flow_flag".equals(event.getMsg())) {
            noticeDilag();
        }
    }

    private void noticeDilag() {
        if (PhoneUtils.getNetWorkState(this) == PhoneUtils.CURRENT_NETWORK_STATES_NO
                || PhoneUtils.getNetWorkState(this) == PhoneUtils.CURRENT_NETWORK_STATES_WIFI) {
            return;
        }
        if (Lock2Activity.class.getSimpleName().equals(this.getClass().getSimpleName()) ||
                InitActivity.class.getSimpleName().equals(this.getClass().getSimpleName())) {
            return;
        }

        long quota = PreferencesManager.getSingleInstance().getTraffictotal("quota");
        if (quota <= 0) {
            return;
        }
        if (TheTang.getSingleInstance().getUsageStats()) {
            NetworkStatsManager networkStatsManager = (NetworkStatsManager) getSystemService(NETWORK_STATS_SERVICE);
            if (quota > 0 && (quota * 1024 * 1024) < (DataFlowStatsHelper.getAllMonthMobile(this, null))) {

                if (mMInfos != null) {
                    mMInfos.setData("额定流量", "    您当前的移动流量为" + ConvertUtils.convertTraffic(DataFlowStatsHelper.getAllMonthMobile(this, null)) + ",已经超过设置的额定移动流量" + quota + "M,是否关闭移动数据? \n  ");
                } else {
                    mMInfos = new DialogInfos(this, "额定流量", "    您当前的移动流量为" + ConvertUtils.convertTraffic(DataFlowStatsHelper.getAllMonthMobile(this, null)) + ",已经超过设置的额定移动流量" + quota + "M,是否关闭移动数据? \n  ", null, null, new DialogInfos.ConfirmListeners() {

                        @Override
                        public void sure() {
                            MDM.getSingleInstance().openDataConnectivity(false);
                        }

                        @Override
                        public void cancle() {
                            PreferencesManager.getSingleInstance().setTraffictotal("quota", 0);
                            PreferencesManager.getSingleInstance().removeTraffictotal("quota");
                            mMInfos = null;
                        }
                    });
                    resize(this, mMInfos);
                }
                mMInfos.show();
            }
        }
    }

    protected abstract int getLayoutId();

    protected abstract void initData();

    protected abstract void initView();

    private void initAction(ViewHolder holder) {

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        Toolbar mToolbar = holder.get(R.id.toolbar);

        if (mToolbar != null) {
            this.setSupportActionBar(mToolbar);
        }
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toast(String msg, int type) {
        TheTang.getSingleInstance().showToastByRunnable(getApplicationContext(), msg, type);
    }

    public void toastLong(String msg) {
        toast(msg, Toast.LENGTH_LONG);
    }

    public void openActivity(Class<?> cls) {
        openActivity(this, cls);
    }

    public void openActivity(Context context, Class<?> cls) {
        startActivity(new Intent(context, cls));
    }

    //请求允许权限
    public void requestRuntimePermissions(String[] permissions, PermissionListener permissionListener) {

        LogUtil.writeToFile(TAG, "PermissionListener!");
        List<String> permissionsList = new ArrayList<>();
        this.permissionListener = permissionListener;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
            }
        }

        if (!permissionsList.isEmpty()) {

            String[] strings = new String[permissionsList.size()];
            ActivityCompat.requestPermissions(this, permissionsList.toArray(strings), 0);

        } else {
            LogUtil.writeToFile(TAG, "requestRuntimePermissions1!");
            permissionListener.onGranted();
        }
    }

    //权限请求反馈
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        List<String> permissionsList = new ArrayList<>();
        switch (requestCode) {
            case 0:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        permissionsList.add(permissions[i]);
                    }
                }

                if (!permissionsList.isEmpty()) {
                    permissionListener.onDenied(permissionsList);
                } else {
                    permissionListener.onGranted();
                }
                break;
            default:
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "BaseActivity remove activity!" + getClass().getSimpleName());
        LogUtil.writeToFile(TAG, "BaseActivity remove activity!-----" + getClass().getSimpleName());

        ActivityCollector.removeActivity(this);
        Set<Class<?>> classes = ActivityCollector.getActivity().keySet();
        Log.w(TAG, "after remove  left activity!-----" + classes.toString());
        LogUtil.writeToFile(TAG, "after remove left activity=" + classes.toString());
        //  unregisterReceiver( mLoginOutReceiver );

        if (!InitActivity.class.getSimpleName().equals(this.getClass().getSimpleName()) && EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

            //如果在安全局域下，有点击桌面就拦截返回键
            if (!TextUtils.isEmpty(preferencesManager.getSecurityData(Common.safetyTosecureFlag))) {
                if (!TextUtils.isEmpty(preferencesManager.getSecurityData(Common.secureDesktopFlag))) {
                    if (getClass().getName().equals(SafeDeskActivity.class.getName())) {

                        return true;
                    } else if (ActivityCollector.getActivity() != null && ActivityCollector.getActivity()
                            .size() > 1) {

                        //  return super.onKeyDown(keyCode, event);
                        ActivityCollector.removeActivity(this);
                        return true;
                    } else {
                        startActivity(new Intent(this, SafeDeskActivity.class));
                        return true;
                    }
                } else {
                    //不执行任何动作，安全局域就算不打钩安全桌面就默认

                }

            } else {

                if (TextUtils.isEmpty(PreferencesManager.getSingleInstance()
                        .getFenceData(Common.setToSecureDesktop)) || "2".equals(
                        PreferencesManager.getSingleInstance()
                                .getFenceData(Common.setToSecureDesktop)) || TextUtils.isEmpty(
                        preferencesManager.getFenceData(Common.insideAndOutside)) || "false".equals(
                        preferencesManager.getFenceData(Common.insideAndOutside))) {

                    String codeSafedesk = preferencesManager.getSafedesktopData("code");

                    if (TextUtils.isEmpty(codeSafedesk)) {
                        //vivo专用
                        if (isLastActivityToLaucner()) {
                            return true;
                        }

                        // return super.onKeyDown(keyCode, event);
                    } else {


                        if (getClass().getName()
                                .equals(SafeDeskActivity.class.getName())) {

                            return true;
                        } else if (ActivityCollector.getActivity() != null && ActivityCollector.getActivity()
                                .size() > 1) {

                            return super.onKeyDown(keyCode, event);
                        } else {
                            startActivity(new Intent(this, SafeDeskActivity.class));
                            return true;
                        }

                    }
                } else if (!TextUtils.isEmpty(preferencesManager.getFenceData(Common.insideAndOutside)) && "true".equals(
                        preferencesManager.getFenceData(Common.insideAndOutside))) {

                    if (!TextUtils.isEmpty(preferencesManager.getFenceData(Common.setToSecureDesktop)) && "1".equals(
                            preferencesManager.getFenceData(Common.setToSecureDesktop))) {


                        if (getClass().getName().equals(SafeDeskActivity.class.getName())) {

                            return true;
                        } else if (ActivityCollector.getActivity() != null && ActivityCollector.getActivity()
                                .size() > 1) {

                            return super.onKeyDown(keyCode, event);
                        } else {
                            startActivity(new Intent(this, SafeDeskActivity.class));
                            return true;
                        }
                    } else if (!TextUtils.isEmpty(preferencesManager.getFenceData(Common.setToSecureDesktop)) && "0".equals(
                            preferencesManager.getFenceData(Common.setToSecureDesktop))) {
                        if (isLastActivityToLaucner()) {
                            return true;
                        }
                        Log.w(TAG, "ActivityCollector.getActivity().size()=" + ActivityCollector.getActivity().size());
                        // return super.onKeyDown(keyCode, event);
                        ActivityCollector.removeActivity(this);
                        return true;
                    } else if (TextUtils.isEmpty(preferencesManager.getFenceData(Common.setToSecureDesktop)) || "2".equals(
                            preferencesManager.getFenceData(Common.setToSecureDesktop))) {

                        String codeSafedesk = preferencesManager.getSafedesktopData("code");

                        if (TextUtils.isEmpty(codeSafedesk)) {
                            //vivo专用
                            if (isLastActivityToLaucner()) {
                                return true;
                            }

                            //return super.onKeyDown(keyCode, event);
                            ActivityCollector.removeActivity(this);
                            return true;
                        } else {
                            if (getClass().getName().equals(SafeDeskActivity.class.getName())) {
                                return true;
                            } else if (ActivityCollector.getActivity() != null && ActivityCollector.getActivity().size() > 1) {

                                //return super.onKeyDown(keyCode, event);
                                ActivityCollector.removeActivity(this);
                                return true;
                            } else {
                                startActivity(new Intent(this, SafeDeskActivity.class));
                                return true;
                            }
                        }
                    }
                }
            }
            //vivo专用
            if (isLastActivityToLaucner()) {
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_HOME)

        {
            Log.i(TAG, "===HOME====");
        } else if (keyCode == KeyEvent.KEYCODE_MENU)

        {
            Log.i(TAG, "===KEYCODE_MENU====");
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)

        {
            Log.i(TAG, "===KEYCODE_KEYCODE_VOLUME_UP====");
        }


        Log.w(TAG, "ActivityCollector.getActivity().size()s=" + ActivityCollector.getActivity().size());
        return super.onKeyDown(keyCode, event);
    }

    //用于退出应用
    class LoginOutReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.writeToFile(TAG, "login out!");
            Log.e(TAG, "login out!");
            finish();
        }
    }

    public void toastDialogs(final Context context, String content) {

        mMInfo = new DialogInfos(context, null, content, null, new DialogInfos.ConfirmListener() {
            @Override
            public void ok() {
                mMInfo.dismiss();
            }
        });
        resize(context, mMInfo);
        mMInfo.show();

    }


    public void loadDialog(Activity context, int res) {
        // 加载样式
        try {
            if (null == dialogNew) {
                dialogNew = new Dialog(context, R.style.my_dialog);
                dialogNew.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogNew.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialogNew.setCanceledOnTouchOutside(false);
                LayoutInflater inflater = LayoutInflater.from(context);
                // 得到加载view dialog_common
                View v = inflater.inflate(R.layout.load_progress_info, null);
                TextView tv = (TextView) v.findViewById(R.id.tv_dialog);
                tv.setText(res);
                // dialog透明
                WindowManager.LayoutParams lp = dialogNew.getWindow().getAttributes();
                lp.alpha = 0.8f;
                dialogNew.getWindow().setAttributes(lp);
                dialogNew.setContentView(v);
            }

            resize(context, dialogNew);
            dialogNew.show();
        } catch (Exception e) {
            Log.e(TAG, "" + e);
        }
    }


    public void dissDialog() {
        if (null != dialogNew) {
            BaseActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        dialogNew.dismiss();
                        dialogNew = null;
                    } catch (Exception e) {
                    }
                }
            });
        }
    }

    /**
     * 重设dialog 大小 为屏幕的80%
     */
    public static void resize(Context activity, Dialog dialog) {

        WindowManager m = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = m.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        //(int) (height * 0.5);
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.width = (int) (width * 0.85);
        window.setAttributes(layoutParams);

    }

    private boolean isLastActivityToLaucner() {
        //vivo专用这里，这里防止按回退键，应用被杀死，所以判断如果任务栈剩最后一activity就跳到桌面
        boolean whetherLogin = false;
        whetherLogin = PreferencesManager.getSingleInstance().getData(Common.token) != null && PreferencesManager.getSingleInstance().getData(Common.alias) != null;
        if (!whetherLogin) {
            return false;
        }
        if (ActivityCollector.getActivity() != null && ActivityCollector.getActivity().size() < 2) {
            if (!getClass().getName().equals(SafeDeskActivity.class.getName())) {
                Log.e(TAG, "用户登录，判断如果任务栈剩最后一activity就跳到桌面s");
                //跳转到主界面
                Intent intent = new Intent();
                intent.setAction("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.HOME");
                NewsLifecycleHandler.LockFlag = true;
                startActivity(intent);
                //还有一种情况是，围栏内有安全桌面，但是可以退出来(不是禁止退出的)
            } else if (!TextUtils.isEmpty(PreferencesManager.getSingleInstance().getFenceData(Common.setToSecureDesktop)) && "0".equals(
                    PreferencesManager.getSingleInstance().getFenceData(Common.setToSecureDesktop))) {
                Log.e(TAG, "用户登录，判断如果任务栈剩最后一activity就跳到桌面s");
                //跳转到主界面
                Intent intent = new Intent();
                intent.setAction("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.HOME");
                NewsLifecycleHandler.LockFlag = true;
                startActivity(intent);

            }


            return true;
        }
        return false;
    }
}

package com.zoomtech.emm.features.lockscreen;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zoomtech.emm.R;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.features.event.NotifySafedesk;
import com.zoomtech.emm.features.lockscreen.util.Consts;
import com.zoomtech.emm.features.lockscreen.widget.MyPasswordTextView;
import com.zoomtech.emm.features.lockscreen.widget.NumericKeyboard;
import com.zoomtech.emm.utils.ActivityCollector;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.view.activity.BaseActivity;
import com.zoomtech.emm.view.activity.MainActivity;
import com.zoomtech.emm.view.activity.SafeDeskActivity;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by lenovo on 2017/8/13
 */
public class Lock2Activity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "Lock2Activity";
    private TextView mTvDelete;
    private TextView mTvForgetPwd;
    // 数字键盘布局
    private NumericKeyboard nk;
    // 密码框
    private MyPasswordTextView et_pwd1, et_pwd2, et_pwd3, et_pwd4,et_pwd5,et_pwd6;
    private int type;
    private TextView tv_info;//提示信息
    //声明字符串保存每一次输入的密码
    private String input;
    private StringBuffer fBuffer = new StringBuffer();



    @Override
    protected int getLayoutId() {
        return R.layout.activity_lock2;
    }

    @Override
    protected void initData() {
        Log.w(TAG, " ===========2====is in : activity_lock2 "  );
        initListener();// 事件处理
       // Log.w(TAG, " ===========3====is in : activity_lock2 "  );
        //获取界面传递的值
        // type = getIntent().getIntExtra("type", 1);
        type =Consts.LOGIN_PASSWORD;


        /*if (PreferencesManager.getSingleInstance().getLockPassword("password").isEmpty()){
            type = Consts.SETTING_PASSWORD;
        }else {
            type=Consts.LOGIN_PASSWORD;
        }*/
    }
    @Override
    protected void initView() {

        //只要进入这里锁屏界面就做个标志，在NewsLifecycleHandler 类要判断
        PreferencesManager.getSingleInstance().setLockFlag("LockFlag",false);

        LogUtil.writeToFile("Lock2Activity","");
        nk = (NumericKeyboard) findViewById(R.id.nk);// 数字键盘
        // 密码框
        et_pwd1 = (MyPasswordTextView) findViewById(R.id.et_pwd1);
        et_pwd2 = (MyPasswordTextView) findViewById(R.id.et_pwd2);
        et_pwd3 = (MyPasswordTextView) findViewById(R.id.et_pwd3);
        et_pwd4 = (MyPasswordTextView) findViewById(R.id.et_pwd4);
        et_pwd5 = (MyPasswordTextView) findViewById(R.id.et_pwd5);
        et_pwd6 = (MyPasswordTextView) findViewById(R.id.et_pwd6);
        tv_info = (TextView) findViewById(R.id.tv_info);//提示信息
        mTvDelete = (TextView) findViewById(R.id.tv_delete);
        mTvForgetPwd = (TextView) findViewById(R.id.tv_forget_pwd);




        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

                //判断是否在安全局域里面
        if (!TextUtils.isEmpty( preferencesManager.getSecurityData( Common.safetyTosecureFlag ) )  ) {
                    if (!TextUtils.isEmpty( preferencesManager.getSecurityData( Common.secureDesktopFlag ) )){
                        MDM.getSingleInstance().enableFingerNavigation(false);
                        MDM.getSingleInstance().setKeyVisible( true );
                        MDM.getSingleInstance().setRecentKeyVisible(false);
                        MDM.getSingleInstance().setHomeKeyVisible(false);
                    }else {
                        //不执行任何动作，安全局域就算不打钩安全桌面就默认
                        MDM.getSingleInstance().enableFingerNavigation(true);
                        MDM.getSingleInstance().setRecentKeyVisible(true);
                        MDM.getSingleInstance().setHomeKeyVisible(true);
                    }

                }else {
                    if ( TextUtils.isEmpty(preferencesManager.getFenceData( Common.setToSecureDesktop))  ||
                            "2".equals( preferencesManager.getFenceData( Common.setToSecureDesktop) ) ||
                           TextUtils.isEmpty(preferencesManager.getFenceData( Common.insideAndOutside)) ||
                            "false".equals(preferencesManager.getFenceData( Common.insideAndOutside))  ){

                        if ( ! TextUtils.isEmpty(preferencesManager.getSafedesktopData("code"))) {
                            Log.w(TAG, preferencesManager.getSafedesktopData("code") + "---隐藏虚拟机");
                            MDM.getSingleInstance().enableFingerNavigation(false);
                            MDM.getSingleInstance().setKeyVisible( true );
                            MDM.getSingleInstance().setRecentKeyVisible(false);
                            MDM.getSingleInstance().setHomeKeyVisible(false);
                        } else {
                            MDM.getSingleInstance().enableFingerNavigation(true);
                            MDM.getSingleInstance().setRecentKeyVisible(true);
                            MDM.getSingleInstance().setHomeKeyVisible(true);
                        }
                    }else   if (
                            preferencesManager.getFenceData( Common.insideAndOutside) != null &&
                                    "true".equals(preferencesManager.getFenceData(Common.insideAndOutside))){

                        if ( !TextUtils.isEmpty(preferencesManager.getFenceData(Common.setToSecureDesktop))  &&
                                !"2".equals(preferencesManager.getFenceData(Common.setToSecureDesktop)) &&
                                "1".equals(preferencesManager.getFenceData(Common.setToSecureDesktop)) ){
                            MDM.getSingleInstance().enableFingerNavigation(false);
                            MDM.getSingleInstance().setKeyVisible( true );
                            MDM.getSingleInstance().setRecentKeyVisible(false);
                            MDM.getSingleInstance().setHomeKeyVisible(false);
                        }else {
                            MDM.getSingleInstance().enableFingerNavigation(true);
                            MDM.getSingleInstance().setRecentKeyVisible(true);
                            MDM.getSingleInstance().setHomeKeyVisible(true);
                        }

                    }
                }





    }

    /**
     * 事件处理
     */
    private void initListener() {
        // 设置点击的按钮回调事件
        nk.setOnNumberClick(new NumericKeyboard.OnNumberClick() {
            @Override
            public void onNumberReturn(int number) {
                // 设置显示密码
                setText(number + "");
            }
        });
        et_pwd1.setOnMyTextChangedListener(new MyPasswordTextView.OnMyTextChangedListener() {
            @Override
            public void textChanged(String content) {
                if (TextUtils.isEmpty(content)) {
                    mTvDelete.setVisibility(View.GONE);
                } else {
                    mTvDelete.setVisibility(View.VISIBLE);
                }
            }
        });
        //监听最后一个密码框的文本改变事件回调
        et_pwd6.setOnMyTextChangedListener(new MyPasswordTextView.OnMyTextChangedListener() {
            @Override
            public void textChanged(String content) {
                input = et_pwd1.getTextContent() + et_pwd2.getTextContent() +
                        et_pwd3.getTextContent() + et_pwd4.getTextContent()+ et_pwd5.getTextContent()+et_pwd6.getTextContent();
                //判断类型
                if (type == Consts.SETTING_PASSWORD) {//设置密码
                    //重新输入密码
                    tv_info.setText(getString(R.string.please_input_pwd_again));
                    type = Consts.SURE_SETTING_PASSWORD;
                    fBuffer.append(input);//保存第一次输入的密码
                } else {
                    PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
                    if (type == Consts.LOGIN_PASSWORD) {
                        //登录
                        if (!input.equals(preferencesManager.getLockPassword("password"))) {
                            showToastMsg("密码错误,请重新输入");
                        } else {
                            // showToastMsg("密码正确,欢迎登录");
                            preferencesManager.setLockFlag("lock2Acityvity",false);
                            //showToastMsg("密码正确");
                            //    startActivity(new Intent(Lock2Activity.this, LuckyActivity.class));
                            String secureDesktopFlag = preferencesManager.getSecurityData( Common.secureDesktopFlag );
                            Log.e(TAG," Common.secureDesktop="+ secureDesktopFlag );

                            /*-------跳转到相应的界面*/

                            String safetyTosecureFlag = preferencesManager.getSecurityData( Common.safetyTosecureFlag );
                            if (!TextUtils.isEmpty( safetyTosecureFlag )) {

                                    if (! TextUtils.isEmpty( secureDesktopFlag ) ) {

                                        Log.e(TAG," Common.secureDesktop="+ secureDesktopFlag +"跳转到相应的界面");
                                        openActivity(SafeDeskActivity.class);
                                    }else {
                                        //什么都不执行
                                        if (ActivityCollector.isActivityExist(SafeDeskActivity.class)){

                                            openActivity( MainActivity.class );
                                            EventBus.getDefault().post(new NotifySafedesk("finsh"));
                                        }
                                        //不执行任何动作
                                        Log.e(TAG," Common.secureDesktop="+ secureDesktopFlag +"不执行任何动作");
                                        MDM.getSingleInstance().enableFingerNavigation(true);
                                        MDM.getSingleInstance().setRecentKeyVisible(true);
                                        MDM.getSingleInstance().setHomeKeyVisible(true);

                                    }



                            }else {
                                    if ( TextUtils.isEmpty(preferencesManager.getFenceData( Common.insideAndOutside))  ||
                                            "false".equals(preferencesManager.getFenceData( Common.insideAndOutside))  ||
                                            TextUtils.isEmpty(preferencesManager.getFenceData( Common.insideAndOutside))){

                                        if ( ! TextUtils.isEmpty(preferencesManager.getSafedesktopData("code"))) {
                                            Log.w(TAG, preferencesManager.getSafedesktopData("code") + "---隐藏虚拟机");
                                            openActivity( SafeDeskActivity.class );

                                        } else {
                                            if (ActivityCollector.isActivityExist(SafeDeskActivity.class)){

                                                openActivity( MainActivity.class );
                                                EventBus.getDefault().post(new NotifySafedesk("finsh"));
                                            }
                                            MDM.getSingleInstance().enableFingerNavigation(true);
                                            MDM.getSingleInstance().setRecentKeyVisible(true);
                                            MDM.getSingleInstance().setHomeKeyVisible(true);
                                        }
                                    }else   if (
                                            preferencesManager.getFenceData( Common.insideAndOutside) != null &&
                                                    "true".equals(preferencesManager.getFenceData(Common.insideAndOutside))){

                                        if ( !TextUtils.isEmpty(preferencesManager.getFenceData(Common.setToSecureDesktop))  &&
                                                !"2".equals(preferencesManager.getFenceData(Common.setToSecureDesktop))){

                                            openActivity(SafeDeskActivity.class);
                                        }else {
                                            if (ActivityCollector.isActivityExist(SafeDeskActivity.class)){

                                                openActivity( MainActivity.class );
                                                EventBus.getDefault().post(new NotifySafedesk("finsh"));
                                            }
                                            MDM.getSingleInstance().enableFingerNavigation(true);
                                            MDM.getSingleInstance().setRecentKeyVisible(true);
                                            MDM.getSingleInstance().setHomeKeyVisible(true);
                                        }



                                    }
                            }
                            finish();
                            ActivityCollector.removeAllLock2Activity();
                        }
                    } else if (type == Consts.SURE_SETTING_PASSWORD) {//确认密码
                        //判断两次输入的密码是否一致
                        if (input.equals(fBuffer.toString())) {//一致
                            showToastMsg(getString(R.string.setting_pwd_success));
                            //保存密码到文件中

                            preferencesManager.setLockPassword("password", input);
                            type = Consts.LOGIN_PASSWORD;
                            tv_info.setText("你可以登录了");
                        } else {//不一致
                            showToastMsg(getString(R.string.not_equals));
                        }
                    }
                }
                startTimer();
            }
        });
        mTvDelete.setOnClickListener(this);
        mTvForgetPwd.setOnClickListener(this);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            clearText();
        }
    };

    private void startTimer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(0);
            }
        }).start();
    }

    /**
     * 设置显示的密码
     *
     * @param text
     */
    private void setText(String text) {
        // 从左往右依次显示
        if (TextUtils.isEmpty(et_pwd1.getTextContent())) {
            et_pwd1.setTextContent(text);
        } else if (TextUtils.isEmpty(et_pwd2.getTextContent())) {
            et_pwd2.setTextContent(text);
        } else if (TextUtils.isEmpty(et_pwd3.getTextContent())) {
            et_pwd3.setTextContent(text);
        } else if (TextUtils.isEmpty(et_pwd4.getTextContent())) {
            et_pwd4.setTextContent(text);
        }else if (TextUtils.isEmpty(et_pwd5.getTextContent())) {
            et_pwd5.setTextContent(text);
        }else if (TextUtils.isEmpty(et_pwd6.getTextContent())) {
            et_pwd6.setTextContent(text);
        }
    }

    /**
     * 清除输入的内容--重输
     */
    private void clearText() {
        et_pwd1.setTextContent("");
        et_pwd2.setTextContent("");
        et_pwd3.setTextContent("");
        et_pwd4.setTextContent("");
        et_pwd5.setTextContent("");
        et_pwd6.setTextContent("");
        mTvDelete.setVisibility(View.GONE);
    }

    /**
     * 删除刚刚输入的内容
     */
    private void deleteText() {
        // 从右往左依次删除
        if (!TextUtils.isEmpty(et_pwd6.getTextContent())){
            et_pwd6.setTextContent("");
        }else if (!TextUtils.isEmpty(et_pwd5.getTextContent())){
            et_pwd5.setTextContent("");
        }else if (!TextUtils.isEmpty(et_pwd4.getTextContent())) {
            et_pwd4.setTextContent("");
        } else if (!TextUtils.isEmpty(et_pwd3.getTextContent())) {
            et_pwd3.setTextContent("");
        } else if (!TextUtils.isEmpty(et_pwd2.getTextContent())) {
            et_pwd2.setTextContent("");
        } else if (!TextUtils.isEmpty(et_pwd1.getTextContent())) {
            et_pwd1.setTextContent("");
            mTvDelete.setVisibility(View.GONE);
        }
    }

    /**
     * 显示Toast提示信息
     *
     * @param text
     */
    private void showToastMsg(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        //判断点击的按钮
        switch (view.getId()) {
            case R.id.tv_forget_pwd://忘记密码?

                // startActivity(new Intent(this,FindPwdActivty.class));
                //  finish();
                //  showToastMsg("快去退出登录取消锁屏吧");
                showToastMsg("请联系管理员");
                break;
            case R.id.tv_delete://删除
                deleteText();//删除刚刚输入的内容
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // 按下键盘上返回按钮

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
            //如果在安全局域下，有点击桌面就拦截返回键
            if (!TextUtils.isEmpty( preferencesManager.getSecurityData( Common.safetyTosecureFlag ) )  ) {
                if (!TextUtils.isEmpty( preferencesManager.getSecurityData( Common.secureDesktopFlag ) )) {

                    return true;
                }else {
                    //不执行任何动作，安全局域就算不打钩安全桌面就默认

                }
            } else {

            if (TextUtils.isEmpty(preferencesManager.getFenceData(Common.insideAndOutside))  ||
                    "false".equals(preferencesManager.getFenceData(Common.insideAndOutside))) {

                String codeSafedesk = preferencesManager.getSafedesktopData("code");
                //如果是在围栏外，但是是有安全桌面策略，则直接拦截
                if (!TextUtils.isEmpty( codeSafedesk )) {
                    return true;
                }

            } else if (preferencesManager.getFenceData(Common.insideAndOutside) != null &&
                    "true".equals(preferencesManager.getFenceData(Common.insideAndOutside))) {
                //如果是在围栏内，而且是有安全桌面，则直接拦截
                if (!TextUtils.isEmpty(preferencesManager.getFenceData(Common.setToSecureDesktop))  &&
                        "1".equals(preferencesManager.getFenceData(Common.setToSecureDesktop))) {

                    return true;
                } else if (TextUtils.isEmpty(preferencesManager.getFenceData(Common.setToSecureDesktop)) ||
                        "2".equals(preferencesManager.getFenceData(Common.setToSecureDesktop))){

                    String codeSafedesk = preferencesManager.getSafedesktopData("code");
                    //如果是在围栏外，但是是有安全桌面策略，则直接拦截
                    if (!TextUtils.isEmpty( codeSafedesk )) {
                        return true;
                    }
                }
            }

            }
               /* List<Activity> activities = ((BaseApplication)getApplication()).getActivities();
                android.util.Log.w(TAG, "application is in KeyEvent.KEYCODE_BACK: "+activities.size() );

            for(Activity activity:activities){

                android.util.Log.w(TAG, "application is in finish: " +activity.getClass().getSimpleName());
                activity.finish();
                //	System.exit(0);
                //建议用这种
                //android.os.Process.killProcess(android.os.Process.myPid());
            }*/
            //ActivityCollector.removeAllActivity();
            NewsLifecycleHandler.LockFlag = true;
            Intent intent = new Intent();
            intent.setAction("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.HOME");
            startActivity(intent);

            MDM.getSingleInstance().enableFingerNavigation(true);
            MDM.getSingleInstance().setRecentKeyVisible(true);
            MDM.getSingleInstance().setHomeKeyVisible(true);
            finish();
            ActivityCollector.removeAllLock2Activity();
         //   System.exit(0);
            //建议用这种
          //  android.os.Process.killProcess(android.os.Process.myPid());
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

}

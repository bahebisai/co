package com.zoomtech.emm.view.activity;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zoomtech.emm.R;
import com.zoomtech.emm.definition.Common;
import com.zoomtech.emm.features.QR.utils.CommonUtil;
import com.zoomtech.emm.features.QR.zxing.activity.CaptureActivity;
import com.zoomtech.emm.features.event.LoginEvent;
import com.zoomtech.emm.features.excute.XiaomiMDMController;
import com.zoomtech.emm.model.LoginBackData;
import com.zoomtech.emm.utils.AppUtils;
import com.zoomtech.emm.utils.viewUtils.KeyboardUtils;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.MDM;
import com.zoomtech.emm.utils.PhoneUtils;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.HttpUrl;

/**
 * 登录界面
 * 1、PassWord加密
 * 2、软键盘监听
 * 3、EventBus回调
 * Created by Administrator on 2017/5/26.
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener{
    public static final String TAG = "LoginActivity";
    //LoginListener loginListener;
    EditText mUserName;
    EditText mPassword;
    EditText mIpAddress;
    EditText mPort;
    //Spinner mTransfer;
    Button mLogin;
    Button mQRcode;

    String userName = null;
    String passWord = null;
    String ipAddress = null;
    String ipPort = null;

    LinearLayout llLoginRoot, relContent;
    //打开扫描界面请求码
    public static int REQUEST_CODE = 0x01;
    //扫描成功返回码
    public static int RESULT_OK = 0xA1;

    PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    dissDialog();
                    openActivity(FirstScreenPasswordActivity.class);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        keepLoginBtnNotOver( llLoginRoot, relContent );

        //触摸外部，键盘消失
        llLoginRoot.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyboardUtils.closeKeyboard( LoginActivity.this );
                return false;
            }
        } );
        registerKeyBoardListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected int getLayoutId() {
        if (! TextUtils.isEmpty(preferencesManager.getData( Common.alias))){
            if (TextUtils.isEmpty( preferencesManager.getLockPassword( "password" ) )){
                openActivity(FirstScreenPasswordActivity.class);
            }else {
                openActivity(MainActivity.class);
            }
        }
        return R.layout.activity_login;
    }

    //PassWord监听
    @Override
    protected void initData() {
    }

    @Override
    protected void initView() {
        activeDeviceAdmin(XiaomiMDMController.mDevicePolicyManager, XiaomiMDMController.mComponentName);

        mUserName = mViewHolder.get( R.id.username );
        mPassword = mViewHolder.get( R.id.password );
        mIpAddress = mViewHolder.get( R.id.ipAddress );
        mPort = mViewHolder.get( R.id.ipPort );
        llLoginRoot = mViewHolder.get( R.id.ll_login_root );
        relContent = mViewHolder.get( R.id.rel_content );
        //mTransfer = mViewHolder.get( R.id.transfer_spinner );

        mLogin = mViewHolder.get( R.id.login );
        mQRcode = mViewHolder.get( R.id.QR_code );

        //监听软键盘的确定键
        mUserName.setOnEditorActionListener( new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                PreferencesManager.getSingleInstance().setData( "baseUrl", "https://" + mIpAddress.getText().toString() + ":" + mPort.getText().toString() + "/" );
                login( mUserName.getText().toString(), mPassword.getText().toString() );

                return false;
            }
        } );

        mPassword.setOnEditorActionListener( new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                PreferencesManager.getSingleInstance().setData( "baseUrl", "https://" + mIpAddress.getText().toString() + ":" + mPort.getText().toString() + "/" );

                login( mUserName.getText().toString(), mPassword.getText().toString() );
                return false;
            }
        } );
        mLogin.setOnClickListener( this );
        mQRcode.setOnClickListener( this );
    }

    /**
     * 登录
     *
     * @param userName
     * @param passWord
     */
    public void login(final String userName, final String passWord) {
        if (!PhoneUtils.isNetworkConnected(this)) {
            toastDialogs( this, getResources().getString(R.string.net_not_allow) );
            return;
        }
        final String baseUrl = preferencesManager.getData( "baseUrl" );
        preferencesManager.setData( Common.userName, userName );
        preferencesManager.setData( Common.passWord, passWord );
        if (!TextUtils.isEmpty( baseUrl )) {
            HttpUrl httpUrl = HttpUrl.parse( baseUrl );
            if (httpUrl == null) {
                PreferencesManager.getSingleInstance().removeData( "baseUrl" );
                TheTang.getSingleInstance().showToastByRunnable( TheTang.getSingleInstance().getContext(), getResources().getString(R.string.ip_or_port_error), Toast.LENGTH_SHORT );
                return;
            }

            loadDialog(this, R.string.login_loading);
            theTang.login( userName, passWord );
            /*else {
                if (Patterns.WEB_URL.matcher( baseUrl ).matches()) {
                    loadDialog(this, R.string.login_loading);
                    //符合标准
                    theTang.login( userName, passWord );
                } else {
                    //不符合标准
                    TheTang.getSingleInstance().showToastByRunnable( TheTang.getSingleInstance().getContext(), getResources().getString(R.string.net_not_allow), Toast.LENGTH_SHORT );
                }
            }*/
        } else {
            TheTang.getSingleInstance().showToastByRunnable(this, getResources().getString(R.string.ip_or_port_error), Toast.LENGTH_SHORT );
        }
    }


    /**
     * 注册软键盘监听
     */
    private void registerKeyBoardListener() {
        final View rootView = mViewHolder.getRootView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (isKeyboardShown( rootView )) {
                    mViewHolder.get( R.id.span1 ).setVisibility( View.GONE );
                    mViewHolder.get( R.id.span2 ).setVisibility( View.GONE );
                } else {
                    mViewHolder.get( R.id.span1 ).setVisibility( View.INVISIBLE );
                    mViewHolder.get( R.id.span2 ).setVisibility( View.INVISIBLE );
                }
            }
        } );
    }

    private boolean isKeyboardShown(View rootView) {
        final int softKeyboardHeight = 100;
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame( r );
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
        int heightDiff = rootView.getBottom() - r.bottom;
        return heightDiff > softKeyboardHeight * dm.density;
    }

    /**
     * EventBus回调
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogin(LoginEvent event) {

        String logs = null;
        if (event.isOk()) {

            PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService( Service.TELEPHONY_SERVICE );

            if (preferencesManager.getData( Common.alias ) == null) {
                //    theTang.setAlias(1000, ((LoginBackData)event.getBean()).getToken().getUser_alias());
                preferencesManager.setData( Common.token, ((LoginBackData) event.getBean()).getToken().getAccess_token() );
                preferencesManager.setData( Common.alias, ((LoginBackData) event.getBean()).getToken().getUser_alias() );

                //保存应用版本号
                preferencesManager.setData(Common.appVersion, AppUtils.getAppVersionName(this, Common.packageName));

                preferencesManager.setData( Common.keepAliveHost, ((LoginBackData) event.getBean()).getToken().getKeepAliveHost() );
                preferencesManager.setData( Common.keepAlivePort, ((LoginBackData) event.getBean()).getToken().getKeepAlivePort() );//preferencesManager.setData( Common.userName, userName );

                //用于存储手机的imie 以及sim卡的iccid
                //preferencesManager.setMachineData( Common.imei_phone, telephonyManager.getDeviceId() );
                //取出ICCID
                preferencesManager.setComplianceData( Common.iccid_card, telephonyManager.getSimSerialNumber() );
            }

            //logs = "登录成功!";
            String password = preferencesManager.getLockPassword( "password" );

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //请求白名单
                    MDM.getSingleInstance().updateTelepfohonyWhiteList();
                }
            }).start();

            if (TextUtils.isEmpty( password )) {
                mHandler.sendEmptyMessageDelayed(1, 2000);
            }

        } else {
            dissDialog();
            switch (event.getCode()) {
                /*case 1001:
                    logs = getResources().getString(R.string.device_had_bind);
                    break;*/
                /*case 1002:
                    logs = "登录失败：密码错误！";
                    break;*/
                case -1009:
                    //设备已绑定
                    logs = getResources().getString(R.string.device_had_bind);
                    break;
                case -1008:
                    //用户已绑定
                    logs = getResources().getString(R.string.user_had_bind);
                    break;
                case -1002:
                    //用户不存在
                    logs = getResources().getString(R.string.user_no_exist);
                    break;
                case -1001:
                    logs = getResources().getString(R.string.password_error);
                    break;
                case -1000:
                    logs = getResources().getString(R.string.server_dismiss);
                    break;
                case -1:
                    logs = getResources().getString(R.string.net_timeout);
                    break;
                case 0:
                    logs = getResources().getString(R.string.backdata_error);
                    break;
                case 500:
                    logs = getResources().getString(R.string.server_error);
                    break;

                default:
                    logs = getResources().getString(R.string.unkown_error, String.valueOf(event.getCode()));
                    break;
            }
            toastLong( logs );
        }

        LogUtil.writeToFile( TAG, logs );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        /**
         * 扫描结果回调
         */
        if (resultCode == RESULT_OK) { //RESULT_OK = -1
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString( "qr_scan_result" );
            jsonParser( scanResult );
        }
    }

    /**
     * 扫描返回数据解析
     *
     * @param scanResult
     */
    private void jsonParser(String scanResult) {
        try {
            JSONObject object = new JSONObject( scanResult );
            userName = object.getString( "loginName" );
            passWord = object.getString( "code" );

            ipPort = object.getString( "port" );
            ipAddress = object.getString( "domainName" );
            //用于切换Http或Https
            String wetherHttp = object.getString( "isHTTPS" );

            /*if (wetherHttp != null && "true".equals(wetherHttp)) {
                mTransfer.setSelection( 1 );
            } else {
                mTransfer.setSelection( 0 );
            }*/

            mUserName.setText( userName );
            mPassword.setText( passWord );
            mIpAddress.setText( ipAddress );

            mPort.setText( ipPort );

            if (userName.isEmpty() || passWord.isEmpty() || ipAddress.isEmpty() || ipPort.isEmpty()) {
                return;
            }

            PreferencesManager.getSingleInstance().setData( "baseUrl", "https://" + ipAddress + ":" + ipPort + "/" );
            login( userName, passWord );

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 保持登录按钮始终不会被覆盖
     *
     * @param root
     * @param subView
     */
    private void keepLoginBtnNotOver(final View root, final View subView) {
        root.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                // 获取root在窗体的可视区域
                root.getWindowVisibleDisplayFrame( rect );
                // 获取root在窗体的不可视区域高度(被其他View遮挡的区域高度)
                int rootInvisibleHeight = root.getRootView().getHeight() - rect.bottom;
                // 若不可视区域高度大于200，则键盘显示,其实相当于键盘的高度
                if (rootInvisibleHeight > 200) {
                    // 显示键盘时
                    int srollHeight = rootInvisibleHeight - (root.getHeight() - subView.getHeight()) - KeyboardUtils.getNavigationBarHeight( root.getContext() );
                    //当键盘高度覆盖按钮时
                    if (srollHeight > 0) {
                        root.scrollTo( 0, srollHeight - 600 );
                    }
                } else {
                    // 隐藏键盘时
                    root.scrollTo( 0, 0 );
                }
            }
        } );
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login:
                userName = mUserName.getText().toString();
                passWord = mPassword.getText().toString();
                ipAddress = mIpAddress.getText().toString();
                ipPort = mPort.getText().toString();

                if (ipAddress.isEmpty()) {
                    Toast.makeText( this, getResources().getString(R.string.input_ip), Toast.LENGTH_SHORT ).show();
                    return;
                }

                if (ipPort.isEmpty()) {
                    Toast.makeText( this, getResources().getString(R.string.input_port), Toast.LENGTH_SHORT ).show();
                    return;
                }

                if (userName.isEmpty()) {
                    Toast.makeText( this, getResources().getString(R.string.input_username), Toast.LENGTH_SHORT ).show();
                    return;
                }

                if (passWord.isEmpty()) {
                    Toast.makeText( this, getResources().getString(R.string.input_password), Toast.LENGTH_SHORT ).show();
                    return;
                }

                PreferencesManager.getSingleInstance().setData( "baseUrl", "https://" + ipAddress + ":" + ipPort + "/" );

                login( userName, passWord );
                break;
            case R.id.QR_code:
                if (CommonUtil.isCameraCanUse()) {
                    Intent intent = new Intent( this, CaptureActivity.class );
                    startActivityForResult( intent, this.REQUEST_CODE );
                } else {
                    Toast.makeText( this, getResources().getString(R.string.open_camera_permission), Toast.LENGTH_SHORT ).show();
                }
                break;
            default:
                break;
        }
    }

    private void activeDeviceAdmin(DevicePolicyManager devicePolicyManager, ComponentName componentName) {
        boolean isAdminActive = devicePolicyManager
                .isAdminActive(componentName);

        if(!isAdminActive){
            Intent intent = new Intent();
            //指定动作
            intent.setAction(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            //指定给那个组件授权
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            startActivity(intent);
        }
    }
}

package com.xiaomi.emm.view.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.xiaomi.emm.R;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.impl.SettingRequestImpl;
import com.xiaomi.emm.utils.ActivityCollector;
import com.xiaomi.emm.features.presenter.MDM;
import com.xiaomi.emm.features.manager.PreferencesManager;
import com.xiaomi.emm.view.activity.AboutActivity;
import com.xiaomi.emm.view.activity.AgreementActivity;
import com.xiaomi.emm.view.activity.HelpActivity;
import com.xiaomi.emm.view.activity.OldPasswordActivity;
import com.xiaomi.emm.view.activity.SupportActivity;

/**
 * Created by Administrator on 2017/6/27.
 */

public class SettingFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "SettingFragment";
    //View modify_login_password;
    View modify_unlock_password; //修改锁屏密码
    View help_text; //帮助
    View about_text; //关于
    View agreement_text; //许可协议
    View vpn_text; //vpn
    View stand_by_text; //支持
    View exit_workfragement_text; //退出工作台
    View check_notify_text; //检测更新
    Switch app_auto_switch; //自动安装
    //Button login_out;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_setting;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView(View view) {

        modify_unlock_password = mViewHolder.get( R.id.modify_unlock_password );
        modify_unlock_password.setClickable( true );
        modify_unlock_password.setOnClickListener( this );

        help_text = mViewHolder.get( R.id.help );
        help_text.setClickable( true );
        help_text.setOnClickListener( this );

        about_text = mViewHolder.get( R.id.about );
        about_text.setClickable( true );
        about_text.setOnClickListener( this );

        agreement_text = mViewHolder.get( R.id.agreement );
        agreement_text.setClickable( true );
        agreement_text.setOnClickListener( this );

        vpn_text = mViewHolder.get( R.id.vpn );
        vpn_text.setClickable( true );
        vpn_text.setOnClickListener( this );

        stand_by_text = mViewHolder.get( R.id.stand_by );
        stand_by_text.setClickable( true );
        stand_by_text.setOnClickListener( this );

        check_notify_text = mViewHolder.get( R.id.check_notify );
        check_notify_text.setClickable( true );
        check_notify_text.setOnClickListener( this );

        exit_workfragement_text = mViewHolder.get( R.id.exitworkfragement );
        exit_workfragement_text.setClickable( true );
        exit_workfragement_text.setOnClickListener( this );
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
        // == null &&
        if (TextUtils.isEmpty( preferencesManager.getSecurityData( Common.secureDesktopFlag ) )) {

                 if(   TextUtils.isEmpty( preferencesManager.getFenceData( Common.insideAndOutside ) ) ||
                         TextUtils.isEmpty(preferencesManager.getFenceData( Common.setToSecureDesktop))  ||
                         "2".equals( preferencesManager.getFenceData( Common.setToSecureDesktop) )||
                    "false".equals( preferencesManager.getFenceData( Common.insideAndOutside ) )) {
                //如果时间围栏外，或者时间围栏的里面的安全桌面策略不存在
                if (preferencesManager.getSafedesktopData( "passwordOrNot" ) != null && !preferencesManager.getSafedesktopData( "passwordOrNot" ).isEmpty()
                        && "1".equals( preferencesManager.getSafedesktopData( "passwordOrNot" ) )) {
                    exit_workfragement_text.setVisibility( View.VISIBLE );
                } else {
                    exit_workfragement_text.setVisibility( View.GONE );
                }
            }
        }
        app_auto_switch = mViewHolder.get( R.id.switch_app );
        app_auto_switch.setOnCheckedChangeListener( new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        } );

        new Thread( new Runnable() {
            @Override
            public void run() {
                SettingRequestImpl mSettingRequestImpl = new SettingRequestImpl( getActivity() );
                mSettingRequestImpl.getSettingData();
            }
        } ).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*case R.id.modify_login_password:

                break;*/
            case R.id.modify_unlock_password:
                Intent intent = new Intent( getActivity(), OldPasswordActivity.class );
                startActivity( intent );
                break;
            case R.id.help:
                Intent intent1 = new Intent( getActivity(), HelpActivity.class );
                startActivity( intent1 );
                break;
            case R.id.about:
                Intent intent2 = new Intent( getActivity(), AboutActivity.class );
                startActivity( intent2 );
                break;
            case R.id.agreement:
                Intent intent3 = new Intent( getActivity(), AgreementActivity.class );
                startActivity( intent3 );
                break;
            case R.id.vpn:
                break;
            case R.id.stand_by:
                Intent intent5 = new Intent( getActivity(), SupportActivity.class );
                startActivity( intent5 );
                break;
            case R.id.check_notify:
                break;
            case R.id.switch_app:
                break;
            /*case R.id.login_out:
                break;*/
            case R.id.exitworkfragement:

                final View dialogView = LayoutInflater.from( getActivity() )
                        .inflate( R.layout.layout_edit, null );
                AlertDialog.Builder builder = new AlertDialog.Builder( getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT );
                builder.setTitle( getResources().getString(R.string.please_input_pwd) ).
                        setIcon( android.R.drawable.ic_dialog_info ).setView( dialogView ).
                        setPositiveButton( getResources().getString(R.string.bt_dialog_sure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 获取EditView中的输入内容
                                EditText editText =
                                        (EditText) dialogView.findViewById( R.id.edit_text );
                                PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
                                String password = preferencesManager.getSafedesktopData( "password" );
                                if (password.equals( editText.getText().toString().trim() )) {

                                    MDM.enableFingerNavigation(true);
//                                    HuaweiMDMController.getSingleInstance().setRecentKeyVisible( true );//bai
//                                    HuaweiMDMController.getSingleInstance().setHomeKeyVisible( true );//bai
                                    dialog.dismiss();

                                    ActivityCollector.removeAllActivity();
                                    getActivity().finish();
                                } else {
                                    Toast.makeText( getActivity(), getResources().getString(R.string.input_password_error), Toast.LENGTH_SHORT ).show();
                                }

                            }
                        } ).setNegativeButton( getResources().getString(R.string.btnCancel), null );
                builder.setCancelable( false );
                AlertDialog dialog = builder.show();

                break;
            default:
                break;
        }
    }
}

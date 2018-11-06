package com.xiaomi.emm.view.activity;

import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.WindowManager;

import com.xiaomi.emm.R;
import com.xiaomi.emm.features.password.PasswordView;
import com.xiaomi.emm.utils.ActivityCollector;
import com.xiaomi.emm.features.manager.PreferencesManager;
import com.xiaomi.emm.features.presenter.TheTang;

/**
 * Created by Administrator on 2017/11/24.
 */

public class SecondScreenPasswordActivity extends BaseActivity implements TextWatcher {
    PasswordView password;
    String firstPassword = null;

    @Override
    protected int getLayoutId() {
        boolean lockFlag = !TextUtils.isEmpty( PreferencesManager.getSingleInstance().getLockPassword( "password" ) );
        if (lockFlag) {
            finish();
        }
        return R.layout.activity_secondscreen;
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void initView() {

        Intent intent = getIntent();
        firstPassword = intent.getStringExtra( "password" );

        Toolbar toolbar = mViewHolder.get( R.id.toolbar );

        toolbar.setPadding(
                toolbar.getPaddingLeft(),
                toolbar.getPaddingTop() + TheTang.getSingleInstance().getStatusBarHeight( this ),
                toolbar.getPaddingRight(),
                toolbar.getPaddingBottom() );

        password = mViewHolder.get( R.id.new_password );
        password.requestFocus();
        getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE );

        password.addTextChangedListener( this );
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //modify by duanxin for bug81 on 2017.09.22
        if (password.getText().toString().length() == 6) {

            PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

            if (password.getText().toString().equals( firstPassword )) {
                preferencesManager.setLockPassword( "password", password.getText().toString() );
                Intent intent = new Intent( this, MainActivity.class );
                //判断是否为第一次进入MainActivity
                intent.putExtra( "first", "true" );
                startActivity( intent );

                if (ActivityCollector.getActivity( FirstScreenPasswordActivity.class ) != null) {
                    ActivityCollector.getActivity( FirstScreenPasswordActivity.class ).finish();
                }
                if (ActivityCollector.getActivity( SecondScreenPasswordActivity.class ) != null) {
                    ActivityCollector.getActivity( SecondScreenPasswordActivity.class ).finish();
                }
            } else {
                password.getText().clear();
                toastLong( getResources().getString(R.string.password_inconsistent) );
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}

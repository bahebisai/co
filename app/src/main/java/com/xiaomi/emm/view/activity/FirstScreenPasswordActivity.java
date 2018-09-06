package com.xiaomi.emm.view.activity;

import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.WindowManager;

import com.xiaomi.emm.R;
import com.xiaomi.emm.features.password.PasswordView;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

/**
 * Created by Administrator on 2017/11/24.
 */

public class FirstScreenPasswordActivity extends BaseActivity implements TextWatcher {
    PasswordView password;

    @Override
    protected void onResume() {
        super.onResume();
        if (password.getText().toString() != null) {
            password.getText().clear();
        }
    }

    @Override
    protected int getLayoutId() {
        boolean lockFlag = !TextUtils.isEmpty( PreferencesManager.getSingleInstance().getLockPassword( "password" ) );
        if (lockFlag) {
            finish();
        }
        return R.layout.activity_firstscreen;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {

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
            Intent intent = new Intent( this, SecondScreenPasswordActivity.class );
            intent.putExtra( "password", password.getText().toString() );
            startActivity( intent );
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}

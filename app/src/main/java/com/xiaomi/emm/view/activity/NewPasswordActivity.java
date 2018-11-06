package com.xiaomi.emm.view.activity;

import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;

import com.xiaomi.emm.R;
import com.xiaomi.emm.features.password.PasswordView;
import com.xiaomi.emm.features.manager.PreferencesManager;
import com.xiaomi.emm.features.presenter.TheTang;

/**
 * Created by Administrator on 2017/8/10.
 */

public class NewPasswordActivity extends BaseActivity implements TextWatcher {

    PasswordView new_password;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_newpassword;
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

        new_password = mViewHolder.get(R.id.new_password);
        new_password.requestFocus();
        getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        new_password.addTextChangedListener( this );
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //modify by duanxin for bug81 on 2017.09.22
        if (new_password.getText().toString().length() == 6) {
            PreferencesManager.getSingleInstance().setLockPassword( "password", new_password.getText().toString());
            toastLong(getResources().getString(R.string.password_modify_success));
            finish();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}

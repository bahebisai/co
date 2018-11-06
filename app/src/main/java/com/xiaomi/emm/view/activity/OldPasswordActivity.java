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

public class OldPasswordActivity extends BaseActivity implements TextWatcher {

    PasswordView old_passwordView;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_oldpassword;
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

        old_passwordView = mViewHolder.get(R.id.old_password);
        old_passwordView.requestFocus();
        //弹出软键盘
        getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        old_passwordView.addTextChangedListener(this);

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //modify by duanxin for bug81 on 2017.09.22
        if (old_passwordView.getText().toString().length() == 6) {
            String old_pass = PreferencesManager.getSingleInstance().getLockPassword( "password" );
            if (old_pass.equals(old_passwordView.getText().toString())) {
                //跳转到新密码设置界面
                openActivity(NewPasswordActivity.class);
                finish();
            } else {
                old_passwordView.getText().clear();
                toastLong(getResources().getString(R.string.input_success_password));
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}

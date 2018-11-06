package com.xiaomi.emm.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

import com.xiaomi.emm.R;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.event.SettingEvent;
import com.xiaomi.emm.features.manager.PreferencesManager;
import com.xiaomi.emm.features.presenter.TheTang;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Administrator on 2017/8/10.
 */

public class SupportActivity extends BaseActivity {

    WebView support_text;
    String support_str = null;
    PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
    @Override
    protected int getLayoutId() {
        return R.layout.activity_support;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void initData() {
        support_str = preferencesManager.getSettingData( Common.setting_stand_by );

        if (support_str != null) {
            support_text.loadDataWithBaseURL( "",support_str, "text/html", "UTF-8",null );
        } else {
            support_text.loadDataWithBaseURL( "",null, "text/html", "UTF-8",null );
        }
    }

    @Override
    protected void initView() {

        Toolbar toolbar = mViewHolder.get( R.id.toolbar );

        toolbar.setPadding(
                toolbar.getPaddingLeft(),
                toolbar.getPaddingTop() + TheTang.getSingleInstance().getStatusBarHeight( this ),
                toolbar.getPaddingRight(),
                toolbar.getPaddingBottom() );

        support_text = mViewHolder.get(R.id.support_text);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void settingData(SettingEvent event) {
        initData();
    }
}

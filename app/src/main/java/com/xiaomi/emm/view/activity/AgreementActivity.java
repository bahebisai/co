package com.xiaomi.emm.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.xiaomi.emm.R;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.event.SettingEvent;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Administrator on 2017/8/10.
 */

public class AgreementActivity extends BaseActivity {
    WebView agreement_text;
    String agreement_str = null;
    PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_agreement;
    }


    @Override
    protected void initData() {

         agreement_str = preferencesManager.getSettingData( Common.setting_agreement );

        if (agreement_str != null) {
            agreement_text.loadDataWithBaseURL( "",agreement_str, "text/html", "UTF-8",null );
        } else {
            agreement_text.loadDataWithBaseURL( "",null, "text/html", "UTF-8",null );
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

        agreement_text = mViewHolder.get(R.id.agreement_text);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void settingData(SettingEvent event) {
        initData();
    }
}

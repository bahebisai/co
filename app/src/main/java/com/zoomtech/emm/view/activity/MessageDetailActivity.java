package com.zoomtech.emm.view.activity;

import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.zoomtech.emm.R;
import com.zoomtech.emm.features.event.MessageEvent;
import com.zoomtech.emm.model.MessageInfo;
import com.zoomtech.emm.features.presenter.TheTang;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2017/8/3.
 */

public class MessageDetailActivity extends BaseActivity {
    Toolbar toolbar;
    TextView fromText;
    TextView timeText;
    TextView aboutText;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_message_detail;
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        fromText.setText(intent.getStringExtra("from"));
        timeText.setText(intent.getStringExtra("time"));
        aboutText.setText(intent.getStringExtra("about"));
    }

    @Override
    protected void initView() {
        toolbar = mViewHolder.get( R.id.toolbar );

        toolbar.setPadding(
                toolbar.getPaddingLeft(),
                toolbar.getPaddingTop() + TheTang.getSingleInstance().getStatusBarHeight( this ),
                toolbar.getPaddingRight(),
                toolbar.getPaddingBottom() );

        toolbar.setTitle(getResources().getString(R.string.message_detail));

        fromText = mViewHolder.get(R.id.message_detail_from);
        timeText = mViewHolder.get(R.id.message_detail_time);
        aboutText = mViewHolder.get(R.id.message_detail_about);

        MessageInfo messageInfo = new MessageInfo();
        EventBus.getDefault().post(new MessageEvent(messageInfo));
    }
}

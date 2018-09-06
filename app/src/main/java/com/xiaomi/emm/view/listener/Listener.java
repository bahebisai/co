package com.xiaomi.emm.view.listener;

import android.view.View;

import com.xiaomi.emm.view.ViewHolder;

/**
 * Created by Administrator on 2017/7/6.
 */

public abstract class Listener implements View.OnClickListener {

    @Override
    public void onClick(View view) {
        onClickView(view);
    }

    public abstract void onClickView(View view);

    public void setOnClickListener(ViewHolder viewHolder, int ...ids) {
        if (ids == null) {
            return;
        }
        for (int id : ids) {
            viewHolder.get(id).setOnClickListener(this);
        }
    }
}

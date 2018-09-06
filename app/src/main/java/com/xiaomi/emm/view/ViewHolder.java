package com.xiaomi.emm.view;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 界面布局及监听
 * Created by Administrator on 2017/5/26.
 */

public class ViewHolder {
    protected View mRootView;
    protected SparseArray<View> mViews;

    public ViewHolder (LayoutInflater inflater, ViewGroup parent,int layout) {
        this.mRootView = inflater.inflate(layout,parent,false);
        this.mViews = new SparseArray<>();
    }

    public <T extends View> T get(int resId) {
        View view = mViews.get(resId);
        if (view == null) {
            view = mRootView.findViewById(resId);
            mViews.put(resId,view);
        }
        return (T)view;
    }

    public View getRootView() {
        return mRootView;
    }

    public void setOnClickListener(View.OnClickListener listener, int ...ids) {
        if (ids == null) {
            return;
        }
        for (int id : ids) {
            get(id).setOnClickListener(listener);
        }
    }
}

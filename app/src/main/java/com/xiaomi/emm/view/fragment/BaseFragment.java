package com.xiaomi.emm.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.emm.features.presenter.TheTang;
import com.xiaomi.emm.view.ViewHolder;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2017/6/28.
 */

public abstract class BaseFragment extends Fragment {
    public ViewHolder mViewHolder;
    public TheTang theTang;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mViewHolder = new ViewHolder(inflater,null,getLayoutId());
        theTang = TheTang.getSingleInstance();
        initToolBar();
        initView(mViewHolder.getRootView());
        return mViewHolder.getRootView();
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister( this );
    }


    private void initToolBar() {
        setHasOptionsMenu(true);
    }

    protected abstract int getLayoutId();

    protected abstract void initData();

    protected abstract void initView(View view);

    public void openActivity(Class<?> cls) {
        openActivity(getActivity(),cls);
    }

    public void openActivity(Context context, Class<?> cls) {
        startActivity(new Intent(context,cls));
    }
}

package com.zoomtech.emm.view.fragment;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;

import com.zoomtech.emm.R;
import com.zoomtech.emm.features.event.NotifyEvent;
import com.zoomtech.emm.utils.AppUtils;
import com.zoomtech.emm.view.adapter.AppStoreAdapter;
import com.zoomtech.emm.features.db.DatabaseOperate;
import com.zoomtech.emm.model.APPInfo;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.view.viewutils.ViewLoadingInterface;
import com.zoomtech.emm.view.viewutils.ViewLoadingLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/6/27.
 */

public class AppStoreFragment extends BaseFragment {
    private ViewLoadingLayout viewloading;
    List<APPInfo> list_info;
    ListView listView;
    AppStoreAdapter mAppStoreAdapter;

    Handler handler = new Handler(  ) {

        @Override
        public void handleMessage(Message msg) {
            if (list_info != null && list_info.size() > 0){

                viewloading.setErrorType(ViewLoadingLayout.HIDE_LAYOUT);
                mAppStoreAdapter.setData(list_info);
            }else {
                viewloading.setErrorType(ViewLoadingLayout.NODATA);
            }
            mAppStoreAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_appstore;
    }

    @Override
    protected void initData() {
        list_info = new ArrayList<>();
        readList();
    }

    private void readList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (list_info != null && list_info.size() > 0) {
                    list_info.clear();
                }
                list_info = TheTang.getSingleInstance().getInstallAppInfo();
                /*解决降序排列问题 added by duanxin for Bug62 on 2017/08/31*/
                Collections.reverse(list_info);

                for (APPInfo appInfo : list_info) {
                    PackageManager packageManager = getContext().getPackageManager();
                    String version = null;
                    try {
                        PackageInfo info = packageManager.getPackageInfo( appInfo.getPackageName(), 0 );
                        version = info.versionName;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        DatabaseOperate.getSingleInstance().deleteInstallAppInfo( appInfo.getAppId() );
                        continue;
                    }

                    appInfo.setVersion( version );

                    try {
                        String size = AppUtils.getAppSize(getContext(), appInfo.getPackageName());
                        appInfo.setSize( size );

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendMessage( new Message() );
            }
        }).start();
    }

    @Override
    protected void initView(View view) {

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this); //EventBus注册
        }

        listView = mViewHolder.get(R.id.store_list);
        listView.setFooterDividersEnabled(false);
        mAppStoreAdapter = new AppStoreAdapter(getActivity());
        listView.setAdapter(mAppStoreAdapter);
        viewloading = (ViewLoadingLayout) mViewHolder.get(R.id.viewloading);

        viewloading.setInit(new ViewLoadingInterface() {
            @Override
            public void Reload() {
                //点击重新加载调用
              //  Toast.makeText(getActivity(),TheTang.getSingleInstance().getContext().getResources().getString(R.string.device_getnetdate),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void NoDate(int viewId) {
                if (viewId == R.id.btn_1) {
                    //按钮1
                    //getNetData(refresh,minid);

                }
            }
        },getResources().getString(R.string.app_information),null);



    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void notifyData(NotifyEvent event) {
        readList();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}

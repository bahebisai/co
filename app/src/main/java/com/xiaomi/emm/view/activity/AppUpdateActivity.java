package com.xiaomi.emm.view.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.ViewStub;
import android.widget.ListView;
import android.widget.Toast;

import com.xiaomi.emm.R;
import com.xiaomi.emm.utils.AppUtils;
import com.xiaomi.emm.view.adapter.AppStoreAdapter;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.event.NotifyEvent;
import com.xiaomi.emm.model.APPInfo;
import com.xiaomi.emm.features.presenter.TheTang;
import com.xiaomi.emm.view.viewutils.ViewLoadingInterface;
import com.xiaomi.emm.view.viewutils.ViewLoadingLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/7/5.
 */

public class AppUpdateActivity extends BaseActivity {
    Toolbar toolbar;
    List<APPInfo> list_info;
    ListView listView;
    AppStoreAdapter mAppStoreAdapter;
    private ViewLoadingLayout viewloading;
    Handler handler = new Handler(  ) {

        @Override
        public void handleMessage(Message msg) {
            if (list_info!=null && list_info.size()>0){

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
        return R.layout.activity_appstore;
    }

    @Override
    protected void initData() {
        list_info = new ArrayList<>();
        readList();
    }

    /**
     * 读取数据库安装应用数据
     */
    private void readList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (list_info != null && list_info.size() > 0) {
                    list_info.clear();
                }

                List<APPInfo> newList = new ArrayList<>(  );

                newList = TheTang.getSingleInstance().getInstallAppInfo();

                /*解决降序排列问题 added by duanxin for Bug62 on 2017/08/31*/
                Collections.reverse(newList);

                for (APPInfo appInfo : newList) {
                    PackageManager packageManager = getPackageManager();
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
                        String size = AppUtils.getAppSize(AppUpdateActivity.this, appInfo.getPackageName());
                        appInfo.setSize( size );

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    list_info.add( appInfo );

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
    protected void initView() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this); //EventBus注册
        }

        toolbar = mViewHolder.get(R.id.toolbar);

        toolbar.setPadding(
                toolbar.getPaddingLeft(),
                toolbar.getPaddingTop() + TheTang.getSingleInstance().getStatusBarHeight( this ),
                toolbar.getPaddingRight(),
                toolbar.getPaddingBottom() );

        toolbar.setNavigationIcon(R.mipmap.arrow_back);

        listView = mViewHolder.get(R.id.store_activity_list);
        listView.addFooterView(new ViewStub(this));

        mAppStoreAdapter = new AppStoreAdapter(this);
        listView.setAdapter(mAppStoreAdapter);
        viewloading = (ViewLoadingLayout) mViewHolder.get(R.id.viewloading);

        viewloading.setInit(new ViewLoadingInterface() {
            @Override
            public void Reload() {
                //点击重新加载调用
                Toast.makeText(AppUpdateActivity.this,getResources().getString(R.string.device_getnetdate),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void NoDate(int viewId) {
                if (viewId == R.id.btn_1) {
                    //按钮1
                    //getNetData(refresh,minid);

                }
            }
        }, null, null);



    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    @Override
    public void notifyData(NotifyEvent event) {
        readList();
    }
}

package com.xiaomi.emm.view.fragment;

import android.app.usage.NetworkStatsManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomi.emm.R;
import com.xiaomi.emm.base.BaseApplication;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.event.MessageEvent;
import com.xiaomi.emm.features.event.NotifyEvent;
import com.xiaomi.emm.features.progress.CircleProgress;
import com.xiaomi.emm.model.APPInfo;
import com.xiaomi.emm.model.FileInfo;
import com.xiaomi.emm.model.MessageInfo;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.utils.MDM;
import com.xiaomi.emm.utils.NetworkStatsHelper;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;
import com.xiaomi.emm.view.activity.AppUpdateActivity;
import com.xiaomi.emm.view.activity.FileUpdateActivity;
import com.xiaomi.emm.view.activity.MessageActivity;
import com.xiaomi.emm.view.adapter.LauncherAdapter;
import com.xiaomi.emm.view.adapter.WorkBenchAdapter;
import com.xiaomi.emm.view.viewutils.ViewLoaddingInterface;
import com.xiaomi.emm.view.viewutils.ViewLoadingLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.NETWORK_STATS_SERVICE;

/**
 * Created by Administrator on 2017/11/28.
 */

public class WorkBenchFragment extends BaseFragment {

    private String TAG = "WorkBenchFragment";
    private ViewLoadingLayout viewloading;
    //ViewPage
    List<View> views;
    ViewPager mViewPager;
    View storage_page;
    View app_page;
    CircleProgress storage_progress;
    CircleProgress app_progress;
    ImageView arrow_right;
    ImageView arrow_left;

    //CardView
    View stub;
    View app_update;
    View file_update;
    View wifi_update;
    View data_update;

    TextView app_count;
    TextView file_count;
    TextView wifi_count;
    TextView data_count;

    //RecycleView
    //推送的已安装的App信息
    private List<ApplicationInfo> mApps;
    private LauncherAdapter launcherAdapter;
    private RecyclerView mRecyclerView;

    //Remind
    TextView remindText;

    //Data
    float total_size = 0;
    float remain_size = 0;
    String unit = null;


    List<LauncherActivityInfo> launcherInfoList = new ArrayList<>();

    List<FileInfo> list_info = new ArrayList<>();

    List<APPInfo> appList = new ArrayList<>();

    List<MessageInfo> messageList = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_workbench;
    }

    @Override
    protected void initData() {
        mApps = new ArrayList<>();
        LoadAppTask loadAppTask = new LoadAppTask();
        loadAppTask.execute( "load" );
        mRecyclerView.setAdapter( launcherAdapter );
    }

    @Override
    protected void initView(View view) {

        //EventBus
        if (!EventBus.getDefault().isRegistered( this )) {
            EventBus.getDefault().register( this ); //EventBus注册
        }

        //ViewPage
        mViewPager = mViewHolder.get( R.id.viewpager );

        View storage_page = getActivity().getLayoutInflater().inflate( R.layout.layout_storage_page, null );
        View app_page = getActivity().getLayoutInflater().inflate( R.layout.layout_app_page, null );

        views = new ArrayList<>();

        views.add( storage_page );
        views.add( app_page );

        mViewPager.setAdapter( new WorkBenchAdapter( views ) );

        storage_progress = (CircleProgress) storage_page.findViewById( R.id.storage_progress );
        app_progress = (CircleProgress) app_page.findViewById( R.id.app_progress );
        viewloading = (ViewLoadingLayout) mViewHolder.get( R.id.viewloading );

        //CardView
        stub = ((ViewStub) mViewHolder.get( R.id.card_view )).inflate();

        app_update = stub.findViewById( R.id.app_module );
        file_update = stub.findViewById( R.id.file_module );
        wifi_update = stub.findViewById( R.id.layout_wifi );
        data_update = stub.findViewById( R.id.layout_flow );

        app_update.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity( AppUpdateActivity.class );
            }
        } );

        file_update.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity( FileUpdateActivity.class );
            }
        } );

        app_count = (TextView) app_update.findViewById( R.id.app_update );
        file_count = (TextView) file_update.findViewById( R.id.file_update );
        wifi_count = (TextView) wifi_update.findViewById( R.id.wifi_traffic );
        data_count = (TextView) data_update.findViewById( R.id.data_traffic );

        //RecycleView
        mRecyclerView = mViewHolder.get( R.id.app_launcher );

        GridLayoutManager gridLayoutManager = new GridLayoutManager( getActivity(), 5 );
        gridLayoutManager.setOrientation( GridLayoutManager.VERTICAL );
        mRecyclerView.setLayoutManager( gridLayoutManager );

        launcherAdapter = new LauncherAdapter( getActivity() );

        viewloading.setInit( new ViewLoaddingInterface() {
            @Override
            public void Reload() {
                //点击重新加载调用
                Toast.makeText( getActivity(), TheTang.getSingleInstance().getContext().getResources().getString( R.string.device_getnetdate ), Toast.LENGTH_SHORT ).show();
            }

            @Override
            public void NoDate(int viewId) {
                if (viewId == R.id.btn_1) {
                    //按钮1
                    //getNetData(refresh,minid);

                }
            }
        }, getResources().getString(R.string.app_information), null );
        viewloading.setErrorType( ViewLoadingLayout.HIDE_LAYOUT );
        viewloading.setVisibility( View.GONE );


    }

    public class LoadAppTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            loadApps();
            getTotal();
            return "load";
        }

        @Override
        protected void onPostExecute(Object object) {

            super.onPostExecute( object );

            if (mApps != null && mApps.size() > 0) {
                viewloading.setErrorType( ViewLoadingLayout.HIDE_LAYOUT );
                viewloading.setVisibility( View.GONE );
                launcherAdapter.setData( mApps );
            } else {
                viewloading.setVisibility( View.VISIBLE );
                viewloading.setErrorType( ViewLoadingLayout.NODATA );
            }
            launcherAdapter.notifyDataSetChanged();
            setData();
        }

    }

    /**
     * 获取已安装应用信息
     */
    private void loadApps() {
        if (mApps.size() > 0) {
            mApps.clear();
        }

        if (launcherInfoList.size() > 0) {
            launcherInfoList.clear();
        }

        if (appList.size() > 0) {
            appList.clear();
        }

        launcherInfoList = TheTang.getSingleInstance().getLauncherApps();

        appList = TheTang.getSingleInstance().getInstallAppInfo();

        PackageManager packageManager = TheTang.getSingleInstance().getPackageManager();
        //加入安全界面的图标
        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

        String securityData = preferencesManager.getSecurityData(Common.safetyTosecureFlag);
        if (!TextUtils.isEmpty(securityData)) {

            if (!TextUtils.isEmpty(preferencesManager.getSecurityData(Common.secureDesktopFlag))) {
                try {

                    mApps.add(packageManager.getApplicationInfo(TheTang.getSingleInstance().getContext().getPackageName(), 0));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

        } else {
           if (!TextUtils.isEmpty(preferencesManager.getFenceData(Common.insideAndOutside)) &&
                    !TextUtils.isEmpty(preferencesManager.getFenceData(Common.setToSecureDesktop)) &&
                    !"2".equals(preferencesManager.getFenceData(Common.setToSecureDesktop)) &&
                    "true".equals(preferencesManager.getFenceData(Common.insideAndOutside)) ||
                    !TextUtils.isEmpty(preferencesManager.getSafedesktopData("code")) ){
            try {

                mApps.add(packageManager.getApplicationInfo(TheTang.getSingleInstance().getContext().getPackageName(), 0));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();

            }
        }

    }
        List<APPInfo> removedApps = new ArrayList<>(  );

        if (appList != null && appList.size() > 0) {
            for (APPInfo app : appList) {
                try {
                    String str = app.getPackageName();
                    ApplicationInfo info = packageManager.getApplicationInfo( str, 0 );
                    mApps.add( info );
                } catch (PackageManager.NameNotFoundException e) {
                    LogUtil.writeToFile( TAG, e.toString() );
                    removedApps.add( app );
                    DatabaseOperate.getSingleInstance().deleteInstallAppInfo( app.getAppId() );
                    continue;
                }

            }
        }

        if (removedApps.size() > 0) {
            for (APPInfo app : removedApps) {
                appList.remove( app );
            }
        }
    }

    public void getTotal() {

        long mobileTxBytes = TrafficStats.getMobileTxBytes();//获取手机3g/2g网络上传的总流量  ///1024
        long mobileRxBytes = TrafficStats.getMobileRxBytes();//手机2g/3g下载的总流量

        long mobile_traffics = mobileTxBytes + mobileRxBytes;

        long totalTxBytes = TrafficStats.getTotalTxBytes();//手机全部网络接口 包括wifi，3g、2g上传的总流量
        long totalRxBytes = TrafficStats.getTotalRxBytes();//手机全部网络接口 包括wifi，3g、2g下载的总流量
        long data_total = totalTxBytes + totalRxBytes;

        TrafficStats.getTotalTxBytes();//获取总共的发送（上传）的流量（包括3G，4G，WIFI）
        TrafficStats.getTotalRxBytes();//获取总共的接收（下载）的流量（包括3G，4G，WIFI）

        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

        mobile_traffics = mobile_traffics + preferencesManager.getTraffictotal( "mobileTotal" );
        data_total = data_total + preferencesManager.getTraffictotal( "trafficTotal" );

        long wifi_traffics = data_total - mobile_traffics;
        final String wifi_traffi = theTang.convertTraffic( wifi_traffics );
        final String data_traffi = theTang.convertTraffic( mobile_traffics );

        if (theTang.getUsageStats()) {

            NetworkStatsManager networkStatsManager = (NetworkStatsManager) this.theTang.getContext().getSystemService( NETWORK_STATS_SERVICE );
            final NetworkStatsHelper networkStatsHelper = new NetworkStatsHelper( networkStatsManager );

            if (getActivity() != null) {
                getActivity().runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        wifi_count.setText( theTang.convertTraffic( NetworkStatsHelper.getAllMonthWifi()/*networkStatsHelper.getAllRxBytesWifi()+networkStatsHelper.getAllTxBytesWifi()*/ ) );
                        data_count.setText( theTang.convertTraffic( NetworkStatsHelper.getAllMonthMobile( TheTang.getSingleInstance().getContext(), null ) ) );
                    }
                } );
            }
        } else {
            if (getActivity() != null) {
                getActivity().runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        wifi_count.setText( wifi_traffi );
                        data_count.setText( data_traffi );
                    }
                } );
            }
        }
    }

    /**
     * 消息更新
     */
    public class MessageTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            return getMessageNotReadNum();
        }

        @Override
        protected void onPostExecute(Object notReadNum) {

            super.onPostExecute( notReadNum );
            if ((int) notReadNum > 0) {
                remindText.setText( String.valueOf( notReadNum ) );
                remindText.setVisibility( View.VISIBLE );
            } else {
                remindText.setVisibility( View.GONE );
            }
            setData();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void notifyMessage(MessageEvent event) {
        setMessageNum();

    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister( this );
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setData() {
        setDataTraffic();
        Log.d( TAG, "setDataTraffic" );
        setStorageProgress();
        Log.d( TAG, "setStorageProgress" );
        setAppProgress();
        Log.d( TAG, "setAppProgress" );
        setFileAndAppNum();
        Log.d( TAG, "setFileAndAppNum" );
    }

    private void setDataTraffic() {
    }

    private void setStorageProgress() {
        total_size = TheTang.getSingleInstance().getTotalStorage();
        remain_size = TheTang.getSingleInstance().getRemainStorage();
        unit = TheTang.getSingleInstance().getUnit( remain_size );
        storage_progress.setMaxValue( Float.parseFloat( TheTang.getSingleInstance().formatFile( (long) total_size ) ) );
        storage_progress.setValue( Float.parseFloat( TheTang.getSingleInstance().formatFile( (long) (total_size - remain_size) ) ) );
        storage_progress.setUnit( unit );
    }

    private void setAppProgress() {

        int size = 0;

        if (appList != null) {
            size = appList.size();
        }

        int noSystemAppNum = launcherInfoList != null ? launcherInfoList.size() : 0;

        app_progress.setMaxValue( noSystemAppNum );
        app_progress.setValue( size );
    }

    /**
     * 设置文件和应用更新数量
     */
    private void setFileAndAppNum() {
        File file = new File( BaseApplication.baseFilesPath );
        if (file.isDirectory()) {
            if (list_info.size() > 0) {
                list_info.clear();
            }
            scanFile( file );
        }

        if (list_info != null && list_info.size() > 0) {
            file_count.setText( getResources().getString(R.string.one_unit, String.valueOf(list_info.size())) );
        } else {
            file_count.setText( getResources().getString(R.string.one_unit, "0" ));
        }

        app_count.setText( appList != null ? getResources().getString(R.string.one_unit, String.valueOf(appList.size()))
                : getResources().getString(R.string.one_unit, "0" ) );

    }

    private void scanFile(File file) {

        for (File subFile : file.listFiles()) {
            if (subFile.isDirectory()) {
                scanFile( subFile );
            } else {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFileName( subFile.getName() );
                list_info.add( fileInfo );
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu( menu, inflater );
        inflater.inflate( R.menu.workbench_menu, menu );
        MenuItem item = menu.getItem( 0 );
        View view = LayoutInflater.from( getActivity() ).inflate( R.layout.layout_work_remind, null );
        item.setActionView( view );

        remindText = (TextView) view.findViewById( R.id.notification_num );
        setMessageNum();
        view.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity( MessageActivity.class );
            }
        } );
    }

    /**
     * 设置未读消息
     */
    private void setMessageNum() {
        MessageTask messageTask = new MessageTask();
        messageTask.execute( "message" );
    }

    /**
     * @return 获取未读消息个数
     */
    private int getMessageNotReadNum() {

        int notReadNum = 0;
        messageList = DatabaseOperate.getSingleInstance().queryAllMessageInfo();
        for (MessageInfo message : messageList) {
            if ("true".equals( message.getMessage_icon() )) {
                notReadNum++;
            }
        }
        return notReadNum;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void notifyData(NotifyEvent event) {

        if (event.getMsg() != null && "flow_flag".equals( event.getMsg() )) {

            getTotal();

        } else {

            LoadAppTask loadAppTask = new LoadAppTask();
            loadAppTask.execute( "load" );

        }
    }

}

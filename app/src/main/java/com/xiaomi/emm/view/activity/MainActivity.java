package com.xiaomi.emm.view.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.xiaomi.emm.R;
import com.xiaomi.emm.base.BaseApplication;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.event.AvatarUpdateEvent;
import com.xiaomi.emm.features.excute.MDMOrderService;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.features.presenter.MDM;
import com.xiaomi.emm.features.manager.PreferencesManager;
import com.xiaomi.emm.features.presenter.TheTang;
import com.xiaomi.emm.view.fragment.AppStoreFragment;
import com.xiaomi.emm.view.fragment.MessageFragment;
import com.xiaomi.emm.view.fragment.SettingFragment;
import com.xiaomi.emm.view.fragment.StrategeFragment;
import com.xiaomi.emm.view.fragment.TelWhiteListFragment;
import com.xiaomi.emm.view.fragment.WorkBenchFragment;
import com.xiaomi.emm.view.photoview.CircleImageView;
import com.xiaomi.emm.view.photoview.PersonalInformationAcitivty;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Set;

/**
 * 主界面，登录后的界面
 * Created by Administrator on 2017/6/1.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private String TAG = "MainActivity";
    Toolbar toolbar;
    CircleImageView person_photoImageView;
    NavigationView mNavigationView;
    DrawerLayout mDrawerLayout;
    TextView tv_userName;
    PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

    FragmentManager mFragmentManager;
    AppStoreFragment mAppStoreFragment;

    MessageFragment mMessageFragment;
    SettingFragment mSettingFragment;
    StrategeFragment mStrategeFragment;
    TelWhiteListFragment mTelWhiteListFragment;
    WorkBenchFragment mWorkBenchFragment;

    Fragment fragment = new Fragment();
    int id = 0;
    String title = null;

    Bitmap bitmap = null;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {

        if (isNotificationListenerEnabled( this )) {
            openNotificationListenSettings();
        }

        firstEnter();

        mNavigationView = mViewHolder.get( R.id.navigationView );
        mDrawerLayout = mViewHolder.get( R.id.drawerLayout );

        mDrawerLayout.addDrawerListener( new MainDrawerListener() );

        toolbar = mViewHolder.get( R.id.toolbar );
        toolbar.setPadding(
                toolbar.getPaddingLeft(),
                toolbar.getPaddingTop() + TheTang.getSingleInstance().getStatusBarHeight( this ),
                toolbar.getPaddingRight(),
                toolbar.getPaddingBottom() );

        //先获得头部的View，再findViewByid();
        View headerView = mNavigationView.getHeaderView( 0 );

        person_photoImageView = (CircleImageView) headerView.findViewById( R.id.person_photo );
        tv_userName = (TextView) headerView.findViewById( R.id.tv_userName );
        toolbar.setNavigationIcon( R.mipmap.menu_white );
        toolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer( mNavigationView );
            }
        } );

        tv_userName.setText( preferencesManager.getData( Common.userName ) );
        Log.w( TAG, "  preferencesManager.setData(Common.userName=" + preferencesManager.getData( Common.userName ) );
        mNavigationView.setNavigationItemSelectedListener( this );
        ColorStateList colorStateList = this.getResources().getColorStateList( R.color.nativation_itemcolor );
        mNavigationView.setItemTextColor( colorStateList );
        person_photoImageView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity( MainActivity.this, PersonalInformationAcitivty.class );
                mDrawerLayout.closeDrawer( mNavigationView );
            }
        } );
        mFragmentManager = this.getSupportFragmentManager();
        mAppStoreFragment = new AppStoreFragment();
        //mLightAppFragment = new LightAppFragment();
        mMessageFragment = new MessageFragment();
        mSettingFragment = new SettingFragment();
        mStrategeFragment = new StrategeFragment();
        mTelWhiteListFragment = new TelWhiteListFragment();
        mWorkBenchFragment = new WorkBenchFragment();
        initReplaceFragment( 0, getResources().getString( R.string.work_bench ), mWorkBenchFragment );
        LogUtil.writeToFile( TAG, "MDMOrderService init end!" );

        ////////////////////用于高德定位权限的弹出
        popUpGaodeLocation();

    }

    /**
     * 判断是否为启动
     */
    private void firstEnter() {
        LogUtil.writeToFile( TAG, "firstEnter!" );
        String first = getIntent().getStringExtra( "first" );

        if (first != null && "true".equals( first )) {

            //初始化FeedBackImpl
            //TheTang.getSingleInstance().initImplTwo();
            //LogUtil.writeToFile( TAG, "WatchingOrderService!" );
            TheTang.getSingleInstance().startService( new Intent( this, MDMOrderService.class ) );
            //startService( new Intent( this, WatchingOrderService.class ) );
        }
    }

    /**
     * 高德定位权限框的弹出
     */
    private void popUpGaodeLocation() {
        TheTang.getSingleInstance().getThreadPoolObject().submit( new Runnable() {
            @Override
            public void run() {

                MDM.closeForceLocation();
                try {
                    Thread.sleep( 2000 );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                MDM.forceLocationService();

                try {
                    Thread.sleep( 2000 );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MDM.closeForceLocation();

                PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();
                if (!TextUtils.isEmpty( preferencesManager.getFenceData( Common.geographical_fence ) )) {
                    MDM.forceLocationService();//用于强制定位服务
                } else {
                    if ("0".equals( preferencesManager.getPolicyData( Common.default_allowLocation ) )) {
                        MDM.enableLocationService( false );//用于返回禁止状态
                    }
                }
            }
        } );
    }

    /**
     * 初始化Fragment
     *
     * @param id
     * @param title
     * @param fragment
     */
    private void initReplaceFragment(int id, String title, Fragment fragment) {
        toolbar.setTitle( title );

        mNavigationView.getMenu().getItem( checkedNUm ).setChecked( false );
        checkedNUm = id;
        mNavigationView.getMenu().getItem( id ).setChecked( true );

        FragmentTransaction transaction = mFragmentManager.beginTransaction();

        transaction.replace( R.id.frame, fragment );
        transaction.setTransitionStyle( FragmentTransaction.TRANSIT_FRAGMENT_FADE );
        transaction.commit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        /*    case R.id.person_photo:

            break;*/
            default:
                break;
        }
    }

    int checkedNUm = 0;

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.work_bench:
                id = 0;
                fragment = mWorkBenchFragment;
                title = getResources().getString( R.string.work_bench );
                break;
            case R.id.app_store:
                id = 1;
                fragment = mAppStoreFragment;
                title = getResources().getString( R.string.app_store );
                break;
            /*case R.id.light_app:
                replaceFragment(2,getResources().getString(R.string.light_app),mLightAppFragment);
                break;*/
            case R.id.message:
                id = 3;
                fragment = mMessageFragment;
                title = getResources().getString( R.string.message );
                break;
            case R.id.strategy:
                id = 2;
                fragment = mStrategeFragment;
                title = getResources().getString( R.string.stratege );
                break;
            case R.id.setting:
                id = 5;
                fragment = mSettingFragment;
                title = getResources().getString( R.string.setting );
                break;
            case R.id.tel_whitelist:
                //Added by duanxin for bug253 on 2017/10/09
                id = 4;
                fragment = mTelWhiteListFragment;
                title = getResources().getString( R.string.tel_white );
                break;
        }
        replaceFragment( );
        return false;
    }

    private void replaceFragment() {
        mDrawerLayout.closeDrawer( mNavigationView );
    }

    class MainDrawerListener implements DrawerLayout.DrawerListener {
        boolean isOpened = false;

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            if (isOpened && slideOffset < 0.1 && checkedNUm != id) {
                toolbar.setTitle( title );
                mNavigationView.getMenu().getItem( checkedNUm ).setChecked( false );
                checkedNUm = id;
                mNavigationView.getMenu().getItem( id ).setChecked( true );

                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                transaction.replace( R.id.frame, fragment );
                transaction.commit();
                isOpened = false;
            }
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            isOpened = true;
        }

        @Override
        public void onDrawerClosed(View drawerView) {
        }

        @Override
        public void onDrawerStateChanged(int newState) {
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        setAvater();
    }

    /**
     * 设置用户头像
     */
    private void setAvater() {


        File file = new File( BaseApplication.baseImagesPath + File.separator + "personPic.png" );
        if (file.exists() && file.length() >0) {
            //bitmap回收，防止OOM
            if(bitmap != null && !bitmap.isRecycled()){
                bitmap.recycle();
                bitmap = null;
            }

            bitmap = BitmapFactory.decodeFile( file.getPath() );
            if (bitmap != null) {
                person_photoImageView.setImageBitmap( bitmap );
            } else {
                person_photoImageView.setImageResource( R.drawable.avatar );
            }

        } else {
            person_photoImageView.setImageResource( R.drawable.avatar );
        }
    }

    /**
     * EventBus回调
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateAvater(AvatarUpdateEvent event) {
        setAvater();
    }

    public boolean isNotificationListenerEnabled(Context context) {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages( this );
        if (packageNames.contains( context.getPackageName() )) {
            return false;
        }
        return true;
    }

    public void openNotificationListenSettings() {
        try {
            Intent intent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                intent = new Intent( Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS );
            }
            startActivity( intent );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

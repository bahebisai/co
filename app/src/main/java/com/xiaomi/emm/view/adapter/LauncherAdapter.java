package com.xiaomi.emm.view.adapter;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiaomi.emm.R;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.event.NotifyEvent;
import com.xiaomi.emm.features.lockscreen.NewsLifecycleHandler;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;
import com.xiaomi.emm.view.activity.AppUpdateActivity;
import com.xiaomi.emm.view.activity.SafeDeskActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Created by Administrator on 2017/7/5.
 */

public class LauncherAdapter extends RecyclerView.Adapter<LauncherAdapter.LauncherViewHolder> {
    private final static String TAG = "LauncherAdapter";
    private Context mContext;
    private List<ApplicationInfo> mList = new ArrayList<>();

    public LauncherAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setData(List<ApplicationInfo> list) {
        if (mList != null && mList.size() > 0) {
            mList.clear();
        }
        //modify by duanxin for bug183 on 2017.09.22
        mList.addAll( list );
    }

    @Override
    public LauncherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LauncherViewHolder viewHolder = new LauncherViewHolder( LayoutInflater .from( mContext ).inflate( R.layout.layout_launcher, parent, false ) );
        Log.d( TAG, "onCreateViewHolder" );
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(LauncherViewHolder holder, final int position) {

        if (position == 9) {
            holder.imageView.setImageResource( R.mipmap.more1 );
            holder.textView.setText( mContext.getResources().getString(R.string.more_app) );
        } else {
            if (mList.get( position ).packageName.equals( mContext.getPackageName() )) {

                holder.imageView.setImageResource( R.mipmap.mi8sesplit8split1);
                holder.textView.setText( mContext.getResources().getString(R.string.security_desk ) );
            } else {
                String label = TheTang.getSingleInstance().getAppLabel( mList.get( position ).packageName );

                if (TextUtils.isEmpty( label )) {
                    EventBus.getDefault().post( new NotifyEvent() );
                }
                if (TheTang.getSingleInstance().getAppIcon( mList.get( position ) ) != null) {

                    holder.imageView.setImageDrawable( TheTang.getSingleInstance().getAppIcon( mList.get( position ) ) );
                    holder.textView.setText( label );
                }
            }
        }

        holder.ll_app.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == 9) {
                    mContext.startActivity( new Intent( mContext, AppUpdateActivity.class ) );
                    return;
                }

                ApplicationInfo info = mList.get( position );
                Log.w( TAG, info.packageName + "" );
                if (info.packageName.equals( mContext.getPackageName() )) {

                    Intent intents = new Intent( mContext, SafeDeskActivity.class );
                    mContext.startActivity( intents );

                } else {
                    Intent intent = TheTang.getSingleInstance().getPackageManager().getLaunchIntentForPackage( info.packageName );
                    if (intent != null) {
                        mContext.startActivity( intent );

                       NewsLifecycleHandler.LockFlag = true;
                        PreferencesManager preferencesManager = PreferencesManager.getSingleInstance();

                        //判断是否在安全局域里面
                        if (!TextUtils.isEmpty( preferencesManager.getSecurityData( Common.safetyTosecureFlag)) &&
                                !TextUtils.isEmpty(preferencesManager.getSecurityData( Common.secureDesktopFlag )) ){

                               EventBus.getDefault().post( new NotifyEvent( "start" ) );

                        }else {
                            if ( TextUtils.isEmpty(preferencesManager.getFenceData( Common.setToSecureDesktop))  ||
                                    "2".equals( preferencesManager.getFenceData( Common.setToSecureDesktop) ) ||
                                    TextUtils.isEmpty(preferencesManager.getFenceData( Common.insideAndOutside)) ||
                                    "false".equals(preferencesManager.getFenceData( Common.insideAndOutside))  ){


                                if ( ! TextUtils.isEmpty(preferencesManager.getSafedesktopData("code"))) {

                                    EventBus.getDefault().post( new NotifyEvent( "start" ) );
                                }
                            }else   if (
                                    preferencesManager.getFenceData( Common.insideAndOutside) != null &&
                                            "true".equals(preferencesManager.getFenceData(Common.insideAndOutside))){

                                if ( !TextUtils.isEmpty(preferencesManager.getFenceData(Common.setToSecureDesktop))  &&
                                        !"2".equals(preferencesManager.getFenceData(Common.setToSecureDesktop)) &&
                                        "1".equals(preferencesManager.getFenceData(Common.setToSecureDesktop)) ){

                                    EventBus.getDefault().post( new NotifyEvent( "start" ) );
                                }


                            }
                        }

                    }

                }

            }
        } );
    }

    @Override
    public int getItemCount() {
        if (mList != null) {
            if (mList.size() < 10) {

                return mList.size();
            } else {
                return 10;
            }
        }
        return 0;
    }

    class LauncherViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textView;
        LinearLayout ll_app;

        public LauncherViewHolder(View view) {
            super( view );
            imageView = (ImageView) view.findViewById( R.id.launcher_image );
            textView = (TextView) view.findViewById( R.id.launcher_text );
            ll_app = (LinearLayout) view.findViewById( R.id.ll_app );
        }
    }
}

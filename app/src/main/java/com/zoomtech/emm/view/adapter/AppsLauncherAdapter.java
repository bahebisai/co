package com.zoomtech.emm.view.adapter;

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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zoomtech.emm.R;
import com.zoomtech.emm.features.event.NotifyEvent;
import com.zoomtech.emm.features.lockscreen.NewsLifecycleHandler;
import com.zoomtech.emm.features.policy.device.ShortcutActivity;
import com.zoomtech.emm.model.ConfigureStrategyData;
import com.zoomtech.emm.utils.AppUtils;
import com.zoomtech.emm.features.manager.PreferencesManager;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.view.activity.MainActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by lenovo on 2017/9/13.
 */

public class AppsLauncherAdapter extends RecyclerView.Adapter<AppsLauncherAdapter.ViewHolder> {
    private final static String TAG = "AppsLauncherAdapter";
    private Context mContext;
    private List<ApplicationInfo> mApps = new ArrayList<>();
  //  Bitmap bitmap = null;

    public AppsLauncherAdapter(Context context) {
        mContext = context;
    }

    public void setData(Collection<ApplicationInfo> apps) {
        if (mApps != null && mApps.size() > 0) {
            mApps.clear();
        }
        mApps.addAll( apps );

        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ViewHolder viewHolder = new ViewHolder( LayoutInflater.from( mContext ).inflate( R.layout.layout_launcher, parent, false ) );
        Log.d( TAG, "onCreateViewHolder" );
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        //bitmap回收，防止OOM
       /* if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }*/

        if (mApps.get( position ) != null && !TextUtils.isEmpty( mApps.get( position ).packageName ) && mApps.get( position ).packageName.contains( "com.huawei.fido.uafclient-" )) {
            String picName = mApps.get( position ).packageName.split( "-" )[1];

            String IN_PATH = "/MDM/Files/images/";

            String savePath = TheTang.getSingleInstance().getContext().getApplicationContext().getFilesDir().getAbsolutePath() + IN_PATH;

            File file = new File( savePath + picName );

            if (file.exists() && file.length()>0) {
             //   bitmap = BitmapFactory.decodeFile( savePath + picName );//"pic" + ".jpg"
                Glide.with(mContext).load(file).override(100,100)
                        .skipMemoryCache(true) // 不使用内存缓存
                        .diskCacheStrategy(DiskCacheStrategy.NONE)  // 不使用磁盘缓存
                        .into( holder.img);
                Log.w(TAG,"file---bitmap"+file.getAbsolutePath().toString());
            } else {
              //  bitmap = BitmapFactory.decodeResource( TheTang.getSingleInstance().getContext().getResources(), R.mipmap.ic_launcher );
                holder.img.setImageResource( R.mipmap.ic_launcher );
            }

         //   holder.img.setImageBitmap( bitmap );
            holder.title.setText( picName );

        } else if (mApps.get( position ) != null && !TextUtils.isEmpty( mApps.get( position ).packageName ) && mApps.get( position ).packageName.contains( "com.huawei.fido.uafclient" )) {
            String IN_PATH = "/MDM/Files/images/";
            String savePath = TheTang.getSingleInstance().getContext().getApplicationContext().getFilesDir().getAbsolutePath() + IN_PATH;

            String webclipConfig = PreferencesManager.getSingleInstance().getConfiguration( "WebclipConfig" );
            if (!TextUtils.isEmpty( webclipConfig )) {
                File file = new File( savePath + mApps.get( position ).name );

                Type type = new TypeToken<ArrayList<ConfigureStrategyData.ConfigureStrategyBean.WebclipListBean>>() {
                }.getType();
                ArrayList<ConfigureStrategyData.ConfigureStrategyBean.WebclipListBean> webclipListBeen = new Gson().fromJson( webclipConfig, type );
                String i = String.valueOf( mApps.get( position ).packageName.charAt( mApps.get( position ).packageName.length() - 1 ) );
                String webClipImgPath = webclipListBeen.get( Integer.parseInt( i ) ).getWebClipImgPath();

                String picName = webClipImgPath.split( "\\\\" )[webClipImgPath.split( "\\\\" ).length - 1];
               // bitmap = BitmapFactory.decodeFile( savePath + picName );//"pic" + ".jpg"
               // holder.img.setImageBitmap( bitmap );
                Glide.with(mContext).load(new File(savePath + picName)).override(100,100)
                        .skipMemoryCache(true) // 不使用内存缓存
                        .diskCacheStrategy(DiskCacheStrategy.NONE)  // 不使用磁盘缓存
                        .into( holder.img);
                String webClipName = webclipListBeen.get( Integer.parseInt( i ) ).getWebClipName();
                holder.title.setText( webClipName );//PreferencesManager.getSingleInstance().getConfiguration("webClipName")
            }
        } else {
            if (AppUtils.getAppIcon(mContext, mApps.get(position)) != null) {
                holder.img.setImageDrawable(AppUtils.getAppIcon(mContext, mApps.get(position)));
                holder.title.setText(AppUtils.getAppLabel(mContext, mApps.get(position).packageName));
            }
        }

        holder.img.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplicationInfo info = mApps.get( position );
                Log.w( TAG, info.packageName + "" );
                if (mApps.get( position ) != null && !TextUtils.isEmpty( mApps.get( position ).packageName ) && mApps.get( position ).packageName.contains( "com.huawei.fido.uafclient-" ) || mApps.get( position ).packageName.contains( "com.huawei.fido.safe-" )) {

                    NewsLifecycleHandler.LockFlag = true;
                    Intent intent = new Intent( mContext, ShortcutActivity.class );
                    intent.putExtra( "url", info.name );
                    PreferencesManager.getSingleInstance().setLockFlag( "intentMainAciticity", true );
                    mContext.startActivity( intent );
                } else if (mApps.get( position ) != null && !TextUtils.isEmpty( mApps.get( position ).packageName ) && "com.android.phone".equals( info.packageName )) {

                    NewsLifecycleHandler.LockFlag = true;
                    Intent intents = new Intent( Intent.ACTION_DIAL );

                    intents.setClassName( "com.android.contacts", "com.android.contacts.activities.TwelveKeyDialer" );//todo baii 硬编码且会随项目改变
                    mContext.startActivity( intents );

                    EventBus.getDefault().post( new NotifyEvent( "start" ) );
                } else if (mApps.get( position ) != null && !TextUtils.isEmpty( mApps.get( position ).packageName ) && info.packageName.equals( mContext.getPackageName() )) {
                    Intent intents = new Intent( mContext, MainActivity.class );
                    mContext.startActivity( intents );

                } else {
                    Intent intent = mContext.getPackageManager().getLaunchIntentForPackage( info.packageName );
                    if (intent != null) {
                        NewsLifecycleHandler.LockFlag = true;
                        mContext.startActivity( intent );
                    }
                    EventBus.getDefault().post( new NotifyEvent( "start" ) );
                }

            }
        } );


    }


    @Override
    public int getItemCount() {
        if (mApps != null) {
            return mApps.size();
        }
        return 0;
    }

    //在外面先定义，ViewHolder静态类
    static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;
        public TextView title;

        public ViewHolder(View view) {
            super( view );
            img = (ImageView) view.findViewById( R.id.launcher_image );
            title = (TextView) view.findViewById( R.id.launcher_text );
        }

    }


}

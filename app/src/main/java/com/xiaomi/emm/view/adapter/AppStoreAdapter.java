package com.xiaomi.emm.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiaomi.emm.R;
import com.xiaomi.emm.features.event.NotifyEvent;
import com.xiaomi.emm.features.lockscreen.NewsLifecycleHandler;
import com.xiaomi.emm.model.APPInfo;
import com.xiaomi.emm.utils.AppUtils;
import com.xiaomi.emm.utils.TheTang;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/3.
 */

public class AppStoreAdapter extends BaseAdapter {

    private List<APPInfo> mList = new ArrayList<>(  );
    private Context mContext;

    public AppStoreAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<APPInfo> list) {
        if (mList != null && mList.size() > 0) {
            mList.clear();
        }
        mList.addAll( list );
    }

    @Override
    public int getCount() {
        if (mList != null && mList.size() > 0) {
            return mList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mList != null && mList.size() > 0) {
            return mList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_appstore, null);
            mViewHolder = new ViewHolder();
            mViewHolder.app_icon = (ImageView) convertView.findViewById(R.id.app_icon);
            mViewHolder.app_name = (TextView) convertView.findViewById(R.id.app_name);
            mViewHolder.app_info = (TextView) convertView.findViewById(R.id.app_info);
            mViewHolder.app_size = (TextView) convertView.findViewById(R.id.app_size);
            mViewHolder.open_btn = (Button) convertView.findViewById(R.id.btn_open);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        final String packageName = mList.get(position).getPackageName();

        String label = TheTang.getSingleInstance().getAppLabel(packageName);

        if ( TextUtils.isEmpty( label ) ) {
            EventBus.getDefault().post( new NotifyEvent() );
        }

        Drawable drawable = TheTang.getSingleInstance().getAppIcon(packageName);

        if (drawable != null) {
            mViewHolder.app_icon.setImageDrawable(drawable);
        }

        mViewHolder.app_name.setText(label);
        mViewHolder.app_info.setText("v" + mList.get(position).getVersion());
        mViewHolder.app_size.setText( mList.get(position).getSize() );
        mViewHolder.open_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NewsLifecycleHandler.LockFlag = true;
                        Intent intent = AppUtils.getPackageManager(mContext).getLaunchIntentForPackage(packageName);
                        mContext.startActivity(intent);
            }
        });
        return convertView;
    }

    public static class ViewHolder {
        public ImageView app_icon;
        public TextView app_name;
        public TextView app_info;
        public TextView app_size;
        public Button open_btn;
    }

}
package com.xiaomi.emm.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xiaomi.emm.R;
import com.xiaomi.emm.model.MessageInfo;
import com.xiaomi.emm.features.presenter.TheTang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/7/13.
 */

public class MessageAdapter extends BaseAdapter {

    private final static String TAG = "MessageAdapter";
    private List<MessageInfo> mList  = new ArrayList<>(  );
    private Context mContext;

    public MessageAdapter(Context context) {
        mContext = context;
    }

    HashMap<Integer, View>  lmap = new HashMap<Integer, View>();

    public void setData(List<MessageInfo> list) {
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
        View view;
        ViewHolder mViewHolder;
        if (lmap.get(position) == null) {
            view = LayoutInflater.from(mContext).inflate( R.layout.layout_message, null);
            mViewHolder = new ViewHolder();
            mViewHolder.message_icon = (TextView) view.findViewById(R.id.message_icon);
            mViewHolder.message_from = (TextView) view.findViewById(R.id.message_from);
            mViewHolder.message_time = (TextView) view.findViewById(R.id.message_time);
            mViewHolder.message_about = (TextView) view.findViewById(R.id.message_about);
            view.setTag(mViewHolder);
        } else {
            view = lmap.get(position);
            mViewHolder = (ViewHolder) view.getTag();
        }

        if ("true".equals(mList.get(position).getMessage_icon())) {
            mViewHolder.message_icon.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.message_icon.setVisibility(View.INVISIBLE);
        }

        String message = TheTang.getSingleInstance().getMeaasgeInfo(mList.get(position).getMessage_id());

        mViewHolder.message_from.setText(message);
        mViewHolder.message_time.setText(TheTang.getSingleInstance().formatTime(Long.parseLong(mList.get(position).getMessage_time())));
        mViewHolder.message_about.setText(mList.get(position).getMessage_about());
        return view;
    }

    public static class ViewHolder {
        public TextView message_icon;
        public TextView message_from;
        public TextView message_time;
        public TextView message_about;
    }
}

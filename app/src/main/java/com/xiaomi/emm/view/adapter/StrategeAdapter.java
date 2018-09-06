package com.xiaomi.emm.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xiaomi.emm.R;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.model.StrategeInfo;
import com.xiaomi.emm.utils.TheTang;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/16.
 */
public class StrategeAdapter  extends BaseAdapter {

    private final static String TAG = "StrategeAdapter";
    private List<StrategeInfo> mList  = new ArrayList<>(  );
    private Context mContext;

    public StrategeAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<StrategeInfo> list) {
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
            convertView = LayoutInflater.from(mContext).inflate( R.layout.layout_stratege, null);
            mViewHolder = new ViewHolder();
            mViewHolder.stratege_type = (TextView) convertView.findViewById(R.id.stratege_type);
            mViewHolder.stratege_name = (TextView) convertView.findViewById(R.id.stratege_name);
            mViewHolder.stratege_time = (TextView) convertView.findViewById(R.id.stratege_time);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.stratege_type.setText(getStrategeInfo(mList.get(position).strategeId));
        mViewHolder.stratege_name.setText(mList.get(position).strategeName);
        mViewHolder.stratege_time.setText( TheTang.getSingleInstance().formatTime(Long.parseLong(mList.get(position).strategeTime)));

        return convertView;
    }

    public static class ViewHolder {
        public TextView stratege_type;
        public TextView stratege_name;
        public TextView stratege_time;
    }

    /**
     * 获得id相对应命令
     *
     * @param stratege_id
     * @return
     */
    private String getStrategeInfo(String stratege_id) {
        String stratege = null;
        if (stratege_id != null) {
            for (int i = 0; i < Common.stratege_info.length; i++) {
                if (stratege_id.equals(String.valueOf(Common.stratege_info[i][0]))) {
                    stratege = mContext.getResources().getString((int)Common.stratege_info[i][1]);
                    break;
                }
            }
        }
        return stratege;
    }
}

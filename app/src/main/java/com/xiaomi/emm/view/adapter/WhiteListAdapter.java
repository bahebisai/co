package com.xiaomi.emm.view.adapter;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.xiaomi.emm.R;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.view.listener.CheckBoxListener;
import com.xiaomi.emm.model.TelephoyWhiteUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/6/29.
 */

public class WhiteListAdapter extends BaseAdapter {
    CheckBoxListener mCheckBoxListener;
    private List<TelephoyWhiteUser> mList  = new ArrayList<>(  );
    private Context mContext;
    //modify by duanxin for bug69 on 2017/09/04
    HashMap<Integer,View>  lmap = new HashMap<Integer,View>();

    public WhiteListAdapter(Context context,CheckBoxListener mCheckBoxListener) {
        this.mCheckBoxListener = mCheckBoxListener;
        mContext = context;
    }

    public void setData(List<TelephoyWhiteUser> list) {

        if (mList != null && mList.size() > 0) {
            mList.clear();
        }

        mList.addAll( list );

        if (lmap != null && lmap.size() > 0) {
            lmap.clear();
        }

        LogUtil.writeToFile( "WhiteListAdapter", mList.size() + "" );
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
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_whitelist, null);
            mViewHolder = new ViewHolder();
            mViewHolder.tel_name = (TextView) view.findViewById(R.id.tel_name);
            mViewHolder.tel_address = (TextView) view.findViewById(R.id.tel_address);
            mViewHolder.tel_num = (TextView) view.findViewById(R.id.tel_num);
            mViewHolder.tel_shortnum = (TextView) view.findViewById(R.id.tel_shortnum);

            lmap.put(position,view);
            view.setTag(mViewHolder);
            //将UserID设为view的ID，方便list item数据的获取
            view.setId( Integer.valueOf( mList.get(position).getUserId() ) );
        } else {
            view = (View)lmap.get(position);
            mViewHolder = (ViewHolder) view.getTag();
        }

        if (! TextUtils.isEmpty( mList.get(position).getUserName() )){

        mViewHolder.tel_name.setText(mList.get(position).getUserName());
        }
        if (! TextUtils.isEmpty( mList.get(position).getUserAddress() )){



            mViewHolder.tel_address.setText(mList.get(position).getUserAddress());
        }else {
            mViewHolder.tel_address.setText("");
        }
        if (! TextUtils.isEmpty( mList.get(position).getTelephonyNumber() )){

        mViewHolder.tel_num.setText(mList.get(position).getTelephonyNumber());
        }
        if (! TextUtils.isEmpty( mList.get(position).getShortPhoneNum() )){
            mViewHolder.tel_shortnum.setVisibility(View.VISIBLE);
            mViewHolder.tel_shortnum.setText(mList.get(position).getShortPhoneNum());
        }else {
            mViewHolder.tel_shortnum.setText("");
            mViewHolder.tel_shortnum.setVisibility(View.GONE);

        }
        // Log.w("WhiteListAdapter--getShortPhoneNum==","PhoneNum ="+mList.get(position).getShortPhoneNum() );
        return view;
    }

    public static class ViewHolder {
        public TextView tel_name;
        public TextView tel_address;
        public TextView tel_num;
        public TextView tel_shortnum;
       // public CheckBox tel_check;
    }
}
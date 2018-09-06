package com.xiaomi.emm.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaomi.emm.R;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.model.FileInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/5.
 */

public class FileAdapter extends BaseAdapter {

    List<FileInfo> mList  = new ArrayList<>(  );
    Context mContext;

    public FileAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<FileInfo> list) {
        if (mList != null && mList.size() > 0) {
            mList.clear();
        }
        mList.addAll( list );
    }

    @Override
    public int getCount() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mList != null) {
            return mList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_file, null);
            mHolder = new ViewHolder();
            mHolder.file_icon = (ImageView) convertView.findViewById(R.id.file_icon);
            mHolder.file_name = (TextView) convertView.findViewById(R.id.file_name);
            mHolder.file_size = (TextView) convertView.findViewById(R.id.file_size);
            mHolder.file_time = (TextView) convertView.findViewById(R.id.file_time);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder)convertView.getTag();
        }

        mHolder.file_icon.setImageDrawable(mContext.getDrawable(getFileIcon(mList.get(position).getFileName())));
        mHolder.file_name.setText(mList.get(position).getFileName());
        mHolder.file_size.setText(mList.get(position).getFileSize());
        mHolder.file_time.setText(mList.get(position).getFileTime());

        return convertView;
    }

    public class ViewHolder {
        public ImageView file_icon;
        public TextView file_name;
        public TextView file_size;
        public TextView file_time;
    }

    /**
     * 获得文件相应类型图标
     * @param fileName
     * @return
     */
    public int getFileIcon(String fileName) {
        int id = R.mipmap.unkown;

        for (int i = 0;i < Common.FileIcon_Table.length;i++) {
            if (fileName.endsWith((String) Common.FileIcon_Table[i][0])) {
                id = (int) Common.FileIcon_Table[i][1];
                break;
            }
        }
      return id;
    }
}

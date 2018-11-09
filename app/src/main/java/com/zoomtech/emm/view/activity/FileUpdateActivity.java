package com.zoomtech.emm.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;

import com.zoomtech.emm.R;
import com.zoomtech.emm.features.lockscreen.NewsLifecycleHandler;
import com.zoomtech.emm.utils.ConvertUtils;
import com.zoomtech.emm.view.adapter.FileAdapter;
import com.zoomtech.emm.base.BaseApplication;
import com.zoomtech.emm.features.event.NotifyEvent;
import com.zoomtech.emm.model.FileInfo;
import com.zoomtech.emm.utils.LogUtil;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.view.viewutils.ViewLoadingInterface;
import com.zoomtech.emm.view.viewutils.ViewLoadingLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



/**
 * Created by Administrator on 2017/7/5.
 */

public class FileUpdateActivity extends BaseActivity {

    public final static String TAG = "FileUpdateActivity";
    Toolbar toolbar;
    List<FileInfo> list_info;
    ListView listView;
    FileAdapter mFileAdapter;
    private ViewLoadingLayout viewloading;
    Handler handler = new Handler(  ) {

        @Override
        public void handleMessage(Message msg) {
            if (list_info!=null && list_info.size()>0){

                viewloading.setErrorType(ViewLoadingLayout.HIDE_LAYOUT);
                mFileAdapter.setData( list_info );
            }else {
                viewloading.setErrorType(ViewLoadingLayout.NODATA);
            }
            mFileAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_file;
    }

    @Override
    protected void initData() {
        list_info = new ArrayList<>();
        readList();
    }

    private void readList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File( BaseApplication.baseFilesPath );
                if (file.isDirectory()) {
                    if (list_info.size() > 0) {
                        list_info.clear();
                    }
                    scanFile( file );
                }
                /*解决降序排列问题 added by duanxin for Bug62 on 2017/08/31*/
                Collections.reverse(list_info);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendMessage( new Message() );
            }
        }).start();
    }

    /**
     * 扫描文件
     * @param file
     */
    private void scanFile(File file) {

        for (File subFile : file.listFiles()) {
            if (subFile.isDirectory()) {
                scanFile( subFile );
            } else {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFileName( subFile.getName() );
                fileInfo.setFileSize( getFileSize(subFile ) );
                fileInfo.setFileTime( TheTang.getSingleInstance().formatTime(subFile.lastModified()));
                fileInfo.setFilePath(subFile.getPath());
                list_info.add( fileInfo );
            }
        }
    }

    /**
     * 获取文件大小
     * @param file
     * @return
     */
    public String getFileSize(File file)  {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                size = fis.available();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ConvertUtils.formatFileSize(size);
    }

    @Override
    protected void initView() {

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this); //EventBus注册
        }

        toolbar = mViewHolder.get( R.id.toolbar );

        toolbar.setPadding(
                toolbar.getPaddingLeft(),
                toolbar.getPaddingTop() + TheTang.getSingleInstance().getStatusBarHeight( this ),
                toolbar.getPaddingRight(),
                toolbar.getPaddingBottom() );

        listView = mViewHolder.get( R.id.activity_file );
        listView.addFooterView(new ViewStub(this));
        mFileAdapter = new FileAdapter( this );
        listView.setAdapter( mFileAdapter );

        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //调用第三方软件打开文件
                try {
                    Uri uri = null;
                    FileAdapter.ViewHolder viewHolder = (FileAdapter.ViewHolder) view.getTag();
                    File file = new File(list_info.get(position).getFilePath());
                    if (Build.VERSION.SDK_INT >= 24) {
                        uri = FileProvider.getUriForFile(getApplicationContext(), "com.xiaomi.emm.fileprovider",file);
                    } else {
                        uri = Uri.fromFile(file);
                    }

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.setDataAndType(uri, "application/pdf");
                    intent.setDataAndType(uri, "*/*");//所有类型
                    startActivityForResult(intent,999);
                } catch (Exception e) {
                    LogUtil.writeToFile(TAG,e.getCause().toString());
                    toastLong(getResources().getString(R.string.disable_open));
                }
            }
        } );

        viewloading = (ViewLoadingLayout) mViewHolder.get(R.id.viewloading);

        viewloading.setInit(new ViewLoadingInterface() {
            @Override
            public void Reload() {
                //点击重新加载调用
                //  Toast.makeText(getActivity(),TheTang.getSingleInstance().getContext().getResources().getString(R.string.device_getnetdate),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void NoDate(int viewId) {
                if (viewId == R.id.btn_1) {
                    //按钮1
                    //getNetData(refresh,minid);

                }
            }
        }, getResources().getString(R.string.none_software), null);


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @Override
    public void notifyData(NotifyEvent event) {
        readList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        if (resultCode == 999) {
            NewsLifecycleHandler.LockFlag = true;
        }
    }
}

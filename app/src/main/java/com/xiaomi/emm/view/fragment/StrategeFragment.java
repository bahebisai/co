package com.xiaomi.emm.view.fragment;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.xiaomi.emm.R;
import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.event.StrategeEvent;
import com.xiaomi.emm.model.StrategeInfo;
import com.xiaomi.emm.utils.LogUtil;
import com.xiaomi.emm.features.presenter.TheTang;
import com.xiaomi.emm.view.activity.StrategeDetailActivity;
import com.xiaomi.emm.view.adapter.StrategeAdapter;
import com.xiaomi.emm.view.viewutils.ViewLoadingInterface;
import com.xiaomi.emm.view.viewutils.ViewLoadingLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/6/27.
 */

public class StrategeFragment extends BaseFragment {
    ListView mListView = null;
    StrategeAdapter mStrategeAdapter = null;
    List<StrategeInfo> mStrategeList = new ArrayList<>();
    private ViewLoadingLayout viewloading;
    Handler handler = new Handler(  ) {

        @Override
        public void handleMessage(Message msg) {
            if (mStrategeList !=null && mStrategeList.size()>0){
                viewloading.setErrorType(ViewLoadingLayout.HIDE_LAYOUT);
                mStrategeAdapter.setData( mStrategeList );
            }else {
                viewloading.setErrorType(ViewLoadingLayout.NODATA);
            }
            mStrategeAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_stratege;
    }

    @Override
    protected void initData() {
        readStratege();
    }

    /**
     * 读取Stratege
     */
    private void readStratege() {
        new Thread( new Runnable() {
            @Override
            public void run() {
                if (mStrategeList != null && mStrategeList.size() > 0) {
                    mStrategeList.clear();
                }
                mStrategeList = DatabaseOperate.getSingleInstance().queryAllStrategeInfo();
                /*解决降序排列问题 added by duanxin for Bug62 on 2017/08/31*/
                Collections.reverse(mStrategeList);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendMessage( new Message() );
            }
        }).start();
    }

    @Override
    protected void initView(View view) {

        if (!EventBus.getDefault().isRegistered( this )) {
            EventBus.getDefault().register( this ); //EventBus注册
        }

        mListView = mViewHolder.get( R.id.stratege_list );

        mStrategeAdapter = new StrategeAdapter( getActivity() );
        mListView.setAdapter( mStrategeAdapter );
        viewloading = (ViewLoadingLayout) mViewHolder.get(R.id.viewloading);

        viewloading.setInit(new ViewLoadingInterface() {
            @Override
            public void Reload() {
                //点击重新加载调用
                Toast.makeText(getActivity(),TheTang.getSingleInstance().getContext().getResources().getString(R.string.device_getnetdate),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void NoDate(int viewId) {
                if (viewId == R.id.btn_1) {
                    //按钮1
                    //getNetData(refresh,minid);

                }
            }
        } ,getResources().getString(R.string.stratege_information),null);

        mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StrategeAdapter.ViewHolder mViewHolder =  (StrategeAdapter.ViewHolder) view.getTag();
                Intent mIntent = new Intent(getActivity(), StrategeDetailActivity.class);
                mIntent.putExtra("strategy_id", getStrategeInfo(mViewHolder.stratege_type.getText().toString()));
                mIntent.putExtra("strategy_type_name", mViewHolder.stratege_type.getText().toString());
                mIntent.putExtra("strategy_name", mViewHolder.stratege_name.getText().toString());
                startActivity(mIntent);
            }
        });
    }

    /**
     * 通过value获取id
     * @param stratege_value
     * @return
     */
    private int getStrategeInfo(String stratege_value) {
        int stratege_id = 0;
        if (stratege_value != null) {
            for (int i = 0; i < Common.stratege_info.length; i++) {
                if (stratege_value.equals(getResources().getString((int)Common.stratege_info[i][1]))) {
                    stratege_id = (int)Common.stratege_info[i][0];
                    break;
                }
            }
        }
        LogUtil.writeToFile("11111111","stratege_id" + stratege_id );
        return stratege_id;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu( menu, inflater );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected( item );
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void notifyStratege(StrategeEvent event) {
        readStratege();
        mStrategeAdapter.notifyDataSetChanged();
    }
}
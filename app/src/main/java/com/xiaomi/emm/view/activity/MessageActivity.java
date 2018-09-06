package com.xiaomi.emm.view.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;

import com.xiaomi.emm.R;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.event.MessageEvent;
import com.xiaomi.emm.model.MessageInfo;
import com.xiaomi.emm.utils.TheTang;
import com.xiaomi.emm.view.adapter.MessageAdapter;
import com.xiaomi.emm.view.viewutils.ViewLoaddingInterface;
import com.xiaomi.emm.view.viewutils.ViewLoadingLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by Administrator on 2017/8/7.
 */

public class MessageActivity extends BaseActivity implements ListView.OnItemClickListener {
    private ViewLoadingLayout viewloading;
    ListView mListView = null;
    MessageAdapter mMessageAdapter = null;
    List<MessageInfo> mMessageList = null;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if (mMessageList != null && mMessageList.size() > 0) {
                viewloading.setErrorType( ViewLoadingLayout.HIDE_LAYOUT );
                mMessageAdapter.setData( mMessageList );
            } else {
                viewloading.setErrorType( ViewLoadingLayout.NODATA );
            }

            mMessageAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_message;
    }

    @Override
    protected void initData() {
        readMessage();
    }

    private void readMessage() {
        new Thread( new Runnable() {
            @Override
            public void run() {
                mMessageList = DatabaseOperate.getSingleInstance().queryAllMessageInfo();
                /*解决降序排列问题 added by duanxin for Bug62 on 2017/08/31*/
                Collections.reverse( mMessageList );
                try {
                    Thread.sleep( 1000 );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendMessage( new Message() );
            }
        } ).start();
    }

    @Override
    protected void initView() {

        Toolbar toolbar = mViewHolder.get( R.id.toolbar );

        toolbar.setPadding(
                toolbar.getPaddingLeft(),
                toolbar.getPaddingTop() + TheTang.getSingleInstance().getStatusBarHeight( this ),
                toolbar.getPaddingRight(),
                toolbar.getPaddingBottom() );

        mListView = mViewHolder.get( R.id.message_activity );
        mListView.addFooterView( new ViewStub( this ) );
        mMessageAdapter = new MessageAdapter( this );
        mListView.setAdapter( mMessageAdapter );
        mListView.setOnItemClickListener( this );
        viewloading = (ViewLoadingLayout) mViewHolder.get( R.id.viewloading );

        viewloading.setInit( new ViewLoaddingInterface() {
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
        }, getResources().getString(R.string.none_message), null );

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.message_delete:

                break;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MessageAdapter.ViewHolder viewHolder = (MessageAdapter.ViewHolder) view.getTag();
        DatabaseOperate.getSingleInstance().updateMessageInfo( "false", mMessageList.get( position ).getMessage_time() );
        toMessageDetailActivity( viewHolder, position );
    }

    private void toMessageDetailActivity(MessageAdapter.ViewHolder viewHolder, int position) {
        Intent intent = new Intent( this, MessageDetailActivity.class );
        intent.putExtra( "from", viewHolder.message_from.getText().toString() );
        intent.putExtra( "time", viewHolder.message_time.getText().toString() );
        intent.putExtra( "about", viewHolder.message_about.getText().toString() );
        startActivity( intent );
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void notifyMessage(MessageEvent event) {
        readMessage();
    }
}

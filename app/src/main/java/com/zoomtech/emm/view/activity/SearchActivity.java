package com.zoomtech.emm.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.zoomtech.emm.R;
import com.zoomtech.emm.features.presenter.TheTang;
import com.zoomtech.emm.view.adapter.WhiteListAdapter;
import com.zoomtech.emm.model.TelephoyWhiteUser;
import com.zoomtech.emm.features.db.DatabaseOperate;
import com.zoomtech.emm.view.listener.CheckBoxListener;

import java.util.List;

/**
 * Created by Administrator on 2017/6/29.
 */

public class SearchActivity extends BaseActivity implements SearchView.OnQueryTextListener, CheckBoxListener {
    Toolbar toolbar;
    SearchView mSearchView;
    ListView listView;
    WhiteListAdapter mWhiteListAdapter;
    private List<TelephoyWhiteUser> list_name;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (list_name != null) {
                mWhiteListAdapter.setData(list_name);
            }

            mWhiteListAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_search;
    }

    @Override
    protected void initData() {

    }

    @SuppressLint("ResourceType")
    @Override
    protected void initView() {
        toolbar = mViewHolder.get( R.id.toolbar );

        toolbar.setPadding(
                toolbar.getPaddingLeft(),
                toolbar.getPaddingTop() + TheTang.getSingleInstance().getStatusBarHeight( this ),
                toolbar.getPaddingRight(),
                toolbar.getPaddingBottom() );

        toolbar.setNavigationIcon( R.mipmap.arrow_back );

        mSearchView = (SearchView) LayoutInflater.from( this ).inflate( R.layout.layout_search, null );

        mSearchView.setOnQueryTextListener( this );

        mSearchView.setIconified( false );
        int id = mSearchView.getContext().getResources().getIdentifier( "android:id/search_src_text", null, null );
        TextView textView = (TextView) mSearchView.findViewById( id );
        textView.setHintTextColor( Color.parseColor( "#ffffff" ) );
        textView.setHint( getResources().getString( R.string.search_hint ) );

        /*added by duanxin for Bug84 on 2017/08/31*/
        textView.setTextColor( Color.parseColor( "#ffffff" ) );

        //删除下划线
        int id1 = mSearchView.getContext().getResources().getIdentifier( "android:id/search_plate", null, null );
        LinearLayout layout = (LinearLayout) mSearchView.findViewById( id1 );
        layout.setBackgroundResource( Color.TRANSPARENT );

        int search_mag_icon_id = mSearchView.getContext().getResources().getIdentifier( "android:id/search_mag_icon", null, null );
        ImageView mSearchViewIcon = (ImageView) mSearchView.findViewById( search_mag_icon_id );
        mSearchViewIcon.setVisibility( View.GONE );

        mSearchView.setBackgroundColor( Color.TRANSPARENT );
        toolbar.addView( mSearchView );

        mWhiteListAdapter = new WhiteListAdapter( this, this );
        listView = mViewHolder.get( R.id.list_search );
        listView.setAdapter( mWhiteListAdapter );

        listView.setOnItemClickListener( new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WhiteListAdapter.ViewHolder viewHolder = (WhiteListAdapter.ViewHolder) view.getTag();
                if (viewHolder.tel_num.getText().toString() != null) {
                    call(viewHolder.tel_num.getText().toString());
                }
            }
        } );


        getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE );//弹出软键盘
    }

    /**
     * 拨号
     * @param phone
     */
    private void call(String phone) {
        Intent intent = new Intent( Intent.ACTION_CALL, Uri.parse( "tel:" + phone ) );
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.CALL_PHONE ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity( intent );
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {

        new Thread( new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty( newText )) {
                    if (list_name != null) {
                        list_name.clear();
                    }
                } else {
                    list_name = DatabaseOperate.getSingleInstance().searchTelephonyWhite( newText );
                }

                handler.sendMessage( new Message() );
            }
        }).start();

        return false;
    }

    @Override
    public void addChenckBoxToList(TelephoyWhiteUser telephoyWhiteUser) {

    }

    @Override
    public void removeChenckBoxToList(TelephoyWhiteUser telephoyWhiteUser) {

    }

}

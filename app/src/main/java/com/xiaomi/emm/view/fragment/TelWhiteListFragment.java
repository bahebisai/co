package com.xiaomi.emm.view.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomi.emm.R;
import com.xiaomi.emm.utils.TheTang;
import com.xiaomi.emm.view.activity.AppUpdateActivity;
import com.xiaomi.emm.view.activity.SearchActivity;
import com.xiaomi.emm.view.adapter.WhiteListAdapter;
import com.xiaomi.emm.features.event.WhiteListEvent;
import com.xiaomi.emm.view.listener.CheckBoxListener;
import com.xiaomi.emm.model.TelephoyWhiteUser;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.view.viewutils.ViewLoaddingInterface;
import com.xiaomi.emm.view.viewutils.ViewLoadingLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/27.
 */

public class TelWhiteListFragment extends BaseFragment implements CheckBoxListener {

    private static final String TAG = "TelWhiteListFragment";
    private ViewLoadingLayout viewloading;
    private WhiteListAdapter mWhiteListAdapter;
    private List<TelephoyWhiteUser> list;
    private ListView listView;
    private List<TelephoyWhiteUser> list_name; //用于添加需要删除的联系人
    private boolean isAllSelete = false;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (list != null && list.size() > 0) {
                viewloading.setErrorType(ViewLoadingLayout.HIDE_LAYOUT);
                mWhiteListAdapter.setData(list);
            } else {
                viewloading.setErrorType(ViewLoadingLayout.NODATA);
            }
            mWhiteListAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_telwhitelist;
    }

    @Override
    protected void initData() {
        list_name = new ArrayList<>();
        list = new ArrayList<TelephoyWhiteUser>();
        readList();
    }

    private void readList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (list != null && list.size() > 0) {
                    list.clear();
                }
                list = DatabaseOperate.getSingleInstance().queryTelephonyWhite();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendMessage(new Message());
            }
        }).start();
    }

    @Override
    protected void initView(View view) {

        listView = (ListView) view.findViewById(R.id.white_list);

        mWhiteListAdapter = new WhiteListAdapter(getActivity(), this);

        viewloading = (ViewLoadingLayout) mViewHolder.get(R.id.viewloading);

        viewloading.setInit(new ViewLoaddingInterface() {
            @Override
            public void Reload() {
                //点击重新加载调用
                Toast.makeText(getActivity(), TheTang.getSingleInstance().getContext().getResources().getString(R.string.device_getnetdate), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void NoDate(int viewId) {
                if (viewId == R.id.btn_1) {
                    //按钮1
                    //getNetData(refresh,minid);

                }
            }
        }, getResources().getString(R.string.telephone_information), null);


        listView.setAdapter(mWhiteListAdapter);

        listView.setOnItemClickListener(new ListView.OnItemClickListener() {

                                            @Override
                                            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                                                WhiteListAdapter.ViewHolder viewHolder = (WhiteListAdapter.ViewHolder) view.getTag();
                                                String name = viewHolder.tel_name.getText().toString();
                                                final String number = viewHolder.tel_num.getText().toString();
                                                final String tel_shortnum = viewHolder.tel_shortnum.getText().toString().trim();

                                                View view1 = LayoutInflater.from(getActivity()).inflate(R.layout.phone_dialog, null);

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                // builder.setTitle(R.string.test);
                                                builder.setView(view1);
                                                builder.create();
                                                TextView titleTv = (TextView) view1.findViewById(R.id.title);
                                                TextView short_numTv = (TextView) view1.findViewById(R.id.short_num);
                                                TextView phone_numTv = (TextView) view1.findViewById(R.id.phone_num);
                                                View v_line = (View) view1.findViewById(R.id.v_line);
                                                LinearLayout ll_Callphone = view1.findViewById(R.id.ll_Callphone);
                                                LinearLayout ll_CallShortphone = view1.findViewById(R.id.ll_CallShortphone);
                                                Button no = (Button) view1.findViewById(R.id.no);
                                                final AlertDialog dialog = builder.create();
                                                if (name != null) {
                                                    titleTv.setText(/*getResources().getString(R.string.tel_white_list) +*/ name);
                                                }
                                                if (!TextUtils.isEmpty(number)) {
                                                    phone_numTv.setText(getResources().getString(R.string.telephone_num) + number);
                                                } else {
                                                    phone_numTv.setText("");
                                                }
                                                if (!TextUtils.isEmpty(tel_shortnum)) {
                                                    ll_CallShortphone.setVisibility(View.VISIBLE);
                                                    v_line.setVisibility(View.VISIBLE);
                                                    short_numTv.setText(getResources().getString(R.string.short_num) + tel_shortnum);
                                                } else {
                                                    short_numTv.setText("");
                                                    short_numTv.setVisibility(View.GONE);
                                                    v_line.setVisibility(View.GONE);
                                                    ll_CallShortphone.setVisibility(View.GONE);
                                                }
                                                ll_Callphone.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        if (!TextUtils.isEmpty(number)) {
                                                            call(number);
                                                            dialog.dismiss();
                                                        }
                                                    }
                                                });
                                                ll_CallShortphone.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        // Log.i("测试", "对话框中的tel_shortnum被点击了" +tel_shortnum);
                                                        if (!TextUtils.isEmpty(tel_shortnum)) {
                                                            call(tel_shortnum);
                                                            dialog.dismiss();
                                                        }
                                                    }
                                                });

                                                no.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                dialog.show();
                                            }
                                        }
        );
    }

    /**
     * 拨号
     *
     * @param phone
     */
    private void call(String phone) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }

    /**
     * 使用EventBus必须重写此方法，返回true
     */
    public boolean hasEventBus() {
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.whitelist_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                startActivity(new Intent(getActivity(), SearchActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void addChenckBoxToList(TelephoyWhiteUser telephoyWhiteUser) {
        list_name.add(telephoyWhiteUser);
    }

    @Override
    public void removeChenckBoxToList(TelephoyWhiteUser telephoyWhiteUser) {
        list_name.remove(telephoyWhiteUser);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void notifyData(WhiteListEvent event) {
        readList();
    }
}

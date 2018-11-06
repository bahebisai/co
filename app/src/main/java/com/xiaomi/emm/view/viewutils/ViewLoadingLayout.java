package com.xiaomi.emm.view.viewutils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xiaomi.emm.R;

/**
 * 自定义的加载类
 *
 * @author
 */
public class ViewLoadingLayout extends LinearLayout implements View.OnClickListener {// , ISkinUIObserver {
    private static final String TAG = "ViewLoadingLayout ";
    public static final int NETWORK_ERROR = 1;// 网络错误
    public static final int NETWORK_LOADING = 2;// 正在加载
    public static final int NODATA = 3;// 什么都没有
    public static final int HIDE_LAYOUT = 4; //正常加载数据了
    public static final int NODATA_ENABLE_CLICK = 5;
    public static final int NO_LOGIN = 6;

    private ProgressBar animProgress;

    public ImageView img;
    private TextView tv;
    private LinearLayout mLayout;
    private final Context context;
    private OnClickListener listener;
    private boolean clickEnable = true;
    private int mErrorState;
    private String strNoDataContent = "";
    View view;
    private ViewLoadingInterface viewLoadingInterface;//自定义回调接口
    String nodate;                                         //没有数据显示的文字
    String[] btn_text;                                     //按钮数组
    int noDataImgId = -1;                                      //没有数据的图片数据源
    String title;                                          //没有数据的问题,替换好凄凉那个地方

    public void setViewLoadingInterface(ViewLoadingInterface viewLoadingInterface) {
        this.viewLoadingInterface = viewLoadingInterface;
    }

    /**
     * @param viewLoadingInterface 回调接口
     * @param nodate               没数据显示的文字        ,为空默认
     * @param btn_text             按钮数组,数组长度可为1,2,为空木有按钮
     */
    public void setInit(ViewLoadingInterface viewLoadingInterface, String nodate, String[] btn_text) {
        setViewLoadingInterface(viewLoadingInterface);
        this.nodate = nodate;
        this.btn_text = btn_text;
        if (isNetworkConnected(context)) {
            //有网
            this.setErrorType(NETWORK_LOADING);
        } else {
            //没网
            this.setErrorType(NETWORK_ERROR);
        }
    }

    /**
     * @param viewLoadingInterface 回调接口
     * @param nodate               没数据显示的文字        ,为空默认
     * @param btn_text             按钮数组,数组长度可为1,2,为空木有按钮
     * @param noDataImgId          好凄凉最上面的图片,传图片id   默认传-1
     * @param noDataImgId          标题.就是好凄凉那个位置的文字  默认传null
     */
    public void setInit(ViewLoadingInterface viewLoadingInterface, String nodate, String[] btn_text, int noDataImgId, String title) {
        setInit(viewLoadingInterface, nodate, btn_text);
        this.noDataImgId = noDataImgId;
        this.title = title;
    }

    public ViewLoadingLayout(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ViewLoadingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        view = View.inflate(context, R.layout.view_load_nothing, null);
        img = (ImageView) view.findViewById(R.id.img_load_lothing);
        tv = (TextView) view.findViewById(R.id.tv_load_lothing);
        mLayout = (LinearLayout) view.findViewById(R.id.noresult_layout);
        animProgress = (ProgressBar) view.findViewById(R.id.load_progressBar);
//		setBackgroundColor(-1);
        setOnClickListener(this);
        img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickEnable) {
                    // setErrorType(NETWORK_LOADING);
                    if (listener != null) {
                        listener.onClick(v);
                    }
                }
            }
        });
        addView(view);
    }

    public void dismiss() {
        mErrorState = HIDE_LAYOUT;
        setVisibility(View.GONE);
    }

    public int getErrorState() {
        return mErrorState;
    }

    public boolean isLoadError() {
        return mErrorState == NETWORK_ERROR;
    }

    public boolean isLoading() {
        return mErrorState == NETWORK_LOADING;
    }

    @Override
    public void onClick(View v) {
        if (clickEnable) {
            // setErrorType(NETWORK_LOADING);
            if (listener != null) {
                listener.onClick(v);
            }
        }
    }

    public void setErrorType(int i) {
        setVisibility(View.VISIBLE);
        switch (i) {
            case NETWORK_ERROR:
                //      Log.e( TAG, "--------NETWORK_ERROR---------------" );
                mErrorState = NETWORK_ERROR;
                setVisibility(View.VISIBLE);
                // img.setBackgroundDrawable(SkinsUtil.getDrawable(context,"pagefailed_bg"));
                if (isNetworkConnected(context)) {
                    tv.setText(context.getResources().getString(R.string.error_view_load_error_click_to_refresh));
                    img.setImageResource(R.drawable.ic_no_weibo);
                } else {
                    tv.setText(context.getResources().getString(R.string.error_view_network_error_click_to_refresh));
                    img.setImageResource(R.drawable.page_icon_network);
                }
                img.setVisibility(View.VISIBLE);
                animProgress.setVisibility(View.GONE);
                clickEnable = true;

                if (isNetworkConnected(context)) {
                    //有网,但是加载失败,可能是网络超时,数据返回错误等原因。
                    this.removeView(view);
                    view = View.inflate(context, R.layout.viewloading_network_error, null);
                    addView(view);
                } else {
                    //彻底没网,加载没有网络的布局
                    this.removeView(view);
                    view = View.inflate(context, R.layout.viewloading_network_error, null);
                    TextView tv_content = (TextView) view.findViewById(R.id.tv_content);
                    TextView tv_describe = (TextView) view.findViewById(R.id.tv_describe);
                    tv_content.setText(context.getResources().getString(R.string.view_aasv_wlanwrong));
                    tv_describe.setText(context.getResources().getString(R.string.view_aasv_checkwlan));
                    addView(view);
                }
                if (viewLoadingInterface != null) {
                    //处理回调
                    Button btn_data = (Button) view.findViewById(R.id.btn_data);
                    btn_data.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            viewLoadingInterface.Reload();
                        }
                    });
                }
                break;
            case NETWORK_LOADING:
                setVisibility(View.VISIBLE);
                mErrorState = NETWORK_LOADING;
                // animProgress.setBackgroundDrawable(SkinsUtil.getDrawable(context,"loadingpage_bg"));
                animProgress.setVisibility(View.VISIBLE);
                img.setVisibility(View.GONE);
                tv.setText(context.getResources().getString(R.string.error_view_loading));
                clickEnable = false;

                this.removeView(view);
                view = View.inflate(context, R.layout.viewloading_init, null);
                addView(view);
                break;
            case NODATA:
                //没有数据
                //   Log.e( TAG, "--------NETWORK_LOADING---------------" );
                mErrorState = NODATA;
                // img.setBackgroundDrawable(SkinsUtil.getDrawable(context,"page_icon_empty"));
                setVisibility(View.VISIBLE);
                img.setImageResource(R.drawable.ic_no_weibo);
                img.setVisibility(View.VISIBLE);
                animProgress.setVisibility(View.GONE);
                setTvNoDataContent();
                clickEnable = true;

                this.removeView(view);
                view = View.inflate(context, R.layout.viewloading_nodata, null);
                TextView tv_describe = (TextView) view.findViewById(R.id.tv_describe);
                LinearLayout ll_btn = (LinearLayout) view.findViewById(R.id.ll_btn);
                Button btn1 = (Button) view.findViewById(R.id.btn_1);
                Button btn2 = (Button) view.findViewById(R.id.btn_2);
                if (nodate != null) {
                    tv_describe.setText(nodate);
                }
                if (btn_text == null) {
                    ll_btn.setVisibility(GONE);
                } else if (btn_text.length == 1) {
                    btn1.setVisibility(VISIBLE);
                    btn1.setText(btn_text[0]);
                    btn2.setVisibility(GONE);
                } else if (btn_text.length == 2) {
                    btn1.setVisibility(VISIBLE);
                    btn1.setText(btn_text[0]);
                    btn2.setVisibility(VISIBLE);
                    btn2.setText(btn_text[1]);
                }

                ImageView iv_pic = (ImageView) view.findViewById(R.id.iv_pic);
                TextView tv_content = (TextView) view.findViewById(R.id.tv_content);
                if (noDataImgId != -1) {
                    iv_pic.setBackgroundResource(noDataImgId);
                }
                if (title != null) {
                    tv_content.setText(title);
                }
                if (btn_text != null && viewLoadingInterface != null) {
                    OnClickListener onClickListener = new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            viewLoadingInterface.NoDate(v.getId());
                        }
                    };
                }
                if (viewLoadingInterface != null) {
                    //处理回调
                    Button btn_1 = (Button) view.findViewById(R.id.btn_1);
                    Button btn_2 = (Button) view.findViewById(R.id.btn_2);
                    OnClickListener onClickListener = new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            viewLoadingInterface.NoDate(v.getId());
                        }
                    };
                    btn_1.setOnClickListener(onClickListener);
                    btn_2.setOnClickListener(onClickListener);
                }
                addView(view);
                break;
            case HIDE_LAYOUT:
                //    Log.e( TAG, "--------HIDE_LAYOUT---------------" );
                setVisibility(View.GONE);
                break;
            case NODATA_ENABLE_CLICK:
                mErrorState = NODATA_ENABLE_CLICK;
                img.setBackgroundResource(R.drawable.page_icon_empty);
                // img.setBackgroundDrawable(SkinsUtil.getDrawable(context,"page_icon_empty"));
                img.setVisibility(View.VISIBLE);
                animProgress.setVisibility(View.GONE);
                setTvNoDataContent();
                clickEnable = true;
                break;
            default:
                break;
        }
    }

    public void setInitParameter() {

    }

    public void setOnLayoutClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    /**
     * 新添设置背景
     *
     * @author 火蚁 2015-1-27 下午2:14:00
     */
    public void setErrorImag(int imgResource) {
        try {
            img.setImageResource(imgResource);
        } catch (Exception e) {
        }
    }

//	public void setErrorcolor() {
//		try {
//			img.setBackgroundColor(getResources().getColor(R.color.bg_gray));
//		} catch (Exception e) {
//		}
//	}

    public void setErrorMessage(String msg) {
        tv.setText(msg);
    }

    public void setTvNoDataContent() {
        if (!"".equals(strNoDataContent)) {
            tv.setText(strNoDataContent);
        } else {
            tv.setText(context.getResources().getString(R.string.error_view_no_data));
        }
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == View.GONE) {
            mErrorState = HIDE_LAYOUT;
        }
        super.setVisibility(visibility);
    }

    /**
     * 判断是否有网络连接
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return /*mNetworkInfo.isAvailable()*/true;
            }
        }
        return /*false*/true;
    }
}

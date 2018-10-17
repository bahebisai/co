package com.xiaomi.emm.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.xiaomi.emm.R;


/**
 * Created by admin on 2017/7/19.
 */

public class DialogInfos extends Dialog {//todo baii util view

    Context context;
    private DialogInfos.ConfirmListener confirmListener;
    private DialogInfos.ConfirmListeners confirmListeners;
    //private TextView ;
    private TextView mContentstrm;
    private TextView mTitle,mTitles;
    private Button btn_sure,btn_cancel,ok;

    public DialogInfos(Context context, String title, String content, String btnEnsure, DialogInfos.ConfirmListener confirmListener)
    {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_infos);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(0));
        this.setCanceledOnTouchOutside(false);
        this.confirmListener = confirmListener;
        this.context = context;
        initView(title,content,btnEnsure);

        setLister();
    }


    public DialogInfos(Context context, String title, String content, String btnEnsure, String btnCancle, DialogInfos.ConfirmListeners confirmListeners)
    {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_infoss);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(0));
        this.setCanceledOnTouchOutside(false);
        this.confirmListeners = confirmListeners;
        this.context = context;
        initView(title,content,btnEnsure,btnCancle);

        setListers();
    }

    public  void setData(String title, String content){
        if (mTitles ==null || mContentstrm ==null) {
            return;
        }

        if(title!=null){

            mTitles.setText(title);
        }

        if(content!=null){
            mContentstrm.setText(content);
        }


    }
    private void setListers() {
        if (confirmListeners ==null) {
            return;
        }

        btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                confirmListeners.sure();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                confirmListeners.cancle();
            }
        });
    }


    private void setLister() {

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                confirmListener.ok();
            }
        });

    }

    private void initView(String title, String content, String btnEnsure) {

        ok = (Button) findViewById(R.id.btnOk);
        mContentstrm = (TextView) findViewById(R.id.content);

        mTitle = (TextView) findViewById(R.id.title);


        if(title!=null){

            mTitle.setText(title);
        }

        if(content!=null){
            mContentstrm.setText(content);
        }
        if(btnEnsure!=null){
            ok.setText(btnEnsure);
        }
    }


    private void initView(String title, String content,String btnEnsure,String btnCancle ) {

        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_sure = (Button) findViewById(R.id.btn_sure);
        mContentstrm = (TextView) findViewById(R.id.content);

        mTitles = (TextView) findViewById(R.id.title);


        if(title!=null){

            mTitles.setText(title);
        }

        if(content!=null){
            mContentstrm.setText(content);
        }

        if(btnEnsure!=null){
            btn_sure.setText(btnEnsure);
        }
        if(btnEnsure!=null){
            btn_cancel.setText(btnEnsure);
        }
    }



    public interface ConfirmListener {
        public void ok();

    }


    public interface ConfirmListeners {

        public void sure();
        public void cancle();

    }

}

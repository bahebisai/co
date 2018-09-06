package com.xiaomi.emm.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.RemoteException;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomi.emm.R;

/**
 * Created by Administrator on 2017/11/9.
 */

public class MagicToast extends Toast {

    public static final String TAG = "MagicToast";
    static Context context;
    static Toast result = null;
    public MagicToast(Context context) {
        super( context );
        this.context = context;
    }

    /**
     * 自定义Toast布局
     * @param context
     * @param text
     * @return
     */
    public Toast makeText(Context context, CharSequence text) {

        result = new Toast(context);
        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflate.inflate( R.layout.layout_magic_toast, null);

        TextView textView = (TextView) view.findViewById( R.id.toast_text );
        textView.setText( text );
        //设置显示位置
        setGravity(view);

        result.setView( view );
        result.setDuration( Toast.LENGTH_LONG );

        return result;
    }

    /**
     * 设置显示位置
     */
    private static void setGravity(View view) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        result.setGravity( Gravity.BOTTOM, 0, height/2 );
    }
}

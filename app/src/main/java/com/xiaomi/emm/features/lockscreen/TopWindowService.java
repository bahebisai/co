package com.xiaomi.emm.features.lockscreen;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.IntDef;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomi.emm.R;
import com.xiaomi.emm.utils.TheTang;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


public class TopWindowService extends Service {
	private  static final  String TAG="TopWindowService";
	public static final String OPERATION = "operation";
	public static final int OPERATION_SHOW = 100;
	public static final int OPERATION_HIDE = 101;

	private static final int HANDLE_CHECK_ACTIVITY = 200;

	private boolean isAdded = false; // 是否已增加悬浮窗
	private static WindowManager wm;
	private static WindowManager.LayoutParams params;
	private Button btn_floatView;

	private List<String> homeList; // 桌面应用程序包名列表
	private ActivityManager mActivityManager;

	IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
	private RelativeLayout mInView;
	private MyReceiver receiver;

	private class MyReceiver extends BroadcastReceiver {

		private final String SYSTEM_DIALOG_REASON_KEY = "reason";
		private final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
		private final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);

				if (reason == null) {
                    return;
                }

				// Home键
				/*if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
					Toast.makeText(getApplicationContext(), "按了Home键", Toast.LENGTH_SHORT).show();
					Log.w(TAG,"按了Home键");
				}*/

				// 最近任务列表键
				if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
					Toast.makeText(getApplicationContext(), "按了最近任务列表", Toast.LENGTH_SHORT).show();
					Log.w(TAG,"按了最近任务列表");
					//startActivity(new Intent(TheTang.getSingleInstance().getContext(),Lock2Activity.class));
					if (wm !=null  && mInView != null){

					//	wm.removeView(mInView);
					}

				}
			}
		}

}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		createFloatView();
		//homeList = getHomes();
		receiver = new MyReceiver();
		registerReceiver(receiver, homeFilter);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int operation = intent.getIntExtra(OPERATION, OPERATION_SHOW);
		Log.w(TAG,operation+"---pppp");
		switch (operation)
		{
			case OPERATION_SHOW:
				//createFloatView();
			/*	mHandler.removeMessages(HANDLE_CHECK_ACTIVITY);
				mHandler.sendEmptyMessage(HANDLE_CHECK_ACTIVITY);*/

		/*		MyReceiver receiver = new MyReceiver();
				registerReceiver(receiver, homeFilter);*/
				break;
			case OPERATION_HIDE:


				if (wm!=null && mInView != null){
					wm.removeView(mInView);
				}
				break;
		}
		return super.onStartCommand(intent, flags, startId);
	}



	/**
	 * 创建悬浮窗
	 */
	private void createFloatView()
	{
		btn_floatView = new Button(getApplicationContext());
		btn_floatView.setText("警告");


		LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
//加载需要的XML布局文件
		final RelativeLayout mInView = (RelativeLayout)inflater.inflate(R.layout.activity_lock2, null, false);

		wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

		params = new WindowManager.LayoutParams();

		// 设置window type
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		/*
		 * 如果设置为params.type = WindowManager.LayoutParams.TYPE_PHONE; 那么优先级会降低一些,
		 * 即拉下通知栏不可见
		 */

		//	params.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明

		// 设置Window flag
		params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

		params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;




		/*
		 * 下面的flags属性的效果形同“锁定”。 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
		 * wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL |
		 * LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
		 */

		// 设置悬浮窗的长得宽
		/*params.width = 200;
		params.height = 200;*/

		//int flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
		// | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		// 如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
		//	params.flags = flags;
		// 不设置这个弹出框的透明遮罩显示为黑色
		params.format = PixelFormat.TRANSLUCENT;
		// FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
		// 设置 FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按
		// 不设置这个flag的话，home页的划屏会有问题
		params.width = WindowManager.LayoutParams.MATCH_PARENT;
		params.height = WindowManager.LayoutParams.MATCH_PARENT;
		//	params.gravity = Gravity.CENTER;


		mInView.setFocusableInTouchMode(true);

		mInView.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				Log.e("wytings","onKeyListener");
				Log.w(TAG,"onKeyListener");

				//mHandler.removeMessages(HANDLE_CHECK_ACTIVITY);

				//	return super.onKeyDown(keyCode, event);

				/*if (keyCode == KeyEvent.KEYCODE_){

				}*/
				Log.w(TAG,"onKeyListener==="+keyCode);
				wm.removeView(mInView);
				return  false;

			}
		});

		wm.addView(mInView, params);
		isAdded = true;
	}


}

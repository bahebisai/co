package com.xiaomi.emm.socket.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.xiaomi.emm.R;
import com.xiaomi.emm.utils.TheTang;


/**
 * 后台保持连接服务
 */
public class TVBoxService extends Service {
    public static final String TAG = "TVBoxService";

    // 服务单例
    private static TVBoxService sInstance;

    // 启动的任务
    private ConnTask conn_task;

    // 与 UDP服务器交互时用到的消息
    public static final int MSG_UDP_SEND_ERROR = 20;
    public static final int MSG_UDP_RECV_ERROR = 21;
    public static final int MSG_UDP_RECV_SUCC = 22;

    public static final int MSG_QRY_BOX_SUCC = 23;
    public static final int MSG_QRY_BOX_ERROR = 23;

    @Override
    public void onCreate() {
        super.onCreate();
        TheTang.getSingleInstance().startForeground(this,getResources().getString(R.string.long_link_service), "EMM", 1);
        sInstance = this;
        Log.e(TAG, "初始化TVBoxService");
        // 建立长连接任务
        conn_task = new ConnTask(this);
        if (TheTang.getSingleInstance().isNetworkConnected(TheTang.getSingleInstance().getContext())) {
            conn_task.stopReconnOpt();
            conn_task.start();
        }
    }

    public static TVBoxService getInstance() {
        return sInstance;
    }


    public ConnTask getConnTask() {
        if (conn_task == null) {
            conn_task = new ConnTask(this);
        }
        return conn_task;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        //设置为前台Service
		/*try {
			Notification notification = null;
			PendingIntent pendingIntent = PendingIntent.getService( this, 110, intent, PendingIntent.FLAG_UPDATE_CURRENT );
			notification = new Notification.Builder( this )
					.setContentTitle( "EMM" )
					.setContentText( "长连接正在运行!" )
					.setSmallIcon( R.mipmap.logo )
					//.setLargeIcon(R.mipmap.ic_launcher)
					.setContentIntent( pendingIntent )
					.build();

			notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
			notification.flags |= Notification.FLAG_NO_CLEAR;
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			//让该service前台运行，避免手机休眠时系统自动杀掉该服务
			//如果 id 为 0 ，那么状态栏的 notification 将不会显示。
			startForeground(110, notification);
		} catch (Exception e) {
			Log.w( TAG, e.toString() );
		}


		/*sInstance = this;

		// 建立长连接任务
		conn_task = new ConnTask(this);
		*/
	/*	if (conn_task ==null){
			//UserMgr.createInstance(this);
			// 建立长连接任务
			conn_task = new ConnTask(this);

		}else  if (!conn_task.isRunning()){
			//如果长连接已经stop了就start
			conn_task.start();
		}*/
        //TheTang.getSingleInstance().startForeground(this, intent,"长连接正在运行!", "EMM", 1);
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sInstance = null;

        if (conn_task != null) {
            conn_task.stop();
            conn_task = null;
        }
    }

    /**
     * 日志上报云端回调接口
     */
/*	private ILog.OnReportToNetListener myLogNetReport = new ILog.OnReportToNetListener() {
		@Override
		public void onLogToNet(final String msg) {
			// 普通日志不需要新开线程上报（已经是处于新开线程里）
			StringBuilder sb = new StringBuilder();
			sb.append(AppEnv.LOG_URL);
			sb.append("?eventID=app.debug&MOBILE=");
			sb.append(UserMgr.getUserInfo().mobile);
			sb.append("&HOME_ID=");
			sb.append(UserMgr.getUserInfo().home_id);
			String url = sb.toString();

			try {
				JSONObject json = new JSONObject();
				json.put("channel_id", AppEnv.m_channel_id);
				String base64 = BASE64.getBASE64(msg);
				json.put("log_info", base64);

				HttpUtils.postBizRet(url, json);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onCrashToNet(final String msg) {
			new Thread(){

				@Override
				public void run() {
					ThreadUtils.setToBackground();

					// crash 时尽快将日志上报上去，提升线程优先级
					Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);

					StringBuilder sb = new StringBuilder();
					sb.append(AppEnv.LOG_URL);
					sb.append("?eventID=app.crash&MOBILE=");
					sb.append(UserMgr.getUserInfo().mobile);
					sb.append("&HOME_ID=");
					sb.append(UserMgr.getUserInfo().home_id);
					String url = sb.toString();

					try {
						JSONObject json = new JSONObject();
						json.put("ua", StdHardwareMgr.getModel());
						json.put("ver", AppEnv.ver_name);
						json.put("channel_id", AppEnv.m_channel_id);
						String base64 = BASE64.getBASE64(msg);
						json.put("crash_info", base64);

						HttpUtils.postBizRet(url, json);
					} catch(Exception e) {
						e.printStackTrace();
					}
				};
			}.start();
		}
	};*/
}
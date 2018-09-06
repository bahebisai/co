package com.xiaomi.emm.socket.time;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.SparseArray;

public class StdAlarmImpl {
	private static final String LOG_TAG = "StdAlarmImpl";

	/**
	 * 对外接口相关
	 */
	public interface IAlarmResponse {
		public abstract void onAlarm();
	}

	/**
	 * 初始化
	 * @param context
	 */
	public static void init(Context context) {
		m_context = context;
	}

	/**
	 * 单个定时
	 * @param response
	 * @param delay
	 * @return
	 */
	public static int schedule(IAlarmResponse response, long delay) {
		if(m_context == null) {
			return -1;
		}

		int id = newId();
		addAlarm(id, response, false, delay);
		setAlarm(id, delay);
		return id;
	}

	/**
	 * 循环定时
	 * @param response
	 * @param delay
	 * @param period
	 * @return
	 */
	public static int scheduleWithFixedDelay(IAlarmResponse response, long delay,
											 long period) {
		if(m_context == null) {
			return -1;
		}

		int id = newId();
		addAlarm(id, response, true, period);
		setAlarm(id, delay);
		return id;
	}

	/**
	 * 取消定时
	 * @param id
	 */
	public static void cancel(int id) {
		if(m_context == null) {
			return;
		}

		Intent intent = new Intent(ALARM_ACTION);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(m_context, id,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager m_alarmManager = (AlarmManager) m_context
				.getSystemService(Context.ALARM_SERVICE);
		m_alarmManager.cancel(pendingIntent);
		removeAlarm(id);
	}

	/**
	 * 内部实现
	 */
	static class StdAlarmItem {
		IAlarmResponse m_response;
		boolean m_bRepeat;
		long m_delay;

		public StdAlarmItem(IAlarmResponse response, boolean repeat, long delay) {
			m_response = response;
			m_bRepeat = repeat;
			m_delay = delay;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("repeat = ");
			sb.append(m_bRepeat);
			sb.append(", delay = ");
			sb.append(m_delay);

			return sb.toString();
		}
	}

	private static final String ALARM_ACTION = "org.std.control.time.alarm";
	private static final String ALARM_INTENT_PACKAGE = "package";
	private static final String ALARM_INTENT_ID = "id";

	private static Context m_context;
	private static int m_id;
	private static SparseArray<StdAlarmItem> m_alarms;

	static {
		m_id = 1;
		m_alarms = new SparseArray<StdAlarmItem>();

		m_broadcastReceiver = null;
	}

	private static synchronized int newId() {
		return m_id++;
	}

	private static void addAlarm(int id, IAlarmResponse response, boolean repeat,
								 long delay) {
		StdAlarmItem item = new StdAlarmItem(response, repeat, delay);
		m_alarms.append(id, item);
	}

	private static synchronized void removeAlarm(int id) {
		try {
			m_alarms.remove(id);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(m_alarms.size() == 0 && m_broadcastReceiver != null){
			m_context.unregisterReceiver(m_broadcastReceiver);
			m_broadcastReceiver = null;
		}
	}

	private static void setAlarm(int id, long delay) {
		if(m_broadcastReceiver == null) {
			// 接收广播
			m_broadcastReceiver = new AlarmReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(ALARM_ACTION);
			m_context.registerReceiver(m_broadcastReceiver, filter);
		}

		Intent intent = new Intent(ALARM_ACTION);
		intent.putExtra(ALARM_INTENT_PACKAGE, m_context.getPackageName());
		intent.putExtra(ALARM_INTENT_ID, id);

		int rtc_type = TimerCorrectiveHelper.getRtcType();
		TimerCorrectiveHelper.onSend(m_context, rtc_type, id,
				System.currentTimeMillis() + delay, intent);
	}

	/**
	 * 广播
	 */
	private static BroadcastReceiver m_broadcastReceiver;

	private static class AlarmReceiver extends BroadcastReceiver {
		public void onReceive(Context paramContext, Intent paramIntent) {
			// 判断是否属于本应用
			String package_name = paramIntent.getStringExtra(ALARM_INTENT_PACKAGE);
			if (package_name == null
					|| !package_name.equals(m_context.getPackageName())) {
				return;
			}
			// 判断ID是否有效
			int id = paramIntent.getIntExtra(ALARM_INTENT_ID, -1);
			if (id == -1) {
				return;
			}

			// 处理
			boolean res = TimerCorrectiveHelper.onReceive(m_context, id,
					paramIntent);
			if (!res) {
				String action = paramIntent.getAction();
				if (action.equals(ALARM_ACTION)) {
					StdAlarmItem item = m_alarms.get(id);
					if(item == null){
						return;
					}

					//ILog.i(LOG_TAG, "recv alarm : " + item);
					try {
						item.m_response.onAlarm();
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (item.m_bRepeat) {
						setAlarm(id, item.m_delay);
					} else {
						removeAlarm(id);
					}
				}
			}
		}
	};

}

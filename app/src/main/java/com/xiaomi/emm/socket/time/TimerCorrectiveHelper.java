package com.xiaomi.emm.socket.time;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.lang.reflect.Method;

public class TimerCorrectiveHelper {
	private static final String TAG = "TimerCorrectiveHelper";

	/**
	 * 判断是否小米系列手机
	 */
	public static boolean isMiPhone() {
		if (XIAOMI.equalsIgnoreCase(Build.MANUFACTURER)) {
			return true;
		}

		return false;
	}

	/**
	 * 判断是否红米手机
	 *
	 * @return
	 */
	public static boolean isHongmi() {
		if (isMiPhone() && Build.MODEL.startsWith("HM")) {
			return true;
		}

		return false;
	}

	public static int getRtcType() {
		if (TimerCorrectiveHelper.isHongmi()) {
			// 测试发现红米手机用RTC才起作用，用RTC_WAKEUP不起作用
			return AlarmManager.RTC;
		}

		return AlarmManager.RTC_WAKEUP;
	}

	/**
	 * 这个方法产生一个定时广播(Broadcast)的 {@link PendingIntent}。
	 *
	 *
	 * @param context
	 *            The Context the intent should be performed in.
	 *
	 *
	 * @see AlarmManager#set(int, long, PendingIntent)
	 */
	public static void onSend(Context context, int type, int id,
                              long triggerAtMillis, Intent intent) {
		if (isMiPhone()) {
			long curTime = System.currentTimeMillis();
			curTime += AHEAD_TIME_MILLIS;
			// 红米手机是AlarmManager.RTC，所以这里需要屏蔽
			if (/*type == AlarmManager.RTC_WAKEUP && */triggerAtMillis > curTime) {
				Intent newIntent = new Intent();
				newIntent.setAction(intent.getAction());
				newIntent.putExtra(ORIGIONAL_INTENT, intent);
				newIntent.putExtra(ORIGIONAL_TIMER, triggerAtMillis);

				triggerAtMillis -= AHEAD_TIME_MILLIS;
				intent = newIntent;
			}
		}

		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		PendingIntent operation = PendingIntent.getBroadcast(context, id,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		setAlarm(alarmManager, type, triggerAtMillis, operation);
	}

	/**
	 * 在 { AlarmReceiver} 处理接受的intent之前调用这个方法，该方法用来对做了“唤醒对齐”功能的intent进行处理。
	 *
	 * @return True 如果接受到的intent是一个“唤醒”对齐处理的意图，并且继续延迟，否则，返回false。
	 */
	public static boolean onReceive(Context context, int id, Intent intent) {
		final Intent origionalIntent = intent
				.getParcelableExtra(ORIGIONAL_INTENT);
		if (origionalIntent != null) {
			final long triggerAtMillis = intent.getLongExtra(ORIGIONAL_TIMER,
					System.currentTimeMillis());

			PendingIntent operation = PendingIntent.getBroadcast(context, id,
					origionalIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			AlarmManager alarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			setAlarm(alarmManager, AlarmManager.RTC, triggerAtMillis, operation);
			return true;
		}
		return false;
	}

	/**
	 * 根据SDK的版本，调用不同的方法
	 * @param alarmManager
	 * @param type
	 * @param triggerAtMillis
	 * @param operation
	 */
	private static void setAlarm(AlarmManager alarmManager, int type,
                                 long triggerAtMillis, PendingIntent operation) {
		if (Build.VERSION.SDK_INT >= 19) {
			try {
				Class<?> arrayOfClass[] = new Class[3];
				arrayOfClass[0] = Integer.TYPE;
				arrayOfClass[1] = Long.TYPE;
				arrayOfClass[2] = PendingIntent.class;

				Object[] arrayOfObject = new Object[3];
				arrayOfObject[0] = Integer.valueOf(type);
				arrayOfObject[1] = Long.valueOf(triggerAtMillis);
				arrayOfObject[2] = operation;

				Method method = AlarmManager.class.getMethod("setExact",
						arrayOfClass);
				method.invoke(alarmManager, arrayOfObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//ILog.d(TAG, "AlarmManager setExact()");
		} else {
			alarmManager.set(type, triggerAtMillis, operation);
			//ILog.d(TAG, "AlarmManager set()");
		}
	}

	/**
	 * 内部实现
	 */
	private static final String ORIGIONAL_INTENT = "origional_intent";

	private static final String ORIGIONAL_TIMER = "origional_timer";

	/**
	 * 提前的时间段为5分钟。小米手机的对齐时间大概是5分钟。
	 */
	private static final long AHEAD_TIME_MILLIS = 5 * 60 * 1000;

	/**
	 * 小米手机的ROM的 {@link AlarmManager} 做了“对齐”，
	 */
	private static final String XIAOMI = "Xiaomi";

}

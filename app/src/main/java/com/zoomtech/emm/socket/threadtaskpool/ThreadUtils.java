package com.zoomtech.emm.socket.threadtaskpool;

import android.os.Handler;
import android.os.Looper;

public class ThreadUtils {


	/**
	 * 调整当前线程为后台任务线程级别，降低它的优先级，防止影响UI线程
	 */
	public static void setToBackground(){
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
	}

	/**
	 * 检测当前是否运行在主线程中，如果不是，抛出运行时异常。
	 * @param functionName
	 */
	public static void checkInMain(String functionName){
		if (Looper.myLooper() != Looper.getMainLooper()){
			throw new IllegalAccessError(functionName + " NOT called in main thread.");
		}
	}

	/**
	 * 检测是否在主线程中
	 * @return
	 */
	public static boolean isInMain(){
		return Looper.myLooper() == Looper.getMainLooper();
	}

	/**
	 * 提交一个可执行的任务
	 */
	public static void submitRunnable(Runnable runnable){
		final Runnable frunnable = runnable;
		new Thread(){
			@Override
			public void run() {
				setToBackground();
				frunnable.run();
			}
		}.start();
	}

	/**
	 * 在主线程中执行代码
	 * @param runnable
	 */
	public static void runInMain(Runnable runnable){
		runInMain(null, runnable);
	}

	/**
	 * 在主线程中执行代码
	 * @param handler
	 * @param runnable
	 */
	public static void runInMain(Handler handler, Runnable runnable){
		if (runnable == null){
			return;
		}
		if (isInMain()){
			runnable.run();
		} else {
			if (handler != null){
				handler.post(runnable);
			} else {
				new Handler(Looper.getMainLooper()).post(runnable);
			}
		}
	}
}

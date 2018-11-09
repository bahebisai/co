package com.zoomtech.emm.socket.threadtaskpool;

import android.text.TextUtils;
import android.util.Log;

import com.zoomtech.emm.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


class ThreadTask implements Comparable<ThreadTask> {
	private static final String TAG = "ThreadTask";
	/** ThreadTask名称 */
	private String name;
	/** 执行当前ThreadTask的ThreadTaskPool */
	private ThreadTaskPool threadTaskPool;
	/** 前置ThreadTasks */
	private ArrayList<ThreadTask> preTasks;
	/** 后置threadTasks */
	private ArrayList<ThreadTask> nextTasks;
	/** 要执行的任务 */
	private final Callable<Result> callable;
	/** 是否是网络请求任务 */
	private volatile boolean isNetworkTask;
	/** 是否执行完成 */
	private volatile boolean isDone;
	/** 执行失败的次数 */
	private volatile int failedTimes;
	/** 记录开始执行的时间 */
	private volatile long startTime;
	/** 最后一次执行成功的耗时 */
	private volatile long execDuration;
	/** 线程任务执行完成的监听 */
	private OnThreadTaskFinishedListener onThreadTaskFinishedListener;
	/** 线程任务抛出未捕获的异常 */
	private OnUncaughtExceptionThrownListener onUncaughtExceptionThrownListener;


	ThreadTask(ThreadTaskPool threadTaskPool, final String name, final TaskWithResult taskWithResult,
               String[] preTaskNames, boolean isNetworkTask,
               OnThreadTaskFinishedListener onThreadTaskFinishedListener,
               OnUncaughtExceptionThrownListener onUncaughtExceptionThrownListener){
		this.threadTaskPool = threadTaskPool;
		this.name = name;
		this.isNetworkTask = isNetworkTask;
		this.preTasks = new ArrayList<ThreadTask>();
		initPreTasks(preTaskNames);
		this.nextTasks = new ArrayList<ThreadTask>();
		this.callable = new Callable<Result>(){

			@Override
			public Result call() throws Exception {
				ResultType resultType = ResultType.RET_SUCCESS;
				try {
					resultType = taskWithResult.run();
				} catch (Exception e){
					if (ThreadTask.this.onUncaughtExceptionThrownListener != null){
						ThreadTask.this.onUncaughtExceptionThrownListener.onUncaughtExceptionThrown(e);
					}
					Log.w(TAG, e.toString());
					Log.w(TAG, LogUtil.getExceptionInfo(e));
				}
				return new Result(resultType, ThreadTask.this);
			}

		};
		this.isDone = false;
		this.failedTimes = 0;
		this.execDuration = -1;
		this.onThreadTaskFinishedListener = onThreadTaskFinishedListener;
		this.onUncaughtExceptionThrownListener = onUncaughtExceptionThrownListener;
	}

	private void initPreTasks(String[] preTaskNames) {
		if (preTaskNames == null || preTaskNames.length == 0){
			return;
		}
		for (String taskName : preTaskNames){
			if (TextUtils.isEmpty(taskName)){
				continue;
			}
			ThreadTask preTask = threadTaskPool.getThreadTaskByName(taskName);
			if (preTask == null){
				throw new IllegalArgumentException("Not found pre-threadTask ["+taskName+"]");
			}
			addPreThreadTask(preTask);
		}
	}

	Callable<Result> getCallable(){
		return callable;
	}

	/**
	 * 添加前置ThreadTask
	 * @param threadTask
	 */
	void addPreThreadTask(ThreadTask threadTask){
		preTasks.add(threadTask);
		threadTask.addNextThreadTask(this);
	}

	/**
	 * 添加后置ThreadTask
	 * @param threadTask
	 */
	synchronized private void addNextThreadTask(ThreadTask threadTask){
		nextTasks.add(threadTask);
	}

	/**
	 * 当前ThreadTask执行完成，通知所有后置ThreadTask
	 */
	synchronized void notifyNextTheadTasks(){
		for (ThreadTask threadTask : nextTasks){
			threadTask.onPreThreadTaskDone();
		}
	}

	/**
	 * 当有前置ThreadTask执行完毕时的回调
	 */
	synchronized private void onPreThreadTaskDone(){
		boolean allPreDone = true;
		for (ThreadTask threadTask : preTasks){
			if (!threadTask.isDone()){
				allPreDone = false;
				break;
			}
		}
		if (allPreDone){
			threadTaskPool.execute(this);
		}
	}

	/**
	 * 当前ThreadTask是否可以执行
	 * @return
	 */
	synchronized boolean canExecute(){
		boolean ret = true;
		for (ThreadTask threadTask : preTasks){
			if (!threadTask.isDone()){
				ret = false;
				break;
			}
		}
		return ret;
	}

	/**
	 * 获取当前ThreadTask所有的前置ThreadTask
	 * @return
	 */
	List<ThreadTask> getPreThreadTasks(){
		return preTasks;
	}

	/**
	 * 设置该任务是否已经执行完成
	 * @param isDone
	 */
	void setDone(boolean isDone){
		this.isDone = isDone;
		if (isDone){
			execDuration = System.currentTimeMillis() - startTime;
		}
	}

	/**
	 * 当前ThreadTask是否已经执行完成
	 * @return
	 */
	boolean isDone(){
		return this.isDone;
	}

	/**
	 * 获取ThreadTask名称
	 * @return
	 */
	String getName(){
		return this.name;
	}

	@Override
	public int compareTo(ThreadTask another) {
		// 失败次数多的排在前面
		return failedTimes < another.failedTimes ? 1 :
				(failedTimes > another.failedTimes ? -1 : 0);
	}

	/**
	 * 递增执行失败的次数记录
	 */
	void increaseFailedTimes(){
		this.failedTimes++;
	}

	/**
	 * 获取执行失败的次数
	 * @return
	 */
	int getFailedTimes(){
		return this.failedTimes;
	}

	/**
	 * 设置开始执行的时间
	 * @param startTime
	 */
	void setStartTime(long startTime){
		this.startTime = startTime;
	}

	/**
	 * 获取执行开始时间，如果之前有失败过，则记录的是最近一次运行的开始时间
	 * @return
	 */
	long getStartTime(){
		return this.startTime;
	}

	/**
	 * 获取执行完成消耗的时间，如果没有执行完成，则返回-1
	 * @return
	 */
	long getExecDuration(){
		return this.execDuration;
	}

	/**
	 * 是否是网络请求任务
	 * @return
	 */
	boolean isNetworkTask(){
		return isNetworkTask;
	}

	ThreadTaskStat getStat(){
		return new ThreadTaskStat(name, isDone, failedTimes, startTime, execDuration);
	}

	public OnThreadTaskFinishedListener getOnFinishedListener(){
		return onThreadTaskFinishedListener;
	}

	public OnUncaughtExceptionThrownListener getOnUncaughtExceptionThrownListener(){
		return onUncaughtExceptionThrownListener;
	}
}

package com.zoomtech.emm.socket.threadtaskpool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Process;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;


/**
 * 增强版的线程池，主要针对网络请求任务的管理，对任务之间网状依赖关系、网络异常自处理、任务执行情况统计有良好的支持。
 * @author chenyi
 */
public class ThreadTaskPool {
	public static final String TAG = "ThreadTaskPool";
	private static final String NETWORK_CONN_CHANGED_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	private Context context;
	/**
	 * 用来实际执行任务的线程池
	 */
	private ExecutorService threadPool;
	/**
	 * 保存当前正在执行的任务的future results
	 */
	private ConcurrentLinkedQueue<Future<Result>> futureResults;
	/**
	 * 用来循环监测正在线程池中执行的子线程返回结果的线程
	 */
	private Runnable futureResultThread;
	/**
	 * 从可执行的网络请求任务队列中获取ThreadTask提交给线程池执行
	 */
	private Runnable submitNetworkThreadTaskThread;
	/**
	 * 可执行的网络请求相关的任务队列
	 */
	private PriorityBlockingQueue<ThreadTask> networkThreadTasks;
	/**
	 * 所有添加进来的ThreadTask
	 */
	private ConcurrentHashMap<String, ThreadTask> allThreadTasks;
	/**
	 * ThreadTaskPool是否已经处于关闭状态
	 */
	private volatile boolean isShutdown;
	/**
	 * 待执行完成的任务数量
	 */
	private AtomicInteger toDoNums;
	/**
	 * futureResultThread相关的锁。<br>
	 * 当待执行任务为空，且isShutdown == false时，保持futureResultThread阻塞状态
	 */
	private Object futureResultThreadLock;
	/**
	 * 网络连接管理器
	 */
	private ConnectivityManager connectivityManager;
	/**
	 * 网络状态变化的锁对象
	 */
	private Object networkAvaliableLock;
	/**
	 * 监听网络状态变化的广播接收器
	 */
	private NetworkConnectChangedReceiver networkConnectChangedReceiver;
	/**
	 * ThreadTaskPool创建的时间
	 */
	private volatile long createTime;
	/**
	 * ThreadTaskPool从创建到shutdown的持续时间
	 */
	private volatile long execDuration;

	/**
	 * ThreadTaskPool执行完所有任务退出的监听器
	 */
	private OnFinishedListener onFinishedListener;
	/**
	 * 所有任务执行完成后的执行状态结果
	 */
	private List<ThreadTaskStat> threadTaskStats;
	/**
	 * 是否已经中断所有任务执行
	 */
	private volatile boolean isInterrupted;

	public ThreadTaskPool(Context context){
		this.context = context.getApplicationContext();
		this.connectivityManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
		this.networkAvaliableLock = new Object();
		// 注册对网络状态变化的监听
		networkConnectChangedReceiver = new NetworkConnectChangedReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NETWORK_CONN_CHANGED_ACTION);
		this.context.registerReceiver(networkConnectChangedReceiver, intentFilter);

		this.threadPool = Executors.newCachedThreadPool(new TaskThreadFactory());
		this.allThreadTasks = new ConcurrentHashMap<String, ThreadTask>();
		this.futureResults = new ConcurrentLinkedQueue<Future<Result>>();
		this.futureResultThread = new FutureResultThread();
		this.futureResultThreadLock = new Object();
		this.toDoNums = new AtomicInteger(0);
		// 启动监测ThreadTask执行结果的线程
		this.threadPool.execute(futureResultThread);

		this.networkThreadTasks = new PriorityBlockingQueue<ThreadTask>();
		this.submitNetworkThreadTaskThread = new SubmitNetworkThreadTaskThread();
		this.threadPool.execute(submitNetworkThreadTaskThread);

		this.isShutdown = false;
		this.createTime = System.currentTimeMillis();
		this.execDuration = -1L;
		this.threadTaskStats = null;
		this.isInterrupted = false;
	}

	/**
	 * 添加任务
	 * @param taskName 任务名称，唯一标识一个任务
	 * @param task 任务
	 * @param preTaskNames 当前任务的所有前置任务的名称，即所有前置任务执行完成当前任务才开始执行
	 */
	public void addTask(String taskName, TaskWithResult task, String[] preTaskNames){
		addTask(taskName, task, preTaskNames, false, null, null);
	}

	/**
	 * 添加任务
	 * @param taskName 任务名称，唯一标识一个任务
	 * @param task 任务
	 * @param preTaskNames 当前任务的所有前置任务的名称，即所有前置任务执行完成当前任务才开始执行
	 * @param isNetworkTask 当前任务是否是网络请求任务，如果标记为网络请求类型的任务，当网络不可用时，将会暂停运行直至网络恢复可用；否则会不断尝试重复执行。
	 */
	public void addTask(String taskName, TaskWithResult task, String[] preTaskNames, boolean isNetworkTask){
		addTask(taskName, task, preTaskNames, isNetworkTask, null, null);
	}

	/**
	 * 添加任务
	 * @param taskName 任务名称，唯一标识一个任务
	 * @param task 任务
	 * @param preTaskNames 当前任务的所有前置任务的名称，即所有前置任务执行完成当前任务才开始执行
	 * @param isNetworkTask 当前任务是否是网络请求任务，如果标记为网络请求类型的任务，当网络不可用时，将会暂停运行直至网络恢复可用；否则会不断尝试重复执行。
	 * @param onThreadTaskFinishedListener 线程任务执行完成的回调
	 */
	public void addTask(String taskName, TaskWithResult task, String[] preTaskNames, boolean isNetworkTask,
                        OnThreadTaskFinishedListener onThreadTaskFinishedListener){
		addTask(taskName, task, preTaskNames, isNetworkTask, onThreadTaskFinishedListener, null);
	}

	/**
	 * 添加任务
	 * @param taskName 任务名称，唯一标识一个任务
	 * @param task 任务
	 * @param preTaskNames 当前任务的所有前置任务的名称，即所有前置任务执行完成当前任务才开始执行
	 * @param isNetworkTask 当前任务是否是网络请求任务，如果标记为网络请求类型的任务，当网络不可用时，将会暂停运行直至网络恢复可用；否则会不断尝试重复执行。
	 * @param onUncaughtExceptionThrownListener 线程任务抛出未捕获的异常的回调
	 */
	public void addTask(String taskName, TaskWithResult task, String[] preTaskNames, boolean isNetworkTask,
                        OnUncaughtExceptionThrownListener onUncaughtExceptionThrownListener){
		addTask(taskName, task, preTaskNames, isNetworkTask, null, onUncaughtExceptionThrownListener);
	}

	/**
	 * 添加任务
	 * @param taskName 任务名称，唯一标识一个任务
	 * @param task 任务
	 * @param preTaskNames 当前任务的所有前置任务的名称，即所有前置任务执行完成当前任务才开始执行
	 * @param isNetworkTask 当前任务是否是网络请求任务，如果标记为网络请求类型的任务，当网络不可用时，将会暂停运行直至网络恢复可用；否则会不断尝试重复执行。
	 * @param onThreadTaskFinishedListener 线程任务执行完成的回调
	 * @param onUncaughtExceptionThrownListener 线程任务抛出未捕获的异常的回调
	 */
	public void addTask(String taskName, TaskWithResult task, String[] preTaskNames, boolean isNetworkTask,
                        OnThreadTaskFinishedListener onThreadTaskFinishedListener,
                        OnUncaughtExceptionThrownListener onUncaughtExceptionThrownListener){
		if (isShutdown){
			throw new RuntimeException("Add task [" + taskName + "] failed. ThreadTaskPool has shut down.");
		}
		synchronized (futureResultThreadLock) {
			toDoNums.incrementAndGet();
			futureResultThreadLock.notifyAll();
		}
		ThreadTask threadTask = new ThreadTask(this, taskName, task, preTaskNames, isNetworkTask,
				onThreadTaskFinishedListener, onUncaughtExceptionThrownListener);
		allThreadTasks.put(taskName, threadTask);
		if (threadTask.canExecute()){
			execute(threadTask);
		}
	}

	/**
	 * 执行任务
	 * @param threadTask
	 */
	void execute(ThreadTask threadTask){
		if (threadTask.isNetworkTask()){
			networkThreadTasks.add(threadTask);
		} else {
			Future<Result> futureResult = threadPool.submit(threadTask.getCallable());
			futureResults.add(futureResult);
			threadTask.setStartTime(System.currentTimeMillis());
		}
	}

	/**
	 * 通过任务名称查找对应的ThreadTask
	 * @param taskName ThreadTask名称
	 * @return
	 */
	ThreadTask getThreadTaskByName(String taskName){
		return allThreadTasks.get(taskName);
	}

	/**
	 * 获取指定任务名称的线程任务的当前执行状况
	 * @param taskName
	 * @return
	 */
	public ThreadTaskStat getThreadTaskState(String taskName){
		ThreadTask threadTask = getThreadTaskByName(taskName);
		if (threadTask == null){
			return new ThreadTaskStat("", false, 0, 0, 0);
		}
		return threadTask.getStat();
	}

	/**
	 * 获取当前线程池中所有线程任务的当前执行状况
	 *  taskName
	 * @return
	 */
	public List<ThreadTaskStat> getThreadTaskStates(){
		if (threadTaskStats != null){
			return threadTaskStats;
		}
		Set<Entry<String, ThreadTask>> entrySet = allThreadTasks.entrySet();
		List<ThreadTaskStat> stats = new ArrayList<ThreadTaskStat>(entrySet.size());
		ThreadTask threadTask = null;
		for (Entry<String, ThreadTask> entry : entrySet){
			threadTask = entry.getValue();
			stats.add(new ThreadTaskStat(threadTask.getName(), threadTask.isDone(), threadTask.getFailedTimes(),
					threadTask.getStartTime(), threadTask.getExecDuration()));
		}
		return stats;
	}

	/**
	 * 获取ThreadTaskPool从创建到shutdown的持续时间，如果当前没有shutdown,则返回从创建到当前的时间间隔
	 * @return
	 */
	public long getExecDuration(){
		if (isTerminated()){
			return execDuration;
		} else {
			return System.currentTimeMillis() - createTime;
		}
	}

	/**
	 * 停止添加任务，现有任务执行完成后，ThreadTaskPool自动停止
	 */
	public void shutdown(){
		isShutdown = true;
		synchronized (futureResultThreadLock) {
			futureResultThreadLock.notifyAll();
		}
	}

	/**
	 * 终端ThreadTaskPool的执行
	 */
	public void interrupt(){
		shutdown();
		terminate(true);
		isInterrupted = true;
	}

	/**
	 * ThreadTaskPool是否已经运行结束
	 * @return
	 */
	public boolean isTerminated(){
		return threadPool.isTerminated();
	}

	/**
	 * 关闭ThreadTaskPool
	 * @param isInterrupt 是否是中断操作导致的终止
	 */
	private synchronized void terminate(boolean isInterrupt){
		if (isTerminated()){
			return;
		}
		// 解注册网络状态改变的广播监听
		try {
			context.unregisterReceiver(networkConnectChangedReceiver);
		} catch (Exception e){
			 Log.w(TAG, e.toString());
		}
		threadTaskStats = getThreadTaskStates();
		allThreadTasks.clear();
		futureResults.clear();
		networkThreadTasks.clear();
		execDuration = System.currentTimeMillis() - createTime;
		threadPool.shutdownNow();
		if (onFinishedListener != null && !isInterrupt){
			onFinishedListener.onFinished();
		}
	}

	/**
	 * 循环监测threadTask执行返回结果
	 * @author chenyi
	 */
	private class FutureResultThread implements Runnable {

		@Override
		public void run() {
			while (!Thread.interrupted()){
				List<Future<Result>> needRemovedResults = new ArrayList<Future<Result>>();
				Iterator<Future<Result>> iterator = futureResults.iterator();
				while (iterator.hasNext()){
					if (isInterrupted){
						return;
					}
					Future<Result> futureResult = iterator.next();
					if (futureResult.isDone()){
						Result result = null;
						Exception thrownException = null;
						try {
							result = futureResult.get();
						} catch (InterruptedException e) {
							thrownException = e;
						} catch (ExecutionException e) {
							thrownException = e;
						}
						if (result == null){
							throw new RuntimeException(thrownException);
						} else {
							switch (result.getResultType()){
								case RET_SUCCESS:
									onRetSuccess(result);
									break;
								case RET_FAILED:
									onRetFailed(result);
									break;
							}
						}
						needRemovedResults.add(futureResult);
					}
				}

				// 移除已经执行完成的任务的future result
				for (Future<Result> futureResult : needRemovedResults){
					futureResults.remove(futureResult);
				}

				synchronized (futureResultThreadLock) {
					if (isInterrupted){
						return;
					}
					if (toDoNums.get() == 0){
						if (isShutdown){
							terminate(false);
							break;
						} else {
							try {
								futureResultThreadLock.wait();
							} catch (InterruptedException e) {
								break;
							} catch (IllegalMonitorStateException e2){
								// 执行 wait()时，当前thread已经处于interrupted状态
								break;
							}
						}
					}
				}


			}
		}

		private void onRetSuccess(Result result){
			toDoNums.decrementAndGet();
			result.getThreadTask().setDone(true);
			result.getThreadTask().notifyNextTheadTasks();

			OnThreadTaskFinishedListener onFinishedListener = result.getThreadTask().getOnFinishedListener();
			if (onFinishedListener != null){
				onFinishedListener.onFinished(result.getThreadTask().getStat());
			}
		}

		private void onRetFailed(Result result){
			result.getThreadTask().increaseFailedTimes();
			execute(result.getThreadTask());
		}
	}

	/**
	 * 负责从可执行队列中获取ThreadTask放到线程池执行
	 * @author chenyi
	 */
	private class SubmitNetworkThreadTaskThread implements Runnable {
		@Override
		public void run() {
			try {
				while (!Thread.interrupted()){
					// 如果网络不可用，等待网络可用
					synchronized (networkAvaliableLock) {
						while (!isNetworkAvaliable()){
							networkAvaliableLock.wait();
						}
					}
					ThreadTask threadTask = networkThreadTasks.take();
					Future<Result> futureResult = threadPool.submit(threadTask.getCallable());
					futureResults.add(futureResult);
					threadTask.setStartTime(System.currentTimeMillis());
				}
			} catch (InterruptedException e){
				// 正常中断，忽略异常
			}
		}
	}

	private class NetworkConnectChangedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (isNetworkAvaliable()){
				synchronized (networkAvaliableLock) {
					networkAvaliableLock.notifyAll();
				}
			}
		}
	}

	/**
	 * 当前是否有可用网络
	 * @return
	 */
	private boolean isNetworkAvaliable(){
		if (this.connectivityManager.getActiveNetworkInfo() == null){
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 设置ThreadTaskPool执行完所有任务的监听
	 * @param onFinishedListener
	 */
	public void setOnFinishedListener(OnFinishedListener onFinishedListener){
		this.onFinishedListener = onFinishedListener;
	}

	/**
	 * 当ThreadTaskPool执行完所有任务后，清理资源关闭线程池后的回调
	 * @author chenyi
	 */
	public interface OnFinishedListener {
		void onFinished();
	}

	static class TaskThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			return new TaskThread(r);
		}

	}

	static class TaskThread extends Thread {
		public TaskThread(Runnable r){
			super(r);
		}

		@Override
		public void run() {
			Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
			super.run();
		}
	}
}

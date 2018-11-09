package com.zoomtech.emm.socket.threadtaskpool;



import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 没有依赖关系的线程任务管理池，防止重复提交任务
 * @author chenyi
 */
public class TaskSetPool {
	/**
	 * 用来实际执行任务的线程池
	 */
	private ExecutorService threadPool;
	/**
	 * 当前正在执行的任务名称
	 */
	private SynchronizedTreeSet<String> taskNames;

	public TaskSetPool(){
		threadPool = Executors.newCachedThreadPool(new ThreadTaskPool.TaskThreadFactory());
		taskNames = new SynchronizedTreeSet<String>();
	}

	/**
	 * 添加任务
	 * @param taskName 任务名称，唯一标识一个任务
	 * @param task 子线程任务
	 * @param onTaskFinishedListener 任务执行完成的监听
	 */
	public void addTask(String taskName, TaskWithResult task,
                        OnTaskFinishedListener onTaskFinishedListener){
		boolean added = taskNames.add(taskName);
		if (!added){
			return;
		}
		threadPool.execute(new TaskRunnable(taskName, task, onTaskFinishedListener));
	}

	private class TaskRunnable implements Runnable {
		private String taskName;
		private TaskWithResult task;
		private OnTaskFinishedListener onTaskFinishedListener;

		public TaskRunnable(String taskName, TaskWithResult task,
                            OnTaskFinishedListener onTaskFinishedListener) {
			this.taskName = taskName;
			this.task = task;
			this.onTaskFinishedListener = onTaskFinishedListener;
		}

		@Override
		public void run() {
			ResultType resultType = null;
			do {
				resultType = task.run();
			} while (resultType != ResultType.RET_SUCCESS);
			if (onTaskFinishedListener != null){
				onTaskFinishedListener.onFinished();
			}
			taskNames.remove(taskName);
		}
	}

	/**
	 * 线程安全的TreeSet
	 * @author chenyi
	 */
	private class SynchronizedTreeSet<T> {
		private TreeSet<T> set = new TreeSet<T>();
		private synchronized boolean add(T t){
			return set.add(t);
		}
		private synchronized boolean remove(T t){
			return set.remove(t);
		}
	}

	public static interface OnTaskFinishedListener {
		void onFinished();
	}
}

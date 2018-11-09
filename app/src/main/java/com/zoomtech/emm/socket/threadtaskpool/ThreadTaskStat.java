package com.zoomtech.emm.socket.threadtaskpool;

public class ThreadTaskStat {
	/** ThreadTask的名称 */
	private String name;
	/** 是否执行完毕 */
	private boolean isDone;
	/** 失败次数 */
	private int failedTimes;
	/** 执行开始时间 */
	private long startTime;
	/** 执行完成的耗时 */
	private long execDuration;

	public ThreadTaskStat(String name, boolean isDone, int failedTimes,
                          long startTime, long execDuration) {
		super();
		this.name = name;
		this.isDone = isDone;
		this.failedTimes = failedTimes;
		this.startTime = startTime;
		this.execDuration = execDuration;
	}

	/**
	 * 获取线程任务名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 线程任务是否执行完成
	 */
	public boolean isDone() {
		return isDone;
	}

	/**
	 * 获取执行失败的次数
	 */
	public int getFailedTimes() {
		return failedTimes;
	}

	/**
	 * 获取执行开始时间，如果之前有失败过，则记录的是最近一次运行的开始时间
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * 获取执行完成消耗的时间，如果没有执行完成，则返回-1
	 */
	public long getExecDuration() {
		return execDuration;
	}

}

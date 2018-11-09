package com.zoomtech.emm.socket.threadtaskpool;

/**
 * TheadTask执行返回的结果
 * @author chenyi
 *
 */
public class Result {
	/**
	 * 结果类型
	 */
	private ResultType resultType;
	/**
	 * 对应的ThreadTask
	 */
	private ThreadTask threadTask;

	public Result(ResultType resultType, ThreadTask threadTask){
		this.resultType = resultType;
		this.threadTask = threadTask;
	}

	public ResultType getResultType() {
		return resultType;
	}

	public ThreadTask getThreadTask() {
		return threadTask;
	}


}

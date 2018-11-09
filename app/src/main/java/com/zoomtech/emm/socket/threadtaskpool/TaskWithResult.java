package com.zoomtech.emm.socket.threadtaskpool;

/**
 * 带返回结果的可执行任务接口
 * @author chenyi
 *
 */
public interface TaskWithResult {
	ResultType run();
}

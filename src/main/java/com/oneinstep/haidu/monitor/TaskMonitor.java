package com.oneinstep.haidu.monitor;

public interface TaskMonitor {
    void onTaskStart(String taskId);

    void onTaskComplete(String taskId, long duration);

    void onTaskError(String taskId, Throwable error);

    void onTaskTimeout(String taskId);

    // 性能指标
    void recordTaskDuration(String taskId, long duration);

    void recordTaskQueueTime(String taskId, long queueTime);
}
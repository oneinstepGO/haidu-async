package com.oneinstep.haidu.context;

import com.oneinstep.haidu.config.TaskConfig;
import com.oneinstep.haidu.core.AbstractTask;
import com.oneinstep.haidu.result.Result;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务执行上下文
 */
@Data
public class RequestContext {
    // 请求上下文参数
    private Map<String, Object> requestParam = new ConcurrentHashMap<>();
    // 任务配置
    private TaskConfig taskConfig;
    // 保存任务结果
    private Map<String, Result<?>> taskResultMap = new ConcurrentHashMap<>();

    // 任务引擎是否已启动
    private volatile boolean engineStarted = false;

    // 任务引擎是否已停止
    private volatile boolean engineStopped = false;

    /**
     * 运行时 task 类缓存，避免重复反射创建
     */
    private final Map<String, AbstractTask<?>> taskInstanceMap = new ConcurrentHashMap<>();

    /**
     * 清空任务实例缓存
     */
    public void clearTaskInstanceMap() {
        taskInstanceMap.clear();
    }

    /**
     * 获取任务实例
     *
     * @param taskId 任务ID
     * @return 任务实例
     */
    public AbstractTask<?> getTaskInstance(String taskId) {
        return taskInstanceMap.get(taskId);
    }
}

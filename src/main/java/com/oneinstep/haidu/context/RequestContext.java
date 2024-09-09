package com.oneinstep.haidu.context;

import com.oneinstep.haidu.config.TaskConfig;
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
    private Map<String, Object> requestParam;
    // 任务配置
    private TaskConfig taskConfig;
    // 保存任务结果
    private Map<String, Result> taskResultMap = new ConcurrentHashMap<>();

    private Map<String, String> taskClassNameMap;
}

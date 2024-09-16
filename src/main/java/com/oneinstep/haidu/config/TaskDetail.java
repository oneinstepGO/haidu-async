package com.oneinstep.haidu.config;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 任务详情
 */
@Data
public class TaskDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    // task 标识
    private String taskId;
    // 全限定类名
    private String fullClassName;
    // 重试次数
    private Integer retries;
    // 超时时间
    private Long timeout;
    // 原始任务参数
    private List<TaskParam> taskParams;
    // 处理后的任务参数
    private Map<String, Object> params;
}

package com.oneinstep.haidu.config;

import lombok.Data;

import java.util.Map;

/**
 * 任务详情
 */
@Data
public class TaskDetail {
    // task 标识
    private String taskId;
    // 全限定类名
    private String fullClassName;
    // 重试次数
    private Integer retries;
    // 超时时间
    private Long timeout;
    // 任务参数
    private Map<String, Object> params;
}

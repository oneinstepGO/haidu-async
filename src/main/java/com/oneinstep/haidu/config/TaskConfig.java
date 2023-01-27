package com.oneinstep.haidu.config;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 任务配置
 */
@Data
public class TaskConfig {
    // 编排规则
    private List<List<String>> arrangeRule;
    // 任务详情
    private Map<String, TaskDetail> taskDetailsMap;
}

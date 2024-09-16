package com.oneinstep.haidu.config;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 任务配置
 */
@Data
public class TaskConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    // 编排名称
    private String arrangeName;
    // 编排描述
    private String description;
    // 编排规则
    private List<List<String>> arrangeRule;
    // 任务详情
    private Map<String, TaskDetail> taskDetailsMap;
}

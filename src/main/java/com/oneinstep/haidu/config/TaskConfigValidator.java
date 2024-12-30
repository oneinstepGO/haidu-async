package com.oneinstep.haidu.config;

import java.util.List;
import java.util.Map;

public class TaskConfigValidator {

    // 验证任务配置的合法性
    public static void validate(TaskConfig config) {
        validateArrangeRules(config.getArrangeRule());
        validateTaskDetails(config.getTaskDetailsMap());
        validateParameters(config.getTaskDetailsMap());
        validateCircularDependencies(config);
    }

    private static void validateArrangeRules(List<List<String>> arrangeRule) {
    }

    private static void validateTaskDetails(Map<String, TaskDetail> taskDetailsMap) {
    }

    private static void validateParameters(Map<String, TaskDetail> taskDetailsMap) {
    }

    private static void validateCircularDependencies(TaskConfig config) {
    }
}

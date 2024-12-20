package com.oneinstep.haidu.config;

import org.apache.commons.collections4.CollectionUtils;

import java.io.Reader;
import java.util.List;

/**
 * 任务配置工厂
 */
public class TaskConfigFactory {

    /**
     * 任务定义读取器
     */
    private final TaskDefinitionReader reader;

    public TaskConfigFactory(TaskDefinitionReader reader) {
        this.reader = reader;
    }

    /**
     * 从任务描述符创建任务配置列表
     *
     * @param tasksDescriptor 任务描述符
     * @return 任务配置列表
     * @throws Exception 创建任务配置时发生的异常
     */
    public List<TaskConfig> createConfigs(Reader tasksDescriptor) throws Exception {
        List<TaskConfig> configs = reader.read(tasksDescriptor);

        if (CollectionUtils.isEmpty(configs)) {
            throw new IllegalArgumentException("No task config definitions found in the descriptor");
        }
        return configs;
    }

    /**
     * 从任务描述符创建单个任务配置
     *
     * @param tasksDescriptor 任务描述符
     * @return 任务配置
     * @throws Exception 创建任务配置时发生的异常
     */
    public TaskConfig createConfig(Reader tasksDescriptor) throws Exception {
        List<TaskConfig> taskConfigs = createConfigs(tasksDescriptor);
        if (CollectionUtils.isEmpty(taskConfigs)) {
            throw new IllegalArgumentException("No task config definitions found in the descriptor");
        }
        if (taskConfigs.size() != 1) {
            throw new IllegalArgumentException("Expected a single task config definition");
        }
        return taskConfigs.get(0);
    }

}

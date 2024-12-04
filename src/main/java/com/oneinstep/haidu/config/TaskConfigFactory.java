package com.oneinstep.haidu.config;

import org.apache.commons.collections4.CollectionUtils;

import java.io.Reader;
import java.util.List;

/**
 * 任务配置工厂
 */
public class TaskConfigFactory {

    private final TaskDefinitionReader reader;

    public TaskConfigFactory(TaskDefinitionReader reader) {
        this.reader = reader;
    }

    /**
     * Create a list of task configs from a task descriptor.
     *
     * @param tasksDescriptor
     * @return
     * @throws Exception
     */
    public List<TaskConfig> createConfigs(Reader tasksDescriptor) throws Exception {
        List<TaskConfig> configs = reader.read(tasksDescriptor);

        if (CollectionUtils.isEmpty(configs)) {
            throw new IllegalArgumentException("No task config definitions found in the descriptor");
        }
        return configs;
    }

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

package com.oneinstep.haidu.config;

import com.oneinstep.haidu.exception.TaskConfigReadException;

import java.io.Reader;
import java.util.List;

/**
 * 任务定义读取器
 */
@FunctionalInterface
public interface TaskDefinitionReader {

    /**
     * 读取任务配置
     *
     * @param reader 任务配置读取器
     * @return 任务配置列表
     * @throws TaskConfigReadException 读取任务配置时发生的异常
     */
    List<TaskConfig> read(Reader reader) throws TaskConfigReadException;

}

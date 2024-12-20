package com.oneinstep.haidu.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.oneinstep.haidu.exception.IllegalTaskConfigException;
import com.oneinstep.haidu.exception.TaskConfigReadException;
import lombok.extern.slf4j.Slf4j;

import java.io.Reader;
import java.util.List;

/**
 * JSON 任务定义读取器
 */
@Slf4j
public class JsonTaskDefinitionReader implements TaskDefinitionReader {

    @Override
    public List<TaskConfig> read(Reader reader) throws TaskConfigReadException {
        // 读取 JSON 字符串
        StringBuilder stringBuilder = new StringBuilder();
        int ch;
        try {
            while ((ch = reader.read()) != -1) {
                stringBuilder.append((char) ch);
            }
        } catch (Exception e) {
            log.error("读取任务配置文件失败", e);
            throw new TaskConfigReadException("读取任务配置文件失败: " + e.getMessage());
        }

        try {
            // 解析 JSON 字符串为 TaskConfig 列表
            return JSON.parseObject(stringBuilder.toString(), new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("解析任务配置文件失败", e);
            throw new IllegalTaskConfigException("Failed to parse task config: + " + e.getMessage());
        }

    }

}

package com.oneinstep.haidu.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.oneinstep.haidu.exception.IllegalTaskConfigException;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Yaml 任务定义读取器
 */
@Slf4j
public class YamlTaskDefinitionReader implements TaskDefinitionReader {
    private final Yaml yaml;

    /**
     * 创建一个新的 {@link YamlTaskDefinitionReader}
     */
    public YamlTaskDefinitionReader() {
        this(new Yaml());
    }

    /**
     * 创建一个新的 {@link YamlTaskDefinitionReader}
     *
     * @param yaml 用于读取配置定义的 Yaml 实例
     */
    public YamlTaskDefinitionReader(Yaml yaml) {
        this.yaml = yaml;
    }

    @Override
    public List<TaskConfig> read(Reader reader) {
        Map map = yaml.loadAs(reader, Map.class);
        if (map == null) {
            return Collections.emptyList();
        }

        Object o = map.get("asyncTasks");
        if (o == null) {
            return Collections.emptyList();
        }

        // 使用 JSON 解析，因为 YAML 会将所有对象读取为 LinkedHashMap
        try {
            return JSON.parseObject(JSON.toJSONString(o), new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("读取任务配置文件失败", e);
            throw new IllegalTaskConfigException("Failed to parse task config: + " + e.getMessage());
        }

    }

}

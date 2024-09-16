package com.oneinstep.haidu.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class YamlTaskDefinitionReader implements TaskDefinitionReader {
    private final Yaml yaml;

    /**
     * Create a new {@link YamlTaskDefinitionReader}.
     */
    public YamlTaskDefinitionReader() {
        this(new Yaml());
    }

    /**
     * Create a new {@link YamlTaskDefinitionReader}.
     *
     * @param yaml to use to read config definitions
     */
    public YamlTaskDefinitionReader(Yaml yaml) {
        this.yaml = yaml;
    }


    @Override
    public List<TaskConfig> read(Reader reader) throws Exception {
        Map map = yaml.loadAs(reader, Map.class);
        if (map == null) {
            return Collections.emptyList();
        }

        Object o = map.get("asyncTasks");
        if (o == null) {
            return Collections.emptyList();
        }

        // use json because yml read all object to LinkedHashMap
        try {
            return JSON.parseObject(JSON.toJSONString(o), new TypeReference<List<TaskConfig>>() {
            });
        }catch (Exception e) {
            log.error("Failed to parse task config", e);
        }

        return Collections.emptyList();
    }

}

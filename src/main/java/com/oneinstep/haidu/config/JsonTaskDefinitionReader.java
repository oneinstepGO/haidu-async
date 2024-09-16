package com.oneinstep.haidu.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.oneinstep.haidu.exception.IllegalTaskConfigException;

import java.io.Reader;
import java.util.List;

public class JsonTaskDefinitionReader implements TaskDefinitionReader {

    @Override
    public List<TaskConfig> read(Reader reader) throws Exception {
        // read string from reader
        StringBuilder stringBuilder = new StringBuilder();
        int ch;
        while ((ch = reader.read()) != -1) {
            stringBuilder.append((char) ch);
        }

        try {
            return JSON.parseObject(stringBuilder.toString(), new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalTaskConfigException("Failed to parse task config: + " + e.getMessage());
        }

    }


}

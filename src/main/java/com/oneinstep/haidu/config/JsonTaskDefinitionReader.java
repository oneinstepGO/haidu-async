package com.oneinstep.haidu.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

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
        // parse string to map
        return JSON.parseObject(stringBuilder.toString(), new TypeReference<>() {
        });

    }


}

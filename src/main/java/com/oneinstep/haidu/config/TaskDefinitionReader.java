package com.oneinstep.haidu.config;

import java.io.Reader;
import java.util.List;

@FunctionalInterface
public interface TaskDefinitionReader {

    List<TaskConfig> read(Reader reader) throws Exception;

}

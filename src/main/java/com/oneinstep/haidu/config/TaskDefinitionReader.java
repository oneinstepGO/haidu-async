package com.oneinstep.haidu.config;

import java.io.Reader;
import java.util.List;

@FunctionalInterface
public interface TaskDefinitionReader {

    /**
     * Read a list of rule definitions from a rule descriptor.
     *
     * <strong> The descriptor is expected to contain a collection of rule definitions
     * even for a single rule.</strong>
     *
     * @param reader of the rules descriptor
     * @return a list of rule definitions
     * @throws Exception if a problem occurs during rule definition reading
     */
    List<TaskConfig> read(Reader reader) throws Exception;

}

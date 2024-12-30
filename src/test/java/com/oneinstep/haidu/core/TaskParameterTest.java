package com.oneinstep.haidu.core;

import com.oneinstep.haidu.config.TaskParam;
import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.exception.IllegalTaskConfigException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TaskParameterTest {

    private TaskEngine taskEngine;
    private RequestContext context;

    @BeforeEach
    void setUp() {
        taskEngine = TaskEngine.getInstance(null);
        context = new RequestContext();
    }

    @ParameterizedTest
    @MethodSource("provideValidParameters")
    void shouldHandleValidParameters(TaskParam.Type type, String value, Object expected) {
        List<TaskParam> taskParams = new ArrayList<>();
        TaskParam param = createTaskParam("testParam", type, value, true);
        taskParams.add(param);

        Map<String, Object> result = taskEngine.handleTaskParams(taskParams, context);

        assertEquals(expected, result.get("testParam"));
    }

    private static Stream<Arguments> provideValidParameters() {
        return Stream.of(
                Arguments.of(TaskParam.Type.STRING, "test", "test"),
                Arguments.of(TaskParam.Type.INT, "123", 123),
                Arguments.of(TaskParam.Type.LONG, "123456789", 123456789L),
                Arguments.of(TaskParam.Type.DOUBLE, "123.456", 123.456),
                Arguments.of(TaskParam.Type.BOOLEAN, "true", true),
                Arguments.of(TaskParam.Type.LIST, "a,b,c", List.of("a", "b", "c")),
                Arguments.of(TaskParam.Type.MAP, "key1:value1,key2:value2",
                        Map.of("key1", "value1", "key2", "value2")),
                Arguments.of(TaskParam.Type.JSON, "{\"key\":\"value\"}",
                        Map.of("key", "value")),
                Arguments.of(TaskParam.Type.JSON_ARRAY, "[1,2,3]", List.of(1, 2, 3))
        );
    }

    @Test
    void shouldHandleEmptyOptionalParameter() {
        List<TaskParam> taskParams = new ArrayList<>();
        taskParams.add(createTaskParam("optional", TaskParam.Type.STRING, "", false));

        Map<String, Object> result = taskEngine.handleTaskParams(taskParams, context);

        assertFalse(result.containsKey("optional"));
    }

    @Test
    void shouldThrowExceptionForInvalidJson() {
        List<TaskParam> taskParams = new ArrayList<>();
        taskParams.add(createTaskParam("json", TaskParam.Type.JSON, "{invalid}", true));

        assertThrows(IllegalTaskConfigException.class, () ->
                taskEngine.handleTaskParams(taskParams, context));
    }

    @Test
    void shouldThrowExceptionForInvalidNumber() {
        List<TaskParam> taskParams = new ArrayList<>();
        taskParams.add(createTaskParam("number", TaskParam.Type.INT, "not_a_number", true));

        assertThrows(IllegalTaskConfigException.class, () ->
                taskEngine.handleTaskParams(taskParams, context));
    }

    @Test
    void shouldThrowExceptionForMissingRequiredParameter() {
        List<TaskParam> taskParams = new ArrayList<>();
        taskParams.add(createTaskParam("required", TaskParam.Type.STRING, "", true));

        assertThrows(IllegalTaskConfigException.class, () ->
                taskEngine.handleTaskParams(taskParams, context));
    }

    private TaskParam createTaskParam(String name, TaskParam.Type type, String value, boolean required) {
        TaskParam param = new TaskParam();
        param.setName(name);
        param.setType(type);
        param.setValue(value);
        param.setRequired(required);
        return param;
    }
} 
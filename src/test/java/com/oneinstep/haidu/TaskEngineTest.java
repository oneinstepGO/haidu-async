package com.oneinstep.haidu;

import com.oneinstep.haidu.config.TaskParam;
import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.core.TaskEngine;
import com.oneinstep.haidu.exception.IllegalTaskConfigException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TaskEngineTest {

    private TaskEngine taskEngine;
    private RequestContext context;

    @BeforeEach
    void setUp() {
        taskEngine = TaskEngine.getInstance(null);
        context = new RequestContext();
    }

    @Test
    void handleTaskParams_shouldHandleStringType() {
        List<TaskParam> taskParams = new ArrayList<>();
        taskParams.add(createTaskParam(TaskParam.Type.STRING, "value1"));

        Map<String, Object> params = taskEngine.handleTaskParams(taskParams, context);

        assertEquals("value1", params.get("param1"));
    }

    @Test
    void handleTaskParams_shouldHandleIntType() {
        List<TaskParam> taskParams = new ArrayList<>();
        taskParams.add(createTaskParam(TaskParam.Type.INT, "123"));

        Map<String, Object> params = taskEngine.handleTaskParams(taskParams, context);

        assertEquals(123, params.get("param1"));
    }

    @Test
    void handleTaskParams_shouldHandleBooleanType() {
        List<TaskParam> taskParams = new ArrayList<>();
        taskParams.add(createTaskParam(TaskParam.Type.BOOLEAN, "true"));

        Map<String, Object> params = taskEngine.handleTaskParams(taskParams, context);

        assertEquals(true, params.get("param1"));
    }

    @Test
    void handleTaskParams_shouldHandleListType() {
        List<TaskParam> taskParams = new ArrayList<>();
        taskParams.add(createTaskParam(TaskParam.Type.LIST, "a,b,c"));

        Map<String, Object> params = taskEngine.handleTaskParams(taskParams, context);

        List<String> expected = Arrays.asList("a", "b", "c");
        assertEquals(expected, params.get("param1"));
    }

    @Test
    void handleTaskParams_shouldHandleMapType() {
        List<TaskParam> taskParams = new ArrayList<>();
        taskParams.add(createTaskParam(TaskParam.Type.MAP, "key1:value1,key2:value2"));

        Map<String, Object> params = taskEngine.handleTaskParams(taskParams, context);

        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");

        assertEquals(expected, params.get("param1"));
    }

    @Test
    void handleTaskParams_shouldHandleJsonType() {
        List<TaskParam> taskParams = new ArrayList<>();
        taskParams.add(createTaskParam(TaskParam.Type.JSON, "{\"key\":\"value\"}"));

        Map<String, Object> params = taskEngine.handleTaskParams(taskParams, context);

        Map<String, Object> expected = new HashMap<>();
        expected.put("key", "value");

        assertEquals(expected, params.get("param1"));
    }

    @Test
    void handleTaskParams_shouldHandleJsonArrayType() {
        List<TaskParam> taskParams = new ArrayList<>();
        taskParams.add(createTaskParam(TaskParam.Type.JSON_ARRAY, "[\"value1\",\"value2\"]"));

        Map<String, Object> params = taskEngine.handleTaskParams(taskParams, context);

        List<String> expected = new ArrayList<>();
        expected.add("value1");
        expected.add("value2");

        assertEquals(expected, params.get("param1"));
    }

    @Test
    void handleTaskParams_shouldHandleContextType() {
        List<TaskParam> taskParams = new ArrayList<>();
        taskParams.add(createTaskParam(TaskParam.Type.CONTEXT, "#(contextKey)#"));

        // Set the context value
        context.getRequestParam().put("contextKey", "contextValue");

        Map<String, Object> params = taskEngine.handleTaskParams(taskParams, context);

        assertEquals("#(contextKey)#", params.get("param1"));
    }

    @Test
    void handleTaskParams_shouldThrowExceptionForRequiredParam() {
        List<TaskParam> taskParams = new ArrayList<>();
        taskParams.add(createTaskParam(TaskParam.Type.STRING, ""));

        IllegalTaskConfigException exception = assertThrows(IllegalTaskConfigException.class, () -> {
            taskEngine.handleTaskParams(taskParams, context);
        });

        assertTrue(exception.getMessage().contains("task param is required, but value is empty"));
    }

    private TaskParam createTaskParam(TaskParam.Type type, String value) {
        TaskParam taskParam = new TaskParam();
        taskParam.setName("param1");
        taskParam.setType(type);
        taskParam.setValue(value);
        taskParam.setRequired(true);
        return taskParam;
    }


    @Test
    void test() {
        Map<String, Object> params = new HashMap<>();
        params.put("key", "#(key_in_request)#");
        Map<String, Object> requestParam = new HashMap<>();
        requestParam.put("key_in_request", "value_in_request");
        params.forEach((key, value) -> {
            if (value instanceof String && ((String) value).startsWith("#(") && ((String) value).endsWith(")#")) {
                params.put(key, requestParam.get(((String) value).substring(2, ((String) value).length() - 2)));
            }
        });
        assertEquals("value_in_request", params.get("key"));
    }
}

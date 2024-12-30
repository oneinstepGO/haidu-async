package com.oneinstep.haidu.integration;

import com.oneinstep.haidu.config.JsonTaskDefinitionReader;
import com.oneinstep.haidu.config.TaskConfig;
import com.oneinstep.haidu.config.TaskConfigFactory;
import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.core.TaskEngine;
import com.oneinstep.haidu.result.Result;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 策四 Context 类型参数
 */
class TaskParameterIntegrationTest {

    @Test
    void shouldExecuteTaskWithParameters() throws Exception {
        // 准备配置
        String config = """
                {
                  "arrangeName": "test",
                  "description": "Parameter test",
                  "arrangeRule": [
                    ["paramTask"]
                  ],
                  "taskDetailsMap": {
                    "paramTask": {
                      "taskId": "paramTask",
                      "fullClassName": "com.oneinstep.haidu.task.ParameterTestTask",
                      "taskParams": [
                        {
                          "name": "stringParam",
                          "type": "STRING",
                          "value": "test",
                          "required": true
                        },
                        {
                          "name": "numberParam",
                          "type": "INT",
                          "value": "42",
                          "required": true
                        },
                        {
                          "name": "contextParam",
                          "type": "CONTEXT",
                          "value": "#(testKey)#",
                          "required": true
                        }
                      ]
                    }
                  }
                }
                """;

        // 创建上下文
        RequestContext context = new RequestContext();
        context.getRequestParam().put("testKey", "contextValue");

        // 配置任务
        TaskConfigFactory factory = new TaskConfigFactory(new JsonTaskDefinitionReader());
        TaskConfig taskConfig = factory.createConfig(new StringReader(config));
        context.setTaskConfig(taskConfig);

        // 执行任务
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        TaskEngine engine = TaskEngine.getInstance(executorService);

        try {
            engine.startEngine(context);

            // 验证结果
            Map<String, Result<?>> results = context.getTaskResultMap();
            Result<?> result = results.get("paramTask");

            assertNotNull(result);
            assertTrue(result.success());
            assertEquals("test-42-contextValue", result.getData());
        } finally {
            executorService.shutdown();
        }
    }
} 
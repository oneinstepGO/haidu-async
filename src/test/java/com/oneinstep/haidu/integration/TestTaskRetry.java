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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 测试任务重试
 */
public class TestTaskRetry {

    @Test
    void shouldHandleTaskRetry() throws Exception {
        // 准备配置
        String config = """
                {
                  "arrangeName": "retry-test",
                  "description": "Retry test",
                  "arrangeRule": [
                    ["retryTask"]
                  ],
                  "taskDetailsMap": {
                    "retryTask": {
                      "taskId": "retryTask",
                      "retries": 2,
                      "fullClassName": "com.oneinstep.haidu.task.RetryTask",
                      "taskParams": [
                
                      ]
                    }
                  }
                }
                """;

        // 创建上下文
        RequestContext context = new RequestContext();

        // 配置任务
        TaskConfigFactory factory = new TaskConfigFactory(new JsonTaskDefinitionReader());
        TaskConfig taskConfig = factory.createConfig(new StringReader(config));
        context.setTaskConfig(taskConfig);

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        TaskEngine engine = TaskEngine.getInstance(executorService);

        engine.startEngine(context);

        // 验证结果
        Map<String, Result<?>> results = context.getTaskResultMap();
        Result<?> result = results.get("retryTask");

        // 验证结果
        assertNotNull(result);
        assertEquals("success-after-retry", result.getData());
    }

}

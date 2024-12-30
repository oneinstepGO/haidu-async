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

import static org.junit.jupiter.api.Assertions.assertNull;

class TestTaskTimeout {

    // 添加更多测试场景
    @Test
    void shouldHandleTaskTimeout() throws Exception {
        // 准备配置
        String config = """
                {
                  "arrangeName": "test-timeout",
                  "description": "Task timeout test",
                  "arrangeRule": [
                    ["timeOutTask"]
                  ],
                  "taskDetailsMap": {
                    "timeOutTask": {
                      "taskId": "timeOutTask",
                      "fullClassName": "com.oneinstep.haidu.task.TimeOutTask",
                      "timeout": 1000,
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

        // 执行任务
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        TaskEngine engine = TaskEngine.getInstance(executorService);

        engine.startEngine(context);

        // 验证结果
        Map<String, Result<?>> results = context.getTaskResultMap();
        Result<?> result = results.get("timeOutTask");

        // 验证结果
        assertNull(result);

    }

    @Test
    void shouldHandleCircularDependencies() {
    }

    @Test
    void shouldHandleConcurrentTaskExecution() {
    }

}

package com.oneinstep.haidu;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.oneinstep.haidu.config.TaskConfig;
import com.oneinstep.haidu.config.TaskConfigFactory;
import com.oneinstep.haidu.config.YamlTaskDefinitionReader;
import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.core.TaskEngine;
import com.oneinstep.haidu.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class TestDefaultTaskEngineYaml {
    private TaskConfig taskConfig;
    private ExecutorService executorService;

    @BeforeEach
    public void init() {

        String fileName = Objects.requireNonNull(getClass().getClassLoader().getResource("config/demo.task.config.yml")).getFile();

        TaskConfigFactory factory = new TaskConfigFactory(new YamlTaskDefinitionReader());
        try {
            this.taskConfig = factory.createConfig(new FileReader(fileName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.executorService = new ThreadPoolExecutor(
                20,
                20,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                new ThreadFactoryBuilder().setNameFormat("taskThreadPool-%d").build(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    @Test
    void testAsync() {
        RequestContext requestContext = new RequestContext();
        long start = System.currentTimeMillis();
        requestContext.setTaskConfig(this.taskConfig);
        TaskEngine taskEngine = TaskEngine.getInstance(executorService);
        taskEngine.startEngine(requestContext);

        long end = System.currentTimeMillis();
        log.info("cost time:{}", end - start);
        assertResult(requestContext);
    }

    @Test
    void testAsyncAgain() {
        RequestContext requestContext = new RequestContext();
        long start = System.currentTimeMillis();
        requestContext.setTaskConfig(this.taskConfig);
        TaskEngine taskEngine = TaskEngine.getInstance(executorService);
        taskEngine.startEngine(requestContext);

        assertThrows(IllegalStateException.class, () -> {
            // 重复启动
            taskEngine.startEngine(requestContext);
        }, "任务引擎已经启动!");

        long end = System.currentTimeMillis();
        log.info("cost time:{}", end - start);
        assertResult(requestContext);
    }

    private static void assertResult(RequestContext requestContext) {
        Map<String, Result<?>> taskResultMap = requestContext.getTaskResultMap();
        Result<?> result1 = taskResultMap.get("1");
        Result<?> result2 = taskResultMap.get("2");
        Result<?> result3 = taskResultMap.get("3");
        Result<?> result1001 = taskResultMap.get("1001");
        Result<?> result1002 = taskResultMap.get("1002");
        Result<?> result1003 = taskResultMap.get("1003");
        Result<?> result1004 = taskResultMap.get("1004");
        Result<?> result1005 = taskResultMap.get("1005");
        Result<?> result1006 = taskResultMap.get("1006");
        Result<?> result9998 = taskResultMap.get("9998");
        Result<?> result9999 = taskResultMap.get("9999");
        assertEquals("DATA:1", result1.getData(), "task1 数据不对");
        assertEquals("DATA:2", result2.getData(), "task2 数据不对");
        assertEquals("[DATA:1,DATA:2] -> DATA:3", result3.getData(), "task3 数据不对");
        assertEquals("[DATA:1,DATA:2] -> DATA:1001", result1001.getData(), "task1001 数据不对");
        assertEquals("[DATA:1,DATA:2] -> DATA:1002", result1002.getData(), "task1002 数据不对");
        assertEquals("[[DATA:1,DATA:2] -> DATA:1001] -> DATA:1003", result1003.getData(), "task1003 数据不对");
        assertEquals("[[DATA:1,DATA:2] -> DATA:1001,[DATA:1,DATA:2] -> DATA:1002] -> DATA:1004", result1004.getData(), "task1004 数据不对");
        assertEquals("[[DATA:1,DATA:2] -> DATA:1002] -> DATA:1005", result1005.getData(), "task1005 数据不对");
        assertEquals("[[[DATA:1,DATA:2] -> DATA:1001] -> DATA:1003,[[DATA:1,DATA:2] -> " +
                "DATA:1001,[DATA:1,DATA:2] -> DATA:1002] -> DATA:1004,[[DATA:1,DATA:2] -> DATA:1002] -> DATA:1005] -> DATA:1006", result1006.getData(), "task1006 数据不对");
        assertEquals("[[[[DATA:1,DATA:2] -> DATA:1001] -> DATA:1003,[[DATA:1,DATA:2] -> DATA:1001,[DATA:1,DATA:2] -> DATA:1002] " +
                "-> DATA:1004,[[DATA:1,DATA:2] -> DATA:1002] -> DATA:1005] -> DATA:1006] -> DATA:9998", result9998.getData(), "result9998 数据不对");
        assertEquals("[[[[DATA:1,DATA:2] -> DATA:1001] -> DATA:1003,[[DATA:1,DATA:2] -> DATA:1001,[DATA:1,DATA:2] -> DATA:1002] " +
                "-> DATA:1004,[[DATA:1,DATA:2] -> DATA:1002] -> DATA:1005] -> DATA:1006] -> DATA:9999", result9999.getData(), "task9999 数据不对");
    }

}

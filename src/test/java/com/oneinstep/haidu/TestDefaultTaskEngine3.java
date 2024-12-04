package com.oneinstep.haidu;


import com.alibaba.fastjson2.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.oneinstep.haidu.config.TaskConfig;
import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.core.TaskEngine;
import com.oneinstep.haidu.result.Result;
import com.oneinstep.haidu.task.Task1;
import com.oneinstep.haidu.task2.Task9999;
import com.oneinstep.haidu.task2.TaskA;
import com.oneinstep.haidu.task2.TaskB;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class TestDefaultTaskEngine3 {
    private TaskConfig taskConfig;
    private ExecutorService executorService;

    @BeforeEach
    public void init() {

        String valueStr = null;
        try {
            String demoConfigFilePath = "/config/demo.task.config3.json";
            valueStr = IOUtils.toString(Objects.requireNonNull(TestDefaultTaskEngine3.class.getResourceAsStream(demoConfigFilePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("read demo task config from file error....", e);
        }

        this.taskConfig = JSONObject.parseObject(valueStr, TaskConfig.class);
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
    public void testAsync() {
        RequestContext requestContext = new RequestContext();
        long start = System.currentTimeMillis();
        requestContext.setTaskConfig(this.taskConfig);
        TaskEngine taskEngine = TaskEngine.getInstance(executorService);
        taskEngine.startEngine(requestContext);

        long end = System.currentTimeMillis();
        log.info("cost time:{}", end - start);
        assertResult(requestContext);
    }

    private static void assertResult(RequestContext requestContext) {
        Map<String, Result> taskResultMap = requestContext.getTaskResultMap();
        Result result1 = taskResultMap.get("2-1");
        Result result1001 = taskResultMap.get("2-1001");
        Result result1002 = taskResultMap.get("2-1002");
        Result result1003 = taskResultMap.get("2-1003");
        Result result1004 = taskResultMap.get("2-1004");
        Result result1005 = taskResultMap.get("2-1005");

        Result result9999 = taskResultMap.get("2-9999");
        assertEquals("DATA:2-1", result1.getData(), "task1 数据不对");
        assertEquals("DATA:2-1 -> DATA:2-1001", result1001.getData(), "task1001 数据不对");
        assertEquals("DATA:2-1 -> DATA:2-1002", result1002.getData(), "task1002 数据不对");
        assertEquals("DATA:2-1 -> DATA:2-1003", result1003.getData(), "task1003 数据不对");
        assertEquals("[DATA:2-1 -> DATA:2-1001,DATA:2-1 -> DATA:2-1002,DATA:2-1 -> DATA:2-1003] -> DATA:2-1004", result1004.getData(), "task1004 数据不对");
        assertEquals("[DATA:2-1 -> DATA:2-1001,DATA:2-1 -> DATA:2-1002,DATA:2-1 -> DATA:2-1003] -> DATA:2-1005", result1005.getData(), "task1005 数据不对");
        assertEquals("[[DATA:2-1 -> DATA:2-1001,DATA:2-1 -> DATA:2-1002,DATA:2-1 -> DATA:2-1003] -> DATA:2-1004,[DATA:2-1 -> DATA:2-1001,DATA:2-1 -> DATA:2-1002,DATA:2-1 -> DATA:2-1003] -> DATA:2-1005] -> DATA:2-9999", result9999.getData(), "task9999 数据不对");
    }

    @Test
    public void execOneByOne() {
        RequestContext requestContext = new RequestContext();
        long start = System.currentTimeMillis();

        Task1 task1 = new Task1();
        task1.setTaskId("2-1");
        task1.accept(requestContext);
        task1.setParams(Map.of("param1", "value1"));
        task1.accept(requestContext);

        TaskA task1001 = new TaskA();
        task1001.setTaskId("2-1001");
        task1001.setParams(Map.of("useId", "1001"));
        task1001.accept(requestContext);

        TaskA task1002 = new TaskA();
        task1002.setTaskId("2-1002");
        task1002.setParams(Map.of("useId", "1002"));
        task1002.accept(requestContext);

        TaskA task1003 = new TaskA();
        task1003.setTaskId("2-1003");
        task1003.setParams(Map.of("useId", "1003"));
        task1003.accept(requestContext);

        TaskB task1004 = new TaskB();
        task1004.setTaskId("2-1004");
        task1004.setParams(Map.of("pool", "today"));
        task1004.accept(requestContext);

        TaskB task1005 = new TaskB();
        task1005.setTaskId("2-1005");
        task1005.setParams(Map.of("type", "all"));
        task1005.accept(requestContext);

        Task9999 task9999 = new Task9999();
        task9999.setTaskId("2-9999");
        task9999.accept(requestContext);

        long end = System.currentTimeMillis();
        log.info("cost time:{}", end - start);

        assertResult(requestContext);
    }
}

package com.oneinstep.haidu;


import com.alibaba.fastjson2.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.oneinstep.haidu.config.TaskConfig;
import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.core.TaskEngine;
import com.oneinstep.haidu.result.Result;
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
public class TestDefaultTaskEngine5 {
    private TaskConfig taskConfig;
    private ExecutorService executorService;

    @BeforeEach
    public void init() {

        String valueStr = null;
        try {
            String demoConfigFilePath = "/config/demo.task.config5.json";
            valueStr = IOUtils.toString(Objects.requireNonNull(TestDefaultTaskEngine5.class.getResourceAsStream(demoConfigFilePath)), StandardCharsets.UTF_8);
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


        Result result2001 = taskResultMap.get("2-2001");
        Result result2002 = taskResultMap.get("2-2002");
        Result result2003 = taskResultMap.get("2-2003");
        Result result2004 = taskResultMap.get("2-2004");
        Result result2005 = taskResultMap.get("2-2005");

        assertEquals("DATA:2-1 -> DATA:2-2001", result2001.getData(), "task2001 数据不对");
        assertEquals("DATA:2-1 -> DATA:2-2002", result2002.getData(), "task2002 数据不对");
        assertEquals("DATA:2-1 -> DATA:2-2003", result2003.getData(), "task2003 数据不对");
        assertEquals("[DATA:2-1 -> DATA:2-2001,DATA:2-1 -> DATA:2-2002,DATA:2-1 -> DATA:2-2003] -> DATA:2-2004", result2004.getData(), "task2004 数据不对");
        assertEquals("[DATA:2-1 -> DATA:2-2001,DATA:2-1 -> DATA:2-2002,DATA:2-1 -> DATA:2-2003] -> DATA:2-2005", result2005.getData(), "task2005 数据不对");
    }

}

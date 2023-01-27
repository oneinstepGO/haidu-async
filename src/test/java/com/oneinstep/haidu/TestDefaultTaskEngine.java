package com.oneinstep.haidu;


import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.oneinstep.haidu.config.TaskConfig;
import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.core.TaskEngine;
import com.oneinstep.haidu.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TestDefaultTaskEngine {
    private TaskConfig taskConfig;
    private ExecutorService executorService;

    @Before
    public void init() {

        String valueStr = null;
        try {
            String demoConfigFilePath = "/config/demo.task.config.json";
            valueStr = IOUtils.toString(Objects.requireNonNull(TestDefaultTaskEngine.class.getResourceAsStream(demoConfigFilePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("read demo task config from file error....", e);
        }

        this.taskConfig = JSONObject.parseObject(valueStr, TaskConfig.class);
        this.executorService =   new ThreadPoolExecutor(
                20,
                20,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                new ThreadFactoryBuilder().setNameFormat("taskThreadPool-%d").build(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    @Test
    public void test1() {
        RequestContext requestContext = new RequestContext();
        requestContext.setTaskConfig(this.taskConfig);
        TaskEngine taskEngine = TaskEngine.getInstance(executorService);
        taskEngine.taskArrangeAndExec(requestContext);

        Map<String, Result> taskResultMap = requestContext.getTaskResultMap();
        Result result1 = taskResultMap.get("1");
        Result result2 = taskResultMap.get("2");
        Result result1001 = taskResultMap.get("1001");
        Result result1002 = taskResultMap.get("1002");
        Result result1003 = taskResultMap.get("1003");
        Result result1004 = taskResultMap.get("1004");
        Result result1005 = taskResultMap.get("1005");
        Result result1006 = taskResultMap.get("1006");
        Result result9999 = taskResultMap.get("9999");
        Assert.assertEquals("task1 数据不对", "DATA:1", result1.getData());
        Assert.assertEquals("task2 数据不对", "DATA:2", result2.getData());
        Assert.assertEquals("task1001 数据不对", "[DATA:1,DATA:2] -> DATA:1001", result1001.getData());
        Assert.assertEquals("task1002 数据不对", "[DATA:1,DATA:2] -> DATA:1002", result1002.getData());
        Assert.assertEquals("task1003 数据不对", "[[DATA:1,DATA:2] -> DATA:1001] -> DATA:1003", result1003.getData());
        Assert.assertEquals("task1004 数据不对", "[[DATA:1,DATA:2] -> DATA:1001,[DATA:1,DATA:2] -> DATA:1002] -> DATA:1004", result1004.getData());
        Assert.assertEquals("task1005 数据不对", "[[DATA:1,DATA:2] -> DATA:1002] -> DATA:1005", result1005.getData());
        Assert.assertEquals("task1006 数据不对", "[[[DATA:1,DATA:2] -> DATA:1001] -> DATA:1003,[[DATA:1,DATA:2] -> " +
                "DATA:1001,[DATA:1,DATA:2] -> DATA:1002] -> DATA:1004,[[DATA:1,DATA:2] -> DATA:1002] -> DATA:1005] -> DATA:1006", result1006.getData());
        Assert.assertEquals("task9999 数据不对", "[[[[DATA:1,DATA:2] -> DATA:1001] -> DATA:1003,[[DATA:1,DATA:2] -> DATA:1001,[DATA:1,DATA:2] -> DATA:1002] " +
                "-> DATA:1004,[[DATA:1,DATA:2] -> DATA:1002] -> DATA:1005] -> DATA:1006] -> DATA:9999", result9999.getData());
    }
}

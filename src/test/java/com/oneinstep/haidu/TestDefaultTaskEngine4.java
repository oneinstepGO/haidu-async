package com.oneinstep.haidu;


import com.alibaba.fastjson2.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.oneinstep.haidu.config.TaskConfig;
import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.core.TaskEngine;
import com.oneinstep.haidu.exception.IllegalTaskConfigException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class TestDefaultTaskEngine4 {
    private TaskConfig taskConfig;
    private ExecutorService executorService;

    @BeforeEach
    public void init() {

        String valueStr = null;
        try {
            String demoConfigFilePath = "/config/demo.task.config4.json";
            valueStr = IOUtils.toString(Objects.requireNonNull(TestDefaultTaskEngine4.class.getResourceAsStream(demoConfigFilePath)), StandardCharsets.UTF_8);
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

        assertThrows(IllegalTaskConfigException.class, () -> {
            taskEngine.startEngine(requestContext);
        }, "检测到循环依赖！");

        long end = System.currentTimeMillis();
        log.info("cost time:{}", end - start);
    }

}

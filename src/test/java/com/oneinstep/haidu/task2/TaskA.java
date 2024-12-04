package com.oneinstep.haidu.task2;

import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.core.AbstractTask;
import com.oneinstep.haidu.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class TaskA extends AbstractTask<String> {
    @Override
    protected Result<String> invoke(RequestContext requestContext) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        try {
            Thread.sleep(100 + random.nextInt(20));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("the params of taskA is: {}", getParams());
        String data;
        if ("2-1001".equals(this.getTaskId()) || "2-1002".equals(this.getTaskId()) || "2-1003".equals(this.getTaskId())) {
            Result result1 = requestContext.getTaskResultMap().get("2-1");
            data = result1.getData() + " -> DATA:" + this.getTaskId();
        } else {
            Result result1001 = requestContext.getTaskResultMap().get("2-1001");
            Result result1002 = requestContext.getTaskResultMap().get("2-1002");
            Result result1003 = requestContext.getTaskResultMap().get("2-1003");
            data = "[" + result1001.getData() + "," + result1002.getData() + "," + result1003.getData() + "]" + " -> DATA:" + this.getTaskId();
        }
        return Result.success(data);
    }

    @Override
    protected void beforeInvoke(RequestContext requestContext) {

    }

    @Override
    protected void afterInvoke(RequestContext requestContext) {

    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}

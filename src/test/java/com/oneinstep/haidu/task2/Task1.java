package com.oneinstep.haidu.task2;

import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.core.AbstractTask;
import com.oneinstep.haidu.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class Task1 extends AbstractTask<String> {
    @Override
    protected Result<String> invoke(RequestContext requestContext) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        try {
            Thread.sleep(100 + random.nextInt(20));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("the params of task1 is: {}", getParams());
        return Result.success("DATA:" + this.getTaskId());
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
package com.oneinstep.haidu.task5;

import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.core.AbstractTask;
import com.oneinstep.haidu.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class TaskB extends AbstractTask<String> {
    @Override
    protected Result<String> invoke(RequestContext requestContext) {
        Result<?> result2001 = requestContext.getTaskResultMap().get("2-2001");
        Result<?> result2002 = requestContext.getTaskResultMap().get("2-2002");
        Result<?> result2003 = requestContext.getTaskResultMap().get("2-2003");
        ThreadLocalRandom random = ThreadLocalRandom.current();
        try {
            Thread.sleep(100 + random.nextInt(20));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("the params of taskB is: {}", getParams());
        return Result.success("[" + result2001.getData() + "," + result2002.getData() + "," + result2003.getData() + "]" + " -> DATA:" + this.getTaskId());
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

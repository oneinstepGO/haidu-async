package com.oneinstep.haidu.task2;

import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.core.AbstractTask;
import com.oneinstep.haidu.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class Task9999 extends AbstractTask<String> {
    @Override
    protected Result<String> invoke(RequestContext requestContext) {
        Result<?> result1004 = requestContext.getTaskResultMap().get("2-1004");
        Result<?> result1005 = requestContext.getTaskResultMap().get("2-1005");
        ThreadLocalRandom random = ThreadLocalRandom.current();
        try {
            Thread.sleep(random.nextInt(80, 100));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("the params of task9999 is: {}", getParams());
        return Result.success("[" + result1004.getData() + "," + result1005.getData() + "]" + " -> DATA:" + this.getTaskId());
    }

    @Override
    protected void beforeInvoke(RequestContext requestContext) {
        log.info("before invoke: 2-1005->{}", requestContext.getTaskResultMap().get("2-1005"));
    }

    @Override
    protected void afterInvoke(RequestContext requestContext) {

    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}

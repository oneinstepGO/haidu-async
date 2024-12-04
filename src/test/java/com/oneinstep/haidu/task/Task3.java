package com.oneinstep.haidu.task;

import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.core.AbstractTask;
import com.oneinstep.haidu.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class Task3 extends AbstractTask<String> {
    @Override
    protected Result<String> invoke(RequestContext requestContext) {
        Result result1 = requestContext.getTaskResultMap().get("1");
        Result result2 = requestContext.getTaskResultMap().get("2");
        ThreadLocalRandom random = ThreadLocalRandom.current();
        try {
            Thread.sleep(100 + random.nextInt(20));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Result.success("[" + result1.getData() + "," + result2.getData() + "]" + " -> DATA:" + this.getTaskId());
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

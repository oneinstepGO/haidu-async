package com.oneinstep.haidu.task;

import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.core.AbstractTask;
import com.oneinstep.haidu.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class Task1003 extends AbstractTask<String> {
    @Override
    protected Result<String> invoke(RequestContext requestContext) {
        Result<?> result1001 = requestContext.getTaskResultMap().get("1001");
        ThreadLocalRandom random = ThreadLocalRandom.current();
        try {
            Thread.sleep(300 + random.nextInt(50));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Result.success("[" + result1001.getData() + "]" + " -> DATA:" + this.getTaskId());
    }

    @Override
    protected void beforeInvoke(RequestContext requestContext) {
        log.info("before invoke: Task1001->{}", requestContext.getTaskResultMap().get("1001"));
    }

    @Override
    protected void afterInvoke(RequestContext requestContext) {

    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}

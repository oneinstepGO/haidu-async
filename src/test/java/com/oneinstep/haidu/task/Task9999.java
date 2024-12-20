package com.oneinstep.haidu.task;

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
        Result<?> result1006 = requestContext.getTaskResultMap().get("1006");
        ThreadLocalRandom random = ThreadLocalRandom.current();
        try {
            Thread.sleep(random.nextInt(80, 100));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Result.success("[" + result1006.getData() + "]" + " -> DATA:" + this.getTaskId());
    }

    @Override
    protected void beforeInvoke(RequestContext requestContext) {
        log.info("before invoke: Task1006->{}", requestContext.getTaskResultMap().get("1006"));
    }

    @Override
    protected void afterInvoke(RequestContext requestContext) {

    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}

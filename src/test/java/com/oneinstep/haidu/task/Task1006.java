package com.oneinstep.haidu.task;

import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.core.AbstractTask;
import com.oneinstep.haidu.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class Task1006 extends AbstractTask<String> {
    @Override
    protected Result<String> invoke(RequestContext requestContext) {
        Result result1003 = requestContext.getTaskResultMap().get("1003");
        Result result1004 = requestContext.getTaskResultMap().get("1004");
        Result result1005 = requestContext.getTaskResultMap().get("1005");
        ThreadLocalRandom random = ThreadLocalRandom.current();
        try {
            Thread.sleep(200 + random.nextInt(50));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Result.success("[" + result1003.getData() + "," + result1004.getData() + "," + result1005.getData() + "]" + " -> DATA:" + this.getTaskId());
    }

    @Override
    protected void beforeInvoke(RequestContext requestContext) {
        log.info("before invoke: Task1003->{}", requestContext.getTaskResultMap().get("1003"));
        log.info("before invoke: Task1004->{}", requestContext.getTaskResultMap().get("1004"));
        log.info("before invoke: Task1005->{}", requestContext.getTaskResultMap().get("1005"));
    }

    @Override
    protected void afterInvoke(RequestContext requestContext) {

    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}

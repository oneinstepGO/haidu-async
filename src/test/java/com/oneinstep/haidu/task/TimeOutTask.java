package com.oneinstep.haidu.task;

import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.core.AbstractTask;
import com.oneinstep.haidu.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class TimeOutTask extends AbstractTask<String> {

    @Override
    protected Result<String> invoke(RequestContext requestContext) {
        try {
            Thread.sleep(getTimeout() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.success("data");
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

package com.oneinstep.haidu.task;

import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.core.AbstractTask;
import com.oneinstep.haidu.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class RetryTask extends AbstractTask<String> {

    private volatile int index = 0;

    @Override
    protected void afterInvoke(RequestContext requestContext) {

    }

    @Override
    protected void beforeInvoke(RequestContext requestContext) {

    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected Result<String> invoke(RequestContext requestContext) {
        // mock 异常
        if (index == 0) {
            index++;
            throw new RuntimeException("mock exception");
        }
        return Result.success("success-after-retry");
    }

}

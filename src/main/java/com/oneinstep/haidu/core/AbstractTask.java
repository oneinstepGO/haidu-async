package com.oneinstep.haidu.core;

import com.alibaba.fastjson.JSON;
import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.result.Result;
import org.slf4j.Logger;

import java.util.function.Consumer;

public abstract class AbstractTask<T> implements Consumer<RequestContext> {

    protected String taskId;
    protected abstract Result<T> invoke(RequestContext requestContext);
    protected abstract void beforeInvoke(RequestContext requestContext);
    protected abstract void afterInvoke(RequestContext requestContext);
    protected abstract Logger getLogger();
    protected boolean checkResult(RequestContext requestContext, Result<T> result) {
        return true;
    };

    @Override
    public void accept(RequestContext requestContext) {
        beforeInvoke(requestContext);
        Result<T> result = invoke(requestContext);
        getLogger().info("the Result of taskId:{} -> {}", getTaskId(), JSON.toJSONString(result));
        if (checkResult(requestContext, result)) {
            requestContext.getTaskResultMap().putIfAbsent(getTaskId(), result);
            afterInvoke(requestContext);
        } else {
            getLogger().warn("Result of taskId;{} check invalid.", getTaskId());
        }
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}

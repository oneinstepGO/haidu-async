package com.oneinstep.haidu.exception;

import com.oneinstep.haidu.context.RequestContext;

public interface ExceptionHandler {
    void handle(String taskId, Throwable e, RequestContext context);
}
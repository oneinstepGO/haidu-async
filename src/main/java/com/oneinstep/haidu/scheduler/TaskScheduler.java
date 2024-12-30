package com.oneinstep.haidu.scheduler;

import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.core.AbstractTask;

// 支持自定义任务调度策略
public interface TaskScheduler {
    void schedule(AbstractTask<?> task, RequestContext context);
}
package com.oneinstep.haidu.core;

import com.alibaba.fastjson2.JSON;
import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.result.Result;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractTask<T> implements Consumer<RequestContext> {

    // 任务ID
    protected String taskId;
    // 任务参数
    protected Map<String, Object> params = new HashMap<>();
    // 任务重试次数
    protected Integer retries = 0; // default retries is 0
    // 任务超时时间
    protected Long timeout = 1000L; // default timeout is 1000ms

    /**
     * 任务执行逻辑
     *
     * @param requestContext 请求上下文
     * @return 任务执行结果
     */
    protected abstract Result<T> invoke(RequestContext requestContext);

    /**
     * 任务执行前置处理
     *
     * @param requestContext 请求上下文
     */
    protected abstract void beforeInvoke(RequestContext requestContext);

    /**
     * 任务执行后置处理
     *
     * @param requestContext 请求上下文
     */
    protected abstract void afterInvoke(RequestContext requestContext);

    /**
     * 获取日志记录器
     *
     * @return 日志记录器
     */
    protected abstract Logger getLogger();

    /**
     * 检查任务执行结果
     *
     * @param requestContext 请求上下文
     * @param result         任务执行结果
     * @return 是否通过检查
     */
    protected boolean checkResult(RequestContext requestContext, Result<T> result) {
        return true;
    }

    /**
     * 任务执行异常处理
     *
     * @param requestContext 请求上下文
     * @param e              异常
     */
    protected void whenException(RequestContext requestContext, Exception e) {
        getLogger().error("taskId:{} invoke exception.", getTaskId(), e);
        throw new RuntimeException(e);
    }

    @Override
    public void accept(RequestContext requestContext) {
        int attempts = 0;
        boolean success = false;
        long startTime = System.currentTimeMillis();

        // 处理任务参数，TYPE 为 CONTEXT
        getParams().forEach((key, value) -> {
            if (value instanceof String && ((String) value).startsWith("#(") && ((String) value).endsWith(")#")) {
                getParams().put(key, requestContext.getRequestParam().get(((String) value).substring(2, ((String) value).length() - 2)));
            }
        });

        while (attempts <= getRetries() && !success) {
            try {
                beforeInvoke(requestContext);
                Result<T> result = invoke(requestContext);

                if (isTimeout(startTime)) {
                    getLogger().warn("taskId:{} invoke timeout.", getTaskId());
                    break;
                }

                success = checkAndPutResult(requestContext, result);

            } catch (Exception e) {
                handleException(requestContext, e, attempts);
            }
            attempts++;
        }
    }

    private void handleException(RequestContext requestContext, Exception e, int attempts) {
        if (attempts == getRetries()) {
            whenException(requestContext, e);
        } else {
            getLogger().warn("taskId:{} invoke exception, retrying... Attempt: {}", getTaskId(), attempts + 1, e);
        }
    }

    private boolean isTimeout(long startTime) {
        return Optional.ofNullable(getTimeout()).orElse(1000L) > 0
                && (System.currentTimeMillis() - startTime) > getTimeout();
    }

    private boolean checkAndPutResult(RequestContext requestContext, Result<T> result) {
        getLogger().info("The Result of taskId:{} -> {}", getTaskId(), JSON.toJSONString(result));
        if (checkResult(requestContext, result)) {
            requestContext.getTaskResultMap().putIfAbsent(getTaskId(), result);
            afterInvoke(requestContext);
            return true;
        } else {
            getLogger().warn("Result of taskId:{} check invalid.", getTaskId());
            return false;
        }
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setRetryTimes(Integer retries) {
        if (retries != null && (retries < 0 || retries > 10)) {
            throw new IllegalArgumentException("Retries must be between 0 and 10.");
        }
        this.retries = retries;
    }

    public void setTimeout(Long timeout) {
        if (timeout != null && (timeout < 0 || timeout > 10000)) {
            throw new IllegalArgumentException("Timeout must be between 0 and 10000.");
        }
        this.timeout = timeout;
    }

    public void setParams(Map<String, Object> params) {
        if (params != null) {
            this.params = params;
        }
    }

    public Integer getRetries() {
        return Optional.ofNullable(this.retries).orElse(0);
    }

    public Long getTimeout() {
        return Optional.ofNullable(this.timeout).orElse(1000L);
    }

    public Map<String, Object> getParams() {
        return Optional.ofNullable(this.params).orElse(new HashMap<>());
    }
}

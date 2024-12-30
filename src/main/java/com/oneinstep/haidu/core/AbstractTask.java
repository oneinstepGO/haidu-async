package com.oneinstep.haidu.core;

import com.alibaba.fastjson2.JSON;
import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.result.Result;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 抽象任务类，定义任务的基本属性和方法
 */
public abstract class AbstractTask<T> implements Consumer<RequestContext> {

    // 任务ID
    @Setter
    @Getter
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
     * 任务执行错误处理
     *
     * @param context 请求上下文
     * @param e       异常
     */
    protected void onError(RequestContext context, Throwable e) {
        getLogger().error("taskId:{} invoke error.", getTaskId(), e);
        throw new RuntimeException(e);
    }

    /**
     * 任务执行超时处理
     *
     * @param context 请求上下文
     */
    protected void onTimeout(RequestContext context) {
        getLogger().warn("taskId:{} invoke timeout.", getTaskId());
    }

    /**
     * 任务执行取消处理
     *
     * @param context 请求上下文
     */
    protected void onCancel(RequestContext context) {
        getLogger().warn("taskId:{} invoke cancel.", getTaskId());
    }

    @Override
    public final Consumer<RequestContext> andThen(Consumer<? super RequestContext> after) {
        return Consumer.super.andThen(after);
    }

    @Override
    public final void accept(RequestContext requestContext) {
        int attempts = 0;
        boolean success = false;
        long startTime = System.currentTimeMillis();

        // 处理任务参数，TYPE 为 CONTEXT
        getParams().forEach((key, value) -> {
            if (value instanceof String str && str.startsWith("#(") && str.endsWith(")#")) {
                getParams().put(key, requestContext.getRequestParam()
                        .get(str.substring(2, str.length() - 2)));
            }
        });

        // 重试机制
        while (attempts <= getRetries() && !success) {
            try {
                // 任务执行前置处理
                beforeInvoke(requestContext);
                // 任务执行
                Result<T> result = invoke(requestContext);
                // 检查任务是否超时
                if (isTimeout(startTime)) {
                    onTimeout(requestContext);
                    break;
                }
                // 检查任务执行结果并存储
                success = checkAndPutResult(requestContext, result);

            } catch (Exception e) {
                // 处理任务执行异常
                onError(requestContext, e);
            }
            // 重试次数加1
            attempts++;
        }
    }

    /**
     * 检查任务是否超时
     *
     * @param startTime 任务开始时间
     * @return 是否超时
     */
    private boolean isTimeout(long startTime) {
        return Optional.ofNullable(getTimeout()).orElse(1000L) > 0
                && (System.currentTimeMillis() - startTime) > getTimeout();
    }

    /**
     * 检查任务执行结果并存储
     *
     * @param requestContext 请求上下文
     * @param result         任务执行结果
     * @return 是否成功
     */
    private boolean checkAndPutResult(RequestContext requestContext, Result<T> result) {
        if (getLogger().isInfoEnabled()) {
            getLogger().info("The Result of taskId:{} -> {}", getTaskId(), JSON.toJSONString(result));
        }
        if (checkResult(requestContext, result)) {
            requestContext.getTaskResultMap().putIfAbsent(getTaskId(), result);
            afterInvoke(requestContext);
            return true;
        } else {
            getLogger().warn("Result of taskId:{} check invalid.", getTaskId());
            return false;
        }
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

    /**
     * 获取任务重试次数
     *
     * @return 任务重试次数
     */
    public Integer getRetries() {
        return Optional.ofNullable(this.retries).orElse(0);
    }

    /**
     * 获取任务超时时间
     *
     * @return 任务超时时间
     */
    public Long getTimeout() {
        return Optional.ofNullable(this.timeout).orElse(1000L);
    }

    /**
     * 获取任务参数
     *
     * @return 任务参数
     */
    public Map<String, Object> getParams() {
        return Optional.ofNullable(this.params).orElse(new HashMap<>());
    }
}

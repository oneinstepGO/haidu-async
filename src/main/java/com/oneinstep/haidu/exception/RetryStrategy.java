package com.oneinstep.haidu.exception;

// 支持自定义异常重试策略
public interface RetryStrategy {
    boolean shouldRetry(Throwable e, int attempts);

    long getDelayMillis(int attempts);
}

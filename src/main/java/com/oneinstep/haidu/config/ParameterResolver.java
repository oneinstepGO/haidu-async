package com.oneinstep.haidu.config;

import com.oneinstep.haidu.context.RequestContext;

// 支持自定义参数解析器
public interface ParameterResolver<T> {
    T resolve(String value, RequestContext context);
}

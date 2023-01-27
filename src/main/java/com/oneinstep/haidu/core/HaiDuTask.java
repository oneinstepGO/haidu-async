package com.oneinstep.haidu.core;

@FunctionalInterface
public interface HaiDuTask<P, R> {
    Void exec(P param);
}

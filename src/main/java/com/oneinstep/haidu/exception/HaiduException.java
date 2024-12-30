package com.oneinstep.haidu.exception;

/**
 * haidu 异常基类
 */
public class HaiduException extends RuntimeException {

    public HaiduException(String message) {
        super(message);
    }

    public HaiduException(Throwable cause) {
        super(cause);
    }

    public HaiduException(String message, Throwable t) {
        super(message, t);
    }

}

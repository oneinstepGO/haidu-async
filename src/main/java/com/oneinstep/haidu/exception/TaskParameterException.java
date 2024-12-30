package com.oneinstep.haidu.exception;

public class TaskParameterException extends HaiduException {

    public TaskParameterException(String message) {
        super(message);
    }

    public TaskParameterException(String message, Throwable cause) {
        super(message + ": " + cause.getMessage());
    }
} 
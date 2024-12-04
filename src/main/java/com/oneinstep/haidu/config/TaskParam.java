package com.oneinstep.haidu.config;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TaskParam implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务参数名称
     */
    private String name;
    /**
     * 任务参数类型
     */
    private Type type;
    /**
     * 任务参数值
     */
    private String value;
    /**
     * 任务参数 是否必填
     */
    private Boolean required;
    /**
     * 任务参数描述
     */
    private String description;


    public enum Type {
        STRING,
        INT,
        LONG,
        DOUBLE,
        BOOLEAN,
        LIST, // split by ','
        MAP, // split by ','   key:value
        JSON, // JSON格式
        JSON_ARRAY, // JSON数组格式
        CMS, // 从CMS中获取
        CONTEXT // 从请求上下文中获取
    }
}

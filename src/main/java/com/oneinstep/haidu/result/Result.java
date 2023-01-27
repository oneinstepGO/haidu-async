package com.oneinstep.haidu.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private String code;
    private String msg;
    private T data;

    public static <T> Result success(final T data) {
        Result result = new Result();
        result.setCode("200");
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    public static <T> Result fail(final T data) {
        Result result = new Result();
        result.setCode("-1");
        result.setMsg("fail");
        return result;
    }

    public boolean success() {
        return "200".equals(code);
    }

}

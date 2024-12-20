package com.oneinstep.haidu.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @param <T>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private String code;
    private String msg;
    private T data;

    public static <T> Result<T> success(final T data) {
        Result<T> result = new Result<>();
        result.setCode("200");
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    public static Result<Void> fail() {
        return fail("-1", "ERROR");
    }

    public static Result<Void> fail(String code, String msg) {
        Result<Void> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public boolean success() {
        return "200".equals(code);
    }

}

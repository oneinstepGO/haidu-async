package com.oneinstep.haidu.task;

import com.oneinstep.haidu.context.RequestContext;
import com.oneinstep.haidu.core.AbstractTask;
import com.oneinstep.haidu.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.Map;
import java.util.StringJoiner;

@Slf4j
public class ParameterTestTask extends AbstractTask<String> {

    @Override
    protected Result<String> invoke(RequestContext requestContext) {

        Map<String, Object> taskParams = getParams();
        String stringParam = (String) taskParams.get("stringParam");
        Integer numberParam = (Integer) taskParams.get("numberParam");
        Object contextParam = taskParams.get("contextParam");

        StringJoiner joiner = new StringJoiner("-");
        joiner.add(stringParam);
        joiner.add(numberParam.toString());
        joiner.add(contextParam.toString());
        return Result.success(joiner.toString());
    }

    @Override
    protected void beforeInvoke(RequestContext requestContext) {

    }

    @Override
    protected void afterInvoke(RequestContext requestContext) {

    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
